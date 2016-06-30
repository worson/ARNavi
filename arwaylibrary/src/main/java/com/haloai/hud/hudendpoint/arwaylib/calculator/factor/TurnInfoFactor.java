package com.haloai.hud.hudendpoint.arwaylib.calculator.factor;

import android.graphics.Bitmap;

/**
 * Created by wangshengxing on 16/6/23.
 */
public class TurnInfoFactor extends SuperFactor {
    public int    turnIconDistance = 0;
    public Bitmap turnIconBitmap   = null;

    private static TurnInfoFactor mTurnInfoFactor = new TurnInfoFactor();

    public static TurnInfoFactor getInstance() {
        return mTurnInfoFactor;
    }

    public void init(boolean isDraw, int turnIconDistance, Bitmap turnIconBitmap) {
        this.mNeedDraw = isDraw;
        this.turnIconDistance = turnIconDistance;
        this.turnIconBitmap = turnIconBitmap;
    }
}
