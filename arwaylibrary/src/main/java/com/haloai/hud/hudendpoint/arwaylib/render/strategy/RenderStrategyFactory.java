package com.haloai.hud.hudendpoint.arwaylib.render.strategy;

/**
 * @author Created by Mo Bing(mobing@haloai.com) on 23/10/2016.
 */
public class RenderStrategyFactory {

    IRenderStrategy generateRenderStrategy() {
        return new HardcodeRenderStrategy();
    }

    IRenderStrategy generateConfigurableRenderStrategy(String configStr) {
        return new ConfigurableRenderStrategy(configStr);
    }

    IRenderStrategy generateMockTestRenderStrategy() {
        return  new MockTestRenderStrategy();
    }

}
