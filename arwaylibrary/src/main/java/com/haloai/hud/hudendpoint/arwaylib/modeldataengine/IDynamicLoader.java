package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import com.amap.api.maps.model.LatLng;

import java.util.List;


/**
 * Created by ylq on 16/10/31.
 */
public interface IDynamicLoader {

    int updateOriginPath(List<LatLng> path, int dataLevel);


    void updateCurPoint(int realPointIndex);



    interface IDynamicLoadNotifer{
        void loadNewRoad(int startIndex,int endIndex);
    }

    void setIDynamicLoadNotifer(IDynamicLoadNotifer dynamicLoadNotifer);

}
