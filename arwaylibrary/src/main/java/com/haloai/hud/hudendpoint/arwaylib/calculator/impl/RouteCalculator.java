package com.haloai.hud.hudendpoint.arwaylib.calculator.impl;

import android.graphics.Point;
import android.graphics.PointF;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.NaviLatLng;
import com.haloai.hud.hudendpoint.arwaylib.calculator.SuperCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.RouteFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.RouteResult;
import com.haloai.hud.hudendpoint.arwaylib.utils.DrawUtils;
import com.haloai.hud.utils.HaloLogger;

import java.util.List;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.calculator.result;
 * project_name : hudlauncher;
 */
public class RouteCalculator extends SuperCalculator<RouteResult, RouteFactor> {

    private static final float HUDWAY_LENGTH_IN_SCREEN = 1000;

    private AMapNaviLocation mPreLocation          = null;
    private AMapNaviLocation mCurrentLocation      = null;
    private AMapNaviLocation mFakerCurrentLocation = null;

    private int    mCurrentFramesCounter  = 0;
    private int    mPreviousFramesCounter = 0;
    private long   mPreTime               = 0l;
    private long   mCurrentTime           = 0l;
    private int    mCurrent               = -1;
    private double mFakerPointX           = 0f;
    private double mFakerPointY           = 0f;
    private int    mDrawIndex             = 1;
    public boolean mFakeOver = false;

    private        int             mCurrentIndex    = 1;
    private static RouteCalculator mRouteCalculator = new RouteCalculator();

    private RouteCalculator() {}

    public static RouteCalculator getInstance() {
        return mRouteCalculator;
    }

    @Override
    public void reset() {
        mPreLocation = null;
        mCurrentLocation = null;
        mFakerCurrentLocation = null;
        mCurrentFramesCounter = 0;
        mPreviousFramesCounter = 0;
        mCurrent = -1;
        mFakerPointX = 0f;
        mFakerPointY = 0f;
        mCurrentIndex = 1;
        mDrawIndex = 1;
    }

    @Override
    public RouteResult calculate(RouteFactor routeFactor) {
        RouteResult routeResult = RouteResult.getInstance();
        //        routeResult.reset();
        //fullPointsAndLatLngs + handle points
        //if we can draw , and current location is a useful location.
        routeResult.mCanDraw = routeFactor.mCanDraw;
        routeResult.mMayBeErrorLocation = routeFactor.mMayBeErrorLocation;
        //计算当前定位点的下一个形状点下标
        int currentIndex = getCurrentIndex(routeFactor.mPathLatLngs, routeFactor.mCroodsInSteps, routeFactor.mCurrentPoint, routeFactor.mCurrentStep);

        if (routeResult.mCanDraw && !routeResult.mMayBeErrorLocation
                && routeFactor.mCurrentLocation != null && currentIndex >= 1) {
            routeResult.mProjection = routeFactor.mProjection;
            long performanceLogTime;
            performanceLogTime = System.currentTimeMillis();
            this.mFakerCurrentLocation = getFakerLocation(routeFactor.mCurrentLocation, routeFactor.mProjection);
            if (this.mFakerCurrentLocation != null) {

                boolean currentIndexChange = false;
                if (this.mCurrentIndex != currentIndex) {
                    this.mCurrentIndex = currentIndex;
                    currentIndexChange = true;
                }
                // full points in list
                fullPointsAndLatLngs(this.mFakerCurrentLocation, routeFactor.mPathLatLngs,
                                     routeFactor.mProjection, routeResult.mCurrentLatLngs,
                                     currentIndexChange
                );

                routeResult.mPreLocation = this.mPreLocation;
                routeResult.mFakeLocation = this.mFakerCurrentLocation;
                routeResult.mFakerPointX = this.mFakerPointX;
                routeResult.mFakerPointY = this.mFakerPointY;
                routeResult.mCurrentIndex = this.mCurrentIndex;
                routeResult.mDrawIndex = this.mDrawIndex == 1 ? this.mCurrentIndex : this.mDrawIndex;
                routeResult.mCurrentLocation = routeFactor.mCurrentLocation;
                routeResult.mFakeOver = this.mFakeOver;

                // FIXME: 2016/6/12
                //处理由于index值得改变导致faker点与形状点的距离计算本身就是错误的(因为此时faker点处于的形状点范围与真实的形状点范围是一样的,index已经加1了,但是faker点实际还是前一个形状点处)
                //if the faker latlng to next latlng`s distance bigger than last latlng to next latlng`s distance , error.
                //只需要在currentIndexChange为true时处理,只有此时才可能发生这种情况
                if (currentIndexChange) {
                    routeResult.mDrawIndex = routeResult.mCurrentIndex;
                float distance_diff = AMapUtils.calculateLineDistance(DrawUtils.naviLatLng2LatLng(routeResult.mFakeLocation.getCoord()), DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(1)))
                        - AMapUtils.calculateLineDistance(DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(0)), DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(1)));
                int index = 2;
                double offsetHeight = routeResult.mFakerPointY -
                        routeResult.mProjection.toScreenLocation(DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(0))).y;

                HaloLogger.logE("route_log_info", "=========current index change start=============");
                HaloLogger.logE("route_log_info", "currentIndex:" + routeResult.mCurrentIndex);
                HaloLogger.logE("route_log_info", "darwIndex:" + routeResult.mDrawIndex);
                HaloLogger.logE("route_log_info", "distance : " + distance_diff);
                //处理绘制点落在currentLatLngs集合第一个点之前的情况
                while (this.mCurrentIndex - index >= 0 && distance_diff >= 0) {
                    routeResult.mDrawIndex = this.mCurrentIndex - index + 1;
                    HaloLogger.logE("route_log_info", "********** add ************ point ,darwIndex:" + routeResult.mDrawIndex);
//                    HaloLogger.logE("route_log_info_test___", "********** add ************ point ,darwIndex:" + routeResult.mDrawIndex);
                    routeResult.mCurrentLatLngs.add(0, routeFactor.mPathLatLngs.get(this.mCurrentIndex - index));
                    distance_diff = AMapUtils.calculateLineDistance(DrawUtils.naviLatLng2LatLng(routeResult.mFakeLocation.getCoord()), DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(1)))
                            - AMapUtils.calculateLineDistance(DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(0)), DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(1)));
                    HaloLogger.logE("route_log_info", "distance : " + distance_diff);
                    index++;
                }
                HaloLogger.logE("route_log_info", "=========current index change end===============");

                }

                // FIXME: 2016/6/12
                // drawIndex <= currentIndex这个条件是否有必要,已经证实了是可能的,就是说绘制点跑到了GPS点之后
                // 那么这种情况下我们是否应该控制drawIndex?
                // 控制drawIndex是没有必要的,虽然理论上都让我Index不应该大于currentIndex,但是根据数据 返回绘制这种情况是有可能的,不能直接就限制死
                //处理currentLatLngs中补充点之后,绘制点经过某个点之后需要从currentLatLngs中移除补充点的情况
                //if (!currentIndexChange) {
                HaloLogger.logE("route_log_info", "=========not change update start=============");
                float distance_diff_ = AMapUtils.calculateLineDistance(DrawUtils.naviLatLng2LatLng(routeResult.mFakeLocation.getCoord()), DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(0)))
                        - AMapUtils.calculateLineDistance(DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(1)), DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(0)));
                Point point = routeResult.mProjection.toScreenLocation(DrawUtils.naviLatLng2LatLng(routeResult.mFakeLocation.getCoord()));
                float height_diff = (point.y - routeResult.mProjection.toScreenLocation(DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(0))).y)
                        -(routeResult.mProjection.toScreenLocation(DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(1))).y - routeResult.mProjection.toScreenLocation(DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(0))).y);
                HaloLogger.logE("route_log_info", "currentIndex:" + routeResult.mCurrentIndex);
                HaloLogger.logE("route_log_info", "darwIndex:" + routeResult.mDrawIndex);
                HaloLogger.logE("route_log_info", "distance : " + distance_diff_);
                HaloLogger.logE("route_log_info", "faker : " + mFakerCurrentLocation.getCoord().getLatitude() + "," + mFakerCurrentLocation.getCoord().getLongitude()
                        + ",  fake point:"+routeResult.mProjection.toScreenLocation(DrawUtils.naviLatLng2LatLng(mFakerCurrentLocation.getCoord())));
                while ((distance_diff_ >= 0)&& routeResult.mCurrentLatLngs.size() > 2 && routeResult.mDrawIndex < routeFactor.mPathLatLngs.size() - 1) {
                    routeResult.mCurrentLatLngs.remove(0);
                    routeResult.mDrawIndex++;
                    HaloLogger.logE("route_log_info", "********** remove ************ point ,darwIndex:" + routeResult.mDrawIndex);
//                    HaloLogger.logE("route_log_info_test___", "********** remove ************ point ,darwIndex:" + routeResult.mDrawIndex);
                    height_diff = (point.y - routeResult.mProjection.toScreenLocation(DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(0))).y)
                            -(routeResult.mProjection.toScreenLocation(DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(1))).y - routeResult.mProjection.toScreenLocation(DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(0))).y);
                    distance_diff_ = AMapUtils.calculateLineDistance(DrawUtils.naviLatLng2LatLng(routeResult.mFakeLocation.getCoord()), DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(0)))
                            - AMapUtils.calculateLineDistance(DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(1)), DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(0)));
                    HaloLogger.logE("route_log_info", "distance : " + distance_diff_);
                }
//                HaloLogger.logE("route_log_info_test", "origin points size : " + routeResult.mCurrentLatLngs.size() + ",points:" + routeResult.mCurrentLatLngs + "");
                HaloLogger.logE("route_log_info", "=========not change update end===============");
                HaloLogger.logE("route_log_info", "\n\n");
                //}

                if(currentIndexChange && routeResult.mDrawIndex == this.mDrawIndex){

                }

                //将当前drawIndex保存下来供下次使用
                this.mDrawIndex = routeResult.mDrawIndex;

                // if currentPoints is null or its size is zero , clear the points and return.
                if (routeResult.mCurrentLatLngs == null || routeResult.mCurrentLatLngs.size() <= 1) {
                    routeResult.mCurrentLatLngs.clear();
                    return routeResult;
                }
                routeResult.mCurrentPoints.clear();
                for (int i = 0; i < routeResult.mCurrentLatLngs.size(); i++) {
                    PointF tPoint = new PointF(routeResult.mProjection.toScreenLocation(DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(i))));
                    routeResult.mCurrentPoints.add(tPoint);
                }

                Point fakePoint = routeResult.mProjection.toScreenLocation(DrawUtils.naviLatLng2LatLng(routeResult.mFakeLocation.getCoord()));
                routeResult.mFakerPointX = fakePoint.x;
                routeResult.mFakerPointY = fakePoint.y;

                // FIXME: 16/6/15
                if(true){
                    if(routeResult.mCurrentLatLngs != null && routeResult.mCurrentLatLngs.size()>3){
                    /*HaloLogger.logE("route_log_info_test___", "points size : " + routeResult.mCurrentLatLngs.size() + ",points:" + routeResult.mCurrentLatLngs.get(0)
                            + routeResult.mCurrentLatLngs.get(1)+routeResult.mCurrentLatLngs.get(2)+routeResult.mCurrentLatLngs.get(3));*/
                        HaloLogger.logE("route_log_info", "points size : " + routeResult.mCurrentLatLngs.size() + ",points:" + routeResult.mCurrentLatLngs);
                    }
                    HaloLogger.logE("route_log_info", "currentIndex:" + routeResult.mCurrentIndex+"   ,darwIndex:" + routeResult.mDrawIndex+", FakeOver :"+routeResult.mFakeOver);
                }

                HaloLogger.logE("route_log_info", " origion points size : " + routeResult.mCurrentPoints.size() + ",points:" + routeResult.mCurrentPoints + "");

                LatLng testLatLng = DrawUtils.naviLatLng2LatLng(routeResult.mPreLocation.getCoord());
                Point testPoint;
                testPoint = routeResult.mProjection.toScreenLocation(testLatLng);
                HaloLogger.logE("route_log_info", " pre FakeLocation" +",Latlng :"+testLatLng+",Points :"+testPoint);
                testLatLng = DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLocation.getCoord());
                testPoint = routeResult.mProjection.toScreenLocation(testLatLng);
                HaloLogger.logE("route_log_info", " Current FakeLocation" +",Latlng :"+testLatLng+",Points :"+testPoint);
                HaloLogger.logI("route_log_info_test_performance","=========performance_log=========== calculate time = "+ (System.currentTimeMillis()-performanceLogTime));
                //                //if the point1 is look like point2 , remove it.
                //                for (int i = 1; i < routeResult.mCurrentPoints.size(); i++) {
                //                    Point p1 = routeResult.mCurrentPoints.get(i - 1);
                //                    Point p2 = routeResult.mCurrentPoints.get(i);
                //                    if (Math.abs(p1.y - p2.y) < 3 && Math.abs(p1.x - p2.x) < 3) {
                //                        routeResult.mCurrentPoints.remove(i);
                //                        i--;
                //                    }
                //                }

                //create next road name and its position.
                //                routeResult.mHasNextRoadName = routeFactor.mNextRoadName != null && routeFactor.mNextRoadName.length() > 0;
                //                if (routeResult.mHasNextRoadName) {
                //                    routeResult.mNextRoadName = routeFactor.mNextRoadName;
                //                    routeResult.mNextRoadType = routeFactor.mNextRoadType;
                //                    routeResult.mNextRoadNamePosition = null;
                //                    List<NaviLatLng> latLngs = routeFactor.mRoadNameLatLngs.get(routeFactor.mNextRoadName);
                //                    if (latLngs != null && latLngs.size() >= 1) {
                //                        for (int i = 1; i < routeResult.mCurrentLatLngs.size(); i++) {
                //                            NaviLatLng latLng = routeFactor.mPathLatLngs.get(this.mCurrentIndex + i - 1);
                //                            if (latLngs.contains(latLng)) {
                //                                routeResult.mNextRoadNamePosition = latLng;
                //                                break;
                //                            }
                //                        }
                //                    }
                //                }
            }
        }
        return routeResult;
    }

    private void fullPointsAndLatLngs(AMapNaviLocation fakerLocation, List<NaviLatLng> pathLatLngs, Projection projection,
                                      List<NaviLatLng> currentLatLngs, boolean currentIndexChange) {
        //NaviLatLng prePreLatLng = prePreLocation.getCoord();
        if (fakerLocation == null || pathLatLngs == null || pathLatLngs.size() <= 0) {
            return;
        }
        if (currentIndexChange) {
            float totalLength = 0;
            currentLatLngs.clear();
            Point currentScreenPoint = projection
                    .toScreenLocation(DrawUtils.naviLatLng2LatLng(pathLatLngs.get(mCurrentIndex - 1)));
            currentLatLngs.add(pathLatLngs.get(mCurrentIndex - 1));

            for (int i = mCurrentIndex; i < pathLatLngs.size(); i++) {
                NaviLatLng pathLatLng = pathLatLngs.get(i);
                Point pathPoint = projection
                        .toScreenLocation(DrawUtils.naviLatLng2LatLng(pathLatLng));
                float distance = 0;
                if (i == mCurrentIndex) {
                    if (currentScreenPoint.equals(pathPoint)) {
                        continue;
                    }
                    float curPoint2NextPointDist = AMapUtils
                            .calculateLineDistance(
                                    DrawUtils.naviLatLng2LatLng(fakerLocation.getCoord()),
                                    DrawUtils.naviLatLng2LatLng(pathLatLngs.get(i)));
                    distance = curPoint2NextPointDist;
                    totalLength += curPoint2NextPointDist;
                } else {
                    if (pathPoint.equals(projection.toScreenLocation(
                            DrawUtils.naviLatLng2LatLng(pathLatLngs.get(i - 1))))) {
                        continue;
                    }
                    distance = AMapUtils.calculateLineDistance(
                            DrawUtils.naviLatLng2LatLng(pathLatLngs.get(i - 1)),
                            DrawUtils.naviLatLng2LatLng(pathLatLngs.get(i)));
                    totalLength += distance;
                }
                //be sure the total distance is HUDWAY_LENGTH_IN_SCREEN
                if (totalLength == HUDWAY_LENGTH_IN_SCREEN) {
                    currentLatLngs.add(pathLatLng);
                    return;
                } else if (totalLength > HUDWAY_LENGTH_IN_SCREEN) {
                    float div = totalLength - HUDWAY_LENGTH_IN_SCREEN;
                    Point prePoint = null;
                    if (i == mCurrentIndex) {
                        prePoint = projection
                                .toScreenLocation(DrawUtils.naviLatLng2LatLng(fakerLocation.getCoord()));
                    } else {
                        prePoint = projection
                                .toScreenLocation(DrawUtils.naviLatLng2LatLng(pathLatLngs.get(i - 1)));
                    }
                    Point makePoint = new Point(
                            (int) (prePoint.x + (pathPoint.x - prePoint.x) * ((distance - div) / distance)),
                            (int) (prePoint.y + (pathPoint.y - prePoint.y) * ((distance - div) / distance)));
                    currentLatLngs.add(DrawUtils.latLng2NaviLatLng(projection.fromScreenLocation(makePoint)));
                    return;
                } else {
                    currentLatLngs.add(pathLatLng);
                }
            }
        } else {
            //FIXME: 2016/6/11 此处应该是保证route是平滑增长而不是一段一段出现的,目前在调试抖动问题因此暂时先不处理,后续加上
            /*//remove make latlng
            currentLatLngs.remove(currentLatLngs.size() - 1);
            float curPoint2NextPointDist = AMapUtils
                    .calculateLineDistance(
                            DrawUtils.naviLatLng2LatLng(fakerLocation.getCoord()),
                            DrawUtils.naviLatLng2LatLng(currentLatLngs.get(1)));
            float totalLength = curPoint2NextPointDist;
            for (int i = 1; i < currentLatLngs.size() - 1; i++) {
                totalLength += AMapUtils
                        .calculateLineDistance(
                                DrawUtils.naviLatLng2LatLng(currentLatLngs.get(i)),
                                DrawUtils.naviLatLng2LatLng(currentLatLngs.get(i + 1)));
            }
            float div = totalLength - HUDWAY_LENGTH_IN_SCREEN;*/


        }
        return;
    }

    private int getCurrentIndex(List<NaviLatLng> pathLatLngs, List<Integer> croodsInSteps, int currentPoint, int currentStep) {
        int currentIndex = 0;
        for (int i = 0; i < currentStep; i++) {
            currentIndex += croodsInSteps.get(i);
        }
        currentIndex += currentPoint + 1;
        if (currentIndex >= pathLatLngs.size()) {
            currentIndex = pathLatLngs.size() - 1;
        }
        return currentIndex;
    }

    private AMapNaviLocation getFakerLocation(AMapNaviLocation currentLocation, Projection projection) {
        //if we do get the location not yet , return null.
        if (currentLocation == null) {
            return null;
        }
        //if prePreLocation is null , set the value to it , and return null
        if (mPreLocation == null) {
            mPreLocation = currentLocation;
            mPreTime = System.currentTimeMillis();
            return null;
        }
        mCurrentFramesCounter++;
        //if mPreLocation is null , so this is the first step to draw
        if (mCurrentLocation == null) {
            mCurrentLocation = currentLocation;
            mCurrentTime = System.currentTimeMillis();

        } else if (mCurrentLocation != currentLocation) {
            mPreLocation = mFakerCurrentLocation == null ? mCurrentLocation : mFakerCurrentLocation;
            mPreTime = mCurrentTime;
            mCurrentLocation = currentLocation;
            mCurrentTime = System.currentTimeMillis();
        }
        long currentFrameTime = System.currentTimeMillis();
        long diff = currentFrameTime - (mCurrentTime - mPreTime) - mPreTime;
        long diff_pre_cur = mCurrentTime - mPreTime;
        if (diff >= 0 && diff < diff_pre_cur) {
            HaloLogger.logE("empty_points_count", "=======================normal======================");
            Point prePrePoint = projection.toScreenLocation(
                    DrawUtils.naviLatLng2LatLng(mPreLocation.getCoord()));
            Point prePoint = projection.toScreenLocation(
                    DrawUtils.naviLatLng2LatLng(mCurrentLocation.getCoord()));
            this.mFakerPointX = (prePrePoint.x + (prePoint.x - prePrePoint.x) * (1.0 * diff / diff_pre_cur));
            this.mFakerPointY = (prePrePoint.y + (prePoint.y - prePrePoint.y) * (1.0 * diff / diff_pre_cur));
            Point point = new Point((int) this.mFakerPointX, (int) this.mFakerPointY);
            AMapNaviLocation location = new AMapNaviLocation();
            location.setCoord(DrawUtils.latLng2NaviLatLng(projection.fromScreenLocation(point)));
            // FIXME: 16/6/22 经纬度直接线性插值，效果好些，需要必成非线性插值
            if (false){
                double lan = mPreLocation.getCoord().getLatitude()+(mCurrentLocation.getCoord().getLatitude()-mPreLocation.getCoord().getLatitude())* (1.0 * diff / diff_pre_cur);
                double lng = mPreLocation.getCoord().getLongitude()+(mCurrentLocation.getCoord().getLongitude()-mPreLocation.getCoord().getLongitude())* (1.0 * diff / diff_pre_cur);
                location.setCoord(new NaviLatLng(lan,lng));
            }
            mFakeOver = false;
            return location;
        } else {
            HaloLogger.logE("empty_points_count", "=================error=============");
            Point point = projection.toScreenLocation(DrawUtils.naviLatLng2LatLng(mFakerCurrentLocation.getCoord()));
            this.mFakerPointX = point.x;
            this.mFakerPointY = point.y;
            mFakeOver = true;
            return mFakerCurrentLocation;
        }
    }

    //    private AMapNaviLocation getFakerLocation(AMapNaviLocation currentLocation, Projection projection) {
    //        //if we do get the location not yet , return null.
    //        if (currentLocation == null) {
    //            return null;
    //        }
    //        //if prePreLocation is null , set the value to it , and return null
    //        if (mPreLocation == null) {
    //            mPreLocation = currentLocation;
    //            return null;
    //        }
    //        mCurrentFramesCounter++;
    //        //if mPreLocation is null , so this is the first step to draw
    //        if (mCurrentLocation == null) {
    //            mCurrentLocation = currentLocation;
    //            mCurrent = 1;
    //
    //            mPreviousFramesCounter = mCurrentFramesCounter;
    //            mCurrentFramesCounter = 0;
    //        } else if (mCurrentLocation != currentLocation) {
    //            mPreLocation = mFakerCurrentLocation == null ? mCurrentLocation : mFakerCurrentLocation;
    //            mCurrentLocation = currentLocation;
    //            mCurrent = 1;
    //
    //            // FIXME: 2016/6/14  临时将mCurrentFramesCounter+5后赋值给mPreviousFramesCounter,减少下方进else的此处
    //            mPreviousFramesCounter = mCurrentFramesCounter+5;
    //            mCurrentFramesCounter = 0;
    //        }
    //        HaloLogger.logE("empty_points_count", "=======================one frame======================");
    //        if (mPreviousFramesCounter != 0 && mCurrent <= mPreviousFramesCounter) {
    //            Point prePrePoint = projection.toScreenLocation(
    //                    DrawUtils.naviLatLng2LatLng(mPreLocation.getCoord()));
    //            Point prePoint = projection.toScreenLocation(
    //                    DrawUtils.naviLatLng2LatLng(mCurrentLocation.getCoord()));
    //            this.mFakerPointX = (prePrePoint.x + (prePoint.x - prePrePoint.x) * (1.0 * mCurrent / mPreviousFramesCounter));
    //            this.mFakerPointY = (prePrePoint.y + (prePoint.y - prePrePoint.y) * (1.0 * mCurrent / mPreviousFramesCounter));
    //            Point point = new Point((int) this.mFakerPointX, (int) this.mFakerPointY);
    //            AMapNaviLocation location = new AMapNaviLocation();
    //            location.setCoord(DrawUtils.latLng2NaviLatLng(projection.fromScreenLocation(point)));
    //            mCurrent++;
    //            return location;
    //        } else {
    //            if (mPreviousFramesCounter == 0) {
    //                HaloLogger.logE("empty_points_count", "mPreviousFramesCounter == 0");
    //            } else if (mCurrent > mPreviousFramesCounter) {
    //                HaloLogger.logE("empty_points_count", "mCurrent > mPreviousFramesCounter");
    //            } else {
    //                HaloLogger.logE("empty_points_count", "unknown");
    //            }
    //            Point point = projection.toScreenLocation(DrawUtils.naviLatLng2LatLng(mFakerCurrentLocation.getCoord()));
    //            this.mFakerPointX = point.x;
    //            this.mFakerPointY = point.y;
    //            return mFakerCurrentLocation;
    //        }
    //    }
}


