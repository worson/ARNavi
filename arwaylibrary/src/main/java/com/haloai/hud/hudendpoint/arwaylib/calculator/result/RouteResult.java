package com.haloai.hud.hudendpoint.arwaylib.calculator.result;

import android.graphics.Point;

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
public class RouteResult {
    public boolean          mCanDraw            = true;
    public List<Point>      mCurrentPoints      = new ArrayList<Point>();
    public List<NaviLatLng> mCurrentLatLngs     = new ArrayList<NaviLatLng>();
    public boolean          mMayBeErrorLocation = false;
    public boolean          mHasNextRoadName    = false;
    public String           mNextRoadName       = null;

    public         Projection             mProjection           = null;
    public         NaviLatLng             mNextRoadNamePosition = null;
    private static RouteResult            mRouteResult          = new RouteResult();
    public         AMapNaviLocation       mPrePreLocation       = null;
    public         AMapNaviLocation       mFakeLocation         = null;
    public         RouteBean.NextRoadType mNextRoadType         = null;
    public         double                 mFakerPointX          = 0f;
    public         double                 mFakerPointY          = 0f;
    public         int                    mCurrentIndex         = 0;
    public         AMapNaviLocation       mCurrentLocation      = null;
    public         boolean                mFlag                 = false;

    private RouteResult() {}

    public static RouteResult getInstance() {
        return mRouteResult;
    }

    public void reset() {
        mCanDraw = true;
        mMayBeErrorLocation = false;
        mHasNextRoadName = false;
        mFlag = false;
        mCurrentPoints.clear();
        mCurrentLatLngs.clear();
        mNextRoadNamePosition = null;
        mNextRoadName = null;
        mNextRoadType = null;
        mProjection = null;
        mPrePreLocation = null;
        mFakeLocation = null;
        mCurrentLocation = null;
        mFakerPointX = 0f;
        mFakerPointY = 0f;
        mCurrentIndex = 0;
    }
}
