package com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl;

import android.content.Context;
import android.graphics.Canvas;

import com.haloai.hud.hudendpoint.arwaylib.calculator.CalculatorFactory;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.CameraFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.CameraCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.CameraResult;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;
import com.haloai.hud.hudendpoint.arwaylib.framedata.FrameDataFactory;
import com.haloai.hud.hudendpoint.arwaylib.framedata.SuperFrameData;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.RouteFrameData;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl_opengl.CameraFrameData;

/**
 * Created by wangshengxing on 16/7/10.
 */
public class DrawCamera extends DrawObject{
    private static DrawCamera mDrawCamera = new DrawCamera();

    private CameraCalculator mCameraCalculator = (CameraCalculator) CalculatorFactory.getCalculator(CalculatorFactory.CalculatorType.GL_CAMERA);

    public DrawCamera() {
    }

    public static DrawCamera getInstance() {
        return mDrawCamera;
    }
    @Override
    public void doDraw(Context context) {
        SuperFrameData frameData = null;
        frameData = CameraFrameData.getInstance();
        CameraFrameData cameraFrameData = (CameraFrameData) frameData;
        CameraFactor cameraFactor = CameraFactor.getInstance();
        cameraFactor.init();
        CameraResult cameraResult = mCameraCalculator.calculate(cameraFactor);
        cameraFrameData.update(cameraResult);
    }
}
