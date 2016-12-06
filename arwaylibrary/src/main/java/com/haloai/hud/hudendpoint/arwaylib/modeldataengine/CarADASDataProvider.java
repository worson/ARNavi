package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import org.rajawali3d.math.vector.Vector3;

/**
 * author       : 龙;
 * date         : 2016/11/19;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.modeldataengine;
 * project_name : hudlauncher;
 */
public class CarADASDataProvider implements ICarADASDataProvider {
    private ICarADASNotifier mCarADASNotifier;

    @Override
    public void setCarADASNotifier(ICarADASNotifier carADASNotifier) {
        mCarADASNotifier = carADASNotifier;
    }

    @Override
    public void setAnim(Vector3 from, Vector3 to, double degrees, long duration) {
        if(mCarADASNotifier!=null){
            mCarADASNotifier.onCarAnimationUpdate(new AnimData(from,to,degrees,duration));
        }
    }

    @Override
    public void showCar(double x, double y, double z, double direction) {
        if(mCarADASNotifier!=null){
            mCarADASNotifier.onCarShow(x,y,z,direction);
        }
    }

    @Override
    public void hideCar() {
        if(mCarADASNotifier!=null){
            mCarADASNotifier.onCarHide();
        }
    }

    @Override
    public void distChange(double dist) {
        if(mCarADASNotifier!=null){
            mCarADASNotifier.onDistChange(dist);
        }
    }
}
