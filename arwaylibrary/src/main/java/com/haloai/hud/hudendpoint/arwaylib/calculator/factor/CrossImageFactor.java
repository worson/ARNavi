package com.haloai.hud.hudendpoint.arwaylib.calculator.factor;

import android.graphics.Bitmap;

/**
 * Created by wangshengxing on 16/6/23.
 */
public class CrossImageFactor extends SuperFactor {
    public Bitmap crossIconBitmap   = null;
    private static CrossImageFactor mCrossImageFactor = new CrossImageFactor();

    public static CrossImageFactor getInstance() {
        return mCrossImageFactor;
    }
    public void init(boolean isDraw,Bitmap bitmap) {
        this.mNeedDraw = isDraw;
        this.crossIconBitmap = bitmap;
    }
}
