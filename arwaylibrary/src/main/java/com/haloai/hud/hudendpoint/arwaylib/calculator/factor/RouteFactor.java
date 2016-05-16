package com.haloai.hud.hudendpoint.arwaylib.calculator.factor;

import com.amap.api.maps.Projection;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.NaviLatLng;

import java.util.List;
import java.util.Map;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.calculator.result;
 * project_name : hudlauncher;
 */
public class RouteFactor {
    //RouteResult中的数据应该可以通过计算得到以下数据
    //RouteResult的值基本通过RouteBean得到
    //mCanDraw 此时是否应该绘制(例如有其他界面到来,例如打电话等,此时直接停止draw)
    //mMayBeErrorLocation 此时的location是否为正确的(设置了path后50m内该值为true,之后改为false)
    //mCurrentPoints 用于绘制的屏幕点的集合
    public boolean                       mCanDraw            = false;
    public boolean                       mMayBeErrorLocation = true;
    public int                           mCurrentPoint       = 0;
    public int                           mCurrentStep        = 0;
    public AMapNaviLocation              mStartLocation      = null;
    public List<NaviLatLng>              mPathLatLngs        = null;
    public List<Integer>                 mCroodsInSteps      = null;
    public Projection                    mProjection         = null;
    public String                        mNextRoadName       = null;
    public Map<String, List<NaviLatLng>> mRoadNameLatLngs    = null;

    private static RouteFactor mRouteFactor = new RouteFactor();


    private RouteFactor() {}

    public static RouteFactor getInstance() {
        return mRouteFactor;
    }

    public void init(boolean canDraw, boolean mayBeErrorLocation, int currentPoint, int currentStep, AMapNaviLocation startLocation, List<NaviLatLng> pathLatLngs, List<Integer> croodsInSteps, Projection projection, String nextRoadName, Map<String, List<NaviLatLng>> roadNameLatLngs) {
        mCanDraw = canDraw;
        mMayBeErrorLocation = mayBeErrorLocation;
        mCurrentPoint = currentPoint;
        mCurrentStep = currentStep;
        mStartLocation = startLocation;
        mPathLatLngs = pathLatLngs;
        mCroodsInSteps = croodsInSteps;
        mProjection = projection;
        mNextRoadName = nextRoadName;
        mRoadNameLatLngs = roadNameLatLngs;
    }
}
