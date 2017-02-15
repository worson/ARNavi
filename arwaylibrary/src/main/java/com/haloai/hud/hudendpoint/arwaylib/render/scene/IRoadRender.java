package com.haloai.hud.hudendpoint.arwaylib.render.scene;

import org.rajawali3d.math.vector.Vector3;

import java.util.List;

/**
 * Created by wangshengxing on 16/9/23.
 */
public interface IRoadRender {
    /**
     * 渲染当前导航道路,需要提交才能显示到场景中
     * @param path
     * @return
     */
    public boolean renderNaviPath(List<Vector3> path);
    public int removeNaviPath();

    //渲染蚯蚓线
    public boolean renderGuideLine(List<Vector3> path);

    /**
     * 渲染路网，需要提交才能显示到场景中
     * @param cross
     * @return
     */
    public boolean renderRoadNet(List<List<Vector3>> cross); //cross数据结构未定
    public int removeRoadNet();

    public void renderFloor(float left,float top,float right,float bottom,float spacing,float widthrate);
    public void moveCenterFloor(float x,float y);
    public int removeFloor();

    public void renderTrafficLight(List<Vector3> lights);

    public void renderEndScene(List<Vector3> path);

    /**
     * 提交渲染任务
     */
    public void commitRender();

}
