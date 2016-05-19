package com.haloai.hud.hudendpoint.arwaylib.calculator.impl;

import com.haloai.hud.hudendpoint.arwaylib.calculator.SuperCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.ScaleFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.ScaleResult;
import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.calculator.impl;
 * project_name : hudlauncher;
 */
public class ScaleCalculator extends SuperCalculator<ScaleResult, ScaleFactor> {
    @Override
    public ScaleResult calculate(ScaleFactor scalaFactor) {
        ScaleResult scalaResult = new ScaleResult();
        long currentTime = System.currentTimeMillis();
        long offsetTimeTotal = currentTime - scalaFactor.mStartTime;
        long offsetTime =  currentTime - scalaFactor.mLastTime;
        if (offsetTimeTotal >= scalaFactor.mDuration) {
            scalaResult.mIsOver = true;
        }
        if (scalaFactor.mReverse) {
            if (offsetTimeTotal <= scalaFactor.mDuration / 2) {
                scalaResult.mOffsetWidthScala =  (MathUtils.getOffsetValue(
                        scalaFactor.mFromWidthScala, scalaFactor.mToWidthScala,
                        offsetTime, scalaFactor.mDuration/2));
                scalaResult.mOffsetHeightScala =  (MathUtils.getOffsetValue(
                        scalaFactor.mFromHeightScala, scalaFactor.mToHeightScala,
                        offsetTime, scalaFactor.mDuration/2));
            } else {
                scalaResult.mOffsetWidthScala =  (MathUtils.getOffsetValue(
                        scalaFactor.mToWidthScala, scalaFactor.mFromWidthScala,
                        offsetTime, scalaFactor.mDuration/2));
                scalaResult.mOffsetHeightScala =  (MathUtils.getOffsetValue(
                        scalaFactor.mToHeightScala, scalaFactor.mFromHeightScala,
                        offsetTime, scalaFactor.mDuration/2));
            }
        } else {
            scalaResult.mOffsetWidthScala = (MathUtils.getOffsetValue(
                    scalaFactor.mFromWidthScala, scalaFactor.mToWidthScala, offsetTime, scalaFactor.mDuration));
            scalaResult.mOffsetHeightScala = (MathUtils.getOffsetValue(
                    scalaFactor.mFromHeightScala, scalaFactor.mToHeightScala, offsetTime, scalaFactor.mDuration));
        }
        scalaResult.mWidthScala = scalaFactor.mLastWidthScala + scalaResult.mOffsetWidthScala;
        scalaResult.mHeightScala = scalaFactor.mLastHeightScala + scalaResult.mOffsetHeightScala;
        return scalaResult;
    }

    private static ScaleCalculator mScalaFactor = new ScaleCalculator();
    private ScaleCalculator(){}
    public static ScaleCalculator getInstance() {
        return mScalaFactor;
    }

    @Override
    public void reset() {

    }
}