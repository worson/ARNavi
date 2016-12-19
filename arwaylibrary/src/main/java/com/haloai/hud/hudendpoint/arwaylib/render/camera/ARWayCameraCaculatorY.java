package com.haloai.hud.hudendpoint.arwaylib.render.camera;

import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.hudendpoint.arwaylib.utils.TimeRecorder;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.math.vector.Vector3;

/**
 * Created by ylq on 16/10/24.
 */
public class ARWayCameraCaculatorY {


    public static double mNearPlaneDistance = ARWayConst.CAMERA_NEAR_PLANE;
    public static double mFieldOfView = 45;//
    public static double mFarPlaneDistance = ARWayConst.CAMERA_FAR_PLANE;
    public static double mNearPlaneHeight = 2.0 * Math.tan(Math.toRadians(mFieldOfView/2)) * mNearPlaneDistance;
    public static double mCarOffset = 0.;
    public static double DEFAULTP = 8.0;//在Scale为1.0时，摄像机与参考点的距离／摄像机与参考点映射到近平面上的点的距离 ＝4.0

    private static TimeRecorder mTimeRecorder = null;

    public static void calculateCameraPositionAndLookAtPoint(CameraParam param,Vector3 position, Vector3 lookAt){
        double angelR = Math.atan(mNearPlaneHeight*(0.5-param.mInScreenPorportion)/mNearPlaneDistance);
        double c2NearPlaneDistance = mNearPlaneDistance/Math.cos(angelR); //摄像机与参考点映射到近平面上的点的距离
        double c2CarDistance = c2NearPlaneDistance * (DEFAULTP/param.mScale);//摄像机到车的距离

        double P_Z = c2CarDistance * Math.cos(Math.toRadians(param.getmAngel()));

        double p_XY2Car = c2CarDistance * Math.sin(Math.toRadians(param.getmAngel())) - param.getCarOffset();//摄像机在X-Y平面投影到车的距离
        Vector3 p2C = new Vector3(p_XY2Car *Math.sin(param.getmCarRotZ()),p_XY2Car * Math.cos(param.getmCarRotZ()),0);
        position.x = param.getmCarLocation().x - p2C.x;
        position.y = param.getmCarLocation().y - p2C.y;
        position.z = P_Z;
        double p_XY2Look = P_Z * Math.tan(Math.toRadians(param.getmAngel() + Math.toDegrees(angelR)));
        Vector3 p2L = new Vector3(p_XY2Look *Math.sin(param.getmCarRotZ()),p_XY2Look *Math.cos(param.getmCarRotZ()),0);
        lookAt.x = position.x + p2L.x;
        lookAt.y = position.y + p2L.y;
        lookAt.z = 0;

        if (mTimeRecorder == null) {
            mTimeRecorder = new TimeRecorder();
            mTimeRecorder.enableTimeFilter(true);
            mTimeRecorder.setUpdateLogTime(false);
            mTimeRecorder.setLogFilterTime(5000);
        }
        double distance = Vector3.distanceTo(lookAt,position);
        if(distance>10 || Double.isNaN(distance)){
            mTimeRecorder.forceLogTime();
            HaloLogger.postE(ARWayConst.SPECIAL_LOG_TAG,String.format(" camera info error "));
            HaloLogger.postE(ARWayConst.SPECIAL_LOG_TAG,String.format(" angelR %s , c2NearPlaneDistance %s , c2CarDistance %s , P_Z %s , p_XY2Car %s  ",
                    angelR,c2NearPlaneDistance, c2CarDistance,P_Z,p_XY2Car));

        }
        if (mTimeRecorder.isTimeLoggable()) {
            mTimeRecorder.timerLog(ARWayConst.SPECIAL_LOG_TAG,"CameraParam is "+param.toString());
            mTimeRecorder.timerLog(ARWayConst.SPECIAL_LOG_TAG,String.format("camera info , distance %s , position %s ,%s , %s",distance,position.x,position.y,position.z));
            mTimeRecorder.timerLog(ARWayConst.SPECIAL_LOG_TAG,String.format("camera info , lookat %s ,%s , %s",distance,lookAt.x,lookAt.y,lookAt.z));
            mTimeRecorder.updateLogTime();
        }

    }

    public static void setmCarOffset(double mCarOffset) {
        ARWayCameraCaculatorY.mCarOffset = mCarOffset;
    }
}
