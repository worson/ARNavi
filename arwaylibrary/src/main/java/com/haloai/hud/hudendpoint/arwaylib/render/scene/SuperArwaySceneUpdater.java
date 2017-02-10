package com.haloai.hud.hudendpoint.arwaylib.render.scene;

import android.content.Context;

import org.rajawali3d.Object3D;
import org.rajawali3d.cameras.Camera;
import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.AFrameTask;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.scene.Scene;

import java.util.LinkedList;

/**
 * Created by wangshengxing on 16/9/22.
 */
public class SuperArwaySceneUpdater {
    protected Scene mScene;
    protected Camera mCamera;
    protected Vector3 mCurPosition = new Vector3();
    protected TextureManager         mTextureManager;
    protected Renderer               mRenderer;
    //basic
    protected Context                mContext;
    private   LinkedList<AFrameTask> mFrameTaskQueue;
    private double mMaxFrameTaskTime = 50;

    public SuperArwaySceneUpdater() {
        this(null);
    }

    public SuperArwaySceneUpdater(Scene scene) {
        mScene = scene;
        mFrameTaskQueue = new LinkedList<>();
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

    public void setContext(Context context) {
        mContext = context;
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
        AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                if (mScene == null) {
                    mScene.clearChildren();
                }
            }
        };
        internalOfferTask(task);
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
    /**
     * Adds a task to the frame task queue.
     *
     * @param task AFrameTask to be added.
     * @return boolean True on successful addition to queue.
     */
    public boolean internalOfferTask(AFrameTask task) {
        synchronized (mFrameTaskQueue) {
            return mFrameTaskQueue.offer(task);
        }
    }

    /**
     * Internal method for performing frame tasks. Should be called at the
     * start of onDrawFrame() prior to render().
     */
    public void performFrameTasks() {
        double sTime = System.currentTimeMillis();
        synchronized (mFrameTaskQueue) {
            //Fetch the first task
            AFrameTask task = mFrameTaskQueue.poll();
            while (task != null) {
                task.run();
                //Retrieve the next task
                if ((System.currentTimeMillis()-sTime)< mMaxFrameTaskTime) {
                    task = mFrameTaskQueue.poll();
                }else {
                    task=null;
                }
            }
        }
    }

    public void setMaxFrameTaskTime(double maxFrameTaskTime) {
        mMaxFrameTaskTime = maxFrameTaskTime;
    }
}
