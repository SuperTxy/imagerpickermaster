package com.example.apple.glidetest.media

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.view.MotionEvent
import android.view.View
import com.example.apple.glidetest.R
import com.example.apple.glidetest.RecordMediaActivity
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.slide_view.view.*

/**
 * Created by Apple on 17/9/14.
 */

class SlideHolder(private var view: View) : Animator.AnimatorListener, View.OnTouchListener {

    private var downX = 0f
    private var lastX = 0f
    private var leftPos = 0
    private var rightPos = 0
    private var halfRedWidth = 0
    var isRedLeft: Boolean = true

    init {
        view.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (leftPos == 0) {
                val widthTv = view.tvCamera.width
                leftPos = view.tvCamera.left + widthTv / 2
                rightPos = view.tvVideo.left + widthTv / 2
                halfRedWidth = view.viewRed.width / 2
                Logger.e(leftPos.toString() + "------->" + rightPos.toString() + "---->" + halfRedWidth)
                switchStatus()
            }
        }
        view.tvCamera.setOnClickListener {
            if (!isRedLeft) {
                startAnim(rightPos, leftPos)
                isRedLeft = true
                switchStatus()
                if (view.context is RecordMediaActivity){
                    (view.context  as RecordMediaActivity).changeMediaType(isRedLeft)
                }
            }
        }
        view.tvVideo.setOnClickListener {
            if (isRedLeft) {
                startAnim(leftPos, rightPos)
                isRedLeft = false
                switchStatus()
                if (view.context is RecordMediaActivity){
                    (view.context  as RecordMediaActivity).changeMediaType(isRedLeft)
                }
            }
        }
        view.llSlide.setOnTouchListener(this)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.rawX
                lastX = downX
                Logger.d("ACTION_DOWN" + downX)
            }
            MotionEvent.ACTION_MOVE -> {
                val offsetX = lastX - event.rawX
                if ((offsetX < 0 && !isRedLeft) || (offsetX > 0 && isRedLeft)) {
                    move(offsetX.toInt())
                    lastX = event.rawX
                    Logger.d("ACTION_MOVE" + offsetX)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val redCenterX = view.viewRed.left + halfRedWidth / 2
                if (redCenterX > (leftPos + rightPos) / 2) {
                    startAnim(redCenterX, rightPos)
                    isRedLeft = false
                } else {
                    startAnim(redCenterX, leftPos)
                    isRedLeft = true
                }
                switchStatus()
                Logger.d("ACTION_UP" + isRedLeft)
            }
        }
        return true
    }

    private fun startAnim(startX: Int, endX: Int) {
        Logger.e(startX.toString() + "------->" + endX)
        val animator = ObjectAnimator.ofInt(view.viewRed, "translationX", startX, endX)
        val duration = Math.abs(endX - startX) / (rightPos - leftPos) * 200L
        animator.duration = duration
        Logger.e(duration.toString())
        animator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator?) {
                Logger.e(animation!!.animatedValue.toString())
            }

        })
        animator.start()
    }

    private fun move(offsetX: Int) {
        var offset = offsetX
        val redCenterPos = view.viewRed.left + halfRedWidth
        if (redCenterPos >= leftPos && redCenterPos <= rightPos) {
            Logger.e("开始move了")
            if (redCenterPos + offsetX < leftPos)
                offset = leftPos - redCenterPos
            if (redCenterPos + offsetX > rightPos)
                offset = rightPos - redCenterPos
            view.viewRed.layout(view.viewRed.left + offset, view.viewRed.top, view.viewRed.bottom, view.viewRed.right + offset)
            view.viewRed.requestLayout()
        }
    }

    override fun onAnimationEnd(animation: Animator?) {
        switchStatus()
    }

    fun switchStatus() {
        view.tvCamera.visibility = View.VISIBLE
        view.tvVideo.visibility = View.VISIBLE
        view.tvCamera.paint.textSize = selectedDimen(isRedLeft)
        view.tvVideo.paint.textSize = selectedDimen(!isRedLeft)
        view.tvCamera.setTextColor(selectedColor(isRedLeft))
        view.tvVideo.setTextColor(selectedColor(!isRedLeft))
        view.tvCamera.invalidate()
        view.tvVideo.invalidate()
        val pos = if (isRedLeft) leftPos else rightPos
        view.viewRed.layout(pos - halfRedWidth, view.viewRed.top, pos + halfRedWidth, view.viewRed.bottom)
        view.viewRed.visibility = View.VISIBLE
    }

    override fun onAnimationStart(animation: Animator?) {
        Logger.e("onAnimationStart")
    }

    override fun onAnimationRepeat(animation: Animator?) {
        Logger.e("onAnimationRepeat")
    }

    override fun onAnimationCancel(animation: Animator?) {
        Logger.e("onAnimationCancel")

    }

    private fun selectedDimen(isSelected: Boolean): Float {
        val resources = view.context.resources
        return if (isSelected) resources.getDimension(R.dimen.sp_16) else resources.getDimension(R.dimen.sp_13)
    }

    private fun selectedColor(isSelected: Boolean): Int {
        return if (isSelected) ContextCompat.getColor(view.context, R.color.colore93a3a)
        else Color.WHITE
    }

    fun finish() {
        view.viewRed.visibility = View.INVISIBLE
        view.tvCamera.visibility = if (isRedLeft) View.VISIBLE else View.GONE
        view.tvVideo.visibility = if (!isRedLeft) View.VISIBLE else View.GONE
        val tv = if (isRedLeft) view.tvCamera else view.tvVideo
        tv.setTextColor(Color.WHITE)
    }
}
