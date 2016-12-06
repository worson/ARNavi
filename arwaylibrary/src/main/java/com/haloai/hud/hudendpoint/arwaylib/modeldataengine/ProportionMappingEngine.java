package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import android.graphics.PointF;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayProjection;
import com.haloai.hud.hudendpoint.arwaylib.utils.Douglas;
import com.haloai.hud.hudendpoint.arwaylib.utils.jni_data.LatLngOutSide;
import com.haloai.hud.utils.HaloLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * author       : 龙;
 * date         : 2016/10/31;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.modeldataengine;
 * project_name : hudlauncher;
 * <p/>
 * 该类用于实现原始路径到渲染路径之间的一个比例映射关系.
 * <p/>
 * 由于涉及到Double型的比较,因此当两个double相减的绝对值小于等于0.00000000001D即认为相等.
 */
public class ProportionMappingEngine {
    private static final String TAG = "ProportionMappingEngine";
    private static final double X   = 0.00000000001D;

    private List<LatLngOutSide> mOriPath;//原始的路径
    private List<LatLngOutSide> mRenderPath;//实际渲染路径
    private List<Double>        mProportionListOri;//原始路径的中的比例关系
    private List<Double>        mProportionListRender;//实际渲染路径中的比例关系
    private double              mTolerance;
    private int                 mDefaultLevel;

    /**
     * 构造函数需要传入初始的路径,并进行一些初始化的操作
     *
     * @param _oriPath
     */
    public ProportionMappingEngine(List<LatLngOutSide> _oriPath) {
        mOriPath = new ArrayList<>(_oriPath);
        mRenderPath = new ArrayList<>(_oriPath);
        mProportionListOri = new ArrayList<>();
        List<Double> lengths = new ArrayList<>();
        lengths.add(0D);
        double totalLength = 0;
        for (int i = 1; i < mOriPath.size(); i++) {
            totalLength += AMapUtils.calculateLineDistance(
                    new LatLng(mOriPath.get(i - 1).lat, mOriPath.get(i - 1).lng),
                    new LatLng(mOriPath.get(i).lat, mOriPath.get(i).lng));
            lengths.add(totalLength);
        }
        for (int i = 0; i < lengths.size(); i++) {
            mProportionListOri.add(lengths.get(i) / totalLength);
        }

        mProportionListRender = new ArrayList<>(mProportionListOri);
    }

    /**
     * 对渲染路径进行抽析
     *
     * @param keepPointIndexs 需要保留的点在原始路径中的下标
     * @param tolerance       抽析的容差(单位是opengl)
     * @param defaultLevel
     */
    public void rarefyDouglas(List<Integer> keepPointIndexs, double tolerance, int defaultLevel) {
        mTolerance = tolerance;
        mDefaultLevel = defaultLevel;
        HaloLogger.logE(TAG, "tolerance=" + tolerance);
        List<Integer> keepIndexs = new ArrayList<>(keepPointIndexs);
        keepIndexs.add(0, 0);
        //Douglas.rarefyGetPointFs(pointIndexsToKeep, returnPath, originalPath, RAREFY_PIXEL_COUNT / ARWayProjection.K);
        List<LatLngOutSide> renderPath = new ArrayList<>(mRenderPath);
        List<Double> proportionListRender = new ArrayList<>(mProportionListRender);
        mRenderPath = new ArrayList<>();
        mProportionListRender = new ArrayList<>();

        /*HaloLogger.logE(TAG,"cross start");
        for(LatLngOutSide LatLngOutSide : renderPath){
            HaloLogger.logE(TAG, LatLngOutSide.latitude+","+LatLngOutSide.longitude);
        }
        HaloLogger.logE(TAG,"cross end");
        List<Integer> keepInRarefy = new ArrayList<>();
        List<PointF> vertices = new ArrayList<>();
        for(LatLngOutSide LatLngOutSide:renderPath){
            ARWayProjection.PointD pd = ARWayProjection.toOpenGLLocation(LatLngOutSide, defaultLevel);
            vertices.add(new PointF((float)pd.x,(float)pd.y));
        }
        Douglas.rarefyGetIndexs(keepInRarefy,vertices,tolerance);
        for(int i=0;i<keepInRarefy.size();i++){
            mRenderPath.add(renderPath.get(keepInRarefy.get(i)));
            mProportionListRender.add(proportionListRender.get(keepInRarefy.get(i)));
        }
        HaloLogger.logE(TAG,"cross start");
        for(LatLngOutSide LatLngOutSide : mRenderPath){
            HaloLogger.logE(TAG, LatLngOutSide.latitude+","+LatLngOutSide.longitude);
        }
        HaloLogger.logE(TAG,"cross end");*/

        for (int i = 0; i < keepIndexs.size() - 1; i++) {
            int start = keepIndexs.get(i);
            int end = keepIndexs.get(i + 1) + 1;
            List<LatLngOutSide> partPath = renderPath.subList(start, end);
            List<Double> partProp = proportionListRender.subList(start, end);
            List<Integer> keepInRarefy = new ArrayList<>();
            rarefy(tolerance, defaultLevel, partPath, keepInRarefy);
            if (i == 0) {
                mRenderPath.add(partPath.get(0));
                mProportionListRender.add(partProp.get(0));
            }
            for (int j = 1; j < keepInRarefy.size(); j++) {
                int index = keepInRarefy.get(j);
                mRenderPath.add(partPath.get(index));
                mProportionListRender.add(partProp.get(index));
            }
        }
    }

    private void rarefy(double tolerance, int defaultLevel, List<LatLngOutSide> partPath, List<Integer> keepInRarefy) {
        List<PointF> vertices = new ArrayList<>();
        for (LatLngOutSide LatLngOutSide : partPath) {
            ARWayProjection.PointD pd = ARWayProjection.toOpenGLLocation(LatLngOutSide, defaultLevel);
            vertices.add(new PointF((float) pd.x, (float) pd.y));
        }
        Douglas.rarefyGetIndexs(keepInRarefy, vertices, tolerance);
    }

    /**
     * 使用一段数据替换掉实际渲染路径中的一段
     *
     * @param subPath           新的数据段
     * @param start             开始替换点在原始路径中的位置
     * @param end               结束替换点在原始路径中的位置
     * @param _crossPointIndexs 需要在抽析中被保留的点
     */
    public void mapping(List<LatLngOutSide> subPath, int start, int end, List<Integer> _crossPointIndexs) {
        //首先对subPath进行抽析处理
        HaloLogger.logE(TAG, "cross start");
        for (LatLngOutSide latlng : subPath) {
            HaloLogger.logE(TAG, latlng.lat + "," + latlng.lng);
        }
        HaloLogger.logE(TAG, "cross end");
        List<Integer> crossPointIndexs = new ArrayList<>(_crossPointIndexs);
        crossPointIndexs.add(0, 0);
        crossPointIndexs.add(subPath.size() - 1);
        List<LatLngOutSide> temp = new ArrayList<>(subPath);
        subPath = new ArrayList<>();
        for (int i = 0; i < crossPointIndexs.size() - 1; i++) {
            int _start = crossPointIndexs.get(i);
            int _end = crossPointIndexs.get(i + 1) + 1;
            List<LatLngOutSide> partPath = temp.subList(_start, _end);
            List<Integer> keepInRarefy = new ArrayList<>();
            rarefy(mTolerance, mDefaultLevel, partPath, keepInRarefy);
            if (i == 0) {
                subPath.add(partPath.get(0));
            }
            for (int j = 1; j < keepInRarefy.size(); j++) {
                int index = keepInRarefy.get(j);
                subPath.add(partPath.get(index));
            }
        }
        HaloLogger.logE(TAG, "cross start");
        for (LatLngOutSide latlng : subPath) {
            HaloLogger.logE(TAG, latlng.lat + "," + latlng.lng);
        }
        HaloLogger.logE(TAG, "cross end");

        HaloLogger.logE("tt__tt", "被替换的主路部分 cross start");
        for (LatLngOutSide latlng : mOriPath.subList(start, end)) {
            HaloLogger.logE("tt__tt", latlng.lat + "," + latlng.lng);
        }
        HaloLogger.logE("tt__tt", "被替换的主路部分 cross end");
        //        HaloLogger.logE("tt__tt","cross start");
        //        for(LatLngOutSide latlng:mRenderPath){
        //            HaloLogger.logE("tt__tt",latlng.lat+","+latlng.lng);
        //        }
        //        HaloLogger.logE("tt__tt","cross end");
        double startProp = mProportionListOri.get(start);
        double endProp = mProportionListOri.get(end);
        for (int i = 0; i < mProportionListRender.size(); i++) {
            double prop = mProportionListRender.get(i);
            if (Math.abs(prop - startProp) <= X || prop > startProp) {
                int replaceStart = i == 0 ? 0 : /*Math.abs(prop - startProp) <= X ? i : */i - 1;
                for (int j = i; j < mProportionListRender.size(); j++) {
                    prop = mProportionListRender.get(j);
                    if (Math.abs(prop - endProp) <= X || prop > endProp) {
                        int replaceEnd = j;
                        HaloLogger.logE("tt__tt", "替换的主路 cross start");
                        for (LatLngOutSide latlng : subPath) {
                            HaloLogger.logE("tt__tt", latlng.lat + "," + latlng.lng);
                        }
                        HaloLogger.logE("tt__tt", "替换的主路 cross end");
                        HaloLogger.logE("tt__tt", "替换后的主路 path start");
                        for (LatLngOutSide latlng : mRenderPath.subList(replaceStart, replaceEnd + 1)) {
                            HaloLogger.logE("tt__tt", latlng.lat + "," + latlng.lng);
                        }
                        HaloLogger.logE("tt__tt", "替换后的主路 path end");
                        List<LatLngOutSide> headRenderPath = mRenderPath.subList(0, replaceStart + 1);
                        List<LatLngOutSide> tailRenderPath = mRenderPath.subList(replaceEnd, mRenderPath.size());
                        List<Double> headProportion = mProportionListRender.subList(0, replaceStart + 1);
                        List<Double> tailProportion = mProportionListRender.subList(replaceEnd, mProportionListRender.size());
                        double replacedStartProportion = mProportionListRender.get(replaceStart);
                        double replacedEndProportion = mProportionListRender.get(replaceEnd);
                        double replacedTotalProportion = replacedEndProportion - replacedStartProportion;
                        LatLngOutSide replaceStartLatLngOutSide = mRenderPath.get(replaceStart);
                        LatLngOutSide replaceEndLatLngOutSide = mRenderPath.get(replaceEnd);
                        List<Double> middleProportion = new ArrayList<>();
                        List<Double> lengths = new ArrayList<>();
                        double totalLength = 0;
                        totalLength += AMapUtils.calculateLineDistance(
                                new LatLng(replaceStartLatLngOutSide.lat, replaceStartLatLngOutSide.lng),
                                new LatLng(subPath.get(0).lat, subPath.get(0).lng));
                        lengths.add(totalLength);
                        for (int k = 1; k < subPath.size(); k++) {
                            lengths.add(totalLength += AMapUtils.calculateLineDistance(
                                    new LatLng(subPath.get(k - 1).lat, subPath.get(k - 1).lng),
                                    new LatLng(subPath.get(k).lat, subPath.get(k).lng)));
                        }
                        totalLength += AMapUtils.calculateLineDistance(
                                new LatLng(subPath.get(subPath.size() - 1).lat, subPath.get(subPath.size() - 1).lng),
                                new LatLng(replaceEndLatLngOutSide.lat, replaceEndLatLngOutSide.lng));
                        for (int k = 0; k < lengths.size(); k++) {
                            middleProportion.add(replacedStartProportion + lengths.get(k) / totalLength * replacedTotalProportion);
                        }
                        List<Double> _middleProportion = new ArrayList<>(middleProportion);
                        _middleProportion.add(0, replacedStartProportion);
                        _middleProportion.add(replacedEndProportion);
                        //PrintUtils.printList(_middleProportion,TAG,"middleProportion");
                        // TODO: 2016/10/31 注意:middleProportion中第一个比例不能小于等于startProportion,最后一个不能大于等于endProportion

                        mRenderPath = new ArrayList<>();
                        mRenderPath.addAll(headRenderPath);
                        mRenderPath.addAll(subPath);
                        mRenderPath.addAll(tailRenderPath);

                        mProportionListRender = new ArrayList<>();
                        mProportionListRender.addAll(headProportion);
                        mProportionListRender.addAll(middleProportion);
                        mProportionListRender.addAll(tailProportion);

                        //PrintUtils.printList(mRenderPath,TAG,"mapping render path");
                        //PrintUtils.printList(mProportionListRender,TAG,"mapping proportion");
                        break;
                    }
                }
                break;
            }
        }
        //        HaloLogger.logE("tt__tt","screen start");
        //        for(LatLngOutSide latlng:mRenderPath){
        //            HaloLogger.logE("tt__tt",latlng.lat+","+latlng.lng);
        //        }
        //        HaloLogger.logE("tt__tt","screen end");
    }

    /**
     * 返回渲染路径
     *
     * @return
     */
    public List<LatLngOutSide> getRenderPath() {
        return mRenderPath;
    }

    /**
     * @param LatLngOutSide
     * @param curIndex
     * @return
     */
    public LatLngOutSide mapping(LatLngOutSide LatLngOutSide, int curIndex) {
        if (curIndex >= mProportionListOri.size() - 1) {
            return LatLngOutSide;
        }
        double startProp = mProportionListOri.get(curIndex);
        double endProp = mProportionListOri.get(curIndex + 1);
        LatLngOutSide startLatLngOutSide = mOriPath.get(curIndex);
        LatLngOutSide endLatLngOutSide = mOriPath.get(curIndex + 1);
        double curProp = startProp + (endProp - startProp) *
                (AMapUtils.calculateLineDistance(
                        new LatLng(startLatLngOutSide.lat, startLatLngOutSide.lng),
                        new LatLng(LatLngOutSide.lat, LatLngOutSide.lng)) /
                        AMapUtils.calculateLineDistance(
                                new LatLng(startLatLngOutSide.lat, startLatLngOutSide.lng),
                                new LatLng(endLatLngOutSide.lat, endLatLngOutSide.lng)));
        for (int i = 1; i < mProportionListRender.size(); i++) {
            double nextProp = mProportionListRender.get(i);
            LatLngOutSide nextLatLngOutSide = mRenderPath.get(i);
            if (nextProp >= curProp) {
                if (nextProp == curProp) {
                    return nextLatLngOutSide;
                } else {
                    double preProp = mProportionListRender.get(i - 1);
                    LatLngOutSide preLatLngOutSide = mRenderPath.get(i - 1);
                    double lat = preLatLngOutSide.lat + (nextLatLngOutSide.lat - preLatLngOutSide.lat) * ((curProp - preProp) / (nextProp - preProp));
                    double lng = preLatLngOutSide.lng + (nextLatLngOutSide.lng - preLatLngOutSide.lng) * ((curProp - preProp) / (nextProp - preProp));
                    return new LatLngOutSide(lat, lng);
                }
            }
        }
        return LatLngOutSide;
    }

    public ARWayProjection.PointD mappingV(LatLngOutSide LatLngOutSide, int curIndex) {
        if (curIndex >= mProportionListOri.size() - 1) {
            return ARWayProjection.toOpenGLLocation(LatLngOutSide, mDefaultLevel);
        }
        double startProp = mProportionListOri.get(curIndex);
        double endProp = mProportionListOri.get(curIndex + 1);
        LatLngOutSide startLatLngOutSide = mOriPath.get(curIndex);
        LatLngOutSide endLatLngOutSide = mOriPath.get(curIndex + 1);
        double curProp = startProp + (endProp - startProp) *
                (AMapUtils.calculateLineDistance(
                        new LatLng(startLatLngOutSide.lat, startLatLngOutSide.lng),
                        new LatLng(LatLngOutSide.lat, LatLngOutSide.lng)) /
                        AMapUtils.calculateLineDistance(
                                new LatLng(startLatLngOutSide.lat, startLatLngOutSide.lng),
                                new LatLng(endLatLngOutSide.lat, endLatLngOutSide.lng)));
        for (int i = 1; i < mProportionListRender.size(); i++) {
            double nextProp = mProportionListRender.get(i);
            LatLngOutSide nextLatLngOutSide = mRenderPath.get(i);
            if (nextProp >= curProp) {
                if (nextProp == curProp) {
                    return ARWayProjection.toOpenGLLocation(nextLatLngOutSide, mDefaultLevel);
                } else {
                    double preProp = mProportionListRender.get(i - 1);
                    LatLngOutSide preLatLngOutSide = mRenderPath.get(i - 1);
                    //double lat = preLatLngOutSide.latitude+(nextLatLngOutSide.latitude-preLatLngOutSide.latitude)*((curProp-preProp)/(nextProp-preProp));
                    //double lng = preLatLngOutSide.longitude+(nextLatLngOutSide.longitude-preLatLngOutSide.longitude)*((curProp-preProp)/(nextProp-preProp));
                    ARWayProjection.PointD prePD = ARWayProjection.toOpenGLLocation(preLatLngOutSide, mDefaultLevel);
                    ARWayProjection.PointD nextPD = ARWayProjection.toOpenGLLocation(nextLatLngOutSide, mDefaultLevel);
                    double x = prePD.x + (nextPD.x - prePD.x) * ((curProp - preProp) / (nextProp - preProp));
                    double y = prePD.y + (nextPD.y - prePD.y) * ((curProp - preProp) / (nextProp - preProp));
                    return new ARWayProjection.PointD(x, y);
                }
            }
        }
        return ARWayProjection.toOpenGLLocation(LatLngOutSide, mDefaultLevel);
    }

    /**
     * 根据在原始路径中的起始点和结束点,返回一段渲染路径
     *
     * @param start 原始路径的起点
     * @param end   原始路径的终点
     * @return
     */
    public List<LatLngOutSide> mapping(int start, int end) {
        double startProp = mProportionListOri.get(start >= mProportionListOri.size() - 1 ? mProportionListOri.size() - 2 : start);
        double endProp = mProportionListOri.get(end >= mProportionListOri.size() ? mProportionListOri.size() - 1 : end);
        for (int i = 0; i < mProportionListRender.size() - 1; i++) {
            double prop = mProportionListRender.get(i);
            if (prop >= startProp) {
                int startRender = prop == startProp ? i : i == 0 ? 0 : i - 1;
                for (int j = i; j < mProportionListRender.size(); j++) {
                    prop = mProportionListRender.get(j);
                    if (prop >= endProp) {
                        int endRender = j;
                        return mRenderPath.subList(startRender, endRender + 1);
                    }
                }
                break;
            }
        }
        return null;
    }

    public List<LatLngOutSide> getRenderPart(int start,int end){
        return mRenderPath.subList(start,end);
    }
    /**
     * 传入机动点的角标,返回蚯蚓线的Path
     * 默认长度是30m,前后各15m
     *
     * @param curIndexInPath
     * @return null表示没有蚯蚓线需要显示
     */
    public List<LatLngOutSide> mappingGuide(int curIndexInPath) {
        double GUILD_LENGTH = 80;
        if (curIndexInPath >= mProportionListOri.size() - 1) {
            return null;
        }
        double prop = mProportionListOri.get(curIndexInPath);
        List<LatLngOutSide> guildLine = new ArrayList<>();
        for (int i = 1; i < mProportionListRender.size(); i++) {
            double nextProp = mProportionListRender.get(i);
            if (nextProp >= prop) {
                LatLngOutSide nextLatLngOutSide = mRenderPath.get(i);
                double preProp = mProportionListRender.get(i - 1);
                LatLngOutSide preLatLngOutSide = mRenderPath.get(i - 1);
                double lat = preLatLngOutSide.lat + (nextLatLngOutSide.lat - preLatLngOutSide.lat) * ((prop - preProp) / (nextProp - preProp));
                double lng = preLatLngOutSide.lng + (nextLatLngOutSide.lng - preLatLngOutSide.lng) * ((prop - preProp) / (nextProp - preProp));
                LatLngOutSide curLatLngOutSide = new LatLngOutSide(lat, lng);
                //left
                double addUp = 0;
                for (int j = i - 1; j >= 0; j--) {
                    guildLine.add(0, mRenderPath.get(j));
                    if (j == i - 1) {
                        addUp += AMapUtils.calculateLineDistance(
                                new LatLng(curLatLngOutSide.lat, curLatLngOutSide.lng),
                                new LatLng(preLatLngOutSide.lat, preLatLngOutSide.lng));
                    } else {
                        addUp += AMapUtils.calculateLineDistance(
                                new LatLng(mRenderPath.get(j + 1).lat, mRenderPath.get(j + 1).lng),
                                new LatLng(mRenderPath.get(j).lat, mRenderPath.get(j).lng));
                    }
                    if (addUp >= GUILD_LENGTH / 2) {
                        break;
                    }
                }
                //right
                addUp = 0;
                for (int j = i; j < mRenderPath.size(); j++) {
                    guildLine.add(mRenderPath.get(j));
                    if (j == i) {
                        addUp += AMapUtils.calculateLineDistance(
                                new LatLng(curLatLngOutSide.lat, curLatLngOutSide.lng),
                                new LatLng(nextLatLngOutSide.lat, nextLatLngOutSide.lng));
                    } else {
                        addUp += AMapUtils.calculateLineDistance(
                                new LatLng(mRenderPath.get(j).lat, mRenderPath.get(j).lng),
                                new LatLng(mRenderPath.get(j - 1).lat, mRenderPath.get(j - 1).lng));
                    }
                    if (addUp >= GUILD_LENGTH / 2) {
                        break;
                    }
                }
                break;
            }
        }
        return guildLine;
    }

    /**
     * 传入机动点的角标,返回蚯蚓线的Path
     * 默认长度是30m,前后各15m
     *
     * @param curIndexInPath
     * @return null表示没有蚯蚓线需要显示
     */
    public List<ARWayProjection.PointD> mappingGuideV(int curIndexInPath) {
        double GUILD_LENGTH = 80;
        if (curIndexInPath >= mProportionListOri.size() - 1) {
            return null;
        }
        double prop = mProportionListOri.get(curIndexInPath);
        List<ARWayProjection.PointD> guildLine = new ArrayList<>();
        for (int i = 1; i < mProportionListRender.size(); i++) {
            double nextProp = mProportionListRender.get(i);
            if (nextProp >= prop) {
                LatLngOutSide nextLatLngOutSide = mRenderPath.get(i);
                double preProp = mProportionListRender.get(i - 1);
                LatLngOutSide preLatLngOutSide = mRenderPath.get(i - 1);
                double lat = preLatLngOutSide.lat + (nextLatLngOutSide.lat - preLatLngOutSide.lat) * ((prop - preProp) / (nextProp - preProp));
                double lng = preLatLngOutSide.lng + (nextLatLngOutSide.lng - preLatLngOutSide.lng) * ((prop - preProp) / (nextProp - preProp));
                LatLngOutSide curLatLngOutSide = new LatLngOutSide(lat, lng);
                //left
                double addUp = 0;
                for (int j = i - 1; j >= 0; j--) {
                    guildLine.add(0, ARWayProjection.toOpenGLLocation(mRenderPath.get(j), mDefaultLevel));
                    double dist;
                    if (j == i - 1) {
                        addUp += (dist = AMapUtils.calculateLineDistance(
                                new LatLng(curLatLngOutSide.lat, curLatLngOutSide.lng),
                                new LatLng(mRenderPath.get(j).lat, mRenderPath.get(j).lng)));
                    } else {
                        addUp += (dist = AMapUtils.calculateLineDistance(
                                new LatLng(mRenderPath.get(j + 1).lat, mRenderPath.get(j + 1).lng),
                                new LatLng(mRenderPath.get(j).lat, mRenderPath.get(j).lng)));
                    }
                    if (addUp >= GUILD_LENGTH / 2) {
                        if (addUp > GUILD_LENGTH / 2) {
                            ARWayProjection.PointD prePD = j == i - 1 ? ARWayProjection.toOpenGLLocation(curLatLngOutSide, mDefaultLevel) : guildLine.get(1);
                            ARWayProjection.PointD nextPD = guildLine.remove(0);
                            double scale = 1 - ((addUp - GUILD_LENGTH / 2) / dist);
                            double x = prePD.x + (nextPD.x - prePD.x) * scale;
                            double y = prePD.y + (nextPD.y - prePD.y) * scale;
                            ARWayProjection.PointD makePD = new ARWayProjection.PointD(x, y);
                            guildLine.add(0, makePD);
                        }
                        break;
                    }
                }
                //right
                addUp = 0;
                for (int j = i; j < mRenderPath.size(); j++) {
                    guildLine.add(ARWayProjection.toOpenGLLocation(mRenderPath.get(j), mDefaultLevel));
                    double dist;
                    if (j == i) {
                        addUp += (dist = AMapUtils.calculateLineDistance(
                                new LatLng(curLatLngOutSide.lat, curLatLngOutSide.lng),
                                new LatLng(mRenderPath.get(j).lat, mRenderPath.get(j).lng)));
                    } else {
                        addUp += (dist = AMapUtils.calculateLineDistance(
                                new LatLng(mRenderPath.get(j).lat, mRenderPath.get(j).lng),
                                new LatLng(mRenderPath.get(j - 1).lat, mRenderPath.get(j - 1).lng)));
                    }
                    if (addUp >= GUILD_LENGTH / 2) {
                        if (addUp > GUILD_LENGTH / 2) {
                            ARWayProjection.PointD prePD = j == i ? ARWayProjection.toOpenGLLocation(curLatLngOutSide, mDefaultLevel) : guildLine.get(guildLine.size() - 2);
                            ARWayProjection.PointD nextPD = guildLine.remove(guildLine.size() - 1);
                            double scale = 1 - ((addUp - GUILD_LENGTH / 2) / dist);
                            double x = prePD.x + (nextPD.x - prePD.x) * scale;
                            double y = prePD.y + (nextPD.y - prePD.y) * scale;
                            ARWayProjection.PointD makePD = new ARWayProjection.PointD(x, y);
                            guildLine.add(makePD);
                        }
                        break;
                    }
                }
                break;
            }
        }
        return guildLine;
    }

    /**
     * TODO : 是否需要这一步??
     * 将一个旧的中心点指向到替换后的部分主路的新中心点上
     * 在调用蚯蚓线的映射函数时需要使用到该部分对应关系
     *
     * @param oldCenterIndexInPath
     * @param newCenterIndexInPart
     */
    public void mappingC(int oldCenterIndexInPath, int newCenterIndexInPart) {

    }

    /**
     * 通过传入一个LatLng,返回该LatLng在渲染路径中的下标
     *
     * @param destLatLng
     * @return
     */
    public int mapping(LatLngOutSide destLatLng) {
        int index = 0;
        for (LatLngOutSide latlng : mRenderPath) {
            if (Math.abs(latlng.lat - destLatLng.lat) <= X && Math.abs(latlng.lng - destLatLng.lng) <= X) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * 根据当前位置向后去N距离的路径
     * @param curIndexInPath
     * @param length
     * @return
     */
    public List<ARWayProjection.PointD> mappingPart(int curIndexInPath, int length) {
        double GUILD_LENGTH = length*2;
        if (curIndexInPath >= mProportionListOri.size() - 1) {
            return null;
        }
        double prop = mProportionListOri.get(curIndexInPath);
        List<ARWayProjection.PointD> partPath = new ArrayList<>();
        for (int i = 1; i < mProportionListRender.size(); i++) {
            double nextProp = mProportionListRender.get(i);
            if (nextProp >= prop) {
                LatLngOutSide nextLatLngOutSide = mRenderPath.get(i);
                double preProp = mProportionListRender.get(i - 1);
                LatLngOutSide preLatLngOutSide = mRenderPath.get(i - 1);
                double lat = preLatLngOutSide.lat + (nextLatLngOutSide.lat - preLatLngOutSide.lat) * ((prop - preProp) / (nextProp - preProp));
                double lng = preLatLngOutSide.lng + (nextLatLngOutSide.lng - preLatLngOutSide.lng) * ((prop - preProp) / (nextProp - preProp));
                LatLngOutSide curLatLngOutSide = new LatLngOutSide(lat, lng);
                //left
                double addUp = 0;
                for (int j = i - 1; j >= 0; j--) {
                    partPath.add(0, ARWayProjection.toOpenGLLocation(mRenderPath.get(j), mDefaultLevel));
                    double dist;
                    if (j == i - 1) {
                        addUp += (dist = AMapUtils.calculateLineDistance(
                                new LatLng(curLatLngOutSide.lat, curLatLngOutSide.lng),
                                new LatLng(mRenderPath.get(j).lat, mRenderPath.get(j).lng)));
                    } else {
                        addUp += (dist = AMapUtils.calculateLineDistance(
                                new LatLng(mRenderPath.get(j + 1).lat, mRenderPath.get(j + 1).lng),
                                new LatLng(mRenderPath.get(j).lat, mRenderPath.get(j).lng)));
                    }
                    if (addUp >= GUILD_LENGTH / 2) {
                        if (addUp > GUILD_LENGTH / 2) {
                            ARWayProjection.PointD prePD = j == i - 1 ? ARWayProjection.toOpenGLLocation(curLatLngOutSide, mDefaultLevel) : partPath.get(1);
                            ARWayProjection.PointD nextPD = partPath.remove(0);
                            double scale = 1 - ((addUp - GUILD_LENGTH / 2) / dist);
                            double x = prePD.x + (nextPD.x - prePD.x) * scale;
                            double y = prePD.y + (nextPD.y - prePD.y) * scale;
                            ARWayProjection.PointD makePD = new ARWayProjection.PointD(x, y);
                            partPath.add(0, makePD);
                        }
                        break;
                    }
                }
                //right
                addUp = 0;
                for (int j = i; j < mRenderPath.size(); j++) {
                    partPath.add(ARWayProjection.toOpenGLLocation(mRenderPath.get(j), mDefaultLevel));
                    double dist;
                    if (j == i) {
                        addUp += (dist = AMapUtils.calculateLineDistance(
                                new LatLng(curLatLngOutSide.lat, curLatLngOutSide.lng),
                                new LatLng(mRenderPath.get(j).lat, mRenderPath.get(j).lng)));
                    } else {
                        addUp += (dist = AMapUtils.calculateLineDistance(
                                new LatLng(mRenderPath.get(j).lat, mRenderPath.get(j).lng),
                                new LatLng(mRenderPath.get(j - 1).lat, mRenderPath.get(j - 1).lng)));
                    }
                    if (addUp >= GUILD_LENGTH / 2) {
                        if (addUp > GUILD_LENGTH / 2) {
                            ARWayProjection.PointD prePD = j == i ? ARWayProjection.toOpenGLLocation(curLatLngOutSide, mDefaultLevel) : partPath.get(partPath.size() - 2);
                            ARWayProjection.PointD nextPD = partPath.remove(partPath.size() - 1);
                            double scale = 1 - ((addUp - GUILD_LENGTH / 2) / dist);
                            double x = prePD.x + (nextPD.x - prePD.x) * scale;
                            double y = prePD.y + (nextPD.y - prePD.y) * scale;
                            ARWayProjection.PointD makePD = new ARWayProjection.PointD(x, y);
                            partPath.add(makePD);
                        }
                        break;
                    }
                }
                break;
            }
        }
        return partPath;
    }
}
