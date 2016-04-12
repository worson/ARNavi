package com.haloaitestarwaymatrix;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Op;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class TestMatrixView extends SurfaceView  implements SurfaceHolder.Callback {
	
	private static final String TAG = TestMatrixView.class.getName();

	private final static long FPS_TIME = 30;
	private final static float ROTAGE_ANGLE = 50.0f;
	private final static int LINE_WIDTH = 70;
	private final static int PADDING = 10;//100;
	
	private HudwayFlushThread mHudwayFlushThread;
	private Rect mViewRect;
	private Rect mVisibleRect;
	private Point[] mPathPoints;
	private Point[] mPathPoints2;
	private Bitmap mBufferBmp;
	private Rect mBufferBmpRect;
	
	private final static int xTurnOffset1 = 20; 
	private final static int yTurnOffset1 = 100;
	private Matrix mMatrixCanvasHud;
	private Matrix mMatrixCanvasTest;
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
				if(i==0){
					path.moveTo(point.x,point.y+mCurrentIndex*2);
				}else{
					path.lineTo(point.x,point.y+mCurrentIndex*2);
				}
			}
			mCurrentIndex+=5;
			paint.setColor(Color.RED);
			if(mMatrixCanvasHud!=null){
				canvas.setMatrix(mMatrixCanvasHud);
			}
			canvas.drawBitmap(flushBitmap(path), mVisibleRect.left, mVisibleRect.bottom - mBufferBmpRect.height(),null);
		}
		
		private Bitmap flushBitmap(Path path) {
			Canvas bmpCanvas = new Canvas(mBufferBmp);
			
			if(mMatrixCanvasTest!=null){
				bmpCanvas.setMatrix(mMatrixCanvasTest);
			}
			
			Paint paint = new Paint();
			paint.setColor(Color.GRAY);
			bmpCanvas.drawPaint(paint);
			
			paint.setColor(Color.BLACK);
			paint.setStrokeWidth(LINE_WIDTH);
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(Color.RED);
			paint.setAntiAlias(true);
			bmpCanvas.drawPath(path, paint);
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
			Log.e(TAG, "matrix1=" + matrix.toString());
//			canvas.save();
			try {
				canvas.setMatrix(matrix);
			} catch (Throwable t) {
				t.printStackTrace();
			}

			canvas.drawBitmap(bitmap, mVisibleRect.left, mVisibleRect.bottom - mBufferBmpRect.height(), null);
			
//			canvas.restore();
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
	
	//用于存放用于绘制路线的点的集合
	List<Point> mPoints = new ArrayList<Point>();
	//代表的是当前路线行走的距离，此值增大则拉近摄像机
	int mCurrentIndex = 0;
	
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
		mPoints.clear();
		mPoints.add(new Point(mVisibleRect.centerX(), mVisibleRect.bottom));
		mPoints.add(new Point(mVisibleRect.centerX(), mVisibleRect.bottom-200));
		
		mPoints.add(new Point(mVisibleRect.centerX()+10,mVisibleRect.bottom-230));
		mPoints.add(new Point(mVisibleRect.centerX()+30, mVisibleRect.bottom-260));
		mPoints.add(new Point(mVisibleRect.centerX()+40, mVisibleRect.bottom-270));
		mPoints.add(new Point(mVisibleRect.centerX()+50, mVisibleRect.bottom-270));
		mPoints.add(new Point(mVisibleRect.centerX()+60, mVisibleRect.bottom-260));
		mPoints.add(new Point(mVisibleRect.centerX()+70, mVisibleRect.bottom-240));
		mPoints.add(new Point(mVisibleRect.centerX()+55, mVisibleRect.bottom-210));
		mPoints.add(new Point(mVisibleRect.centerX()+30, mVisibleRect.bottom-200));
		
		mPoints.add(new Point(mVisibleRect.centerX()-200, mVisibleRect.bottom-200));
		
		mCurrentIndex = 0;
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
