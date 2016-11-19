package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

/**
 * author       : 龙;
 * date         : 2016/11/19;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.modeldataengine;
 * project_name : hudlauncher;
 */
public interface IADASDataProcessor {
    void reset();
    void showWalkerADAS();
    void hideWalkerADAS();
    void showLaneADAS(boolean isLeft);
    void hideLaneADAS();

    class CarADASData{

    }
    void showCarADAS(CarADASData carADASData);
    void hideCarADAS();

    void setWalkerADASNotifier(IWalkerADASDataProvider.IWalkerADASNotifier adasNotifier);
    void setCarADASNotifier(ICarADASDataProvider.ICarADASNotifier renderer);
    void setLaneADASNotifier(ILaneADASDataProvider.ILaneADASNotifier renderer);

    ICarADASDataProvider getCarProvider();
    ILaneADASDataProvider getLaneProvider();
    IWalkerADASDataProvider getWalkerProvider();
}
