package com.haloai.hud.hudendpoint.arwaylib.bean.impl_opengl;

import android.graphics.Bitmap;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.Projection;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviLatLng;
import com.haloai.hud.hudendpoint.arwaylib.bean.SuperBean;
import com.haloai.hud.hudendpoint.arwaylib.utils.DrawUtils;

import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.bean.impl;
 * project_name : hudlauncher;
 */
public class OpenglRouteBean extends SuperBean {
    private static final int CORRECTING_DISTANCE = 50;
    private static final int GPS_SET_COUNTER = 3;

    private List<Vector3> mPathPoints = new ArrayList<Vector3>();
    private int mAllLength = 0;

    private List<NaviLatLng>              mPathLatLngs     = new ArrayList<NaviLatLng>();
    private List<Integer>                 mCroodsInSteps   = new ArrayList<Integer>();
    private Map<String, List<NaviLatLng>> mRoadNameLatLngs = new HashMap<String, List<NaviLatLng>>();


    private Projection       mProjection      = null;
    private AMapNaviLocation mCurrentLocation = null;
    private AMapNaviLocation mPreLocation     = null;
    private AMapNaviPath     mAMapNaviPath    = null;
    private Bitmap           mCrossImage      = null;

    private boolean mMayBeErrorLocation = true;

    private boolean      mCanDrawHudway   = false;
    private int          mCurrentPoint    = 0;
    private int          mCurrentStep     = 0;
    private int          mCurrentDistance = 0;
    private double       mRealStartPointX = 0f;
    private double       mRealStartPointY = 0f;
    private String       mNextRoadName    = null;
    private NextRoadType mNextRoadType    = null;

    private int mGpsNumberSetCounter = 1;
    private int mGpsNumber = 0;
    private int mGpsNumberSum = 0;

    private static final int NOT_MATCH_PATH_NUBER = 1;
    private int mIsNotMactchPathCounter = 0;
    private boolean mIsMatchNaviPath = true;

    public enum NextRoadType {
        LEFT,
        BACK,
        NONE,
        RIGHT
    }

    /**
     * without mCanDrawHudway
     */
    @Override
    public void reset() {
        mCurrentLocation = null;
        mPreLocation = null;
        mAMapNaviPath = null;
        mCrossImage = null;
        mCurrentPoint = 0;
        mCurrentStep = 0;
        mCurrentDistance = 0;
        mMayBeErrorLocation = true;
        mNextRoadName = null;
        mNextRoadType = null;
        mPathLatLngs.clear();
        mCroodsInSteps.clear();
        mRoadNameLatLngs.clear();
        mGpsNumber = 0;
        mGpsNumberSetCounter = 1;
        mGpsNumberSum = 0;
        //opengl
        mAllLength=0;
        mPathPoints=null;
    }

    /***
     * 进行了连续几次取平均操作
     * */
    public OpenglRouteBean setGpsNumber(int gpsNumber) {
        mGpsNumber = gpsNumber;
        //平均处理
        /*++mGpsNumberSetCounter;
        mGpsNumberSum += gpsNumber;
        mGpsNumber = mGpsNumberSum / mGpsNumberSetCounter;
        if ( mGpsNumberSetCounter > GPS_SET_COUNTER) {
            mGpsNumberSetCounter = 0;
            mGpsNumberSum = 0;
        }*/
        return this;
    }

    public int getAllLength() {
        return mAllLength;
    }

    public OpenglRouteBean setAllLength(int allLength) {
        mAllLength = allLength;
        return this;
    }

    public int getGpsNumber() {
        return mGpsNumber;
    }

    public boolean isMayBeErrorLocation() {
        return mMayBeErrorLocation;
    }

    public int getCurrentDistance() {
        return mCurrentDistance;
    }

    public List<Integer> getCroodsInSteps() {
        return mCroodsInSteps;
    }

    public List<NaviLatLng> getPathLatLngs() {
        return mPathLatLngs;
    }

    public AMapNaviPath getPath() {
        return mAMapNaviPath;
    }

    public OpenglRouteBean setPathPoints(List<Vector3> pathPoints) {
        if(pathPoints==null && pathPoints.size()<=0){
            return this;
        }else {
            if(mPathPoints.size()>0){
                mPathPoints.clear();
            }
            mPathPoints.addAll(pathPoints);
        }
        return this;
    }

    public List<Vector3> getPathPoints() {
        return mPathPoints;
    }
    public boolean isCanDrawHudway() {
        return mCanDrawHudway;
    }

    public OpenglRouteBean setCanDrawARway(boolean canDrawHudway) {
        mCanDrawHudway = canDrawHudway;
        return this;
    }

    public Bitmap getCrossImage() {
        return mCrossImage;
    }

    public OpenglRouteBean setCrossImage(Bitmap crossImage) {
        mCrossImage = crossImage;
        return this;
    }

    public AMapNaviLocation getCurrentLocation() {
        return mCurrentLocation;
    }

    public AMapNaviLocation getPreLocation(){
        return mPreLocation;
    }

    public boolean isMatchNaviPath() {
        return mIsMatchNaviPath;
    }

    public OpenglRouteBean setCurrentLocation(AMapNaviLocation currentLocation) {
        mPreLocation = mCurrentLocation;
        mCurrentLocation = currentLocation;
        if(!currentLocation.isMatchNaviPath()){
            if(++mIsNotMactchPathCounter > NOT_MATCH_PATH_NUBER){
                mIsNotMactchPathCounter = 0;
                mIsMatchNaviPath = false;
            }
        }else {
            if (!mIsMatchNaviPath){
                mIsMatchNaviPath = true;
            }
        }
        if (mPathLatLngs == null || mPathLatLngs.size()<=0){
            return this;
        }
        if (mCurrentDistance < CORRECTING_DISTANCE) {
            if (mPreLocation != null) {
                //如果distance小于1m，就判定为不是车的移动而是location持续返回的误差，就不将其加入到mCurrentDistance中
                float distance = AMapUtils.calculateLineDistance(
                        DrawUtils.naviLatLng2LatLng(mCurrentLocation.getCoord()),
                        DrawUtils.naviLatLng2LatLng(mPreLocation.getCoord()));
                distance = distance < 1 ? 0 : distance;
                this.mCurrentDistance += distance;
            }
            mMayBeErrorLocation = mCurrentDistance <= CORRECTING_DISTANCE;
        } else {
            mMayBeErrorLocation = false;
        }
        return this;
    }

    public int getCurrentPoint() {
        return mCurrentPoint;
    }

    public OpenglRouteBean setCurrentPoint(int currentPoint) {
        mCurrentPoint = currentPoint;
        return this;
    }

    public int getCurrentStep() {
        return mCurrentStep;
    }

    public OpenglRouteBean setCurrentStep(int currentStep) {
        mCurrentStep = currentStep;
        return this;
    }

    public Projection getProjection() {
        return mProjection;
    }

    public OpenglRouteBean setProjection(Projection projection) {
        mProjection = projection;
        return this;
    }

    public double getRealStartPointX() {
        return mRealStartPointX;
    }

    public double getRealStartPointY() {
        return mRealStartPointY;
    }

    public OpenglRouteBean setNextRoadNameAndType(String nextRoadName, NextRoadType nextRoadType) {
        mNextRoadName = nextRoadName;
        mNextRoadType = nextRoadType;
        return this;
    }

    public String getNextRoadName() {
        return mNextRoadName;
    }

    public NextRoadType getNextRoadType(){
        return mNextRoadType;
    }

    public Map<String, List<NaviLatLng>> getRoadNameLatLngs() {
        return mRoadNameLatLngs;
    }
}
