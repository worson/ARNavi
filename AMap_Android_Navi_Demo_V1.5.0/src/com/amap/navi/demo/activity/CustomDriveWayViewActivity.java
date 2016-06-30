package com.amap.navi.demo.activity;

/**
 * 创建时间：11/10/15 16:07
 * 项目名称：newNaviDemo
 *
 * @author lingxiang.wang
 * @email lingxiang.wang@alibaba-inc.com
 * 类说明：
 */

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.DriveWayView;
import com.amap.navi.demo.R;

public class CustomDriveWayViewActivity extends BaseActivity implements AMapNaviListener {
    private DriveWayView myDriveWayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //为了能最快的看到效果
        startLatlng = new NaviLatLng(39.92458861111111, 116.43543861111111);

        setContentView(R.layout.activity_custom_drive_way_view);
        naviView = (AMapNaviView) findViewById(R.id.navi_view);
        naviView.onCreate(savedInstanceState);

        myDriveWayView = (DriveWayView) findViewById(R.id.myDriveWayView);

        //设置布局完全不可见
        AMapNaviViewOptions viewOptions = naviView.getViewOptions();
        viewOptions.setLayoutVisible(false);

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] laneInfos, byte[] laneBackgroundInfo, byte[] laneRecommendedInfo) {

        //你可以使用我们的DriveWayView来自定义位置
        myDriveWayView.loadDriveWayBitmap(laneBackgroundInfo, laneRecommendedInfo);
        myDriveWayView.invalidate();
        myDriveWayView.setVisibility(View.VISIBLE);


        //or
        //只接收数据，自行绘制属于你的道路选择view
        //下面是解释

        Log.d("解释：", "当前车道数量为" + laneInfos.length + "条");
        for (int i = 0; i < laneInfos.length; i++) {
            AMapLaneInfo info = laneInfos[i];
            Log.d("解释：", "该条车道的类型为" + info.getLaneTypeIdHexString());
//            你将收到两位字符
//            第一位表示背景
//            第二位表示当前推荐的方向（如果不推荐则为F）
//            请看drawable-hpdi，里面有一些图
//            其中，从0 - E，各自代表

//             直行0
//             左转1
//             左转，直行2
//             右转3
//             右转和直行4
//             左转调头5
//             左转和右转6
//             直行，左转，右转 7
//             右转调头8
//             直行，左转调头9
//             直行，右转调头A
//             左转和左转调头B
//             右转和右转掉头C
//             。。。

//            所以（以下三图均存在）
//            如果00，说明该车道为直行且推荐直行
//            如果0F，说明该车道为直行，但不推荐
//            如果20，说明该车道为左转直行车道，推荐直行
//            以此类推
        }


    }

    @Override
    public void hideLaneInfo() {
        myDriveWayView.setVisibility(View.INVISIBLE);
    }
}

