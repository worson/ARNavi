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

    private static final float HUDWAY_LENGTH_IN_SCREEN = 350;

    private AMapNaviLocation mPrePreLocation       = null;
    private AMapNaviLocation mPreLocation          = null;
    private AMapNaviLocation mFakerCurrentLocation = null;

    private int    mCurrentFramesCounter  = 0;
    private long   mPreviousFramesCounter = 0l;
    private int    mCurrent               = -1;
    private double mRealStartPointX       = 0f;
    private double mRealStartPointY       = 0f;
    private int    mCurrentIndex          = 1;

    private static RouteCalculator mRouteCalculator = new RouteCalculator();

    private RouteCalculator() {}

    public static RouteCalculator getInstance() {
        return mRouteCalculator;
    }

    @Override
    public RouteResult calculate(RouteFactor routeFactor) {
        RouteResult routeResult = RouteResult.getInstance();
        routeResult.reset();

        routeResult.mProjection = routeFactor.mProjection;

        //RouteFactor:
        //canDraw , maybeErrorLocation , currentDist,currentPoint,currentStep
        //mStartLocation , mPathLatLngs, mCroodsInStep.

        //fullPointsAndLatLngs + handle points

        //if we can draw , and current location is a useful location.
        if (routeResult.mCanDraw = routeFactor.mCanDraw && !(routeResult.mMayBeErrorLocation = routeFactor.mMayBeErrorLocation)
                && routeFactor.mStartLocation != null) {
            this.mFakerCurrentLocation = getFakerLocation(routeFactor.mStartLocation, routeFactor.mProjection);
            if (this.mFakerCurrentLocation != null) {
                // full points in list
                fullPointsAndLatLngs(this.mFakerCurrentLocation,
                                     routeFactor.mPathLatLngs, routeFactor.mCroodsInSteps,
                                     routeFactor.mProjection, routeFactor.mCurrentPoint,
                                     routeFactor.mCurrentStep, routeResult.mCurrentPoints,
                                     routeResult.mCurrentLatLngs
                );

                // if currentPoints is null or it`s size is zero , return routeResult.
                if (routeResult.mCurrentPoints == null || routeResult.mCurrentPoints.size() <= 1) {
                    routeResult.mCurrentPoints.clear();
                    return routeResult;
                }

                // The first point in list may be have some little error,so we could not use it.
                // We should be use realStartPoint as first point.
                routeResult.mCurrentPoints.remove(0);
                routeResult.mCurrentPoints.add(0, new Point(
                        (int) Math.rint(this.mRealStartPointX), (int) Math.rint(this.mRealStartPointY)));

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
                routeResult.mHasNextRoadName = routeFactor.mNextRoadName != null;
                if (routeResult.mHasNextRoadName) {
                    routeResult.mNextRoadName = routeFactor.mNextRoadName;
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

    private void fullPointsAndLatLngs(AMapNaviLocation aMapNaviLocation, List<NaviLatLng> pathLatLngs, List<Integer> croodsInSteps, Projection projection,
                                      int currentPoint, int currentStep, List<Point> points, List<NaviLatLng> currentLatLngs) {
        NaviLatLng currentLatLng = aMapNaviLocation.getCoord();
        if (currentLatLng == null || pathLatLngs == null || pathLatLngs.size() <= 0) {
            return;
        }
        float totalLength = 0;
        points.clear();
        currentLatLngs.clear();
        Point currentScreenPoint = projection
                .toScreenLocation(DrawUtils.naviLatLng2LatLng(currentLatLng));
        points.add(currentScreenPoint);
        currentLatLngs.add(currentLatLng);

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
                                DrawUtils.naviLatLng2LatLng(currentLatLng),
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
                            .toScreenLocation(DrawUtils.naviLatLng2LatLng(currentLatLng));
                } else {
                    prePoint = projection
                            .toScreenLocation(DrawUtils.naviLatLng2LatLng(pathLatLngs.get(i - 1)));
                }
                Point makePoint = new Point(
                        (int) (prePoint.x + (pathPoint.x - prePoint.x) * ((distance - div) / distance)),
                        (int) (prePoint.y + (pathPoint.y - prePoint.y) * ((distance - div) / distance)));
                points.add(makePoint);
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
        //if mPreLocation is null , so this is the first step to draw
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
            this.mRealStartPointX = (prePrePoint.x + (prePoint.x - prePrePoint.x) * (1.0 * mCurrent / mPreviousFramesCounter));
            this.mRealStartPointY = (prePrePoint.y + (prePoint.y - prePrePoint.y) * (1.0 * mCurrent / mPreviousFramesCounter));
            Point point = new Point((int) this.mRealStartPointX, (int) this.mRealStartPointY);
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
