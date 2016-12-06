package com.haloai.hud.hudendpoint.arwaylib.arway;

import android.graphics.Bitmap;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.NaviInfo;
import com.haloai.hud.navigation.NavigationSDKAdapter;

/**
 * Created by wangshengxing on 16/8/31.
 */
public interface INaviUpdater {
    void onNaviStarted();
    void onArriveDestination();
    void updateYawStart();
    void updateYawEnd();
    void onNaviCalculateRouteFailure(int errorInfo);
    void onGpsStatusChanged(boolean work);
    void onNetworkStatusChanged(boolean work);
    void showCrossImage(Bitmap crossimage);
    void hideCrossImage();

    void updateLocation(AMapNaviLocation location);
    void updateNaviText(NavigationSDKAdapter.NavigationNotifier.NaviTextType textType, String text);

    void updatePath(AMapNavi aMapNavi);
    void updateNaviInfo(NaviInfo info);
    void onSpeedUpgraded(float speed);

    public void showLaneInfo(AMapLaneInfo[] laneInfos, byte[] laneBackgroundInfo, byte[] laneRecommendedInfo);
    public void hideLaneInfo();
}
