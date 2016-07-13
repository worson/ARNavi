package com.haloai.hud.hudendpoint.arwaylib.calculator.result;

import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.renderer.Renderer;

import java.util.List;

/**
 * Created by wangshengxing on 16/7/10.
 */
public class CameraResult extends SuperResult {
    public         Renderer          mRenderer     = null;
    public         List<Animation3D> mAnimations    = null;
    private static CameraResult      mCameraResult = new CameraResult();
    public static CameraResult getInstance() {
        return mCameraResult;
    }
    
    @Override
    public void reset() {
        super.reset();
        mAnimations = null;
        mRenderer = null;
    }
}
