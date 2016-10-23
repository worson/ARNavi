package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import java.util.ArrayList;
import java.util.List;

/**
 * author       : 龙;
 * date         : 2016/10/23;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.modeldataengine;
 * project_name : hudlauncher;
 */
public class RoadNetDataProvider implements IRoadNetDataProvider{
    List<List<LatLng_RoadNet>> mRoadNetData = new ArrayList<>();

    @Override
    public void reset() {

    }

    @Override
    public void processStep(List<List<LatLng_RoadNet>> naviLinks, List<NaviInfo_RoadNet> linkInfos,
                            LatLng_RoadNet centerLatLng, Size2i_RoadNet szCover, String sourceFilePath) {
        //填充以及更新路网数据到集合中
    }

    @Override
    public List<List<LatLng_RoadNet>> getRoadNet() {
        return null;
    }
}
