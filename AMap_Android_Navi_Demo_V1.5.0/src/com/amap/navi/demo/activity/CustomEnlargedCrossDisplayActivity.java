package com.amap.navi.demo.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.navi.demo.R;

/**
 * 创建时间：11/10/15 16:08
 * 项目名称：newNaviDemo
 *
 * @author lingxiang.wang
 * @email lingxiang.wang@alibaba-inc.com
 * 类说明：
 */

public class CustomEnlargedCrossDisplayActivity extends BaseActivity implements AMapNaviListener, OnMapLoadedListener, OnCameraChangeListener {

    private ImageView myCustomEnlargedCross;
	public static long mCrossImageCreateTime = 0l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_custom_enlarged_cross);
        naviView = (AMapNaviView) findViewById(R.id.navi_view);
        naviView.onCreate(savedInstanceState);
		aMap = this.naviView.getMap();
        aMap.setOnMapLoadedListener(this);
        aMap.setOnCameraChangeListener(this);
        aMap.showMapText(false);
        
        myCustomEnlargedCross = (ImageView) findViewById(R.id.myEnlargedCross);
        //设置布局完全不可见
        AMapNaviViewOptions viewOptions = naviView.getViewOptions();
        viewOptions.setLayoutVisible(false);
        naviView.setViewOptions(viewOptions);
        
        

    }

	@Override
	public void onMapLoaded() {
		aMap.showMapText(false);
		
	}
	
	public void initAMapNaviView() {

        AMapNaviViewOptions viewOptions = naviView.getViewOptions();
        viewOptions.setNaviNight(true);
        viewOptions.setNaviViewTopic(AMapNaviViewOptions.BLUE_COLOR_TOPIC);
        viewOptions.setCrossDisplayShow(false);
        naviView.setViewOptions(viewOptions);

        AMap aMap = this.naviView.getMap();
        CameraPosition cameraPos = aMap.getCameraPosition();
        float curZoom = cameraPos.zoom;
        float curTilt = cameraPos.tilt;
        float maxZoomLevel = ((int) aMap.getMaxZoomLevel()) / 2 * 2;
        if (curZoom < maxZoomLevel || curTilt != 0.0) {
            cameraPos = CameraPosition.builder(cameraPos).tilt(0).zoom(maxZoomLevel).build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPos);
            aMap.moveCamera(cameraUpdate);
        }
    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {
    	this.mCrossImageCreateTime  = System.currentTimeMillis();
        myCustomEnlargedCross.setImageBitmap(aMapNaviCross.getBitmap());
        try {
			write2(bitmap2Bytes2(aMapNaviCross.getBitmap()),"cross_image_"+mCrossImageCreateTime+".png");
		} catch (IOException e) {
			Log.e("test", "xxxx:"+e.toString());
			e.printStackTrace();
		}

        myCustomEnlargedCross.setVisibility(View.VISIBLE);
        
        mIsFlushFakerCross = true;
        
        if(mHudwayView!=null){
        	mHudwayView.drawWay();
        }
    }
    
    public byte[] bitmap2Bytes2(Bitmap bm) {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	bm.compress(Bitmap.CompressFormat.PNG, 100, baos);//png类型
    	return baos.toByteArray();
    	}

    	// 写到sdcard中
    public void write2(byte[] bs,String filename) throws IOException{
    	File file = new File("/sdcard/testimage/crossimages/"+filename);
    	if(!file.exists()){
    		file.createNewFile();
    	}
    	FileOutputStream out=new FileOutputStream(file);
    	out.write(bs);
    	out.flush();
    	out.close();
    }

    @Override
    public void hideCross() {
        myCustomEnlargedCross.setVisibility(View.INVISIBLE);
        
        if(mHudwayView!=null){
        	mHudwayView.notDrawWay();
        }
    }

	@Override
	public void onCameraChange(CameraPosition arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCameraChangeFinish(CameraPosition arg0) {
		initAMapNaviView();
	}

    
}
