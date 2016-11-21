package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import org.rajawali3d.math.vector.Vector3;

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

    class CarADASData {
        public double  distWithFrontCar;
        public Vector3 pos;
        public long    time;
        public double  bearing;
        public Vector3 carRealPos;
        public double  carRealDegrees;

        public CarADASData(Vector3 prePos, double preBearing, Vector3 pos,double bearing,
                           long time, double distWithFrontCar) {
            this.bearing = bearing;
            this.distWithFrontCar = distWithFrontCar;
            this.pos = pos;
            this.carRealDegrees = preBearing;
            this.carRealPos = prePos;
            this.time = time;
        }
    }

    void updateCarADAS(CarADASData carADASData);

    void hideCarADAS();

    void setWalkerADASNotifier(IWalkerADASDataProvider.IWalkerADASNotifier adasNotifier);

    void setCarADASNotifier(ICarADASDataProvider.ICarADASNotifier renderer);

    void setLaneADASNotifier(ILaneADASDataProvider.ILaneADASNotifier renderer);

    ICarADASDataProvider getCarProvider();

    ILaneADASDataProvider getLaneProvider();

    IWalkerADASDataProvider getWalkerProvider();
}
