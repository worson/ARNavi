package com.haloai.hud.hudendpoint.arwaylib.framedata.impl;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Picture;

import com.haloai.hud.hudendpoint.arwaylib.calculator.result.SpeedResult;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.SuperResult;
import com.haloai.hud.hudendpoint.arwaylib.framedata.SuperFrameData;
import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;
import com.haloai.hud.utils.HaloLogger;

/**
 * Created by wangshengxing on 16/6/23.
 */
public class SpeedFrameData extends SuperFrameData{
    private Path mPath = new Path();
    private final static int X = 0;
    private final static int Y = 0;
    private Picture mPicture = new Picture();
    private int IMAGE_WIDTH = 0;
    private int IMAGE_HEIGHT = 0;
    private int SPEED_TEXT_START_X = 0;
    private int SPEED_TEXT_START_Y = 0;
    private int TEXT_SIZE = 0;

    private int SCALE_TEXT_START_X = 0;
    private int SCALE_TEXT_START_Y_BOTTOM = 0;
    private int SCALE_TEXT_SIZE = 0;


    private static SpeedFrameData mSpeedFrameData = new SpeedFrameData();

    public void initDrawLine(int bitmap_width, int bitmap_height) {
        this.IMAGE_WIDTH = MathUtils.formatAsEvenNumber(bitmap_width);
        this.IMAGE_HEIGHT = MathUtils.formatAsEvenNumber(bitmap_height);
        this.TEXT_SIZE = (int)(IMAGE_WIDTH*0.070);;
        this.SPEED_TEXT_START_X = (int)(IMAGE_WIDTH*0.056);
        this.SPEED_TEXT_START_Y = (int)(IMAGE_HEIGHT*0.796);
        this.SCALE_TEXT_SIZE = (int)(IMAGE_WIDTH*0.050);;
        this.SCALE_TEXT_START_X = SPEED_TEXT_START_X+(int)(IMAGE_WIDTH*0.143);
        this.SCALE_TEXT_START_Y_BOTTOM = (int)(IMAGE_HEIGHT*0.939);


    }

    private SpeedFrameData() {
        setPosition(X,Y);
    }

    public static SpeedFrameData getInstance() {
        return mSpeedFrameData;
    }

    @Override
    public void animOver() {

    }

    @Override
    public void update(SuperResult result) throws Exception {
        SpeedResult speedResult = null;
        HaloLogger.logE("SpeedFrameData","IMAGE_WIDTH :"+IMAGE_WIDTH+",IMAGE_HEIGHT"+IMAGE_HEIGHT);
        if (result instanceof SpeedResult) {
            speedResult = (SpeedResult)result;
        }else {
            throw new Exception("SuperResult 的实例类型不可用");
        }
        Picture picture = this.mPicture;
        Canvas canvas = picture.beginRecording(IMAGE_WIDTH, IMAGE_HEIGHT);
        this.mPaint.reset();
        mPaint.setColor(Color.WHITE);
        if (!speedResult.mShouldDraw){
            picture.endRecording();
            return;
        }
        mPath.reset();
        mPath.moveTo(SPEED_TEXT_START_X,SCALE_TEXT_START_Y_BOTTOM);
        mPath.lineTo(SCALE_TEXT_START_X+IMAGE_WIDTH,SCALE_TEXT_START_Y_BOTTOM);
        int speed = speedResult.speed;
        mPaint.setTextSize(TEXT_SIZE);
        canvas.drawTextOnPath(speed+"",mPath,0,0,mPaint);
//        canvas.drawText(speed+"",SPEED_TEXT_START_X,SPEED_TEXT_START_Y,mPaint);

        mPaint.setTextSize(SCALE_TEXT_SIZE);
        mPath.reset();
        mPath.moveTo(SCALE_TEXT_START_X,SCALE_TEXT_START_Y_BOTTOM);
        mPath.lineTo(SCALE_TEXT_START_X+IMAGE_WIDTH,SCALE_TEXT_START_Y_BOTTOM);
        canvas.drawTextOnPath("km/h",mPath,0,0,mPaint);
//        canvas.drawText("km/h",SCALE_TEXT_START_X, SCALE_TEXT_START_Y_BOTTOM-25,mPaint);
        picture.endRecording();
    }

    public Picture getPicture() {
        Picture picture = mPicture;
        return picture;
    }
}
