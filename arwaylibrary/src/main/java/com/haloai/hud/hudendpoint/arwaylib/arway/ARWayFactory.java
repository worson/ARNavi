package com.haloai.hud.hudendpoint.arwaylib.arway;

import android.content.Context;

import com.haloai.hud.hudendpoint.arwaylib.arway.impl.ARWaySurfaceView;

/**
 * Created by 龙 on 2016/4/29.
 */
public class ARWayFactory {
    public enum ARWayType {
        SURFACE_VIEW,
        OPENGL
    }

    public static IARWay getARWay(Context context, ARWayType arWayType) {
        IARWay arway_surfaceview = null;
        switch (arWayType) {
            case SURFACE_VIEW:
                if (arway_surfaceview == null) {
                    arway_surfaceview = new ARWaySurfaceView(context);
                }
                return arway_surfaceview;
            case OPENGL:
        }
        return null;
    }
}
