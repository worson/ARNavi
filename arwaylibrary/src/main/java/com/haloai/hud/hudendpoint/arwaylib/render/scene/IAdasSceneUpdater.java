package com.haloai.hud.hudendpoint.arwaylib.render.scene;

import org.rajawali3d.math.vector.Vector3;

import java.util.List;

/**
 * Created by wangshengxing on 19/11/2016.
 */

public interface IAdasSceneUpdater {
    /**
     * 行人检测
     * @param path
     * @param left true 左侧偏移 false 右侧偏移
     */
    void showLaneYawLine(List<Vector3> path,boolean left);//显示车道偏移点
    void hideLaneYawLine();

    /**
     * 前车检测
     * @param distance 与前车的距离，米
     * @param direction 当前行车的方向弧度(3点钟方向为起点，逆时针方向)
     */
    void updateTrafficDetection(Vector3 position,double distance,double direction);
    void hideTrafficDetection();

}
