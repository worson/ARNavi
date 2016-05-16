package com.haloai.hud.hudendpoint.arwaylib.utils;


import com.haloai.hud.utils.HaloLogger;

/**
 * author       : 龙;
 * date         : 2016/5/6;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.utils;
 * project_name : hudlauncher;
 */
public class MathUtils {
    public static <VALUE_TYPE extends Number> double getOffsetValue(VALUE_TYPE from, VALUE_TYPE to, long timeOffset, long duration){
        double offsetValue = ((to.doubleValue() - from.doubleValue())*(1.0*timeOffset/duration));
        HaloLogger.logE("position__","from:"+from.doubleValue()+",to:"+to.doubleValue()+",time_offset:"+timeOffset);
        return offsetValue;
    }

    public static int formatAsEvenNumber(int number) {
        if (IsOddNumber(number)) {
            return number - 1;
        } else {
            return number;
        }
    }

    private static boolean IsOddNumber(int n) {
        return n % 2 != 0;
    }
}
