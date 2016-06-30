package com.haloai.hud.hudendpoint.arwaylib;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.amap.api.maps.Projection;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.haloai.hud.hudendpoint.arwaylib.arway.ARWayFactory;
import com.haloai.hud.hudendpoint.arwaylib.arway.IARWay;
import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.CompassBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.MusicBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.NaviInfoBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.NetworkBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.RouteBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.SatelliteBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.SpeedBean;
import com.haloai.hud.utils.HaloLogger;

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
    /**
     * the class for update arway status.
     */
    public static class ARWayStatusUpdater {
        public static void back2Init() {
            mARWay.reset();
        }

        public static void release() {
            resetBean();
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
            resetBean();
            mARWay.stop();
        }

        public static void reset() {
            resetBean();
        }
        public static boolean isRunning() {
            return mARWay.isRunning();
        }
        /**
         * reset bean data
         */
        public static void resetBean() {
            HaloLogger.logE("ARWayController","resetBean called");
            RouteBeanUpdater.reset();
            SpeedBeanUpdater.reset();
            NetworkBeanUpdater.reset();
            MusicBeanUpdater.reset();
            SatelliteBeanUpdater.reset();
            NaviInfoBeanUpdate.reset();
            RouteBeanUpdater.reset();
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

        public static RouteBean setGpsNumber(int gpsNumber) {
            synchronized (ARWayController.class) {
                return mRouteBean.setGpsNumber(gpsNumber);
            }
        }

        public static void reset(){
            mRouteBean.reset();
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

        public static NaviInfoBean setNaviIconBitmap(Bitmap bitmap){
            return mNaviInfoBean.setNaviIconBitmap(bitmap);
        }

        public static NaviInfoBean setCrossBitmap(Bitmap bitmap){
            return mNaviInfoBean.setCrossBitmap(bitmap);
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

        public static NaviInfoBean setPathRetainDistance(int pathRetainDistance){
            return mNaviInfoBean.setPathRetainDistance(pathRetainDistance);
        }

        public static NaviInfoBean setPathRetainTime(int pathRetainTime){
            return mNaviInfoBean.setPathRetainTime(pathRetainTime);
        }

        public static NaviInfoBean setNaviText(String naviText){
            return mNaviInfoBean.setNaviText(naviText);
        }

        public static void reset(){
            mNaviInfoBean.reset();
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

        public static void reset(){
            mSatelliteBean.reset();
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

        public static void reset(){
            mMusicBean.reset();
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

        public static void reset(){
            mNetworkBean.reset();
        }
    }

    /***
     * the class for update data about car speed dispaly
     * NetworkBeanUpdater class is a packing class for car speed.
     */
    public static class SpeedBeanUpdater {
        private static SpeedBean mSpeedBean = (SpeedBean) BeanFactory.getBean(BeanFactory.BeanType.SPEED);

        public static SpeedBean setIsShow(boolean isShow) {
            synchronized (ARWayController.class) {
                mSpeedBean.setIsShow(isShow);
                return mSpeedBean;
            }
        }
        /**
         * @param speed km/h
         * */
        public static void setSpeed(int speed) {
            synchronized (ARWayController.class) {
                mSpeedBean.setSpeed(speed);
            }
        }

        public static void reset(){
            mSpeedBean.reset();
        }
    }

    /***
     * the class for update data about compass
     * NetworkBeanUpdater class is a packing class for compass.
     */
    public static class CompassBeanUpdater {
        private static CompassBean mCompassBean = (CompassBean) BeanFactory.getBean(BeanFactory.BeanType.COMPASS);

        public static CompassBean setIsShow(boolean isShow) {
            synchronized (ARWayController.class) {
                mCompassBean.setIsShow(isShow);
                return mCompassBean;
            }
        }

        public static void setDirection(int direction) {
            mCompassBean.setDirection(direction);
        }

        public static void setOrientation(int orientation) {
            mCompassBean.setOrientation(orientation);
        }

        public static void reset(){
            mCompassBean.reset();
        }

    }
    

}
