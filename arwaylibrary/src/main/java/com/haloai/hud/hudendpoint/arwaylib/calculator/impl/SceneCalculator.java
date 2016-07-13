package com.haloai.hud.hudendpoint.arwaylib.calculator.impl;

import com.haloai.hud.hudendpoint.arwaylib.calculator.SuperCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.SceneFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.SceneResult;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.curves.CatmullRomCurve3D;
import org.rajawali3d.math.vector.Vector3;

import java.util.List;

/**
 * Created by wangshengxing on 16/7/10.
 */
public class SceneCalculator extends SuperCalculator<SceneResult,SceneFactor> {
    private static SceneCalculator mSceneCalculator = new SceneCalculator();
    public static SceneCalculator getInstance() {
        return mSceneCalculator;
    }



    //data
    private double mTotalDistance   = 0f;
    private double mTotalLength     = 0;
    private double mLength2Distance = 0f;

    //constantly data 实时数据
    private long   mStartTime   = 0l;
    private double mStartLength = 0.0;

    private static final double BIGGER_TIME       = 300000.0;
    private static final int    CURVE_TIME        = 10;
    private static final double ROAD_WIDTH        = 1;

    @Override
    public SceneResult calculate(SceneFactor sceneFactor) {
        SceneResult result = SceneResult.getInstance();
        result.mRenderer = sceneFactor.mRenderer;
        calculatePath(result,sceneFactor.mPath,sceneFactor.mAllLength);
        if(result.mCalculatePath == null || result.mLength2FinalPoint==null ){
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "scene calculate error");
        }else {
            HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG, "scene calculate called");
        }
        return result;
    }

    /**
     * clear data about draw arway!
     */
    private void clearAllData() {
        mTotalDistance = 0f;
        mTotalLength = 0;
        mLength2Distance = 0f;
        mStartTime = 0l;
        mStartLength = 0.0;
    }

    /**
     * 判断集合中是否包含某个元素
     *
     * @param path
     * @param v
     * @return true表示包含, false表示不包含
     */
    private boolean containPoint(List<Vector3> path, Vector3 v) {
        for (int i = 0; i < path.size(); i++) {
            if (path.get(i).equals(v)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param path
     * @param allLength
     */
    public void calculatePath(SceneResult result,List<Vector3> path, double allLength) {
        if (path == null || path.size()<=0) {
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "calculatePath path is null");
            return;
        }else {
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "calculatePath path size is "+path.size());
        }
        clearAllData();
        result.reset();
        //此处由于tempPath中的点是path中点的子集,去除了相同的点,因此二者长度不一致,之后不要在使用path

        for (int i = 0; i < path.size(); i++) {
            Vector3 v = path.get(i);
            // FIXME: 16/7/11 遍历耗时 sen
            if (!containPoint(result.mCalculatePath, v)) {
                result.mCalculatePath.add(new Vector3(v));
                result.mOriginalPath.add(new Vector3(v));
            }
        }

        //clear the points because that points is too close
        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "pre size:" + result.mCalculatePath.size());
        int tempBigTime4Calc = 10000000;
        //保留path中头尾两个点不被删除
        for (int i = 0; i < result.mCalculatePath.size() - 2; i++) {
            int distance = (int) (MathUtils.calculateDistance(
                    result.mCalculatePath.get(i).x, result.mCalculatePath.get(i).y, result.mCalculatePath.get(i + 1).x, result.mCalculatePath.get(i + 1).y) * tempBigTime4Calc);
            HaloLogger.logE("helong_debug", "dist:" + distance);
            //if the distance 10 time is less than 10,remove second point.
            if (distance <= 100) {
                result.mCalculatePath.remove(i + 1);
                i--;
            }
        }
        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "next size:" + result.mCalculatePath.size());

        if(result.mCalculatePath.size()<2){
            // TODO: 16/7/13
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "calculatePath path size()<2");
            return;
        }

        //save the offsetXY between point and point.
        for (int i = 1; i < result.mCalculatePath.size(); i++) {
            double x = result.mCalculatePath.get(i).x;
            double y = result.mCalculatePath.get(i).y;
            result.mOffsetX.add(x - result.mCalculatePath.get(i - 1).x);
            result.mOffsetY.add(y - result.mCalculatePath.get(i - 1).y);
        }

        //result.mCalculatePath为原始数据,需要移动,放大,旋转
        //to bigger
        for (int i = 1; i < result.mCalculatePath.size(); i++) {
            result.mCalculatePath.get(i).x = result.mCalculatePath.get(i - 1).x + result.mOffsetX.get(i - 1) * BIGGER_TIME;
            result.mCalculatePath.get(i).y = result.mCalculatePath.get(i - 1).y + result.mOffsetY.get(i - 1) * BIGGER_TIME;
        }

        //使用更多的点来实现曲线path
        CatmullRomCurve3D catmull = new CatmullRomCurve3D();
        catmull.addPoint(new Vector3(result.mCalculatePath.get(0)));
        for (int i = 0; i < result.mCalculatePath.size(); i++) {
            catmull.addPoint(new Vector3(result.mCalculatePath.get(i)));
        }
        catmull.addPoint(new Vector3(result.mCalculatePath.get(result.mCalculatePath.size()-1)));
        int pathLength = result.mCalculatePath.size();
        result.mCalculatePath.clear();
        for (int i = 0; i < pathLength * CURVE_TIME; i++) {
            Vector3 pos = new Vector3();
            catmull.calculatePoint(pos, (1.0 * i) / (1.0 * pathLength * CURVE_TIME));
            result.mCalculatePath.add(new Vector3(pos));
        }

        //rotate path with matrix
        //        double rotateZ = MathUtils.getDegrees(result.mCalculatePath.get(0).x, result.mCalculatePath.get(0).y, result.mCalculatePath.get(1).x, result.mCalculatePath.get(1).y);
        //        Matrix matrix = new Matrix();
        //        matrix.setRotate((float) -rotateZ, (float) result.mCalculatePath.get(0).x, (float) result.mCalculatePath.get(0).y);
        //        for (int i = 1; i < result.mCalculatePath.size(); i++) {
        //            Vector3 v = result.mCalculatePath.get(i);
        //            float[] xy = new float[2];
        //            matrix.mapPoints(xy, new float[]{(float) v.x, (float) v.y});
        //            v.x = xy[0];
        //            v.y = xy[1];
        //        }

        //move to screen center
        double offsetX = result.mCalculatePath.get(0).x - 0;
        double offsetY = result.mCalculatePath.get(0).y - 0;
        for (int i = 0; i < result.mCalculatePath.size(); i++) {
            result.mCalculatePath.get(i).x -= offsetX;
            result.mCalculatePath.get(i).y -= offsetY;
        }

        //calc totalDist in opengl
        mTotalLength = allLength;
        mTotalDistance = 0f;
        for (int i = 0; i < result.mCalculatePath.size() - 1; i++) {
            Vector3 v1 = result.mCalculatePath.get(i);
            Vector3 v2 = result.mCalculatePath.get(i + 1);
            double distance = MathUtils.calculateDistance(v1.x, v1.y, v2.x, v2.y);
            mTotalDistance += distance;
            result.mDistance2FinalPoint.add(distance);
            //在最后添加一个0到末点,表示最后一个点到finalPoint距离为0
            if (i == result.mCalculatePath.size() - 2) {
                result.mDistance2FinalPoint.add(0.0);
            }
        }

        //mLength2Distance表示一米在openGL当前场景中有多大
        mLength2Distance = mTotalDistance / mTotalLength;

        //save every point to final point`s dist m and opengl
        double lastDistance = 0.0;
        for (int i = 0; i < result.mDistance2FinalPoint.size(); i++) {
            if (i == 0) {
                lastDistance = result.mDistance2FinalPoint.get(i);
                result.mDistance2FinalPoint.set(i, mTotalDistance);
            } else {
                double temp = result.mDistance2FinalPoint.get(i);
                result.mDistance2FinalPoint.set(i, result.mDistance2FinalPoint.get(i - 1) - lastDistance);
                lastDistance = temp;
            }
            result.mLength2FinalPoint.add(result.mDistance2FinalPoint.get(i) / mLength2Distance);
        }

        //full left path and right path
        MathUtils.points2path(result.mLeftPath, result.mRightPath, result.mCalculatePath, ROAD_WIDTH);


    }



    @Override
    public void reset() {

    }
}
