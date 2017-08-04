package com.example.apple.glidetest.view

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * Created by Apple on 17/8/4.
 * 解決ViewPager中PhotoView收拾滑动冲突
 */
class HackyViewPager @JvmOverloads constructor(context: Context,attributeSet: AttributeSet)
    : ViewPager(context,attributeSet){
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
       try {
           return super.onInterceptTouchEvent(ev)
       }catch (e:IllegalArgumentException ){
           return false
       }
    }
}