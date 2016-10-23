package com.haloai.hud.hudendpoint.arwaylib.render.strategy;

/**
 * @author Created by Mo Bing(mobing@haloai.com) on 23/10/2016.
 */
public class HardcodeRenderStrategy extends RenderStrategy {

    @Override
    public void updateCurrentRoadInfo(int roadClass, int mpDistance) {
        boolean needsUpdate = true;
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
