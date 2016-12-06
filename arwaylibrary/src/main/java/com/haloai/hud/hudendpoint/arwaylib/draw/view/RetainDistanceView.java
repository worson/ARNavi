package com.haloai.hud.hudendpoint.arwaylib.draw.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;

import com.haloai.hud.hudendpoint.arwaylib.R;

/**
 * author       : 龙;
 * date         : 2016/7/6;
 * email        : helong@haloai.com;
 * package_name : demo.opengl.haloai.com.compassdemo;
 * project_name : ComPassDemo;
 */
public class RetainDistanceView extends View {
    private int            WIDTH                = 0;
    private int            HEIGHT               = 0;
    private float          mTotalDistance       = 0;
    private float          mRetainDistance      = 0;
    private double         mOutsideCircleRadius = 0;
    private RectF          mSpeedRectF          = null;
    private Bitmap         mOutsideBitmap       = null;
    private Bitmap         mInsideBitmap        = null;
    private Bitmap         mPointBitmap         = null;
    private Bitmap         mInsideClipBitmap    = null;
    private float          mCurrentDistance     = 0;
    private ObjectAnimator mLocalAnim           = null;

    public RetainDistanceView(Context context) {
        super(context);
    }

    public RetainDistanceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.CustomView, 0, 0);
        int width,height;
        width = (int)a.getDimension(R.styleable.CustomView_custom_height,240);
        height = (int)a.getDimension(R.styleable.CustomView_custom_height,240);
        a.recycle();
        init(context,width,height);
    }

    public RetainDistanceView(Context context, int width, int height) {
        super(context);
        init(context,width,height);
    }
    private void  init(Context context, int width, int height) {

        WIDTH = width;
        HEIGHT = height;

        mOutsideBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.distance_outside_ring);
        Bitmap target = Bitmap.createBitmap(width, height, mOutsideBitmap.getConfig());
        Canvas temp_canvas = new Canvas(target);
        temp_canvas.drawBitmap(mOutsideBitmap, null, new Rect(0, 0, target.getWidth(), target.getHeight()), null);
        mOutsideBitmap = target;

        mInsideBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.distance_inside_ring);
        target = Bitmap.createBitmap(width, height, mInsideBitmap.getConfig());
        temp_canvas = new Canvas(target);
        temp_canvas.drawBitmap(mInsideBitmap, null, new Rect(0, 0, target.getWidth(), target.getHeight()), null);
        mInsideBitmap = target;

        mInsideClipBitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        mSpeedRectF = new RectF(0, 0, WIDTH, HEIGHT);
        mOutsideCircleRadius = Math.sqrt(Math.pow(WIDTH / 2, 2) + Math.pow(HEIGHT / 2, 2));

        mPointBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.distance_point);
        target = Bitmap.createBitmap(width / 7, height / 7, mPointBitmap.getConfig());
        temp_canvas = new Canvas(target);
        temp_canvas.drawBitmap(mPointBitmap, null, new Rect(0, 0, target.getWidth(), target.getHeight()), null);
        mPointBitmap = target;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();

        canvas.drawBitmap(mOutsideBitmap, 0, 0, null);

        cutBitmap();

        canvas.drawBitmap(mInsideClipBitmap, 0, 0, null);

        PointF position = new PointF();
        getPointPosition(mRetainDistance, mTotalDistance, position);

        canvas.drawBitmap(mPointBitmap, position.x, position.y, null);

        canvas.restore();
    }

    private void getPointPosition(float retainDistance, float totalDistance, PointF position) {
        /**
         * 圆点坐标：(x0,y0)
         半径：r
         角度：a0
         则圆上任一点为：（x1,y1）
         x1   =   x0   +   r   *   cos(degree   *   3.14   /180   )
         y1   =   y0   +   r   *   sin(degree   *   3.14   /180   )
         */
        double radius = WIDTH / 2 -  WIDTH*0.136f;
        double percent = 1.0 * retainDistance / totalDistance;
        if (percent * 0.73 <= 0.61) {
            percent = 0.61 - percent * 0.73;
        } else {
            percent = 1 - (percent * 0.73 - 0.61);
        }
        double degree = percent * 360 - 87;
        float x = (float) ((WIDTH / 2) + radius * Math.cos(Math.PI * degree / 180));
        float y = (float) ((HEIGHT / 2) + radius * Math.sin(Math.PI * degree / 180));
        //注意在获取绘制点的x和y时,将点本身的宽高考虑进去
        position.x = x - mPointBitmap.getWidth() / 2;
        position.y = y - mPointBitmap.getHeight() / 2;
    }

    private void cutBitmap() {
        Canvas canvas = new Canvas(mInsideClipBitmap);
        canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
        Path path = new Path();
        path.moveTo(WIDTH / 2, HEIGHT / 2);

        double fromPercent = 0.61;
        float linetoX = (float) (mSpeedRectF.right / 2 + mOutsideCircleRadius * Math.sin(Math.PI * 2 * fromPercent));
        float linetoY = (float) (mSpeedRectF.bottom / 2 - mOutsideCircleRadius * Math.cos(Math.PI * 2 * fromPercent));
        path.lineTo(linetoX, linetoY);

        //0.81包括一个0上方左侧超过顶点的部分
        double toPercent1 = (mRetainDistance / mTotalDistance) * 0.73;
        double toPercent2 = 0;
        if (toPercent1 > fromPercent) {
            toPercent2 = toPercent1 - fromPercent;
            toPercent1 = fromPercent;
        }
        toPercent1 = fromPercent - toPercent1;

        if (toPercent1 < 0.125f) {
            path.lineTo(mSpeedRectF.right, mSpeedRectF.bottom);
            path.lineTo(mSpeedRectF.right, 0);
        } else if (toPercent1 < 0.375f) {
            path.lineTo(mSpeedRectF.right, mSpeedRectF.bottom);
        }

        linetoX = (float) (mSpeedRectF.right / 2 + mOutsideCircleRadius * Math.sin(Math.PI * 2 * (toPercent1)));
        linetoY = (float) (mSpeedRectF.bottom / 2 - mOutsideCircleRadius * Math.cos(Math.PI * 2 * (toPercent1)));
        path.lineTo(linetoX, linetoY);

        if (toPercent2 > 0) {
            //if (toPercent2 > 0.125f) {
            //    path.lineTo(mSpeedRectF.right, 0);
            //}
            //if (toPercent2 > 0.375f) {
            //    path.lineTo(mSpeedRectF.right, mSpeedRectF.bottom);
            //}
            //if (toPercent2 > 0.625f) {
            //    path.lineTo(0, mSpeedRectF.bottom);
            //}
            //if (toPercent2 > 0.875f) {
            //    path.lineTo(0, 0);
            //}
            toPercent2 = 1 - toPercent2;
            linetoX = (float) (mSpeedRectF.right / 2 + mOutsideCircleRadius * Math.sin(Math.PI * 2 * toPercent2));
            linetoY = (float) (mSpeedRectF.bottom / 2 - mOutsideCircleRadius * Math.cos(Math.PI * 2 * toPercent2));
            path.lineTo(linetoX, linetoY);
        }

        path.close();
        canvas.clipPath(path, Region.Op.INTERSECT);
        canvas.drawBitmap(mInsideBitmap, 0, 0, null);
    }

    public void setTotalDistance(float totalDistance) {
        mTotalDistance = totalDistance;
    }

    public void setRetainDistance(float retainDistance) {
        if (retainDistance < 0) {
            retainDistance = 0;
        } else if (retainDistance > mTotalDistance) {
            retainDistance = mTotalDistance;
        }
        mRetainDistance = retainDistance;
        invalidate();
    }

    public void setRetainDistanceAnim(float retainDistance) {
        if (mLocalAnim != null && mLocalAnim.isRunning()) {
            mLocalAnim.pause();
            mLocalAnim.cancel();
        }
        if (retainDistance != mCurrentDistance){

            ObjectAnimator.ofFloat(this, "RetainDistance", mCurrentDistance, retainDistance);
            mLocalAnim.setDuration(500);
            mLocalAnim.setRepeatCount(0);
            mLocalAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Float f= (Float) animation.getAnimatedValue();
                    if (f != null) {
                        mCurrentDistance = f.floatValue();
                    }
                }
            });
            mLocalAnim.start();
        }

    }

}
