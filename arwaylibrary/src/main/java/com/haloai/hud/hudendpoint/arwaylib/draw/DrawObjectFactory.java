package com.haloai.hud.hudendpoint.arwaylib.draw;

import com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline.DrawCrossImage;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline.DrawExit;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline.DrawIcon;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline.DrawMusic;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline.DrawNextRoadName;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline.DrawWay;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline.DrawTurnInfo;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.draw;
 * project_name : hudlauncher;
 */
public class DrawObjectFactory {
    public enum DrawType{
        CROSS_IMAGE,
        EXIT,
        MUSIC,
        NEXT_ROAD_NAME,
        WAY,
        TURN_INFO,
        SATELLITE,
        NETWORK
    }
    public static DrawObject getDrawObject(DrawType drawType){
        DrawObject drawObject = null;
        switch(drawType) {
            case CROSS_IMAGE:
                drawObject = DrawCrossImage.getInstance();
                break;
            case EXIT:
                drawObject = DrawExit.getInstance();
                break;
            case MUSIC:
                drawObject = DrawMusic.getInstance();
                break;
            case NEXT_ROAD_NAME:
                drawObject = DrawNextRoadName.getInstance();
                break;
            case WAY:
                drawObject = DrawWay.getInstance();
                break;
            case TURN_INFO:
                drawObject = DrawTurnInfo.getInstance();
                break;
            case SATELLITE:
                drawObject = DrawIcon.getInstance(DrawType.SATELLITE);
                break;
            case NETWORK:
                drawObject = DrawIcon.getInstance(DrawType.NETWORK);
                break;
            default:
                break;
        }
        return drawObject;
    }
}
