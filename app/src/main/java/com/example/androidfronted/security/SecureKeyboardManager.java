package com.example.androidfronted.security;

import android.app.Activity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.example.androidfronted.R;
import com.valence.safe.keyboard.SafeKeyboard;
import com.valence.safe.keyboard.SafeKeyboardConfig;

/**
 * 安全键盘管理器 —— 对第三方库 SafeKeyboard（JitPack: com.github.SValence:SafeKeyboard:2.0，MIT）
 * 的统一封装。
 *
 * 安全能力（来自库本身）：
 *  - 输入框获得焦点时自动弹出应用内键盘，屏蔽系统输入法，杜绝第三方输入法记录键值；
 *  - 固定 QWERTY 键位（bind），与系统键盘习惯一致，降低输错概率；
 *  - 支持字母/数字/符号键盘切换，覆盖密码复杂度要求；
 *  - 焦点离开或点击"完成"自动收起。
 *
 * 本封装额外固定：
 *  - 关闭按键气泡预览（setForbidPreview），防止肩窥；
 *  - 键盘标题标注页面来源，便于测试定位；
 *  - 统一返回键拦截与 release 释放，防止内存泄漏；
 *  - 底部导航条适配：targetSdk 35+ 强制 edge-to-edge，键盘容器统一注入
 *    navigationBars 底部内边距，键盘不会被手势条/导航条遮挡；
 *  - 防截屏防录屏托管（B+C 组合策略，页面无需再自行开关 FLAG_SECURE）：
 *      B. 键盘联动：安全键盘弹起 → ScreenCaptureGuard.enable；收起 → disable
 *         （后台中被收起则保持防护，防止任务卡片泄漏）；
 *      C. 兜底：宿主进入 onStop（退后台/被其他页面完全覆盖）强制开启防护，
 *         任务卡片永远黑屏；窗口重新获得焦点时按键盘显示状态同步恢复。
 */
public final class SecureKeyboardManager {

    private final Activity host;
    private final SafeKeyboard safeKeyboard;
    private final LinearLayout keyboardPlace;

    /** 上一次键盘显示状态，用于布局事件差分 */
    private boolean lastKeyboardShown = false;
    /** 当前防护是否由本管理器开启（所有权跟踪，避免误清其他页面设置的 FLAG_SECURE） */
    private boolean guardOwnedByUs = false;
    private boolean released = false;

    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener;
    private ViewTreeObserver.OnWindowFocusChangeListener focusChangeListener;
    private LifecycleEventObserver lifecycleObserver;
    private Lifecycle hostLifecycle;

    private SecureKeyboardManager(Activity host,
                                  SafeKeyboard safeKeyboard,
                                  LinearLayout keyboardPlace) {
        this.host = host;
        this.safeKeyboard = safeKeyboard;
        this.keyboardPlace = keyboardPlace;
    }

    /**
     * 创建并挂载安全键盘
     */
    public static SecureKeyboardManager attach(@NonNull Activity host,
                                               @NonNull LinearLayout keyboardPlace,
                                               @NonNull View rootView,
                                               @NonNull View pushUpTarget,
                                               @Nullable String title) {
        SafeKeyboardConfig config = SafeKeyboardConfig.getDefaultConfig();
        if (title != null) {
            config.keyboardTitle = title;
        }
        // 自定义配色（浅色主题，深色变体见 values-night/colors.xml），
        // 对应 res/layout/layout_keyboard_container.xml 覆盖布局：
        //  - keyboardBgResId          键盘整体背景
        //  - keyboardTitleColor       顶部标题文字颜色
        //  - keyboardSpecialKeyBgResId 功能键（删除/大小写/键盘切换）背景
        //  - keyboardDoneImgLayoutResId 顶部右侧"完成"按钮容器背景
        // 普通按键背景与按键文字颜色在覆盖布局中通过 keyBackground / keyTextColor 定义
        config.keyboardBgResId = R.drawable.bg_safe_keyboard;
        config.keyboardTitleColor = R.color.safe_keyboard_title;
        config.keyboardSpecialKeyBgResId = R.drawable.bg_safe_keyboard_special_key;
        config.keyboardDoneImgLayoutResId = R.drawable.bg_safe_keyboard_done;
        SafeKeyboard keyboard = new SafeKeyboard(host, keyboardPlace, rootView, pushUpTarget, config);
        // 关闭按键气泡预览，防止旁人通过预览气泡看到按下的字符
        keyboard.setForbidPreview(true);
        SecureKeyboardManager manager = new SecureKeyboardManager(host, keyboard, keyboardPlace);
        manager.installNavigationBarInsetsFix();
        manager.installScreenCaptureGuard();
        return manager;
    }

    /**
     * 底部导航条适配：targetSdk 35+ 强制 edge-to-edge 后，键盘贴窗口底部，
     * 底部内边距：键盘背景延伸到手势条后方，按键内容整体抬升到导航条之上；
     * 浅色/深色主题均生效。
     */
    private void installNavigationBarInsetsFix() {
        // 键盘容器根布局由库在构造时填充进 keyboardPlace
        View containerRoot = keyboardPlace.getChildAt(0);
        if (containerRoot == null) {
            return;
        }
        final int originalPaddingBottom = containerRoot.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(containerRoot, (v, insets) -> {
            int bottom = insets.getInsets(
                    WindowInsetsCompat.Type.navigationBars()
                            | WindowInsetsCompat.Type.displayCutout()).bottom;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(),
                    originalPaddingBottom + bottom);
            return insets;
        });
        // 兜底：清掉外部容器可能的 padding，避免背景与键盘之间出现露底缝隙
        keyboardPlace.setPadding(0, 0, 0, 0);
    }

    /**
     * 防截屏防录屏托管（B+C 组合）：
     *  - 布局监听 + isShow() 差分监听键盘弹出/收起（库 2.0 无回调，只能监听容器变化）；
     *  - 宿主 onStop 强制开启（C 兜底，任务卡片黑屏）；
     *  - 窗口重新获得焦点时按键盘状态同步（覆盖"后台返回但键盘已收起"的场景）。
     */
    private void installScreenCaptureGuard() {
        keyboardLayoutListener = () -> onKeyboardVisibilityChanged(safeKeyboard.isShow());
        keyboardPlace.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);

        focusChangeListener = hasFocus -> {
            if (released || !hasFocus) {
                return;
            }
            // 窗口重新获得焦点（从后台/被覆盖页面返回）：按键盘状态同步防护
            syncGuardWithKeyboard();
        };
        host.getWindow().getDecorView().getViewTreeObserver()
                .addOnWindowFocusChangeListener(focusChangeListener);

        lifecycleObserver = (owner, event) -> {
            if (event == Lifecycle.Event.ON_STOP) {
                // C 兜底：离开前台（退后台/被其他页面覆盖）强制开启，任务卡片黑屏
                enableGuard();
            } else if (event == Lifecycle.Event.ON_DESTROY) {
                cleanupGuard();
            }
        };
        // 注意：Activity 静态类型上没有 getLifecycle()，须通过 LifecycleOwner 接口访问
        if (host instanceof LifecycleOwner) {
            hostLifecycle = ((LifecycleOwner) host).getLifecycle();
            hostLifecycle.addObserver(lifecycleObserver);
        }
    }

    private void onKeyboardVisibilityChanged(boolean shown) {
        if (released || shown == lastKeyboardShown) {
            return;
        }
        lastKeyboardShown = shown;
        if (shown) {
            // B：键盘弹起 → 开启防护（输密码期间整窗防截屏防录屏）
            enableGuard();
        } else if (host.getWindow().getDecorView().hasWindowFocus()) {
            // B：前台收起键盘 → 恢复可截屏；后台中被收起 → 保持防护不恢复
            disableGuard();
        }
    }

    private void syncGuardWithKeyboard() {
        boolean shown = safeKeyboard.isShow();
        if (shown == lastKeyboardShown) {
            return;
        }
        lastKeyboardShown = shown;
        if (shown) {
            enableGuard();
        } else {
            disableGuard();
        }
    }

    private void enableGuard() {
        guardOwnedByUs = true;
        ScreenCaptureGuard.enable(host);
    }

    private void disableGuard() {
        guardOwnedByUs = false;
        ScreenCaptureGuard.disable(host);
    }

    private void cleanupGuard() {
        if (keyboardLayoutListener != null) {
            keyboardPlace.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardLayoutListener);
            keyboardLayoutListener = null;
        }
        if (focusChangeListener != null) {
            host.getWindow().getDecorView().getViewTreeObserver()
                    .removeOnWindowFocusChangeListener(focusChangeListener);
            focusChangeListener = null;
        }
        if (lifecycleObserver != null) {
            if (hostLifecycle != null) {
                hostLifecycle.removeObserver(lifecycleObserver);
            }
            lifecycleObserver = null;
        }
    }

    /** 绑定输入框：弹出固定 QWERTY 键盘（字母/数字/符号可切换，键位不随机） */
    public SecureKeyboardManager bind(EditText editText) {
        safeKeyboard.putEditText(editText);
        return this;
    }

    /**
     * 返回键拦截：键盘正在显示时先收起键盘并消费本次返回事件。
     * 在 Activity#onBackPressed / Fragment 所在 Activity 返回处理中调用。
     *
     * @return true 表示键盘正在显示且已收起，返回事件已被消费
     */
    public boolean onBackPressed() {
        if (safeKeyboard.stillNeedOptManually(false)) {
            safeKeyboard.hideKeyboard();
            return true;
        }
        return false;
    }

    /** 页面销毁时释放键盘资源：先解除防护托管与监听，再释放库资源 */
    public void release() {
        if (released) {
            return;
        }
        released = true;
        cleanupGuard();
        if (guardOwnedByUs) {
            // 仅当防护由本管理器开启时才关闭，避免误清后续敏感页面设置的 FLAG_SECURE
            disableGuard();
        }
        safeKeyboard.release();
    }
}
