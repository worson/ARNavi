package com.haloai.hud.hudendpoint.arwaylib.calculator.factor;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.calculator.result;
 * project_name : hudlauncher;
 */
public class AlphaFactor extends SuperFactor {
    public float mFromAlpha    = 0f;
    public float mToAlpha      = 0f;
    public float mCurrentAlpha = 0f;
    public AlphaFactor(long startTime, long lastTime, long duration, float fromAlpha, float toAlpha, float curAlpha, boolean reverse) {
        this.mStartTime = startTime;
        this.mLastTime = lastTime;
        this.mDuration = duration;
        this.mReverse = reverse;
        this.mFromAlpha = fromAlpha;
        this.mToAlpha = toAlpha;
        this.mCurrentAlpha = curAlpha;
    }
}
