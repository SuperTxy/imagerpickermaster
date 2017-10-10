package com.example.apple.glidetest.media;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import com.example.apple.glidetest.R;
import com.txy.androidutils.TxyScreenUtils;

/**
 * Created by Apple on 17/9/23.
 */

public class FocusView extends View {
    private int size;
    private int center_x;
    private int center_y;
    private int length;
    private Paint mPaint;

    public FocusView(Context context) {
        this(context, null);
    }

    public FocusView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FocusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.size = TxyScreenUtils.getScreenWidth(context) / 5;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(ContextCompat.getColor(context,R.color.colore93a3a));
        mPaint.setStrokeWidth(TxyScreenUtils.dp2px(context,2));
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        center_x = (int) (size / 2.0);
        center_y = (int) (size / 2.0);
        length = (int) (size / 2.0) - 2;
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(center_x - length, center_y - length, center_x + length, center_y + length, mPaint);
        canvas.drawLine(2, getHeight() / 2, size / 10, getHeight() / 2, mPaint);
        canvas.drawLine(getWidth() - 2, getHeight() / 2, getWidth() - size / 10, getHeight() / 2, mPaint);
        canvas.drawLine(getWidth() / 2, 2, getWidth() / 2, size / 10, mPaint);
        canvas.drawLine(getWidth() / 2, getHeight() - 2, getWidth() / 2, getHeight() - size / 10, mPaint);
    }
}
