package com.haloai.hud.hudendpoint.arwaylib.utils;

import android.graphics.PointF;

import com.haloai.hud.utils.HaloLogger;

import java.util.Collections;
import java.util.List;

/**
 * author       : 龙;
 * date         : 2016/10/10;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.utils;
 * project_name : hudlauncher;
 *
 * 使用该工具类对数据进行道格拉斯-普克抽析
 */
public class Douglas {
    /**
     * 对比两个数值
     * @param aItem
     * @param bItem
     * @return 0表示相等,aItem大返回正数,bItem大返回负数
     */
    private static int compare(int aItem , int bItem) {
        if(aItem!=0 || bItem!=0)
            return 0;
        return (aItem - bItem);
    }

    /**
     * @param pointIndexsToKeep 需要在抽析中保留的点的index
     * @param vertices 需要进行抽析的数据集合
     * @param tolerance 抽析的容差,这个值越大,抽析掉的点越多
     */
    public static void rarefyGetIndexs(List<Integer> pointIndexsToKeep, List<PointF> vertices, double tolerance) {
        int firstPoint = 0;
        int lastPoint = vertices.size() - 1;

        // Add the first and last index to the keepers
        pointIndexsToKeep.add(firstPoint);

        while (vertices.get(firstPoint).equals(vertices.get(lastPoint))){
            lastPoint--;
            if (lastPoint <= 0) {
                pointIndexsToKeep.add(lastPoint);
                return;
            }
        }

        // add adjusted lastPoint
        pointIndexsToKeep.add(lastPoint);

        douglasPeuckerReduction(vertices, firstPoint, lastPoint, tolerance,
                                pointIndexsToKeep);

        // revert the former sequence
        Collections.sort(pointIndexsToKeep);
        return;
    }

    /**
     * 道格拉斯-普克算法对直线进行抽稀
     *
     *
     * @param pointIndexsToKeep
     * @param returnPoints 结果保存的集合
     * @param vertices 进行处理的数据集合
     * @param tolerance 这个值越大被抽掉的越多
     * @return
     */
    public static void rarefyGetPointFs(List<Integer> pointIndexsToKeep, List<PointF> returnPoints, List<PointF> vertices, double tolerance) {
        //在函数调用前判断了
        int pointSize = vertices.size();
        if (vertices.isEmpty() || (pointSize < 3)) {
            returnPoints.addAll(vertices);
            return;
        }

        int firstPoint = 0;
        int lastPoint = vertices.size() - 1;
        pointIndexsToKeep.clear();

        // Add the first and last index to the keepers
        pointIndexsToKeep.add(firstPoint);

        while (vertices.get(firstPoint).equals(vertices.get(lastPoint))){
            lastPoint--;
            if (lastPoint <= 0) {
                for (int i=0; i < vertices.size(); i++) {
                    returnPoints.add(vertices.get(i));
                }
                return;
            }
        }

        // add adjusted lastPoint
        pointIndexsToKeep.add(lastPoint);

        douglasPeuckerReduction(vertices, firstPoint, lastPoint, tolerance,
                                pointIndexsToKeep);

        // revert the former sequence
        Collections.sort(pointIndexsToKeep);

        for (int i = 0; i < pointIndexsToKeep.size(); i++) {
            int vIndex = pointIndexsToKeep.get(i);
            returnPoints.add(vertices.get(vIndex));
        }
        return;
    }

    private static void douglasPeuckerReduction(List<PointF> vertices, int firstPoint, int lastPoint, double tolerance,
                                                List<Integer> pointIndexsToKeep) {

        double maxDistance = 0;
        int indexFarthest = 0;

        for (int index = firstPoint; index < lastPoint; index++) {
            PointF firstVertex = vertices.get(firstPoint);
            PointF lastVertex = vertices.get(lastPoint);
            PointF indexVertex = vertices.get(index);

            double distance = perpendicularDistance(firstVertex, lastVertex, indexVertex);
            if (distance > maxDistance) {
                maxDistance = distance;
                indexFarthest = index;
            }
        }

        HaloLogger.logE("branch_line_douglas","maxDistance:"+maxDistance);
        HaloLogger.logE("branch_line_douglas","tolerance:"+tolerance);
        if (maxDistance > tolerance && indexFarthest != 0) {
            // Add the largest point that exceeds the tolerance
            pointIndexsToKeep.add(indexFarthest);

            douglasPeuckerReduction(vertices, firstPoint, indexFarthest,
                                    tolerance, pointIndexsToKeep);

            douglasPeuckerReduction(vertices, indexFarthest, lastPoint,
                                    tolerance, pointIndexsToKeep);
        }
    }

    /**
     * 点到直线的距离
     *
     * @param lineStartPoint
     * @param lineEndPoint
     * @param point
     * @return
     */
    private static double perpendicularDistance(PointF lineStartPoint, PointF lineEndPoint, PointF point) {
        /*// 过滤掉 不构成三角形的情况
        if (lineStartPoint.equals(lineEndPoint)
                || point.equals(lineStartPoint)
                || point.equals(lineEndPoint)) {
            return 0;
        }
        double area = Math.abs(0.5 * (lineStartPoint.x * lineEndPoint.y + lineEndPoint.x * point.y + point.x
                * lineStartPoint.y - lineEndPoint.x * lineStartPoint.y - point.x * lineEndPoint.y - lineStartPoint.x
                * point.y));
        double bottom = Math.sqrt(Math.pow(lineStartPoint.x - lineEndPoint.x, 2)
                                      + Math.pow(lineStartPoint.y - lineEndPoint.y, 2));
        double height = area * 2 / bottom;
        //HaloLogger.logE("branch_line_level_","height:"+height);
        return height;*/
        PointF pProjection = new PointF();
        getProjectivePoint(lineStartPoint, lineEndPoint, point, pProjection);
        return calculateDistance(point.x,point.y,pProjection.x,pProjection.y);
    }

    /**
     * 计算两点之间直线距离
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    private static double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow((y2 - y1), 2) + Math.pow((x2 - x1), 2));
    }

    /**
     * 求直线外一点到直线上的投影点
     *
     * @param pLine    线上一点
     * @param k        斜率
     * @param pOut     线外一点
     * @param pProject 投影点
     */
    private static void getProjectivePoint(PointF pLine, double k, PointF pOut, PointF pProject) {
        if (k == 0) {//垂线斜率不存在情况
            pProject.x = pOut.x;
            pProject.y = pLine.y;
        } else {
            pProject.x = (float) ((k * pLine.x + pOut.x / k + pOut.y - pLine.y) / (1 / k + k));
            pProject.y = (float) (-1 / k * (pProject.x - pOut.x) + pOut.y);
        }
    }

    /**
     * 求pOut在pLine以及pLine2所连直线上的投影点
     *
     * @param pLine
     * @param pLine2
     * @param pOut
     * @param pProject
     */
    private static void getProjectivePoint(PointF pLine, PointF pLine2, PointF pOut, PointF pProject) {
        double k = 0;
        try {
            k = getSlope(pLine.x, pLine.y, pLine2.x, pLine2.y);
        } catch (Exception e) {
            k = 0;
        }
        getProjectivePoint(pLine, k, pOut, pProject);
    }

    /**
     * 通过两个点坐标计算斜率
     * 已知A(x1,y1),B(x2,y2)
     * 1、若x1=x2,则斜率不存在；
     * 2、若x1≠x2,则斜率k=[y2－y1]/[x2－x1]
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @throws Exception 如果x1==x2,则抛出该异常
     */
    private static double getSlope(double x1, double y1, double x2, double y2) throws Exception {
        if (x1 == x2) {
            throw new Exception("Slope is not existence,and div by zero!");
        }
        return (y2 - y1) / (x2 - x1);
    }
}
