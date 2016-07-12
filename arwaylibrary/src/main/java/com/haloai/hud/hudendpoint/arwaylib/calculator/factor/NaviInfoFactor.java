package com.haloai.hud.hudendpoint.arwaylib.calculator.factor;


/**
 * Created by wangshengxing on 16/6/23.
 */
public class NaviInfoFactor extends SuperFactor{
    public int remainDistance = 0;
    public int remainTime = 0;
    public String mNaviText;

    private static NaviInfoFactor mNaviInfoFactor = new NaviInfoFactor();

    public static NaviInfoFactor getInstance() {
        return mNaviInfoFactor;
    }

    public void init(boolean isDraw, int remainDistance, int remainTime,String naviText) {
        this.mNeedDraw = isDraw;
        this.remainDistance = remainDistance;
        this.remainTime = remainTime;
        this.mNaviText = naviText;
    }
}
