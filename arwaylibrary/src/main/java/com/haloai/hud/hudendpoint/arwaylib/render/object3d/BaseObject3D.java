package com.haloai.hud.hudendpoint.arwaylib.render.object3d;

import android.util.Log;

import com.haloai.hud.hudendpoint.arwaylib.render.vertices.GeometryData;

import org.rajawali3d.Geometry3D;
import org.rajawali3d.Object3D;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by wangshengxing on 16/10/19.
 */
public class BaseObject3D extends Object3D {
    //debug
    public static final boolean LOG_OUT = false;
    public static final String  TAG     = SuperRoadObject.class.getSimpleName();

    //VERTIX
    public static final int VERTIX_NUMBER_PER_PLANE = 4;
    public static final int NUMBER_OF_VERTIX        = 3;
    public static final int NUMBER_OF_TEXTURE       = 2;
    public static final int NUMBER_OF_NORMAL        = 3;
    public static final int NUMBER_OF_COLOR         = 4;
    public static final int NUMBER_OF_INDICE        = 3;

    //MATH
    public final float PI = (float) Math.PI;

    //render
    protected GeometryData mGeometryData;

    protected Object3D mLock       = new Object3D();
    protected volatile boolean  mNeedRender = false;
    private boolean hasVerticesColor = false;


    public BaseObject3D() {
    }

    public BaseObject3D(String name) {
        super(name);
    }

    public boolean isHasVerticesColor() {
        return hasVerticesColor;
    }

    public void setHasVerticesColor(boolean hasVerticesColor) {
        this.hasVerticesColor = hasVerticesColor;
    }
    protected void replaceGeometry3D(Geometry3D geometry3D){
        if (geometry3D == null) {
            return;
        }
        synchronized (mLock) {
            if (mGeometry != null) {
                mGeometry.destroy();
            }
            mGeometry = geometry3D;
        }
    }

    protected void addVerties(GeometryData element){
        if(element == null || !element.isDataValid()){
            return;
        }
        List<GeometryData> elements = new LinkedList<>();
        if (mGeometryData != null && mGeometryData.isDataValid()){
            elements.add(mGeometryData);
        }
        elements.add(element);
        GeometryData totalElement = GeometryData.addAllElement(elements);
        if (totalElement != null && totalElement.isDataValid()) {
            if(LOG_OUT){
                Log.e(TAG, String.format("addVerties called ,verties size is %s",totalElement.vertices.length));
            }
            mGeometryData = totalElement;
        }
    }
    protected void applyVerties(){
        GeometryData totalElement = mGeometryData;
        if (totalElement != null && totalElement.isDataValid()) {
            if(LOG_OUT){
                Log.e(TAG, String.format("applyVerties called ,verties size is %s",totalElement.vertices.length));
            }
            mGeometryData = totalElement;
            setData(totalElement.vertices, totalElement.normals,
                    totalElement.textureCoords, totalElement.colors, totalElement.indices, true);
            mGeometryData.free();
        }
    }
}
