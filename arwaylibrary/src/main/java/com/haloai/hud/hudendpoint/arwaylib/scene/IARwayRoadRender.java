package com.haloai.hud.hudendpoint.arwaylib.scene;

import org.rajawali3d.math.vector.Vector3;

import java.util.List;

/**
 * Created by wangshengxing on 16/9/23.
 */
public interface IARwayRoadRender {
    //渲染当前显示的道路
    public boolean renderNaviPath(List<Vector3> path);
    //渲染蚯蚓线
    public boolean renderIndicationLine(List<Vector3> path);
    public boolean renderCrossRoad(List<List<Vector3>> cross); //cross数据结构未定
    public boolean clearRoadnetwork();
    public boolean clearNaviRoad();

}
