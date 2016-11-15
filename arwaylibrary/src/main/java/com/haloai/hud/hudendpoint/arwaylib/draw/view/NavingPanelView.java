package com.haloai.hud.hudendpoint.arwaylib.draw.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.haloai.hud.hudendpoint.arwaylib.R;

public class NavingPanelView extends View {
    private double mOutsideCircleRadius = 0;
    private int    WIDTH                = 0;
    private int    HEIGHT               = 0;
    private float  SPEED_MAX            = 260f;
    private float  SPEED_MIN            = 0f;
    private Bitmap mEmptySpeedBitmap    = null;
    private Bitmap mFullSpeedPreBitmap  = null;
    private Bitmap mFullSpeedClipBitmap = null;
    private Canvas mTempCanvas          = null;
    private float  mSpeed               = 0f;
    private Path   mPath                = new Path();
    private RectF  mSpeedRectF          = null;

    public NavingPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.CustomView, 0, 0);
        int width,height;
        width = (int)a.getDimension(R.styleable.CustomView_custom_height,240);
        height = (int)a.getDimension(R.styleable.CustomView_custom_height,240);
        a.recycle();
        init(context,width,height);
    }

    public NavingPanelView(Context context) {
        super(context);
    }

    public NavingPanelView(Context context, int width, int height) {
        super(context);
        init(context,width,height);

    }

    private void init(Context context, int width, int height){
        WIDTH = width;
        HEIGHT = height;

        //生成速度为0的底图
        mEmptySpeedBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.speed_circle_empty);
        Bitmap target = Bitmap.createBitmap(width, height, mEmptySpeedBitmap.getConfig());
        Canvas temp_canvas = new Canvas(target);
        temp_canvas.drawBitmap(mEmptySpeedBitmap, null, new Rect(0, 0, target.getWidth(), target.getHeight()), null);
        mEmptySpeedBitmap = target;

        //生成最大速度的底图
        mFullSpeedPreBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.speed_circle_full);
        target = Bitmap.createBitmap(width, height, mFullSpeedPreBitmap.getConfig());
        temp_canvas = new Canvas(target);
        temp_canvas.drawBitmap(mFullSpeedPreBitmap, null, new Rect(0, 0, target.getWidth(), target.getHeight()), null);
        mFullSpeedPreBitmap = target;

        //创建一个对最大速度进行裁剪之后的底图,最终绘制到画布上的是该图
        mFullSpeedClipBitmap = Bitmap.createBitmap(mFullSpeedPreBitmap.getWidth(), mFullSpeedPreBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        mTempCanvas = new Canvas(mFullSpeedClipBitmap);

        //该矩形对应的外切圆的半径
        mOutsideCircleRadius = Math.sqrt(Math.pow(WIDTH / 2, 2) + Math.pow(HEIGHT / 2, 2));
        //该view对应的边框矩形
        mSpeedRectF = new RectF(0, 0, WIDTH, HEIGHT);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();

        //将底图绘制到画布上
        canvas.drawBitmap(mEmptySpeedBitmap, 0, 0, null);

        //根据当前速度对最大速度图进行裁剪
        cutSpeedFullBitmap();

        //将裁剪后得到的图片绘制到画布上
        canvas.drawBitmap(mFullSpeedClipBitmap, 0, 0, null);

        canvas.restore();
    }

    private void cutSpeedFullBitmap() {
        //创建新画布(不能复用该画布,否则无法达到连续裁剪形成的动态效果)
        mTempCanvas = new Canvas(mFullSpeedClipBitmap);
        //清空画布
        mTempCanvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
        //创建用于裁剪的path(最终就是将画布中path围成的部分裁剪出来)
        mPath = new Path();
        //移动到中点
        mPath.moveTo(WIDTH / 2, HEIGHT / 2);

        //fromPercent:代表的是从整个园的哪个位置开始裁剪(0-1)
        //因为我们速表的刻度是从右下方开始的,大概处于园的0.405处
        double fromPercent = 0.405;
        float linetoX = (float) (mSpeedRectF.right / 2 + mOutsideCircleRadius * Math.sin(Math.PI * 2 * fromPercent));
        float linetoY = (float) (mSpeedRectF.bottom / 2 - mOutsideCircleRadius * Math.cos(Math.PI * 2 * fromPercent));
        //linetoX,linetoY:这两个点代表的是0.405对应的矩形外切圆上的一个点
        //之所以是lineTo到这个点是因为这个外切圆上的点肯定是处于矩形外或者矩形上的,也就是说这个点的连线肯定能把矩形
        //内的内容包起来
        mPath.lineTo(linetoX, linetoY);

        //toPercent1:代表当前速度对应下的位置(大于等于0.405,小于等于1)
        //toPercent2:如果mToPercent1大于1,toPercent2就有了值(1-toPercent1)
        //之所以会大于1,是因为我们速表的终点并不是园的顶点(也就是1的位置),而是顶点再往右一部分
        double toPercent1 = fromPercent + (mSpeed / SPEED_MAX) * 0.677;
        double toPercent2 = 0;
        if (toPercent1 > 1) {
            toPercent2 = toPercent1 - 1;
            toPercent1 = 1;
        }
//        if (mToPercent1 > 0.125f) {
//            mPath.lineTo(mSpeedRectF.right, 0);
//        }
//        if (mToPercent1 > 0.375f) {
//            mPath.lineTo(mSpeedRectF.right, mSpeedRectF.bottom);
//        }
        if (toPercent1 > 0.625f) {
            mPath.lineTo(0, mSpeedRectF.bottom);
        }
        if (toPercent1 > 0.875f) {
            mPath.lineTo(0, 0);
        }
        linetoX = (float) (mSpeedRectF.right / 2 + mOutsideCircleRadius * Math.sin(Math.PI * 2 * toPercent1));
        linetoY = (float) (mSpeedRectF.bottom / 2 - mOutsideCircleRadius * Math.cos(Math.PI * 2 * toPercent1));
        mPath.lineTo(linetoX, linetoY);

        //如果toPercent2大于0,表示速度的值对应的位置大于1,也就是超过了顶点,此时需要继续往右lineTo
        if (toPercent2 > 0) {
            if (toPercent2 > 0.125f) {
                mPath.lineTo(mSpeedRectF.right, 0);
            }
            if (toPercent2 > 0.375f) {
                mPath.lineTo(mSpeedRectF.right, mSpeedRectF.bottom);
            }
            if (toPercent2 > 0.625f) {
                mPath.lineTo(0, mSpeedRectF.bottom);
            }
            if (toPercent2 > 0.875f) {
                mPath.lineTo(0, 0);
            }
            linetoX = (float) (mSpeedRectF.right / 2 + mOutsideCircleRadius * Math.sin(Math.PI * 2 * toPercent2));
            linetoY = (float) (mSpeedRectF.bottom / 2 - mOutsideCircleRadius * Math.cos(Math.PI * 2 * toPercent2));
            mPath.lineTo(linetoX, linetoY);
        }

        mPath.close();
        //根据path对画布进行裁剪,此处采用的模式是相交,也就是保留画布上的path围起来的部分,其他部分裁剪掉
        mTempCanvas.clipPath(mPath, Region.Op.INTERSECT);
        //将最大速度底图绘制到被裁剪了的画布上
        mTempCanvas.drawBitmap(mFullSpeedPreBitmap, 0, 0, null);
    }

    /**
     * 速度值在0-250之间
     * 该方法由于涉及到更新view,因此必须运行在UI线程中
     * @param speed 0-260
     */
    public void setSpeed(float speed) {
        if (speed < SPEED_MIN) {
            speed = SPEED_MIN;
        } else if (speed > SPEED_MAX) {
            speed = SPEED_MAX;
        }
        mSpeed = speed;
        invalidate();
    }

    public float getSpeed() {
        return mSpeed;
    }
}
