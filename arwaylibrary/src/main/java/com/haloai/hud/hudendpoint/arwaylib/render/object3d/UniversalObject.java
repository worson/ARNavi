package com.haloai.hud.hudendpoint.arwaylib.render.object3d;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.Matrix4;

/**
 * Created by wangshengxing on 16/10/19.
 */
public class UniversalObject extends BaseObject3D{
    public UniversalObject() {

    }
    @Override
    public void render(Camera camera, Matrix4 vpMatrix, Matrix4 projMatrix, Matrix4 vMatrix, Matrix4 parentMatrix, Material sceneMaterial) {
        if(!mNeedRender){
            return;
        }
        super.render(camera, vpMatrix, projMatrix, vMatrix, parentMatrix, sceneMaterial);
    }
}
