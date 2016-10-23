package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import com.amap.api.location.AMapLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviInfo;

import org.rajawali3d.math.vector.Vector3;

import java.util.List;

/**
 * author       : 龙;
 * date         : 2016/10/23;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.modeldataengine;
 * project_name : hudlauncher;
 */
public class AMapNaviPathDataProvider implements INaviPathDataProvider<AMapNaviPath,NaviInfo,AMapLocation> {
    @Override
    public void reset() {

    }

    @Override
    public void setNaviPath(AMapNaviPath naviPath) {

    }

    @Override
    public void setNaviInfo(NaviInfo naviInfo) {

    }

    @Override
    public void setLocation(AMapLocation aMapLocation) {

    }

    @Override
    public List<Vector3> getNaviPathByLevel(int level) {
        return null;
    }

    @Override
    public AnimData getNaviAnim() {
        return null;
    }
}
