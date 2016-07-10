package com.amap.navi.demo.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
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
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.NaviLatLng;

/**
 * 该SurfaceView用于持续绘制Hudway在子线程中
 * 帧率为每秒33帧
 * 同时根据当前位置以及车速生成数据进行绘制解决不平滑问题
 * @author 龙
 *
 * TODO helong 计算点的生成方式：
 * 	1.首先肯定得有一个location作为整个绘制的起点
 *	2.其次根据当前location在屏幕上的位置，以及车速得到下一个30ms要绘制的图形的起点
 *		这个点的坐标是处于起点和每个相邻形状点之间，根据 speed * 30ms 算的距离，根据距离和
 *		形状点的间距按照比例得到计算点的坐标
 *	3.依此类推
 *	4.当location不再是原来的location时，也就是有新的回调时，用这个新回调更新用于绘制的起点
 *		1.判断新回调的点位于绘制的起点的前方还是后方
 *			1.前方：在基础上减慢speed，等车的真实点赶上来
 *			2.后方：在基础上加大speed，赶上车的真实点坐标
 *	5.更新速度
 */
public class HudwayView1 extends SurfaceView implements SurfaceHolder.Callback{
	private final static long FPS_TIME = 30;
	private HudwayFlushThread mHudwayFlushThread;
	
	//navigation class
	private AMapNaviPath mCurrentPath;
	private boolean mCanCreateHUDWay;
	private List<NaviLatLng> mPathLatLngs = new ArrayList<NaviLatLng>();
	private List<Integer> mCroodsInSteps = new ArrayList<Integer>();
	private List<Float> mPointsDistance = new ArrayList<Float>();
	private List<Boolean> mIsRedLine = new ArrayList<Boolean>();
	
	//current info
	private AMapNaviLocation mCurrentLocation;
	private float mCurrentSpeed;
	private int mCurrentIndex = 1;
	private float mCurPoint2FirstPointDist;
	private AMapNaviLocation mPreLocation;
	
	private static final float RED_LINE_LENGTH = 30;
	private static final int HUDWAY_LENGTH_IN_SCREEN = 500;
	
	private NaviLatLng mCurrentLatLng;
	private HUDWayBitmapFactory mHudwayFactory;
	private Context mContext;
	private LocationManager mLocationManager;
	private AMapNaviLocation mPrePreLocation;
	private int mCurrent;
	private Projection mProjection;
	private long mPrePreTime;
	private long mPreTime;
	private long mCurrentTime;
	private long mCount;
	
	/**
	 * 构造函数
	 * @param context
	 */
	public HudwayView1(Context context ,Projection projection) {
		super(context);
		
		this.mContext = context;
		mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		SurfaceHolder holder = this.getHolder();
		this.setZOrderOnTop(true);
		holder.setFormat(PixelFormat.TRANSPARENT);
		holder.addCallback(this);
		setFocusableInTouchMode(true);
		
		this.mHudwayFactory = new HUDWayBitmapFactory(projection);
	}
	public HudwayView1(Context context, AttributeSet attrs, int defStyle) {
		super(context);
	}
	public HudwayView1(Context context, AttributeSet attrs) {
		super(context);
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		
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
		private List<Point> mPoints4Hudway;
		
        public HudwayFlushThread(SurfaceHolder surfaceHolder) {
        	this.surfaceHolder = surfaceHolder;
        	mPoints4Hudway = new ArrayList<Point>();
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
					if (mRunning && mCurrentLocation!=null && mProjection!=null)
						doDraw(can);
				}
				SystemClock.sleep(FPS_TIME);
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
			getPoints4Hudway(mPoints4Hudway);
			canvas.save();
			mHudwayFactory.drawHudway(canvas, mPoints4Hudway, mIsRedLine);
			canvas.restore();
		}
	}
	
	//===========================get points for hudway===============
	/**
	 * 根据当前点，以及计算方法得到用于绘制的点的集合
	 * @param points
	 */
	private void getPoints4Hudway(List<Point> points) {
		float totalLength = 0;
//		mCurrentLatLng = getCurrentLatLng();
//		mCurrentLatLng = getCurrentLatLng2();
		mCurrentLatLng = getCurrentLatLng3();
		if(mCurrentLatLng==null){
			return;
		}
		mIsRedLine.clear();
		points.clear();
		Point currentScreenPoint = mProjection
				.toScreenLocation(naviLatLng2LatLng(mCurrentLatLng));
		points.add(currentScreenPoint);
		for (int i = mCurrentIndex; i < mPathLatLngs.size(); i++) {
			Point pathPoint = mProjection
					.toScreenLocation(naviLatLng2LatLng(mPathLatLngs.get(i)));
			float distance = 0;
			if (i == mCurrentIndex) {
				if (currentScreenPoint.equals(pathPoint)) {
					continue;
				}
				this.mCurPoint2FirstPointDist = AMapUtils
						.calculateLineDistance(
								naviLatLng2LatLng(mCurrentLatLng),
								naviLatLng2LatLng(mPathLatLngs.get(i)));
				distance = mCurPoint2FirstPointDist;
				totalLength += mCurPoint2FirstPointDist;
			} else {
				if (pathPoint.equals(mProjection
						.toScreenLocation(
								naviLatLng2LatLng(mPathLatLngs.get(i - 1))))) {
					continue;
				}
				distance = AMapUtils.calculateLineDistance(
						naviLatLng2LatLng(mPathLatLngs.get(i - 1)),
						naviLatLng2LatLng(mPathLatLngs.get(i)));
				totalLength += distance;
			}
			mIsRedLine.add(AMapUtils.calculateLineDistance(
					naviLatLng2LatLng(mPathLatLngs.get(i - 1)),
					naviLatLng2LatLng(mPathLatLngs.get(i))) <= RED_LINE_LENGTH);
			//be sure the total distance is HUDWAY_LENGTH_IN_SCREEN
			if(totalLength == HUDWAY_LENGTH_IN_SCREEN){
				points.add(pathPoint);
				return;
			}else if(totalLength > HUDWAY_LENGTH_IN_SCREEN){
				float div = totalLength - HUDWAY_LENGTH_IN_SCREEN;
				Point prePoint = null;
				if(i == mCurrentIndex){
					prePoint = mProjection
							.toScreenLocation(naviLatLng2LatLng(mCurrentLatLng));
				}else{
					prePoint = mProjection
							.toScreenLocation(naviLatLng2LatLng(mPathLatLngs.get(i - 1)));
				}
				Point makePoint = new Point(
						(int)(prePoint.x + (pathPoint.x - prePoint.x)*((distance-div)/distance)),
						(int)(prePoint.y + (pathPoint.y - prePoint.y)*((distance-div)/distance)));
				points.add(makePoint);
				return;
			}else{
				points.add(pathPoint);
			}
		}
		return;
	}
	
	/**
	 * 计算得到当前绘制的路径的起点
	 * 通过主动请求gps位置
	 * getLastKnownLocation获取的数据如果同样是依赖于onLocationChange，那么该方式
	 * 获取到的数据是没意义的
	 * @return
	 */
	private NaviLatLng getCurrentLatLng2() {
		Criteria criteria = new Criteria();
		criteria.setCostAllowed(false); //设置位置服务免费
		criteria.setAccuracy(Criteria.ACCURACY_COARSE); //设置水平位置精度
		final String  providerName =mLocationManager.getBestProvider(criteria, true);
		if(providerName!=null && !providerName.isEmpty()){
			Location location = mLocationManager.getLastKnownLocation(providerName);
			return location==null?null : new NaviLatLng(location.getLatitude(), location.getLongitude());
		}else{
			return null;
		}
	}
	
	/**
	 * 计算得到当前绘制的路径的起点
	 * 得到一个location作为prepre点
	 * 得到下一个location作为pre点
	 * 以prepre为起点开始绘制，速度使用prepre获得的速度
	 * 得到location作为reference点进行修正
	 * 
	 * test 
	 * 一个location为prepre
	 * 一个location为pre
	 * 得到pre时开始绘制prepre--pre之间的路径
	 * 绘制到pre就停止
	 * 等到新的location到来作为新的pre，原pre作为prepre进行绘制
	 * @return
	 */
	private NaviLatLng getCurrentLatLng3(){
		if(1==1){
			return mCurrentLocation == null?null:mCurrentLocation.getCoord();
		}
		//if have not yet to return location , return null
		if(mCurrentLocation == null){
			return null;
		}
		//if prePreLocation is null , set the value to it , and return null
		if(mPrePreLocation == null){
			mPrePreLocation = mCurrentLocation;
			mPrePreTime = mCurrentTime;
			return null;
		}
		//if mPreLocation is null , 
		if(mPreLocation == null){
			mPreLocation = mCurrentLocation;
			mPreTime = mCurrentTime;
			mCurrent = 0;
		}else if(mPreLocation != mCurrentLocation){
			mPrePreLocation = mPreLocation;
			mPrePreTime = mPreTime;
			mPreLocation = mCurrentLocation;
			mPreTime = mCurrentTime;
			mCurrent = 0;
		}
		if(mCurrent == 0){
			//calculate distance with prepre and pre
			long time = mPreTime - mPrePreTime;
			mCount = time / FPS_TIME;
		}
		if(mCurrent < mCount){
			Point prePrePoint = mProjection.toScreenLocation(
					naviLatLng2LatLng(mPrePreLocation.getCoord()));
			Point prePoint = mProjection.toScreenLocation(
					naviLatLng2LatLng(mPreLocation.getCoord()));
			Point point = new Point((int)(prePrePoint.x + (prePoint.x - prePrePoint.x)*(1.0*mCurrent/mCount)), 
					(int)(prePrePoint.y + (prePoint.y - prePrePoint.y)*(1.0*mCurrent/mCount)));
			mCurrent++;
			Log.e("mCurrent-mCount", mCurrent+":"+mCount);
			return latLng2NaviLatLng(mProjection.fromScreenLocation(point));
		}
		return null;
	}

	/**
	 * 计算得到当前绘制的路径的起点
	 * 通过上一个绘制点和下一个形状点，以及速度来计算得到的新起点
	 * 速度本身不准确，而且有延后性，相当于用旧的速度绘制新的路线
	 * @return
	 */
	private NaviLatLng getCurrentLatLng() {
		if(1==1){
			return mCurrentLocation.getCoord();
		}
		if(mPreLocation == null){
			//表示这是第一个点
			mPreLocation = mCurrentLocation;
			return mCurrentLocation.getCoord();
		}else{
			Point currentPoint = mProjection.
					toScreenLocation(naviLatLng2LatLng(mCurrentLatLng));
			Point nextPoint = mProjection.
					toScreenLocation(naviLatLng2LatLng(mPathLatLngs.get(mCurrentIndex)));
			float factor=1;
			if(!mCurrentLocation.equals(mPreLocation)){
				//表示有新数据回调，需要与当前的绘制起点进行比较，确定是加速还是减速
				int offset = calculateCurrentPointOffset(
						naviLatLng2LatLng(mCurrentLatLng),
						naviLatLng2LatLng(mPathLatLngs.get(mCurrentIndex)),
						naviLatLng2LatLng(mCurrentLocation.getCoord()));
				//TODO 如何确定这个速度修正量
				if(offset < 0){
					factor -= 0.1;
				}else if(offset > 0){
					factor += 0.1;
				}
			}
			float step = mCurrentSpeed * factor * 0.03f;
			float distance = AMapUtils.calculateLineDistance(naviLatLng2LatLng(mCurrentLatLng), 
					naviLatLng2LatLng(mPathLatLngs.get(mCurrentIndex)));
			int x=0;
			if(distance == 0){
				//TODO
				x++;
				currentPoint = nextPoint;
				nextPoint = mProjection.
						toScreenLocation(naviLatLng2LatLng(mPathLatLngs.get(mCurrentIndex+x)));
				distance = AMapUtils.calculateLineDistance(naviLatLng2LatLng(mPathLatLngs.get(mCurrentIndex+x-1)), 
						naviLatLng2LatLng(mPathLatLngs.get(mCurrentIndex+x)));
			}
			if(step<=distance){
				//当前还在nextPoint前
			}else{
				//已经移动至nextPoint后
				while(step > distance && mCurrentIndex+x < mPathLatLngs.size()){
					x++;
					step=step - distance;
					currentPoint = nextPoint;
					nextPoint = mProjection.
							toScreenLocation(naviLatLng2LatLng(mPathLatLngs.get(mCurrentIndex+x)));
					distance = AMapUtils.calculateLineDistance(naviLatLng2LatLng(mPathLatLngs.get(mCurrentIndex+x-1)), 
							naviLatLng2LatLng(mPathLatLngs.get(mCurrentIndex+x)));					
				}
				Log.e("step-distance", step + "---" + distance);
			}
			Point point = new Point((int)(currentPoint.x + (nextPoint.x-currentPoint.x)*(step/distance)), (int)(currentPoint.y + 
					(nextPoint.y-currentPoint.y)*(step/distance)));
			LatLng latLng = mProjection.fromScreenLocation(point);
			Log.e("helong", "上一个绘制点："+currentPoint);
			Log.e("helong", "         绘制点："+point);
			Log.e("helong", "         真实点："+mProjection.
					toScreenLocation(naviLatLng2LatLng(mCurrentLocation.getCoord())));
			Log.e("helong", "下一个形状点："+nextPoint);
			Log.e("helong", "=========================================");
			return latLng2NaviLatLng(latLng);
		}
	}
	
	/**
	 * 计算当前回调数据的点处于绘制的点的前方还是后方或者是刚好处于绘制的点上
	 * @param currentLatLng	当前绘制的点
	 * @param nextLatLng 下一个形状点
	 * @param realLatLng 回调数据点（真实车的位置点）
	 * @return 负数代表绘制点快了，正数代表绘制点慢了，0表示正好真实点和绘制点处于同一位置
	 */
	private int calculateCurrentPointOffset(LatLng currentLatLng, LatLng nextLatLng,
			LatLng realLatLng) {
		return (int) (AMapUtils.calculateLineDistance(currentLatLng, nextLatLng) - 
				AMapUtils.calculateLineDistance(realLatLng, nextLatLng));
	}
	//===========================set current location and speed , current index in the list=========
	/**
	 * 设置车的当前位置，用于修正绘制数据
	 * @param location
	 */
	public void setCurrentLocation(AMapNaviLocation location){
		mCurrentLocation = location;
		mCurrentTime = System.currentTimeMillis();
	}
	
	/**
	 * 设置车的当前车速，用于计算数据
	 * @param speed
	 */
	public void setCurrentSpeed(float speed){
		// m/s
		this.mCurrentSpeed = speed;
	}
	
	/**
	 * 设置当前车位于形状点集合的哪个点前
	 * @param curPoint
	 * @param curStep
	 */
	public void setCurrentIndex(int curPoint, int curStep) {
		if (!mCanCreateHUDWay || mProjection == null) {
			return;
		}
		mCurrentIndex = 0;
		for (int i = 0; i < curStep; i++) {
			mCurrentIndex += mCroodsInSteps.get(i);
		}
		mCurrentIndex += curPoint + 1;
		Log.i("CurPoint", mCurrentIndex + "");
		if (mCurrentIndex >= mPathLatLngs.size()) {
			mCurrentIndex = mPathLatLngs.size() - 1;
		}
		mHudwayFactory.setCurrentIndex(curPoint, curStep);
	}
	
	//============================set path========================
	/**
	 * 用于设置整条路径的形状点
	 * @param naviPath
	 * @param amapNaviView
	 */
	public void setPath(AMapNaviPath naviPath, AMapNaviView amapNaviView) {
		this.mCurrentPath = naviPath;
		mProjection = amapNaviView.getMap().getProjection();
		if (mProjection == null || mCurrentPath == null) {
			return;
		}
		// 完整路径经纬度点获取
		List<NaviLatLng> pathLatLngs = mCurrentPath.getCoordList();
		// 路径中的每一个step
		List<AMapNaviStep> naviStepList = mCurrentPath.getSteps();
		// 完整路径屏幕坐标点获取
		List<Point> screenPoints = new ArrayList<Point>();
		for (int j = 0; j < pathLatLngs.size(); j++) {
			NaviLatLng naviLatLng = pathLatLngs.get(j);
			LatLng latLng = new LatLng(naviLatLng.getLatitude(),
					naviLatLng.getLongitude());
			Point screenPt = mProjection
					.toScreenLocation(latLng);
			screenPoints.add(screenPt);
		}
		setRouteLatLngAndScreenPoints(pathLatLngs, screenPoints, naviStepList, mProjection); 
		this.mCanCreateHUDWay = true;
	}
	
	/**
	 * set the lat lng list for route and screen points
	 * 
	 * @param pathLatLngs
	 * @param naviStepList
	 * @param screenPoints
	 */
	public void setRouteLatLngAndScreenPoints(List<NaviLatLng> pathLatLngs,
			List<Point> screenPoints, List<AMapNaviStep> naviStepList,Projection projection) {
		init();
		this.mProjection = projection;
		for (int i = 0; i < screenPoints.size(); i++) {
			// delete repeat item
			if (i == 0 || !screenPoints.get(i - 1).equals(screenPoints.get(i))) {
				mPathLatLngs.add(pathLatLngs.get(i));
			}
		}
		Log.i("pathsize", "pathLatLng.size = "+pathLatLngs.size());		
		

		for (int i = 0; i < mPathLatLngs.size() - 1; i++) {
			mPointsDistance.add(AMapUtils.calculateLineDistance(
					naviLatLng2LatLng(mPathLatLngs.get(i)),
					naviLatLng2LatLng(mPathLatLngs.get(i + 1))));
		}

		for (int i = 0; i < naviStepList.size(); i++) {
			mCroodsInSteps.add(naviStepList.get(i).getCoords().size());
		}
		
		mHudwayFactory.setRouteLatLngAndScreenPoints(pathLatLngs, screenPoints, naviStepList);
	}

	/**
	 * 初始化状态
	 */
	private void init() {
		this.mPathLatLngs.clear();
		this.mCroodsInSteps.clear();
		this.mPointsDistance.clear();
		this.mIsRedLine.clear();
		this.mCurrentIndex = 1;
		this.mCanCreateHUDWay = false;
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
	public void setRoute(List<NaviLatLng> pathLatLngs,
			List<Point> lsRouterScreenPt, List<AMapNaviStep> naviStepList) {
		mHudwayFactory.setRouteLatLngAndScreenPoints(pathLatLngs, lsRouterScreenPt, naviStepList);
	}
}
