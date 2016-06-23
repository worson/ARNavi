package com.haloai.hud.hudendpoint.arwaylib.utils;


import android.graphics.Bitmap;
import android.graphics.Matrix;
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

    /***
     * 获取两个屏幕点的连线与水平线之间的距离
     *
     * @param x
     * @param y
     * @param x2
     * @param y2
     * @return
     */
    public static float getDegrees(float x, float y, float x2, float y2) {
        float degrees = 0f;
        if (x2 == x) {
            if (y2 <= y) {
                degrees = 0;
            } else {
                degrees = 180;
            }
        } else if (y2 == y) {
            if (x2 <= x) {
                degrees = 90;
            } else {
                degrees = 270;
            }
        } else {
            double c = Math.sqrt(Math.pow(Math.abs(x - x2), 2.0) + Math.pow(Math.abs(y - y2), 2.0));
            double b = Math.abs(y - y2);
            double a = Math.abs(x - x2);
            degrees = (float) (Math.acos((c * c + b * b - a * a) / (2 * b * c)) / 2 / Math.PI * 360);
            if (x2 >= x && y2 >= y) {
                degrees += 180;
            } else if (x2 <= x && y2 >= y) {
                degrees = (90 - degrees) + 90;
            } else if (x2 <= x && y2 <= y) {
                degrees += 0;
            } else if (x2 >= x && y2 <= y) {

                degrees = 270 + (90 - degrees);
            }
        }
        return degrees;
    }

    /**
     * 旋转一个bitmap,根据srcPointF,dstPointF以及centerPointF(默认0,0)
     * 旋转角度为(centerPointF,srcPointF)与(centerPointF,dstPointF)的夹角
     * <p/>
     * 根据向量坐标求出向量夹角的余弦值(PS:向量AB的另一个点均为原点)
     * (向量A 点乘 向量B)/(向量A的模 乘以 向量B的模) 等于 cosAB夹角
     * 如果给出
     * a=(X1,Y1),b=(X2,Y2)
     * 向量点积:a.b=X1X2+Y1Y2
     * 向量模相乘:|a|*|b|等于(X1平方+Y1平方)开方 * (X2平方+Y2平方)开方
     *
     * @param srcPointF  源点
     * @param dstPointF  目标点
     * @param crossImage 需要被旋转的bitmap
     * @return 旋转后的bitmap
     */
    public static Bitmap getRotateBitmap(PointF srcPointF, PointF dstPointF, Bitmap crossImage) {
        Matrix matrix = new Matrix();
//        float cosAB =
//                (float) (((srcPointF.x * dstPointF.x + srcPointF.y * dstPointF.y) /
//                        ((Math.sqrt(Math.pow(srcPointF.x,2)+Math.pow(srcPointF.y,2)))*(Math.sqrt(Math.pow(dstPointF.x,2)+Math.pow(dstPointF.y,2))))));
//        float[] values =
//                {cosAB, (float) -(1 - Math.pow(cosAB, 2)), 0,
//                        (float) (1 - Math.pow(cosAB, 2)), cosAB, 0,
//                        0, 0, 1};
//        matrix.setValues(values);
        float degreesSrc = getDegrees(0,0,srcPointF.x,srcPointF.y);
        float degreesDst = getDegrees(0,0,dstPointF.x,dstPointF.y);
        matrix.setRotate(degreesSrc-degreesDst);
        return Bitmap.createBitmap(crossImage, 0, 0, crossImage.getWidth(), crossImage.getHeight(), matrix, true);
    }
}
