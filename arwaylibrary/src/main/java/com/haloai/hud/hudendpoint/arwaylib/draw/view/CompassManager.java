package com.haloai.hud.hudendpoint.arwaylib.draw.view;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangshengxing on 17/11/2016.
 */

public class CompassManager {
    private SensorManager mSensorManager;
    private Sensor        accelerometer;//加速度传感器
    private Sensor        magnetic; // 地磁场传感器
    private              float[] accelerometerValues = new float[3];
    private              float[] magneticFieldValues = new float[3];


    private static final String  TAG                 = CompassManager.class.getSimpleName();
    private float mRotation = 0;
    private List<CompassLister> mCompassListers = null;

    private CompassSensorEventListener mSensorEventListener = new CompassSensorEventListener();

    private static CompassManager mCompassManager = new CompassManager();

    public static CompassManager getInstance(){
        return mCompassManager;
    }

    public interface CompassLister{
        void onOrientationChange(float orientation);
    }

    class CompassSensorEventListener implements SensorEventListener {

        @Override

        public void
        onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = event.values;
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticFieldValues = event.values;
            }
            calculateOrientation();

        }


        @Override

        public void onAccuracyChanged(Sensor sensor, int accuracy) {


        }


    }

    public void addCompassLister(CompassLister compassLister){
        if (mCompassListers == null) {
            mCompassListers = new ArrayList<>();
        }
        mCompassListers.add(compassLister);
        Log.d(TAG, TAG+" addCompassLister");
    }
    public void removeCompassLister(CompassLister compassLister){
        if (mCompassListers == null) {
            return;
        }
        mCompassListers.remove(compassLister);
    }

    public void setRotation(float rotation) {
        mRotation = rotation%360;
    }

    private void calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);
        values[0] = (float) Math.toDegrees(values[0]);
        float orientation = values[0];
        if(orientation<0){
            orientation = 360+orientation;
        }
        orientation += mRotation;
        if (orientation>360){
            orientation -= 360;
        }
        for (CompassLister lister: mCompassListers){
            lister.onOrientationChange(orientation);
        }
//        Log.d(TAG, values[0] + "");
    }

    public void init(Context context){
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        // 初始化加速度传感器
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // 初始化地磁场传感器
        magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //注册监听
        mSensorManager.registerListener(mSensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mSensorEventListener, magnetic, SensorManager.SENSOR_DELAY_UI);
        Log.d(TAG, TAG+" init");
    }

    public void unbind(){
        mSensorManager.unregisterListener(mSensorEventListener);
    }
}
