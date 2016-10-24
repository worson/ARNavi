package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import com.haloai.hud.hudendpoint.arwaylib.render.strategy.IRenderStrategy;

import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
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
    private List<Vector3>              mRenderPath;
    private double                     mObjStartOrientation;
    private IRenderStrategy.DataLevel  mCurDataLevel;
    private int  mCurFactor;

    @Override
    public void setNaviPathChangeNotifier(INaviPathDataChangeNotifer naviPathChangeNotifier) {
        mNaviPathChangeNotifier = naviPathChangeNotifier;
    }

    @Override
    public void reset() {
        mCurDataLevel = IRenderStrategy.DataLevel.LEVEL_20;
        mCurFactor = 1;
    }

    @Override
    public void setAnim(Vector3 from, Vector3 to, double degrees, long duration) {
        if(mNaviPathChangeNotifier!=null) {
            mNaviPathChangeNotifier.onAnimUpdate(new AnimData(from, to, degrees, duration));
        }
    }

    @Override
    public void setPath(List<Vector3> renderPath) {
        mRenderPath = renderPath;
        if(mNaviPathChangeNotifier!=null)
            mNaviPathChangeNotifier.onPathUpdate();
    }

    @Override
    public void setObjStartOrientation(double rotateZ) {
        mObjStartOrientation = rotateZ;
    }

    @Override
    public List<Vector3> getNaviPathByLevel(IRenderStrategy.DataLevel level) {
        mCurDataLevel = level;
        int factor = 1;
        switch (level){
            case LEVEL_20:
                break;
            case LEVEL_18:
                factor = 4;
                break;
            case LEVEL_16:
                factor = 16;
                break;
            case LEVEL_15:
                factor = 32;
                break;
            case LEVEL_14:
                factor = 64;
                break;
            case LEVEL_13:
                factor = 128;
                break;
            case LEVEL_12:
                factor = 256;
                break;
        }
        mCurFactor = factor;
        if(factor==1){
            return mRenderPath;
        }
        List<Vector3> renderElseLevel = new ArrayList<>();
        for(Vector3 v : mRenderPath){
            renderElseLevel.add(new Vector3(v.x/factor,v.y/factor,v.z/factor));
        }
        return renderElseLevel;
    }

    @Override
    public double getObjStartOrientation() {
        return mObjStartOrientation;
    }

    @Override
    public int getCurDataLevelFactor() {
        return mCurFactor;
    }
}
