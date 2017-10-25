package com.supertxy.media.media

import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.LinearLayout
import com.supertxy.media.R
import com.supertxy.media.RecordMediaActivity
import com.txy.androidutils.TxyScreenUtils
import kotlinx.android.synthetic.main.activity_record_media.view.*

/**
 * Created by Apple on 17/9/14.
 */

class SlideHolder(private var view: View){

    var isFinish: Boolean = false
    var isRedLeft: Boolean = true
        set(value) {
            field = value
            switchStatus()
        }

    init {
        view.tvCamera.setOnClickListener {
            switchToCamera()
        }
        view.tvVideo.setOnClickListener {
            switchToVideo()
        }
    }

     fun switchToVideo() {
        if (isRedLeft) {
            isRedLeft = false
            switchStatus()
            if (view.context is RecordMediaActivity) {
                (view.context as RecordMediaActivity).changeMediaType(isRedLeft)
            }
        }
    }

     fun switchToCamera() {
        if (!isRedLeft) {
            isRedLeft = true
            switchStatus()
            if (view.context is RecordMediaActivity) {
                (view.context as RecordMediaActivity).changeMediaType(isRedLeft)
            }
        }
    }

    fun switchStatus() {
        view.tvVideoHint.text = view.context.getString(R.string.press_to_record)
        view.tvVideoHint.visibility = if (isRedLeft) View.GONE else View.VISIBLE
        view.tvCamera.visibility = View.VISIBLE
        view.tvVideo.visibility = View.VISIBLE
        view.tvCamera.paint.textSize = selectedDimen(isRedLeft)
        view.tvVideo.paint.textSize = selectedDimen(!isRedLeft)
        view.tvCamera.setTextColor(selectedColor(isRedLeft))
        view.tvVideo.setTextColor(selectedColor(!isRedLeft))
        view.tvCamera.setShadowLayer(if (!isRedLeft)16f else 0f,1f,1f,Color.BLACK)
        view.tvVideo.setShadowLayer(if (isRedLeft)16f else 0f,1f,1f,Color.BLACK)
        view.tvCamera.invalidate()
        view.tvVideo.invalidate()
        view.viewRed.visibility = View.VISIBLE
        val params = view.viewRed.layoutParams as LinearLayout.LayoutParams
        params.rightMargin = if (isRedLeft) TxyScreenUtils.dp2px(view.context, 20) else 0
        params.leftMargin = if (isRedLeft) 0 else TxyScreenUtils.dp2px(view.context, 20)
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
        isFinish = true
        view.tvVideoHint.text = view.context.getString(R.string.press_to_record_again)
        view.viewRed.visibility = View.INVISIBLE
        view.tvCamera.visibility = if (isRedLeft) View.VISIBLE else View.GONE
        view.tvVideo.visibility = if (!isRedLeft) View.VISIBLE else View.GONE
        val tv = if (isRedLeft) view.tvCamera else view.tvVideo
        tv.setTextColor(Color.WHITE)
    }
}
