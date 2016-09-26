package com.haloai.hud.hudendpoint.arwaylib.rajawali.camera;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.math.vector.Vector3;

/**
 * Created by ylq on 16/9/22.
 */
public class ARWayCameraCaculator {
    static private double mNearPlaneDistance ;
    static private double mFarPlaneDistance ;

    static private double mNearPlaneWidth;
    static private double mNearPlaneHeight;
    static private double mFieldOfView;

    public static void cameraCaculatorInit (Camera camera){
        mNearPlaneDistance = camera.getNearPlane();
        mFarPlaneDistance = camera.getFarPlane();
        mFieldOfView = camera.getFieldOfView();
        mNearPlaneHeight = 2.0 * Math.tan(mFieldOfView / 2.0/180*Math.PI) * mNearPlaneDistance;
        double aspect = 2;   //camera.getLastWidth()/(double)camera.getLastHeight();
        mNearPlaneWidth = mNearPlaneHeight *aspect;


    }//设置摄像头默认视景体；

    public static void calculateCameraPositionAndLookAtPoint(Vector3 position, Vector3 lookAt, CameraModel cameraModel){

        if (cameraModel.mRoadWidthProportion >cameraModel.mRoadWidth/mNearPlaneWidth){
            cameraModel.mRoadWidthProportion = cameraModel.mRoadWidth/mNearPlaneWidth;
        }//如果比例大于路的世界坐标宽度比上近景图宽度
        double calculateProportion = (mNearPlaneWidth*cameraModel.mRoadWidthProportion)/cameraModel.mRoadWidth;

        Vector3 centerPoint = cameraModel.mLocation;
        double cLength = Math.sqrt(Math.pow(mNearPlaneDistance,2.0) + Math.pow(mNearPlaneHeight/2,2.0));
        double camera2CenterLength = cLength/calculateProportion;
        double angelB = (90.0 - cameraModel.mNearPlaneWithDrawPlane_Angel +  mFieldOfView/2)/180.0*Math.PI;
        double cosLength = -camera2CenterLength *Math.cos(angelB);
        double sinLength = camera2CenterLength *Math.sin(angelB);

        double angelLookAt = (90.0 - cameraModel.mNearPlaneWithDrawPlane_Angel)/180.0*Math.PI;
        double lookatLength = sinLength/Math.tan(angelLookAt);

        position.x = centerPoint.x + cosLength * Math.sin(cameraModel.mRotZ);
        lookAt.x = position.x + lookatLength *Math.sin(cameraModel.mRotZ);

        position.y = centerPoint.y + cosLength * Math.cos(cameraModel.mRotZ);
        lookAt.y = position.y + lookatLength * Math.cos(cameraModel.mRotZ);

        position.z = sinLength;
        lookAt.z = 0;

        if (cameraModel.mBottomDistanceProportion != 0){
            double distanceH = mNearPlaneHeight * cameraModel.mBottomDistanceProportion;
            double angleL = (180 - mFieldOfView)/2/180 *Math.PI;
            double angleP = Math.atan(distanceH *Math.sin(angleL)/(cLength - (distanceH *Math.cos(angleL))));
            double tyInNearPlaneLength = distanceH *Math.cos(Math.PI - angelB - angleL) + (distanceH *Math.sin(Math.PI - angelB - angleL))/Math.tan(angelB - angleP);
            double realLength = tyInNearPlaneLength/calculateProportion;
            position.x = position.x -realLength *Math.sin(cameraModel.mRotZ);
            lookAt.x = lookAt.x - realLength *Math.sin(cameraModel.mRotZ);
            position.y = position.y - realLength *Math.cos(cameraModel.mRotZ);
            lookAt.y = lookAt.y - realLength *Math.cos(cameraModel.mRotZ);
        }
    }

}
