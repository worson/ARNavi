package com.haloai.hud.hudendpoint.arwaylib.render.camera;

import org.rajawali3d.math.vector.Vector3;

/**
 * Created by ylq on 16/9/22.
 */
public class CameraModel {
    protected Vector3 mLocation;//车的位置
    protected double mRotZ;//物体绕Z轴旋转的方向
    protected double mRoadWidthProportion;//屏幕底边路段宽度与surfaceview的比例；
    protected double mNearPlaneWithDrawPlane_Angel;//近平面与绘画平面的角度; 0～90度之间
    protected double mRoadWidth;//opengl 的路宽
    protected double mBottomDistanceProportion;//车到屏幕底边与屏幕高度的比例
    public CameraModel(){}

    public CameraModel(Vector3 location,double rotZ,double roadWidth,double roadWidthProportion,double nearPlaneWithDrawPlane_Angel,double bottomDistanceProportion) {
        mLocation = location;
        mRotZ = rotZ;
        mRoadWidth = roadWidth;
        mRoadWidthProportion = roadWidthProportion;
        mNearPlaneWithDrawPlane_Angel = nearPlaneWithDrawPlane_Angel;
        mBottomDistanceProportion = bottomDistanceProportion;
    }

    public void setLocation(Vector3 location) {
        mLocation = location;
    }

    public void setRotZ(double rotZ) {
        mRotZ = rotZ;
    }

    public void setRoadWidthProportion(double roadWidthProportion) {
        mRoadWidthProportion = roadWidthProportion;
    }

    public void setNearPlaneWithDrawPlane_Angel(double nearPlaneWithDrawPlane_Angel) {
        mNearPlaneWithDrawPlane_Angel = nearPlaneWithDrawPlane_Angel;
    }

    public void setRoadWidth(double roadWidth) {
        mRoadWidth = roadWidth;
    }

    public void setBottomDistanceProportion(double bottomDistanceProportion) {
        mBottomDistanceProportion = bottomDistanceProportion;
    }



    public double setRotZBy(double rotZ) {
        return mRotZ += rotZ;
    }

    public double setRoadWidthProportionBy(double roadWidthProportion) {
        return mRoadWidthProportion += roadWidthProportion;
    }

    public double setNearPlaneWithDrawPlane_AngelBy(double nearPlaneWithDrawPlane_Angel) {
        return mNearPlaneWithDrawPlane_Angel += nearPlaneWithDrawPlane_Angel;
    }

    public double setRoadWidthBy(double roadWidth) {
        return mRoadWidth += roadWidth;
    }

    public double setBottomDistanceProportionBy(double bottomDistanceProportion) {
        return mBottomDistanceProportion += bottomDistanceProportion;
    }
}
