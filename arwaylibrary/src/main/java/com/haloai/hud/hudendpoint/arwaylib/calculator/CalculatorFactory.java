package com.haloai.hud.hudendpoint.arwaylib.calculator;

import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.AlphaCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.PositionCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.RotateCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.RouteCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.ScaleCalculator;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.calculator.result;
 * project_name : hudlauncher;
 */
public class CalculatorFactory {
    public enum CalculatorType {
        ALPHA,
        POSITION,
        ROTATE,
        ROUTE,
        SCALA
    }

    public static SuperCalculator getCalculator(CalculatorType calculatorType) {
        SuperCalculator calculator = null;
        switch (calculatorType) {
            case ALPHA:
                calculator = AlphaCalculator.getInstance();
                break;
            case POSITION:
                calculator = PositionCalculator.getInstance();
                break;
            case ROTATE:
                calculator = RotateCalculator.getInstance();
                break;
            case ROUTE:
                calculator = RouteCalculator.getInstance();
                break;
            case SCALA:
                calculator = ScaleCalculator.getInstance();
                break;
            default:
                break;
        }
        return calculator;
    }
}
