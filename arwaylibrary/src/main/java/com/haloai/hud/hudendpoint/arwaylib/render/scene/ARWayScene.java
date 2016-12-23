package com.haloai.hud.hudendpoint.arwaylib.render.scene;

import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.scene.Scene;
import org.rajawali3d.scenegraph.IGraphNode;

/**
 * author       : wangshengxing;
 * date         : 22/12/2016;
 * email        : wangshengxing@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.render.scene;
 * project_name : TestARWay;
 */
public class ARWayScene extends Scene{
    public static final String TAG = ARWayScene.class.getSimpleName();
    public ARWayScene(Renderer renderer) {
        super(renderer);
    }

    public ARWayScene(Renderer renderer, IGraphNode.GRAPH_TYPE type) {
        super(renderer, type);
    }

    @Override
    public void performFrameTasks() {
        long starttime = System.currentTimeMillis();
        super.performFrameTasks();
        HaloLogger.logE(TAG,"performFrameTasks time "+(System.currentTimeMillis()-starttime));
    }
}
