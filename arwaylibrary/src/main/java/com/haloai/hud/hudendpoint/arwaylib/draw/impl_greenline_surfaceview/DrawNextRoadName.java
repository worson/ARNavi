package com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline_surfaceview;

import android.content.Context;
import android.graphics.Canvas;

import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline;
 * project_name : hudlauncher;
 */
public class DrawNextRoadName extends DrawObject {
    private static DrawNextRoadName mDrawNextRoadName = new DrawNextRoadName();

    private DrawNextRoadName() {}

    public static DrawNextRoadName getInstance() {
        return mDrawNextRoadName;
    }
    @Override
    public void doDraw(Context context, Canvas canvas) {

    }
}
