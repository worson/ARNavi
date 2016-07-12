package com.haloai.hud.hudendpoint.arwaylib.framedata.impl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.RectF;

import com.haloai.hud.hudendpoint.arwaylib.calculator.result.CrossImageResult;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.SuperResult;
import com.haloai.hud.hudendpoint.arwaylib.framedata.SuperFrameData;
import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;
import com.haloai.hud.utils.HaloLogger;

/**
 * Created by 龙 on 2016/4/29.
 */
public class CrossImageFrameData extends SuperFrameData {
    private final static int X = 0;
    private final static int Y = 0;
    private Picture mPicture = new Picture();
    private int   IMAGE_WIDTH                  = 0;//840
    private int   IMAGE_HEIGHT                 = 0;//280
    private int   CROSS_ICON_START_X                 = 0;
    private int   CROSS_ICON_START_Y                 = 0;
    private int   CROSS_ICON_WIDTH                 = 0;
    private int   CROSS_ICON_HEIGHT                 = 0;

    private static CrossImageFrameData mCrossImageFrameData = new CrossImageFrameData();

    public void initDrawLine(int bitmap_width, int bitmap_height) {
        this.IMAGE_WIDTH = MathUtils.formatAsEvenNumber(bitmap_width);
        this.IMAGE_HEIGHT = MathUtils.formatAsEvenNumber(bitmap_height);
        this.CROSS_ICON_START_X = (int)(IMAGE_WIDTH*0.050);
        this.CROSS_ICON_START_Y = 0;
        this.CROSS_ICON_WIDTH = (int)(IMAGE_WIDTH*0.342);
        this.CROSS_ICON_HEIGHT = (int)(IMAGE_HEIGHT*0.696);

    }

    private CrossImageFrameData() {
        setPosition(X,Y);
    }

    public static CrossImageFrameData getInstance() {
        return mCrossImageFrameData;
    }

    @Override
    public void animOver() {

    }

    @Override
    public void update(SuperResult result) throws Exception {
        CrossImageResult crossImageResult = null;
        if (result instanceof CrossImageResult) {
            crossImageResult = (CrossImageResult)result;
        }else {
            throw new Exception("SuperResult 的实例类型不可用");
        }
        Picture picture = this.mPicture;
        Canvas canvas = picture.beginRecording(IMAGE_WIDTH, IMAGE_HEIGHT);
        if (!crossImageResult.mShouldDraw){
            picture.endRecording();
            return;
        }
        HaloLogger.logE("CrossImageFrameData","IMAGE_WIDTH :"+IMAGE_WIDTH+",IMAGE_HEIGHT"+IMAGE_HEIGHT);
        this.mPaint.reset();
        mPaint.setColor(Color.WHITE);
        Bitmap bitmap = crossImageResult.crossBitmap;
        if (bitmap != null) {
            canvas.drawBitmap(bitmap,null,new RectF(CROSS_ICON_START_X,CROSS_ICON_START_Y,CROSS_ICON_START_X+CROSS_ICON_WIDTH,CROSS_ICON_START_Y+CROSS_ICON_HEIGHT),mPaint);
//            canvas.drawBitmap(bitmap,CROSS_ICON_START_X,CROSS_ICON_START_Y,mPaint);//左下
        }else {
            HaloLogger.logE("CrossImageFrameData","bitmap is null ");
        }
        picture.endRecording();

    }

    public Picture getPicture() {
        Picture picture = mPicture;
        return picture;
    }
}
