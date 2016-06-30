package com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline_surfaceview;

import android.content.Context;
import android.graphics.Canvas;

import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;
import com.haloai.hud.hudendpoint.arwaylib.framedata.FrameDataFactory;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.MusicFrameData;
import com.haloai.hud.utils.HaloLogger;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline;
 * project_name : hudlauncher;
 */
public class DrawMusic extends DrawObject {
    private static DrawMusic mDrawMusic = new DrawMusic();

    private DrawMusic() {}

    public static DrawMusic getInstance() {
        return mDrawMusic;
    }
    @Override
    public void doDraw(Context context,Canvas canvas) {
        if(BeanFactory.getBean(BeanFactory.BeanType.MUSIC).isShow()) {
            MusicFrameData musicFrameData = (MusicFrameData) FrameDataFactory.getFrameData4Draw(
                    context, FrameDataFactory.FrameDataType.MUSIC);
            if (musicFrameData.getImage() == null) {
                return;
            }
            canvas.setMatrix(musicFrameData.getMatrix());
            canvas.drawBitmap(musicFrameData.getImage(), null,
                              musicFrameData.getDrawRect(), musicFrameData.getPaint());
            HaloLogger.logE("position:", musicFrameData.getDrawRect().left + "," + musicFrameData.getDrawRect().top + ","
                    + musicFrameData.getDrawRect().right + "," + musicFrameData.getDrawRect().bottom);
        }
    }
}
