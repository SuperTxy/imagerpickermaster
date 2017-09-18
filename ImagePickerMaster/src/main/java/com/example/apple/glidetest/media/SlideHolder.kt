package com.example.apple.glidetest.media

import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import com.example.apple.glidetest.R
import com.example.apple.glidetest.RecordMediaActivity
import com.orhanobut.logger.Logger
import com.txy.androidutils.ScreenUtils
import kotlinx.android.synthetic.main.slide_view.view.*

/**
 * Created by Apple on 17/9/14.
 */

class SlideHolder(private var view: View) : View.OnTouchListener {

    private var downX = 0f
    var isRedLeft: Boolean = true

    init {
        switchStatus()
        view.tvCamera.setOnClickListener {
            if (!isRedLeft) {
                isRedLeft = true
                switchStatus()
                if (view.context is RecordMediaActivity) {
                    (view.context as RecordMediaActivity).changeMediaType(isRedLeft)
                }
            }
        }
        view.tvVideo.setOnClickListener {
            if (isRedLeft) {
                isRedLeft = false
                switchStatus()
                if (view.context is RecordMediaActivity) {
                    (view.context as RecordMediaActivity).changeMediaType(isRedLeft)
                }
            }
        }
        view.setOnTouchListener(this)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.rawX
                Logger.d("ACTION_DOWN" + downX)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                var upX = event.rawX
                if (upX - downX < 0) {
                    if (isRedLeft) {
                        isRedLeft = false
                        switchStatus()
                    }
                } else {
                    if (!isRedLeft) {
                        isRedLeft = true
                        switchStatus()
                    }
                }
                Logger.d("ACTION_UP" + isRedLeft)
            }
        }
        return true
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
        view.viewRed.visibility = View.VISIBLE
        val params = view.viewRed.layoutParams as LinearLayout.LayoutParams
        params.rightMargin = if (isRedLeft) ScreenUtils.dp2px(view.context, 20) else 0
        params.leftMargin = if (isRedLeft) 0 else ScreenUtils.dp2px(view.context, 20)
        view.viewRed.layoutParams = params

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
