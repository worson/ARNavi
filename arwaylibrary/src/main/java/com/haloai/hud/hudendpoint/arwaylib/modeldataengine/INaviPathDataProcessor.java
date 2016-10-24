package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import com.haloai.hud.hudendpoint.arwaylib.render.strategy.IRenderStrategy;

import org.rajawali3d.math.vector.Vector3;

/**
 * @author Created by Mo Bing(mobing@haloai.com) on 22/10/2016.
 */
public interface INaviPathDataProcessor<NaviPath,NaviInfo,Location> {
    //data update
    void reset();
    int setPath(NaviPath naviPath);
    void setNaviInfo(NaviInfo naviInfo);
    void setLocation(Location location, Vector3 animPos, double animDegrees);

    /*//state update
    void onNaviStop();
    void onNaviStart();*/

    //Render Strategy Setter
    boolean setRenderStrategy(IRenderStrategy renderStrategy);
    //Road Net data change notifier Setter
    boolean setRoadNetChangeNotifier(IRoadNetDataProvider.IRoadNetDataNotifier roadNetChangeNotifier);
    //Navi Path data change notifier Setter
    boolean setNaviPathChangeNotifier(INaviPathDataProvider.INaviPathDataChangeNotifer naviPathChangeNotifier);

    //data return
    INaviPathDataProvider getNaviPathDataProvider();
    IRoadNetDataProvider getRoadNetDataProvider();

}
