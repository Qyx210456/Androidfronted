package com.example.androidfronted.security;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
 *  - 统一返回键拦截与 release 释放，防止内存泄漏。
 *
 */
public final class SecureKeyboardManager {

    private final SafeKeyboard safeKeyboard;

    private SecureKeyboardManager(SafeKeyboard safeKeyboard) {
        this.safeKeyboard = safeKeyboard;
    }

    /**
     * 创建并挂载安全键盘
     *
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
        // 自定义配色（深蓝灰主题），对应 res/layout/layout_keyboard_container.xml 覆盖布局：
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
        return new SecureKeyboardManager(keyboard);
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

    /** 页面销毁时释放键盘资源 */
    public void release() {
        safeKeyboard.release();
    }
}
