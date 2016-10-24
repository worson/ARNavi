package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import org.rajawali3d.math.vector.Vector3;

import java.util.List;

/**
 * author       : 龙;
 * date         : 2016/10/23;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.modeldataengine;
 * project_name : hudlauncher;
 */
public interface INaviPathDataProvider{
    class AnimData{
        public AnimData(Vector3 _from,Vector3 _to,double _degrees,long _duration){
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
    interface INaviPathDataChangeNotifer{
        void onPathUpdate();
        void onAnimUpdate(AnimData animData);
    }
    void setNaviPathChangeNotifier(INaviPathDataChangeNotifer naviPathChangeNotifier);

    void reset();
    void setAnim(Vector3 start,Vector3 end,double degrees,long duration);
    void setPath(List<Vector3> renderPath);
    void setObjStartOrientation(double rotateZ);

    List<Vector3> getNaviPathByLevel(int level);
    double getObjStartOrientation();
}
