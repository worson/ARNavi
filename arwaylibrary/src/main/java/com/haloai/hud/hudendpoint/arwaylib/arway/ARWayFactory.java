package com.haloai.hud.hudendpoint.arwaylib.arway;

import android.app.Fragment;
import android.content.Context;

import com.haloai.hud.hudendpoint.arwaylib.arway.impl.ARWaySurfaceView;
import com.haloai.hud.hudendpoint.arwaylib.arway.impl_gl.ARwayOpenGLFragment;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.arway;
 * project_name : hudlauncher;
 */
public class ARWayFactory {
    private static IARWay mArwaySurfaceview = null;
    private static Fragment mArwayOpenglFragment = null;

    public enum ARWayType {
        SURFACE_VIEW,
        OPENGL
    }

    public static IARWay getARWay(Context context, ARWayType arWayType) {

        switch (arWayType) {
            case SURFACE_VIEW:
                if (mArwaySurfaceview == null) {
                    mArwaySurfaceview = new ARWaySurfaceView(context);
                }
                return mArwaySurfaceview;
            case OPENGL:

        }
        return null;
    }

    public static Fragment getArwayOpenglFragment(Context context, ARWayType arWayType){
        switch (arWayType) {
            case OPENGL:
                if (mArwayOpenglFragment == null) {
                    mArwayOpenglFragment = new ARwayOpenGLFragment();
                }
                return mArwayOpenglFragment;
        }
        return null;
    }

}
