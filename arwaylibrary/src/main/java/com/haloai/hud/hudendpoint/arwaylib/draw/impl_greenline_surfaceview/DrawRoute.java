package com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline_surfaceview;

import android.content.Context;
import android.graphics.Canvas;

import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;
import com.haloai.hud.hudendpoint.arwaylib.framedata.FrameDataFactory;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.RouteFrameData;
import com.haloai.hud.utils.HaloLogger;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline;
 * project_name : hudlauncher;
 */
public class DrawRoute extends DrawObject {
    private static DrawRoute mDrawRoute = new DrawRoute();

    private DrawRoute() {}

    public static DrawRoute getInstance() {
        return mDrawRoute;
    }

    @Override
    public void doDraw(Context context, Canvas canvas) {
        if (BeanFactory.getBean(BeanFactory.BeanType.ROUTE).isShow()) {
            RouteFrameData routeFrameData = (RouteFrameData) FrameDataFactory.getFrameData4Draw(
                    context, FrameDataFactory.FrameDataType.ROUTE);
            if (routeFrameData.getPicture() == null) {
                return;
            }
            // FIXME: 16/6/14
            long  performanceLogTime = System.currentTimeMillis();
            routeFrameData.getPicture().draw(canvas);
//            Paint paint = new Paint();
//            paint.setTextSize(100);
//            paint.setStrokeWidth(10);
//            paint.setColor(Color.RED);
//            canvas.drawText("You must be kidding me!!!", 300, 300, paint);
            HaloLogger.logI("performance_log","=========performance_log=========== draw time = "+ (System.currentTimeMillis()-performanceLogTime));
        }
    }
}
