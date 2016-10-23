package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

/**
 * @author Created by Mo Bing(mobing@haloai.com) on 22/10/2016.
 */
public interface INaviPathDataProcessor<NaviPath,NaviInfo,Location> {
    //data update
    void onPathUpdate(NaviPath naviPath);
    void onNaviInfoUpdate(NaviInfo naviInfo);
    void onLocationUpdate(Location location);

    /*//state update
    void onNaviStop();
    void onNaviStart();*/

    //data return
    INaviPathDataProvider getNaviPathDataProvider();
    IRoadNetDataProvider getRoadNetDataProvider();

}
