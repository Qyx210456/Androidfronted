package com.example.androidfronted.ui.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Outline
import android.graphics.Rect
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.androidfronted.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class FloatingBallView(context: Context) : FrameLayout(context) {

    companion object {
        private const val TAG = "FloatingBallView"
        private const val BALL_SIZE_DP = 56
        private const val TOUCH_SLOP_DP = 5
        private const val MARGIN_DP = 0
        private const val KEYBOARD_THRESHOLD_DP = 150
        private const val PREFS_NAME = "floating_ball_prefs"
        private const val KEY_BALL_X = "ball_x"
        private const val KEY_BALL_Y = "ball_y"
    }

    private val ballSize: Int = dpToPx(BALL_SIZE_DP)
    private val touchSlop: Int = dpToPx(TOUCH_SLOP_DP)
    private val margin: Int = dpToPx(MARGIN_DP)
    private val keyboardThreshold: Int = dpToPx(KEYBOARD_THRESHOLD_DP)

    private val ballImageView: ImageView
    private var isDragging: Boolean = false
    private var downRawX: Float = 0f
    private var downRawY: Float = 0f
    private var lastRawX: Float = 0f
    private var lastRawY: Float = 0f
    private var totalDistance: Float = 0f

    private var statusBarHeight: Int = 0
    private var navBarHeight: Int = 0
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

    private var snapAnimator: ValueAnimator? = null

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var onBallClickListener: (() -> Unit)? = null

    private var lastKeyboardCheckTime: Long = 0
    private val keyboardCheckInterval: Long = 200  // 200ms间隔检查

    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        // 限制检查频率，避免频繁调用
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastKeyboardCheckTime >= keyboardCheckInterval) {
            lastKeyboardCheckTime = currentTime
            checkKeyboardVisibility()
        }
    }

    private var isKeyboardVisible: Boolean = false
    private var isHiddenByKeyboard: Boolean = false

    init {
        ballImageView = ImageView(context).apply {
            setImageResource(R.drawable.ic_smart_customer_service_ball)
            scaleType = ImageView.ScaleType.FIT_CENTER
            background = ContextCompat.getDrawable(context, R.drawable.bg_smart_customer_service_ball)
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    view?.let {
                        outline?.setOval(0, 0, it.width, it.height)
                    }
                }
            }
            clipToOutline = true
            setPadding(dpToPx(8f), dpToPx(8f), dpToPx(8f), dpToPx(8f))
        }

        addView(ballImageView, LayoutParams(ballSize, ballSize).apply {
            gravity = Gravity.CENTER
        })

        layoutParams = LayoutParams(ballSize, ballSize).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        alpha = 0f
        initSystemBars()
    }

    private fun initSystemBars() {
        val displayMetrics = context.resources.displayMetrics
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels

        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = context.resources.getDimensionPixelSize(resourceId)
        }

        val navResourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (navResourceId > 0) {
            navBarHeight = context.resources.getDimensionPixelSize(navResourceId)
        }
    }

    fun setOnBallClickListener(listener: () -> Unit) {
        onBallClickListener = listener
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        restorePosition()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
        snapAnimator?.cancel()
        snapAnimator = null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downRawX = event.rawX
                downRawY = event.rawY
                lastRawX = event.rawX
                lastRawY = event.rawY
                totalDistance = 0f
                isDragging = false
                animatePress(true)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX - lastRawX
                val deltaY = event.rawY - lastRawY
                totalDistance += abs(deltaX) + abs(deltaY)

                if (totalDistance > touchSlop) {
                    isDragging = true
                }

                if (isDragging) {
                    val newX = x + deltaX
                    val newY = y + deltaY
                    updatePosition(newX, newY)
                }

                lastRawX = event.rawX
                lastRawY = event.rawY
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                animatePress(false)

                if (!isDragging && totalDistance <= touchSlop) {
                    onBallClickListener?.invoke()
                } else {
                    snapToEdge()
                    savePosition()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun updatePosition(newX: Float, newY: Float) {
        val minX = margin.toFloat()
        val maxX = (screenWidth - ballSize - margin).toFloat()
        val minY = (statusBarHeight + margin).toFloat()
        val maxY = (screenHeight - navBarHeight - ballSize - margin).toFloat()

        x = max(minX, min(maxX, newX))
        y = max(minY, min(maxY, newY))
    }

    private fun snapToEdge() {
        snapAnimator?.cancel()
        snapAnimator = null

        val currentX = x
        val centerX = screenWidth / 2f
        val targetX = if (currentX < centerX) {
            margin.toFloat()
        } else {
            (screenWidth - ballSize - margin).toFloat()
        }

        snapAnimator = ValueAnimator.ofFloat(currentX, targetX).apply {
            duration = 200
            interpolator = android.view.animation.DecelerateInterpolator()
            addUpdateListener { animation ->
                x = animation.animatedValue as Float
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    snapAnimator = null
                }
            })
            start()
        }
    }

    private fun animatePress(pressed: Boolean) {
        val targetScale = if (pressed) 0.9f else 1.0f
        animate()
            .scaleX(targetScale)
            .scaleY(targetScale)
            .setDuration(100)
            .start()
    }

    fun show() {
        if (isHiddenByKeyboard) return
        visibility = VISIBLE
        alpha = 1f
    }

    fun hide() {
        visibility = GONE
        alpha = 0f
    }

    private fun checkKeyboardVisibility() {
        val rect = Rect()
        getWindowVisibleDisplayFrame(rect)
        val availableHeight = rect.bottom - rect.top

        val isKeyboardNowVisible = screenHeight - availableHeight > keyboardThreshold

        if (isKeyboardNowVisible != isKeyboardVisible) {
            isKeyboardVisible = isKeyboardNowVisible

            if (isKeyboardNowVisible && visibility == VISIBLE) {
                isHiddenByKeyboard = true
                visibility = GONE
                alpha = 0f
            } else if (!isKeyboardNowVisible && isHiddenByKeyboard) {
                isHiddenByKeyboard = false
                if (visibility == VISIBLE || visibility == GONE) {
                    visibility = VISIBLE
                    alpha = 1f
                }
            }
        }
    }

    private var lastSaveX: Float = 0f
    private var lastSaveY: Float = 0f
    private var hasPendingSave: Boolean = false

    private fun savePosition() {
        // 只有位置发生明显变化时才保存，避免频繁IO
        if (abs(x - lastSaveX) < 5 && abs(y - lastSaveY) < 5) {
            return
        }

        lastSaveX = x
        lastSaveY = y

        // 使用异步保存，避免阻塞主线程
        if (!hasPendingSave) {
            hasPendingSave = true
            postDelayed({
                prefs.edit()
                    .putFloat(KEY_BALL_X, lastSaveX)
                    .putFloat(KEY_BALL_Y, lastSaveY)
                    .apply()
                hasPendingSave = false
            }, 500)  // 延迟500ms保存，减少频繁保存
        }
    }

    private fun restorePosition() {
        val savedX = prefs.getFloat(KEY_BALL_X, -1f)
        val savedY = prefs.getFloat(KEY_BALL_Y, -1f)

        if (savedX >= 0 && savedY >= 0) {
            x = savedX
            y = savedY
        } else {
            x = (screenWidth - ballSize - margin).toFloat()
            y = (screenHeight / 2 - ballSize / 2).toFloat()
        }
    }

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}
