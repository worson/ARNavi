package com.haloai.hud.hudendpoint.arwaylib.arway.impl_gl;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;

import com.haloai.hud.hudendpoint.arwaylib.modeldataengine.INaviPathDataProvider;
import com.haloai.hud.hudendpoint.arwaylib.modeldataengine.IRoadNetDataProvider;
import com.haloai.hud.hudendpoint.arwaylib.render.camera.ARWayCameraCaculatorY;
import com.haloai.hud.hudendpoint.arwaylib.render.camera.CameraModel;
import com.haloai.hud.hudendpoint.arwaylib.render.camera.CameraParam;
import com.haloai.hud.hudendpoint.arwaylib.render.refresher.IRefreshDataLevelNotifer;
import com.haloai.hud.hudendpoint.arwaylib.render.refresher.RenderParamsRefresher;
import com.haloai.hud.hudendpoint.arwaylib.render.scene.ArwaySceneUpdater;
import com.haloai.hud.hudendpoint.arwaylib.render.strategy.IRenderStrategy;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.hudendpoint.arwaylib.utils.TimeRecorder;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.ATransformable3D;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.IAnimationListener;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.cameras.Camera;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.view.TextureView;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * author       : 龙;
 * date         : 2016/6/29;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.fragments.amap;
 * project_name : hudlauncher;
 * distance     : 用于表示openGl中的距离
 * length       : 用于表示物理世界的距离米
 */
public class ARwayRenderer extends Renderer implements IAnimationListener, IRenderStrategy.RenderParamsNotifier, INaviPathDataProvider.INaviPathDataChangeNotifer, IRoadNetDataProvider.IRoadNetDataNotifier,IRefreshDataLevelNotifer {
    //content
    private static final String TAG               = "com.haloai.hud.hudendpoint.arwaylib.arway.impl_gl.ARwayRenderer";
    private static final double OBJ_4_CHASE_Z     = 0;
    private static final int    FRAME_RATE        = ARWayConst.FRAME_RATE;
    //private static final double ROAD_WIDTH        = ARWayProjection.ROAD_WIDTH/*Math.tan(Math.toRadians(22.5))*2*400/280 * 0.5*/ /*ARWayConst.ROAD_WIDTH*/;
    private static final double CAMERA_OFFSET_X   = 0;
    private static final double CAMERA_OFFSET_Y   = 0;
    private static final double CAMERA_OFFSET_Z   = /*4*/0.6/*1*/;
    private static final double CAMERA_CUT_OFFSET = /*0*/0.6/*1*/;
    private static final double LOOK_AT_DIST      = /*0*/1.3;
    private              int    SCREEN_WIDTH      = 0;
    private              int    SCREEN_HEIGHT     = 0;

    private TextureView mTextureView = null;
    //list data
    private List<Vector3> mRenderPath = new ArrayList<>();

    //rajawali about
    private Object3D          mObject4Chase;
    private Object3D          mCarObject;
    private ArwaySceneUpdater mSceneUpdater = ArwaySceneUpdater.getInstance();

    //about animation
    private TranslateAnimation3D  mTransAnim  = null;
    private RotateOnAxisAnimation mRotateAnim = null;

    //state
    //ps:mIsInitScene代表Rajawali自己初始化场景是否完成
    //ps:mIsMyInitScene代表我们得到数据后去add元素到场景中是否完成
    //ps:mCanInitScene代表我们是否能够去add元素到场景中(满足一下条件即可:1.数据到来setPath被调用 2.本次数据未被加载成元素添加到场景中)
    private boolean mIsInitScene    = false;
    private boolean mIsMyInitScene  = false;
    private boolean mCanMyInitScene = false;


    //about camera
    private CameraModel mCameraModel            = new CameraModel();
    private float       mRoadWidthProportion    = 0.13f;
    private float       mCameraPerspectiveAngel = 70;


    private RenderParamsRefresher mParamsRefresher = new RenderParamsRefresher();


    //time recorder
    private TimeRecorder mRenderTimeRecorder = new TimeRecorder();

    //新架构
    private INaviPathDataProvider mNaviPathDataProvider;
    private IRoadNetDataProvider  mRoadNetDataProvider;
    private IRenderStrategy.RenderParams mRenderParams;

    public ARwayRenderer(Context context) {
        super(context);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        SCREEN_WIDTH = wm.getDefaultDisplay().getWidth();
        SCREEN_HEIGHT = wm.getDefaultDisplay().getHeight();
    }

    public void setTextureViewAndInit(TextureView textureView) {
        mTextureView = textureView;
        initSceneAnimator();
    }

    @Override
    public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
        super.onRenderSurfaceCreated(config, gl, width, height);
    }

    @Override
    public void initScene() {
        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "ARRender init called!");
        getCurrentScene().setBackgroundColor(0,0,0,0);
        setFrameRate(FRAME_RATE);
        mSceneUpdater = ArwaySceneUpdater.getInstance();
        mSceneUpdater.setRenderer(this);
        mSceneUpdater.setContext(getContext());
        mSceneUpdater.setScene(getCurrentScene());
        mSceneUpdater.initScene();
        mSceneUpdater.setCamera(getCurrentCamera());

        mIsInitScene = true;
        if (!mIsMyInitScene && mCanMyInitScene) {
            myInitScene();
        }
    }

    @Override
    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        super.onRenderSurfaceSizeChanged(gl, width, height);
    }

    @Override
    public void onRenderSurfaceDestroyed(SurfaceTexture surface) {
        super.onRenderSurfaceDestroyed(surface);
    }

    /*
    private void updateCamera(ATransformable3D transObject) {
        Camera camera = getCurrentCamera();
        Vector3 location = transObject.getPosition();
        Vector3 position = new Vector3(location.x, location.y, CAMERA_OFFSET_Z);
        Vector3 lookat = new Vector3(0, 0, 0);

        if (true) {
            mCameraModel.setLocation(mObject4Chase.getPosition());
            mCameraModel.setRotZ(mObject4Chase.getRotZ());
            ARWayCameraCaculator.calculateCameraPositionAndLookAtPoint(position, lookat, mCameraModel);
        } else {
            updateCameraLookatAndPosition(location, transObject.getRotZ(), LOOK_AT_DIST, position, lookat);
        }
        camera.setPosition(position);
        camera.setLookAt(lookat);
    }
     */
    // TODO: 2016/10/14 修改道路底边的宽度--修改摄像机的高度和LOOK_DIST
    public void changeRoadShowWidthBy(double changeValue) {
        mRoadWidthProportion += changeValue;
        mRoadWidthProportion = mRoadWidthProportion <= 0 ? 0 : mRoadWidthProportion;
    }

    // TODO: 2016/10/14 修改摄像机高度
    public double changeCameraZBy(double changeValue) {
        return mCameraModel.setRoadWidthProportionBy(changeValue);
    }

    // TODO: 2016/10/14  修改摄像机角度
    public double changeCameraLookDistBy(double changeValue) {
        return mCameraModel.setNearPlaneWithDrawPlane_AngelBy(changeValue);
    }

    private void updateCameraLookatAndPosition(Vector3 cPos, double yaw, double dist, Vector3 position, Vector3 lookat) {
        if (cPos == null || position == null || lookat == null) {
            return;
        }
        final double LOOK_OFFSET = dist;
        final double CAMERA_OFFSET = -CAMERA_CUT_OFFSET;
        double offsetY, offsetX;
        double rYaw = yaw;

        offsetX = LOOK_OFFSET * Math.sin(rYaw);
        offsetY = LOOK_OFFSET * Math.cos(rYaw);
        lookat.x = cPos.x + offsetX;
        lookat.y = cPos.y + offsetY;

        offsetX = CAMERA_OFFSET * Math.sin(rYaw);
        offsetY = CAMERA_OFFSET * Math.cos(rYaw);

        position.x = cPos.x + offsetX;
        position.y = cPos.y + offsetY;

    }

    @Override
    protected void onRender(long ellapsedRealtime, double deltaTime) {
        mRenderTimeRecorder.start();

        if (!mIsMyInitScene) {
            return;
        }
        if (mObject4Chase != null) {
            //deltaTime表示每一帧间隔的秒数,注意单位是秒
            //一帧一帧去通过车速实时计算位置角度等,作用于被追随物体的摄像头移动方式
            //updateObject4Chase(mObject4Chase, mCarSpeed, deltaTime);
            //updateCamera(mObject4Chase);
             //Log.e("ylq","carPosition:"+mObject4Chase.getPosition());
            mParamsRefresher.cameraRefresh(getCurrentCamera(),mObject4Chase.getPosition(),mObject4Chase.getRotZ());
        }
        mSceneUpdater.onRender(ellapsedRealtime,deltaTime);
        super.onRender(ellapsedRealtime, deltaTime);

        if (ARWayConst.ENABLE_PERFORM_TEST) {
            mRenderTimeRecorder.recordeAndLog("onRenderFrame", "onRenderFrame");
        }
    }

    @Override
    public void onOffsetsChanged(float v, float v1, float v2, float v3, int i, int i1) {

    }

    @Override
    public void onTouchEvent(MotionEvent motionEvent) {

    }

    @Override
    public void onRenderFrame(GL10 gl) {
        super.onRenderFrame(gl);
    }

    @Override
    public int getGLMinorVersion() {
        return super.getGLMinorVersion();
    }

    @Override
    public boolean getSceneCachingEnabled() {
        return super.getSceneCachingEnabled();
    }

    @Override
    protected void render(long ellapsedRealtime, double deltaTime) {
        super.render(ellapsedRealtime, deltaTime);
    }

    //====================================amap data start========================================//

    /**
     * 开始偏航(yawStart)：停止当前绘制内容，进入状态显示界面(显示指南针、速度等内容)
     */
    public void yawStart() {

    }

    /**
     * 结束偏航(yawEnd)：
     */
    public void yawEnd() {

    }

    public void onNaviStop() {
        arriveDestination();
    }

    /**
     * 到达目的地时调用
     */
    public void arriveDestination() {
        clearAllData();
        System.gc();
    }

    /**
     * clear data about draw arway!
     */
    private void clearAllData() {
        mRenderPath.clear();
        mIsMyInitScene = false;
        mCanMyInitScene = false;
        mObject4Chase = null;
        mTransAnim = null;
        mRotateAnim = null;
        System.gc();
    }

    public void initDefaultRenderParams(IRenderStrategy.RenderParams params){

        mParamsRefresher.initDefaultRenderParmars(params.dataLevel.getLevel(),params.glCameraAngle,params.glInScreenProportion,params.glScale);
        mParamsRefresher.setIRefeshDataLevelNotifer(this);
    }


    private void myInitScene() {

        clearScene();
        mSceneUpdater.reset();
        //啊奇
        mSceneUpdater.getRenderOptions().setLayersWidth((float) mParamsRefresher.getInitializtionRoadWidth());
        
        mSceneUpdater.reset();

        if (mObject4Chase == null) {
            mObject4Chase = mSceneUpdater.getCarObject();
        }
        mObject4Chase.setPosition(mRenderPath.get(0).x, mRenderPath.get(0).y,0);
        double rotateZ = mNaviPathDataProvider == null ? 0 : mNaviPathDataProvider.getObjStartOrientation();
        mObject4Chase.setRotation(Vector3.Axis.Z, -rotateZ);

        mCarObject = new Sphere(0.05f, 20, 20);
        Material cMaterial = new Material();
        cMaterial.setColor(Color.argb(0, 76, 0, 0));//Color.argb(255,76,0,0)
        mCarObject.setMaterial(cMaterial);
        mCarObject.setPosition(mObject4Chase.getPosition());
        //        getCurrentScene().addChild(mCarObject);

//        mSceneUpdater.setCarObject(mCarObject);

        Camera camera = getCurrentCamera();
        camera.enableLookAt();
        camera.setUpAxis(Vector3.Axis.Z);

        getCurrentCamera().setNearPlane(ARWayConst.CAMERA_NEAR_PLANE);
        getCurrentCamera().setFarPlane(ARWayConst.CAMERA_FAR_PLANE);

        Vector3 position = new Vector3();
        Vector3 lookAt = new Vector3();
        CameraParam param = new CameraParam(mObject4Chase.getPosition(),mObject4Chase.getRotZ(),1.0,60,0);
        ARWayCameraCaculatorY.calculateCameraPositionAndLookAtPoint(param,position,lookAt);
        getCurrentCamera().setPosition(position);
        getCurrentCamera().setLookAt(lookAt);



        /*
        updateCamera(mObject4Chase);
        ARWayCameraCaculator.cameraCaculatorInit(camera);

        mCameraModel.setNearPlaneWithDrawPlane_Angel(mCameraPerspectiveAngel);
        mCameraModel.setRoadWidthProportion(mRoadWidthProportion);
        mCameraModel.setRoadWidth(ROAD_WIDTH);
        mCameraModel.setBottomDistanceProportion(0.0f);
        */
        mSceneUpdater.clearSceneObjects();
        addRoadNet2Scene();
        addNaviPath2Scene();

        //update flag
        mIsMyInitScene = true;
        mCanMyInitScene = false;
    }

    private void addRoadNet2Scene() {
        List<List<Vector3>> branchLinesList = new ArrayList<>();
        branchLinesList.add(mRenderPath);
        HaloLogger.logE("AMapNaviPathDataProcessor","__size="+mRenderPath.size());
        mSceneUpdater.renderRoadNet(branchLinesList);
        //// TODO: 16/10/27
        mSceneUpdater.commitRender();

    }

    private void addNaviPath2Scene() {
        mSceneUpdater.renderTrafficLight(mRenderPath.get(4),0);
        mSceneUpdater.renderNaviPath(mRenderPath);
        mSceneUpdater.renderFloor(-100,100,100,-100,1,0.f);
        mSceneUpdater.commitRender();
    }

    private void clearLastAnim() {
        if (mTransAnim != null) {
            if (mTransAnim.isPlaying()) {
                mTransAnim.pause();
            }
            getCurrentScene().unregisterAnimation(mTransAnim);
            mTransAnim = null;
        }
        if (mRotateAnim != null) {
            if (mRotateAnim.isPlaying()) {
                mRotateAnim.pause();
            }
            getCurrentScene().unregisterAnimation(mRotateAnim);
            mRotateAnim = null;
        }
    }

    /**
     * 开始动画在两个位置点之间,以相同时间进行平移和旋转动画
     * 旋转角度0-360
     *
     * @param from
     * @param to
     * @param degrees
     * @param duration
     */
    private void startAnim(Vector3 from, Vector3 to, double degrees, long duration) {
        mTransAnim = createTranslateAnim(from, to, duration, mObject4Chase);
        mRotateAnim = createRotateAnim(Vector3.Axis.Z,
                Math.abs(degrees) > 180 ? (degrees > 0 ? degrees - 360 : degrees + 360) : degrees,
                duration, mObject4Chase);
        mTransAnim.play();
        mRotateAnim.play();
    }

    private TranslateAnimation3D createTranslateAnim(Vector3 from, Vector3 to, long duration, Object3D object4Chase) {
        TranslateAnimation3D transAnim = new TranslateAnimation3D(from, to);
        transAnim.setTransformable3D(object4Chase);
        transAnim.setDurationMilliseconds(duration);
        transAnim.setInterpolator(new LinearInterpolator());
        transAnim.registerListener(this);
        getCurrentScene().registerAnimation(transAnim);
        return transAnim;
    }

    private RotateOnAxisAnimation createRotateAnim(Vector3.Axis axis, double degrees, Long duration, ATransformable3D transformable3D) {
        RotateOnAxisAnimation anim = new RotateOnAxisAnimation(axis, degrees);
        anim.setTransformable3D(transformable3D);
        anim.setDurationMilliseconds(duration);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatMode(Animation.RepeatMode.NONE);
        anim.registerListener(this);
        getCurrentScene().registerAnimation(anim);
        return anim;
    }

    public void clearScene() {
        getCurrentScene().clearChildren();
    }

    //====================================rajawali animation callback start========================================//
    @Override
    public void onAnimationEnd(Animation animation) {

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationUpdate(Animation animation, double v) {

    }
    //====================================rajawali animation callback end========================================//


    public void setEvent(int type) {
        if (type == 1) {//左下
            mCameraPerspectiveAngel += 5;
            if (mCameraPerspectiveAngel > 90) {
                mCameraPerspectiveAngel = 50;
            }
        }
        if (type == 2) {//右上
            mRoadWidthProportion += 0.1;
            if (mRoadWidthProportion > 1) {
                mRoadWidthProportion = 1f;
            }
        } else if (type == 3) {//下
            mRoadWidthProportion -= 0.1;
            if (mRoadWidthProportion < 0) {
                mRoadWidthProportion = 0.1f;
            }
        }
        if (mRoadWidthProportion >= 0 && mRoadWidthProportion <= 1) {
            mCameraModel.setRoadWidthProportion(mRoadWidthProportion);
        }
        if (mCameraPerspectiveAngel > 0 && mCameraPerspectiveAngel < 90) {
            mCameraModel.setNearPlaneWithDrawPlane_Angel(mCameraPerspectiveAngel);
        }
    }


    ///////////////////////////////////////新架构///////////////////////////////////////

    @Override
    public void onPathInit() {
        if (mNaviPathDataProvider != null) {
            //啊奇
            IRenderStrategy.DataLevel level = IRenderStrategy.DataLevel.LEVEL_20;
            for (IRenderStrategy.DataLevel temple:IRenderStrategy.DataLevel.values()){
                if (temple.getLevel() == mParamsRefresher.getInitializtionLevel()){
                    level = temple;
                    break;
                }
            }

           
            Vector3 curObjPos = new Vector3(0,0,0);
            if(mIsMyInitScene){
                curObjPos.setAll(mObject4Chase.getPosition());
            }
            mRenderPath = mNaviPathDataProvider.getNaviPathByLevel(level,curObjPos.x,curObjPos.y).get(0);
            if (mRenderPath != null && mRenderPath.size() >= 2) {
                mCanMyInitScene = true;
                if (mIsInitScene) {
                    myInitScene();
                }
            }
        }
    }

    @Override
    public void onPathUpdate() {

    }

    @Override
    public void onAnimUpdate(INaviPathDataProvider.AnimData animData) {
        if (mIsMyInitScene) {
            clearLastAnim();
            startAnim(animData.from, animData.to, animData.degrees, animData.duration);
        }

    }

    @Override
    public void onGuideLineUpdate(List<Vector3> guideLineUpdate) {
        mSceneUpdater.renderGuideLine(guideLineUpdate);
    }

    @Override
    public void onRoadNetDataChange() {

    }

    @Override
    public void onRenderParamsUpdated(IRenderStrategy.RenderParams renderParams,int animationType,double duration){
        Log.e("ylq","onRenderParamsUpdated");
        switch (animationType){
            case IRenderStrategy.SCALE_TYPE:
                mParamsRefresher.doScaleAnimation(renderParams.dataLevel.getLevel(),renderParams.glScale,duration);
                break;
            case IRenderStrategy.ANGLE_TYPE:
                mParamsRefresher.doAngelAnimation(renderParams.glCameraAngle,duration);
                break;
            case IRenderStrategy.INSCREENPROPORTION_TYPE:
                mParamsRefresher.doInScreenProportion(renderParams.glInScreenProportion,duration);
                break;
            case IRenderStrategy.SCALE_TYPE|IRenderStrategy.ANGLE_TYPE:
                mParamsRefresher.doScaleAnimation(renderParams.dataLevel.getLevel(),renderParams.glScale,duration);
                mParamsRefresher.doAngelAnimation(renderParams.glCameraAngle,duration);
                break;
            case IRenderStrategy.SCALE_TYPE|IRenderStrategy.INSCREENPROPORTION_TYPE:
                mParamsRefresher.doScaleAnimation(renderParams.dataLevel.getLevel(),renderParams.glScale,duration);
                mParamsRefresher.doInScreenProportion(renderParams.glInScreenProportion,duration);
                break;
            case IRenderStrategy.ANGLE_TYPE|IRenderStrategy.INSCREENPROPORTION_TYPE:
                mParamsRefresher.doAngelAnimation(renderParams.glCameraAngle,duration);
                mParamsRefresher.doInScreenProportion(renderParams.glInScreenProportion,duration);
                break;
            case IRenderStrategy.ANGLE_TYPE|IRenderStrategy.INSCREENPROPORTION_TYPE|IRenderStrategy.SCALE_TYPE:
                mParamsRefresher.doScaleAnimation(renderParams.dataLevel.getLevel(),renderParams.glScale,duration);
                mParamsRefresher.doAngelAnimation(renderParams.glCameraAngle,duration);
                mParamsRefresher.doInScreenProportion(renderParams.glInScreenProportion,duration);
                break;
            default:
                //
                Log.e("ylq","Worng AnimationType");
                break;
        }
    }

    @Override
    public void onRefreshDataLevel(int DataLevel,double roadWidth){
        Log.e("ylq","onRefreshDataLevel:"+DataLevel);
        //add

        IRenderStrategy.DataLevel level = IRenderStrategy.DataLevel.LEVEL_20;
        for (IRenderStrategy.DataLevel temple:IRenderStrategy.DataLevel.values()){
            if (temple.getLevel() == DataLevel){
                level = temple;
                break;
            }
        }

        clearLastAnim();

        mParamsRefresher.cameraRefresh(getCurrentCamera(),mObject4Chase.getPosition(),mObject4Chase.getRotZ());

        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, String.format("onRenderParamsUpdated called,thread is = %s",Thread.currentThread().getId()));
        mSceneUpdater.getRenderOptions().setLayersWidth((float)roadWidth);
//        mSceneUpdater.renderFloor(-100,100,100,-100,1);
        List<Vector3> naviPath =  mNaviPathDataProvider.getNaviPathByLevel(level,mObject4Chase.getX(),mObject4Chase.getY()).get(0);
        mSceneUpdater.renderNaviPath(naviPath);

        mHandler.sendEmptyMessage(SCENE_HIDE_ANIMATION_ID);
    }
    int mRefreshDataCnt = 0;


    public void setNaviPathDataProvider(INaviPathDataProvider naviPathDataProvider) {
        this.mNaviPathDataProvider = naviPathDataProvider;
    }

    public void setRoadNetDataProvider(IRoadNetDataProvider roadNetDataProvider) {
        this.mRoadNetDataProvider = roadNetDataProvider;
    }

    public Vector3 getCurPos() {
        return mObject4Chase == null ? null : mObject4Chase.getPosition();
    }

    public double getCurDegrees() {
        return mObject4Chase == null ? 0 : mObject4Chase.getRotZ();
    }

    public void changeStrategy(IRenderStrategy.DataLevel level){
        clearLastAnim();
        //HaloLogger.logE("test__hah","screen start");
        //HaloLogger.logE("test__hah",mObject4Chase.getX()+","+mObject4Chase.getY());
        //HaloLogger.logE("test__hah","screen end");
        mRenderPath = mNaviPathDataProvider.getNaviPathByLevel(level,mObject4Chase.getX(),mObject4Chase.getY()).get(0);
        addRoadNet2Scene();
    }

    /**********************************SceneUpdater*********************************************/
    public ArwaySceneUpdater getSceneUpdater() {
        return mSceneUpdater;
    }

    private ObjectAnimator createViewAlphaAnimator(View view, float from, float to, long duration){
        ObjectAnimator animator=  ObjectAnimator
                .ofFloat(view, "Alpha", from, to);
        animator.setDuration(duration);
        return animator;
    }
    private void restartAnimator(ObjectAnimator a){
        if(a.isStarted()){
            a.cancel();
        }
        a.start();
    }

    public static final int SCENE_HIDE_ANIMATION_ID = 0;
    public static final int SCENE_RENDER_APLLY_ID = 1;

    private Handler        mHandler      = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SCENE_HIDE_ANIMATION_ID:
                    restartAnimator(mHideAnimator);
                    break;
                case SCENE_RENDER_APLLY_ID:
                    mSceneUpdater.clearSceneObjects();
                    mSceneUpdater.commitRender();
                    break;
                default:
            }
        }
    };
    private ObjectAnimator mHideAnimator = null;
    private ObjectAnimator mShowAnimator = null;

    private void initSceneAnimator(){
        int duration = 100;
        float invivable = 0.6f;
        mShowAnimator = createViewAlphaAnimator(mTextureView,invivable,1,duration);
        mHideAnimator = createViewAlphaAnimator(mTextureView,1,invivable,duration);
        mHideAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mHandler.sendEmptyMessage(SCENE_RENDER_APLLY_ID);
                restartAnimator(mShowAnimator);
                HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, String.format("onAnimationEnd called,thread is = %s",Thread.currentThread().getId()));
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

}
