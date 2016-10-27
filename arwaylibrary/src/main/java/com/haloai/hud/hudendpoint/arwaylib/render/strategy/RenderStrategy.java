package com.haloai.hud.hudendpoint.arwaylib.render.strategy;

import com.amap.api.navi.enums.RoadClass;

/**
 * @author Created by Mo Bing(mobing@haloai.com) on 23/10/2016.
 */
public abstract class RenderStrategy implements IRenderStrategy {
    protected RenderParamsNotifier renderParamsNotifier;

    protected DataLevel currentDataLevel = DataLevel.LEVEL_20;
    protected double currentGLCameraAngle = 45;
    protected double currentGLScale = 1.0;
    protected double currentGLInScreenProportion = 0.5;

    @Override
    public void setRenderParamsNotifier(RenderParamsNotifier renderParamsNotifier) {
        this.renderParamsNotifier = renderParamsNotifier;
    }

    public RenderParams getDefaultRenderParams() {
        //return the default params
        return new RenderParams(currentDataLevel, currentGLCameraAngle,currentGLScale,currentGLInScreenProportion);
    }


}
