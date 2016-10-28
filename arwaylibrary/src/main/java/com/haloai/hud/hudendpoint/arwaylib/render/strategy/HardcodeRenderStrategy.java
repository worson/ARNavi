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

        if (this.renderParamsNotifier == null) {
            //Placeholder
            return;
        }
        /*
        boolean needsUpdate = false;
        //this.renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams());
        if (mpDistance >100){
            if (currentDataLevel != DataLevel.LEVEL_20){
                currentDataLevel = DataLevel.LEVEL_20;
                needsUpdate = true;
            }
            if (currentGLCameraAngle != 45.0){
                currentGLCameraAngle = 45.0;
                needsUpdate = true;
            }
            if (currentGLScale != 3.8){
                currentGLScale = 3.8;
                needsUpdate = true;
            }
            if (currentGLInScreenProportion != 0.0){
                currentGLInScreenProportion = 0.0;
                needsUpdate = true;
            }

        }else {
            if (currentDataLevel != DataLevel.LEVEL_20) {
                currentDataLevel = DataLevel.LEVEL_20;
                needsUpdate = true;
            }
            if (currentGLCameraAngle != 35.0) {
                currentGLCameraAngle = 35.0;
                needsUpdate = true;
            }
            if (currentGLScale != 1.0) {
                currentGLScale = 1.0;
                needsUpdate = true;
            }
            if (currentGLInScreenProportion != 0.5){
                currentGLInScreenProportion = 0.5;
                needsUpdate = true;
            }
        }
       */



        if (mpDistance >150) {
            if (currentDataLevel != DataLevel.LEVEL_20 || currentGLScale != 3.8) {
                currentDataLevel = DataLevel.LEVEL_20;
                currentGLScale = 3.8;
                //renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(), IRenderStrategy.SCALE_TYPE, 3.0);
            }
            if (currentGLCameraAngle != 45.0) {
                currentGLCameraAngle = 45.0;
                //renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(), IRenderStrategy.ANGLE_TYPE, 3.0);
            }
            if (currentGLInScreenProportion != 0.0) {
                currentGLInScreenProportion = 0.0;
                //renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(), IRenderStrategy.INSCREENPROPORTION_TYPE, 3.0);
            }
            renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(),IRenderStrategy.SCALE_TYPE|IRenderStrategy.INSCREENPROPORTION_TYPE|IRenderStrategy.ANGLE_TYPE,3.0);
        } else if (mpDistance <=150 && mpDistance >100){
                if (currentDataLevel != DataLevel.LEVEL_20 || currentGLScale != 2.0) {
                    currentDataLevel = DataLevel.LEVEL_20;
                    currentGLScale = 2.0;
                    //renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(), IRenderStrategy.SCALE_TYPE, 3.0);
                }
            if (currentGLInScreenProportion != 0.0) {
                currentGLInScreenProportion = 0.0;
               // renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(), IRenderStrategy.INSCREENPROPORTION_TYPE, 3.0);
            }
            if (currentGLCameraAngle != 45.0) {
                currentGLCameraAngle = 45.0;
                //renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(), IRenderStrategy.ANGLE_TYPE, 3.0);
            }
            renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(),IRenderStrategy.SCALE_TYPE|IRenderStrategy.INSCREENPROPORTION_TYPE|IRenderStrategy.ANGLE_TYPE,2.0);
        }else if (mpDistance <=100){
            if (currentDataLevel != DataLevel.LEVEL_20 || currentGLScale != 1.0) {
                currentDataLevel = DataLevel.LEVEL_20;
                currentGLScale = 1.0;
                //renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(), IRenderStrategy.SCALE_TYPE, 3.0);
            }
            if (currentGLCameraAngle != 90.0- Math.toDegrees(Math.atan(4/7.46))-20.0) {
                currentGLCameraAngle = 90.0- Math.toDegrees(Math.atan(4/7.46))-20.0;
                //renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(), IRenderStrategy.ANGLE_TYPE, 2.0);
            }
            if (currentGLInScreenProportion != 0.5) {
                currentGLInScreenProportion = 0.5;
                // renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(), IRenderStrategy.INSCREENPROPORTION_TYPE, 3.0);
                renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(),IRenderStrategy.SCALE_TYPE|IRenderStrategy.INSCREENPROPORTION_TYPE|IRenderStrategy.ANGLE_TYPE,3.0);
            }

        }





    }






    @Override
    public RenderParams getCurrentRenderParams() {
        return getDefaultRenderParams();
    }



}
