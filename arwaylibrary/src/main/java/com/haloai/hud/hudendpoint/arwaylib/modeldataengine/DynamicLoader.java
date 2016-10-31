package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.NaviLatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ylq on 16/10/31.
 */
public class DynamicLoader implements IDynamicLoader {

    private IDynamicLoader.IDynamicLoadNotifer mDynamicLoadNotifer;

    private int RETAINDISTANCE;//距离当前路段终点距离多少时该加载新路,根据道路等级来确定

    private int PASSDISTANC;//通过当前路段多少距离时清除旧路，根据道路等级来确定

    private List<NaviLatLng> mCoordList;

    private List<Integer> mStepIndexList = new ArrayList<>();

    private int curStartIndex = 0;

    private int curEndIndex;

    private int curRemoveIndex;

    private int curAddIndex;


    public void setIDynamicLoadNotifer(IDynamicLoadNotifer dynamicLoadNotifer){
        mDynamicLoadNotifer = dynamicLoadNotifer;

    }

    public int updateOriginPath(AMapNaviPath path, int dataLevel){  //更新原始的高德导航路径
        this.mCoordList = path.getCoordList();
        this.mStepIndexList.clear();
        for (AMapNaviStep temple : path.getSteps()){

        }
        return curEndIndex;
    }

    public void updateCurPoint(int curIndex){  //更新当前小车位置的前一个形状点

    }




}
