package com.haloai.hud.hudendpoint.arwaylib.calculator.result;

import android.graphics.Bitmap;

/**
 * Created by wangshengxing on 16/6/23.
 */
public class CrossImageResult extends SuperResult  {
    public Bitmap crossBitmap   = null;

    private static CrossImageResult            mCrossImageResult          = new CrossImageResult();
    public static CrossImageResult getInstance() {
        return mCrossImageResult;
    }
}
