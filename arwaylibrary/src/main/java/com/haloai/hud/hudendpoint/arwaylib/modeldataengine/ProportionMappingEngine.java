package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import android.graphics.PointF;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayProjection;
import com.haloai.hud.hudendpoint.arwaylib.utils.Douglas;
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

    private List<LatLng> mOriPath;//原始的路径
    private List<LatLng> mRenderPath;//实际渲染路径
    private List<Double> mProportionListOri;//原始路径的中的比例关系
    private List<Double> mProportionListRender;//实际渲染路径中的比例关系
    private double       mTolerance;
    private int          mDefaultLevel;

    /**
     * 构造函数需要传入初始的路径,并进行一些初始化的操作
     *
     * @param _oriPath
     */
    public ProportionMappingEngine(List<LatLng> _oriPath) {
        mOriPath = new ArrayList<>(_oriPath);
        mRenderPath = new ArrayList<>(_oriPath);
        mProportionListOri = new ArrayList<>();
        List<Double> lengths = new ArrayList<>();
        lengths.add(0D);
        double totalLength = 0;
        for (int i = 1; i < mOriPath.size(); i++) {
            totalLength += AMapUtils.calculateLineDistance(mOriPath.get(i - 1), mOriPath.get(i));
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
        List<LatLng> renderPath = new ArrayList<>(mRenderPath);
        List<Double> proportionListRender = new ArrayList<>(mProportionListRender);
        mRenderPath = new ArrayList<>();
        mProportionListRender = new ArrayList<>();

        /*List<Integer> keepInRarefy = new ArrayList<>();
        List<PointF> vertices = new ArrayList<>();
        for(LatLng latlng:renderPath){
            ARWayProjection.PointD pd = ARWayProjection.toOpenGLLocation(latlng, defaultLevel);
            vertices.add(new PointF((float)pd.x,(float)pd.y));
        }
        Douglas.rarefyGetIndexs(keepInRarefy,vertices,tolerance);
        for(int i=0;i<keepInRarefy.size();i++){
            HaloLogger.logE(TAG,"index:"+keepInRarefy.get(i));
            mRenderPath.add(renderPath.get(keepInRarefy.get(i)));
            mProportionListRender.add(proportionListRender.get(keepInRarefy.get(i)));
        }*/

        for (int i = 0; i < keepIndexs.size() - 1; i++) {
            int start = keepIndexs.get(i);
            int end = keepIndexs.get(i + 1) + 1;
            List<LatLng> partPath = renderPath.subList(start, end);
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

    private void rarefy(double tolerance, int defaultLevel, List<LatLng> partPath, List<Integer> keepInRarefy) {
        List<PointF> vertices = new ArrayList<>();
        for (LatLng latlng : partPath) {
            ARWayProjection.PointD pd = ARWayProjection.toOpenGLLocation(latlng, defaultLevel);
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
     * @param crossPointIndexs_ 需要在抽析中被保留的点
     */
    public void mapping(List<LatLng> subPath, int start, int end, List<Integer> crossPointIndexs_) {
        //首先对subPath进行抽析处理
        List<Integer> crossPointIndexs = new ArrayList<>(crossPointIndexs_);
        crossPointIndexs.add(0, 0);
        crossPointIndexs.add(subPath.size() - 1);
        List<LatLng> temp = new ArrayList<>(subPath);
        subPath = new ArrayList<>();
        for (int i = 0; i < crossPointIndexs.size() - 1; i++) {
            int _start = crossPointIndexs.get(i);
            int _end = crossPointIndexs.get(i + 1) + 1;
            List<LatLng> partPath = temp.subList(_start, _end);
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

        double startProp = mProportionListOri.get(start);
        double endProp = mProportionListOri.get(end);
        for (int i = 0; i < mProportionListRender.size(); i++) {
            double prop = mProportionListRender.get(i);
            if (Math.abs(prop - startProp) <= X || prop > startProp) {
                int replaceStart = i == 0 ? 0 : i - 1;
                for (int j = i; j < mProportionListRender.size(); j++) {
                    prop = mProportionListRender.get(j);
                    if (Math.abs(prop - endProp) <= X || prop > endProp) {
                        int replaceEnd = j;
                        List<LatLng> headRenderPath = mRenderPath.subList(0, replaceStart + 1);
                        List<LatLng> tailRenderPath = mRenderPath.subList(replaceEnd, mRenderPath.size());
                        List<Double> headProportion = mProportionListRender.subList(0, replaceStart + 1);
                        List<Double> tailProportion = mProportionListRender.subList(replaceEnd, mProportionListRender.size());
                        double replacedStartProportion = mProportionListRender.get(replaceStart);
                        double replacedEndProportion = mProportionListRender.get(replaceEnd);
                        double replacedTotalProportion = replacedEndProportion - replacedStartProportion;
                        LatLng replaceStartLatLng = mRenderPath.get(replaceStart);
                        LatLng replaceEndLatLng = mRenderPath.get(replaceEnd);
                        List<Double> middleProportion = new ArrayList<>();
                        List<Double> lengths = new ArrayList<>();
                        double totalLength = 0;
                        totalLength += AMapUtils.calculateLineDistance(replaceStartLatLng, subPath.get(0));
                        lengths.add(totalLength);
                        for (int k = 1; k < subPath.size(); k++) {
                            lengths.add(totalLength += AMapUtils.calculateLineDistance(subPath.get(k - 1), subPath.get(k)));
                        }
                        totalLength += AMapUtils.calculateLineDistance(subPath.get(subPath.size() - 1), replaceEndLatLng);
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
    }

    /**
     * 返回渲染路径
     *
     * @return
     */
    public List<LatLng> getRenderPath() {
        return mRenderPath;
    }

    public List<LatLng> getRenderPath(int start, int end) {
        return mRenderPath.subList(start, end);
    }

    /**
     * @param latlng
     * @param curIndex
     * @return
     */
    public LatLng mapping(LatLng latlng, int curIndex) {
        if (curIndex >= mProportionListOri.size() - 1) {
            return latlng;
        }
        double startProp = mProportionListOri.get(curIndex);
        double endProp = mProportionListOri.get(curIndex + 1);
        LatLng startLatLng = mOriPath.get(curIndex);
        LatLng endLatLng = mOriPath.get(curIndex + 1);
        double curProp = startProp + (endProp - startProp) *
                (AMapUtils.calculateLineDistance(startLatLng, latlng) / AMapUtils.calculateLineDistance(startLatLng, endLatLng));
        for (int i = 1; i < mProportionListRender.size(); i++) {
            double nextProp = mProportionListRender.get(i);
            LatLng nextLatLng = mRenderPath.get(i);
            if (nextProp >= curProp) {
                if (nextProp == curProp) {
                    return nextLatLng;
                } else {
                    double preProp = mProportionListRender.get(i - 1);
                    LatLng preLatLng = mRenderPath.get(i - 1);
                    double lat = preLatLng.latitude + (nextLatLng.latitude - preLatLng.latitude) * ((curProp - preProp) / (nextProp - preProp));
                    double lng = preLatLng.longitude + (nextLatLng.longitude - preLatLng.longitude) * ((curProp - preProp) / (nextProp - preProp));
                    return new LatLng(lat, lng);
                }
            }
        }
        return latlng;
    }

    public ARWayProjection.PointD mappingV(LatLng latlng, int curIndex) {
        if (curIndex >= mProportionListOri.size() - 1) {
            return ARWayProjection.toOpenGLLocation(latlng, mDefaultLevel);
        }
        double startProp = mProportionListOri.get(curIndex);
        double endProp = mProportionListOri.get(curIndex + 1);
        LatLng startLatLng = mOriPath.get(curIndex);
        LatLng endLatLng = mOriPath.get(curIndex + 1);
        double curProp = startProp + (endProp - startProp) *
                (AMapUtils.calculateLineDistance(startLatLng, latlng) / AMapUtils.calculateLineDistance(startLatLng, endLatLng));
        for (int i = 1; i < mProportionListRender.size(); i++) {
            double nextProp = mProportionListRender.get(i);
            LatLng nextLatLng = mRenderPath.get(i);
            if (nextProp >= curProp) {
                if (nextProp == curProp) {
                    return ARWayProjection.toOpenGLLocation(nextLatLng, mDefaultLevel);
                } else {
                    double preProp = mProportionListRender.get(i - 1);
                    LatLng preLatLng = mRenderPath.get(i - 1);
                    //double lat = preLatLng.latitude+(nextLatLng.latitude-preLatLng.latitude)*((curProp-preProp)/(nextProp-preProp));
                    //double lng = preLatLng.longitude+(nextLatLng.longitude-preLatLng.longitude)*((curProp-preProp)/(nextProp-preProp));
                    ARWayProjection.PointD prePD = ARWayProjection.toOpenGLLocation(preLatLng, mDefaultLevel);
                    ARWayProjection.PointD nextPD = ARWayProjection.toOpenGLLocation(nextLatLng, mDefaultLevel);
                    double x = prePD.x + (nextPD.x - prePD.x) * ((curProp - preProp) / (nextProp - preProp));
                    double y = prePD.y + (nextPD.y - prePD.y) * ((curProp - preProp) / (nextProp - preProp));
                    return new ARWayProjection.PointD(x, y);
                }
            }
        }
        return ARWayProjection.toOpenGLLocation(latlng, mDefaultLevel);
    }

    /**
     * 传入机动点的角标,返回蚯蚓线的Path
     * 默认长度是30m,前后各15m
     *
     * @param curIndexInPath
     * @return null表示没有蚯蚓线需要显示
     */
    public List<LatLng> mappingGuide(int curIndexInPath) {
        double GUILD_LENGTH = 80;
        if (curIndexInPath >= mProportionListOri.size() - 1) {
            return null;
        }
        double prop = mProportionListOri.get(curIndexInPath);
        List<LatLng> guildLine = new ArrayList<>();
        for (int i = 1; i < mProportionListRender.size(); i++) {
            double nextProp = mProportionListRender.get(i);
            if (nextProp >= prop) {
                LatLng nextLatLng = mRenderPath.get(i);
                double preProp = mProportionListRender.get(i - 1);
                LatLng preLatLng = mRenderPath.get(i - 1);
                double lat = preLatLng.latitude + (nextLatLng.latitude - preLatLng.latitude) * ((prop - preProp) / (nextProp - preProp));
                double lng = preLatLng.longitude + (nextLatLng.longitude - preLatLng.longitude) * ((prop - preProp) / (nextProp - preProp));
                LatLng curLatLng = new LatLng(lat, lng);
                //left
                double addUp = 0;
                for (int j = i - 1; j >= 0; j--) {
                    guildLine.add(0, mRenderPath.get(j));
                    if (j == i - 1) {
                        addUp += AMapUtils.calculateLineDistance(curLatLng, preLatLng);
                    } else {
                        addUp += AMapUtils.calculateLineDistance(mRenderPath.get(j + 1), mRenderPath.get(j));
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
                        addUp += AMapUtils.calculateLineDistance(curLatLng, nextLatLng);
                    } else {
                        addUp += AMapUtils.calculateLineDistance(mRenderPath.get(j), mRenderPath.get(j-1));
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
}
