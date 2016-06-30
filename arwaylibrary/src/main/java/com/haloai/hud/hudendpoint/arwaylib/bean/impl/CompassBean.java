package com.haloai.hud.hudendpoint.arwaylib.bean.impl;

import com.haloai.hud.hudendpoint.arwaylib.bean.SuperBean;

/**
 * Created by wangshengxing on 16/6/22.
 * detail       : 该bean用于更新指南针表盘
 */
public class CompassBean extends SuperBean {
    //高德，自车方向（单位度），以正北为基准，顺时针增加
    private int mDirection;

    //手机获取，自车方向（单位度），以正北为基准，顺时针增加
    private float mOrientation;

    @Override
    public void reset() {
        mDirection = 0;
        mOrientation = 0;
    }

    public float getDirection() {
        return mDirection;
    }

    public CompassBean setDirection(int direction) {
        mDirection = direction;
        return this;
    }

    public float getOrientation() {
        return mOrientation;
    }

    public CompassBean setOrientation(float orientation) {
        mOrientation = orientation;
        return this;
    }
}
