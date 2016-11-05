package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import com.haloai.hud.hudendpoint.arwaylib.render.strategy.IRenderStrategy;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayProjection;
import com.haloai.hud.utils.HaloLogger;

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

    //float left,float top,float right,float bottom,float spacing,float widthrate
    private double mLeftborder;
    private double mRightborder;
    private double mTopborder;
    private double mBottomborder;

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
    public void updatePath(List<List<Vector3>> newPath) {
        mRenderPath = newPath;

        /*//TODO test dynamic data
        for(List<Vector3> path:mRenderPath) {
            HaloLogger.logE("test_dynamic", "cross start");
            for(Vector3 v:path){
                HaloLogger.logE("test_dynamic", v.x+","+v.y);
            }
            HaloLogger.logE("test_dynamic", "cross end");
        }
        for(int i=1;i<mRenderPath.size();i++) {
            mRenderPath.remove(i);
        }*/

        HaloLogger.logE("ylq","updatePath  path size="+mRenderPath.size());
        if (mNaviPathChangeNotifier != null) {
            mNaviPathChangeNotifier.onPathUpdate();
        }
    }

    @Override
    public void setObjStartOrientation(double rotateZ) {
        mObjStartOrientation = rotateZ;
    }

    @Override
    public void setGuildLine(List<Vector3> guildLine) {
        if(mNaviPathChangeNotifier!=null){
            mNaviPathChangeNotifier.onGuideLineUpdate(guildLine);
        }
    }

    @Override
    public List<List<Vector3>> getNaviPathByLevel(IRenderStrategy.DataLevel level, double curPointX, double curPointY) {

        double add_Width = ARWayProjection.NEAR_PLANE_WIDTH/2 * 10;
        //假设curPoint为15级时的数据,现在拉取的是18级的数据
        IRenderStrategy.DataLevel lastLevel = mCurDataLevel;
        //factor_last_new =getFactorByLevel(lastLevel)/getFactorByLevel(level)
        int oldFactor = getFactorByLevel(lastLevel);
        int newFactor = getFactorByLevel(level);
        //curPointX+mCurOffsetX -- 将当前点平移回以0,0点为原点的坐标系中
        // * oldFactor -- 将数据恢复到20级下的数据
        // / newFactor -- 将数据计算到请求的新级别下的数据
        // - curPointX -- 求新级别与旧级别之间的offsetX
        mCurOffsetX = (curPointX+mCurOffsetX) * oldFactor / newFactor - curPointX;
        mCurOffsetY = (curPointY+mCurOffsetY) * oldFactor / newFactor - curPointY;
        mCurDataLevel = level;
        int factor = newFactor;
        mCurFactor = factor;
        List<List<Vector3>> renderElseLevel = new ArrayList<>();
        for (List<Vector3> path : mRenderPath) {
            List<Vector3> _path = new ArrayList<>();
            boolean isFirst = true;
            for (Vector3 v : path) {
                Vector3 vec = new Vector3(v.x / mCurFactor - mCurOffsetX, v.y / mCurFactor - mCurOffsetY, v.z / mCurFactor);
                _path.add(vec);
                if(path == mRenderPath.get(0)) {
                    if (isFirst) {
                        mLeftborder = v.x - add_Width;
                        mRightborder = v.x + add_Width;
                        mTopborder = v.y + add_Width;
                        mBottomborder = v.y - add_Width;
                        isFirst = false;
                    } else {
                        if (v.x - add_Width < mLeftborder) {
                            mLeftborder = v.x - add_Width;
                        }
                        if (v.x + add_Width > mRightborder) {
                            mRightborder = v.x + add_Width;
                        }
                        if (v.y - add_Width < mBottomborder) {
                            mBottomborder = v.y - add_Width;
                        }
                        if (v.y + add_Width > mTopborder) {
                            mTopborder = v.y + add_Width;
                        }
                    }
                }
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

    @Override
    public double getLeftborder(){return mLeftborder;}

    @Override
    public double getRightborder(){return mRightborder;}

    @Override
    public double getTopborder(){return mTopborder;}

    @Override
    public double getBottomborder(){return  mBottomborder;}
}
