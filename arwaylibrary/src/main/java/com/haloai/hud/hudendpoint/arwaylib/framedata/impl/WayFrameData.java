package com.haloai.hud.hudendpoint.arwaylib.framedata.impl;

import android.graphics.AvoidXfermode;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;

import com.haloai.hud.hudendpoint.arwaylib.bean.impl.RouteBean;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.RouteResult;
import com.haloai.hud.hudendpoint.arwaylib.framedata.SuperFrameData;
import com.haloai.hud.hudendpoint.arwaylib.utils.DrawUtils;
import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;
import com.haloai.hud.utils.HaloLogger;

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
    private static final int    X                  = 100;
    private static final int    Y                  = 100;
    private static final int    MAGNIFIED_TIME     = 8;
    //路线错乱的容忍值当点的Y坐标大于起始点的Y坐标+TOLERATE_VALUE,代表绘制该点可能会出现错乱的情况
    private static final int    TOLERATE_VALUE     = 70;
    private static final float  NOT_DRAW_TEXT_X    = 50;
    private static final float  NOT_DRAW_TEXT_Y    = 270;
    private static final float  CIRCLE_INTERVAL    = 1000f;
    private static final float  CIRCLE_RADIUS      = 20f;
    //在一个点的左右两侧多少距离生成两个点与当前点组成一个贝塞尔曲线
    private static final double ADD_POINT_INTERVAL = 100f;

    private int IMAGE_WIDTH        = 0;
    private int IMAGE_HEIGHT       = 0;
    private int OUTSIDE_LINE_WIDTH = 0;
    private int MIDDLE_LINE_WIDTH  = 0;
    private int INSIDE_LINE_WIDTH  = 0;

    private List<Point> mTempPoints             = new ArrayList<Point>();
    private List<Point> mOffsetPoints           = new ArrayList<Point>();
    //此paint可以将Route中的黑色去掉变为透明
    private Paint       mPaintBitmapColorFilter = new Paint();
    private Rect        mSrcRect                = new Rect();
    private Rect        mDestRect               = new Rect();

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

            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setAntiAlias(true);

            float offsetHeight = routeResult.mProjection.toScreenLocation(DrawUtils.naviLatLng2LatLng(routeResult.mFakeLocation.getCoord())).y -
                    routeResult.mCurrentPoints.get(0).y;
            float offsetWidth = routeResult.mProjection.toScreenLocation(DrawUtils.naviLatLng2LatLng(routeResult.mFakeLocation.getCoord())).x -
                    routeResult.mCurrentPoints.get(0).x;
            for (int i = 1; i < routeResult.mCurrentPoints.size(); i++) {
                routeResult.mCurrentPoints.get(i).x -= offsetWidth;
                routeResult.mCurrentPoints.get(i).y -= offsetHeight;
            }

            // move to screen center
            float offsetX = this.IMAGE_WIDTH / 2 - routeResult.mCurrentPoints.get(0).x;
            float offsetY = this.IMAGE_HEIGHT - routeResult.mCurrentPoints.get(0).y;
            for (int i = 0; i < routeResult.mCurrentPoints.size(); i++) {
                Point temp_point = routeResult.mCurrentPoints.get(i);
                temp_point.x += offsetX;
                temp_point.y += offsetY;
            }

            // get offset with current and next point.
            mOffsetPoints.clear();
            for (int i = 0; i < routeResult.mCurrentPoints.size() - 1; i++) {
                Point offset_point = new Point();
                offset_point.x = routeResult.mCurrentPoints.get(i + 1).x - routeResult.mCurrentPoints.get(i).x;
                offset_point.y = routeResult.mCurrentPoints.get(i + 1).y - routeResult.mCurrentPoints.get(i).y;
                mOffsetPoints.add(offset_point);
            }

            // Magnified N times
            // start point is constant，another points will change
            for (int i = 1; i < routeResult.mCurrentPoints.size(); i++) {
                Point temp_point = routeResult.mCurrentPoints.get(i);
                Point pre_point = routeResult.mCurrentPoints.get(i - 1);
                Point offset_point = mOffsetPoints.get(i - 1);
                temp_point.x = pre_point.x + offset_point.x * MAGNIFIED_TIME;
                temp_point.y = pre_point.y + offset_point.y * MAGNIFIED_TIME;
            }

            //remove the point in list if it may be error to draw
            Point firstPoint = routeResult.mCurrentPoints.get(0);
            mTempPoints.clear();
            mTempPoints.add(firstPoint);
            Point tempPoint = null;
            for (int i = 1; i < routeResult.mCurrentPoints.size(); i++) {
                tempPoint = routeResult.mCurrentPoints.get(i);
                if (tempPoint.y > firstPoint.y + TOLERATE_VALUE) {
                    //TODO 计算得到的舍弃点的补偿点的坐标在某些情况下有问题（去深圳湾的掉头时，补偿点会画到屏幕右侧）,因此此处暂时没有使用补偿点
                    //                tempPoint.y = firstPoint.y;
                    //                tempPoint.x = (int) (tempPoint.y*(pointsXY[i*2]+pointsXY[i*2-2])/(pointsXY[i*2+1]+pointsXY[i*2-1]));
                    //                mTempPoints.add(tempPoint);
                    break;
                } else {
                    mTempPoints.add(tempPoint);
                }
            }

            if (mTempPoints.size() <= 1) {
                return;
            }

            // here we must be 3D turn around first ,and rotate the path second(Now we do not to rotate).
            // first:3D turn around and set matrix
            Point first_point = mTempPoints.get(0);
            DrawUtils.setRotateMatrix4Canvas(first_point.x, first_point.y, -100f, 50f, canvas);


            //竖直情况下,纵向移动量(offsetHeight -- px)
            /*float offsetDiv = (1 -
                    AMapUtils.calculateLineDistance(DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(1)),
                                                    DrawUtils.naviLatLng2LatLng(routeResult.mFakeLocation.getCoord()))
                            /
                            AMapUtils.calculateLineDistance(DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(1)),
                                                            DrawUtils.naviLatLng2LatLng(routeResult.mPrePreLocation.getCoord()))
            );*/

            //            float offsetHeight = (offsetDiv/2 * (mTempPoints.get(1).y - mTempPoints.get(0).y));
            //                                                                               DrawUtils.naviLatLng2LatLng(routeResult.mFakeLocation.getCoord())));

            // draw the path(outside,inside,circle)
            Path basePath = new Path();
            basePath.moveTo(first_point.x, first_point.y);
            Point test_point = null;
            for (int i = 1; i < mTempPoints.size(); i++) {
                //例如刚开始时从p1,到p2,到p3
                Point temp_point = mTempPoints.get(i);
                //move
                //                temp_point.y -= offsetHeight;
                /*if(temp_point.y >= first_point.y + TOLERATE_VALUE){
                    break;
                }
                if (i != mTempPoints.size() - 1) {
                    //不是最后一个点,创建该点左右两个点作为lineTo点,当前点作为queato的控制点
                    //TODO 关键在于以下两个点如何获取,通过当前点和其前一个后一个三个点
                    Point pre_point = null;
                    if(test_point!=null){
                        pre_point = test_point;
                        test_point = null;
                    }else {
                        if (i != 1) {
                            pre_point = mTempPoints.get(i - 1);
                        } else {
                            pre_point = first_point;
                        }
                    }
                    Point next_point = mTempPoints.get(i + 1);
                    Point left_point = new Point();
                    Point right_point = new Point();
                    double pre_temp_distance = MathUtils.calculateDistance(pre_point, temp_point);
                    if (pre_temp_distance <= ADD_POINT_INTERVAL) {
                        left_point = pre_point;
                    } else {
                        double div = ADD_POINT_INTERVAL / pre_temp_distance;
                        left_point.x = (int) (temp_point.x + div * (pre_point.x - temp_point.x));
                        left_point.y = (int) (temp_point.y + div * (pre_point.y - temp_point.y));
                    }
                    double next_temp_distance = MathUtils.calculateDistance(next_point, temp_point);
                    if (next_temp_distance <= ADD_POINT_INTERVAL) {
                        right_point = next_point;
                    } else {
                        double div = ADD_POINT_INTERVAL / next_temp_distance;
                        right_point.x = (int) (temp_point.x + div * (next_point.x - temp_point.x));
                        right_point.y = (int) (temp_point.y + div * (next_point.y - temp_point.y));
                    }
                    basePath.lineTo(left_point.x, left_point.y);
                    basePath.quadTo(temp_point.x, temp_point.y, right_point.x, right_point.y);
                    //right_point == next_point or right_point != next_point
                    if(right_point == next_point){
                        //如果当前贝塞尔曲线的尾点就是下一个点,那么直接跳过下一个点即可.
                        i++;
                    }else{
                        //如果当前贝塞尔曲线的尾点还没到下一个点的位置,此时下一个点的贝塞尔绘制不能简单的执行
                        //而是受限于right点.
                        test_point = right_point;
                    }
                } else {
                    //到达最后一个点
                    basePath.lineTo(temp_point.x, temp_point.y);
                }*/
                basePath.lineTo(temp_point.x, temp_point.y);
            }

            HaloLogger.logE("points_temp:", mTempPoints + "");

            Path circlePath = new Path();
            //上两个点之间间隔的剩余值,如果小于circle_interval,那么就是该值,如果大于,就是与circle_interval的差值
            float offsetDistance = 0f;
            //不使用最后一个点,使用一个点代替最后一个点
            int tempPointsSize = mTempPoints.size();
            //value为最后两个点之间的距离
            Point last_pre_point = mTempPoints.get(tempPointsSize - 2);
            Point last_point = mTempPoints.get(tempPointsSize - 1);
            double value = MathUtils.calculateDistance(last_point, last_pre_point);
            Point remove_point = null;
            Point add_point = null;
            if (value % CIRCLE_INTERVAL == 0) {
                //1.最后两个点之间的距离是circle_interval的整数倍,则不需要处理
            } else {
                //2.如果不是整数倍:
                value = value / CIRCLE_INTERVAL;
                if (value >= 1.0) {
                    //如果大于1.0,则在N.0处取一个点;
                    double decimal = value - (int) (value);
                    double scala = (value - decimal) / value;
                    float distX = last_pre_point.x - last_point.x;
                    float distY = last_pre_point.y - last_point.y;
                    add_point = new Point((int) (last_pre_point.x + distX * scala), (int) (last_pre_point.y + distY * scala));
                } else {
                    //如果小于1.0,则舍弃该点
                    remove_point = mTempPoints.remove(tempPointsSize - 1);
                }
            }

            for (int i = mTempPoints.size() - 2; i >= 1; i--) {
                Point curPoint = mTempPoints.get(i);
                Point targetPoint = mTempPoints.get(i - 1);
                float distance = (float) MathUtils.calculateDistance(curPoint, targetPoint);
                if (distance + offsetDistance >= CIRCLE_INTERVAL) {
                    float scala = (CIRCLE_INTERVAL - offsetDistance) / distance;
                    float distX = targetPoint.x - curPoint.x;
                    float distY = targetPoint.y - curPoint.y;
                    float circle_x = curPoint.x + (distX * scala);
                    float circle_y = curPoint.y + (distY * scala);
                    circlePath.addCircle(circle_x, circle_y, CIRCLE_RADIUS, Path.Direction.CW);
                    offsetDistance += distance - CIRCLE_INTERVAL;

                    while (offsetDistance >= CIRCLE_INTERVAL) {
                        scala = CIRCLE_INTERVAL / offsetDistance;
                        distX = targetPoint.x - circle_x;
                        distY = targetPoint.y - circle_y;
                        circle_x += distX * scala;
                        circle_y += distY * scala;
                        circlePath.addCircle(circle_x, circle_y, CIRCLE_RADIUS, Path.Direction.CW);

                        offsetDistance -= CIRCLE_INTERVAL;
                    }
                } else {
                    offsetDistance += distance;
                }
            }
            if (add_point != null) {
                mTempPoints.remove(add_point);
            }
            if (remove_point != null) {
                mTempPoints.add(remove_point);
            }

            // set corner path for right angle(直角) 300-平滑程度
            CornerPathEffect cornerPathEffect = new CornerPathEffect(300);
            mPaint.setPathEffect(cornerPathEffect);

            // draw outside line
            mPaint.setStrokeWidth(OUTSIDE_LINE_WIDTH);
            mPaint.setColor(Color.WHITE);
            canvas.drawPath(basePath, mPaint);

            // draw inside line
            mPaint.setStrokeWidth(MIDDLE_LINE_WIDTH);
            mPaint.setColor(Color.BLACK);
            canvas.drawPath(basePath, mPaint);

            // draw white circle path
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.WHITE);
            canvas.drawPath(circlePath, mPaint);

            //delete black color background
            canvas.drawPaint(mPaintBitmapColorFilter);

            //draw next road name
            Matrix final_matrix = canvas.getMatrix();
            int nextRoadIndex = -1;
            for (int i = 0; i < mTempPoints.size() - 1 && i < routeResult.mCurrentLatLngs.size() - 1; i++) {
                //TODO index out of bound ,because mCurrentLatLngs.size is less one than mCurrentPoints.size.
                if (routeResult.mCurrentLatLngs.get(i).equals(routeResult.mNextRoadNamePosition)) {
                    nextRoadIndex = i;
                    break;
                }
            }
            if (routeResult.mHasNextRoadName && routeResult.mNextRoadNamePosition != null && nextRoadIndex != -1) {
                Paint paint = new Paint();
                paint.setColor(Color.rgb(0x00, 0x7a, 0xff));
                Point nextRoadPoint1 = mTempPoints.get(nextRoadIndex);
                Point nextRoadPoint2 = mTempPoints.get(nextRoadIndex + 1);
                double distance_12 = MathUtils.calculateDistance(nextRoadPoint1, nextRoadPoint2);
                float dist_x = nextRoadPoint2.x - nextRoadPoint1.x;
                float dist_y = nextRoadPoint2.y - nextRoadPoint1.y;
                nextRoadPoint2.x = (int) (1500 / distance_12 * dist_x + nextRoadPoint1.x);
                nextRoadPoint2.y = (int) (1500 / distance_12 * dist_y + nextRoadPoint1.y);

                Matrix init_matrix = new Matrix();
                canvas.setMatrix(init_matrix);
                float[] xy_1 = new float[2];
                float[] xy_2 = new float[2];
                //-500将文字向上移动
                final_matrix.mapPoints(xy_1, new float[]{nextRoadPoint1.x, nextRoadPoint1.y - 500});
                final_matrix.mapPoints(xy_2, new float[]{nextRoadPoint2.x, nextRoadPoint2.y - 500});
                Path text_path = new Path();
                //if text order is horizontal
                if (Math.abs(xy_1[0] - xy_2[0]) > Math.abs(xy_1[1] - xy_2[1])) {
                    if (xy_1[0] >= xy_2[0]) {
                        text_path.moveTo(xy_2[0], xy_2[1]);
                        text_path.lineTo(xy_1[0], xy_1[1]);
                    } else {
                        text_path.moveTo(xy_1[0], xy_1[1]);
                        text_path.lineTo(xy_2[0], xy_2[1]);
                    }
                    //if text order is vertical(竖向还没有很好的方法可以显示的比较好看)
                } /*else {
                    if (xy_1[1] >= xy_2[1]) {
                        text_path.moveTo(xy_1[0], xy_1[1]);
                        text_path.lineTo(xy_2[0], xy_2[1]);
                    } else {
                        text_path.moveTo(xy_2[0], xy_2[1]);
                        text_path.lineTo(xy_1[0], xy_1[1]);
                    }
                }*/
                paint.setTextSize(15 + (xy_1[1] / 470 * 80));
                String road_name = "";
                if (routeResult.mNextRoadType == RouteBean.NextRoadType.LEFT) {
                    road_name = "<< " + routeResult.mNextRoadName;
                } else if (routeResult.mNextRoadType == RouteBean.NextRoadType.RIGHT) {
                    road_name = routeResult.mNextRoadName + " >>";
                } else {
                    road_name = "∩ " + routeResult.mNextRoadName;
                }
                canvas.drawTextOnPath(road_name, text_path, 0, 0, paint);
            }

            mPaint.reset();

            /*//update src rect and dest rect
            //TODO 这部分代码有问题
            Point startPoint = new Point(mTempPoints.get(0).x, mTempPoints.get(0).y);
            float[] startXY = new float[2];
            final_matrix.mapPoints(startXY, new float[]{startPoint.x, startPoint.y});
            startPoint.x = (int) startXY[0];
            startPoint.y = (int) startXY[1];

            Point secondPoint = new Point(mTempPoints.get(1).x, mTempPoints.get(1).y);
            float[] secondXY = new float[2];
            final_matrix.mapPoints(secondXY, new float[]{secondPoint.x, secondPoint.y});
            secondPoint.x = (int) secondXY[0];
            secondPoint.y = (int) secondXY[1];

            float offsetDiv = (1 -
                    AMapUtils.calculateLineDistance(DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(1)),
                                                    DrawUtils.naviLatLng2LatLng(routeResult.mFakeLocation.getCoord()))
                            /
                            AMapUtils.calculateLineDistance(DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(1)),
                                                            DrawUtils.naviLatLng2LatLng(routeResult.mPrePreLocation.getCoord()))
            );
            int fakerX = mTempPoints.get(0).x + (int) (offsetDiv * (mTempPoints.get(1).x - mTempPoints.get(0).x));
            int fakerY = mTempPoints.get(0).y + (int) (offsetDiv * (mTempPoints.get(1).y - mTempPoints.get(0).y));
            float[] fakerXY = new float[2];
            final_matrix.mapPoints(fakerXY,new float[]{fakerX,fakerY});
            Point fakerPoint = new Point((int)fakerXY[0],(int)fakerXY[1]);

            int offsetHeight = fakerPoint.y - startPoint.y;
            mSrcRect.set(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT + offsetHeight);
            mDestRect.set(0, 0 - offsetHeight, IMAGE_WIDTH, IMAGE_HEIGHT);
            HaloLogger.logE("src_des_rect:", "distance:" + (secondPoint.y - startPoint.y));
            HaloLogger.logE("src_des_rect:", "src:" + 0 + "," + 0 + "," + IMAGE_WIDTH + "," + (IMAGE_HEIGHT + offsetHeight));
            HaloLogger.logE("src_des_rect:", "des:" + 0 + "," + (0 - offsetHeight) + "," + IMAGE_WIDTH + "," + IMAGE_HEIGHT);
            HaloLogger.logE("src_des_rect:", "fake_dist:" + (AMapUtils.calculateLineDistance(DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(1)),
                                                                                             DrawUtils.naviLatLng2LatLng(routeResult.mFakeLocation.getCoord()))));
            HaloLogger.logE("src_des_rect:", "start_dist:" + (AMapUtils.calculateLineDistance(DrawUtils.naviLatLng2LatLng(routeResult.mCurrentLatLngs.get(1)),
                                                                                              DrawUtils.naviLatLng2LatLng(routeResult.mPrePreLocation.getCoord()))));
            */
        }
    }

    public Rect getSrcRect() {
        return mSrcRect;
    }

    public Rect getDestRect() {
        return mDestRect;
    }
}
