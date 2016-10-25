package com.haloai.hud.hudendpoint.arwaylib.render.strategy;

/**
 * @author Created by Mo Bing(mobing@haloai.com) on 23/10/2016.
 */
public class HardcodeRenderStrategy extends RenderStrategy {

    @Override
    public int getRoadClass(int _roadClassSDK) {
        //高德不需要做转换,如果是其他SDK此处需要做SDK的roadClass和我们自定义的RoadClass之间的转换
        return _roadClassSDK;
    }

    @Override
    public void updateCurrentRoadInfo(int _roadClassSDK, int mpDistance) {
        int roadClass = getRoadClass(_roadClassSDK);
        boolean needsUpdate = true;
        if (mpDistance > 300){
            if (currentDataLevel != DataLevel.LEVEL_16){
                currentDataLevel = DataLevel.LEVEL_16;
                currentGLCameraAngle = 60;
                currentGLScale = 1.0;
                currentGLInScreenProportion = 0.0;
                currentGLRoadWidth = 0.25;
            }else {
                needsUpdate = false;
            }
        }else if (mpDistance > 100 && mpDistance <= 300){
            if (currentDataLevel != DataLevel.LEVEL_18){
                currentDataLevel = DataLevel.LEVEL_18;
                currentGLCameraAngle = 30;
                currentGLScale = 1.0;
                currentGLInScreenProportion = 0;
                currentGLRoadWidth = 0.5;
            }else {
                needsUpdate = false;
            }
        }else {
            if (currentDataLevel != DataLevel.LEVEL_20){
                currentDataLevel = DataLevel.LEVEL_20;
                currentGLCameraAngle = 0;
                currentGLScale = 1.0;
                currentGLInScreenProportion = 0;
                currentGLRoadWidth = 1;
            }else {
                needsUpdate = false;
            }
        }
        if (needsUpdate && this.renderParamsNotifier != null) {
            //Placeholder
            this.renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams());
        }
    }

    @Override
    public RenderParams getCurrentRenderParams() {
        return getDefaultRenderParams();
    }



}
