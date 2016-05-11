package com.haloai.hud.hudendpoint.arwaylib.framedata.impl;

import android.graphics.Rect;

import com.haloai.hud.hudendpoint.arwaylib.calculator.result.PositionResult;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.ScaleResult;
import com.haloai.hud.hudendpoint.arwaylib.framedata.SuperFrameData;
import com.haloai.hud.utils.HaloLogger;

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

    private Rect   mDrawRect    = new Rect(100, 100, 200, 200);
    private String mMusicName   = "";
    private double mWidthScala  = 1f;
    private double mHeightScala = 1f;
    //这两个变量用于保存在设置position时的小数部分,如果不保存会直接被剪掉,导致误差越来越多.
    private double mXFloatValue = 0f;
    private double mYFloatValue = 0f;

    private static MusicFrameData mMusicFrameData = new MusicFrameData();

    private MusicFrameData() {
        setPosition(X, Y);
        setAnimStartPosition(X, Y);
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
        this.mWidthScala += scalaResult.mOffsetWidthScala;
        this.mHeightScala += scalaResult.mOffsetHeightScala;

        double offsetX = this.mPosition.x - scalaResult.mOffsetWidthScala * IMAGE_WIDTH / 2+this.mXFloatValue;
        this.mXFloatValue = offsetX - (int)offsetX;
        this.mPosition.x = (int) offsetX;
        double offsetY = this.mPosition.y - scalaResult.mOffsetHeightScala * IMAGE_HEIGHT / 2+this.mYFloatValue;
        this.mYFloatValue = offsetY - (int)offsetY;
        this.mPosition.y = (int) offsetY;
        this.mDrawRect.set(mPosition.x, mPosition.y, (int) (mPosition.x + IMAGE_WIDTH + ((this.mWidthScala - 1) * IMAGE_WIDTH)),
                           (int) (mPosition.y + IMAGE_HEIGHT + ((this.mHeightScala - 1) * IMAGE_HEIGHT)));
        /*HaloLogger.logE("position__:", "scala:"+this.mDrawRect.left+","+this.mDrawRect.top+","
                +this.mDrawRect.right+","+this.mDrawRect.bottom);*/
    }

    public void updateWithPosition(PositionResult positionResult) {
        this.mPosition.x += positionResult.mOffsetPosition.x;
        this.mPosition.y += positionResult.mOffsetPosition.y;
        this.mDrawRect.set(mPosition.x, mPosition.y, (int) (mPosition.x + IMAGE_WIDTH + ((this.mWidthScala - 1) * IMAGE_WIDTH)),
                           (int) (mPosition.y + IMAGE_HEIGHT + ((this.mHeightScala - 1) * IMAGE_HEIGHT)));
        HaloLogger.logE("position__:", "position:"+this.mDrawRect.left+","+this.mDrawRect.top+","
                +this.mDrawRect.right+","+this.mDrawRect.bottom);
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

    public double getWidthScala() {
        return this.mWidthScala;
    }

    public double getHeightScala() {
        return this.mHeightScala;
    }

    @Override
    public void animOver() {
        this.mAnimStartPosition.set(this.mPosition.x, this.mPosition.y);
    }
}
