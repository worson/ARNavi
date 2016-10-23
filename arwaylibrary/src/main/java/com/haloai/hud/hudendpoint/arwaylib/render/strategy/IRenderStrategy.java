package com.haloai.hud.hudendpoint.arwaylib.render.strategy;

/**
 * @author Created by Mo Bing(mobing@haloai.com) on 23/10/2016.
 */
public interface IRenderStrategy {
    enum DataLevel {
        LEVEL_18,
        LEVEL_16,
        LEVEL_15,
        LEVEL_14,
        LEVEL_13,
        LEVEL_12
    }

    DataLevel getCurrentDataLevel();
}
