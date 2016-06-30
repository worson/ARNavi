package com.haloai.hud.hudendpoint.arwaylib.arway;

import android.content.Context;

import com.haloai.hud.hudendpoint.arwaylib.arway.impl.ARWaySurfaceView;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.arway;
 * project_name : hudlauncher;
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
