package com.haloai.hud.hudendpoint.arwaylib.render.strategy;

/**
 * @author Created by Mo Bing(mobing@haloai.com) on 23/10/2016.
 */
public class MockTestRenderStrategy extends RenderStrategy {

    @Override
    public int getRoadClass(int _roadClassSDK) {
        return 0;
    }

    @Override
    public void updateCurrentRoadInfo(int roadClass, int mpDistance) {

    }

    @Override
    public RenderParams getCurrentRenderParams() {
        return null;
    }

    public void setCameraAngle(double cameraAngle) {

    }

    public void setDataLevel(DataLevel dataLevel) {

    }

    public void setLevelInnerScale(double scale) {

    }
}
