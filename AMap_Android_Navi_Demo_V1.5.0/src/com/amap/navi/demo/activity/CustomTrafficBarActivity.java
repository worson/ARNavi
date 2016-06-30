package com.amap.navi.demo.activity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;

import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapTrafficStatus;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.view.CustomTmcView;
import com.amap.navi.demo.R;

import java.util.List;

/**
 * 创建时间：11/13/15 16:32
 * 项目名称：newNaviDemo
 *
 * @author lingxiang.wang
 * @email lingxiang.wang@alibaba-inc.com
 * 类说明：
 */

public class CustomTrafficBarActivity extends BaseActivity {

    private CustomTmcView myCustomTrafficBar;
    private AMapNaviPath naviPath;
    private NaviInfo lastNaviInfo;
    private int remainingDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_traffic_bar);


        naviView = (AMapNaviView) findViewById(R.id.navi_view);
        //设置布局完全不可见
        AMapNaviViewOptions viewOptions = naviView.getViewOptions();
        viewOptions.setLayoutVisible(false);

        //主动隐藏蚯蚓线
        viewOptions.setTrafficBarEnabled(false);


        //可以自由设置自车位置，第一个参数为在宽的百分之多少处
        //第二个参数为在高的百分之多少处
        viewOptions.setPointToCenter(1.0 / 2, 1.0 / 2);

        //请保证onCreate方法最后调用
        naviView.onCreate(savedInstanceState);
        naviView.setAMapNaviViewListener(this);


        myCustomTrafficBar = (CustomTmcView) findViewById(R.id.myTrafficBar);

        
        //设置原始高度的百分比，其中若值<0.1 则用0.1 若值>1 则用1
        
        //设置横屏时高度为原始图片的0.5
        myCustomTrafficBar.setTmcBarHeightWhenLandscape(0.5);

        //设置竖屏时高度和原始图片一致
        myCustomTrafficBar.setTmcBarHeightWhenPortrait(1);

    }


    @Override
    public void onNaviInfoUpdate(NaviInfo naviinfo) {
        super.onNaviInfoUpdate(naviinfo);
        lastNaviInfo = naviinfo;

        //每次NaviInfo更新的时候 准确的获取接下来的路长，以及接下来的路况
        remainingDistance = lastNaviInfo.getPathRetainDistance();
    }

    @Override
    public void onCalculateRouteSuccess() {
        super.onCalculateRouteSuccess();

        if (aMapNavi != null) {
            naviPath = aMapNavi.getNaviPath();
        }

        int end = naviPath.getAllLength();
        int start = 0;
        //第一次算路成功进行整个路况的绘制
        //首先获取整个路况信息
        List<AMapTrafficStatus> totalRoadCondition = aMapNavi.getTrafficStatuses(start, end);

        //将路况和路长两项传入进行绘制
        myCustomTrafficBar.update(totalRoadCondition, end);
        myCustomTrafficBar.invalidate();
    }

    @Override
    public void onTrafficStatusUpdate() {
        //路况变化的时候重新绘制余下路况的蚯蚓线

        int end = naviPath.getAllLength();
        int start = end - remainingDistance;

        List<AMapTrafficStatus> remainingRoadCondition = aMapNavi.getTrafficStatuses(start, end);
        myCustomTrafficBar.update(remainingRoadCondition, naviPath.getAllLength());
        myCustomTrafficBar.invalidate();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);


        boolean isLandscape = isOrientationLandscape();
        myCustomTrafficBar.onConfigurationChanged(isLandscape);
        myCustomTrafficBar.invalidate();
    }

    private boolean isOrientationLandscape() {
        return this.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }


}
