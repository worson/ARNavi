package com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl;

import android.content.Context;
import android.graphics.Canvas;

import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.calculator.CalculatorFactory;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.SceneFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.CameraCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.SceneCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.SceneResult;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;
import com.haloai.hud.hudendpoint.arwaylib.framedata.FrameDataFactory;
import com.haloai.hud.hudendpoint.arwaylib.framedata.SuperFrameData;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.RouteFrameData;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl_opengl.SceneFrameData;

/**
 * Created by wangshengxing on 16/7/10.
 */
public class DrawScene extends DrawObject {


    private SceneCalculator  mSceneCalculator  = (SceneCalculator) CalculatorFactory.getCalculator(CalculatorFactory.CalculatorType.GL_SCENE);
    private static DrawScene        mDrawScene        = new DrawScene();

    public DrawScene() {
    }

    public static DrawScene getInstance() {
        return mDrawScene;
    }
    @Override
    public void doDraw(Context context) {
        SuperFrameData frameData = null;
        frameData = SceneFrameData.getInstance();
        SceneFrameData sceneFrameData = (SceneFrameData) frameData;
        SceneFactor sceneFactor = SceneFactor.getInstance();
        sceneFactor.init();
        SceneResult sceneResult = mSceneCalculator.calculate(sceneFactor);
        sceneFrameData.update(sceneResult);
    }
}
