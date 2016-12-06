package com.haloai.hud.hudendpoint.arwaylib.bean;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.bean;
 * project_name : hudlauncher;
 */
public abstract class SuperBean {
    /**
     * let the bean back to reset status.
     */
    public abstract void reset();

    /***
     * this bean should be or not shuold be to show.
     */
    private boolean mIsShow = false;
    public boolean isShow(){
        return mIsShow;
    }
    public void setIsShow(boolean isShow){
        this.mIsShow = isShow;
    }
}
