package com.haloai.hud.hudendpoint.arwaylib.scene;

import org.rajawali3d.Object3D;
import org.rajawali3d.scene.Scene;

/**
 * Created by wangshengxing on 16/9/22.
 */
public class SuperArwaySceneUpdater {
    protected Scene mScene;

    public SuperArwaySceneUpdater(Scene scene) {
        mScene = scene;
    }

    public boolean removeObject(Object3D[] object3Ds){
        boolean result = true;
        for(Object3D object3D:object3Ds){
            if(mScene.hasChild(object3D)){
                result &= mScene.removeChild(object3D);
            }
        }
        return result;
    }

    public boolean addObject(Object3D[] object3Ds){
        boolean result = true;
        for(Object3D object3D:object3Ds){
            if(!mScene.hasChild(object3D)){
                result &= mScene.addChild(object3D);
            }
        }
        return result;
    }

    public boolean addObject(Object3D object3D){
        boolean result = true;
        if(!mScene.hasChild(object3D)){
            result &= mScene.addChild(object3D);
        }
        return result;
    }
}
