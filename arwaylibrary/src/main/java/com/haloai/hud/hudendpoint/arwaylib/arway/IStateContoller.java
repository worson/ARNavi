package com.haloai.hud.hudendpoint.arwaylib.arway;

/**
 * Created by wangshengxing on 16/8/31.
 */
public interface IStateContoller {
    void prepareARWayStart(); //启动ARway前准备操作
    void onARWayStart(); //启动ARway
    void stopDrawHudway(); //停止绘制ARway
}
