package com.haloai.hud.hudendpoint.arwaylib.render.strategy;

import com.amap.api.navi.enums.RoadClass;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.utils.HaloLogger;

/**
 * @author Created by Mo Bing(mobing@haloai.com) on 23/10/2016.
 */
public abstract class RenderStrategy implements IRenderStrategy {
    protected RenderParamsNotifier renderParamsNotifier;


    protected DataLevel currentDataLevel = DataLevel.LEVEL_20;
    protected double currentGLCameraAngle = 0.0;
    protected double currentGLScale = 4.0;
    protected double currentGLInScreenProportion = 0.0;
    protected double offset = 0.0;

    @Override
    public void setRenderParamsNotifier(RenderParamsNotifier renderParamsNotifier) {
        this.renderParamsNotifier = renderParamsNotifier;
    }

    public RenderParams getDefaultRenderParams() {
        //return the default params
        return new RenderParams(currentDataLevel, currentGLCameraAngle,currentGLScale,currentGLInScreenProportion,offset);
    }

    @Override
    public void reset() {
        currentGLCameraAngle = 10F;
        currentGLScale = 1.5;
        currentGLInScreenProportion = 0.6;
        offset=0;
        if(ARWayConst.IS_DEBUG_MODE){
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG,"RenderStrategy reset ");
        }
    }
}
