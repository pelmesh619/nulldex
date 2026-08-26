package org.pelmeshke.nulldex.ui.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import androidx.cardview.widget.CardView
import kotlin.math.abs

class PokemonCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    fun interface SwipeDismissListener {
        fun onSwipeDismiss(direction: Int)
    }

    var swipeDismissListener: SwipeDismissListener? = null
    var swipeDismissEnabled: Boolean = true

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val minFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity.toFloat()

    private var downRawX = 0f
    private var downRawY = 0f
    private var dragging = false
    private var dismissed = false
    private var pastDismissThreshold = false
    private var velocityTracker: VelocityTracker? = null

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        // NestedScrollView would otherwise block a horizontal swipe-to-dismiss.
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!canDismiss()) {
            return super.onInterceptTouchEvent(ev)
        }
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downRawX = ev.rawX
                downRawY = ev.rawY
                dragging = false
                pastDismissThreshold = false
                obtainTracker().addMovement(ev)
            }
            MotionEvent.ACTION_MOVE -> {
                obtainTracker().addMovement(ev)
                val dx = ev.rawX - downRawX
                if (isHorizontalSwipe(dx, ev.rawY - downRawY)) {
                    dragging = true
                    parent?.requestDisallowInterceptTouchEvent(true)
                    applyDrag(dx)
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> recycleTracker()
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!canDismiss()) {
            return super.onTouchEvent(event)
        }
        obtainTracker().addMovement(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downRawX = event.rawX
                downRawY = event.rawY
                dragging = false
                pastDismissThreshold = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - downRawX
                val dy = event.rawY - downRawY
                if (!dragging && isHorizontalSwipe(dx, dy)) {
                    dragging = true
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
                if (dragging) {
                    applyDrag(dx)
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                recycleAndFinish()
                if (dragging) {
                    dragging = false
                    settleOrDismiss()
                    return true
                }
                performClick()
            }
            MotionEvent.ACTION_CANCEL -> {
                recycleTracker()
                if (dragging) {
                    dragging = false
                    snapBack()
                }
            }
        }
        return dragging || super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDetachedFromWindow() {
        animate().cancel()
        recycleTracker()
        super.onDetachedFromWindow()
    }

    private fun canDismiss(): Boolean = swipeDismissEnabled && swipeDismissListener != null && !dismissed

    private fun isHorizontalSwipe(dx: Float, dy: Float): Boolean {
        return abs(dx) > touchSlop && abs(dx) > abs(dy)
    }

    private fun dismissThreshold(): Float = width * 0.28f

    private fun applyDrag(dx: Float) {
        translationX = dx
        val progress = (abs(translationX) / (width.coerceAtLeast(1) * 0.5f)).coerceIn(0f, 1f)
        alpha = 1f - progress * 0.45f
        val scale = 1f - progress * 0.08f
        scaleX = scale
        scaleY = scale

        val crossed = abs(translationX) > dismissThreshold()
        if (crossed && !pastDismissThreshold) {
            performThresholdHaptic()
        }
        pastDismissThreshold = crossed
    }

    private fun performThresholdHaptic() {
        isHapticFeedbackEnabled = true
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE
        } else {
            HapticFeedbackConstants.CLOCK_TICK
        }
        performHapticFeedback(type)
    }

    private fun settleOrDismiss() {
        val vx = lastVelocityX
        val threshold = dismissThreshold()
        val flung = abs(vx) >= minFlingVelocity * 1.5f && sameSign(vx, translationX)
        if (abs(translationX) > threshold || flung) {
            dismissOffscreen(if (translationX >= 0f) 1 else -1)
        } else {
            snapBack()
        }
    }

    private fun dismissOffscreen(direction: Int) {
        if (dismissed) return
        dismissed = true
        val target = direction * width * 1.15f
        animate()
            .translationX(target)
            .alpha(0f)
            .scaleX(0.92f)
            .scaleY(0.92f)
            .setDuration(180)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction { swipeDismissListener?.onSwipeDismiss(direction) }
            .start()
    }

    private fun snapBack() {
        animate()
            .translationX(0f)
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(200)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private var lastVelocityX = 0f

    private fun recycleAndFinish() {
        velocityTracker?.computeCurrentVelocity(1000)
        lastVelocityX = velocityTracker?.xVelocity ?: 0f
        recycleTracker()
    }

    private fun obtainTracker(): VelocityTracker {
        return velocityTracker ?: VelocityTracker.obtain().also { velocityTracker = it }
    }

    private fun recycleTracker() {
        velocityTracker?.recycle()
        velocityTracker = null
    }

    private fun sameSign(a: Float, b: Float): Boolean = a == 0f || b == 0f || a > 0f == b > 0f
}
