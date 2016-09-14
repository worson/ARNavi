package com.haloai.hud.hudendpoint.arwaylib.view;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.utils.HaloLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * author       : 龙;
 * date         : 2016/7/6;
 * email        : helong@haloai.com;
 * package_name : demo.opengl.haloai.com.compassdemo;
 * project_name : ComPassDemo;
 */
public class ComPassView extends View implements SensorEventListener {
    private int WIDTH  = 0;
    private int HEIGHT = 0;

    private boolean DEBUG_MODE = false;
    private boolean DISPLAY_ARROW = false;


    private Paint mPaint = new Paint();
    private Paint mBackgroundPaint = new Paint();


    //compass draw data
    private long           mStartTime         = 0l;
    private float          mStartComPassValue = 0f;
    private ObjectAnimator mRotateAnim        = null;

    private float mCurrentRotationX = 0;
    private float mCurrentDegree = 0;

    private Bitmap mComPassOutsideRingBitmap = null;
    private Bitmap mComPassDestArrowBitmap   = null;
    private int    mDestDirection            = 0;

    private boolean mIsCutCanvas   = false;
    private int mCurrentCutDegree   = 0;
    private Path    mCanvasCutPath = null;
    private Paint   mTextPaint     = null;
    private boolean enableDirectionArrow = false;

    private List<CompassLister> mCompassListers = new ArrayList<>();

    private float BACKGROUND_CIRCLE_FATOR = 0.88f;
    private Matrix mDirectionmatrix = new Matrix();
    private Matrix mArrorMatrix = new Matrix();


    public interface CompassLister{
        public void degreeChanged(float degree);
    }

    public ComPassView(Context context) {
        super(context);
    }

    public ComPassView(Context context, int width, int height) {
        super(context);
        initSensor(context);
        initBitmap(context, width, height);
        resourceInit();
    }

    public ComPassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.CustomView, 0, 0);
        int width,height;
        width = (int)a.getDimension(R.styleable.CustomView_custom_width,240);
        height = (int)a.getDimension(R.styleable.CustomView_custom_height,240);
        HaloLogger.logE("compass_debug","ComPassView init,width is "+width+"    ,height is "+height);
        a.recycle();
        initSensor(context);
        initBitmap(context, width, height);
        resourceInit();
    }

    private void resourceInit() {
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setStrokeWidth(WIDTH*0.1f);
        enableCut(false);
        setCutDegree(60);

        mBackgroundPaint.setColor(Color.BLACK);
        mBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mCompassListers.clear();

//        enableDirectionArrow(true);
    }

    private void initBitmap(Context context, int width, int height) {
        WIDTH = width;
        HEIGHT = height;
        float scale = 1f;
        mComPassOutsideRingBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.compass_outside_ring_v3);
        Bitmap target = Bitmap.createBitmap((int)(width*scale), (int)(height*scale), mComPassOutsideRingBitmap.getConfig());
        Canvas temp_canvas = new Canvas(target);
        temp_canvas.drawBitmap(mComPassOutsideRingBitmap, null, new Rect(0, 0, target.getWidth(), target.getHeight()), null);
        mComPassOutsideRingBitmap = target;

        mComPassDestArrowBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.compass_dest_arrow2);
        target = Bitmap.createBitmap(width, height, mComPassDestArrowBitmap.getConfig());
        temp_canvas = new Canvas(target);
        temp_canvas.drawBitmap(mComPassDestArrowBitmap, null, new Rect(0, 0, target.getWidth(), target.getHeight()), null);
        mComPassDestArrowBitmap = target;

    }

    private void initSensor(Context context) {
        // 传感器管理器
        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        // 注册传感器(Sensor.TYPE_ORIENTATION(方向传感器);SENSOR_DELAY_FASTEST(0毫秒延迟);
        // SENSOR_DELAY_GAME(20,000毫秒延迟)、SENSOR_DELAY_UI(60,000毫秒延迟))
        // 如果不采用SENSOR_DELAY_FASTEST的话,在0度和360左右之间做动画会有反向转一大圈的感觉
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void addLister(CompassLister lister){
        if(mCompassListers.contains(lister)){
            return;
        }
        mCompassListers.add(lister);
    }
    public void enableCut(boolean cut){
        mIsCutCanvas = cut;
        invalidate();
    }

    public void enableDirectionArrow(boolean enableDirectionArrow) {
        this.enableDirectionArrow = enableDirectionArrow;
//        invalidate();
    }

    public void setCutDegree(int degree){
        mCurrentCutDegree =degree;
        setCanvasCut(mIsCutCanvas,-degree,degree);
    }

    private void setCanvasCut(boolean cut,int from,int to) {
        mIsCutCanvas = cut;
        updateCanvasCutPath(from,to);
        invalidate();
    }

    private void rUpdateCanvasCutPath() {
        updateCanvasCutPath(-mCurrentCutDegree,mCurrentCutDegree);
    }
    /**
     * @param from 逆时针起始角
     *
     * */
    private void updateCanvasCutPath(int from, int to) {
        PointF lu,ld,ru,rd,c;
        double dfrom =  Math.toRadians(from);
        double dto = Math.toRadians(to);

        PointF src = new PointF(WIDTH/2,0);
        c = new PointF(WIDTH/2,HEIGHT/2);

        lu = new PointF(0,0);
        ld = new PointF();
        rotation(src,c,ld,dfrom);
        rd = new PointF();
        rotation(src,c,rd,dto);
        ru = new PointF(WIDTH,0);

        if (mCanvasCutPath == null) {
            mCanvasCutPath = new Path();
        }else {
            mCanvasCutPath.reset();
        }
        PointF[] rPath = new PointF[]{lu,ld,c,rd,ru};
        PointF rotationCenter = new PointF(c.x,c.y);
        for (int i = 0; i <rPath.length ; i++) {
            PointF rp = rPath[i];
            rotation(rp,rotationCenter,rp,Math.toRadians(mCurrentDegree));
        }

        mCanvasCutPath.moveTo(lu.x,lu.y);
        PointF[] path = new PointF[]{ld,c,rd,ru};

        for (int i = 0; i <path.length ; i++) {
            PointF p = path[i];
            mCanvasCutPath.lineTo(p.x,p.y);
        }
        mCanvasCutPath.addCircle(c.x,c.y,WIDTH*0.225f, Path.Direction.CCW);

//        mCanvasCutPath.reset();
//        mCanvasCutPath.lineTo(0,0);
//        mCanvasCutPath.lineTo(0,HEIGHT);
//        mCanvasCutPath.lineTo(WIDTH,HEIGHT);
//        mCanvasCutPath.lineTo(WIDTH,0);

        mCanvasCutPath.close();
        if(DEBUG_MODE){
            float r = WIDTH*0.5f/2;
            mCanvasCutPath.addCircle(c.x,c.y,r, Path.Direction.CCW);
        }
    }

    private void rotation(PointF src,PointF ref,PointF dest,double radians){
        if (src == null || dest == null || ref == null) {
            return;
        }
        PointF rPoint = new PointF(src.x-ref.x,src.y-ref.y);
        double c=Math.cos(radians);
        double s=Math.sin(radians);
        float x=(float)(rPoint.x*c-rPoint.y*s+ref.x);
        float y=(float)(rPoint.x*s+rPoint.y*c+ref.y);
        dest.x = x;
        dest.y = y;
    }
    public static void setRotateMatrix4Canvas(float translateX,float translateY,float offsetY,float rotateXDegrees,float rotateYDegrees,float rotateZDegrees, Canvas canvas) {
        final Camera camera = new Camera();
//        @SuppressWarnings("deprecation")
//        final Matrix matrix = canvas.getMatrix();
        final Matrix matrix = new Matrix();
        // save the camera status for restore
        camera.save();
        // around X rotate N degrees
        //		camera.rotateX(50);
        //		camera.translate(0.0f, -100f, 0.0f);
        camera.rotateX(rotateXDegrees);
        camera.rotateY(rotateYDegrees);
        camera.rotateZ(rotateZDegrees);
        camera.translate(0.0f, offsetY, 0.0f);
        //x = -500 则为摄像头向右移动
        //y = 200 则为摄像头向下移动
        //z = 500 则为摄像头向高处移动
        // get the matrix from camera
        camera.getMatrix(matrix);
        // restore camera from the next time
        camera.restore();
//        matrix.preTranslate(-translateX, -translateY);
        matrix.postTranslate(translateX, translateY);
        canvas.setMatrix(matrix);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(DEBUG_MODE){
            HaloLogger.logE("compass_debug","ComPassView onDraw,canvas width is "+canvas.getWidth()+"    ,height is "+canvas.getHeight());
        }
        PointF center = new PointF(canvas.getWidth() / 2, canvas.getHeight() / 2);
        mDirectionmatrix.reset();

        float degree = -mCurrentDegree;
        mDirectionmatrix.setRotate(degree,center.x,center.y);



        float r = mComPassOutsideRingBitmap.getWidth()/2;
        canvas.drawCircle(center.x,center.y,r*BACKGROUND_CIRCLE_FATOR,mBackgroundPaint);
//        canvas.drawRect(new RectF(r*(1-bScale),center.y,r*(1+bScale),2*r),mBackgroundPaint);

        /*if(mIsCutCanvas){
            if (mCanvasCutPath != null) {
                canvas.clipPath(mCanvasCutPath, Region.Op.INTERSECT);
            }
        }*/
        canvas.drawBitmap(mComPassOutsideRingBitmap, mDirectionmatrix, null);

        if (DISPLAY_ARROW || !mIsCutCanvas){
            if(enableDirectionArrow){
                mArrorMatrix.reset();
                mArrorMatrix.setRotate(degree+mDestDirection, canvas.getWidth() / 2, canvas.getHeight() / 2);
                canvas.drawBitmap(mComPassDestArrowBitmap, mArrorMatrix, null);
            }
        }
        if (DEBUG_MODE) {
            mPaint.setColor(Color.RED);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2,canvas.getHeight()*0.03f,mPaint);
        }

    }


    public void setDestDegree(int degree) {
        mDestDirection = degree;
//        new Thread() {
//            final long DURATION = 2000;
//            long startTime = 0l;
//            int direction = mDestDirection - 50;
//
//            @Override
//            public void run() {
//                startTime = System.currentTimeMillis();
//                while (*//*System.currentTimeMillis() - startTime > DURATION && direction == mDestDirection*//*true) {
//                    mDestDirection = direction;
//                    direction += 5;
//                    SystemClock.sleep(30);
//                }
//            }
//        }.start();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            float degree = (event.values[0]-90+360)%360;
            for(CompassLister compassLister : mCompassListers){
                if (compassLister != null) {
                    compassLister.degreeChanged(degree);
                }
            }
            mCurrentDegree = degree;
            if(mIsCutCanvas){
                rUpdateCanvasCutPath();
            }
//            rUpdateRotationX();
            if(false){
                //first time
                if (mStartTime == 0) {
                    mStartTime = System.currentTimeMillis();
                    mStartComPassValue = -degree;
                } else {
                    if (mRotateAnim != null && mRotateAnim.isRunning()) {
                        mRotateAnim.pause();
                        mRotateAnim.cancel();
                    }
                    long endTime = System.currentTimeMillis();
                    mRotateAnim = ObjectAnimator.ofFloat(this, "rotation", mStartComPassValue, -degree);
                    mRotateAnim.setDuration(endTime - mStartTime);
                    mRotateAnim.setRepeatCount(0);
                    mRotateAnim.start();

                    mStartTime = endTime;
                    mStartComPassValue = -degree;
                }
            }else {
//                setRotation(-(degree));
            }
            if (DEBUG_MODE){
                HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG,"compassview onSensorChanged ,degress is "+degree);
            }
        }
        invalidate();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //精度改变
    }
}
