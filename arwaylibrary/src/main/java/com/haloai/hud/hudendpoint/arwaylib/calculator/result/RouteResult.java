package com.haloai.hud.hudendpoint.arwaylib.calculator.result;

import android.graphics.Point;
import android.graphics.PointF;

import com.amap.api.maps.Projection;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.NaviLatLng;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.RouteBean;

import java.util.ArrayList;
import java.util.List;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.calculator.result;
 * project_name : hudlauncher;
 */
public class RouteResult extends SuperResult{
    public boolean          mCanDraw            = true;
    public List<NaviLatLng> mCurrentLatLngs     = new ArrayList<NaviLatLng>();
    public List<PointF>     mCurrentPoints      = new ArrayList<>();
    public boolean          mMayBeErrorLocation = false;
    public boolean          mHasNextRoadName    = false;
    public String           mNextRoadName       = null;

    private static RouteResult            mRouteResult          = new RouteResult();
    public         Projection             mProjection           = null;
    public         NaviLatLng             mNextRoadNamePosition = null;
    public         AMapNaviLocation       mPreLocation          = null;
    public         AMapNaviLocation       mFakeLocation         = null;
    public         RouteBean.NextRoadType mNextRoadType         = null;
    public         double                 mFakerPointX          = 0f;
    public         double                 mFakerPointY          = 0f;
    public         int                    mCurrentIndex         = 0;
    public         int                    mDrawIndex            = 0;
    public         AMapNaviLocation       mCurrentLocation      = null;
    public         boolean                mFlag                 = false;
    public boolean mFakeOver;
    public int     mGpsNumber       = 0;
    public String  mNaviText        = null;
    public boolean mIsMatchNaviPath = true;
    public boolean mIsYaw           = false;
    public boolean mNaviEnd         = false;

    private RouteResult() {}

    public static RouteResult getInstance() {
        return mRouteResult;
    }
    @Override
    public void reset() {
        super.reset();
        mCanDraw = true;
        mCurrentLatLngs.clear();
        mCurrentPoints.clear();
        mMayBeErrorLocation = false;
        mHasNextRoadName = false;
        mNextRoadName = null;
        mProjection = null;
        mNextRoadNamePosition = null;
        mPreLocation = null;
        mFakeLocation = null;
        mNextRoadType = null;
        mFakerPointX = 0f;
        mFakerPointY = 0f;
        mCurrentIndex = 0;
        mDrawIndex = 0;
        mCurrentLocation = null;
        mFlag = false;
        mIsYaw = false;
        mNaviEnd = false;

    }

    @Override
    public void release() {
        super.release();
        mCanDraw = true;
        mMayBeErrorLocation = false;
        mHasNextRoadName = false;
        mFlag = false;
        mCurrentLatLngs.clear();
        mNextRoadNamePosition = null;
        mNextRoadName = null;
        mNextRoadType = null;
        mProjection = null;
        mPreLocation = null;
        mFakeLocation = null;
        mCurrentLocation = null;
        mFakerPointX = 0f;
        mFakerPointY = 0f;
        mCurrentIndex = 0;
        mDrawIndex=0;

        mFlag = false;
        mIsYaw = false;
        mNaviEnd = false;
    }


}
