package com.haloai.hud.hudendpoint.arwaylib.render.options;

import android.graphics.Color;

/**
 * Created by wangshengxing on 16/10/19.
 */
public class RoadRenderOption {

    public static final boolean IS_ROAD_NET_REFLINE = true;
    //VERTICE_ROAD
    private static final float    ROAD_WIDTH                 = 0.8f;
    public static final float TRAFFIC_DEVIATION_DISTANCE = 0.85F;//0.65
    public static final float ROAD_DEVIATION_DISTANCE = 0.75F;

    public boolean isRoadFog = true;
    public float fogDistance = 4.5f;
    public float fogRate = 0.66f;
    //render configuration
    public float mRoadLevel         = 20;

    public float naviRoadWidth     = ROAD_WIDTH * 0.55f;
    public float roadWidth         = ROAD_WIDTH * 1f;
    public float netRoadWidth      = ROAD_WIDTH * 1f;
    public float guideLineWidth    = ROAD_WIDTH * 0.3f;
    public float refLineHeight     = ROAD_WIDTH * 1.485f;
    public float refLineWidth      = ROAD_WIDTH * 1.1f;
    public float refLineStepLength = ROAD_WIDTH * 2.75f;
    public float crossrefLineStepLength = ROAD_WIDTH * 0.7f;
    public float netRefLineHeight  = ROAD_WIDTH * 1.1f/2;
    public float netRrefLineWidth  = ROAD_WIDTH * 1.1f/2;
    public float adasWidth         = ROAD_WIDTH * 0.55f;

    public void setLayersWidth(final float width){
        naviRoadWidth = width * 0.45f;
        roadWidth = width * 1.1f;
        netRoadWidth = width * 0.88f;
        guideLineWidth = width * 0.3f;
        refLineHeight = width * 1.485f;
        refLineWidth = width * 1.1f;
        refLineStepLength = width * 2.75f;
        crossrefLineStepLength = width * 0.33f;
        netRefLineHeight = width * 0.5f/6f;
        netRrefLineWidth = width * 0.99f/2;
        adasWidth = width * 0.75f;
    }

    public LayersColor mColors = new LayersColor();

    public class LayersColor {
        public int guideLine     = Color.argb(0xff, 0xdf, 0x7a, 0x13);
        public int netRefLine    = Color.argb(0xff, 0x30, 0xb4, 0xcc);
        public int refLine       = Color.BLACK;//Color.argb(0xff,0,160,233)
        public int naviLine      = Color.argb(0xff, 0x30, 0xb4, 0xcc);//Color.argb(0xff, 0, 174, 195);
        public int netRoadBottom = 0xff272727;//Color.GRAY
        public int netRoad       = Color.BLACK;
        public int bottomRoad    = 0xff272727;
        public int road          = Color.BLACK;
        public float naviRoadAlpha   = 1f;
        public float crossRoadAlpha  = 1f;
    }

    public LayersColor getRoadColors() {
        return mColors;
    }
}
