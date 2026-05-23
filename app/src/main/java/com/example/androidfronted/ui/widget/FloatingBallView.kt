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

/**
 * 悬浮球自定义View
 * 
 * 功能特性：
 * - 支持上下左右拖拽移动
 * - 点击进入智能客服
 * - 松手后边缘吸附动画
 * - 淡入淡出动画
 * - 位置持久化存储
 * - 键盘弹出时自动隐藏
 * - 安全区适配（避开状态栏和导航栏）
 */
class FloatingBallView(context: Context) : FrameLayout(context) {

    companion object {
        private const val TAG = "FloatingBallView"
        
        // 悬浮球尺寸（dp）- 略大于横向功能区图标
        private const val BALL_SIZE_DP = 56
        
        // 点击/拖拽判定阈值（dp）
        private const val TOUCH_SLOP_DP = 5
        
        // 边缘边距（dp）- 0 表示贴边
        private const val MARGIN_DP = 0
        
        // 淡入动画时长（ms）
        private const val FADE_IN_DURATION_MS = 150L
        
        // 淡出动画时长（ms）
        private const val FADE_OUT_DURATION_MS = 100L
        
        // 边缘吸附动画时长（ms）
        private const val SNAP_ANIMATION_DURATION_MS = 200L
        
        // 键盘弹出检测阈值（dp）
        private const val KEYBOARD_THRESHOLD_DP = 150
        
        // SharedPreferences配置
        private const val PREFS_NAME = "floating_ball_prefs"
        private const val KEY_BALL_X = "ball_x"
        private const val KEY_BALL_Y = "ball_y"
    }

    // 悬浮球尺寸（px）
    private val ballSize: Int = dpToPx(BALL_SIZE_DP)
    
    // 点击判定阈值（px）
    private val touchSlop: Int = dpToPx(TOUCH_SLOP_DP)
    
    // 边缘边距（px）
    private val margin: Int = dpToPx(MARGIN_DP)
    
    // 键盘检测阈值（px）
    private val keyboardThreshold: Int = dpToPx(KEYBOARD_THRESHOLD_DP)

    // 悬浮球图标ImageView
    private val ballImageView: ImageView
    
    // 是否正在拖拽
    private var isDragging: Boolean = false
    
    // 按下时的原始坐标
    private var downRawX: Float = 0f
    private var downRawY: Float = 0f
    
    // 上次触摸坐标
    private var lastRawX: Float = 0f
    private var lastRawY: Float = 0f
    
    // 总移动距离
    private var totalDistance: Float = 0f

    // 系统栏高度
    private var statusBarHeight: Int = 0
    private var navBarHeight: Int = 0
    
    // 屏幕尺寸
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

    // 动画对象
    private var snapAnimator: ValueAnimator? = null
    private var fadeAnimator: ValueAnimator? = null

    // 位置持久化存储
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // 点击监听器
    private var onBallClickListener: (() -> Unit)? = null

    // 全局布局监听器（用于检测键盘）
    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        checkKeyboardVisibility()
    }

    // 键盘状态
    private var isKeyboardVisible: Boolean = false
    private var isHiddenByKeyboard: Boolean = false

    init {
        Log.d(TAG, "Initializing FloatingBallView")
        
        // 创建悬浮球图标 - 同时作为背景和图标
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
        
        Log.d(TAG, "FloatingBallView initialized: ballSize=${ballSize}px, touchSlop=${touchSlop}px")
    }

    /**
     * 初始化系统栏高度信息
     * 获取状态栏和导航栏高度，用于安全区适配
     */
    private fun initSystemBars() {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
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
        
        Log.d(TAG, "System bars: screenWidth=$screenWidth, screenHeight=$screenHeight, statusBar=$statusBarHeight, navBar=$navBarHeight")
    }

    /**
     * 设置悬浮球点击监听器
     * @param listener 点击回调函数
     */
    fun setOnBallClickListener(listener: () -> Unit) {
        onBallClickListener = listener
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(TAG, "onAttachedToWindow")
        viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        restorePosition()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d(TAG, "onDetachedFromWindow")
        viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
        cancelAnimations()
    }

    /**
     * 处理触摸事件
     * 实现拖拽和点击判定
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "onTouchEvent: ACTION_DOWN at (${event.rawX}, ${event.rawY})")
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
                Log.d(TAG, "onTouchEvent: ACTION_UP, totalDistance=$totalDistance, isDragging=$isDragging")
                animatePress(false)

                if (!isDragging && totalDistance <= touchSlop) {
                    Log.d(TAG, "Detected click, invoking listener")
                    onBallClickListener?.invoke()
                } else {
                    Log.d(TAG, "Detected drag, snapping to edge")
                    snapToEdge()
                    savePosition()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * 更新悬浮球位置
     * 限制在安全区域内
     * @param newX 新的X坐标
     * @param newY 新的Y坐标
     */
    private fun updatePosition(newX: Float, newY: Float) {
        val minX = margin.toFloat()
        val maxX = (screenWidth - ballSize - margin).toFloat()
        val minY = (statusBarHeight + margin).toFloat()
        val maxY = (screenHeight - navBarHeight - ballSize - margin).toFloat()

        x = max(minX, min(maxX, newX))
        y = max(minY, min(maxY, newY))
    }

    /**
     * 执行边缘吸附动画
     * 松手后自动吸附到最近的左/右边缘
     */
    private fun snapToEdge() {
        cancelAnimations()

        val currentX = x
        val centerX = screenWidth / 2f
        val targetX = if (currentX < centerX) {
            margin.toFloat()
        } else {
            (screenWidth - ballSize - margin).toFloat()
        }

        Log.d(TAG, "snapToEdge: currentX=$currentX, targetX=$targetX")

        snapAnimator = ValueAnimator.ofFloat(currentX, targetX).apply {
            duration = SNAP_ANIMATION_DURATION_MS
            interpolator = android.view.animation.DecelerateInterpolator()
            addUpdateListener { animation ->
                x = animation.animatedValue as Float
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    snapAnimator = null
                    Log.d(TAG, "snapToEdge animation ended")
                }
            })
            start()
        }
    }

    /**
     * 执行按压缩放动画
     * @param pressed 是否按下状态
     */
    private fun animatePress(pressed: Boolean) {
        val targetScale = if (pressed) 0.9f else 1.0f
        animate()
            .scaleX(targetScale)
            .scaleY(targetScale)
            .setDuration(100)
            .start()
    }

    /**
     * 淡入动画
     * 将悬浮球从透明渐变为可见
     */
    fun fadeIn() {
        if (isHiddenByKeyboard) {
            Log.d(TAG, "fadeIn: skipped, hidden by keyboard")
            return
        }
        
        Log.d(TAG, "fadeIn: current alpha=$alpha")
        cancelFadeAnimation()
        fadeAnimator = ValueAnimator.ofFloat(alpha, 1f).apply {
            duration = FADE_IN_DURATION_MS
            addUpdateListener { animation ->
                alpha = animation.animatedValue as Float
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    fadeAnimator = null
                    Log.d(TAG, "fadeIn animation ended, alpha=$alpha")
                }
            })
            start()
        }
    }

    /**
     * 淡出动画
     * 将悬浮球从可见渐变为透明
     * @param callback 动画结束回调
     */
    fun fadeOut(callback: (() -> Unit)? = null) {
        Log.d(TAG, "fadeOut: current alpha=$alpha")
        cancelFadeAnimation()
        fadeAnimator = ValueAnimator.ofFloat(alpha, 0f).apply {
            duration = FADE_OUT_DURATION_MS
            addUpdateListener { animation ->
                alpha = animation.animatedValue as Float
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    fadeAnimator = null
                    Log.d(TAG, "fadeOut animation ended, alpha=$alpha")
                    callback?.invoke()
                }
            })
            start()
        }
    }

    /**
     * 显示悬浮球
     * 设置可见并执行淡入动画
     */
    fun show() {
        if (isHiddenByKeyboard) {
            Log.d(TAG, "show: skipped, hidden by keyboard")
            return
        }
        Log.d(TAG, "show: setting visibility to VISIBLE")
        visibility = VISIBLE
        fadeIn()
    }

    /**
     * 隐藏悬浮球
     * 执行淡出动画后设置为GONE
     * @param callback 动画结束回调
     */
    fun hide(callback: (() -> Unit)? = null) {
        Log.d(TAG, "hide: starting fade out")
        fadeOut {
            visibility = GONE
            Log.d(TAG, "hide: visibility set to GONE")
            callback?.invoke()
        }
    }

    /**
     * 检测键盘可见性
     * 当键盘弹出时自动隐藏悬浮球
     */
    private fun checkKeyboardVisibility() {
        val rect = Rect()
        getWindowVisibleDisplayFrame(rect)
        val availableHeight = rect.bottom - rect.top

        val isKeyboardNowVisible = screenHeight - availableHeight > keyboardThreshold

        if (isKeyboardNowVisible != isKeyboardVisible) {
            isKeyboardVisible = isKeyboardNowVisible
            Log.d(TAG, "checkKeyboardVisibility: keyboardVisible=$isKeyboardNowVisible")

            if (isKeyboardNowVisible && visibility == VISIBLE) {
                isHiddenByKeyboard = true
                fadeOut()
            } else if (!isKeyboardNowVisible && isHiddenByKeyboard) {
                isHiddenByKeyboard = false
                if (visibility == VISIBLE) {
                    fadeIn()
                }
            }
        }
    }

    /**
     * 保存悬浮球位置到SharedPreferences
     */
    private fun savePosition() {
        prefs.edit()
            .putFloat(KEY_BALL_X, x)
            .putFloat(KEY_BALL_Y, y)
            .apply()
        Log.d(TAG, "savePosition: x=$x, y=$y")
    }

    /**
     * 从SharedPreferences恢复悬浮球位置
     */
    private fun restorePosition() {
        val savedX = prefs.getFloat(KEY_BALL_X, -1f)
        val savedY = prefs.getFloat(KEY_BALL_Y, -1f)

        if (savedX >= 0 && savedY >= 0) {
            x = savedX
            y = savedY
            Log.d(TAG, "restorePosition: restored x=$x, y=$y")
        } else {
            x = (screenWidth - ballSize - margin).toFloat()
            y = (screenHeight / 2 - ballSize / 2).toFloat()
            Log.d(TAG, "restorePosition: using default position x=$x, y=$y")
        }
    }

    /**
     * 取消所有动画
     */
    private fun cancelAnimations() {
        snapAnimator?.cancel()
        snapAnimator = null
        cancelFadeAnimation()
    }

    /**
     * 取消淡入淡出动画
     */
    private fun cancelFadeAnimation() {
        fadeAnimator?.cancel()
        fadeAnimator = null
    }

    /**
     * dp转px工具方法
     * @param dp dp值
     * @return px值
     */
    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }

    /**
     * dp转px工具方法
     * @param dp dp值
     * @return px值
     */
    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}
