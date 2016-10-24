package com.haloai.hud.hudendpoint.arwaylib.render.strategy;

import com.amap.api.navi.enums.RoadClass;

/**
 * @author Created by Mo Bing(mobing@haloai.com) on 23/10/2016.
 */
public interface IRenderStrategy {

    enum DataLevel {
        LEVEL_20,
        LEVEL_18,
        LEVEL_16,
        LEVEL_15,
        LEVEL_14,
        LEVEL_13,
        LEVEL_12
    }

    //渲染策略输入参数
    class HaloRoadClass extends RoadClass { }//道路等级参数，重用高德的道路等级划分
    int getRoadClass(int _roadClassSDK);//将SDK的道路等级转换成我们自定义的道路等级
    void updateCurrentRoadInfo(int roadClass, int mpDistance);//更新当前进入的道路等级和距离下一个机动点的距离

    //渲染策略输出
    class RenderParams {
        public RenderParams(DataLevel dataLevel, double glCameraAngle) {
            this.dataLevel = dataLevel;
            this.glCameraAngle = glCameraAngle;
        }

        public IRenderStrategy.DataLevel dataLevel;
        public double glCameraAngle;
    }
    RenderParams getCurrentRenderParams();

    interface RenderParamsNotifier {
        void onRenderParamsUpdated(RenderParams renderParams);
    }
    void setRenderParamsNotifier(RenderParamsNotifier renderParamsNotifier);


}
