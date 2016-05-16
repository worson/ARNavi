package com.haloai.hud.hudendpoint.arwaylib.calculator.factor;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.calculator.result;
 * project_name : hudlauncher;
 */
public class RotateFactor extends SuperFactor {
    public double mRotateX = 0f;
    public double mRotateY = 0f;
    public double mRotateZ = 0f;
    public RotateFactor(long startTime, long lastTime, long duration, double rotateX, double rotateY, double rotateZ, boolean reverse){
        this.mStartTime = startTime;
        this.mLastTime = lastTime;
        this.mDuration = duration;
        this.mReverse = reverse;
        this.mRotateX = rotateX;
        this.mRotateY = rotateY;
        this.mRotateZ = rotateZ;
    }
}
