package com.haloai.hud.hudendpoint.arwaylib.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.haloai.hud.hudendpoint.arwaylib.utils.DisplayUtil;

/**
 * Created by wangshengxing on 16/8/15.
 */
public class CompassOutletView extends View implements ComPassView.CompassLister{
    private boolean DEBUG_MODE     = false;
    private boolean EXTERN_SENSOR  = true;
    private Paint   mTextPaint     = new TextPaint();
    private Paint   mTestPaint     = new TextPaint();
    private int     mTextPaintSise = 10;

    private boolean mIsSloping = false;

    private float mDegree = 0;
    private final float  VIEW_SCALE_FACTOR = 0.65f;
    private static final int  DEFAULUT_TEXTPAINT_SIZE = 10;



    public CompassOutletView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initResource(context);
    }

    public CompassOutletView(Context context) {
        super(context);
        initResource(context);
    }

    private void initSensor(Context context) {
        // 传感器管理器
        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        // 注册传感器(Sensor.TYPE_ORIENTATION(方向传感器);SENSOR_DELAY_FASTEST(0毫秒延迟);
        // SENSOR_DELAY_GAME(20,000毫秒延迟)、SENSOR_DELAY_UI(60,000毫秒延迟))
        // 如果不采用SENSOR_DELAY_FASTEST的话,在0度和360左右之间做动画会有反向转一大圈的感觉
        sm.registerListener(new SensorEventListener(){

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                    float degree = (event.values[0]-90)%360;
                    updateDirection(degree);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void initResource(Context context) {
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setStyle(Paint.Style.STROKE);
        mTextPaintSise = DisplayUtil.sp2px(context,DEFAULUT_TEXTPAINT_SIZE);
        mTextPaint.setTextSize(mTextPaintSise);

        mTestPaint.setColor(Color.GREEN);
        if(!EXTERN_SENSOR){
            initSensor(context);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float scale = 1;
        initSize(canvas.getWidth()*scale,canvas.getHeight()*scale);
        rotatePoints();
        drawText(canvas);
        if(DEBUG_MODE){
            canvas.drawCircle(canvas.getWidth()/2,canvas.getHeight()/2,canvas.getHeight()*0.01f,mTestPaint);
        }
    }


    private void moveRectF(RectF rect,float offsetX,float offsetY){
        if (rect == null) {
            return;
        }
        rect.left += offsetX;
        rect.right += offsetX;
        rect.top += offsetY;
        rect.bottom += offsetY;
    }



    public void enableSloping(boolean enable){
        mIsSloping = enable;
        invalidate();
    }

    private void drawText(Canvas canvas) {
        if (canvas == null) {
            return;
        }
        Path textPath = new Path();
        Rect rect = getTextRect("东",mTextPaint);

        PointF[] points = new PointF[]{mLeftCenter,mUpCenter,mRightCenter,mBottomCenter};
        int[] degrees = new int[]{270,0,90,180};

        for (int i = 0; i < mDirectionText.length; i++) {
            PointF p = points[i];
            float degree = degrees[i]+90;
            RectF textRect = new RectF(p.x,p.y,p.x+rect.width(),p.y+rect.height());
            PointF offset = new PointF(-rect.width()/2,-rect.height()/2);
            offset.y += Math.sin(Math.toRadians(degree));
            offset.x += Math.cos(Math.toRadians(degree));
            moveRectF(textRect,offset.x,offset.y);

            textPath.reset();
            textPath.moveTo(textRect.left,textRect.bottom);
            textPath.lineTo(textRect.right,textRect.bottom);
            if (mIsSloping){
                double tdegree = (degrees[i]-mDegree+360+180)%360;
                tdegree = tdegree>180?tdegree-360:tdegree;
                double scale = (1+0.59f*Math.abs(1-tdegree/180));//Math.cos(Math.toRadians(tdegree/2))
                scale = 1.5f;
                mTextPaint.setTextSize((float)(mTextPaintSise*scale));
            }else {
                mTextPaint.setTextSize(mTextPaintSise);
            }
            canvas.drawTextOnPath(mDirectionText[i],textPath,0,0,mTextPaint);
            if(DEBUG_MODE){
                canvas.drawCircle(p.x,p.y,mTextPaintSise*0.1f,mTestPaint);
            }
        }
    }

    private PointF mLeftCenter ;
    private PointF mRightCenter ;
    private PointF mUpCenter ;
    private PointF mBottomCenter ;
    private PointF mCenter ;

    private String[] mDirectionText = new String[]{"西","北","东","南"};

    private void rotatePoints() {
        PointF[] rPath = new PointF[]{mLeftCenter,mRightCenter,mUpCenter,mBottomCenter};
        PointF rotationCenter = mCenter;
        for (int i = 0; i <rPath.length ; i++) {
            PointF rp = rPath[i];
            rotationPointF(rp,rotationCenter,rp,Math.toRadians(mDegree));
        }
    }

    private void rotationPointF(PointF src,PointF ref,PointF dest,double radians){
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

    private void initSize(float width, float height) {
        float scale = VIEW_SCALE_FACTOR;
//        float r = (float) (scale*Math.sqrt(width*width+height*height)/2);
        float r = width/2*scale;

        mCenter = new PointF(width/2,height/2);

        mLeftCenter = new PointF(mCenter.x-r,mCenter.y);
        mUpCenter = new PointF(mCenter.x,mCenter.y-r);
        mRightCenter = new PointF(mCenter.x+r,mCenter.y);
        mBottomCenter = new PointF(mCenter.y,mCenter.y+r);


        /*mLeftCenter = new PointF(0+width/2*(1-scale),height/2);
        mUpCenter = new PointF(width/2,height/2*(1-scale));
        mRightCenter = new PointF(width-width/2*(1-scale),height/2);
        mBottomCenter = new PointF(width/2,height-height/2*(1-scale));
        mCenter = new PointF(width/2,height/2);*/
    }

    public static Rect getTextRect(String content, Paint textPaint){
        Rect rect = new Rect();
        textPaint.getTextBounds(content,0,content.length(),rect);
        return rect;
    }

    public void updateDirection(float direction){
        mDegree = -direction;
        invalidate();
    }

    @Override
    public void degreeChanged(float degree) {
        if(EXTERN_SENSOR){
            updateDirection(degree);
        }
    }
}
