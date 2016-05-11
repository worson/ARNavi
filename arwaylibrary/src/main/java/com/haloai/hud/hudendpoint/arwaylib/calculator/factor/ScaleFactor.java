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
    public double mFromWidthScala = 1f;
    public double mLastWidthScala = 1f;
    public double mToWidthScala   = 1f;

    public double mFromHeightScala = 1f;
    public double mLastHeightScala = 1f;
    public double mToHeightScala   = 1f;

    public ScaleFactor(long startTime, long lastTime, long duration,
                       double fromWidthScala, double toWidthScala, double lastWidthScala,
                       double fromHeightScala, double toHeightScala, double lastHeightScala,
                       Point position, boolean reverse) {
        this.mStartTime = startTime;
        this.mLastTime = lastTime;
        this.mDuration = duration;
        this.mFromWidthScala = fromWidthScala;
        this.mToWidthScala = toWidthScala;
        this.mLastWidthScala = lastWidthScala;
        this.mFromHeightScala = fromHeightScala;
        this.mToHeightScala = toHeightScala;
        this.mLastHeightScala = lastHeightScala;
        this.mReverse = reverse;
        this.mFromPosition = position;
    }
}
