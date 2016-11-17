package com.haloai.hud.hudendpoint.arwaylib.draw.fragment;


import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.draw.view.SpeedPanelView;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class HudHomePageFragment extends Fragment implements SensorEventListener {
    private Context mContext;

    private ViewGroup      mNaviPanelViewgroup;
    private ViewGroup      mCompassViewgroup;
    private TextView       mDirectionTextview;
    private TextView       mSpeedTextview;
    private SpeedPanelView mSpeedView;
    private ImageView mDirectionImageview;

    private ImageView mSystemTimeHourTenImageview;
    private ImageView mSystemTimeHourOneImageview;
    private ImageView mSystemTimeMinuteTenImageview;
    private ImageView mSystemTimeMinuteOneImageview;

    private int[]        mSpeedNumImg       = {R.drawable.smooth_number_0, R.drawable.smooth_number_1, R.drawable.smooth_number_2,
            R.drawable.smooth_number_3, R.drawable.smooth_number_4, R.drawable.smooth_number_5, R.drawable.smooth_number_6,
            R.drawable.smooth_number_7, R.drawable.smooth_number_8, R.drawable.smooth_number_9};
    private boolean      mTimeDisplayEnbale = false;
    private Time         mSystemTimer       = null;
    private TimeReceiver mTimeReceiver      = null;


    private IntentFilter mTimeFilter      = null;
    private float        mTargetDirection = 0;
    private List<ObjectAnimator> mAnimators = new ArrayList<>();

    class TimeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                refreshDisplayTime();
            }
        }
    }

    public HudHomePageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        // Inflate the layout for this fragment
        View mMainLayout = inflater.inflate(R.layout.fragment_hud_home_page, container, false);

        mSystemTimeHourTenImageview = (ImageView) mMainLayout.findViewById(R.id.hour_ten_imageview);
        mSystemTimeHourOneImageview = (ImageView) mMainLayout.findViewById(R.id.hour_one_imageview);
        mSystemTimeMinuteTenImageview = (ImageView) mMainLayout.findViewById(R.id.minute_ten_imageview);
        mSystemTimeMinuteOneImageview = (ImageView) mMainLayout.findViewById(R.id.minute_one_imageview);


        mNaviPanelViewgroup = (ViewGroup) mMainLayout.findViewById(R.id.navi_panel_viewgroup);
        mCompassViewgroup = (ViewGroup) mMainLayout.findViewById(R.id.compass_viewgroup);
        mDirectionImageview = (ImageView) mMainLayout.findViewById(R.id.compass_direction_imageview);
        mDirectionImageview.setAlpha(0.5f);
        mSpeedView = (SpeedPanelView) mMainLayout.findViewById(R.id.navi_panel_view);
        mDirectionTextview = (TextView) mMainLayout.findViewById(R.id.compass_textview);
        mSpeedTextview = (TextView) mMainLayout.findViewById(R.id.speed_panel_textview);

        initTimer(mContext);
        initCompassSensor(mContext);
        prepareSpeedPanelAnim();

        updatePanelSpeed(120);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideSpeedPanelAnim();
            }
        },3000);
        return mMainLayout;
    }

    Handler mHandler = new Handler();

    public void prepareSpeedPanelAnim() {
        mAnimators.clear();

        ObjectAnimator anim;
        anim = ObjectAnimator.ofFloat(mNaviPanelViewgroup, "ScaleX",1,0.1f);
        anim.setInterpolator(new LinearInterpolator());
        mAnimators.add(anim);

        anim = ObjectAnimator.ofFloat(mNaviPanelViewgroup, "ScaleY",1,0.1f);
        anim.setInterpolator(new LinearInterpolator());
        mAnimators.add(anim);

        anim = ObjectAnimator.ofFloat(mCompassViewgroup, "alpha",1,0.1f);
        anim.setInterpolator(new LinearInterpolator());
        mAnimators.add(anim);




    }

    public void hideSpeedPanelAnim() {
        int duration = 1000;
        for (ObjectAnimator a: mAnimators){
            if(a.isStarted()){
                a.cancel();
            }
            a.setDuration(duration);
            a.start();
        }
    }

    public void showNaviPanel(){
        mNaviPanelViewgroup.setScaleX(1);
        mNaviPanelViewgroup.setScaleY(1);
        mCompassViewgroup.setAlpha(1);
    }


    public void initTimer(Context context) {
        mContext = context;
        initTimeTick();
        refreshDisplayTime();

    }

    private void initTimeTick() {
        mSystemTimer = new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料。
        mTimeReceiver = new TimeReceiver();
        mTimeFilter = new IntentFilter();
        mTimeFilter.addAction(Intent.ACTION_TIME_TICK);
        mContext.registerReceiver(mTimeReceiver, mTimeFilter);
        mTimeDisplayEnbale = true;
    }

    public void updatePanelSpeed(int speed) {
        if (mSpeedView != null) {
            mSpeedView.setSpeed(speed);
        }
        if (mSpeedTextview != null) {
            mSpeedTextview.setText("" + speed);
        }

    }

    private void refreshDisplayTime() {
        if (!mTimeDisplayEnbale) {
            return;
        }
        mSystemTimer.setToNow(); // 取得系统时间。
        int hour = mSystemTimer.hour;
        int minute = mSystemTimer.minute;
        mSystemTimeHourTenImageview.setImageResource(mSpeedNumImg[(hour / 10) % 10]);
        mSystemTimeHourOneImageview.setImageResource(mSpeedNumImg[(hour) % 10]);
        mSystemTimeMinuteTenImageview.setImageResource(mSpeedNumImg[(minute / 10) % 10]);
        mSystemTimeMinuteOneImageview.setImageResource(mSpeedNumImg[(minute) % 10]);
    }

    public void updatePanelDirection() {
        float direction = normalizeDegree(mTargetDirection * -1.0f);
        String text = "";
        if (direction > 22.5f && direction < 157.5f) {
            // east
            text = "东";

        } else if (direction > 202.5f && direction < 337.5f) {
            // west
            text = "西";
        }

        if (direction > 112.5f && direction < 247.5f) {
            // south
            text = "南";
        } else if (direction < 67.5 || direction > 292.5f) {
            // north
            text = "北";
        }
        // TODO: 16/11/2016 判断方向
        text = "北";
        if (mDirectionTextview != null) {
            mDirectionTextview.setText(text);
        }
    }

    private void initCompassSensor(Context context) {
        // 传感器管理器
        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        // 注册传感器(Sensor.TYPE_ORIENTATION(方向传感器);SENSOR_DELAY_FASTEST(0毫秒延迟);
        // SENSOR_DELAY_GAME(20,000毫秒延迟)、SENSOR_DELAY_UI(60,000毫秒延迟))
        // 如果不采用SENSOR_DELAY_FASTEST的话,在0度和360左右之间做动画会有反向转一大圈的感觉
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            float direction = event.values[0] * -1.0f;
            Log.e("compass", "direction is " + direction);
            mTargetDirection = normalizeDegree(direction);
            updatePanelDirection();
        }
    }

    private float normalizeDegree(float degree) {
        return (degree + 720) % 360;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}
