package com.haloai.hud.hudendpoint.arwaylib.render.camera;

import org.rajawali3d.math.vector.Vector3;

/**
 * Created by ylq on 16/10/24.
 */
public class CameraParam {
    protected Vector3 mCarLocation;//参考点（车）的opengl位置 （在X-Y平面上）
    protected double mCarOffset;//与屏幕底边的距离
    protected double mCarRotZ;//参考点绕Z轴的的方向
    protected double mScale;//以参考点为中心点进行放大 1.0～2.0
    protected double mAngel;//摄像头与参考点的连线与z轴的夹角; 0～67.5 度之间
    protected double mInScreenPorportion;//参考点出现在屏幕的位置 0～0.5  ps：0表示参考点出现在屏幕低端中央 ,0.5表示参考点出现在屏幕正中央
    public CameraParam(){}

    public CameraParam(Vector3 carLocation,double carRotZ,double scale,double angel,double inScreenPorportion,double offset) {
        mCarLocation = carLocation;
        mCarRotZ = carRotZ;
        mScale = scale;
        mAngel = angel;
        mInScreenPorportion = inScreenPorportion;
        mCarOffset = offset;
    }

    public void setmCarLocation(Vector3 mCarLocation) {
        this.mCarLocation = mCarLocation;
    }

    public void setmCarRotZ(double mCarRotZ) {
        this.mCarRotZ = mCarRotZ;
    }

    public void setmScale(double mScale) {
        this.mScale = mScale;
    }

    public void setmAngel(double mAngel) {
        this.mAngel = mAngel;
    }

    public void setmInScreenPorportion(double mInScreenPorportion) {
        this.mInScreenPorportion = mInScreenPorportion;
    }

    public Vector3 getmCarLocation() {
        return mCarLocation;
    }

    public double getmCarRotZ() {
        return mCarRotZ;
    }

    public double getmScale() {
        return mScale;
    }

    public double getmAngel() {
        return mAngel;
    }

    public double getmInScreenPorportion() {
        return mInScreenPorportion;
    }

    public double getCarOffset() {
        return mCarOffset;
    }

    public void setCarOffset(double carOffset) {
        mCarOffset = carOffset;
    }

    @Override
    public String toString() {
        return String.format(" mCarOffset %s , mCarRotZ %s , mScale %s , mAngel %s , mInScreenPorportion %s , mCarLocation %s , %s , %s ",mCarOffset,mCarRotZ,mScale,mAngel,mInScreenPorportion,mCarLocation.x,mCarLocation.y,mCarLocation.z);
    }

}
