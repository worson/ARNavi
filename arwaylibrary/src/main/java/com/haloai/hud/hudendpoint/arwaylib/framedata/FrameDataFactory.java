package com.haloai.hud.hudendpoint.arwaylib.framedata;

import android.content.Context;
import android.graphics.Point;

import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.CommonBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.MusicBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.NaviInfoBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.NetworkBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.RouteBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.SatelliteBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.SpeedBean;
import com.haloai.hud.hudendpoint.arwaylib.calculator.CalculatorFactory;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.AlphaFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.CrossImageFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.NaviInfoFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.PositionFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.RotateFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.RouteFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.ScaleFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.SpeedFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.TurnInfoFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.AlphaCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.CrossImageCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.NaviInfoCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.PositionCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.RotateCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.RouteCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.ScaleCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.SpeedCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.TurnInfoCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.AlphaResult;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.CrossImageResult;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.NaviInfoResult;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.PositionResult;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.RotateResult;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.RouteResult;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.ScaleResult;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.SpeedResult;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.TurnInfoResult;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.CrossImageFrameData;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.ExitFrameData;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.IconFrameData;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.MusicFrameData;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.NaviInfoFrameData;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.NextRoadNameFrameData;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.RouteFrameData;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.SpeedFrameData;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.TurnInfoFrameData;
import com.haloai.hud.utils.HaloLogger;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.framedata;
 * project_name : hudlauncher;
 */
public class FrameDataFactory {
    //calculator
    private static ScaleCalculator    mScalaCalculator    = (ScaleCalculator) CalculatorFactory.getCalculator(CalculatorFactory.CalculatorType.SCALA);
    private static PositionCalculator mPositionCalculator = (PositionCalculator) CalculatorFactory.getCalculator(CalculatorFactory.CalculatorType.POSITION);
    private static AlphaCalculator    mAlphaCalculator    = (AlphaCalculator) CalculatorFactory.getCalculator(CalculatorFactory.CalculatorType.ALPHA);
    private static RotateCalculator   mRotateCalculator   = (RotateCalculator) CalculatorFactory.getCalculator(CalculatorFactory.CalculatorType.ROTATE);
    private static RouteCalculator    mRouteCalculator    = (RouteCalculator) CalculatorFactory.getCalculator(CalculatorFactory.CalculatorType.ROUTE);
    private static TurnInfoCalculator mTurnInfoCalculator    = (TurnInfoCalculator) CalculatorFactory.getCalculator(CalculatorFactory.CalculatorType.TURN_INFO);
    private static NaviInfoCalculator mNaviInfoCalculator    = (NaviInfoCalculator) CalculatorFactory.getCalculator(CalculatorFactory.CalculatorType.NAVI_INFO);
    private static CrossImageCalculator mCrossImageCalculator    = (CrossImageCalculator) CalculatorFactory.getCalculator(CalculatorFactory.CalculatorType.CROSS_IMAGE);
    private static SpeedCalculator mSpeedCalculator    = (SpeedCalculator) CalculatorFactory.getCalculator(CalculatorFactory.CalculatorType.SPEED);

    //bean
    private static CommonBean    mCommonBean     = (CommonBean) BeanFactory.getBean(BeanFactory.BeanType.COMMON);
    private static MusicBean     mMusicBean     = (MusicBean) BeanFactory.getBean(BeanFactory.BeanType.MUSIC);
    private static RouteBean     mRouteBean     = (RouteBean) BeanFactory.getBean(BeanFactory.BeanType.ROUTE);
    private static NetworkBean   mNetworkBean   = (NetworkBean) BeanFactory.getBean(BeanFactory.BeanType.NETWORK);
    private static SatelliteBean mSatelliteBean = (SatelliteBean) BeanFactory.getBean(BeanFactory.BeanType.SATELLITE);
    private static NaviInfoBean  mNaviInfoBean  = (NaviInfoBean) BeanFactory.getBean(BeanFactory.BeanType.NAVI_INFO);
    private static SpeedBean mSpeedBean  = (SpeedBean) BeanFactory.getBean(BeanFactory.BeanType.SPEED);

    public static void resetCalculators(){
        mScalaCalculator.reset();
        mPositionCalculator.reset();
        mAlphaCalculator.reset();
        mRotateCalculator.reset();
        mRouteCalculator.reset();
        mTurnInfoCalculator.reset();
        mNaviInfoCalculator.reset();
        mCrossImageCalculator.reset();
        mSpeedCalculator.reset();
    }

    public enum FrameDataType {

        CROSS_IMAGE,
        EXIT,
        MUSIC,
        NETWORK,
        NEXT_ROAD_NAME,
        ROUTE,
        SATELLITE,
        NAVI_INFO,
        TURN_INFO,
        SPEED
    }

    /***
     * 获取FrameData数据,同时去更新这些数据
     *
     * @param frameDataType
     * @return
     */
    public static SuperFrameData getFrameData4Update(FrameDataType frameDataType) {
        SuperFrameData frameData = null;
        switch (frameDataType) {
            case CROSS_IMAGE:
                frameData = CrossImageFrameData.getInstance();
                break;
            case EXIT:
                frameData = ExitFrameData.getInstance();
                break;
            case MUSIC:
                frameData = MusicFrameData.getInstance();
                break;
            case NETWORK:
                frameData = IconFrameData.getInstance(FrameDataType.NETWORK);
                break;
            case NEXT_ROAD_NAME:
                frameData = NextRoadNameFrameData.getInstance();
                break;
            case ROUTE:
                frameData = RouteFrameData.getInstance();
                break;
            case SATELLITE:
                frameData = IconFrameData.getInstance(FrameDataType.SATELLITE);
                break;
            case TURN_INFO:
                frameData = TurnInfoFrameData.getInstance();
                break;
            case NAVI_INFO:
                frameData = NaviInfoFrameData.getInstance();
                break;
            case SPEED:
                frameData = SpeedFrameData.getInstance();
                break;
            default:
                break;
        }
        return frameData;
    }

    /***
     * 获取FrameData数据用于绘制元素
     *
     * @param context
     * @param frameDataType
     * @return
     */
    public static SuperFrameData getFrameData4Draw(Context context, FrameDataType frameDataType) {
        SuperFrameData frameData = null;
        switch (frameDataType) {
            case CROSS_IMAGE:
                frameData = CrossImageFrameData.getInstance();
                updateCrossImage(context,frameData);
                break;
            case EXIT:

                break;
            case MUSIC:
                frameData = MusicFrameData.getInstance();
                updateMusic(context, frameData);
                break;
            case NEXT_ROAD_NAME:
                frameData = NextRoadNameFrameData.getInstance();
                updateNextRoadName(context, frameData);
                break;
            case ROUTE:
                frameData = RouteFrameData.getInstance();
                updateRoute(context, frameData);
                break;
            case NAVI_INFO:
                frameData = NaviInfoFrameData.getInstance();
                updateNaviInfo(context, frameData);
                break;
            case TURN_INFO:
                frameData = TurnInfoFrameData.getInstance();
                updateTurnInfo(context, frameData);
                break;
            case SATELLITE:

                break;
            case NETWORK:

                break;
            case SPEED:
                frameData = SpeedFrameData.getInstance();
                updateSpeedDisplay(context, frameData);
                break;
            default:
                break;
        }
        return frameData;
    }

    private static void updateSpeedDisplay(Context context, SuperFrameData frameData){
        SpeedFrameData speedFrameData = (SpeedFrameData) frameData;
        SpeedFactor speedFactor = SpeedFactor.getInstance();
        speedFactor.init(true ,mSpeedBean.getSpeed());
        SpeedResult speedResult = mSpeedCalculator.calculate(speedFactor);
        try {
            speedFrameData.update(speedResult);
        }catch (Exception e){

        }
    }
    private static void updateCrossImage(Context context, SuperFrameData frameData){
        CrossImageFrameData crossImageFrameData = (CrossImageFrameData) frameData;
        CrossImageFactor crossImageFactor = CrossImageFactor.getInstance();
        crossImageFactor.init(true,mNaviInfoBean.getCrossBitmap());
        CrossImageResult crossImageResult = mCrossImageCalculator.calculate(crossImageFactor);
        try {
            crossImageFrameData.update(crossImageResult);
        }catch (Exception e){

        }
    }
    private static void updateTurnInfo(Context context, SuperFrameData frameData){
        TurnInfoFrameData turnInfoFrameData = (TurnInfoFrameData) frameData;
        TurnInfoFactor turnInfoFactor = TurnInfoFactor.getInstance();
        turnInfoFactor.init(true,mNaviInfoBean.getNaviIconDistance(),mNaviInfoBean.getNaviIconBitmap());
        TurnInfoResult turnInfoResult = mTurnInfoCalculator.calculate(turnInfoFactor);
        try {
            turnInfoFrameData.update(turnInfoResult);
        }catch (Exception e){

        }

    }

    private static void updateNaviInfo(Context context, SuperFrameData frameData) {
        HaloLogger.logE("FrameDataFactory","updateNaviInfo called ,MayBeErrorLocation is "+mRouteBean.isMayBeErrorLocation());
        NaviInfoFrameData naviInfoFrameData = (NaviInfoFrameData) frameData;
        NaviInfoFactor naviInfoFactor = NaviInfoFactor.getInstance();
        naviInfoFactor.init(true ,mNaviInfoBean.getPathRetainDistance(),mNaviInfoBean.getPathRetainTime(),mNaviInfoBean.getNaviText());
        NaviInfoResult naviInfoResult = mNaviInfoCalculator.calculate(naviInfoFactor);
        try {
            naviInfoFrameData.update(naviInfoResult);
        }catch (Exception e){

        }
    }

    /**
     * 更新NextRoadName用于绘制的NextRoadNameFrameData
     * @param context
     * @param frameData
     */
    private static void updateNextRoadName(Context context, SuperFrameData frameData) {
        NextRoadNameFrameData nextRoadNameFrameData = (NextRoadNameFrameData) frameData;

    }

    /**
     * 更新Route用于绘制的RouteFrameData
     * @param context
     * @param frameData
     */
    private static void updateRoute(Context context, SuperFrameData frameData) {
        RouteFrameData routeFrameData = (RouteFrameData) frameData;
        RouteFactor routeFactor = RouteFactor.getInstance();
        routeFactor.init(mRouteBean.isCanDrawHudway(), mRouteBean.isMayBeErrorLocation(),
                         mRouteBean.getCurrentPoint(), mRouteBean.getCurrentStep(),
                         mRouteBean.getCurrentLocation(), mRouteBean.getPathLatLngs(),
                         mRouteBean.getCroodsInSteps(), mRouteBean.getProjection(),
                         mRouteBean.getNextRoadName(),mRouteBean.getNextRoadType(),
                         mRouteBean.getRoadNameLatLngs(),mRouteBean.getGpsNumber(),
                         mNaviInfoBean.getNaviText(),mRouteBean.isMatchNaviPath(),mCommonBean.isYaw());
        // FIXME: 16/6/14
        long performanceLogTime;
        performanceLogTime = System.currentTimeMillis();
        RouteResult routeResult = mRouteCalculator.calculate(routeFactor);
        HaloLogger.logI("performance_log","=========performance_log=========== calculate time = "+ (System.currentTimeMillis()-performanceLogTime));
        performanceLogTime = System.currentTimeMillis();
        try {
            routeFrameData.update(routeResult);
        }catch (Exception e){

        }
        HaloLogger.logI("performance_log","=========performance_log=========== update time = "+ (System.currentTimeMillis()-performanceLogTime));
    }

    /**
     * 更新音乐部分用于绘制的MusicFrameData
     * @param context
     * @param frameData
     */
    private static void updateMusic(Context context, SuperFrameData frameData) {
        MusicFrameData musicFrameData = (MusicFrameData) frameData;
        musicFrameData.setMusicName(mMusicBean.getMusicName());
        musicFrameData.setImageWithMusicStatus(context,mMusicBean.getMusicStatus());
        boolean updateMusicFrameData = !(mMusicBean.getMusicStatus()== MusicBean.MusicStatus.PLAYING || mMusicBean.getMusicStatus()== MusicBean.MusicStatus.UNPLAYING);

        if (updateMusicFrameData) {

            //position animation(move to)
            PositionFactor positionFactorMoveTo = new PositionFactor(
                    mMusicBean.getStartTime(), mMusicBean.getLastTime(), mMusicBean.getDuration(),
                    musicFrameData.getAnimStartPosition(), new Point(400, 400), true);
            PositionResult positionResultMoveTo = mPositionCalculator.calculate(positionFactorMoveTo);
            musicFrameData.updateWithPosition(positionResultMoveTo);

            //position animation(move by)
            PositionFactor positionFactorMoveBy = new PositionFactor(
                    mMusicBean.getStartTime(), mMusicBean.getLastTime(), mMusicBean.getDuration(),
                    150, 290, true);
            PositionResult positionResultMoveBy = mPositionCalculator.calculate(positionFactorMoveBy);
            musicFrameData.updateWithPosition(positionResultMoveBy);

            //scala animation
            ScaleFactor scalaFactor = new ScaleFactor(
                    mMusicBean.getStartTime(), mMusicBean.getLastTime(), mMusicBean.getDuration(),
                    1f, 2f, musicFrameData.getWidthScala(),
                    1f, 2f, musicFrameData.getHeightScala(),
                    musicFrameData.getPosition(), false);
            ScaleResult scalaResult = mScalaCalculator.calculate(scalaFactor);
            musicFrameData.updateWithScala(scalaResult);

            //alpha animation
            AlphaFactor alphaFactor = new AlphaFactor(
                    mMusicBean.getStartTime(), mMusicBean.getLastTime(), mMusicBean.getDuration(),
                    1f, 0f, musicFrameData.getAlpha(), true);
            AlphaResult alphaResult = mAlphaCalculator.calculate(alphaFactor);
            musicFrameData.updateWithAlpha(alphaResult);

            //rotate animation
            RotateFactor rotateFactor = new RotateFactor(
                    mMusicBean.getStartTime(), mMusicBean.getLastTime(), mMusicBean.getDuration(),
                    300f, 500f, 60f, true);
            RotateResult rotateResult = mRotateCalculator.calculate(rotateFactor);
            musicFrameData.updateWithRotate(rotateResult);

            mMusicBean.setLastTime(System.currentTimeMillis());

            //if animation is over,set the music current status with playing or unplaying.
            if (positionResultMoveBy.mIsOver) {
                musicFrameData.animOver();
                if (mMusicBean.getMusicStatus() == MusicBean.MusicStatus.STOP) {
                    mMusicBean.setMusicStatus(MusicBean.MusicStatus.UNPLAYING);
                } else {
                    mMusicBean.setMusicStatus(MusicBean.MusicStatus.PLAYING);
                }
            }
        }
    }
}
