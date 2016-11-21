package com.haloai.hud.hudendpoint.arwaylib.render.object3d;


/**
 * Created by wangshengxing on 21/11/2016.
 */

public class PlaneArrowObject3D extends BaseObject3D{
    private float mTotalWidth;
    private float mTotalHeight;
    private float mArrowHeight;

    public PlaneArrowObject3D(float totalWidth, float totalHeight, float arrowHeight) {
        mTotalWidth = totalWidth;
        mTotalHeight = totalHeight;
        mArrowHeight = arrowHeight;
    }
}
