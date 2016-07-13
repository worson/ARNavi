package com.haloai.hud.hudendpoint.arwaylib.calculator.factor;

import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.Renderer;

import java.util.List;

/**
 * Created by wangshengxing on 16/7/10.
 */
public class SceneFactor extends SuperFactor {

    public Renderer mRenderer = null;
    public List<Vector3> mPath;
    public int mAllLength = 0;

    private static SceneFactor mSceneFactor = new SceneFactor();

    public static SceneFactor getInstance() {
        return mSceneFactor;
    }


    public void init(Renderer renderer,List<Vector3> path, int allLength) {
        mRenderer = renderer;
        mPath = path;
        mAllLength = allLength;
    }

}
