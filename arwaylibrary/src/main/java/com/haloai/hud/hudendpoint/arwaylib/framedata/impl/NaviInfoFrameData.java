package com.haloai.hud.hudendpoint.arwaylib.framedata.impl;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;

import com.haloai.hud.hudendpoint.arwaylib.calculator.result.NaviInfoResult;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.SuperResult;
import com.haloai.hud.hudendpoint.arwaylib.framedata.SuperFrameData;
import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;
import com.haloai.hud.utils.HaloLogger;

/**
 * author       : 龙;
 * date         : 2016/6/12;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.framedata.impl;
 * project_name : hudlauncher;
 */
public class NaviInfoFrameData extends SuperFrameData <NaviInfoResult>{
    private int   IMAGE_WIDTH                  = 0;
    private int   IMAGE_HEIGHT                 = 0;

    private int TEXT_START_X = 0;
    private int TEXT_START_Y = 0;
    private int TEXT_SIZE = 0;

    private static NaviInfoFrameData mNaviInfoFrameData = new NaviInfoFrameData();
    private Picture mPictureOne = new Picture();

    public static NaviInfoFrameData getInstance() {
        return mNaviInfoFrameData;
    }

    public void initDrawLine(int bitmap_width, int bitmap_height) {
        this.IMAGE_WIDTH = MathUtils.formatAsEvenNumber(bitmap_width);
        this.IMAGE_HEIGHT = MathUtils.formatAsEvenNumber(bitmap_height);
        this.TEXT_START_X = (int)(IMAGE_WIDTH*0.058);
        this.TEXT_START_Y = (int)(IMAGE_HEIGHT*0.651);
        this.TEXT_SIZE = (int)IMAGE_WIDTH/29;

    }
    @Override
    public void animOver() {

    }
    @Override
    public void update(NaviInfoResult result){
        NaviInfoResult naviInfoResult = result;
        Picture picture = this.mPictureOne;
        Canvas canvas = picture.beginRecording(IMAGE_WIDTH, IMAGE_HEIGHT);
        if (!naviInfoResult.mShouldDraw ){//取名不太贴切，目前是如果不画主路时
            if(naviInfoResult.mNaviText != null){
                /*this.mPaint.reset();
                mPaint.setColor(Color.WHITE);
                mPaint.setTextSize(TEXT_SIZE);
                String tNaviText = (naviInfoResult.mNaviText.length()< 15)?naviInfoResult.mNaviText:naviInfoResult.mNaviText.substring(0,15);
                canvas.drawText(tNaviText, TEXT_START_X, TEXT_START_Y, mPaint);*/
            }
            picture.endRecording();
            return;
        }
        HaloLogger.logE("NaviInfoFrameData","IMAGE_WIDTH :"+IMAGE_WIDTH+",IMAGE_HEIGHT"+IMAGE_HEIGHT);

        this.mPaint.reset();
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(TEXT_SIZE);
        canvas.drawText("剩余"+((int)(naviInfoResult.remainDistance/100))*1.0/10+"km"+"≈"+((int)(naviInfoResult.remainTime/6))*1.0/10+"min",TEXT_START_X,TEXT_START_Y,mPaint);
        picture.endRecording();
    }

    public Picture getPicture() {
        Picture picture = mPictureOne;
        return picture;
    }
}
