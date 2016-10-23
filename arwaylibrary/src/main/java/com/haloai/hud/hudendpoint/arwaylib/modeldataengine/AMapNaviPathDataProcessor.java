package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import android.graphics.PointF;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayProjection;
import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by Mo Bing(mobing@haloai.com) on 22/10/2016.
 */
public class AMapNaviPathDataProcessor implements INaviPathDataProcessor<AMapNaviPath, NaviInfo, AMapLocation> {
    private static final String TAG                        = "AMapNaviPathDataProcessor";
    //Cache all navigation path data.
    private INaviPathDataProvider mNaviPathDataProvider = null;
    private IRoadNetDataProvider  mRoadNetDataProvider  = null;

    @Override
    /**
     * @param aMapNaviPath 导航路径
     * @return 1:路径处理正常,可以调用getNaviPathDataProvider获取数据 -1:异常情况
     */
    public int onPathUpdate(AMapNaviPath aMapNaviPath) {
        //1.check data legal
        HaloLogger.logE(TAG,"onPathUpdate check data legal");
        if (aMapNaviPath == null) {
            return -1;
        }

        List<Vector3> path_pointf = new ArrayList<>();
        List<LatLng> path_latlng = new ArrayList<>();
        for (AMapNaviStep step : aMapNaviPath.getSteps()) {
            if (step != null) {
                for (int i = 0; i < step.getCoords().size(); i++) {
                    NaviLatLng coord = step.getCoords().get(i);
                    LatLng latLng = new LatLng(coord.getLatitude(), coord.getLongitude());
                    path_latlng.add(latLng);
                    PointF pf = ARWayProjection.toOpenGLLocation(latLng);
                    path_pointf.add(new Vector3(pf.x, -pf.y, 0));
                }
            }
        }

        //2.data pre handle
        HaloLogger.logE(ARWayConst.SPECIAL_LOG_TAG, "setPathAndCalcData start");
        clearAllData();
        //此处由于mPath中的点是path中点的子集,去除了相同的点,因此二者长度不一致,之后不要在使用path
        //如果做了下面这一步会导致path中的点的集合与原始的不同步
        for (int i = 0; i < path.size(); i++) {
            Vector3 v = path.get(i);
            if (true/*!containPoint(mPath, v)*/) {
                mPath.add(new Vector3(v.x, v.y, OBJ_4_CHASE_Z));
            }
        }
        path.clear();
        if (mPath.size() < 2) {
            mPath.clear();
            return 0;
        }

        //clear the points because that points is too close
        /*if (ARWayConst.IS_FILTER_PATH_LITTLE_DISTANCE) {
            HaloLogger.logE("helong_debug", "pre size:" + mPath.size());
            int tempBigTime4Calc = 30000000;
            //保留path中头尾两个点不被删除
            for (int i = 0; i < mPath.size() - 2; i++) {
                int distance = (int) (MathUtils.calculateDistance(
                        mPath.get(i).x, mPath.get(i).y, mPath.get(i + 1).x, mPath.get(i + 1).y) * tempBigTime4Calc);
                //if the distance 30 time is less than 30,remove second point.
                if (distance <= 300) {
                    mPath.remove(i + 1);
                    i--;
                }
            }
            HaloLogger.logE("helong_debug", "next size:" + mPath.size());
        }*/

        //mPath为原始数据,需要移动,放大,旋转
        //to bigger
        for (int i = 0; i < mPath.size(); i++) {
            mPath.get(i).x *= BIGGER_TIME;
            mPath.get(i).y *= BIGGER_TIME;
        }

        //move to screen center
        mOffsetX = mPath.get(0).x - 0;
        mOffsetY = mPath.get(0).y - 0;
        for (int i = 0; i < mPath.size(); i++) {
            mPath.get(i).x -= mOffsetX;
            mPath.get(i).y -= mOffsetY;
        }

        /*if (ARWayConst.IS_CAT_MULL_ROM) {
            int pathLength = mPath.size();
            CatmullRomCurve3D catmull = new CatmullRomCurve3D();
            //controll point 1
            catmull.addPoint(new Vector3(mPath.get(0)));
            for (int i = 0; i < mPath.size(); i++) {
                catmull.addPoint(new Vector3(mPath.get(i)));
            }
            //controll point 2
            catmull.addPoint(new Vector3(mPath.get(mPath.size() - 1)));
            catmull.reparametrizeForUniformDistribution(50);
            mPath.clear();
            for (int i = 0; i <= pathLength * CURVE_TIME; i++) {
                Vector3 pos = new Vector3();
                catmull.calculatePoint(pos, (1.0 * i) / (1.0 * pathLength * CURVE_TIME));
                mPath.add(new Vector3(pos));
            }
            HaloLogger.logE("helong_debug", "插值后 path_size:" + mPath.size());
        }*/

        /*//rotate path with matrix
        double rotateZ = MathUtils.getDegrees(mPath.get(0).x, mPath.get(0).y, mPath.get(CURVE_TIME - 1).x, mPath.get(CURVE_TIME - 1).y);
        mObject4ChaseStartOrientation = rotateZ;
        Matrix matrix = new Matrix();
        matrix.setRotate((float) rotateZ - 180, (float) mPath.get(0).x, (float) mPath.get(0).y);
        for (int i = 1; i < mPath.size(); i++) {
            Vector3 v = mPath.get(i);
            float[] xy = new float[2];
            matrix.mapPoints(xy, new float[]{(float) v.x, (float) v.y});
            v.x = xy[0];
            v.y = xy[1];
        }*/

        //calc and save the car need to rotate degrees
        Vector3 p1 = mPath.get(0);
        Vector3 p2 = mPath.get(1);
        for (int i = 1; i < mPath.size(); i++) {
            if (!p1.equals(mPath.get(i))) {
                p2 = mPath.get(i);
                break;
            }
        }
        double rotateZ = (Math.toDegrees(MathUtils.getRadian(p1.x, p1.y, p2.x, p2.y)) + 270) % 360;
        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, String.format("path orientation,origin first point is %s,second point is %s", p1, p2));
        mObject4ChaseStartOrientation = rotateZ;

        //calc totalDist in opengl
        mTotalLength = allLength;
        mRetainTotalLength = allLength;
        mTotalDistance = 0f;
        for (int i = 0; i < mPath.size() - 1; i++) {
            Vector3 v1 = mPath.get(i);
            Vector3 v2 = mPath.get(i + 1);
            double distance = MathUtils.calculateDistance(v1.x, v1.y, v2.x, v2.y);
            mTotalDistance += distance;
            mDist2FinalPoint.add(distance);
            //在最后添加一个0到末点,表示最后一个点到finalPoint距离为0
            if (i == mPath.size() - 2) {
                mDist2FinalPoint.add(0.0);
            }
        }

        //mLength2Distance表示一米在openGL当前场景中有多大
        mLength2DistanceScale = (mTotalDistance/* - distFake2First*/) / mTotalLength;

        //save every point to final point`s dist and length
        double lastDistance = 0.0;
        for (int i = 0; i < mDist2FinalPoint.size(); i++) {
            if (i == 0) {
                lastDistance = mDist2FinalPoint.get(i);
                mDist2FinalPoint.set(i, mTotalDistance);
            } else {
                double temp = mDist2FinalPoint.get(i);
                mDist2FinalPoint.set(i, mDist2FinalPoint.get(i - 1) - lastDistance);
                lastDistance = temp;
            }
            mLength2FinalPoint.add(mDist2FinalPoint.get(i) / mLength2DistanceScale);
        }

        //手动对path进行250米的划分,记录这些划分点的index和length
        double tempLength = mTotalLength;
        mLoadStepStartIndexs.add(0);
        mLoadStepLengths.add(tempLength);
        for (int i = 1; i < mLength2FinalPoint.size(); i++) {
            if (tempLength - mLength2FinalPoint.get(i) >= LOAD_PATH_LENGTH) {
                mLoadStepStartIndexs.add(i);
                mLoadStepLengths.add(mLength2FinalPoint.get(i));
                tempLength = mLength2FinalPoint.get(i);
            }
        }
        if (!mLoadStepStartIndexs.contains(mLength2FinalPoint.size() - 1)) {
            mLoadStepStartIndexs.add(mLength2FinalPoint.size() - 1);
            mLoadStepLengths.add(0.0);
        }

        //Through will calc left and right path in insertARWayObject,but they calc with many littlePath not mPath.
        //PS:ROAD_WIDTH 代表的是逻辑路宽,由于路宽被用于与lookAt点计算相交,为了确保lookAt的点在道路中央,将逻辑路宽设置的比现实路宽要小很多即可
        //同时,逻辑路宽小能够确保摄像机到lookAt的连线与道路走向吻合
        MathUtils.points2path(mLeftPath, mRightPath, mPath, LOGIC_ROAD_WIDTH);

        //将path切割成N个小path分别加载到场景中
        //splitPath2LittlePathesWithIndexAndLength(mPath, mChildPathPositions, mChildPathes, mStartAddPlaneIndex, ADD_PLANE_LENGTH);

        HaloLogger.logE("branch_line", "path start=========");
        for (int i = 0; i < mPath.size(); i++) {
            Vector3 v = mPath.get(i);

            if (mStepLastPointIndex.contains(i)) {
                HaloLogger.logE("branch_line", v.x + "," + v.y + ",last");
                mStepLastPoint.add(new Vector3(v));
            } else {
                HaloLogger.logE("branch_line", v.x + "," + v.y);
            }
        }
        HaloLogger.logE("branch_line", "path end===========");
        //3.create DataProvider instance
        //4.call processSteps(IRoadNetDataProcessor to get data and create IRoadNetDataProvider something)
        return 1;
    }

    @Override
    public void onLocationUpdate(AMapLocation location) {
        //call DataProvider to update anim with cur location
    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {
        //call DataProvider to update data with naviInfo
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
     * 判断当前传入的path是否与上一次的path相同,如果相同则不进行处理
     *
     * @param path_src
     * @return
     */
    private boolean isPathRepeat(List<Vector3> path_src,List<Vector3> path_dst) {
        if (path_src == null || path_src.size() <= 0 || path_dst == null || path_dst.size() <= 0 || path_src.size() != path_dst.size()) {
            return false;
        }
        for (int i = 0; i < path_src.size(); i++) {
            Vector3 vNew = path_src.get(i);
            Vector3 vOld = path_dst.get(i);
            if (!vNew.equals(vOld)) {
                return false;
            }
        }
        return true;
    }
}
