package com.haloai.hud.hudendpoint.arwaylib.arway.impl_gl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.SurfaceTexture;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;

import com.amap.api.maps.Projection;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.NaviLatLng;
import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.rajawali.camera.ARWayCameraCaculator;
import com.haloai.hud.hudendpoint.arwaylib.rajawali.camera.CameraModel;
import com.haloai.hud.hudendpoint.arwaylib.rajawali.object3d.ARWayRoadObject;
import com.haloai.hud.hudendpoint.arwaylib.scene.ArwaySceneUpdater;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayProjection;
import com.haloai.hud.hudendpoint.arwaylib.utils.DrawUtils;
import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;
import com.haloai.hud.hudendpoint.arwaylib.utils.TimeRecorder;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.ATransformable3D;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.IAnimationListener;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.animation.SplineTranslateAnimation3D;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.cameras.Camera;
import org.rajawali3d.curves.CatmullRomCurve3D;
import org.rajawali3d.curves.LinearBezierCurve3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.renderer.Renderer;

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
public class ARwayRenderer extends Renderer implements IAnimationListener {
    //content
    private static final double  ANIMATION_LENGTH       = 30;
    private static final double  OBJ_4_CHASE_Z          = 0;
    private static final double  BIGGER_TIME            = ARWayConst.AMAP_TO_ARWAY_GL_RATE;//1;//
    private static final double  CAMERA_MIN_LENGTH      = 20;
    private static final int     FRAME_RATE             = ARWayConst.FRAME_RATE;
    private static final int     CURVE_TIME             = 5;
    private static final double  LOGIC_ROAD_WIDTH       = 0.4;
    private static final double  ROAD_WIDTH             = ARWayConst.ROAD_WIDTH;//0.5*Math.tan(Math.toRadians(22.5))*2*400/280;//
    private static final double  CAMERA_OFFSET_X        = 0;
    private static final double  CAMERA_OFFSET_Y        = 0;
    private static final double  CAMERA_OFFSET_Z        = 0.6;
    private static final double  CAMERA_CUT_OFFSET      = 0.6;
    private static final double  LOOK_AT_DIST           = 1.3;
    private static final int     INTERSECTION_COUNT     = 30;
    private static final double  CAMERA_NEAR_PLANE      = 0.5;
    private static final double  CAMERA_FAR_PLANE       = 25;
    private static final int     CHILD_PATH_SIZE        = 4;
    private static final long    PRETENSION_TIME        = 1000;
    private static final long    ANIM_DURATION_REDUNDAN = 100;
    private static final int     LOAD_PATH_LENGTH       = 250;
    private static final boolean DEBUG_MODE             = false;
    private static final boolean IS_MOVE_PATH           = false;
    private static       int     SCREEN_WIDTH           = 0;
    private static       int     SCREEN_HEIGHT          = 0;
    private static final double  BRANCH_LINE_Z          = -0.01;
    private static final double  ADD_PLANE_LENGTH       = Double.MAX_VALUE;
    private static final String  TAG                    = "com.haloai.hud.hudendpoint.arwaylib.arway.impl_gl.ARwayRenderer";

    //list data
    private List<Vector3>       mPath                = new ArrayList<>();
    private List<Vector3>       mOriginalPath        = new ArrayList<>();
    private List<Vector3>       mLeftPath            = new ArrayList<>();
    private List<Vector3>       mRightPath           = new ArrayList<>();
    private List<Integer>       mStepsLength         = new ArrayList<>();
    private List<Double>        mDist2FinalPoint     = new ArrayList<>();
    private List<Double>        mLength2FinalPoint   = new ArrayList<>();
    private List<Integer>       mCurIndexes          = new ArrayList<>();
    private List<Vector3>       mPreChildEndPos      = new ArrayList<>();
    private List<Vector3>       mChildPathPositions  = new ArrayList<>();
    private List<List<Vector3>> mChildPathes         = new ArrayList<>();
    private List<Vector3>       mLastThroughPosition = new ArrayList<>();
    /*private List<ARWayRoadObject> mMainRoadObjects     = new ArrayList<>();
    private List<ARWayRoadObject> mBranchRoadObjects   = new ArrayList<>();*/

    //rajawali about
    /*private Line3D   mLine3D             = null;
    private Sphere   mSphere             = null;
    private Sphere   mSphere1            = null;
    private Sphere   mSphere2            = null;*/
    /*private Texture  mMainRoadTexture    = null;
    private Texture  mBranchRoadTexture  = null;
    private Texture  mBranchBlackTexture = null;*/
    private Object3D mObject4Chase    = null;
    private Texture  mMainRoadTexture = null;
    /*private Scene    mCurScene        = null;*/

    //data
    private double mTotalDistance        = 0f;
    private double mTotalLength          = 0;
    private double mLength2DistanceScale = 0f;

    //constantly data 实时数据
    private long    mStartTime          = 0l;
    private double  mStartLength        = 0.0;
    private int     mCurIndexInPath     = 0;
    private int     mStartAddPlaneIndex = 0;
    private double  mEndLength          = 0.0;
    private double  mRetainTotalLength  = 0.0;
    private Vector3 mRealCurPosition    = new Vector3();

    //about animation
    private ArrayList<SplineTranslateAnimation3D> mTranslateAnims     = new ArrayList<>();
    private int                                   mTranslateAnimIndex = 0;
    private ArrayList<RotateOnAxisAnimation>      mRotateAnims        = new ArrayList<>();
    private int                                   mRotateAnimIndex    = 0;

    //about screen coord to opengl coord
    /*private int[]    mViewport         = null;
    private double[] mNearPos4         = new double[4];
    private double[] mFarPos4          = new double[4];
    private Vector3  mNearPos          = new Vector3();
    private Vector3  mFarPos           = new Vector3();
    private Matrix4  mViewMatrix       = null;
    private Matrix4  mProjectionMatrix = null;*/

    //state
    //ps:mIsInitScene代表Rajawali自己初始化场景是否完成
    //ps:mIsMyInitScene代表我们得到数据后去add元素到场景中是否完成
    //ps:mCanInitScene代表我们是否能够去add元素到场景中(满足一下条件即可:1.数据到来setPath被调用 2.本次数据未被加载成元素添加到场景中)
    private boolean mIsInitScene    = false;
    private boolean mIsMyInitScene  = false;
    private boolean mCanMyInitScene = false;

    //dynamic load about
    private List<Integer> mLoadStepStartIndexs = new ArrayList<>();
    private List<Double>  mLoadStepLengths     = new ArrayList<>();
    private int           mLoadStepIndex       = 0;

    //image handle
    //private CrossPathManager mCrossPathManager = CrossPathManager.getInstance();

    //else
    private double     mObject4ChaseStartOrientation = 0;
//    private Projection mProjection                   = null;
    private ARWayProjection mProjection                   = new ARWayProjection();
    //车速，单位是m/s
    private double mCarSpeed = 0;
    private double mOffsetX  = 0;
    private double mOffsetY  = 0;

    private ArwaySceneUpdater mSceneUpdater;
    
    private CameraModel mCameraModel = new CameraModel();
    private float mRoadWidthProportion = 0.3f;
    private float mCameraPerspectiveAngel = 76;


    private TimeRecorder mRenderTimeRecorder = new TimeRecorder();

    public ARwayRenderer(Context context) {
        super(context);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        SCREEN_WIDTH = wm.getDefaultDisplay().getWidth();
        SCREEN_HEIGHT = wm.getDefaultDisplay().getHeight();

        setFrameRate(FRAME_RATE);
    }

    @Override
    public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
        super.onRenderSurfaceCreated(config, gl, width, height);
    }

    @Override
    public void initScene() {
        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "ARRender initScene called!");
        /*mViewport = new int[]{0, 0, getViewportWidth(), getViewportHeight()};
        mViewMatrix = getCurrentCamera().getViewMatrix();
        mProjectionMatrix = getCurrentCamera().getProjectionMatrix();*/
        setFrameRate(FRAME_RATE);
        mSceneUpdater = ArwaySceneUpdater.getInstance();
        mSceneUpdater.setScene(getCurrentScene());
        //getCurrentScene().setBackgroundColor(Color.DKGRAY);
        mIsInitScene = true;
        if (!mIsMyInitScene && mCanMyInitScene) {
            myInitScene();
        }
    }

    @Override
    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        super.onRenderSurfaceSizeChanged(gl, width, height);
        mProjection.initScale(width, height);
        /*mViewport[2] = getViewportWidth();
        mViewport[3] = getViewportHeight();
        mViewMatrix = getCurrentCamera().getViewMatrix();
        mProjectionMatrix = getCurrentCamera().getProjectionMatrix();*/
    }

    @Override
    public void onRenderSurfaceDestroyed(SurfaceTexture surface) {
        super.onRenderSurfaceDestroyed(surface);
    }

    private void updateCamera(ATransformable3D transObject) {
        Camera camera = getCurrentCamera();
        Vector3 location = transObject.getPosition();
        Vector3 position = new Vector3(location.x, location.y, CAMERA_OFFSET_Z);
        Vector3 lookat = new Vector3(0, 0, 0);

        if (false) {
            mCameraModel.setLocation(mObject4Chase.getPosition());
            mCameraModel.setRotZ(mObject4Chase.getRotZ());

            ARWayCameraCaculator.calculateCameraPositionAndLookAtPoint(position, lookat, mCameraModel);
        } else {
            updateCameraLookatAndPosition(location, transObject.getRotZ(), LOOK_AT_DIST, position, lookat);
        }
        camera.setPosition(position);
        camera.setLookAt(lookat);
    }

    private void updateCameraLookatAndPosition(Vector3 cPos, double yaw, double dist, Vector3 position, Vector3 lookat) {
        if (cPos == null || position == null || lookat == null) {
            return;
        }
        final double LOOK_OFFSET = dist;
        final double CAMERA_OFFSET = -CAMERA_CUT_OFFSET;
        double offsetY = 0, offsetX = 0;
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

    public void setCarSpeed(double carSpeed) {
        this.mCarSpeed = carSpeed;
    }

    double[] speeds = new double[]{43.0, 42.0, 42.0, 41.0, 40.0, 40.0, 40.0, 40.0, 39.0, 39.0, 39.0, 39.0, 38.0, 38.0,
            37.0, 37.0, 36.0, 36.0, 36.0, 35.0, 36.0, 36.0, 33.0, 34.0, 30.0, 30.0, 29.0, 29.0, 29.0, 29.0, 29.0, 30.0,
            30.0, 31.0, 31.0, 32.0, 32.0, 33.0, 34.0, 35.0, 37.0, 41.0, 42.0, 43.0, 44.0, 45.0, 46.0, 51.0, 51.0, 52.0,
            52.0, 53.0, 54.0, 55.0, 56.0, 57.0, 57.0, 58.0, 59.0, 59.0, 60.0, 60.0, 60.0, 61.0, 62.0, 64.0, 65.0, 66.0,
            68.0, 69.0, 73.0, 74.0, 72.0, 69.0, 66.0, 64.0, 65.0, 66.0, 67.0, 70.0, 70.0, 69.0, 69.0, 69.0, 69.0, 69.0,
            71.0, 72.0, 72.0, 71.0, 72.0, 69.0, 65.0, 65.0, 63.0, 63.0, 63.0, 64.0, 64.0, 64.0, 63.0, 63.0, 63.0, 63.0,
            63.0, 64.0, 62.0, 63.0, 63.0, 59.0, 41.0, 38.0, 38.0, 38.0, 40.0, 42.0, 44.0, 44.0, 44.0, 43.0, 42.0, 41.0,
            40.0, 41.0, 41.0, 40.0, 39.0, 38.0, 37.0, 37.0, 35.0, 33.0, 30.0, 28.0, 27.0, 25.0, 20.0, 15.0, 11.0, 11.0,
            11.0, 13.0, 13.0, 14.0, 18.0, 18.0, 18.0, 17.0, 17.0, 18.0, 17.0, 17.0, 16.0, 16.0, 17.0, 18.0, 19.0, 21.0,
            21.0, 22.0, 23.0, 23.0, 23.0, 22.0, 22.0, 23.0, 23.0, 23.0, 22.0, 20.0, 19.0, 17.0, 15.0, 12.0, 10.0, 9.0,
            7.0, 6.0, 6.0, 6.0, 6.0, 6.0, 4.0, 4.0, 4.0, 5.0, 7.0, 11.0, 12.0, 13.0, 14.0, 16.0, 4.0, 4.0, 0.0, 0.0, 0.0,
            20.0, 20.0, 21.0, 22.0, 22.0, 25.0, 33.0, 40.0, 41.0};

    int     temp           = 0;
    int     mCurIndexAfter = 0;
    boolean mIsRotate      = false;
    double  mFrameRotate   = 0;
    final double FRAME_STEP = 0.5;

    private void updateObject4Chase(Object3D object4Chase, double carSpeed, double deltaTime) {
        // TODO: 2016/9/21 模拟测试时提供速度
        carSpeed = speeds[temp++ / 10 % speeds.length];
        HaloLogger.logE("cross_handle", "1 yaw:" + Math.toDegrees(object4Chase.getRotZ()));
        Vector3 pos = getPosWithSpeed(object4Chase, carSpeed, deltaTime);
        object4Chase.disableLookAt();
        object4Chase.setPosition(pos.x, pos.y, 0.01);
        if (mIsRotate) {
            HaloLogger.logE("cross_handle", "3 rotate:" + mFrameRotate);
            object4Chase.rotate(Vector3.Axis.Z,/*object4Chase.getRotZ()+*/mFrameRotate);
            HaloLogger.logE("cross_handle", "4 yaw:" + Math.toDegrees(object4Chase.getRotZ()));
        }
    }

    private Vector3 getPosWithSpeed(Object3D object4Chase, double speed, double deltaTime) {
        Vector3 pos = object4Chase.getPosition();
        if (speed == 0) {
            return pos;
        }
        //此时的speed表示每秒移动多少opengl中的距离
        speed = speed / 3.6 * mLength2DistanceScale;
        double dist = speed * deltaTime;
        while (mCurIndexAfter + 1 < mPath.size()) {
            Vector3 nextV = mPath.get(mCurIndexAfter + 1);
            double temp = MathUtils.calculateDistance(pos.x, pos.y, nextV.x, nextV.y);
            if (temp > dist) {
                Vector3 result = new Vector3();
                double scale = dist / temp;
                result.x = pos.x + (nextV.x - pos.x) * scale;
                result.y = pos.y + (nextV.y - pos.y) * scale;
                result.z = 0;
                pos = result;

                double angle = MathUtils.getRotateDegreesWithLineAndAngle(
                        mPath.get(mCurIndexAfter).x, mPath.get(mCurIndexAfter).y,
                        mPath.get(mCurIndexAfter + 1).x, mPath.get(mCurIndexAfter + 1).y, object4Chase.getRotZ());
                HaloLogger.logE("cross_handle__", "1.5 need:" + angle);
                // TODO: 2016/9/22 可能由于angle的计算问题导致旋转有误差的出现
                if (angle != 0) {
                    mIsRotate = true;
                    //mFrameRotate = angle / FRAME_STEP;
                    if (Math.abs(angle) < FRAME_STEP) {
                        mFrameRotate = angle;
                    } else {
                        mFrameRotate = angle > 0 ? FRAME_STEP : -FRAME_STEP;
                    }
                    HaloLogger.logE("cross_handle", "2 angle:" + mFrameRotate);
                } else {
                    mIsRotate = false;
                }
                break;
            } else if (temp == dist) {
                mCurIndexAfter++;
                pos = nextV;
                break;
            } else {
                mCurIndexAfter++;
                dist -= temp;
                pos = nextV;
            }
        }
        return pos;
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
            updateCamera(mObject4Chase);
        }
        super.onRender(ellapsedRealtime, deltaTime);

        if(ARWayConst.ENABLE_PERFORM_TEST){
            mRenderTimeRecorder.recordeAndLog("onRenderFrame","onRenderFrame");
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

    //====================================handle cross image start========================================//

    //====================================new handle cross image start==========================================
    public void setEnlargeCrossBranchLines(Bitmap crossImage) {
        /*if (crossImage == null) {
            HaloLogger.logE(TAG, "getBrachLines faild , CrossImage is null!");
            return;
        }
        List<List<Vector3>> branchLines = mCrossPathManager.setEnlargeCrossBranchLiens(crossImage);
        if (branchLines == null || branchLines.size() <= 0) {
            HaloLogger.logE(TAG, "setEnlargeCrossBranchLines is error , do not get the branch line!");
        }
        for (List<Vector3> branchLine : branchLines) {
            Vector3 pos = branchLine.get(0);
            HaloLogger.logE("branch_line", "=============start===============");
            for (Vector3 v : branchLine) {
                HaloLogger.logE("branch_line", v.x + "," + v.y);
                v.x -= pos.x;
                v.y -= pos.y;
                v.z -= pos.z;
                Sphere sphere = new Sphere(0.1f, 24, 24);
                sphere.setPosition(v.x + pos.x, v.y + pos.y, v.z + pos.z);
                sphere.setMaterial(new Material());
                sphere.setColor(Color.BLUE);
                getCurrentScene().addChild(sphere);
            }
            HaloLogger.logE("branch_line", "==============end================");
            if (!mIsMyInitScene) {
                return;
            }
            insertARWayObject(branchLine, pos, ROAD_WIDTH, 2);
        }*/
    }

    public void handleCrossInfo(int currentPathStep, int width, int height) {
        //mCrossPathManager.handleCrossInfo(currentPathStep, width, height);
    }

    /*public void addCrossImageData2Collector(String filePath, String bitmapFileName, CrossImageDataCollector crossImageDataCollector) {
        mCrossPathManager.addCrossImageData2Collector(filePath, bitmapFileName, crossImageDataCollector);
    }*/

    //====================================new handle cross image end============================================


    /**
     * 通过岔路点的集合以及路口放大图中箭头位置的点的坐标会实现将岔路添加到场景中
     */
    public void setEnlargeCrossBranchLines(double length, int naviIcon) {
        /*if (length <= 0 || !mIsMyInitScene) {
            return;
        }

        double divDegrees = 0;
        double rotation = 0;
        Vector3 center = new Vector3();
        Vector3 next = new Vector3();
        Vector3 fin = new Vector3();
        List<Vector3> leftPath = new ArrayList<>();
        List<Vector3> rightPath = new ArrayList<>();
        List<Vector3> intersectPath = new ArrayList<>();
        //注意此时并不一定是300m,可能多一点或者少很多或者一点
        //获取中心点以及中心点下一个点,下一个点的获取策略为中心点后CURVE_TIME/2个点
        rotation = getCenterAndNext(naviIcon, center, next, fin, leftPath, rightPath, intersectPath, length);
        //divDegrees = getDivDegrees(center, next, mainRoadTailend);
        List<Vector3> branchPointsOpengl = new ArrayList<>();
        List<Vector3> branchPointsOpengl2 = new ArrayList<>();
        //        HaloLogger.logE("helong_fix____", "next:" + next);
        //        HaloLogger.logE("helong_fix____", "length:" + length);
        //        HaloLogger.logE("helong_fix____", "degrees:" + divDegrees);
        //        HaloLogger.logE("helong_fix____", "branch line size:" + branchLines.size());
        for (int i = 0; i < 1*//*branchLines.size()*//*; i++) {
            //            EnlargedCrossProcess.ECBranchLine branch = branchLines.get(i);
            //            //得到的是以路口放大图400*400中心为原点的岔路的相对坐标,还需要:1.像素转Opengl,2.旋转坐标到当前状态,3.考虑坐标太大导致的抖动问题,也就是添加pos部分
            //            if (branch == null) {
            //                continue;
            //            }
            //            List<Point> branchPointsScreen = branch.getLinePoints();
            //            if (branchPointsScreen == null || branchPointsScreen.size() < 2) {
            //                continue;
            //            }
            //            branchPointsOpengl.clear();
            //            //0.坐标过滤
            //            filterBranchCoordinate(branchPointsScreen);
            //            //1.坐标转换
            //            convertScreen2Opengl(branchPointsScreen, branchPointsOpengl, center);
            //            //2.旋转坐标
            //            rotateBranchWithDivDegrees(branchPointsOpengl, divDegrees);

            // TODO: 2016/7/25 模拟假数据来实现路口放大图
            branchPointsOpengl.clear();
            branchPointsOpengl2.clear();
            boolean isSimulation = false;
            if (branchPointsOpengl.size() <= 0) {
                isSimulation = true;
            }
            CatmullRomCurve3D catmullRomCurve3D = new CatmullRomCurve3D();
            catmullRomCurve3D.addPoint(new Vector3(center));
            catmullRomCurve3D.addPoint(new Vector3(center));
            catmullRomCurve3D.addPoint(new Vector3(next));
            catmullRomCurve3D.addPoint(new Vector3(fin));
            catmullRomCurve3D.addPoint(new Vector3(fin));
            for (int j = 0; j < catmullRomCurve3D.getNumPoints() * 5; j++) {
                Vector3 v = new Vector3();
                catmullRomCurve3D.calculatePoint(v, (1.0 * j) / (catmullRomCurve3D.getNumPoints() * 5));
                branchPointsOpengl.add(v);
            }
            Vector3 offset = null;
            rotateBranchWithDivDegreesTest(branchPointsOpengl, rotation + 180);
            for (int j = 0; j < branchPointsOpengl.size()*//*//*3*//*; j++) {
                *//*Vector3 v = branchPointsOpengl.get(j);
                if(j==0){
                    Vector3 target = branchPointsOpengl.get(branchPointsOpengl.size()/5);
                    offset = new Vector3(v.x-target.x,v.y-target.y,v.z-target.z);
                }
                branchPointsOpengl2.add(new Vector3(v.x-offset.x,v.y-offset.y,v.z-offset.z));*//*
                branchPointsOpengl2.add(new Vector3(branchPointsOpengl.get(j)));
            }
            rotateBranchWithDivDegreesTest(branchPointsOpengl2, rotation + 180);

            //3.计算岔路pos,并通过设置posZ轴值来降低岔路高度,避免覆盖
            Vector3 pos = new Vector3();
            calcPosition(branchPointsOpengl, pos, BRANCH_LINE_Z);
            mBranchRoadObjects.add(insertARWayObject(branchPointsOpengl, pos, ROAD_WIDTH, 2));
            calcPosition(branchPointsOpengl2, pos, BRANCH_LINE_Z - 0.01);
            //insertARWayObject(branchPointsOpengl2, pos, ROAD_WIDTH, 2);

            for (int j = 0; j < intersectPath.size() - 1; j++) {
                mCoverRoadPlanes.add(insertRajawaliPlane(new Vector3(intersectPath.get(j)), new Vector3(intersectPath.get(j + 1))));
            }
            if (isSimulation) {
                break;
            }
        }*/
    }

    private Plane insertRajawaliPlane(Vector3 v1, Vector3 v2) {
        float width = 0f;
        float height = 0f;
        double degrees = MathUtils.getDegrees(v1.x, v1.y, v2.x, v2.y);
        if (Math.abs(v2.x - v1.x) > Math.abs(v2.y - v1.y)) {
            height = (float) MathUtils.calculateDistance(v1.x, v1.y, v2.x, v2.y) + 0.05f;
            width = 0.1f;
        } else {
            width = (float) MathUtils.calculateDistance(v1.x, v1.y, v2.x, v2.y) + 0.05f;
            height = 0.1f;
            //如果是横向的话就相当于已经转了90度
            degrees -= 90;
        }
        Plane plane = new Plane(width, height, 1, 1/*, Vector3.Axis.Z, true, true, 1*/);
        plane.setMaterial(new Material());
        plane.setDoubleSided(true);
        plane.setColor(0x000000);
        plane.setPosition((v1.x + v2.x) / 2, (v1.y + v2.y) / 2, 0.01);
        Quaternion qn = plane.getOrientation();
        qn.fromAngleAxis(Vector3.Axis.Z, degrees);
        plane.setOrientation(qn);
        getCurrentScene().addChild(plane);
        return plane;
    }

    private void rotateBranchWithDivDegreesTest(List<Vector3> branchPointsOpengl, double divDegrees) {
        Vector3 base = new Vector3(branchPointsOpengl.get(0));
        for (int i = 0; i < branchPointsOpengl.size(); i++) {
            MathUtils.rotateCoordinate(base, branchPointsOpengl.get(i), divDegrees);
        }
    }

    /**
     * 过滤岔路点的信息,少于等于12个点就不进行过滤了,否则每10个点取一个
     *
     * @param branchPointsScreen
     */
    private void filterBranchCoordinate(List<Point> branchPointsScreen) {
        if (branchPointsScreen.size() < 12) {
            return;
        }
        for (int i = 1, j = 1; i < branchPointsScreen.size() - 1; i++, j++) {
            if (j % 10 != 0) {
                branchPointsScreen.remove(i--);
            }
        }
    }

    /**
     * 计算岔路的position防止数值过大导致路线抖动
     *
     * @param branchPointsOpengl
     * @param pos
     */
    private void calcPosition(List<Vector3> branchPointsOpengl, Vector3 pos, double z) {
        if (branchPointsOpengl == null || branchPointsOpengl.size() <= 0) {
            return;
        }
        pos.setAll(branchPointsOpengl.get(0));
        //降低岔路高度,避免覆盖主路
        pos.z = z;
        for (Vector3 v : branchPointsOpengl) {
            v.x -= pos.x;
            v.y -= pos.y;
        }
    }

    /**
     * 获取路口放大图与当前道路情况所成夹角(中心点计算,下一个点计算,以及路口放大图上的中心点和箭头部分角度)
     *
     * @param mainRoadTailend
     * @return
     */
    private double getDivDegrees(Vector3 center, Vector3 next, Point mainRoadTailend) {
        return MathUtils.getDegrees(center.x, center.y, next.x, next.y) -
                MathUtils.getDegrees(0, 0, mainRoadTailend.x, mainRoadTailend.y);
    }

    /**
     * 获取中心点以及中心点的后一个点用于之后计算角度
     *
     * @param naviIcon
     * @param center
     * @param next
     * @param leftPath
     * @param rightPath
     */
    private double getCenterAndNext(int naviIcon, Vector3 center, Vector3 next, Vector3 fin, List<Vector3> leftPath, List<Vector3> rightPath, List<Vector3> intersectPath, double length) {
        // TODO: 2016/7/28 由于目前获取中心点总是提前了,因此此处做乘以1.3的应急处理来减少错误误差
        double dist300 = length * 1.2 * mLength2DistanceScale;
        double rotation = 0;
        Vector3 intersect = new Vector3();
        Vector3 intersect2 = new Vector3();
        for (int i = mCurIndexInPath + 1; i < mPath.size(); i++) {
            Vector3 start = null;
            Vector3 end = new Vector3(mPath.get(i));
            if (i == mCurIndexInPath + 1) {
                start = new Vector3(mRealCurPosition);
            } else {
                start = new Vector3(mPath.get(i - 1));
            }
            double dist = MathUtils.calculateDistance(start.x, start.y, end.x, end.y);
            if (dist >= dist300) {
                center.x = start.x + (end.x - start.x) * (dist300 / dist);
                center.y = start.y + (end.y - start.y) * (dist300 / dist);
                center.z = 0;
                double rot = 0;
                double rot2 = 0;
                int index = 0;
                /**
                 * 岔路情况设置:
                 *  2tl:左拐,岔路:一条竖直长岔路,一条向右岔路
                 *  6tlb:左转掉头,岔路:一条向右岔路
                 *  4tlf:左前方行驶,岔路:一条竖直长岔路
                 *  8tb:掉头,岔路:向右岔路
                 *  3tr:右拐,岔路:向左岔路,一条竖直长岔路
                 *  7trb:右转掉头,岔路:一条竖直长岔路
                 *  5trf:右前方行驶,岔路:一条竖直长岔路
                 *  其它:从主路copy一条路并按照该路的走向生成岔路(也就是最初做法)
                 * 将生成一条岔路以及岔路的覆盖物抽取成一个方法(岔路坐标,旋转,Plane生成,添加到场景,覆盖物的生成,添加到场景...);
                 */
                //                switch (naviIcon) {
                //                    case 2://tl
                //                        next.setAll(center.x + 5, center.y + 3, center.z);
                //                        fin.setAll(center.x + 10, center.y + 4, center.z);
                //                        break;
                //                    case 6://tlb
                //                        next.setAll(center.x + 5, center.y + 6, center.z);
                //                        fin.setAll(center.x + 10, center.y + 8, center.z);
                //                        break;
                //                    case 4://tlf
                //                        next.setAll(center.x, center.y + 5, center.z);
                //                        fin.setAll(center.x, center.y + 10, center.z);
                //                        break;
                //                    case 8://tb
                //                        next.setAll(center.x + 5, center.y + 4, center.z);
                //                        fin.setAll(center.x + 10, center.y + 6, center.z);
                //                        break;
                //                    case 3://tr
                //                        next.setAll(center.x - 5, center.y + 3, center.z);
                //                        fin.setAll(center.x - 10, center.y + 4, center.z);
                //                        break;
                //                    case 7://trb
                //                        next.setAll(center.x, center.y + 5, center.z);
                //                        fin.setAll(center.x, center.y + 10, center.z);
                //                        break;
                //                    case 5://trf
                //                        next.setAll(center.x, center.y + 5, center.z);
                //                        fin.setAll(center.x, center.y + 10, center.z);
                //                        break;
                //                    case 9://tf
                //                        next.setAll(center.x + 4, center.y + 4, center.z);
                //                        fin.setAll(center.x + 10, center.y + 6, center.z);
                //                        break;
                //                    default://else
                //                        index = (i + 12) < mPath.size() ? (i + 12) : mPath.size() - 1;
                //                        next.setAll(mPath.get(index));
                //                        index = (i + 25) < mPath.size() ? (i + 25) : mPath.size() - 1;
                //                        fin.setAll(mPath.get(index));
                //                        rot = MathUtils.getDegrees(center.x, center.y, next.x, next.y) - 180;
                //                        rot2 = MathUtils.getDegrees(next.x, next.y, fin.x, fin.y) - 180;
                //                        break;
                //                }
                //                if (rot2 > rot) {
                //                    rotation = -25;
                //                } else if (rot2 < rot) {
                //                    rotation = 25;
                //                } else {
                //                    rotation = -getCurrentCamera().getRotZ();
                //                }

                // TODO: 2016/8/3 翻转从主路copy出来的一段路,作为岔路显示,效果可能会更加自然多样,因此上述代码是不起作用的
                index = (i + 10) < mPath.size() ? (i + 10) : mPath.size() - 1;
                next.setAll(mPath.get(index));
                index = (i + 20) < mPath.size() ? (i + 20) : mPath.size() - 1;
                fin.setAll(mPath.get(index));
                rot = MathUtils.getDegrees(center.x, center.y, next.x, next.y) - 180;
                rot2 = MathUtils.getDegrees(next.x, next.y, fin.x, fin.y) - 180;

                switch (naviIcon) {
                    case 2://tl
                    case 6://tlb
                    case 4://tlf
                    case 8://tb
                        rotation = -35;
                        break;
                    case 3://tr
                    case 7://trb
                    case 5://trf
                        rotation = 35;
                        break;
                    case 9://tf
                        rotation = 25;
                        break;
                    default://else
                        if (rot2 > rot) {
                            rotation = -35;
                        } else {
                            rotation = 35;
                        }
                        break;
                }

                switch (naviIcon) {
                    case 2://tl
                    case 3://tr
                        center.setAll(next);
                        next.setAll(fin);
                        fin.x = next.x + (next.x - center.x) * (10 / MathUtils.calculateDistance(next.x, next.y, center.x, center.y));
                        fin.y = next.y + (next.y - center.y) * (10 / MathUtils.calculateDistance(next.x, next.y, center.x, center.y));
                        rotation /= 2;
                        break;
                }

                PointF projection = new PointF();
                MathUtils.getProjectivePoint(new PointF((float) center.x, (float) center.y),
                                             new PointF((float) next.x, (float) next.y),
                                             new PointF((float) fin.x, (float) fin.y), projection);
                fin.x = projection.x + (projection.x - fin.x);
                fin.y = projection.y + (projection.y - fin.y);

                //通过岔路坐标得到左右两侧的坐标,得到两个交点,将这两个交点绘制一个plane,贴图黑色,宽度设置为0.1即可
                List<Vector3> path = new ArrayList<>();
                CatmullRomCurve3D catmullRomCurve3D = new CatmullRomCurve3D();
                catmullRomCurve3D.addPoint(new Vector3(center));
                catmullRomCurve3D.addPoint(new Vector3(center));
                catmullRomCurve3D.addPoint(new Vector3(next));
                catmullRomCurve3D.addPoint(new Vector3(fin));
                catmullRomCurve3D.addPoint(new Vector3(fin));
                for (int j = 0; j < catmullRomCurve3D.getNumPoints() * 5; j++) {
                    Vector3 v = new Vector3();
                    catmullRomCurve3D.calculatePoint(v, (1.0 * j) / (catmullRomCurve3D.getNumPoints() * 5));
                    path.add(v);
                }
                rotateBranchWithDivDegreesTest(path, rotation + 180);
                MathUtils.points2path(leftPath, rightPath, path, ROAD_WIDTH/* + 0.05*/);

                int startIndex = i - 5 < 0 ? 0 : i - 5;
                int endIndex = index;
                for (int j = startIndex; j < endIndex; j++) {
                    if (rotation >= 0) {
                        Vector3 v1 = mLeftPath.get(j);
                        Vector3 v2 = mLeftPath.get(j + 1);
                        boolean isIntersection = false;
                        for (int k = 0; k < leftPath.size() - 1; k++) {
                            Vector3 l1 = leftPath.get(k);
                            Vector3 l2 = leftPath.get(k + 1);
                            if (MathUtils.getIntersection(v1, v2, l1, l2, intersect) == 1) {
                                for (int m = startIndex; m < endIndex; m++) {
                                    v1 = mLeftPath.get(m);
                                    v2 = mLeftPath.get(m + 1);
                                    for (int n = 0; n < rightPath.size() - 1; n++) {
                                        l1 = rightPath.get(n);
                                        l2 = rightPath.get(n + 1);
                                        if (MathUtils.getIntersection(v1, v2, l1, l2, intersect2) == 1) {
                                            intersectPath.add(intersect);
                                            if (j != m) {
                                                startIndex = j < m ? j + 1 : m + 1;
                                                endIndex = j < m ? m : j;
                                                for (int s = startIndex; s <= endIndex; s++) {
                                                    intersectPath.add(new Vector3(mLeftPath.get(s)));
                                                }
                                            }
                                            intersectPath.add(intersect2);
                                            //将组成覆盖主路和岔路之间粘合这部分点向主路内部方向移动0.02距离,否则会覆盖掉岔路的一部分
                                            //目的只是想要覆盖掉主路的路边贴图而已:偏移量由intersect2向l1移动0.02计算x,y的移动量
                                            double div = 0.095 / MathUtils.calculateDistance(intersect2.x, intersect2.y, l1.x, l1.y);
                                            double offsetX = (l1.x - intersect2.x) * (div);
                                            double offsetY = (l1.y - intersect2.y) * (div);
                                            for (int s = 0; s < intersectPath.size(); s++) {
                                                Vector3 v = intersectPath.get(s);
                                                v.x += offsetX;
                                                v.y += offsetY;
                                            }
                                            //将两个交点向覆盖线的内部移动一点
                                            Vector3 p1 = intersectPath.get(0);
                                            Vector3 p2 = intersectPath.get(1);
                                            div = 0.2 / MathUtils.calculateDistance(p1.x, p1.y, p2.x, p2.y);
                                            p1.x = p1.x + (p2.x - p1.x) * div;
                                            p1.y = p1.y + (p2.y - p1.y) * div;
                                            if (div >= 1) {
                                                intersectPath.remove(1);
                                            }
                                            p1 = intersectPath.get(intersectPath.size() - 1);
                                            p2 = intersectPath.get(intersectPath.size() - 2);
                                            div = 0.2 / MathUtils.calculateDistance(p1.x, p1.y, p2.x, p2.y);
                                            p1.x = p1.x + (p2.x - p1.x) * div;
                                            p1.y = p1.y + (p2.y - p1.y) * div;
                                            if (div >= 1) {
                                                intersectPath.remove(intersectPath.size() - 2);
                                            }
                                            isIntersection = true;
                                            break;
                                        }
                                    }
                                    if (isIntersection) {
                                        break;
                                    }
                                }
                                if (isIntersection) {
                                    break;
                                }
                            }
                        }
                        if (isIntersection) {
                            break;
                        }
                    } else {
                        Vector3 v1 = mRightPath.get(j);
                        Vector3 v2 = mRightPath.get(j + 1);
                        boolean isIntersection = false;
                        for (int k = 0; k < leftPath.size() - 1; k++) {
                            Vector3 l1 = leftPath.get(k);
                            Vector3 l2 = leftPath.get(k + 1);
                            if (MathUtils.getIntersection(v1, v2, l1, l2, intersect) == 1) {
                                for (int m = startIndex; m < endIndex; m++) {
                                    v1 = mRightPath.get(m);
                                    v2 = mRightPath.get(m + 1);
                                    isIntersection = false;
                                    for (int n = 0; n < rightPath.size() - 1; n++) {
                                        l1 = rightPath.get(n);
                                        l2 = rightPath.get(n + 1);
                                        if (MathUtils.getIntersection(v1, v2, l1, l2, intersect2) == 1) {
                                            intersectPath.add(intersect);
                                            if (j != m) {
                                                startIndex = j < m ? j + 1 : m + 1;
                                                endIndex = j < m ? m : j;
                                                for (int s = startIndex; s <= endIndex; s++) {
                                                    intersectPath.add(new Vector3(mRightPath.get(s)));
                                                }
                                            }
                                            intersectPath.add(intersect2);
                                            double div = 0.095 / MathUtils.calculateDistance(intersect2.x, intersect2.y, l1.x, l1.y);
                                            double offsetX = (l1.x - intersect2.x) * (div);
                                            double offsetY = (l1.y - intersect2.y) * (div);
                                            for (int s = 0; s < intersectPath.size(); s++) {
                                                Vector3 v = intersectPath.get(s);
                                                v.x += offsetX;
                                                v.y += offsetY;
                                            }
                                            //将两个交点向覆盖线的内部移动一点
                                            Vector3 p1 = intersectPath.get(0);
                                            Vector3 p2 = intersectPath.get(1);
                                            div = 0.2 / MathUtils.calculateDistance(p1.x, p1.y, p2.x, p2.y);
                                            p1.x = p1.x + (p2.x - p1.x) * div;
                                            p1.y = p1.y + (p2.y - p1.y) * div;
                                            if (div >= 1) {
                                                intersectPath.remove(1);
                                            }
                                            p1 = intersectPath.get(intersectPath.size() - 1);
                                            p2 = intersectPath.get(intersectPath.size() - 2);
                                            div = 0.2 / MathUtils.calculateDistance(p1.x, p1.y, p2.x, p2.y);
                                            p1.x = p1.x + (p2.x - p1.x) * div;
                                            p1.y = p1.y + (p2.y - p1.y) * div;
                                            if (div >= 1) {
                                                intersectPath.remove(intersectPath.size() - 2);
                                            }
                                            isIntersection = true;
                                            break;
                                        }
                                    }
                                    if (isIntersection) {
                                        break;
                                    }
                                }
                                if (isIntersection) {
                                    break;
                                }
                            }
                        }
                        if (isIntersection) {
                            break;
                        }
                    }
                }
                /*for (int j = startIndex; j < endIndex; j++) {
                    Vector3 v1 = mLeftPath.get(j);
                    Vector3 v2 = mLeftPath.get(j + 1);
                    boolean isIntersection = false;
                    for (int k = 0; k < rightPath.size() - 1; k++) {
                        Vector3 l1 = rightPath.get(k);
                        Vector3 l2 = rightPath.get(k + 1);
                        if (MathUtils.getIntersection(v1, v2, l1, l2, intersect2) == 1) {

                            isIntersection = true;
                            break;
                        }
                    }
                    if (isIntersection) {
                        break;
                    }

                    v1 = mRightPath.get(j);
                    v2 = mRightPath.get(j + 1);
                    isIntersection = false;
                    for (int k = 0; k < rightPath.size() - 1; k++) {
                        Vector3 l1 = rightPath.get(k);
                        Vector3 l2 = rightPath.get(k + 1);
                        if (MathUtils.getIntersection(v1, v2, l1, l2, intersect2) == 1) {

                            isIntersection = true;
                            break;
                        }
                    }
                    if (isIntersection) {
                        break;
                    }
                }*/
                return rotation;
            } else {
                dist300 -= dist;
            }
        }
        return 0;
    }
    //=====================================handle cross image end=========================================//

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
        mOriginalPath.clear();
    }

    /**
     * 到达目的地时调用
     */
    public void arriveDestination() {
        //        getCurrentScene().destroyScene();
        //        removeScene(mCurScene);
        //        getCurrentScene().initScene();
        //        getCurrentScene().clearChildren();
        //        getCurrentScene().performFrameTasks();
        //        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "children number is " + getCurrentScene().getNumChildren());
        //        getCurrentScene().clearAnimations();
        //        if (mMainRoadObjects != null && mMainRoadObjects.size() > 0) {
        //            for (ARWayRoadObject arWayRoadObject : mMainRoadObjects) {
        //                arWayRoadObject.removeMaterial();
        //                arWayRoadObject.destroy();
        //            }
        //        }
        //        if (mBranchRoadObjects != null && mBranchRoadObjects.size() > 0) {
        //            for (ARWayRoadObject arWayRoadObject : mBranchRoadObjects) {
        //                arWayRoadObject.removeMaterial();
        //                arWayRoadObject.destroy();
        //            }
        //        }
        //        if (mCoverRoadPlanes != null && mCoverRoadPlanes.size() > 0) {
        //            for (Plane plane : mCoverRoadPlanes) {
        //                plane.destroy();
        //            }
        //        }
        //        if (mObject4Chase != null) {
        //            mObject4Chase.destroy();
        //        }
        //        MaterialManager.getInstance().reset();
        //        setSceneCachingEnabled(false);
        //        getCurrentScene().clearChildren();
        //        getCurrentScene().performFrameTasks();
        clearAllData();
        mOriginalPath.clear();
        System.gc();
    }

    /**
     * 外部通过该方法设置路径到OpenGL部分初始化道路
     *
     * @param naviPath
     * @return 1 设置路径成功 -1 projection或naviPath为null -2 路线重复 0 未知错误
     */

    public int setPath(Projection projection, AMapNaviPath naviPath, boolean repeat) {
        if (naviPath == null) {
            return -1;
        }
        if(ARWayConst.IS_AMAP_VIEW){
            if(projection == null) return -1;
//            mProjection = projection;
        }

        List<Vector3> path = new ArrayList<>();
        mStepsLength.clear();

        for (AMapNaviStep step : naviPath.getSteps()) {
            mStepsLength.add(step.getLength());
            if (step != null) {
                /*List<Point> stepScreenPoints = new ArrayList<>();
                List<PointF> stepOpenglPoints = new ArrayList<>();*/
                for (NaviLatLng coord : step.getCoords()) {
                    LatLng latLng = new LatLng(coord.getLatitude(), coord.getLongitude());

                    PointF pf = mProjection.toOpenGLLocation(latLng);
                    Vector3 v = new Vector3(pf.x, -pf.y, 0);
                    path.add(v);
                }
                /*if (stepScreenPoints.size() > 0) {
                    naviStepsScreen.add(stepScreenPoints);
                    naviStepsOpengl.add(stepOpenglPoints);
                }*/
            }

        }

        if (!repeat || !isPathRepeat(path)) {
            mOriginalPath.clear();
            mOriginalPath.addAll(path);
            return setPathAndCalcData(mOriginalPath, naviPath.getAllLength()/*, naviStepsScreen, naviStepsOpengl*/);
        } else {
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "arway setPath is repeat path");
            return -2;
        }
    }

    /**
     * 判断当前传入的path是否与上一次的path相同,如果相同则不进行处理
     *
     * @param path
     * @return
     */
    private boolean isPathRepeat(List<Vector3> path) {
        if (path == null || path.size() <= 0 || mOriginalPath == null || mOriginalPath.size() <= 0 || path.size() != mOriginalPath.size()) {
            return false;
        }
        for (int i = 0; i < path.size(); i++) {
            Vector3 vNew = path.get(i);
            Vector3 vOld = mOriginalPath.get(i);
            if (!vNew.equals(vOld)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 设置导航的路线以及路线总长度(单位为米)
     *
     * @param path
     * @param allLength
     * @return 0 失败 1 成功
     */
    private int setPathAndCalcData(List<Vector3> path, double allLength/*, List<List<Point>> naviStepsScreen, List<List<PointF>> naviStepsOpengl*/) {
        HaloLogger.logE(ARWayConst.SPECIAL_LOG_TAG, "setPathAndCalcData start");
        clearAllData();
        //此处由于mPath中的点是path中点的子集,去除了相同的点,因此二者长度不一致,之后不要在使用path
        for (int i = 0; i < path.size(); i++) {
            Vector3 v = path.get(i);
            if (!containPoint(mPath, v)) {
                mPath.add(new Vector3(v));
            }
        }
        path.clear();
        if (mPath.size() < 2) {
            mPath.clear();
            return 0;
        }

        //clear the points because that points is too close
        /*if (ARWayConst.IS_FILTER_PATH_LITTLE_DISTANCE) {
            HaloLogger.logE("helong_debug", "pre size:" + mPath.size());
            int tempBigTime4Calc = 30000000;
            //保留path中头尾两个点不被删除
            for (int i = 0; i < mPath.size() - 2; i++) {
                int distance = (int) (MathUtils.calculateDistance(
                        mPath.get(i).x, mPath.get(i).y, mPath.get(i + 1).x, mPath.get(i + 1).y) * tempBigTime4Calc);
                //if the distance 30 time is less than 30,remove second point.
                if (distance <= 300) {
                    mPath.remove(i + 1);
                    i--;
                }
            }
            HaloLogger.logE("helong_debug", "next size:" + mPath.size());
        }*/

        //mPath为原始数据,需要移动,放大,旋转
        //to bigger
        for (int i = 0; i < mPath.size(); i++) {
            mPath.get(i).x *= BIGGER_TIME;
            mPath.get(i).y *= BIGGER_TIME;
        }

        //move to screen center
        mOffsetX = mPath.get(0).x - 0;
        mOffsetY = mPath.get(0).y - 0;
        for (int i = 0; i < mPath.size(); i++) {
            mPath.get(i).x -= mOffsetX;
            mPath.get(i).y -= mOffsetY;
        }

        /*if (ARWayConst.IS_CAT_MULL_ROM) {
            int pathLength = mPath.size();
            CatmullRomCurve3D catmull = new CatmullRomCurve3D();
            //controll point 1
            catmull.addPoint(new Vector3(mPath.get(0)));
            for (int i = 0; i < mPath.size(); i++) {
                catmull.addPoint(new Vector3(mPath.get(i)));
            }
            //controll point 2
            catmull.addPoint(new Vector3(mPath.get(mPath.size() - 1)));
            catmull.reparametrizeForUniformDistribution(50);
            mPath.clear();
            for (int i = 0; i <= pathLength * CURVE_TIME; i++) {
                Vector3 pos = new Vector3();
                catmull.calculatePoint(pos, (1.0 * i) / (1.0 * pathLength * CURVE_TIME));
                mPath.add(new Vector3(pos));
            }
            HaloLogger.logE("helong_debug", "插值后 path_size:" + mPath.size());
        }*/

        /*//rotate path with matrix
        double rotateZ = MathUtils.getDegrees(mPath.get(0).x, mPath.get(0).y, mPath.get(CURVE_TIME - 1).x, mPath.get(CURVE_TIME - 1).y);
        mObject4ChaseStartOrientation = rotateZ;
        Matrix matrix = new Matrix();
        matrix.setRotate((float) rotateZ - 180, (float) mPath.get(0).x, (float) mPath.get(0).y);
        for (int i = 1; i < mPath.size(); i++) {
            Vector3 v = mPath.get(i);
            float[] xy = new float[2];
            matrix.mapPoints(xy, new float[]{(float) v.x, (float) v.y});
            v.x = xy[0];
            v.y = xy[1];
        }*/

        //calc and save the car need to rotate degrees
        Vector3 p1 = mPath.get(0);
        Vector3 p2 = mPath.get(1);
        for (int i = 1; i < mPath.size(); i++) {
            if (!p1.equals(mPath.get(i))) {
                p2 = mPath.get(i);
                break;
            }
        }
        double rotateZ = (Math.toDegrees(MathUtils.getRadian(p1.x, p1.y, p2.x, p2.y)) + 270) % 360;
        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, String.format("path orientation,origin first point is %s,second point is %s", p1, p2));
        mObject4ChaseStartOrientation = rotateZ;

        //calc totalDist in opengl
        mTotalLength = allLength;
        mRetainTotalLength = allLength;
        mTotalDistance = 0f;
        for (int i = 0; i < mPath.size() - 1; i++) {
            Vector3 v1 = mPath.get(i);
            Vector3 v2 = mPath.get(i + 1);
            double distance = MathUtils.calculateDistance(v1.x, v1.y, v2.x, v2.y);
            mTotalDistance += distance;
            mDist2FinalPoint.add(distance);
            //在最后添加一个0到末点,表示最后一个点到finalPoint距离为0
            if (i == mPath.size() - 2) {
                mDist2FinalPoint.add(0.0);
            }
        }

        //mLength2Distance表示一米在openGL当前场景中有多大
        mLength2DistanceScale = (mTotalDistance/* - distFake2First*/) / mTotalLength;

        //save every point to final point`s dist and length
        double lastDistance = 0.0;
        for (int i = 0; i < mDist2FinalPoint.size(); i++) {
            if (i == 0) {
                lastDistance = mDist2FinalPoint.get(i);
                mDist2FinalPoint.set(i, mTotalDistance);
            } else {
                double temp = mDist2FinalPoint.get(i);
                mDist2FinalPoint.set(i, mDist2FinalPoint.get(i - 1) - lastDistance);
                lastDistance = temp;
            }
            mLength2FinalPoint.add(mDist2FinalPoint.get(i) / mLength2DistanceScale);
        }

        //手动对path进行250米的划分,记录这些划分点的index和length
        double tempLength = mTotalLength;
        mLoadStepStartIndexs.add(0);
        mLoadStepLengths.add(tempLength);
        for (int i = 1; i < mLength2FinalPoint.size(); i++) {
            if (tempLength - mLength2FinalPoint.get(i) >= LOAD_PATH_LENGTH) {
                mLoadStepStartIndexs.add(i);
                mLoadStepLengths.add(mLength2FinalPoint.get(i));
                tempLength = mLength2FinalPoint.get(i);
            }
        }
        if (!mLoadStepStartIndexs.contains(mLength2FinalPoint.size() - 1)) {
            mLoadStepStartIndexs.add(mLength2FinalPoint.size() - 1);
            mLoadStepLengths.add(0.0);
        }

        //Through will calc left and right path in insertARWayObject,but they calc with many littlePath not mPath.
        //PS:ROAD_WIDTH 代表的是逻辑路宽,由于路宽被用于与lookAt点计算相交,为了确保lookAt的点在道路中央,将逻辑路宽设置的比现实路宽要小很多即可
        //同时,逻辑路宽小能够确保摄像机到lookAt的连线与道路走向吻合
        MathUtils.points2path(mLeftPath, mRightPath, mPath, LOGIC_ROAD_WIDTH);

        //将path切割成N个小path分别加载到场景中
        //splitPath2LittlePathesWithIndexAndLength(mPath, mChildPathPositions, mChildPathes, mStartAddPlaneIndex, ADD_PLANE_LENGTH);

        HaloLogger.logE("branch_line", "path start=========");
        for (Vector3 v : mPath) {
            HaloLogger.logE("branch_line", v.x + "," + v.y);
        }
        HaloLogger.logE("branch_line", "path end===========");

        //同步这些操作到CrossPathManager中
        //mCrossPathManager.parseNaviPathInfo(naviStepsScreen, naviStepsOpengl, BIGGER_TIME, rotateZ);

        //set flag with true indicates(表示) data is inited
        mCanMyInitScene = true;
        //如果Rajawali自身的初始化场景完毕,那么就可以进行我们自己的场景初始化(目前就是绘制道路,并进行贴图移动)
        if (mIsInitScene) {
            myInitScene();
        }

        //clear not userful data to free memory
        clearUnuseDataAfterInitPathData();
        return 1;
    }

    /**
     * //split path to many little path with 30(for now)
     *
     * @param allPath
     * @param childPathPositions
     * @param childPathes
     * @param startAddPlaneIndex
     */
    /*private void splitPath2LittlePathesWithIndexAndLength(List<Vector3> allPath, List<Vector3> childPathPositions, List<List<Vector3>> childPathes, int startAddPlaneIndex, double addPlaneLength) {
        List<Vector3> path = new ArrayList<>();
        childPathes.clear();
        childPathPositions.clear();
        double addPathLength = 0;
        for (int i = startAddPlaneIndex; i < allPath.size() && addPathLength < addPlaneLength * mLength2DistanceScale; i++) {
            path.add(new Vector3(allPath.get(i)));
            if (i != 0) {
                Vector3 v1 = allPath.get(i - 1);
                Vector3 v2 = allPath.get(i);
                addPathLength += MathUtils.calculateDistance(v1.x, v1.y, v2.x, v2.y);
            }
            mStartAddPlaneIndex = i;
        }

        Vector3 preChildEndPosOfChildPath = null;
        for (int i = 0; i < (path.size() % CHILD_PATH_SIZE == 0 ? path.size() / CHILD_PATH_SIZE : path.size() / CHILD_PATH_SIZE + 1); i++) {
            Vector3 beginPosOfChildPath = null;
            List<Vector3> childPath = new ArrayList<>();
            for (int j = 0; j < CHILD_PATH_SIZE && (j + (i * CHILD_PATH_SIZE)) < path.size(); j++) {
                Vector3 v = new Vector3(path.get(j + (i * CHILD_PATH_SIZE)));
                if (j == 0) {
                    if (i == 0) {
                        beginPosOfChildPath = new Vector3(v);
                    } else {
                        beginPosOfChildPath = preChildEndPosOfChildPath;
                    }
                    childPath.add(new Vector3(0, 0, 0));
                } else if (j == CHILD_PATH_SIZE - 1) {
                    preChildEndPosOfChildPath = new Vector3(v.x, v.y, v.z);
                    childPath.add(new Vector3(v.x - beginPosOfChildPath.x, v.y - beginPosOfChildPath.y, v.z - beginPosOfChildPath.z));

                    mPreChildEndPos.add(preChildEndPosOfChildPath);
                } else {
                    childPath.add(new Vector3(v.x - beginPosOfChildPath.x, v.y - beginPosOfChildPath.y, v.z - beginPosOfChildPath.z));
                }
            }
            childPathPositions.add(beginPosOfChildPath);
            childPathes.add(childPath);
        }
    }*/

    /**
     * clear data about draw arway!
     */
    private void clearAllData() {
        mPath.clear();
        mLeftPath.clear();
        mRightPath.clear();
        mLength2FinalPoint.clear();
        mDist2FinalPoint.clear();
        mCurIndexes.clear();
        mTranslateAnims.clear();
        mRotateAnims.clear();
        mChildPathPositions.clear();
        mChildPathes.clear();
        mLastThroughPosition.clear();
        /*mMainRoadObjects.clear();
        mBranchRoadObjects.clear();
        mCoverRoadPlanes.clear();*/
        mTranslateAnimIndex = 0;
        mRotateAnimIndex = 0;
        mTotalDistance = 0f;
        mTotalLength = 0;
        mCurIndexInPath = 0;
        mLength2DistanceScale = 0f;
        mStartTime = 0l;
        mStartLength = 0.0;
        mStartAddPlaneIndex = 0;
        mRetainTotalLength = 0;
        mIsMyInitScene = false;
        mCanMyInitScene = false;
        mObject4Chase = null;
        /*mPreLocation = null;
        mCurLocation = null;
        mLastTime = 0;*/
        mCurIndexAfter = 0;
        mOffsetX = 0;
        mOffsetY = 0;
        mIsRotate = false;
        mFrameRotate = 0;
        mFromPos = null;
        mToPos = null;
        mTransAnim = null;
        mRotateAnim = null;
        mPreTime = 0;
        mFromDegrees = 0;
        mToDegrees = 0;
        /*mPreBearing = 0;
        mRotateAnimScale = 0;
        mRotateAnimDegrees = 0;*/
        mLoadStepStartIndexs.clear();
        mLoadStepLengths.clear();
        mLoadStepIndex = 0;
        System.gc();
    }

    /**
     * clear not use data after we init path data!
     */
    private void clearUnuseDataAfterInitPathData() {
        System.gc();
    }

    /**
     * clear not use data after add something to scene!
     */
    private void clearUnuseDataAfterMyInitScene() {
        System.gc();
    }

    /**
     * clear not use data after add plane to scene.
     */
    private void clearUnuseDataAfterAddPlane2Scene() {
        mChildPathPositions.clear();
        mChildPathes.clear();
        mPreChildEndPos.clear();
        System.gc();
    }

    /**
     * clear not use data after remove plane from scene.
     */
    private void clearUnuseDataAfterRemovePlaneFromScene() {
        //mMainRoadObjects.clear();
        System.gc();
    }

    /**
     * 判断集合中是否包含某个元素
     *
     * @param path
     * @param v
     * @return true表示包含, false表示不包含
     */
    private boolean containPoint(List<Vector3> path, Vector3 v) {
        for (int i = path.size() - 1; i >= 0; i--) {
            if (path.get(i).equals(v)) {
                return true;
            }
        }
        return false;
    }

    private void myInitScene() {

        getCurrentScene().clearChildren();
        mSceneUpdater.initScene();

        if (mObject4Chase != null) {
            mObject4Chase.destroy();
        }
        mObject4Chase = new Plane(0.4f, 0.4f, 1, 1);
        mObject4Chase.isDepthTestEnabled();
        mObject4Chase.setPosition(mPath.get(0).x, mPath.get(0).y, 0);
        Material material = new Material();
        material.setColorInfluence(0);
        try {
            material.addTexture(new Texture("obj_4_chase", R.drawable.car_and_compass));
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        mObject4Chase.setMaterial(material);
        mObject4Chase.setRotation(Vector3.Axis.Z, -mObject4ChaseStartOrientation);

        Camera cCamera = getCurrentCamera();
        cCamera.enableLookAt();
        cCamera.setUpAxis(Vector3.Axis.Z);
        cCamera.setRotation(0, 0, 0);
        cCamera.setPosition(mPath.get(0).x, mPath.get(0).y, CAMERA_OFFSET_Z);
        updateCamera(mObject4Chase);

        getCurrentCamera().setNearPlane(CAMERA_NEAR_PLANE);
        getCurrentCamera().setFarPlane(CAMERA_FAR_PLANE);

        ARWayCameraCaculator.cameraCaculatorInit(cCamera);

        mCameraModel.setNearPlaneWithDrawPlane_Angel(mCameraPerspectiveAngel);
        mCameraModel.setRoadWidthProportion(mRoadWidthProportion);
        mCameraModel.setRoadWidth(ROAD_WIDTH);
        mCameraModel.setBottomDistanceProportion(0.0f);

        updatePlane2Scene(mLoadStepIndex);

        //被追随物体必须在道路添加到场景后添加到场景中,否则会被道路盖住
        if (ARWayConst.IS_DEBUG_SCENE) {
            getCurrentScene().addChild(mObject4Chase);
        }
        //update flag
        mIsMyInitScene = true;
        mCanMyInitScene = false;

        clearUnuseDataAfterMyInitScene();
    }

    private void insertTestPlane(Vector3 v2) {
        Plane plane = new Plane(0.2f, 0.2f, 1, 1);
        plane.setPosition(v2.x, v2.y, 0.000001);
        Material material = new Material();
        plane.setMaterial(material);
        plane.setColor(Color.GRAY);
        getCurrentScene().addChild(plane);
    }

    /***
     * 向场景中添加道路
     * 1.如果是刚开始导航,那么是加载两段(每段约为250m)
     * 2.如果是导航过程中,那么则是加载三段(当前,以及前后两段)
     */
    private void updatePlane2Scene(int loadStepIndex) {
        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, String.format("renderVisiblePath start"));

        int startIndex = loadStepIndex == 0 ? mLoadStepStartIndexs.get(loadStepIndex) : mLoadStepStartIndexs.get(loadStepIndex - 1);

        int endIndex = loadStepIndex + 2 >= mLoadStepStartIndexs.size() ? mPath.size() - 1 : mLoadStepStartIndexs.get(loadStepIndex + 2);
        HaloLogger.logE("testtest", "startIndex:" + startIndex + ",endIndex:" + endIndex);
//        mSceneUpdater.renderVisiblePath(mPath.subList(startIndex, endIndex));
        mSceneUpdater.renderVisiblePath(mPath);

        clearUnuseDataAfterAddPlane2Scene();
    }

    /**
     * 从场景中移除道路(每次移除小于300m的最大距离)
     */
    /*private void removePlaneFromScene() {
        int size = mMainRoadObjects.size() / 3;
        List<ARWayRoadObject> toRemovedList = mMainRoadObjects.subList(0, size);
        for (int i = 0; i < size; i++) {
            getCurrentScene().removeChild(toRemovedList.get(i));
            toRemovedList.get(i).removeMaterial();
        }
        mMainRoadObjects.remove(toRemovedList);
        toRemovedList.clear();
        clearUnuseDataAfterRemovePlaneFromScene();
    }*/
    private ARWayRoadObject insertARWayObject(List<Vector3> littlePath, Vector3 position, double leftWidth, double rightWidth, int type) {

        if (littlePath == null || littlePath.size() <= 0 || position == null/* || mLeftPath == null || mLeftPath.size() <= 0 || mRightPath == null || mRightPath.size() <= 0*/) {
            return null;
        }

        /*if (mMainRoadTexture == null) {
            mMainRoadTexture = new Texture("route_main", R.drawable.route_new_line);
        }
        if (mBranchRoadTexture == null) {
            mBranchRoadTexture = new Texture("route_branch", R.drawable.route_new_branch);
        }
        if (mBranchBlackTexture == null) {
            mBranchBlackTexture = new Texture("route_branch_black", R.drawable.route_new_red);
        }
        if (mMainRoadTexture == null) {
            mMainRoadTexture = new Texture("route_main", R.drawable.route_new_line);
        }*/
        String roadType = ARWayRoadObject.ARWAY_ROAD_TYPE_MAIN;
        /*if (type == 2)
            roadType = ARWayRoadObject.ARWAY_ROAD_TYPE_BRANCH;
        else if (type == 3)
            roadType = ARWayRoadObject.ARWAY_ROAD_TYPE_BRANCH_BLACK;*/
        //        ARWayRoadObject arWayRoadObject = new ARWayRoadObject(new ArrayList<>(littlePath), leftWidth, rightWidth, roadType);
        ARWayRoadObject arWayRoadObject = new ARWayRoadObject(new ArrayList<>(littlePath), leftWidth, rightWidth, roadType);
        /*Material material = arWayRoadObject.getMaterial();
        material.setColor(0);
        try {
            if (type == 2) {
                material.addTexture(mBranchRoadTexture);
            } else if (type == 1) {
                material.addTexture(mMainRoadTexture);
            } else if (type == 3) {
                material.addTexture(mBranchBlackTexture);
            }
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }*/
        arWayRoadObject.setDoubleSided(true);
        arWayRoadObject.setPosition(position);
        getCurrentScene().addChild(arWayRoadObject);
        return arWayRoadObject;
    }

    private ARWayRoadObject insertARWayObject(List<Vector3> littlePath, Vector3 position, float roadWidth, int color) {

        if (littlePath == null || littlePath.size() <= 0 || position == null/* || mLeftPath == null || mLeftPath.size() <= 0 || mRightPath == null || mRightPath.size() <= 0*/) {
            return null;
        }
        ARWayRoadObject mainRoadObject = new ARWayRoadObject(new ArrayList<>(littlePath), roadWidth, color);
        mainRoadObject.setPosition(position);
        getCurrentScene().addChild(mainRoadObject);
        return mainRoadObject;
    }

    private ARWayRoadObject insertARWayObject(List<Vector3> littlePath, Vector3 position, double width, int type) {
        return insertARWayObject(littlePath, position, width, width, type);
    }

    /*private LatLng mPreLocation = null;
    private LatLng mCurLocation = null;
    private long   mLastTime    = 0;

    public void onLocationChange(AMapNaviLocation location) {
        HaloLogger.logE("cross_handle", "onLocationChange start==========================");
        if (mProjection != null) {
            Point p = mProjection.toScreenLocation(DrawUtils.naviLatLng2LatLng(location.getCoord()));
            HaloLogger.logE("cross_handle", "location_change:" + p.x + "," + p.y);
        }
        HaloLogger.logE("cross_handle", "onLocationChange speed:" + location.getSpeed());
        HaloLogger.logE("cross_handle", "onLocationChange time:" + System.currentTimeMillis());
        *//*if (mLastTime != 0 && System.currentTimeMillis() - mLastTime < 2000) {
            HaloLogger.logE("cross_handle","onLocationChange end==========================");
            return;
        }*//*
        mLastTime = System.currentTimeMillis();
        LatLng latLng = DrawUtils.naviLatLng2LatLng(location.getCoord());
        if (mPreLocation == null) {
            HaloLogger.logE("cross_handle", "navi start");
            HaloLogger.logE("cross_handle", "onLocationChange end==========================");
            return;
        }
        double retainDist = 0;
        if (mCurLocation == null) {
            mCurLocation = latLng;
        } else {
            if (!latLng.equals(mCurLocation)) {
                mPreLocation = mCurLocation;
                mCurLocation = latLng;
            } else {
                return;
            }
        }
        retainDist = mStartLength - AMapUtils.calculateLineDistance(mCurLocation, mPreLocation);
        HaloLogger.logE("cross_handle", "retainDist_LocationChange:" + retainDist);
        HaloLogger.logE("cross_handle", "onLocationChange end==========================");
        setRetainDistance(retainDist);
    }

    public void onLocationChange(NaviInfo info) {
        HaloLogger.logE("cross_handle", "onNaviInfo start==========================");
        if (mProjection != null) {
            Point p = mProjection.toScreenLocation(DrawUtils.naviLatLng2LatLng(info.getCoord()));
            HaloLogger.logE("cross_handle", "navi_info:" + p.x + "," + p.y);
        }
        HaloLogger.logE("cross_handle", "onNaviInfo time:" + System.currentTimeMillis());
        *//*if (mLastTime != 0 && System.currentTimeMillis() - mLastTime < 2000) {
            HaloLogger.logE("cross_handle","onNaviInfo end==========================");
            return;
        }*//*
        mLastTime = System.currentTimeMillis();
        LatLng latLng = DrawUtils.naviLatLng2LatLng(info.getCoord());
        if (mPreLocation == null) {
            HaloLogger.logE("cross_handle", "navi start");
            HaloLogger.logE("cross_handle", "onNaviInfo end==========================");
            mPreLocation = latLng;
            return;
        } else if (mCurLocation == null) {
            mCurLocation = latLng;
        } else {
            if (!latLng.equals(mCurLocation)) {
                mPreLocation = mCurLocation;
                mCurLocation = latLng;
            } else {
                return;
            }
        }
        HaloLogger.logE("cross_handle", "retainDist_NaviInfo:" + info.getPathRetainDistance());
        HaloLogger.logE("cross_handle", "onNaviInfo end==========================");
        setRetainDistance(info.getPathRetainDistance());
    }*/

    public void setPathRetainLength4DynamicLoad(int pathRetainLength) {
        for (int i = mLoadStepIndex; i < mLoadStepLengths.size() - 1; i++) {
            if (pathRetainLength <= mLoadStepLengths.get(i) && pathRetainLength >= mLoadStepLengths.get(i + 1)) {
                updatePlane2Scene(i);
                HaloLogger.logE("testtests", "index:" + i);
                mLoadStepIndex = i + 1;
                break;
            }
        }
    }

    private Vector3               mFromPos     = null;
    private Vector3               mToPos       = null;
    private double                mFromDegrees = 0;
    private double                mToDegrees   = 0;
    private long                  mPreTime     = 0;
    /*private double                mPreBearing        = 0;
    private double                mRotateAnimScale   = 0;
    private double                mRotateAnimDegrees = 0;*/
    private TranslateAnimation3D  mTransAnim   = null;
    private RotateOnAxisAnimation mRotateAnim  = null;

    /**
     * 根据返回的GPS位置处理得到可用数据,以摄像机当前位置和属性为start(如果是第一次则是以前一个GPS位置)
     * 此时的数据为end
     *
     * @param location
     * @return 1 动画成功 0 场景尚未初始化完成 -1 mStartLatLng == null -2 此时为第一个位置点,无法开始动画
     */
    public int updateLocation(AMapNaviLocation location) {
        if (!mIsMyInitScene) {
            return 0;
        }
        HaloLogger.logE("testtesttest", "bearing:" + MathUtils.convertAMapBearing2OpenglBearing(location.getBearing()));
        HaloLogger.logE("testtesttest", "rotZ:" + Math.toDegrees(mObject4Chase.getRotZ()));
        clearLastAnim();
        if (mFromPos == null) {
            ARWayProjection.PointD fromPos = ARWayProjection.glMapPointFormCoordinate(DrawUtils.naviLatLng2LatLng(location.getCoord()));
            mFromPos = new Vector3(fromPos.x * BIGGER_TIME - mOffsetX, (-fromPos.y) * BIGGER_TIME - mOffsetY, OBJ_4_CHASE_Z);
            mFromDegrees = MathUtils.convertAMapBearing2OpenglBearing(location.getBearing());
            mPreTime = location.getTime();
            return -2;
        } else {
            long duration = location.getTime() - mPreTime;
            mPreTime = location.getTime();

            if (mToPos != null) {
                mFromPos = mObject4Chase.getPosition();
                mFromDegrees = Math.toDegrees(mObject4Chase.getRotZ());
                mFromDegrees = mFromDegrees < 0 ? mFromDegrees + 360 : mFromDegrees;
            }

            ARWayProjection.PointD toPos = ARWayProjection.glMapPointFormCoordinate(DrawUtils.naviLatLng2LatLng(location.getCoord()));
            mToPos = new Vector3(toPos.x * BIGGER_TIME - mOffsetX, (-toPos.y) * BIGGER_TIME - mOffsetY, OBJ_4_CHASE_Z);
            mToDegrees = MathUtils.convertAMapBearing2OpenglBearing(location.getBearing());
            /*HaloLogger.logE("branch_line", "anim start");
            HaloLogger.logE("branch_line", mFromPos.x + "," + mFromPos.y);
            HaloLogger.logE("branch_line", mToPos.x + "," + mToPos.y);
            HaloLogger.logE("branch_line", "anim end");*/
            startAnim(mFromPos, mToPos, mToDegrees - mFromDegrees, duration + ANIM_DURATION_REDUNDAN);
            return 1;
        }
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

    private void setRetainDistance(double endLength) {
        if (endLength > mLength2FinalPoint.get(0)) {
            return;
        }

        //first time callback
        if (mStartTime == 0) {
            mStartTime = System.currentTimeMillis();
            mStartLength = endLength;
            return;
        }

        //防止回退
        long endTime = System.currentTimeMillis();
        if (mStartLength <= endLength/* || endTime - mStartTime < 4000*/) {
            return;
        }

        //到此表示返回的数据满足动画要求
        Object3D chaseObject = mObject4Chase;
        long duration = endTime - mStartTime;
        Vector3 cameraVector3 = chaseObject.getPosition();
        Vector3 startPosition = new Vector3(cameraVector3.x, cameraVector3.y, 0);
        List<Vector3> throughPosition = new ArrayList<>();
        Vector3 endPosition = new Vector3();

        //上一次的动画未完成
        if (mTranslateAnims.size() > (mTranslateAnimIndex + 1)) {
            if (mLastThroughPosition != null && mLastThroughPosition.size() > mTranslateAnimIndex) {
                int cnt = mLastThroughPosition.size();
                for (int i = mTranslateAnimIndex + 1; i < cnt; i++) {
                    throughPosition.add(new Vector3(mLastThroughPosition.get(i)));
                }
            }
        }
        mLastThroughPosition.clear();

        clearLastAnims();

        setEndAndThroughPosition(mPath, mLength2FinalPoint, mStartLength, endLength, endPosition, throughPosition);
        mLastThroughPosition.add(new Vector3(startPosition));
        mLastThroughPosition.addAll(throughPosition);
        mLastThroughPosition.add(new Vector3(endPosition));

        updateAnimsAndStart(startPosition, throughPosition, endPosition, duration);

        mRealCurPosition.setAll(endPosition);

        //reset data
        mStartTime = endTime;
        mStartLength = endLength;
        mEndLength = endLength;
    }

    private void clearLastAnims() {
        if (mTranslateAnims != null && mTranslateAnims.size() > 0) {
            for (int i = 0; i < mTranslateAnims.size(); i++) {
                if (mTranslateAnims.get(i).isPlaying()) {
                    mTranslateAnims.get(i).pause();
                    if (ARWayConst.ENABLE_TEST_LOG && ARWayConst.ENABLE_FAST_LOG) {
                        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "*************mTranslateAnims translate anim stop !!!!!!!!!!!******************");
                    }
                }
                //TODO 此处不能直接取消注册,否则有可能会引发多线程问题,导致Animation处报角标越界
                //mTranslateAnims.get(i).unregisterListener(this);
                //不加这句在程序运行时间过长时会导致内存泄漏,场景本身持有了大量的动画对象
                getCurrentScene().unregisterAnimation(mTranslateAnims.get(i));
            }
            mTranslateAnims.clear();
        }
        if (mRotateAnims != null && mRotateAnims.size() > 0) {
            for (int i = 0; i < mRotateAnims.size(); i++) {
                if (mRotateAnims.get(i).isPlaying()) {
                    mRotateAnims.get(i).pause();
                    if (ARWayConst.ENABLE_TEST_LOG && ARWayConst.ENABLE_FAST_LOG) {
                        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "*************mRotateAnims rotate anim stop !!!!!!!!!!!******************");
                    }
                }
                //mRotateAnims.get(i).unregisterListener(this);
                //不加这句在程序运行时间过长时会导致内存泄漏,场景本身持有了大量的动画对象
                getCurrentScene().unregisterAnimation(mRotateAnims.get(i));
            }
            mRotateAnims.clear();
        }
    }

    private void updateAnimsAndStart(Vector3 startPosition, List<Vector3> throughPosition, Vector3 endPosition, long duration) {
        /**
         * 使用自己维护的动画集合来代替组合动画
         */
        mTranslateAnims.clear();
        mRotateAnims.clear();
        mTranslateAnimIndex = 0;
        mRotateAnimIndex = 0;
        if (throughPosition == null || throughPosition.size() <= 0) {
            LinearBezierCurve3D curve3D = new LinearBezierCurve3D(
                    new Vector3(startPosition.x, startPosition.y, OBJ_4_CHASE_Z),
                    new Vector3(endPosition.x, endPosition.y, OBJ_4_CHASE_Z));
            mTranslateAnims.add(createTranslateAnim(curve3D, duration, mObject4Chase));
            testLog(startPosition, endPosition);
            double degrees = MathUtils.getRotateDegreesWithLineAndAngle(startPosition.x, startPosition.y, endPosition.x, endPosition.y, mObject4Chase.getRotZ());
            mRotateAnims.add(createRotateAnim(Vector3.Axis.Z, degrees, duration, mObject4Chase));
        } else {
            if (throughPosition.size() == 1) {
                List<Long> childDurations = new ArrayList<>();
                List<Double> childLength = new ArrayList<>();
                double totalLength = 0;
                double length = 0;
                length = MathUtils.calculateDistance(startPosition.x, startPosition.y, throughPosition.get(0).x, throughPosition.get(0).y);
                childLength.add(length);
                totalLength += length;
                length = MathUtils.calculateDistance(throughPosition.get(0).x, throughPosition.get(0).y, endPosition.x, endPosition.y);
                totalLength += length;
                childLength.add(length);

                childDurations.add((long) (childLength.get(0) / totalLength * duration));
                childDurations.add((long) (childLength.get(1) / totalLength * duration));

                LinearBezierCurve3D curve3D = null;
                curve3D = new LinearBezierCurve3D(
                        new Vector3(startPosition.x, startPosition.y, OBJ_4_CHASE_Z),
                        new Vector3(throughPosition.get(0).x, throughPosition.get(0).y, OBJ_4_CHASE_Z));
                testLog(startPosition, throughPosition.get(0));
                mTranslateAnims.add(createTranslateAnim(curve3D, childDurations.get(0), mObject4Chase));
                curve3D = new LinearBezierCurve3D(
                        new Vector3(throughPosition.get(0).x, throughPosition.get(0).y, OBJ_4_CHASE_Z),
                        new Vector3(endPosition.x, endPosition.y, OBJ_4_CHASE_Z));
                testLog(throughPosition.get(0), endPosition);
                mTranslateAnims.add(createTranslateAnim(curve3D, childDurations.get(1), mObject4Chase));

                double degrees = MathUtils.getRotateDegreesWithLineAndAngle(startPosition.x, startPosition.y, throughPosition.get(0).x, throughPosition.get(0).y, mObject4Chase.getRotZ());
                mRotateAnims.add(createRotateAnim(Vector3.Axis.Z, degrees, childDurations.get(0), mObject4Chase));
                degrees = MathUtils.getRotateDegreesWithTwoLines(startPosition.x, startPosition.y, throughPosition.get(0).x, throughPosition.get(0).y, endPosition.x, endPosition.y);
                mRotateAnims.add(createRotateAnim(Vector3.Axis.Z, degrees, childDurations.get(1), mObject4Chase));
            } else {
                List<Long> childDurations = new ArrayList<>();
                List<Double> childLength = new ArrayList<>();
                double totalLength = 0;
                for (int i = 0; i < throughPosition.size(); i++) {
                    double length = 0;
                    if (i == 0) {
                        length = MathUtils.calculateDistance(startPosition.x, startPosition.y, throughPosition.get(0).x, throughPosition.get(0).y);
                        childLength.add(length);
                        totalLength += length;
                        length = MathUtils.calculateDistance(throughPosition.get(0).x, throughPosition.get(0).y, throughPosition.get(1).x, throughPosition.get(1).y);
                        totalLength += length;
                        childLength.add(length);
                    } else if (i == throughPosition.size() - 1) {
                        length = MathUtils.calculateDistance(throughPosition.get(i).x, throughPosition.get(i).y, endPosition.x, endPosition.y);
                        childLength.add(length);
                        totalLength += length;
                    } else {
                        length = MathUtils.calculateDistance(throughPosition.get(i).x, throughPosition.get(i).y, throughPosition.get(i + 1).x, throughPosition.get(i + 1).y);
                        childLength.add(length);
                        totalLength += length;
                    }
                }
                double tempDuration = 0;
                for (int i = 0; i < childLength.size(); i++) {
                    if (i != childLength.size() - 1) {
                        double dur = childLength.get(i) / totalLength * duration;
                        tempDuration += dur;
                        childDurations.add((long) dur);
                    } else {
                        childDurations.add((long) (duration - tempDuration));
                    }
                }
                for (int i = 0; i < throughPosition.size(); i++) {
                    LinearBezierCurve3D curve3D = null;
                    if (i == 0) {
                        curve3D = new LinearBezierCurve3D(
                                new Vector3(startPosition.x, startPosition.y, OBJ_4_CHASE_Z),
                                new Vector3(throughPosition.get(i).x, throughPosition.get(i).y, OBJ_4_CHASE_Z));
                        testLog(startPosition, throughPosition.get(i));
                        mTranslateAnims.add(createTranslateAnim(curve3D, childDurations.get(0), mObject4Chase));
                        double degrees = MathUtils.getRotateDegreesWithLineAndAngle(startPosition.x, startPosition.y, throughPosition.get(0).x, throughPosition.get(0).y, mObject4Chase.getRotZ());
                        mRotateAnims.add(createRotateAnim(Vector3.Axis.Z, degrees, childDurations.get(0), mObject4Chase));

                    } else if (i == throughPosition.size() - 1) {
                        curve3D = new LinearBezierCurve3D(
                                new Vector3(throughPosition.get(i - 1).x, throughPosition.get(i - 1).y, OBJ_4_CHASE_Z),
                                new Vector3(throughPosition.get(i).x, throughPosition.get(i).y, OBJ_4_CHASE_Z));
                        testLog(throughPosition.get(i - 1), throughPosition.get(i));
                        mTranslateAnims.add(createTranslateAnim(curve3D, childDurations.get(childDurations.size() - 2), mObject4Chase));
                        curve3D = new LinearBezierCurve3D(
                                new Vector3(throughPosition.get(i).x, throughPosition.get(i).y, OBJ_4_CHASE_Z),
                                new Vector3(endPosition.x, endPosition.y, OBJ_4_CHASE_Z));
                        testLog(throughPosition.get(i), endPosition);
                        mTranslateAnims.add(createTranslateAnim(curve3D, childDurations.get(childDurations.size() - 1), mObject4Chase));
                        double degrees = MathUtils.getRotateDegreesWithTwoLines(throughPosition.get(i - 2).x, throughPosition.get(i - 2).y, throughPosition.get(i - 1).x, throughPosition.get(i - 1).y, throughPosition.get(i).x, throughPosition.get(i).y);
                        mRotateAnims.add(createRotateAnim(Vector3.Axis.Z, degrees, childDurations.get(childDurations.size() - 2), mObject4Chase));
                        degrees = MathUtils.getRotateDegreesWithTwoLines(throughPosition.get(i - 1).x, throughPosition.get(i - 1).y, throughPosition.get(i).x, throughPosition.get(i).y, endPosition.x, endPosition.y);
                        mRotateAnims.add(createRotateAnim(Vector3.Axis.Z, degrees, childDurations.get(childDurations.size() - 1), mObject4Chase));
                    } else {
                        curve3D = new LinearBezierCurve3D(
                                new Vector3(throughPosition.get(i - 1).x, throughPosition.get(i - 1).y, OBJ_4_CHASE_Z),
                                new Vector3(throughPosition.get(i).x, throughPosition.get(i).y, OBJ_4_CHASE_Z));
                        testLog(throughPosition.get(i - 1), throughPosition.get(i));
                        mTranslateAnims.add(createTranslateAnim(curve3D, childDurations.get(i), mObject4Chase));
                        if (i == 1) {
                            double degrees = MathUtils.getRotateDegreesWithTwoLines(startPosition.x, startPosition.y, throughPosition.get(i - 1).x, throughPosition.get(i - 1).y, throughPosition.get(i).x, throughPosition.get(i).y);
                            mRotateAnims.add(createRotateAnim(Vector3.Axis.Z, degrees, childDurations.get(i), mObject4Chase));
                        } else {
                            double degrees = MathUtils.getRotateDegreesWithTwoLines(throughPosition.get(i - 2).x, throughPosition.get(i - 2).y, throughPosition.get(i - 1).x, throughPosition.get(i - 1).y, throughPosition.get(i).x, throughPosition.get(i).y);
                            mRotateAnims.add(createRotateAnim(Vector3.Axis.Z, degrees, childDurations.get(i), mObject4Chase));
                        }
                    }
                }
            }
        }

        if (mTranslateAnims.size() > 0) {
            mTranslateAnims.get(mTranslateAnimIndex++).play();
        }
        //        mRotateAnims.clear();
        if (mRotateAnims.size() > 0) {
            mRotateAnims.get(mRotateAnimIndex++).play();
        }
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

    private SplineTranslateAnimation3D createTranslateAnim(LinearBezierCurve3D curve, long duration, ATransformable3D transformable3D) {
        SplineTranslateAnimation3D anim = new SplineTranslateAnimation3D(curve);
        anim.setDurationMilliseconds(duration);
        anim.setTransformable3D(transformable3D);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatMode(Animation.RepeatMode.NONE);
        anim.registerListener(this);
        //cameraAnim.setOrientToPath(true);
        getCurrentScene().registerAnimation(anim);
        return anim;
    }

    // TODO: 2016/7/1
    private void testLog(Vector3 v1, Vector3 v2) {
        /*StringBuilder sb = new StringBuilder();

        sb.append("{");
        sb.append("v1(");
        sb.append("x:" + v1.x + ",");
        sb.append("y:" + v1.y + ",");
        sb.append("z:" + v1.z);
        sb.append(")");
        sb.append("v2(");
        sb.append("x:" + v2.x + ",");
        sb.append("y:" + v2.y + ",");
        sb.append("z:" + v2.z);
        sb.append(")");
        sb.append("}");

        HaloLogger.logE("helong_debug", "compoundAnim:" + sb.toString());*/
    }

    public void clearScene() {
        getCurrentScene().clearChildren();
    }

    @Override
    protected void render(long ellapsedRealtime, double deltaTime) {
        super.render(ellapsedRealtime, deltaTime);
    }

    /**
     * 通过path等各种数据获取结束点以及途经点的位置
     *
     * @param path
     * @param length2FinalPoint
     * @param startLength
     * @param endLength
     * @param endPosition
     * @param throughPosition
     */
    private void setEndAndThroughPosition(final List<Vector3> path, final List<Double> length2FinalPoint,
                                          final double startLength, final double endLength,
                                          Vector3 endPosition, List<Vector3> throughPosition) {
        mCurIndexes.clear();
        //set through position and end position
        for (int i = 0; i < length2FinalPoint.size(); i++) {
            if (length2FinalPoint.get(i) < startLength) {
                if (i != 0) {
                    mCurIndexInPath = i - 1;
                } else {
                    mCurIndexInPath = 0;
                }
                mCurIndexes.add(mCurIndexInPath);
                //through position
                for (int j = i; j < length2FinalPoint.size(); j++) {
                    Vector3 v = path.get(j);
                    throughPosition.add(new Vector3(v.x, v.y, v.z));
                    double length = length2FinalPoint.get(j);
                    mCurIndexes.add(j);

                    //end position
                    if (length2FinalPoint.get(j + 1) < endLength) {
                        Vector3 v1 = path.get(j);
                        Vector3 v2 = path.get(j + 1);
                        double l1 = length2FinalPoint.get(j);
                        double l2 = length2FinalPoint.get(j + 1);
                        double scale = (endLength - l1) / (l2 - l1);
                        endPosition.x = v1.x + (v2.x - v1.x) * scale;
                        endPosition.y = v1.y + (v2.y - v1.y) * scale;
                        endPosition.z = v1.z + (v2.z - v1.z) * scale;
                        break;
                    }
                }
                break;
            }
        }
    }

    //=====================================amap data end=========================================//

    //====================================rajawali animation callback start========================================//
    @Override
    public void onAnimationEnd(Animation animation) {
        if (mTranslateAnims != null && mTranslateAnims.size() > 0 && mTranslateAnimIndex < mTranslateAnims.size() && mTranslateAnimIndex >= 1) {
            if (animation == mTranslateAnims.get(mTranslateAnimIndex - 1)) {
                //                animation.unregisterListener(this);
                if (mCurIndexes.size() > mTranslateAnimIndex) {
                    mCurIndexInPath = mCurIndexes.get(mTranslateAnimIndex);
                }
                mTranslateAnims.get(mTranslateAnimIndex++).play();
                return;
            }
        }
        if (mRotateAnims != null && mRotateAnims.size() > 0 && mRotateAnimIndex < mRotateAnims.size() && mRotateAnimIndex >= 1) {
            if (animation == mRotateAnims.get(mRotateAnimIndex - 1)) {
                //                animation.unregisterListener(this);
                mRotateAnims.get(mRotateAnimIndex++).play();
                return;
            }
            /*// TODO: 2016/8/7 TEST 新的旋转动画方式测试
            if (mRotateAnimIndex < mTranslateAnimIndex) {
                mRotateAnims.get(mRotateAnimIndex++).play();
            }
            return;*/
        }
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
    public void setEvent(int type){
        if(type==1){//左下
            mCameraPerspectiveAngel += 5;
            if(mCameraPerspectiveAngel>90){
                mCameraPerspectiveAngel=50;
            }
        }if(type==2){//右上
            mRoadWidthProportion += 0.1;
            if(mRoadWidthProportion>1){
                mRoadWidthProportion=1f;
            }
        }else if(type==3){//下
            mRoadWidthProportion -= 0.1;
            if(mRoadWidthProportion<0){
                mRoadWidthProportion=0.1f;
            }
        }
        if(mRoadWidthProportion>=0 && mRoadWidthProportion<=1){
            mCameraModel.setRoadWidthProportion(mRoadWidthProportion);
        }
        if(mCameraPerspectiveAngel>0 && mCameraPerspectiveAngel<90){
            mCameraModel.setNearPlaneWithDrawPlane_Angel(mCameraPerspectiveAngel);
        }
    }
}
