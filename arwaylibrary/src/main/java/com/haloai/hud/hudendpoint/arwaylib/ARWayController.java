package com.haloai.hud.hudendpoint.arwaylib;

import android.content.Context;
import android.view.View;

import com.amap.api.maps.Projection;
import com.haloai.hud.hudendpoint.arwaylib.arway.ARWayFactory;
import com.haloai.hud.hudendpoint.arwaylib.arway.IARWay;
import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.ExitBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.MusicBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.NaviBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.NetworkBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.SatelliteBean;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib;
 * project_name : hudlauncher;
 * description  : ARWayLib main class .
 * Caller can control anything with this class .
 * And they also can update any data about arway.
 */
public class ARWayController {
    private static IARWay     mARWay      = null;
    private static Projection mProjection = null;
    private static Context    mContext    = null;

    /**
     * get arway for show.
     * You must be call this methoh first,and call else method next.
     *
     * @return view
     */
    public static View getARWayAndInit(Context context, Projection projection) {
        mContext = context;
        mProjection = projection;
        if (mARWay == null) {
            mARWay = ARWayFactory.getARWay(context, ARWayFactory.ARWayType.SURFACE_VIEW);
        }
        return mARWay.getARWay();
    }

    /**
     * the class for update arway status.
     */
    public static class ARWayStatusUpdater {
        public static void back2Init() {
            mARWay.reset();
        }

        public static void release() {
            mARWay.release();
        }

        public static void start() {
            mARWay.start();
        }

        public static void continue_() {
            mARWay.continue_();
        }

        public static void pause() {
            mARWay.pause();
        }

        public static void stop() {
            mARWay.stop();
        }

        public static boolean isRunning() {
            return mARWay.isRunning();
        }
    }

    /***
     * the class for update data about navigation.
     * NaviBeanUpdater class is a packing class for NaviBean.
     */
    public static class NaviBeanUpdater {
        private static NaviBean mNaviBean = (NaviBean) BeanFactory.getBean(BeanFactory.BeanType.NAVIGATION);

        public static NaviBean setIsShow(boolean isShow) {
            synchronized (ARWayController.class) {
                return (NaviBean) mNaviBean.setIsShow(isShow);
            }
        }
    }

    /***
     * the class for update data about satellite.
     * SatelliteBeanUpdater class is a packing class for SatelliteBean.
     */
    public static class SatelliteBeanUpdater {
        private static SatelliteBean mSatelliteBean = (SatelliteBean) BeanFactory.getBean(BeanFactory.BeanType.SATELLITE);

        public static SatelliteBean setIsShow(boolean isShow) {
            synchronized (ARWayController.class) {
                return (SatelliteBean) mSatelliteBean.setIsShow(isShow);
            }
        }
    }

    /***
     * the class for update data about music.
     * MusicBeanUpdater class is a packing class for MusicBean.
     */
    public static class MusicBeanUpdater {
        private static MusicBean mMusicBean = (MusicBean) BeanFactory.getBean(BeanFactory.BeanType.MUSIC);

        public static MusicBean setIsShow(boolean isShow) {
            synchronized (ARWayController.class) {
                return (MusicBean) mMusicBean.setIsShow(isShow);
            }
        }

        public static MusicBean setMusicName(String musicName) {
            synchronized (ARWayController.class) {
                return mMusicBean.setMusicName(musicName);
            }
        }

        public static MusicBean setMusicStatus(MusicBean.MusicStatus musicStatus){
            synchronized (ARWayController.class) {
                return mMusicBean.setMusicStatus(musicStatus);
            }
        }

        public static MusicBean setMusicDuration(long duration){
            synchronized (ARWayController.class) {
                return mMusicBean.setDuration(duration);
            }
        }

    }

    /***
     * the class for update data about exit navigation.
     * ExitBeanUpdater class is a packing class for ExitBean.
     */
    public static class ExitBeanUpdater {
        private static ExitBean mExitBean = (ExitBean) BeanFactory.getBean(BeanFactory.BeanType.EXIT);

        public static ExitBean setIsShow(boolean isShow) {
            synchronized (ARWayController.class) {
                return (ExitBean) mExitBean.setIsShow(isShow);
            }
        }
    }

    /***
     * the class for update data about network navigation.
     * NetworkBeanUpdater class is a packing class for NetworkBean.
     */
    public static class NetworkBeanUpdater {
        private static NetworkBean mNetworkBean = (NetworkBean) BeanFactory.getBean(BeanFactory.BeanType.NETWORK);

        public static NetworkBean setIsShow(boolean isShow) {
            synchronized (ARWayController.class) {
                return (NetworkBean) mNetworkBean.setIsShow(isShow);
            }
        }
    }

}
