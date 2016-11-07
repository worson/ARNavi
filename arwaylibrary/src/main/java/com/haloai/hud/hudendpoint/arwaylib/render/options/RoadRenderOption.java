package com.haloai.hud.hudendpoint.arwaylib.render.options;

import android.graphics.Color;

/**
 * Created by wangshengxing on 16/10/19.
 */
public class RoadRenderOption {
    //VERTICE_ROAD
    private static final float    ROAD_WIDTH                 = 0.8f;

    public boolean isRoadFog = false;
    //render configuration
    public float mRoadLevel         = 20;

    public float naviRoadWidth     = ROAD_WIDTH * 0.55f;
    public float roadWidth         = ROAD_WIDTH * 1f;
    public float netRoadWidth      = ROAD_WIDTH * 1f;
    public float guideLineWidth    = ROAD_WIDTH * 0.3f;
    public float refLineHeight     = ROAD_WIDTH * 1.485f;
    public float refLineWidth      = ROAD_WIDTH * 1.1f;
    public float refLineStepLength = ROAD_WIDTH * 2.75f;
    public float netRefLineHeight  = ROAD_WIDTH * 1.1f;
    public float netRrefLineWidth  = ROAD_WIDTH * 1.1f;

    public void setLayersWidth(final float width){
        naviRoadWidth = width * 0.55f;
        roadWidth = width * 1f;
        netRoadWidth = width * 1f;
        guideLineWidth = width * 0.3f;
        refLineHeight = width * 1.485f;
        refLineWidth = width * 1.1f;
        refLineStepLength = width * 2.75f;
        netRefLineHeight = width * 1.1f;
        netRrefLineWidth = width * 1.1f;
    }

    public LayersColor mColors = new LayersColor();

    public class LayersColor {
        public int guideLine     = Color.GREEN;
        public int netRefLine    = Color.DKGRAY;
        public int refLine       = Color.BLACK;//Color.argb(0xff,0,160,233)
        public int naviLine      = Color.argb(0xff, 0, 174, 195);
        public int netRoadBottom = Color.GRAY;
        public int netRoad       = Color.BLACK;
        public int bottomRoad    = Color.GRAY;
        public int road          = Color.BLACK;
    }

    public LayersColor getRoadColors() {
        return mColors;
    }
}
