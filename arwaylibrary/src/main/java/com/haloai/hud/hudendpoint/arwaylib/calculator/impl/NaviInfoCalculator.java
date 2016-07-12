package com.haloai.hud.hudendpoint.arwaylib.calculator.impl;

import com.haloai.hud.hudendpoint.arwaylib.calculator.SuperCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.NaviInfoFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.NaviInfoResult;

/**
 * Created by wangshengxing on 16/6/23.
 */
public class NaviInfoCalculator extends SuperCalculator<NaviInfoResult,NaviInfoFactor>{
    private static NaviInfoCalculator mNaviInfoCalculator = new NaviInfoCalculator();
    @Override
    public NaviInfoResult calculate(NaviInfoFactor naviInfoFactor) {
        NaviInfoResult naviInfoResult = NaviInfoResult.getInstance();
        naviInfoResult.remainDistance = naviInfoFactor.remainDistance;
        naviInfoResult.remainTime = naviInfoFactor.remainTime;
        naviInfoResult.mShouldDraw = naviInfoFactor.mNeedDraw;
        naviInfoResult.mNaviText = naviInfoFactor.mNaviText;
        return naviInfoResult;
    }

    @Override
    public void reset() {

    }

    public static NaviInfoCalculator getInstance() {
        return mNaviInfoCalculator;
    }
}
