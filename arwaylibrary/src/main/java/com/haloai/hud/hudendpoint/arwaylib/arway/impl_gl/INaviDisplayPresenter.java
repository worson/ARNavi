package com.haloai.hud.hudendpoint.arwaylib.arway.impl_gl;

/**
 * Created by wangshengxing on 16/9/13.
 */
public interface INaviDisplayPresenter {
    /***
     * 导航开始前界面控制
     */
    public void prepareNavingStartView();

    /***
     * 导航开始时界面控制
     */
    public void onNavingStartView();

    /***
     * 导航中时界面控制
     */
    public void onNavingView();

    /***
     * 导航结束时界面控制
     */
    public void onNavingEndView();

    /***
     * 导航退出时界面控制
     */
    public void onNavingStopView();


    /***
     * 偏航开始时界面控制
     */
    public void onYawStartView();

    /***
     * 偏航结束时界面控制
     */
    public void onYawEndView();

    /***
     * 导航的环境：GPS、网络，生变变化时的界面更新
     * 显示情景：
     * 无网络
     *  未偏航时，能正常导航
     *  偏航时，文字显示
     *无GPS
     *  不能更新速度、里程，指南针正常工作
     */
    public void onNavingContextChangedView();
}
