package com.supertxy.media.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class SameHWLayout extends FrameLayout {
    public SameHWLayout(Context context) {
        super(context);
    }

    public SameHWLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SameHWLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));

        int childWidthSize = getMeasuredWidth();
        int childHeightSize = getMeasuredHeight();
        heightMeasureSpec = widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
