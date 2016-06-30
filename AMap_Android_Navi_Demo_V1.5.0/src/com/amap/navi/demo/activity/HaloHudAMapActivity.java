package com.amap.navi.demo.activity;

import android.app.Activity;
import android.os.Bundle;

import com.amap.api.maps.AMap;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.NaviInfo;
import com.amap.navi.demo.R;
import com.autonavi.tbt.TrafficFacilityInfo;

public class HaloHudAMapActivity extends Activity implements AMapNaviListener, AMapNaviViewListener {

    private AMapNaviView naviView;
    private AMapNavi aMapNavi;
    private AMap aMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_halo_hud_amap);
        naviView = (AMapNaviView) findViewById(R.id.navi_view);
        naviView.onCreate(savedInstanceState);
        
        aMap = naviView.getMap();
        //为了尽最大可能避免内存泄露问题，建议这么写
        aMapNavi = AMapNavi.getInstance(getApplicationContext());
        aMapNavi.setAMapNaviListener(this);
//        aMapNavi.setAMapNaviListener(ttsManager);
//        aMapNavi.setEmulatorNaviSpeed(150);

	}

	@Override
	public void onLockMap(boolean arg0) {
		
	}

	@Override
	public boolean onNaviBackClick() {
		return false;
	}

	@Override
	public void onNaviCancel() {
		
	}

	@Override
	public void onNaviMapMode(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNaviSetting() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNaviTurnClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNextRoadClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScanViewButtonClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnUpdateTrafficFacility(TrafficFacilityInfo arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hideCross() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hideLaneInfo() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onArriveDestination() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onArrivedWayPoint(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCalculateRouteFailure(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCalculateRouteSuccess() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEndEmulatorNavi() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetNavigationText(int arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGpsOpenStatus(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInitNaviFailure() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onInitNaviSuccess() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLocationChange(AMapNaviLocation arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNaviInfoUpdate(NaviInfo arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNaviInfoUpdated(AMapNaviInfo arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReCalculateRouteForTrafficJam() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReCalculateRouteForYaw() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStartNavi(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTrafficStatusUpdate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showCross(AMapNaviCross arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showLaneInfo(AMapLaneInfo[] arg0, byte[] arg1, byte[] arg2) {
		// TODO Auto-generated method stub
		
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
