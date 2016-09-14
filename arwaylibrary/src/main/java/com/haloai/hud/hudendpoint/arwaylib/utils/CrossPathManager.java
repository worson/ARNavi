package com.haloai.hud.hudendpoint.arwaylib.utils;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by wangshengxing on 16/8/29.
 */
public class CrossPathManager {
    private static final String             TAG                = CrossPathManager.class.getSimpleName();
    private              List<List<Point>>  mNaviStepsScreen   = new ArrayList<>();
    private              List<List<PointF>> mNaviStepsOpengl   = new ArrayList<>();
    private              int                mCenterPointIndex  = 0;
    private              List<Point>        mScreenPoints      = new ArrayList<>();
    private              Point              mCenterPoint       = new Point();
    private              PointF             mCenterPointOpengl = null;
    private              PointF             mPrePointOpengl    = null;

    private CrossPathManager() {}

    private static CrossPathManager     mInstance             = new CrossPathManager();
//    private static EnlargedCrossProcess mEnlargedCrossProcess = new EnlargedCrossProcess();

    public static CrossPathManager getInstance() {
        return mInstance;
    }

    /**
     * 转换整个path的屏幕点
     *
     * @param naviStepsScreen
     * @param naviStepsOpengl
     * @param biggerTime
     * @param rotateZ
     */
    public void parseNaviPathInfo(List<List<Point>> naviStepsScreen, List<List<PointF>> naviStepsOpengl, double biggerTime, double rotateZ) {
        if (/*naviPath == null || */mNaviStepsScreen == null || mNaviStepsOpengl == null) {
            return;
        }
        mNaviStepsScreen.clear();
        mNaviStepsOpengl.clear();

        mNaviStepsScreen.addAll(naviStepsScreen);
        mNaviStepsOpengl.addAll(naviStepsOpengl);
        if (mNaviStepsOpengl.size() > 0) {
            //关键是如何对List<List>做这些操作,而不单单是一个List
            //1.放大
            List<List<Double>> offsetXArrs = new ArrayList<>();
            List<List<Double>> offsetYArrs = new ArrayList<>();
            for (int i = 0; i < mNaviStepsOpengl.size(); i++) {
                List<PointF> naviStep = mNaviStepsOpengl.get(i);
                List<Double> offsetXArr = new ArrayList<>();
                List<Double> offsetYArr = new ArrayList<>();
                for (int j = 0; j < naviStep.size(); j++) {
                    if (j == 0) {
                        if (i == 0) {
                            continue;
                        } else {
                            offsetXArr.add((double) (naviStep.get(j).x - mNaviStepsOpengl.get(i - 1).get(mNaviStepsOpengl.get(i - 1).size() - 1).x));
                            offsetYArr.add((double) (naviStep.get(j).y - mNaviStepsOpengl.get(i - 1).get(mNaviStepsOpengl.get(i - 1).size() - 1).y));
                        }
                    } else {
                        offsetXArr.add((double) (naviStep.get(j).x - naviStep.get(j - 1).x));
                        offsetYArr.add((double) (naviStep.get(j).y - naviStep.get(j - 1).y));
                    }
                }
                offsetXArrs.add(offsetXArr);
                offsetYArrs.add(offsetYArr);
            }
            for (int i = 0; i < mNaviStepsOpengl.size(); i++) {
                List<PointF> naviStep = mNaviStepsOpengl.get(i);
                List<Double> offsetXArr = offsetXArrs.get(i);
                List<Double> offsetYArr = offsetYArrs.get(i);
                int offsetIndex = i == 0 ? 1 : 0;
                for (int j = 0; j < naviStep.size(); j++) {
                    if (j == 0) {
                        if (i == 0) {
                            continue;
                        } else {
                            PointF preStepLastPointF = mNaviStepsOpengl.get(i - 1).get(mNaviStepsOpengl.get(i - 1).size() - 1);
                            naviStep.get(j).x = (float) (preStepLastPointF.x + offsetXArr.get(0) * biggerTime);
                            naviStep.get(j).y = (float) (preStepLastPointF.y + offsetYArr.get(0) * biggerTime);
                        }
                    } else {
                        naviStep.get(j).x = (float) (naviStep.get(j - 1).x + offsetXArr.get(j - offsetIndex) * biggerTime);
                        naviStep.get(j).y = (float) (naviStep.get(j - 1).y + offsetYArr.get(j - offsetIndex) * biggerTime);
                    }
                }
            }
            /*for (int i = 1; i < mPath.size(); i++) {
                double x = mPath.get(i).x;
                double y = mPath.get(i).y;
                mOffsetX.add(x - mPath.get(i - 1).x);
                mOffsetY.add(y - mPath.get(i - 1).y);
            }
            for (int i = 1; i < mPath.size(); i++) {
                mPath.get(i).x = mPath.get(i - 1).x + mOffsetX.get(i - 1) * biggerTime;
                mPath.get(i).y = mPath.get(i - 1).y + mOffsetY.get(i - 1) * biggerTime;
            }*/
            //2.移动到0,0点
            double offsetX = mNaviStepsOpengl.get(0).get(0).x - 0;
            double offsetY = mNaviStepsOpengl.get(0).get(0).y - 0;
            for (List<PointF> naviStepOpengl : mNaviStepsOpengl) {
                for (PointF pointF : naviStepOpengl) {
                    pointF.x -= offsetX;
                    pointF.y -= offsetY;
                }
            }
            //3.旋转rotateZ角度
            /*for (List<PointF> naviStepOpengl : mNaviStepsOpengl) {
                boolean isFirst = true;
                for (PointF pointF : naviStepOpengl) {
                    *//*if(isFirst){
                        isFirst=false;
                        continue;
                    }*//*
                    MathUtils.rotateCoordinate( mNaviStepsOpengl.get(0).get(0), pointF, rotateZ);
                }
            }*/
            /*Matrix matrix = new Matrix();
            matrix.setRotate((float) rotateZ - 180, mNaviStepsOpengl.get(0).get(0).x,
                             mNaviStepsOpengl.get(0).get(0).y);
            for (List<PointF> naviStepOpengl : mNaviStepsOpengl) {
                for (int i = 1; i < naviStepOpengl.size(); i++) {
                    PointF p = naviStepOpengl.get(i);
                    float[] xy = new float[2];
                    matrix.mapPoints(xy, new float[]{p.x, p.y});
                    p.x = xy[0];
                    p.y = xy[1];
                }
            }*/
            HaloLogger.logE("branch_line", "cross start=============");
            for (List<PointF> naviStepOpengl : mNaviStepsOpengl) {
                for (PointF pointF : naviStepOpengl) {
                    HaloLogger.logE("branch_line", pointF.x + "," + pointF.y);
                }
            }
            HaloLogger.logE("branch_line", "cross end===============");
            HaloLogger.logE("branch_line", "screen start=============");
            for (List<Point> naviStepScreen : mNaviStepsScreen) {
                for (Point point : naviStepScreen) {
                    HaloLogger.logE("branch_line", point.x + "," + point.y);
                }
            }
            HaloLogger.logE("branch_line", "screen end===============");
            /*Matrix matrix = new Matrix();
            matrix.setRotate((float) rotateZ - 180, mNaviStepsOpengl.get(0).get(0).x, mNaviStepsOpengl.get(0).get(0).y);
            for (int i = 1; i < mPath.size(); i++) {
                Vector3 v = mPath.get(i);
                float[] xy = new float[2];
                matrix.mapPoints(xy, new float[]{(float) v.x, (float) v.y});
                v.x = xy[0];
                v.y = xy[1];
            }*/
        }
    }

    /**
     * 获取中心点的Opengl绝对坐标
     *
     * @return
     */
    public PointF getCenterPointOpengl() {
        return mCenterPointOpengl;
    }

    /**
     * 获取上一个点的Opengl绝对坐标
     *
     * @return
     */
    public PointF getPrePointOpengl() {
        return mPrePointOpengl;
    }

    /**
     * 获取中心点下标
     *
     * @return
     */
    public int getCenterPointIndex() {
        return mCenterPointIndex;
    }

    /**
     * 获取中心点附近的屏幕点
     *
     * @return
     */
    public List<Point> getScreenPoints() {
        return mScreenPoints;
    }

    /**
     * 获取中心点
     *
     * @return
     */
    public Point getCenterPoint() {
        return mCenterPoint;
    }

    private void resetCrossData() {
        mCenterPointIndex = 0;
        mCenterPoint.x = 0;
        mCenterPoint.y = 0;
        mScreenPoints.clear();
        mCenterPointOpengl = null;
        mPrePointOpengl = null;
    }

    /**
     * 处理路口放大图中心点数据
     *
     * @param stepIndex
     * @param width     路口放大图宽
     * @param height    路口放大图高
     * @return
     */
    public boolean handleCrossInfo(int stepIndex, int width, int height) {
        if (mNaviStepsScreen == null || mNaviStepsScreen.size() <= 0) {
            Log.e(TAG, "handleCrossInfo is error , mNaviStepsScreen == null || mNaviStepsScreen.size()<=0");
            return false;
        }
        resetCrossData();

        Point centerPoint = null;
        List<Point> screenPoints = new ArrayList<>();
        Point centerNextPoint = new Point(0, 0);

        Log.e(TAG, String.format("showCross writeCrossInfo stepIndex is %d", stepIndex));
        boolean breakFlg = false;
        if (stepIndex < mNaviStepsScreen.size()) {
            List<Point> stepPoints = mNaviStepsScreen.get(stepIndex);
            centerPoint = new Point(stepPoints.get(stepPoints.size() - 1));
            //保存opengl中的中心点绝对坐标, 以及中心点前一个点的绝对坐标
            mCenterPointOpengl = new PointF();
            mCenterPointOpengl.set(mNaviStepsOpengl.get(stepIndex).get(mNaviStepsOpengl.get(stepIndex).size() - 1));
            mPrePointOpengl = new PointF();
            mPrePointOpengl.set(mNaviStepsOpengl.get(stepIndex).get(mNaviStepsOpengl.get(stepIndex).size() - 2));
            if (mNaviStepsScreen.size() > (stepIndex)) {
                for (int i = stepIndex + 1; i < mNaviStepsScreen.size(); i++) {
                    List<Point> step = mNaviStepsScreen.get(i);
                    for (int j = 0; j < step.size(); j++) {
                        Point p = step.get(j);
                        if (pointDistance(centerPoint, p) > 10) {
                            centerNextPoint = new Point(p);
                            breakFlg = true;
                            break;
                        }
                    }
                    if (breakFlg) {
                        break;
                    }
                }
                if (!breakFlg) {
                    centerNextPoint = mNaviStepsScreen.get(stepIndex + 1).get(0);
                    Log.e(TAG, String.format("showCross writeCrossInfo centerNextPoint is error,centerNextPoint is %s", centerNextPoint));
                }
            } else {
                Log.e(TAG, String.format("showCross writeCrossInfo centerNextPoint is error,centerNextPoint is %s", centerNextPoint));
                return false;
            }

            List<List<Point>> stepsPoints = mNaviStepsScreen;
            breakFlg = false;
            //路口放大图
            Point rectPoint = centerPoint;
            int w = width * 2;
            int h = height * 2;
            Rect crossRect = new Rect(rectPoint.x - w / 2, rectPoint.y - h / 2, rectPoint.x + w / 2, rectPoint.y + h / 2);
            LinkedList<Point> prePoints = new LinkedList<>();
            int errorCnt = 0;

            //计算形状点
            final int MIN_THREAD_SIZE = 10;
            final int MAX_ERROR_SIZE = 3;
            //往前计算
            int preCnt = 0;
            int outR = (int) (Math.sqrt(width * width + height * height) / 2);
            outR = (int) (200 * Math.sqrt(2));

            errorCnt = 0;
            Log.e(TAG, String.format("showCross writeCrossInfo ,centerPoint is %s,region  is %s", centerPoint, crossRect));
            for (int step = stepIndex; step >= 0; step--) {
                List<Point> cStepPoints = stepsPoints.get(step);
                int cnt = cStepPoints.size();
                for (int i = cnt - 1; i >= 0; i--) {
                    Point p = cStepPoints.get(i);
                    if (isIncludePoint(crossRect, p) || preCnt < MIN_THREAD_SIZE) {//GeometryUtils.isIncludePoint(centerPoint, outR, p)
                        prePoints.addFirst(new Point(p));
                        preCnt++;
                        errorCnt = 0;
                    } else {
                        Log.e(TAG, String.format("showCross writeCrossInfo out of region ,point is %s,preCnt is %d,errorCnt is %d", p, preCnt, errorCnt));
                        if (++errorCnt > MAX_ERROR_SIZE) {
                            breakFlg = true;
                            break;
                        } else {
                            prePoints.addFirst(new Point(p));
                            preCnt++;
                        }
                    }
                }
                if (breakFlg) {
                    break;
                }
            }
            screenPoints.addAll(prePoints);

            //往后计算
            breakFlg = false;
            int nextCnt = 0;
            errorCnt = 0;
            for (int step = stepIndex + 1; step < stepsPoints.size(); step++) {
                for (Point p : stepsPoints.get(step)) {
                    if (isIncludePoint(crossRect, p) || nextCnt < MIN_THREAD_SIZE) {
                        screenPoints.add(new Point(p));
                        nextCnt++;
                        errorCnt = 0;
                    } else {
                        Log.e(TAG, String.format("showCross writeCrossInfo out of region ,point is %s,nextCnt is %d,errorCnt is %d ", p, nextCnt, errorCnt));
                        if (++errorCnt > MAX_ERROR_SIZE) {
                            breakFlg = true;
                            break;
                        } else {
                            screenPoints.add(new Point(p));
                            nextCnt++;
                        }
                    }
                }
                if (breakFlg) {
                    break;
                }
            }
            mCenterPointIndex = preCnt - 1;
            /*if (screenPoints.size() > preCnt + 1) {
                centerNextPoint = new Point(screenPoints.get(preCnt + 1));
            }*/

            /*int preStep = stepIndex-1;
            screenPoints.clear();
            if(preStep>=0){
//                screenPoints.addAll(mNaviStepsScreen.get(preStep));
            }
            screenPoints.addAll(mNaviStepsScreen.get(stepIndex));
            if(mNaviStepsScreen.size()>(stepIndex+1)){
                screenPoints.addAll(mNaviStepsScreen.get(stepIndex+1));
            }*/

            Log.e(TAG, String.format("width is %s ,height is %s,preCnt is %d,nextCnt is %d ,stepIndex is %d", width, height, preCnt, nextCnt, stepIndex));
            if (screenPoints.size() > 0) {
                Log.e(TAG, String.format("showCross writeCrossInfo has got basic data"));
                RectMapPara rectPara = getCenterPara(width, height, centerPoint);
                if (rectPara != null) {
                    rectRemap(mScreenPoints, screenPoints, rectPara);
                    mCenterPoint = rectRemapPoint(centerPoint, rectPara);
                    Point rCenterNextPoint = rectRemapPoint(centerNextPoint, rectPara);
                    Log.e(TAG, String.format("showCross writeCrossInfo got all data"));
                    Log.e(TAG, String.format("showCross writeCrossInfo saved data ,rCenterPoint is %s,,centerNextPoint is %s,rCenterNextPoint is %s ,rScreenPoints is %s ", mCenterPoint, centerNextPoint, rCenterNextPoint, mScreenPoints));
                } else {
                    Log.e(TAG, String.format("showCross writeCrossInfo rectPara is null"));
                }

            } else {
                Log.e(TAG, String.format("showCross writeCrossInfo get basic data error"));
            }
        } else {
            Log.e(TAG, String.format("showCross writeCrossInfo points out of size"));
            return false;
        }

        return true;
    }

    /*public List<List<Vector3>> setEnlargeCrossBranchLiens(Bitmap crossImage) {
        int centerPointIndex = getCenterPointIndex();
        String[] mainRoadArr = getMainRoadArr();
        if (centerPointIndex < 0 || centerPointIndex >= mainRoadArr.length / 2) {
            HaloLogger.logE(TAG, "getBranchLines faild , centerPointIndex < 0 or centerPointIndex >= mainRoadArr.length / 2!");
            return null;
        }

        List<EnlargedCrossProcess.ECBranchLine> ecBranchLines =
                mEnlargedCrossProcess.recognizeBranchInECImage(crossImage, centerPointIndex, mainRoadArr);
        if (ecBranchLines == null || ecBranchLines.size() <= 0) {
            HaloLogger.logE(TAG, "getBranchLines faild , ecBranchLines == null or ecBranchLines.size() <= 0!");
            return null;
        }
        //过滤岔路点每隔10个点取一个
        *//*for (int i = 0; i < ecBranchLines.size(); i++) {
            EnlargedCrossProcess.ECBranchLine ecb = ecBranchLines.get(i);
            List<Point> line = ecb.getLinePoints();
            for (int j = 0, count = 0; j < line.size(); j++) {
                if (count++ % 10 != 0) {
                    line.remove(j--);
                }
            }
        }*//*
        for (EnlargedCrossProcess.ECBranchLine ecb : ecBranchLines) {
            HaloLogger.logE("branch_handle", "=========return start=========");
            int count = 0;
            for (Point p : ecb.getLinePoints()) {
                if(count%10==0) {
                    HaloLogger.logE("branch_handle", p.x + "," + p.y);
                }
                count++;
            }
            HaloLogger.logE("branch_handle", "==========return end==========");
        }
        List<List<Vector3>> branchLines = new ArrayList<>();
        double scale = calcScaleFromScreen2Opengl(mainRoadArr, centerPointIndex);
        for (int i = 0; i < ecBranchLines.size(); i++) {
            //计算比例转换返回的岔路坐标到Opengl坐标, 并添加到场景中
            EnlargedCrossProcess.ECBranchLine ecb = ecBranchLines.get(i);
            List<Point> line = ecb.getLinePoints();
            Point centerScreenPoint = getCenterPoint();
            PointF centerOpenglPoint = getCenterPointOpengl();
            line.add(0, centerScreenPoint);
            List<Vector3> branchLine = new ArrayList<>();
            for (Point point : line) {
                Vector3 v = new Vector3(point.x, point.y, 0);
                branchLine.add(v);
            }
            line.clear();
            //平移line第一个点到centerPointOpengl点
            double offsetX = centerOpenglPoint.x - centerScreenPoint.x;
            double offsetY = centerOpenglPoint.y - centerScreenPoint.y;
            for (Vector3 v : branchLine) {
                v.x += offsetX;
                v.y += offsetY;
            }
            //记录offsetXY集合,并计算得到新的坐标数据
            List<Double> mOffsetX = new ArrayList<>();
            List<Double> mOffsetY = new ArrayList<>();
            for (int j = 1; j < branchLine.size(); j++) {
                double x = branchLine.get(j).x;
                double y = branchLine.get(j).y;
                mOffsetX.add(x - branchLine.get(j - 1).x);
                mOffsetY.add(y - branchLine.get(j - 1).y);
            }
            for (int j = 1; j < branchLine.size(); j++) {
                branchLine.get(j).x = branchLine.get(j - 1).x + mOffsetX.get(j - 1) * scale;
                branchLine.get(j).y = branchLine.get(j - 1).y + mOffsetY.get(j - 1) * scale;
            }
            branchLines.add(branchLine);
        }
        return branchLines;
    }*/

    private double calcScaleFromScreen2Opengl(String[] mainRoadArr, int centerPointIndex) {
        PointF centerPointOpengl = getCenterPointOpengl();
        PointF prePointOpengl = getPrePointOpengl();
        PointF centerPointScreen = new PointF(200, 200);
        PointF prePointScreen = new PointF(Float.parseFloat(mainRoadArr[(centerPointIndex - 1) * 2]),
                                           Float.parseFloat(mainRoadArr[(centerPointIndex - 1) * 2 + 1]));
        return MathUtils.calculateDistance(centerPointOpengl, prePointOpengl) /
                MathUtils.calculateDistance(centerPointScreen, prePointScreen);
    }

    /**
     * 获取传入JNI部分的主路点坐标集合
     * 格式:[x1,y1,x2,y2...xn,yn]
     *
     * @return
     */
    private String[] getMainRoadArr() {
        List<Point> screenPoints = getScreenPoints();
        // TODO: 2016/9/6 test
        HaloLogger.logE("branch_handle", "=========ori start=========");
        int centerPointIndex = getCenterPointIndex();
        int startIndex = centerPointIndex-4>=0?centerPointIndex-4:0;
        int endIndex = centerPointIndex+4<=screenPoints.size()?centerPointIndex+4:screenPoints.size();
        for (int i=startIndex;i<endIndex;i++) {
            Point p = screenPoints.get(i);
            HaloLogger.logE("branch_handle", p.x + "," + p.y);
        }
        HaloLogger.logE("branch_handle", "==========ori end==========");
        String[] mainRoadArr = new String[screenPoints.size() * 2];
        for (int i = 0; i < screenPoints.size(); i++) {
            mainRoadArr[i * 2] = "" + screenPoints.get(i).x;
            mainRoadArr[i * 2 + 1] = "" + screenPoints.get(i).y;
        }
        return mainRoadArr;
    }

    private static class RectMapPara {
        private Point  refPoint;
        private double scalefactor;
        private int    widthMove;
        private int    heightMove;
    }

    public static RectMapPara getCenterPara(int width, int height, Point center) {
        RectMapPara rectMapPara = new RectMapPara();
        rectMapPara.refPoint = new Point(0, 0);
        rectMapPara.scalefactor = 1;
        rectMapPara.heightMove = width / 2 - center.y;
        rectMapPara.widthMove = height / 2 - center.x;
        return rectMapPara;

    }

    public static Point rectRemapPoint(Point srcPoint, RectMapPara rectMapPara) {
        Point refPoint = rectMapPara.refPoint;
        int newX, newY;
        newX = (int) ((srcPoint.x - refPoint.x) * rectMapPara.scalefactor) + rectMapPara.widthMove;
        newY = (int) ((srcPoint.y - refPoint.y) * rectMapPara.scalefactor) + rectMapPara.heightMove;
        Point newPoint = new Point(newX, newY);
        return newPoint;
    }

    public static void rectRemap(List<Point> newPointList, List<Point> points, RectMapPara rectMapPara) {
        if (newPointList == null) {
            return;
        }
        Point refPoint = rectMapPara.refPoint;
        double scalefactor = rectMapPara.scalefactor;
        int widthMove = rectMapPara.widthMove;
        int heightMove = rectMapPara.heightMove;
        int newX, newY;
        newPointList.clear();
        for (Point point : points) {
            newX = (int) ((point.x - refPoint.x) * scalefactor) + widthMove;
            newY = (int) ((point.y - refPoint.y) * scalefactor) + heightMove;
            Point newPoint = new Point(newX, newY);
            newPointList.add(newPoint);
        }
    }

    /**
     * 两点距离的距离
     */
    public static double pointDistance(Point a, Point b) {
        return Math.sqrt(powDistance(a, b));
    }

    /**
     * 两点距离平方和
     */
    public static int powDistance(Point a, Point b) {
        int diffX = Math.abs(a.x - b.x);
        int diffY = Math.abs(a.y - b.y);
        return diffX * diffX + diffY * diffY;
    }

    /**
     * 判断点是否在一个矩形内
     */

    public static boolean isIncludePoint(Rect rect, Point point) {
        boolean result = false;
        if (rect != null) {
            result = (point.x >= rect.left && point.x <= rect.right) && (point.y >= rect.top && point.y <= rect.bottom);
        }
        return result;
    }
}
