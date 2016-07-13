package com.haloai.hud.hudendpoint.arwaylib.calculator.impl;

import android.view.animation.LinearInterpolator;

import com.haloai.hud.hudendpoint.arwaylib.calculator.SuperCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.CameraFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.CameraResult;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.IAnimationListener;
import org.rajawali3d.animation.SplineTranslateAnimation3D;
import org.rajawali3d.curves.CompoundCurve3D;
import org.rajawali3d.curves.LinearBezierCurve3D;
import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by wangshengxing on 16/7/10.
 */
public class CameraCalculator extends SuperCalculator<CameraResult,CameraFactor>{

    private static final double  CAMERA_Z           = 2;

    //constantly data 实时数据
    private long   mStartTime   = 0l;
    private double mStartLength = 0.0;

    //about animation
    private boolean                               mSingleAnimation = false;
    private List<Animation3D>                     mAnimations      = new LinkedList();
    private SplineTranslateAnimation3D            mCameraAnim      = null;
    private ArrayList<SplineTranslateAnimation3D> mAnims           = new ArrayList<>();


    private static CameraCalculator mCameraCalculator = new CameraCalculator();
    public static CameraCalculator getInstance() {
        return mCameraCalculator;
    }

    @Override
    public void reset() {

    }

    @Override
    public CameraResult calculate(CameraFactor factor) {
        CameraResult result = CameraResult.getInstance();
        result.mRenderer = factor.mRenderer;
        if(mSingleAnimation){
            result.mAnimations = getTourAnimations(factor.mPath);
        }else {
            result.mAnimations = getAnimations(factor,factor.mPathRetainDistance);
        }
        return result;
    }


    private List<Animation3D> getTourAnimations(List<Vector3> path) {

        HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG, "calculate getTourAnimations");
        int temp = 1;//10个点取一个
        CompoundCurve3D compoundCurve3D = new CompoundCurve3D();
        for (int i = 0; i < (path.size() - 1) / temp; i++) {
            Vector3 v1 = path.get(i * temp);
            Vector3 v2 = path.get(i * temp + 1);
            compoundCurve3D.addCurve(
                    new LinearBezierCurve3D(new Vector3(v1.x, v1.y, CAMERA_Z), new Vector3(v2.x, v2.y, CAMERA_Z)));
        }
        SplineTranslateAnimation3D mCameraAnim = new SplineTranslateAnimation3D(compoundCurve3D);
        mCameraAnim.setDurationMilliseconds(500000);
        mCameraAnim.setInterpolator(new LinearInterpolator());
        mCameraAnim.setRepeatMode(Animation.RepeatMode.NONE);
        mCameraAnim.registerListener(mIAnimationListener);

        mAnimations.clear();
        mAnimations.add(mCameraAnim);
        return mAnimations;
    }

    public List<Animation3D> getAnimations(CameraFactor factor,double endLength) {
        //first time callback
        if (mStartTime == 0) {
            mStartTime = System.currentTimeMillis();
            mStartLength = endLength;
            return null;
        }

        //not first time
        //暂定动画移动的距离最小为50m,也就是说目前是固定距离来做动画
        //之后可以考虑使用固定时间,这个要看效果
        //小于50直接return
        //        if (mStartLength - endLength < ANIMATION_LENGTH) {
        //            return;
        //        }

        long endTime = System.currentTimeMillis();
        //        if (endTime - mStartTime < 3000) {
        //            return;
        //        }

        //到此表示返回的剩余距离满足要求
        if (mCameraAnim != null && mCameraAnim.isPlaying()) {
            mCameraAnim.pause();
            mCameraAnim.unregisterListener(mIAnimationListener);
        }
        if (mAnims != null && mAnims.size() > 0) {
            for (int i = 0; i < mAnims.size(); i++) {
                if (mAnims.get(i).isPlaying()) {
                    mAnims.get(i).pause();
                    mAnims.get(i).unregisterListener(mIAnimationListener);
                }
            }
        }
        long duration = endTime - mStartTime;
        Vector3 cameraVector3 = factor.mRenderer.getCurrentCamera().getPosition();
        Vector3 startPosition = new Vector3(cameraVector3.x, cameraVector3.y, 0);
        List<Vector3> throughPosition = new ArrayList<>();
        Vector3 endPosition = new Vector3();

        setEndAndThroughPosition(factor.mPath, factor.mLength2FinalPoint, mStartLength, endLength, endPosition, throughPosition);
//        throughPosition.clear();
        List<Animation3D> anims = rGetAnimations(startPosition, throughPosition, endPosition, duration);

        //reset data
        mStartTime = endTime;
        mStartLength = endLength;
        return anims;
    }

    /**
     * 通过path等各种数据获取结束点以及途经点的位置
     *
     * @param path
     * @param length2FinalPoint
     * @param startLength
     * @param endLength
     * @param endPosition
     * @param throughPosition
     */
    private void setEndAndThroughPosition(final List<Vector3> path, final List<Double> length2FinalPoint,
                                          final double startLength, final double endLength,
                                          Vector3 endPosition, List<Vector3> throughPosition) {
        List<Double> lengths = new ArrayList<>();
        //set through position and end position
        for (int i = 0; i < length2FinalPoint.size(); i++) {
            if (length2FinalPoint.get(i) < startLength) {
                //through position
                for (int j = i; j < length2FinalPoint.size(); j++) {
                    Vector3 v = path.get(j);
                    throughPosition.add(new Vector3(v.x, v.y, v.z));
                    double length = length2FinalPoint.get(j);
                    lengths.add(length);

                    //end position
                    if (length2FinalPoint.get(j + 1) < endLength) {
                        Vector3 v1 = path.get(j);
                        Vector3 v2 = path.get(j + 1);
                        double l1 = length2FinalPoint.get(j);
                        double l2 = length2FinalPoint.get(j + 1);
                        double scale = (endLength - l1) / (l2 - l1);
                        endPosition.x = v1.x + (v2.x - v1.x) * scale;
                        endPosition.y = v1.y + (v2.y - v1.y) * scale;
                        endPosition.z = v1.z + (v2.z - v1.z) * scale;
                        break;
                    }
                }
                break;
            }
        }

    }

    /**
     * 根据获取到的数据开始一段组合动画
     *
     * @param startPosition
     * @param throughPosition
     * @param endPosition
     * @param duration
     */
    private List<Animation3D> rGetAnimations(final Vector3 startPosition, final List<Vector3> throughPosition,
                                final Vector3 endPosition, final long duration) {

        CompoundCurve3D compound = new CompoundCurve3D();
        /*LinearBezierCurve3D curve3D = new LinearBezierCurve3D(
                new Vector3(startPosition.x, startPosition.y, CAMERA_Z),
                new Vector3(endPosition.x, endPosition.y, CAMERA_Z));
        compound.addCurve(curve3D);*/
        /*for (int i = 0; i < throughPosition.size() / 2; i++) {
            throughPosition.set(i * 2, null);
        }
        for (int i = 0; i < throughPosition.size(); i++) {
            if (throughPosition.get(i) == null) {
                throughPosition.remove(i);
                i--;
            }
        }*/
        HaloLogger.logE("helong_debug", "==========================================");
        if (throughPosition == null || throughPosition.size() <= 0) {
            LinearBezierCurve3D curve3D = new LinearBezierCurve3D(
                    new Vector3(startPosition.x, startPosition.y, CAMERA_Z),
                    new Vector3(endPosition.x, endPosition.y, CAMERA_Z));
            compound.addCurve(curve3D);
            testLog(startPosition, endPosition);
        } else {
            if (throughPosition.size() == 1) {
                LinearBezierCurve3D curve3D = null;
                curve3D = new LinearBezierCurve3D(
                        new Vector3(startPosition.x, startPosition.y, CAMERA_Z),
                        new Vector3(throughPosition.get(0).x, throughPosition.get(0).y, CAMERA_Z));
                compound.addCurve(curve3D);
                testLog(startPosition, throughPosition.get(0));
                curve3D = new LinearBezierCurve3D(
                        new Vector3(throughPosition.get(0).x, throughPosition.get(0).y, CAMERA_Z),
                        new Vector3(endPosition.x, endPosition.y, CAMERA_Z));
                compound.addCurve(curve3D);
                testLog(throughPosition.get(0), endPosition);
            } else {
                for (int i = 0; i < throughPosition.size(); i++) {
                    LinearBezierCurve3D curve3D = null;
                    if (i == 0) {
                        curve3D = new LinearBezierCurve3D(
                                new Vector3(startPosition.x, startPosition.y, CAMERA_Z),
                                new Vector3(throughPosition.get(i).x, throughPosition.get(i).y, CAMERA_Z));
                        compound.addCurve(curve3D);
                        testLog(startPosition, throughPosition.get(i));
                    } else if (i == throughPosition.size() - 1) {
                        curve3D = new LinearBezierCurve3D(
                                new Vector3(throughPosition.get(i - 1).x, throughPosition.get(i - 1).y, CAMERA_Z),
                                new Vector3(throughPosition.get(i).x, throughPosition.get(i).y, CAMERA_Z));
                        compound.addCurve(curve3D);
                        testLog(throughPosition.get(i - 1), throughPosition.get(i));
                        curve3D = new LinearBezierCurve3D(
                                new Vector3(throughPosition.get(i).x, throughPosition.get(i).y, CAMERA_Z),
                                new Vector3(endPosition.x, endPosition.y, CAMERA_Z));
                        compound.addCurve(curve3D);
                        testLog(throughPosition.get(i), endPosition);
                    } else {
                        curve3D = new LinearBezierCurve3D(
                                new Vector3(throughPosition.get(i - 1).x, throughPosition.get(i - 1).y, CAMERA_Z),
                                new Vector3(throughPosition.get(i).x, throughPosition.get(i).y, CAMERA_Z));
                        compound.addCurve(curve3D);
                        testLog(throughPosition.get(i - 1), throughPosition.get(i));
                    }
                }
            }
        }
        HaloLogger.logE("helong_debug", "==========================================");

        compound.setCalculateTangents(true);
        mCameraAnim = new SplineTranslateAnimation3D(compound);
        mCameraAnim.setDurationMilliseconds(duration);
        mCameraAnim.setInterpolator(new LinearInterpolator());
        mCameraAnim.setRepeatMode(Animation.RepeatMode.NONE);
        mCameraAnim.registerListener(mIAnimationListener);

        mAnimations.clear();
        mAnimations.add(mCameraAnim);
        return mAnimations;
    }

    private IAnimationListener mIAnimationListener = new IAnimationListener() {
        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationUpdate(Animation animation, double v) {
//            HaloLogger.logE("sen_debug_gl", "onAnimationUpdate called");
        }
    };

    private void testLog(Vector3 v1, Vector3 v2) {
        StringBuilder sb = new StringBuilder();

        sb.append("{");
        sb.append("v1(");
        sb.append("x:" + v1.x + ",");
        sb.append("y:" + v1.y + ",");
        sb.append("z:" + v1.z);
        sb.append(")");
        sb.append("v2(");
        sb.append("x:" + v2.x + ",");
        sb.append("y:" + v2.y + ",");
        sb.append("z:" + v2.z);
        sb.append(")");
        sb.append("}");

        HaloLogger.logE("helong_debug", "compoundAnim:" + sb.toString());
    }
}
