package com.haloaitestarwaymatrix;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private TestMatrixView mMatrixView;
	private FrameLayout mFl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mFl = (FrameLayout) findViewById(R.id.matrix_view_container);
		mMatrixView = new TestMatrixView(this);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mMatrixView.setLayoutParams(layoutParams);
		mFl.addView(mMatrixView);
	}
	
	public void drawPath(View v){
		mMatrixView.drawPath();
	}
	
	/**
	 * 测试create bitmap 来旋转一个bitmap
	 * @param v
	 */
	public void testCreateBitmap(View v){/*
		try{
		String strPoints = "475.0,540.0,487.0,300.0,475.0,162.0,475.0,-1188.0,313.0,-1308.0,-833.0,-1350.0,-1235.0,-1350.0," +
				"-2117.0,-1392.0,-2483.0,-1272.0,-2819.0,-444.0,-2903.0,-222.0,-3485.0,1182.0,-3575.0,1386.0";
		String[] pointsXYs = strPoints.split(",");
		
		List<Point> mPoints = new ArrayList<Point>();
		for (int i=0;i<pointsXYs.length ; i++) {
			int x = (int) Double.parseDouble(pointsXYs[i]);
			i++;
			int y = (int) Double.parseDouble(pointsXYs[i]);
			Point point = new Point(x, y);
			mPoints .add(point);
		}
		
		Path path = new Path();
		for (int i=0;i<mPoints.size();i++) {
			Point point = mPoints.get(i);
			if(i==0){
				path.moveTo(point.x,point.y);
			}else{
				path.lineTo(point.x,point.y);
			}
		}
		
		int width = 3000;
		int height = 3000;
		Bitmap mBufferBmp = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_4444);
		Canvas bmpCanvas = new Canvas(mBufferBmp);
		
		Paint paint = new Paint();
		paint.setColor(Color.GRAY);
		bmpCanvas.drawPaint(paint);
		
		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(70);
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.RED);
		paint.setAntiAlias(true);
		bmpCanvas.drawPath(path, paint);
		
		Bitmap mBufferBmp2 = Bitmap.createBitmap(mBufferBmp, 0, 0, width,height, getRotateMatrix(width,height), true);  
		
		ImageView view = new ImageView(this);
		view.setImageBitmap(mBufferBmp2);
		mFl.addView(view);
		}catch(Exception e){
			Toast.makeText(this, e.getMessage(), 1).show();
		}
	*/}
	
	public void pauseOrPlay(View v){
		mMatrixView.setIsPause();
		Button btn = (Button) v;
		String text = (String) btn.getText();
		if(text.equals("继续")){
			btn.setText("暂停");
		}else{
			btn.setText("继续");			
		}
	}
	
	/**
	 * 测试掉头引发的路线覆盖问题
	 * @param v
	 */
	public void diaotouTest(View v){
		mMatrixView.drawDiaotou();
	}
	
	/**
	 * 测试大回环引发的路线覆盖问题
	 * @param v
	 */
	public void dahuihuanTest(View v){
		mMatrixView.drawDahuihuan();		
	}
	
	/**
	 * 测试路线错乱闪动问题
	 * @param v
	 */
	public void cuoluanTest(View v){
		mMatrixView.drawCuoluanTest();				
	}
	
	public void cuoluanHudProject(View v){
		mMatrixView.drawCuoluanHudProject();						
	}

	
	private Matrix getRotateMatrix(int width, int height) {
		Camera camera = new Camera();
		camera.save();
		camera.rotateX(50);
		Matrix matrix = new Matrix();
		camera.getMatrix(matrix);
		
//		matrix.preTranslate(-mPathPoint[0].x, -mPathPoint[0].y);
//		matrix.postTranslate(mPathPoint[0].x, mPathPoint[0].y);
		matrix.preTranslate(-width/2, -height);
		matrix.postTranslate(width/2, height);
		
		camera.restore();
	
		//Matrix{[0.5, -0.1701389, 103.7847][0.0, 0.16825575, 202.36395][0.0, -4.3402778E-4, 0.7647569]}
		return matrix;
	}
}
