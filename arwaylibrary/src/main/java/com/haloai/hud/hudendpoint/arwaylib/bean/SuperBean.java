package com.haloai.hud.hudendpoint.arwaylib.bean;

/**
 * Created by 龙 on 2016/4/28.
 *
 * Every bean class use
 */
public abstract class SuperBean {
    /**
     * let the bean back to init status.
     */
    public abstract void reset();

    /***
     * this bean should be or not shuold be to show.
     */
    private boolean mIsShow = false;
    public boolean isShow(){
        return mIsShow;
    }
    public SuperBean setIsShow(boolean isShow){
        this.mIsShow = isShow;
        return this;
    }
}
