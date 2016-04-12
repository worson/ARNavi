package com.haloaitestarwaymatrix;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
