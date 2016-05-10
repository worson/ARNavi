package com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline;

import android.content.Context;
import android.graphics.Canvas;

import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.calculator.result;
 * project_name : hudlauncher;
 */
public class DrawCrossImage extends DrawObject {
    private static DrawCrossImage mDrawCrossImage = new DrawCrossImage();

    private DrawCrossImage() {

    }

    public static DrawCrossImage getInstance() {
        return mDrawCrossImage;
    }

    @Override
    public void doDraw(Context context, Canvas canvas) {

    }
}
