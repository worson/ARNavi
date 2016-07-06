package com.haloai.hud.hudendpoint.arwaylib.utils;


import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;

import com.haloai.hud.utils.HaloLogger;

import java.util.ArrayList;
import java.util.List;

import rajawali.math.vector.Vector3;

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
        return calculateDistance(point1.x,point1.y,point2.x,point2.y);
    }

    public static double calculateDistance(PointF point1, PointF point2) {
        //(y2-y1)^2+(x2-x1)^2=Z^2
        return calculateDistance(point1.x,point1.y,point2.x,point2.y);
    }

    public static double calculateDistance(double x1,double y1,double x2,double y2){
        return Math.sqrt(Math.pow((y2 - y1), 2) + Math.pow((x2 - x1), 2));
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
        return getDegrees(x,y,x2,y2);
    }

    public static double getDegrees(double x, double y, double x2, double y2){
        double degrees = 0f;
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
            degrees = (Math.acos((c * c + b * b - a * a) / (2 * b * c)) / 2 / Math.PI * 360);
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
        double degreesSrc = getDegrees(0,0,srcPointF.x,srcPointF.y);
        double degreesDst = getDegrees(0,0,dstPointF.x,dstPointF.y);
        matrix.setRotate((float) (degreesSrc-degreesDst));
        return Bitmap.createBitmap(crossImage, 0, 0, crossImage.getWidth(), crossImage.getHeight(), matrix, true);
    }


    /**
     * 根据一条线获取该先左右两侧点的集合
     */
    public static void points2path(List<Vector3> lineLeft, List<Vector3> lineRight, List<Vector3> prePoints, double pathWidth) {
        /**
         A(a,b) B(m,n) BC = L

         x1= m - (b-n) /√[(a-m)^2+(b-n)^2]
         x2= m + (b-n) /√[(a-m)^2+(b-n)^2]
         y1 = n + L(a-m) /√[(a-m)^2+(b-n)^2]
         y2 = n - L(a-m) /√[(a-m)^2+(b-n)^2]
         */
        //获取所有计算后的点的集合

        double path_width = pathWidth;

        //获取一侧点的集合
        ArrayList<Vector3> leftPoints = new ArrayList<>();
        ArrayList<Vector3> rightPoints = new ArrayList<>();
        leftPoints.clear();
        for(int i=0;i<prePoints.size();i++){
            Vector3 currentVector3 = prePoints.get(i);
            Vector3 secondVector3;
            double m = currentVector3.x;
            double n = currentVector3.y;
            if(i==prePoints.size()-1){
                secondVector3= prePoints.get(i-1);
            }else{
                secondVector3= prePoints.get(i+1);
            }
            double a;
            double b;
            a = secondVector3.x;
            b = secondVector3.y;

            double x;
            double y;

            //			x =  (m - (b-n) / Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));
            //			y =  (n + 50*(a-m) /Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));

            /**
             C1(x,y) c2(x3,y3) A(x2,y2) B(x1,y1) BC=a

             x=x1-a*sin{arctan[(y2-y1)/(x2-x1)]}
             y=y1+a*cos{arctan[(y2-y1)/(x2-x1)]}
             x3=x1+a*sin{arctan[(y2-y1)/(x2-x1)]}
             y3=y1- a*cos{arctan[(y2-y1)/(x2-x1)]}
             */

            //x1,y1为当前点，x2,y2为下一个点
            //m,n为B，a,b为A
            double x1=m,y1=n;
            double x2=a,y2=b;
            if(y2==y1){
                x = (m - (b-n) / Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));
                y = (n + (path_width/2)*(a-m) /Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));
            }else if(x2==x1){
                x = x1-(path_width/2);
                y = y1;
            }else if((x2<x1 && y2>y1) || (x2<x1 && y2<y1)){
                x= (x1+(path_width/2)*Math.sin(Math.atan((y2-y1)/(x2-x1))));
                y= (y1-(path_width/2)*Math.cos(Math.atan((y2-y1)/(x2-x1))));
            }else{
                x= (x1-(path_width/2)*Math.sin(Math.atan((y2-y1)/(x2-x1))));
                y= (y1+(path_width/2)*Math.cos(Math.atan((y2-y1)/(x2-x1))));
            }

            Vector3 vector3 = new Vector3(x,y,0);

            //如果不是第一个或者最后一个，那么需要取该点和该点的前一个点继续运算得到x,y，然后取中间值
            if(i!=0 && i!=prePoints.size()-1){
                secondVector3 = prePoints.get(i-1);
                a=secondVector3.x;
                b=secondVector3.y;
                x1=m;
                y1=n;
                x2=a;
                y2=b;
                if(y2==y1){
                    x =  (m + (b-n) / Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));
                    y =  (n - (path_width/2)*(a-m) /Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));
                }else if(x2==x1){
                    x = x1-(path_width/2);
                    y = y1;
                }else if((x2<x1 && y2>y1) || (x2<x1 && y2<y1)){
                    x= (x1-(path_width/2)*Math.sin(Math.atan((y2-y1)/(x2-x1))));
                    y= (y1+(path_width/2)*Math.cos(Math.atan((y2-y1)/(x2-x1))));
                }else{
                    x= (x1+(path_width/2)*Math.sin(Math.atan((y2-y1)/(x2-x1))));
                    y= (y1-(path_width/2)*Math.cos(Math.atan((y2-y1)/(x2-x1))));
                }
                vector3.x = (vector3.x+x)/2;
                vector3.y = (vector3.y+y)/2;
            }
            leftPoints.add(vector3);

        }

        //获取另一侧点的集合
        rightPoints.clear();
        for(int i=0;i<prePoints.size();i++){
            Vector3 currentVector3 = prePoints.get(i);
            Vector3 secondVector3;
            double m = currentVector3.x;
            double n = currentVector3.y;
            if(i==prePoints.size()-1){
                secondVector3= prePoints.get(i-1);
            }else{
                secondVector3= prePoints.get(i+1);
            }
            double a;
            double b;
            a = secondVector3.x;
            b = secondVector3.y;

            double x;
            double y;

            //			x =  (m + (b-n) / Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));
            //			y =  (n - 50*(a-m) /Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));

            double x1=m,y1=n;
            double x2=a,y2=b;
            if(y2==y1){
                x =  (m + (b-n) / Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));
                y =  (n - (path_width/2)*(a-m) /Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));
            }else if(x2==x1){
                x = x1+(path_width/2);
                y = y1;
            }else if((x2<x1 && y2>y1) || (x2<x1 && y2<y1)){
                x= (x1-(path_width/2)*Math.sin(Math.atan((y2-y1)/(x2-x1))));
                y= (y1+(path_width/2)*Math.cos(Math.atan((y2-y1)/(x2-x1))));
            }else{
                x= (x1+(path_width/2)*Math.sin(Math.atan((y2-y1)/(x2-x1))));
                y= (y1-(path_width/2)*Math.cos(Math.atan((y2-y1)/(x2-x1))));
            }

            Vector3 vector3 = new Vector3(x, y, 0);

            //如果不是第一个或者最后一个，那么需要取该点和该点的前一个点继续运算得到x,y，然后取中间值
            if(i!=0 && i!=prePoints.size()-1){
                secondVector3 = prePoints.get(i-1);
                a=secondVector3.x;
                b=secondVector3.y;
                x1=m;
                y1=n;
                x2=a;
                y2=b;
                if(y2==y1){
                    x =  (m - (b-n) / Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));
                    y =  (n + (path_width/2)*(a-m) /Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));
                }else if(x2==x1){
                    x = x1+(path_width/2);
                    y = y1;
                }else if((x2<x1 && y2>y1) || (x2<x1 && y2<y1)){
                    x= (x1+(path_width/2)*Math.sin(Math.atan((y2-y1)/(x2-x1))));
                    y= (y1-(path_width/2)*Math.cos(Math.atan((y2-y1)/(x2-x1))));
                }else{
                    x= (x1-(path_width/2)*Math.sin(Math.atan((y2-y1)/(x2-x1))));
                    y= (y1+(path_width/2)*Math.cos(Math.atan((y2-y1)/(x2-x1))));
                }
                vector3.x = (vector3.x+x)/2;
                vector3.y = (vector3.y+y)/2;
            }
            rightPoints.add(vector3);
        }

        if(leftPoints.size()<=0 || rightPoints.size()<=0){
            return;
        }

        //TODO 由于最后一个点的坐标是反向计算出来的，因此它的left和right是反的，在此做交换处理
        Vector3 temp = leftPoints.remove(leftPoints.size()-1);
        leftPoints.add(rightPoints.remove(rightPoints.size()-1));
        rightPoints.add(temp);

        lineLeft.clear();
        lineLeft.addAll(leftPoints);

        lineRight.clear();
        lineRight.addAll(rightPoints);

        /*//将点集合转成成矩形Path
        mRectPath.reset();
        Vector3 Vector3 = mPointsPath.get(0);
        mRectPath.moveTo(point.x,point.y);
        for(int i=1;i<mPointsPath.size();i++){
            if(i<mLeftPoints.size()){
                Vector3 = mLeftPoints.get(i);
            }else{
                Vector3 = mRightPoints.get(mRightPoints.size()-(i-mLeftPoints.size()+1));
            }
            mRectPath.lineTo(point.x, point.y);
        }


        //将点集合转换成Path集合，Path集合个数为原始点的个数减一(此处可表示为left或者right集合长度减一)
        mPaths.clear();
        for(int i=0;i<mLeftPoints.size()-1;i++){
            Path path = new Path();
            Vector3 leftCurrentVector3 = mLeftPoints.get(i);
            Vector3 leftNextVector3 = mLeftPoints.get(i+1);
            Vector3 rightCurrentVector3 = mRightPoints.get(i);
            Vector3 rightNextVector3 = mRightPoints.get(i+1);
            path.moveTo(leftCurrentPoint.x,leftCurrentPoint.y);
            path.lineTo(leftNextPoint.x,leftNextPoint.y);
            path.lineTo(rightNextPoint.x,rightNextPoint.y);
            path.lineTo(rightCurrentPoint.x,rightCurrentPoint.y);
            path.close();
            mPaths.add(path);
        }*/
    }
}
