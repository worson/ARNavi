package com.amap.navi.demo.activity;


import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.amap.api.navi.AMapNavi;
import com.amap.navi.demo.R;

/**
 * 首页面
 */
public class IndexActivity extends Activity implements TextWatcher {


	public static int route_length = 0;
    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                startActivity(new Intent(IndexActivity.this, BasicNaviActivity.class));
            } else if (position == 1) {
                startActivity(new Intent(IndexActivity.this, CustomRouteActivity.class));
            } else if (position == 2) {
                startActivity(new Intent(IndexActivity.this, CustomEnlargedCrossDisplayActivity.class));
            } else if (position == 3) {
                startActivity(new Intent(IndexActivity.this, CustomDriveWayViewActivity.class));
            } else if (position == 4) {
                startActivity(new Intent(IndexActivity.this, HudDisplayActivity.class));
            } else if (position == 5) {
                startActivity(new Intent(IndexActivity.this, IntelligentBroadcastActivity.class));
            } else if (position == 6) {
                startActivity(new Intent(IndexActivity.this, RoutePlanningActivity.class));
            } else if (position == 7) {
                startActivity(new Intent(IndexActivity.this, GPSNaviActivity.class));
            } else if (position == 8) {
                startActivity(new Intent(IndexActivity.this, CustomTrafficBarActivity.class));
            }
        }
    };
    private String[] examples = new String[]

            {
                    "基本导航页", "自定义路段", "自定义路口放大图", "自定义道路选择", "HUD显示", "智能播报", "路径规划", "实时导航", "自定义路况蚯蚓线"
            };
	private EditText mEt;
	private EditText mEt2;
	public static float mDraw_scale = 1f;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        File file1 = new File("/sdcard/testimage");
        if(!file1.exists()){
        	file1.mkdir();
        }
        File file2 = new File("/sdcard/testimage/crossimages");
        if(!file2.exists()){
        	file2.mkdir();
        }
        File file3 = new File("/sdcard/testimage/selfimages");
        if(!file3.exists()){
        	file3.mkdir();
        }
        
        mEt = (EditText) findViewById(R.id.et);
        mEt2 = (EditText) findViewById(R.id.et2);
        mEt.addTextChangedListener(this);
        mEt2.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				try{
					mDraw_scale = Float.parseFloat(s.toString());
					}catch(Exception e){
						mDraw_scale = 1.0f;
					}
			}
		});
        

        initView();
    }

    private void initView() {
        ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, examples));
        setTitle("导航SDK " + AMapNavi.getVersion());
        listView.setOnItemClickListener(mItemClickListener);
    }


    /**
     * 返回键处理事件
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            System.exit(0);// 退出程序
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		
	}

	@Override
	public void afterTextChanged(Editable s) {
		try{
			route_length = Integer.parseInt(s.toString());
		}catch(Exception e){
			route_length = 500;
		}
	}

}
