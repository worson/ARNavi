package com.haloai.hud.hudendpoint.arwaylib.calculator.impl;

import com.haloai.hud.hudendpoint.arwaylib.calculator.SuperCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.PositionFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.PositionResult;
import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;

/**
 * author       : 龙;
 * date         : 2016/5/6;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.calculator.impl;
 * project_name : hudlauncher;
 * note         : is move to ,not move by.
 */
public class PositionCalculator extends SuperCalculator<PositionResult, PositionFactor> {
    @Override
    public PositionResult calculate(PositionFactor positionFactor) {
        PositionResult positionResult = new PositionResult();
        long currentTime = System.currentTimeMillis();
        long offsetTimeTotal = currentTime - positionFactor.mStartTime;
        long offsetTime = currentTime - positionFactor.mLastTime;
        if (offsetTimeTotal >= positionFactor.mDuration) {
            positionResult.mIsOver = true;
        }
        if (positionFactor.mReverse) {
            if (offsetTimeTotal <= positionFactor.mDuration / 2) {
                positionResult.mOffsetPosition.x =
                        (int)MathUtils.getOffsetValue(
                                positionFactor.mFromPosition.x,
                                positionFactor.mToPosition.x,
                                offsetTime,
                                positionFactor.mDuration / 2,
                                positionResult.mOffsetPosition.x);
                positionResult.mOffsetPosition.y =
                        (int) MathUtils.getOffsetValue(
                                positionFactor.mFromPosition.y,
                                positionFactor.mToPosition.y,
                                offsetTime,
                                positionFactor.mDuration / 2,
                                positionResult.mOffsetPosition.y);
            } else {
                positionResult.mOffsetPosition.x =
                        (int) MathUtils.getOffsetValue(
                                positionFactor.mToPosition.x,
                                positionFactor.mFromPosition.x,
                                offsetTime - positionFactor.mDuration / 2,
                                positionFactor.mDuration / 2,
                                positionResult.mOffsetPosition.x);
                positionResult.mOffsetPosition.y =
                        (int) MathUtils.getOffsetValue(
                                positionFactor.mToPosition.y,
                                positionFactor.mFromPosition.y,
                                offsetTime - positionFactor.mDuration / 2,
                                positionFactor.mDuration / 2,
                                positionResult.mOffsetPosition.y);
            }
        } else {
            positionResult.mOffsetPosition.x =
                    (int) MathUtils.getOffsetValue(
                            positionFactor.mFromPosition.x,
                            positionFactor.mToPosition.x,
                            offsetTime,
                            positionFactor.mDuration,
                            positionResult.mOffsetPosition.x);
            positionResult.mOffsetPosition.y =
                    (int) MathUtils.getOffsetValue(
                            positionFactor.mFromPosition.y,
                            positionFactor.mToPosition.y,
                            offsetTime,
                            positionFactor.mDuration,
                            positionResult.mOffsetPosition.y);
        }

        return positionResult;
    }

    private static PositionCalculator mPositionFactor = new PositionCalculator();

    private PositionCalculator() {}

    public static PositionCalculator getInstance() {
        return mPositionFactor;
    }
}
