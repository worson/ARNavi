package com.haloai.hud.hudendpoint.arwaylib.render.options;

import android.graphics.Color;

/**
 * Created by wangshengxing on 16/10/19.
 */
public class RoadRenderOption {
    private static final float    ROAD_WIDTH                 = 0.8f;

    public float mNaviRoadWidth     = ROAD_WIDTH;
    public float mRoadWidth         = ROAD_WIDTH*1f;
    public float mRefLineHeight     = ROAD_WIDTH;
    public float mRefLineWidth      = ROAD_WIDTH;
    public float mRefLineStep = ROAD_WIDTH;

    public int mRefLineColor = Color.WHITE;
    public int mRoadBottomColor = Color.WHITE;
    public int mRoadColor = Color.DKGRAY;
    public int mMainRoadColor = Color.BLUE;
}
