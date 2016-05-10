package com.haloai.hud.hudendpoint.arwaylib.framedata.impl;

import com.haloai.hud.hudendpoint.arwaylib.framedata.SuperFrameData;

/**
 * Created by 龙 on 2016/4/29.
 */
public class CrossImageFrameData extends SuperFrameData {
    private final static int X = 0;
    private final static int Y = 0;

    private static CrossImageFrameData mCrossImageFrameData = new CrossImageFrameData();

    private CrossImageFrameData() {
        setPosition(X,Y);
    }

    public static CrossImageFrameData getInstance() {
        return mCrossImageFrameData;
    }
}
