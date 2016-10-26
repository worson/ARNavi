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
public class AMapNaviPathDataProvider implements INaviPathDataProvider {
    private INaviPathDataChangeNotifer mNaviPathChangeNotifier;
    private List<List<Vector3>>        mRenderPath;
    private double                     mObjStartOrientation;

    private IRenderStrategy.DataLevel mCurDataLevel;
    private int                       mCurFactor;
    private double                    mCurOffsetX;
    private double                    mCurOffsetY;

    @Override
    public void setNaviPathChangeNotifier(INaviPathDataChangeNotifer naviPathChangeNotifier) {
        mNaviPathChangeNotifier = naviPathChangeNotifier;
    }

    @Override
    public void reset() {
        mCurDataLevel = IRenderStrategy.DataLevel.LEVEL_20;
        mCurFactor = 1;
        mCurOffsetX = 0;
        mCurOffsetY = 0;
    }

    @Override
    public void setAnim(Vector3 from, Vector3 to, double degrees, long duration) {
        if (mNaviPathChangeNotifier != null) {
            mNaviPathChangeNotifier.onAnimUpdate(new AnimData(from, to, degrees, duration));
        }
    }

    @Override
    public void initPath(List<List<Vector3>> renderPath) {
        mRenderPath = renderPath;
        if (mNaviPathChangeNotifier != null)
            mNaviPathChangeNotifier.onPathInit();
    }

    @Override
    public void updatePath(List<Vector3> newPath) {
        mRenderPath.remove(0);
        mRenderPath.add(newPath);
        if (mNaviPathChangeNotifier != null)
            mNaviPathChangeNotifier.onPathUpdate();
    }

    @Override
    public void setObjStartOrientation(double rotateZ) {
        mObjStartOrientation = rotateZ;
    }

    @Override
    public List<List<Vector3>> getNaviPathByLevel(IRenderStrategy.DataLevel level, double curPointX, double curPointY) {
        //假设curPoint为15级时的数据,现在拉取的是18级的数据
        IRenderStrategy.DataLevel lastLevel = mCurDataLevel;
        //factor_last_new =getFactorByLevel(lastLevel)/getFactorByLevel(level)
        int oldFactor = getFactorByLevel(lastLevel);
        int newFactor = getFactorByLevel(level);
        //curPointX+=mCurOffsetX;
        //curPointY+=mCurOffsetY;
        mCurOffsetX = (curPointX+mCurOffsetX) * oldFactor / newFactor - (curPointX);
        mCurOffsetY = (curPointY+mCurOffsetY) * oldFactor / newFactor - (curPointY);
        mCurDataLevel = level;
        int factor = newFactor;
        mCurFactor = factor;
        List<List<Vector3>> renderElseLevel = new ArrayList<>();
        for (List<Vector3> path : mRenderPath) {
            List<Vector3> _path = new ArrayList<>();
            for (Vector3 v : path) {
                _path.add(new Vector3(v.x / mCurFactor - mCurOffsetX, v.y / mCurFactor - mCurOffsetY, v.z / mCurFactor));
            }
            renderElseLevel.add(_path);
        }
        return renderElseLevel;
    }

    /**
     * 根据道路等级获取当前的转换系数(From 20)
     * N*factor=20_N
     * N位传入的等级对应的数据,20_N对应的是20级下对应的数据
     * @param level
     * @return
     */
    private int getFactorByLevel(IRenderStrategy.DataLevel level) {
        int factor = 1;
        switch (level) {
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
        return factor;
    }

    @Override
    public double getObjStartOrientation() {
        return mObjStartOrientation;
    }

    @Override
    public int getCurDataLevelFactor() {
        return mCurFactor;
    }

    @Override
    public double getCurOffsetX() {
        return mCurOffsetX;
    }

    @Override
    public double getCurOffsetY() {
        return mCurOffsetY;
    }
}
