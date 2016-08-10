package com.haloai.hud.hudendpoint.arwaylib.draw;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline_surfaceview.DrawCompass;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline_surfaceview.DrawCrossImage;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline_surfaceview.DrawExit;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline_surfaceview.DrawIcon;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline_surfaceview.DrawMusic;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline_surfaceview.DrawNaviInfo;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline_surfaceview.DrawNextRoadName;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline_surfaceview.DrawRoute;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline_surfaceview.DrawSpeed;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_greenline_surfaceview.DrawTurnInfo;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl.DrawCamera;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl.DrawScene;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl.GlDrawCompass;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl.GlDrawNaviInfo;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl.GlDrawRetainDistance;
import com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl.GlDrawSpeedDial;
import com.haloai.hud.hudendpoint.arwaylib.view.ComPassView;

import 	android.view.LayoutInflater;


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
        ROUTE,
        TURN_INFO,
        SATELLITE,
        NAVI_INFO,
        NETWORK,
        SPEED,
        RETAIN_DISTANCE,
        COMPASS,
        GL_SCENE,
        GL_CAMERA,
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
            case ROUTE:
                drawObject = DrawRoute.getInstance();
                break;
            case NAVI_INFO:
                drawObject = DrawNaviInfo.getInstance();
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
            case SPEED:
                drawObject = DrawSpeed.getInstance();
                break;
            case COMPASS:
                drawObject = DrawCompass.getInstance();
                break;
            case GL_SCENE:
                drawObject = DrawScene.getInstance();
                break;
            case GL_CAMERA:
                drawObject = DrawCamera.getInstance();
                break;
            default:
                break;
        }
        return drawObject;
    }

    /**
     * 得到ARway的layout的总布局的View
     * 并相关View的实例到相关的DrawObject中
     * @param context
     * @return
     */
    public static View createGlDrawObjectLayoutIntance(Context context,ViewGroup container,int layoutid){
        View drawView = null;
        LayoutInflater inflater  = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup mLayout = (ViewGroup) inflater.inflate(layoutid, container, true);

        DrawScene drawScene = (DrawScene)getGlDrawObject(DrawType.GL_SCENE);
//        drawScene.setView(context,mLayout);

        ViewGroup glSurfaceViewgroup = (ViewGroup)mLayout.findViewById(R.id.opengl_viewgroup);
        drawView = drawScene.getViewInstance(context);

        if (drawView != null && drawView.getParent()!=null){
            ViewGroup vg = (ViewGroup) drawView.getParent();
            if (vg != null) {
                vg.removeView(drawView);
            }
        }
        if (drawView != null && drawView.getParent() ==null && glSurfaceViewgroup != null) {
            glSurfaceViewgroup.addView(drawView);
        }

        GlDrawCompass glDrawCompass = GlDrawCompass.getInstance();
        glDrawCompass.setView(context,mLayout);


        GlDrawSpeedDial glDrawSpeedDial = GlDrawSpeedDial.getInstance();
        glDrawSpeedDial.setView(context,mLayout);



        GlDrawRetainDistance glDrawRetainDistance = GlDrawRetainDistance.getInstance();
        glDrawRetainDistance.setView(context,mLayout);

        GlDrawNaviInfo.getInstance().setView(context,mLayout);

        return mLayout;
    }

    public static DrawObject getGlDrawObject(DrawType drawType) {
        DrawObject drawObject = null;
        switch (drawType) {
            case CROSS_IMAGE:
                break;
            case NAVI_INFO:
                drawObject = GlDrawNaviInfo.getInstance();
                break;
            case SPEED:
                drawObject = GlDrawSpeedDial.getInstance();
                break;
            case COMPASS:
                drawObject = GlDrawCompass.getInstance();
                break;
            case GL_SCENE:
                drawObject = DrawScene.getInstance();
                break;
            case GL_CAMERA:
                drawObject = DrawCamera.getInstance();
                break;
            case RETAIN_DISTANCE:
                drawObject = GlDrawRetainDistance.getInstance();
                break;
            default:
                break;
        }
        return drawObject;
    }
}
