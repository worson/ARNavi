package com.haloai.hud.hudendpoint.arwaylib.calculator;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.calculator.result;
 * project_name : hudlauncher;
 */
public abstract class SuperCalculator<CALCULATOR_RESULT,CALCULATOR_FACTOR> {
    /**
     * full the frameData with data which calculator`s result.
     */
    public abstract CALCULATOR_RESULT calculate(CALCULATOR_FACTOR factor);

    public abstract void reset();
}
