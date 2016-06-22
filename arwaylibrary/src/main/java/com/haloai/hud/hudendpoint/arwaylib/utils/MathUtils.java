package com.haloai.hud.hudendpoint.arwaylib.utils;


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

    /**
     * 以某个参照点旋转一个点
     * @param angle 以弧度计算
     */
    public static PointF pointRotate(PointF src,PointF ref,double angle){
        PointF result = null;
        PointF rPoint = new PointF(src.x-ref.x,src.y-ref.y);
        double c=Math.cos(angle);
        double s=Math.sin(angle);
        float x=(float)(rPoint.x*c-rPoint.y*s+ref.x);
        float y=(float)(rPoint.x*s+rPoint.y*c+ref.y);
        result = new PointF(x,y);
        return result;
    }
    /**
     * 得到两点之间的角度 以逆时针方向，0-360
     */
    public static double pointDegree(PointF p1,PointF p2){
        double angle = 0;
        float diffX,diffY;
        diffX = p2.x - p1.x;
        diffY = p2.y - p1.y;
        if (diffX==0){
            if (diffY>0){
                return 90;
            }else {
                return -90;
            }
        }else if(diffY==0){
            if (diffX>=0){
                return 0;
            }else {
                return 180;
            }
        }else {
            double k = (1.0f*diffY)/diffX;   //斜率
            double rad  = Math.atan(k); //注意这个角度的范围是 [-pi/2..pi/2], 不是0到90°
            angle = (rad*180)/Math.PI;
            if(diffY>0 && diffX<0){
                angle += 180;
            }else if(diffY<0 && diffX<0){
                angle += 180;
            }else if(diffY<0 && diffX>0){
                angle += 360;
            }
        }
        return  angle;
    }
}
