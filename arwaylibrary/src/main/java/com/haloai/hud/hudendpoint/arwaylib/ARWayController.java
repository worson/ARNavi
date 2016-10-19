package com.haloai.hud.hudendpoint.arwaylib;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.amap.api.maps.Projection;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.haloai.hud.hudendpoint.arwaylib.arway.IARWay;
import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.CommonBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.NaviInfoBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.RouteBean;
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
    private static IARWay   mARWay         = null;
    private static Fragment mARWayFragment = null;
    private static Context  mContext       = null;


    /**
     * get arway for show.
     * You must be call this methoh first,and call else method next.
     *
     * @return view
     */
    public static View getARWayAndInit(Context context) {
        mContext = context;
        return null;
    }

    /***
     * get arway fragment for show.
     *
     * @param context
     * @return
     */
    public static Fragment getARWayFragmentAndInit(Context context) {
        mContext = context;
        return mARWayFragment;
    }

    /**
     * the class for update arway status.
     */
    public static class ARWayStatusUpdater {
        /***
         * 保证ARWAY停止后才能清空数据，任务根据需求再次启动
         * 偏航时：更改绘制内容，领航成功时重置内容，再写入新路径
         */

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

        public static void reStart() {
            mARWay.stop();
        }

        public static void reset() {
            mARWay.reset();
        }

        /**
         * 开始偏航(yawStart)：停止当前绘制内容，进入状态显示界面(显示指南针、速度等内容)
         * 尽可能保证beean中的计算代码的健壮性
         */
        public static void yawStart() {
            if (isRunning()) {
                CommonBeanUpdater.setYaw(true);
            }
        }

        /**
         * 结束偏航(yawEnd)：resetData ，重新开始绘制draw 导航路径等内容
         */
        public static void yawEnd() {
            if (isRunning()) {
                resetData();
            }
        }

        /**
         * 到达目的地时调用
         */
        public static void arriveDestination() {
            CommonBeanUpdater.setNaviEnd(true);
        }

        public static boolean isRunning() {
            return mARWay.isRunning();
        }

        /**
         * reset bean data
         */
        public static void resetData() {
            HaloLogger.logE("ARWayController", "resetData called");
            CommonBeanUpdater.reset();
        }
    }

    public static class CommonBeanUpdater {
        private static CommonBean mCommonBean = (CommonBean) BeanFactory.getBean(BeanFactory.BeanType.COMMON);

        public static void setYaw(boolean yaw) {
            synchronized (ARWayController.class) {
                mCommonBean.setYaw(yaw);
            }
        }

        public static void setNaviEnd(boolean naviEnd) {
            synchronized (ARWayController.class) {
                mCommonBean.setNaviEnd(naviEnd);
            }
        }

        public static void setStartOk(boolean startOk) {
            synchronized (ARWayController.class) {
                mCommonBean.setStartOk(startOk);
            }
        }

        public static void setNavingStart(boolean navingStart) {
            synchronized (ARWayController.class) {
                mCommonBean.setNavingStart(navingStart);
            }
        }

        public static void setGpsWork(boolean gpsWork) {
            synchronized (ARWayController.class) {
                mCommonBean.setGpsWork(gpsWork);
            }
        }

        public static void setMatchNaviPath(boolean matchNaviPath) {
            synchronized (ARWayController.class) {
                mCommonBean.setMatchNaviPath(matchNaviPath);
            }
        }

        public static void setHasNetwork(boolean hasNetwork) {
            synchronized (ARWayController.class) {
                mCommonBean.setHasNetwork(hasNetwork);
            }
        }


        public static void reset() {
            mCommonBean.reset();
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
                return mRouteBean.setProjection(projection);
            }
        }

        public static RouteBean setPath(AMapNaviPath AMapNaviPath) {
            synchronized (ARWayController.class) {
                // FIXME: 16/7/5 暂且在设定路径的时候重新bean
//                ARWayStatusUpdater.resetData();
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

        public static RouteBean setNextRoadName(String nextRoadName, RouteBean.NextRoadType nextRoadType) {
            synchronized (ARWayController.class) {
                return mRouteBean.setNextRoadNameAndType(nextRoadName, nextRoadType);
            }
        }

        public static RouteBean setGpsNumber(int gpsNumber) {
            synchronized (ARWayController.class) {
                return mRouteBean.setGpsNumber(gpsNumber);
            }
        }

        public static void reset() {
            mRouteBean.reset();
        }
    }

    /**
     * the class for udpate navi info data.
     * NaviInfoBeanUpdate class is a packing class for NaviInfoBean.
     */
    public static class NaviInfoBeanUpdate {
        private static NaviInfoBean mNaviInfoBean = (NaviInfoBean) BeanFactory.getBean(BeanFactory.BeanType.NAVI_INFO);

        public static NaviInfoBean setIsShow(boolean isShow) {
            mNaviInfoBean.setIsShow(isShow);
            return mNaviInfoBean;
        }

        public static NaviInfoBean setPathTotalDistance(int distance) {
            mNaviInfoBean.setPathTotalDistance(distance);
            return mNaviInfoBean;
        }

        public static NaviInfoBean setNaviIcon(int naviIcon) {
            return mNaviInfoBean.setNaviIcon(naviIcon);
        }

        public static NaviInfoBean setNaviIconBitmap(Bitmap bitmap) {
            return mNaviInfoBean.setNaviIconBitmap(bitmap);
        }

        public static NaviInfoBean setCrossBitmap(Bitmap bitmap) {
            return mNaviInfoBean.setCrossBitmap(bitmap);
        }

        public static NaviInfoBean setNaviIconDist(int naviIcon) {
            return mNaviInfoBean.setNaviIconDistance(naviIcon);
        }

        public static NaviInfoBean setCurrentRoadName(String currentRoadName) {
            return mNaviInfoBean.setCurrentRoadName(currentRoadName);
        }

        public static NaviInfoBean setNextRoadName(String nextRoadName) {
            return mNaviInfoBean.setNextRoadName(nextRoadName);
        }

        public static NaviInfoBean setPathRetainDistance(int pathRetainDistance) {
            return mNaviInfoBean.setPathRetainDistance(pathRetainDistance);
        }

        public static NaviInfoBean setPathRetainTime(int pathRetainTime) {
            return mNaviInfoBean.setPathRetainTime(pathRetainTime);
        }

        public static NaviInfoBean setNaviText(String naviText) {
            return mNaviInfoBean.setNaviText(naviText);
        }

        public static void reset() {
            mNaviInfoBean.reset();
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
         */
        public static void setSpeed(int speed) {
            synchronized (ARWayController.class) {
                mSpeedBean.setSpeed(speed);
            }
        }

        public static void reset() {
            mSpeedBean.reset();
        }
    }


}
