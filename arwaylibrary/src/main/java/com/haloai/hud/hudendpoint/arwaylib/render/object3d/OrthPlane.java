package com.haloai.hud.hudendpoint.arwaylib.render.object3d;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.primitives.Plane;

/**
 * Created by wangshengxing on 2016/10/29.
 */

public class OrthPlane extends Plane {

    private boolean mIsOrth       = false;
    private double  mDisplayScale = 1;
    public void setOrthographic(boolean isOrth, float displayScale){
        mIsOrth = isOrth;
        mDisplayScale = displayScale;
    }

    @Override
    public void render(Camera camera, Matrix4 vpMatrix, Matrix4 projMatrix, Matrix4 vMatrix, Material sceneMaterial) {
        if(mIsOrth) {
            double dist = Math.sqrt(Math.pow(camera.getPosition().x - getPosition().x, 2.0)
                    + Math.pow(camera.getPosition().y - getPosition().y, 2.0)
                    + Math.pow(camera.getPosition().z - getPosition().z, 2.0));
            double near = camera.getNearPlane();
            double convert = (near) / ((dist + near));
            setScale(mDisplayScale/convert);
            Quaternion quaternion = camera.getOrientation();
            setOrientation(quaternion);
        }
        super.render(camera, vpMatrix, projMatrix, vMatrix, sceneMaterial);
    }

    @Override
    public void render(Camera camera, Matrix4 vpMatrix, Matrix4 projMatrix, Matrix4 vMatrix, Matrix4 parentMatrix, Material sceneMaterial) {

        super.render(camera, vpMatrix, projMatrix, vMatrix, parentMatrix, sceneMaterial);//parentMatrix
    }

}
