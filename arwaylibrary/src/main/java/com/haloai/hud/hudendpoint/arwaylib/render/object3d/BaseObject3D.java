package com.haloai.hud.hudendpoint.arwaylib.render.object3d;

import android.util.Log;

import com.haloai.hud.hudendpoint.arwaylib.render.vertices.GeometryData;

import org.rajawali3d.Geometry3D;
import org.rajawali3d.Object3D;
import org.rajawali3d.cameras.Camera;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.renderer.AFrameTask;

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

    private boolean mIsOrth       = false;
    private boolean mIsOrthScale = false;
    private double  mDisplayScale = 1;

    private final LinkedList<AFrameTask> mFrameTaskQueue;


    public BaseObject3D() {
        super();
        mFrameTaskQueue = new LinkedList<>();
    }


    public boolean isHasVerticesColor() {
        return hasVerticesColor;
    }

    public void setHasVerticesColor(boolean hasVerticesColor) {
        this.hasVerticesColor = hasVerticesColor;
    }
    protected void replaceGeometry3D( final Geometry3D geometry3D){
        if (geometry3D == null) {
            return;
        }
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                if (mGeometry != null) {
                    mGeometry.destroy();
                }
                mGeometry = geometry3D;
            }
        };
        commitTask(task);
    }

    protected void addVerties(final GeometryData element){
        if(element == null || !element.isDataValid()){
            return;
        }

        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
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
        };
        commitTask(task);

    }
    protected void applyVerties(){
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
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
        };
        commitTask(task);

    }
/*
    @Override
    public boolean removeChild(final Object3D child) {
        boolean result = mChildren.contains(mChildren);
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mChildren.remove(child);
            }
        };
        commitTask(task);
        return result;
    }

    @Override
    public void addChild(final Object3D child) {
        final Object3D parent = this;
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                if(child.getParent() != null)
                    child.getParent().removeChild(child);
                mChildren.add(child);
                child.setParent(parent);
                if (mRenderChildrenAsBatch)
                    child.setPartOfBatch(true);
            }
        };
        commitTask(task);
    }*/

    public void clearChildren(){
        mChildren.clear();
        /*
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {

            }
        };
        commitTask(task);*/
    }

    protected void commitTask(AFrameTask task){
        task.run();
//        internalOfferTask(task);
    }

    public void setOrthographic(boolean isOrth, float displayScale){
        mIsOrth = isOrth;
        mDisplayScale = displayScale;
        mIsOrthScale = true;
    }

    public void setOrthographic(boolean isOrth){
        mIsOrth = isOrth;
    }

    /**
     * Adds a task to the frame task queue.
     *
     * @param task AFrameTask to be added.
     * @return boolean True on successful addition to queue.
     */
    private boolean internalOfferTask(AFrameTask task) {
        synchronized (mFrameTaskQueue) {
            return mFrameTaskQueue.offer(task);
        }
    }

    /**
     * Internal method for performing frame tasks. Should be called at the
     * start of onDrawFrame() prior to render().
     */
    public void performFrameTasks() {
        synchronized (mFrameTaskQueue) {
            //Fetch the first task
            AFrameTask task = mFrameTaskQueue.poll();
            while (task != null) {
                task.run();
                //Retrieve the next task
                task = mFrameTaskQueue.poll();
            }
        }
    }

    public void preRenderHandle(Camera camera){
        performFrameTasks(); //Handle the task queue
        if(mIsOrth) {
            if(mIsOrthScale) {
                double dist = Math.sqrt(Math.pow(camera.getPosition().x - getPosition().x, 2.0)
                        + Math.pow(camera.getPosition().y - getPosition().y, 2.0)
                        + Math.pow(camera.getPosition().z - getPosition().z, 2.0));
                double near = camera.getNearPlane();
                double convert = (near) / ((dist + near));
                setScale(mDisplayScale / convert);
            }
            Quaternion quaternion = camera.getOrientation();
            quaternion.multiply(-1);
            setOrientation(quaternion);
        }
    }
    @Override
    public void render(Camera camera, Matrix4 vpMatrix, Matrix4 projMatrix, Matrix4 vMatrix, Material sceneMaterial) {
        super.render(camera, vpMatrix, projMatrix, vMatrix, sceneMaterial);
    }

    @Override
    public void render(Camera camera, Matrix4 vpMatrix, Matrix4 projMatrix, Matrix4 vMatrix, Matrix4 parentMatrix, Material sceneMaterial) {
        preRenderHandle(camera);
        super.render(camera, vpMatrix, projMatrix, vMatrix, parentMatrix, sceneMaterial);
    }
}
