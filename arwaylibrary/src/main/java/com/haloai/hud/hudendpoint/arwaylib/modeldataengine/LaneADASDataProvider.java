package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import org.rajawali3d.math.vector.Vector3;

import java.util.List;

import static android.R.attr.path;

/**
 * author       : 龙;
 * date         : 2016/11/19;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.modeldataengine;
 * project_name : hudlauncher;
 */
public class LaneADASDataProvider implements ILaneADASDataProvider {
    private static ILaneADASNotifier laneADASNotifier;

    @Override
    public void setLaneADASNotifier(ILaneADASNotifier laneADASNotifier) {
        this.laneADASNotifier = laneADASNotifier;
    }

    @Override
    public void showLaneADAS(List<Vector3> path, boolean isLeft) {
        if(laneADASNotifier!=null){
            laneADASNotifier.onShowLaneADAS(path,isLeft);
        }
    }

    @Override
    public void hideLaneADAS() {
        if(laneADASNotifier!=null){
            laneADASNotifier.onHideLaneADAS();
        }
    }
}
