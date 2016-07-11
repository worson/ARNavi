package com.haloai.hud.hudendpoint.arwaylib.framedata.impl_opengl;

import com.haloai.hud.hudendpoint.arwaylib.calculator.result.CameraResult;
import com.haloai.hud.hudendpoint.arwaylib.framedata.SuperFrameData;

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

    }
}
