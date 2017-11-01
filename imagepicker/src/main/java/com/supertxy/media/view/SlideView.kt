package com.supertxy.media.view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.supertxy.media.R
import com.supertxy.media.RecordMediaActivity
import com.txy.androidutils.TxyScreenUtils
import kotlinx.android.synthetic.main.layout_slide_view.view.*

/**
 * Created by Apple on 17/10/31.
 */
class SlideView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attributeSet, defStyleAttr) {

    var isLeft: Boolean = true
    var isFinish: Boolean = false
        set(value) {
            field = value
            if (!field) {
                ll_slide.visibility = View.VISIBLE
                tv_center.visibility = View.GONE
                redDot.visibility = View.VISIBLE
                tvVideoHint.text = context.getString(R.string.press_to_record)
            }
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_slide_view, this)
        tv_record.setOnClickListener {
            if (isLeft) {
                switch()
            }
        }
        tv_camera.setOnClickListener {
            if (!isLeft) {
                switch()
            }
        }

    }

    fun switch() {
        if (isFinish) return
        val itemWidth = TxyScreenUtils.dp2px(context, 50).toFloat()
        isLeft = !isLeft
        tvVideoHint.visibility = if (isLeft) View.INVISIBLE else View.VISIBLE
        val start = if (isLeft) -itemWidth else 0f
        val end = if (isLeft) 0f else -itemWidth
        val animator = ObjectAnimator.ofFloat(ll_slide, "translationX", start, end)
        animator.duration = 200
        animator.start()
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                handleSelected()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }
        })
        if (context is RecordMediaActivity) {
            (context as RecordMediaActivity).changeMediaType(isLeft)
        }
    }

    fun finish() {
        isFinish = true
        ll_slide.visibility = View.GONE
        redDot.visibility = View.INVISIBLE
        tv_center.visibility = View.VISIBLE
        tv_center.text = if (isLeft) "拍照" else "摄像"
        tvVideoHint.text = context.getString(R.string.press_to_record_again)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val lp = ll_slide.layoutParams as LinearLayout.LayoutParams
            val screenWidth = TxyScreenUtils.getScreenWidth(context)
            lp.leftMargin = screenWidth / 2 - tv_camera.width / 2
            ll_slide.layoutParams = lp
        }
    }

    private fun handleSelected() {
        tv_camera.paint.textSize = selectedDimen(isLeft)
        tv_record.paint.textSize = selectedDimen(!isLeft)
        tv_camera.setTextColor(selectedColor(isLeft))
        tv_record.setTextColor(selectedColor(!isLeft))
    }

    private fun selectedDimen(isSelected: Boolean): Float {
        return if (isSelected) resources.getDimension(R.dimen.sp_16) else resources.getDimension(R.dimen.sp_13)
    }

    private fun selectedColor(isSelected: Boolean): Int {
        if (isSelected)
            return ContextCompat.getColor(context, R.color.colore93a3a)
        else return Color.WHITE
    }

}