package com.haloai.hud.hudendpoint.arwaylib.render.scene;

import org.rajawali3d.Object3D;
import org.rajawali3d.scene.Scene;

/**
 * Created by wangshengxing on 16/9/22.
 */
public class SuperArwaySceneUpdater {
    protected Scene mScene;

    public SuperArwaySceneUpdater() {
    }

    public SuperArwaySceneUpdater(Scene scene) {
        mScene = scene;
    }

    public void setVisible(Object3D[] object3Ds,boolean visible){
        for(Object3D object3D:object3Ds){
            object3D.setVisible(visible);
        }
    }

    public boolean removeObject(Object3D[] object3Ds){
        boolean result = true;
        for(Object3D object3D:object3Ds){
            if(true || mScene.hasChild(object3D)){
                result &= mScene.removeChild(object3D);
            }
        }
        return result;
    }
    public boolean removeObject(Object3D object3D){
        boolean result = true;
        if(true || mScene.hasChild(object3D)){
            result &= mScene.removeChild(object3D);
        }
        return result;
    }

    public void setScene(Scene scene) {
        mScene = scene;
    }

    public boolean addObject(Object3D[] object3Ds){
        boolean result = true;
        for(Object3D object3D:object3Ds){
            if(true || !mScene.hasChild(object3D)){
                result &= mScene.addChild(object3D);
            }
        }
        return result;
    }

    public boolean addObject(Object3D object3D){
        boolean result = true;
        if(true || !mScene.hasChild(object3D)){
            result &= mScene.addChild(object3D);
        }
        return result;
    }

    public void clearAll(){
        if (mScene == null) {
            mScene.clearChildren();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        clearAll();
    }
}
