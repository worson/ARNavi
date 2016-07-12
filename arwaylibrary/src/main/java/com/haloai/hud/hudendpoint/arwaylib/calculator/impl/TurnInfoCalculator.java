package com.haloai.hud.hudendpoint.arwaylib.calculator.impl;

import com.haloai.hud.hudendpoint.arwaylib.calculator.SuperCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.TurnInfoFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.TurnInfoResult;

/**
 * Created by wangshengxing on 16/6/23.
 */
public class TurnInfoCalculator extends SuperCalculator<TurnInfoResult,TurnInfoFactor> {

    private static TurnInfoCalculator mTurnInfoCalculator = new TurnInfoCalculator();
    @Override
    public TurnInfoResult calculate(TurnInfoFactor turnInfoFactor) {
        TurnInfoResult turnInfoResult = TurnInfoResult.getInstance();
        turnInfoResult.turnIconDistance = turnInfoFactor.turnIconDistance;
        turnInfoResult.turnIconBitmap = turnInfoFactor.turnIconBitmap;
        turnInfoResult.mShouldDraw = turnInfoFactor.mNeedDraw;
        return turnInfoResult;
    }

    @Override
    public void reset() {

    }

    public static TurnInfoCalculator getInstance() {
        return mTurnInfoCalculator;
    }
}
