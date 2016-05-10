package com.haloai.hud.hudendpoint.arwaylib.bean;

import com.haloai.hud.hudendpoint.arwaylib.bean.impl.ExitBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.MusicBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.NaviBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.NetworkBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.SatelliteBean;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.bean;
 * project_name : hudlauncher;
 */
public class BeanFactory {
    public enum BeanType {
        NAVIGATION,
        SATELLITE,
        MUSIC,
        NETWORK,
        EXIT
    }

    /**
     * is or not inited.
     */
    private static boolean mIsInited = false;

    private static NaviBean      mNaviBean      = null;
    private static SatelliteBean mSatelliteBean = null;
    private static MusicBean     mMusicBean     = null;
    private static ExitBean      mExitBean      = null;
    private static NetworkBean   mNetworkBean   = null;

    public static SuperBean getBean(BeanType beanType) {
        if(!mIsInited) {
            init();
        }
        SuperBean hudBean = null;
        switch (beanType) {
            case NAVIGATION:
                hudBean = mNaviBean;
                break;
            case SATELLITE:
                hudBean = mSatelliteBean;
                break;
            case MUSIC:
                hudBean = mMusicBean;
                break;
            case EXIT:
                hudBean = mExitBean;
                break;
            case NETWORK:
                hudBean = mNetworkBean;
                break;
            default:
                throw new RuntimeException("bean type is error or missing break.");
        }
        return hudBean;
    }

    private static void init() {
        synchronized (BeanFactory.class){
            mNaviBean = new NaviBean();
            mSatelliteBean = new SatelliteBean();
            mMusicBean = new MusicBean();
            mExitBean = new ExitBean();
            mNetworkBean = new NetworkBean();
            mIsInited = true;
        }
    }
}
