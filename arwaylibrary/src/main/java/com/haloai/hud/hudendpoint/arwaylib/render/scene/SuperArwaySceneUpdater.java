package com.haloai.hud.hudendpoint.arwaylib.render.scene;

import org.rajawali3d.Object3D;
import org.rajawali3d.cameras.Camera;
import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.scene.Scene;

/**
 * Created by wangshengxing on 16/9/22.
 */
public class SuperArwaySceneUpdater {
    protected Scene mScene;
    protected Camera mCamera;
    protected Vector3 mCurPosition = new Vector3();
    protected TextureManager mTextureManager;
    protected Renderer mRenderer;

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

    public void setCamera(Camera camera) {
        mCamera = camera;
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

    public void clearScene(){
        if (mScene == null) {
            mScene.clearChildren();
        }
    }

    public Vector3 getCurrentPosition() {
        return mCurPosition;
    }

    public void setCurrentPosition(Vector3 curPosition) {
        mCurPosition.setAll(curPosition);
    }

    public void setTextureManager(TextureManager textureManager) {
        mTextureManager = textureManager;
    }

    public void setRenderer(Renderer renderer) {
        mRenderer = renderer;
        setScene(renderer.getCurrentScene());
    }
}
