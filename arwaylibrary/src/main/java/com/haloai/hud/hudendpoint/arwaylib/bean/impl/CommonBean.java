package com.haloai.hud.hudendpoint.arwaylib.bean.impl;

import com.haloai.hud.hudendpoint.arwaylib.bean.SuperBean;

/**
 * Created by wangshengxing on 16/7/4.
 */
public class CommonBean extends SuperBean{

    private boolean mIsYaw       = false;
    private boolean mNaviEnd     = false;
    private boolean mStartOk     = false;//起步完成
    private boolean mNavingStart = false;//开始导航

    @Override
    public void reset() {
        mIsYaw = false;
        mNaviEnd = false;
        mStartOk = false;
        mNavingStart = false;
    }

    public boolean isNavingStart() {
        return mNavingStart;
    }

    public void setNavingStart(boolean navingStart) {
        mNavingStart = navingStart;
    }

    public boolean isYaw() {
        return mIsYaw;
    }

    public void setYaw(boolean yaw) {
        mIsYaw = yaw;
    }

    public boolean isNaviEnd() {
        return mNaviEnd;
    }

    public void setNaviEnd(boolean naviEnd) {
        mNaviEnd = naviEnd;
    }

    public boolean isStartOk() {
        return mStartOk;
    }

    public void setStartOk(boolean startOk) {
        mStartOk = startOk;
    }
}
