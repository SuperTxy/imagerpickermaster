package com.example.apple.glidetest.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.example.apple.glidetest.R;
import com.txy.androidutils.PermissionUtils;
import com.txy.androidutils.ScreenUtils;

/**
 * Created by Apple on 17/9/8.
 */

public class VideoRecordBtn extends View {
    /**
     * 每隔多长时间重绘一次
     */
    private long interval = 50;
    /**
     * 最长录制时间10s
     */
    private long maxLength = 10000;
    private boolean isPressed = false;
    private float progress = 0;
    private int startAngle = 270;
    private Context context;
    private CountDownTimer countDownTimer;
    private OnRecordListener listener;
    private PermissionUtils permissionUtils;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public VideoRecordBtn(Context context) {
        this(context, null);
    }

    public VideoRecordBtn(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoRecordBtn(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        permissionUtils = new PermissionUtils((Activity) context);
        countDownTimer = new CountDownTimer(maxLength, interval) {
            @Override
            public void onTick(long l) {
                if (isPressed) {
                    progress = (maxLength - l) / (float) maxLength;
                    invalidate();
                }
            }

            @Override
            public void onFinish() {
                release();
            }
        };
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                permissionUtils.checkPermission(permissions, context.getString(R.string.no_record_permission),
                        new Runnable() {
                            @Override
                            public void run() {
                                press();
                            }
                        });
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                release();
                break;
        }
        return true;
    }

    private void press() {
        isPressed = true;
        invalidate();
        countDownTimer.start();
        if (listener != null)
            listener.onRecordStart();
    }

    private void release() {
        isPressed = false;
        progress = 0;
        countDownTimer.cancel();
        invalidate();
        if (listener != null)
            listener.onRecordFinish();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerX = (getWidth()) / 2;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        if (isPressed) {
            drawPressed(paint, canvas, centerX);
        } else drawReleased(paint, canvas, centerX);
    }

    private void drawPressed(Paint paint, Canvas canvas, float centerX) {
        float pressInnerRadius = centerX / 2;
        float pressBorderWidth = ScreenUtils.dp2px(context,5);
        paint.setColor(ContextCompat.getColor(context, R.color.colorf4f5f7));
        canvas.drawCircle(centerX, centerX, centerX, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(centerX, centerX, pressInnerRadius, paint);
//        画最外层的进度圆弧
        paint.setColor(ContextCompat.getColor(context, R.color.colore93a3a));
        paint.setStrokeWidth(pressBorderWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        int offset = (int) (pressBorderWidth / 2);
        RectF rectF = new RectF(offset, offset, 2 * centerX - offset, 2 * centerX - offset);
        float sweepAngle = progress * 360;
        canvas.drawArc(rectF, startAngle, sweepAngle, false, paint);
    }


    private void drawReleased(Paint paint, Canvas canvas, float centerX) {
        float releaseInnerRadius = centerX * 2 / 4;
        float releaseOuterRadius = centerX * 3 / 4;
        paint.setColor(ContextCompat.getColor(context, R.color.colorf4f5f7));
        canvas.drawCircle(centerX, centerX, releaseOuterRadius, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(centerX, centerX, releaseInnerRadius, paint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        countDownTimer.cancel();
    }

    public void setOnRecordListener(OnRecordListener listener) {
        this.listener = listener;
    }

    public interface OnRecordListener {
        void onRecordFinish();

        void onRecordStart();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void destroy() {
        if (permissionUtils != null)
            permissionUtils.destroy();
    }
}
