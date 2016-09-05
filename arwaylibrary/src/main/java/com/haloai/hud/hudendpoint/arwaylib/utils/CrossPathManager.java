package com.haloai.hud.hudendpoint.arwaylib.utils;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.amap.api.maps.Projection;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.NaviLatLng;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by wangshengxing on 16/8/29.
 */
public class CrossPathManager {
    private static final String TAG = CrossPathManager.class.getSimpleName();
    private List<List<Point>> mNaviStepsPoints = new ArrayList<>();
    private int   mCenterPointIndex = 0;
    private List<Point> mScreenPoints = new ArrayList<>();
    private Point mCenterPoint = new Point();
    private CrossPathManager(){}
    private static CrossPathManager mInstance = new CrossPathManager();
    public static CrossPathManager getInstance(){
        return mInstance;
    }

    /**
     * 转换整个path的屏幕点
     * @param naviPath
     * @param projection
     */
    public void parseNaviPathInfo(AMapNaviPath naviPath ,Projection projection) {
        if (naviPath == null || mNaviStepsPoints == null) {
            return;
        }
        mNaviStepsPoints.clear();
        int stepCnt = 0;
        for (AMapNaviStep step : naviPath.getSteps()) {
            if (step != null) {
                List<Point> stepPoints = new ArrayList<>();
                for (NaviLatLng coord : step.getCoords()) {
                    if (projection != null) {
                        LatLng latLng = new LatLng(coord.getLatitude(), coord.getLongitude());
                        Point p = projection.toScreenLocation(latLng);
                        stepPoints.add(p);
                    }
                }
                Log.e(TAG, String.format("parseNaviPathInfo ,stepCnt is %d ,size is %d,test point is %s", stepCnt, stepPoints.size(), stepPoints.get(0)));
                stepCnt++;
                if (stepPoints.size() > 0) {
                    mNaviStepsPoints.add(stepPoints);
                }
            }

        }
    }

    /**
     * 获取中心点下标
     * @return
     */
    public int getCenterPointIndex() {
        return mCenterPointIndex;
    }
    /**
     * 获取中心点附近的屏幕点
     * @return
     */
    public List<Point> getScreenPoints() {
        return mScreenPoints;
    }
    /**
     * 获取中心点
     * @return
     */
    public Point getCenterPoint() {
        return mCenterPoint;
    }

    private void resetCrossData(){
        mCenterPointIndex = 0;
        mCenterPoint.x=0;
        mCenterPoint.y=0;
        mScreenPoints.clear();
    }

    /**
     * 处理路口放大图中心点数据
     * @param stepIndex
     * @param width 路口放大图长
     * @param height 路口放大宽
     * @return
     */
    public boolean handleCrossInfo(int stepIndex,int width,int height){
        if (mNaviStepsPoints == null) {
            Log.e(TAG, String.format("showCross writeCrossInfo return"));
            return false;
        }
        resetCrossData();

        Point centerPoint       = new Point(0, 0);
        List<Point> screenPoints = new ArrayList<>();
        Point centerNextPoint = new Point(0, 0);

        Log.e(TAG, String.format("showCross writeCrossInfo stepIndex is %d", stepIndex));
        boolean breakFlg = false;
        if (stepIndex < mNaviStepsPoints.size()) {
            List<Point> stepPoints = mNaviStepsPoints.get(stepIndex);
            centerPoint = new Point(stepPoints.get(stepPoints.size() - 1));

            if (mNaviStepsPoints.size() > (stepIndex)) {
                for (int i = stepIndex + 1; i < mNaviStepsPoints.size(); i++) {
                    List<Point> step = mNaviStepsPoints.get(i);
                    for (int j = 0; j < step.size(); j++) {
                        Point p = step.get(j);
                        if (pointDistance(centerPoint, p) > 10) {
                            centerNextPoint = new Point(p);
                            breakFlg = true;
                            break;
                        }
                    }
                    if (breakFlg) {
                        break;
                    }
                }
                if (!breakFlg) {
                    centerNextPoint = mNaviStepsPoints.get(stepIndex + 1).get(0);
                    Log.e(TAG, String.format("showCross writeCrossInfo centerNextPoint is error,centerNextPoint is %s", centerNextPoint));
                }
            } else {
                Log.e(TAG, String.format("showCross writeCrossInfo centerNextPoint is error,centerNextPoint is %s", centerNextPoint));
                return false;
            }

            List<List<Point>> stepsPoints = mNaviStepsPoints;
            breakFlg = false;
            //路口放大图
            Point rectPoint = centerPoint;
            int w = width * 2;
            int h = height * 2;
            Rect crossRect = new Rect(rectPoint.x - w / 2, rectPoint.y - h / 2, rectPoint.x + w / 2, rectPoint.y + h / 2);
            LinkedList<Point> prePoints = new LinkedList<>();
            int errorCnt = 0;

            //计算形状点
            final int MIN_THREAD_SIZE = 10;
            final int MAX_ERROR_SIZE = 3;
            //往前计算
            int preCnt = 0;
            int outR = (int) (Math.sqrt(width * width + height * height) / 2);
            outR = (int) (200 * Math.sqrt(2));

            errorCnt = 0;
            Log.e(TAG, String.format("showCross writeCrossInfo ,centerPoint is %s,region  is %s", centerPoint, crossRect));
            for (int step = stepIndex; step >= 0; step--) {
                List<Point> cStepPoints = stepsPoints.get(step);
                int cnt = cStepPoints.size();
                for (int i = cnt - 1; i >= 0; i--) {
                    Point p = cStepPoints.get(i);
                    if (isIncludePoint(crossRect, p) || preCnt < MIN_THREAD_SIZE) {//GeometryUtils.isIncludePoint(centerPoint, outR, p)
                        prePoints.addFirst(new Point(p));
                        preCnt++;
                        errorCnt = 0;
                    } else {
                        Log.e(TAG, String.format("showCross writeCrossInfo out of region ,point is %s,preCnt is %d,errorCnt is %d", p, preCnt, errorCnt));
                        if (++errorCnt > MAX_ERROR_SIZE) {
                            breakFlg = true;
                            break;
                        } else {
                            prePoints.addFirst(new Point(p));
                            preCnt++;
                        }
                    }
                }
                if (breakFlg) {
                    break;
                }
            }
            screenPoints.addAll(prePoints);

            //往后计算
            breakFlg = false;
            int nextCnt = 0;
            errorCnt = 0;
            for (int step = stepIndex + 1; step < stepsPoints.size(); step++) {
                for (Point p : stepsPoints.get(step)) {
                    if (isIncludePoint(crossRect, p) || nextCnt < MIN_THREAD_SIZE) {
                        screenPoints.add(new Point(p));
                        nextCnt++;
                        errorCnt = 0;
                    } else {
                        Log.e(TAG, String.format("showCross writeCrossInfo out of region ,point is %s,nextCnt is %d,errorCnt is %d ", p, nextCnt, errorCnt));
                        if (++errorCnt > MAX_ERROR_SIZE) {
                            breakFlg = true;
                            break;
                        } else {
                            screenPoints.add(new Point(p));
                            nextCnt++;
                        }
                    }
                }
                if (breakFlg) {
                    break;
                }
            }
            mCenterPointIndex = preCnt-1;
            /*if (screenPoints.size() > preCnt + 1) {
                centerNextPoint = new Point(screenPoints.get(preCnt + 1));
            }*/

            /*int preStep = stepIndex-1;
            screenPoints.clear();
            if(preStep>=0){
//                screenPoints.addAll(mNaviStepsPoints.get(preStep));
            }
            screenPoints.addAll(mNaviStepsPoints.get(stepIndex));
            if(mNaviStepsPoints.size()>(stepIndex+1)){
                screenPoints.addAll(mNaviStepsPoints.get(stepIndex+1));
            }*/

            Log.e(TAG, String.format("width is %s ,height is %s,preCnt is %d,nextCnt is %d ,stepIndex is %d", width, height, preCnt, nextCnt, stepIndex));
            if (screenPoints.size() > 0) {
                Log.e(TAG, String.format("showCross writeCrossInfo has got basic data"));
                RectMapPara rectPara = getCenterPara(width, height, centerPoint);
                if (rectPara != null) {
                    rectRemap(mScreenPoints, screenPoints, rectPara);
                    mCenterPoint = rectRemapPoint(centerPoint, rectPara);
                    Point rCenterNextPoint = rectRemapPoint(centerNextPoint, rectPara);
                    Log.e(TAG, String.format("showCross writeCrossInfo got all data"));
                    Log.e(TAG, String.format("showCross writeCrossInfo saved data ,rCenterPoint is %s,,centerNextPoint is %s,rCenterNextPoint is %s ,rScreenPoints is %s ", mCenterPoint, centerNextPoint, rCenterNextPoint, mScreenPoints));
                } else {
                    Log.e(TAG, String.format("showCross writeCrossInfo rectPara is null"));
                }

            } else {
                Log.e(TAG, String.format("showCross writeCrossInfo get basic data error"));
            }
        } else {
            Log.e(TAG, String.format("showCross writeCrossInfo points out of size"));
            return false;
        }

        return true;
    }

    private static class RectMapPara
    {
        private Point refPoint;
        private double scalefactor;
        private int widthMove;
        private int heightMove;
    }

    public static RectMapPara getCenterPara(int width, int height, Point center){
        RectMapPara rectMapPara = new RectMapPara();
        rectMapPara.refPoint=new Point(0,0);
        rectMapPara.scalefactor=1;
        rectMapPara.heightMove =width/2-center.y;
        rectMapPara.widthMove=height/2-center.x;
        return  rectMapPara;

    }
    public static Point rectRemapPoint(Point srcPoint,RectMapPara rectMapPara){
        Point refPoint = rectMapPara.refPoint;
        int newX ,newY;
        newX = (int)((srcPoint.x-refPoint.x)*rectMapPara.scalefactor)+rectMapPara.widthMove;
        newY = (int)((srcPoint.y-refPoint.y)*rectMapPara.scalefactor)+rectMapPara.heightMove;
        Point newPoint = new Point(newX,newY);
        return newPoint;
    }

    public static void rectRemap(List<Point> newPointList,List<Point> points, RectMapPara rectMapPara){
        if (newPointList == null) {
            return;
        }
        Point refPoint = rectMapPara.refPoint;
        double scalefactor = rectMapPara.scalefactor;
        int widthMove = rectMapPara.widthMove;
        int heightMove = rectMapPara.heightMove;
        int newX ,newY;
        newPointList.clear();
        for(Point point: points){
            newX = (int)((point.x-refPoint.x)*scalefactor)+widthMove;
            newY = (int)((point.y-refPoint.y)*scalefactor)+heightMove;
            Point newPoint = new Point(newX,newY);
            newPointList.add(newPoint);
        }
    }

    /**
     * 两点距离的距离
     */
    public static double pointDistance(Point a, Point b){
        return Math.sqrt(powDistance(a,b));
    }
    /**
     * 两点距离平方和
     */
    public static int powDistance(Point a, Point b){
        int diffX = Math.abs(a.x-b.x);
        int diffY = Math.abs(a.y-b.y);
        return diffX*diffX+diffY*diffY;
    }

    /**
     * 判断点是否在一个矩形内
     */

    public static boolean isIncludePoint(Rect rect, Point point) {
        boolean result = false;
        if (rect != null) {
            result = (point.x>=rect.left && point.x <=rect.right)&&(point.y >= rect.top && point.y <= rect.bottom);
        }
        return result;
    }
}
