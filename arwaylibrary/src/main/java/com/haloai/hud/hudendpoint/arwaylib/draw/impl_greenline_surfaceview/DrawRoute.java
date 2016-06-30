package com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline_surfaceview;

import android.content.Context;
import android.graphics.Canvas;

import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;
import com.haloai.hud.hudendpoint.arwaylib.framedata.FrameDataFactory;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.RouteFrameData;

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

            routeFrameData.getPicture().draw(canvas);

            //TODO helong test 放大四倍
            ///*new Rect(0,0,RouteFrameData.IMAGE_WIDTH*4,RouteFrameData.IMAGE_HEIGHT*4)*/
//            canvas.drawBitmap(routeFrameData.getBitmap(), null,
//                              new Rect(-((RouteFrameData.IMAGE_WIDTH*4-RouteFrameData.IMAGE_WIDTH)/2),
//                                       -((RouteFrameData.IMAGE_HEIGHT*4-RouteFrameData.IMAGE_HEIGHT)/2),
//                                       RouteFrameData.IMAGE_WIDTH*4-(RouteFrameData.IMAGE_WIDTH*4-RouteFrameData.IMAGE_WIDTH)/2,
//                                       RouteFrameData.IMAGE_HEIGHT*4-(RouteFrameData.IMAGE_HEIGHT*4-RouteFrameData.IMAGE_HEIGHT)/2),
//                              null);
//            canvas.drawBitmap(routeFrameData.getBitmap(),0,0,null);
//            Paint paint = new Paint();
//            paint.setTextSize(100);
//            paint.setStrokeWidth(10);
//            paint.setColor(Color.RED);
//            canvas.drawText("You must be kidding me!!!", 300, 300, paint);
        }
    }
}
