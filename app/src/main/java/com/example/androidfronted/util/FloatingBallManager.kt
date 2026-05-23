package com.example.androidfronted.util

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.androidfronted.R
import com.example.androidfronted.ui.*
import com.example.androidfronted.ui.loan.*
import com.example.androidfronted.ui.notification.NotificationBusinessDetailFragment
import com.example.androidfronted.ui.personalinformationinfo.*
import com.example.androidfronted.ui.settings.SettingsFragment
import com.example.androidfronted.ui.smartcustomerservice.SmartCustomerServiceFragment
import com.example.androidfronted.ui.widget.FloatingBallView

/**
 * 悬浮球管理器
 * 
 * 负责管理悬浮球的全局生命周期，包括：
 * - 根据Activity生命周期自动挂载/卸载悬浮球
 * - 根据Fragment黑名单自动显示/隐藏悬浮球
 * - 处理跨Activity导航到智能客服的逻辑
 * - 悬浮球位置的持久化存储
 */
class FloatingBallManager private constructor(private val app: Application) {

    companion object {
        private const val TAG = "FloatingBallManager"
        private const val EXTRA_NAVIGATE_TO_SMART_CUSTOMER_SERVICE = "navigate_to_smart_customer_service"

        @Volatile
        private var instance: FloatingBallManager? = null

        /**
         * 获取FloatingBallManager单例实例
         * @param app Application实例
         * @return FloatingBallManager单例
         */
        @JvmStatic
        fun getInstance(app: Application): FloatingBallManager {
            return instance ?: synchronized(this) {
                instance ?: FloatingBallManager(app).also { instance = it }
            }
        }
    }

    private var floatingBallView: FloatingBallView? = null
    private var currentActivity: Activity? = null
    private var currentBlacklistedFragment: Fragment? = null
    private var isBallEnabled: Boolean = true
    private var isBallAttached: Boolean = false

    /**
     * Activity黑名单
     * 在这些Activity中不显示悬浮球
     */
    private val activityBlackList: Set<Class<out Activity>> = setOf(
        LoginActivity::class.java,
        RegisterStep1Activity::class.java,
        RegisterStep2Activity::class.java,
        ImagePreviewActivity::class.java,
        ImageEditActivity::class.java,
        ProductApplyActivity::class.java,
        ProductApplySuccessActivity::class.java,
        NotificationCenterActivity::class.java,
        NotificationDetailActivity::class.java
    )

    /**
     * Fragment黑名单
     * 当这些Fragment显示时隐藏悬浮球
     */
    private val fragmentBlackList: Set<Class<out Fragment>> = setOf(
        VerifyCodeLoginFragment::class.java,
        PasswordLoginFragment::class.java,
        AvatarEditFragment::class.java,
        PaymentFragment::class.java,
        AccountPasswordFragment::class.java,
        UsernameEditFragment::class.java,
        PersonalInfoFragment::class.java,
        CertificateOfIdFragment::class.java,
        CertificateOfJobFragment::class.java,
        CertificateOfPropertyFragment::class.java,
        CertificateOfThirdPartyFragment::class.java,
        InfoConfirmSuccessFragment::class.java,
        InfoConfirmBankCardsSuccessFragment::class.java,
        PaymentSuccessFragment::class.java,
        SettingsFragment::class.java,
        SmartCustomerServiceFragment::class.java,
        ProfileFragment::class.java,
        NotificationBusinessDetailFragment::class.java,
        ApplicationRecordDetailFragment::class.java,
        LoanOrderDetailFragment::class.java,
        RepaymentPlanFragment::class.java,
        PersonalInformationFragment::class.java,
        MyBankCardsFragment::class.java,
        AccountSecurityFragment::class.java,
        ApplyDeferFragment::class.java,
        ApplyDeferSuccessFragment::class.java
    )

    /**
     * Activity生命周期回调
     * 监听所有Activity的创建、恢复、暂停、销毁事件
     */
    private val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            Log.d(TAG, "onActivityCreated: ${activity.javaClass.simpleName}")
            if (activity is FragmentActivity) {
                activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                    fragmentLifecycleCallbacks,
                    true
                )
            }
        }

        override fun onActivityStarted(activity: Activity) {
            Log.d(TAG, "onActivityStarted: ${activity.javaClass.simpleName}")
        }

        override fun onActivityResumed(activity: Activity) {
            Log.d(TAG, "onActivityResumed: ${activity.javaClass.simpleName}")
            currentActivity = activity
            evaluateAndAttach(activity)
        }

        override fun onActivityPaused(activity: Activity) {
            Log.d(TAG, "onActivityPaused: ${activity.javaClass.simpleName}")
            if (activity == currentActivity) {
                detachBall()
            }
        }

        override fun onActivityStopped(activity: Activity) {
            Log.d(TAG, "onActivityStopped: ${activity.javaClass.simpleName}")
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {
            Log.d(TAG, "onActivityDestroyed: ${activity.javaClass.simpleName}")
            if (activity is FragmentActivity) {
                activity.supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks)
            }
            if (activity == currentActivity) {
                currentActivity = null
            }
        }
    }

    /**
     * Fragment生命周期回调
     * 监听Fragment的显示、隐藏、恢复、暂停事件
     * 特别处理show/hide模式的Fragment切换
     */
    private val fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: android.content.Context) {
            Log.d(TAG, "onFragmentAttached: ${f.javaClass.simpleName}")
        }

        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
            Log.d(TAG, "onFragmentResumed: ${f.javaClass.simpleName}, isHidden=${f.isHidden}, isVisible=${f.isVisible}, userVisibleHint=${f.userVisibleHint}")
            // 对于 show/hide 模式，需要检查 Fragment 是否真正可见
            evaluateFragmentVisibility(f)
        }

        override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
            Log.d(TAG, "onFragmentPaused: ${f.javaClass.simpleName}, isHidden=${f.isHidden}")
            if (f == currentBlacklistedFragment) {
                currentBlacklistedFragment = null
                reevaluateTopFragment()
            }
        }
    }

    /**
     * 初始化悬浮球管理器
     * 注册Activity生命周期回调
     */
    fun init() {
        try {
            app.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
            Log.d(TAG, "FloatingBallManager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FloatingBallManager", e)
        }
    }

    /**
     * 当 Fragment 被显示时调用（用于 show/hide 模式）
     * MainActivity 应在 showMainFragment 中调用此方法
     * @param fragment 被显示的 Fragment
     */
    fun onFragmentShown(fragment: Fragment) {
        Log.d(TAG, "onFragmentShown: ${fragment.javaClass.simpleName}")
        evaluateFragmentVisibility(fragment)
    }

    /**
     * 检查Activity是否在黑名单中
     * @param activity 要检查的Activity
     * @return 是否在黑名单中
     */
    private fun isActivityBlacklisted(activity: Activity): Boolean {
        return activityBlackList.contains(activity.javaClass)
    }

    /**
     * 检查Fragment是否在黑名单中
     * @param fragment 要检查的Fragment
     * @return 是否在黑名单中
     */
    private fun isFragmentBlacklisted(fragment: Fragment): Boolean {
        return fragmentBlackList.contains(fragment.javaClass)
    }

    /**
     * 评估Fragment的可见性并决定是否显示/隐藏悬浮球
     * @param fragment 当前显示的Fragment
     */
    private fun evaluateFragmentVisibility(fragment: Fragment) {
        Log.d(TAG, "evaluateFragmentVisibility: ${fragment.javaClass.simpleName}, isHidden=${fragment.isHidden}, isVisible=${fragment.isVisible}, isAdded=${fragment.isAdded}, isBlacklisted=${isFragmentBlacklisted(fragment)}")
        
        if (fragment.isHidden) {
            Log.d(TAG, "Fragment is hidden, skipping")
            return
        }
        
        if (!fragment.isAdded) {
            Log.d(TAG, "Fragment is not added, skipping")
            return
        }

        if (fragment is com.google.android.material.bottomsheet.BottomSheetDialogFragment) {
            Log.d(TAG, "Fragment is BottomSheetDialogFragment, skipping")
            return
        }
        
        if (isFragmentBlacklisted(fragment)) {
            if (currentBlacklistedFragment != fragment) {
                Log.d(TAG, "Fragment ${fragment.javaClass.simpleName} is blacklisted, hiding ball")
                currentBlacklistedFragment = fragment
                hideBall()
            } else {
                Log.d(TAG, "Fragment ${fragment.javaClass.simpleName} is already currentBlacklistedFragment, skipping")
            }
        } else {
            Log.d(TAG, "Fragment ${fragment.javaClass.simpleName} is not blacklisted, showing ball")
            currentBlacklistedFragment = null
            showBall()
        }
    }

    /**
     * 重新评估当前顶部Fragment的状态
     * 用于Fragment切换后重新判断悬浮球显示状态
     */
    private fun reevaluateTopFragment() {
        val activity = currentActivity as? FragmentActivity ?: return
        
        val topFragment = getTopFragment(activity)
        Log.d(TAG, "reevaluateTopFragment: topFragment=${topFragment?.javaClass?.simpleName}")
        
        if (topFragment != null) {
            if (isFragmentBlacklisted(topFragment)) {
                currentBlacklistedFragment = topFragment
                hideBall()
            } else {
                showBall()
            }
        } else {
            showBall()
        }
    }

    /**
     * 评估Activity并挂载悬浮球
     * 检查Activity黑名单和当前顶部Fragment状态
     * @param activity 当前Activity
     */
    private fun evaluateAndAttach(activity: Activity) {
        try {
            Log.d(TAG, "evaluateAndAttach: ${activity.javaClass.simpleName}, isBallEnabled=$isBallEnabled")
            
            if (!isBallEnabled) {
                Log.d(TAG, "Ball is disabled, skipping attach")
                return
            }

            if (isActivityBlacklisted(activity)) {
                Log.d(TAG, "Activity ${activity.javaClass.simpleName} is blacklisted, skipping attach")
                return
            }

            attachBall(activity)

            if (activity is FragmentActivity) {
                val topFragment = getTopFragment(activity)
                Log.d(TAG, "Top fragment: ${topFragment?.javaClass?.simpleName}")
                
                if (topFragment != null && isFragmentBlacklisted(topFragment)) {
                    Log.d(TAG, "Top fragment ${topFragment.javaClass.simpleName} is blacklisted, hiding ball")
                    currentBlacklistedFragment = topFragment
                    hideBall()
                } else {
                    Log.d(TAG, "Top fragment is not blacklisted (or null), showing ball")
                    currentBlacklistedFragment = null
                    showBall()
                }
            } else {
                Log.d(TAG, "Activity is not FragmentActivity, showing ball")
                showBall()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in evaluateAndAttach", e)
        }
    }

    /**
     * 获取当前顶部可见的Fragment
     * 处理show/hide模式和replace模式
     * @param activity FragmentActivity实例
     * @return 顶部可见的Fragment，如果没有则返回null
     */
    private fun getTopFragment(activity: FragmentActivity): Fragment? {
        val fragments = activity.supportFragmentManager.fragments
        Log.d(TAG, "getTopFragment: fragments count=${fragments.size}")
        
        for (i in fragments.indices.reversed()) {
            val fragment = fragments[i]
            Log.d(TAG, "  Fragment[$i]: ${fragment?.javaClass?.simpleName}, isHidden=${fragment?.isHidden}, isVisible=${fragment?.isVisible}, isAdded=${fragment?.isAdded}")
            
            if (fragment != null && !fragment.isHidden && fragment.isAdded) {
                Log.d(TAG, "  -> Selected as top fragment")
                return fragment
            }
        }
        
        Log.d(TAG, "getTopFragment: No visible fragment found")
        return null
    }

    /**
     * 将悬浮球挂载到Activity的DecorView上
     * @param activity 目标Activity
     */
    private fun attachBall(activity: Activity) {
        try {
            Log.d(TAG, "attachBall: ${activity.javaClass.simpleName}")
            
            val decorView = activity.window.decorView as? ViewGroup
            if (decorView == null) {
                Log.e(TAG, "DecorView is null, cannot attach ball")
                return
            }

            // 检查当前悬浮球是否已经附加到这个 DecorView
            floatingBallView?.let { ball ->
                val parent = ball.parent
                if (parent === decorView) {
                    Log.d(TAG, "Ball already attached to this DecorView, reusing")
                    isBallAttached = true
                    return
                }
            }

            // 如果悬浮球有其他父容器，需要先移除
            // 但如果移除失败，直接创建新的悬浮球
            floatingBallView?.let { ball ->
                val parent = ball.parent
                if (parent != null && parent is ViewGroup) {
                    try {
                        Log.d(TAG, "Removing ball from previous parent: ${parent.javaClass.simpleName}")
                        parent.removeView(ball)
                        Log.d(TAG, "Ball removed successfully")
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to remove ball from parent, will create new one: ${e.message}")
                        // 移除失败，放弃旧的悬浮球，创建新的
                        floatingBallView = null
                        isBallAttached = false
                    }
                }
            }

            // 创建新的悬浮球（如果需要）
            if (floatingBallView == null) {
                Log.d(TAG, "Creating new FloatingBallView")
                floatingBallView = FloatingBallView(activity).apply {
                    setOnBallClickListener {
                        navigateToSmartCustomerService()
                    }
                }
            }

            // 添加到 DecorView
            floatingBallView?.let { ball ->
                if (ball.parent == null) {
                    Log.d(TAG, "Adding ball to DecorView")
                    decorView.addView(ball)
                    isBallAttached = true
                    Log.d(TAG, "Ball attached successfully")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error attaching ball", e)
            floatingBallView = null
            isBallAttached = false
        }
    }

    /**
     * 从当前Activity卸载悬浮球
     */
    private fun detachBall() {
        try {
            Log.d(TAG, "detachBall: isBallAttached=$isBallAttached")
            
            floatingBallView?.let { ball ->
                ball.fadeOut {
                    (ball.parent as? ViewGroup)?.removeView(ball)
                    isBallAttached = false
                    Log.d(TAG, "Ball detached")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error detaching ball", e)
        }
    }

    /**
     * 显示悬浮球
     * 带淡入动画
     */
    private fun showBall() {
        try {
            Log.d(TAG, "showBall: isBallAttached=$isBallAttached, floatingBallView=$floatingBallView")
            
            if (!isBallAttached) {
                Log.d(TAG, "Ball not attached, cannot show")
                return
            }
            
            floatingBallView?.show()
            Log.d(TAG, "Ball shown")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing ball", e)
        }
    }

    /**
     * 隐藏悬浮球
     * 带淡出动画
     */
    private fun hideBall() {
        try {
            Log.d(TAG, "hideBall: isBallAttached=$isBallAttached, floatingBallView=$floatingBallView")
            
            if (!isBallAttached) {
                Log.d(TAG, "Ball not attached, cannot hide")
                return
            }
            
            floatingBallView?.hide()
            Log.d(TAG, "Ball hidden")
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding ball", e)
        }
    }

    /**
     * 设置悬浮球是否启用
     * 可用于全局控制悬浮球的显示/隐藏
     * @param enabled 是否启用
     */
    fun setBallEnabled(enabled: Boolean) {
        Log.d(TAG, "setBallEnabled: $enabled")
        isBallEnabled = enabled
        if (!enabled) {
            detachBall()
        } else {
            currentActivity?.let { evaluateAndAttach(it) }
        }
    }

    /**
     * 临时隐藏悬浮球
     * 用于Dialog显示时隐藏悬浮球
     */
    fun temporarilyHideBall() {
        Log.d(TAG, "temporarilyHideBall")
        hideBall()
    }

    /**
     * 恢复悬浮球显示
     * 用于Dialog关闭后恢复悬浮球
     */
    fun restoreBall() {
        Log.d(TAG, "restoreBall")
        if (currentBlacklistedFragment == null) {
            showBall()
        }
    }

    /**
     * 导航到智能客服页面
     * 根据当前Activity类型选择不同的导航方式
     */
    private fun navigateToSmartCustomerService() {
        try {
            val activity = currentActivity
            Log.d(TAG, "navigateToSmartCustomerService: currentActivity=${activity?.javaClass?.simpleName}")
            
            if (activity == null) {
                Log.e(TAG, "Current activity is null, cannot navigate")
                return
            }

            when (activity) {
                is MainActivity -> {
                    Log.d(TAG, "Navigating within MainActivity")
                    val fragment = SmartCustomerServiceFragment.newInstance()
                    activity.supportFragmentManager.beginTransaction()
                        .replace(R.id.container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
                else -> {
                    Log.d(TAG, "Navigating from other Activity to MainActivity")
                    val intent = Intent(activity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra(EXTRA_NAVIGATE_TO_SMART_CUSTOMER_SERVICE, true)
                    }
                    activity.startActivity(intent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to smart customer service", e)
        }
    }

    /**
     * 处理MainActivity接收到的Intent
     * 用于从其他Activity跳转回来时导航到智能客服
     * @param activity MainActivity实例
     * @param intent 接收到的Intent
     */
    fun handleIntent(activity: MainActivity, intent: Intent) {
        Log.d(TAG, "handleIntent: hasExtra=${intent.getBooleanExtra(EXTRA_NAVIGATE_TO_SMART_CUSTOMER_SERVICE, false)}")
        
        if (intent.getBooleanExtra(EXTRA_NAVIGATE_TO_SMART_CUSTOMER_SERVICE, false)) {
            try {
                Log.d(TAG, "Handling smart customer service navigation intent")
                activity.supportFragmentManager.popBackStackImmediate(
                    null,
                    androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                val fragment = SmartCustomerServiceFragment.newInstance()
                activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit()
            } catch (e: Exception) {
                Log.e(TAG, "Error handling intent", e)
            }
        }
    }
}
