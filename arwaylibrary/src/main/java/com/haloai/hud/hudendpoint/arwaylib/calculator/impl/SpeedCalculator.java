package com.haloai.hud.hudendpoint.arwaylib.calculator.impl;

import com.haloai.hud.hudendpoint.arwaylib.calculator.SuperCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.SpeedFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.SpeedResult;

/**
 * Created by wangshengxing on 16/6/23.
 */
public class SpeedCalculator extends SuperCalculator<SpeedResult,SpeedFactor>{
    private static SpeedCalculator mSpeedCalculator = new SpeedCalculator();
    @Override
    public SpeedResult calculate(SpeedFactor speedFactor) {
        SpeedResult speedResult = SpeedResult.getInstance();
        speedResult.speed = speedFactor.speed;
        speedResult.mShouldDraw = speedFactor.mNeedDraw;
        return speedResult;
    }

    @Override
    public void reset() {

    }

    public static SpeedCalculator getInstance() {
        return mSpeedCalculator;
    }
}
