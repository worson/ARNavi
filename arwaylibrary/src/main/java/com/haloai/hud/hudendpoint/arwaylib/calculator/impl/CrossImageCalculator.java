package com.haloai.hud.hudendpoint.arwaylib.calculator.impl;

import com.haloai.hud.hudendpoint.arwaylib.calculator.SuperCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.CrossImageFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.CrossImageResult;

/**
 * Created by wangshengxing on 16/6/23.
 */
public class CrossImageCalculator extends SuperCalculator<CrossImageResult,CrossImageFactor> {
    private static CrossImageCalculator mCrossImageCalculator = new CrossImageCalculator();
    @Override
    public CrossImageResult calculate(CrossImageFactor turnInfoFactor) {
        CrossImageResult turnInfoResult = CrossImageResult.getInstance();
        turnInfoResult.crossBitmap = turnInfoFactor.crossIconBitmap;
        turnInfoResult.mShouldDraw = turnInfoFactor.mNeedDraw;
        return turnInfoResult;
    }

    @Override
    public void reset() {

    }

    public static CrossImageCalculator getInstance() {
        return mCrossImageCalculator;
    }
}
