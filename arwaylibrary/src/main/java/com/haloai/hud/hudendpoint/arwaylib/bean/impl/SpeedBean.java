package com.haloai.hud.hudendpoint.arwaylib.bean.impl;

import com.haloai.hud.hudendpoint.arwaylib.bean.SuperBean;

/**
 * Created by wangshengxing on 16/6/22.
 * detail       : 该bean用于更新速度表盘
 */
public class SpeedBean extends SuperBean {
    private int speed=0;

    @Override
    public void reset() {
        speed = 0;
    }

    public int getSpeed() {
        return speed;
    }

    /**
     * @param speed km/h
     * */
    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
