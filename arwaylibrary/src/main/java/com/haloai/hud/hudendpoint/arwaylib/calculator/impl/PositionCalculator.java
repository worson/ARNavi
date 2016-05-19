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
 * note         : is move to or move by.
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
        //move to
        if (positionFactor.mPositionType == PositionFactor.PositionType.MOVE_TO) {
            move(positionResult, positionFactor.mReverse, positionFactor.mDuration,
                 positionFactor.mFromPosition.x, positionFactor.mToPosition.x,
                 positionFactor.mFromPosition.y, positionFactor.mToPosition.y,
                 offsetTime, offsetTimeTotal);
        } else {
            //move by
            move(positionResult, positionFactor.mReverse, positionFactor.mDuration,
                 0, positionFactor.mXValue,
                 0, positionFactor.mYValue,
                 offsetTime, offsetTimeTotal);
        }

        return positionResult;
    }

    private void move(PositionResult positionResult, boolean reverse, long duration, int from_x, int to_x, int from_y, int to_y, long offsetTime, long offsetTimeTotal) {
        if (reverse) {
            if (offsetTimeTotal <= duration / 2) {
                positionResult.mOffsetPosition.x =
                        (int) (Math.round(MathUtils.getOffsetValue(
                                from_x,
                                to_x,
                                offsetTime,
                                duration / 2
                        )));
                positionResult.mOffsetPosition.y =
                        (int) Math.round(MathUtils.getOffsetValue(
                                from_y,
                                to_y,
                                offsetTime,
                                duration / 2
                        ));
            } else {
                positionResult.mOffsetPosition.x =
                        (int) Math.round(MathUtils.getOffsetValue(
                                to_x,
                                from_x,
                                offsetTime,
                                duration / 2
                        ));
                positionResult.mOffsetPosition.y =
                        (int) Math.round(MathUtils.getOffsetValue(
                                to_y,
                                from_y,
                                offsetTime,
                                duration / 2
                        ));
            }
        } else {
            positionResult.mOffsetPosition.x =
                    (int) Math.round(MathUtils.getOffsetValue(
                            from_x,
                            to_x,
                            offsetTime,
                            duration
                    ));
            positionResult.mOffsetPosition.y =
                    (int) Math.round(MathUtils.getOffsetValue(
                            from_y,
                            to_y,
                            offsetTime,
                            duration
                    ));
        }
    }

    private static PositionCalculator mPositionFactor = new PositionCalculator();

    private PositionCalculator() {}

    public static PositionCalculator getInstance() {
        return mPositionFactor;
    }

    @Override
    public void reset() {
    }
}
