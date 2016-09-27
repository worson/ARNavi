package com.haloai.hud.hudendpoint.arwaylib.rajawali.object3d;

import android.util.Log;

import org.rajawali3d.Geometry3D;
import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by wangshengxing on 16/9/10.
 */
public class SuperRoadObject extends Object3D {
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
    protected ObjectElement mObjectElement;

    protected Object3D mLock       = new Object3D();
    protected volatile boolean  mNeedRender = false;

    protected static Material mRoadMaterial = new Material();
    static {
        mRoadMaterial.useVertexColors(true);
    }


    public SuperRoadObject(List<Vector3> roadPath, float width, int color) {
        super();
    }

    public SuperRoadObject() {

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

    protected void addVerties(ObjectElement element){
        if(element == null || !element.isDataValid()){
            return;
        }
        List<ObjectElement> elements = new LinkedList<>();
        if (mObjectElement != null && mObjectElement.isDataValid()){
            elements.add(mObjectElement);
        }
        elements.add(element);
        ObjectElement totalElement = ObjectElement.addAllElement(elements);
        if (totalElement != null && totalElement.isDataValid()) {
            if(LOG_OUT){
                Log.e(TAG, String.format("addVerties called ,verties size is %s",totalElement.vertices.length));
            }
            mObjectElement = totalElement;
//            setData(totalElement.vertices, totalElement.normals, totalElement.textureCoords, totalElement.colors, totalElement.indices, false);
        }
    }
    protected void applyVerties(){
        ObjectElement totalElement = mObjectElement;
        if (totalElement != null && totalElement.isDataValid()) {
            if(LOG_OUT){
                Log.e(TAG, String.format("applyVerties called ,verties size is %s",totalElement.vertices.length));
            }
            mObjectElement = totalElement;
            setData(totalElement.vertices, totalElement.normals,
                    totalElement.textureCoords, totalElement.colors, totalElement.indices, true);
            mObjectElement.free();
        }
    }
}
