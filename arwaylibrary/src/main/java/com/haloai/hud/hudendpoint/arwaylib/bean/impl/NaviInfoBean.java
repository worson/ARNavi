package com.haloai.hud.hudendpoint.arwaylib.bean.impl;

import android.graphics.Bitmap;

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
    private Bitmap mNaviIconBitmap   = null;
    private Bitmap mCrossBitmap   = null;
    private String mCurrentRoadName  = "";
    private String mNextRoadName     = "";
    private int mSpeed;
    private int mLimitSpeed;
    private int mServiceAreaDistance;

    //道路向导信息
    private int mStepRetainDistance;//获取路线剩余时间 min
    private int mPathRetainTime;//获取路线剩余时间 min
    private int mPathRetainDistance;//获取路线剩余距离 m

    private int mPathTotalDistance = 0;//获取路线总距离 m

    private String mNaviText;


    @Override
    public void reset() {
        mNaviIconDistance = 0;
        mNaviIcon = 0;
        mNaviIconBitmap = null;
        mCrossBitmap = null;
        mCurrentRoadName = "";
        mNextRoadName = "";
        mSpeed=0;
        mNaviText = "";
        mPathTotalDistance = 0;
        mPathRetainDistance = 0;
        mPathRetainTime = 0;
        mStepRetainDistance=0;
    }

    public int getPathTotalDistance() {
        return mPathTotalDistance;
    }

    public void setPathTotalDistance(int pathTotalDistance) {
        mPathTotalDistance = pathTotalDistance;
    }

    public int getStepRetainDistance() {
        return mStepRetainDistance;
    }

    public NaviInfoBean setStepRetainDistance(int stepRetainDistance) {
        mStepRetainDistance = stepRetainDistance;
        return this;
    }

    public NaviInfoBean setNaviText(String beginNaviText) {
        mNaviText = beginNaviText;
        return this;
    }

    public String getNaviText() {
        return mNaviText;
    }

    public int getPathRetainTime() {
        return mPathRetainTime;
    }

    public NaviInfoBean setPathRetainTime(int pathRetainTime) {
        mPathRetainTime = pathRetainTime;
        return this;
    }

    public int getPathRetainDistance() {
        return mPathRetainDistance;
    }

    public NaviInfoBean setPathRetainDistance(int pathRetainDistance) {
        mPathRetainDistance = pathRetainDistance;
        return this;
    }

    public int getNaviIcon() {
        return mNaviIcon;
    }

    public NaviInfoBean setNaviIcon(int naviIcon) {
        mNaviIcon = naviIcon;
        return this;
    }

    public Bitmap getNaviIconBitmap() {
        return mNaviIconBitmap;
    }

    public NaviInfoBean setNaviIconBitmap(Bitmap naviIconBitmap) {
        mNaviIconBitmap = naviIconBitmap;
        return this;
    }

    public int getServiceAreaDistance() {
        return mServiceAreaDistance;
    }

    public NaviInfoBean setServiceAreaDistance(int serviceAreaDistance) {
        mServiceAreaDistance = serviceAreaDistance;
        return this;
    }

    public Bitmap getCrossBitmap() {
        return mCrossBitmap;
    }

    public int getLimitSpeed() {
        return mLimitSpeed;
    }

    public NaviInfoBean setLimitSpeed(int limitSpeed) {
        mLimitSpeed = limitSpeed;
        return this;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public NaviInfoBean setSpeed(int speed) {
        mSpeed = speed;
        return this;
    }

    public NaviInfoBean setCrossBitmap(Bitmap crossBitmap) {
        mCrossBitmap = crossBitmap;
        return this;
    }

    public int getNaviIconDistance() {
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
