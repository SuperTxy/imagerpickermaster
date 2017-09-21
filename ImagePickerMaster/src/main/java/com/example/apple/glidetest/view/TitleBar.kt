package com.example.apple.glidetest.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.example.apple.glidetest.R
import com.txy.androidutils.TxyScreenUtils
import kotlinx.android.synthetic.main.title_bar.view.*

/**
 * Created by Apple on 17/8/1.
 */
class TitleBar @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0)
    : RelativeLayout(context, attributeSet, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.title_bar, this)
        val ta = context.obtainStyledAttributes(attributeSet, R.styleable.TitleBar)
        val centerText = ta.getString(R.styleable.TitleBar_centerText)
        val leftImage = ta.getDrawable(R.styleable.TitleBar_leftImage)
        val rightText = ta.getString(R.styleable.TitleBar_rightText)
        val rightImage = ta.getDrawable(R.styleable.TitleBar_rightImage)
        val backgroundColor = ta.getColor(R.styleable.TitleBar_backgroudColor,Color.WHITE)
        val leftText = ta.getString(R.styleable.TitleBar_leftText)
        val centerImg = ta.getDrawable(R.styleable.TitleBar_centerImg)
        ta.recycle()
        background = ColorDrawable(backgroundColor)
        if (leftImage == null) {
            ivLeft.visibility = View.GONE
        } else {
            ivLeft.visibility = View.VISIBLE
            ivLeft.setImageDrawable(leftImage)
        }
        if (!centerText.isNullOrEmpty()) {
            tvCenter.text = centerText
        }
        if (!rightText.isNullOrEmpty()) {
            tvRight.visibility = View.VISIBLE
            tvRight.text = rightText
        }
        if (rightImage != null) {
            ivRight.setBackgroundDrawable(rightImage)
        }
        if(!leftText.isNullOrEmpty()){
            tvLeft.visibility= View.VISIBLE
            tvLeft.text = leftText
        }
        if (centerImg !=null){
            centerImg.setBounds(0,0,centerImg.minimumWidth,centerImg.minimumHeight)
            tvCenter.setCompoundDrawables(null,null,centerImg,null)
            tvCenter.compoundDrawablePadding = TxyScreenUtils.dp2px(context,5)
        }
    }
}