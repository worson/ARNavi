package com.haloai.hud.hudendpoint.arwaylib.framedata.impl;

import com.haloai.hud.hudendpoint.arwaylib.framedata.SuperFrameData;

/**
 * Created by 龙 on 2016/4/29.
 */
public class TurnInfoFrameData extends SuperFrameData {
    private final static int X = 0;
    private final static int Y = 0;

    private static TurnInfoFrameData mTurnInfoFrameData = new TurnInfoFrameData();

    private TurnInfoFrameData() {
        setPosition(X,Y);
    }

    public static TurnInfoFrameData getInstance() {
        return mTurnInfoFrameData;
    }

    @Override
    public void animOver() {

    }
}
