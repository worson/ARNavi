package com.haloai.hud.hudendpoint.arwaylib.debug;

import com.amap.api.navi.model.NaviLatLng;

import java.util.List;

public interface INaviDataCollector {

	public abstract void startNavigation(List<NaviLatLng> naviPath);
	public abstract void endNavigation();
}
