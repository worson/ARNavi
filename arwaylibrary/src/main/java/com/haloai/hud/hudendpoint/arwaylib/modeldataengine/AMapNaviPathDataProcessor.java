package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.model.AMapNaviLink;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.haloai.hud.hudendpoint.arwaylib.render.options.RoadRenderOption;
import com.haloai.hud.hudendpoint.arwaylib.render.strategy.IRenderStrategy;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayProjection;
import com.haloai.hud.hudendpoint.arwaylib.utils.Douglas;
import com.haloai.hud.hudendpoint.arwaylib.utils.EnlargedCrossProcess;
import com.haloai.hud.hudendpoint.arwaylib.utils.FileUtils;
import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;
import com.haloai.hud.hudendpoint.arwaylib.utils.jni_data.LatLngOutSide;
import com.haloai.hud.hudendpoint.arwaylib.utils.jni_data.LinkInfoOutside;
import com.haloai.hud.hudendpoint.arwaylib.utils.jni_data.Size2iOutside;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Created by Mo Bing(mobing@haloai.com) on 22/10/2016.
 */
public class AMapNaviPathDataProcessor implements INaviPathDataProcessor<AMapNavi, AMapNaviPath, NaviInfo, AMapNaviLocation>, IDynamicLoader.IDynamicLoadNotifer {
    //constant
    private static final String TAG                           = "AMapNaviPathDataProcessor";
    private static final double DEFAULT_OPENGL_Z              = 0;//被追随物体的Z轴高度,用于构建Vector3中的Z
    private static final float  TIME_15_20                    = 32;//15级数据到20级数据转换的系数
    private static final double RAREFY_PIXEL_COUNT            = 1;//道格拉斯抽析的像素个数
    private static final int    DEFAULT_LEVEL                 = 15;//默认转换等级15(需要转换成20)
    private static final int    ANIM_DURATION_REDUNDAN        = 100;//动画默认延长时间避免停顿
    private static final int    CROSS_COUNT_INIT              = 3;//初始拉取路网数据的路口个数
    private static final double NEED_OPENGL_LENGTH            = 50;//摄像头高度角度不考虑时视口需要显示的opengl长度
    private static final double FACTOR_LEVEL20_OPENGL_2_METER = 16.5;//20级下opengl到米的转换系数 20级别下--1opengl~=16.5meter
    private static final double SEGMENT_OPENGL_LEGNTH         = 10;//每一个小段对应的opengl长度(也就是说需要四段20/5)
    private static final int    NUMBER_TRAFFIC_LIGHT          = 4;

    //Cache all navigation path data.That two member can not change address,because renderer is use that too.
    private INaviPathDataProvider mNaviPathDataProvider = new AMapNaviPathDataProvider();
    private IRoadNetDataProvider  mRoadNetDataProvider  = new RoadNetDataProvider();

    //listener and notifier
    private IRenderStrategy mRenderStrategy;

    //middle data
    private AMapNavi            mAMapNavi;
    private List<LatLngOutSide> mPathLatLng;
    private List<Vector3>       mPathVector3;
    private List<Vector3>       mDouglasPath;
    private List<List<Vector3>> mRenderPaths;
    //private List<Integer>       mPointIndexsToKeep;
    private List<Integer>       mStepLengths;
    private List<Integer>       mStepPointIndexs;
    private double              mOffsetX;
    private double              mOffsetY;
    private boolean             mIsPathInited;
    private int                 mTotalSize;

    //real-time data
    private int mCurIndexInPath;
    private int mCurStep;

    //animation
    private Vector3 mFromPos     = null;
    private Vector3 mToPos       = null;
    private double  mFromDegrees = 0;
    private double  mToDegrees   = 0;
    private long    mPreTime     = 0;

    //dynamic load
    private int           mCurLevelNeedMeter;
    private List<Integer> mSplitPointIndexs;
    private int           mCurIndexInSplitPoints;
    private double        METER_2_OPENGL;
    private double        NEED_LOAD_METER;
    private double        mLeftMeterLength;
    private List<Integer> mAlreadyLoadStep = new ArrayList<>();
    private int mPreDynamicStartIndex;
    private int mPreDynamicEndIndex;

    //Road Net
    private EnlargedCrossProcess mEnlargedCrossProcess = new EnlargedCrossProcess();
    private double PIXEL_2_LATLNG;
    private int                       mPreStartBreak      = 0;
    private int                       mPreEndBreak        = 0;
    private int                       mPreStepIndex       = 0;
    private List<List<List<Vector3>>> mBranchPaths        = new ArrayList<>();
    private List<Integer>             mBranchInPathIndexs = new ArrayList<>();

    //test
    String        _fileDir  = "/sdcard/daoge_log/";
    String        _filename = "";
    StringBuilder sb_helong = null;
    StringBuilder sb_daoge  = new StringBuilder();
    String        fileDir   = "/sdcard/daoge_log/";
    String        filename  = "";

    //proportion mapping
    private ProportionMappingEngine mProportionMappingEngine;

    //ylqtest
    private IDynamicLoader mDynamicLoader = new DynamicLoader();
    private int            mLastLink      = 0;
    private int            mLastStep      = -1;

    private AMapNaviLocation mLocation;

    public AMapNaviPathDataProcessor() {
        mDynamicLoader.setIDynamicLoadNotifer(this);
    }

    @Override
    public void reset() {
        mCurIndexInPath = 0;
        mCurStep = 0;
        mOffsetX = 0;
        mOffsetY = 0;
        mFromPos = null;
        mToPos = null;
        mFromDegrees = 0;
        mToDegrees = 0;
        mPreTime = 0;
        mIsPathInited = false;
        mTotalSize = 0;
        mCurIndexInSplitPoints = 0;
        mLeftMeterLength = 0;
        mPreStartBreak = 0;
        mPreEndBreak = 0;
        mPreStepIndex = 0;
        mPreDynamicStartIndex = 0;
        mPreDynamicEndIndex = 0;
        mAlreadyLoadStep.clear();
        mBranchPaths.clear();
        mBranchInPathIndexs.clear();
        mCurLevelNeedMeter = (int) (NEED_OPENGL_LENGTH * FACTOR_LEVEL20_OPENGL_2_METER);
        mRoadNetDataProvider.reset();
        mNaviPathDataProvider.reset();
        mLastLink = 0;
        mLastStep = -1;
        mEnlargedCrossProcess.clearJNIStatus();

        count = 0;
        _fileDir = "/sdcard/daoge_log/";
        _filename = "";
        sb_helong = null;
        fileDir = "/sdcard/daoge_log/";
        filename = "";
        sb_daoge = null;
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
        HaloLogger.postI(ARWayConst.NECESSARY_LOG_TAG, "AMapNaviPathDataProcessor setpath enter");
        if (amapNavi == null || aMapNaviPath == null) {
            HaloLogger.postE(ARWayConst.ERROR_LOG_TAG,"setPath error path ");
            return -1;
        }
        // TODO: 08/12/2016 测试
//        findTrafficLight(aMapNaviPath);
        mAMapNavi = amapNavi;

        List<Vector3> path_vector3 = new ArrayList<>();
        List<LatLngOutSide> path_latlng = new ArrayList<>();
        List<Integer> step_lengths = new ArrayList<>();
        List<Integer> stepPointIndexs = new ArrayList<>();
        for (AMapNaviStep step : aMapNaviPath.getSteps()) {
            if (step != null) {
                step_lengths.add(step.getCoords().size());
                for (int i = 0; i < step.getCoords().size(); i++) {
                    NaviLatLng coord = step.getCoords().get(i);
                    LatLngOutSide latLng = new LatLngOutSide(coord.getLatitude(), coord.getLongitude());
                    path_latlng.add(latLng);
                    ARWayProjection.PointD pd = ARWayProjection.toOpenGLLocation(latLng, DEFAULT_LEVEL);
                    path_vector3.add(new Vector3(pd.x, -pd.y, DEFAULT_OPENGL_Z));
                    if (i == step.getCoords().size() - 1) {
                        stepPointIndexs.add(path_latlng.size() - 1);
                    }
                }
            }
        }
        mPreDynamicStartIndex = 0;
        mPreDynamicEndIndex = mDynamicLoader.updateOriginPath(path_latlng, 20) + 1;
        HaloLogger.logE("ylq", "first end index = " + mPreDynamicEndIndex);
        mStepLengths = step_lengths;
        mStepPointIndexs = stepPointIndexs;
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
        //目前抽析放在映射引擎中实现
        /*List<PointF> returnPath = new ArrayList<>();
        List<PointF> originalPath = new ArrayList<>();
        for (Vector3 v : path_vector3) {
            originalPath.add(new PointF((float) v.x, (float) v.y));
        }
        List<Integer> pointIndexsToKeep = new ArrayList<>();*/
        /*Douglas.rarefyGetPointFs(pointIndexsToKeep, returnPath, originalPath, RAREFY_PIXEL_COUNT / ARWayProjection.K);
        List<Vector3> douglasPath = new ArrayList();
        for (PointF p : returnPath) {
            douglasPath.add(new Vector3(p.x * TIME_15_20, p.y * TIME_15_20, DEFAULT_OPENGL_Z));
        }*/
        List<Vector3> douglasPath = new ArrayList();
        for (Vector3 p : path_vector3) {
            douglasPath.add(new Vector3(p.x * TIME_15_20, p.y * TIME_15_20, DEFAULT_OPENGL_Z));
        }
        //delete break point 去折点
        //..................
        //bigger ori path
        for (Vector3 v : path_vector3) {
            v.x *= TIME_15_20;
            v.y *= TIME_15_20;
        }
        //求20级下像素与经纬度的对应关系一个像素对应多少经纬度单位
        double latlng_dist = MathUtils.calculateDistance(
                path_latlng.get(0).lat, path_latlng.get(0).lng,
                path_latlng.get(1).lat, path_latlng.get(1).lng);
        Point _p0 = ARWayProjection.toScreenLocation(path_latlng.get(0));
        Point _p1 = ARWayProjection.toScreenLocation(path_latlng.get(1));
        double pixel_dist = MathUtils.calculateDistance(_p0.x, _p0.y, _p1.x, _p1.y);
        PIXEL_2_LATLNG = latlng_dist / pixel_dist;

        mPathVector3 = path_vector3;
        mPathLatLng = path_latlng;
        mDouglasPath = douglasPath;
        //mPointIndexsToKeep = pointIndexsToKeep;

        //calc and save the car_mtl need to rotate degrees
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
                break;
            }
        }
        mSplitPointIndexs = splitPointIndexs;

        //finally set path to provider and call back to renderer in Provider.
        //segmentCount = NEED_OPENGL_LENGTH / SEGMENT_OPENGL_LEGNTH + 1;
        /*List<List<Vector3>> renderPath = new ArrayList<>();
        double showLength = 0;
        for (int i = 0; i < mSplitPointIndexs.size() - 1; i++) {
            int start = mSplitPointIndexs.get(i);
            int end = mSplitPointIndexs.get(i + 1);
            renderPath.add(mDouglasPath.subList(start, end + 1));
            double partLength = 0;
            for (int j = 0; j < renderPath.get(renderPath.size() - 1).size() - 1; j++) {
                Vector3 v1 = renderPath.get(renderPath.size() - 1).get(j);
                Vector3 v2 = renderPath.get(renderPath.size() - 1).get(j + 1);
                partLength += (MathUtils.calculateDistance(v1.x, v1.y, v2.x, v2.y));
            }
            showLength += partLength;
            if (showLength >= NEED_OPENGL_LENGTH || i == mSplitPointIndexs.size() - 2) {
                mCurIndexInSplitPoints = i;
                break;
            }
        }
        double totalLen = 0;
        for (int i = 0; i < mDouglasPath.size() - 1; i++) {
            Vector3 v1 = mDouglasPath.get(i);
            Vector3 v2 = mDouglasPath.get(i + 1);
            totalLen += MathUtils.calculateDistance(v1.x, v1.y, v2.x, v2.y);
        }
        //表示一个opengl单位表示多少米--  20级别下--1opengl~=16.5meter
        METER_2_OPENGL = aMapNaviPath.getAllLength() / totalLen;
        NEED_LOAD_METER = METER_2_OPENGL * SEGMENT_OPENGL_LEGNTH ;
        mLeftMeterLength = aMapNaviPath.getAllLength();

        mRenderPaths = renderPath;
        mNaviPathDataProvider.initPath(mRenderPaths);*/

        mRenderPaths = new ArrayList<>();
        //mRenderPaths.add(mDouglasPath);
        mProportionMappingEngine = new ProportionMappingEngine(mPathLatLng);
        mProportionMappingEngine.rarefyDouglas(mStepPointIndexs, RAREFY_PIXEL_COUNT / ARWayProjection.K, DEFAULT_LEVEL);

        _fileDir = "/sdcard/daoge_log/";
        long log_time = System.currentTimeMillis();
        _filename = "log_helong_" + log_time + ".txt";
        sb_helong = new StringBuilder();
        fileDir = "/sdcard/daoge_log/";
        filename = "log_daoge_" + log_time + ".txt";
        sb_daoge = new StringBuilder();
        //动态加载 0--endIndex
        HaloLogger.logE(TAG, "mPathLatLng path start");
        sb_helong.append("mPathLatLng path start" + "\n");
        for (LatLngOutSide latlng : mPathLatLng) {
            HaloLogger.logE(TAG, latlng.lat + "," + latlng.lng);
            sb_helong.append(latlng.lat + "," + latlng.lng + "\n");
        }
        HaloLogger.logE(TAG, "mPathLatLng path end");
        sb_helong.append("mPathLatLng path end" + "\n");

        HaloLogger.logE("ylq", "step indexs size = " + mStepPointIndexs.size());
        //TODO 根据动态加载的距离去拉去路网信息(真正运行时需要走的逻辑)
        /*for (int i = 0; i < mPreDynamicEndIndex; i++) {
            if (mStepPointIndexs.contains(i)) {
                HaloLogger.logE("ylq", "i=" + i + ",index=" + mStepPointIndexs.indexOf(i));
                processSteps(mStepPointIndexs.indexOf(i));
            }
        }*/

        //TODO 没有动态加载的情况下一次性拉去路网信息(测试时使用)
        for (int i = 0; i < mStepPointIndexs.size(); i++) {
            processSteps(i);
        }

        //TODO 北京演示时使用这部分,模拟一段假的但是效果更好的岔路显示出来
//        getShowBranchLines();

        FileUtils.write(sb_daoge.toString(), fileDir, filename);
        HaloLogger.logE(TAG, "mProportionMappingEngine.getRenderPath screen start");
        sb_helong.append("mProportionMappingEngine.getRenderPath screen start" + "\n");
        for (LatLngOutSide latlng : mProportionMappingEngine.getRenderPath()) {
            HaloLogger.logE(TAG, latlng.lat + "," + latlng.lng);
            sb_helong.append(latlng.lat + "," + latlng.lng + "\n");
        }
        HaloLogger.logE(TAG, "mProportionMappingEngine.getRenderPath screen end");
        sb_helong.append("mProportionMappingEngine.getRenderPath screen end" + "\n");
        FileUtils.write(sb_helong.toString(), _fileDir, _filename);
        List<Vector3> mainRoad = new ArrayList<>();
        for (LatLngOutSide latlng : mProportionMappingEngine.mapping(0, mPreDynamicEndIndex)) {
            ARWayProjection.PointD pd = ARWayProjection.toOpenGLLocation(latlng, DEFAULT_LEVEL);
            mainRoad.add(new Vector3((pd.x - mOffsetX) * TIME_15_20, (-pd.y - mOffsetY) * TIME_15_20, DEFAULT_OPENGL_Z));
        }
        mRenderPaths.add(mainRoad);

        //        // TODO: 2016/11/8 测试北京数据 start
        //        mBranchPaths.clear();
        //        List<List<Vector3>> branchPaths = new ArrayList<>();
        //        String[] lines = ARWayConst.BRANCH_LINES_BJ.split("\n");
        //        for(int i=0;i<lines.length;i++){
        //            if(lines[i].contains("start")){
        //                //get branch
        //                List<LatLng> branch = new ArrayList<>();
        //                for(int j=i+1;j<lines.length;j++){
        //                    if(!lines[j].contains("end")) {
        //                        branch.add(new LatLng(Double.parseDouble(lines[j].split(",")[0]),Double.parseDouble(lines[j].split(",")[1])));
        //                    }else{
        //                        i=j;
        //                        break;
        //                    }
        //                }
        //                //convert latlng to vector3
        //                List<Vector3> branchV = new ArrayList<>();
        //                for(LatLng latlng:branch){
        //                    ARWayProjection.PointD pd = ARWayProjection.toOpenGLLocation(latlng, DEFAULT_LEVEL);
        //                    branchV.add(new Vector3((pd.x-mOffsetX)*TIME_15_20,(-pd.y-mOffsetY)*TIME_15_20,DEFAULT_OPENGL_Z));
        //                }
        //                //add to mBranchPaths
        //                branchPaths.add(branchV);
        //            }
        //        }
        //        mBranchPaths.add(branchPaths);
        //        // TODO: 2016/11/8 测试北京数据 end


        for (List<List<Vector3>> paths : mBranchPaths) {
            mRenderPaths.addAll(paths);
        }
        mNaviPathDataProvider.initPath(mRenderPaths);
        mFromPos = new Vector3(mRenderPaths.get(0).get(0));
        mPreTime = System.currentTimeMillis();

        //显示第一根蚯蚓线
        processGuildLine(mStepPointIndexs.get(0));

        mRenderStrategy.updateAnimation(IRenderStrategy.AnimationType.NAVI_START);
        //4.call processSteps(IRoadNetDataProcessor to get data and create IRoadNetDataProvider something)
        //HaloLogger.logE(TAG, "initPath call processSteps(IRoadNetDataProcessor to get data and create IRoadNetDataProvider something)");
        //processSteps(0,1,2)
        //mRoadNetChangeNotifier.onRoadNetDataChange();
        // TODO: 15/11/2016 调用画不场景
        if (mPathLatLng.size() - 1 <= mPreDynamicEndIndex) {
            HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG, "");
            mNaviPathDataProvider.setEndPath();
        }
        mIsPathInited = true;
        HaloLogger.postI(ARWayConst.NECESSARY_LOG_TAG, "AMapNaviPathDataProcessor setpath eixt,total size "+mPathLatLng.size());
        return 1;
    }

    private void getShowBranchLines() {
        String[] showBranchLines = ARWayConst.BRANCH_SHOW_LINE.split("\n");
        List<List<Vector3>> branchPaths = new ArrayList<>();
        for(int i=0;i<showBranchLines.length;i++){
            if(showBranchLines[i].contains("start")){
                List<Vector3> branchPath = new ArrayList<>();
                for(int j=i+1;j<showBranchLines.length;j++){
                    if(showBranchLines[j].contains("end")){
                        i=j;
                        break;
                    } else {
                        branchPath.add(parseLatlng(Double.parseDouble(showBranchLines[j].split(",")[0]),Double.parseDouble(showBranchLines[j].split(",")[1])));
                    }
                }
                branchPaths.add(branchPath);
            }
        }
        mBranchPaths.add(branchPaths);
    }

    @Override
    public void loadNewRoad(int startIndex, int endIndex) {
        // TODO: 2016/11/8 测试北京数据 start
        HaloLogger.logE("dynamic", "dynamic loadNewRoad roadNet ,start time = " + System.currentTimeMillis());
        Log.e("ylq", "startIndex:" + startIndex + " endIndex" + endIndex);
        Log.e("ylq", "remove start");
        //1.删除掉mBranchPaths以及mBranchInPathIndexs中已经不在start和end之间的部分
        for (int i = mPreDynamicStartIndex; i < startIndex; i++) {
            if (mBranchInPathIndexs.contains(i)) {
                int index = mBranchInPathIndexs.indexOf(i);
                int removeIndex = mBranchInPathIndexs.remove(index);
                mBranchPaths.remove(index);
                Log.e("ylq", "i=" + i);
                Log.e("ylq", "index=" + index);
                Log.e("ylq", "removeIndex=" + removeIndex);
            }
        }
        Log.e("ylq", "remove end");
        //2.拉取新的部分的路网数据
        for (int i = startIndex; i < endIndex; i++) {
            if (mStepPointIndexs.contains(i)) {
                processSteps(mStepPointIndexs.indexOf(i));
            }
        }
        // TODO: 2016/11/8 测试北京数据 end


        //3.拉取对应的主路数据
        List<Vector3> dynamicPath = new ArrayList<>();
        for (LatLngOutSide latlng : mProportionMappingEngine.mapping(startIndex, endIndex)) {
            ARWayProjection.PointD pd = ARWayProjection.toOpenGLLocation(latlng, DEFAULT_LEVEL);
            dynamicPath.add(new Vector3((pd.x - mOffsetX) * TIME_15_20, (-pd.y - mOffsetY) * TIME_15_20, DEFAULT_OPENGL_Z));
        }
        mRenderPaths.clear();
        mRenderPaths.add(dynamicPath);
        for (List<List<Vector3>> paths : mBranchPaths) {
            mRenderPaths.addAll(paths);
        }
        HaloLogger.logE("dynamic", "dynamic loadNewRoad roadNet ,end time = " + System.currentTimeMillis());
        HaloLogger.logE("ylq", "loadNewRoad path size = " + mRenderPaths.size());
        mNaviPathDataProvider.updatePath(mRenderPaths);

        HaloLogger.logE("dynamic", "dynamic loadNewRoad guild line ,start time = " + System.currentTimeMillis());
        //4.更新蚯蚓线,因为主路被替换了所以需要刷新下
        processGuildLine(mStepPointIndexs.get(mCurStep));
        HaloLogger.logE("dynamic", "dynamic loadNewRoad guild line ,end time = " + System.currentTimeMillis());

        mPreDynamicEndIndex = endIndex;
        mPreDynamicStartIndex = startIndex;

        if (mPathLatLng.size() - 1 <= endIndex) {
            mNaviPathDataProvider.setEndPath();
        }
    }

    @Override
    public void setLocation(AMapNaviLocation location, Vector3 animPos, double animDegrees) {
        if (mIsPathInited) {
            mLocation = location;
            //call DataProvider to update anim with cur location
            if (mFromPos == null) {
                mFromPos = convertLocation(location, mCurIndexInPath);
                mFromDegrees = MathUtils.convertAMapBearing2OpenglBearing(location.getBearing());
                mPreTime = location.getTime();
            } else {
                long duration = location.getTime() - mPreTime;
                if(duration <= 250){
                    return;
                }
                mPreTime = location.getTime();
                if (mToPos != null) {
                    mFromPos = animPos;
                    mFromDegrees = Math.toDegrees(animDegrees);
                    mFromDegrees = mFromDegrees < 0 ? mFromDegrees + 360 : mFromDegrees;
                } else {
                    mFromDegrees = MathUtils.convertAMapBearing2OpenglBearing(location.getBearing());
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
            mDynamicLoader.updateCurPoint(mCurIndexInPath);
            //1.Calculate the distance of maneuver point and get road class.
            int distanceOfMP = naviInfo.getCurStepRetainDistance();
            AMapNaviLink curLink = mAMapNavi.getNaviPath().getSteps().get(naviInfo.getCurStep())
                    .getLinks().get(naviInfo.getCurLink());
            mRenderStrategy.updateCurrentRoadInfo(curLink.getRoadClass(), distanceOfMP, naviInfo.getPathRetainDistance());
            /*//2.dynamic load with current path retain distance
            HaloLogger.logE(TAG,"mLeftMeterLength - naviInfo.getPathRetainDistance() = "+(mLeftMeterLength - naviInfo.getPathRetainDistance()));
            HaloLogger.logE(TAG,"NEED_LOAD_METER = "+NEED_LOAD_METER);
            if(mLeftMeterLength - naviInfo.getPathRetainDistance() >= NEED_LOAD_METER){
                if(mCurIndexInSplitPoints<mSplitPointIndexs.size()-1) {
                    //dynamic load
                    HaloLogger.logE(TAG,"dynamic load");
                    int start = mSplitPointIndexs.get(mCurIndexInSplitPoints);
                    int end = mSplitPointIndexs.get(mCurIndexInSplitPoints + 1);
                    HaloLogger.logE(TAG,"start = "+start);
                    HaloLogger.logE(TAG,"end = "+end);
                    mNaviPathDataProvider.updatePath(mDouglasPath.subList(start, end + 1));
                    mCurIndexInSplitPoints++;
                    mLeftMeterLength = naviInfo.getPathRetainDistance();
                }
            }*/
            if (naviInfo.getCurStep() > mCurStep) {
                //3.Guide line change
                processGuildLine(mStepPointIndexs.get(naviInfo.getCurStep()));
                mCurStep = naviInfo.getCurStep();
            }
            testProcessTrafficLight(naviInfo.getCurStep(), naviInfo.getCurLink());
        }
    }

    private void processTrafficLight(int curStep, int curLink) {
        if (mAMapNavi == null) {
            return;
        }
        AMapNaviPath path = mAMapNavi.getNaviPath();
        int linkIndex = absoluteLinkIndex(path, curStep, curLink);
        int stepSize = path.getSteps().size();
        int maxIndex = absoluteLinkIndex(path,stepSize-1,path.getSteps().get(stepSize-1).getLinks().size()-1);
        if ((linkIndex - mLastLink) > 0 || mLastLink == 0) {
            List<AMapNaviLink> links = new ArrayList<>();
            mLastLink = findLinks(path, curStep, curLink, NUMBER_TRAFFIC_LIGHT,links);
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG,String.format("curStep %s ,curLink %s, linkIndex %s ,mLastLink %s,link size %s,maxIndex %s",curStep,curLink,linkIndex,mLastLink,links.size(),maxIndex));
            List<Vector3> lights = new ArrayList<>();
            int linkCnt = maxIndex==mLastLink?links.size():links.size()-1;
            for(int i = 0; i < linkCnt; i++){//links的最后一个一般link不渲染，作为判断，只有一个值时当到达目的地处理
                AMapNaviLink link=links.get(i);
                if (link.getCoords().size()>1 && link.getTrafficLights()) {
                    int start = link.getCoords().size()-2;
                    int end = link.getCoords().size()-1;
                    NaviLatLng startLatlng = link.getCoords().get(start);
                    NaviLatLng endLatlng = link.getCoords().get(end);
                    Vector3 p1 = parseLatlng(startLatlng.getLatitude(),startLatlng.getLongitude());
                    Vector3 p0 = parseLatlng(endLatlng.getLatitude(),endLatlng.getLongitude());
                    List<Vector3> posList = new ArrayList<>();
                    List<Vector3> resultList = new ArrayList<>();
                    posList.add(p1);
                    posList.add(p0);
                    float radius = RoadRenderOption.TRAFFIC_DEVIATION_DISTANCE;
                    Vector3 trafficPos = new Vector3();
                    if(i < links.size()-1){
                        AMapNaviLink nextLink = links.get(i+1);
                        Vector3 p3 = null;
                        if (nextLink != null && nextLink.getCoords().size()>0) {
                            NaviLatLng netLatLng = nextLink.getCoords().get(0);
                            p3 = parseLatlng(netLatLng.getLatitude(),netLatLng.getLongitude());
                        }
                        if (p3 != null) {
                            posList.add(p3);
                            MathUtils.translatePath(posList,resultList,radius);
                            if (resultList.size() >= 2){
                                trafficPos.setAll(resultList.get(1));
                            }else {
                                HaloLogger.logE(ARWayConst.ERROR_LOG_TAG,String.format("processTrafficLight called , traffic error,resultList %s,posList %s",resultList.size(),posList));
                            }
                        }
                    }else {
                        MathUtils.translatePath(posList,resultList,radius);
                        if (resultList.size() >= 2){
                            trafficPos.setAll(resultList.get(1));
                        }else {
                            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG,String.format("processTrafficLight called , last path traffic error ,resultList %s , posList %s",resultList.size(),posList));
                        }
                    }

                    trafficPos = p0;

                    String record_tag = "light_record_tag";
                    String split_tag = "python";
                    Vector3 position = trafficPos;
                    Log.e(record_tag,String.format(" %s %s , %s , %s",split_tag,position.x,position.y,position.z));


                    lights.add(trafficPos);
                    HaloLogger.logE(ARWayConst.ERROR_LOG_TAG,String.format("processTrafficLight called ,absolute index %s,curStep %s, curLink %s, lanlng %s , position is %s",mLastLink,curStep, curLink,endLatlng,trafficPos));
                    /*
                    double distance = Vector3.distanceTo(p0, p1);
                    PointD position = new PointD();
                    position.x = p0.x + (p1.x - p0.x) * radius / distance;
                    position.y = p0.y + (p1.y - p0.y) * radius / distance;
                    MathUtils.rotateAround(p0.x, p0.y, position.x, position.y, position, 90);
                    lights.add(new Vector3(position.x, position.y, DEFAULT_OPENGL_Z));*/


                }
            }
            mNaviPathDataProvider.setTrafficLight(lights);
//            mLastLink = linkIndex>0 ? linkIndex-1:linkIndex;
            mLastLink = mLastLink>0 ? mLastLink-1:0;
        }
    }

    private void findTrafficLight(AMapNaviPath path) {
        String record_tag = "light_record_tag";
        String split_tag = "python";
        int lightCnt = 0;
        int calLightCnt = 0;
        int linkCnt = 0;
        if (path != null) {
            Log.e(record_tag,"path start");
            for (int i = 0; i < path.getSteps().size(); i++) {
                AMapNaviStep step=path.getSteps().get(i);
                for (int j = 0; j < step.getLinks().size(); j++) {
                    AMapNaviLink link=step.getLinks().get(j);
                    for(NaviLatLng latLng :link.getCoords()){
                        Vector3 position = parseLatlng(latLng.getLatitude(),latLng.getLongitude());
                        Log.e(record_tag,String.format(" %s %s , %s , %s",split_tag,position.x,position.y,position.z));
                    }
                }
            }
            Log.e(record_tag,"path end");
            Log.e(record_tag,"light start");
            mLastStep=-1;
            for (int i = 0; i < path.getSteps().size(); i++) {
                calLightCnt += testProcessTrafficLight(i,0);
                AMapNaviStep step=path.getSteps().get(i);
                for (int j = 0; j < step.getLinks().size(); j++) {
                    int abIndex = absoluteLinkIndex(path,i,j);
                    AMapNaviLink link=step.getLinks().get(j);
                    HaloLogger.logE(ARWayConst.ERROR_LOG_TAG,String.format("findTrafficLight step %s,link %s,link size %s ,light %s ,abIndex %s , linkCnt %s",i,j,step.getLinks().size(),link.getTrafficLights(),abIndex,linkCnt));
                    if (link.getTrafficLights()){
                        NaviLatLng pos = link.getCoords().get(link.getCoords().size()-1);
                        lightCnt++;
//                        mark.setPosition(new LatLng(pos.getLatitude(),pos.getLongitude()));
                    }
                    linkCnt++;
                }
            }
            Log.e(record_tag,String.format("light end ,lightCnt %s,calLightCnt %s",lightCnt,calLightCnt));
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG,String.format("findTrafficLight linkCnt %s,light cnt %s",linkCnt,lightCnt));
        }

    }

    private int testProcessTrafficLight(int curStep, int curLink) {
        int lightCnt = 0;
        if (mAMapNavi == null) {
            return lightCnt;
        }
        String tag = "trafficLight_tag";
        AMapNaviPath path = mAMapNavi.getNaviPath();
        int callCnt =0;
        if(curStep>mLastStep){
            mLastStep = curStep;
            String record_tag = "light_record_tag";
            String split_tag = "python";
            List<Vector3> lights = new ArrayList<>();
            int stepSize = path.getSteps().size();
            AMapNaviStep step = path.getSteps().get(curStep);
            int linkSize = step.getLinks().size();
            for (int j = 0; j < linkSize; j++) {
                AMapNaviLink link=step.getLinks().get(j);
                if(!link.getTrafficLights()){
                    continue;
                }
                int end = link.getCoords().size()-1;
                int start = link.getCoords().size()-2;
                NaviLatLng startLatlng = link.getCoords().get(start);
                NaviLatLng endLatlng = link.getCoords().get(end);
                Vector3 prePos = parseLatlng(startLatlng.getLatitude(),startLatlng.getLongitude());
                Vector3 arround = parseLatlng(endLatlng.getLatitude(),endLatlng.getLongitude());

                Vector3 position = new Vector3();
                final double radius = RoadRenderOption.TRAFFIC_DEVIATION_DISTANCE;
                double distance = Vector3.distanceTo(prePos, arround);
                position.x = arround.x + (prePos.x - arround.x) * radius / distance;
                position.y = arround.y + (prePos.y - arround.y) * radius / distance;
//                MathUtils.rotateAround(arround.x, arround.y, position.x, position.y, position, Math.PI/2);

                List<AMapNaviLink> links = new ArrayList<>();
                links.add(link);
                AMapNaviLink nextLink= null;
                if (j+1<linkSize){
                    nextLink = step.getLinks().get(j+1);
                    links.add(nextLink);
                }else if(curStep+1<stepSize){
                    nextLink = path.getSteps().get(curStep+1).getLinks().get(0);
                    links.add(nextLink);
                }
                int  distCnt = 0;
                for (int i = 0; i < 8; i++) {
                    boolean postionOk = true;
                    MathUtils.rotateAround(arround.x, arround.y, position.x, position.y, position, Math.PI/4);
                    for(AMapNaviLink lightLink:links){
                        for (NaviLatLng latLng :lightLink.getCoords()){
                            Vector3 vector = parseLatlng(latLng.getLatitude(),latLng.getLongitude());
                            double vDistance = Vector3.distanceTo(vector,position);
                            distCnt++;
                            if((vDistance-radius) < -0.1){
//                                Log.e(tag, "testProcessTrafficLight: distance is "+vDistance);
                                postionOk = false;
                            }
                            if(!postionOk){
                                break;
                            }
                        }
                        if(!postionOk){
                            break;
                        }
                    }
                    if(postionOk){
                        lightCnt++;
                        Log.e(record_tag,String.format(" %s %s , %s , %s",split_tag,position.x,position.y,position.z));
                        lights.add(position);
                        break;
                    }
                }
//                Log.e(tag,String.format(" step %s call cnt %s ",curStep,++callCnt));
            }
            mNaviPathDataProvider.setTrafficLight(lights);
        }
        return lightCnt;
    }

    private Vector3 parseLatlng(double lat, double lng) {
        ARWayProjection.PointD pd = ARWayProjection.toOpenGLLocation(new LatLngOutSide(lat, lng), DEFAULT_LEVEL);
        Vector3 position = new Vector3((pd.x - mOffsetX) * TIME_15_20, (-pd.y - mOffsetY) * TIME_15_20, DEFAULT_OPENGL_Z);
        return position;
    }

    /**
     * 找到连续的几个link
     * @param path
     * @param curStep
     * @param curLink
     * @param cnt
     * @param links
     * @return
     */
    private int  findLinks(AMapNaviPath path, int curStep, int curLink, int cnt,List<AMapNaviLink> links) {
        int linkIndex = 0;
        int has = 0;
        int startLink = curLink;
        int linkCnt = 0;
        boolean over = false;
        int i=0,j=0;
        for (i = curStep; i < path.getStepsCount(); i++) {
            AMapNaviStep step = path.getSteps().get(i);
            linkCnt = step.getLinks().size();
            for (j = startLink; j < linkCnt; j++) {
                startLink = 0;
                links.add(step.getLinks().get(j));
                if (++has >= cnt) {
                    over = true;
                    break;
                }

            }
            if (over) {
                break;
            }
        }
//        int stepIndice = i>curStep?i-1:curStep;
//        int linkIndice = j>0?j-1:0;
//        if (over){
//            stepIndice = i;
//            linkIndice = j;
//        }
        linkIndex  = absoluteLinkIndex(path, curStep, curLink)+has;
        return linkIndex>0?linkIndex-1:0;
    }

    /**
     * 返回绝对下标，某个结点前面的个数，不包括当前点
     * @param path
     * @param curStep
     * @param curLink
     * @return
     */
    private int absoluteLinkIndex(AMapNaviPath path, int curStep, int curLink) {
        if (path.getStepsCount() < curStep) {
            return -1;
        }
        int index = 0;
        for (int i = 0; i < curStep; i++) {
            index += path.getSteps().get(i).getLinks().size();
        }
        if (curLink >= 0 && curLink < path.getSteps().get(curStep).getLinks().size()) {
            index += curLink;
        }
//        index = index>0?index-1:0;
        return index;
    }


    private void processGuildLine(int curIndexInPath) {
        List<ARWayProjection.PointD> guildLine = mProportionMappingEngine.mappingGuideV(curIndexInPath);
        if (guildLine != null) {
            List<Vector3> guildLineVector3 = new ArrayList<>();
            for (ARWayProjection.PointD pointD : guildLine) {
                Vector3 v = new Vector3((pointD.x - mOffsetX) * TIME_15_20, (-pointD.y - mOffsetY) * TIME_15_20, DEFAULT_OPENGL_Z);
                guildLineVector3.add(v);
            }
            mNaviPathDataProvider.setGuildLine(guildLineVector3);
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

    int count = 0;

    /**
     * TODO:暂时不对路网数据进行处理
     * 访问路网模块获取指定steps的路网数据
     */
    private void processSteps(int... stepIndexs) {
        HaloLogger.logE(TAG, "process steps start " + (count + 1));
        Log.e("ylq", "add start");
        for (Integer stepIndex : stepIndexs) {
            if (mAlreadyLoadStep.contains(stepIndex)) {
                continue;
            }
            mAlreadyLoadStep.add(stepIndex);
            //1.根据stepIndex构建数据
            //  1.机动点前后道路link形式(暂时由一条link表示整个导航路)
            //  2.每个link的info
            //  3.机动点经纬度
            //  4.切割机动点前后N*N范围的点,且取到边缘点
            //  5.使用返回的数据替代原先的数据(主路部分)
            Size2iOutside szCover = new Size2iOutside();
            szCover.width = 800;
            szCover.height = 800;

            List<List<LatLngOutSide>> links = new ArrayList<>();
            List<LatLngOutSide> link = new ArrayList<>();
            LatLngOutSide centerLatLng = new LatLngOutSide();
            centerLatLng.lat = mPathLatLng.get(mStepPointIndexs.get(stepIndex)).lat;
            centerLatLng.lng = mPathLatLng.get(mStepPointIndexs.get(stepIndex)).lng;
            LatLng[] point8 = new LatLng[8];
            int[] se = getPartPathFromCover(szCover, stepIndex, mPathLatLng, centerLatLng, link, point8);
            int breakStart = se[0];
            int breakEnd = se[1];
            if (breakStart < mPreEndBreak) {
                //1.合并两个step,问题是按照当前算法路线越长越复杂,越容易匹配到错误的道路
                /*LatLng lastEnd = mPathLatLng.get(mPreEndBreak);
                LatLng thisStart = mPathLatLng.get(breakStart);
                szCover.width = (int) (szCover.width*2-(Math.max(Math.abs(lastEnd.latitude - thisStart.latitude), Math.abs(lastEnd.longitude - thisStart.longitude)))/PIXEL_2_LATLNG);
                szCover.height = (int) (szCover.height*2-(Math.max(Math.abs(lastEnd.latitude - thisStart.latitude), Math.abs(lastEnd.longitude - thisStart.longitude)))/PIXEL_2_LATLNG);
                HaloLogger.logE(TAG,"width="+szCover.width);
                HaloLogger.logE(TAG,"height="+szCover.height);
                //将扩充后的窗口中的点也添加到link中
                for(int i=breakStart;i>=mPreStartBreak;i--){
                    link.add(0,new LatLngOutSide(mPathLatLng.get(i).latitude,mPathLatLng.get(i).longitude));
                }
                breakStart = mPreStartBreak;
                mPreEndBreak = breakEnd;*/
                //2.暂时先跳过该路口不做处理,因为合并处理时得不到岔路,问题是可能会跳过多个路口,导致路口显示过少
                //continue;
                //3.当覆盖时,缩小窗口,同时缩短Path到新窗口的边缘
                LatLngOutSide preCenterLatLng = new LatLngOutSide();
                preCenterLatLng.lat = mPathLatLng.get(mStepPointIndexs.get(mPreStepIndex)).lat;
                preCenterLatLng.lng = mPathLatLng.get(mStepPointIndexs.get(mPreStepIndex)).lng;
                double offsetCover = szCover.width - (Math.max(Math.abs(centerLatLng.lat - preCenterLatLng.lat), Math.abs(centerLatLng.lng - preCenterLatLng.lng))) / PIXEL_2_LATLNG;
                HaloLogger.logE(TAG, "szCover.width=" + szCover.width);
                HaloLogger.logE(TAG, "max=" + (Math.max(Math.abs(centerLatLng.lat - preCenterLatLng.lat), Math.abs(centerLatLng.lng - preCenterLatLng.lng))) / PIXEL_2_LATLNG);
                HaloLogger.logE(TAG, "offsetCover=" + offsetCover);
                HaloLogger.logE(TAG, "pre center=" + preCenterLatLng.lat + "," + preCenterLatLng.lng);
                HaloLogger.logE(TAG, "cur center=" + centerLatLng.lat + "," + centerLatLng.lng);
                //TODO : 为什么会有明明很远的两个点,但是却判断到了相交导致offsetCover算出来是个负数的情况??????
                if (offsetCover < 0 || offsetCover >= szCover.width / 2) {
                    continue;
                }
                szCover.width -= 2 * offsetCover;
                szCover.height -= 2 * offsetCover;
                se = getPartPathFromCover(szCover, stepIndex, mPathLatLng, centerLatLng, link, point8);
                breakStart = se[0];
                breakEnd = se[1];

            }
            HaloLogger.logE("cover", "szCover : " + szCover.width + "," + szCover.height);
            HaloLogger.logE("cover", "breakStart : " + breakStart);
            HaloLogger.logE("cover", "breakEnd : " + breakEnd);
            HaloLogger.logE("cover", "cover cross start");
            for (LatLng latlng : point8) {
                HaloLogger.logE("cover", latlng.latitude + "," + latlng.longitude);
            }
            HaloLogger.logE("cover", "cover cross end");
            HaloLogger.logE("cover", "main cross start");
            for (LatLngOutSide latlng : link) {
                HaloLogger.logE("cover", latlng.lat + "," + latlng.lng);
            }
            HaloLogger.logE("cover", "main cross end");

            HaloLogger.logE(TAG, "width=" + szCover.width + ",height=" + szCover.height);
            if (breakEnd == 0) {
                breakEnd = mPathLatLng.size() - 1;
            }
            HaloLogger.logE(TAG, "breakStart=" + breakStart + ",breakEnd=" + breakEnd);
            links.add(link);

            List<LinkInfoOutside> linkInfos = new ArrayList<>();

            LatLngOutSide centerPoint = new LatLngOutSide();
            centerPoint.lat = centerLatLng.lat;
            centerPoint.lng = centerLatLng.lng;

            List<List<LatLngOutSide>> crossLinks = new ArrayList<>();
            List<LatLngOutSide> mainRoad = new ArrayList<>();
            List<Integer> crossPointIndexs = new ArrayList<>();


            //TODO : data for daoge

            StringBuilder sb = new StringBuilder();
            sb.append((++count) + " ");
            sb.append(centerPoint.lat + " ");
            sb.append(centerPoint.lng + " ");
            sb.append(szCover.width + " ");
            sb.append(szCover.height + " ");
            for (LatLngOutSide latlng : links.get(0)) {
                sb.append(latlng.lat + " ");
                sb.append(latlng.lng + " ");
            }
            HaloLogger.logE("daoge", sb.toString().trim());
            sb_daoge.append(sb.toString() + "\n");


            HaloLogger.logE(TAG, "into jni");
            int res = mEnlargedCrossProcess.updateCrossLinks(links, linkInfos, centerPoint, szCover, crossLinks, mainRoad, crossPointIndexs);
            HaloLogger.logE(TAG, "outto jni");
            HaloLogger.logE(TAG, "res=" + res + ",and cross links size=" + crossLinks.size());

            if (res == 0) {
                HaloLogger.logE("daoge", "mainRoad point size = " + mainRoad.size());
                sb_daoge.append("mainRoad point size = " + mainRoad.size() + "\n");
                HaloLogger.logE("daoge", "\t" + mainRoad);
                sb_daoge.append("\t" + mainRoad + "\n");
                HaloLogger.logE("daoge", "crossLinks.size = " + crossLinks.size());
                sb_daoge.append("crossLinks.size = " + crossLinks.size() + "\n");
                for (List<LatLngOutSide> cross : crossLinks) {
                    HaloLogger.logE("daoge", "\t" + cross);
                    sb_daoge.append("\t" + cross + "\n");
                }
                HaloLogger.logE("daoge", "main and road inter points size = " + crossPointIndexs.size());
                sb_daoge.append("main and road inter points size = " + crossPointIndexs.size() + "\n");
                HaloLogger.logE("daoge", "\t" + crossPointIndexs.subList(0, crossPointIndexs.size() - 1));
                sb_daoge.append("\t" + crossPointIndexs.subList(0, crossPointIndexs.size() - 1) + "\n");
            } else {
                sb_daoge.append("mainRoad point size = " + 0 + "\n");
                sb_daoge.append("crossLinks.size = " + 0 + "\n");
                sb_daoge.append("main and road inter points size = " + 0 + "\n");
                sb_daoge.append("\t[]" + "\n");
            }
            if (res == 0 && crossLinks.size() > 0) {
                HaloLogger.logE(TAG, "jni get road net success,crossLinks size=" + crossLinks.size() + ",mainRoad size=" + mainRoad.size());
                mPreStartBreak = breakStart;
                mPreEndBreak = breakEnd;
                mPreStepIndex = stepIndex;
                //2.将经纬度数据处理转换成Vector3数据,将主路拼接到原主路上,将其他link添加到路网中
                //2.1处理岔路--抽析--转换--填充到集合中
                List<List<Vector3>> branchPaths = new ArrayList<>();
                for (int i = 0; i < crossLinks.size(); i++) {
                    List<LatLngOutSide> crossLink = crossLinks.get(i);
                    //抽析岔路
                    List<Vector3> crossLinkVector3 = new ArrayList<>();
                    List<PointF> returnPath = new ArrayList<>();
                    List<PointF> originalPath = new ArrayList<>();
                    List<Vector3> pathV3 = new ArrayList<>();
                    for (LatLngOutSide latlng : crossLink) {
                        ARWayProjection.PointD pd = ARWayProjection.toOpenGLLocation(latlng, DEFAULT_LEVEL);
                        pathV3.add(new Vector3(pd.x, -pd.y, DEFAULT_OPENGL_Z));
                    }
                    for (Vector3 v : pathV3) {
                        originalPath.add(new PointF((float) v.x, (float) v.y));
                    }
                    Douglas.rarefyGetPointFs(new ArrayList<Integer>(), returnPath, originalPath, RAREFY_PIXEL_COUNT / ARWayProjection.K);
                    HaloLogger.logE(TAG, "ori size = " + originalPath.size());
                    HaloLogger.logE(TAG, "ret size = " + returnPath.size());
                    for (PointF p : returnPath) {
                        crossLinkVector3.add(new Vector3((p.x - mOffsetX) * TIME_15_20, (p.y - mOffsetY) * TIME_15_20, DEFAULT_OPENGL_Z));
                    }
                    /*List<Vector3> crossLinkVector3 = new ArrayList<>();
                    for (LatLngOutSide latlng : crossLink) {
                        ARWayProjection.PointD pd = ARWayProjection.toOpenGLLocation(new LatLng(latlng.lat, latlng.lng), DEFAULT_LEVEL);
                        crossLinkVector3.add(new Vector3((pd.x - mOffsetX) * TIME_15_20, (-pd.y - mOffsetY) * TIME_15_20, DEFAULT_OPENGL_Z));
                    }*/
                    HaloLogger.logE(TAG, "crossLink cross start");
                    sb_helong.append("crossLink cross start" + "\n");
                    for (LatLngOutSide latlng : crossLink) {
                        HaloLogger.logE(TAG, latlng.lat + "," + latlng.lng);
                        sb_helong.append(latlng.lat + "," + latlng.lng + "\n");
                    }
                    HaloLogger.logE(TAG, "crossLink cross end");
                    sb_helong.append("crossLink cross end" + "\n");

                    //此links代表的是岔路
                    branchPaths.add(crossLinkVector3);
                }
                mBranchPaths.add(branchPaths);
                mBranchInPathIndexs.add(mStepPointIndexs.get(stepIndex));
                /*HaloLogger.logE(TAG, "crossLink cross start");
                for (LatLngOutSide latlng : link) {
                    HaloLogger.logE(TAG, latlng.lat + "," + latlng.lng);
                }
                HaloLogger.logE(TAG, "crossLink cross end");
                HaloLogger.logE(TAG, "crossLink cross start");
                for (LatLngOutSide latlng : mainRoad) {
                    HaloLogger.logE(TAG, latlng.lat + "," + latlng.lng);
                }
                HaloLogger.logE(TAG, "crossLink cross end");*/
                //2.2处理新的中心点角标,以及重新调整保存主路与岔路的交点所在的数组
                int newCenterIndex = crossPointIndexs.remove(crossPointIndexs.size() - 1);
                if (!crossPointIndexs.contains(newCenterIndex)) {
                    for (int i = 0; i < crossPointIndexs.size(); i++) {
                        if (crossPointIndexs.get(i) > newCenterIndex) {
                            crossPointIndexs.add(i, newCenterIndex);
                        }
                    }
                }
                HaloLogger.logE(TAG, "new center index = " + newCenterIndex);
                //2.2处理主路以及对主路部分进行抽析
                //preCrossPoints
                mProportionMappingEngine.mapping(mainRoad, breakStart + 1, breakEnd, crossPointIndexs);
                HaloLogger.logE("ProportionMappingEngine", "cross start");
                for (LatLngOutSide latlng : mProportionMappingEngine.mapping(breakStart + 1, breakEnd)) {
                    HaloLogger.logE("ProportionMappingEngine", latlng.lat + "," + latlng.lng);
                }
                HaloLogger.logE("ProportionMappingEngine", "cross end");
                //HaloLogger.logE("daoge", "mainRoad_render");
                //HaloLogger.logE("daoge","\t"+mProportionMappingEngine.getRenderPath(breakStart, breakEnd));

                //2.3对岔路数据进行前一个点的填充
                /*for(int i=0;i<mBranchPaths.get(mBranchPaths.size()-1).size();i++){
                    List<LatLngOutSide> latlngs = crossLinks.get(i);
                    int pre_start = mProportionMappingEngine.mapping(latlngs.get(0))-1;
                    if(pre_start!=-1) {
                        int end = pre_start + 1;
                        LatLngOutSide latlng = mProportionMappingEngine.getRenderPart(pre_start, end).get(0);
                        mBranchPaths.get(mBranchPaths.size() - 1).get(i).add(0, parseLatlng(latlng.lat, latlng.lng));
                    }
                }*/
                /*HaloLogger.logE(TAG, "jiaodian cross start");
                for (int i = 0; i < crossPointIndexs.size(); i++) {
                    HaloLogger.logE(TAG, mainRoad.get(crossPointIndexs.get(i)).lat + "," + mainRoad.get(crossPointIndexs.get(i)).lng);
                }
                HaloLogger.logE(TAG, "jiaodian cross end");*/
            }
        }
        Log.e("ylq", "add end");
        HaloLogger.logE(TAG, "process steps end");
    }

    /**
     * 根据覆盖区域大小,中心点角标,以及原Path求出该覆盖区域的部分Path
     * 并填充到out中
     *
     * @param szCover
     * @param stepIndex
     * @param path
     * @param centerLatLng
     * @param link         [out]
     * @return
     */
    private int[] getPartPathFromCover(Size2iOutside szCover, int stepIndex, List<LatLngOutSide> path, LatLngOutSide centerLatLng, List<LatLngOutSide> link, LatLng[] point8) {
        link.clear();
        double latlng_width = szCover.width * PIXEL_2_LATLNG;
        double latlng_height = szCover.height * PIXEL_2_LATLNG;
        //LatLng[] point8 = new LatLng[8];
        //上,右,下,左
        point8[0] = new LatLng(centerLatLng.lat - latlng_width / 2, centerLatLng.lng - latlng_height / 2);
        point8[1] = new LatLng(centerLatLng.lat + latlng_width / 2, centerLatLng.lng - latlng_height / 2);
        point8[2] = new LatLng(centerLatLng.lat + latlng_width / 2, centerLatLng.lng - latlng_height / 2);
        point8[3] = new LatLng(centerLatLng.lat + latlng_width / 2, centerLatLng.lng + latlng_height / 2);
        point8[4] = new LatLng(centerLatLng.lat + latlng_width / 2, centerLatLng.lng + latlng_height / 2);
        point8[5] = new LatLng(centerLatLng.lat - latlng_width / 2, centerLatLng.lng + latlng_height / 2);
        point8[6] = new LatLng(centerLatLng.lat - latlng_width / 2, centerLatLng.lng + latlng_height / 2);
        point8[7] = new LatLng(centerLatLng.lat - latlng_width / 2, centerLatLng.lng - latlng_height / 2);
        /*HaloLogger.logE(TAG, "crossLink cross start");
        for (LatLng latlng : point8) {
            HaloLogger.logE(TAG, latlng.latitude + "," + latlng.longitude);
        }
        HaloLogger.logE(TAG, "crossLink cross end");*/
        link.add(new LatLngOutSide(centerLatLng.lat, centerLatLng.lng));
        //breakStart:JNI部分数据返回后用于拼接抽析数据部分的开始下标
        //breakEnd:结束下标
        int breakStart = 0;
        for (int i = mStepPointIndexs.get(stepIndex) - 1; i >= 0; i--) {
            LatLngOutSide latlng = path.get(i);
            double offsetLat = Math.abs(centerLatLng.lat - latlng.lat);
            double offsetLng = Math.abs(centerLatLng.lng - latlng.lng);
            if (offsetLat >= latlng_width / 2 || offsetLng >= latlng_height / 2) {
                if (offsetLat == latlng_width / 2 || offsetLng == latlng_height / 2) {
                    link.add(0, new LatLngOutSide(latlng.lat, latlng.lng));
                    breakStart = i <= 0 ? 0 : i - 1;
                } else {
                    LatLngOutSide preLatLng = path.get(i + 1);
                    for (int j = 0; j < point8.length; j += 2) {
                        LatLng lineStart = point8[j];
                        LatLng lineEnd = point8[j + 1];
                        Vector3 result = new Vector3();
                        int res = MathUtils.getIntersection(new Vector3(latlng.lat, latlng.lng, 0),
                                                            new Vector3(preLatLng.lat, preLatLng.lng, 0),
                                                            new Vector3(lineStart.latitude, lineStart.longitude, 0),
                                                            new Vector3(lineEnd.latitude, lineEnd.longitude, 0),
                                                            result);
                        if (res == 1) {
                            link.add(0, new LatLngOutSide(result.x, result.y));
                            breakStart = i;
                            break;
                        }
                    }
                }
                break;
            } else if (i == 0) {
                breakStart = 0;
                link.add(0, new LatLngOutSide(latlng.lat, latlng.lng));
                break;
            } else {
                link.add(0, new LatLngOutSide(latlng.lat, latlng.lng));
            }
        }
        int breakEnd = 0;
        for (int i = mStepPointIndexs.get(stepIndex) + 1; i < path.size(); i++) {
            LatLngOutSide latlng = path.get(i);
            double offsetLat = Math.abs(centerLatLng.lat - latlng.lat);
            double offsetLng = Math.abs(centerLatLng.lng - latlng.lng);
            if (offsetLat >= latlng_width / 2 || offsetLng >= latlng_height / 2) {
                if (offsetLat == latlng_width / 2 || offsetLng == latlng_height / 2) {
                    link.add(new LatLngOutSide(latlng.lat, latlng.lng));
                    breakEnd = i >= path.size() - 1 ? path.size() - 1 : i + 1;
                    HaloLogger.logE(TAG, "offset bigger than width or height,offsetLat == latlng_width/2");
                } else {
                    LatLngOutSide preLatLng = path.get(i - 1);
                    for (int j = 0; j < point8.length; j += 2) {
                        LatLng lineStart = point8[j];
                        LatLng lineEnd = point8[j + 1];
                        Vector3 result = new Vector3();
                        int res = MathUtils.getIntersection(new Vector3(latlng.lat, latlng.lng, 0),
                                                            new Vector3(preLatLng.lat, preLatLng.lng, 0),
                                                            new Vector3(lineStart.latitude, lineStart.longitude, 0),
                                                            new Vector3(lineEnd.latitude, lineEnd.longitude, 0),
                                                            result);
                        if (res == 1) {
                            link.add(new LatLngOutSide(result.x, result.y));
                            breakEnd = i;
                            break;
                        }
                    }
                    HaloLogger.logE(TAG, "offset bigger than width or height,res == ???");
                }
                break;
            } else if (i == path.size() - 1) {
                HaloLogger.logE(TAG, "offset bigger than width or height,i == path.size()-1");
                breakEnd = path.size() - 1;
                link.add(new LatLngOutSide(latlng.lat, latlng.lng));
                break;
            } else {
                link.add(new LatLngOutSide(latlng.lat, latlng.lng));
            }
        }
        return new int[]{breakStart, breakEnd};
    }

    /**
     * 默认准备三级数据,(20,18,16)当渲染层每申请一层数据,除了将数据返回外都判断
     * 是否需要替换数据,例如渲染层申请了16级的数据,那么此时就需要将20级数据替换成15级
     * TODO 这种方案仅适用于渲染层会顺序申请数据,而不是跨等级申请
     */
    private void prepareLevelData() {

    }

    private int fromOriIndex2RenderIndex(List<Integer> pointIndexsToKeep, int oriIndex, boolean behindMe) {
        for (int i = 0; i < pointIndexsToKeep.size(); i++) {

            if (!(pointIndexsToKeep.get(i) < oriIndex)) {
                if (behindMe) {
                    return i;
                } else {
                    return i == 0 ? 0 : i - 1;
                }
            }
        }
        return oriIndex;
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
    public Vector3 convertLocation(AMapNaviLocation location, int curIndex) {
        LatLngOutSide latlng = new LatLngOutSide(location.getCoord().getLatitude(), location.getCoord().getLongitude());
        //latlng = mProportionMappingEngine.mapping(latlng, curIndex);
        //ARWayProjection.PointD pointD = ARWayProjection.toOpenGLLocation(latlng, DEFAULT_LEVEL);
        //不使用上面那种先映射到LatLng,再转换成opengl坐标,而是采用下面这种直接映射到opengl坐标的原因:
        //  如果映射的结果是LatLng,那么因为计算产生的微小误差就会被放大(经纬度对经度要求非常高),因此此处采取
        //  取映射后的opengl坐标,这样因为LatLng产生的误差就会被转换成opengl之前消除
        ARWayProjection.PointD pointD = mProportionMappingEngine.mappingV(latlng, curIndex);
        Vector3 v = new Vector3(
                (pointD.x - mOffsetX) * TIME_15_20,
                (-pointD.y - mOffsetY) * TIME_15_20,
                DEFAULT_OPENGL_Z);
        /*for (int i = 0; i < mPointIndexsToKeep.size(); i++) {
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
        }*/

        //根据不同的道路等级,拿到的GPS转换到的opengl点也需要做响应的转换,默认情况下factor=1
        int factor = mNaviPathDataProvider.getCurDataLevelFactor();
        v.x /= factor;
        v.y /= factor;
        v.z /= factor;
        double offsetX = mNaviPathDataProvider.getCurOffsetX();
        double offsetY = mNaviPathDataProvider.getCurOffsetY();
        v.x -= offsetX;
        v.y -= offsetY;
        return v;
    }

    public Vector3 convertLocation(AMapNaviLocation location, int curPoint, int curStep) {
        return convertLocation(location,getIndexInPath(curPoint,curStep));
    }

    public double getDistWithFrontCar(AMapNaviLocation location) {
        return mLocation == null ? 0:AMapUtils.calculateLineDistance(new LatLng(location.getCoord().getLatitude(),location.getCoord().getLongitude()),
                new LatLng(mLocation.getCoord().getLatitude(),mLocation.getCoord().getLongitude()));
    }

    /**
     * 获取用于车道偏移的一段Path
     * @return
     */
    public List<Vector3> getCurPathPart() {
        List<ARWayProjection.PointD> listP = mProportionMappingEngine.mappingPart(mCurIndexInPath,200);
        if (listP != null) {
            List<Vector3> listV = new ArrayList<>();
            for (ARWayProjection.PointD pointD : listP) {
                Vector3 v = new Vector3((pointD.x - mOffsetX) * TIME_15_20, (-pointD.y - mOffsetY) * TIME_15_20, DEFAULT_OPENGL_Z);
                listV.add(v);
            }
            return listV;
        }
        return null;
    }
}
