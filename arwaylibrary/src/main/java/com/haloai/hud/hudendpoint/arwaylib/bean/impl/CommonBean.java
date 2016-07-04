package com.haloai.hud.hudendpoint.arwaylib.bean.impl;

import com.haloai.hud.hudendpoint.arwaylib.bean.SuperBean;

/**
 * Created by wangshengxing on 16/7/4.
 */
public class CommonBean extends SuperBean{

    private boolean mIsYaw = false;

    @Override
    public void reset() {
        mIsYaw = false;
    }

    public boolean isYaw() {
        return mIsYaw;
    }

    public void setYaw(boolean yaw) {
        mIsYaw = yaw;
    }
}
