package com.haloai.hud.hudendpoint.arwaylib.render.strategy;

import com.amap.api.navi.enums.RoadClass;

/**
 * @author Created by Mo Bing(mobing@haloai.com) on 23/10/2016.
 */
public interface IRenderStrategy {

    enum DataLevel {
        LEVEL_20(20),
        LEVEL_19(19),
        LEVEL_18(18),
        LEVEL_17(17),
        LEVEL_16(16),
        LEVEL_15(15),
        LEVEL_14(14),
        LEVEL_13(13),
        LEVEL_12(12);

        int nCode;
        DataLevel(int _nCode){
            this.nCode = _nCode;
        }
        public int getLevel(){
            return this.nCode;
        }
    }



    //渲染策略输入参数
    class HaloRoadClass extends RoadClass { }//道路等级参数，重用高德的道路等级划分
    int getRoadClass(int _roadClassSDK);//将SDK的道路等级转换成我们自定义的道路等级
    void updateCurrentRoadInfo(int roadClass, int mpDistance);//更新当前进入的道路等级和距离下一个机动点的距离

    //渲染策略输出
    class RenderParams {
        public RenderParams(DataLevel dataLevel, double glCameraAngle ,double glScale,double glInScreenProportion) {
            this.dataLevel = dataLevel;
            this.glCameraAngle = glCameraAngle;
            this.glScale = glScale;
            this.glInScreenProportion = glInScreenProportion;
        }

        public IRenderStrategy.DataLevel dataLevel;
        public double glCameraAngle;
        public double glScale;
        public double glInScreenProportion;
    }
    RenderParams getCurrentRenderParams();

    interface RenderParamsNotifier {
        void onRenderParamsUpdated(RenderParams renderParams);
    }
    void setRenderParamsNotifier(RenderParamsNotifier renderParamsNotifier);


}
