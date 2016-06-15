package com.amap.navi.demo.activity;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviException;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.navi.demo.R;

import java.util.ArrayList;

/**
 * 创建时间：11/10/15 17:08
 * 项目名称：newNaviDemo
 *
 * @author lingxiang.wang
 * @email lingxiang.wang@alibaba-inc.com
 * 类说明：
 */

public class CustomRouteActivity extends BaseActivity implements AMapNaviListener {

    NaviLatLng wayPoint = new NaviLatLng(39.935041, 116.447901);
    NaviLatLng wayPoint1 = new NaviLatLng(39.945041, 116.447901);
    NaviLatLng wayPoint2 = new NaviLatLng(39.955041, 116.447901);
    NaviLatLng wayPoint3 = new NaviLatLng(39.965041, 116.447901);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        wayPointList = new ArrayList<NaviLatLng>();

        setContentView(R.layout.activity_basic_navi);
        naviView = (AMapNaviView) findViewById(R.id.navi_view);
        //关闭自动绘制路线（如果你想自行绘制路线的话，必须关闭！！！）
        //PS:必须在onCreate之前
//        naviView.getViewOptions().setAutoDrawRoute(false);
        naviView.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        wayPointList.add(wayPoint);
        wayPointList.add(wayPoint1);
        wayPointList.add(wayPoint2);
        wayPointList.add(wayPoint3);

        super.onResume();
    }


    @Override
    public void onCalculateRouteSuccess() {
//        如果根据获取的导航路线来自定义绘制
        RouteOverLay routeOverlay = new RouteOverLay(naviView.getMap(), aMapNavi.getNaviPath(), this);
        routeOverlay.setStartPointBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.r1));
        routeOverlay.setEndPointBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.b1));
        routeOverlay.setWayPointBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.b2));
        try {
			routeOverlay.setWidth(30);
		} catch (AMapNaviException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        int color[] = new int[10];
        color[0] = Color.BLACK;
        color[1] = Color.RED;
        color[2] = Color.BLUE;
        color[3] = Color.YELLOW;
        color[4] = Color.GRAY;
        routeOverlay.addToMap(color, aMapNavi.getNaviPath().getWayPointIndex());


        aMapNavi.startNavi(AMapNavi.EmulatorNaviMode);
    }

   

}
