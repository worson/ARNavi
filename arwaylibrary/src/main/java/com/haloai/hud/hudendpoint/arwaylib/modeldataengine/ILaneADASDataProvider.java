package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import org.rajawali3d.math.vector.Vector3;

import java.util.List;

/**
 * author       : 龙;
 * date         : 2016/11/19;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.modeldataengine;
 * project_name : hudlauncher;
 */
public interface ILaneADASDataProvider {
    interface ILaneADASNotifier {
        void setLaneADASDataProvider(ILaneADASDataProvider adasDataProvider);
        void onShowLaneADAS(List<Vector3> path,boolean isLeft);
        void onHideLaneADAS();
    }

    void setLaneADASNotifier(ILaneADASNotifier laneADASNotifier);

    void showLaneADAS(List<Vector3> path,boolean isLeft);
    void hideLaneADAS();
}
