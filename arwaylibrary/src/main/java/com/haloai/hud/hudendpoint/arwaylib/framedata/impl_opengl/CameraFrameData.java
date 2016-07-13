package com.haloai.hud.hudendpoint.arwaylib.framedata.impl_opengl;

import com.haloai.hud.hudendpoint.arwaylib.calculator.result.CameraResult;
import com.haloai.hud.hudendpoint.arwaylib.framedata.SuperFrameData;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.renderer.Renderer;

/**
 * Created by wangshengxing on 16/7/10.
 */
public class CameraFrameData extends SuperFrameData <CameraResult> {
    private static CameraFrameData mCameraFrameData = new CameraFrameData();

    public static CameraFrameData getInstance() {
        return mCameraFrameData;
    }

    @Override
    public void animOver() {

    }

    @Override
    public void update(CameraResult cameraResult) {
        HaloLogger.logE("sen_debug_gl", "camera update called");
        if (cameraResult == null || cameraResult.mRenderer == null) {
            return;
        }
        Renderer renderer = cameraResult.mRenderer;
        if (cameraResult.mAnimations != null) {
            for (Animation3D animation:cameraResult.mAnimations) {
                if (animation != null) {
                    renderer.getCurrentScene().registerAnimation(animation);
                    animation.setTransformable3D(renderer.getCurrentCamera());
                    animation.play();
                    HaloLogger.logE("sen_debug_gl", "camera update anim called");
                }
            }
        }
    }


}
