package com.example.apple.glidetest.media

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import com.example.apple.glidetest.R
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.slide_view.view.*

/**
 * Created by Apple on 17/9/14.
 */

class SlideView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        LinearLayout(context, attrs, defStyleAttr), Animator.AnimatorListener {

    private var downX = 0f
    private var lastX = 0f
    private var leftPos = 0
    private var rightPos = 0
    private var halfRedWidth = 0
    private var view: View? = null
    var isRedLeft: Boolean = true

    init {
        view = LayoutInflater.from(context).inflate(R.layout.slide_view, this)
        switchStatus()
        tvCamera.setOnClickListener {

        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthTv = view!!.tvCamera.width
        leftPos = view!!.tvCamera.left + widthTv / 2
        rightPos = view!!.tvVideo.left + widthTv / 2
        halfRedWidth = view!!.viewRed.width / 2
//        Logger.e(leftPos.toString() + "------->" + rightPos.toString() + "---->" + halfRedWidth)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.rawX
                lastX = downX
            }
            MotionEvent.ACTION_MOVE -> {
                val offsetX = event.rawX - lastX
                if ((offsetX < 0 && isRedLeft) || (offsetX > 0 && !isRedLeft)) {
                    move(offsetX.toInt())
                    lastX = event.rawX
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val redCenterX = view!!.viewRed.left + halfRedWidth / 2
                if (redCenterX > (leftPos + rightPos) / 2) {
                    startAnim(redCenterX, rightPos)
                    isRedLeft = false
                } else {
                    startAnim(redCenterX, leftPos)
                    isRedLeft = true
                }
            }
        }
        return true
    }

    private fun startAnim(startX: Int, endX: Int) {
        Logger.e(startX.toString() + "------->" + endX)
        val animator = ObjectAnimator.ofInt(viewRed, "translationX", startX, endX)
        val duration = Math.abs(endX - startX) / (rightPos - leftPos) * 300L
        animator.duration = duration
        animator.start()
        animator.addListener(this)
    }

    private fun move(offsetX: Int) {
        Logger.e("move---->" + offsetX)
        var offset = offsetX
        val redCenterPos = view!!.viewRed.left + halfRedWidth
        if (redCenterPos > leftPos && redCenterPos < rightPos) {
            if (redCenterPos + offsetX < leftPos)
                offset = leftPos - redCenterPos
            if (redCenterPos + offsetX > rightPos)
                offset = rightPos - redCenterPos
            view!!.viewRed.layout(view!!.viewRed.left + offset, view!!.viewRed.top, view!!.viewRed.bottom, view!!.viewRed.right + offset)
            view!!.viewRed.invalidate()
        }
    }

    override fun onAnimationEnd(animation: Animator?) {
        Logger.e("onAnimationEnd-------")
        switchStatus()
    }

    private fun switchStatus() {
        tvCamera.paint.textSize = selectedDimen(isRedLeft)
        tvVideo.paint.textSize = selectedDimen(!isRedLeft)
        tvCamera.setTextColor(selectedColor(isRedLeft))
        tvVideo.setTextColor(selectedColor(!isRedLeft))
        tvCamera.invalidate()
        tvVideo.invalidate()
    }

    override fun onAnimationStart(animation: Animator?) {
    }

    override fun onAnimationRepeat(animation: Animator?) {
    }

    override fun onAnimationCancel(animation: Animator?) {
    }

    private fun selectedDimen(isSelected: Boolean): Float {
        return if (isSelected) resources.getDimension(R.dimen.sp_16) else resources.getDimension(R.dimen.sp_13)
    }

    private fun selectedColor(isSelected: Boolean): Int {
        return if (isSelected) ContextCompat.getColor(context, R.color.colore93a3a)
        else Color.WHITE
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

    }
}
