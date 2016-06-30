package com.amap.navi.demo.activity;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.NaviInfo;
import com.amap.navi.demo.R;
import com.amap.navi.demo.util.TTSController;
import com.autonavi.tbt.TrafficFacilityInfo;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 创建时间：11/11/15 16:31
 * 项目名称：newNaviDemo
 *
 * @author lingxiang.wang
 * @email lingxiang.wang@alibaba-inc.com
 * 类说明：
 * 无须事先规划路线的智能播报
 */

public class IntelligentBroadcastActivity extends Activity implements AMapNaviListener {


    private MapView mapView;
    private AMap aMap;
    private Marker myLocationMarker;

    // 是否需要跟随定位
    private boolean isNeedFollow = true;

    // 处理静止后跟随的timer
    private Timer needFollowTimer;

    // 屏幕静止DELAY_TIME之后，再次跟随
    private long DELAY_TIME = 5000;
    private AMapNavi aMapNavi;
    private TTSController ttsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_intelligent_broadcast);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写

        init();
    }

    /**
     * 初始化各种对象
     */
    private void init() {
        if (aMap == null) {
            aMapNavi = AMapNavi.getInstance(this);
            aMapNavi.setDetectedMode(AMapNavi.CameraAndSpecialRoadDetectedMode);

            ttsManager = TTSController.getInstance(this);
            ttsManager.init();

            aMapNavi.setAMapNaviListener(this);
            aMapNavi.setAMapNaviListener(ttsManager);

            aMap = mapView.getMap();
            // 初始化 显示我的位置的Marker
            myLocationMarker = aMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(getResources(), R.drawable.car))));

            setMapInteractiveListener();
        }

    }

    /**
     * 设置导航监听
     */
    private void setMapInteractiveListener() {

        aMap.setOnMapTouchListener(new AMap.OnMapTouchListener() {

            @Override
            public void onTouch(MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 按下屏幕
                        // 如果timer在执行，关掉它
                        clearTimer();
                        // 改变跟随状态
                        isNeedFollow = false;
                        break;

                    case MotionEvent.ACTION_UP:
                        // 离开屏幕
                        startTimerSomeTimeLater();
                        break;

                    default:
                        break;
                }
            }
        });

    }

    /**
     * 取消timer任务
     */
    private void clearTimer() {
        if (needFollowTimer != null) {
            needFollowTimer.cancel();
            needFollowTimer = null;
        }
    }

    /**
     * 如果地图在静止的情况下
     */
    private void startTimerSomeTimeLater() {
        // 首先关闭上一个timer
        clearTimer();
        needFollowTimer = new Timer();
        // 开启一个延时任务，改变跟随状态
        needFollowTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                isNeedFollow = true;
            }
        }, DELAY_TIME);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        ttsManager.destroy();
        aMapNavi.destroy();
    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onInitNaviSuccess() {

    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation location) {
        if (location != null) {
            LatLng latLng = new LatLng(location.getCoord().getLatitude(),
                    location.getCoord().getLongitude());
            // 显示定位小图标，初始化时已经创建过了，这里修改位置即可
            myLocationMarker.setPosition(latLng);
            if (isNeedFollow) {
                // 跟随
                aMap.animateCamera(CameraUpdateFactory.changeLatLng(latLng));
            }
        } else {
            Toast.makeText(IntelligentBroadcastActivity.this, "定位出现异常",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGetNavigationText(int i, String s) {
        ttsManager.playText(s);
    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onCalculateRouteSuccess() {

    }

    @Override
    public void onCalculateRouteFailure(int i) {

    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {
    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviinfo) {
    }

	@Override
	public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCalculateMultipleRoutesSuccess(int[] arg0) {
		// TODO Auto-generated method stub
		
	}
}
