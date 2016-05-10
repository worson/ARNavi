package com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline;

import android.content.Context;
import android.graphics.Canvas;

import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;

/**
 * Created by 龙 on 2016/4/29.
 */
public class DrawRoute extends DrawObject {
    private static DrawRoute mDrawRoute = new DrawRoute();

    private DrawRoute() {}

    public static DrawRoute getInstance() {
        return mDrawRoute;
    }
    @Override
    public void doDraw(Context context, Canvas canvas) {

    }
}
