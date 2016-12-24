package com.haloai.hud.hudendpoint.arwaylib.arway.impl_gl;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;

import com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl.NaviAnimationNotifer;
import com.haloai.hud.hudendpoint.arwaylib.modeldataengine.ICarADASDataProvider;
import com.haloai.hud.hudendpoint.arwaylib.modeldataengine.ILaneADASDataProvider;
import com.haloai.hud.hudendpoint.arwaylib.modeldataengine.INaviPathDataProvider;
import com.haloai.hud.hudendpoint.arwaylib.modeldataengine.IRoadNetDataProvider;
import com.haloai.hud.hudendpoint.arwaylib.render.camera.ARWayCameraCaculatorY;
import com.haloai.hud.hudendpoint.arwaylib.render.camera.CameraModel;
import com.haloai.hud.hudendpoint.arwaylib.render.camera.CameraParam;
import com.haloai.hud.hudendpoint.arwaylib.render.object3d.ARWayRoadBuffredObject;
import com.haloai.hud.hudendpoint.arwaylib.render.object3d.BaseObject3D;
import com.haloai.hud.hudendpoint.arwaylib.render.options.RoadRenderOption;
import com.haloai.hud.hudendpoint.arwaylib.render.refresher.RenderParamsInterpolator;
import com.haloai.hud.hudendpoint.arwaylib.render.refresher.RenderParamsInterpolatorListener;
import com.haloai.hud.hudendpoint.arwaylib.render.scene.AdasSceneUpdater;
import com.haloai.hud.hudendpoint.arwaylib.render.scene.ArwaySceneUpdater;
import com.haloai.hud.hudendpoint.arwaylib.render.strategy.IRenderStrategy;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayCurver;
import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;
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
public class ARwayRenderer extends Renderer implements IAnimationListener, IRenderStrategy.RenderParamsNotifier,
        INaviPathDataProvider.INaviPathDataChangeNotifer, IRoadNetDataProvider.IRoadNetDataNotifier,
        RenderParamsInterpolatorListener, ICarADASDataProvider.ICarADASNotifier, ILaneADASDataProvider.ILaneADASNotifier {
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

    private TextureView         mTextureView = null;
    //list data
    private List<List<Vector3>> mRenderPaths = new ArrayList<>();
    private List<Vector3>       mRenderPath  = new ArrayList<>();

    //rajawali about
    private Object3D     mObject4Chase;
    private Object3D     mAdasCarObject;
    private BaseObject3D mAdasDetectObject;

    private ArwaySceneUpdater mSceneUpdater = null;//ArwaySceneUpdater.getInstance()
    private AdasSceneUpdater  mAdasUpdater  = null;


    //about animation
    private TranslateAnimation3D  mTransAnim  = null;
    private RotateOnAxisAnimation mRotateAnim = null;

    private TranslateAnimation3D  mAdasAnim  = null;
    private RotateOnAxisAnimation mAdasRotateAnim = null;

    //state
    //ps:mIsInitScene代表Rajawali自己初始化场景是否完成
    //ps:mIsMyInitScene代表我们得到数据后去add元素到场景中是否完成
    //ps:mCanInitScene代表我们是否能够去add元素到场景中(满足一下条件即可:1.数据到来setPath被调用 2.本次数据未被加载成元素添加到场景中)
    private boolean mIsInitScene      = false;
    private boolean mIsMyInitScene    = false;
    private boolean mCanMyInitScene   = false;
    private boolean mIsReadyForUpdate = false;
    private boolean mIsRenderEndPath  = false;


    //about camera
    private CameraModel mCameraModel            = new CameraModel();
    private float       mRoadWidthProportion    = 0.13f;
    private float       mCameraPerspectiveAngel = 70;


    private RenderParamsInterpolator mParamsRefresher = new RenderParamsInterpolator();


    //time recorder
    private TimeRecorder mRenderTimeRecorder = null;{
        mRenderTimeRecorder = new TimeRecorder();
        mRenderTimeRecorder.enableTimeFilter(false);
        mRenderTimeRecorder.setLogFilterTime(0);

    }

    //新架构
    private INaviPathDataProvider        mNaviPathDataProvider;
    private IRoadNetDataProvider         mRoadNetDataProvider;
    private IRenderStrategy.RenderParams mRenderParams;
    private ICarADASDataProvider         mCarADASDataProvider;
    private ILaneADASDataProvider mLaneADASDataProvider;

    private NaviAnimationNotifer mNaviAnimationNotifer;

    private ObjectAnimator mHideAnimator = null;
    private ObjectAnimator mShowAnimator = null;

    public static final int SCENE_HIDE_ANIMATION_ID = 0;
    public static final int SCENE_RENDER_APLLY_ID   = 1;
    public static final int RENDR_ROAD_NET_ID   = 2;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SCENE_HIDE_ANIMATION_ID:
                    restartAnimator(mHideAnimator);
                    break;
                case SCENE_RENDER_APLLY_ID:
                    mSceneUpdater.commitRender();
                    break;
                case RENDR_ROAD_NET_ID:
                    break;
                default:
                    break;
            }
        }
    };

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
        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "ARRender init called! ,thread id = " + Thread.currentThread().getId());
//        replaceAndSwitchScene(getCurrentScene(),new ARWayScene(this));
        getCurrentScene().setBackgroundColor(0, 0, 0, 0);
        if(ARWayConst.IS_DEBUG_MODE){
//            getCurrentScene().setBackgroundColor(Color.GRAY);
        }
        setFrameRate(FRAME_RATE);
        initHandler();
        initSceneUpdater();
        mIsInitScene = true;
        if (!mIsMyInitScene && mCanMyInitScene) {
            myInitScene();
        }
    }

    private void initHandler() {
        /*mHandler =  new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case SCENE_HIDE_ANIMATION_ID:
                        restartAnimator(mHideAnimator);
                        break;
                    case SCENE_RENDER_APLLY_ID:
                        mSceneUpdater.commitRender();
                        break;
                    case RENDR_ROAD_NET_ID:
                        addNaviPath2Scene();
                        addRoadNet2Scene();
                        break;
                    default:
                }
            }
        };*/
    }

    private void initSceneUpdater() {
        mSceneUpdater = ArwaySceneUpdater.getInstance();
        RoadRenderOption roadOption =  mSceneUpdater.getRenderOptions();
        roadOption.setLayersWidth(1);
        mSceneUpdater.setRenderer(this);
        mSceneUpdater.setContext(getContext());
        mSceneUpdater.setScene(getCurrentScene());
        mSceneUpdater.initScene();
        mSceneUpdater.setCamera(getCurrentCamera());

        if (ARWayConst.IS_ADAS) {
            mAdasUpdater = AdasSceneUpdater.getInstance();
            mAdasUpdater.setRenderer(this);
            mAdasUpdater.setOptions(mSceneUpdater.getRenderOptions());
            mAdasUpdater.setContext(getContext());
            mAdasUpdater.initScene();

            mAdasDetectObject = mSceneUpdater.getTrafficDetectionLayer();
            mAdasUpdater.setYawLaneObject(mSceneUpdater.getYawLaneLayer());
            mAdasUpdater.setTrafficDetectionLayer(mAdasDetectObject);
            mAdasUpdater.setAdasCarObject(mSceneUpdater.getAdasCarObject());

            mAdasCarObject = mSceneUpdater.getAdasCarObject();
            mAdasCarObject.setVisible(false);
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
        mRenderTimeRecorder.start("onRender");

        //======
        mRenderStartTime = System.currentTimeMillis();
        mRenderAllstartTime = mRenderStartTime;
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append(" ARWayRenderer render time : ");
        //====

        if (!mIsMyInitScene) {
            return;
        }
        if (mObject4Chase != null) {
            //deltaTime表示每一帧间隔的秒数,注意单位是秒
            //一帧一帧去通过车速实时计算位置角度等,作用于被追随物体的摄像头移动方式
            //updateObject4Chase(mObject4Chase, mCarSpeed, deltaTime);
            //updateCamera(mObject4Chase);
            //Log.e("ylq","carPosition:"+mObject4Chase.getPosition());
            mParamsRefresher.cameraRefresh(getCurrentCamera(), mObject4Chase.getPosition(), mObject4Chase.getRotZ());
        }

//        recordTime(stringBuilder,"cameraRefresh:");

        if(ARWayConst.IS_ADAS){
            mAdasUpdater.onRender(ellapsedRealtime,deltaTime);
            calculateTrafficDetectionObject();
        }

        mSceneUpdater.onRender(ellapsedRealtime, deltaTime);
//        recordTime(stringBuilder,"mSceneUpdater.onRender:");

        super.onRender(ellapsedRealtime, deltaTime);
//        recordTime(stringBuilder,"super.onRender:");

//        mRenderTimeRecorder.recordeAndLog(ARWayConst.NECESSARY_LOG_TAG,"ARWayRenderer::onRender: "+stringBuilder.toString());
        //打印摄像头信息
        /*{
            Camera camera = getCurrentCamera();
            Vector3 lookat = camera.getLookAt();
            Vector3 postion = camera.getPosition();
            Quaternion orientation =  camera.getOrientation();
            double distance = Vector3.distanceTo(lookat,postion);
            if(distance>10){
                HaloLogger.postE(ARWayConst.ERROR_LOG_TAG,"camera distance error  "+distance);
            }
            mRenderTimeRecorder.timerLog(ARWayConst.INDICATE_LOG_TAG,String.format("camera info , distance %s , orientation %s ,%s , %s",distance,orientation.getRoll(),orientation.getPitch(),orientation.getYaw()));
//            mRenderTimeRecorder.timerLog(ARWayConst.INDICATE_LOG_TAG,String.format("camera info , lookat %s,%s,%s , postion %s %s %s , distance %s ",lookat.x,lookat.y,lookat.z,postion.x,postion.y,postion.z,Vector3.distanceTo(lookat,postion)));
        }*/
//        if (ARWayConst.ENABLE_PERFORM_TEST) {
//            mRenderTimeRecorder.recordeAndLog(ARWayConst.ERROR_LOG_TAG, String.format("onRenderFrame , scene %s , buffredobjcet %s ,",getCurrentScene().getNumChildren(), ARWayRoadBuffredObject.totalTime));
//        }
        ARWayRoadBuffredObject.totalTime =0;
    }
    private long mRenderStartTime = System.currentTimeMillis();
    private long mRenderAllstartTime = mRenderStartTime;
    private long mRenderCurrentTime = System.currentTimeMillis();

    private void recordTime(StringBuilder stringBuilder,String name){
//        mRenderCurrentTime = System.currentTimeMillis();
//        stringBuilder.append(String.format(",%s,%s",name,mRenderCurrentTime-mRenderStartTime));
//        mRenderStartTime = mRenderCurrentTime;
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
        if (mSceneUpdater != null) {
            mSceneUpdater.reset();
            mSceneUpdater.clearScene();
        }
        clearScene();
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

    public void initDefaultRenderParams(IRenderStrategy.RenderParams params) {

        mParamsRefresher.initDefaultRenderParmars(params.dataLevel.getLevel(), params.glCameraAngle, params.glInScreenProportion, params.glScale, params.offset);
        mParamsRefresher.setRenderParamsInterpolatorListener(this);
    }


    private void myInitScene() {
        HaloLogger.logE("ylq__", "myInitScene");
        if (mRenderTimeRecorder != null) {
            mRenderTimeRecorder.reset();
        }
        clearScene();
        mSceneUpdater.reset();
        if(ARWayConst.IS_ADAS){
            mAdasUpdater.reset();
        }
        //啊奇
        mSceneUpdater.getRenderOptions().setLayersWidth((float) mParamsRefresher.getInitializtionRoadWidth());

        mObject4Chase = mSceneUpdater.getCarObject();
        mObject4Chase.setPosition(mRenderPath.get(0).x, mRenderPath.get(0).y, 0);
        double rotateZ = mNaviPathDataProvider == null ? 0 : mNaviPathDataProvider.getObjStartOrientation();
        HaloLogger.logE("test_bug","show degrees : "+(-rotateZ));
        mObject4Chase.setRotation(Vector3.Axis.Z, -rotateZ);
        HaloLogger.logE("ylq__", "__" + mRenderPath.get(0));
        HaloLogger.logE("ylq__", "__" + rotateZ);

        Camera camera = getCurrentCamera();
        camera.enableLookAt();
        camera.setUpAxis(Vector3.Axis.Z);

        getCurrentCamera().setNearPlane(ARWayConst.CAMERA_NEAR_PLANE);
        getCurrentCamera().setFarPlane(ARWayConst.CAMERA_FAR_PLANE);

        Vector3 position = new Vector3();
        Vector3 lookAt = new Vector3();
        CameraParam param = new CameraParam(mObject4Chase.getPosition(), mObject4Chase.getRotZ(), 1.0, 60, 0, 0);
        HaloLogger.postE(ARWayConst.SPECIAL_LOG_TAG,String.format(" camera info == first init  "+param.toString()));
        Camera curCamera = getCurrentCamera();
        HaloLogger.postE(ARWayConst.SPECIAL_LOG_TAG,String.format(" camera info == first init ,isInitialized %s , isLookAtEnabled %s , %s isLookAtValid ",
                curCamera.isInitialized(),curCamera.isLookAtEnabled(),curCamera.isLookAtValid()));
        ARWayCameraCaculatorY.calculateCameraPositionAndLookAtPoint(param, position, lookAt);
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
        initRoadNet2Scene();
        initNaviPath2Scene();
        if (mIsRenderEndPath) {
            mIsRenderEndPath = false;
            onEndPath();
        }

        //update flag
        mIsMyInitScene = true;
        mCanMyInitScene = false;
    }

    private void initRoadNet2Scene() {
        List<List<Vector3>> branchLinesList = new ArrayList<>();
        branchLinesList.addAll(mRenderPaths);//mRenderPaths.subList(1,mRenderPaths.size())
        mSceneUpdater.renderRoadNet(branchLinesList);
        mSceneUpdater.commitRender();
    }

    private void addRoadNet2Scene() {
        List<List<Vector3>> branchLinesList = new ArrayList<>();
        branchLinesList.addAll(mRenderPaths);
        mSceneUpdater.renderRoadNet(branchLinesList);
        mSceneUpdater.removeRoadNet();
        mSceneUpdater.commitRender();

    }

    private void initNaviPath2Scene() {
        //mSceneUpdater.renderModelTrafficLight(mRenderPath.get(4),0);
        initStartScene(mRenderPath);
        mSceneUpdater.renderStartScene(mRenderPath);
        mSceneUpdater.renderNaviPath(mRenderPath);
        //        mSceneUpdater.moveCenterFloor((float) (mNaviPathDataProvider.getLeftborder()+mNaviPathDataProvider.getRightborder())/2,(float)(mNaviPathDataProvider.getTopborder()+mNaviPathDataProvider.getBottomborder())/2);
        mSceneUpdater.renderFloor((float) mNaviPathDataProvider.getLeftborder(), (float) mNaviPathDataProvider.getTopborder(), (float) mNaviPathDataProvider.getRightborder(), (float) mNaviPathDataProvider.getBottomborder(), 1, 0.f);
        mSceneUpdater.commitRender();
        
    }

    public void naviStartAnimation() {
        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "onNaviStartAnimation initStartScene ");

        final int rotationTime = 2;
        final int transTime = 1;

        mIsReadyForUpdate = false;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIsReadyForUpdate = true;
            }
        },(rotationTime+transTime)*1000);
        //先加载fragment view动画
        //摄像头旋转动画
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mParamsRefresher.doAngelAnimation(50, rotationTime);
                mParamsRefresher.doScaleAnimation(mParamsRefresher.getInitializtionLevel(), 3.9f, rotationTime);
                mParamsRefresher.doInScreenProportion(0.4f, rotationTime);
                mParamsRefresher.doOffsetAnimation(0.7f, rotationTime);

            }
        }, (transTime) * 1000);
    }

    private void initStartScene(List<Vector3> path) {
        if ( path != null && path.size() >= 2) {
            Vector3 tmpStart = new Vector3(path.get(0));
            final Vector3 end = new Vector3(path.get(1));
            final Vector3 start = new Vector3();
            MathUtils.longerPoint(start, tmpStart, end, -3);
            float direction = (float) Math.atan2(end.y - tmpStart.y, end.x - tmpStart.x);
            List<Vector3> lines = new ArrayList<>();
            lines.add(start);
            lines.add(end);
            HaloLogger.postI(ARWayConst.NECESSARY_LOG_TAG,String.format("arender initStartScene , direction %s , tmpStart %s ,end %s , %s start",direction,tmpStart,end,start));

            if (!Double.isNaN(direction)){
                mObject4Chase.setRotation(Vector3.Axis.Z, 90 - Math.toDegrees(direction));
            }else {
                HaloLogger.postE(ARWayConst.NECESSARY_LOG_TAG,String.format("arender initStartScene , direction is NAN"));
            }

            mSceneUpdater.renderNaviPath(lines, 10);
            if (false && ARWayConst.IS_DEBUG_MODE) {
                Sphere sphere = new Sphere(0.5f, 20, 20);
                sphere.setMaterial(new Material());
                getCurrentScene().addChild(sphere);
                sphere.setPosition(end);
            }else {
                HaloLogger.postI(ARWayConst.NECESSARY_LOG_TAG,String.format("arender initStartScene ,error path is small "));
            }
        }
    }

    private void addNaviPath2Scene() {
        //mSceneUpdater.renderModelTrafficLight(mRenderPath.get(4),0);
        mSceneUpdater.renderNaviPath(mRenderPath);
        //        mSceneUpdater.moveCenterFloor((float) mRenderPath.get(0).x,(float) mRenderPath.get(0).y);
        //        mSceneUpdater.moveCenterFloor((float) (mNaviPathDataProvider.getLeftborder()+mNaviPathDataProvider.getRightborder())/2,(float)(mNaviPathDataProvider.getTopborder()+mNaviPathDataProvider.getBottomborder())/2);
        mSceneUpdater.renderFloor((float) mNaviPathDataProvider.getLeftborder(), (float) mNaviPathDataProvider.getTopborder(), (float) mNaviPathDataProvider.getRightborder(), (float) mNaviPathDataProvider.getBottomborder(), 1, 0.f);
        mSceneUpdater.removeFloor();
        mSceneUpdater.removeNaviPath();
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
        // FIXME: 22/12/2016 动画没有清除
        if (mTransAnim != null) {
            getCurrentScene().unregisterAnimation(mTransAnim);
        }
        if (mRotateAnim != null) {
            getCurrentScene().unregisterAnimation(mRotateAnim);
        }
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
        HaloLogger.postI(ARWayConst.NECESSARY_LOG_TAG, "render onPathInit enter");
        if (mNaviPathDataProvider != null) {
            //啊奇
            IRenderStrategy.DataLevel level = IRenderStrategy.DataLevel.LEVEL_20;
            for (IRenderStrategy.DataLevel temple : IRenderStrategy.DataLevel.values()) {
                if (temple.getLevel() == mParamsRefresher.getInitializtionLevel()) {
                    level = temple;
                    break;
                }
            }

            Vector3 curObjPos = new Vector3(0, 0, 0);
            if (mIsMyInitScene) {
                curObjPos.setAll(mObject4Chase.getPosition());
            }
            mRenderPaths = new ArrayList<>();
            List<List<Vector3>> renderPaths = mNaviPathDataProvider.getNaviPathByLevel(level, curObjPos.x, curObjPos.y);
            for (List<Vector3> path : renderPaths) {
                List<Vector3> curvePath = new ArrayList<>();
                ARWayCurver.makeCurvePlanB(path, curvePath);
                mRenderPaths.add(curvePath);
            }
            //mRenderPaths = renderPaths;
            mRenderPath = mRenderPaths.get(0);

            if (mRenderPath != null && mRenderPath.size() >= 2) {
                mCanMyInitScene = true;
                if (mIsInitScene) {
                    myInitScene();
                }
            }

        }
        HaloLogger.postI(ARWayConst.NECESSARY_LOG_TAG, String.format("render onPathInit exit, path size %s",mRenderPath.size()));
    }

    @Override
    public void onEndPath() {
        if (!mIsInitScene) {
            mIsRenderEndPath = true;
            return;
        }
        HaloLogger.postI(ARWayConst.NECESSARY_LOG_TAG, "onEndPath called ");
        mRenderPath = mRenderPaths.get(0);
        if (mRenderPath.size() >= 2) {
            mSceneUpdater.renderEndScene(mRenderPath);
        }
    }

    @Override
    public void onPathUpdate() {
        HaloLogger.postI(ARWayConst.NECESSARY_LOG_TAG, "onPathUpdate called enter");
        Vector3 curObjPos = new Vector3(0, 0, 0);
        if (mIsMyInitScene) {
            curObjPos.setAll(mObject4Chase.getPosition());
        }
        //啊奇
        IRenderStrategy.DataLevel level = IRenderStrategy.DataLevel.LEVEL_20;
        for (IRenderStrategy.DataLevel temple : IRenderStrategy.DataLevel.values()) {
            if (temple.getLevel() == mParamsRefresher.getInitializtionLevel()) {
                level = temple;
                break;
            }
        }
        mRenderPaths = new ArrayList<>();
        List<List<Vector3>> renderPaths = mNaviPathDataProvider.getNaviPathByLevel(level, curObjPos.x, curObjPos.y);
        for (List<Vector3> path : renderPaths) {
            List<Vector3> curvePath = new ArrayList<>();
            ARWayCurver.makeCurvePlanB(path, curvePath);
            mRenderPaths.add(curvePath);
        }
        //mRenderPaths = renderPaths;
        mRenderPath = mRenderPaths.get(0);
        if (mRenderPath != null && mRenderPath.size() >= 2) {
            addRoadNet2Scene();
            addNaviPath2Scene();
        }
        HaloLogger.postI(ARWayConst.NECESSARY_LOG_TAG, String.format("onPathUpdate called eixt,path size %s",mRenderPath.size()));
    }

    @Override
    public void onAnimUpdate(INaviPathDataProvider.AnimData animData) {
        if (mIsMyInitScene) {
            clearLastAnim();
            HaloLogger.logE("test_bug","onAnimUpdate:"+animData.duration);
            startAnim(animData.from, animData.to, animData.degrees, animData.duration);
        }

    }

    @Override
    public void onGuideLineUpdate(List<Vector3> guideLineUpdate) {
        if (mIsMyInitScene) {
            mSceneUpdater.renderGuideLine(guideLineUpdate);
        }
    }

    @Override
    public void onRoadNetDataChange() {

    }

    @Override
    public void onTrafficLight(List<Vector3> lights) {
        if (mIsMyInitScene) {
            mSceneUpdater.renderTrafficLight(lights);
        }
    }

    @Override
    public void onTrafficCamera(Vector3 postion, int type) {
        if (mIsMyInitScene) {
            mSceneUpdater.renderTrafficCamera(postion,type);
        }
    }

    @Override
    public void onRenderParamsUpdated(IRenderStrategy.RenderParams renderParams, int animationType, double duration) {
        //Log.e("ylq","onRenderParamsUpdated");
        if (!mIsReadyForUpdate) {
            return;
        }
        if ((animationType & IRenderStrategy.SCALE_TYPE) == IRenderStrategy.SCALE_TYPE) {
            mParamsRefresher.doScaleAnimation(renderParams.dataLevel.getLevel(), renderParams.glScale, duration);
        }
        if ((animationType & IRenderStrategy.ANGLE_TYPE) == IRenderStrategy.ANGLE_TYPE) {
            mParamsRefresher.doAngelAnimation(renderParams.glCameraAngle, duration);
        }
        if ((animationType & IRenderStrategy.INSCREENPROPORTION_TYPE) == IRenderStrategy.INSCREENPROPORTION_TYPE) {
            mParamsRefresher.doInScreenProportion(renderParams.glInScreenProportion, duration);
        }
        if ((animationType & IRenderStrategy.OFFSET_TYPE) == IRenderStrategy.OFFSET_TYPE) {
            mParamsRefresher.doOffsetAnimation(renderParams.offset, duration);
        }
        /*switch (animationType) {
            case IRenderStrategy.SCALE_TYPE:
                mParamsRefresher.doScaleAnimation(renderParams.dataLevel.getLevel(), renderParams.glScale, duration);
                break;
            case IRenderStrategy.ANGLE_TYPE:
                mParamsRefresher.doAngelAnimation(renderParams.glCameraAngle, duration);
                break;
            case IRenderStrategy.INSCREENPROPORTION_TYPE:
                mParamsRefresher.doInScreenProportion(renderParams.glInScreenProportion, duration);
                break;
            case IRenderStrategy.SCALE_TYPE | IRenderStrategy.ANGLE_TYPE:
                mParamsRefresher.doScaleAnimation(renderParams.dataLevel.getLevel(), renderParams.glScale, duration);
                mParamsRefresher.doAngelAnimation(renderParams.glCameraAngle, duration);
                break;
            case IRenderStrategy.SCALE_TYPE | IRenderStrategy.INSCREENPROPORTION_TYPE:
                mParamsRefresher.doScaleAnimation(renderParams.dataLevel.getLevel(), renderParams.glScale, duration);
                mParamsRefresher.doInScreenProportion(renderParams.glInScreenProportion, duration);
                break;
            case IRenderStrategy.ANGLE_TYPE | IRenderStrategy.INSCREENPROPORTION_TYPE:
                mParamsRefresher.doAngelAnimation(renderParams.glCameraAngle, duration);
                mParamsRefresher.doInScreenProportion(renderParams.glInScreenProportion, duration);
                break;
            case IRenderStrategy.ANGLE_TYPE | IRenderStrategy.INSCREENPROPORTION_TYPE | IRenderStrategy.SCALE_TYPE:
                mParamsRefresher.doScaleAnimation(renderParams.dataLevel.getLevel(), renderParams.glScale, duration);
                mParamsRefresher.doAngelAnimation(renderParams.glCameraAngle, duration);
                mParamsRefresher.doInScreenProportion(renderParams.glInScreenProportion, duration);
                break;
            default:
                //
                Log.e("ylq", "Worng AnimationType");
                break;
        }*/
    }

    @Override
    public void onAnimationUpdated(IRenderStrategy.AnimationType type) {

    }

    @Override
    public void onRefreshDataLevel(int DataLevel, double roadWidth) {
        Log.e("ylq", "onRefreshDataLevel:" + DataLevel);
        //add

        IRenderStrategy.DataLevel level = IRenderStrategy.DataLevel.LEVEL_20;
        for (IRenderStrategy.DataLevel temple : IRenderStrategy.DataLevel.values()) {
            if (temple.getLevel() == DataLevel) {
                level = temple;
                break;
            }
        }

        clearLastAnim();

        mParamsRefresher.cameraRefresh(getCurrentCamera(), mObject4Chase.getPosition(), mObject4Chase.getRotZ());

        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, String.format("onRenderParamsUpdated called,thread is = %s", Thread.currentThread().getId()));
        mSceneUpdater.getRenderOptions().setLayersWidth((float) roadWidth);
        //        mSceneUpdater.renderFloor(-100,100,100,-100,1);
        List<Vector3> naviPath = mNaviPathDataProvider.getNaviPathByLevel(level, mObject4Chase.getX(), mObject4Chase.getY()).get(0);
        mSceneUpdater.renderNaviPath(naviPath);

        mHandler.sendEmptyMessage(SCENE_HIDE_ANIMATION_ID);
    }

    int mRefreshDataCnt = 0;

    public void setNaviAnimationNotifer(NaviAnimationNotifer naviAnimationNotifer) {
        mNaviAnimationNotifer = naviAnimationNotifer;
    }

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

    public void changeStrategy(IRenderStrategy.DataLevel level) {
        clearLastAnim();
        //HaloLogger.logE("test__hah","screen start");
        //HaloLogger.logE("test__hah",mObject4Chase.getX()+","+mObject4Chase.getY());
        //HaloLogger.logE("test__hah","screen end");
        mRenderPath = mNaviPathDataProvider.getNaviPathByLevel(level, mObject4Chase.getX(), mObject4Chase.getY()).get(0);
        addRoadNet2Scene();
    }

    /**********************************
     * SceneUpdater
     *********************************************/
    public ArwaySceneUpdater getSceneUpdater() {
        return mSceneUpdater;
    }

    private ObjectAnimator createViewAlphaAnimator(View view, float from, float to, long duration) {
        ObjectAnimator animator = ObjectAnimator
                .ofFloat(view, "Alpha", from, to);
        animator.setDuration(duration);
        return animator;
    }

    private void restartAnimator(ObjectAnimator a) {
        if (a.isStarted()) {
            a.cancel();
        }
        a.start();
    }

    private void initSceneAnimator() {
        int duration = 100;
        float invivable = 0.6f;
        mShowAnimator = createViewAlphaAnimator(mTextureView, invivable, 1, duration);
        mHideAnimator = createViewAlphaAnimator(mTextureView, 1, invivable, duration);
        mHideAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mHandler.sendEmptyMessage(SCENE_RENDER_APLLY_ID);
                restartAnimator(mShowAnimator);
                HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, String.format("onAnimationEnd called,thread is = %s", Thread.currentThread().getId()));
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    @Override
    public void onCarShow(double x,double y,double z,double direction) {
        if (mAdasUpdater != null) {
            mAdasCarObject.setVisible(true);
            mAdasCarObject.setPosition(x,y,z);
            mAdasCarObject.setRotation(Vector3.Axis.Z,Math.toDegrees(mObject4Chase.getRotZ()));
        }
    }

    @Override
    public void onCarAnimationUpdate(ICarADASDataProvider.AnimData animData) {
        if (!mIsMyInitScene || mAdasUpdater == null) {
            return;
        }

        if (mAdasAnim != null) {
            if (mAdasAnim.isPlaying()) {
                mAdasAnim.pause();
            }
            getCurrentScene().unregisterAnimation(mAdasAnim);
            mAdasAnim = null;
        }
        if (mAdasRotateAnim != null) {
            if (mAdasRotateAnim.isPlaying()) {
                mAdasRotateAnim.pause();
            }
            getCurrentScene().unregisterAnimation(mAdasRotateAnim);
            mAdasRotateAnim = null;
        }
        mAdasAnim = createTranslateAnim(animData.from, animData.to, animData.duration, mAdasCarObject);
        double degrees =  animData.degrees;
        mAdasRotateAnim = createRotateAnim(Vector3.Axis.Z,
                Math.abs(degrees) > 180 ? (degrees > 0 ? degrees - 360 : degrees + 360) : degrees,
                animData.duration, mAdasCarObject);
        mAdasAnim.play();
        mAdasRotateAnim.play();
    }

    private void calculateTrafficDetectionObject() {
        if (mAdasDetectObject == null || mObject4Chase == null || mAdasCarObject == null) {
            return;
        }
        String tag = "test_roation";
        double dist = 1;
        double roz = mObject4Chase.getRotZ();
        Vector3 carPostion = new Vector3(mObject4Chase.getPosition());
        MathUtils.rotateAround(carPostion.x, carPostion.y, carPostion.x + dist, carPostion.y, carPostion, Math.PI / 2 - roz);
        mAdasDetectObject.setPosition(carPostion);

        /*Vector3 chase = new Vector3(mObject4Chase.getPosition());
        Vector3 destPos = new Vector3(mAdasCarObject.getPosition());
        double radius = Math.atan2(destPos.y-chase.y,destPos.x-chase.x);
        radius = radius< Math.PI?radius:2*Math.PI-radius;
        double degress = 90-Math.toDegrees(radius);
        HaloLogger.logE(tag,String.format(" radius %s destPos %s,curPostion %s ,degress %s ",radius,destPos,carPostion,degress));*/

        mAdasDetectObject.setRotation(Vector3.Axis.Z, Math.toDegrees(roz));
    }
    @Override
    public void onDistChange(double dist) {
        // TODO: 21/11/2016 确认方向
        if (mAdasUpdater != null) {
            mAdasUpdater.updateTrafficDetection(dist,90);
            mAdasDetectObject.setVisible(true);
            calculateTrafficDetectionObject();
        }
    }

    @Override
    public void onCarHide() {
        if (mAdasUpdater != null) {
            mAdasCarObject.setVisible(false);
            mAdasDetectObject.setVisible(false);
        }
    }



    @Override
    public void onShowLaneADAS(List<Vector3> path, boolean isLeft) {
        if (mAdasUpdater != null) {
            mAdasUpdater.showLaneYawLine(path,isLeft);
        }
    }

    @Override
    public void onHideLaneADAS() {
        if (mAdasUpdater != null) {
            mAdasUpdater.hideLaneYawLine();
        }
    }

    //adas
    @Override
    public void setCarADASDataProvider(ICarADASDataProvider adasDataProvider) {
        mCarADASDataProvider = adasDataProvider;
    }

    @Override
    public void setLaneADASDataProvider(ILaneADASDataProvider adasDataProvider) {
        mLaneADASDataProvider = adasDataProvider;
    }

    public Vector3 getCurPos4OtherCar() {
        return mAdasCarObject == null ? null : mAdasCarObject.getPosition();
    }

    public double getCurDegrees4OtherCar() {
        return mAdasCarObject == null ? 0 : mAdasCarObject.getRotZ();
    }
}
