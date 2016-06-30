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
    public int mXValue = 0;
    public int mYValue = 0;
    public PositionType mPositionType = null;
    public enum PositionType{
        MOVE_TO,
        MOVE_BY
    }

    /**
     * move to
     * @param startTime anim start time
     * @param lastTime last frame time
     * @param duration anim duration total
     * @param fromPosition from value
     * @param toPosition to value
     * @param reverse is or not reverse
     */
    public PositionFactor(long startTime, long lastTime, long duration, Point fromPosition, Point toPosition, boolean reverse) {
        this.mStartTime = startTime;
        this.mLastTime = lastTime;
        this.mDuration = duration;
        this.mFromPosition = fromPosition;
        this.mToPosition = toPosition;
        this.mReverse = reverse;
        this.mPositionType = PositionType.MOVE_TO;
    }

    /**
     * move by
     * @param startTime anim start time
     * @param lastTime last frame time
     * @param duration anim duration total
     * @param xValue x offset value total
     * @param yValue y offset value total
     * @param reverse is or not reverse
     */
    public PositionFactor(long startTime, long lastTime, long duration, int xValue, int yValue, boolean reverse) {
        this.mStartTime = startTime;
        this.mLastTime = lastTime;
        this.mDuration = duration;
        this.mXValue = xValue;
        this.mYValue = yValue;
        this.mReverse = reverse;
        this.mPositionType = PositionType.MOVE_BY;
    }
}
