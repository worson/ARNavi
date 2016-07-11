package com.haloai.hud.hudendpoint.arwaylib.framedata.impl;

import com.haloai.hud.hudendpoint.arwaylib.calculator.result.SuperResult;
import com.haloai.hud.hudendpoint.arwaylib.framedata.SuperFrameData;

/**
 * Created by 龙 on 2016/4/29.
 */
public class NextRoadNameFrameData extends SuperFrameData <SuperResult>{
    private final static int X = 0;
    private final static int Y = 0;

    private static NextRoadNameFrameData mNextRoadNameFrameData = new NextRoadNameFrameData();

    private NextRoadNameFrameData() {
        setPosition(X,Y);
    }

    public static NextRoadNameFrameData getInstance() {
        return mNextRoadNameFrameData;
    }

    @Override
    public void animOver() {

    }

    @Override
    public void update(SuperResult result){

    }
}
