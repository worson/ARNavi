package com.haloai.hud.hudendpoint.arwaylib.calculator.impl;

import com.haloai.hud.hudendpoint.arwaylib.calculator.SuperCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.RotateFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.RotateResult;
import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.calculator.result;
 * project_name : hudlauncher;
 */
public class RotateCalculator extends SuperCalculator<RotateResult, RotateFactor> {
    @Override
    public RotateResult calculate(RotateFactor rotateFactor) {
        RotateResult rotateResult = new RotateResult();
        long currentTime = System.currentTimeMillis();
        long offsetTimeTotal = currentTime - rotateFactor.mStartTime;
        long offsetTime = currentTime - rotateFactor.mLastTime;
        if (offsetTimeTotal >= rotateFactor.mDuration) {
            rotateResult.mIsOver = true;
        }
        if (rotateFactor.mReverse) {
            if (offsetTimeTotal <= rotateFactor.mDuration / 2) {
                rotateResult.mOffsetRotateX = MathUtils.getOffsetValue(0, rotateFactor.mRotateX, offsetTime, rotateFactor.mDuration / 2);
                rotateResult.mOffsetRotateY = MathUtils.getOffsetValue(0, rotateFactor.mRotateY, offsetTime, rotateFactor.mDuration / 2);
                rotateResult.mOffsetRotateZ = MathUtils.getOffsetValue(0, rotateFactor.mRotateZ, offsetTime, rotateFactor.mDuration / 2);
            } else {
                rotateResult.mOffsetRotateX = MathUtils.getOffsetValue(rotateFactor.mRotateX, 0, offsetTime, rotateFactor.mDuration / 2);
                rotateResult.mOffsetRotateY = MathUtils.getOffsetValue(rotateFactor.mRotateY, 0, offsetTime, rotateFactor.mDuration / 2);
                rotateResult.mOffsetRotateZ = MathUtils.getOffsetValue(rotateFactor.mRotateZ, 0, offsetTime, rotateFactor.mDuration / 2);
            }
        } else {
            rotateResult.mOffsetRotateX = MathUtils.getOffsetValue(0, rotateFactor.mRotateX, offsetTime, rotateFactor.mDuration);
            rotateResult.mOffsetRotateY = MathUtils.getOffsetValue(0, rotateFactor.mRotateY, offsetTime, rotateFactor.mDuration);
            rotateResult.mOffsetRotateZ = MathUtils.getOffsetValue(0, rotateFactor.mRotateZ, offsetTime, rotateFactor.mDuration);

        }
        return rotateResult;
    }

    private static RotateCalculator mRotateFactor = new RotateCalculator();

    private RotateCalculator() {}

    public static RotateCalculator getInstance() {
        return mRotateFactor;
    }

    @Override
    public void reset() {
    }
}
