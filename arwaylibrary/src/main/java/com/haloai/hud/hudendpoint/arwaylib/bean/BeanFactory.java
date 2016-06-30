package com.haloai.hud.hudendpoint.arwaylib.bean;

import com.haloai.hud.hudendpoint.arwaylib.bean.impl.CompassBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.MusicBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.NaviInfoBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.NetworkBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.RouteBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.SatelliteBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.SpeedBean;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.bean;
 * project_name : hudlauncher;
 */
public class BeanFactory {
    public enum BeanType {
        ROUTE,
        SATELLITE,
        MUSIC,
        NETWORK,
        NAVI_INFO,
        SPEED,
        COMPASS,
    }

    /**
     * is or not inited.
     */
    private static boolean mIsInited = false;

    private static RouteBean     mRouteBean     = null;
    private static SatelliteBean mSatelliteBean = null;
    private static MusicBean     mMusicBean     = null;
    private static NetworkBean   mNetworkBean   = null;
    private static NaviInfoBean  mNaviInfoBean  = null;
    private static SpeedBean     mSpeedBean     = null;
    private static CompassBean   mCompassBean   = null;

    public static SuperBean getBean(BeanType beanType) {
        if (!mIsInited) {
            init();
        }
        SuperBean hudBean = null;
        switch (beanType) {
            case ROUTE:
                hudBean = mRouteBean;
                break;
            case SATELLITE:
                hudBean = mSatelliteBean;
                break;
            case MUSIC:
                hudBean = mMusicBean;
                break;
            case NETWORK:
                hudBean = mNetworkBean;
                break;
            case NAVI_INFO:
                hudBean = mNaviInfoBean;
                break;
            case SPEED:
                hudBean = mSpeedBean;
                break;
            case COMPASS:
                hudBean = mCompassBean;
                break;
            default:
                throw new RuntimeException("bean type is error or missing break.");
        }
        return hudBean;
    }

    private static void init() {
        synchronized (BeanFactory.class) {
            mRouteBean = new RouteBean();
            mSatelliteBean = new SatelliteBean();
            mMusicBean = new MusicBean();
            mNetworkBean = new NetworkBean();
            mNaviInfoBean = new NaviInfoBean();
            mSpeedBean = new SpeedBean();
            mCompassBean = new CompassBean();
            mIsInited = true;
        }
    }
}
