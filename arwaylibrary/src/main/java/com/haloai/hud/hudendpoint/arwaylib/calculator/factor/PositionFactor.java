package com.haloai.hud.hudendpoint.arwaylib.calculator.factor;

import android.graphics.Point;

/**
 * author       : 龙;
 * date         : 2016/5/6;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.calculator.factor;
 * project_name : hudlauncher;
 */
public class PositionFactor extends SuperFactor {

    public Point mToPosition   = null;

    public PositionFactor(long startTime, long lastTime, long duration, Point fromPosition, Point toPosition, boolean reverse) {
        this.mStartTime = startTime;
        this.mLastTime = lastTime;
        this.mDuration = duration;
        this.mFromPosition = fromPosition;
        this.mToPosition = toPosition;
        this.mReverse = reverse;
    }
}
