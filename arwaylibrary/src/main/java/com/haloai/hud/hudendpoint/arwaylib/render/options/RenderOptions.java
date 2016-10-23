package com.haloai.hud.hudendpoint.arwaylib.render.options;

/**
 * Created by wangshengxing on 16/10/19.
 */
public class RenderOptions {
    //地图数据
    public float dateLevel = 19; //数据级别

    //摄像头
    public float scale = 19; //数据级别
    public float lookAngle = 70; //俯视角，与负Z轴的角度值
    public float lookHeight = 5; //俯视高度
    public float naviIconOffset = 0; //车标与屏幕底边的像素值

    private RoadRenderOption mRoadOption = new RoadRenderOption();

    public RoadRenderOption getRoadOption() {
        return mRoadOption;
    }

    public void setRoadOption(RoadRenderOption roadOption) {
        mRoadOption = roadOption;
    }
}
