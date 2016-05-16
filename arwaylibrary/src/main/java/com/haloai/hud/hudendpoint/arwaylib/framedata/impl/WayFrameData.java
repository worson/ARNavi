package com.haloai.hud.hudendpoint.arwaylib.framedata.impl;

import android.graphics.AvoidXfermode;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.haloai.hud.hudendpoint.arwaylib.calculator.result.RouteResult;
import com.haloai.hud.hudendpoint.arwaylib.framedata.SuperFrameData;
import com.haloai.hud.hudendpoint.arwaylib.utils.DrawUtils;
import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.framedata.impl;
 * project_name : hudlauncher;
 */
public class WayFrameData extends SuperFrameData {
    private static final int   X               = 100;
    private static final int   Y               = 100;
    private static final int   MAGNIFIED_TIME  = 8;
    //路线错乱的容忍值当点的Y坐标大于起始点的Y坐标+TOLERATE_VALUE,代表绘制该点可能会出现错乱的情况
    private static final int   TOLERATE_VALUE  = 70;
    private static final float NOT_DRAW_TEXT_X = 50;
    private static final float NOT_DRAW_TEXT_Y = 270;

    private int IMAGE_WIDTH        = 0;
    private int IMAGE_HEIGHT       = 0;
    private int OUTSIDE_LINE_WIDTH = 0;
    private int MIDDLE_LINE_WIDTH  = 0;
    private int INSIDE_LINE_WIDTH  = 0;

    private List<Point> mTempPoints             = new ArrayList<Point>();
    //此paint可以将Route中的黑色去掉变为透明
    private Paint       mPaintBitmapColorFilter = new Paint();

    private static WayFrameData mWayFrameData = new WayFrameData();

    private WayFrameData() {
        setPosition(X, Y);

        this.mPaintBitmapColorFilter.setDither(true);
        this.mPaintBitmapColorFilter.setFilterBitmap(true);
        this.mPaintBitmapColorFilter.setARGB(0, 0, 0, 0);
        this.mPaintBitmapColorFilter.setXfermode(new AvoidXfermode(0x000000, 10, AvoidXfermode.Mode.TARGET));

    }

    public static WayFrameData getInstance() {
        return mWayFrameData;
    }

    @Override
    public void animOver() {

    }

    public void initDrawLine(int bitmap_width, int bitmap_height) {
        this.IMAGE_WIDTH = MathUtils.formatAsEvenNumber(bitmap_width);
        this.IMAGE_HEIGHT = MathUtils.formatAsEvenNumber(bitmap_height);
        this.OUTSIDE_LINE_WIDTH = MathUtils.formatAsEvenNumber(Math.round(this.IMAGE_WIDTH * 0.537f));
        this.MIDDLE_LINE_WIDTH = MathUtils.formatAsEvenNumber(Math.round(this.IMAGE_WIDTH * 0.465f));
        this.INSIDE_LINE_WIDTH = MathUtils.formatAsEvenNumber(Math.round(this.IMAGE_WIDTH * 0.453f));
    }

    public void update(RouteResult routeResult) {

        if (routeResult.mCanDraw) {
            this.mImage = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888);
            //create a mPaint and set attribute
            Canvas canvas = new Canvas(this.mImage);

            //if the location point may be a error point ,do not to draw path and to draw text to warning user.
            if (routeResult.mMayBeErrorLocation) {
                this.mPaint.reset();
                this.mPaint.setTextSize(50);
                this.mPaint.setColor(Color.WHITE);
                canvas.drawText("正在进行GPS定位，请继续行驶...", NOT_DRAW_TEXT_X, NOT_DRAW_TEXT_Y, this.mPaint);
                return;
            }

            if (routeResult.mCurrentPoints == null || routeResult.mCurrentPoints.size() <= 1) {
                return;
            }

            this.mPaint.reset();
            mPaint.setColor(Color.BLACK);
            canvas.drawPaint(mPaint);

            mPaint.setStrokeWidth(OUTSIDE_LINE_WIDTH);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.WHITE);

            float[] pointsXY = new float[routeResult.mCurrentPoints.size() * 2];
            // get the points x and y
            for (int i = 0; i < pointsXY.length; i++) {
                if (i % 2 == 0) {
                    pointsXY[i] = routeResult.mCurrentPoints.get(i / 2).x;
                } else {
                    pointsXY[i] = routeResult.mCurrentPoints.get(i / 2).y;
                }
            }

            // move to screen center , here set 1120-130,1120 is the center
            float offsetX = this.IMAGE_WIDTH / 2 - pointsXY[0];
            float offsetY = this.IMAGE_HEIGHT - pointsXY[1];
            for (int i = 0; i < pointsXY.length; i++) {
                pointsXY[i] = i % 2 == 0 ? pointsXY[i] + offsetX : pointsXY[i]
                        + offsetY;
            }

            // save this distance point to point, 2~1, 3~2 .....
            // offsetXY 中的元素是前后两个屏幕点的XY方向上的偏移量
            float[] offsetXY = new float[pointsXY.length - 2];
            for (int i = 0; i < offsetXY.length; i++) {
                offsetXY[i] = pointsXY[i + 2] - pointsXY[i];
            }

            // Magnified N times
            // start point is constant，another points will change
            for (int i = 2; i < pointsXY.length; i++) {
                pointsXY[i] = pointsXY[i - 2] + offsetXY[i - 2] * MAGNIFIED_TIME;
            }

            //remove the point in list if it may be error to draw
            Point firstPoint = new Point((int) pointsXY[0], (int) pointsXY[1]);
            mTempPoints.clear();
            mTempPoints.add(firstPoint);
            for (int i = 1; i < pointsXY.length / 2; i++) {
                Point tempPoint = new Point();
                if (pointsXY[i * 2 + 1] > firstPoint.y + TOLERATE_VALUE) {
                    //TODO 计算得到的舍弃点的补偿点的坐标在某些情况下有问题（去深圳湾的掉头时，补偿点会画到屏幕右侧）,因此此处没有使用补偿点
                    //                tempPoint.y = firstPoint.y;
                    //                tempPoint.x = (int) (tempPoint.y*(pointsXY[i*2]+pointsXY[i*2-2])/(pointsXY[i*2+1]+pointsXY[i*2-1]));
                    //                mTempPoints.add(tempPoint);
                    break;
                } else {
                    tempPoint.x = (int) pointsXY[i * 2];
                    tempPoint.y = (int) pointsXY[i * 2 + 1];
                    mTempPoints.add(tempPoint);
                }
            }

            if(mTempPoints.size()<=1){
                return;
            }

            pointsXY = new float[mTempPoints.size() * 2];
            for (int i = 0; i < mTempPoints.size(); i++) {
                pointsXY[i * 2] = mTempPoints.get(i).x;
                pointsXY[i * 2 + 1] = mTempPoints.get(i).y;
            }

            // here we must be 3D turn around first ,and rotate the path second.
            // first:3D turn around and set matrix
            DrawUtils.setRotateMatrix4Canvas(pointsXY[0], pointsXY[1], -100.0f, 50f, canvas);

            // draw the path
            Path basePath = new Path();

            basePath.moveTo(pointsXY[0], pointsXY[1]);
            for (int i = 0; i < pointsXY.length / 2 - 1; i++) {
                basePath.lineTo(pointsXY[(i + 1) * 2], pointsXY[(i + 1) * 2 + 1]);
            }

            // set corner path for right angle(直角)
            CornerPathEffect cornerPathEffect = new CornerPathEffect(20);
            mPaint.setPathEffect(cornerPathEffect);

            // draw outside line
            canvas.drawPath(basePath, mPaint);

            //draw black line
            mPaint.setStrokeWidth(MIDDLE_LINE_WIDTH);
            mPaint.setColor(Color.BLACK);
            canvas.drawPath(basePath, mPaint);

            //delete black color
            canvas.drawPaint(mPaintBitmapColorFilter);

            //draw next road name
            if (routeResult.mHasNextRoadName && routeResult.mNextRoadNamePosition != null) {
                /*Matrix matrix = new Matrix();
                canvas.setMatrix(matrix);*/
                Paint paint = new Paint();
                paint.setTextSize(100);
                paint.setStrokeWidth(10);
                paint.setColor(Color.YELLOW);
                //text position to draw next road name.
                Point text_position = routeResult.mProjection.toScreenLocation(DrawUtils.naviLatLng2LatLng(routeResult.mNextRoadNamePosition));
                text_position.x += offsetX;
                text_position.y += offsetY;
//                canvas.drawText(routeResult.mNextRoadName, text_position.x, text_position.y, paint);
//                canvas.drawCircle(mTempPoints.get(1).x,mTempPoints.get(1).y,30,paint);
//                Path textPath = new Path();
//                textPath.moveTo(mTempPoints.get(0).x,mTempPoints.get(0).y);
//                textPath.lineTo(mTempPoints.get(1).x,mTempPoints.get(1).y);
                canvas.drawTextOnPath(routeResult.mNextRoadName,basePath,0,0,paint);
            }
            mPaint.reset();
        }
    }
}
