package com.haloai.hud.hudendpoint.arwaylib.test;

import android.content.Context;
import android.graphics.AvoidXfermode;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.NaviLatLng;
import com.haloai.hud.utils.HaloLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * 根据传入的坐标集合绘制HUDWay路线 通过当前位置实时更新路线
 *
 * @author 龙
 */
public class HUDWayBitmapFactory {

    private final List<NaviLatLng> mPathLatLngs             = new ArrayList<NaviLatLng>();
    private final List<Integer>    mCroodsInSteps           = new ArrayList<Integer>();
    private final List<NaviLatLng> mTrafficLightNaviLatLngs = new ArrayList<NaviLatLng>();
    private final List<Float>      mPointsDistance          = new ArrayList<Float>();
    private final List<Boolean>    mIsRedLine               = new ArrayList<Boolean>();
    private final List<Point>      mRightSidePoints         = new ArrayList<Point>();
    private final List<Point>      mLeftSidePoints          = new ArrayList<Point>();
    private final List<Point>      mTempPoints              = new ArrayList<Point>();

    //计算当前点到集合中的currentIndex间的距离
    private static final int   HUDWAY_LENGTH_IN_SCREEN = 350;
    //参照物点之间的距离
    private static final int   HUDWAY_POINT_INTERVAL   = 40;
    //路线的放大倍数
    private static final float MAGNIFIED_TIME          = 6;
    //路线错乱的容忍值当点的Y坐标大于起始点的Y坐标+TOLERATE_VALUE,代表绘制该点可能会出现错乱的情况
    private static final int   TOLERATE_VALUE          = 70;

    private int BITMAP_WIDTH       = 0;//475;
    private int BITMAP_HEIGHT      = 0;//270 + 80;
    private int OUTSIDE_LINE_WIDTH = 0;//350;
    private int MIDDLE_LINE_WIDTH  = 0;//330;
    private int INSIDE_LINE_WIDTH  = 0;//310;
    private int CIRCLE_LINE_WIDTH  = 0;//45;

    private int              mCurrentIndex             = 1;
    private float            mCurPoint2NextPointDist   = 0f;
    private Projection       mProjection               = null;
    private Context          mContext                  = null;
    private Path             mRectPath                 = new Path();
    private Paint            mPaint4CrossImageCanvas   = new Paint();
    private Paint            mPaintFilterBitmapColor   = new Paint();
    private Bitmap           mCrossImage               = null;
    private Bitmap           mCrossImageTarget         = null;
    private AMapNaviLocation mCrossImageLastLatLng     = null;
    private int              mCrossImageRetainDistance = 0;
    //已经走过的距离(距离初始路口放大图)
    private float            mCrossImageDist           = 0f;

    public HUDWayBitmapFactory(Context context, Projection projection) {
        this.mContext = context;
        this.mProjection = projection;
        this.mPaint4CrossImageCanvas.setAlpha(100);
        this.mPaint4CrossImageCanvas.setAntiAlias(true);
        this.mPaintFilterBitmapColor.setDither(true);
        this.mPaintFilterBitmapColor.setFilterBitmap(true);
        this.mPaintFilterBitmapColor.setARGB(0, 0, 0, 0);
        this.mPaintFilterBitmapColor.setXfermode(new AvoidXfermode(0x000000, 10, AvoidXfermode.Mode.TARGET));
    }

    /**
     * init line width with bitmap_width and bitmap_height
     *
     * @param bitmap_width  bitmap`s width
     * @param bitmap_height bitmap`s height
     */
    public void initDrawLine(int bitmap_width, int bitmap_height) {
        this.BITMAP_WIDTH = formatAsEvenNumber(bitmap_width);
        this.BITMAP_HEIGHT = formatAsEvenNumber(bitmap_height);
        this.OUTSIDE_LINE_WIDTH = formatAsEvenNumber(Math.round(this.BITMAP_WIDTH * 0.537f));// 350/475
        this.MIDDLE_LINE_WIDTH = formatAsEvenNumber(Math.round(this.BITMAP_WIDTH * 0.465f));//0.495f 330/475
        this.INSIDE_LINE_WIDTH = formatAsEvenNumber(Math.round(this.BITMAP_WIDTH * 0.453f));// 310/474
        this.CIRCLE_LINE_WIDTH = formatAsEvenNumber(Math.round(this.BITMAP_WIDTH * 0.015f)) + 2;// 45/475
    }

    private int formatAsEvenNumber(int number) {
        if (IsOddNumber(number)) {
            return number - 1;
        } else {
            return number;
        }
    }

    private boolean IsOddNumber(int n) {
        return n % 2 != 0;
    }

    /**
     * set the lat lng list for route and screen points
     *
     * @param naviStepList
     */
    public void setRouteLatLngAndScreenPoints(List<AMapNaviStep> naviStepList) {
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
    }

    /**
     * initialization status
     */
    private void init() {
        this.mPathLatLngs.clear();
        this.mCroodsInSteps.clear();
        this.mTrafficLightNaviLatLngs.clear();
        this.mPointsDistance.clear();
        this.mIsRedLine.clear();
        this.mRightSidePoints.clear();
        this.mLeftSidePoints.clear();
        this.mTempPoints.clear();
        this.mRectPath.reset();
        this.mCurrentIndex = 1;
        this.mCrossImageLastLatLng = null;
        this.mCrossImageRetainDistance = 0;
        this.mCrossImageDist = 0;
    }

    /**
     * draw hudway in the canvas
     *
     * @param can                the canvas for draw
     * @param location           the current location for car
     * @param mayBeErrorLocation true:the location may be error location , so do not to draw hudway . false:can draw hudway
     * @param crossImage         cross image to draw
     * @param realStartPoint     real point in screen without error
     */
    public void drawHudway(Canvas can, AMapNaviLocation location, boolean mayBeErrorLocation,
                           Bitmap crossImage, int retainDistance, HudwayView.PointWithDouble realStartPoint) {

        //if location is null.
        if (location == null) {
            return;
        }

        // get the points for draw bitmap
        List<Point> currentPoints = getCurrentPoints(location);
        // The first point in list may be have some little error,so we could not use it.
        // We should be use realStartPoint as first point.
        currentPoints.remove(0);
        currentPoints.add(0, new Point((int) Math.rint(realStartPoint.x), (int) Math.rint(realStartPoint.y)));

        /*//TODO 测试观察处于哪些大概位置的点会显示错乱
        Point point = currentPoints.get(0);
        currentPoints.clear();
        currentPoints.add(point);
        currentPoints.add(new Point(point.x,point.y-200));
        currentPoints.add(new Point(point.x+10000,point.y-200+270));*/

        //Log.e("pointss", currentPoints+"");
        // if currentPoints is null or it`s size is zero , return
        if (currentPoints == null || currentPoints.size() <= 1) {
            return;
        }

        //if the point1 is look like point2 , remove it.
        for (int i = 1; i < currentPoints.size(); i++) {
            Point p1 = currentPoints.get(i - 1);
            Point p2 = currentPoints.get(i);
            if (Math.abs(p1.y - p2.y) < 3 && Math.abs(p1.x - p2.x) < 3) {
                currentPoints.remove(i);
                i--;
            }
        }

        //to cover last frame image
        can.drawColor(Color.BLACK);

        //create a bitmap to draw
        Bitmap hudwayBitmap = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_4444);

        //create a mPaint and set attribute
        Paint paint = new Paint();
        Canvas canvas = new Canvas(hudwayBitmap);
        paint.setColor(Color.BLACK);
        canvas.drawPaint(paint);

        paint.setStrokeWidth(OUTSIDE_LINE_WIDTH);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);

        float[] pointsXY = new float[currentPoints.size() * 2];
        // get the points x and y
        for (int i = 0; i < pointsXY.length; i++) {
            if (i % 2 == 0) {
                pointsXY[i] = currentPoints.get(i / 2).x;
            } else {
                pointsXY[i] = currentPoints.get(i / 2).y;
            }
        }

        // move to screen center , here set 1120-130,1120 is the center
        float offsetX = BITMAP_WIDTH / 2 - pointsXY[0];
        float offsetY = BITMAP_HEIGHT - pointsXY[1];
        for (int i = 0; i < pointsXY.length; i++) {
            pointsXY[i] = i % 2 == 0 ? pointsXY[i] + offsetX : pointsXY[i]
                    + offsetY;
        }

        // save this distance point to point, 2~1, 3~2 .....
        float[] distance = new float[pointsXY.length - 2];
        for (int i = 0; i < distance.length; i++) {
            distance[i] = pointsXY[i + 2] - pointsXY[i];
        }

        // Magnified N times
        // start point is constant，another points will change
        for (int i = 2; i < pointsXY.length; i++) {
            pointsXY[i] = pointsXY[i - 2] + distance[i - 2] * MAGNIFIED_TIME;
        }

        //remove the point in list if it may be error to draw
        Point firstPoint = new Point((int) pointsXY[0], (int) pointsXY[1]);
        mTempPoints.clear();
        mTempPoints.add(firstPoint);
        for (int i = 1; i < pointsXY.length / 2; i++) {
            Point tempPoint = new Point();
            if (pointsXY[i * 2 + 1] > firstPoint.y + TOLERATE_VALUE) {
                //TODO 计算得到的舍弃点的补偿点的坐标在某些情况下有问题（去深圳湾的掉头时，补偿点会画到屏幕右侧）
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

        HaloLogger.logE("route_result___", mTempPoints + "");

        pointsXY = new float[mTempPoints.size() * 2];
        for (int i = 0; i < mTempPoints.size(); i++) {
            pointsXY[i * 2] = mTempPoints.get(i).x;
            pointsXY[i * 2 + 1] = mTempPoints.get(i).y;
        }

        // here we must be 3D turn around first ,and rotate the path second.
        // first:3D turn around and set matrix
        setRotateMatrix4Canvas(pointsXY[0], pointsXY[1], -100.0f, 50f, canvas);

        /*// second:Calculate degrees and rotate path with it.
        float degrees = 0f;
        // if the line is vertical
        if (pointsXY[2] == pointsXY[0]) {
            if (pointsXY[3] <= pointsXY[1]) {
                degrees = 0;
            } else {
                degrees = 180;
            }
            // if the line is horizontal
        } else if (pointsXY[3] == pointsXY[1]) {
            if (pointsXY[2] <= pointsXY[0]) {
                degrees = 90;
            } else {
                degrees = 270;
            }
        } else {
            // if the line is not a vertical or horizontal,we should be to
            // calculate the degrees
            // cosA = (c*c + b*b - a*a)/(2*b*c)
            // A = acos(A)/2/PI*360
            double c = Math.sqrt(Math.pow(Math.abs(pointsXY[0] - pointsXY[2]),
                                          2.0) + Math.pow(Math.abs(pointsXY[1] - pointsXY[3]), 2.0));
            double b = Math.abs(pointsXY[1] - pointsXY[3]);
            double a = Math.abs(pointsXY[0] - pointsXY[2]);
            degrees = (float) (Math.acos((c * c + b * b - a * a) / (2 * b * c))
                    / 2 / Math.PI * 360);
            if (pointsXY[2] >= pointsXY[0] && pointsXY[3] >= pointsXY[1]) {
                degrees += 180;
            } else if (pointsXY[2] <= pointsXY[0] && pointsXY[3] >= pointsXY[1]) {
                degrees = (90 - degrees) + 90;
            } else if (pointsXY[2] <= pointsXY[0] && pointsXY[3] <= pointsXY[1]) {
                degrees += 0;
            } else if (pointsXY[2] >= pointsXY[0] && pointsXY[3] <= pointsXY[1]) {

                degrees = 270 + (90 - degrees);
            }
        }*/

        //rotate the path because the fakeLocation is created by our caculate , so it may be has a little error.
        //canvas.rotate(angel, pointsXY[0], pointsXY[1]);


        // draw the path
        Path basePath = new Path();
        Path redPointPath = new Path();
        Path circlePath = new Path();

		/*//将数组中的内容倒置，改善线的显示效果
        for(int i=0 ; i<pointsXY.length/2/2 ; i++){
			float tempX = pointsXY[i*2];
			float tempY = pointsXY[i*2+1];
			pointsXY[i*2]=pointsXY[(pointsXY.length/2-1-i)*2];
			pointsXY[i*2+1]=pointsXY[(pointsXY.length/2-1-i)*2+1];
			pointsXY[(pointsXY.length/2-1-i)*2]=tempX;
			pointsXY[(pointsXY.length/2-1-i)*2+1]=tempY;
		}*/

        basePath.moveTo(pointsXY[0], pointsXY[1]);
        redPointPath.moveTo(pointsXY[0], pointsXY[1]);
        for (int i = 0; i < pointsXY.length / 2 - 1; i++) {
            if (mCurrentIndex + i - 1 >= mPointsDistance.size()) {
                return;
            }
            float lineLength = i == 0 ? mCurPoint2NextPointDist : mPointsDistance.get(mCurrentIndex + i - 1);

            basePath.lineTo(pointsXY[(i + 1) * 2], pointsXY[(i + 1) * 2 + 1]);
            //TODO draw cicrle path
            /*if (i < pointsXY.length / 2 - 1 - 1) {
                // circlePath is the circle route , so it less one point to
				// shorter than outside line and inside line
				circlePath.lineTo(pointsXY[(i + 1) * 2],
						pointsXY[(i + 1) * 2 + 1]);
			}

			//add circle to circlePath for draw can move path
			//every 50 we will add a point,from end to start
			int pointCount=(int) (lineLength/HUDWAY_POINT_INTERVAL);
			//if the line length greater than HUDWAY_POINT_INTERVAL
			if(pointCount>0){
				float step_x = (pointsXY[(i + 1) * 2]-pointsXY[(i ) * 2])/lineLength*HUDWAY_POINT_INTERVAL;
				float step_y = (pointsXY[(i + 1) * 2 + 1]-pointsXY[(i) * 2 + 1])/lineLength*HUDWAY_POINT_INTERVAL;
				for(int j = 0 ; j < pointCount ; j ++){
					circlePath.addCircle(pointsXY[(i + 1) * 2]-(j+1)*step_x,pointsXY[(i+1 ) * 2 + 1]-(j+1)*step_y,10, Path.Direction.CW);
				}
			}*/

            //TODO draw red path
            /*if (mIsRedLine.get(mCurrentIndex + i - 1)) {
                redPointPath.lineTo(pointsXY[(i + 1) * 2], pointsXY[(i + 1) * 2 + 1]);
            } else {
                redPointPath.moveTo(pointsXY[(i + 1) * 2], pointsXY[(i + 1) * 2 + 1]);
                if (lineLength > 50) {
                    int pointCount = (int) (lineLength / HUDWAY_POINT_INTERVAL);
                    //if the line length greater than HUDWAY_POINT_INTERVAL
                    if (pointCount > 0) {
                        float step_x = (pointsXY[(i + 1) * 2] - pointsXY[(i) * 2]) / lineLength * HUDWAY_POINT_INTERVAL;
                        float step_y = (pointsXY[(i + 1) * 2 + 1] - pointsXY[(i) * 2 + 1]) / lineLength * HUDWAY_POINT_INTERVAL;
                        for (int j = 0; j < pointCount; j++) {
                            circlePath.addCircle(pointsXY[(i + 1) * 2] - (j + 1) * step_x, pointsXY[(i + 1) * 2 + 1] - (j + 1) * step_y, CIRCLE_LINE_WIDTH, Path.Direction.CW);
                        }
                    }
                }
            }*/
        }

        // set corner path for right angle(直角)
        CornerPathEffect cornerPathEffect = new CornerPathEffect(20);
        paint.setPathEffect(cornerPathEffect);

        //if the location point may be a error point ,do not to draw path and to draw text to warning user.
        if (mayBeErrorLocation) {
            Paint textPaint = new Paint();
            textPaint.setTextSize(50);
            textPaint.setColor(Color.WHITE);
            can.drawText("正在进行GPS定位，请继续行驶...", 50, 270, textPaint);
            return;
        }

        //TODO TEST Do not to draw route when cross image is showed.
        /*if(crossImage!=null){
            paint.setColor(Color.BLACK);
        }*/
        // draw outside line
        canvas.drawPath(basePath, paint);

        //draw black line
        paint.setStrokeWidth(MIDDLE_LINE_WIDTH);
        paint.setColor(Color.BLACK);
        canvas.drawPath(basePath, paint);

        //delete black color
        canvas.drawPaint(mPaintFilterBitmapColor);

        //draw red line
        //		mPaint.setColor(Color.RED);
        //		canvas.drawPath(redPointPath, mPaint);

        // draw inside line
        //      mPaint.setStrokeWidth(INSIDE_LINE_WIDTH);
        //      mPaint.setColor(Color.BLACK);
        //      canvas.drawPath(basePath, mPaint);

        //TODO draw rectPath to see real road rect
        /*if (!mRectPath.isEmpty()) {
            mPaint.setColor(Color.RED);
            mPaint.setStrokeWidth(10);
            canvas.drawPath(mRectPath, mPaint);
        }*/

        /*// draw can move point
        mPaint.setColor(Color.RED);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setStrokeWidth(CIRCLE_LINE_WIDTH);
		canvas.drawPath(circlePath,mPaint);*/

        //draw cross image
        if (crossImage != null) {
            if (mCrossImageLastLatLng == null) {
                mCrossImageLastLatLng = location;
                mCrossImageRetainDistance = retainDistance;
            }
            //offsetHeight表示图片要往下移动的量(px)
            int offsetHeight = 0;
            if (location != mCrossImageLastLatLng) {
                //TODO 这里计算的都是直线距离,但是这两个点之间的道路并不一定是直线,因此这样计算是有问题的
                mCrossImageDist += AMapUtils.calculateLineDistance(naviLatLng2LatLng(mCrossImageLastLatLng.getCoord()), naviLatLng2LatLng(location.getCoord()));
                offsetHeight = (int) (mCrossImageDist / mCrossImageRetainDistance * BITMAP_HEIGHT / 2);
                mCrossImageLastLatLng = location;
            }
            if (crossImage != mCrossImage) {
                mCrossImageLastLatLng = null;
                mCrossImageDist = 0;
                mCrossImage = crossImage;
                mCrossImageTarget = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, crossImage.getConfig());
                Canvas temp_canvas = new Canvas(mCrossImageTarget);
                temp_canvas.drawBitmap(crossImage, null,
                                       new Rect(0, 0, mCrossImageTarget.getWidth(), mCrossImageTarget.getHeight()),
                                       mPaint4CrossImageCanvas);
            }
            can.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            setRotateMatrix4Canvas(pointsXY[0], pointsXY[1], 0f, 50f, can);
            //            if(offsetHeight!=0){
            //                mCrossImageTarget = DrawUtils.centerSquareScaleBitmap(mCrossImageTarget,6*BITMAP_WIDTH);
            //            }
            can.drawBitmap(mCrossImageTarget,
                           //根据当前路口放大图和相对于路口放大图刚出现时的位置差对路口放大图进行截取--offsetHeight就是被截取部分的高度
                           new Rect(0, 0, BITMAP_WIDTH, BITMAP_HEIGHT - offsetHeight),
                           new Rect((int) (-3.5 * BITMAP_WIDTH), (int) (-6.5 * BITMAP_HEIGHT) + offsetHeight,
                                    (int) (4.5 * BITMAP_WIDTH), BITMAP_HEIGHT), null);
        } else if (mCrossImageLastLatLng != null) {
            mCrossImageLastLatLng = null;
            mCrossImageDist = 0;
        }

        Matrix initMatrix = new Matrix();
        can.setMatrix(initMatrix);
        //finally draw arway to surface view.
        can.drawBitmap(hudwayBitmap, 0, 0, null);

    }

    /**
     * set matrix to canvas with rotate and translate.
     *
     * @param translateX
     * @param translateY
     * @param offsetX
     * @param rotateXDegrees
     * @param canvas
     */
    private void setRotateMatrix4Canvas(float translateX, float translateY, float offsetX, float rotateXDegrees, Canvas canvas) {
        final Camera camera = new Camera();
        @SuppressWarnings("deprecation")
        final Matrix matrix = canvas.getMatrix();
        // save the camera status for restore
        camera.save();
        // around X rotate N degrees
        //		camera.rotateX(50);
        //		camera.translate(0.0f, -100f, 0.0f);
        camera.rotateX(rotateXDegrees);
        camera.translate(0.0f, offsetX, 0.0f);
        //x = -500 则为摄像头向右移动
        //y = 200 则为摄像头向下移动
        //z = 500 则为摄像头向高处移动
        // get the matrix from camera
        camera.getMatrix(matrix);
        // restore camera from the next time
        camera.restore();
        matrix.preTranslate(-translateX, -translateY);
        matrix.postTranslate(translateX, translateY);
        canvas.setMatrix(matrix);
    }

    /**
     * 根据当前的location获取到用于绘制的屏幕点的集合
     *
     * @return 用于绘制Hudway的点的集合
     */
    public List<Point> getCurrentPoints(AMapNaviLocation location) {
        List<Point> points = new ArrayList<Point>();
        fullPoints(location.getCoord(), points);
        return points;
    }

    /**
     * calculate to full points to draw path bitmap
     *
     * @param currentLatLng our car current location latlng
     * @param points        points list from draw the path bitmap
     */
    private void fullPoints(NaviLatLng currentLatLng, List<Point> points) {
        if (currentLatLng == null || mPathLatLngs == null || mPathLatLngs.size() <= 0) {
            return;
        }
        float totalLength = 0;
        points.clear();
        Point currentScreenPoint = mProjection
                .toScreenLocation(naviLatLng2LatLng(currentLatLng));
        points.add(currentScreenPoint);
        for (int i = mCurrentIndex; i < mPathLatLngs.size(); i++) {
            Point pathPoint = mProjection
                    .toScreenLocation(naviLatLng2LatLng(mPathLatLngs.get(i)));
            float distance = 0;
            if (i == mCurrentIndex) {
                if (currentScreenPoint.equals(pathPoint)) {
                    continue;
                }
                this.mCurPoint2NextPointDist = AMapUtils
                        .calculateLineDistance(
                                naviLatLng2LatLng(currentLatLng),
                                naviLatLng2LatLng(mPathLatLngs.get(i)));
                distance = mCurPoint2NextPointDist;
                totalLength += mCurPoint2NextPointDist;
            } else {
                if (pathPoint.equals(mProjection.toScreenLocation(
                        naviLatLng2LatLng(mPathLatLngs.get(i - 1))))) {
                    continue;
                }
                distance = AMapUtils.calculateLineDistance(
                        naviLatLng2LatLng(mPathLatLngs.get(i - 1)),
                        naviLatLng2LatLng(mPathLatLngs.get(i)));
                totalLength += distance;
            }
            //be sure the total distance is HUDWAY_LENGTH_IN_SCREEN
            if (totalLength == HUDWAY_LENGTH_IN_SCREEN) {
                points.add(pathPoint);
                return;
            } else if (totalLength > HUDWAY_LENGTH_IN_SCREEN) {
                float div = totalLength - HUDWAY_LENGTH_IN_SCREEN;
                Point prePoint = null;
                if (i == mCurrentIndex) {
                    prePoint = mProjection
                            .toScreenLocation(naviLatLng2LatLng(currentLatLng));
                } else {
                    prePoint = mProjection
                            .toScreenLocation(naviLatLng2LatLng(mPathLatLngs.get(i - 1)));
                }
                Point makePoint = new Point(
                        (int) (prePoint.x + (pathPoint.x - prePoint.x) * ((distance - div) / distance)),
                        (int) (prePoint.y + (pathPoint.y - prePoint.y) * ((distance - div) / distance)));
                points.add(makePoint);
                return;
            } else {
                points.add(pathPoint);
            }
        }
        return;
    }

    private LatLng naviLatLng2LatLng(NaviLatLng naviLatLng) {
        return naviLatLng == null ? null : new LatLng(naviLatLng.getLatitude(), naviLatLng.getLongitude());
    }

    /**
     * set index for current location `s next point
     *
     * @param curPoint current point index
     * @param curStep  current step index
     */
    public void setCurrentIndex(int curPoint, int curStep) {
        mCurrentIndex = 0;
        for (int i = 0; i < curStep; i++) {
            mCurrentIndex += mCroodsInSteps.get(i);
        }
        mCurrentIndex += curPoint + 1;
        if (mCurrentIndex >= mPathLatLngs.size()) {
            mCurrentIndex = mPathLatLngs.size() - 1;
        }
    }

    /**
     * draw test line
     *
     * @param width    bitmap width
     * @param height   bitmap height
     * @param location current location
     * @param degrees  bitmap`s degrees
     * @return
     */
    public Bitmap createLineTest(int width, int height, AMapNaviLocation location, float degrees) {
        if (mProjection == null || mCurrentIndex <= 0 || mCurrentIndex >= mPathLatLngs.size() - 1) {
            return null;
        }
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bm);
        Point locPoint = mProjection.toScreenLocation(naviLatLng2LatLng(location.getCoord()));
        Point prePoint = mProjection.toScreenLocation(naviLatLng2LatLng(mPathLatLngs.get(mCurrentIndex - 1)));
        Point curPoint = mProjection.toScreenLocation(naviLatLng2LatLng(mPathLatLngs.get(mCurrentIndex)));
        Point nextPoint = mProjection.toScreenLocation(naviLatLng2LatLng(mPathLatLngs.get(mCurrentIndex + 1)));

        Paint paint = new Paint();
        paint.setStrokeWidth(20);
        paint.setColor(Color.YELLOW);
        canvas.drawPoint(locPoint.x, locPoint.y, paint);
        paint.setStrokeWidth(10);
        paint.setColor(Color.RED);
        canvas.drawLine(prePoint.x, prePoint.y, curPoint.x, curPoint.y, paint);
        paint.setColor(Color.GREEN);
        canvas.drawLine(curPoint.x, curPoint.y, nextPoint.x, nextPoint.y, paint);
        paint.setTextSize(30);
        paint.setColor(Color.WHITE);
        canvas.drawText("curIndex : " + mCurrentIndex, 300, 500, paint);
        canvas.rotate(degrees);

        return bm;
    }

    /**
     * 根据
     *
     * @param crossImage a cross bitmap to cut
     * @param pointsXY   cut with point list
     * @return
     */
    private Bitmap cutBitmap(Bitmap crossImage, float[] pointsXY) {
        Path rectPath = points2path(pointsXY);

        int width = BITMAP_WIDTH;
        int height = BITMAP_HEIGHT;
        int LINE_WIDTH = (OUTSIDE_LINE_WIDTH - MIDDLE_LINE_WIDTH) / 2;

        Bitmap target = Bitmap.createBitmap(width, height, crossImage.getConfig());
        Canvas temp_canvas = new Canvas(target);

        Path bitmap_path = new Path();
        bitmap_path.moveTo(pointsXY[0] - crossImage.getWidth() / 2 - LINE_WIDTH / 2,
                           pointsXY[1] - crossImage.getHeight());
        bitmap_path.lineTo(pointsXY[0] + crossImage.getWidth() / 2 + LINE_WIDTH / 2,
                           pointsXY[1] - crossImage.getHeight());
        bitmap_path.lineTo(pointsXY[0] + crossImage.getWidth() / 2 + LINE_WIDTH / 2,
                           pointsXY[1]);
        bitmap_path.lineTo(pointsXY[0] - crossImage.getWidth() / 2 - LINE_WIDTH / 2,
                           pointsXY[1]);
        bitmap_path.close();
        if (bitmap_path.op(rectPath, Path.Op.INTERSECT)) {
            Paint paint = new Paint();
            paint.setStrokeWidth(1);
            paint.setStyle(Paint.Style.FILL);
            temp_canvas.drawPath(bitmap_path, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            temp_canvas.drawBitmap(crossImage, pointsXY[0] - crossImage.getWidth() / 2,
                                   pointsXY[1] - crossImage.getHeight(), paint);
        } else {
            temp_canvas.drawBitmap(crossImage, pointsXY[0] - crossImage.getWidth() / 2,
                                   pointsXY[1] - crossImage.getHeight(), null);
        }

        return target;
    }

    /**
     * 根据现有的points生成该点左右两侧的点的集合组成的RectPath
     *
     * @param pointsXY
     */
    private Path points2path(float[] pointsXY) {
        List<Point> points = new ArrayList<Point>();
        for (int i = 0; i < pointsXY.length; i += 2) {
            points.add(new Point((int) pointsXY[i], (int) pointsXY[i + 1]));
        }

        int path_width = MIDDLE_LINE_WIDTH;

        //获取一侧点的集合
        mRightSidePoints.clear();
        for (int i = 0; i < points.size(); i++) {
            Point currentPoint = points.get(i);
            Point secondPoint;
            int m = currentPoint.x;
            int n = currentPoint.y;
            if (i == points.size() - 1) {
                secondPoint = points.get(i - 1);
            } else {
                secondPoint = points.get(i + 1);
            }
            int a;
            int b;
            a = secondPoint.x;
            b = secondPoint.y;

            int x;
            int y;

            //m,n为B，a,b为A
            int x1 = m, y1 = n;
            int x2 = a, y2 = b;
            if (y2 == y1) {
                x = (int) (m - (b - n) / Math.sqrt(Math.pow((a - m), 2) + Math.pow((b - n), 2)));
                y = (int) (n + (path_width / 2) * (a - m) / Math.sqrt(Math.pow((a - m), 2) + Math.pow((b - n), 2)));
            } else if (x2 == x1) {
                x = x1 + (path_width / 2);
                y = y1;
            } else if ((x2 < x1 && y2 > y1) || (x2 < x1 && y2 < y1)) {
                x = (int) (x1 + (path_width / 2) * Math.sin(Math.atan((y2 - y1) / (x2 - x1))));
                y = (int) (y1 - (path_width / 2) * Math.cos(Math.atan((y2 - y1) / (x2 - x1))));
            } else {
                x = (int) (x1 - (path_width / 2) * Math.sin(Math.atan((y2 - y1) / (x2 - x1))));
                y = (int) (y1 + (path_width / 2) * Math.cos(Math.atan((y2 - y1) / (x2 - x1))));
            }
            Point point = new Point(x, y);

            //如果不是第一个或者最后一个，那么需要取该点和该点的前一个点继续运算得到x,y，然后取中间值
            if (i != 0 && i != points.size() - 1) {
                secondPoint = points.get(i - 1);
                a = secondPoint.x;
                b = secondPoint.y;
                x1 = m;
                y1 = n;
                x2 = a;
                y2 = b;
                if (y2 == y1) {
                    x = (int) (m + (b - n) / Math.sqrt(Math.pow((a - m), 2) + Math.pow((b - n), 2)));
                    y = (int) (n - (path_width / 2) * (a - m) / Math.sqrt(Math.pow((a - m), 2) + Math.pow((b - n), 2)));
                } else if (x2 == x1) {
                    x = x1 + (path_width / 2);
                    y = y1;
                } else if ((x2 < x1 && y2 > y1) || (x2 < x1 && y2 < y1)) {
                    x = (int) (x1 - (path_width / 2) * Math.sin(Math.atan((y2 - y1) / (x2 - x1))));
                    y = (int) (y1 + (path_width / 2) * Math.cos(Math.atan((y2 - y1) / (x2 - x1))));
                } else {
                    x = (int) (x1 + (path_width / 2) * Math.sin(Math.atan((y2 - y1) / (x2 - x1))));
                    y = (int) (y1 - (path_width / 2) * Math.cos(Math.atan((y2 - y1) / (x2 - x1))));
                }
                point.x = (point.x + x) / 2;
                point.y = (point.y + y) / 2;
            }

            mRightSidePoints.add(point);
        }

        //获取另一侧点的集合
        mLeftSidePoints.clear();
        for (int i = 0; i < points.size(); i++) {
            Point currentPoint = points.get(i);
            Point secondPoint;
            int m = currentPoint.x;
            int n = currentPoint.y;
            if (i == points.size() - 1) {
                secondPoint = points.get(i - 1);
            } else {
                secondPoint = points.get(i + 1);
            }
            int a;
            int b;
            a = secondPoint.x;
            b = secondPoint.y;

            int x;
            int y;


            int x1 = m, y1 = n;
            int x2 = a, y2 = b;
            if (y2 == y1) {
                x = (int) (m + (b - n) / Math.sqrt(Math.pow((a - m), 2) + Math.pow((b - n), 2)));
                y = (int) (n - (path_width / 2) * (a - m) / Math.sqrt(Math.pow((a - m), 2) + Math.pow((b - n), 2)));
            } else if (x2 == x1) {
                x = x1 - (path_width / 2);
                y = y1;
            } else if ((x2 < x1 && y2 > y1) || (x2 < x1 && y2 < y1)) {
                x = (int) (x1 - (path_width / 2) * Math.sin(Math.atan((y2 - y1) / (x2 - x1))));
                y = (int) (y1 + (path_width / 2) * Math.cos(Math.atan((y2 - y1) / (x2 - x1))));
            } else {
                x = (int) (x1 + (path_width / 2) * Math.sin(Math.atan((y2 - y1) / (x2 - x1))));
                y = (int) (y1 - (path_width / 2) * Math.cos(Math.atan((y2 - y1) / (x2 - x1))));
            }
            Point point = new Point(x, y);
            //如果不是第一个或者最后一个，那么需要取该点和该点的前一个点继续运算得到x,y，然后取中间值
            if (i != 0 && i != points.size() - 1) {
                secondPoint = points.get(i - 1);
                a = secondPoint.x;
                b = secondPoint.y;
                x1 = m;
                y1 = n;
                x2 = a;
                y2 = b;
                if (y2 == y1) {
                    x = (int) (m - (b - n) / Math.sqrt(Math.pow((a - m), 2) + Math.pow((b - n), 2)));
                    y = (int) (n + (path_width / 2) * (a - m) / Math.sqrt(Math.pow((a - m), 2) + Math.pow((b - n), 2)));
                } else if (x2 == x1) {
                    x = x1 - (path_width / 2);
                    y = y1;
                } else if ((x2 < x1 && y2 > y1) || (x2 < x1 && y2 < y1)) {
                    x = (int) (x1 + (path_width / 2) * Math.sin(Math.atan((y2 - y1) / (x2 - x1))));
                    y = (int) (y1 - (path_width / 2) * Math.cos(Math.atan((y2 - y1) / (x2 - x1))));
                } else {
                    x = (int) (x1 - (path_width / 2) * Math.sin(Math.atan((y2 - y1) / (x2 - x1))));
                    y = (int) (y1 + (path_width / 2) * Math.cos(Math.atan((y2 - y1) / (x2 - x1))));
                }
                point.x = (point.x + x) / 2;
                point.y = (point.y + y) / 2;
            }

            mLeftSidePoints.add(point);
        }

        //由于最后一个点的坐标是反向计算出来的，因此它的left和right是反的，在此做交换处理
        Point temp = mRightSidePoints.remove(mRightSidePoints.size() - 1);
        mRightSidePoints.add(mLeftSidePoints.remove(mLeftSidePoints.size() - 1));
        mLeftSidePoints.add(temp);

        //将点集合转成成矩形Path
        mRectPath.reset();
        Point point = mLeftSidePoints.get(0);
        mRectPath.moveTo(point.x, point.y);
        for (int i = 1; i < mRightSidePoints.size() + mLeftSidePoints.size(); i++) {
            if (i < mLeftSidePoints.size()) {
                point = mLeftSidePoints.get(i);
            } else {
                point = mRightSidePoints.get(mRightSidePoints.size() - (i - mLeftSidePoints.size() + 1));
            }
            mRectPath.lineTo(point.x, point.y);
        }

        /*
        //将点集合转换成Path集合，Path集合个数为原始点的个数减一(此处可表示为left或者right集合长度减一)
		mPaths.clear();
		for(int i=0;i<mRightSidePoints.size()-1;i++){
			Path path = new Path();
			Point leftCurrentPoint = mRightSidePoints.get(i);
			Point leftNextPoint = mRightSidePoints.get(i+1);
			Point rightCurrentPoint = mLeftSidePoints.get(i);
			Point rightNextPoint = mLeftSidePoints.get(i+1);
			path.moveTo(leftCurrentPoint.x,leftCurrentPoint.y);
			path.lineTo(leftNextPoint.x,leftNextPoint.y);
			path.lineTo(rightNextPoint.x,rightNextPoint.y);
			path.lineTo(rightCurrentPoint.x,rightCurrentPoint.y);
			path.close();
			mPaths.add(path);
		}*/
        return mRectPath;
    }
}
