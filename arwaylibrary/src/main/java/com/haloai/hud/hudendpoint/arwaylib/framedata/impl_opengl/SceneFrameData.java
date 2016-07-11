package com.haloai.hud.hudendpoint.arwaylib.framedata.impl_opengl;

import com.haloai.hud.hudendpoint.arwaylib.calculator.result.SceneResult;
import com.haloai.hud.hudendpoint.arwaylib.framedata.SuperFrameData;

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

    }
}
