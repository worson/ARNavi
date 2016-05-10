package com.haloai.hud.hudendpoint.arwaylib.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.NaviLatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * surfaceView用于绘制Hudway
 *
 * @author 龙
 */
public class HudwayView extends SurfaceView implements SurfaceHolder.Callback {

    private List<NaviLatLng> mPathLatLngs             = new ArrayList<NaviLatLng>();
    private List<Integer>    mCroodsInSteps           = new ArrayList<Integer>();
    private List<NaviLatLng> mTrafficLightNaviLatLngs = new ArrayList<NaviLatLng>();
    private List<Float>      mPointsDistance          = new ArrayList<Float>();
    private List<Boolean>    mIsRedLine               = new ArrayList<Boolean>();

    //correcting 校正
    private static final int CORRECTING_DISTANCE = 50;

    private Projection                   mProjection                         = null;
    private HUDWayBitmapFactory          mHudwayFactory                      = null;
    private AMapNaviLocation             mCurrentLocation                    = null;
    private AMapNaviLocation             mPrePreLocation                     = null;
    private AMapNaviLocation             mFakerCurrentLocation               = null;
    private AMapNaviLocation             mTestLocation                       = null;
    private AMapNaviLocation             mPreLocation                        = null;
    private FramesInterpolation<Integer> mCurStepRetainDistanceInterpolation = null;
    private Bitmap                       mCrossImage                         = null;
    private HudwayFlushThread            mHudwayFlushThread                  = null;
    private PointWithFloat               mRealPointInScreen                  = new PointWithFloat(0, 0);

    private boolean mCanDrawHudway         = false;
    private int     mCurrent               = -1;
    private long    mCurrentFramesCounter  = 0l;
    private long    mPreviousFramesCounter = 0l;
    private int     mCurrentIndex          = 1;
    private long    fps_time               = 30;
    private boolean mIsErrorLocation       = true;
    private int     mCurrentDistance       = 0;

    private float amapViewDegrees = 0f;
    private float selfCarDegrees  = 0f;

    //hanyu
    private int   repeatCount = 0;
    private float angleStep   = 0;
    private float preAngle    = 0;
    private float pre2Angle   = 0;
    private float angle       = 0;

    private float stepUpAngle                            = 0;
    private int   CUR_STEP_RETAIN_DISTANCE_THRESHOLD     = 300;
    private int   CUR_STEP_RETAIN_DISTANCE_THRESHOLD_MAX = 300;
    private int   CUR_STEP_RETAIN_DISTANCE_THRESHOLD_MIN = 200;
    private int   CAMERA_Z_AXIS_MAX                      = 2000;
    private int   CAMERA_Z_AXIS_MIN                      = 0;
    private int   CAMERA_ROTATEX_MAX_ANGLE               = 50;
    private int   CAMERA_ROTATEX_MIN_ANGLE               = 0;
    private int   CAMERA_ROTATEX_SET_LOOP                = 0;
    private float newAngle                               = 0f;//= 0; CAMERA_ROTATEX_MIN_ANGLE;
    private float newZAxis                               = 0f;//= 0; CAMERA_Z_AXIS_MAX;
    private float newYAxis                               = 0f;//200.0f;

    /**
     * 构造函数
     *
     * @param context
     */
    public HudwayView(Context context, Projection projection) {
        super(context);

        SurfaceHolder holder = this.getHolder();
        this.setZOrderOnTop(true);
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.addCallback(this);
        setFocusableInTouchMode(true);

        mProjection = projection;
        mHudwayFactory = new HUDWayBitmapFactory(context, mProjection);
        mCurStepRetainDistanceInterpolation = new FramesInterpolation<Integer>();

    }

    public HudwayView(Context context, AttributeSet attrs, int defStyle) {
        super(context);
    }

    public HudwayView(Context context, AttributeSet attrs) {
        super(context);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        mHudwayFactory.initDrawLine(this.getWidth(), this.getHeight());

        mHudwayFlushThread = new HudwayFlushThread(holder);
        mHudwayFlushThread.setRunning(true);
        mHudwayFlushThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHudwayFlushThread.setRunning(false);
    }

    /**
     * set cross image to draw on arway
     *
     * @param crossImage
     */
    public void setCrossImage(Bitmap crossImage) {
        mCrossImage = crossImage;
    }

    //draw thread
    class HudwayFlushThread extends Thread {
        private boolean mRunning = false;
        private SurfaceHolder surfaceHolder;
        private long   startTime = 0;
        private long   endTime   = 0;
        private Canvas can       = null;

        public HudwayFlushThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        //start and stop thread
        public void setRunning(boolean running) {
            mRunning = running;
        }

        @Override
        public void run() {
            while (mRunning) {
                can = surfaceHolder.lockCanvas(null);
                synchronized (surfaceHolder) {
                    if (can != null && mRunning && mCurrentLocation != null && mCanDrawHudway) {
                        startTime = System.currentTimeMillis();
                        doDraw(can);
                        endTime = System.currentTimeMillis();
                    } else if (can != null) {
                        can.drawColor(Color.BLACK);
                    }
                }
                if (endTime - startTime < fps_time) {
                    SystemClock.sleep(fps_time - (endTime - startTime));
                }
                if (can != null) {
                    surfaceHolder.unlockCanvasAndPost(can);
                }
            }
        }

        /**
         * 用于调用HudwayFactory的方法传入canvas和points进行绘制
         *
         * @param canvas
         */
        private void doDraw(Canvas canvas) {
            if (canvas == null) {
                return;
            }
            canvas.save();
//			repeatCount = repeatCount+1;
            mFakerCurrentLocation = getFakerLocation();
            if (mFakerCurrentLocation == null) {
                return;
            }
            /*float newInterpolationDistance = FramesInterpolation.performInterpolationAction(mCurStepRetainDistanceInterpolation);
            //float rate = mCurStepRetainDistanceInterpolation.getInterpolationRate();
            float distanceGap = newInterpolationDistance - CUR_STEP_RETAIN_DISTANCE_THRESHOLD_MIN;
            float newLinearInterpolatiorRate = distanceGap / 100f;
            //float newLinearInterpolatiorRate = ()/CUR_STEP_RETAIN_DISTANCE_THRESHOLD-CUR_STEP_RETAIN_DISTANCE_THRESHOLD_MIN;
            //Log.e("hanyu", "newLinearInterpolatiorRate: " + newLinearInterpolatiorRate);
            float CAMERA_ROTEATEX_INCREMENT = 0.1f;
            float CAMERA_Z_AXIS_INCREMENT = 4f;
            float CAMERA_Y_AXIS_INCREMENT = 0.4f;
            if (newLinearInterpolatiorRate > 0) {
                if (newLinearInterpolatiorRate < 1) {
                    //CAMERA_ROTATEX_SET_LOOP = 0;
                    newAngle = getLinearInterpolatorValue(newLinearInterpolatiorRate, CAMERA_ROTATEX_MAX_ANGLE, CAMERA_ROTATEX_MIN_ANGLE, true);
                    newZAxis = getLinearInterpolatorValue(newLinearInterpolatiorRate, CAMERA_Z_AXIS_MAX, CAMERA_Z_AXIS_MIN, false);
                    newYAxis = getDecelerateInterpolatorValue(newLinearInterpolatiorRate, 200, -100, true);
                    //
                    //Log.e("hanyu", "newLinearInterpolatiorRate: " + newLinearInterpolatiorRate + " newYAxis: "+ newYAxis+ " newZAxis: "+newZAxis+ " newAngle: "+newAngle);
                } else {
                    newAngle = newAngle + CAMERA_ROTEATEX_INCREMENT;
                    if (newAngle > CAMERA_ROTATEX_MAX_ANGLE) {
                        newAngle = CAMERA_ROTATEX_MAX_ANGLE;
                    }
                    newZAxis = newZAxis - CAMERA_Z_AXIS_INCREMENT;
                    if (newZAxis < CAMERA_Z_AXIS_MIN) {
                        newZAxis = CAMERA_Z_AXIS_MIN;
                    }
                    newYAxis = newYAxis + CAMERA_Y_AXIS_INCREMENT;
                    if (newYAxis > 200) {
                        newYAxis = 200;
                    }
                    //newAngle = CAMERA_ROTATEX_MAX_ANGLE;
                    //newZAxis = CAMERA_Z_AXIS_MIN;
                    //newYAxis = 200f;
                    //Log.e("hanyu", "> 0 ==== newLinearInterpolatiorRate: " + newLinearInterpolatiorRate + " newYAxis: "+ newYAxis+ " newZAxis: "+newZAxis+ " newAngle: "+newAngle);
                }
            } else {
                newAngle = newAngle - CAMERA_ROTEATEX_INCREMENT;
                if (newAngle < (CAMERA_ROTATEX_MIN_ANGLE)) {
                    newAngle = CAMERA_ROTATEX_MIN_ANGLE;
                }

                newZAxis = newZAxis + CAMERA_Z_AXIS_INCREMENT;
                if (newZAxis > CAMERA_Z_AXIS_MAX) {
                    newZAxis = CAMERA_Z_AXIS_MAX;
                }
                newYAxis = newYAxis - CAMERA_Y_AXIS_INCREMENT;
                if (newYAxis < -100) {
                    newYAxis = -100f;
                }
                //newAngle = CAMERA_ROTATEX_MIN_ANGLE;
                //newZAxis = CAMERA_Z_AXIS_MAX;
                //newYAxis = -100f;
                CAMERA_ROTATEX_SET_LOOP = 0;
            }*/
//			mHudwayFactory.updateCameraInfo(newAngle,newZAxis,newYAxis);
            mHudwayFactory.drawHudway(canvas, mFakerCurrentLocation, mIsErrorLocation, mCrossImage, mRealPointInScreen);
            canvas.restore();
        }
    }

    /**
     * return location for draw
     *
     * @return
     */
    private AMapNaviLocation getFakerLocation() {
        //if have not yet to return location , return null
        if (mCurrentLocation == null) {
            return null;
        }
        //if prePreLocation is null , set the value to it , and return null
        if (mPrePreLocation == null) {
            mPrePreLocation = mCurrentLocation;
            return null;
        }
        mCurrentFramesCounter++;
        //if mPreLocation is null , so this is the first step to draw
        if (mPreLocation == null) {
            mPreLocation = mCurrentLocation;
            mCurrent = 1;

            mPreviousFramesCounter = mCurrentFramesCounter;
            mCurrentFramesCounter = 0;
        } else if (mPreLocation != mCurrentLocation) {
            mPrePreLocation = mFakerCurrentLocation == null ? mPreLocation : mFakerCurrentLocation;
            mPreLocation = mCurrentLocation;
            mCurrent = 1;

            mPreviousFramesCounter = mCurrentFramesCounter;
            mCurrentFramesCounter = 0;
        }
        if (mPreviousFramesCounter != 0 && mCurrent <= mPreviousFramesCounter) {
            Point prePrePoint = mProjection.toScreenLocation(
                    naviLatLng2LatLng(mPrePreLocation.getCoord()));
            Point prePoint = mProjection.toScreenLocation(
                    naviLatLng2LatLng(mPreLocation.getCoord()));
            mRealPointInScreen.x = (prePrePoint.x + (prePoint.x - prePrePoint.x) * (1.0 * mCurrent / mPreviousFramesCounter));
            mRealPointInScreen.y = (prePrePoint.y + (prePoint.y - prePrePoint.y) * (1.0 * mCurrent / mPreviousFramesCounter));
            Point point = new Point((int) mRealPointInScreen.x, (int) mRealPointInScreen.y);
//			Point startPoint = mProjection.toScreenLocation(naviLatLng2LatLng(mPathLatLngs.get(mCurrentIndex - 1)));
//			Point endPoint = mProjection.toScreenLocation(naviLatLng2LatLng(mPathLatLngs.get(mCurrentIndex)));
//			if(!startPoint.equals(endPoint)){
//				Log.e("projection", "prePoint"+point+"");
//				Log.e("projection", "startPoint"+startPoint+"");
//				Log.e("projection", "endPoint"+endPoint+"");
//				point = PointsLines.projection(startPoint,endPoint,point);
//				Log.e("projection", "proPoint"+point+"");
//				Log.e("projection", "==============================");
//				if(!PointsLines.isPointInLine(startPoint, endPoint, point) && mCurrentIndex + 1 < mPathLatLngs.size()){
//					NaviLatLng startLatLng = mPathLatLngs.get(mCurrentIndex-1+1);
//					NaviLatLng endLatLng = mPathLatLngs.get(mCurrentIndex+1);
//					startPoint = mProjection.toScreenLocation(new LatLng(startLatLng.getLatitude(),startLatLng.getLongitude()));
//					endPoint = mProjection.toScreenLocation(new LatLng(endLatLng.getLatitude(),endLatLng.getLongitude()));
//					point = PointsLines.projection(startPoint, endPoint, point);
//					Log.e("currentIndex", "currentIndex + 1:"+(mCurrentIndex +1));
//				}
//			}
            AMapNaviLocation location = new AMapNaviLocation();

            location.setCoord(latLng2NaviLatLng(mProjection.fromScreenLocation(point)));
            mCurrent++;
            return location;
        }
        return null;
    }

    /***
     * 自定义点类
     * 使用double型来减少误差
     */
    public class PointWithFloat {
        double x;
        double y;

        public PointWithFloat(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void init() {
            this.x = 0;
            this.y = 0;
        }
    }

//	private long updateFpsTime(long time) {
//		long div = 1000;
//		long fps_time = 0;
//		for(long i=25 ; i<=35 ; i++){
//			long temp = time - time/i*i;
//			if(div > temp){
//				fps_time = i;
//				div = temp;
//			}
//		}
//		return fps_time;
//	}

    private float getDegree(int new_point_x, int new_point_y, int ori_point_x, int ori_point_y) {
        float degrees = 0f;
        if (new_point_x == ori_point_x) {
            if (new_point_y <= ori_point_y) {
                degrees = 360;
            } else {
                degrees = 180;
            }
            // if the line is horizontal
        } else if (new_point_y == ori_point_y) {
            if (new_point_x <= ori_point_x) {
                degrees = 90;
            } else {
                degrees = 270;
            }
        } else {
            // if the line is not a vertical or horizontal,we should be to
            // calculate the degrees
            // cosA = (c*c + b*b - a*a)/(2*b*c)
            // A = acos(A)/2/PI*360
            double c = Math.sqrt(Math.pow(Math.abs(ori_point_x - new_point_x),
                                          2.0) + Math.pow(Math.abs(ori_point_y - new_point_y), 2.0));
            double b = Math.abs(ori_point_y - new_point_y);
            double a = Math.abs(ori_point_x - new_point_x);
            degrees = (float) (Math.acos((c * c + b * b - a * a) / (2 * b * c))
                    / 2 / Math.PI * 360);
            if (new_point_x >= ori_point_x && new_point_y >= ori_point_y) {
                degrees += 180;
            } else if (new_point_x <= ori_point_x && new_point_y >= ori_point_y) {
                degrees = (90 - degrees) + 90;
            } else if (new_point_x <= ori_point_x && new_point_y <= ori_point_y) {
                degrees += 360;
            } else if (new_point_x >= ori_point_x && new_point_y <= ori_point_y) {
                degrees = 270 + (90 - degrees);
            }
        }
        return degrees;

//		return 360 - amapViewDegrees + selfCarDegrees;
    }

    /**
     * NaviLatLng to LatLng
     *
     * @param naviLatLng
     * @return latLng
     */
    private LatLng naviLatLng2LatLng(NaviLatLng naviLatLng) {
        return naviLatLng == null ? null : new LatLng(naviLatLng.getLatitude(),
                                                      naviLatLng.getLongitude());
    }

    /**
     * LatLng to NaviLatLng
     *
     * @param latLng
     * @return naviLatLng
     */
    private NaviLatLng latLng2NaviLatLng(LatLng latLng) {
        return latLng == null ? null : new NaviLatLng(latLng.latitude,
                                                      latLng.longitude);
    }


    private void setRouteLatLngAndScreenPoints(List<AMapNaviStep> naviStepList) {
        init();
        for (int i = 0; i < naviStepList.size(); i++) {
            mCroodsInSteps.add(naviStepList.get(i).getCoords().size());
        }

        for (AMapNaviStep aMapNaviStep : naviStepList) {
            mPathLatLngs.addAll(aMapNaviStep.getCoords());
        }

        for (int i = 0; i < mPathLatLngs.size() - 1; i++) {
            mPointsDistance.add(AMapUtils.calculateLineDistance(naviLatLng2LatLng(mPathLatLngs.get(i)), naviLatLng2LatLng(mPathLatLngs.get(i + 1))));
            if (mPointsDistance.get(i) <= 30) {
                mIsRedLine.add(true);
            } else {
                mIsRedLine.add(false);
            }
        }

        mHudwayFactory.setRouteLatLngAndScreenPoints(naviStepList);
    }

    public void setCurrentIndex(int curPoint, int curStep) {
        mCurrentIndex = 0;
        for (int i = 0; i < curStep; i++) {
            mCurrentIndex += mCroodsInSteps.get(i);
        }
        mCurrentIndex += curPoint + 1;
        if (mCurrentIndex >= mPathLatLngs.size()) {
            mCurrentIndex = mPathLatLngs.size() - 1;
        }

        mHudwayFactory.setCurrentIndex(curPoint, curStep);

    }

    private void init() {
        this.mPathLatLngs.clear();
        this.mCroodsInSteps.clear();
        this.mTrafficLightNaviLatLngs.clear();
        this.mPointsDistance.clear();
        this.mIsRedLine.clear();
        this.mCurrentIndex = 1;
        this.mCurrentDistance = 0;
        this.mRealPointInScreen.init();
    }

    public void setCurrentLocation(AMapNaviLocation location) {
//		Point point = mProjection.toScreenLocation(naviLatLng2LatLng(location.getCoord()));
//		Point startPoint = mProjection.toScreenLocation(naviLatLng2LatLng(mPathLatLngs.get(mCurrentIndex - 1)));
//		Point endPoint = mProjection.toScreenLocation(naviLatLng2LatLng(mPathLatLngs.get(mCurrentIndex)));
//		if(!startPoint.equals(endPoint)){
//			point = PointsLines.projection(startPoint, endPoint, point);
//		}
//		LatLng latLng = mProjection.fromScreenLocation(point);
//		mCurrentLocation = new AMapNaviLocation();
//		mCurrentLocation.setCoord(latLng2NaviLatLng(latLng));
        mCurrentLocation = location;
        if (mCurrentDistance < CORRECTING_DISTANCE) {
            if (mPreLocation != null) {
                //如果distance小于1m，就判定为不是车的移动而是location持续返回的误差，就不将其加入到mCurrentDistance中
                float distance = AMapUtils.calculateLineDistance(
                        naviLatLng2LatLng(mCurrentLocation.getCoord()), naviLatLng2LatLng(mPreLocation.getCoord()));
                distance = distance < 1 ? 0 : distance;
                this.mCurrentDistance += distance;
            }
            mIsErrorLocation = true;
        } else {
            mIsErrorLocation = false;
        }
    }

    public void setPath(AMapNavi aMapNavi) {
        mCurrentDistance = 0;
        mIsErrorLocation = true;
        AMapNaviPath naviPath = aMapNavi.getNaviPath();
        List<AMapNaviStep> naviStepList = naviPath.getSteps();
        setRouteLatLngAndScreenPoints(naviStepList);
    }

    /**
     * start to draw hudway
     */
    public void startDrawHudway() {
        mCanDrawHudway = true;
    }

    /**
     * stop to draw hudway
     */
    public void stopDrawHudway() {
        mCanDrawHudway = false;
    }

    //test draw line
    public Bitmap createLineTest(int width, int height, AMapNaviLocation location) {
        mTestLocation = location;
        return mHudwayFactory.createLineTest(width, height, location, amapViewDegrees);
    }

    public Bitmap createLineTest(int width, int height) {
        if (mTestLocation != null) {
            return mHudwayFactory.createLineTest(width, height, mTestLocation, amapViewDegrees);
        }
        return null;
    }

    public void setLineDegrees(float bearing) {
        amapViewDegrees = bearing;
    }

    /**
     * 根据诱导信息更新显示状态（普通模式，俯视模式OverlookingMode）
     *
     * @param iconType
     * @param curStepRetainDistance
     */
    public void updateShowType(int iconType, int curStepRetainDistance) {
        if (iconType == 2 || iconType == 3 || iconType == 6 || iconType == 7 || iconType == 8) {// in case of turn left or turn right
            //Log.e("hanyu", "turn naviIcon: " + iconType + " distance:"+curStepRetainDistance);
            if (curStepRetainDistance > CUR_STEP_RETAIN_DISTANCE_THRESHOLD) {
                mCurStepRetainDistanceInterpolation.addInterpolationItem(CUR_STEP_RETAIN_DISTANCE_THRESHOLD);
            } else {
                mCurStepRetainDistanceInterpolation.addInterpolationItem(curStepRetainDistance);
            }
        } else {
            //Log.e("hanyu", " not turn naviIcon: " + iconType + " distance:"+curStepRetainDistance);
            mCurStepRetainDistanceInterpolation.addInterpolationItem(CUR_STEP_RETAIN_DISTANCE_THRESHOLD);
        }
    }

    /**
     * 根据比例返回线性差值
     *
     * @author 大羽
     * @usage getInterpolatorValue(10/100f, 60, 10, true);//remind to keep the f flag for input_gap
     */
    private float getLinearInterpolatorValue(float input_gap, int max, int min, boolean isInCreasing) {
        if (input_gap > 1f || input_gap < 0f || max < min || min < 0)
            return 0;
        Interpolator interpolator = new LinearInterpolator();
        float interpolatorOutputValue;
        if (isInCreasing) {
            interpolatorOutputValue = interpolator.getInterpolation(input_gap);
        } else {
            interpolatorOutputValue = interpolator.getInterpolation(1 - input_gap);
        }
        return (max - min) * interpolatorOutputValue + min;
    }

    private float getDecelerateInterpolatorValue(float input_gap, int max, int min, boolean isInCreasing) {
        if (input_gap > 1f || input_gap < 0f || max < min)
            return 0;
        //Interpolator interpolator = new OvershootInterpolator()
        Interpolator interpolator = new DecelerateInterpolator(4f);
        float interpolatorOutputValue;
        if (isInCreasing) {
            interpolatorOutputValue = interpolator.getInterpolation(input_gap);
        } else {
            interpolatorOutputValue = interpolator.getInterpolation(1 - input_gap);
        }
        return (max - min) * interpolatorOutputValue + min;
    }
}

