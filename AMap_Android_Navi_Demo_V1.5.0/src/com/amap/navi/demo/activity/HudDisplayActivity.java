package com.amap.navi.demo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

import com.amap.api.navi.AMapHudView;
import com.amap.api.navi.AMapHudViewListener;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.navi.demo.R;
import com.amap.navi.demo.util.TTSController;
import com.autonavi.tbt.TrafficFacilityInfo;

import java.util.ArrayList;

/**
 * 创建时间：11/11/15 15:48
 * 项目名称：newNaviDemo
 *
 * @author lingxiang.wang
 * @email lingxiang.wang@alibaba-inc.com
 * 类说明：
 */

public class HudDisplayActivity extends Activity implements AMapHudViewListener, AMapNaviListener {


    private AMapHudView mAmapHudView;
    private TTSController ttsManager;
    private AMapNavi aMapNavi;


    //起点终点
    private NaviLatLng mNaviStart = new NaviLatLng(39.989614, 116.481763);
    private NaviLatLng mNaviEnd = new NaviLatLng(39.983456, 116.3154950);
    //起点终点列表
    private ArrayList<NaviLatLng> mStartPoints = new ArrayList<NaviLatLng>();
    private ArrayList<NaviLatLng> mEndPoints = new ArrayList<NaviLatLng>();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ttsManager = TTSController.getInstance(this);
        ttsManager.init();
        ttsManager.startSpeaking();

        aMapNavi = AMapNavi.getInstance(this);
        aMapNavi.setAMapNaviListener(ttsManager);
        aMapNavi.setAMapNaviListener(this);
        aMapNavi.setEmulatorNaviSpeed(150);


        setContentView(R.layout.activity_hud);
        mAmapHudView = (AMapHudView) findViewById(R.id.hudview);
        mAmapHudView.setHudViewListener(this);
    }

    //-----------------HUD返回键按钮事件-----------------------
    @Override
    public void onHudViewCancel() {
        stopNavi();
        finish();
    }

    private void stopNavi() {
        aMapNavi.stopNavi();
        ttsManager.stopSpeaking();
    }

    protected void onResume() {
        super.onResume();
        mAmapHudView.onResume();
        mStartPoints.add(mNaviStart);
        mEndPoints.add(mNaviEnd);
        aMapNavi.calculateDriveRoute(mStartPoints, mEndPoints, null, 5);
    }

    /**
     * 返回键监听
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            stopNavi();
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPause() {
        super.onPause();
        mAmapHudView.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAmapHudView.onDestroy();
        aMapNavi.destroy();
        ttsManager.destroy();
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
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {
    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onCalculateRouteSuccess() {
        AMapNavi.getInstance(this).startNavi(AMapNavi.EmulatorNaviMode);
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
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

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
	public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCalculateMultipleRoutesSuccess(int[] arg0) {
		// TODO Auto-generated method stub
		
	}

}
