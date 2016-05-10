package com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline;

import android.content.Context;
import android.graphics.Canvas;

import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;

/**
 * Created by 龙 on 2016/4/29.
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
