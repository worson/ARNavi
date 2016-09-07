package com.haloai.hud.hudendpoint.arwaylib.arway;

import android.graphics.Bitmap;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.NaviInfo;
import com.haloai.hud.navigation.NavigationSDKAdapter;

/**
 * Created by wangshengxing on 16/8/31.
 */
public interface INaviUpdater {
    public void onNaviStarted();
    public void onArriveDestination();
    public void updateYawStart();
    public void updateYawEnd();
    public void onNaviCalculateRouteFailure(int errorInfo);
    public void onGpsStatusChanged(boolean work);
    public void onNetworkStatusChanged(boolean work);
    public void showCrossImage(Bitmap crossimage);
    public void hideCrossImage();

    public void updateLocation(AMapNaviLocation location);
    public void updateNaviText(NavigationSDKAdapter.NavigationNotifier.NaviTextType textType, String text);

    public void updatePath(AMapNavi aMapNavi);
    public void updateNaviInfo(NaviInfo info);
    public void onSpeedUpgraded(float speed);
}
