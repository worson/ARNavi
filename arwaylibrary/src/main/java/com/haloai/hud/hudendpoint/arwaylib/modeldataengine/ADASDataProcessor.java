package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

/**
 * author       : 龙;
 * date         : 2016/11/19;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.modeldataengine;
 * project_name : hudlauncher;
 */
public class ADASDataProcessor implements IADASDataProcessor{
    private ICarADASDataProvider mCarADASDataProvider = new CarADASDataProvider();
    private ILaneADASDataProvider mLaneADASDataProvider = new LaneADASDataProvider();
    private IWalkerADASDataProvider mWalkerADASDataProvider = new WalkerADASDataProvider();

    @Override
    public void reset() {

    }

    @Override
    public void showWalkerADAS() {

    }

    @Override
    public void hideWalkerADAS() {

    }

    @Override
    public void showLaneADAS(boolean isLeft) {

    }

    @Override
    public void hideLaneADAS() {

    }

    @Override
    public void showCarADAS(CarADASData carADASData) {

    }

    @Override
    public void hideCarADAS(){

    }

    @Override
    public void setWalkerADASNotifier(IWalkerADASDataProvider.IWalkerADASNotifier adasNotifier) {

    }

    @Override
    public void setCarADASNotifier(ICarADASDataProvider.ICarADASNotifier adasNotifier) {
        mCarADASDataProvider.setCarADASNotifier(adasNotifier);
    }

    @Override
    public void setLaneADASNotifier(ILaneADASDataProvider.ILaneADASNotifier adasNotifier) {

    }

    @Override
    public ICarADASDataProvider getCarProvider() {
        return mCarADASDataProvider;
    }

    @Override
    public ILaneADASDataProvider getLaneProvider() {
        return mLaneADASDataProvider;
    }

    @Override
    public IWalkerADASDataProvider getWalkerProvider() {
        return mWalkerADASDataProvider;
    }
}
