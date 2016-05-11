package com.haloai.hud.hudendpoint.arwaylib.framedata;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Point;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.framedata;
 * project_name : hudlauncher;
 */
public abstract class SuperFrameData {
    protected Bitmap mImage = null;
    public void setImage(Bitmap image){
        this.mImage = image;
    }
    public Bitmap getImage(){
        return this.mImage;
    }

    protected Point mPosition = new Point();
    public void setPosition(int x,int y){
        this.mPosition.x = x;
        this.mPosition.y = y;
    }
    public Point getPosition(){
        return this.mPosition;
    }

    protected Paint paint = null;
    public Paint getPaint() {
        return paint;
    }

    protected Point mAnimStartPosition = new Point();
    public void setAnimStartPosition(int x,int y){
        this.mAnimStartPosition.x = x;
        this.mAnimStartPosition.y = y;
    }
    public Point getAnimStartPosition(){
        return this.mAnimStartPosition;
    }

    /**
     * 在相关元素的动画结束时调用该方法进行数据重置等相关操作
     */
    public abstract void animOver();
}
