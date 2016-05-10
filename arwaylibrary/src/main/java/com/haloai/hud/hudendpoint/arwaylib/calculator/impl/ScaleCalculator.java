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
            offsetTime = scalaFactor.mDuration;
        }
        if (scalaFactor.mReverse) {
            if (offsetTimeTotal <= scalaFactor.mDuration / 2) {
                /*scalaResult.mOffsetScala = scalaFactor.mFromScala + (scalaFactor.mToScala - scalaFactor.mFromScala)
                        * (offsetTime / (scalaFactor.mDuration / 2));*/
                scalaResult.mOffsetScala = (float) (scalaFactor.mFromScala + MathUtils.getOffsetValue(
                        scalaFactor.mFromScala, scalaFactor.mToScala, offsetTime, scalaFactor.mDuration/2,scalaFactor.mLastScala));
            } else {
                /*scalaResult.mOffsetScala = scalaFactor.mFromScala + (scalaFactor.mToScala - scalaFactor.mFromScala)
                        * (1 - offsetTime / (scalaFactor.mDuration / 2));*/
                scalaResult.mOffsetScala = (float) (scalaFactor.mFromScala + MathUtils.getOffsetValue(
                        scalaFactor.mToScala, scalaFactor.mFromScala, offsetTime-scalaFactor.mDuration/2, scalaFactor.mDuration/2,scalaFactor.mLastScala));
            }
        } else {
            /*scalaResult.mOffsetScala = scalaFactor.mFromScala + (scalaFactor.mToScala - scalaFactor.mFromScala)
                    * (offsetTime / scalaFactor.mDuration);*/
            scalaResult.mOffsetScala = (float)(scalaFactor.mFromScala +  MathUtils.getOffsetValue(
                    scalaFactor.mFromScala, scalaFactor.mToScala, offsetTime, scalaFactor.mDuration,scalaFactor.mLastScala));
        }
        scalaResult.mOffsetWidthScala = scalaResult.mOffsetScala;
        scalaResult.mOffsetHeightScala = scalaResult.mOffsetScala;
        scalaResult.mWidthScala += scalaResult.mOffsetScala;
        scalaResult.mHeightScala += scalaResult.mOffsetScala;
        //TODO 计算有问题
        scalaResult.mOffsetPosition.x = (int)(((scalaResult.mOffsetScala - 1) / 2 * scalaFactor.mFromPosition.x));
        scalaResult.mOffsetPosition.y = (int)(((scalaResult.mOffsetScala - 1) / 2 * scalaFactor.mFromPosition.y));
        return scalaResult;
    }

    private static ScaleCalculator mScalaFactor = new ScaleCalculator();
    private ScaleCalculator(){}
    public static ScaleCalculator getInstance() {
        return mScalaFactor;
    }
}