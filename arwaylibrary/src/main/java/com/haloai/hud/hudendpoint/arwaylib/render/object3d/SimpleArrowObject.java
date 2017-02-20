package com.haloai.hud.hudendpoint.arwaylib.render.object3d;

import android.opengl.GLES20;

import org.rajawali3d.Object3D;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Plane;

import java.util.List;

/**
 * 将箭头的头和身体直接合成一个箭头，需要保证path至少有2个点
 * author       : wangshengxing;
 * date         : 15/02/2017;
 * email        : wangshengxing@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.render.object3d;
 * project_name : TestARWay;
 */
public class SimpleArrowObject {
    private ARWayRoadBuffredObject mBody;
    private Plane                  mArrow;
    private float                  mArrowWidth;

    public SimpleArrowObject(List<Vector3> path, float arrowWidth, int arrowColor) {
        mArrowWidth = arrowWidth;

        int pathSize = path.size();
        final Vector3 offset = new Vector3(path.get(0));

        mBody = new ARWayRoadBuffredObject(mArrowWidth, arrowColor, ARWayRoadBuffredObject.ShapeType.TEXTURE_ROAD);
        mBody.setColor(arrowColor);
        mBody.setPosition(offset);
        mBody.updateBufferedRoad(path, offset);


        mArrow = new Plane(mArrowWidth * 4, mArrowWidth * 4, 10, 10, Vector3.Axis.Z,
                true, false, 1, true);
        mArrow.setDepthTestEnabled(false);
        mArrow.setColor(arrowColor);
        mArrow.setBlendingEnabled(true);
        mArrow.setBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);


        Vector3 start = path.get(pathSize - 2);
        Vector3 end = path.get(pathSize - 1);
        float cDegree = (float) Math.toDegrees(Math.atan2((end.y - start.y), (end.x - start.x)));
        mArrow.setPosition(end);
        mArrow.setRotation(Vector3.Axis.Z, -(cDegree - 90));
    }

    public void setAlpha(float alpha){
        if (mBody != null) {
            mBody.setAlpha(alpha);
        }
        if (mArrow != null) {
            mArrow.setAlpha(alpha);
        }
    }

    public Object3D getArrow() {
        return mArrow;
    }

    public Object3D getBody() {
        return mBody;
    }
}
