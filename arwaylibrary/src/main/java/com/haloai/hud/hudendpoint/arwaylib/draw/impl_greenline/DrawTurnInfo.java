package com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline;

import android.content.Context;
import android.graphics.Canvas;

import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;

/**
 * Created by 龙 on 2016/4/29.
 */
public class DrawTurnInfo extends DrawObject {
    private static DrawTurnInfo mDrawTurnInfo = new DrawTurnInfo();

    private DrawTurnInfo() {}

    public static DrawTurnInfo getInstance() {
        return mDrawTurnInfo;
    }
    @Override
    public void doDraw(Context context, Canvas canvas) {

    }
}
