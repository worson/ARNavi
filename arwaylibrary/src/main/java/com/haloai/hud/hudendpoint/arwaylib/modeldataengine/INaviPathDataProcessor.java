package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import com.haloai.hud.hudendpoint.arwaylib.render.strategy.IRenderStrategy;

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

    //Render Strategy Setter
    void setRenderStrategy(IRenderStrategy renderStrategy);

    //data return
    INaviPathDataProvider getNaviPathDataProvider();
    IRoadNetDataProvider getRoadNetDataProvider();

}
