package com.haloai.hud.hudendpoint.arwaylib.utils;

import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ylq on 16/11/4.
 */
public class ARWayCurver {
    static private double CURVELENGTH = 1.0;
    static private double scale = 0.4;//控制点的收缩系数

    /*
    public static void createCurve(List<ARWayProjection.PointD> originPoints, List<ARWayProjection.PointD> curvePoints){  //points.count >=5
        for (int i = 0;i < originPoints.size()-1;i++){
            if (originPoints.get(i).x==originPoints.get(i+1).x && originPoints.get(i).y ==originPoints.get(i+1).y){
                originPoints.remove(i+1);
                i--;
            }
        }
        int originCount = originPoints.size();
        if (originCount < 5){
            return;
        }
        List<ARWayProjection.PointD> midPoints = new ArrayList<ARWayProjection.PointD>();
        List<Double> proportionList = new ArrayList<Double>();
        //找中心点和比例
        for (int i = 0;i < originCount-1;i ++){
            ARWayProjection.PointD originPoint = originPoints.get(i);
            ARWayProjection.PointD nextPoint = originPoints.get(i+1);
            ARWayProjection.PointD midPoint = new ARWayProjection.PointD((originPoint.x + nextPoint.x)/2,(originPoint.y + nextPoint.y)/2);
            midPoints.add(midPoint);
            if (i < originCount-2){
                ARWayProjection.PointD nexnextPoint = originPoints.get(i+2);
                Double p = Math.sqrt(Math.pow(nextPoint.x - originPoint.x,2.0) + Math.pow(nextPoint.y - originPoint.y,2.0))/Math.sqrt(Math.pow(nexnextPoint.x-nextPoint.x,2.0)+Math.pow(nexnextPoint.y-nextPoint.y,2.0));
                proportionList.add(p);
            }

        }

        List<ARWayProjection.PointD> extraPoints = new ArrayList<ARWayProjection.PointD>();
        for(int i = 0;i < proportionList.size();i ++){
            ARWayProjection.PointD oriMidPoint = midPoints.get(i);
            ARWayProjection.PointD nexMidPoint = midPoints.get(i+1);
            double p = proportionList.get(i).doubleValue();
            double origin_X = oriMidPoint.x + (p/(1+p)) * (nexMidPoint.x - oriMidPoint.x);
            double origin_Y = oriMidPoint.y + (p/(1+p)) * (nexMidPoint.y - oriMidPoint.y);
            ARWayProjection.PointD startPoint = originPoints.get(i+1);
            double offset_X = startPoint.x - origin_X;
            double offset_Y = startPoint.y - origin_Y;

            ARWayProjection.PointD leftExtraPoint = new ARWayProjection.PointD(oriMidPoint.x + offset_X,oriMidPoint.y + offset_Y);
            ARWayProjection.PointD rightExtraPoint = new ARWayProjection.PointD(nexMidPoint.x + offset_X,nexMidPoint.y + offset_Y);

            leftExtraPoint.x = leftExtraPoint.x - scale * (leftExtraPoint.x - startPoint.x);
            leftExtraPoint.y = leftExtraPoint.y - scale * (leftExtraPoint.y - startPoint.y);

            rightExtraPoint.x = rightExtraPoint.x - scale * (rightExtraPoint.x -startPoint.x);
            rightExtraPoint.y = rightExtraPoint.y - scale * (rightExtraPoint.y - startPoint.y);

            extraPoints.add(leftExtraPoint);
            extraPoints.add(rightExtraPoint);
        }

        extraPoints.remove(0);

        curvePoints.add(originPoints.get(0));
        for (int i = 1;i <originCount-2;i ++){
            List<ARWayProjection.PointD> controlPoints = new ArrayList<ARWayProjection.PointD>();
            int extraIndex = (i-1) * 2;
            controlPoints.add(originPoints.get(i));
            controlPoints.add(extraPoints.get(extraIndex));
            controlPoints.add(extraPoints.get(extraIndex+1));
            controlPoints.add(originPoints.get(i+1));

            ARWayProjection.PointD originPoint = originPoints.get(i);
            ARWayProjection.PointD nextPoint = originPoints.get(i+1);
            boolean isDo = false;
            if (Math.sqrt(Math.pow(nextPoint.x - originPoint.x,2.0) + Math.pow(nextPoint.y - originPoint.y,2.0))>4){
                isDo = true;
            }

            float u = 1;
            float step = (float) 0.1;
            while (u >= 0){
                double px = bezier3funcX(u,controlPoints);
                double py = bezier3funcY(u,controlPoints);
                u -= step;
                ARWayProjection.PointD point = new ARWayProjection.PointD(px,py);
                curvePoints.add(point);
            }
        }
        curvePoints.add(originPoints.get(originCount-1));
    }
*/

    private static double bezier3funcX(float uu,List<Vector3> controlPoints){
        double part0 = controlPoints.get(0).x * uu *uu * uu;
        double part1 = 3 *controlPoints.get(1).x * uu * uu * (1 - uu);
        double part2 = 3 *controlPoints.get(2).x * uu * (1-uu) * (1-uu);
        double part3 = controlPoints.get(3).x * (1-uu) * (1-uu) * (1-uu);
        return part0+part1+part2+part3;
    }

    private static double bezier3funcY(float uu,List<Vector3> controlPoints){
        double part0 = controlPoints.get(0).y * uu *uu * uu;
        double part1 = 3 *controlPoints.get(1).y * uu * uu * (1 - uu);
        double part2 = 3 *controlPoints.get(2).y * uu * (1-uu) * (1-uu);
        double part3 = controlPoints.get(3).y * (1-uu) * (1-uu) * (1-uu);
        return part0+part1+part2+part3;

    }


    private static double bezier2funcX(float uu,List<Vector3> controlPoints){
        double part0 = controlPoints.get(0).x * uu * uu;
        double part1 = 2 *controlPoints.get(1).x *uu *(1-uu);
        double part2 = controlPoints.get(2).x *(1-uu)*(1-uu);
        return  part0+part1+part2;
    }

    private static double bezier2funcY(float uu,List<Vector3> controlPoints){
        double part0 = controlPoints.get(0).y * uu * uu;
        double part1 = 2 *controlPoints.get(1).y *uu *(1-uu);
        double part2 = controlPoints.get(2).y *(1-uu)*(1-uu);
        return  part0+part1+part2;
    }


    public static void makeCurvePlanB(List<Vector3> originPoints, List<Vector3> curvePoints){
        for (int i = 0;i < originPoints.size()-1;i++){
            if (originPoints.get(i).x==originPoints.get(i+1).x && originPoints.get(i).y ==originPoints.get(i+1).y){
                originPoints.remove(i+1);
                i--;
            }
        }
        curvePoints.add(originPoints.get(0));
        double currentLineLength = getLengthFromTwoPoint(originPoints.get(0),originPoints.get(1));
        for (int i = 0;i < originPoints.size()-2;i ++){
            double nextLineLength = getLengthFromTwoPoint(originPoints.get(i+1),originPoints.get(i+2));
            double shouldBeCurLength;
            if (currentLineLength < nextLineLength ){
                if (currentLineLength/2 >= CURVELENGTH){
                    shouldBeCurLength = CURVELENGTH/2;
                }else {
                    shouldBeCurLength = currentLineLength/2;
                }
            }else {
                if (nextLineLength/2 >= CURVELENGTH){
                    shouldBeCurLength = CURVELENGTH/2;
                }else {
                    shouldBeCurLength = nextLineLength/2;
                }
            }
            double offsetX = shouldBeCurLength/currentLineLength *(originPoints.get(i+1).x - originPoints.get(i).x);
            double offsetY = shouldBeCurLength/currentLineLength *(originPoints.get(i+1).y - originPoints.get(i).y);
            Vector3 startPoint = new Vector3(originPoints.get(i+1).x - offsetX,originPoints.get(i+1).y - offsetY,0);
            offsetX = shouldBeCurLength/nextLineLength *(originPoints.get(i+2).x - originPoints.get(i+1).x);
            offsetY = shouldBeCurLength/nextLineLength *(originPoints.get(i+2).y - originPoints.get(i+1).y);
            Vector3 endPoint = new Vector3(originPoints.get(i+1).x + offsetX,originPoints.get(i+1).y + offsetY,0);
            List<Vector3> res = new ArrayList<>();
            threePointMakeBezier2(startPoint,originPoints.get(i+1),endPoint,res);
            curvePoints.addAll(res);
            currentLineLength = nextLineLength;
        }
        curvePoints.add(originPoints.get(originPoints.size()-1));
    }


    private static void threePointMakeBezier2(Vector3 startPoint , Vector3 centerPoint , Vector3 endPoint, List<Vector3> resultList){
        List<Vector3> controlPoints = new ArrayList<Vector3>();
        controlPoints.add(startPoint);
        controlPoints.add(centerPoint);
        controlPoints.add(endPoint);

        float u = 1;
        float step = (float) 0.05;
        while (u >= 0){
            double px = bezier2funcX(u,controlPoints);
            double py = bezier2funcY(u,controlPoints);
            u -= step;
            Vector3 resPoint = new Vector3(px,py,0);
            resultList.add(resPoint);
        }

    }



    private static void threePointMakeBezier3(Vector3 startPoint , Vector3 centerPoint , Vector3 endPoint, List<Vector3> resultList){
        double degree = 0.2;//值越小 越尖锐 但是越接近转向点  越大则反之
        Vector3 leftExtraPoint = new Vector3(centerPoint.x - degree * (centerPoint.x - startPoint.x),centerPoint.y - degree * (centerPoint.y - startPoint.y),0);
        Vector3 rightExtraPoint = new Vector3(centerPoint.x - degree * (centerPoint.x - endPoint.x),centerPoint.y - degree * (centerPoint.y - endPoint.y),0);
        List<Vector3> controlPoints = new ArrayList<>();
        controlPoints.add(startPoint);
        controlPoints.add(leftExtraPoint);
        controlPoints.add(rightExtraPoint);
        controlPoints.add(endPoint);

        float u = 1;
        float step = (float) 0.1;
        while (u >= 0){
            double px = bezier3funcX(u,controlPoints);
            double py = bezier3funcY(u,controlPoints);
            u -= step;
            Vector3 resPoint = new Vector3(px,py,0);
            resultList.add(resPoint);
        }


    }

    private static double getLengthFromTwoPoint(Vector3 firstPoint, Vector3 nextPoint){
        return Math.sqrt(Math.pow(nextPoint.x - firstPoint.x,2.0) + Math.pow(nextPoint.y - firstPoint.y,2.0));
    }
}
