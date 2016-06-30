package com.haloai.hud.hudendpoint.arwaylib.framedata.impl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.RectF;

import com.haloai.hud.hudendpoint.arwaylib.calculator.result.SuperResult;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.TurnInfoResult;
import com.haloai.hud.hudendpoint.arwaylib.framedata.SuperFrameData;
import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;
import com.haloai.hud.utils.HaloLogger;


/**
 * Created by 龙 on 2016/4/29.
 */
public class TurnInfoFrameData extends SuperFrameData {
    private final static int X = 0;
    private final static int Y = 0;

    private int   IMAGE_WIDTH                  = 0;
    private int   IMAGE_HEIGHT                 = 0;
    private int   TEXT_SIZE                 = 0;
    private int   TURN_ICON_START_X                 = 0;
    private int   TURN_ICON_START_Y                 = 0;
    private int   TURN_ICON_WIDTH                 = 0;
    private int   TURN_ICON_HEIGHT                 = 0;
    private int   TURN_TEXT_START_X                 = 0;
    private int   TURN_TEXT_START_Y                 = 0;

    private Picture mPicture = new Picture();


    private static TurnInfoFrameData mTurnInfoFrameData = new TurnInfoFrameData();

    public void initDrawLine(int bitmap_width, int bitmap_height) {
        this.IMAGE_WIDTH = MathUtils.formatAsEvenNumber(bitmap_width);
        this.IMAGE_HEIGHT = MathUtils.formatAsEvenNumber(bitmap_height);
        this.TEXT_SIZE = (int)(IMAGE_WIDTH*0.060);
        this.TURN_ICON_START_X = (int) (0.068*IMAGE_WIDTH);
        this.TURN_ICON_START_Y = (int) (0.139*IMAGE_HEIGHT);
        this.TURN_ICON_WIDTH = (int) (0.102*IMAGE_WIDTH);
        this.TURN_ICON_HEIGHT = (int) (0.332*IMAGE_HEIGHT);
//        this.TURN_TEXT_START_X = TURN_ICON_START_X+TURN_ICON_WIDTH+(int)(0.105*IMAGE_WIDTH);
        this.TURN_TEXT_START_X = (int)(0.187*IMAGE_WIDTH);
        this.TURN_TEXT_START_Y = (int)(0.339*IMAGE_HEIGHT);
    }

    private TurnInfoFrameData() {
        setPosition(X,Y);
    }

    public static TurnInfoFrameData getInstance() {
        return mTurnInfoFrameData;
    }

    @Override
    public void animOver() {

    }

    @Override
    public void update(SuperResult result) throws Exception{
        if (!(result instanceof TurnInfoResult)){
            throw new Exception("SuperResult 的实例类型不可用");
        }
        TurnInfoResult turnInfoResult = (TurnInfoResult)result;
        Picture picture = this.mPicture;
        Canvas canvas = picture.beginRecording(IMAGE_WIDTH, IMAGE_HEIGHT);
        if (!turnInfoResult.mShouldDraw){
            picture.endRecording();
            return;
        }
        HaloLogger.logE("NaviInfoFrameData","IMAGE_WIDTH :"+IMAGE_WIDTH+",IMAGE_HEIGHT"+IMAGE_HEIGHT);
        HaloLogger.logE("NaviInfoFrameData","update called!"+"NextRoadName:" +turnInfoResult.turnIconDistance+",CurrentRoadName:");
        this.mPaint.reset();
        mPaint.setColor(Color.WHITE);
        Bitmap bitmap = turnInfoResult.turnIconBitmap;
        int naviIconWidth = 0;
        if (bitmap != null) {
//            bitmap.reconfigure(TURN_ICON_WIDTH,TURN_ICON_HEIGHT,bitmap.getConfig());
            canvas.drawBitmap(bitmap,null,new RectF(TURN_ICON_START_X,TURN_ICON_START_Y,TURN_ICON_START_X+TURN_ICON_WIDTH,TURN_ICON_START_Y+TURN_ICON_HEIGHT),mPaint);
//            canvas.drawBitmap(bitmap,TURN_ICON_START_X,TURN_ICON_START_Y,mPaint);//左下
            naviIconWidth = bitmap.getWidth();
        }
        mPaint.setTextSize(TEXT_SIZE);
        String distance="0";
        if(turnInfoResult.turnIconDistance>=1000){
            distance = ((int)(turnInfoResult.turnIconDistance/100))*1.0/10+"km";
        }else {
            distance = turnInfoResult.turnIconDistance+"m";
        }
        canvas.drawText(distance,TURN_TEXT_START_X,TURN_TEXT_START_Y,mPaint);

        picture.endRecording();

    }

    public Picture getPicture() {
        Picture picture = mPicture;
        return picture;
    }
}
