package com.haloai.hud.hudendpoint.arwaylib.render.object3d;

import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;

import java.util.List;

/**
 * Created by wangshengxing on 16/9/10.
 */
public class SuperRoadObject extends BaseObject3D {


    protected boolean mFogEnable = true;
    protected Vector3 mFogStart = new Vector3(0,0,0);
    protected Vector3 mFogEnd = new Vector3(0,1,0);


    protected static Material mRoadMaterial = new Material();
    static {
        mRoadMaterial.useVertexColors(true);
    }


    public SuperRoadObject(List<Vector3> roadPath, float width, int color) {
        super();
    }

    public SuperRoadObject() {

    }



    public boolean isFogEnable() {
        return mFogEnable;
    }

    public void setFogEnable(boolean fogEnable) {
        mFogEnable = fogEnable;
    }

    public Vector3 getFogStart() {
        return mFogStart;
    }

    public void setFogStart(Vector3 fogStart) {
        mFogStart.setAll(fogStart);
    }

    public Vector3 getFogEnd() {
        return mFogEnd;
    }

    public void setFogEnd(Vector3 fogEnd) {
        mFogEnd.setAll(fogEnd);
    }
}
