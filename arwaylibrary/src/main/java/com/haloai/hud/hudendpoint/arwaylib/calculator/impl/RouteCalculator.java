package com.haloai.hud.hudendpoint.arwaylib.calculator.impl;

import com.haloai.hud.hudendpoint.arwaylib.calculator.SuperCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.RouteFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.RouteResult;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.calculator.result;
 * project_name : hudlauncher;
 */
public class RouteCalculator extends SuperCalculator<RouteResult,RouteFactor> {
    @Override
    public RouteResult calculate(RouteFactor routeFactor) {
        return null;
    }

    private static RouteCalculator mRouteFactor = new RouteCalculator();
    private RouteCalculator(){}
    public static RouteCalculator getInstance() {
        return mRouteFactor;
    }
}
