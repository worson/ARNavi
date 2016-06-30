package com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline_surfaceview;

import android.content.Context;
import android.graphics.Canvas;

import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;
import com.haloai.hud.hudendpoint.arwaylib.framedata.FrameDataFactory;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.NaviInfoFrameData;
import com.haloai.hud.utils.HaloLogger;

/**
 * author       : 龙;
 * date         : 2016/6/12;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline_surfaceview;
 * project_name : hudlauncher;
 */
public class DrawNaviInfo extends DrawObject {
    private static DrawNaviInfo mDrawNaviInfo = new DrawNaviInfo();

    private DrawNaviInfo() {}

    public static DrawNaviInfo getInstance() {
        return mDrawNaviInfo;
    }
    @Override
    public void doDraw(Context context, Canvas canvas) {
        if (BeanFactory.getBean(BeanFactory.BeanType.NAVI_INFO).isShow()) {
            NaviInfoFrameData naviInfoFrameData = (NaviInfoFrameData) FrameDataFactory.getFrameData4Draw(
                    context, FrameDataFactory.FrameDataType.NAVI_INFO);
            if (naviInfoFrameData.getPicture() == null) {
                return;
            }
            naviInfoFrameData.getPicture().draw(canvas);
            HaloLogger.logE("DrawNaviInfo","DrawNaviInfo doDraw");

        }
    }
}
