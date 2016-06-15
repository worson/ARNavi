package com.amap.navi.demo.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.NaviInfo;

public class NaviMockGPSGeneratorForRecordGPS {

	private static final String TAG = "NaviMockGPSGenerator";
	private Context context;
	private ArrayList<Object> csvLocationContainer = new ArrayList<Object>();
	private String gpsCVSFileName;
	private boolean isRecordingGps = false;

	public NaviMockGPSGeneratorForRecordGPS(Context context) {
		this.context = context;
	}

	public void startRecordGPS(String gpsFileName) {
		isRecordingGps = true;
		gpsCVSFileName = gpsFileName;
		Toast.makeText(context, "start record", 0).show();
	}

	public void stopRecordGPS() {
		isRecordingGps = false;
		gpsDataCsvFileExport(gpsCVSFileName);
		csvLocationContainer.clear();
		Toast.makeText(context, "stop record", 0).show();
	}

	public void onLocationChanged(AMapNaviLocation location) {
			if (location!=null && isRecordingGps) {
				csvLocationContainer.add(location);
			}
			Log.i("test", "location change");
	}

	public void onNaviInfoUpdate(NaviInfo naviInfo) {
		if (naviInfo != null) {
			if (isRecordingGps) {
				csvLocationContainer.add(naviInfo);
			}
		}
		Log.i("test", "naviinfo update");
	}

	@SuppressLint("SimpleDateFormat") 
	private void gpsDataCsvFileExport(String fileName) {
		try {
			SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
			String tempPath = Environment.getExternalStorageDirectory().getPath()
					+ "/" + "GPS" + timeFormatter.format(System.currentTimeMillis()) + ".csv";
			
			File csv = new File(tempPath);
			// 若存在则删除
			if (csv.exists())
				csv.delete();
			// 创建文件
			csv.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(csv, true));

			for (Object obj : csvLocationContainer) {
				if(obj==null){
					break;
				}
				if (obj instanceof AMapNaviLocation) {
					AMapNaviLocation location = (AMapNaviLocation) obj;
					if(location.getCoord()==null){
						break;
					}
					String timeStamp = timeFormatter.format(System
							.currentTimeMillis());
					String gpsStr = String.format(
							"%d, %.12f, %.12f, %.1f, %.1f, %.1f, %s, 0", 
							2,
							location.getCoord().getLatitude(),
							location.getCoord().getLongitude(), 
							location.getSpeed(), 
							location.getBearing(),
							location.getAccuracy(),
							timeFormatter.format(location.getTime()));
					String gpsStrFinished = gpsStr + "," + timeStamp + "\r\n";
					bw.write(gpsStrFinished);
				} else {
					NaviInfo naviInfo = (NaviInfo) obj;
					if(naviInfo.getCoord()==null){
						break;
					}
					String timeStep = timeFormatter.format(System
							.currentTimeMillis());
					String gpsStr = String.format(
							"%d, %.12f, %.12f, %d, %d, %d, %s, %d, %d, %d, %d, %d, %s, %d, %d, %d, %s, 0", 
							1,
							naviInfo.getCoord().getLatitude(), 
							naviInfo.getCoord().getLongitude(), 
							naviInfo.getCurPoint(), 
							naviInfo.getCurLink(), 
							naviInfo.getCurStep(), 
							naviInfo.getCurrentRoadName(),
							naviInfo.getCurStepRetainDistance(),
							naviInfo.getDirection(),
							naviInfo.getIconType(),
							naviInfo.getLimitSpeed(),
							naviInfo.getNaviType(),
							naviInfo.getNextRoadName(),
							naviInfo.getPathRetainDistance(),
							naviInfo.getPathRetainTime(),
							naviInfo.getServiceAreaDistance(),
							"naviinfo");
					String gpsStrFinished = gpsStr + "," + timeStep + "\r\n";
					bw.write(gpsStrFinished);
				}
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
