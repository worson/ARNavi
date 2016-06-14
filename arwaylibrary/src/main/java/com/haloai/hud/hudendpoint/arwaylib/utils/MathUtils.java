package com.haloai.hud.hudendpoint.arwaylib.utils;


import android.graphics.Point;
import android.graphics.PointF;

import com.haloai.hud.utils.HaloLogger;

/**
 * author       : 龙;
 * date         : 2016/5/6;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.utils;
 * project_name : hudlauncher;
 */
public class MathUtils {
    public static <VALUE_TYPE extends Number> double getOffsetValue(VALUE_TYPE from, VALUE_TYPE to, long timeOffset, long duration) {
        double offsetValue = ((to.doubleValue() - from.doubleValue()) * (1.0 * timeOffset / duration));
        HaloLogger.logE("position__", "from:" + from.doubleValue() + ",to:" + to.doubleValue() + ",time_offset:" + timeOffset);
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

    /**
     * 通过两点的坐标得到两点的距离
     *
     * @param point1
     * @param point2
     * @return
     */
    public static double calculateDistance(Point point1, Point point2) {
        //(y2-y1)^2+(x2-x1)^2=Z^2
        return Math.sqrt(Math.pow((point2.y - point1.y), 2) + Math.pow((point2.x - point1.x), 2));
    }

    public static double calculateDistance(PointF point1, PointF point2) {
        //(y2-y1)^2+(x2-x1)^2=Z^2
        return Math.sqrt(Math.pow((point2.y - point1.y), 2) + Math.pow((point2.x - point1.x), 2));
    }
}
