package com.haloai.hud.hudendpoint.arwaylib.utils;


/**
 * author       : 龙;
 * date         : 2016/5/6;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.utils;
 * project_name : hudlauncher;
 */
public class MathUtils {
    public static <VALUE_TYPE extends Number> double getOffsetValue(VALUE_TYPE from, VALUE_TYPE to, long timeOffset, long duration,double lastValue){
        double offsetValue = ((to.doubleValue() - from.doubleValue())*(1.0*timeOffset/duration));
        return offsetValue-lastValue;
    }
}
