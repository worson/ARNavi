package com.haloai.hud.hudendpoint.arwaylib.arway.impl;


import android.app.Fragment;
import android.content.Context;
import android.graphics.AvoidXfermode;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviInfo;
import com.haloai.hud.hudendpoint.arwaylib.ARWayController;
import com.haloai.hud.hudendpoint.arwaylib.R;
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
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.navigation.NavigationSDKAdapter;
import com.haloai.hud.utils.HaloLogger;
import com.haloai.hud.utils.ShareDrawables;

import org.rajawali3d.renderer.ISurfaceRenderer;
import org.rajawali3d.view.IDisplay;
import org.rajawali3d.view.TextureView;


public class ARwayOpenGLFragment extends Fragment implements IDisplay ,OnMapLoadedListener, OnCameraChangeListener {
    // form HudAMapFragmentNavigation
    public final static boolean IS_DEBUG_MODE=false;

    private Context mContext;

    private static final int GPS_STATUS_FINE         = 0;
    private static final int GPS_STATUS_WEEK         = 1;
    private static final int GPS_STATUS_BAD          = 2;

    private static final int HANDLER_MSG_UPDATE_PATH = 0;




    //amap
    private AMapNavi mAMapNavi          = null;
    private boolean  mNeedUpdatePath    = false;
    private boolean  mMapLoaded         = false;
    private Bitmap   mCurrentCrossImage = null;
    private boolean  mCrossCanShow      = true;
    private int      mCurrentGpsStatus  = GPS_STATUS_FINE;

    private ViewGroup  mNaviView     = null;
    private AMapNaviView mAmapNaviView = null;

    private int mLastNaviIconType = 0;
    private Bitmap mNaviIconBitmap = null;
    private View arway;



    //opengle
    protected ViewGroup           mLayout;
    protected TextureView         mRenderSurface;
    protected ARwayRenderer mRenderer;
    private boolean mCameraChangeFinish = false;

    //bean
    private static NaviInfoBean mNaviInfoBean = (NaviInfoBean) BeanFactory.getBean(BeanFactory.BeanType.NAVI_INFO);
    private static RouteBean    mRouteBean    = (RouteBean) BeanFactory.getBean(BeanFactory.BeanType.ROUTE);
    private static CommonBean   mCommonBean   = (CommonBean) BeanFactory.getBean(BeanFactory.BeanType.COMMON);


    public ARwayOpenGLFragment() {
        HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG,"fragment 正在初始化");
        // Required empty public constructor
    }

    //draw object
    private DrawScene            mDrawScene            = (DrawScene)DrawObjectFactory.getGlDrawObject(DrawObjectFactory.DrawType.GL_SCENE);
    private GlDrawCompass        mGlDrawCompass        = (GlDrawCompass)DrawObjectFactory.getGlDrawObject(DrawObjectFactory.DrawType.COMPASS);
    private GlDrawNaviInfo       mGlDrawNaviInfo       = (GlDrawNaviInfo)DrawObjectFactory.getGlDrawObject(DrawObjectFactory.DrawType.NAVI_INFO);
    private GlDrawSpeedDial      mGlDrawSpeedDial      = (GlDrawSpeedDial)DrawObjectFactory.getGlDrawObject(DrawObjectFactory.DrawType.SPEED);
    private GlDrawRetainDistance mGlDrawRetainDistance = (GlDrawRetainDistance)DrawObjectFactory.getGlDrawObject(DrawObjectFactory.DrawType.RETAIN_DISTANCE);

    // var
    private boolean mLastIsReady    = false;


    private Runnable mUpdatePathRunable = new Runnable() {
        @Override
        public void run() {
            updatePath(mAMapNavi);
        }
    };
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case HANDLER_MSG_UPDATE_PATH:
                    updatePath(mAMapNavi);
                    break;

            }
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        // Inflate the layout for this fragment
        mLayout = (ViewGroup) inflater.inflate(R.layout.fragment_arway_open_gl, container, false);

        View mainARWayView = DrawObjectFactory.createGlDrawObjectLayoutIntance(mContext, mLayout);

        /*if (mainARWayView !=null  && mainARWayView.getParent()!= null) {
            ViewGroup vg = (ViewGroup)mainARWayView.getParent();
            vg.removeView(mainARWayView);
        }
        mLayout.addView(mainARWayView);*/

        mRenderSurface = (TextureView) mDrawScene.getViewInstance(mContext);
        mRenderer = (ARwayRenderer) createRenderer();
        onBeforeApplyRenderer();
        applyRenderer();


        mNaviView = mLayout;
        arway = mRenderSurface;
        //init amap navi view
        mAmapNaviView = (AMapNaviView) mLayout.findViewById(R.id.amap_navi_amapnaviview);
        mAmapNaviView.onCreate(savedInstanceState);

        if (mAmapNaviView.getMap() == null) {
            HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG,"Map is null");
        }else {
            mAmapNaviView.getMap().setOnMapLoadedListener(this);
            mAmapNaviView.getMap().setOnCameraChangeListener(this);
        }
        if (IS_DEBUG_MODE) {
            mAmapNaviView.setVisibility(View.VISIBLE);
            mAmapNaviView.setAlpha(1);
            mAmapNaviView.bringToFront();
        } else {
            mAmapNaviView.setVisibility(View.INVISIBLE);
        }
        // FIXME: 16/8/2 移除地图基本保证转换出的opengl坐标是正常的
        removeAMapNaviView();
        hideARWay();
        mDrawScene.showHide(false);
        HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG,"fragment onCreateView");
        return mLayout;
    }


    @Override
    public ISurfaceRenderer createRenderer() {
//        return new ARwayOpenGLRenderer(getActivity(),this);
        return new ARwayRenderer(getActivity());
    }

    protected void onBeforeApplyRenderer() {
    }

    protected void applyRenderer() {
        mRenderSurface.setSurfaceRenderer(mRenderer);
    }


    private void resetNaviStatus(){
        this.mLastIsReady = false;
        this.mNeedUpdatePath = false;

        mGlDrawNaviInfo.resetView();
        mGlDrawRetainDistance.resetView();
        mGlDrawSpeedDial.resetView();
        mGlDrawCompass.resetView();

        onNaviViewUpdate();
        updateSpeedDialDisplay();
        updateRetainDistanceDialDisplay();
        if (ARWayConst.ENABLE_LOG_OUT){
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "arway resetNaviStatus called" );
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        mRenderer.onResume();
        mAmapNaviView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mRenderer.onPause();
        mAmapNaviView.onPause();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (mNaviView != null) {
            if (hidden) {
//                mRenderer.pause();
                mCurrentCrossImage = null;
                mCrossCanShow = true;
            } else {
//                mRenderer.continue_();

            }
        }

        removeAMapNaviView();
        if (!hidden) {
            addAMapNaviView();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAmapNaviView.onSaveInstanceState(outState);
    }

    public void setSatelliteNum(int satelliteNum) {
        //TODO 为了在室内也能正常测试使用导航功能,如果没有下面这句,则导航中会一直处于信号差不可用状态,上线记得删除
//        ARWayController.SceneBeanUpdater.setGpsNumber(satelliteNum);
        satelliteNum += 10;
        if (satelliteNum <= 0) {
            //mIv_gps_status.setImageResource(R.drawable.greenline_navigation_satellite_red);
            //mTv_gps_status.setText("GPS信号弱");
            //mTv_gps_status.setVisibility(View.VISIBLE);
            //mIv_gps_status.setVisibility(View.VISIBLE);
            this.mCurrentGpsStatus = GPS_STATUS_BAD;
        } else if (satelliteNum < 5) {
            //mIv_gps_status.setImageResource(R.drawable.greenline_navigation_satellite_yellow);
            //mTv_gps_status.setText("GPS信号弱");
            //mTv_gps_status.setVisibility(View.VISIBLE);
            //mIv_gps_status.setVisibility(View.VISIBLE);
            this.mCurrentGpsStatus = GPS_STATUS_WEEK;
        } else {
            //mIv_gps_status.setImageBitmap(null);
            //mTv_gps_status.setText("");
            this.mCurrentGpsStatus = GPS_STATUS_FINE;
        }
    }

    /**
     * 开始导航
     */
    public void onARWayStart() {
        if (ARWayConst.ENABLE_TEST_LOG){
            HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG,"onARWayStart called ,begin");
        }
        hideARWay();
        mDrawScene.showHide(false);
        mGlDrawCompass.showHideInstant(true);
        resetNaviStatus();
        ARWayController.CommonBeanUpdater.setNavingStart(true);
        if (ARWayConst.ENABLE_TEST_LOG){
            HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG,"onARWayStart called ,end");
        }
    }

    /**
     * show cross image in image view
     *
     * @param crossimage Bitmap for show cross
     */
    public void showCrossImage(Bitmap crossimage) {
        if (crossimage == null){
            ARWayController.NaviInfoBeanUpdate.setCrossBitmap(null);
            return;
        }
        if (crossimage != null) {
            if (mCrossCanShow) {
                if (mNaviInfoBean != null) {
                    try {
                        mRenderer.setEnlargeCrossBranchLines(mNaviInfoBean.getStepRetainDistance(), mNaviInfoBean.getNaviIcon());
                    }catch(Exception e){
                        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG,"showCrossImage ,image set error!! "+e.toString());
                    }
                }
                ARWayController.NaviInfoBeanUpdate.setCrossBitmap(mCurrentCrossImage);
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
        Paint mPaint = new Paint();// 去锯齿
        mPaint.setAntiAlias(true);// 防抖动
        mPaint.setDither(true);// 图像过滤
        mPaint.setFilterBitmap(true);
        mPaint.setARGB(255, 0, 0, 0); // ARGB for the color to replace

        mPaint.setXfermode(new AvoidXfermode(0x616161, 50, AvoidXfermode.Mode.TARGET));
        c.drawPaint(mPaint);

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
        if (mCurrentGpsStatus == GPS_STATUS_BAD) {
            //mTv_time_distance.setText("");
            return;
        }
        //mTv_time_distance.setText("剩余" + (distance / 1000 + 1) + "km" + "≈" + (time / 60 + 1) + "min");
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
        // FIXME: 16/6/28 直接更新位置进去，在ARWYAN库中判断，方便根据情况处理显示
//        ARWayController.SceneBeanUpdater.setCurrentLocation(location);
        /*if (mCurrentGpsStatus != GPS_STATUS_FINE) {
            HaloLogger.logE("sen_debug_location","onLocationChanged ,but gps star is 0");
            mRenderer.pause();
        } else {
            mRenderer.continue_();
            ARWayController.SceneBeanUpdater.setCurrentLocation(location);
        }*/
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
     * */
    public void updateNaviText(NavigationSDKAdapter.NavigationNotifier.NaviTextType textType, String text){
        ARWayController.NaviInfoBeanUpdate.setNaviText(text);
        if (mGlDrawNaviInfo != null) {
            mGlDrawNaviInfo.doDraw();
        }
    }

    /**
     * 路线规划成功或者是偏航后的重新规划后调用该方法重新设置路线图
     * @param aMapNavi
     * @return
     * 0 设置成功
     * -1 AMapNavi 为空
     * -2 路径太长
     * -3 renderer出错
     */
    public int updatePath(AMapNavi aMapNavi) {
        int result = -1;
        if (aMapNavi == null) {
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG,"updatePath,aMapNavi is null ");
            return result;
        }
        this.mAMapNavi = aMapNavi;
        Projection projection = mAmapNaviView.getMap().getProjection();
        AMapNaviPath naviPath = aMapNavi.getNaviPath();
        if (projection != null && naviPath != null) {//mCameraChangeFinish &&  mMapLoaded &&
            if (mRenderer != null) {
                hideARWay();
                mDrawScene.showHide(false);
                mGlDrawCompass.showHideInstant(true);
                HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "arway updatePath setPath,mode is "+aMapNavi.getNaviPath().getStrategy());
                if(ARWayConst.ENABLE_LOG_OUT){
                    HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "arway updatePath total poinst size is "+naviPath.getCoordList().size());
                }
                if (ARWayConst.NAVI_ENABLE_RESTRICT_DISTANCE && naviPath.getCoordList().size() > ARWayConst.NAVI_MAX_RESTRICT_POINT_NUMBER){
                    return -2;
                }
                mRenderer.setPath(projection, naviPath,(!mNeedUpdatePath));//
                result=0;

            } else {
                result=-3;
                HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "arway updatePath Renderer is null");
            }
            ARWayController.ARWayStatusUpdater.resetData();
            resetNaviStatus();
            //更新总距离
            int distance = aMapNavi.getNaviPath().getAllLength();
            ARWayController.NaviInfoBeanUpdate.setPathTotalDistance(distance);
            ARWayController.CommonBeanUpdater.setNavingStart(true);
            mNeedUpdatePath = false;
        } else {
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "arway updatePath failed,projection is null?" + (projection == null) + "path is null??" + (naviPath == null));
            mNeedUpdatePath = true;
        }

        return result;

    }

    /**
     * 更新ARway中的导航数据
     *
     * @param info
     */
    public void updateNaviInfo(NaviInfo info){
        if (info == null) {
            return;
        }
        updateNaviInfoDate(info);

        int distance = info.getPathRetainDistance();
        if(arway.isShown() ){
            mRenderer.setRetainDistance(distance);
        }
        onNaviViewUpdate();

        if (ARWayConst.ENABLE_LOG_OUT){
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG,"updateNaviInfo called , distance is "+distance);
        }

    }

    private void onNaviViewUpdate() {
        if (mGlDrawRetainDistance != null) {
            mGlDrawRetainDistance.doDraw();
        }
        if (mGlDrawNaviInfo != null) {
            mGlDrawNaviInfo.doDraw();
            if (mGlDrawRetainDistance != null) {
                mGlDrawRetainDistance.showHide(mGlDrawNaviInfo.getNaviStatusText() == null);
            }
        }
        boolean ready = isNavingReady();
        if((this.mLastIsReady != ready) && mCommonBean.isNavingStart() ){//|| (!arway.isShown())
            if(ready){
                mDrawScene.showHide(true);
                ARWayController.CommonBeanUpdater.setStartOk(true);
                switchViewStatus(IDriveStateLister.DriveState.DRIVING);
            }else {
                // FIXME: 16/7/30 导航到最后的时候，距离结点的距离会显示为起点的大小 
                /*mDrawScene.showHide(false);
                ARWayController.CommonBeanUpdater.setStartOk(false);
                switchViewStatus(IDriveStateLister.DriveState.PAUSE);*/
            }
//            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG,"onNaviViewUpdate ,mLastIsReady is "+mLastIsReady+"    ,"+ready);
            this.mLastIsReady = ready;
        }
    }

    private void updateNaviInfoDate(NaviInfo info) {
        // TODO: 16/7/10 sen ,需要引用主工程的转向标资源
        int iconResource = ShareDrawables.getNaviDirectionId(info.getIconType());//info
        Bitmap iconBitmap = null;
        // TODO: 16/6/22 应该不会内存溢出
        if(mLastNaviIconType !=iconResource &&  iconResource != 0){
            iconBitmap = BitmapFactory.decodeResource(getActivity().getResources(), iconResource);
            mNaviIconBitmap = iconBitmap;
            mLastNaviIconType = iconResource;
        }
        ARWayController.NaviInfoBeanUpdate.setNaviIconBitmap(mNaviIconBitmap).setNaviIcon(iconResource);
        //update arway data
        ARWayController.NaviInfoBeanUpdate.setNaviIconDist(info.getCurStepRetainDistance())
                .setCurrentRoadName(info.getCurrentRoadName())
                .setNextRoadName(info.getNextRoadName())
                .setPathRetainDistance(info.getPathRetainDistance())
                .setPathRetainTime(info.getPathRetainTime())
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


    public void initAMapNaviView() {
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
        }

    }

    @Override
    public void onMapLoaded() {
//        initAMapNaviView();
        mAmapNaviView.getMap().showMapText(false);
        // TODO: 16/7/22 需要更新版本
        mAmapNaviView.getMap().showBuildings(false);
        ARWayController.SceneBeanUpdater
                .setProjection(mAmapNaviView.getMap().getProjection());

        if(!mMapLoaded){
            mMapLoaded=true;
        }
        LogI(ARWayConst.INDICATE_LOG_TAG,"地图加载成功");
        if(mNeedUpdatePath ) {//&& mCameraChangeFinish
            if (mAMapNavi != null) {
                LogI(ARWayConst.INDICATE_LOG_TAG," onMapLoaded updatePath called");
                updatePath(mAMapNavi);

            }else {
                LogI(ARWayConst.ERROR_LOG_TAG,"updatePath 不成功!!!!!!!");
            }

        }


    }

    private void LogI(String tag, String msg) {
        HaloLogger.logE(tag,msg);
    }


    /**
     *更新偏航开始画面
     */
    public void updateYawStart(){
        mRenderer.yawStart();
        //切换显示地图
        /*addAMapNaviView();
        mAmapNaviView.setVisibility(View.VISIBLE);
        mAmapNaviView.setAlpha(1);
        hideARWay();*/

        ARWayController.CommonBeanUpdater.setYaw(true);
        updateNaviInfoDisplay();
        switchViewStatus(IDriveStateLister.DriveState.PAUSE);
    }
    /**
     *
     *更新偏般结束界面
     */
    public void updateYawEnd(){
        //切换隐藏地图
        /*mAmapNaviView.setVisibility(View.INVISIBLE);
        mAmapNaviView.setAlpha(0);
        showARWay();*/

        startDrawHudway();
        mRenderer.yawEnd();

        ARWayController.CommonBeanUpdater.setYaw(false);
        updateNaviInfoDisplay();
    }


    /**
     * 退出ARway时调用
     * stop to draw hudway
     */
    public void stopDrawHudway() {
        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "arway stopDrawHudway");
        ARWayController.ARWayStatusUpdater.resetData();
        resetNaviStatus();
        switchViewStatus(IDriveStateLister.DriveState.PAUSE);
        ARWayController.CommonBeanUpdater.setNaviEnd(true);
        updateNaviInfoDisplay();
        updateRetainDistanceDialDisplay();

    }

    /**
     * 到达目的地
     */
    public void onArriveDestination() {
        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "arway onArriveDestination");
        arriveDestinationView();
    }

    private void arriveDestinationView(){
        //显示gl 场景
        /*GlDrawRetainDistance.getInstance().changeDriveState(IDriveStateLister.DriveState.PAUSE);
        GlDrawSpeedDial.getInstance().changeDriveState(IDriveStateLister.DriveState.PAUSE);*/
        switchViewStatus(IDriveStateLister.DriveState.PAUSE);
        hideARWay();

        ARWayController.CommonBeanUpdater.setNaviEnd(true);
        ARWayController.CommonBeanUpdater.setNavingStart(false);
        updateNaviInfoDisplay();
        updateRetainDistanceDialDisplay();
        mRenderer.arriveDestination();
    }

    public void onNaviCalculateRouteFailure(int errorInfo) {

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
            arway.setVisibility(View.INVISIBLE);
        }

    }

    /**
     * show ARWay
     */
    public void showARWay() {
//        mRenderer.continue_();
        if (arway != null && !arway.isShown()) {//
            arway.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if(ARWayConst.ENABLE_LOG_OUT){
            HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG,"onCameraChange called");
        }
        mCameraChangeFinish = false;
    }

    @Override
    public void onCameraChangeFinish(CameraPosition arg0) {
        if(ARWayConst.ENABLE_LOG_OUT){
            HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG,"onCameraChangeFinish called");
        }
        mCameraChangeFinish=true;
        if(mNeedUpdatePath && mCameraChangeFinish) {
            LogI(ARWayConst.INDICATE_LOG_TAG," onCameraChangeFinish updatePath called");
//            mHandler.postDelayed(mUpdatePathRunable,3000);
//            mHandler.sendEmptyMessage(HANDLER_MSG_UPDATE_PATH);
            /*if (mAMapNavi != null) {
                LogI(ARWayConst.INDICATE_LOG_TAG," onCameraChangeFinish updatePath called");
                updatePath(mAMapNavi);

            }else {
                LogI(ARWayConst.ERROR_LOG_TAG,"onCameraChangeFinish updatePath 不成功!!!!!!!");
            }*/

        }
        initAMapNaviView();

    }

    public void removeAMapNaviView() {
        if (mAmapNaviView != null && mAmapNaviView.getParent() != null) {
            ViewGroup parent = (ViewGroup) mAmapNaviView.getParent();
            parent.removeView(mAmapNaviView);
        }
    }

    public void addAMapNaviView() {
        if (mAmapNaviView != null) {
            removeAMapNaviView();
//            mNaviView.addView(mAmapNaviView);
        }
    }

    /**
     * 是否起步超过一定距离
     * @return
     */
    private boolean isNavingReady() {
        NaviInfoBean naviBean = (NaviInfoBean)BeanFactory.getBean(BeanFactory.BeanType.NAVI_INFO);
        int totalDistance = naviBean.getPathTotalDistance();
        int distance = naviBean.getPathRetainDistance();
        return totalDistance>=distance && (totalDistance-distance)> ARWayConst.NAVI_CAR_START_DISTANCE;
    }

    void updateNaviInfoDisplay(){
        if (mGlDrawNaviInfo != null) {
            mGlDrawNaviInfo.doDraw();
        }
    }

    void updateRetainDistanceDialDisplay(){
        if (mGlDrawRetainDistance != null) {
            mGlDrawRetainDistance.doDraw();
        }else {
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG,"updateRetainDistanceDialDisplay ,mGlDrawRetainDistance is null !");
        }
    }

    void updateSpeedDialDisplay(){
        if (mGlDrawSpeedDial != null) {
            mGlDrawSpeedDial.doDraw();
        }else {
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG,"updateSpeedDialDisplay ,mGlDrawSpeedDial is null !");
        }
    }

    /***
     * 切换驾车起步和驾驶后view显示
     * @param state
     */
    public void switchViewStatus(IDriveStateLister.DriveState state){
        GlDrawRetainDistance.getInstance().changeDriveState(state);
        GlDrawCompass.getInstance().changeDriveState(state);
        GlDrawSpeedDial.getInstance().changeDriveState(state);

    }

    /**
     * 配合主工程是否显示表盘
     * @param show
     */
    public void showHideSpeedPanel(boolean show){
        if (mGlDrawSpeedDial != null) {
            mGlDrawSpeedDial.showHide(show);
        }
    }

    /**
     * 显示显示指南针
     * @param show
     */
    public void showHideCompass(boolean show){
        if (mGlDrawCompass != null) {
            mGlDrawCompass.showHide(show);
        }
    }

    /**
     * 是否显示里程表盘
     * @param show
     */
    public void showHideDistancePanel(boolean show){
        if (mGlDrawRetainDistance != null) {
            mGlDrawRetainDistance.showHide(show);
        }
    }



}
