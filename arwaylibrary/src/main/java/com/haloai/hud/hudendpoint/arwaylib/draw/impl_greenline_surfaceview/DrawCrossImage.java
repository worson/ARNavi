package com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline_surfaceview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Picture;

import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;
import com.haloai.hud.hudendpoint.arwaylib.framedata.FrameDataFactory;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.CrossImageFrameData;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.TurnInfoFrameData;

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
        if (BeanFactory.getBean(BeanFactory.BeanType.NAVI_INFO).isShow()) {
            CrossImageFrameData CrossFrameData = (CrossImageFrameData) FrameDataFactory.getFrameData4Draw(
                    context, FrameDataFactory.FrameDataType.CROSS_IMAGE);
            if (CrossFrameData.getPicture() == null) {
                return;
            }
            CrossFrameData.getPicture().draw(canvas);
        }
    }
}
