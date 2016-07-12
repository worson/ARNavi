package com.haloai.hud.hudendpoint.arwaylib.calculator.result;


/**
 * Created by wangshengxing on 16/6/23.
 */
public class SpeedResult extends SuperResult{
    public int    speed = 0;

    private static SpeedResult            mSpeedResult          = new SpeedResult();
    public static SpeedResult getInstance() {
        return mSpeedResult;
    }
}
