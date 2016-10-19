package com.haloai.hud.hudendpoint.arwaylib.test.debug;

import android.graphics.Point;

import com.thoughtworks.xstream.XStream;

import java.util.ArrayList;
import java.util.List;


public class CrossImageData {
	private String bitmapPath = "/sdcard/Halo/crossImage/";
	private String bitmapFileName = "cross.png";
	private CenterPoint centerPoint = new CenterPoint();
	private List<ScreenPoint> screenPoints = new ArrayList<>();
	private List<ScreenPoint> centerNextPoint = new ArrayList<>();
	private int stepIndex=0;
//	private ScreenPoint centerNextPoint = new ScreenPoint();

	{
//		screenPoints.add(new ScreenPoint(1,2));
	}

	public static class ListScreenPoint {
		public List<ScreenPoint> screenPoints = new ArrayList<>();
		public int               stepIndex    =0;

	}
	public static class ScreenPoint {
		public int screenX;
		public int screenY;

		public ScreenPoint() {

		}

		public ScreenPoint(int screenX, int screenY) {
			this.screenX = screenX;
			this.screenY = screenY;
		}

		public ScreenPoint(Point point) {
			this.screenX = point.x;
			this.screenY = point.y;
		}

	}

	public static class CenterPoint {
		public ScreenPoint screenPoint = new ScreenPoint(0,0);
		public int pointIndex = 0;

		public void setScreenPoint(ScreenPoint screenPoint) {
			this.screenPoint = screenPoint;
		}

		public void setPointIndex(int pointIndex) {
			this.pointIndex = pointIndex;
		}
	}

	public CrossImageData(String bitmapPath, String bitmapFileName, Point centerPoint,int centerPointIndex, List<Point> screenPoints, Point centerNextPoint,int stepIndex) {
		this.bitmapPath = bitmapPath;
		this.bitmapFileName = bitmapFileName;
		this.centerPoint.setScreenPoint(new ScreenPoint(centerPoint));
		this.centerPoint.setPointIndex(centerPointIndex);

		this.screenPoints = new ArrayList<>();
		if (screenPoints != null) {
			for (Point point:screenPoints){
				this.screenPoints.add(new ScreenPoint(point));
			}
		}
		this.centerNextPoint.clear();
		this.centerNextPoint.add(new ScreenPoint(centerNextPoint));
		this.stepIndex = stepIndex;
	}

	public static void formatXStream(XStream xStream){
		xStream.alias("crossImage", CrossImageData.class);
		xStream.alias("centerPoint", CenterPoint.class);
		xStream.alias("screenPoint", ScreenPoint.class);
		xStream.alias("listScreenPoint", ListScreenPoint.class);
	}


}
