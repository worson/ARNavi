package com.haloai.hud.hudendpoint.arwaylib.utils;

import com.haloai.hud.utils.HaloLogger;

/**
 * 用途：
 * 计算代码段的消耗时间和平均时间
 * 计算代码的调用频率
 * Created by wangshengxing on 16/9/27.
 */
public class TimeRecorder {
    private final static String START_MSG = "noStart";
    private String mStartMsg = START_MSG;

    private boolean mIsLogFilter = false;
    private boolean mUpdateLogTime =true;
    private int mLogFilterTime = 1000;
    private double mLogTime = 0;
    private int    cnt      = 0;
    private double cTime    = 0;//当前时间
    private double lTime    = 0;//上次时间
    private double sTime    = 0;//开始时间
    private double interval = 0;//间隔时间
    private double tTime    = 0;//总时间

    private double  frame  = 0;
    private double tFrame = 0;

    private double MAX_LOG_FRAME = 3;
    public void start(){
        start(null);
    }
    public void start(String msg){
        if (msg != null) {
            mStartMsg = msg;
        }
        sTime = System.currentTimeMillis();
    }
    public void reset(){
        mLogTime = 0;
        cnt      = 0;
        cTime    = 0;//当前时间
        lTime    = 0;//上次时间
        sTime    = 0;//开始时间
        interval = 0;//间隔时间
        tTime    = 0;//总时间
        frame  = 0;
        tFrame = 0;
    }

    /**
     * 记录调试的时间
     */
    public void recorde(){
        cTime = System.currentTimeMillis();
        interval = cTime-sTime;
        tTime += interval;
        cnt++;
        frame = (1000/(cTime-lTime));
        tFrame += frame;
        lTime = cTime;
    }

    public void enableTimeFilter(boolean timeFilter) {
        mIsLogFilter = timeFilter;
    }

    public void setLogFilterTime(int logFilterTime) {
        mLogFilterTime = logFilterTime;
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

    public String getLog(String name){
        return String.format(", %s , cnt , %s , interval , %3f ms ,average interval , %3f ms ,frame , %s , average frame , %s",name,cnt,interval,getAverageInterval(),frame,getAverageFrame());
    }

    /**
     * 打印调用频率和与@start的时间间隔
     * @param tag
     * @param name
     */
    public void recordeAndLog(String tag,String name){
        recorde();
        if(!mIsLogFilter || (cTime-mLogTime)>=mLogFilterTime) {
            mLogTime = cTime;
            HaloLogger.logE(tag, mStartMsg+" ,"+getLog(name));
            mStartMsg = START_MSG;
        }
    }

    public boolean isTimeLoggable(){
        cTime = System.currentTimeMillis();
        return !mIsLogFilter || (cTime-mLogTime)>=mLogFilterTime;
    }
    /**
     * 达到一个的时间才会打印log，用于过滤频率高的log
     * @param tag
     * @param msg
     */
    public void timerLog(String tag,String msg){
        cTime = System.currentTimeMillis();
        if((cTime-mLogTime)>=mLogFilterTime){
            if (mUpdateLogTime){
                updateLogTime();
            }
            HaloLogger.logE(tag, msg);
        }
    }

    public void setUpdateLogTime(boolean updateLogTime) {
        mUpdateLogTime = updateLogTime;
    }

    public void forceLogTime(){
        mLogTime = cTime-2*mLogFilterTime;
    }
    public void updateLogTime(){
        mLogTime = cTime;
    }
    public void recordeAndPrint(String name){
        recorde();
        System.out.print(getLog(name)+"\n");
    }
}
