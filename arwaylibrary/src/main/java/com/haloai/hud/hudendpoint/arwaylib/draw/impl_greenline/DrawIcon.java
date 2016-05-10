package com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;

import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObjectFactory;

/**
 * author       : 龙;
 * date         : 2016/5/6;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline;
 * project_name : hudlauncher;
 */
public class DrawIcon extends DrawObject {
    private Bitmap mIcon    = null;
    private Point  mPosition = null;

    private DrawIcon(Bitmap icon,Point position) {
        this.mIcon = icon;
        this.mPosition = position;
    }

    public static DrawIcon getInstance(DrawObjectFactory.DrawType drawType) {
        DrawIcon drawIcon = null;
        switch(drawType){
            case SATELLITE:
                drawIcon = new DrawIcon(null,null);
                break;
            case NETWORK:
                drawIcon = new DrawIcon(null,null);
                break;
            default:
                break;
        }
        return drawIcon;
    }

    @Override
    public void doDraw(Context context, Canvas canvas) {

    }
}
