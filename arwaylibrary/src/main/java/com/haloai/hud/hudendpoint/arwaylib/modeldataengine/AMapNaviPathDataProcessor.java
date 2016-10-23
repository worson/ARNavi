package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import com.amap.api.location.AMapLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviInfo;

/**
 * @author Created by Mo Bing(mobing@haloai.com) on 22/10/2016.
 */
public class AMapNaviPathDataProcessor implements INaviPathDataProcessor<AMapNaviPath,NaviInfo,AMapLocation> {
    //Cache all navigation path data.
    private INaviPathDataProvider mNaviPathDataProvider = null;

    @Override
    public void onPathUpdate(AMapNaviPath aMapNaviPath) {
        //1.check data legal
        //2.data pre handle
        //3.create DataProvider instance
        //4.call processSteps(IRoadNetDataProcessor to get data and create IRoadNetDataProvider something)
    }

    @Override
    public void onLocationUpdate(AMapLocation location) {
        //call DataProvider to update anim with cur location
    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {
        //call DataProvider to update data with naviInfo
    }

    @Override
    public INaviPathDataProvider getNaviPathDataProvider() {
        return mNaviPathDataProvider;
    }

    /**
     * TODO:暂时不对路网数据进行处理
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
