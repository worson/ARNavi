package com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl;

import android.content.Context;

import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.NaviInfoBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl_opengl.OpenglRouteBean;
import com.haloai.hud.hudendpoint.arwaylib.calculator.CalculatorFactory;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.CameraFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.CameraCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.SceneCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.CameraResult;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.SceneResult;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;
import com.haloai.hud.hudendpoint.arwaylib.draw.IOpenglFrame;
import com.haloai.hud.hudendpoint.arwaylib.framedata.SuperFrameData;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl_opengl.CameraFrameData;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.renderer.Renderer;

/**
 * Created by wangshengxing on 16/7/10.
 */
public class DrawCamera extends DrawObject implements IOpenglFrame {
    private static DrawCamera mDrawCamera = new DrawCamera();

    private CameraCalculator mCameraCalculator = (CameraCalculator) CalculatorFactory.getCalculator(CalculatorFactory.CalculatorType.GL_CAMERA);

    public DrawCamera() {
    }

    public static DrawCamera getInstance() {
        return mDrawCamera;
    }
    @Override
    public void doDraw(Context context, Renderer renderer) {
        SuperFrameData frameData = null;
        frameData = CameraFrameData.getInstance();
        CameraFrameData cameraFrameData = (CameraFrameData) frameData;
        CameraFactor cameraFactor = CameraFactor.getInstance();

        NaviInfoBean naviInfoBean = (NaviInfoBean)BeanFactory.getBean(BeanFactory.BeanType.NAVI_INFO);
        /*OpenglRouteBean openglRouteBean = (OpenglRouteBean)BeanFactory.getBean(BeanFactory.BeanType.GL_ROUTE);
        SceneCalculator sceneCalculator = SceneCalculator.getInstance();*/
        SceneResult sceneResult = SceneResult.getInstance();
        if (sceneResult.mCalculatePath != null) {//有进行计算优化坐标点
            cameraFactor.init(renderer,sceneResult.mCalculatePath,naviInfoBean.getPathRetainDistance(),sceneResult.mLength2FinalPoint);
            CameraResult cameraResult = mCameraCalculator.calculate(cameraFactor);
            cameraFrameData.update(cameraResult);
        }else {
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "DrawCamera doDraw error");
        }

    }

    @Override
    public void onOpenglFrame(Renderer renderer) {

    }
}
