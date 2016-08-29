package com.haloai.hud.hudendpoint.arwaylib.arway.impl_gl;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.SurfaceTexture;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;

import com.amap.api.maps.Projection;
import com.amap.api.navi.model.AMapNaviPath;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.hudendpoint.arwaylib.utils.DrawUtils;
import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;
import com.haloai.hud.hudendpoint.arwaylib.view.ARWayRoadObject;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.ATransformable3D;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.IAnimationListener;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.animation.SplineTranslateAnimation3D;
import org.rajawali3d.cameras.Camera;
import org.rajawali3d.curves.CatmullRomCurve3D;
import org.rajawali3d.curves.CompoundCurve3D;
import org.rajawali3d.curves.LinearBezierCurve3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.util.GLU;

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
    private static final double ANIMATION_LENGTH   = 30;
    private static final double OBJ_4_CHASE_Z      = 0;
    private static final double BIGGER_TIME        = 1000000.0;
    private static final double CAMERA_MIN_LENGTH  = 20;
    private static final int    FRAME_RATE         = 10;
    private static final int    CURVE_TIME         = 30;
    private static final double LOGIC_ROAD_WIDTH   = 0.4;
    private static final double ROAD_WIDTH         = 0.4;
    private static final double CAMERA_OFFSET_X    = 0;
    private static final double CAMERA_OFFSET_Y    = 0;
    private static final double CAMERA_OFFSET_Z    = 0.6;
    private static final double CAMERA_CUT_OFFSET  = 0.7;
    private static final double LOOK_AT_DIST       = 1.3;
    private static final int    INTERSECTION_COUNT = 30;
    private static final double CAMERA_NEAR_PLANE  = 0.5;
    private static final double CAMERA_FAR_PLANE   = 25;
    private static final int    CHILD_PATH_SIZE    = 20;
    private static final long   PRETENSION_TIME    = 1000;
    private static       int    SCREEN_WIDTH       = 0;
    private static       int    SCREEN_HEIGHT      = 0;
    private static final double BRANCH_LINE_Z      = -0.01;
    private static final double ADD_PLANE_LENGTH   = 600;

    //list data
    private List<Vector3>         mPath                = new ArrayList<>();
    private List<Vector3>         mOriginalPath        = new ArrayList<>();
    private List<Vector3>         mLeftPath            = new ArrayList<>();
    private List<Vector3>         mRightPath           = new ArrayList<>();
    private List<Double>          mOffsetX             = new ArrayList<>();
    private List<Double>          mOffsetY             = new ArrayList<>();
    private List<Double>          mDist2FinalPoint     = new ArrayList<>();
    private List<Double>          mLength2FinalPoint   = new ArrayList<>();
    private List<Integer>         mCurIndexes          = new ArrayList<>();
    private List<Vector3>         mPreChildEndPos      = new ArrayList<>();
    private List<Vector3>         mChildPathPositions  = new ArrayList<>();
    private List<List<Vector3>>   mChildPathes         = new ArrayList<>();
    private List<Vector3>         mLastThroughPosition = new ArrayList<>();
    private List<ARWayRoadObject> mMainRoadObjects     = new ArrayList<>();
    private List<ARWayRoadObject> mBranchRoadObjects   = new ArrayList<>();
    private List<Plane>           mCoverRoadPlanes     = new ArrayList<>();

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
    private SplineTranslateAnimation3D            mCameraAnim         = null;
    private ArrayList<SplineTranslateAnimation3D> mTranslateAnims     = new ArrayList<>();
    private int                                   mTranslateAnimIndex = 0;
    private ArrayList<RotateOnAxisAnimation>      mRotateAnims        = new ArrayList<>();
    private int                                   mRotateAnimIndex    = 0;

    //about screen coord to opengl coord
    private int[]    mViewport         = null;
    private double[] mNearPos4         = new double[4];
    private double[] mFarPos4          = new double[4];
    private Vector3  mNearPos          = new Vector3();
    private Vector3  mFarPos           = new Vector3();
    private Matrix4  mViewMatrix       = null;
    private Matrix4  mProjectionMatrix = null;

    //state
    //ps:mIsInitScene代表Rajawali自己初始化场景是否完成
    //ps:mIsMyInitScene代表我们得到数据后去add元素到场景中是否完成
    //ps:mCanInitScene代表我们是否能够去add元素到场景中(满足一下条件即可:1.数据到来setPath被调用 2.本次数据未被加载成元素添加到场景中)
    private boolean mIsInitScene   = false;
    private boolean mIsMyInitScene = false;
    private boolean mCanInitScene  = false;

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

        mViewport = new int[]{0, 0, getViewportWidth(), getViewportHeight()};
        mViewMatrix = getCurrentCamera().getViewMatrix();
        mProjectionMatrix = getCurrentCamera().getProjectionMatrix();

        //getCurrentScene().setBackgroundColor(Color.DKGRAY);
        mIsInitScene = true;

        if (!mIsMyInitScene && mCanInitScene) {
            myInitScene();
        }
    }

    @Override
    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        super.onRenderSurfaceSizeChanged(gl, width, height);
        mViewport[2] = getViewportWidth();
        mViewport[3] = getViewportHeight();
        mViewMatrix = getCurrentCamera().getViewMatrix();
        mProjectionMatrix = getCurrentCamera().getProjectionMatrix();
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
        findCameraLookatAndPosition(location, transObject.getRotZ(), LOOK_AT_DIST, position, lookat);
        camera.setPosition(position);
        camera.setLookAt(lookat);
    }

    private void findCameraLookatAndPosition(Vector3 cPos, double yaw, double dist, Vector3 position, Vector3 lookat) {
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

    @Override
    protected void onRender(long ellapsedRealtime, double deltaTime) {
        if (!mIsMyInitScene) {
            return;
        }

        if (mObject4Chase != null) {
            updateCamera(mObject4Chase);
        }

        //        Quaternion qt = new Quaternion();
        //        getCurrentCamera().getCameraOrientation(qt);
        //        HaloLogger.logE("helong_fix_", "camera_quaternion:" + qt);
        //        double rotX = 180 / Math.PI * qt.getRoll();
        //        double rotZ = 180 / Math.PI * qt.getPitch();
        //        double rotY = 180 / Math.PI * qt.getYaw();
        //        double camRotationX = rotX;
        //        HaloLogger.logE("helong_fix_", "camera_orientation_rotZ:" + rotZ);
        //        HaloLogger.logE("helong_fix_", "camera_orientation_rotX:" + rotX);
        //        HaloLogger.logE("helong_fix_", "camera_orientation_rotY:" + rotY);
        //
        //        Vector3 pos = mObject4Chase.getPosition();
        //        //        mObject4Chase.setPosition(pos.x+mTestPosX,pos.y+mTestPosY,pos.z);
        //        qt = new Quaternion();
        //        mObject4Chase.getOrientation(qt);
        //        rotX = 180 / Math.PI * qt.getRoll();
        //        rotZ = 180 / Math.PI * qt.getPitch();
        //        rotY = 180 / Math.PI * qt.getYaw();
        //        double rotationX = rotX;

        //        HaloLogger.logE("helong_fix_", "object_orientation_rotZ:" + rotZ);
        //        HaloLogger.logE("helong_fix_", "object_orientation_rotX:" + rotX);
        //        HaloLogger.logE("helong_fix_", "object_orientation_rotY:" + rotY);
        //        HaloLogger.logE("helong_fix_____", "====================================");
        //        HaloLogger.logE("helong_fix_____", "object_rotationX:" + rotationX);
        //        HaloLogger.logE("helong_fix_____", "camera_rotationX:" + camRotationX);

        //        getCurrentCamera().setRotX(rotX);

        //        getCurrentCamera().setRotZ(rotZ);
        //        getCurrentCamera().setRotX(rotX);
        //        getCurrentCamera().setRotY(rotY);
        //
        //
        ////        getCurrentCamera().getCameraOrientation(qt);
        //        getCurrentCamera().getOrientation(qt);
        //        rotX = 180/Math.PI*qt.getRoll();
        //        rotZ = 180/Math.PI*qt.getPitch();
        //        rotY = 180/Math.PI*qt.getYaw();
        //        //        HaloLogger.logE("helong_fix_", System.currentTimeMillis()
        //        //                + "," + pos.x + "," + pos.y);
        //        HaloLogger.logE("helong_fix_", "camera_orientation_rotZ:" + rotZ);
        //        HaloLogger.logE("helong_fix_", "camera_orientation_rotX:" + rotX);
        //        HaloLogger.logE("helong_fix_", "camera_orientation_rotY:" + rotY);
        //
        //        /**
        //         a=-130/180*pi;
        //         src.x=0;
        //         src.y=0;
        //         length = 30;
        //         offX = sin(a)*length
        //         offY = cos(a)*length
        //         dst.x = src.x+offX;
        //         dst.y = src.y+offY;
        //         */


        /*Vector3 lookAt = new Vector3();
        Vector3 nextPoint = mPath.get(mCurIndexInPath + 1);
        double dist = MathUtils.calculateDistance(pos.x, pos.y, nextPoint.x, nextPoint.y);
        if (dist >= LOOK_AT_DIST) {
            lookAt.x = pos.x + (nextPoint.x - pos.x) * (LOOK_AT_DIST / dist);
            lookAt.y = pos.y + (nextPoint.y - pos.y) * (LOOK_AT_DIST / dist);
        } else {
            double comp = LOOK_AT_DIST - dist;
            for (int i = mCurIndexInPath + 2; i < mPath.size(); i++) {
                Vector3 pre = mPath.get(i - 1);
                Vector3 next = mPath.get(i);
                if (i == mPath.size() - 1) {
                    lookAt.x = next.x;
                    lookAt.y = next.y;
                    break;
                } else {
                    dist = MathUtils.calculateDistance(pre.x, pre.y, next.x, next.y);
                    if (dist >= comp) {
                        lookAt.x = pre.x + (next.x - pre.x) * (comp / dist);
                        lookAt.y = pre.y + (next.y - pre.y) * (comp / dist);
                        break;
                    } else {
                        comp -= dist;
                    }
                }
            }
        }*/

        //        lookAt.x = pos.x + mTestPosX + Math.sin(rotX / 180 * Math.PI) * LOOK_AT_DIST;
        //        lookAt.y = pos.y + mTestPosY + Math.cos(rotX / 180 * Math.PI) * LOOK_AT_DIST;
        //        lookAt.z = /*OBJ_4_CHASE_Z/4*3*/0;
        //
        //        //TODO 计算lookAt是否超出了道路,如果超出了,那么就截取到与道路的交点即可
        //        //TODO 起始判断点为mCurrentIndexInPath,结束点为该值+n个形状点(暂定)
        //        //TODO: 2016/7/19 这部分代码应该还是有问题的,有时候会看到lookAt的点在道路外的情况发生
        //        if (mCurIndexInPath < mLeftPath.size() - 1) {
        //            for (int i = mCurIndexInPath; i < mLeftPath.size() - 1 && i < mRightPath.size() - 1 && i < mCurIndexInPath + CURVE_TIME * INTERSECTION_COUNT; i++) {
        //                Vector3 start = mLeftPath.get(i);
        //                Vector3 end = mLeftPath.get(i + 1);
        //                PointF result = new PointF();
        //                int isIntersection = MathUtils.getIntersection(
        //                        new PointF((float) start.x, (float) start.y), new PointF((float) end.x, (float) end.y),
        //                        new PointF((float) pos.x, (float) pos.y), new PointF((float) lookAt.x, (float) lookAt.y), result);
        //                if (isIntersection == 1) {
        //                    lookAt.x = result.x;
        //                    lookAt.y = result.y;
        //                    break;
        //                }
        //                start = mRightPath.get(i);
        //                end = mRightPath.get(i + 1);
        //                isIntersection = MathUtils.getIntersection(
        //                        new PointF((float) start.x, (float) start.y), new PointF((float) end.x, (float) end.y),
        //                        new PointF((float) pos.x, (float) pos.y), new PointF((float) lookAt.x, (float) lookAt.y), result);
        //                if (isIntersection == 1) {
        //                    lookAt.x = result.x;
        //                    lookAt.y = result.y;
        //                    break;
        //                }
        //            }
        //                    /*for (int i = mCurIndexInPath; i < mLeftPath.size() - 1 && i < mRightPath.size() - 1 && i < mCurIndexInPath + CURVE_TIME * INTERSECTION_COUNT; i++) {
        //                        Vector3 start = mLeftPath.get(i);
        //                        Vector3 end = mLeftPath.get(i + 1);
        //                        PointF result = new PointF();
        //                        int isIntersection = MathUtils.getIntersection(
        //                                new PointF((float) start.x, (float) start.y), new PointF((float) end.x, (float) end.y),
        //                                new PointF((float) pos.x, (float) pos.y), new PointF((float) lookAt.x, (float) lookAt.y), result);
        //                        if (isIntersection == 1) {
        //                            double realDist = MathUtils.calculateDistance(pos.x, pos.y, result.x, result.y);
        //                            double compDist = LOOK_AT_DIST - realDist;
        //                            if (compDist > 0) {
        //                                for (int j = i + 1; j < mLeftPath.size(); j++) {
        //                                    double dist = 0;
        //                                    double x1 = 0;
        //                                    double y1 = 0;
        //                                    double x2 = 0;
        //                                    double y2 = 0;
        //                                    if(j==i+1) {
        //                                        x1 = result.x;
        //                                        y1 = result.y;
        //                                    }else{
        //                                        x1 = mLeftPath.get(j-1).x;
        //                                        y1 = mLeftPath.get(j-1).y;
        //                                    }
        //                                    x2 = mLeftPath.get(j).x;
        //                                    y2 = mLeftPath.get(j).y;
        //                                    dist = MathUtils.calculateDistance(x1,y1,x2,y2);
        //                                    if (dist >= compDist) {
        //                                        lookAt.x = x1 + (x2-x1)*(compDist/dist);
        //                                        lookAt.y = y1 + (y2-y1)*(compDist/dist);
        //                                        break;
        //                                    } else {
        //                                        compDist-=dist;
        //                                    }
        //                                }
        //                            }else{
        //                                lookAt.x = result.x;
        //                                lookAt.y = result.y;
        //                            }
        //                            break;
        //                        }
        //                        start = mRightPath.get(i);
        //                        end = mRightPath.get(i + 1);
        //                        isIntersection = MathUtils.getIntersection(
        //                                new PointF((float) start.x, (float) start.y), new PointF((float) end.x, (float) end.y),
        //                                new PointF((float) pos.x, (float) pos.y), new PointF((float) lookAt.x, (float) lookAt.y), result);
        //                        if (isIntersection == 1) {
        //                            double realDist = MathUtils.calculateDistance(pos.x, pos.y, result.x, result.y);
        //                            double compDist = LOOK_AT_DIST - realDist;
        //                            if (compDist > 0) {
        //                                for (int j = i + 1; j < mRightPath.size(); j++) {
        //                                    double dist = 0;
        //                                    double x1 = 0;
        //                                    double y1 = 0;
        //                                    double x2 = 0;
        //                                    double y2 = 0;
        //                                    if(j==i+1) {
        //                                        x1 = result.x;
        //                                        y1 = result.y;
        //                                    }else{
        //                                        x1 = mRightPath.get(j-1).x;
        //                                        y1 = mRightPath.get(j-1).y;
        //                                    }
        //                                    x2 = mRightPath.get(j).x;
        //                                    y2 = mRightPath.get(j).y;
        //                                    dist = MathUtils.calculateDistance(x1,y1,x2,y2);
        //                                    if (dist >= compDist) {
        //                                        lookAt.x = x1 + (x2-x1)*(compDist/dist);
        //                                        lookAt.y = y1 + (y2-y1)*(compDist/dist);
        //                                        break;
        //                                    } else {
        //                                        compDist-=dist;
        //                                    }
        //                                }
        //                            }else{
        //                                lookAt.x = result.x;
        //                                lookAt.y = result.y;
        //                            }
        //                            break;
        //                        }
        //                    }*/
        //        }

        //        HaloLogger.logE("helong_fix____", "lookAt:" + lookAt);
        //        getCurrentCamera().setLookAt(lookAt.x + mTestLookAtX, lookAt.y + mTestLookAtY, lookAt.z + mTestLookAtZ);
        ////        getCurrentCamera().setRotX(getCurrentCamera().getRotZ()+rotationX);
        //
        //        //        rotX = 180/Math.PI*qt.getRoll();
        //        //        rotZ = 180/Math.PI*qt.getPitch();
        //        //        rotY = 180/Math.PI*qt.getYaw();
        //        //        HaloLogger.logE("helong_fix_", "pre_orientation_rotZ:" + rotZ);
        //        //        HaloLogger.logE("helong_fix_", "pre_orientation_rotX:" + rotX);
        //        //        HaloLogger.logE("helong_fix_", "pre_orientation_rotY:" + rotY);
        //
        //        //将设置好的lookAt通过计算填充到orientation上
        //        getCurrentCamera().calculateModelMatrix(null);

        //        getCurrentCamera().getOrientation(qt);
        //
        //        rotX = 180 / Math.PI * qt.getRoll();
        //        rotZ = 180 / Math.PI * qt.getPitch();
        //        rotY = 180 / Math.PI * qt.getYaw();
        //                HaloLogger.logE("helong_fix_", "look_orientation_rotZ:" + rotZ);
        //                HaloLogger.logE("helong_fix_", "look_orientation_rotX:" + rotX);
        //                HaloLogger.logE("helong_fix_", "look_orientation_rotY:" + rotY);
        //
        //                HaloLogger.logE("helong_fix_", "next_orientation_rotZ:" + rotZ);
        //                HaloLogger.logE("helong_fix_", "next_orientation_rotX:" + (rotX+rotationX));
        //                HaloLogger.logE("helong_fix_", "next_orientation_rotY:" + rotY);

        /*Vector3 lookAtRotation = new Vector3(rotX, rotY, rotZ);
        handleLookAt(lookAtRotation);
        getCurrentCamera().setRotZ(lookAtRotation.z + mTestRotZ);
        getCurrentCamera().setRotY(lookAtRotation.y + mTestRotY);
        getCurrentCamera().setRotX(lookAtRotation.x + mTestRotX);*/

        //        HaloLogger.logE("helong_fix_____", "orientation_rotX:" + (lookAtRotation.x + mTestRotX));
        //        HaloLogger.logE("helong_fix_____", "orientation_rotY:" + (lookAtRotation.y + mTestRotY));
        //        HaloLogger.logE("helong_fix_____", "orientation_rotZ:" + (lookAtRotation.z + mTestRotZ));
        //        HaloLogger.logE("helong_fix_____", "====================================");
        //
        //
        //        //        getCurrentCamera().setUpAxis(Vector3.Axis.Y);
        //        //        if (getCurrentCamera() instanceof NewFirstPersonCamera) {
        //        //            ((NewFirstPersonCamera) getCurrentCamera()).setCameraOffset(offset);
        //        //        }
        //
        // TODO: 2016/7/18 目前看只有lookAt的位置是正常的,摄像机位置以及被追随物体位置经常过偏过一边
        //摄像机位置
        //        mSphere.setPosition(new Vector3(pos.x, pos.y, 0));
        //        //被追随位置
        //        mSphere2.setPosition(new Vector3(mObject4Chase.getPosition().x, mObject4Chase.getPosition().y, 0));
        //        //摄像机的lookAt
        //        mSphere1.setPosition(new Vector3(lookAt.x + mTestLookAtX, lookAt.y + mTestLookAtY, lookAt.z + mTestLookAtZ));

        getCurrentCamera().setNearPlane(CAMERA_NEAR_PLANE);
        getCurrentCamera().setFarPlane(CAMERA_FAR_PLANE);
        super.onRender(ellapsedRealtime, deltaTime);
    }

    // TODO: 2016/7/21 TEST
    private void handleLookAt(Vector3 rotation) {
        //        if (Math.abs(lookAt.x) < 80 && Math.abs(lookAt.x) > 60) {
        //            if(Math.abs(lookAt.y)>Math.abs(lookAt.z)){
        //                lookAt.z = -lookAt.y;
        //            }else{
        //                lookAt.y = -lookAt.z;
        //            }
        //        }else if(Math.abs(lookAt.z) < 80 && Math.abs(lookAt.z) > 60){
        //            if(Math.abs(lookAt.x)>Math.abs(lookAt.y)){
        //                lookAt.y = -lookAt.x;
        //            }else{
        //                lookAt.x = -lookAt.y;
        //            }
        //        }else{
        //            if(Math.abs(lookAt.x)>Math.abs(lookAt.z)){
        //                lookAt.z = -lookAt.x;
        //            }else{
        //                lookAt.x = -lookAt.z;
        //            }
        //        }
        double xTo70 = Math.abs(70 - (Math.abs(rotation.x)));
        double yTo70 = Math.abs(70 - (Math.abs(rotation.y)));
        double zTo70 = Math.abs(70 - (Math.abs(rotation.z)));
        //        HaloLogger.logE("helong_fix_____", "lookat:" + rotation);
        //        HaloLogger.logE("helong_fix_____", "fix:xTo70:" + xTo70 + ",yTo70:" + yTo70 + ",zTo70:" + zTo70);
        if (Math.min(xTo70, Math.min(yTo70, zTo70)) == yTo70 && Math.abs(yTo70) < 10) {
            //x轴最接近70
            //            lookAt.y = lookAt.y < 0 ? -76 : 76/*Math.abs(lookAt.y) > 76 ? 76 : lookAt.y*/;
            //            if (Math.abs(lookAt.x) > Math.abs(lookAt.z)) {
            //                if ((lookAt.z < 0 && lookAt.x >= 0) || (lookAt.z >= 0 && lookAt.x < 0)) {
            //                    //符号不同
            //                    lookAt.z = -lookAt.x;
            //                } else {
            //                    //符号相同
            //                    lookAt.z = lookAt.x;
            //                }
            //            } else {
            //                if ((lookAt.z < 0 && lookAt.x >= 0) || (lookAt.z >= 0 && lookAt.x < 0)) {
            //                    //符号不同
            //                    lookAt.x = -lookAt.z;
            //                } else {
            //                    //符号相同
            //                    lookAt.x = lookAt.z;
            //                }
            //            }


            if (Math.abs(rotation.x) > Math.abs(rotation.z)) {
                if (rotation.y < 0) {
                    //符号不同
                    rotation.z = -rotation.x;
                } else {
                    //符号相同
                    rotation.z = rotation.x;
                }
            } else {
                if (rotation.y < 0) {
                    //符号不同
                    rotation.x = -rotation.z;
                } else {
                    //符号相同
                    rotation.x = rotation.z;
                }
            }

            //            HaloLogger.logE("helong_fix_____", "y_fix:" + rotation);
        } else if (Math.min(xTo70, Math.min(yTo70, zTo70)) == zTo70 && Math.abs(zTo70) < 10) {
            //z轴最接近70,目前测试未发现Y轴代表倾斜的情况,因此暂时不考虑Y轴为70,目前逻辑不是X即认为为Z
            //            lookAt.z = lookAt.z < 0 ? -76 : 76/*Math.abs(lookAt.z) > 76 ? -76 : lookAt.z*/;
            //            if (Math.abs(lookAt.x) > Math.abs(lookAt.y)) {
            //                lookAt.y = -lookAt.x;
            //            } else {
            //                lookAt.x = -lookAt.y;
            //            }
            //            if (Math.abs(lookAt.x) > Math.abs(lookAt.y)) {
            //                if ((lookAt.y < 0 && lookAt.x >= 0) || (lookAt.y >= 0 && lookAt.x < 0)) {
            //                    //符号不同
            //                    lookAt.y = -lookAt.x;
            //                } else {
            //                    //符号相同
            //                    lookAt.y = lookAt.x;
            //                }
            //            } else {
            //                if ((lookAt.y < 0 && lookAt.x >= 0) || (lookAt.y >= 0 && lookAt.x < 0)) {
            //                    //符号不同
            //                    lookAt.x = -lookAt.y;
            //                } else {
            //                    //符号相同
            //                    lookAt.x = lookAt.y;
            //                }
            //            }


            if (Math.abs(rotation.x) > Math.abs(rotation.y)) {
                if (rotation.z < 0) {
                    //符号不同
                    rotation.y = -rotation.x;
                } else {
                    //符号相同
                    rotation.y = rotation.x;
                }
            } else {
                if (rotation.z < 0) {
                    //符号不同
                    rotation.x = -rotation.y;
                } else {
                    //符号相同
                    rotation.x = rotation.y;
                }
            }

            //            HaloLogger.logE("helong_fix_____", "z_fix:" + rotation);
        } /*else if (Math.min(xTo70, Math.min(yTo70, zTo70)) == xTo70 && Math.abs(xTo70) < 10) {
            //lookAt.x = ;
//            if (Math.abs(lookAt.y) > Math.abs(lookAt.z)) {
//                lookAt.z = -lookAt.y;
//            } else {
//                lookAt.y = -lookAt.z;
//            }

            HaloLogger.logE("helong_fix_____", "x_fix:" + lookAt);
        }*/
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

    /**
     * 通过岔路点的集合以及路口放大图中箭头位置的点的坐标会实现将岔路添加到场景中
     */
    public void setEnlargeCrossBranchLines(double length, int naviIcon) {
        if (length <= 0 || !mIsMyInitScene) {
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
        for (int i = 0; i < 1/*branchLines.size()*/; i++) {
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
            for (int j = 0; j < branchPointsOpengl.size()/*/3*/; j++) {
                /*Vector3 v = branchPointsOpengl.get(j);
                if(j==0){
                    Vector3 target = branchPointsOpengl.get(branchPointsOpengl.size()/5);
                    offset = new Vector3(v.x-target.x,v.y-target.y,v.z-target.z);
                }
                branchPointsOpengl2.add(new Vector3(v.x-offset.x,v.y-offset.y,v.z-offset.z));*/
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
        }
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

    /**
     * 从屏幕坐标转换到opengl坐标
     * 注意:道路的数据本身是乘以了BIGGER_TIME的,因此此处转换完也需要乘以该系数
     *
     * @param branchPoints
     * @param branchPointsOpengl
     * @param center
     */
    private void convertScreen2Opengl(List<Point> branchPoints, List<Vector3> branchPointsOpengl, Vector3 center) {
        //将屏幕坐标转换成opengl坐标,由于这些坐标都是相对于路口放大图中心点的相对坐标,因此需要在原来值的基础上加上center才是绝对坐标
        //TODO MathUtils中的屏幕宽高是使用设备屏幕宽高,还是GLSurfaceView的宽高??
        //TODO toGLX和toGLY方法的准确性需要测试,方法中的RATIO值以及宽高值都待定
        for (int i = 0; i < branchPoints.size(); i++) {
            Vector3 v = new Vector3();
            Point p = branchPoints.get(i);
            //路口放大图部分处理时的坐标系左上角为0,0,此处将其移动到中心点
            p.x -= 200;
            p.y -= 200;
            screenCrood2OpenglCrood(p.x, p.y, 0, v);
            v.x += center.x;
            v.y += center.y;
            v.z = 0;
            branchPointsOpengl.add(v);
        }
        //        HaloLogger.logE("helong_fix____", "previous convert branch:" + branchPointsOpengl);
        //save the offsetXY between point and point.
        List<Double> offsetX = new ArrayList<>();
        List<Double> offsetY = new ArrayList<>();
        for (int i = 0; i < branchPointsOpengl.size(); i++) {
            double x = 0;
            double y = 0;
            if (i == 0) {
                x = center.x;
                y = center.y;
            } else {
                x = branchPointsOpengl.get(i - 1).x;
                y = branchPointsOpengl.get(i - 1).y;
            }
            offsetX.add(branchPointsOpengl.get(i).x - x);
            offsetY.add(branchPointsOpengl.get(i).y - y);
        }

        //branchPointsOpengl为原始数据放大
        //to bigger
        for (int i = 0; i < branchPointsOpengl.size(); i++) {
            double preX = 0;
            double preY = 0;
            if (i == 0) {
                preX = center.x;
                preY = center.y;
            } else {
                preX = branchPointsOpengl.get(i - 1).x;
                preY = branchPointsOpengl.get(i - 1).y;
            }
            // TODO: 2016/7/25 由于不确定高德转opengl坐标的比例尺,因此此处不能继续使用BIGGER_TIME,而应该是另外一个值(待定)
            branchPointsOpengl.get(i).x = preX + offsetX.get(i) * 5/*BIGGER_TIME*/;
            branchPointsOpengl.get(i).y = preY + offsetY.get(i) * 5/*BIGGER_TIME*/;
        }
    }

    /**
     * 根据差角旋转岔路数据到实际状况
     *
     * @param branchPointsOpengl
     * @param divDegrees
     */
    private void rotateBranchWithDivDegrees(List<Vector3> branchPointsOpengl, double divDegrees) {
        Vector3 base = new Vector3();
        screenCrood2OpenglCrood(base.x, base.y, base.z, base);
        for (int i = 0; i < branchPointsOpengl.size(); i++) {
            MathUtils.rotateCoordinate(base, branchPointsOpengl.get(i), divDegrees);
        }
    }

    private void screenCrood2OpenglCrood(double x, double y, double z, Vector3 openglCrood) {
        //
        // -- unproject the screen coordinate (2D) to the camera's near plane
        //
        GLU.gluUnProject(x, getViewportHeight() - y, 0, mViewMatrix.getDoubleValues(), 0,
                         mProjectionMatrix.getDoubleValues(), 0, mViewport, 0, mNearPos4, 0);
        //
        // -- unproject the screen coordinate (2D) to the camera's far plane
        //
        GLU.gluUnProject(x, getViewportHeight() - y, 1.f, mViewMatrix.getDoubleValues(), 0,
                         mProjectionMatrix.getDoubleValues(), 0, mViewport, 0, mFarPos4, 0);
        //
        // -- transform 4D coordinates (x, y, z, w) to 3D (x, y, z) by dividing
        // each coordinate (x, y, z) by w.
        //
        mNearPos.setAll(mNearPos4[0] / mNearPos4[3], mNearPos4[1]
                / mNearPos4[3], mNearPos4[2] / mNearPos4[3]);
        mFarPos.setAll(mFarPos4[0] / mFarPos4[3],
                       mFarPos4[1] / mFarPos4[3], mFarPos4[2] / mFarPos4[3]);
        //
        // -- now get the coordinates for the selected object
        //
        double factor = (Math.abs(z) + mNearPos.z)
                / (getCurrentCamera().getFarPlane() - getCurrentCamera()
                .getNearPlane());
        openglCrood.setAll(mFarPos);
        openglCrood.subtract(mNearPos);
        openglCrood.multiply(factor);
        openglCrood.add(mNearPos);
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
     * @param projection
     * @param naviPath
     */
    public void setPath(Projection projection, AMapNaviPath naviPath, boolean repeat) {
        if (projection == null || naviPath == null) {
            return;
        }
        List<Vector3> path = new ArrayList<>();
        for (int i = 0; i < naviPath.getCoordList().size(); i++) {
            PointF openGL = projection.toOpenGLLocation(DrawUtils.naviLatLng2LatLng(naviPath.getCoordList().get(i)));
            //openGL.y轴坐标被翻转过,因此需要使用它的倒数
            path.add(new Vector3(openGL.x, -openGL.y, 0));
        }

        if (!repeat || !isPathRepeat(path)) {
            mOriginalPath.clear();
            mOriginalPath.addAll(path);
            setPathAndCalcData(mOriginalPath, naviPath.getAllLength());
        } else {
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "arway setPath is repeat path");
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
     */
    private void setPathAndCalcData(List<Vector3> path, double allLength) {
        HaloLogger.logE(ARWayConst.SPECIAL_LOG_TAG,"setPathAndCalcData start");
        clearAllData();
        //此处由于mPath中的点是path中点的子集,去除了相同的点,因此二者长度不一致,之后不要在使用path
        for (int i = 0; i < path.size(); i++) {
            Vector3 v = path.get(i);
            if (!containPoint(mPath, v)) {
                mPath.add(new Vector3(v));
            }
        }
        if (mPath.size() < 2) {
            mPath.clear();
            return;
        }

        //clear the points because that points is too close
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

        //save the offsetXY between point and point.
        for (int i = 1; i < mPath.size(); i++) {
            double x = mPath.get(i).x;
            double y = mPath.get(i).y;
            mOffsetX.add(x - mPath.get(i - 1).x);
            mOffsetY.add(y - mPath.get(i - 1).y);
        }

        //mPath为原始数据,需要移动,放大,旋转
        //to bigger
        for (int i = 1; i < mPath.size(); i++) {
            mPath.get(i).x = mPath.get(i - 1).x + mOffsetX.get(i - 1) * BIGGER_TIME;
            mPath.get(i).y = mPath.get(i - 1).y + mOffsetY.get(i - 1) * BIGGER_TIME;
        }

        //使用更多的点来实现曲线path
        //阈值 threshold
        //        double threshold = 250 / (1.0 * tempBigTime4Calc / BIGGER_TIME);
        //        //key is child path index in mPath,and value is child path
        //        HashMap<Integer, ArrayList<Vector3>> childPaths = new HashMap<>();
        //        for (int i = 0; i < mPath.size() - 2; i++) {
        //            Vector3 v1 = mPath.get(i);
        //            Vector3 v2 = mPath.get(i + 1);
        //            Vector3 v3 = mPath.get(i + 2);
        //            if (i < mPath.size() - 3) {
        //                Vector3 v4 = mPath.get(i + 3);
        //                double dist1 = MathUtils.calculateDistance(v1.x, v1.y, v2.x, v2.y);
        //                double dist2 = MathUtils.calculateDistance(v2.x, v2.y, v3.x, v3.y);
        //                double dist3 = MathUtils.calculateDistance(v3.x, v3.y, v4.x, v4.y);
        //                if (dist1 <= threshold && dist2 <= threshold && dist3 <= threshold) {
        //                    int key = i;
        //                    ArrayList<Vector3> childPath = new ArrayList<>();
        //                    childPath.add(v1);
        //                    childPath.add(v2);
        //                    childPath.add(v3);
        //                    childPath.add(v4);
        //                    //这里是加2而不是加3的原因:加1后在下次循环时,这次的最后一个点会被作为下次循环时判断的起始点,该点是有必要的
        //                    i += 3;
        //                    for (int j = i + 3; j < mPath.size() - 1; j++) {
        //                        Vector3 ve1 = mPath.get(j);
        //                        Vector3 ve2 = mPath.get(j + 1);
        //                        if (MathUtils.calculateDistance(ve1.x, ve1.y, ve2.x, ve2.y) <= threshold) {
        //                            i++;
        //                            childPath.add(ve2);
        //                        } else {
        //                            break;
        //                        }
        //                    }
        //                    childPaths.put(key, childPath);
        //                    continue;
        //                }
        //            }
        //
        //            double degrees = MathUtils.getDegrees(
        //                    new PointF((float) v1.x, (float) v1.y),
        //                    new PointF((float) v2.x, (float) v2.y),
        //                    new PointF((float) v3.x, (float) v3.y));
        //            HaloLogger.logE("helong_debug", "线段内侧夹角:" + degrees);
        //            if (degrees <= 135) {
        //                int key = i;
        //                ArrayList<Vector3> childPath = new ArrayList<>();
        //                childPath.add(v1);
        //                childPath.add(v2);
        //                childPath.add(v3);
        //                //这里是加1而不是加2的原因:加1后在下次循环时,这次的最后一个点会被作为下次循环时判断的起始点,该点是有必要的
        //                i++;
        //                for (int j = i; j < mPath.size() - 2; j++) {
        //                    Vector3 ve1 = mPath.get(j);
        //                    Vector3 ve2 = mPath.get(j + 1);
        //                    Vector3 ve3 = mPath.get(j + 2);
        //                    degrees = MathUtils.getDegrees(
        //                            new PointF((float) ve1.x, (float) ve1.y),
        //                            new PointF((float) ve2.x, (float) ve2.y),
        //                            new PointF((float) ve3.x, (float) ve3.y));
        //                    if (degrees <= 135) {
        //                        i++;
        //                        childPath.add(ve3);
        //                    } else {
        //                        i++;
        //                        break;
        //                    }
        //                }
        //                childPaths.put(key, childPath);
        //            }
        //        }
        //
        //        int pathLength = mPath.size();
        //        HaloLogger.logE("helong_debug", "插值前:path_size:" + mPath.size());
        //        List<Vector3> tempPath = new ArrayList<>();
        //        for (int i = 0; i < pathLength; i++) {
        //            if (childPaths.containsKey(i)) {
        //                List<Vector3> childPath = childPaths.get(i);
        //                HaloLogger.logE("helong_debug", "size:" + childPath.size());
        //                CatmullRomCurve3D catmull = new CatmullRomCurve3D();
        //                catmull.addPoint(new Vector3(childPath.get(0)));
        //                for (int j = 0; j < childPath.size(); j++) {
        //                    catmull.addPoint(new Vector3(childPath.get(j)));
        //                }
        //                catmull.addPoint(new Vector3(childPath.get(childPath.size() - 1)));
        //                for (int j = 0; j < childPath.size() * CURVE_TIME; j++) {
        //                    Vector3 pos = new Vector3();
        //                    catmull.calculatePoint(pos, (1.0 * j) / (1.0 * childPath.size() * CURVE_TIME));
        //                    tempPath.add(new Vector3(pos));
        //                }
        //                i += childPath.size() - 1;
        //            } else {
        //                tempPath.add(new Vector3(mPath.get(i)));
        //            }
        //        }
        //        mPath.clear();
        //        mPath.addAll(tempPath);
        //        tempPath.clear();
        //        HaloLogger.logE("helong_debug", "插值后:path_size:" + mPath.size());

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

        //rotate path with matrix
        double rotateZ = MathUtils.getDegrees(mPath.get(0).x, mPath.get(0).y, mPath.get(CURVE_TIME - 1).x, mPath.get(CURVE_TIME - 1).y);
        Matrix matrix = new Matrix();
        matrix.setRotate((float) rotateZ - 180, (float) mPath.get(0).x, (float) mPath.get(0).y);
        for (int i = 1; i < mPath.size(); i++) {
            Vector3 v = mPath.get(i);
            float[] xy = new float[2];
            matrix.mapPoints(xy, new float[]{(float) v.x, (float) v.y});
            v.x = xy[0];
            v.y = xy[1];
        }

        //move to screen center
        double offsetX = mPath.get(0).x - 0;
        double offsetY = mPath.get(0).y - 0;
        for (int i = 0; i < mPath.size(); i++) {
            mPath.get(i).x -= offsetX;
            mPath.get(i).y -= offsetY;
        }

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

        //save every point to final point`s dist m and opengl
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

        //Through will calc left and right path in insertARWayObject,but they calc with many littlePath not mPath.
        //PS:ROAD_WIDTH 代表的是逻辑路宽,由于路宽被用于与lookAt点计算相交,为了确保lookAt的点在道路中央,将逻辑路宽设置的比现实路宽要小很多即可
        //同时,逻辑路宽小能够确保摄像机到lookAt的连线与道路走向吻合
        MathUtils.points2path(mLeftPath, mRightPath, mPath, LOGIC_ROAD_WIDTH);

        //将path切割成N个小path分别加载到场景中
        splitPath2LittlePathesWithIndexAndLength(mPath, mChildPathPositions, mChildPathes, mStartAddPlaneIndex, ADD_PLANE_LENGTH);


        //set flag with true indicates(表示) data is inited
        mCanInitScene = true;
        //如果Rajawali自身的初始化场景完毕,那么就可以进行我们自己的场景初始化(目前就是绘制道路,并进行贴图移动)
        if (mIsInitScene) {
            myInitScene();
        }

        //clear not userful data to free memory
        clearUnuseDataAfterInitPathData();
    }

    /**
     * //split path to many little path with 30(for now)
     *
     * @param allPath
     * @param childPathPositions
     * @param childPathes
     * @param startAddPlaneIndex
     */
    private void splitPath2LittlePathesWithIndexAndLength(List<Vector3> allPath, List<Vector3> childPathPositions, List<List<Vector3>> childPathes, int startAddPlaneIndex, double addPlaneLength) {
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
    }

    /**
     * clear data about draw arway!
     */
    private void clearAllData() {
        mPath.clear();
        mLeftPath.clear();
        mRightPath.clear();
        mOffsetX.clear();
        mOffsetY.clear();
        mLength2FinalPoint.clear();
        mDist2FinalPoint.clear();
        mCurIndexes.clear();
        mTranslateAnims.clear();
        mRotateAnims.clear();
        mChildPathPositions.clear();
        mChildPathes.clear();
        mLastThroughPosition.clear();
        mMainRoadObjects.clear();
        mBranchRoadObjects.clear();
        mCoverRoadPlanes.clear();
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
        mCanInitScene = false;
        mObject4Chase = null;
        System.gc();
    }

    /**
     * clear not use data after we init path data!
     */
    private void clearUnuseDataAfterInitPathData() {
        mOffsetX.clear();
        mOffsetY.clear();
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
        /*mCurScene = new Scene(this);
        mCurScene.resetGLState();
        addAndSwitchScene(mCurScene);
        if (mMainRoadObjects != null && mMainRoadObjects.size() > 0) {
            for (ARWayRoadObject arWayRoadObject : mMainRoadObjects) {
                arWayRoadObject.removeMaterial();
                arWayRoadObject.destroy();
            }
        }
        if (mBranchRoadObjects != null && mBranchRoadObjects.size() > 0) {
            for (ARWayRoadObject arWayRoadObject : mBranchRoadObjects) {
                arWayRoadObject.removeMaterial();
                arWayRoadObject.destroy();
            }
        }
        if (mCoverRoadPlanes != null && mCoverRoadPlanes.size() > 0) {
            for (Plane plane : mCoverRoadPlanes) {
                plane.destroy();
            }
        }
        if (mObject4Chase != null) {
            mObject4Chase.destroy();
        }*/
        //        MaterialManager.getInstance().reset();
        getCurrentScene().clearChildren();

        //        getCurrentScene().setBackgroundColor(0x222222);
        //        List<Vector3> leftPts = new ArrayList<>();
        //        List<Vector3> rightPts = new ArrayList<>();
        //        MathUtils.points2path(leftPts, rightPts, mPreChildEndPos, LOGIC_ROAD_WIDTH);

        //        if (mMainRoadTexture == null) {
        //            mMainRoadTexture = new Texture("route_new", R.drawable.route_new);
        //        }
        //        for (int i = 0; i < mPreChildEndPos.size(); i++) {
        //            //            Stack<Vector3> line = new Stack<>();
        //            //            line.add(leftPts.get(i));
        //            //            line.add(rightPts.get(i));
        //            //            Line3D line3D = new Line3D(line, 1);
        //            //            line3D.setMaterial(new Material());
        //            //            line3D.setColor(0xff0000);
        //            //            getCurrentScene().addChild(line3D);
        //            Sphere sphere = new Sphere(0.1f, 24, 24);
        //            sphere.setPosition(mPreChildEndPos.get(i));
        //            sphere.setMaterial(new Material());
        //            Material sphereM = sphere.getMaterial();
        //            try {
        //                sphereM.addTexture(mMainRoadTexture);
        //            } catch (ATexture.TextureException e) {
        //                e.printStackTrace();
        //            }
        //            sphere.setColor(Color.BLUE);
        //            getCurrentScene().addChild(sphere);
        //        }

        /*mSphere = new Sphere(0.05f, 24, 24);
        mSphere.setPosition(mPath.get(0).x, mPath.get(0).y, 0);
        mSphere.setMaterial(new Material());
        mSphere.setColor(0xff0000);
        if (ARWayConst.IS_DEBUG_SCENE) {
            getCurrentScene().addChild(mSphere);
        }


        mSphere1 = new Sphere(0.05f, 24, 24);
        mSphere1.setPosition(mPath.get(0).x, mPath.get(0).y, 0);
        mSphere1.setMaterial(new Material());
        mSphere1.setColor(Color.RED);
        if (ARWayConst.IS_DEBUG_SCENE) {
            getCurrentScene().addChild(mSphere1);
        }

        mSphere2 = new Sphere(0.05f, 24, 24);
        mSphere2.setPosition(mPath.get(0).x, mPath.get(0).y, 0);
        mSphere2.setMaterial(new Material());
        mSphere2.setColor(0x00ff00);
        if (ARWayConst.IS_DEBUG_SCENE) {
            getCurrentScene().addChild(mSphere2);
        }*/

        //        mObject4Chase = new Object3D();
        //        mObject4Chase.setPosition(mPath.get(0).x, mPath.get(0).y, 0);
        //        mObject4Chase.setMaterial(new Material());
        mObject4Chase = new Sphere(0.0001f, 24, 24);
        mObject4Chase.setPosition(mPath.get(0).x, mPath.get(0).y, 0);
        mObject4Chase.setMaterial(new Material());
        if (ARWayConst.IS_DEBUG_SCENE) {
            getCurrentScene().addChild(mObject4Chase);
        }

        //        NewFirstPersonCamera firstPersonCamera = new NewFirstPersonCamera(new Vector3(
        //                CAMERA_OFFSET_X, CAMERA_OFFSET_Y, CAMERA_OFFSET_Z
        //        ), mObject4Chase);
        //        ChaseCamera firstPersonCamera = new ChaseCamera(new Vector3(
        //                CAMERA_OFFSET_X, CAMERA_OFFSET_Y, CAMERA_OFFSET_Z
        //        ), mObject4Chase);
        //        NewChaseCamera firstPersonCamera = new NewChaseCamera(new Vector3(
        //                CAMERA_OFFSET_X, CAMERA_OFFSET_Y, CAMERA_OFFSET_Z
        //        ), mObject4Chase);
        //        getCurrentScene().replaceAndSwitchCamera(getCurrentCamera(), firstPersonCamera);

        getCurrentCamera().setUpAxis(Vector3.Axis.Z);

        addPlane2Scene();

        //        Material maHo = new Material();
        //        //        maHo.setColorInfluence(0);
        //        //        try {
        //        //            maHo.addTexture(new Texture("route_new", R.drawable.route_new));
        //        //        } catch (ATexture.TextureException e) {
        //        //            e.printStackTrace();
        //        //        }
        //
        //        Material maVe = new Material();
        //        //        maVe.setColorInfluence(0);
        //        //        try {
        //        //            maVe.addTexture(new Texture("route_new", R.drawable.route_new3));
        //        //        } catch (ATexture.TextureException e) {
        //        //            e.printStackTrace();
        //        //        }
        //        for (int i = 0; i < mPath.size() - 1; i++) {
        //            Vector3 v1 = mPath.get(i);
        //            Vector3 v2 = mPath.get(i + 1);
        //            float width = 0f;// Math.abs(v2.x - v1.x) > 0.5 ? (float) Math.abs(v2.x - v1.x) : 0.5f;
        //            float height = 0f;//Math.abs(v2.y - v1.y) > 0.5 ? (float) Math.abs(v2.y - v1.y) : 0.5f;
        //            double degrees = MathUtils.getDegrees(v1.x, v1.y, v2.x, v2.y);
        //            if (Math.abs(v2.x - v1.x) > Math.abs(v2.y - v1.y)) {
        //                height = (float) MathUtils.calculateDistance(v1.x, v1.y, v2.x, v2.y) + 0.05f;
        //                width = 0.6f;
        //            } else {
        //                width = (float) MathUtils.calculateDistance(v1.x, v1.y, v2.x, v2.y) + 0.05f;
        //                height = 0.6f;
        //                //如果是横向的话就相当于已经转了90度
        //                degrees -= 90;
        //            }
        //
        //            Plane plane = new Plane(width, height, 1, 1/*, Vector3.Axis.Z, true, true, 1*/);P
        //            if (Math.abs(v2.x - v1.x) > Math.abs(v2.y - v1.y)) {
        //                plane.setMaterial(maHo);
        //            } else {
        //                plane.setMaterial(maVe);
        //            }
        //            plane.setDoubleSided(true);
        //            if (i % 2 == 0)
        //                plane.setColor(0xff3333ff);
        //            else
        //                plane.setColor(0xff00ff00);
        //            plane.setPosition((v1.x + v2.x) / 2, (v1.y + v2.y) / 2, (v1.z + v2.z) / 2);
        //            Quaternion qn = plane.getOrientation();
        //            qn.fromAngleAxis(Vector3.Axis.Z, degrees);
        //            plane.setOrientation(qn);
        //            getCurrentScene().addChild(plane);
        //        }

        //update flag
        mIsMyInitScene = true;
        mCanInitScene = false;

        clearUnuseDataAfterMyInitScene();
    }

    /***
     * 向场景中添加道路
     */
    private void addPlane2Scene() {
        //mMainRoadObjects.clear();
        for (int i = 0; i < mChildPathPositions.size(); i++) {
            mMainRoadObjects.add(insertARWayObject(mChildPathes.get(i), mChildPathPositions.get(i), ROAD_WIDTH, 1));
        }
        HaloLogger.logE("helong_debug", "arway size:" + mMainRoadObjects.size());
        HaloLogger.logE("helong_debug", "child size:" + getCurrentScene().getNumChildren());
        clearUnuseDataAfterAddPlane2Scene();
    }

    /**
     * 从场景中移除道路(每次移除小于300m的最大距离)
     */
    private void removePlaneFromScene() {
        int size = mMainRoadObjects.size() / 3;
        List<ARWayRoadObject> toRemovedList = mMainRoadObjects.subList(0, size);
        for (int i = 0; i < size; i++) {
            getCurrentScene().removeChild(toRemovedList.get(i));
            toRemovedList.get(i).removeMaterial();
        }
        mMainRoadObjects.remove(toRemovedList);
        toRemovedList.clear();
        clearUnuseDataAfterRemovePlaneFromScene();
    }

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
        if (type == 2)
            roadType = ARWayRoadObject.ARWAY_ROAD_TYPE_BRANCH;
        else if (type == 3)
            roadType = ARWayRoadObject.ARWAY_ROAD_TYPE_BRANCH_BLACK;
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

    private ARWayRoadObject insertARWayObject(List<Vector3> littlePath, Vector3 position, double width, int type) {
        return insertARWayObject(littlePath, position, width, width, type);
    }

    public void setRetainDistance(double endLength) {
        if (endLength > mLength2FinalPoint.get(0)) {
            return;
        }

        //first time callback
        if (mStartTime == 0) {
            mStartTime = System.currentTimeMillis();
            mStartLength = endLength;
            return;
        }

        //not first time
        //暂定动画移动的距离最小为50m,也就是说目前是固定距离来做动画
        //之后可以考虑使用固定时间,这个要看效果
        //小于50直接return
        //        if (mStartLength - endLength < ANIMATION_LENGTH) {
        //            return;
        //        }

        //防止回退
        long endTime = System.currentTimeMillis();
        if (mStartLength <= endLength || endTime - mStartTime < 4000) {
            return;
        }

        //到此表示返回的数据满足动画要求
        if (mCameraAnim != null) {
            if (mCameraAnim.isPlaying()) {
                mCameraAnim.pause();
            }
            mCameraAnim.unregisterListener(this);
        }
        Object3D chaseObject = mObject4Chase;
        //观察到原始durqation基本在4000以上,因此设置PERTENSION_TIME的值为1000,也就是使用更长的动画时间来减少停滞的情况发生
        long duration = endTime - mStartTime + PRETENSION_TIME;
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

        if (mTranslateAnims != null && mTranslateAnims.size() > 0) {
            for (int i = 0; i < mTranslateAnims.size(); i++) {
                if (mTranslateAnims.get(i).isPlaying()) {
                    mTranslateAnims.get(i).pause();
                    if (ARWayConst.ENABLE_TEST_LOG) {
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
                    if (ARWayConst.ENABLE_TEST_LOG) {
                        HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "*************mRotateAnims rotate anim stop !!!!!!!!!!!******************");
                    }
                }
                //mRotateAnims.get(i).unregisterListener(this);
                //不加这句在程序运行时间过长时会导致内存泄漏,场景本身持有了大量的动画对象
                getCurrentScene().unregisterAnimation(mRotateAnims.get(i));
            }
            mRotateAnims.clear();
        }

        setEndAndThroughPosition(mPath, mLength2FinalPoint, mStartLength, endLength, endPosition, throughPosition);
        mLastThroughPosition.add(new Vector3(startPosition));
        mLastThroughPosition.addAll(throughPosition);
        mLastThroughPosition.add(new Vector3(endPosition));

        startAnimation2(startPosition, throughPosition, endPosition, duration);

        mRealCurPosition.setAll(endPosition);
        //reset data
        mStartTime = endTime;
        mStartLength = endLength;
        mEndLength = endLength;

        if (mRetainTotalLength - endLength >= ADD_PLANE_LENGTH / 2) {
            new Thread() {
                @Override
                public void run() {
                    long time = System.currentTimeMillis();
                    removePlaneFromScene();
                    splitPath2LittlePathesWithIndexAndLength(mPath, mChildPathPositions, mChildPathes,
                                                             mStartAddPlaneIndex, mRetainTotalLength - mEndLength);
                    addPlane2Scene();
                    HaloLogger.logE("helong_debug", "add plane,move length:" + (mRetainTotalLength - mEndLength) + ",mStartAddPlaneIndex:" + mStartAddPlaneIndex);
                    mRetainTotalLength = mEndLength;
                    HaloLogger.logE("helong_debug", "over time:" + (System.currentTimeMillis() - time));
                }
            }.start();
        }
    }

    private void startAnimation2(Vector3 startPosition, List<Vector3> throughPosition, Vector3 endPosition, long duration) {
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

    /**
     * 根据获取到的数据开始一段组合动画
     *
     * @param startPosition
     * @param throughPosition
     * @param endPosition
     * @param duration
     */
    private void startAnimation(final Vector3 startPosition, final List<Vector3> throughPosition,
                                final Vector3 endPosition, final long duration) {
        CompoundCurve3D compound = new CompoundCurve3D();
        HaloLogger.logE("helong_debug", "==========================================");
        if (throughPosition == null || throughPosition.size() <= 0) {
            LinearBezierCurve3D curve3D = new LinearBezierCurve3D(
                    new Vector3(startPosition.x, startPosition.y, OBJ_4_CHASE_Z),
                    new Vector3(endPosition.x, endPosition.y, OBJ_4_CHASE_Z));
            compound.addCurve(curve3D);
            testLog(startPosition, endPosition);
        } else {
            if (throughPosition.size() == 1) {
                LinearBezierCurve3D curve3D = null;
                curve3D = new LinearBezierCurve3D(
                        new Vector3(startPosition.x, startPosition.y, OBJ_4_CHASE_Z),
                        new Vector3(throughPosition.get(0).x, throughPosition.get(0).y, OBJ_4_CHASE_Z));
                compound.addCurve(curve3D);
                testLog(startPosition, throughPosition.get(0));
                curve3D = new LinearBezierCurve3D(
                        new Vector3(throughPosition.get(0).x, throughPosition.get(0).y, OBJ_4_CHASE_Z),
                        new Vector3(endPosition.x, endPosition.y, OBJ_4_CHASE_Z));
                compound.addCurve(curve3D);
                testLog(throughPosition.get(0), endPosition);
            } else {
                for (int i = 0; i < throughPosition.size(); i++) {
                    LinearBezierCurve3D curve3D = null;
                    if (i == 0) {
                        curve3D = new LinearBezierCurve3D(
                                new Vector3(startPosition.x, startPosition.y, OBJ_4_CHASE_Z),
                                new Vector3(throughPosition.get(i).x, throughPosition.get(i).y, OBJ_4_CHASE_Z));
                        compound.addCurve(curve3D);
                        testLog(startPosition, throughPosition.get(i));
                    } else if (i == throughPosition.size() - 1) {
                        curve3D = new LinearBezierCurve3D(
                                new Vector3(throughPosition.get(i - 1).x, throughPosition.get(i - 1).y, OBJ_4_CHASE_Z),
                                new Vector3(throughPosition.get(i).x, throughPosition.get(i).y, OBJ_4_CHASE_Z));
                        compound.addCurve(curve3D);
                        testLog(throughPosition.get(i - 1), throughPosition.get(i));
                        curve3D = new LinearBezierCurve3D(
                                new Vector3(throughPosition.get(i).x, throughPosition.get(i).y, OBJ_4_CHASE_Z),
                                new Vector3(endPosition.x, endPosition.y, OBJ_4_CHASE_Z));
                        compound.addCurve(curve3D);
                        testLog(throughPosition.get(i), endPosition);
                    } else {
                        curve3D = new LinearBezierCurve3D(
                                new Vector3(throughPosition.get(i - 1).x, throughPosition.get(i - 1).y, OBJ_4_CHASE_Z),
                                new Vector3(throughPosition.get(i).x, throughPosition.get(i).y, OBJ_4_CHASE_Z));
                        compound.addCurve(curve3D);
                        testLog(throughPosition.get(i - 1), throughPosition.get(i));
                    }
                }
            }
        }
        HaloLogger.logE("helong_debug", "==========================================");

        compound.setCalculateTangents(true);
        mCameraAnim = new SplineTranslateAnimation3D(compound);
        mCameraAnim.setDurationMilliseconds(duration);
        mCameraAnim.setTransformable3D(getCurrentCamera());
        mCameraAnim.setInterpolator(new LinearInterpolator());
        mCameraAnim.setRepeatMode(Animation.RepeatMode.NONE);
        mCameraAnim.registerListener(this);
        getCurrentScene().registerAnimation(mCameraAnim);
        mCameraAnim.play();
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
        //        HaloLogger.logE("helong_debug", "update_camera_position:" + getCurrentCamera().getPosition());
        //        HaloLogger.logE("helong_debug", "update_camera_rotation:" + getCurrentCamera().getRotZ());
    }

    /*@Override
    public Scene getCurrentScene() {
        return mCurScene == null ? super.getCurrentScene() : mCurScene;
    }

    @Override
    public Camera getCurrentCamera() {
        return mCurScene == null ? super.getCurrentCamera() : mCurScene.getCamera();
    }*/

    //=====================================rajawali animation callback end=========================================//


    double mTestRotX    = 0;
    double mTestRotY    = 0;
    double mTestRotZ    = 0;
    double mTestLookAtX = 0;
    double mTestLookAtY = 0;
    double mTestLookAtZ = 0;
    double mTestPosX    = 0;
    double mTestPosY    = 0;

    // TODO: 2016/7/21 TEST
    public void downZ() {
        mTestRotZ -= 10;
    }

    public void upZ() {
        mTestRotZ += 10;
    }

    public void downX() {
        mTestRotX -= 10;
    }

    public void upX() {
        mTestRotX += 10;
    }

    public void downY() {
        mTestRotY -= 10;
    }

    public void upY() {
        mTestRotY += 10;
    }

    public void lookAtLeft() {
        mTestLookAtX -= 0.1;
    }

    public void lookAtRight() {
        mTestLookAtX += 0.1;
    }

    public void lookAtUp() {
        mTestLookAtY += 0.1;
    }

    public void lookAtDown() {
        mTestLookAtY -= 0.1;
    }

    public void lookAtHigh() {
        mTestLookAtZ += 0.1;
    }

    public void lookAtLow() {
        mTestLookAtZ -= 0.1;
    }

    public void posLeft() {
        mTestPosX -= 0.01;
    }

    public void posRight() {
        mTestPosX += 0.01;
    }

    public void posDown() {
        mTestPosY -= 0.01;
    }

    public void posUp() {
        mTestPosY += 0.01;
    }
}
