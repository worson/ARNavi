package com.haloai.hud.hudendpoint.arwaylib.render.scene;

import android.graphics.RectF;

import org.rajawali3d.math.vector.Vector3;

import java.util.List;

/**
 * Created by wangshengxing on 16/9/23.
 */
public interface IARwayRoadRender {
    //渲染当前显示的道路
    public boolean renderNaviPath(List<Vector3> path);

    //渲染蚯蚓线
    public boolean renderDirectorLine(List<Vector3> path);

    public boolean renderRoadNet(List<List<Vector3>> cross); //cross数据结构未定


    public void renderFloor(float left,float top,float right,float bottom,float spacing);

    public void applyRender();


}
