package com.amap.navi.demo.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.model.AMapNaviLink;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.NaviLatLng;

/**
 * surfaceView用于绘制Hudway
 * @author 龙
 */
public class HudwayView2 extends SurfaceView implements SurfaceHolder.Callback{
	private final static long FPS_TIME = 30;
	private HudwayFlushThread mHudwayFlushThread;
	private Context mContext;
	
	// private List<Point> mScreenPoints = new ArrayList<Point>();
	private List<NaviLatLng> mPathLatLngs = new ArrayList<NaviLatLng>();
	private List<Integer> mCroodsInSteps = new ArrayList<Integer>();
	private List<NaviLatLng> mTrafficLightNaviLatLngs = new ArrayList<NaviLatLng>();
	private List<Float> mPointsDistance = new ArrayList<Float>();
	private List<Boolean> mIsRedLine = new ArrayList<Boolean>();

	// 计算当前点到集合中的currentIndex间的距离
	private static int HUDWAY_LENGTH_IN_SCREEN = 500;
	private static int HUDWAY_POINT_INTERVAL = 30;
	
	private int mCurrentIndex = 1;
//	private int mPrePreCurrentIndex =1;
//	private int mPreCurrentIndex =1;
	
	private Projection mProjection;
	private boolean mIsLastPoint = false;
	private float mCurPoint2FirstPointDist;
	private HUDWayBitmapFactory2 mHudwayFactory;
	private AMapNaviLocation mCurrentLocation;
	private AMapNaviLocation mPrePreLocation;
	private AMapNaviLocation mPreLocation;
	private long mPrePreTime;
	private long mPreTime;
	private long mCurrentTime;
	private int mCurrent;
	private long mCount;
	
	/**
	 * 构造函数
	 * @param context
	 */
	public HudwayView2(Context context ,Projection projection) {
		super(context);
		
		this.mContext = context;
		SurfaceHolder holder = this.getHolder();
		this.setZOrderOnTop(true);
		holder.setFormat(PixelFormat.TRANSPARENT);
		holder.addCallback(this);
		setFocusableInTouchMode(true);
		
		mProjection = projection;
		mHudwayFactory = new HUDWayBitmapFactory2(context,projection);
		
	}
	public HudwayView2(Context context, AttributeSet attrs, int defStyle) {
		super(context);
	}
	public HudwayView2(Context context, AttributeSet attrs) {
		super(context);
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		mHudwayFactory.initDrawLine(width, height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) { 
		mHudwayFlushThread = new HudwayFlushThread(holder);
		mHudwayFlushThread.setRunning(true);
		mHudwayFlushThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mHudwayFlushThread.setRunning(false);
	}
	
	//draw thread
	class HudwayFlushThread extends Thread {
		private boolean mRunning = false;
		private SurfaceHolder surfaceHolder;
		private long startTime = 0;
		private long endTime = 0;
		
        public HudwayFlushThread(SurfaceHolder surfaceHolder) {
        	this.surfaceHolder = surfaceHolder;
        }
        
        //start and stop thread
        public void setRunning(boolean running) {
        	mRunning = running;
        }
        
		@Override
		public void run() {
			while (mRunning) {
				Canvas can = null;
				can = surfaceHolder.lockCanvas(null);
				synchronized (surfaceHolder) {
					if (mRunning && mCurrentLocation!=null){
						startTime=System.currentTimeMillis();
						doDraw(can);
//						if(mDrawWay){
//						}else{
//							can.drawColor(Color.BLACK);
//						}
						endTime=System.currentTimeMillis();
					}
				}
				//this 5 is for unknown delay time , and sleep time is (30ms - delay time for other code)
				if(endTime - startTime + 10 < FPS_TIME){
					SystemClock.sleep(FPS_TIME - (endTime - startTime) - 10);
				}
				if (can != null) {
					surfaceHolder.unlockCanvasAndPost(can);
				}
			}
		}
		
		/**
		 * 用于调用HudwayFactory的方法传入canvas和points进行绘制
		 * @param canvas
		 */
		private void doDraw(Canvas canvas) {
			if(canvas == null){
				return ;
			}
			canvas.save();
			//TODO create a faker location for draw hudway
			AMapNaviLocation fakerLocation = getFakerLocation();
			if(fakerLocation==null){
				Log.e("helong fix","come in draw hudway___null");
			}else{
				Log.e("helong fix","come in draw hudway___not null");
			}
//			mHudwayFactory.drawHudway(canvas, fakerLocation, mPreLocation/*, mPrePreCurrentIndex*/);
			mHudwayFactory.drawHudway(canvas, mCurrentLocation);
			canvas.restore();
		}
	}
	
	/**
	 * return location for draw 
	 * @return
	 */
	private AMapNaviLocation getFakerLocation() {
		//if have not yet to return location , return null
		if(mCurrentLocation == null){
			return null;
		}
		//if prePreLocation is null , set the value to it , and return null
		if(mPrePreLocation == null){
			mPrePreLocation = mCurrentLocation;
			mPrePreTime = mCurrentTime;
//			mPrePreCurrentIndex = mCurrentIndex;
			return null;
		}
		//if mPreLocation is null , so this is the first step to draw
		if(mPreLocation == null){
			mPreLocation = mCurrentLocation;
			mPreTime = mCurrentTime;
//			mPreCurrentIndex = mCurrentIndex;
			mCurrent = 0;
		}else if(mPreLocation != mCurrentLocation){
			mPrePreLocation = mPreLocation;
			mPrePreTime = mPreTime;
			mPreLocation = mCurrentLocation;
			mPreTime = mCurrentTime;
//			mPrePreCurrentIndex = mPreCurrentIndex;
//			mPreCurrentIndex = mCurrentIndex;
			mCurrent = 0;
		}
//		if(mCurrentIndex != mPreCurrentIndex){
//			mPrePreCurrentIndex = mPreCurrentIndex;
//			mPreCurrentIndex = mCurrentIndex;			
//		}
		if(mCurrent == 0){
			//calculate distance with prepre and pre
			long time = mPreTime - mPrePreTime;
			mCount = (long) (time / FPS_TIME);
		}
		if(mCurrent <= mCount){
			Point prePrePoint = mProjection.toScreenLocation(
					naviLatLng2LatLng(mPrePreLocation.getCoord()));
			Point prePoint = mProjection.toScreenLocation(
					naviLatLng2LatLng(mPreLocation.getCoord()));
			Point point = new Point((int)(prePrePoint.x + (prePoint.x - prePrePoint.x)*(1.0*mCurrent/mCount)), 
					(int)(prePrePoint.y + (prePoint.y - prePrePoint.y)*(1.0*mCurrent/mCount)));
//			Point startPoint = mProjection.toScreenLocation(naviLatLng2LatLng(mPathLatLngs.get(mCurrentIndex - 1)));
//			Point endPoint = mProjection.toScreenLocation(naviLatLng2LatLng(mPathLatLngs.get(mCurrentIndex)));
//			if(!startPoint.equals(endPoint)){
//				Log.e("projection", "prePoint"+point+"");
//				Log.e("projection", "startPoint"+startPoint+"");
//				Log.e("projection", "endPoint"+endPoint+"");
//				point = PointsLines.projection(startPoint,endPoint,point);
//				Log.e("projection", "proPoint"+point+"");
//				Log.e("projection", "==============================");
//				if(!PointsLines.isPointInLine(startPoint, endPoint, point) && mCurrentIndex + 1 < mPathLatLngs.size()){
//					NaviLatLng startLatLng = mPathLatLngs.get(mCurrentIndex-1+1);
//					NaviLatLng endLatLng = mPathLatLngs.get(mCurrentIndex+1);
//					startPoint = mProjection.toScreenLocation(new LatLng(startLatLng.getLatitude(),startLatLng.getLongitude()));
//					endPoint = mProjection.toScreenLocation(new LatLng(endLatLng.getLatitude(),endLatLng.getLongitude()));
//					point = PointsLines.projection(startPoint, endPoint, point);
//					Log.e("currentIndex", "currentIndex + 1:"+(mCurrentIndex +1));
//				}
//			}
			AMapNaviLocation location = new AMapNaviLocation();
			location.setCoord(latLng2NaviLatLng(mProjection.fromScreenLocation(point)));
			mCurrent++;
			return location;
		}
		return null;
	}
	
	/**
	 * NaviLatLng to LatLng
	 * @param naviLatLng
	 * @return latLng
	 */
	private LatLng naviLatLng2LatLng(NaviLatLng naviLatLng) {
		return naviLatLng == null ? null : new LatLng(naviLatLng.getLatitude(),
				naviLatLng.getLongitude());
	}
	
	/**
	 * LatLng to NaviLatLng
	 * @param latLng
	 * @return naviLatLng
	 */
	private NaviLatLng latLng2NaviLatLng(LatLng latLng) {
		return latLng == null ? null : new NaviLatLng(latLng.latitude,
				latLng.longitude);
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////
	//=========TODO 原HUDWayBitmapFactory2的方法=================
	public void setRouteLatLngAndScreenPoints(List<NaviLatLng> pathLatLngs,
			List<Point> screenPoints, List<AMapNaviStep> naviStepList) {
		init();
		for (int i = 0; i < screenPoints.size(); i++) {
			// delete repeat item 
			if (i == 0 || !screenPoints.get(i - 1).equals(screenPoints.get(i))) {
				mPathLatLngs.add(pathLatLngs.get(i));
			}
		}
		Log.i("pathsize", "pathLatLng.size = "+pathLatLngs.size());
		
		for(int i=0;i<mPathLatLngs.size()-1;i++){
			mPointsDistance.add(AMapUtils.calculateLineDistance(naviLatLng2LatLng(mPathLatLngs.get(i)),naviLatLng2LatLng(mPathLatLngs.get(i+1))));
		}
		
		for (int i = 0; i < naviStepList.size(); i++) {
			mCroodsInSteps.add(naviStepList.get(i).getCoords().size());
		}
		
		//TODO call hudwayfactory`s this method
		mHudwayFactory.setRouteLatLngAndScreenPoints(naviStepList);
	}
	
	public void setCurrentIndex(int curPoint, int curStep) {
		if(mDrawWay){
			mCurrentIndex = 0;
			for (int i = 0; i < curStep; i++) {
				mCurrentIndex += mCroodsInSteps.get(i);
			}
			mCurrentIndex += curPoint +1;
			if (mCurrentIndex >= mPathLatLngs.size()) {
				mCurrentIndex = mPathLatLngs.size() - 1;
			}
			Log.i("CurrentIndex", mCurrentIndex + "");
			
//			//TODO call hudwayfactory`s this method
			mHudwayFactory.setCurrentIndex(curPoint, curStep);
		}
	}
	
	private void init() {
		this.mPathLatLngs.clear();
		this.mCroodsInSteps.clear();
		this.mTrafficLightNaviLatLngs.clear();
		this.mPointsDistance.clear();
		this.mIsRedLine.clear();
		this.mCurrentIndex = 1;
//		this.mPrePreCurrentIndex =1;
//		this.mPreCurrentIndex =1;
		this.mIsLastPoint = false;
	}
	
	public void setCurrentLocation(AMapNaviLocation location) {
//		Point point = mProjection.toScreenLocation(naviLatLng2LatLng(location.getCoord()));
//		Point startPoint = mProjection.toScreenLocation(naviLatLng2LatLng(mPathLatLngs.get(mCurrentIndex - 1)));
//		Point endPoint = mProjection.toScreenLocation(naviLatLng2LatLng(mPathLatLngs.get(mCurrentIndex)));
//		if(!startPoint.equals(endPoint)){
//			point = PointsLines.projection(startPoint, endPoint, point);
//		}
//		LatLng latLng = mProjection.fromScreenLocation(point);
//		mCurrentLocation = new AMapNaviLocation();
//		mCurrentLocation.setCoord(latLng2NaviLatLng(latLng));
		if(mDrawWay){
			mCurrentLocation = location;
			mDrawWay = false;
		}
		mCurrentTime = System.currentTimeMillis();
	}
	
	boolean mDrawWay = false;
	public void drawWay(){
		mDrawWay = true;
	}
	
	public void notDrawWay(){
		mDrawWay = false;
	}
	
}
