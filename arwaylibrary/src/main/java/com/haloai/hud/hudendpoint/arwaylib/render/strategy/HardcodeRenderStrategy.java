package com.haloai.hud.hudendpoint.arwaylib.render.strategy;

import java.util.logging.Level;

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
        boolean needsUpdate = false;

        if (mpDistance >1000){
            if (currentDataLevel != DataLevel.LEVEL_17){
                currentDataLevel = DataLevel.LEVEL_17;
                needsUpdate = true;
            }
            if (currentGLCameraAngle != 60){
                currentGLCameraAngle = 60;
                needsUpdate = true;
            }

        }else if (mpDistance <= 1000 && mpDistance >500){
            if (currentDataLevel != DataLevel.LEVEL_18){
                currentDataLevel = DataLevel.LEVEL_18;
                needsUpdate = true;
            }
            if (currentGLCameraAngle != 60){
                currentGLCameraAngle = 60;
                needsUpdate = true;
            }


        }else if (mpDistance <=500&&mpDistance >100){
            if (currentDataLevel != DataLevel.LEVEL_19){
                currentDataLevel = DataLevel.LEVEL_19;
                needsUpdate = true;
            }
            if (mpDistance <= 300 && mpDistance >100){
                if (currentGLCameraAngle != 60){
                    currentGLCameraAngle =60;
                    needsUpdate = true;
                }
            }


        }else if (mpDistance <=100){
            if (currentDataLevel != DataLevel.LEVEL_20){
                currentDataLevel = DataLevel.LEVEL_20;
                needsUpdate = true;
                if (currentGLCameraAngle != 60){
                    currentGLCameraAngle = 60;
                    needsUpdate = true;
                }
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
