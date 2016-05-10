package com.haloaitestarwaymatrix;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Op;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class TestMatrixView extends SurfaceView  implements SurfaceHolder.Callback {
	
	private static final String TAG = TestMatrixView.class.getName();

	private final static long FPS_TIME = 30;
	private final static float ROTAGE_ANGLE = 50.0f;
	private final static int LINE_WIDTH = 5;
	private final static int PADDING = 10;//100;
	
	private final static int left = -1000;
	private final static int right = 1000;
	private final static int top = -600;
	private final static int bottom = 1000;
	
	private HudwayFlushThread mHudwayFlushThread;
	private Rect mViewRect;
	private Rect mVisibleRect;
	private Point[] mPathPoints;
	private Point[] mPathPoints2;
	private Bitmap mBufferBmp;
	private Bitmap mBufferBmp2;
	private Rect mBufferBmpRect;
	
	private final static int xTurnOffset1 = 20; 
	private final static int yTurnOffset1 = 100;
	private Matrix mMatrixCanvasHud;
	private Matrix mMatrixCanvasTest;
	private boolean mIsPause =true;
//	private int xTurnBeginPt1;
//	private int yTurnBeginPt1;

	public TestMatrixView(Context context) {
		super(context);
		SurfaceHolder holder = this.getHolder();
		holder.setFormat(PixelFormat.TRANSPARENT);
		holder.addCallback(this);
		setFocusableInTouchMode(true);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		
		mViewRect = new Rect();
		mViewRect.left = this.getLeft();
		mViewRect.right = this.getRight();
		mViewRect.top = this.getTop();
		mViewRect.bottom = this.getBottom();
		
		mVisibleRect = new Rect();
		mVisibleRect.left = this.getLeft() + PADDING;
		mVisibleRect.right = this.getRight() - PADDING;
		mVisibleRect.top = this.getTop() + PADDING;
		mVisibleRect.bottom = this.getBottom() - PADDING;
		
		mBufferBmpRect = new Rect();
		mBufferBmpRect.left = 0;
		mBufferBmpRect.top = 0;
		mBufferBmpRect.right = mVisibleRect.width();
		mBufferBmpRect.bottom = mVisibleRect.height();//(int) this.getSourceHeightWithTargetHeightAndRotateAngle(mVisibleRect.height(), ROTAGE_ANGLE);//
		
//		xTurnBeginPt1 = mBufferBmpRect.left + xTurnOffset1;
//		yTurnBeginPt1 = mBufferBmpRect.top + yTurnOffset1;
		
		mBufferBmp = Bitmap.createBitmap(mBufferBmpRect.width(), mBufferBmpRect.height(), Bitmap.Config.ARGB_4444);
		mBufferBmp2 = Bitmap.createBitmap(mBufferBmpRect.width()*2, mBufferBmpRect.height()*2, Bitmap.Config.ARGB_4444);

//		mPathPoints = new Point[]{
//				new Point(mBufferBmpRect.centerX(), mBufferBmpRect.bottom), 
//				new Point(mBufferBmpRect.centerX(), mBufferBmpRect.top + yTurnOffset1),
//				new Point(mBufferBmpRect.centerX()-xTurnOffset1, mBufferBmpRect.top),
//				new Point(mBufferBmpRect.centerX()-xTurnOffset1, mBufferBmpRect.bottom + 500)
//				}; 

		mPathPoints = new Point[]{
				new Point(mBufferBmpRect.centerX(), mBufferBmpRect.bottom), 
				new Point(mBufferBmpRect.centerX(), mBufferBmpRect.top),
				new Point(mBufferBmpRect.centerX()-100, mBufferBmpRect.top)
				}; 
//		mPathPoints2 = new Point[]{
//				new Point(mBufferBmpRect.centerX()-200, mBufferBmpRect.bottom), 
//				new Point(mBufferBmpRect.centerX()-200, mBufferBmpRect.top),
//				new Point(mBufferBmpRect.centerX()-300, mBufferBmpRect.top)
//				}; 
		mPathPoints2 = new Point[]{
				new Point(mBufferBmpRect.left, mBufferBmpRect.centerY()),
				new Point(mBufferBmpRect.right, mBufferBmpRect.centerY()),
				new Point(mBufferBmpRect.right, mBufferBmpRect.centerY() + 100)
		};

		mHudwayFlushThread = new HudwayFlushThread(holder);
		mHudwayFlushThread.setRunning(true);
		mHudwayFlushThread.start();

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mHudwayFlushThread.setRunning(false);
	}

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
				try {
					Canvas can = null;
					can = surfaceHolder.lockCanvas(null);
					synchronized (surfaceHolder) {
						if (mRunning && can != null) {
							startTime=System.currentTimeMillis();
							can.save();
							doDraw(can,true);
							can.restore();
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
				} catch(Throwable ex) {
					ex.printStackTrace();
				}
			}
		}

		private void doDraw(Canvas canvas,boolean isTest){
			Paint paint = new Paint();
			paint.setColor(Color.BLACK);
			paint.setStrokeWidth(LINE_WIDTH);
			paint.setStyle(Paint.Style.STROKE);
			paint.setAntiAlias(true);
			canvas.drawPaint(paint);
			Path path = new Path();
			for (int i=0;i<mPoints.size();i++) {
				Point point = mPoints.get(i);
				if(!check(point)){
					break;
				}
				if(i==0){
					path.moveTo(point.x,point.y+mCurrentIndex*2);
				}else{
					path.lineTo(point.x,point.y+mCurrentIndex*2);
				}
			}
			if(mIsPause){
			}else{
				mCurrentIndex+=5;				
			}
			paint.setColor(Color.RED);
//			if(mMatrixCanvasHud!=null){
//				canvas.setMatrix(mMatrixCanvasHud);
//			}
			canvas.drawColor(Color.YELLOW);
			canvas.drawBitmap(flushBitmap(path), mVisibleRect.left, mVisibleRect.bottom - mBufferBmpRect.height(),null);
//			canvas.drawBitmap(flushBitmap(path), getRotateMatrix(), paint);
		}
		
		private boolean check(Point point) {
//			if(point.x < left || point.x > right/* || point.y+mCurrentIndex*2 < top 
//					|| point.y+mCurrentIndex*2 > bottom*/){
//				return false;
//			}
			return true;
		}

		private Bitmap flushBitmap(Path path) {
			Canvas bmpCanvas = new Canvas(mBufferBmp);
			
//			if(mMatrixCanvasTest!=null){
//				bmpCanvas.setMatrix(mMatrixCanvasTest);
//			}
			
			//TODO set matrix
//			bmpCanvas.setMatrix(getRotateMatrix());
			Paint paint = new Paint();
			paint.setColor(Color.DKGRAY);
			bmpCanvas.drawPaint(paint);
			paint.setAntiAlias(true);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(LINE_WIDTH);
			paint.setColor(Color.BLACK);
			
			
			if(!mRectPath.isEmpty()){
				//TODO 最后绘制路线
				bmpCanvas.drawPath(mRectPath,paint);
				//create 100,100 bitmap
				Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
				int width = mBufferBmpRect.width();
				int height =  mBufferBmpRect.height();
				
				
				Bitmap target = Bitmap.createBitmap(width, height, bitmap.getConfig());
				Canvas temp_canvas = new Canvas(target);
				
				//TODO 做截取操作
				Path bitmap_path = new Path();
				bitmap_path.moveTo(mPoints.get(0).x-bitmap.getWidth()/2+LINE_WIDTH/2,mPoints.get(0).y-bitmap.getHeight()+LINE_WIDTH/2);
				bitmap_path.lineTo(mPoints.get(0).x+bitmap.getWidth()/2-LINE_WIDTH/2,mPoints.get(0).y-bitmap.getHeight()+LINE_WIDTH/2);
				bitmap_path.lineTo(mPoints.get(0).x+bitmap.getWidth()/2-LINE_WIDTH/2,mPoints.get(0).y-LINE_WIDTH/2);
				bitmap_path.lineTo(mPoints.get(0).x-bitmap.getWidth()/2+LINE_WIDTH/2,mPoints.get(0).y-LINE_WIDTH/2);
				bitmap_path.close();
				if(bitmap_path.op(mRectPath,Op.INTERSECT)){
					paint.setStyle(Paint.Style.FILL);
					temp_canvas.drawPath(bitmap_path, paint);
					paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
					temp_canvas.drawBitmap(bitmap,mPoints.get(0).x-bitmap.getWidth()/2,mPoints.get(0).y-bitmap.getHeight(), paint);		
//					bmpCanvas.drawPath(bitmap_path, paint);
//					bmpCanvas.drawBitmap(target, mPoints.get(0).x-width/2,mPoints.get(0).y-height, paint);
				}else{
					temp_canvas.drawBitmap(bitmap,mPoints.get(0).x-bitmap.getWidth()/2,mPoints.get(0).y-bitmap.getHeight(), null);		
//					bmpCanvas.drawBitmap(target, mPoints.get(0).x-width/2,mPoints.get(0).y-height, paint);
				}
				
				//TODO 去除Xfermode
				paint.setXfermode(null);
				bmpCanvas.drawBitmap(target, 0,0, paint);
				
				paint.setStrokeWidth(LINE_WIDTH);
				paint.setStyle(Paint.Style.STROKE);
				//TODO
				//TODO 先绘制需要被遮盖的底图
//				Matrix matrix = new Matrix();
//				matrix.preSkew(1f, 1f,mPoints.get(0).x,mPoints.get(0).y);
//				bmpCanvas.setMatrix(matrix);
				paint.setColor(Color.BLACK);
				
			}else{
				//TODO 最后绘制路线保证路线不会被覆盖
				paint.setStrokeWidth(LINE_WIDTH);
				paint.setColor(Color.RED);
				bmpCanvas.drawPath(path, paint);
			}
			//TODO 最后绘制路线保证路线不会被覆盖
			paint.setStrokeWidth(LINE_WIDTH);
			paint.setColor(Color.RED);
			bmpCanvas.drawPath(path, paint);
			
//			paint.setColor(Color.YELLOW);
//			paint.setStrokeWidth(10);
//			for(int i=0;i<mPoints.size();i++){
//				bmpCanvas.drawPoint(mPoints.get(i).x, mPoints.get(i).y, paint);
//			}
//			
			paint.setColor(Color.RED);
			paint.setStrokeWidth(10);
			for(int i=0;i<mLeftPoints.size();i++){
				bmpCanvas.drawPoint(mLeftPoints.get(i).x, mLeftPoints.get(i).y, paint);
			}
//			
//			paint.setColor(Color.BLUE);
//			paint.setStrokeWidth(10);
//			for(int i=0;i<mRightPoints.size();i++){
//				bmpCanvas.drawPoint(mRightPoints.get(i).x, mRightPoints.get(i).y, paint);
//			}
			
			//在draw完再设置matrix是无效的
//			paint.setStrokeWidth(3);
//			paint.setStyle(Paint.Style.FILL);
//			paint.setColor(Color.WHITE);
//			paint.setTextSize(50);
			//TODO draw text
//			for(int i=0;i<mPoints.size();i++){
//				//mPoints.get(i).x+","+mPoints.get(i).y
//				bmpCanvas.drawText(mPoints.get(i).x+" : "+(mPoints.get(i).y+mCurrentIndex*2), mPoints.get(i).x, mPoints.get(i).y+mCurrentIndex*2, paint);
//			}
			
//			Canvas bmp2Canvas = new Canvas(mBufferBmp2);
//			bmp2Canvas.drawBitmap(mBufferBmp, getRotateMatrix(), new Paint());
			
//			if(mBufferBmp.getWidth() > 0 && mBufferBmp.getHeight() > 0){
//				mBufferBmp2 = Bitmap.createBitmap(mBufferBmp, 0, 0, 700,350, getRotateMatrix(), true);
//			}
			
			return mBufferBmp;
		}

		private void doDraw(Canvas canvas) {

			//Fill the background as black
			Paint paint = new Paint();
			paint.setColor(Color.BLACK);
			canvas.drawPaint(paint);

			//Draw the visible frame.
			paint.setStrokeWidth(LINE_WIDTH);
			paint.setStyle(Paint.Style.STROKE);
			paint.setAntiAlias(true);
			paint.setColor(Color.GREEN);
			canvas.drawRect(mVisibleRect, paint);
			
			Bitmap bitmap = flushBitmap();

			Matrix matrix = getRotateMatrix();
			try {
				canvas.setMatrix(matrix);
			} catch (Throwable t) {
				t.printStackTrace();
			}
			canvas.drawBitmap(bitmap, mVisibleRect.left, mVisibleRect.bottom - mBufferBmpRect.height(), null);
//			canvas.drawColor(Color.YELLOW);
		}
		
		private Bitmap flushBitmap() {
			Canvas bmpCanvas = new Canvas(mBufferBmp);
			Paint paint = new Paint();
			paint.setColor(Color.GRAY);
			bmpCanvas.drawPaint(paint);
			
			Matrix matrix = new Matrix();
			matrix.setTranslate(100, 0);

			float[] pts = points2Floats(mPathPoints);
//			matrix.mapPoints(pts);

			float[] pts2 = points2Floats(mPathPoints2);
//			matrix.mapPoints(pts2);

			//
			paint.setStrokeWidth(LINE_WIDTH);
			paint.setStyle(Paint.Style.STROKE);
			paint.setAntiAlias(true);
			paint.setColor(Color.BLUE);

			//Construct the path1
			Path path1 = new Path();
			path1.moveTo(pts[0], pts[1]);
			for (int i=0; i<mPathPoints.length-1; i++) {
				path1.lineTo(pts[(i + 1) * 2], pts[(i + 1) * 2 + 1]);
			}
			path1.close();
			//Draw the path1
//			bmpCanvas.drawPath(path1, paint);
			
			//Construct the path2
			Path path2 = new Path();
			path2.moveTo(pts2[0], pts2[1]);
			for (int i=0; i<mPathPoints2.length-1; i++) {
				path2.lineTo(pts2[(i + 1) * 2], pts2[(i + 1) * 2 + 1]);
			}
			path2.close();
			path2.addPath(path1);
			//Draw the path2
//			bmpCanvas.drawPath(path2, paint);
			
			//Draw the path op.
			if (path2.op(path1, Op.REVERSE_DIFFERENCE)) {
				if (!path2.isEmpty()) {
					paint.setColor(Color.RED);
					bmpCanvas.drawPath(path2, paint);
				} else {
					Log.e(TAG, "Path2 is empty");
				}
			}
		
			return mBufferBmp;
		}
		
		private float[] points2Floats(Point[] points) {

			float[] pointsXY = new float[points.length * 2];
			for (int i = 0; i < pointsXY.length; i++) {
				if (i % 2 == 0) {
					pointsXY[i] = points[i/2].x;
				} else {
					pointsXY[i] = points[i/2].y;
				}
			}
			
			return pointsXY;
		}

	}
	
	public void setIsPause(){
		mIsPause = !mIsPause;
	}
	
	private Matrix getRotateMatrix() {
		Camera camera = new Camera();
		camera.save();
		camera.rotateX(ROTAGE_ANGLE);
		Matrix matrix = new Matrix();
		camera.getMatrix(matrix);
		
//		matrix.preTranslate(-mPathPoint[0].x, -mPathPoint[0].y);
//		matrix.postTranslate(mPathPoint[0].x, mPathPoint[0].y);
		matrix.preTranslate(-mVisibleRect.centerX(), -mVisibleRect.bottom);
		matrix.postTranslate(mVisibleRect.centerX(), mVisibleRect.bottom);
		
		camera.restore();
	
		//Matrix{[0.5, -0.1701389, 103.7847][0.0, 0.16825575, 202.36395][0.0, -4.3402778E-4, 0.7647569]}
		return matrix;
	}
	
	private Matrix getNormalMatrix(){
		return new Matrix();
	}
	
	//用于存放用于绘制路线的点的集合
	List<Point> mPoints = new ArrayList<Point>();
	List<Point> mPointsPath = new ArrayList<Point>();
	//代表的是当前路线行走的距离，此值增大则拉近摄像机
	int mCurrentIndex = 0;

	private List<Path> mPaths = new ArrayList<Path>();

	private Path mRectPath = new Path();

	private List<Point> mLeftPoints  = new ArrayList<Point>();
	private List<Point> mRightPoints = new ArrayList<Point>();
	
	/**
	 * 修改用于绘制的数组
	 */
	public void drawCuoluanHudProject() {
		mPoints.clear();
		
		String strPoints = "475.0,540.0,487.0,300.0,475.0,162.0,475.0,-1188.0,313.0,-1308.0,-833.0,-1350.0,-1235.0,-1350.0," +
				"-2117.0,-1392.0,-2483.0,-1272.0,-2819.0,-444.0,-2903.0,-222.0,-3485.0,1182.0,-3575.0,1386.0";
		String[] pointsXYs = strPoints.split(",");
		int offsetX = mVisibleRect.centerX() - (int) Double.parseDouble(pointsXYs[0]);
		int offsetY = mVisibleRect.bottom - (int) Double.parseDouble(pointsXYs[1]);
		
		for (int i=0;i<pointsXYs.length ; i++) {
			int x = (int) Double.parseDouble(pointsXYs[i]);
			i++;
			int y = (int) Double.parseDouble(pointsXYs[i]);
			Point point = new Point(x+offsetX, y+offsetY);
			mPoints.add(point);
		}
		
		mCurrentIndex = 0;
		
		mMatrixCanvasTest = getRotateMatrix();
		mMatrixCanvasHud=null;
	}
	public void drawCuoluanTest() {
		mPoints.clear();
		
		String strPoints = "475.0,540.0,487.0,300.0,475.0,162.0,475.0,-1188.0,313.0,-1308.0,-833.0,-1350.0,-1235.0,-1350.0," +
				"-2117.0,-1392.0,-2483.0,-1272.0,-2819.0,-444.0,-2903.0,-222.0,-3485.0,1182.0,-3575.0,1386.0";
		String[] pointsXYs = strPoints.split(",");
		int offsetX = mVisibleRect.centerX() - (int) Double.parseDouble(pointsXYs[0]);
		int offsetY = mVisibleRect.bottom - (int) Double.parseDouble(pointsXYs[1]);
		
		for (int i=0;i<pointsXYs.length ; i++) {
			int x = (int) Double.parseDouble(pointsXYs[i]);
			i++;
			int y = (int) Double.parseDouble(pointsXYs[i]);
			Point point = new Point(x+offsetX, y+offsetY);
			mPoints.add(point);
		}
		
		mCurrentIndex = 0;
		
		mMatrixCanvasHud = getRotateMatrix();
		mMatrixCanvasTest=null;
	}

	/**
	 * 修改用于绘制的数组
	 */
	public void drawDahuihuan() {
		mRectPath.reset();
		
		mPoints.clear();
		mPoints.add(new Point(mVisibleRect.centerX(), mVisibleRect.bottom-20));
		mPoints.add(new Point(mVisibleRect.centerX(), mVisibleRect.bottom-400));
		
		mPoints.add(new Point(mVisibleRect.centerX()+10,mVisibleRect.bottom-500));
		mPoints.add(new Point(mVisibleRect.centerX()+30,mVisibleRect.bottom-580));
		mPoints.add(new Point(mVisibleRect.centerX()+70,mVisibleRect.bottom-620));
		mPoints.add(new Point(mVisibleRect.centerX()+110,mVisibleRect.bottom-590));
		mPoints.add(new Point(mVisibleRect.centerX()+125,mVisibleRect.bottom-560));
		mPoints.add(new Point(mVisibleRect.centerX()+130,mVisibleRect.bottom-530));
		mPoints.add(new Point(mVisibleRect.centerX()+133,mVisibleRect.bottom-500));
		mPoints.add(new Point(mVisibleRect.centerX()+128,mVisibleRect.bottom-470));
		mPoints.add(new Point(mVisibleRect.centerX()+124,mVisibleRect.bottom-440));
		mPoints.add(new Point(mVisibleRect.centerX()+110,mVisibleRect.bottom-400));
		
		mPoints.add(new Point(mVisibleRect.centerX()-300, mVisibleRect.bottom-400));
		
		//points2path();
		
		mCurrentIndex = 0;
	}
	
	public void drawPath(){
		points2path();
		mCurrentIndex = 0;
	}

	private void points2path() {
		/**
		A(a,b) B(m,n) BC = L
		
		x1= m - (b-n) /√[(a-m)^2+(b-n)^2]
		x2= m + (b-n) /√[(a-m)^2+(b-n)^2]
		y1 = n + L(a-m) /√[(a-m)^2+(b-n)^2]
		y2 = n - L(a-m) /√[(a-m)^2+(b-n)^2]
		 */
		//获取所有计算后的点的集合
		
		
		int path_width = 30;
		
		//获取一侧点的集合
		mLeftPoints.clear();
		for(int i=0;i<mPoints.size();i++){
			Point currentPoint = mPoints.get(i);
			Point secondPoint;
			int m = currentPoint.x;
			int n = currentPoint.y;
			if(i==mPoints.size()-1){
				secondPoint= mPoints.get(i-1);
			}else{
				secondPoint= mPoints.get(i+1);
			}
			int a;
			int b;
			a = secondPoint.x;
			b = secondPoint.y;
			
			int x;
			int y;
			
//			x = (int) (m - (b-n) / Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));
//			y = (int) (n + 50*(a-m) /Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));
			
			/**
			C1(x,y) c2(x3,y3) A(x2,y2) B(x1,y1) BC=a
			
			x=x1-a*sin{arctan[(y2-y1)/(x2-x1)]}
			y=y1+a*cos{arctan[(y2-y1)/(x2-x1)]}
			x3=x1+a*sin{arctan[(y2-y1)/(x2-x1)]} 
			y3=y1- a*cos{arctan[(y2-y1)/(x2-x1)]}
			 */
			
			//x1,y1为当前点，x2,y2为下一个点
			//m,n为B，a,b为A
			int x1=m,y1=n;
			int x2=a,y2=b;
			if(y2==y1){
				x = (int) (m - (b-n) / Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));
				y = (int) (n + (path_width/2)*(a-m) /Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));
			}else if(x2==x1){
				x = x1+(path_width/2);
				y = y1;
			}else if((x2<x1 && y2>y1) || (x2<x1 && y2<y1)){
				x=(int) (x1+(path_width/2)*Math.sin(Math.atan((y2-y1)/(x2-x1))));
				y=(int) (y1-(path_width/2)*Math.cos(Math.atan((y2-y1)/(x2-x1))));				
			}else{
				x=(int) (x1-(path_width/2)*Math.sin(Math.atan((y2-y1)/(x2-x1))));
				y=(int) (y1+(path_width/2)*Math.cos(Math.atan((y2-y1)/(x2-x1))));
			}
			
			Point point = new Point(x, y);
			
			//如果不是第一个或者最后一个，那么需要取该点和该点的前一个点继续运算得到x,y，然后取中间值
			if(i!=0 && i!=mPoints.size()-1){
				secondPoint = mPoints.get(i-1);
				a=secondPoint.x;
				b=secondPoint.y;
				x1=m;
				y1=n;
				x2=a;
				y2=b;
				if(y2==y1){
					x = (int) (m + (b-n) / Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));
					y = (int) (n - (path_width/2)*(a-m) /Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));
				}else if(x2==x1){
					x = x1+(path_width/2);
					y = y1;
				}else if((x2<x1 && y2>y1) || (x2<x1 && y2<y1)){
					x=(int) (x1-(path_width/2)*Math.sin(Math.atan((y2-y1)/(x2-x1))));
					y=(int) (y1+(path_width/2)*Math.cos(Math.atan((y2-y1)/(x2-x1))));				
				}else{
					x=(int) (x1+(path_width/2)*Math.sin(Math.atan((y2-y1)/(x2-x1))));
					y=(int) (y1-(path_width/2)*Math.cos(Math.atan((y2-y1)/(x2-x1))));
				}
				point.x = (point.x+x)/2;
				point.y = (point.y+y)/2;
			}
			mLeftPoints.add(point);
			
		}
		
		//获取另一侧点的集合
		mRightPoints.clear();
		for(int i=0;i<mPoints.size();i++){
			Point currentPoint = mPoints.get(i);
			Point secondPoint;
			int m = currentPoint.x;
			int n = currentPoint.y;
			if(i==mPoints.size()-1){
				secondPoint= mPoints.get(i-1);
			}else{
				secondPoint= mPoints.get(i+1);
			}
			int a;
			int b;
			a = secondPoint.x;
			b = secondPoint.y;
			
			int x;
			int y;
			
//			x = (int) (m + (b-n) / Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));
//			y = (int) (n - 50*(a-m) /Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));
			
			int x1=m,y1=n;
			int x2=a,y2=b;
			if(y2==y1){
				x = (int) (m + (b-n) / Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));
				y = (int) (n - (path_width/2)*(a-m) /Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));
			}else if(x2==x1){
				x = x1-(path_width/2);
				y = y1;
			}else if((x2<x1 && y2>y1) || (x2<x1 && y2<y1)){
				x=(int) (x1-(path_width/2)*Math.sin(Math.atan((y2-y1)/(x2-x1))));
				y=(int) (y1+(path_width/2)*Math.cos(Math.atan((y2-y1)/(x2-x1))));				
			}else{
				x=(int) (x1+(path_width/2)*Math.sin(Math.atan((y2-y1)/(x2-x1))));
				y=(int) (y1-(path_width/2)*Math.cos(Math.atan((y2-y1)/(x2-x1))));
			}
			
			Point point = new Point(x, y);
			
			//如果不是第一个或者最后一个，那么需要取该点和该点的前一个点继续运算得到x,y，然后取中间值
			if(i!=0 && i!=mPoints.size()-1){
				secondPoint = mPoints.get(i-1);
				a=secondPoint.x;
				b=secondPoint.y;
				x1=m;
				y1=n;
				x2=a;
				y2=b;
				if(y2==y1){
					x = (int) (m - (b-n) / Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));
					y = (int) (n + (path_width/2)*(a-m) /Math.sqrt(Math.pow((a-m),2)+Math.pow((b-n),2)));
				}else if(x2==x1){
					x = x1-(path_width/2);
					y = y1;
				}else if((x2<x1 && y2>y1) || (x2<x1 && y2<y1)){
					x=(int) (x1+(path_width/2)*Math.sin(Math.atan((y2-y1)/(x2-x1))));
					y=(int) (y1-(path_width/2)*Math.cos(Math.atan((y2-y1)/(x2-x1))));				
				}else{
					x=(int) (x1-(path_width/2)*Math.sin(Math.atan((y2-y1)/(x2-x1))));
					y=(int) (y1+(path_width/2)*Math.cos(Math.atan((y2-y1)/(x2-x1))));
				}
				point.x = (point.x+x)/2;
				point.y = (point.y+y)/2;
			}
			mRightPoints.add(point);
		}
		
		//TODO 由于最后一个点的坐标是反向计算出来的，因此它的left和right是反的，在此做交换处理
		Point temp = mLeftPoints.remove(mLeftPoints.size()-1);
		mLeftPoints.add(mRightPoints.remove(mRightPoints.size()-1));
		mRightPoints.add(temp);
		
		mPointsPath.clear();
		mPointsPath.addAll(mLeftPoints);
		mPointsPath.addAll(mRightPoints);
		
		//将点集合转成成矩形Path
		mRectPath.reset();
		Point point = mPointsPath.get(0);
		mRectPath.moveTo(point.x,point.y);
		for(int i=1;i<mPointsPath.size();i++){
			if(i<mLeftPoints.size()){
				point = mLeftPoints.get(i);
			}else{
				point = mRightPoints.get(mRightPoints.size()-(i-mLeftPoints.size()+1));
			}
			mRectPath.lineTo(point.x, point.y);
		}
		
		
		//将点集合转换成Path集合，Path集合个数为原始点的个数减一(此处可表示为left或者right集合长度减一)
		mPaths.clear();
		for(int i=0;i<mLeftPoints.size()-1;i++){
			Path path = new Path();
			Point leftCurrentPoint = mLeftPoints.get(i);
			Point leftNextPoint = mLeftPoints.get(i+1);
			Point rightCurrentPoint = mRightPoints.get(i);
			Point rightNextPoint = mRightPoints.get(i+1);
			path.moveTo(leftCurrentPoint.x,leftCurrentPoint.y);
			path.lineTo(leftNextPoint.x,leftNextPoint.y);
			path.lineTo(rightNextPoint.x,rightNextPoint.y);
			path.lineTo(rightCurrentPoint.x,rightCurrentPoint.y);
			path.close();
			mPaths.add(path);
		}
	}

	/**
	 * 修改用于绘制的数组
	 */
	public void drawDiaotou() {
		mPoints.clear();
		mPoints.add(new Point(mVisibleRect.centerX(), mVisibleRect.bottom));
		mPoints.add(new Point(mVisibleRect.centerX(), mVisibleRect.bottom-300));
		
		mPoints.add(new Point(mVisibleRect.centerX()-10, mVisibleRect.bottom-320));
		mPoints.add(new Point(mVisibleRect.centerX()-20, mVisibleRect.bottom-330));
		mPoints.add(new Point(mVisibleRect.centerX()-30, mVisibleRect.bottom-330));
		mPoints.add(new Point(mVisibleRect.centerX()-40, mVisibleRect.bottom-320));
		
		mPoints.add(new Point(mVisibleRect.centerX()-50, mVisibleRect.bottom-300));
		mPoints.add(new Point(mVisibleRect.centerX()-50,  mVisibleRect.bottom-180));
		
		mCurrentIndex = 0;
	}
	
	//根据最终高度得到旋转rotateAngle角度，所需要的原始高度 hSrc = hTarget/cos(rotateAngle)
	private float getSourceHeightWithTargetHeightAndRotateAngle(float targetHeight, float rotateDegree) {
		float radian = degree2Radian(rotateDegree);
		double cos = Math.cos(radian);
		float sourceHeight = (float) ((double)targetHeight / cos);
		return sourceHeight;//(float) (targetHeight / degree2Radian(rotateDegree));
	}
	
	//角度转弧度，Math三角函数以弧度为参数
	private float degree2Radian(float degree) {
		return (float) (Math.PI * (degree / 180.0f));
	}
	
	private Path getPathOfLine(Point startPoint, Point endPoint, int stokeWidth) {
		Path targetPath = new Path();
		
		return targetPath;
	}
}
