package com.amap.navi.demo.activity;

import android.os.Bundle;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviView;
import com.amap.navi.demo.R;

/**
 * 创建时间：11/11/15 18:50
 * 项目名称：newNaviDemo
 *
 * @author lingxiang.wang
 * @email lingxiang.wang@alibaba-inc.com
 * 类说明：
 */

public class GPSNaviActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_basic_navi);
        naviView = (AMapNaviView) findViewById(R.id.navi_view);
        naviView.onCreate(savedInstanceState);
        naviView.setAMapNaviViewListener(this);
    }


    @Override
    public void onCalculateRouteSuccess() {
        aMapNavi.startNavi(AMapNavi.GPSNaviMode);
    }
}
