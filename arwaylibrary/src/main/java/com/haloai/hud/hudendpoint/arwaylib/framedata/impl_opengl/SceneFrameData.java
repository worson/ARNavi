package com.haloai.hud.hudendpoint.arwaylib.framedata.impl_opengl;

import com.haloai.hud.hudendpoint.arwaylib.calculator.result.SceneResult;
import com.haloai.hud.hudendpoint.arwaylib.framedata.SuperFrameData;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.scene.Scene;

/**
 * Created by wangshengxing on 16/7/10.
 */
public class SceneFrameData extends SuperFrameData <SceneResult>{



    private static SceneFrameData mSceneFrameData = new SceneFrameData();
    
    public static SceneFrameData getInstance() {
        return mSceneFrameData;
    }
    
    @Override
    public void animOver() {

    }

    @Override
    public void update(SceneResult result){
        if(result.mRenderer==null){
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG,"SceneFrameData update  mRenderer is null");
        }
        Scene scene = result.mRenderer.getCurrentScene();
        Camera camera = result.mRenderer.getCurrentCamera();
        if (result == null|| scene == null||camera == null) {
            return;
        }

    }


}
