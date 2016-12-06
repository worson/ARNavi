package com.haloai.hud.hudendpoint.arwaylib.modeldataengine;

import java.util.List;

/**
 * author       : 龙;
 * date         : 2016/10/23;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.modeldataengine;
 * project_name : hudlauncher;
 */
public interface IRoadNetDataProvider {
    class LatLng_RoadNet{
        public LatLng_RoadNet(){}
        public LatLng_RoadNet(double _lat,double _lng){
            lat=_lat;
            lng=_lng;
        }
        double lat;
        double lng;
    }
    class NaviInfo_RoadNet{
        //something about navi info
    }
    class Size2i_RoadNet{
        public Size2i_RoadNet(){}
        public Size2i_RoadNet(int _width,int _height){
            width = _width;
            height = _height;
        }
        int width;
        int height;
    }
    interface IRoadNetDataNotifier{
        void onRoadNetDataChange();
    }
    void setRoadNetChangeNotifier(IRoadNetDataNotifier roadNetChangeNotifier);

    void reset();
    void processStep(List<List<LatLng_RoadNet>> naviLinks,
                     List<NaviInfo_RoadNet> linkInfos,
                     LatLng_RoadNet centerLatLng,
                     Size2i_RoadNet szCover,
                     String sourceFilePath);
    List<List<LatLng_RoadNet>> getRoadNet();
}
