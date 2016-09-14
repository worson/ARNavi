package com.haloai.hud.hudendpoint.arwaylib.debug;

import android.graphics.Point;

import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.NaviLatLng;
import com.thoughtworks.xstream.XStream;

import java.util.ArrayList;
import java.util.List;

public class NavigationPathData {
	private String startName = null;
	private String endName = null;
	private MyLatLng startLocation;
	private MyLatLng endLocation;

	private List<CrossImageData.ListScreenPoint> pathScreenPoints = new ArrayList<>();
	private List<MyLatLng>                       naviPath         = new ArrayList<MyLatLng>();
	private List<PathStep>                       pathSteps        = new ArrayList<>();


	private class PathStep {
		public List<MyLatLng> stepLatLngs = new ArrayList<>();
		public int            stepIndex   =0;
	}

	public static class MyLatLng {
		public double lat;
		public double lng;

		public MyLatLng() {
		}

		public MyLatLng(NaviLatLng naviLatLng) {
			this.lat = naviLatLng.getLatitude();
			this.lng = naviLatLng.getLongitude();
		}

		@Override
		public String toString() {
			return String.format("lat=%s,lng=%s",lat,lng);
		}
	}
	
	public NavigationPathData(XStream xStream) {
		xStream.alias("NavigationPathData", NavigationPathData.class);
		xStream.alias("LatLng", MyLatLng.class);
		xStream.alias("pathStep", PathStep.class);
	}

	public List<MyLatLng> getNaviPath() {
		return naviPath;
	}

	public void setNaviPath(List<NaviLatLng> naviPath) {
		this.naviPath.clear();
		int cnt = 1;//naviPath.size()
		for (int i=0; i<naviPath.size(); i++) {
			MyLatLng myLatLng = new MyLatLng();
			myLatLng.lat = naviPath.get(i).getLatitude();
			myLatLng.lng = naviPath.get(i).getLongitude();
			this.naviPath.add(myLatLng);
		}

	}

	public void setStepNaviPath(List<AMapNaviStep> steps) {
		this.pathSteps.clear();
		int stepCnt = 0;
		for (AMapNaviStep step:steps) {
			PathStep pathStep = new PathStep();
			for (NaviLatLng naviLatLng: step.getCoords()) {
				pathStep.stepLatLngs.add(new MyLatLng(naviLatLng));
			}
			pathStep.stepIndex = stepCnt;
			this.pathSteps.add(pathStep);
			stepCnt++;
		}
	}

	public void setPathScreenPoints(List<List<Point>> stepsPoints) {
		this.pathScreenPoints.clear();
		int stepCnt = 0;
		for (List<Point> step:stepsPoints) {
			CrossImageData.ListScreenPoint listScreenPoint =  new CrossImageData.ListScreenPoint();
			for (Point p: step) {
				listScreenPoint.screenPoints.add(new CrossImageData.ScreenPoint(p.x,p.y));
			}
			listScreenPoint.stepIndex = stepCnt;
			this.pathScreenPoints.add(listScreenPoint);
			stepCnt++;
		}
	}

	public void setStartName(String startName) {
		this.startName = startName;
	}

	public void setEndName(String endName) {
		this.endName = endName;
	}

	public void setStartLocation(NaviLatLng naviLatLng) {
		this.startLocation = new MyLatLng(naviLatLng);
	}

	public void setEndLocation(NaviLatLng naviLatLng) {
		this.endLocation = new MyLatLng(naviLatLng);
	}
}
