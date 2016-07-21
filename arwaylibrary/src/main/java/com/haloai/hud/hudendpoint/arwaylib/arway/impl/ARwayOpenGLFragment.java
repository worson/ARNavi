package com.haloai.hud.hudendpoint.arwaylib.arway.impl;


import android.app.Fragment;
import android.content.Context;
import android.graphics.AvoidXfermode;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

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
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.RouteBean;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObjectFactory;
import com.haloai.hud.hudendpoint.arwaylib.draw.IDriveStateLister;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl.DrawScene;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl.GlDrawCompass;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl.GlDrawNaviInfo;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl.GlDrawRetainDistance;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl.GlDrawSpeedDial;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.hudendpoint.arwaylib.utils.DrawUtils;
import com.haloai.hud.hudendpoint.arwaylib.view.ComPassView;
import com.haloai.hud.hudendpoint.arwaylib.view.RetainDistanceView;
import com.haloai.hud.hudendpoint.arwaylib.view.SpeedView;
import com.haloai.hud.navigation.NavigationSDKAdapter;
import com.haloai.hud.utils.HaloLogger;
import com.haloai.hud.utils.ShareDrawables;

import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.ISurfaceRenderer;
import org.rajawali3d.view.IDisplay;
import org.rajawali3d.view.TextureView;

import java.util.ArrayList;
import java.util.List;


public class ARwayOpenGLFragment extends Fragment implements IDisplay ,OnMapLoadedListener, OnCameraChangeListener {
    // form HudAMapFragmentNavigation
    public final static boolean IS_DEBUG_MODE=false;

    private static final int GPS_STATUS_FINE = 0;
    private static final int GPS_STATUS_WEEK = 1;
    private static final int GPS_STATUS_BAD  = 2;

    private Context mContext;
    private SpeedView          mSpeedView          = null;
    private RetainDistanceView mRetainDistanceView = null;
    private ComPassView        mComPassView        = null;

    private ViewGroup  mNaviView     = null;
    private AMapNaviView mAmapNaviView = null;

    private AMapNavi mAMapNavi          = null;
    private boolean  mNeedUpdatePath    = false;
    private boolean  mMapLoaded         = false;
    private Bitmap   mCurrentCrossImage = null;
    private boolean  mCrossCanShow      = true;
    private int      mCurrentGpsStatus  = GPS_STATUS_FINE;

    private int mLastNaviIconType = 0;
    private Bitmap mNaviIconBitmap = null;
    private View arway;

    private List<Vector3> mPath = new ArrayList<>();

    //opengle
    protected ViewGroup           mLayout;
    protected RelativeLayout      mLeftLayout;
    protected RelativeLayout      mRightLayout;
    protected RelativeLayout      mMiddleLayout;
    protected TextureView         mRenderSurface;
    protected ARwayOpenGLRenderer mRenderer;


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
    private int     mTotalDistance  = 0;
    private int     mRetainDistance = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        // Inflate the layout for this fragment
        mLayout = (ViewGroup) inflater.inflate(R.layout.fragment_arway_open_gl, container, false);

        View mainARWayView = DrawObjectFactory.createGlDrawObjectLayoutIntance(mContext, container);
        mLayout.addView(mainARWayView);

        mRenderSurface = (TextureView) mDrawScene.getViewInstance(mContext);
        mRenderer = (ARwayOpenGLRenderer)createRenderer();
        onBeforeApplyRenderer();
        applyRenderer();

        //原来从latout中创建
        /*mLeftLayout = (RelativeLayout)mLayout.findViewById(R.id.left_card_viewgroup);
        mMiddleLayout = (RelativeLayout)mLayout.findViewById(R.id.middle_card_viewgroup);
        mRightLayout = (RelativeLayout)mLayout.findViewById(R.id.right_card_viewgroup);
        // Find the TextureView
//        mRenderSurface = (ISurface) mLayout.findViewById(R.id.rajwali_surface);
        mRenderSurface = new TextureView(getActivity());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mRenderSurface.setLayoutParams(params);
        // Create the renderer
        mMiddleLayout.addView(mRenderSurface);*/



//        createToolView();
        //amap view

        mNaviView = mLayout;
//        mNaviView = (ViewGroup) mDrawScene.getViewInstance(mContext).getParent();
        arway = mRenderSurface;
        //init amap navi view
        mAmapNaviView = (AMapNaviView) mLayout.findViewById(R.id.amap_navi_amapnaviview);
        mAmapNaviView.onCreate(savedInstanceState);
        mAmapNaviView.getMap().setOnMapLoadedListener(this);
        mAmapNaviView.getMap().setOnCameraChangeListener(this);
        if (IS_DEBUG_MODE) {
            mAmapNaviView.setVisibility(View.VISIBLE);
        } else {
            mAmapNaviView.setVisibility(View.INVISIBLE);
        }
        HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG,"fragment onCreateView");
        return mLayout;
    }


    private void createToolView(){
        mComPassView = new ComPassView(mContext, 254, 254);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(254, 254);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mComPassView.setLayoutParams(layoutParams);
        mComPassView.setDestDegree(180 - 90);
        mMiddleLayout.addView(mComPassView);

        mSpeedView = new SpeedView(mContext, 118, 118);
        layoutParams = new RelativeLayout.LayoutParams(118, 118);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mSpeedView.setLayoutParams(layoutParams);
        mLeftLayout.addView(mSpeedView);

        mRetainDistanceView = new RetainDistanceView(mContext, 118, 118);
        layoutParams = new RelativeLayout.LayoutParams(118, 118);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mRetainDistanceView.setLayoutParams(layoutParams);
        mRetainDistanceView.setTotalDistance(1000);
        mRightLayout.addView(mRetainDistanceView);


    }

    @Override
    public ISurfaceRenderer createRenderer() {
        return new ARwayOpenGLRenderer(getActivity(),this);
    }

    protected void onBeforeApplyRenderer() {
    }

    protected void applyRenderer() {
        mRenderSurface.setSurfaceRenderer(mRenderer);
    }




    @Override
    public void onResume() {
        super.onResume();
        mAmapNaviView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mAmapNaviView.onPause();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (mNaviView != null) {
            if (hidden) {
                mRenderer.pause();
                //mHudwayView.destroyDrawingCache();
                //mFl_hudway.removeView(mHudwayView);

                //clear navigating bitmap when this fragment is hidden
                //mIv_cross.setImageBitmap(null);
                //TODO temp method to draw arway
                //mHudwayView.setCrossImage(null);
                //mIv_icon_normal.setImageBitmap(null);
                //mIv_icon_big.setImageBitmap(null);
                //mIv_gps_status.setImageBitmap(null);
                //mTv_distance_big.setText("");
                //mTv_distance_normal.setText("");
                //mTv_gps_status.setText("");
                //mTv_next_road.setText("");
                //mTv_time_distance.setText("");
                mCurrentCrossImage = null;
                mCrossCanShow = true;
            } else {
                mRenderer.continue_();
                /*if(arway.getParent()!=mNaviView){
                    mNaviView.addView(arway);
                }*/
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
     * show cross image in image view
     *
     * @param crossimage Bitmap for show cross
     */
    public void showCrossImage(Bitmap crossimage) {
        if (mCurrentGpsStatus == GPS_STATUS_BAD) {
            ARWayController.NaviInfoBeanUpdate.setCrossBitmap(null);
            return;
        }
        if (crossimage != null) {
            mCurrentCrossImage = handleCrossImageBack(crossimage);
            if (mCrossCanShow) {
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
        if (mCurrentGpsStatus == GPS_STATUS_BAD) {
            //mTv_distance_normal.setText("");
            //mIv_icon_normal.setImageBitmap(null);
            return;
        }
        if (mCurrentGpsStatus == GPS_STATUS_FINE) {
            //mRl_navi_icon_distance_normal.setVisibility(View.VISIBLE);
            //mRl_navi_icon_distance_big.setVisibility(View.INVISIBLE);
        } else if (mCurrentGpsStatus == GPS_STATUS_WEEK) {
            //mRl_navi_icon_distance_normal.setVisibility(View.INVISIBLE);
            //mRl_navi_icon_distance_big.setVisibility(View.VISIBLE);
        }
        if (distance >= 1000) {
            //mTv_distance_normal.setText(distance / 1000 + "." + ((distance - distance / 1000 * 1000) / 100) + "km");
            //mTv_distance_big.setText(distance / 1000 + "." + ((distance - distance / 1000 * 1000) / 100) + "km");
        } else {
            //mTv_distance_normal.setText(distance + "m");
            //mTv_distance_big.setText(distance + "m");
        }
        switch (naviIcon) {
            case 15:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_dest);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_dest);
                break;
            case 13:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_service_area);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_service_area);
                break;
            case 14:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_tollgate);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_tollgate);
                break;
            case 16:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_tunnel);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_tunnel);
                break;
            case 10:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_passing_point);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_passing_point);
                break;
            case 24:
                // TODO 通过索道
                break;
            case 26:
                // TODO 通过通道、建筑物穿越通道
                break;
            case 17:
                // TODO 通过人行横道
                break;
            case 28:
                // TODO 通过游船路线
                break;
            case 1:
                // TODO 自车图标
                break;
            case 11:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_ring);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_ring);
                break;
            case 31:
                // TODO 通过阶梯
                break;
            case 2:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_left);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_left);
                break;
            case 6:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_left_back);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_left_back);
                break;
            case 4:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_left_front);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_left_front);
                break;
            case 8:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_back);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_back);
                break;
            case 23:
                // TODO 通过直梯
                break;
            case 0:
                // TODO 无定义
                break;
            case 12:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_ring_out);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_ring_out);
                break;
            case 18:
                // TODO 通过过街天桥
                break;
            case 21:
                // TODO 通过公园
                break;
            case 3:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_right);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_right);
                break;
            case 7:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_right_back);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_right_back);
                break;
            case 5:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_right_front);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_right_front);
                break;
            case 29:
                // TODO 通过观光车路线
                break;
            case 25:
                // TODO 通过空中通道
                break;
            case 30:
                // TODO 通过滑道
                break;
            case 20:
                // TODO 通过广场
                break;
            case 22:
                // TODO 通过扶梯
                break;
            case 9:
                //mIv_icon_normal.setImageResource(R.drawable.greenline_direction__icon_turn_front);
                //mIv_icon_big.setImageResource(R.drawable.greenline_direction__icon_turn_front);
                break;
            case 19:
                // TODO 通过地下通道
                break;
            case 27:
                // TODO 通过行人道路
                break;
        }
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
     *
     * @param aMapNavi
     */
    public void updatePath(AMapNavi aMapNavi) {
        this.mAMapNavi = aMapNavi;
        Projection projection = mAmapNaviView.getMap().getProjection();
        if (mMapLoaded && projection != null) {
            List<Vector3> path = getPathPoints(aMapNavi);
            if (path != null) {
                ARWayController.ARWayStatusUpdater.resetData();
                LogI(ARWayConst.INDICATE_LOG_TAG," updatePath called");
                ARWayController.SceneBeanUpdater.setPath(path)
                        .setAllLength(aMapNavi.getNaviPath().getAllLength());
                mRenderer.onDrawScene();

                //更新总距离
                int distance = aMapNavi.getNaviPath().getAllLength();
                mTotalDistance = distance;
                ARWayController.NaviInfoBeanUpdate.setPathTotalDistance(distance);
            }

            mNeedUpdatePath = false;
        }else {
            mNeedUpdatePath = true;
        }


    }

    public List<Vector3> getPathPoints(AMapNavi aMapNavi){
        AMapNaviPath aMapNaviPath = aMapNavi.getNaviPath();
        if (aMapNaviPath == null || aMapNaviPath.getCoordList().size() <= 0) {
            return null;
        }
        int size = aMapNaviPath.getCoordList().size();
        Projection projection = mAmapNaviView.getMap().getProjection();

        List<Vector3> path = new ArrayList<>(size);
        for (int i = 0; i < aMapNavi.getNaviPath().getCoordList().size(); i++) {
            PointF openGL = projection.toOpenGLLocation(DrawUtils.naviLatLng2LatLng(aMapNavi.getNaviPath().getCoordList().get(i)));
            path.add(new Vector3(openGL.x, openGL.y, 0));
        }
        if (!isPathRepeat(path)) {
            mPath.clear();
            mPath.addAll(path);
            return mPath;
        }
        return null;

    }

    private boolean isPathRepeat(List<Vector3> path) {
        if (path == null || path.size() <= 0 || mPath == null || mPath.size() <= 0) {
            return false;
        }
        for (int i = 0; i < (path.size() >= mPath.size() ? mPath.size() : path.size()); i++) {
            Vector3 vNew = path.get(i);
            Vector3 vOld = mPath.get(i);
            if (!vNew.equals(vOld)) {
                return false;
            }
        }
        return true;
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
                .setPathRetainTime(info.getPathRetainTime());
        //ARWayController.CompassBeanUpdater.setDirection(info.getDirection());
        mRenderer.onCameraChange();
        if (mGlDrawRetainDistance != null) {
            mGlDrawRetainDistance.doDraw();
        }
        if (mGlDrawNaviInfo != null) {
            mGlDrawNaviInfo.doDraw();
            if (mGlDrawRetainDistance != null) {
                mGlDrawRetainDistance.showHide(mGlDrawNaviInfo.getNaviStatusText() == null);
            }
        }
        //倾斜各个表盘
        this.mRetainDistance = info.getPathRetainDistance();
        boolean ready = isNavingReady();
        if(this.mLastIsReady != ready){
            if(ready){
                ARWayController.CommonBeanUpdater.setStartOk(true);
                switchViewStatus(IDriveStateLister.DriveState.DRIVING);
            }else {
                switchViewStatus(IDriveStateLister.DriveState.PAUSE);
            }
            this.mLastIsReady = ready;
        }

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
        viewOptions.setNaviViewTopic(AMapNaviViewOptions.BLUE_COLOR_TOPIC);
        viewOptions.setCrossDisplayShow(false);
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
        mAmapNaviView.getMap().showMapText(false);
        ARWayController.SceneBeanUpdater
                .setProjection(mAmapNaviView.getMap().getProjection());
        if(!mMapLoaded){
            mMapLoaded=true;
            LogI(ARWayConst.INDICATE_LOG_TAG,"地图加载成功");
        }
        if(mNeedUpdatePath) {
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
     * stop to draw hudway
     */
    public void resetDrawHudway() {
        mRenderer.reset();
    }
    /**
     * stop to draw hudway
     */
    public void stopDrawHudway() {
        arriveDestination();
    }

    public void onArriveDestination() {
        arriveDestination();
        switchViewStatus(IDriveStateLister.DriveState.PAUSE);
    }

    private void arriveDestination(){
        ARWayController.CommonBeanUpdater.setNaviEnd(true);
        updateNaviInfoDisplay();
        mRenderer.arriveDestination();
    }

    public void onNaviCalculateRouteFailure(int errorInfo) {

    }

    /**
     * startHomeAnimator to draw hudway
     */
    public void startDrawHudway() {
        mRenderer.start();
    }

    /**
     * hide ARWay
     */
    public void hideARWay() {
//        mRenderer.pause();
        arway.setVisibility(View.INVISIBLE);
       /* if (arway != null && arway.getParent() != null) {
            mNaviView.removeView(arway);
        }*/

    }

    /**
     * show ARWay
     */
    public void showARWay() {
        mRenderer.continue_();
        arway.setVisibility(View.VISIBLE);
        /*if (arway != null && arway.getParent() == null) {
            mNaviView.addView(arway);
        }*/
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition arg0) {
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
            mNaviView.addView(mAmapNaviView);
        }
    }

    /**
     * 是否起步超过一定距离
     * @return
     */
    private boolean isNavingReady() {
        int totalDistance = this.mTotalDistance;
        int distance = this.mRetainDistance;
        return totalDistance>=distance && (totalDistance-distance)> ARWayConst.NAVI_CAR_START_DISTANCE;
    }

    void updateNaviInfoDisplay(){
        if (mGlDrawNaviInfo != null) {
            mGlDrawNaviInfo.doDraw();
        }
    }

    void updateSpeedDialDisplay(){
        if (mGlDrawSpeedDial != null) {
            mGlDrawSpeedDial.doDraw();
        }
    }

    /***
     * 切换驾车起步和驾驶后view显示
     * @param state
     */
    private void switchViewStatus(IDriveStateLister.DriveState state){
        GlDrawCompass.getInstance().changeDriveState(state);
        GlDrawSpeedDial.getInstance().changeDriveState(state);
        GlDrawRetainDistance.getInstance().changeDriveState(state);

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
