package com.amap.navi.demo.util;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.Path.Direction;
import android.graphics.PathDashPathEffect;
import android.graphics.PathDashPathEffect.Style;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.os.Environment;
import android.util.Log;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.model.AMapNaviLink;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.navi.demo.R;
import com.amap.navi.demo.activity.IndexActivity;

/**
 * 根据传入的坐标集合绘制HUDWay路线 通过当前位置实时更新路线 bitmap size 2000*2000 显示路线长1KM ~ 2KM
 * 
 * @author 龙
 * 
 */
public class HUDWayBitmapFactory {

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
	private Projection mProjection;
	private boolean mIsLastPoint = false;
	private float mCurPoint2FirstPointDist;
	private AMapNaviLocation mPreLocation;
	
	private static final int BITMAP_WIDTH = 1000;
	private static final int BITMAP_HEIGHT = 1000;
	
	private static final int OUTSIDE_LINE_WIDTH = 50;
	private static final int MIDDLE_LINE_WIDTH = 40;
	private static final int INSIDE_LINE_WIDTH = 30;
	private static final int CIRCLE_LINE_WIDTH = 5;
	private static final float RED_LINE_LENGTH = 5;

	public HUDWayBitmapFactory( Projection projection) {
		this.mProjection = projection;
	}

	/**
	 * set the lat lng list for route and screen points
	 * 
	 * @param pathLatLngs
	 * @param naviStepList
	 * @param lsRouterScreenPt
	 */
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
			if(mPointsDistance.get(i)<=30){
				mIsRedLine.add(true);
			}else{
				mIsRedLine.add(false);
			}
		}
		
		for (int i = 0; i < naviStepList.size(); i++) {
			mCroodsInSteps.add(naviStepList.get(i).getCoords().size());
		}
	}

	/**
	 * initialization status
	 */
	private void init() {
		this.mPathLatLngs.clear();
		this.mCroodsInSteps.clear();
		this.mTrafficLightNaviLatLngs.clear();
		this.mPointsDistance.clear();
		this.mIsRedLine.clear();
		this.mCurrentIndex = 1;
		this.mIsLastPoint = false;
	}
	
	public void setData(List<Float> pointsDistance, List<Integer> croodsInSteps, List<NaviLatLng> pathLatLngs){
		this.mPointsDistance = pointsDistance;
		this.mCroodsInSteps = croodsInSteps;
		this.mPathLatLngs = pathLatLngs;
	}
	
	public void setCurrentIndex(int currentIndex){
		mCurrentIndex = currentIndex;
	}
	
	public void drawHudway(Canvas can, AMapNaviLocation location/*, int prePreCurrentIndex*/, AMapNaviLocation preLocation) {
		HUDWAY_LENGTH_IN_SCREEN=IndexActivity.route_length;
		Log.e("helong fix","come in draw hudway");
		if(location == null){
			return;
		}
		Log.e("helong fix","come in draw hudway_haha");
		
//		this.mCurrentIndex = prePreCurrentIndex;
		this.mPreLocation = preLocation;
		// get the points for draw bitmap
		List<Point> currentPoints = getCurrentPoints(location);
		Log.e("pointss", "draw:"+currentPoints);
		// if currentPoints is null or it`s size is zero
		if (currentPoints == null || currentPoints.size() <=1) {
			return;
		}
		
		Paint paint = new Paint();
		Canvas canvas = can;
		paint.setColor(Color.BLACK);
		canvas.drawPaint(paint);
//		Paint p = new Paint();
//		p.setColor(Color.RED);
//		for (Point point : currentPoints) {
//			canvas.drawCircle(point.x,point.y, 20, p);
//		}
		
		
		
		// setup paint
		paint.setStrokeWidth(OUTSIDE_LINE_WIDTH);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		paint.setColor(Color.WHITE);
		

		float[] pointsXY = new float[currentPoints.size() * 2];

		// get the points x and y
		for (int i = 0; i < pointsXY.length; i++) {
			if (i % 2 == 0) {
				pointsXY[i] = currentPoints.get(i / 2).x;
			} else {
				pointsXY[i] = currentPoints.get(i / 2).y;
			}
		}

		// move to screen center , here set 1120-130,1120 is the center
		float offsetX = BITMAP_WIDTH/2 - pointsXY[0];
		float offsetY = BITMAP_HEIGHT - pointsXY[1];
		for (int i = 0; i < pointsXY.length; i++) {
			pointsXY[i] = i % 2 == 0 ? pointsXY[i] + offsetX : pointsXY[i]
					+ offsetY;
		}

		// save this distance point to point, 2~1, 3~2 .....
		float[] distance = new float[pointsXY.length - 2];
		for (int i = 0; i < distance.length; i++) {
			distance[i] = pointsXY[i + 2] - pointsXY[i];
		}

		// Magnified 2 times
		// start point is constant，another points will change
		for (int i = 2; i < pointsXY.length; i++) {
			pointsXY[i] = (float) (pointsXY[i - 2] + distance[i - 2] / 2 );
		}

		// Calculate degrees
		float degrees = 0.0f;
		// if the line is vertical
		if (pointsXY[2] == pointsXY[0]) {
			if (pointsXY[3] <= pointsXY[1]) {
				degrees = 0;
			} else {
				degrees = 180;
			}
			// if the line is horizontal
		} else if (pointsXY[3] == pointsXY[1]) {
			if (pointsXY[2] <= pointsXY[0]) {
				degrees = 90;
			} else {
				degrees = 270;
			}
		} else {
			// if the line is not a vertical or horizontal,we should be to
			// calculate the degrees
			// cosA = (c*c + b*b - a*a)/(2*b*c)
			// A = acos(A)/2/PI*360
			double c = Math.sqrt(Math.pow(Math.abs(pointsXY[0] - pointsXY[2]),
					2.0) + Math.pow(Math.abs(pointsXY[1] - pointsXY[3]), 2.0));
			double b = Math.abs(pointsXY[1] - pointsXY[3]);
			double a = Math.abs(pointsXY[0] - pointsXY[2]);
			degrees = (float) (Math.acos((c * c + b * b - a * a) / (2 * b * c))
					/ 2 / Math.PI * 360);
			if (pointsXY[2] >= pointsXY[0] && pointsXY[3] >= pointsXY[1]) {
				degrees += 180;
			} else if (pointsXY[2] <= pointsXY[0] && pointsXY[3] >= pointsXY[1]) {
				degrees = (90 - degrees) + 90;
			} else if (pointsXY[2] <= pointsXY[0] && pointsXY[3] <= pointsXY[1]) {
				degrees += 0;
			} else if (pointsXY[2] >= pointsXY[0] && pointsXY[3] <= pointsXY[1]) {
				degrees = 270 + (90 - degrees);
			}
		}
		
		// here we must be 3D turn around first ,and rotate the path second.
		// 3D turn around and set matrix
//		final Camera camera = new Camera();
//		@SuppressWarnings("deprecation")
//		final Matrix matrix = canvas.getMatrix();
//		// save the camera status for restore
//		camera.save();
//		// around X rotate N degrees
//		camera.rotateX(60);
//		camera.translate(0.0f, 250.0f, 0.0f);
//		//x = -500 则为摄像头向右移动 
//		//y = 200 则为摄像头向下移动 
//		//z = 500 则为摄像头向高处移动 
//		// get the matrix from camera
//		camera.getMatrix(matrix);
//		// restore camera from the next time
//		camera.restore();
//		matrix.preTranslate(-pointsXY[0], -(pointsXY[1]));
//		matrix.postTranslate(pointsXY[0], (pointsXY[1]));
//		// matrix.postScale(4, 3, pointsXY[0], pointsXY[1]);
//		canvas.setMatrix(matrix);
		

		// rotate the path
//		if(degrees > 60 && degrees < 300)
			canvas.rotate(degrees, pointsXY[0], pointsXY[1]);

		// draw the path
		Path path = new Path();
		Path redPointPath = new Path();
		Path circlePath = new Path();
		
		path.moveTo(pointsXY[0], pointsXY[1]);
		redPointPath.moveTo(pointsXY[0], pointsXY[1]);
		Log.e("摆正path", Arrays.toString(pointsXY));
		for (int i = 0; i < pointsXY.length / 2 - 1; i++) {
//			float lineLength = i==0?mCurPoint2FirstPointDist : mPointsDistance.get(mCurrentIndex+i-1);
			if(mCurrentIndex+i-1 >= mPointsDistance.size()){
				return;
			}
			float lineLength = mPointsDistance.get(mCurrentIndex+i-1);
			
			path.lineTo(pointsXY[(i + 1) * 2], pointsXY[(i + 1) * 2 + 1]);
//			if (i < pointsXY.length / 2 - 1 - 1) {
//				// circlePath is the circle route , so it less one point to
//				// shorter than outside line and inside line
//				circlePath.lineTo(pointsXY[(i + 1) * 2],
//						pointsXY[(i + 1) * 2 + 1]);
//			}
			
//			//add circle to circlePath for draw can move path
//			//every 50 we will add a point,from end to start
//			int pointCount=(int) (lineLength/HUDWAY_POINT_INTERVAL);
//			//if the line length greater than HUDWAY_POINT_INTERVAL
//			if(pointCount>0){
//				float step_x = (pointsXY[(i + 1) * 2]-pointsXY[(i ) * 2])/lineLength*HUDWAY_POINT_INTERVAL;
//				float step_y = (pointsXY[(i + 1) * 2 + 1]-pointsXY[(i) * 2 + 1])/lineLength*HUDWAY_POINT_INTERVAL;
//				for(int j = 0 ; j < pointCount ; j ++){
//					circlePath.addCircle(pointsXY[(i + 1) * 2]-(j+1)*step_x,pointsXY[(i+1 ) * 2 + 1]-(j+1)*step_y,10, Path.Direction.CW);
//				}
//			}
			
			//TODO mIsRedLine.get(mCurrent+i-1)
			if(mIsRedLine.get(i)){
				redPointPath.lineTo(pointsXY[(i+1) * 2], pointsXY[(i+1) * 2+1]);
			}else{
				redPointPath.moveTo(pointsXY[(i+1) * 2], pointsXY[(i +1) * 2+1]);
				if(lineLength > 50){
					int pointCount=(int) (lineLength/HUDWAY_POINT_INTERVAL);
					//if the line length greater than HUDWAY_POINT_INTERVAL
					if(pointCount>0){
						float step_x = (pointsXY[(i + 1) * 2]-pointsXY[(i ) * 2])/lineLength*HUDWAY_POINT_INTERVAL;
						float step_y = (pointsXY[(i + 1) * 2 + 1]-pointsXY[(i) * 2 + 1])/lineLength*HUDWAY_POINT_INTERVAL;
						for(int j = 0 ; j < pointCount ; j ++){
							circlePath.addCircle(pointsXY[(i + 1) * 2]-(j+1)*step_x,pointsXY[(i+1 ) * 2 + 1]-(j+1)*step_y,10, Path.Direction.CW);
						}
					}
				}
			}
		}
		
		
		// set corner path for right angle(直角)
		CornerPathEffect cornerPathEffect = new CornerPathEffect(20);
		paint.setPathEffect(cornerPathEffect);

		// draw outside line
		canvas.drawPath(path, paint);

//		// draw point line
//		Path shape = new Path();
//		shape.addCircle(0, 0, 10, Direction.CW);
//		// this is the path effect for circle , the 100 is the offset to look
//		// like center in the route
//		PathEffect effects = new PathDashPathEffect(shape, 200, 100,Style.ROTATE);// offset 100
//		paint.setPathEffect(effects);
		
		//red and black line
		paint.setStrokeWidth(MIDDLE_LINE_WIDTH);
		paint.setColor(Color.BLACK);
		canvas.drawPath(path, paint);
		paint.setColor(Color.RED);
		canvas.drawPath(redPointPath, paint);
		
		// draw inside line
		paint.setStrokeWidth(INSIDE_LINE_WIDTH);
		paint.setColor(Color.BLACK);
		canvas.drawPath(path, paint);
		
		// draw can move point
//		paint.setColor(Color.RED);
//		paint.setStyle(Paint.Style.FILL);
//		paint.setStrokeWidth(CIRCLE_LINE_WIDTH);
//		canvas.drawPath(circlePath,paint);
	}
	
	public void drawHudway(Canvas can , List<Point> points4Hudway, List<Boolean> isRedLine){
		// get the points for draw bitmapmn 
		List<Point> currentPoints = points4Hudway;
		mIsRedLine = isRedLine;
		// if currentPoints is null or it`s size is zero
		if (currentPoints == null || currentPoints.size() <=1) {
			return ;
		}

		Canvas canvas = can;
		canvas.drawColor(Color.BLACK);
		// setup paint
		Paint paint = new Paint();
		paint.setStrokeWidth(OUTSIDE_LINE_WIDTH);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		paint.setColor(Color.WHITE);

		float[] pointsXY = new float[currentPoints.size() * 2];

		// get the points x and y
		for (int i = 0; i < pointsXY.length; i++) {
			if (i % 2 == 0) {
				pointsXY[i] = currentPoints.get(i / 2).x;
			} else {
				pointsXY[i] = currentPoints.get(i / 2).y;
			}
		}

		// move to screen center , here set 1120-130,1120 is the center
		float offsetX = BITMAP_WIDTH/2 - pointsXY[0];
		float offsetY = BITMAP_HEIGHT - pointsXY[1];
		for (int i = 0; i < pointsXY.length; i++) {
			pointsXY[i] = i % 2 == 0 ? pointsXY[i] + offsetX : pointsXY[i]
					+ offsetY;
		}

		// save this distance point to point, 2~1, 3~2 .....
		float[] distance = new float[pointsXY.length - 2];
		for (int i = 0; i < distance.length; i++) {
			distance[i] = pointsXY[i + 2] - pointsXY[i];
		}

		// Magnified 2 times
		// start point is constant，another points will change
		for (int i = 2; i < pointsXY.length; i++) {
			pointsXY[i] = (float) (pointsXY[i - 2] + distance[i - 2] * 1);
		}

		// Calculate degrees
		float degrees = 0.0f;
		// if the line is vertical
		if (pointsXY[2] == pointsXY[0]) {
			if (pointsXY[3] <= pointsXY[1]) {
				degrees = 0;
			} else {
				degrees = 180;
			}
			// if the line is horizontal
		} else if (pointsXY[3] == pointsXY[1]) {
			if (pointsXY[2] <= pointsXY[0]) {
				degrees = 90;
			} else {
				degrees = 270;
			}
		} else {
			// if the line is not a vertical or horizontal,we should be to
			// calculate the degrees
			// cosA = (c*c + b*b - a*a)/(2*b*c)
			// A = acos(A)/2/PI*360
			double c = Math.sqrt(Math.pow(Math.abs(pointsXY[0] - pointsXY[2]),
					2.0) + Math.pow(Math.abs(pointsXY[1] - pointsXY[3]), 2.0));
			double b = Math.abs(pointsXY[1] - pointsXY[3]);
			double a = Math.abs(pointsXY[0] - pointsXY[2]);
			degrees = (float) (Math.acos((c * c + b * b - a * a) / (2 * b * c))
					/ 2 / Math.PI * 360);
			if (pointsXY[2] >= pointsXY[0] && pointsXY[3] >= pointsXY[1]) {
				degrees += 180;
			} else if (pointsXY[2] <= pointsXY[0] && pointsXY[3] >= pointsXY[1]) {
				degrees = (90 - degrees) + 90;
			} else if (pointsXY[2] <= pointsXY[0] && pointsXY[3] <= pointsXY[1]) {
				degrees += 0;
			} else if (pointsXY[2] >= pointsXY[0] && pointsXY[3] <= pointsXY[1]) {
				degrees = 270 + (90 - degrees);
			}
		}
		
		// here we must be 3D turn around first ,and rotate the path second.
		// 3D turn around and set matrix
		final Camera camera = new Camera();
		@SuppressWarnings("deprecation")
		final Matrix matrix = canvas.getMatrix();
		// save the camera status for restore
		camera.save();
		// around X rotate N degrees
		camera.rotateX(60);
		// get the matrix from camera
		camera.getMatrix(matrix);
		// restore camera from the next time
		camera.restore();
		matrix.preTranslate(-pointsXY[0], -(pointsXY[1]));
		matrix.postTranslate(pointsXY[0], (pointsXY[1]));
		// matrix.postScale(4, 3, pointsXY[0], pointsXY[1]);
		canvas.setMatrix(matrix);
		

		// rotate the path
		canvas.rotate(degrees, pointsXY[0], pointsXY[1]);

		// draw the path
		Path path = new Path();
		Path redPointPath = new Path();
		Path circlePath = new Path();
		
		path.moveTo(pointsXY[0], pointsXY[1]);
		redPointPath.moveTo(pointsXY[0], pointsXY[1]);
		int curPointsTotalDistance = 0;
		for (int i = 0; i < pointsXY.length / 2 - 1; i++) {
//			float lineLength = i==0?mCurPoint2FirstPointDist : mPointsDistance.get(mCurrentIndex+i-1);
			float lineLength = mPointsDistance.get(mCurrentIndex+i-1);
			//calculate the cur points total distance 
			curPointsTotalDistance+=lineLength;
			
			path.lineTo(pointsXY[(i + 1) * 2], pointsXY[(i + 1) * 2 + 1]);
//			if (i < pointsXY.length / 2 - 1 - 1) {
//				// circlePath is the circle route , so it less one point to
//				// shorter than outside line and inside line
//				circlePath.lineTo(pointsXY[(i + 1) * 2],
//						pointsXY[(i + 1) * 2 + 1]);
//			}
			
//			//add circle to circlePath for draw can move path
//			//every 50 we will add a point,from end to start
//			int pointCount=(int) (lineLength/HUDWAY_POINT_INTERVAL);
//			//if the line length greater than HUDWAY_POINT_INTERVAL
//			if(pointCount>0){
//				float step_x = (pointsXY[(i + 1) * 2]-pointsXY[(i ) * 2])/lineLength*HUDWAY_POINT_INTERVAL;
//				float step_y = (pointsXY[(i + 1) * 2 + 1]-pointsXY[(i) * 2 + 1])/lineLength*HUDWAY_POINT_INTERVAL;
//				for(int j = 0 ; j < pointCount ; j ++){
//					circlePath.addCircle(pointsXY[(i + 1) * 2]-(j+1)*step_x,pointsXY[(i+1 ) * 2 + 1]-(j+1)*step_y,10, Path.Direction.CW);
//				}
//			}
			
			if(mIsRedLine.get(i)){
				redPointPath.lineTo(pointsXY[(i+1) * 2], pointsXY[(i+1) * 2+1]);
			}else{
				redPointPath.moveTo(pointsXY[(i+1) * 2], pointsXY[(i +1) * 2+1]);
				if(lineLength > 50){
					int pointCount=(int) (lineLength/HUDWAY_POINT_INTERVAL);
					//if the line length greater than HUDWAY_POINT_INTERVAL
					if(pointCount>0){
						float step_x = (pointsXY[(i + 1) * 2]-pointsXY[(i ) * 2])/lineLength*HUDWAY_POINT_INTERVAL;
						float step_y = (pointsXY[(i + 1) * 2 + 1]-pointsXY[(i) * 2 + 1])/lineLength*HUDWAY_POINT_INTERVAL;
						for(int j = 0 ; j < pointCount ; j ++){
							circlePath.addCircle(pointsXY[(i + 1) * 2]-(j+1)*step_x,pointsXY[(i+1 ) * 2 + 1]-(j+1)*step_y,10, Path.Direction.CW);
						}
					}
				}
			}
		}
		
		
		// set corner path for right angle(直角)
		CornerPathEffect cornerPathEffect = new CornerPathEffect(20);
		paint.setPathEffect(cornerPathEffect);

		// draw outside line
		canvas.drawPath(path, paint);

//		// draw point line
//		Path shape = new Path();
//		shape.addCircle(0, 0, 10, Direction.CW);
//		// this is the path effect for circle , the 100 is the offset to look
//		// like center in the route
//		PathEffect effects = new PathDashPathEffect(shape, 200, 100,Style.ROTATE);// offset 100
//		paint.setPathEffect(effects);
		
		//red and black line
		paint.setStrokeWidth(MIDDLE_LINE_WIDTH);
		paint.setColor(Color.BLACK);
		canvas.drawPath(path, paint);
		paint.setColor(Color.RED);
		canvas.drawPath(redPointPath, paint);
		
		// draw inside line
		paint.setStrokeWidth(INSIDE_LINE_WIDTH);
		paint.setColor(Color.BLACK);
		canvas.drawPath(path, paint);
		
		// draw can move point
		paint.setColor(Color.RED);
		paint.setStyle(Paint.Style.FILL);
		paint.setStrokeWidth(CIRCLE_LINE_WIDTH);
		canvas.drawPath(circlePath,paint);
		
	}

	/**
	 * return bitmap for current route with current location
	 * 
	 * @param location
	 *            current location
	 * @param currentScreenPoint
	 *            current point for screen
	 * @return current route path bitmap
	 */
	public Bitmap createHUDWayBitmap(AMapNaviLocation location) {
		// get the points for draw bitmap
		List<Point> currentPoints = getCurrentPoints(location);
		Log.e("points", "create:"+currentPoints);
		// if currentPoints is null or it`s size is zero
		if (currentPoints == null || currentPoints.size() <=1) {
			return null;
		}

		// create a empty bitmap
		Bitmap bitmap = Bitmap
				.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_4444);
		Canvas canvas = new Canvas(bitmap);

		// setup paint
		Paint paint = new Paint();
		paint.setStrokeWidth(OUTSIDE_LINE_WIDTH);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		paint.setColor(Color.WHITE);

		float[] pointsXY = new float[currentPoints.size() * 2];

		// get the points x and y
		for (int i = 0; i < pointsXY.length; i++) {
			if (i % 2 == 0) {
				pointsXY[i] = currentPoints.get(i / 2).x;
			} else {
				pointsXY[i] = currentPoints.get(i / 2).y;
			}
		}

		// move to screen center , here set 1120-130,1120 is the center
		float offsetX = BITMAP_WIDTH/2 - pointsXY[0];
		float offsetY = BITMAP_HEIGHT - pointsXY[1];
		for (int i = 0; i < pointsXY.length; i++) {
			pointsXY[i] = i % 2 == 0 ? pointsXY[i] + offsetX : pointsXY[i]
					+ offsetY;
		}

		// save this distance point to point, 2~1, 3~2 .....
		float[] distance = new float[pointsXY.length - 2];
		for (int i = 0; i < distance.length; i++) {
			distance[i] = pointsXY[i + 2] - pointsXY[i];
		}

		// Magnified 2 times
		// start point is constant，another points will change
		for (int i = 2; i < pointsXY.length; i++) {
			pointsXY[i] = (float) (pointsXY[i - 2] + distance[i - 2] * 2);
		}

		// Calculate degrees
		float degrees = 0.0f;
		// if the line is vertical
		//TODO 使用上一个形状点坐标代替当前绘制点
		//TODO 使用prePoint.x 代替 pointsXY[0] , prePoint.y 代替 pointsXY[1]
		Point prePoint = mProjection.toScreenLocation(naviLatLng2LatLng(mPathLatLngs.get(mCurrentIndex -1))); 
		if (pointsXY[2] ==prePoint.x) {
			if (pointsXY[3] <= prePoint.y) {
				degrees = 0;
			} else {
				degrees = 180;
			}
			// if the line is horizontal
		} else if (pointsXY[3] == prePoint.y) {
			if (pointsXY[2] <= prePoint.x) {
				degrees = 90;
			} else {
				degrees = 270;
			}
		} else {
			// if the line is not a vertical or horizontal,we should be to
			// calculate the degrees
			// cosA = (c*c + b*b - a*a)/(2*b*c)
			// A = acos(A)/2/PI*360
			double c = Math.sqrt(Math.pow(Math.abs(prePoint.x - pointsXY[2]),
					2.0) + Math.pow(Math.abs(prePoint.y - pointsXY[3]), 2.0));
			double b = Math.abs(prePoint.y - pointsXY[3]);
			double a = Math.abs(prePoint.x - pointsXY[2]);
			degrees = (float) (Math.acos((c * c + b * b - a * a) / (2 * b * c))
					/ 2 / Math.PI * 360);
			if (pointsXY[2] >= prePoint.x && pointsXY[3] >= prePoint.y) {
				degrees += 180;
			} else if (pointsXY[2] <= prePoint.x && pointsXY[3] >= prePoint.y) {
				degrees = (90 - degrees) + 90;
			} else if (pointsXY[2] <= prePoint.x && pointsXY[3] <= prePoint.y) {
				degrees += 0;
			} else if (pointsXY[2] >= prePoint.x && pointsXY[3] <= prePoint.y) {
				degrees = 270 + (90 - degrees);
			}
		}
		
		// here we must be 3D turn around first ,and rotate the path second.
		// 3D turn around and set matrix
		final Camera camera = new Camera();
		@SuppressWarnings("deprecation")
		final Matrix matrix = canvas.getMatrix();
		// save the camera status for restore
		camera.save();
		// around X rotate N degrees
		camera.rotateX(60);
		// get the matrix from camera
		camera.getMatrix(matrix);
		// restore camera from the next time
		camera.restore();
		matrix.preTranslate(-pointsXY[0], -(pointsXY[1]));
		matrix.postTranslate(pointsXY[0], (pointsXY[1]));
		// matrix.postScale(4, 3, pointsXY[0], pointsXY[1]);
		canvas.setMatrix(matrix);
		

		// rotate the path
		canvas.rotate(degrees, pointsXY[0], pointsXY[1]);

		// draw the path
		Path path = new Path();
		Path redPointPath = new Path();
		Path circlePath = new Path();
		
		path.moveTo(pointsXY[0], pointsXY[1]);
		redPointPath.moveTo(pointsXY[0], pointsXY[1]);
		int curPointsTotalDistance = 0;
		for (int i = 0; i < pointsXY.length / 2 - 1; i++) {
//			float lineLength = i==0?mCurPoint2FirstPointDist : mPointsDistance.get(mCurrentIndex+i-1);
			float lineLength = mPointsDistance.get(mCurrentIndex+i-1);
			//calculate the cur points total distance 
			curPointsTotalDistance+=lineLength;
			
			path.lineTo(pointsXY[(i + 1) * 2], pointsXY[(i + 1) * 2 + 1]);
//			if (i < pointsXY.length / 2 - 1 - 1) {
//				// circlePath is the circle route , so it less one point to
//				// shorter than outside line and inside line
//				circlePath.lineTo(pointsXY[(i + 1) * 2],
//						pointsXY[(i + 1) * 2 + 1]);
//			}
			
//			//add circle to circlePath for draw can move path
//			//every 50 we will add a point,from end to start
//			int pointCount=(int) (lineLength/HUDWAY_POINT_INTERVAL);
//			//if the line length greater than HUDWAY_POINT_INTERVAL
//			if(pointCount>0){
//				float step_x = (pointsXY[(i + 1) * 2]-pointsXY[(i ) * 2])/lineLength*HUDWAY_POINT_INTERVAL;
//				float step_y = (pointsXY[(i + 1) * 2 + 1]-pointsXY[(i) * 2 + 1])/lineLength*HUDWAY_POINT_INTERVAL;
//				for(int j = 0 ; j < pointCount ; j ++){
//					circlePath.addCircle(pointsXY[(i + 1) * 2]-(j+1)*step_x,pointsXY[(i+1 ) * 2 + 1]-(j+1)*step_y,10, Path.Direction.CW);
//				}
//			}
			
			if(mIsRedLine.get(i)){
				redPointPath.lineTo(pointsXY[(i+1) * 2], pointsXY[(i+1) * 2+1]);
			}else{
				redPointPath.moveTo(pointsXY[(i+1) * 2], pointsXY[(i +1) * 2+1]);
				if(lineLength > 50){
					int pointCount=(int) (lineLength/HUDWAY_POINT_INTERVAL);
					//if the line length greater than HUDWAY_POINT_INTERVAL
					if(pointCount>0){
						float step_x = (pointsXY[(i + 1) * 2]-pointsXY[(i ) * 2])/lineLength*HUDWAY_POINT_INTERVAL;
						float step_y = (pointsXY[(i + 1) * 2 + 1]-pointsXY[(i) * 2 + 1])/lineLength*HUDWAY_POINT_INTERVAL;
						for(int j = 0 ; j < pointCount ; j ++){
							circlePath.addCircle(pointsXY[(i + 1) * 2]-(j+1)*step_x,pointsXY[(i+1 ) * 2 + 1]-(j+1)*step_y,10, Path.Direction.CW);
						}
					}
				}
			}
		}
		
		
		// set corner path for right angle(直角)
		CornerPathEffect cornerPathEffect = new CornerPathEffect(20);
		paint.setPathEffect(cornerPathEffect);

		// draw outside line
		canvas.drawPath(path, paint);

//		// draw point line
//		Path shape = new Path();
//		shape.addCircle(0, 0, 10, Direction.CW);
//		// this is the path effect for circle , the 100 is the offset to look
//		// like center in the route
//		PathEffect effects = new PathDashPathEffect(shape, 200, 100,Style.ROTATE);// offset 100
//		paint.setPathEffect(effects);
		
		//red and black line
		paint.setStrokeWidth(MIDDLE_LINE_WIDTH);
		paint.setColor(Color.BLACK);
		canvas.drawPath(path, paint);
		paint.setColor(Color.RED);
		canvas.drawPath(redPointPath, paint);
		
		// draw inside line
		paint.setStrokeWidth(INSIDE_LINE_WIDTH);
		paint.setColor(Color.BLACK);
		canvas.drawPath(path, paint);
		
		// draw can move point
		paint.setColor(Color.RED);
		paint.setStyle(Paint.Style.FILL);
		paint.setStrokeWidth(CIRCLE_LINE_WIDTH);
		canvas.drawPath(circlePath,paint);
		
		return bitmap;
	}

	/**
	 * return points for current route according to current location
	 * 
	 * @param location
	 *            current location
	 * @param currentScreenPoint
	 *            current location in screen x,y
	 * @return
	 */
	private List<Point> getCurrentPoints(AMapNaviLocation location) {
		// TODO
		NaviLatLng currentLatLng = location.getCoord();
		List<Point> points = new ArrayList<Point>();
		fullPoints(currentLatLng, points);
		// NaviLatLng firstLatLng = mPathLatLngs.get(0);
		// if current location is the start location in path
		// if (currentLatLng.getLatitude() == firstLatLng.getLatitude()
		// && currentLatLng.getLongitude() == firstLatLng.getLongitude()) {
		// fullPoints(currentLatLng, points, currentScreenPoint);
		// } else {
		// fullPoints(currentLatLng, points, currentScreenPoint);
		// }
		
		return points;
	}

	/**
	 * calculate to full points to draw path bitmap
	 * 
	 * @param currentLatLng
	 *            our car current location latlng
	 * @param points
	 *            points list from draw the path bitmap
	 * @param currentScreenPoint
	 *            our car current location in screen
	 */
	private void fullPoints(NaviLatLng currentLatLng, List<Point> points) {
		float totalLength = 0;
		mIsRedLine.clear();
		points.clear();
		Point currentScreenPoint = mProjection
				.toScreenLocation(naviLatLng2LatLng(currentLatLng));
		points.add(currentScreenPoint);
		//TODO
//		if(mPreLocation != null)
//			points.add(mProjection.toScreenLocation(naviLatLng2LatLng(mPreLocation.getCoord())));
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
								naviLatLng2LatLng(currentLatLng),
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
			// set the boolean for current i is or not last one
			if (i == mPathLatLngs.size() - 1) {
				mIsLastPoint = true;
			} else {
				mIsLastPoint = false;
			}
			//be sure the total distance is HUDWAY_LENGTH_IN_SCREEN
			if(totalLength == HUDWAY_LENGTH_IN_SCREEN){
				points.add(pathPoint);
				Log.e("factory", "mCurrentIndex :" + mCurrentIndex +"\n"
						+"points : "+points +"\n"+ "PathLatLngs : " + mPathLatLngs +"\n" +"============================");
				return;
			}else if(totalLength > HUDWAY_LENGTH_IN_SCREEN){
				float div = totalLength - HUDWAY_LENGTH_IN_SCREEN;
				Point prePoint = null;
				if(i == mCurrentIndex){
					prePoint = mProjection
							.toScreenLocation(naviLatLng2LatLng(currentLatLng));
				}else{
					prePoint = mProjection
							.toScreenLocation(naviLatLng2LatLng(mPathLatLngs.get(i - 1)));
				}
				Point makePoint = new Point(
						(int)(prePoint.x + (pathPoint.x - prePoint.x)*((distance-div)/distance)),
						(int)(prePoint.y + (pathPoint.y - prePoint.y)*((distance-div)/distance)));
				points.add(makePoint);
				Log.e("factory",  
						"mCurrentIndex :" + mCurrentIndex +"\n"
						+"points : "+points +"\n"+ "PathLatLngs : " + mPathLatLngs +"\n"+ "=======================");
				return;
			}else{
				points.add(pathPoint);
			}
		}
		return;
	
//		float totalLength = 0;
//		mIsRedLine.clear();
//		points.clear();
//		Point currentScreenPoint = mProjection.toScreenLocation(naviLatLng2LatLng(currentLatLng));
//		points.add(currentScreenPoint);
//		for (int i = mCurrentIndex; i < mPathLatLngs.size(); i++) {
//			Point pathPoint = mProjection
//					.toScreenLocation(naviLatLng2LatLng(mPathLatLngs.get(i)));
//			if (i == mCurrentIndex) {
//				Log.e("ldy", "             当前位置："+currentScreenPoint);
//				Log.e("ldy", "下一个形状点位置："+pathPoint);
//				Log.e("ldy", "=======================================");
//				if(currentScreenPoint.equals(pathPoint)){
//					continue;
//				}
//				this.mCurPoint2FirstPointDist = AMapUtils.calculateLineDistance(
//						naviLatLng2LatLng(currentLatLng),
//						naviLatLng2LatLng(mPathLatLngs.get(i)));
//				totalLength += mCurPoint2FirstPointDist;
//			} else {
//				if(pathPoint.equals(mProjection
//					.toScreenLocation(naviLatLng2LatLng(mPathLatLngs.get(i-1))))){
//					continue;
//				}
//				float distance = AMapUtils.calculateLineDistance(
//						naviLatLng2LatLng(mPathLatLngs.get(i - 1)),
//						naviLatLng2LatLng(mPathLatLngs.get(i)));
//				totalLength += distance;
//			}
//			mIsRedLine.add(AMapUtils.calculateLineDistance(
//					naviLatLng2LatLng(mPathLatLngs.get(i - 1)),
//					naviLatLng2LatLng(mPathLatLngs.get(i)))<=40);
//			//set the boolean for current i is or not last one
//			if(i==mPathLatLngs.size()-1){
//				mIsLastPoint  = true;
//			}else{
//				mIsLastPoint  = false;
//			}
//			points.add(pathPoint);
//			if (totalLength >= HUDWAY_LENGTH_IN_SCREEN || points.size()>=10) {
//				return;
//			}
//		}
//		return;
	}
	
	private LatLng naviLatLng2LatLng(NaviLatLng naviLatLng) {
		return naviLatLng==null ? null:new LatLng(naviLatLng.getLatitude(), naviLatLng.getLongitude());
	}

	public int setCurrentIndex(int curPoint, int curStep) {
		mCurrentIndex = 0;
		for (int i = 0; i < curStep; i++) {
			mCurrentIndex += mCroodsInSteps.get(i);
		}
		mCurrentIndex += curPoint +1;
		Log.i("CurPoint", mCurrentIndex + "");
		if (mCurrentIndex >= mPathLatLngs.size()) {
			mCurrentIndex = mPathLatLngs.size() - 1;
		}
		
		return mCurrentIndex;
	}

	public List<NaviLatLng> getMPathLatLngs() {
		return mPathLatLngs;
	}
}
