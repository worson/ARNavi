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
public interface ICarADASDataProvider {
    class AnimData{
        public AnimData(Vector3 _from, Vector3 _to, double _degrees, long _duration){
            from.setAll(_from);
            to.setAll(_to);
            degrees=_degrees;
            duration=_duration;
        }
        public Vector3 from = new Vector3();
        public Vector3 to = new Vector3();
        public double degrees;
        public long duration;
    }
    interface ICarADASNotifier {
        void onCarShow(double x,double y,double z,double direction);
        void onCarAnimationUpdate(AnimData animData);
        void onDistChange(double dist);
        void onCarHide();
        void setCarADASDataProvider(ICarADASDataProvider adasDataProvider);
        void onShowLaneYaw(List<Vector3> path,boolean left);
        void onHideLaneYaw();
    }
    void setCarADASNotifier(ICarADASNotifier carADASNotifier);


}
