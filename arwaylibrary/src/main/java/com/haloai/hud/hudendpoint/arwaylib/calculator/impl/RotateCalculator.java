package com.haloai.hud.hudendpoint.arwaylib.calculator.impl;

import com.haloai.hud.hudendpoint.arwaylib.calculator.SuperCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.RotateFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.RotateResult;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.calculator.result;
 * project_name : hudlauncher;
 */
public class RotateCalculator extends SuperCalculator<RotateResult,RotateFactor> {
    @Override
    public RotateResult calculate(RotateFactor rotateFactor) {
        return null;
    }

    private static RotateCalculator mRotateFactor = new RotateCalculator();
    private RotateCalculator(){}
    public static RotateCalculator getInstance() {
        return mRotateFactor;
    }
}
