package com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline;

import android.content.Context;
import android.graphics.Canvas;

import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;

/**
 * Created by 龙 on 2016/4/29.
 */
public class DrawExit extends DrawObject {
    private static DrawExit mDrawExit = new DrawExit();

    private DrawExit() {}

    public static DrawExit getInstance() {
        return mDrawExit;
    }
    @Override
    public void doDraw(Context context, Canvas canvas) {

    }
}
