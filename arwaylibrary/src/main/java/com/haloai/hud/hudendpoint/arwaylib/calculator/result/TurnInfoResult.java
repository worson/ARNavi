package com.haloai.hud.hudendpoint.arwaylib.calculator.result;

import android.graphics.Bitmap;

/**
 * Created by wangshengxing on 16/6/23.
 */
public class TurnInfoResult extends SuperResult {


    public int    turnIconDistance = 0;
    public Bitmap turnIconBitmap   = null;

    private static TurnInfoResult            mTurnInfoResult          = new TurnInfoResult();
    public static TurnInfoResult getInstance() {
        return mTurnInfoResult;
    }

}
