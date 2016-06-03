package com.haloai.hud.hudendpoint.arwaylib.calculator.impl;

import android.graphics.Point;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.Projection;
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

    private static final float HUDWAY_LENGTH_IN_SCREEN = 400;

    private AMapNaviLocation mPrePreLocation       = null;
    private AMapNaviLocation mPreLocation          = null;
    private AMapNaviLocation mFakerCurrentLocation = null;

    private int    mCurrentFramesCounter  = 0;
    private long   mPreviousFramesCounter = 0l;
    private int    mCurrent               = -1;
    private double mFakerPointX           = 0f;
    private double mFakerPointY           = 0f;
    private int    mCurrentIndex          = 1;

    private static RouteCalculator mRouteCalculator = new RouteCalculator();

    private RouteCalculator() {}

    public static RouteCalculator getInstance() {
        return mRouteCalculator;
    }

    @Override
    public void reset() {
        mPrePreLocation = null;
        mPreLocation = null;
        mFakerCurrentLocation = null;

        mCurrentFramesCounter = 0;
        mPreviousFramesCounter = 0l;
        mCurrent = -1;
        mFakerPointX = 0f;
        mFakerPointY = 0f;
        mCurrentIndex = 1;
    }

    @Override
    public RouteResult calculate(RouteFactor routeFactor) {
        RouteResult routeResult = RouteResult.getInstance();
        routeResult.reset();

        //fullPointsAndLatLngs + handle points

        //if we can draw , and current location is a useful location.
        if (routeResult.mCanDraw = routeFactor.mCanDraw && !(routeResult.mMayBeErrorLocation = routeFactor.mMayBeErrorLocation)
                && routeFactor.mPreLocation != null && mCurrentIndex>=1) {
            routeResult.mProjection = routeFactor.mProjection;
            this.mFakerCurrentLocation = getFakerLocation(routeFactor.mPreLocation, routeFactor.mProjection);
            if (this.mFakerCurrentLocation != null) {
                // full points in list
                fullPointsAndLatLngs(this.mFakerCurrentLocation,this.mPrePreLocation,
                                     routeFactor.mPathLatLngs, routeFactor.mCroodsInSteps,
                                     routeFactor.mProjection, routeFactor.mCurrentPoint,
                                     routeFactor.mCurrentStep, routeResult.mCurrentPoints,
                                     routeResult.mCurrentLatLngs
                );



                routeResult.mPrePreLocation = this.mPrePreLocation;
                routeResult.mFakeLocation = this.mFakerCurrentLocation;
                routeResult.mFakerPointX = this.mFakerPointX;
                routeResult.mFakerPointY = this.mFakerPointY;
                routeResult.mCurrentIndex = this.mCurrentIndex;
                routeResult.mCurrentLocation = routeFactor.mPreLocation;

                //处理由于index值得改变导致faker点与形状点的距离计算本身就是错误的(因为此时faker点处于的形状点范围与真实的形状点范围是一样的,index已经加1了,但是faker点实际还是前一个形状点处)
                //if the faker latlng to next latlng`s distance bigger than last latlng to next latlng`s distance , error.
                float distance_diff = AMapUtils.calculateLineDistance(DrawUtils.naviLatLng2LatLng(routeResult.mFakeLocation.getCoord()),DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(1)))
                        - AMapUtils.calculateLineDistance(DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(0)),DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(1)));
                int index = 2;
//                if(distance_diff<0){
//                    routeResult.mFlag = false;
//                }else{
//                    routeResult.mFlag = true;
//                }
                while(this.mCurrentIndex-index>=0 && distance_diff >= 0) {
                    routeResult.mCurrentLatLngs.add(0, routeFactor.mPathLatLngs.get(this.mCurrentIndex - index));
                    routeResult.mCurrentPoints.add(0,routeFactor.mProjection.toScreenLocation(DrawUtils.naviLatLng2LatLng(routeFactor.mPathLatLngs.get(this.mCurrentIndex - index))));
                    distance_diff = AMapUtils.calculateLineDistance(DrawUtils.naviLatLng2LatLng(routeResult.mFakeLocation.getCoord()),DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(1)))
                            - AMapUtils.calculateLineDistance(DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(0)),DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(1)));
                    index++;
                }

                // if currentPoints is null or it`s size is zero , clear the points and return.
                if (routeResult.mCurrentPoints == null || routeResult.mCurrentPoints.size() <= 1) {
                    routeResult.mCurrentPoints.clear();
                    return routeResult;
                }

                //if the point1 is look like point2 , remove it.
                for (int i = 1; i < routeResult.mCurrentPoints.size(); i++) {
                    Point p1 = routeResult.mCurrentPoints.get(i - 1);
                    Point p2 = routeResult.mCurrentPoints.get(i);
                    if (Math.abs(p1.y - p2.y) < 3 && Math.abs(p1.x - p2.x) < 3) {
                        routeResult.mCurrentPoints.remove(i);
                        i--;
                    }
                }

                //create next road name and position.
                routeResult.mHasNextRoadName = routeFactor.mNextRoadName != null && routeFactor.mNextRoadName.length() != 0;
                if (routeResult.mHasNextRoadName) {
                    routeResult.mNextRoadName = routeFactor.mNextRoadName;
                    routeResult.mNextRoadType = routeFactor.mNextRoadType;
                    routeResult.mNextRoadNamePosition = null;
                    List<NaviLatLng> latLngs = routeFactor.mRoadNameLatLngs.get(routeFactor.mNextRoadName);
                    if (latLngs != null && latLngs.size() >= 1) {
                        for (int i = 1; i < routeResult.mCurrentPoints.size(); i++) {
                            NaviLatLng latLng = routeFactor.mPathLatLngs.get(this.mCurrentIndex + i - 1);
                            if (latLngs.contains(latLng)) {
                                routeResult.mNextRoadNamePosition = latLng;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return routeResult;
    }

    private void fullPointsAndLatLngs(AMapNaviLocation fakerLocation, AMapNaviLocation prePreLocation, List<NaviLatLng> pathLatLngs, List<Integer> croodsInSteps, Projection projection,
                                      int currentPoint, int currentStep, List<Point> points, List<NaviLatLng> currentLatLngs) {
        //NaviLatLng prePreLatLng = prePreLocation.getCoord();
        if (fakerLocation == null || pathLatLngs == null || pathLatLngs.size() <= 0) {
            return;
        }
        float totalLength = 0;
        points.clear();
        currentLatLngs.clear();
        //TODO helong fix
        Point currentScreenPoint = projection
                .toScreenLocation(DrawUtils.naviLatLng2LatLng(pathLatLngs.get(mCurrentIndex-1)));
        points.add(currentScreenPoint);
        currentLatLngs.add(pathLatLngs.get(mCurrentIndex-1));

        //get current next point`s index in path
        this.mCurrentIndex = getCurrentIndex(pathLatLngs, croodsInSteps, currentPoint, currentStep);

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
                points.add(pathPoint);
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
                points.add(makePoint);
                currentLatLngs.add(DrawUtils.latLng2NaviLatLng(projection.fromScreenLocation(makePoint)));
                return;
            } else {
                points.add(pathPoint);
                currentLatLngs.add(pathLatLng);
            }
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
        if (mPrePreLocation == null) {
            mPrePreLocation = currentLocation;
            return null;
        }
        mCurrentFramesCounter++;
        //if mPrePreLocation is null , so this is the first step to draw
        if (mPreLocation == null) {
            mPreLocation = currentLocation;
            mCurrent = 1;

            mPreviousFramesCounter = mCurrentFramesCounter;
            mCurrentFramesCounter = 0;
        } else if (mPreLocation != currentLocation) {
            mPrePreLocation = mFakerCurrentLocation == null ? mPreLocation : mFakerCurrentLocation;
            mPreLocation = currentLocation;
            mCurrent = 1;

            mPreviousFramesCounter = mCurrentFramesCounter;
            mCurrentFramesCounter = 0;
        }
        if (mPreviousFramesCounter != 0 && mCurrent <= mPreviousFramesCounter) {
            Point prePrePoint = projection.toScreenLocation(
                    DrawUtils.naviLatLng2LatLng(mPrePreLocation.getCoord()));
            Point prePoint = projection.toScreenLocation(
                    DrawUtils.naviLatLng2LatLng(mPreLocation.getCoord()));
            this.mFakerPointX = (prePrePoint.x + (prePoint.x - prePrePoint.x) * (1.0 * mCurrent / mPreviousFramesCounter));
            this.mFakerPointY = (prePrePoint.y + (prePoint.y - prePrePoint.y) * (1.0 * mCurrent / mPreviousFramesCounter));
            Point point = new Point((int) this.mFakerPointX, (int) this.mFakerPointY);
            AMapNaviLocation location = new AMapNaviLocation();

            location.setCoord(DrawUtils.latLng2NaviLatLng(projection.fromScreenLocation(point)));
            mCurrent++;
            return location;
        } else {
            if (mPreviousFramesCounter == 0) {
                HaloLogger.logE("empty_points_count", "mPreviousFramesCounter==0");
            } else if (mCurrent > mPreviousFramesCounter) {
                HaloLogger.logE("empty_points_count", "mCurrent>mPreviousFramesCounter");
            } else {
                HaloLogger.logE("empty_points_count", "unknown");
            }
            return mFakerCurrentLocation;
        }
    }
}
