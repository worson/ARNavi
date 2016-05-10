package com.haloai.hud.hudendpoint.arwaylib.framedata.impl;

import android.graphics.Rect;

import com.haloai.hud.hudendpoint.arwaylib.calculator.result.PositionResult;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.ScaleResult;
import com.haloai.hud.hudendpoint.arwaylib.framedata.SuperFrameData;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.framedata.impl;
 * project_name : hudlauncher;
 */
public class MusicFrameData extends SuperFrameData {
    private static final float IMAGE_WIDTH  = 100;
    private static final float IMAGE_HEIGHT = 100;
    private final static int   X            = 100;
    private final static int   Y            = 100;

    private Rect    mDrawRect                  = new Rect(100, 100, 200, 200);
    private String  mMusicName                 = "";
    private float   mScala                     = 0f;
    private boolean mUpdateSrcRectWithScala    = false;
    private boolean mUpdateSrcRectWithPosition = false;

    private static MusicFrameData mMusicFrameData = new MusicFrameData();

    private MusicFrameData() {
        setPosition(X, Y);
    }

    public static MusicFrameData getInstance() {
        return mMusicFrameData;
    }

    /**
     * scalaResult.mOffsetPosition为本次动画相对于上次动画结束位置的偏移量
     * 而不是本次动画相对于最开始时候的偏移量
     *
     * @param scalaResult scala变换后的结果
     */
    public void updateWithScala(ScaleResult scalaResult) {
        /*this.mSrcRect.set(scalaResult.mOffsetPosition.x,scalaResult.mOffsetPosition.y,
                          scalaResult.mOffsetPosition.x+mImage.getWidth(),
                          scalaResult.mOffsetPosition.y+mImage.getHeight());*/
      /*  this.mDrawRect.set(scalaResult.mOffsetPosition.x, scalaResult.mOffsetPosition.y,
                           (int)(scalaResult.mOffsetPosition.x+mImage.getWidth()*scalaResult.mOffsetWidthScala),
                           (int)(scalaResult.mOffsetPosition.y+mImage.getHeight()*scalaResult.mOffsetHeightScala));*/
        /*if (scalaResult.mIsBack) {
            if (mUpdateSrcRectWithScala) {
                mSrcRect.set(mDrawRect);
                mUpdateSrcRectWithScala = false;
            }
        } else {
            mUpdateSrcRectWithScala = true;
        }
        this.mDrawRect.left = this.mSrcRect.left + scalaResult.mOffsetPosition.x;
        this.mDrawRect.top = this.mSrcRect.top + scalaResult.mOffsetPosition.y;
        this.mDrawRect.right = this.mSrcRect.right - scalaResult.mOffsetPosition.x;
        this.mDrawRect.bottom = this.mSrcRect.bottom - scalaResult.mOffsetPosition.y;
        if(scalaResult.mIsOver){
            mSrcRect.set(mDrawRect);
        }*/

        this.mScala += scalaResult.mOffsetScala;
        this.mPosition.x += scalaResult.mOffsetPosition.x;
        this.mPosition.y += scalaResult.mOffsetPosition.y;
        this.mDrawRect.set(mPosition.x, mPosition.y, (int) (mPosition.x + scalaResult.mWidthScala * IMAGE_WIDTH),
                           (int) (mPosition.y + scalaResult.mWidthScala * IMAGE_HEIGHT));


        /*this.mDrawRect.right+=(int)(scalaResult.mOffsetPosition.x+mImage.getWidth()*scalaResult.mOffsetWidthScala);
        this.mDrawRect.bottom+=(int)(scalaResult.mOffsetPosition.y+mImage.getHeight()*scalaResult.mOffsetHeightScala);*/
    }

    public void updateWithPosition(PositionResult positionResult) {
       /* this.mSrcRect.set(positionResult.mOffsetPosition.x,positionResult.mOffsetPosition.y,
                          positionResult.mOffsetPosition.x+mImage.getWidth(),
                          positionResult.mOffsetPosition.y+mImage.getHeight());*/
        /*this.mDrawRect.set(positionResult.mOffsetPosition.x, positionResult.mOffsetPosition.y,
                           positionResult.mOffsetPosition.x+mImage.getWidth(),
                           positionResult.mOffsetPosition.y+mImage.getHeight());*/
        /*if (positionResult.mIsBack) {
            if (mUpdateSrcRectWithPosition) {
                mSrcRect.set(mDrawRect);
                mUpdateSrcRectWithPosition = false;
            }
        } else {
            mUpdateSrcRectWithPosition = true;
        }
        this.mDrawRect.left = this.mSrcRect.left + positionResult.mOffsetPosition.x;
        this.mDrawRect.top = this.mSrcRect.top + positionResult.mOffsetPosition.y;
        this.mDrawRect.right = this.mSrcRect.right + positionResult.mOffsetPosition.x;
        this.mDrawRect.bottom = this.mSrcRect.bottom + positionResult.mOffsetPosition.y;
        if(positionResult.mIsOver){
            mSrcRect.set(mDrawRect);
        }*/

        this.mPosition.x += positionResult.mOffsetPosition.x;
        this.mPosition.y += positionResult.mOffsetPosition.y;
        this.mDrawRect.set(mPosition.x, mPosition.y, (int) (mPosition.x + IMAGE_WIDTH),
                           (int) (mPosition.y + IMAGE_HEIGHT));

    }

    public Rect getDrawRect() {
        return mDrawRect;
    }

    public void setMusicName(String musicName) {
        this.mMusicName = musicName;
    }

    public String getMusicName() {
        return this.mMusicName;
    }

    public float getScala(){
        return this.mScala;
    }
}
