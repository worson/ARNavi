package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import android.graphics.PointF;

import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.model.AMapNaviLink;
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
public class AMapNaviPathDataProcessor implements INaviPathDataProcessor<AMapNavi, AMapNaviPath, NaviInfo, AMapNaviLocation> {
    //constant
    private static final String TAG                           = "AMapNaviPathDataProcessor";
    private static final double OBJ_4_CHASE_Z                 = 0;//被追随物体的Z轴高度,用于构建Vector3中的Z
    private static final float  TIME_15_20                    = 32;//15级数据到20级数据转换的系数
    private static final int    RAREFY_PIXEL_COUNT            = 1;//道格拉斯抽析的像素个数
    private static final int    DEFAULT_LEVEL                 = 15;//默认转换等级15(需要转换成20)
    private static final int    ANIM_DURATION_REDUNDAN        = 100;//动画默认延长时间避免停顿
    private static final int    CROSS_COUNT_INIT              = 3;//初始拉取路网数据的路口个数
    private static final double NEED_OPENGL_LENGTH            = 20;//摄像头高度角度不考虑时视口需要显示的opengl长度
    private static final double FACTOR_LEVEL20_OPENGL_2_METER = 1;//20级下opengl到米的转换系数
    private static final double SEGMENT_OPENGL_LEGNTH         = 5;//每一个小段对应的opengl长度(也就是说需要四段20/5)

    //Cache all navigation path data.That two member can not change address,because renderer is use that too.
    private INaviPathDataProvider mNaviPathDataProvider = new AMapNaviPathDataProvider();
    private IRoadNetDataProvider  mRoadNetDataProvider  = new RoadNetDataProvider();

    //listener and notifier
    private IRenderStrategy mRenderStrategy;

    //middle data
    private AMapNavi            mAMapNavi;
    private List<LatLng>        mPathLatLng;
    private List<Vector3>       mPathVector3;
    private List<Vector3>       mDouglasPath;
    private List<List<Vector3>> mRenderPath;
    private List<Integer>       mPointIndexsToKeep;
    private List<Integer>       mStepLengths;
    private double              mOffsetX;
    private double              mOffsetY;
    private boolean             mIsPathInited;
    private int                 mTotalSize;

    //real-time data
    private int mCurIndexInPath;
    private int mCurRoadNetIndex;

    //animation
    private Vector3 mFromPos     = null;
    private Vector3 mToPos       = null;
    private double  mFromDegrees = 0;
    private double  mToDegrees   = 0;
    private long    mPreTime     = 0;

    //dynamic load
    private int           mCurLevelNeedMeter;
    private List<Integer> mSplitPointIndexs;

    @Override
    public void reset() {
        mCurIndexInPath = 0;
        mCurRoadNetIndex = 2;
        mOffsetX = 0;
        mOffsetY = 0;
        mFromPos = null;
        mToPos = null;
        mFromDegrees = 0;
        mToDegrees = 0;
        mPreTime = 0;
        mIsPathInited = false;
        mTotalSize = 0;
        mCurLevelNeedMeter = (int) (NEED_OPENGL_LENGTH * FACTOR_LEVEL20_OPENGL_2_METER);
        mRoadNetDataProvider.reset();
        mNaviPathDataProvider.reset();
    }

    @Override
    /**
     * @param aMapNaviPath 导航路径
     * @return 1:路径处理正常,可以调用getNaviPathDataProvider获取数据 -1:异常情况
     */
    public int setPath(AMapNavi amapNavi, AMapNaviPath aMapNaviPath) {
        //0.reset all data
        reset();

        //1.check data legal
        HaloLogger.logE(TAG, "initPath check data legal");
        if (amapNavi == null || aMapNaviPath == null) {
            return -1;
        }
        mAMapNavi = amapNavi;

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
        HaloLogger.logE(TAG, "initPath data pre handle");
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
        List<Vector3> douglasPath = new ArrayList();
        for (PointF p : returnPath) {
            douglasPath.add(new Vector3(p.x * TIME_15_20, p.y * TIME_15_20, OBJ_4_CHASE_Z));
        }
        //delete break point
        //..................
        //bigger ori path
        for (Vector3 v : path_vector3) {
            v.x *= TIME_15_20;
            v.y *= TIME_15_20;
        }
        mPathVector3 = path_vector3;
        mPathLatLng = path_latlng;
        mDouglasPath = douglasPath;
        mPointIndexsToKeep = pointIndexsToKeep;

        //calc and save the car need to rotate degrees
        Vector3 p1 = mDouglasPath.get(0);
        Vector3 p2 = mDouglasPath.get(1);
        for (int i = 1; i < mDouglasPath.size(); i++) {
            if (!p1.equals(mDouglasPath.get(i))) {
                p2 = mDouglasPath.get(i);
                break;
            }
        }
        double rotateZ = (Math.toDegrees(MathUtils.getRadian(p1.x, p1.y, p2.x, p2.y)) + 270) % 360;
        mNaviPathDataProvider.setObjStartOrientation(rotateZ);

        //split douglasPath with mCurLevelNeedMeter and save the split point index to list.
        List<Integer> splitPointIndexs = new ArrayList<>();
        splitPointIndexs.add(0);
        double addUpLength = 0;
        for (int i = 0; i < mDouglasPath.size() - 1; i++) {
            Vector3 v1 = mDouglasPath.get(i);
            Vector3 v2 = mDouglasPath.get(i + 1);
            addUpLength += (MathUtils.calculateDistance(v1.x, v1.y, v2.x, v2.y));
            if (addUpLength >= SEGMENT_OPENGL_LEGNTH) {
                splitPointIndexs.add(i + 1);
                addUpLength = 0;
            } else if (i == mDouglasPath.size() - 2) {
                splitPointIndexs.add(i + 1);
            }
        }
        mSplitPointIndexs = splitPointIndexs;

        //finally set path to provider and call back to renderer in Provider.
        //segmentCount = NEED_OPENGL_LENGTH / SEGMENT_OPENGL_LEGNTH + 1;
        List<List<Vector3>> renderPath = new ArrayList<>();
        /*for (int i = 0; i < NEED_OPENGL_LENGTH / SEGMENT_OPENGL_LEGNTH + 1 && i < mSplitPointIndexs.size() - 1; i++) {
            int start = mSplitPointIndexs.get(i);
            int end = mSplitPointIndexs.get(i + 1);
            renderPath.add(mDouglasPath.subList(start, end));
        }*/
        HaloLogger.logE(TAG,"size="+mDouglasPath.size());
        renderPath.add(mDouglasPath);
        mRenderPath = renderPath;
        mNaviPathDataProvider.initPath(mRenderPath);

        //4.call processSteps(IRoadNetDataProcessor to get data and create IRoadNetDataProvider something)
        //HaloLogger.logE(TAG, "initPath call processSteps(IRoadNetDataProcessor to get data and create IRoadNetDataProvider something)");
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
                mNaviPathDataProvider.setAnim(mFromPos, mToPos, mToDegrees - mFromDegrees, duration + ANIM_DURATION_REDUNDAN);
            }
        }
    }

    @Override
    public void setNaviInfo(NaviInfo naviInfo) {
        if (mIsPathInited && naviInfo != null) {
            //0.calc and save useful data.
            mCurIndexInPath = getIndexInPath(naviInfo.getCurPoint(), naviInfo.getCurStep());
            //1.Calculate the distance of maneuver point and get road class.
            int distanceOfMP = naviInfo.getCurStepRetainDistance();
            AMapNaviLink curLink = mAMapNavi.getNaviPath().getSteps().get(naviInfo.getCurStep())
                    .getLinks().get(naviInfo.getCurLink());
            mRenderStrategy.updateCurrentRoadInfo(curLink.getRoadClass(), distanceOfMP);
            //2.Maybe Road Net is change
            if (naviInfo.getCurStep() > mCurRoadNetIndex) {
                //拉取processSteps(curStep)
                //mRoadNetDataProvider.setDataXX();
                mCurRoadNetIndex = naviInfo.getCurStep();
            }
        }
    }

    @Override
    public boolean setRenderStrategy(IRenderStrategy renderStrategy) {
        this.mRenderStrategy = renderStrategy;
        return true;
    }

    @Override
    public boolean setRoadNetChangeNotifier(IRoadNetDataProvider.IRoadNetDataNotifier roadNetChangeNotifier) {
        mRoadNetDataProvider.setRoadNetChangeNotifier(roadNetChangeNotifier);
        return mRoadNetDataProvider != null;
    }

    @Override
    public boolean setNaviPathChangeNotifier(INaviPathDataProvider.INaviPathDataChangeNotifer naviPathChangeNotifier) {
        mNaviPathDataProvider.setNaviPathChangeNotifier(naviPathChangeNotifier);
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
    private void processSteps(int... stepIndexs) {
        //1.根据stepIndex构建数据{1.机动点前后道路link形式 2.每个link的info 3.机动点经纬度 4.}
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
        PointF pointF = ARWayProjection.toOpenGLLocation(DrawUtils.naviLatLng2LatLng(location.getCoord()), DEFAULT_LEVEL);
        Vector3 v = new Vector3(
                (pointF.x - mOffsetX) * TIME_15_20,
                (-pointF.y - mOffsetY) * TIME_15_20,
                OBJ_4_CHASE_Z);
        for (int i = 0; i < mPointIndexsToKeep.size(); i++) {
            if (!(mPointIndexsToKeep.get(i) < curIndex)) {
                Vector3 line_start = null;
                Vector3 line_end = null;
                if (mPointIndexsToKeep.get(i) == curIndex && i != mPointIndexsToKeep.size() - 1) {
                    line_start = mDouglasPath.get(i);
                    line_end = mDouglasPath.get(i + 1);
                } else if (mPointIndexsToKeep.get(i) > curIndex && i != 0) {
                    line_start = mDouglasPath.get(i - 1);
                    line_end = mDouglasPath.get(i);
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

        //根据不同的道路等级,拿到的GPS转换到的opengl点也需要做响应的转换,默认情况下factor=1
        int factor = mNaviPathDataProvider.getCurDataLevelFactor();
        v.x /= factor;
        v.y /= factor;
        v.z /= factor;
        double offsetX = mNaviPathDataProvider.getCurOffsetX();
        double offsetY = mNaviPathDataProvider.getCurOffsetY();
        v.x-=offsetX;
        v.y-=offsetY;
        return v;
    }
}
