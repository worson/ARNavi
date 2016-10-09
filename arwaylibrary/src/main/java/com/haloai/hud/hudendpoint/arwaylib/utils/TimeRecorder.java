package com.haloai.hud.hudendpoint.arwaylib.utils;

import com.haloai.hud.utils.HaloLogger;

/**
 * 用途：
 * 计算代码段的消耗时间和平均时间
 * 计算代码的调用频率
 * Created by wangshengxing on 16/9/27.
 */
public class TimeRecorder {
    private int cnt = 0;
    private double cTime;//当前时间
    private double lTime;//上次时间
    private double sTime;//开始时间
    private double interval;//间隔时间
    private double tTime = 0;//总时间

    private float frame = 0;
    private double tFrame = 0;

    private double MAX_LOG_FRAME = 3;
    private double mLastLogTime = System.currentTimeMillis();
    public void start(){
        sTime = System.currentTimeMillis();
    }

    public void recorde(){
        cTime = System.currentTimeMillis();
        interval = cTime-sTime;
        tTime += interval;
        cnt++;
        frame = (int)(1000/(cTime-lTime));
        tFrame += frame;
        lTime = cTime;

    }
    public double getInterval() {
        return interval;
    }

    public double getAverageInterval() {
        return tTime/cnt;
    }

    public double getFrame() {
        return frame;
    }

    public double getAverageFrame() {
        return tFrame/cnt;
    }

    public void recordeAndLog(String tag,String name){
        recorde();
        HaloLogger.logE(tag,String.format(" %s: interval = %3f ms ,average interval = %3f ms ,frame = %s , average frame = %s",name,interval,getAverageInterval(),frame,getAverageFrame()));
    }
}
