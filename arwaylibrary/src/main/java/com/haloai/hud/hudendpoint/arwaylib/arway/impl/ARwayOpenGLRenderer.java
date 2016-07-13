package com.haloai.hud.hudendpoint.arwaylib.arway.impl;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.view.MotionEvent;

import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObjectFactory;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl.DrawCamera;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl.DrawScene;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.materials.Material;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.Renderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ARwayOpenGLRenderer extends Renderer {
    final   ARwayOpenGLFragment arwayFragment;
    private Context             mContext;
    private DrawScene  mDrawScene  = (DrawScene) DrawObjectFactory.getDrawObject(DrawObjectFactory.DrawType.GL_SCENE);
    private DrawCamera mDrawCamera = (DrawCamera)DrawObjectFactory.getDrawObject(DrawObjectFactory.DrawType.GL_CAMERA);

    private static final int    FRAME_RATE        = 45;

    private boolean mIsReadyWork = false;
    private boolean mHasDrawTask = false;
    public ARwayOpenGLRenderer(Context context, @Nullable ARwayOpenGLFragment fragment) {
        super(context);
        mContext = context;
        HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG,"ARwayOpenGLRenderer 正在初始化");
        arwayFragment = fragment;
        setFrameRate(FRAME_RATE);
    }

    public boolean readyForDraw(){
        return mIsReadyWork;
    }

    @Override
    protected void initScene() {

//        getCurrentScene().setBackgroundColor(Color.YELLOW);


        Sphere mCameraSphere = null;
        mCameraSphere = new Sphere(0.3f, 4, 4);
        mCameraSphere.setMaterial(new Material());
        mCameraSphere.setColor(Color.RED);
        getCurrentScene().addChild(mCameraSphere);
        HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG,"Renderer initScene");
    }

    @Override
    protected void onRender(long ellapsedRealtime, double deltaTime) {
        super.onRender(ellapsedRealtime, deltaTime);
        mDrawScene.onOpenglFrame(this);
    }

    private void drawScene(){
        HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG, "drawScene called");
        mDrawScene.doDraw(mContext,this);
        // TODO: 16/7/12 临时测试
//        mDrawCamera.doDraw(mContext,this);
        mHasDrawTask = false;
    }
    public void onDrawScene(){
        if(mIsReadyWork){
            drawScene();
        }else {
            mHasDrawTask=true;
        }
    }

    public void onCameraChange() {
        // TODO: 16/7/12 临时测试，需要加上
        if (mIsReadyWork) {
            mDrawCamera.doDraw(mContext, this);
        }
    }
    @Override
    public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
        super.onRenderSurfaceCreated(config, gl, width, height);
        HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG,"onRenderSurfaceCreated");
        mIsReadyWork =true;
        if (mIsReadyWork && mHasDrawTask){
            mHasDrawTask=false;
            drawScene();
        }
    }

    @Override
    public void onOffsetsChanged(float v, float v1, float v2, float v3, int i, int i1) {

    }

    @Override
    public void onTouchEvent(MotionEvent motionEvent) {

    }

    public static void yawStart() {

    }
    public static void yawEnd() {

    }
    public static void arriveDestination() {

    }
    public static void start() {

    }

    public static void continue_() {

    }

    public static void pause() {

    }

    public static void stop() {

    }

    public static void reset() {

    }





}
