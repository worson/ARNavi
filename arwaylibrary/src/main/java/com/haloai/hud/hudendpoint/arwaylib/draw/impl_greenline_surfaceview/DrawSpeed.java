package com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline_surfaceview;

import android.content.Context;
import android.graphics.Canvas;

import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.SpeedBean;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;
import com.haloai.hud.hudendpoint.arwaylib.framedata.FrameDataFactory;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.SpeedFrameData;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.TurnInfoFrameData;
import com.haloai.hud.utils.HaloLogger;

/**
 * Created by wangshengxing on 16/6/22.
 * detail       : 用于更新速度表盘
 */
public class DrawSpeed extends DrawObject{
    private static DrawSpeed mDrawSpeed = new DrawSpeed();

    public DrawSpeed() {
    }

    public static DrawSpeed getInstance() {
        return mDrawSpeed;
    }

    @Override
    public void doDraw(Context context, Canvas canvas) {
        if (BeanFactory.getBean(BeanFactory.BeanType.SPEED).isShow()) {
            SpeedFrameData speedFrameData = (SpeedFrameData) FrameDataFactory.getFrameData4Draw(
                    context, FrameDataFactory.FrameDataType.SPEED);
            if (speedFrameData.getPicture() == null) {
                return;
            }
            speedFrameData.getPicture().draw(canvas);
        }
    }
}
