package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import com.amap.api.navi.model.AMapNaviPath;


/**
 * Created by ylq on 16/10/31.
 */
public interface IDynamicLoader {

    int updateOriginPath(AMapNaviPath path, int dataLevel);


    void updateCurPoint(int curIndex);



    interface IDynamicLoadNotifer{
        void removeOldRoad();

        void addNewRoad(int startIndex,int endIndex);
    }

    void setIDynamicLoadNotifer(IDynamicLoadNotifer dynamicLoadNotifer);

}
