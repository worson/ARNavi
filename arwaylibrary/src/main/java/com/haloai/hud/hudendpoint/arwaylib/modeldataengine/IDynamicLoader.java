package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import com.haloai.hud.hudendpoint.arwaylib.utils.jni_data.LatLngOutSide;

import java.util.List;


/**
 * Created by ylq on 16/10/31.
 */
public interface IDynamicLoader {

    int updateOriginPath(List<LatLngOutSide> path, int dataLevel);


    void updateCurPoint(int realPointIndex);



    interface IDynamicLoadNotifer{
        void loadNewRoad(int startIndex,int endIndex);
    }

    void setIDynamicLoadNotifer(IDynamicLoadNotifer dynamicLoadNotifer);

}
