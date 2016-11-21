package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;

import org.rajawali3d.math.vector.Vector3;

/**
 * author       : 龙;
 * date         : 2016/11/19;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.modeldataengine;
 * project_name : hudlauncher;
 */
public class ADASDataProcessor implements IADASDataProcessor {
    private static final String TAG                    = "ADASDataProcessor";
    private static final long   ANIM_DURATION_REDUNDAN = 100;

    private ICarADASDataProvider    mCarADASDataProvider    = new CarADASDataProvider();
    private ILaneADASDataProvider   mLaneADASDataProvider   = new LaneADASDataProvider();
    private IWalkerADASDataProvider mWalkerADASDataProvider = new WalkerADASDataProvider();

    private static Vector3 mFromPos     = null;
    private static double  mFromDegrees = 0;
    private static long    mPreTime     = 0;
    private static Vector3 mToPos       = null;
    private static double  mToDegrees   = 0;

    @Override
    public void reset() {
        mFromPos = null;
        mFromDegrees = 0;
        mPreTime = 0;
        mToPos = null;
        mToDegrees = 0;
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
    public void updateCarADAS(CarADASData carADASData) {
        mCarADASDataProvider.distChange(carADASData.distWithFrontCar);
        if (mFromPos == null || carADASData.carRealPos == null) {
            mCarADASDataProvider.showCar(
                    carADASData.pos.x, carADASData.pos.y, carADASData.pos.z,
                    MathUtils.convertAMapBearing2OpenglBearing(carADASData.bearing));
            mFromPos = carADASData.pos;
            mFromDegrees = MathUtils.convertAMapBearing2OpenglBearing(carADASData.bearing);
            mPreTime = carADASData.time;
        } else {
            long duration = carADASData.time - mPreTime;
            if (duration <= 300) {
                return;
            }
            mPreTime = carADASData.time;
            if (mToPos != null) {
                mFromPos = carADASData.carRealPos;
                mFromDegrees = Math.toDegrees(carADASData.carRealDegrees);
                mFromDegrees = mFromDegrees < 0 ? mFromDegrees + 360 : mFromDegrees;
            }
            mToPos = carADASData.pos;
            mToDegrees = MathUtils.convertAMapBearing2OpenglBearing(carADASData.bearing);
            mCarADASDataProvider.setAnim(mFromPos, mToPos, mToDegrees - mFromDegrees, duration + ANIM_DURATION_REDUNDAN);
        }
    }

    @Override
    public void hideCarADAS() {
        mCarADASDataProvider.hideCar();
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
