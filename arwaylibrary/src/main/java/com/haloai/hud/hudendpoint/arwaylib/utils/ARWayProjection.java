package com.haloai.hud.hudendpoint.arwaylib.utils;

import android.graphics.Point;

import com.haloai.hud.hudendpoint.arwaylib.utils.jni_data.LatLngOutSide;

/**
 * Created by ylq on 16/9/26.
 */
public class ARWayProjection {

    public static final double PIXEL_METER       = 0.1375;//20级下一个像素对应0.1375米
    public static final double NEAR_PLANE_Height = ARWayConst.CAMERA_NEAR_PLANE * Math.tan(Math.toRadians(22.5)) * 2;
    public static final double NEAR_PLANE_WIDTH  = NEAR_PLANE_Height * 1.43;//0.8
    public static final double K                 = (12.0 / PIXEL_METER) / NEAR_PLANE_WIDTH; //莫卡托转换成opengl坐标的比例  20级的莫卡托转换下0.8openGL长度代表12米

    public static class PointD {
        public double x;
        public double y;

        public PointD() {}

        public PointD(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void set(PointD p) {
            this.x = p.x;
            this.y = p.y;
        }

        @Override
        public String toString() {
            return "PointD(" + x + ", " + y + ")";
        }
    }

    /**
     * 像素坐标转opengl坐标(需要提供像素坐标的等级)
     *
     * @param pixelPoint
     * @return
     */
    public static PointD toOpenGLLocation(Point pixelPoint) {
        PointD mapPoint = new PointD(pixelPoint.x / K, pixelPoint.y / K);
        return mapPoint;
    }

    //经纬度坐标转opengl坐标
    public static PointD toOpenGLLocation(LatLngOutSide coordinate) {
        Point mktPoint = pixelPointFromCoordinate(coordinate, 20.0);
        PointD mapPoint = new PointD((mktPoint.x / K), (mktPoint.y / K));
        return mapPoint;
    }

    //经纬度坐标转opengl坐标
    public static PointD toOpenGLLocation(LatLngOutSide coordinate, double level) {
        Point mktPoint = pixelPointFromCoordinate(coordinate, level);
        PointD mapPoint = new PointD((mktPoint.x / K), (mktPoint.y / K));
        return mapPoint;
    }

    //经纬度转屏幕坐标
    public static Point toScreenLocation(LatLngOutSide coordinate) {
        return pixelPointFromCoordinate(coordinate, 20);
    }

    //经纬度转屏幕坐标
    public static Point toScreenLocation(LatLngOutSide coordinate, double level) {
        return pixelPointFromCoordinate(coordinate, level);
    }

    private static Point pixelPointFromCoordinate(LatLngOutSide coordinate, double level) {
        double dblMercatorLat = Math.log(Math.tan((90 + coordinate.lat) * 0.0087266462599716478846184538424431)) / 0.017453292519943295769236907684886;
        Point pixelPoint = new Point();
        double res = 20.0 - level;
        pixelPoint.x = (int) ((coordinate.lng + 180.0) / 360.0 * 268435456 / Math.pow(2.0, res));
        pixelPoint.y = (int) ((180.0 - dblMercatorLat) / 360.0 * 268435456 / Math.pow(2.0, res));
        return pixelPoint;
    }


}
