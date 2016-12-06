package com.haloai.hud.hudendpoint.arwaylib.bean.impl;

import com.haloai.hud.hudendpoint.arwaylib.bean.SuperBean;

/**
 * Created by wangshengxing on 16/7/4.
 */
public class CommonBean extends SuperBean{

    private boolean mIsSimu      = true;//是否模拟导航
    private boolean mIsYaw       = false;//是否偏航
    private boolean mNaviEnd     = false;//导航结束
    private boolean mStartOk     = false;//起步完成
    private boolean mNavingStart = false;//开始导航
    private boolean mGpsWork     = false;
    private boolean mHasNetwork     = false;//网络状态
    private boolean mIsMatchNaviPath = true;

    @Override
    public void reset() {
        mIsYaw = false;
        mNaviEnd = false;
        mStartOk = false;
        mNavingStart = false;
        mIsMatchNaviPath = true;
        /*mGpsWork = false;
        mHasNetwork = false;*/
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

    public boolean isGpsWork() {
        return mGpsWork;
    }

    public void setGpsWork(boolean gpsWork) {
        mGpsWork = gpsWork;
    }

    public boolean isHasNetwork() {
        return mHasNetwork;
    }

    public void setHasNetwork(boolean hasNetwork) {
        mHasNetwork = hasNetwork;
    }

    public boolean isSimu() {
        return mIsSimu;
    }

    public void setSimu(boolean simu) {
        mIsSimu = simu;
    }

    public boolean isMatchNaviPath() {
        return mIsMatchNaviPath;
    }

    public void setMatchNaviPath(boolean matchNaviPath) {
        mIsMatchNaviPath = matchNaviPath;
    }
}
