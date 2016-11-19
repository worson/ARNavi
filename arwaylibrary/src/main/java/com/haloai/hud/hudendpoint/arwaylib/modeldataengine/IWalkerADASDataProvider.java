package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

/**
 * author       : 龙;
 * date         : 2016/11/19;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.modeldataengine;
 * project_name : hudlauncher;
 */
public interface IWalkerADASDataProvider {
    interface IWalkerADASNotifier {
        void showWalker();
        void hideWalker();
        void setWalkerADASDataProvider(IWalkerADASDataProvider adasDataProvider);
    }
}
