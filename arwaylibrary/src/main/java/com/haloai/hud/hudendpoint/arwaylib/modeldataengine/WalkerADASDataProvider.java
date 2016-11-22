package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

/**
 * author       : 龙;
 * date         : 2016/11/19;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.modeldataengine;
 * project_name : hudlauncher;
 */
public class WalkerADASDataProvider implements IWalkerADASDataProvider {
    private IWalkerADASNotifier mWalkerADASNotifier;

    @Override
    public void setWalkerADASNotifier(IWalkerADASNotifier walkerADASNotifier) {
        this.mWalkerADASNotifier = walkerADASNotifier;
    }

    @Override
    public void showWalker() {
        if(mWalkerADASNotifier!=null){
            mWalkerADASNotifier.showWalker();
        }
    }

    @Override
    public void hideWalker() {
        if(mWalkerADASNotifier!=null){
            mWalkerADASNotifier.hideWalker();
        }
    }
}
