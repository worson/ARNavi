package com.haloai.hud.hudendpoint.arwaylib.calculator.result;

import android.graphics.Bitmap;

/**
 * Created by wangshengxing on 16/6/23.
 */
public class NaviInfoResult extends SuperResult {

    public int remainDistance = 0;
    public int remainTime = 0;
    public String mNaviText;

    private static NaviInfoResult            mNaviInfoResult          = new NaviInfoResult();
    public static NaviInfoResult getInstance() {
        return mNaviInfoResult;
    }
}
