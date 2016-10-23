package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import android.graphics.PointF;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayProjection;
import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.List;

import com.haloai.hud.hudendpoint.arwaylib.render.strategy.IRenderStrategy;


/**
 * @author Created by Mo Bing(mobing@haloai.com) on 22/10/2016.
 */
public class AMapNaviPathDataProcessor implements INaviPathDataProcessor<AMapNaviPath, NaviInfo, AMapLocation> {
    //constant
    private static final String TAG           = "AMapNaviPathDataProcessor";
    private static final double OBJ_4_CHASE_Z = 0;

    //Cache all navigation path data.
    private INaviPathDataProvider mNaviPathDataProvider = null;
    private IRoadNetDataProvider  mRoadNetDataProvider  = null;

    //middle data
    private double mOffsetX;
    private double mOffsetY;

    private IRenderStrategy renderStrategy;


    @Override
    /**
     * @param aMapNaviPath 导航路径
     * @return 1:路径处理正常,可以调用getNaviPathDataProvider获取数据 -1:异常情况
     */
    public int onPathUpdate(AMapNaviPath aMapNaviPath) {
        //1.check data legal
        HaloLogger.logE(TAG, "onPathUpdate check data legal");
        if (aMapNaviPath == null) {
            return -1;
        }

        List<Vector3> path_vector3 = new ArrayList<>();
        List<LatLng> path_latlng = new ArrayList<>();
        for (AMapNaviStep step : aMapNaviPath.getSteps()) {
            if (step != null) {
                for (int i = 0; i < step.getCoords().size(); i++) {
                    NaviLatLng coord = step.getCoords().get(i);
                    LatLng latLng = new LatLng(coord.getLatitude(), coord.getLongitude());
                    path_latlng.add(latLng);
                    PointF pf = ARWayProjection.toOpenGLLocation(latLng);
                    path_vector3.add(new Vector3(pf.x, -pf.y, OBJ_4_CHASE_Z));
                }
            }
        }

        //2.data pre handle
        HaloLogger.logE(TAG, "onPathUpdate data pre handle");
        //mPath为原始数据,需要移动,旋转
        //move to screen center
        mOffsetX = path_vector3.get(0).x - 0;
        mOffsetY = path_vector3.get(0).y - 0;
        for (int i = 0; i < path_vector3.size(); i++) {
            path_vector3.get(i).x -= mOffsetX;
            path_vector3.get(i).y -= mOffsetY;
        }

        //calc totalDist in opengl
        int mTotalLength = aMapNaviPath.getAllLength();
        int mRetainTotalLength = mTotalLength;
        double mTotalDistance = 0f;
        for (int i = 0; i < path_vector3.size() - 1; i++) {
            Vector3 v1 = path_vector3.get(i);
            Vector3 v2 = path_vector3.get(i + 1);
            double distance = MathUtils.calculateDistance(v1.x, v1.y, v2.x, v2.y);
            mTotalDistance += distance;
        }
        //mLength2Distance表示一米在openGL当前场景中有多大
        double mLength2DistanceScale = (mTotalDistance/* - distFake2First*/) / mTotalLength;

        //3.create DataProvider instance
        HaloLogger.logE(TAG, "onPathUpdate create DataProvider instance");
        mNaviPathDataProvider = new 

        //4.call processSteps(IRoadNetDataProcessor to get data and create IRoadNetDataProvider something)
        HaloLogger.logE(TAG, "onPathUpdate call processSteps(IRoadNetDataProcessor to get data and create IRoadNetDataProvider something)");
        return 1;
    }

    @Override
    public void onLocationUpdate(AMapLocation location) {
        //call DataProvider to update anim with cur location

        //Calculate the distance of maneuver point.
        //.....
        int distanceOfMP = 500;
        //Get the step road class.
        int roadClass = IRenderStrategy.HaloRoadClass.MAINWAY;
        renderStrategy.updateCurrentRoadInfo(roadClass, distanceOfMP);
    }

    @Override
    public void setRenderStrategy(IRenderStrategy renderStrategy) {
        this.renderStrategy = renderStrategy;
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
    private boolean isPathRepeat(List<Vector3> path_src, List<Vector3> path_dst) {
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
