package com.haloai.hud.hudendpoint.arwaylib.bean.impl;

import com.haloai.hud.hudendpoint.arwaylib.bean.SuperBean;

/**
 * author       : 龙;
 * date         : 2016/5/6;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.bean.impl;
 * project_name : hudlauncher;
 */
public class MusicBean extends SuperBean {
    private String      mMusicName   = null;
    private MusicStatus mMusicStatus = MusicStatus.UNPLAYING;
    private long        mLastTime    = 0l;
    private long        mStartTime   = 0l;
    private long        mDuration    = 0l;

    public enum MusicStatus {
        UNPLAYING,
        START,
        PLAYING,
        PAUSE,
        STOP,
        PREV,
        NEXT
    }

    @Override
    public void reset() {
        mMusicName = "";
        mMusicStatus = MusicStatus.UNPLAYING;
        mLastTime = 0l;
        mStartTime = 0l;
    }

    public MusicBean setMusicName(String musicName) {
        this.mMusicName = musicName;
        return this;
    }

    public String getMusicName() {
        return this.mMusicName;
    }

    public MusicBean setMusicStatus(MusicStatus musicStatus) {
        this.mMusicStatus = musicStatus;
        this.mLastTime = System.currentTimeMillis();
        this.mStartTime = System.currentTimeMillis();
        return this;
    }

    public MusicStatus getMusicStatus() {
        return this.mMusicStatus;
    }

    /**
     * 该方法会返回最后一次动画的时间的同时也会将最后一次动画时间更新为当前时间
     *
     * @return last time of animation
     */
    public long getLastTime() {
        long lastTime = this.mLastTime;
        this.mLastTime = System.currentTimeMillis();
        return lastTime;
    }

    public long getStartTime(){
        return this.mStartTime;
    }

    public MusicBean setDuration(long duration) {
        this.mDuration = duration;
        return this;
    }

    public long getDuration() {
        return this.mDuration;
    }
}
