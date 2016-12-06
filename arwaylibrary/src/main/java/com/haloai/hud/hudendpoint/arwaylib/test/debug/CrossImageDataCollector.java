package com.haloai.hud.hudendpoint.arwaylib.test.debug;

import android.annotation.SuppressLint;
import android.graphics.Point;

import com.amap.api.navi.model.NaviLatLng;
import com.haloai.hud.hudendpoint.arwaylib.utils.FileUtils;
import com.thoughtworks.xstream.XStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("SdCardPath")
public class CrossImageDataCollector implements INaviDataCollector {
	private XStream xStream = new XStream();
	private NavigationPathData mNaviPathData = null;
	private List<CrossImageData> mCrossImageData = new ArrayList<>();
	private String mCurrentDataTag = null;
	public static final String DEFAULT_FOLDER_PATH = "/sdcard/HaloTest/AINaviData/";
	private String mFolderPath = DEFAULT_FOLDER_PATH;

	public static final String NEXT_LINE_SYMBOL = "\r\n";

	public CrossImageDataCollector() {
		CrossImageData.formatXStream(xStream);
	}

	public void setFolderPath(String folderPath) {
		mFolderPath = folderPath;
	}

	public void startNavigation(List<NaviLatLng> naviPath) {
		if (mNaviPathData != null)
			mNaviPathData = null;

		long systemMill = System.currentTimeMillis();
		mCurrentDataTag = "xmlcrossimagedata_" + systemMill;
		mNaviPathData = new NavigationPathData(xStream);
		mNaviPathData.setNaviPath(naviPath);
	}

	public void addCrossImageData(String bitmapPath, String bitmapFileName, Point centerPoint, int centerPointIndex, List<Point> screenPoints, Point centerNextPoint, int stepIndex){
		if (mCrossImageData == null) {
			mCrossImageData = new ArrayList<>();
		}
		mCrossImageData.add(new CrossImageData(bitmapPath,bitmapFileName,centerPoint,centerPointIndex,screenPoints,centerNextPoint,stepIndex));

	}
	
	public void endNavigation() {
		assert(mNaviPathData != null);
		assert(mCrossImageData != null);

		if (mNaviPathData == null) 
			return;
		if (mCrossImageData == null) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		String xml = xStream.toXML(mNaviPathData);
		sb.append(xml);
		sb.append("\r\n");
		if(mCrossImageData.size()>0){
			sb.append("<crossImages>\r\n");
			for(CrossImageData crossImageData:mCrossImageData){
				xml = xStream.toXML(crossImageData);
				sb.append(xml);
				sb.append(NEXT_LINE_SYMBOL);
			}
			sb.append("</crossImages>\r\n");
		}


		String filename = String.format("%s.xml", mCurrentDataTag);
		String folderPath = mFolderPath;
		if (folderPath == null) {
			folderPath = DEFAULT_FOLDER_PATH;
		}
		try {
			FileUtils.write(sb.toString().getBytes(), folderPath, filename);
		} catch (IOException e) {
			e.printStackTrace();
		}

		clearData();
	}
	
	public NavigationPathData getNaviPathData() {
		return mNaviPathData;
	}
	
	public NavigationPathData getNaviPathData(String xmlNaviData) {
		mNaviPathData = (NavigationPathData) xStream.fromXML(xmlNaviData);
		return mNaviPathData;
	}

	private void clearData(){
		if (mCrossImageData != null) {
			mCrossImageData.clear();
		}

	}

}
