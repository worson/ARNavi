package com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline;

import android.content.Context;
import android.graphics.Canvas;

import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;
import com.haloai.hud.hudendpoint.arwaylib.framedata.FrameDataFactory;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.WayFrameData;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline;
 * project_name : hudlauncher;
 */
public class DrawWay extends DrawObject {
    private static DrawWay mDrawRoute = new DrawWay();

    private DrawWay() {}

    public static DrawWay getInstance() {
        return mDrawRoute;
    }

    @Override
    public void doDraw(Context context, Canvas canvas) {
        if (BeanFactory.getBean(BeanFactory.BeanType.ROUTE).isShow()) {
            WayFrameData wayFrameData = (WayFrameData) FrameDataFactory.getFrameDataForDraw(
                    context, FrameDataFactory.FrameDataType.WAY);
            if (wayFrameData.getImage() == null) {
                return;
            }

//            canvas.drawBitmap(wayFrameData.getImage(), wayFrameData.getSrcRect(), wayFrameData.getDestRect(), null);
            canvas.drawBitmap(wayFrameData.getImage(), 0,0, null);
        }
    }
}
