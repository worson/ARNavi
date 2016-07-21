package com.haloai.hud.hudendpoint.arwaylib.draw;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by 龙 on 2016/4/29.
 */
public abstract class DrawViewObject extends DrawObject implements IViewOperation{

    public static int VIEW_DRIVING_WIDTH  = 192;
    public static int VIEW_DRIVING_HEIGHT = 120;

    public static int VIEW_NOT_DRIVING_WIDTH  = 254;
    public static int VIEW_NOT_DRIVING_HEIGHT = 254;

    public static int VIEW_PARRENT_WIDTH  = 254;
    public static int VIEW_PARRENT_HEIGHT = 254;

    public static int VIEW_ROTATION_DEGREES   = 60;
    public static int VIEW_ANIMATION_DURATION = 1000;

    protected Resources mResources  = null;
    protected ViewGroup mViewParent = null;

    protected void initLayout(Context context, ViewGroup parent, View view){
        mResources = context.getResources();
        if (parent != null) {
            mViewParent = parent;
        }
    }

}
