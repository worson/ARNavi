package com.haloai.hud.hudendpoint.arwaylib.bean.impl;

import com.haloai.hud.hudendpoint.arwaylib.bean.SuperBean;

/**
 * author       : 龙;
 * date         : 2016/6/12;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.bean.impl;
 * project_name : hudlauncher;
 * detail       : 该bean内容包括诱导图标和距离,当前路名下一条路名.
 */
public class NaviInfoBean extends SuperBean {
    private int    mNaviIconDistance = 0;
    private int    mNaviIcon         = 0;
    private String mCurrentRoadName  = "";
    private String mNextRoadName     = "";

    @Override
    public void reset() {
        mNaviIconDistance = 0;
        mNaviIcon = 0;
        mCurrentRoadName = "";
        mNextRoadName = "";
    }

    public int getNaviIcon() {
        return mNaviIcon;
    }

    public NaviInfoBean setNaviIcon(int naviIcon) {
        mNaviIcon = naviIcon;
        return this;
    }

    public float getNaviIconDistance() {
        return mNaviIconDistance;
    }

    public NaviInfoBean setNaviIconDistance(int naviIconDistance) {
        mNaviIconDistance = naviIconDistance;
        return this;
    }

    public String getNextRoadName() {
        return mNextRoadName;
    }

    public NaviInfoBean setNextRoadName(String nextRoadName) {
        mNextRoadName = nextRoadName;
        return this;
    }

    public String getCurrentRoadName() {
        return mCurrentRoadName;
    }

    public NaviInfoBean setCurrentRoadName(String currentRoadName) {
        mCurrentRoadName = currentRoadName;
        return this;
    }
}
