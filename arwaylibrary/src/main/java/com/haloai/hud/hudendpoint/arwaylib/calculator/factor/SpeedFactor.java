package com.haloai.hud.hudendpoint.arwaylib.calculator.factor;

/**
 * Created by wangshengxing on 16/6/23.
 */
public class SpeedFactor extends SuperFactor{
    public int speed;
    private static SpeedFactor mSpeedFactor = new SpeedFactor();

    public static SpeedFactor getInstance() {
        return mSpeedFactor;
    }

    public void init(boolean isDraw, int speed) {
        this.mNeedDraw = isDraw;
        this.speed = speed;
    }
}
