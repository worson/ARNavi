package com.haloai.hud.hudendpoint.arwaylib.bean;

import com.haloai.hud.hudendpoint.arwaylib.bean.impl.CommonBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.NaviInfoBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.RouteBean;
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
        COMMON,
        ROUTE,
        SATELLITE,
        MUSIC,
        NETWORK,
        NAVI_INFO,
        SPEED,
        COMPASS,
        GL_ROUTE,
        GL_CAMERA
    }

    /**
     * is or not inited.
     */
    private static boolean mIsInited = false;

    private static CommonBean      mCommonBean      = null;
    private static RouteBean       mRouteBean       = null;
    private static NaviInfoBean    mNaviInfoBean    = null;
    private static SpeedBean       mSpeedBean       = null;

    public static SuperBean getBean(BeanType beanType) {
        if (!mIsInited) {
            init();
        }
        SuperBean hudBean = null;
        switch (beanType) {
            case COMMON:
                hudBean = mCommonBean;
                break;
            case ROUTE:
                hudBean = mRouteBean;
                break;
            case NAVI_INFO:
                hudBean = mNaviInfoBean;
                break;
            case SPEED:
                hudBean = mSpeedBean;
                break;
            default:
                throw new RuntimeException("bean type is error or missing break.");
        }
        return hudBean;
    }

    private static void init() {
        synchronized (BeanFactory.class) {
            mRouteBean = new RouteBean();
            mNaviInfoBean = new NaviInfoBean();
            mSpeedBean = new SpeedBean();
            mCommonBean = new CommonBean();
            mIsInited = true;
        }
    }
}
