package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import org.rajawali3d.math.vector.Vector3;

import java.util.List;

/**
 * author       : 龙;
 * date         : 2016/10/23;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.modeldataengine;
 * project_name : hudlauncher;
 */
public interface INaviPathDataProvider<NaviPath,NaviInfo,Location> {
    void reset();
    void setNaviPath(NaviPath naviPath);
    void setNaviInfo(NaviInfo naviInfo);
    void setLocation(Location location);
    List<Vector3> getNaviPathByLevel(int level);
    public class AnimData{

    }
    AnimData getNaviAnim();
}
