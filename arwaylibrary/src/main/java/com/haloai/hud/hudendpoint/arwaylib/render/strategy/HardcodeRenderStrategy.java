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
    public void updateCurrentRoadInfo(int _roadClassSDK, int mpDistance,int pathDistance) {
        int roadClass = getRoadClass(_roadClassSDK);

        if (this.renderParamsNotifier == null) {
            //Placeholder
            return;
        }
        if(pathDistance<100){
            currentGLCameraAngle = 10F;
            currentGLScale = 1.5;
            currentGLInScreenProportion = 0.6;
            offset = 0;
            renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(), IRenderStrategy.OFFSET_TYPE | IRenderStrategy.ANGLE_TYPE | IRenderStrategy.INSCREENPROPORTION_TYPE | IRenderStrategy.SCALE_TYPE, 2.0);
            return;
        }
        if (mpDistance >150) {
            if (currentDataLevel != DataLevel.LEVEL_20 || currentGLScale != 3.9) {
                currentDataLevel = DataLevel.LEVEL_20;
                currentGLScale = 3.9;
                renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(), IRenderStrategy.SCALE_TYPE, 3.0);
            }
            if (currentGLCameraAngle != 45.0) {
                currentGLCameraAngle = 45.0;
                //renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(), IRenderStrategy.ANGLE_TYPE, 1.5);
            }
            if (currentGLInScreenProportion != 0.0) {
                currentGLInScreenProportion = 0.0;
                //renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(), IRenderStrategy.INSCREENPROPORTION_TYPE, 3.0);
            }
            renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(),IRenderStrategy.ANGLE_TYPE|IRenderStrategy.INSCREENPROPORTION_TYPE,1.5);
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
        }else if (mpDistance <=100 && mpDistance >50){
            if (currentDataLevel != DataLevel.LEVEL_20 || currentGLScale != 1.5) {
                currentDataLevel = DataLevel.LEVEL_20;
                currentGLScale = 1.5;
                //renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(), IRenderStrategy.SCALE_TYPE, 3.0);
            }
            if (currentGLCameraAngle != 90.0- Math.toDegrees(Math.atan(4.0/7.46))-22.5) {
                currentGLCameraAngle = 90.0- Math.toDegrees(Math.atan(4.0/7.46))-22.5;
                //renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(), IRenderStrategy.ANGLE_TYPE, 2.0);
            }
            if (currentGLInScreenProportion != 0.25) {
                currentGLInScreenProportion = 0.25;
                // renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(), IRenderStrategy.INSCREENPROPORTION_TYPE, 3.0);
            }
            renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(),IRenderStrategy.SCALE_TYPE|IRenderStrategy.INSCREENPROPORTION_TYPE|IRenderStrategy.ANGLE_TYPE,2.0);
        }else if (mpDistance <=50){
            if (currentDataLevel != DataLevel.LEVEL_20 || currentGLScale != 1.0) {
                currentDataLevel = DataLevel.LEVEL_20;
                currentGLScale = 1.0;
                //renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(), IRenderStrategy.SCALE_TYPE, 3.0);
            }
            if (currentGLInScreenProportion != 0.5) {
                currentGLInScreenProportion = 0.5;
                //renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(), IRenderStrategy.INSCREENPROPORTION_TYPE, 2.0);
            }
            renderParamsNotifier.onRenderParamsUpdated(getCurrentRenderParams(),IRenderStrategy.SCALE_TYPE|IRenderStrategy.INSCREENPROPORTION_TYPE,2.0);
        }





    }


    @Override
    public void updateAnimation(AnimationType type) {
        renderParamsNotifier.onAnimationUpdated(type);
    }

    @Override
    public RenderParams getCurrentRenderParams() {
        return getDefaultRenderParams();
    }



}
