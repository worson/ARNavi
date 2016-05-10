package com.haloai.hud.hudendpoint.arwaylib.framedata.impl;

import android.graphics.Bitmap;

import com.haloai.hud.hudendpoint.arwaylib.framedata.FrameDataFactory;
import com.haloai.hud.hudendpoint.arwaylib.framedata.SuperFrameData;

/**
 * author       : 龙;
 * date         : 2016/5/9;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.framedata.impl;
 * project_name : hudlauncher;
 */
public class IconFrameData extends SuperFrameData {
    private final static int X = 0;
    private final static int Y = 0;

    private IconFrameData(Bitmap icon) {
        setImage(icon);
        setPosition(X,Y);
    }

    public static IconFrameData getInstance(FrameDataFactory.FrameDataType frameDataType) {
        IconFrameData iconFrameData = null;
        switch (frameDataType){
            case SATELLITE:
                iconFrameData = new IconFrameData(null);
                break;
            case NETWORK:
                iconFrameData = new IconFrameData(null);
                break;
            default:
                break;
        }
        return iconFrameData;
    }
}
