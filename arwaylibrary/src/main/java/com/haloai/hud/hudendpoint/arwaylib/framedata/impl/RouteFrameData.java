package com.haloai.hud.hudendpoint.arwaylib.framedata.impl;

import com.haloai.hud.hudendpoint.arwaylib.framedata.SuperFrameData;

/**
 * Created by 龙 on 2016/4/29.
 */
public class RouteFrameData extends SuperFrameData {
    private final static int X = 0;
    private final static int Y = 0;

    private static RouteFrameData mRouteFrameData = new RouteFrameData();

    private RouteFrameData() {
        setPosition(X,Y);
    }

    public static RouteFrameData getInstance() {
        return mRouteFrameData;
    }
}
