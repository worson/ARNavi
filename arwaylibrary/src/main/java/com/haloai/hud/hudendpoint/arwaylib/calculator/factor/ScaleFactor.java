package com.haloai.hud.hudendpoint.arwaylib.calculator.factor;

import android.graphics.Point;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.calculator.result;
 * project_name : hudlauncher;
 */
public class ScaleFactor extends SuperFactor {
    public float mFromScala = 1f;
    public float mLastScala = 1f;
    public float mToScala   = 1f;

    public ScaleFactor(long startTime,long lastTime, long duration, float fromScala, float toScala, float lastScala, Point position, boolean reverse) {
        this.mStartTime = startTime;
        this.mLastTime = lastTime;
        this.mDuration = duration;
        this.mFromScala = fromScala;
        this.mToScala = toScala;
        this.mLastScala = lastScala;
        this.mReverse = reverse;
        this.mFromPosition = position;
    }
}
