package com.haloai.hud.hudendpoint.arwaylib.render.camera;

import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;

import org.rajawali3d.math.vector.Vector3;

/**
 * Created by ylq on 16/10/24.
 */
public class ARWayCameraCaculatorY {


    static final private double mNearPlaneDistance = ARWayConst.CAMERA_NEAR_PLANE;
    static final private double mFieldOfView = 45;//
    static final private double mFarPlaneDistance = ARWayConst.CAMERA_FAR_PLANE;
    static final private double mNearPlaneHeight = 2.0 * Math.tan(Math.toRadians(mFieldOfView/2)) * mNearPlaneDistance;

    static final private double DEFAULTP = 8.0;//在Scale为1.0时，摄像机与参考点的距离／摄像机与参考点映射到近平面上的点的距离 ＝4.0

    public static void calculateCameraPositionAndLookAtPoint(CameraParam param,Vector3 position, Vector3 lookAt){
        double angelR = Math.atan(mNearPlaneHeight*(0.5-param.mInScreenPorportion)/mNearPlaneDistance);
        double c2NearPlaneDistance = mNearPlaneDistance/Math.cos(angelR); //摄像机与参考点映射到近平面上的点的距离
        double c2CarDistance = c2NearPlaneDistance * (DEFAULTP/param.mScale);//摄像机到车的距离

        double P_Z = c2CarDistance * Math.cos(Math.toRadians(param.getmAngel()));

        double p_XY2Car = c2CarDistance * Math.sin(Math.toRadians(param.getmAngel()));//摄像机在X-Y平面投影到车的距离
        Vector3 p2C = new Vector3(p_XY2Car *Math.sin(param.getmCarRotZ()),p_XY2Car * Math.cos(param.getmCarRotZ()),0);
        position.x = param.getmCarLocation().x - p2C.x;
        position.y = param.getmCarLocation().y - p2C.y;
        position.z = P_Z;
        double p_XY2Look = P_Z * Math.tan(Math.toRadians(param.getmAngel() + Math.toDegrees(angelR)));
        Vector3 p2L = new Vector3(p_XY2Look *Math.sin(param.getmCarRotZ()),p_XY2Look *Math.cos(param.getmCarRotZ()),0);
        lookAt.x = position.x + p2L.x;
        lookAt.y = position.y + p2L.y;
        lookAt.z = 0;
    }
}
