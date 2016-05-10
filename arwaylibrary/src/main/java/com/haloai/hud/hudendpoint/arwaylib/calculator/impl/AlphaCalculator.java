package com.haloai.hud.hudendpoint.arwaylib.calculator.impl;

import com.haloai.hud.hudendpoint.arwaylib.calculator.SuperCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.AlphaFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.AlphaResult;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.calculator.result;
 * project_name : hudlauncher;
 */
public class AlphaCalculator extends SuperCalculator<AlphaResult,AlphaFactor>{
    @Override
    public AlphaResult calculate(AlphaFactor alphaFactor) {
        return null;
    }

    private static AlphaCalculator mAlphaFactor = new AlphaCalculator();
    private AlphaCalculator(){}
    public static AlphaCalculator getInstance() {
        return mAlphaFactor;
    }
}
