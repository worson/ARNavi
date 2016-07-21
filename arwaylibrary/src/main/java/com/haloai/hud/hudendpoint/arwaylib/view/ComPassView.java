package com.haloai.hud.hudendpoint.arwaylib.view;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.haloai.hud.hudendpoint.arwaylib.R;

/**
 * author       : 龙;
 * date         : 2016/7/6;
 * email        : helong@haloai.com;
 * package_name : demo.opengl.haloai.com.compassdemo;
 * project_name : ComPassDemo;
 */
public class ComPassView extends View implements SensorEventListener {
    //compass draw data
    private long           mStartTime         = 0l;
    private float          mStartComPassValue = 0f;
    private ObjectAnimator mRotateAnim        = null;

    private Bitmap mComPassOutsideRingBitmap = null;
    private Bitmap mComPassDestArrowBitmap   = null;
    private int    mDestDirection            = 0;

    public ComPassView(Context context) {
        super(context);
    }

    public ComPassView(Context context, int width, int height) {
        super(context);
        initSensor(context);
        initBitmap(context, width, height);
    }


    public ComPassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.CustomView, 0, 0);
        int width,height;
        width = (int)a.getDimension(R.styleable.CustomView_custom_height,240);
        height = (int)a.getDimension(R.styleable.CustomView_custom_height,240);
        a.recycle();
        initSensor(context);
        initBitmap(context, width, height);
    }

    private void initBitmap(Context context, int width, int height) {
        mComPassOutsideRingBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.compass_outside_ring2);
        Bitmap target = Bitmap.createBitmap(width, height, mComPassOutsideRingBitmap.getConfig());
        Canvas temp_canvas = new Canvas(target);
        temp_canvas.drawBitmap(mComPassOutsideRingBitmap, null, new Rect(0, 0, target.getWidth(), target.getHeight()), null);
        mComPassOutsideRingBitmap = target;

        mComPassDestArrowBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.compass_dest_arrow2);
        target = Bitmap.createBitmap(width, height, mComPassDestArrowBitmap.getConfig());
        temp_canvas = new Canvas(target);
        temp_canvas.drawBitmap(mComPassDestArrowBitmap, null, new Rect(0, 0, target.getWidth(), target.getHeight()), null);
        mComPassDestArrowBitmap = target;

    }

    private void initSensor(Context context) {
        // 传感器管理器
        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        // 注册传感器(Sensor.TYPE_ORIENTATION(方向传感器);SENSOR_DELAY_FASTEST(0毫秒延迟);
        // SENSOR_DELAY_GAME(20,000毫秒延迟)、SENSOR_DELAY_UI(60,000毫秒延迟))
        // 如果不采用SENSOR_DELAY_FASTEST的话,在0度和360左右之间做动画会有反向转一大圈的感觉
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mComPassOutsideRingBitmap, 0, 0, null);
        Matrix matrix = new Matrix();
        matrix.setRotate(mDestDirection, canvas.getWidth() / 2, canvas.getHeight() / 2);
        canvas.drawBitmap(mComPassDestArrowBitmap, matrix, null);
    }

    public void setDestDegree(int degree) {
        mDestDirection = degree;
//        new Thread() {
//            final long DURATION = 2000;
//            long startTime = 0l;
//            int direction = mDestDirection - 50;
//
//            @Override
//            public void run() {
//                startTime = System.currentTimeMillis();
//                while (*//*System.currentTimeMillis() - startTime > DURATION && direction == mDestDirection*//*true) {
//                    mDestDirection = direction;
//                    direction += 5;
//                    SystemClock.sleep(30);
//                }
//            }
//        }.start();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            float degree = event.values[0];
            //first time
            if (mStartTime == 0) {
                mStartTime = System.currentTimeMillis();
                mStartComPassValue = -degree;
            } else {
                if (mRotateAnim != null && mRotateAnim.isRunning()) {
                    mRotateAnim.pause();
                    mRotateAnim.cancel();
                }
                long endTime = System.currentTimeMillis();
                mRotateAnim = ObjectAnimator.ofFloat(this, "rotation", mStartComPassValue, -degree);
                mRotateAnim.setDuration(endTime - mStartTime);
                mRotateAnim.setRepeatCount(0);
                mRotateAnim.start();

                mStartTime = endTime;
                mStartComPassValue = -degree;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //精度改变
    }
}
