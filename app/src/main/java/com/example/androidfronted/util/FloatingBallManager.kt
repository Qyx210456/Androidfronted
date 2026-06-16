package com.example.androidfronted.util

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
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

class FloatingBallManager private constructor(private val app: Application) {

    companion object {
        private const val TAG = "FloatingBallManager"
        const val EXTRA_FINISH_ON_BACK = "finish_on_back"

        @Volatile
        private var instance: FloatingBallManager? = null

        @JvmStatic
        fun getInstance(app: Application): FloatingBallManager {
            return instance ?: synchronized(this) {
                instance ?: FloatingBallManager(app).also { instance = it }
            }
        }
    }

    private var floatingBallView: FloatingBallView? = null
    private var currentActivity: Activity? = null
    private var isBallEnabled: Boolean = true

    private val activityBlackList: Set<Class<out Activity>> = setOf(
        LoginActivity::class.java,
        RegisterStep1Activity::class.java,
        RegisterStep2Activity::class.java,
        ImagePreviewActivity::class.java,
        ImageEditActivity::class.java,
        ProductApplyActivity::class.java,
        ProductApplySuccessActivity::class.java,
        NotificationCenterActivity::class.java,
        NotificationDetailActivity::class.java,
        SmartCustomerServiceActivity::class.java
    )

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
        ApplyDeferSuccessFragment::class.java,
        EarlyRepaymentFragment::class.java,
        EarlyRepaymentSuccessFragment::class.java
    )

    private val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (activity is FragmentActivity) {
                activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                    fragmentLifecycleCallbacks, true
                )
            }
        }

        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityResumed(activity: Activity) {
            currentActivity = activity
            updateBallVisibility(activity)
        }

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {
            if (activity is FragmentActivity) {
                activity.supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks)
            }
            if (activity == currentActivity) {
                currentActivity = null
            }
        }
    }

    private val fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
            updateBallVisibilityForFragment(f)
        }

        override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {}
    }

    fun init() {
        app.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    private fun isActivityBlacklisted(activity: Activity): Boolean {
        return activityBlackList.contains(activity.javaClass)
    }

    private fun isFragmentBlacklisted(fragment: Fragment): Boolean {
        return fragmentBlackList.contains(fragment.javaClass)
    }

    private fun updateBallVisibility(activity: Activity) {
        if (!isBallEnabled) {
            hideBall()
            return
        }

        if (isActivityBlacklisted(activity)) {
            hideBall()
            return
        }

        attachBallIfNeeded(activity)

        if (activity is FragmentActivity) {
            val topFragment = getTopFragment(activity)
            if (topFragment != null && isFragmentBlacklisted(topFragment)) {
                hideBall()
            } else {
                showBall()
            }
        } else {
            showBall()
        }
    }

    private fun updateBallVisibilityForFragment(fragment: Fragment) {
        if (fragment.isHidden || !fragment.isAdded) return
        if (fragment is com.google.android.material.bottomsheet.BottomSheetDialogFragment) return
        
        if (isFragmentBlacklisted(fragment)) {
            hideBall()
        } else {
            showBall()
        }
    }

    private fun getTopFragment(activity: FragmentActivity): Fragment? {
        val fragments = activity.supportFragmentManager.fragments
        for (i in fragments.indices.reversed()) {
            val fragment = fragments[i]
            if (fragment != null && !fragment.isHidden && fragment.isAdded) {
                return fragment
            }
        }
        return null
    }

    private fun attachBallIfNeeded(activity: Activity) {
        val decorView = activity.window.decorView as? ViewGroup ?: return

        floatingBallView?.let { ball ->
            val parent = ball.parent
            if (parent === decorView) {
                return
            }
            if (parent != null && parent is ViewGroup) {
                parent.removeView(ball)
            }
        }

        if (floatingBallView == null) {
            floatingBallView = FloatingBallView(activity).apply {
                setOnBallClickListener {
                    navigateToSmartCustomerService()
                }
            }
        }

        floatingBallView?.let { ball ->
            if (ball.parent == null) {
                decorView.addView(ball)
            }
        }
    }

    private fun showBall() {
        floatingBallView?.show()
    }

    private fun hideBall() {
        floatingBallView?.hide()
    }

    fun setBallEnabled(enabled: Boolean) {
        isBallEnabled = enabled
        currentActivity?.let { updateBallVisibility(it) }
    }

    fun temporarilyHideBall() {
        hideBall()
    }

    fun restoreBall() {
        currentActivity?.let { updateBallVisibility(it) }
    }

    fun onFragmentShown(fragment: Fragment) {
        updateBallVisibilityForFragment(fragment)
    }

    private fun navigateToSmartCustomerService() {
        val activity = currentActivity ?: return

        when (activity) {
            is MainActivity -> {
                val fragment = SmartCustomerServiceFragment.newInstance(false)
                activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
            else -> {
                val intent = Intent(activity, SmartCustomerServiceActivity::class.java)
                activity.startActivity(intent)
            }
        }
    }

    fun handleIntent(activity: MainActivity, intent: Intent) {
        if (intent.getBooleanExtra("navigate_to_smart_customer_service", false)) {
            val fragment = SmartCustomerServiceFragment.newInstance(false)
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}
