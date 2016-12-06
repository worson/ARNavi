package com.haloai.hud.hudendpoint.arwaylib.render.vertices;

/**
 * Created by wangshengxing on 16/10/14.
 */
abstract public class GeometryProcessor {

    public static final int NUMBER_OF_VERTIX        = 3;
    public static final int NUMBER_OF_TEXTURE       = 2;
    public static final int NUMBER_OF_NORMAL        = 3;
    public static final int NUMBER_OF_COLOR         = 4;
    public static final int NUMBER_OF_INDICE        = 3;

    protected boolean mDateOk = false;

    abstract public GeometryData getGeometryData();


    public boolean isDateOk() {
        return mDateOk;
    }

    public void setDateOk(boolean dateOk) {
        mDateOk = dateOk;
    }
}
