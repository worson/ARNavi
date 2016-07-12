package com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline_surfaceview;

import android.content.Context;
import android.graphics.Canvas;

import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.CompassBean;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;
import com.haloai.hud.utils.HaloLogger;

/**
 * Created by wangshengxing on 16/6/22.
 */
public class DrawCompass extends DrawObject {
    private static DrawCompass mDrawCompass = new DrawCompass();

    public DrawCompass() {
    }

    public static DrawCompass getInstance() {
        return mDrawCompass;
    }

    @Override
    public void doDraw(Context context, Canvas canvas) {
        if (BeanFactory.getBean(BeanFactory.BeanType.COMPASS).isShow()) {
            CompassBean compassBean = (CompassBean)BeanFactory.getBean(BeanFactory.BeanType.COMPASS);
            HaloLogger.logI("DrawCompass","DrawCompass doDraw,direction is :"+compassBean.getDirection());
        }
    }
}
