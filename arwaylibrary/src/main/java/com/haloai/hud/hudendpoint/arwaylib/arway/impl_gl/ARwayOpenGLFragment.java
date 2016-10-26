package com.haloai.hud.hudendpoint.arwaylib.arway.impl_gl;


import android.app.Fragment;
import android.content.Context;
import android.graphics.AvoidXfermode;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.haloai.hud.hudendpoint.arwaylib.ARWayController;
import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.arway.INaviDisplayPresenter;
import com.haloai.hud.hudendpoint.arwaylib.arway.INaviUpdater;
import com.haloai.hud.hudendpoint.arwaylib.arway.IStateContoller;
import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.CommonBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.NaviInfoBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.RouteBean;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObjectFactory;
import com.haloai.hud.hudendpoint.arwaylib.draw.IDriveStateLister;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl.DrawScene;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl.GlDrawCompass;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl.GlDrawNaviInfo;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl.GlDrawRetainDistance;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl.GlDrawSpeedDial;
import com.haloai.hud.hudendpoint.arwaylib.map.amap.MapProjectionMachine;
import com.haloai.hud.hudendpoint.arwaylib.modeldataengine.AMapNaviPathDataProcessor;
import com.haloai.hud.hudendpoint.arwaylib.render.strategy.IRenderStrategy;
import com.haloai.hud.hudendpoint.arwaylib.render.strategy.RenderStrategyFactory;
import com.haloai.hud.hudendpoint.arwaylib.test.debug.CrossImageDataCollector;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.hudendpoint.arwaylib.utils.DrawUtils;
import com.haloai.hud.hudendpoint.arwaylib.utils.FileUtils;
import com.haloai.hud.hudendpoint.arwaylib.utils.TimeRecorder;
import com.haloai.hud.navigation.NavigationSDKAdapter;
import com.haloai.hud.utils.HaloLogger;
import com.haloai.hud.utils.ShareDrawables;

import org.rajawali3d.renderer.ISurfaceRenderer;
import org.rajawali3d.view.IDisplay;
import org.rajawali3d.view.ISurface;
import org.rajawali3d.view.TextureView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ARwayOpenGLFragment extends Fragment implements IDisplay, OnMapLoadedListener, OnCameraChangeListener, IStateContoller, INaviUpdater, INaviDisplayPresenter {
    private static final String  TAG                 = ARWayConst.ERROR_LOG_TAG;
    // form HudAMapFragmentNavigation
    public final static  boolean IS_DEBUG_MODE       = false;
    private static final boolean AMAP_OPTIONS_LOGOUT = true;
    private static final boolean IS_SCREEN_SHOOT     = true;

    private Context mContext;

    private static final int GPS_STATUS_FINE = 0;
    private static final int GPS_STATUS_WEEK = 1;
    private static final int GPS_STATUS_BAD  = 2;

    private static final int HANDLER_MSG_UPDATE_PATH = 0;

    //data
    public static final String DEFAULT_FOLDER_PREFIX = "/sdcard/HaloTest/projection/";
    private             String mFolderPath           = DEFAULT_FOLDER_PREFIX;

    //amap
    private AMapNavi mAMapNavi          = null;
    private Bitmap   mCurrentCrossImage = null;
    private boolean  mCrossCanShow      = true;
    private int      mCurrentGpsStatus  = GPS_STATUS_FINE;

    private ViewGroup    mNaviView     = null;
    private AMapNaviView mAmapNaviView = null;

    private int    mLastNaviIconType = 0;
    private Bitmap mNaviIconBitmap   = null;
    private View   arway             = null;

    //opengl
    protected ViewGroup     mLayout;
    protected TextureView   mRenderSurface;
    protected ARwayRenderer mRenderer;
    private boolean mCameraChangeFinish = false;

    //bean
    private static NaviInfoBean mNaviInfoBean = (NaviInfoBean) BeanFactory.getBean(BeanFactory.BeanType.NAVI_INFO);
    private static RouteBean    mRouteBean    = (RouteBean) BeanFactory.getBean(BeanFactory.BeanType.ROUTE);
    private static CommonBean   mCommonBean   = (CommonBean) BeanFactory.getBean(BeanFactory.BeanType.COMMON);

    //navi temp data
    private int mCurStep        = 0;
    private int mCurPoint       = 0;
    private int mCurIndexInPath = 0;
    private int mNaviIcon       = 0;

    private static       CrossImageDataCollector mCrossImageDataCollector = new CrossImageDataCollector();
    private static final int                     DEFAULT_GPS_NUMBER       = 10;
    private int mGpsWorkCnt;
    private boolean mGpsWork = true;

    public ARwayOpenGLFragment() {
        HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG, "fragment 正在初始化");
        // Required empty public constructor
    }

    //draw object
    private DrawScene            mDrawScene            = (DrawScene) DrawObjectFactory.getGlDrawObject(DrawObjectFactory.DrawType.GL_SCENE);
    private GlDrawCompass        mGlDrawCompass        = (GlDrawCompass) DrawObjectFactory.getGlDrawObject(DrawObjectFactory.DrawType.COMPASS);
    private GlDrawNaviInfo       mGlDrawNaviInfo       = (GlDrawNaviInfo) DrawObjectFactory.getGlDrawObject(DrawObjectFactory.DrawType.NAVI_INFO);
    private GlDrawSpeedDial      mGlDrawSpeedDial      = (GlDrawSpeedDial) DrawObjectFactory.getGlDrawObject(DrawObjectFactory.DrawType.SPEED);
    private GlDrawRetainDistance mGlDrawRetainDistance = (GlDrawRetainDistance) DrawObjectFactory.getGlDrawObject(DrawObjectFactory.DrawType.RETAIN_DISTANCE);

    // var
    private boolean mLastIsReady = false;

    //navi
    private IRenderStrategy           mCurrentRenderStrategy;
    private AMapNaviPathDataProcessor mNaviPathDataProcessor;

    //test
    private static TimeRecorder mUpdatePathRecorder   = null;
    private static TimeRecorder mGpsTimeRecorder      = null;
    private static TimeRecorder mLocationTimeRecorder = null;

    static {
        if (ARWayConst.ENABLE_PERFORM_TEST) {
            mUpdatePathRecorder = new TimeRecorder();
            mGpsTimeRecorder = new TimeRecorder();
            mLocationTimeRecorder = new TimeRecorder();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        // Inflate the layout for this fragment
        mLayout = (ViewGroup) inflater.inflate(R.layout.fragment_arway_open_gl, container, false);

        View mainARWayView = DrawObjectFactory.createGlDrawObjectLayoutIntance(mContext, mLayout, R.layout.arway_opengl_layout);

        /*if (mainARWayView !=null  && mainARWayView.getParent()!= null) {
            ViewGroup vg = (ViewGroup)mainARWayView.getParent();
            vg.removeView(mainARWayView);
        }
        mLayout.addView(mainARWayView);*/

        mRenderSurface = (TextureView) mDrawScene.getViewInstance(mContext);
        mRenderer = (ARwayRenderer) createRenderer();
        mRenderer.setTextureView(mRenderSurface);
        onBeforeApplyRenderer();
        applyRenderer();
        initDataAbout();

        arway = mRenderSurface;
        arway.setVisibility(View.VISIBLE);
        if (ARWayConst.IS_AMAP_VIEW) {
            //reset amap navi view
            mNaviView = mLayout;
            // TODO: 16/9/27 需要显示高德底图时调用
            //            mAmapNaviView = (AMapNaviView) mLayout.findViewById(R.id.amap_navi_amapnaviview);
            mAmapNaviView.onCreate(savedInstanceState);
            if (mAmapNaviView.getMap() == null) {
                HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG, "Map is null");
            } else {
                mAmapNaviView.getMap().setOnMapLoadedListener(this);
                mAmapNaviView.getMap().setOnCameraChangeListener(this);
            }
            if (IS_DEBUG_MODE || !ARWayConst.IS_DARW_ARWAY) {
                mAmapNaviView.setVisibility(View.VISIBLE);
                mAmapNaviView.setAlpha(1);
                mAmapNaviView.bringToFront();
            } else {
                mAmapNaviView.setAlpha(0);
                mAmapNaviView.setVisibility(View.VISIBLE);
            }
            AMapNaviViewOptions viewOptions = mAmapNaviView.getViewOptions();
            if (ARWayConst.ENABLE_LOG_OUT && AMAP_OPTIONS_LOGOUT) {
                HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG, String.format("onCreateView,amapview options , curZoom is %d,curTilt is %d", viewOptions.getZoom(), viewOptions.getTilt()));
            }
        }
        hideARWay();
        mDrawScene.animShowHide(false);

        HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG, "naving fragment onCreateView");
        return mLayout;
    }

    //must call after mRenderer is init
    private void initDataAbout() {
        //init renderStrategy and data notifier and set provider to renderer
        mCurrentRenderStrategy = RenderStrategyFactory.generateRenderStrategy();
        mCurrentRenderStrategy.setRenderParamsNotifier(mRenderer);
        mNaviPathDataProcessor = new AMapNaviPathDataProcessor();
        mNaviPathDataProcessor.setRenderStrategy(mCurrentRenderStrategy);
        mNaviPathDataProcessor.setRoadNetChangeNotifier(mRenderer);
        mNaviPathDataProcessor.setNaviPathChangeNotifier(mRenderer);
        mRenderer.setNaviPathDataProvider(mNaviPathDataProcessor.getNaviPathDataProvider());
        mRenderer.setRoadNetDataProvider(mNaviPathDataProcessor.getRoadNetDataProvider());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG, "naving fragment onCreate");
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG, "naving fragment onViewStateRestored");
    }

    public boolean initAMapNaviView() {
        if (ARWayConst.IS_AMAP_VIEW) {
            AMapNaviViewOptions viewOptions = mAmapNaviView.getViewOptions();
            viewOptions.setNaviNight(true);
            viewOptions.setLayoutVisible(false);
            viewOptions.setNaviViewTopic(AMapNaviViewOptions.BLUE_COLOR_TOPIC);
            viewOptions.setCrossDisplayShow(false);
            viewOptions.setAutoChangeZoom(false);
            viewOptions.setAutoDrawRoute(false);
            //        位图必须在绘制前设置
            //        viewOptions.setCarBitmap(carBM);
            //        setEndPointBitmap(Bitmap icon);
            //        viewOptions.setStartPointBitmap(bm);
            //        setWayPointBitmap(Bitmap icon);
            viewOptions.setCompassEnabled(false);
            //        setFourCornersBitmap(Bitmap fourCornersBitmap);
            viewOptions.setLaneInfoShow(false);
            //设置牵引线颜色,-1为不显示
            viewOptions.setLeaderLineEnabled(-1);
            viewOptions.setMonitorCameraEnabled(false);
            //        viewOptions.setNaviViewTopic(-1);
            viewOptions.setRouteListButtonShow(false);
            viewOptions.setSettingMenuEnabled(false);
            viewOptions.setTrafficBarEnabled(false);
            viewOptions.setTrafficLayerEnabled(false);

            mAmapNaviView.setViewOptions(viewOptions);

            AMap aMap = this.mAmapNaviView.getMap();
            CameraPosition cameraPos = aMap.getCameraPosition();
            float curZoom = cameraPos.zoom;
            float curTilt = cameraPos.tilt;
            float maxZoomLevel = ((int) aMap.getMaxZoomLevel()) / 2 * 2;
            if (curZoom < maxZoomLevel || curTilt != 0.0) {
                cameraPos = CameraPosition.builder(cameraPos).tilt(0).zoom(maxZoomLevel).build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPos);
                aMap.moveCamera(cameraUpdate);
                return false;
            }
        }
        return true;

    }

    @Override
    public void onMapLoaded() {
        //        initAMapNaviView();
        if (ARWayConst.IS_AMAP_VIEW) {
            mAmapNaviView.getMap().showMapText(false);
            // TODO: 16/7/22 需要更新版本
            mAmapNaviView.getMap().showBuildings(false);
            mMapProjectionMachine.work(MapProjectionMachine.Operation.MAP_LOADED);
            LogI(ARWayConst.INDICATE_LOG_TAG, "地图加载成功");
        }
        /*if(mNeedUpdatePath ) {//&& mCameraChangeFinish
            if (mAMapNavi != null) {
                LogI(ARWayConst.INDICATE_LOG_TAG," onMapLoaded updatePath called");
                HaloLogger.logE("helong_debug","updatePath pro!=null");
//                updatePath(mAMapNavi);
                updateAmapView();
            }else {
                LogI(ARWayConst.ERROR_LOG_TAG,"updatePath 不成功!!!!!!!");
            }

        }*/
    }

    private MapProjectionMachine mMapProjectionMachine = new MapProjectionMachine();

    {
        MapProjectionMachine.UpdateMapViewCall updateMapViewCall = new MapProjectionMachine.UpdateMapViewCall() {
            @Override
            public boolean updateMapView() {
                return initAMapNaviView();
            }

        };

        MapProjectionMachine.ProjectionOkCall projectionOkCall = new MapProjectionMachine.ProjectionOkCall() {
            @Override
            public void projectionOk() {
                rUpdatePath(mAMapNavi);
            }
        };
        mMapProjectionMachine.init(updateMapViewCall, projectionOkCall);

    }


    private void LogI(String tag, String msg) {
        HaloLogger.logE(tag, msg);
    }

    @Override
    public void onResume() {
        super.onResume();
        mRenderer.onResume();
        if (ARWayConst.IS_AMAP_VIEW) {
            mAmapNaviView.onResume();
        }
        HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG, "naving fragment onResume");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (ARWayConst.IS_AMAP_VIEW) {
            mAmapNaviView.onDestroy();
        }
        HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG, "naving fragment onDestroyView");
    }

    @Override
    public void onPause() {
        super.onPause();
        mRenderer.onPause();
        if (ARWayConst.IS_AMAP_VIEW) {
            mAmapNaviView.onPause();
        }
        HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG, "naving fragment onPause");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        //        mAmapNaviView.onLowMemory();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG, "naving fragment onHiddenChanged ");
        if (ARWayConst.IS_AMAP_VIEW) {
            if (mNaviView != null) {
                if (hidden) {
                    //                mRenderer.pause();
                    mCurrentCrossImage = null;
                    mCrossCanShow = true;
                } else {
                    //                mRenderer.continue_();

                }
            }
        }

        if (!hidden) {
            showHideSpeedPanel(true);
        }
        if (ARWayConst.IS_AMAP_VIEW) {
            removeAMapNaviView();
            if (!hidden) {
                addAMapNaviView();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (ARWayConst.IS_AMAP_VIEW) {
            mAmapNaviView.onSaveInstanceState(outState);
        }
        HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG, "naving fragment onSaveInstanceState");
    }

    @Override
    public ISurfaceRenderer createRenderer() {
        //        return new ARwayOpenGLRenderer(getActivity(),this);
        return new ARwayRenderer(getActivity());
    }

    protected void onBeforeApplyRenderer() {
        mRenderSurface.setAntiAliasingMode(ISurface.ANTI_ALIASING_CONFIG.MULTISAMPLING);
        mRenderSurface.setSampleCount(3);
    }

    protected void applyRenderer() {
        mRenderSurface.setSurfaceRenderer(mRenderer);
    }


    /**
     * 重置导航状态
     * 更新状态和界面,不做动画切换
     */

    private void resetNaviStatus() {
        if (ARWayConst.ENABLE_LOG_OUT) {
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "arway resetNaviStatus called");
        }

        resetNaviResource();
        resetNaviDisplay();

        onNaviViewUpdate();
        updateSpeedDialDisplay();
        updateRetainDistanceDialDisplay();

    }

    /***
     * 重置导航的资源
     */
    private void resetNaviResource() {
        this.mAMapNavi = null;
        this.mLastIsReady = false;
        mMapProjectionMachine.setNeedUpdatePath(false);
        mMapProjectionMachine.setForceUpdateNaviView4Path(false);
    }

    /***
     * 重置的显示
     */
    private void resetNaviDisplay() {
        mGlDrawNaviInfo.resetView();
        mGlDrawRetainDistance.resetView();
        mGlDrawSpeedDial.resetView();
        mGlDrawCompass.resetView();
    }

    /***
     * 导航开始时界面控制
     */
    public void onNavingStartView() {
        hideARWay();
        mDrawScene.animShowHide(false);
        mGlDrawCompass.showHide(true);
        mGlDrawCompass.onNaviStart();
        quickSwitchViewStatus(IDriveStateLister.DriveState.PAUSE);


        //        animSwitchViewStatus(IDriveStateLister.DriveState.PAUSE);
    }

    /***
     * 导航开始时界面控制
     */
    public void prepareNavingStartView() {
        hideARWay();
        mDrawScene.animShowHide(false);
        mGlDrawCompass.showHide(true);
        mGlDrawCompass.onNaviStart();
        // TODO: 16/8/2 确保不会切换错乱
        quickSwitchViewStatus(IDriveStateLister.DriveState.PAUSE);
    }

    /***
     * 导航中时界面控制
     */
    public void onNavingView() {
        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "onNavingView called");
        ARWayController.CommonBeanUpdater.setStartOk(true);

        mGlDrawSpeedDial.showHide(true);
        mGlDrawRetainDistance.showHide(true);
        mGlDrawNaviInfo.showHide(true);

        mGlDrawCompass.showHide(true);
        //        mGlDrawCompass.animShowHide(false);
        mDrawScene.animShowHide(true);
        animSwitchViewStatus(IDriveStateLister.DriveState.DRIVING);

        /*if(!mCommonBean.isGpsWork() || (!mCommonBean.isHasNetwork() && mCommonBean.isYaw())){
            mGlDrawSpeedDial.showHide(false);
            mGlDrawRetainDistance.showHide(false);
            mGlDrawCompass.showHide(true);
            mDrawScene.showHide(false);
            mGlDrawNaviInfo.showHide(false);
        }else {
            mGlDrawSpeedDial.showHide(true);
            mGlDrawRetainDistance.showHide(true);
            mGlDrawNaviInfo.showHide(true);

            mGlDrawCompass.animShowHide(false);
            mDrawScene.animShowHide(true);
            animSwitchViewStatus(IDriveStateLister.DriveState.DRIVING);
        }*/
    }

    /***
     * 导航结束时界面控制
     */
    public void onNavingEndView() {
        //显示gl 场景
        /*GlDrawRetainDistance.getInstance().changeDriveState(IDriveStateLister.DriveState.PAUSE);
        GlDrawSpeedDial.getInstance().changeDriveState(IDriveStateLister.DriveState.PAUSE);*/
        animSwitchViewStatus(IDriveStateLister.DriveState.PAUSE);
        hideARWay();

        updateNaviInfoDisplay();
        updateRetainDistanceDialDisplay();

    }

    /***
     * 导航退出时界面控制
     */
    public void onNavingStopView() {
        animSwitchViewStatus(IDriveStateLister.DriveState.PAUSE);
        hideARWay();

        updateNaviInfoDisplay();
        updateRetainDistanceDialDisplay();

        // TODO: 2016/9/7
        mCrossImageDataCollector.endNavigation();
    }


    /***
     * 偏航开始时界面控制
     */
    public void onYawStartView() {
        //切换显示地图
        /*addAMapNaviView();
        mAmapNaviView.setVisibility(View.VISIBLE);
        mAmapNaviView.setAlpha(1);
        hideARWay();*/
        onNavingStartView();
        onNavingContextChangedView();
        /*updateNaviInfoDisplay();
        animSwitchViewStatus(IDriveStateLister.DriveState.PAUSE);*/
    }

    /***
     * 偏航结束时界面控制
     */
    public void onYawEndView() {
        onNavingContextChangedView();
        //切换隐藏地图
        /*mAmapNaviView.setVisibility(View.INVISIBLE);
        mAmapNaviView.setAlpha(0);
        showARWay();*/

        //        onNavingView();
    }

    /***
     * 导航的环境：GPS、网络，生变变化时的界面更新
     * 显示情景：
     * 无网络
     * 未偏航时，能正常导航
     * 偏航时，文字显示
     * 无GPS
     * 不能更新速度、里程，指南针正常工作
     */
    public void onNavingContextChangedView() {
        // TODO: 16/8/2 sen
        updateNaviInfoDisplay();
        /*if(!mCommonBean.isGpsWork() || (!mCommonBean.isHasNetwork() && mCommonBean.isYaw())){
            mGlDrawSpeedDial.showHide(false);
            mGlDrawRetainDistance.showHide(false);
            mGlDrawCompass.showHide(true);
            mDrawScene.showHide(false);
            mGlDrawNaviInfo.showHide(false);
        }else {
            mGlDrawSpeedDial.showHide(true);
            mGlDrawRetainDistance.showHide(true);
            mDrawScene.showHide(true);
            mGlDrawCompass.showHide(false);
            mGlDrawNaviInfo.showHide(true);
        }*/
    }

    /**
     * 开始导航
     * 显示起步前数据
     * 只改变显示的模式，不改变显示数据的内容
     */
    public void onARWayStart() {
        if (ARWayConst.ENABLE_TEST_LOG) {
            HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG, "onARWayStart called ");
        }
        resetData();
        onNavingStartView();
        //        resetNaviStatus();
        //        ARWayController.ARWayStatusUpdater.resetData();
        ARWayController.CommonBeanUpdater.setNavingStart(true);
        // TODO: 16/8/30 与path的实际更新位置保持一致
        mMapProjectionMachine.setNeedUpdatePath(true);
    }

    /**
     * 每次次导航开始调用
     */
    private void resetData() {
        mCurStep = 0;
        mCurPoint = 0;
        mCurIndexInPath = 0;
        mNaviIcon = 0;
    }

    /**
     * 开始导航前准备操作
     */
    public void prepareARWayStart() {
        if (ARWayConst.ENABLE_TEST_LOG) {
            HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG, "prepareARWayStart called ");
        }
        prepareNavingStartView();
        resetNaviStatus();
    }

    /**
     * 退出ARway时调用
     * stop to draw hudway
     * 重置相关数据，
     */
    public void stopDrawHudway() {
        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "arway stopDrawHudway");

        ARWayController.ARWayStatusUpdater.resetData();
        ARWayController.CommonBeanUpdater.setNaviEnd(true);

        resetNaviStatus();

        onNavingStopView();

        if (mRenderer != null) {
            mRenderer.onNaviStop();
        }
    }

    /**
     * 到达目的地
     */
    public void onArriveDestination() {
        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "arway onArriveDestination");

        ARWayController.CommonBeanUpdater.setNaviEnd(true);
        ARWayController.CommonBeanUpdater.setNavingStart(false);

        onNavingEndView();
        if (mRenderer != null) {
            mRenderer.arriveDestination();
        }
    }

    /**
     * 更新偏航开始画面
     */
    public void updateYawStart() {
        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "arway updateYawStart");
        mRenderer.yawStart();

        if (mCommonBean.isYaw()) {
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "arway updateYawStart,already yaw");
            return;
        }
        if (ARWayConst.IS_YAW_CLEAR_DISPLAY) {
            ARWayController.ARWayStatusUpdater.resetData();
            resetNaviStatus();
        }
        mMapProjectionMachine.setNeedUpdatePath(true);
        if (ARWayConst.IS_AMAP_VIEW) {
            mMapProjectionMachine.work(MapProjectionMachine.Operation.UPDATE_PATH);
        }

        ARWayController.CommonBeanUpdater.setYaw(true);
        onYawStartView();
    }

    /**
     * 更新偏般结束界面
     */
    public void updateYawEnd() {
        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "arway updateYawEnd");
        mRenderer.yawEnd();
        if (!mCommonBean.isYaw()) {
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "arway updateYawStart,state is not yaw");
        }
        ARWayController.CommonBeanUpdater.setYaw(false);

        mMapProjectionMachine.setNeedUpdatePath(true);
        if (ARWayConst.IS_AMAP_VIEW) {
            mMapProjectionMachine.work(MapProjectionMachine.Operation.UPDATE_PATH);
        }
        onYawEndView();
    }

    /**
     * 导航开始或偏航路径计算失败时回调
     *
     * @param errorInfo
     */
    public void onNaviCalculateRouteFailure(int errorInfo) {

    }


    /**
     * gps状态发生变化时回调
     *
     * @param work 是否工作
     */
    public void onGpsStatusChanged(boolean work) {
        if (mGpsTimeRecorder != null) {
            mGpsTimeRecorder.start();
        }
        if (!work) {
            if (--mGpsWorkCnt < 0) {
                mGpsWorkCnt = 0;
            }
        } else {
            mGpsWorkCnt = DEFAULT_GPS_NUMBER;
        }
        /*if(mGpsWorkCnt>0 != mGpsWork){//检测从无到有和从有到无
            mGpsWork = mGpsWorkCnt>0;
            ARWayController.CommonBeanUpdater.setGpsWork(work);
            onNavingContextChangedView();
        }*/
        //        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG,String.format("gps work = %s, mGpsWorkCnt = %s",work,mGpsWorkCnt));
        ARWayController.CommonBeanUpdater.setGpsWork(mGpsWorkCnt > 0);
        onNavingContextChangedView();
        if (mGpsTimeRecorder != null) {
            mGpsTimeRecorder.recordeAndLog(ARWayConst.ERROR_LOG_TAG, "onGpsStatusChanged");
        }
    }

    /**
     * 网络发生变化时回调
     *
     * @param work 是否工作
     */
    public void onNetworkStatusChanged(boolean work) {
        ARWayController.CommonBeanUpdater.setHasNetwork(true);
        //        onNavingContextChangedView();
    }

    /**
     * gps搜星个发生变化
     *
     * @param satelliteNum
     */
    public void setSatelliteNum(int satelliteNum) {
        //TODO 为了在室内也能正常测试使用导航功能,如果没有下面这句,则导航中会一直处于信号差不可用状态,上线记得删除
        //        ARWayController.SceneBeanUpdater.setGpsNumber(satelliteNum);
        if (satelliteNum <= 0) {
            this.mCurrentGpsStatus = GPS_STATUS_BAD;
        } else if (satelliteNum < 5) {
            this.mCurrentGpsStatus = GPS_STATUS_WEEK;
        } else {
            this.mCurrentGpsStatus = GPS_STATUS_FINE;
        }
    }

    /**
     * show cross image in image view
     *
     * @param crossimage Bitmap for show cross
     */
    public void showCrossImage(Bitmap crossimage) {
        // TODO: 2016/10/7 模拟数据
        List<LatLng> crossLines = new ArrayList<>();
        AMapNaviStep curStep = mAMapNavi.getNaviPath().getSteps().get(mCurStep);
        crossLines.add(DrawUtils.naviLatLng2LatLng(curStep.getCoords().get(curStep.getCoords().size() - 1)));
        double totalLength = 0;
        for (int i = mCurStep + 1; i < mAMapNavi.getNaviPath().getSteps().size(); i++) {
            AMapNaviStep step = mAMapNavi.getNaviPath().getSteps().get(i);
            boolean enough = false;
            for (NaviLatLng latLng : step.getCoords()) {
                totalLength += AMapUtils.calculateLineDistance(crossLines.get(crossLines.size() - 1), DrawUtils.naviLatLng2LatLng(latLng));
                crossLines.add(DrawUtils.naviLatLng2LatLng(latLng));
                if (totalLength > 30) {
                    enough = true;
                    break;
                }
            }
            if (enough) {
                break;
            }
        }
        HaloLogger.logE("testtest", "count:" + crossLines.size());
        HaloLogger.logE("testtest", "naviIcon:" + mNaviIcon);
        try {

            HaloLogger.logE("branch_handle", "save a new cross image");
            FileUtils.write(FileUtils.bitmap2Bytes(crossimage), "/sdcard/testimage/oricrossimage/", System.currentTimeMillis() + ".png");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (crossimage != null) {
            if (mCrossCanShow) {
                /*if (mNaviInfoBean != null) {
                    try {

                        HaloLogger.logE("branch_handle","save a new cross image");
                         FileUtils.write(FileUtils.bitmap2Bytes(crossimage),"/sdcard/testimage/oricrossimage/",System.currentTimeMillis()+".png");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mRenderer.handleCrossInfo(mCurStep, crossimage.getWidth(), crossimage.getHeight());
                    mRenderer.setEnlargeCrossBranchLines(crossimage);
                }*/
            } else {
                ARWayController.NaviInfoBeanUpdate.setCrossBitmap(null);
            }
        }
    }

    /**
     * 处理路口放大图，去掉灰色背景，50为颜色容忍度（左右偏差值）
     *
     * @param crossimage
     * @return
     */
    @SuppressWarnings("deprecation")
    private Bitmap handleCrossImageBack(Bitmap crossimage) {
        // start with a Bitmap bmp
        Bitmap newBmp = crossimage.copy(Bitmap.Config.ARGB_8888, true);
        Canvas c = new Canvas(newBmp);

        // get the int for the colour which needs to be removed
        Paint paint = new Paint();// 去锯齿
        paint.setAntiAlias(true);// 防抖动
        paint.setDither(true);// 图像过滤
        paint.setFilterBitmap(true);
        paint.setARGB(255, 0, 0, 0); // ARGB for the color to replace

        paint.setXfermode(new AvoidXfermode(0x616161, 50, AvoidXfermode.Mode.TARGET));
        c.drawPaint(paint);

        return newBmp;
    }

    /**
     * clear image view
     */
    public void hideCrossImage() {
        ARWayController.NaviInfoBeanUpdate.setCrossBitmap(null);
    }

    /**
     * update time and distance
     *
     * @param time
     * @param distance
     */
    public void updateTimeAndDistance(int time, int distance) {

    }

    /**
     * update navi info with navi icon and current link distance
     *
     * @param naviIcon 表示诱导信息的int值
     * @param distance 当前导航信息所剩距离
     */
    public void updateRouteInfo(int naviIcon, int distance) {

    }

    /**
     * update location to show hudway
     *
     * @param location
     */
    public void updateLocation(AMapNaviLocation location) {
        if (mLocationTimeRecorder != null) {
            mLocationTimeRecorder.start();
        }
        // FIXME: 16/6/28 直接更新位置进去，在ARWYAN库中判断，方便根据情况处理显示
        boolean matchPath = location.isMatchNaviPath();
        if (matchPath) {
            onGpsStatusChanged(true);
        }
        if (mRenderer != null && matchPath && ARWayConst.IS_DARW_ARWAY) {
            //mRenderer.updateLocation(location, mCurIndexInPath);
            mNaviPathDataProcessor.setLocation(location, mRenderer.getCurPos(), mRenderer.getCurDegrees());
        }
        ARWayController.CommonBeanUpdater.setMatchNaviPath(matchPath);
        onNavingContextChangedView();
        if (mLocationTimeRecorder != null) {
            mLocationTimeRecorder.recordeAndLog(ARWayConst.ERROR_LOG_TAG, "updateLocation");
        }
    }

    private int getIndexInPath(AMapNavi amapNavi, int currentPoint, int currentStep) {
        if (amapNavi == null || amapNavi.getNaviPath() == null || amapNavi.getNaviPath().getCoordList() == null ||
                amapNavi.getNaviPath().getCoordList().size() <= 0) {
            return 0;
        }
        int currentIndex = 0;
        List<NaviLatLng> pathLatLngs = amapNavi.getNaviPath().getCoordList();
        for (int i = 0; i < currentStep; i++) {
            currentIndex += amapNavi.getNaviPath().getSteps().get(i).getCoords().size();
        }
        currentIndex += currentPoint;
        if (currentIndex >= pathLatLngs.size()) {
            currentIndex = pathLatLngs.size() - 1;
        }
        return currentIndex;
    }

    /**
     * update current point and step `s index in list
     *
     * @param curPoint
     * @param curStep
     */
    public void updateCurPointIndex(int curPoint, int curStep) {
        //        ARWayController.SceneBeanUpdater.setCurrentPoint(curPoint);
        //        ARWayController.SceneBeanUpdater.setCurrentStep(curStep);
    }

    /***
     * // 导航中的文字提示，可用于语音播报,
     */
    public void updateNaviText(NavigationSDKAdapter.NavigationNotifier.NaviTextType textType, String text) {
        ARWayController.NaviInfoBeanUpdate.setNaviText(text);
        if (mGlDrawNaviInfo != null) {
            mGlDrawNaviInfo.doDraw();
        }
        if (mGlDrawCompass != null) {
            mGlDrawCompass.doDraw();
        }
    }

    private void saveAmapViewBitmap() {
        if (IS_SCREEN_SHOOT) {
            final String path = mFolderPath + "image/";
            final long time = System.currentTimeMillis();
            String name = "projcetion_image_" + time + ".png";
            if (mAmapNaviView != null) {
                mAmapNaviView.getMap().getMapScreenShot(new AMap.OnMapScreenShotListener() {
                    @Override
                    public void onMapScreenShot(Bitmap bitmap) {
                        try {
                            FileUtils.write(FileUtils.bitmap2Bytes(bitmap), path, "cross_image_" + time + "_shoot" + ".png");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onMapScreenShot(Bitmap bitmap, int i) {

                    }
                });
            }
        }
    }

    /**
     * 外部接口，更新导航path
     *
     * @param aMapNavi
     */
    public void updatePath(AMapNavi aMapNavi) {
        this.mAMapNavi = aMapNavi;
        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "updatePath called");
        if (!ARWayConst.IS_AMAP_VIEW) {//不需要绽放比例直接更新路径
            //rUpdatePath(aMapNavi);
            mMapProjectionMachine.work(MapProjectionMachine.Operation.UPDATE_PATH);
        }
    }

    public void onNaviStarted() {
        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "onNaviStarted called");
        if (ARWayConst.IS_AMAP_VIEW) {
            mMapProjectionMachine.work(MapProjectionMachine.Operation.UPDATE_PATH);
        }
    }


    /**
     * 路线规划成功或者是偏航后的重新规划后调用该方法重新设置路线图
     *
     * @param aMapNavi
     * @return 0 设置成功
     * -1 AMapNavi 为空
     * -2 路径太长(dead)
     * -3 renderer出错
     */
    private int rUpdatePath(AMapNavi aMapNavi) {
        int result = -1;
        if (aMapNavi == null) {
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "rUpdatePath,aMapNavi is null ");
            return result;
        }
        AMapNaviPath naviPath = aMapNavi.getNaviPath();
        HaloLogger.logE("helong_debug", "updatePath");
        if (naviPath != null) {//mCameraChangeFinish &&  mMapLoaded &&
            if (mRenderer != null) {
                hideARWay();
                mDrawScene.animShowHide(false);
                mGlDrawCompass.showHide(true);
                HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "arway rUpdatePath initPath,mode is " + aMapNavi.getNaviPath().getStrategy());
                if (ARWayConst.ENABLE_LOG_OUT) {
                    HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "arway rUpdatePath total poinst size is " + naviPath.getCoordList().size());
                }
                if (ARWayConst.NAVI_ENABLE_RESTRICT_DISTANCE && naviPath.getCoordList().size() > ARWayConst.NAVI_MAX_RESTRICT_POINT_NUMBER) {
                    return -2;
                }
                if (ARWayConst.IS_DARW_ARWAY) {
                    HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "mRenderer.initPath called ");
                    Projection projection = null;
                    if (ARWayConst.IS_AMAP_VIEW) {
                        projection = mAmapNaviView.getMap().getProjection();
                    }
                    if (mUpdatePathRecorder != null) {
                        mUpdatePathRecorder.start();
                    }
                    //mRenderer.initPath(projection, naviPath, (!mMapProjectionMachine.isNeedUpdatePath()));
                    mNaviPathDataProcessor.setPath(mAMapNavi,naviPath);
                    if (ARWayConst.ENABLE_PERFORM_TEST) {
                        mUpdatePathRecorder.recordeAndLog(ARWayConst.ERROR_LOG_TAG, "UpdatePath");
                    }
                }
                result = 0;
                // TODO: 16/9/13 测试直接起步
                onNavingView();
            } else {
                result = -3;
                HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "arway rUpdatePath Renderer is null");
            }
            // TODO: 16/9/5 此处不能重置所有数据，这样会造成显示成功的naviinfo信息被清空
            //            ARWayController.ARWayStatusUpdater.resetData();
            //            resetNaviStatus();
            //更新总距离
            int distance = aMapNavi.getNaviPath().getAllLength();
            ARWayController.NaviInfoBeanUpdate.setPathTotalDistance(distance);
            ARWayController.CommonBeanUpdater.setNavingStart(true);
            mMapProjectionMachine.setNeedUpdatePath(false);

        } else {
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "arway rUpdatePath failed," + "path is null " + (naviPath == null));
            mMapProjectionMachine.setNeedUpdatePath(true);
        }

        return result;

    }

    /**
     * 更新ARway中的导航数据
     *
     * @param info
     */
    public void updateNaviInfo(NaviInfo info) {
        if (info == null) {
            return;
        }
        // TODO: 2016/10/23 ARWay新架构
        mNaviPathDataProcessor.setNaviInfo(info);
        mNaviIcon = info.getIconType();
        mCurPoint = info.getCurPoint();
        mCurStep = info.getCurStep();
        mCurIndexInPath = getIndexInPath(mAMapNavi, mCurPoint, mCurStep);
        updateNaviInfoDate(info);
        onNaviViewUpdate();
    }

    private void onNaviViewUpdate() {
        if (mGlDrawRetainDistance != null) {
            mGlDrawRetainDistance.doDraw();
        }
        if (mGlDrawNaviInfo != null) {
            mGlDrawNaviInfo.doDraw();
            if (mGlDrawRetainDistance != null) {
                //                mGlDrawRetainDistance.showHide(mGlDrawNaviInfo.getNaviStatusText() == null);
            }
        }
        boolean ready = isNavingReady();
        if ((this.mLastIsReady != ready) && mCommonBean.isNavingStart()) {//|| (!arway.isShown())
            if (ready) {
                //                mDrawScene.animShowHide(true);
                //                onNavingView();
            }
            //            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG,"onNaviViewUpdate ,mLastIsReady is "+mLastIsReady+"    ,"+ready);
            this.mLastIsReady = ready;
        }
    }

    private void updateNaviInfoDate(NaviInfo info) {
        int iconResource = ShareDrawables.getNaviDirectionId(info.getIconType());//info
        Bitmap iconBitmap = null;
        if (mLastNaviIconType != iconResource && iconResource != 0) {
            iconBitmap = BitmapFactory.decodeResource(getActivity().getResources(), iconResource);
            mNaviIconBitmap = iconBitmap;
            mLastNaviIconType = iconResource;
        }
        ARWayController.NaviInfoBeanUpdate.setNaviIconBitmap(mNaviIconBitmap);
        //update arway data
        ARWayController.NaviInfoBeanUpdate
                .setNaviIconDist(info.getCurStepRetainDistance())
                .setCurrentRoadName(info.getCurrentRoadName())
                .setNextRoadName(info.getNextRoadName())
                .setPathRetainDistance(info.getPathRetainDistance())
                .setPathRetainTime(info.getPathRetainTime())
                .setNaviIcon(info.getIconType())
                .setStepRetainDistance(info.getCurStepRetainDistance());
    }

    public void onSpeedUpgraded(float speed) {
        ARWayController.SpeedBeanUpdater.setSpeed((int) speed);
        updateSpeedDialDisplay();
    }

    /**
     * 更新并显示下一条路的路名,此处需要知道路名和该路是左转,右转还是掉头.
     *
     * @param nextRoadName
     * @param nextRoadType
     */
    public void updateNextRoadName(String nextRoadName, RouteBean.NextRoadType nextRoadType) {
        String str = "";
        if (mCurrentGpsStatus != GPS_STATUS_BAD) {
            str = nextRoadName;
        }
        //        ARWayController.SceneBeanUpdater.setNextRoadName(str, nextRoadType);
    }


    /**
     * startHomeAnimator to draw hudway
     */
    public void startDrawHudway() {
        //        mRenderer.start();
    }

    /**
     * hide ARWay
     */
    public void hideARWay() {
        //        mRenderer.pause();
        if (arway != null) {
            //            ViewGroup vg = (ViewGroup) arway.getParent();
            //            vg.removeView(arway);
            arway.setVisibility(View.INVISIBLE);
            //            arway.setAlpha(0.1f);
        }

    }

    /**
     * show ARWay
     */
    public void showARWay() {
        //        mRenderer.continue_();
        if (arway != null && !arway.isShown()) {//
            arway.setVisibility(View.VISIBLE);
            arway.setAlpha(1);
        }

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (ARWayConst.ENABLE_LOG_OUT && ARWayConst.ENABLE_FAST_LOG) {
            HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG, "onCameraChange called");
        }
        mCameraChangeFinish = false;
    }

    @Override
    public void onCameraChangeFinish(CameraPosition arg0) {
        if (ARWayConst.ENABLE_LOG_OUT && ARWayConst.ENABLE_FAST_LOG) {
            HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG, "onCameraChangeFinish called");
        }
        if (ARWayConst.IS_AMAP_VIEW) {
            mMapProjectionMachine.work(MapProjectionMachine.Operation.MAP_SCALED);
        }

    }

    public void removeAMapNaviView() {
        if (mAmapNaviView != null && mAmapNaviView.getParent() != null) {
            ViewGroup parent = (ViewGroup) mAmapNaviView.getParent();
            HaloLogger.logE(TAG, "glFragment,removeAMapNaviView called");
            parent.removeView(mAmapNaviView);
        }
    }

    public void addAMapNaviView() {
        if (mAmapNaviView != null) {
            removeAMapNaviView();
            ViewGroup parent = (ViewGroup) mAmapNaviView.getParent();
            if (parent == null) {
                HaloLogger.logE(TAG, "glFragment,addAMapNaviView called");
                mNaviView.addView(mAmapNaviView);
            }
        }
    }

    /**
     * 是否起步超过一定距离
     *
     * @return
     */
    private boolean isNavingReady() {
        NaviInfoBean naviBean = (NaviInfoBean) BeanFactory.getBean(BeanFactory.BeanType.NAVI_INFO);
        int totalDistance = naviBean.getPathTotalDistance();
        int distance = naviBean.getPathRetainDistance();
        return totalDistance >= distance && (totalDistance - distance) > ARWayConst.NAVI_CAR_START_DISTANCE;
    }

    void updateNaviInfoDisplay() {
        if (mGlDrawNaviInfo != null) {
            mGlDrawNaviInfo.doDraw();
        }
    }

    void updateRetainDistanceDialDisplay() {
        if (mGlDrawRetainDistance != null) {
            mGlDrawRetainDistance.doDraw();
        } else {
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "updateRetainDistanceDialDisplay ,mGlDrawRetainDistance is null !");
        }
    }

    void updateSpeedDialDisplay() {
        if (mGlDrawSpeedDial != null) {
            mGlDrawSpeedDial.doDraw();
        } else {
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "updateSpeedDialDisplay ,mGlDrawSpeedDial is null !");
        }
    }

    /***
     * 切换驾车起步和驾驶后view显示
     *
     * @param state
     */
    public void animSwitchViewStatus(IDriveStateLister.DriveState state) {
        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "animSwitchViewStatus ,state is " + state);
        GlDrawRetainDistance.getInstance().changeDriveState(state);
        GlDrawCompass.getInstance().changeDriveState(state);
        GlDrawSpeedDial.getInstance().changeDriveState(state);

    }

    public void quickSwitchViewStatus(IDriveStateLister.DriveState state) {
        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "quickSwitchViewStatus ,state is " + state);
        int duration = 1;
        GlDrawRetainDistance.getInstance().changeDriveState(state, duration);
        GlDrawCompass.getInstance().changeDriveState(state, duration);
        GlDrawSpeedDial.getInstance().changeDriveState(state, duration);

    }

    /**
     * 配合主工程是否显示表盘
     *
     * @param show
     */
    public void showHideSpeedPanel(boolean show) {
        if (mGlDrawSpeedDial != null) {
            mGlDrawSpeedDial.showHide(show);
        }
    }

    /**
     * 显示显示指南针
     *
     * @param show
     */
    public void showHideCompass(boolean show) {
        if (mGlDrawCompass != null) {
            mGlDrawCompass.animShowHide(show);
        }
    }

    /**
     * 是否显示里程表盘
     *
     * @param show
     */
    public void showHideDistancePanel(boolean show) {
        if (mGlDrawRetainDistance != null) {
            mGlDrawRetainDistance.showHide(show);
        }
    }

    /***
     * 暂停绘制ARway
     */
    public void pauseARWay() {
        ARWayConst.IS_DARW_ARWAY = false;
        mRenderer.clearScene();
        mRenderer.setFrameRate(1);
    }

    /**
     * 重新绘制ARway
     */
    public void resumeARWay() {
        ARWayConst.IS_DARW_ARWAY = true;
        mRenderer.setFrameRate(ARWayConst.FRAME_RATE);
    }

    public void setEvent(int type) {
        mRenderer.setEvent(type);
    }

    // TODO: 2016/10/14
    public void changeRoadShowWidthBy(double changeValue) {
        mRenderer.changeRoadShowWidthBy(changeValue);
    }

    // TODO: 2016/10/14
    public double changeCameraZBy(double changeValue) {
        return mRenderer.changeCameraZBy(changeValue);
    }

    // TODO: 2016/10/14
    public double changeCameraLookDistBy(double changeValue) {
        return mRenderer.changeCameraLookDistBy(changeValue);
    }

    public void changeStrategy(boolean bol){
        if(bol){
            mRenderer.changeStrategy(IRenderStrategy.DataLevel.LEVEL_20);
        }else{
            mRenderer.changeStrategy(IRenderStrategy.DataLevel.LEVEL_18);
        }
    }
}
