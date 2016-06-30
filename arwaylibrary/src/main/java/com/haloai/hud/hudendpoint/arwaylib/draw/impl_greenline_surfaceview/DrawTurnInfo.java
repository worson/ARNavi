package com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline_surfaceview;

import android.content.Context;
import android.graphics.Canvas;

import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;
import com.haloai.hud.hudendpoint.arwaylib.framedata.FrameDataFactory;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.TurnInfoFrameData;

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
        if (BeanFactory.getBean(BeanFactory.BeanType.NAVI_INFO).isShow()) {
            TurnInfoFrameData turnInfoFrameData = (TurnInfoFrameData) FrameDataFactory.getFrameData4Draw(
                    context, FrameDataFactory.FrameDataType.TURN_INFO);
            if (turnInfoFrameData.getPicture() == null) {
                return;
            }
            turnInfoFrameData.getPicture().draw(canvas);
        }
    }
}
