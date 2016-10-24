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
public class AMapNaviPathDataProvider implements INaviPathDataProvider{
    private INaviPathDataChangeNotifer mNaviPathChangeNotifier;
    private List<Vector3> mRenderPath;
    private double mObjStartOrientation;

    @Override
    public void setNaviPathChangeNotifier(INaviPathDataChangeNotifer naviPathChangeNotifier) {
        this.mNaviPathChangeNotifier = naviPathChangeNotifier;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setAnim(Vector3 from, Vector3 to, double degrees, long duration) {
        if(mNaviPathChangeNotifier!=null)
            mNaviPathChangeNotifier.onAnimUpdate(new AnimData(from,to,degrees,duration));
    }

    @Override
    public void setPath(List<Vector3> renderPath) {
        mRenderPath = renderPath;
        if(mNaviPathChangeNotifier!=null)
            mNaviPathChangeNotifier.onPathUpdate();
    }

    @Override
    public void setObjStartOrientation(double rotateZ) {
        this.mObjStartOrientation = rotateZ;
    }

    @Override
    public List<Vector3> getNaviPathByLevel(int level) {
        //Dont care level for now.
        return mRenderPath;
    }

    @Override
    public double getObjStartOrientation() {
        return this.mObjStartOrientation;
    }
}
