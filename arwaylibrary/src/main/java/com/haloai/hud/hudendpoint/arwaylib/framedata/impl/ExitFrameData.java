package com.haloai.hud.hudendpoint.arwaylib.framedata.impl;

import com.haloai.hud.hudendpoint.arwaylib.framedata.SuperFrameData;

/**
 * Created by 龙 on 2016/4/29.
 */
public class ExitFrameData extends SuperFrameData {
    private final static int X = 0;
    private final static int Y = 0;

    private static ExitFrameData mExitFrameData = new ExitFrameData();

    private ExitFrameData() {
        setPosition(X,Y);
        setImage(null);
    }

    public static ExitFrameData getInstance() {
        return mExitFrameData;
    }

    @Override
    public void animOver() {

    }
}
