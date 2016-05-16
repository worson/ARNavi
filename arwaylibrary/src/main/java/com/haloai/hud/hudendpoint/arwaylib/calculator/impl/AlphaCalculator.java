package com.haloai.hud.hudendpoint.arwaylib.calculator.impl;

import com.haloai.hud.hudendpoint.arwaylib.calculator.SuperCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.AlphaFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.AlphaResult;
import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.calculator.result;
 * project_name : hudlauncher;
 */
public class AlphaCalculator extends SuperCalculator<AlphaResult, AlphaFactor> {
    @Override
    public AlphaResult calculate(AlphaFactor alphaFactor) {
        AlphaResult alphaResult = new AlphaResult();

        long currentTime = System.currentTimeMillis();
        long offsetTimeTotal = currentTime - alphaFactor.mStartTime;
        long offsetTime =  currentTime - alphaFactor.mLastTime;
        if (offsetTimeTotal >= alphaFactor.mDuration) {
            alphaResult.mIsOver = true;
        }
        if (alphaFactor.mReverse) {
            if (offsetTimeTotal <= alphaFactor.mDuration / 2) {
                alphaResult.mOffsetAlpha =  MathUtils.getOffsetValue(alphaFactor.mFromAlpha, alphaFactor.mToAlpha, offsetTime, alphaFactor.mDuration/2);
            } else {
                alphaResult.mOffsetAlpha =  MathUtils.getOffsetValue(alphaFactor.mToAlpha, alphaFactor.mFromAlpha, offsetTime, alphaFactor.mDuration/2);
            }
        }else{
            alphaResult.mOffsetAlpha =  MathUtils.getOffsetValue(alphaFactor.mFromAlpha, alphaFactor.mToAlpha, offsetTime, alphaFactor.mDuration);
        }
        return alphaResult;
    }

    private static AlphaCalculator mAlphaFactor = new AlphaCalculator();

    private AlphaCalculator() {}

    public static AlphaCalculator getInstance() {
        return mAlphaFactor;
    }
}
