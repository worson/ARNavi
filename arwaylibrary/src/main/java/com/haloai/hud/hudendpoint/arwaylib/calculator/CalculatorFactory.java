package com.haloai.hud.hudendpoint.arwaylib.calculator;

import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.AlphaCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.CrossImageCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.NaviInfoCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.PositionCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.RotateCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.RouteCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.ScaleCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.SpeedCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.TurnInfoCalculator;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.TurnInfoFrameData;

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
        SCALA,
        TURN_INFO,
        CROSS_IMAGE,
        SPEED,
        NAVI_INFO,
        GL_SCENE,
        GL_CAMERA
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
            case TURN_INFO:
                calculator = TurnInfoCalculator.getInstance();
                break;
            case CROSS_IMAGE:
                calculator = CrossImageCalculator.getInstance();
                break;
            case SPEED:
                calculator = SpeedCalculator.getInstance();
                break;
            case NAVI_INFO:
                calculator = NaviInfoCalculator.getInstance();
                break;
            default:
                break;
        }
        return calculator;
    }
}
