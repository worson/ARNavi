package com.haloai.hud.hudendpoint.arwaylib.utils;

/**
 * Created by wangshengxing on 16/9/9.
 */
public class PointD {
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
