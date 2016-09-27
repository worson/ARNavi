package com.haloai.hud.hudendpoint.arwaylib.utils;

import android.graphics.Point;
import android.graphics.PointF;

import com.amap.api.maps.model.LatLng;

/**
 * Created by ylq on 16/9/26.
 */
public class ARWayProjection {

    private final static double roadWidth = 2.0;//米

    private final static double mNearPlaneDistance = 0.5;

    private static double mNearPlaneWidth = mNearPlaneDistance * Math.tan(Math.toRadians(22.5))*2*438/280;

    private static double K = (roadWidth/0.137784)/mNearPlaneWidth; //莫卡托转换成opengl坐标的比例


    public static void initScale(int width,int height){
        mNearPlaneWidth = mNearPlaneDistance * Math.tan(Math.toRadians(22.5))*2*width/height;
        K = (roadWidth/0.137784)/mNearPlaneWidth;
    }

    public static class GLMapPoint {
        public double x;
        public double y;

        public GLMapPoint() {}

        public GLMapPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void set(GLMapPoint p) {
            this.x = p.x;
            this.y = p.y;
        }

        @Override
        public String toString() {
            return "GLMapPoint(" + x + ", " + y + ")";
        }
    }


    //莫卡托的每个像素点代表0.137784米

    //经纬度坐标转opengl坐标
    public static GLMapPoint glMapPointFormCoordinate(LatLng coordinate){
        Point mktPoint = pixelPointFromCoordinate(coordinate,20.0);
        GLMapPoint mapPoint = new GLMapPoint(mktPoint.x/K,mktPoint.y/K);
        return mapPoint;
    }

    //经纬度坐标转opengl坐标
    public static PointF toOpenGLLocation(LatLng coordinate){
        Point mktPoint = pixelPointFromCoordinate(coordinate,20.0);
        PointF mapPoint = new PointF((float)(mktPoint.x/K),(float)(mktPoint.y/K));
        return mapPoint;
    }

    public static LatLng coordinateFromGLMapPoint(Point pixelPoint,double level) {
        double res = 20.0 -level;
        double dblMercatorLat = 180.0 - pixelPoint.y *Math.pow(2.0,res) * 360.0 / 268435456;
        double longitude = pixelPoint.x *Math.pow(2.0,res) * 360.0 / 268435456 - 180.0;
        double latitude = Math.atan(Math.exp(dblMercatorLat * 0.017453292519943295769236907684886)) / 0.0087266462599716478846184538424431 - 90;
        LatLng coordinate = new LatLng(latitude,longitude);
        return coordinate;
    }
    public static Point toScreenLocation(LatLng coordinate,double level){
        return pixelPointFromCoordinate(coordinate,level);
    }
    public static Point pixelPointFromCoordinate(LatLng coordinate,double level){
        double dblMercatorLat = Math.log(Math.tan((90 + coordinate.latitude) * 0.0087266462599716478846184538424431))/0.017453292519943295769236907684886;
        Point pixelPoint = new Point();
        double res = 20.0 -level;
        pixelPoint.x = (int)((coordinate.longitude + 180.0) / 360.0 * 268435456/Math.pow(2.0,res));
        pixelPoint.y = (int)((180.0 - dblMercatorLat) / 360.0 * 268435456 * Math.pow(2.0,res)/Math.pow(2.0,res));
        return pixelPoint;

    }








}
