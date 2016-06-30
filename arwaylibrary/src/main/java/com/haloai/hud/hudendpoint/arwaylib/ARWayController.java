package com.haloai.hud.hudendpoint.arwaylib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;

import com.amap.api.maps.Projection;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.haloai.hud.hudendpoint.arwaylib.arway.ARWayFactory;
import com.haloai.hud.hudendpoint.arwaylib.arway.IARWay;
import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.MusicBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.NaviInfoBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.NetworkBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.RouteBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.SatelliteBean;
import com.haloai.hud.hudendpoint.arwaylib.utils.EnlargedCrossProcess;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.IOException;

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
    private static Context    mContext    = null;

    /**
     * get arway for show.
     * You must be call this methoh first,and call else method next.
     *
     * @return view
     */
    public static View getARWayAndInit(Context context) {
        mContext = context;
        if (mARWay == null) {
            mARWay = ARWayFactory.getARWay(context, ARWayFactory.ARWayType.SURFACE_VIEW);


        }
        return mARWay.getARWay();
    }

    private static boolean crossImageEnvReady = false;
    public static void prepareCrossImageEnv(Context context) {
        if (!crossImageEnvReady) {
            crossImageEnvReady = true;
            //Harry Test.
//            BitmapFactory.Options opts = new BitmapFactory.Options();
//            opts.inDither = true;
//            opts.inPreferredConfig = Bitmap.Config.RGB_565;
//            opts.inScaled = false;
//            Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.sample_enlarge_crossimage, opts);

            Mat matBmp = null;
            try {
                matBmp = Utils.loadResource(context, R.drawable.sample_enlarge_crossimage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            EnlargedCrossProcess ecp = new EnlargedCrossProcess();
            ecp.processAMapECImage(matBmp, null);
            Log.e("EnlargedCrossProcess", "!!!! EnlargedCrossProcess !!!");
        }
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
     * RouteBeanUpdater class is a packing class for RouteBean.
     */
    public static class RouteBeanUpdater {
        private static RouteBean mRouteBean = (RouteBean) BeanFactory.getBean(BeanFactory.BeanType.ROUTE);

        public static RouteBean setIsShow(boolean isShow) {
            synchronized (ARWayController.class) {
                mRouteBean.setIsShow(isShow);
                return mRouteBean;
            }
        }

        public static RouteBean setProjection(Projection projection) {
            synchronized (ARWayController.class) {
                return  mRouteBean.setProjection(projection);
            }
        }

        public static RouteBean setPath(AMapNaviPath AMapNaviPath) {
            synchronized (ARWayController.class) {
                return mRouteBean.setPath(AMapNaviPath);
            }
        }

        public static RouteBean setCanDrawHudway(boolean canDrawHudway) {
            synchronized (ARWayController.class) {
                return mRouteBean.setCanDrawARway(canDrawHudway);
            }
        }

        public static RouteBean setCrossImage(Bitmap crossImage) {
            synchronized (ARWayController.class) {
                return mRouteBean.setCrossImage(crossImage);
            }
        }

        public static RouteBean setCurrentLocation(AMapNaviLocation currentLocation) {
            synchronized (ARWayController.class) {
                return mRouteBean.setCurrentLocation(currentLocation);
            }
        }

        public static RouteBean setCurrentPoint(int currentPoint) {
            synchronized (ARWayController.class) {
                return mRouteBean.setCurrentPoint(currentPoint);
            }
        }

        public static RouteBean setCurrentStep(int currentStep) {
            synchronized (ARWayController.class) {
                return mRouteBean.setCurrentStep(currentStep);
            }
        }

        public static RouteBean setNextRoadName(String nextRoadName, RouteBean.NextRoadType nextRoadType){
            synchronized (ARWayController.class) {
                return mRouteBean.setNextRoadNameAndType(nextRoadName,nextRoadType);
            }
        }

        public static RouteBean setDegrees(float bearing) {
            synchronized (ARWayController.class){
                return mRouteBean.setDegrees(bearing);
            }
        }
    }

    /**
     * the class for udpate navi info data.
     * NaviInfoBeanUpdate class is a packing class for NaviInfoBean.
     */
    public static class NaviInfoBeanUpdate{
        private static NaviInfoBean mNaviInfoBean = (NaviInfoBean) BeanFactory.getBean(BeanFactory.BeanType.NAVI_INFO);

        public static NaviInfoBean setIsShow(boolean isShow){
            mNaviInfoBean.setIsShow(isShow);
            return mNaviInfoBean;
        }

        public static NaviInfoBean setNaviIcon(int naviIcon){
            return mNaviInfoBean.setNaviIcon(naviIcon);
        }

        public static NaviInfoBean setNaviIconDist(int naviIcon){
            return mNaviInfoBean.setNaviIconDistance(naviIcon);
        }

        public static NaviInfoBean setCurrentRoadName(String currentRoadName){
            return mNaviInfoBean.setCurrentRoadName(currentRoadName);
        }

        public static NaviInfoBean setNextRoadName(String nextRoadName){
            return mNaviInfoBean.setNextRoadName(nextRoadName);
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
                mSatelliteBean.setIsShow(isShow);
                return mSatelliteBean;
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
                mMusicBean.setIsShow(isShow);
                return mMusicBean;
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
     * the class for update data about network navigation.
     * NetworkBeanUpdater class is a packing class for NetworkBean.
     */
    public static class NetworkBeanUpdater {
        private static NetworkBean mNetworkBean = (NetworkBean) BeanFactory.getBean(BeanFactory.BeanType.NETWORK);

        public static NetworkBean setIsShow(boolean isShow) {
            synchronized (ARWayController.class) {
                mNetworkBean.setIsShow(isShow);
                return mNetworkBean;
            }
        }
    }

}
