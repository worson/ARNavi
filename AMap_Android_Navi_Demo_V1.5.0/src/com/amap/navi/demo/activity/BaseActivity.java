package com.amap.navi.demo.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviGuide;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLink;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.navi.demo.R;
import com.amap.navi.demo.util.HUDWayBitmapFactory;
import com.amap.navi.demo.util.HudwayView2;
import com.amap.navi.demo.util.PointsLines;
import com.amap.navi.demo.util.TTSController;
import com.autonavi.tbt.TrafficFacilityInfo;

/**
 * 创建时间：11/11/15 11:02 项目名称：newNaviDemo
 * 
 * @author lingxiang.wang
 * @email lingxiang.wang@alibaba-inc.com 类说明：
 */

public class BaseActivity extends Activity implements AMapNaviListener,
		AMapNaviViewListener {

	private static final int SIMULATION_SPEED = 200;

	static String TAG = "Harry";

	AMapNaviView naviView;
	AMapNavi aMapNavi;
	TTSController ttsManager;
	// NaviLatLng endLatlng = new NaviLatLng(22.54197,113.989592);//39.925846,
	// 116.432765);
	// NaviLatLng startLatlng = new
	// NaviLatLng(22.540365,113.993518);//39.925041, 116.437901);
	// TODO test mock gps
	// NaviLatLng startLatlng = new NaviLatLng(22.543335, 113.988718);
//	 NaviLatLng endLatlng = new NaviLatLng(22.529822, 113.92235);
	NaviLatLng endLatlng = new NaviLatLng(22.526551, 113.925894);//);//22.547431,113.88884);//22.522304,113.991238);// 113.925894,22.526551 深航飞行员公寓
																	//113.847633,22.600511 固戍A口
																	//清华信息港 113.94633,22.553923
																	//熙龙湾 113.88884,22.547431
																	//福田欢乐海岸  113.991238,22.522304
																	//113.85161,22.593004
//	NaviLatLng startLatlng = new NaviLatLng(22.53183, 114.11720);//22.53183, 114.11720);//罗湖火车站//622.524187, 113.936682);// 113.936682,22.524187
																	// 软件产业基地4A
	NaviLatLng startLatlng = new NaviLatLng(22.600511, 113.847633);// 113.930873,22.523365
																	// 南山区教育幼儿园
	List<NaviLatLng> startList = new ArrayList<NaviLatLng>();
	List<NaviLatLng> endList = new ArrayList<NaviLatLng>();
	List<NaviLatLng> wayPointList = new ArrayList<NaviLatLng>();

	// 棚子停车场出口：22.540365,113.993518,
	// 侨城东路转入香山东街处 22.538978,113.996565,
	// 燕晗花园晗月街入口22.54197, 113.989592,

//	ImageView myOverlayImageView;

	private HUDWayBitmapFactory mHudWayFactory;
	private Projection mProjection;

	private FrameLayout mFl;

	protected HudwayView2 mHudwayView;

//	private ImageView myOverlayImageView2;
	
//	private NaviMockGPSGeneratorForRecordGPS mRecordGPS;
//	private long currentTime = 0;
//	private long nextTime = 0;
	protected AMap aMap;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// 为了尽最大可能避免内存泄露问题，建议这么写
		ttsManager = TTSController.getInstance(getApplicationContext());
		ttsManager.init();
		ttsManager.startSpeaking();

		// 为了尽最大可能避免内存泄露问题，建议这么写
		aMapNavi = AMapNavi.getInstance(getApplicationContext());
		aMapNavi.setAMapNaviListener(this);
		aMapNavi.setAMapNaviListener(ttsManager);
		aMapNavi.setEmulatorNaviSpeed((int) (SIMULATION_SPEED*3.6));
//		 AMapNaviViewOptions viewOptions = naviView.getViewOptions();
//		 viewOptions.setLayoutVisible(false);
//		 naviView.setViewOptions(viewOptions);

//		mRecordGPS = new NaviMockGPSGeneratorForRecordGPS(this);
//		SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
//		String fileName = "GPSFile_" + timeFormatter.format(System.currentTimeMillis());
//		mRecordGPS.startRecordGPS(fileName);
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		naviView.onResume();
		startList.add(startLatlng);
		endList.add(endLatlng);
	}

	@Override
	protected void onPause() {
		super.onPause();
		naviView.onPause();

		// 仅仅是停止你当前在说的这句话，一会到新的路口还是会再说的
		ttsManager.stopSpeaking();
		//
		// 停止导航之后，会触及底层stop，然后就不会再有回调了，但是讯飞当前还是没有说完的半句话还是会说完
		// aMapNavi.stopNavi();
	}

	@Override
	protected void onDestroy() {
		// TODO stop gps record
//		mRecordGPS.stopRecordGPS();
		
		super.onDestroy();
		naviView.onDestroy();
		aMapNavi.destroy();
		ttsManager.destroy();

	}

	@Override
	public void onInitNaviFailure() {
	}

	@Override
	public void onInitNaviSuccess() {
		aMapNavi.calculateDriveRoute(startList, endList, wayPointList, 1);
	}

	@Override
	public void onStartNavi(int type) {

		CameraPosition cameraPos = aMap.getCameraPosition();
		Log.i(TAG, String.format("\n\n\nStartNavi: cameraPos=" + cameraPos));
		 LatLng targetLatLng = new LatLng(startLatlng.getLatitude(),
		 startLatlng.getLongitude());
		 CameraUpdate cameraUpdataCenter =
		 CameraUpdateFactory.changeLatLng(targetLatLng);
		 aMap.moveCamera(cameraUpdataCenter);
		cameraPos = CameraPosition.builder(cameraPos).tilt(45).zoom(10).build();
		CameraUpdate cameraUpdateBearing = CameraUpdateFactory.newCameraPosition(cameraPos);
		aMap.moveCamera(cameraUpdateBearing);
		
		// aMapNavi.stopNavi();
		drawPath();
	}

	private void drawPath() {

		int width = naviView.getWidth();
		int height = naviView.getHeight();
		mProjection = this.naviView.getMap().getProjection();
		AMapNaviPath naviPath = aMapNavi.getNaviPath();
		List<Point> lsRouterScreenPt = new ArrayList<Point>();

		// 完整路径绘制,每个元素是路径中的形状点
		List<NaviLatLng> pathLatLngs = naviPath.getCoordList();
		for (int j = 0; j < pathLatLngs.size(); j++) {
			NaviLatLng naviLatLng = pathLatLngs.get(j);
			Log.i(TAG,
					"LngLat in naviPath.coord" + j + " is "
							+ naviLatLng.getLongitude() + ","
							+ naviLatLng.getLatitude());
			Point screenPt = mProjection.toScreenLocation(new LatLng(naviLatLng
					.getLatitude(), naviLatLng.getLongitude()));
			lsRouterScreenPt.add(screenPt);
			Log.i(TAG, "sceenPt in NaviPath-Coord" + j + " is " + screenPt);
		}

		// 每个路口的坐标
		List<AMapNaviGuide> naviGuideList = aMapNavi.getNaviGuideList();
		for (int i = 0; i < naviGuideList.size(); i++) {
			AMapNaviGuide naviGuide = naviGuideList.get(0);
			NaviLatLng naviLatLng = naviGuide.getCoord();
			Log.i(TAG,
					"LngLat in NaviGuide.turn" + i + " is "
							+ naviLatLng.getLongitude() + ","
							+ naviLatLng.getLatitude());
			Point screenPt = mProjection.toScreenLocation(new LatLng(naviLatLng
					.getLatitude(), naviLatLng.getLongitude()));
			// lsRouterScreenPt.add(screenPt);
		}

		List<AMapNaviStep> naviStepList = naviPath.getSteps();
		for (int i = 0; i < naviStepList.size(); i++) {
			AMapNaviStep naviStep = naviStepList.get(i);
			
			int stepLength = naviStep.getLength();
			int startIdx = naviStep.getStartIndex();
			int endIdx = naviStep.getEndIndex();
			Log.i(TAG, String.format(
					"NaviPath.step%d length=%d startIndex=%d endIndex=%d", i,
					stepLength, startIdx, endIdx));
			List<AMapNaviLink> linksList = naviStep.getLinks();
			if (linksList != null) {
				for (int j = 0; j < linksList.size(); j++) {
					AMapNaviLink link = linksList.get(j);
					int length = link.getLength();
					int roadClass = link.getRoadClass();
					String roadName = link.getRoadName();
					boolean trafficLight = link.getTrafficLights();
					List<NaviLatLng> linkLatLng = link.getCoords();
					Log.i(TAG,
							String.format(
									"NaviPath-Step%d-Link%d: length=%d roadClass=%d linkLatLen.size=%d roadName=",
									i, j, length, roadClass, linkLatLng.size()));

					for (int k = 0; k < linkLatLng.size(); k++) {
						NaviLatLng naviLatLng = linkLatLng.get(k);
						String msg = String.format(
								"LngLat in NaviPath-Link%d-latlng%d is ", j, k);
						Log.i(TAG, msg + naviLatLng.getLongitude() + ","
								+ naviLatLng.getLatitude());
					}
					Log.i(TAG, "=====");
				}
			}
			List<NaviLatLng> latLngList = naviStep.getCoords();
			for (int j = 0; j < latLngList.size(); j++) {
				NaviLatLng naviLatLng = latLngList.get(j);
				Point screenPt = mProjection.toScreenLocation(new LatLng(
						naviLatLng.getLatitude(), naviLatLng.getLongitude()));
				Log.i(TAG,
						"LngLat in NaviPath-Step" + i + "-coord" + j + " is "
								+ naviLatLng.getLongitude() + ","
								+ naviLatLng.getLatitude());
				// lsRouterScreenPt.add(screenPt);
			}
			Log.i(TAG, "===========================");
		}

		Bitmap bmp = drawRouter(lsRouterScreenPt, width, height);
//		myOverlayImageView = (ImageView) this
//				.findViewById(R.id.myOverlayImageView);
//		myOverlayImageView2 = (ImageView) this
//				.findViewById(R.id.myOverlayImageView2);
//		this.myOverlayImageView.setImageBitmap(bmp);

		// TODO init mHudWayFactory hudway
		mHudWayFactory = new HUDWayBitmapFactory(mProjection);
		mHudWayFactory.setRouteLatLngAndScreenPoints(pathLatLngs,
				lsRouterScreenPt,naviStepList);
		
		//TODO test set path
		if(mHudwayView!=null){
			mHudwayView.setRouteLatLngAndScreenPoints(pathLatLngs, lsRouterScreenPt, naviStepList);
		}
	}

	@Override
	public void onTrafficStatusUpdate() {

	}

	@Override
	public void onLocationChange(AMapNaviLocation location) {
		
		float acc = location.getAccuracy();
		double alt = location.getAltitude();
		float bear = location.getBearing();
		float speed = location.getSpeed();
		NaviLatLng naviLatLng = location.getCoord();
		int matchStatus = location.getMatchStatus();
		long time = location.getTime();
		String textLog = String
				.format("onLocationChange loc.bear=%f loc.speed=%f loc.acc=%f loc.status=%d loc.time=%d loc.alt=%f",
						bear, speed, acc, matchStatus, time, alt);
		Log.i(TAG, textLog + " loc.latlng=" + naviLatLng);

		// TODO hudway
		Bitmap hudWayBM = mHudWayFactory.createHUDWayBitmap(location);
		if(hudWayBM!=null){
//			LayoutParams params = (LayoutParams) this.myOverlayImageView.getLayoutParams();
//			this.myOverlayImageView.setLayoutParams(params);
//			this.myOverlayImageView.setBackgroundColor(Color.BLACK);
//			this.myOverlayImageView.setImageBitmap(hudWayBM);
			Point point = mProjection.toScreenLocation(new LatLng(location.getCoord().getLatitude(),location.getCoord().getLongitude()));
			Bitmap bm = drawPoint(point,naviView.getWidth(),naviView.getHeight(),Color.RED,20);
//			this.myOverlayImageView.setImageBitmap(bm);
			
			List<NaviLatLng> mPathLatLngs = mHudWayFactory.getMPathLatLngs();
			if(mPathLatLngs!=null){
				int x = 0;
				NaviLatLng startLatLng = mPathLatLngs.get(mCurrentIndex-1+x);
				NaviLatLng endLatLng = mPathLatLngs.get(mCurrentIndex+x);
				Point start = mProjection.toScreenLocation(new LatLng(startLatLng.getLatitude(),startLatLng.getLongitude()));
				Point end = mProjection.toScreenLocation(new LatLng(endLatLng.getLatitude(),endLatLng.getLongitude()));
				if(!start.equals(end)){
					point = PointsLines.projection(start, end, point);
//					if(!PointsLines.isPointInLine(start, end, point) && !start.equals(end) && mCurrentIndex + x < mPathLatLngs.size()){
//						x++;
//						startLatLng = mPathLatLngs.get(mCurrentIndex-1+x);
//						endLatLng = mPathLatLngs.get(mCurrentIndex+x);
//						start = mProjection.toScreenLocation(new LatLng(startLatLng.getLatitude(),startLatLng.getLongitude()));
//						end = mProjection.toScreenLocation(new LatLng(endLatLng.getLatitude(),endLatLng.getLongitude()));
//						point = PointsLines.projection(start, end, point);
//						Log.e("currentindex", "current index:"+mCurrentIndex +" , x:" +x);
//					}
				}
			}
			
			Bitmap greenBm = drawPoint(point,naviView.getWidth(),naviView.getHeight(),Color.GREEN,15);
//			this.myOverlayImageView2.setImageBitmap(bm);
		}
		
		Log.e("helong fix", "location change");
		//TODO test set location
		if(mHudwayView!=null){
			Log.e("helong fix", "location change");
			mHudwayView.setCurrentLocation(location);
		}

		// record GPS
		//mRecordGPS.onLocationChanged(location);
		
	}

	@Override
	public void onGetNavigationText(int type, String text) {
		Log.i(TAG, String.format("onGetNavigationText type=%d text=%s", type,
				text));
	}

	@Override
	public void onEndEmulatorNavi() {
		Log.i(TAG, String.format("onEndEmulatorNavi"));
	}

	@Override
	public void onArriveDestination() {
		Log.i(TAG, String.format("onArriveDestination"));
	}

	@Override
	public void onCalculateRouteSuccess() {

		mFl = (FrameLayout) findViewById(R.id.fl);
		mHudwayView = new HudwayView2(this,naviView.getMap().getProjection());
		mFl.addView(mHudwayView);
		
		mFakerCrossIV = (ImageView) findViewById(R.id.myFakerCross);

//		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(475, 270);
//		mHudwayView.setLayoutParams(layoutParams);
//		mHudwayView.setBackgroundColor(Color.BLACK);

		
		AMapNaviViewOptions viewOptions = naviView.getViewOptions();
		viewOptions.setNaviNight(true);
		viewOptions.setNaviViewTopic(AMapNaviViewOptions.BLUE_COLOR_TOPIC);
		viewOptions.setZoom(20);
		// Emulator Navi
		//TODO set navi mode
		aMapNavi.startNavi(AMapNavi.EmulatorNaviMode);
		 
//		aMapNavi.startNavi(AMapNavi.GPSNaviMode);
		
		//TODO new thread to send mock gps
/*		new Thread() {
			public void run() {
				try {
//					InputStream in = getAssets().open("mock_gps_data.csv");
//					InputStream in = getAssets().open("GPSFile_20151201213617.csv");
					InputStream in = getAssets().open("GPS20160224160749.csv");
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(in));
					String line = null;
//					String nextLine = null;
					int count = 0;
					while (true) {
//						if(nextLine == null){
//							line = reader.readLine();
//						}else{
//							line = nextLine;
//						}
//						if(line == null){
//							break;
//						}
						
						line = reader.readLine();
						if(line == null){
							break;
						}
						
						//屏蔽掉开始N条数据
						count ++ ;
						if(count <= 12){
							continue;
						}
						String[] items = line.split(",");
						Message msg = Message.obtain();
						msg.what=0;
						if ("2".equals(items[0])) {
							AMapNaviLocation location = new AMapNaviLocation();
							NaviLatLng naviLatLng = new NaviLatLng(
									Double.parseDouble(items[1]),
									Double.parseDouble(items[2]));
							location.setCoord(naviLatLng);
							msg.obj = location;
//							currentTime = Long.parseLong(items[8]);
//							nextLine = reader.readLine();
//							if(nextLine == null){
//								break;
//							}
							
							SystemClock.sleep(300);
						} else {
							NaviInfo naviInfo = new NaviInfo(); 
							naviInfo.setLatitude(Double.parseDouble(items[1]));
							naviInfo.setLongitude(Double.parseDouble(items[2]));
							naviInfo.setCurPoint(Integer.parseInt(items[3].trim()));
							naviInfo.setCurLink(Integer.parseInt(items[4].trim()));
							naviInfo.setCurStep(Integer.parseInt(items[5].trim()));
							msg.obj=naviInfo;
						}
						handler.sendMessage(msg);
					}
					handler.sendEmptyMessage(1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();*/
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				if(msg.obj instanceof AMapNaviLocation){
					AMapNaviLocation naviLocation = (AMapNaviLocation) msg.obj;
					onLocationChange(naviLocation);
				}else{
					NaviInfo naviInfo = (NaviInfo) msg.obj;
					onNaviInfoUpdate(naviInfo);
				}
				break;
			case 1:
				Toast.makeText(getApplicationContext(), "over",
						Toast.LENGTH_SHORT).show();
				break;
			}
		};
	};

	private int mCurrentIndex = 1;

	public static ImageView mFakerCrossIV = null;
	public static boolean mFlushFakerCross = false;
	public static Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			Bitmap bm = (Bitmap) msg.obj;
			mFakerCrossIV.setImageBitmap(bm);
			try {
				write(bitmap2Bytes(bm),"self_image_"+System.currentTimeMillis()+".png");
			} catch (IOException e) {
				e.printStackTrace();
			}
		};
	};
	
	public static byte[] bitmap2Bytes(Bitmap bm) {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	bm.compress(Bitmap.CompressFormat.PNG, 100, baos);//png类型
    	return baos.toByteArray();
    }

    	// 写到sdcard中
    public static void write(byte[] bs,String filename) throws IOException{
    	File file = new File("/sdcard/testimage/selfimages/"+filename);
    	if(!file.exists()){
    		file.createNewFile();
    	}
    	FileOutputStream out=new FileOutputStream(new File("/sdcard/testimage/selfimages/"+filename));
    	out.write(bs);
    	out.flush();
    	out.close();
    }

	@SuppressWarnings("unused")
	private Bitmap drawPoint(List<Point> lsPoints, int width, int height) {
		Bitmap.Config config = Bitmap.Config.ARGB_8888;
		Bitmap targetBitmap = Bitmap.createBitmap(width, height, config);

		Canvas canvas = new Canvas(targetBitmap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.BLUE);

		for (int i = 0; i < lsPoints.size(); i++) {
			Point pt = lsPoints.get(i);
			String strText = "p" + i;
			canvas.drawText(strText, pt.x, pt.y, paint);
		}
		return targetBitmap;
	}

	private Bitmap drawRouter(List<Point> routerPoints, int width, int height) {
		if (width == 0 || height == 0)
			return null;

		Path path = new Path();
		Point beginPt = routerPoints.get(0);
		path.moveTo(beginPt.x, beginPt.y);
		Point prePt = routerPoints.get(0);
		for (int i = 1; i < routerPoints.size(); i++) {
			Point pt = routerPoints.get(i);
			if (pt.x == prePt.x && pt.y == prePt.y) {
				continue;
			}
			path.lineTo(pt.x, pt.y);
		}

		Bitmap.Config config = Bitmap.Config.ARGB_8888;
		Bitmap targetBitmap = Bitmap.createBitmap(width, height, config);

		Canvas canvas = new Canvas(targetBitmap);
		Paint paint = new Paint();
		paint.setStrokeWidth(20);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		paint.setColor(Color.RED);
		canvas.drawPath(path, paint);

		return targetBitmap;
	}
	
	private Bitmap drawPoint(Point point , int width ,int height , int color ,int radius){
		Bitmap.Config config = Bitmap.Config.ARGB_8888;
		Bitmap targetBitmap = Bitmap.createBitmap(width, height, config);

		Canvas canvas = new Canvas(targetBitmap);
		Paint paint = new Paint();
		paint.setColor(color);
		canvas.drawCircle(point.x, point.y, radius, paint);
		return targetBitmap;
	}

	@Override
	public void onCalculateRouteFailure(int errorInfo) {
		Log.i(TAG, String.format("onCalculateRouteFailure error=%d", errorInfo));
	}

	@Override
	public void onReCalculateRouteForYaw() {

	}

	@Override
	public void onReCalculateRouteForTrafficJam() {

	}

	@Override
	public void onArrivedWayPoint(int wayID) {

	}

	@Override
	public void onGpsOpenStatus(boolean enabled) {
	}

	@Override
	public void onNaviSetting() {
	}

	@Override
	public void onNaviMapMode(int isLock) {

	}

	@Override
	public void onNaviCancel() {
		finish();
	}

	@Override
	public void onNaviTurnClick() {

	}

	@Override
	public void onNextRoadClick() {

	}

	@Override
	public void onScanViewButtonClick() {
	}

	@Deprecated
	@Override
	public void onNaviInfoUpdated(AMapNaviInfo naviInfo) {
	}

	@Override
	public void onNaviInfoUpdate(NaviInfo naviinfo) {
		
	mCurrentIndex  = mHudWayFactory.setCurrentIndex(naviinfo.getCurPoint(),naviinfo.getCurStep());
		
		mHudWayFactory.setCurrentIndex(naviinfo.getCurPoint(),naviinfo.getCurStep());
		
		//TODO test set current index
		if (mHudwayView != null)
			mHudwayView.setCurrentIndex(naviinfo.getCurPoint(),naviinfo.getCurStep());
		AMapNaviLocation location = new AMapNaviLocation();
		NaviLatLng naviLatLng = new NaviLatLng(naviinfo.getCoord().getLatitude(), naviinfo.getCoord().getLongitude());
		location.setCoord(naviLatLng);
		location.setSpeed(SIMULATION_SPEED);
		onLocationChange(location);
//		
		
		// record GPS
		//mRecordGPS.onNaviInfoUpdate(naviinfo);
		
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
	public void showLaneInfo(AMapLaneInfo[] laneInfos,
			byte[] laneBackgroundInfo, byte[] laneRecommendedInfo) {

	}

	@Override
	public void hideLaneInfo() {

	}

	@Override
	public void onLockMap(boolean isLock) {
	}

	@Override
	public boolean onNaviBackClick() {
		return false;
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
