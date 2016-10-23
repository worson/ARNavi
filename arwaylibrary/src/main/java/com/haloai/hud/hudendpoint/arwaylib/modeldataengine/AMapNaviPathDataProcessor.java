package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import com.amap.api.location.AMapLocation;
import com.amap.api.navi.model.AMapNaviPath;

/**
 * @author Created by Mo Bing(mobing@haloai.com) on 22/10/2016.
 */
public class AMapNaviPathDataProcessor implements INaviPathDataProcessor {
    //Cache all navigation path data.

    @Override
    public void onPathUpdate(Object aMapNaviPath) {
        if (!(aMapNaviPath instanceof AMapNaviPath)) {
            throw new IllegalArgumentException("Needs AMapNaviPath");
        }
    }

    @Override
    public void onLocationUpdate(Object location, int curIndex) {
        if (!(location instanceof AMapLocation)) {
            throw new IllegalArgumentException("Needs AMapLocation");
        } else if (curIndex < 0) {
            throw new IllegalArgumentException("curIndex < 0 error");
        }
    }

    @Override
    public void onNaviStop() {

    }

    @Override
    public void onNaviStart() {

    }

    /**
     * 访问路网模块获取指定steps的路网数据
     */
    private void processSteps(int stepIndex) {

    }

    /**
     * 准备分级数据(此处的准备并不是真正的准备每一级别的数据,而是得到每一级别对应
     * 原始级别的转换关系即可)
     */
    private void prepareLevelData() {

    }
}
