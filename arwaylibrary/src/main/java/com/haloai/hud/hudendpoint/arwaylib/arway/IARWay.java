package com.haloai.hud.hudendpoint.arwaylib.arway;

import android.view.View;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.calculator.result;
 * project_name : hudlauncher;
 */
public interface IARWay {
    /**
     * let arway back to reset status.
     */
    void reset();

    /**
     * let arway back to reset status , and release the resource about arway.
     */
    void release();

    /**
     * get the arway for show.
     * @return arway view
     */
    View getARWay();

    /**
     * start draw arway.
     */
    void start();

    /**
     * pause draw arway.
     */
    void pause();

    /**
     * stop draw arway.
     */
    void stop();

    /**
     * return is or not running in current time.
     * @return is or not running.
     */
    boolean isRunning();


    void continue_();
}
