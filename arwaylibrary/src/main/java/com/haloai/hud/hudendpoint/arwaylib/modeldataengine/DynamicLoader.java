package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import android.util.Log;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ylq on 16/10/31.
 */
public class DynamicLoader implements IDynamicLoader {


    static final private int DEFAULTLOADDISTANCE = 1000;// 默认一次加载的道路的道路长度单位：米
    static final private int DEFAULTDANGERDISTANCE = 100;// 默认的预留距离
    private IDynamicLoader.IDynamicLoadNotifer mDynamicLoadNotifer;

    private List<LatLng> mCoordList = new ArrayList<>();

    private int curStartIndex;

    private int curEndIndex;

    private int updateIndex;

    private int realLOAD_DISTANCE;

    private int realDANGER_DISTANCE;

    public void setIDynamicLoadNotifer(IDynamicLoadNotifer dynamicLoadNotifer){
        mDynamicLoadNotifer = dynamicLoadNotifer;

    }

    public int updateOriginPath(List<LatLng> path, int dataLevel){  //更新原始的高德导航路径
        mCoordList.clear();
        mCoordList.addAll(path);
        realLOAD_DISTANCE = (int) (DEFAULTLOADDISTANCE * Math.pow(2.0,20 - dataLevel));
        realDANGER_DISTANCE = (int)(DEFAULTDANGERDISTANCE* Math.pow(2.0,20 - dataLevel));
        curStartIndex = 0;
        updateIndex = 0;
        curEndIndex = 0;
        lookForSuitableValues();
        Log.e("ylq","updateOringinPath"+"size:"+mCoordList.size());
        return curEndIndex;
    }

    public void updateCurPoint(int realPointIndex){  //更新当前小车位置的前一个形状点和step点
        if (realPointIndex >= curEndIndex||realPointIndex >= updateIndex){
            curStartIndex = lookForStartIndex(realPointIndex);
            updateIndex = 0;
            lookForSuitableValues();
            mDynamicLoadNotifer.loadNewRoad(curStartIndex,curEndIndex);
        }
    }

    private void lookForSuitableValues(){
        int distance = 0;
        for (int i  = curStartIndex;i < mCoordList.size() - 1;i++){
            LatLng lat = new LatLng(mCoordList.get(i).latitude,mCoordList.get(i).longitude);
            LatLng lat1 = new LatLng(mCoordList.get(i+1).latitude,mCoordList.get(i+1).longitude);
            distance += AMapUtils.calculateLineDistance(lat,lat1);
            if (i != mCoordList.size() - 2){
                if (distance >= realLOAD_DISTANCE/2+realDANGER_DISTANCE&&updateIndex==0&&i+1>=curEndIndex){
                    updateIndex = i+1;
                }
                if (distance >= realLOAD_DISTANCE&&updateIndex!=0){
                    if (i+1 > updateIndex&&AMapUtils.calculateLineDistance(
                            lat1,new LatLng(mCoordList.get(updateIndex).latitude,
                                            mCoordList.get(updateIndex).longitude))>=DEFAULTLOADDISTANCE*2/5){
                        curEndIndex =i+1;
                        break;
                    }
                }
            }else {
                curEndIndex = mCoordList.size()-1;
                updateIndex = mCoordList.size();
            }
        }
    }


    private int lookForStartIndex(int curPointIndex){
        if (curPointIndex <=1){
            return 0;
        }
        int distance = 0;
        int i;
        for (i = curPointIndex;i > 0;i--){
            LatLng lat = new LatLng(mCoordList.get(i).latitude,mCoordList.get(i).longitude);
            LatLng lat1 = new LatLng(mCoordList.get(i-1).latitude,mCoordList.get(i-1).longitude);
            distance += AMapUtils.calculateLineDistance(lat,lat1);
            if (distance >= realDANGER_DISTANCE){
                break;
            }
        }
        return i - 1;
    }


}
