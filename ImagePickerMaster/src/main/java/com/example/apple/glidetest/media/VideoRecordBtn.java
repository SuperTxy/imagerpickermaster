package com.example.apple.glidetest.media;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.apple.glidetest.R;
import com.txy.androidutils.PermissionUtils;
import com.txy.androidutils.ScreenUtils;
import com.txy.androidutils.ToastUtils;

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
    private long MAX_RECORD_TIME = 13500;
    private long MIN_RECORD_TIME = 2500;
    private boolean isPressed = false;
    /**
     * true代表拍照，false代表录制
     */
    public boolean isCamera = true;
    private ToastUtils toastUtils;
    private float progress = 0;
    private int startAngle = 270;
    private Context context;
    private long downTime;
    private CountDownTimer countDownTimer;
    private OnRecordListener listener;
    private PermissionUtils permissionUtils;


    public VideoRecordBtn(Context context) {
        this(context, null);
    }

    public VideoRecordBtn(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoRecordBtn(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        if (context instanceof Activity) {
            permissionUtils = new PermissionUtils((Activity) context);
        }
        toastUtils = new ToastUtils(context);
        countDownTimer = new CountDownTimer(MAX_RECORD_TIME, interval) {
            @Override
            public void onTick(long l) {
                if (isPressed) {
                    progress = (MAX_RECORD_TIME - l) / (float) MAX_RECORD_TIME;
                    invalidate();
                }
            }

            @Override
            public void onFinish() {
                release(false);
            }
        };
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isCamera) {
                    permissionUtils.checkCameraPermission(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null)
                                listener.onRecordStart(true);
                        }
                    });
                } else {
                    downTime = System.currentTimeMillis();
                    permissionUtils.checkRecordVideoPermission(new Runnable() {
                        @Override
                        public void run() {
                            press();
                        }
                    });
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!isCamera)
                    release(System.currentTimeMillis() - downTime < MIN_RECORD_TIME);
                else if (listener != null) {
                    listener.onRecordFinish(false);
                }
                break;
        }
        return true;
    }

    private void press() {
        isPressed = true;
        invalidate();
        countDownTimer.start();
        if (listener != null)
            listener.onRecordStart(false);
    }

    private void release(boolean recordFail) {
        isPressed = false;
        progress = 0;
        countDownTimer.cancel();
        invalidate();
        if (recordFail) {
            toastUtils.toast(getContext().getString(R.string.record_time_is_too_short));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    listener.onRecordFinish(true);
                }
            },1000);
        } else if (listener != null)
            listener.onRecordFinish(false);
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
        float pressBorderWidth = ScreenUtils.dp2px(context, 5);
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
        void onRecordFinish(boolean isFail);

        void onRecordStart(boolean isCamera);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void destroy() {
        if (permissionUtils != null)
            permissionUtils.destroy();
        toastUtils.destroy();
    }
}
