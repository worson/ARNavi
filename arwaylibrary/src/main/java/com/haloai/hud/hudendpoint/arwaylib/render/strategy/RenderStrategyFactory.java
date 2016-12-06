package com.haloai.hud.hudendpoint.arwaylib.render.strategy;

/**
 * @author Created by Mo Bing(mobing@haloai.com) on 23/10/2016.
 */
public class RenderStrategyFactory {

    public static IRenderStrategy generateRenderStrategy() {
        return new HardcodeRenderStrategy();
    }

    public static IRenderStrategy generateConfigurableRenderStrategy(String configStr) {
        return new ConfigurableRenderStrategy(configStr);
    }

    public static IRenderStrategy generateMockTestRenderStrategy() {
        return  new MockTestRenderStrategy();
    }

}
