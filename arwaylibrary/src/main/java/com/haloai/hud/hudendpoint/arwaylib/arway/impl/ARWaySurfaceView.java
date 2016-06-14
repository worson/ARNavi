package com.haloai.hud.hudendpoint.arwaylib.arway.impl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.haloai.hud.hudendpoint.arwaylib.arway.IARWay;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObjectFactory;
import com.haloai.hud.hudendpoint.arwaylib.framedata.FrameDataFactory;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.RouteFrameData;

import java.util.ArrayList;
import java.util.List;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.arway.impl;
 * project_name : hudlauncher;
 */
public class ARWaySurfaceView extends SurfaceView implements SurfaceHolder.Callback, IARWay {
    private List<DrawObject>  mDrawList          = new ArrayList<DrawObject>();
    private boolean           mIsRunning         = false;
    private HudwayFlushThread mHudwayFlushThread = null;
    private boolean           mIsPause           = false;
    private Context           mContext           = null;
    private SurfaceHolder     mSurfaceHolder     = null;

    public ARWaySurfaceView(Context context) {
        super(context);

        this.mContext = context;

        SurfaceHolder holder = this.getHolder();
        this.setZOrderOnTop(true);
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.addCallback(this);
        setFocusableInTouchMode(true);
    }

    @Override
    public void reset() {
        mIsRunning = false;
        mIsPause = false;
    }

    @Override
    public void release() {
        reset();
        mDrawList.clear();
    }

    @Override
    public void start() {
        if (mDrawList.size() <= 0) {
            //warning:this order is draw order.
            //mDrawList.add(DrawObjectFactory.getDrawObject(DrawObjectFactory.DrawType.CROSS_IMAGE));
            mDrawList.add(DrawObjectFactory.getDrawObject(DrawObjectFactory.DrawType.ROUTE));
            mDrawList.add(DrawObjectFactory.getDrawObject(DrawObjectFactory.DrawType.NAVI_INFO));
            //mDrawList.add(DrawObjectFactory.getDrawObject(DrawObjectFactory.DrawType.NEXT_ROAD_NAME));
            //mDrawList.add(DrawObjectFactory.getDrawObject(DrawObjectFactory.DrawType.TURN_INFO));
            //mDrawList.add(DrawObjectFactory.getDrawObject(DrawObjectFactory.DrawType.SATELLITE));
            //mDrawList.add(DrawObjectFactory.getDrawObject(DrawObjectFactory.DrawType.NETWORK));
            //mDrawList.add(DrawObjectFactory.getDrawObject(DrawObjectFactory.DrawType.MUSIC));
            //mDrawList.add(DrawObjectFactory.getDrawObject(DrawObjectFactory.DrawType.EXIT));
        }
        mIsRunning = true;
    }

    @Override
    public void continue_() {
        mIsPause = false;
    }

    @Override
    public void pause() {
        mIsPause = true;
    }

    @Override
    public void stop() {
        mIsRunning = false;
    }

    @Override
    public boolean isRunning() {
        return mIsRunning && !mIsPause;
    }

    @Override
    public View getARWay() {
        return this;
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
        mHudwayFlushThread = new HudwayFlushThread(surfaceHolder);
        mIsRunning = true;
        mHudwayFlushThread.start();
        ((RouteFrameData) FrameDataFactory.getFrameData4Update(FrameDataFactory.FrameDataType.ROUTE)).initDrawLine(this.getWidth(), this.getHeight());
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mIsRunning = false;
    }

    class HudwayFlushThread extends Thread {
        private              SurfaceHolder surfaceHolder = null;
        private              Canvas        can           = null;
        private              long          startTime     = 0;
        private              long          endTime       = 0;
        private static final long          FPS_TIME      = 30;

        public HudwayFlushThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        @Override
        public void run() {
            while (mIsRunning) {
                if (!mIsPause) {
                    can = surfaceHolder.lockCanvas(null);
                    if (can != null) {
                        synchronized (surfaceHolder) {
                            startTime = System.currentTimeMillis();
                            can.drawColor(Color.BLACK);
                            can.save();
                            for (DrawObject drawObject : mDrawList) {
                                drawObject.doDraw(mContext, can);
                            }
                            can.restore();
                            endTime = System.currentTimeMillis();
                        }
                    }
                    if (FPS_TIME > endTime - startTime) {
                        SystemClock.sleep(FPS_TIME - (endTime - startTime));
                    }
                    if (can != null) {
                        surfaceHolder.unlockCanvasAndPost(can);
                    }
                } else {
                    SystemClock.sleep(300);
                }
            }
        }
    }
}
