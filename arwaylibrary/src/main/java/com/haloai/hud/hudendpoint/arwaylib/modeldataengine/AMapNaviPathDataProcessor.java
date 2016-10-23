package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

/**
 * @author Created by Mo Bing(mobing@haloai.com) on 22/10/2016.
 */
public class AMapNaviPathDataProcessor implements INaviPathDataProcessor {
    //Cache all navigation path data.

    @Override
    public void setPath(Object aMapNaviPath) {
        if (!(aMapNaviPath instanceof AMapNaviPath)) {
            throw new IllegalArgumentException("Needs AMapNaviPath");
        }
    }

    private void processSteps() {

    }

    private void prepareLevelData() {

    }
}
