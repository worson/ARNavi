package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import android.graphics.PointF;

import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.haloai.hud.hudendpoint.arwaylib.render.strategy.IRenderStrategy;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayProjection;
import com.haloai.hud.hudendpoint.arwaylib.utils.Douglas;
import com.haloai.hud.hudendpoint.arwaylib.utils.DrawUtils;
import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Created by Mo Bing(mobing@haloai.com) on 22/10/2016.
 */
public class AMapNaviPathDataProcessor implements INaviPathDataProcessor<AMapNaviPath, NaviInfo, AMapNaviLocation> {
    //constant
    private static final String TAG                    = "AMapNaviPathDataProcessor";
    private static final double OBJ_4_CHASE_Z          = 0;
    private static final float  TIME_15_20             = 32;
    private static final int    RAREFY_PIXEL_COUNT     = 1;
    private static final int    DEFAULT_LEVEL          = 15;
    private static final int    ANIM_DURATION_REDUNDAN = 100;

    //Cache all navigation path data.That two member can not change address,because renderer is use that too.
    private INaviPathDataProvider mNaviPathDataProvider = new AMapNaviPathDataProvider();
    private IRoadNetDataProvider  mRoadNetDataProvider  = new RoadNetDataProvider();

    //listener and notifier
    private IRenderStrategy mRenderStrategy;

    //middle data
    private List<LatLng>  mPathLatLng;
    private List<Vector3> mPathVector3;
    private List<Vector3> mRenderPath;
    private List<Integer> mPointIndexsToKeep;
    private List<Integer> mStepLengths;
    private double        mOffsetX;
    private double        mOffsetY;
    private boolean       mIsPathInited;
    private int           mTotalSize;

    //real-time data
    private int mCurIndexInPath;

    //anim about
    private Vector3 mFromPos     = null;
    private Vector3 mToPos       = null;
    private double  mFromDegrees = 0;
    private double  mToDegrees   = 0;
    private long    mPreTime     = 0;

    @Override
    public void reset() {
        mCurIndexInPath = 0;
        mOffsetX = 0;
        mOffsetY = 0;
        mFromPos = null;
        mToPos = null;
        mFromDegrees = 0;
        mToDegrees = 0;
        mPreTime = 0;
        mIsPathInited = false;
        mTotalSize = 0;
        mRoadNetDataProvider.reset();
        mNaviPathDataProvider.reset();
    }

    @Override
    /**
     * @param aMapNaviPath 导航路径
     * @return 1:路径处理正常,可以调用getNaviPathDataProvider获取数据 -1:异常情况
     */
    public int setPath(AMapNaviPath aMapNaviPath) {
        //0.reset all data
        reset();

        //1.check data legal
        HaloLogger.logE(TAG, "setPath check data legal");
        if (aMapNaviPath == null) {
            return -1;
        }
        List<Vector3> path_vector3 = new ArrayList<>();
        List<LatLng> path_latlng = new ArrayList<>();
        List<Integer> step_lengths = new ArrayList<>();
        for (AMapNaviStep step : aMapNaviPath.getSteps()) {
            step_lengths.add(step.getCoords().size());
            if (step != null) {
                for (int i = 0; i < step.getCoords().size(); i++) {
                    NaviLatLng coord = step.getCoords().get(i);
                    LatLng latLng = new LatLng(coord.getLatitude(), coord.getLongitude());
                    path_latlng.add(latLng);
                    PointF pf = ARWayProjection.toOpenGLLocation(latLng, DEFAULT_LEVEL);
                    path_vector3.add(new Vector3(pf.x, -pf.y, OBJ_4_CHASE_Z));
                }
            }
        }
        mStepLengths = step_lengths;
        mTotalSize = path_latlng.size();

        //2.data pre handle
        HaloLogger.logE(TAG, "setPath data pre handle");
        //move to screen center
        mOffsetX = path_vector3.get(0).x - 0;
        mOffsetY = path_vector3.get(0).y - 0;
        for (int i = 0; i < path_vector3.size(); i++) {
            path_vector3.get(i).x -= mOffsetX;
            path_vector3.get(i).y -= mOffsetY;
        }
        //init render path(bigger and rarefy)
        List<PointF> returnPath = new ArrayList<>();
        List<PointF> originalPath = new ArrayList<>();
        for (Vector3 v : path_vector3) {
            originalPath.add(new PointF((float) v.x, (float) v.y));
        }
        List<Integer> pointIndexsToKeep = new ArrayList<>();
        Douglas.rarefyGetPointFs(pointIndexsToKeep, returnPath, originalPath, RAREFY_PIXEL_COUNT / ARWayProjection.K);
        List<Vector3> renderPath = new ArrayList();
        for (PointF p : returnPath) {
            renderPath.add(new Vector3(p.x * TIME_15_20, p.y * TIME_15_20, OBJ_4_CHASE_Z));
        }
        //delete break point
        //..................
        //bigger ori path
        for (Vector3 v : path_vector3) {
            v.x *= TIME_15_20;
            v.y *= TIME_15_20;
        }
        mPointIndexsToKeep = pointIndexsToKeep;
        mRenderPath = renderPath;
        mPathVector3 = path_vector3;
        mPathLatLng = path_latlng;

        //calc and save the car need to rotate degrees
        Vector3 p1 = mRenderPath.get(0);
        Vector3 p2 = mRenderPath.get(1);
        for (int i = 1; i < mRenderPath.size(); i++) {
            if (!p1.equals(mRenderPath.get(i))) {
                p2 = mRenderPath.get(i);
                break;
            }
        }
        double rotateZ = (Math.toDegrees(MathUtils.getRadian(p1.x, p1.y, p2.x, p2.y)) + 270) % 360;
        mNaviPathDataProvider.setObjStartOrientation(rotateZ);

        //finally set path and call back to renderer in Provider.
        mNaviPathDataProvider.setPath(mRenderPath);

        //4.call processSteps(IRoadNetDataProcessor to get data and create IRoadNetDataProvider something)
        //HaloLogger.logE(TAG, "setPath call processSteps(IRoadNetDataProcessor to get data and create IRoadNetDataProvider something)");
        //processSteps(0,1,2)
        //mRoadNetChangeNotifier.onRoadNetDataChange();

        mIsPathInited = true;
        return 1;
    }

    @Override
    public void setLocation(AMapNaviLocation location, Vector3 animPos, double animDegrees) {
        if (mIsPathInited) {
            //call DataProvider to update anim with cur location
            if (mFromPos == null) {
                mFromPos = convertLocation(location, mCurIndexInPath);
                mFromDegrees = MathUtils.convertAMapBearing2OpenglBearing(location.getBearing());
                mPreTime = location.getTime();
            } else {
                long duration = location.getTime() - mPreTime;
                mPreTime = location.getTime();
                if (mToPos != null) {
                    mFromPos = animPos;
                    mFromDegrees = Math.toDegrees(animDegrees);
                    mFromDegrees = mFromDegrees < 0 ? mFromDegrees + 360 : mFromDegrees;
                }
                mToPos = convertLocation(location, mCurIndexInPath);
                mToDegrees = MathUtils.convertAMapBearing2OpenglBearing(location.getBearing());
                HaloLogger.logE(TAG,"from="+mFromPos+",to="+mToPos+",degrees="+(mToDegrees-mFromDegrees)+
                        ",duration="+(duration+ANIM_DURATION_REDUNDAN));
                mNaviPathDataProvider.setAnim(mFromPos, mToPos, mToDegrees - mFromDegrees, duration + ANIM_DURATION_REDUNDAN);
            }
            //Calculate the distance of maneuver point.
            //.....
            int distanceOfMP = 500;
            //Get the step road class.
            int roadClass = IRenderStrategy.HaloRoadClass.MAINWAY;
            mRenderStrategy.updateCurrentRoadInfo(roadClass, distanceOfMP);

            //Maybe Road Net is change
            //checkNeedToProcessSteps();//is or not cur step index is change
            //processSteps(N);
            //mRoadNetDataProvider.setDataXX();
        }
    }

    @Override
    public void setNaviInfo(NaviInfo naviInfo) {
        if (mIsPathInited && naviInfo!=null) {
            mCurIndexInPath = getIndexInPath(naviInfo.getCurPoint(), naviInfo.getCurStep());
            HaloLogger.logE(TAG,"cur index in path="+mCurIndexInPath);
            //call DataProvider to update data with naviInfo
        }
    }

    @Override
    public boolean setRenderStrategy(IRenderStrategy renderStrategy) {
        this.mRenderStrategy = renderStrategy;
        return true;
    }

    @Override
    public boolean setRoadNetChangeNotifier(IRoadNetDataProvider.IRoadNetDataNotifier roadNetChangeNotifier) {
        if (mRoadNetDataProvider != null) {
            mRoadNetDataProvider.setRoadNetChangeNotifier(roadNetChangeNotifier);
        }
        return mRoadNetDataProvider != null;
    }

    @Override
    public boolean setNaviPathChangeNotifier(INaviPathDataProvider.INaviPathDataChangeNotifer naviPathChangeNotifier) {
        if (mNaviPathDataProvider != null) {
            mNaviPathDataProvider.setNaviPathChangeNotifier(naviPathChangeNotifier);
        }
        return mNaviPathDataProvider != null;
    }

    @Override
    public INaviPathDataProvider getNaviPathDataProvider() {
        return mNaviPathDataProvider;
    }

    @Override
    public IRoadNetDataProvider getRoadNetDataProvider() {
        return mRoadNetDataProvider;
    }

    /**
     * TODO:暂时不对路网数据进行处理
     * 访问路网模块获取指定steps的路网数据
     */
    private void processSteps(int stepIndex) {

    }

    /**
     * 准备分级数据(此处的准备并不是真正的准备每一级别的数据,而是得到每一级别对应
     * 原始级别的转换关系即可)
     */
    private void prepareLevelData() {

    }

    /**
     * 求当前点在path中的哪个位置
     *
     * @param currentPoint
     * @param currentStep
     * @return
     */
    private int getIndexInPath(int currentPoint, int currentStep) {
        int currentIndex = 0;
        for (int i = 0; i < currentStep && mStepLengths != null; i++) {
            currentIndex += mStepLengths.get(i);
        }
        currentIndex += currentPoint;
        if (currentIndex >= mTotalSize) {
            currentIndex = mTotalSize - 1;
        }
        return currentIndex;
    }

    /**
     * 处理由一个location转换成Rajawali可用的vector3的过程以及其中的一些数据处理
     *
     * @param location
     * @param curIndex
     * @return
     */
    private Vector3 convertLocation(AMapNaviLocation location, int curIndex) {
        PointF fromPos = ARWayProjection.toOpenGLLocation(DrawUtils.naviLatLng2LatLng(location.getCoord()), DEFAULT_LEVEL);
        Vector3 v = new Vector3(
                (fromPos.x - mOffsetX) * TIME_15_20,
                (-fromPos.y - mOffsetY) * TIME_15_20,
                OBJ_4_CHASE_Z);
        for (int i = 0; i < mPointIndexsToKeep.size(); i++) {
            if (!(mPointIndexsToKeep.get(i) < curIndex)) {
                Vector3 line_start = null;
                Vector3 line_end = null;
                if (mPointIndexsToKeep.get(i) == curIndex && i != mPointIndexsToKeep.size() - 1) {
                    line_start = mRenderPath.get(i);
                    line_end = mRenderPath.get(i + 1);
                } else if (mPointIndexsToKeep.get(i) > curIndex && i != 0) {
                    line_start = mRenderPath.get(i - 1);
                    line_end = mRenderPath.get(i);
                }
                PointF pProjection = new PointF();
                MathUtils.getProjectivePoint(new PointF((float) line_start.x, (float) line_start.y),
                                             new PointF((float) line_end.x, (float) line_end.y),
                                             new PointF((float) v.x, (float) v.y),
                                             pProjection);
                v.x = pProjection.x;
                v.y = pProjection.y;
                break;
            }
        }
        return v;
    }
}
