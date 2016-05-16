package com.haloai.hud.hudendpoint.arwaylib.utils;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.model.NaviLatLng;

/**
 * author       : 龙;
 * date         : 2016/5/6;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.utils;
 * project_name : hudlauncher;
 */
public class DrawUtils {
    /**
     * NaviLatLng to LatLng
     *
     * @param naviLatLng
     * @return latLng
     */
    public static LatLng naviLatLng2LatLng(NaviLatLng naviLatLng) {
        return naviLatLng == null ? null : new LatLng(naviLatLng.getLatitude(),
                                                      naviLatLng.getLongitude());
    }

    /**
     * LatLng to NaviLatLng
     *
     * @param latLng
     * @return naviLatLng
     */
    public static NaviLatLng latLng2NaviLatLng(LatLng latLng) {
        return latLng == null ? null : new NaviLatLng(latLng.latitude,
                                                      latLng.longitude);
    }

    /**
     * set matrix to canvas with rotate and translate.
     * @param translateX
     * @param translateY
     * @param offsetX
     * @param rotateXDegrees
     * @param canvas
     */
    public static void setRotateMatrix4Canvas(float translateX,float translateY,float offsetX,float rotateXDegrees, Canvas canvas) {
        final Camera camera = new Camera();
        @SuppressWarnings("deprecation")
        final Matrix matrix = canvas.getMatrix();
        // save the camera status for restore
        camera.save();
        // around X rotate N degrees
        //		camera.rotateX(50);
        //		camera.translate(0.0f, -100f, 0.0f);
        camera.rotateX(rotateXDegrees);
        camera.translate(0.0f, offsetX, 0.0f);
        //x = -500 则为摄像头向右移动
        //y = 200 则为摄像头向下移动
        //z = 500 则为摄像头向高处移动
        // get the matrix from camera
        camera.getMatrix(matrix);
        // restore camera from the next time
        camera.restore();
        matrix.preTranslate(-translateX, -translateY);
        matrix.postTranslate(translateX, translateY);
        canvas.setMatrix(matrix);
    }

    /**

     * @param bitmap      原图
     * @param edgeLength  希望得到的正方形部分的边长
     * @return  缩放截取正中部分后的位图。
     */
    public static Bitmap centerSquareScaleBitmap(Bitmap bitmap, int edgeLength)
    {
        if(null == bitmap || edgeLength <= 0)
        {
            return  null;
        }

        Bitmap result = bitmap;
        int widthOrg = bitmap.getWidth();
        int heightOrg = bitmap.getHeight();

        if(widthOrg > edgeLength && heightOrg > edgeLength)
        {
            //压缩到一个最小长度是edgeLength的bitmap
            int longerEdge = (int)(edgeLength * Math.max(widthOrg, heightOrg) / Math.min(widthOrg, heightOrg));
            int scaledWidth = widthOrg > heightOrg ? longerEdge : edgeLength;
            int scaledHeight = widthOrg > heightOrg ? edgeLength : longerEdge;
            Bitmap scaledBitmap;

            try{
                scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
            }
            catch(Exception e){
                return null;
            }

            //从图中截取正中间的正方形部分。
            int xTopLeft = (scaledWidth - edgeLength) / 2;
            int yTopLeft = (scaledHeight - edgeLength) / 2;

            try{
                result = Bitmap.createBitmap(scaledBitmap, xTopLeft, yTopLeft, edgeLength, edgeLength);
                scaledBitmap.recycle();
            }
            catch(Exception e){
                return null;
            }
        }

        return result;
    }
}
