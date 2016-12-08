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
import com.haloai.hud.hudendpoint.arwaylib.draw.view.CompassManager;
import com.haloai.hud.hudendpoint.arwaylib.draw.view.SpeedPanelView;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class HudHomePageFragment extends Fragment{
    public static final String TAG = HudHomePageFragment.class.getSimpleName();
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

    private CompassManager mCompassManager;
    private String mOrientaionText[] = new String[]{"北","东北","东","东南","南","西南","西","西北"};


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

    private CompassManager.CompassLister mCompassLister = new CompassManager.CompassLister() {
        @Override
        public void onOrientationChange(float orientation) {
            Log.e(TAG, "onOrientationChange: orientation "+ orientation);
            if (mDirectionTextview != null) {
                mDirectionTextview.setText(mOrientaionText[((int) (orientation+22.5f)%360)/45]);
            }

        }
    };

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
        mCompassManager = CompassManager.getInstance();
        mCompassManager.init(context);
        mCompassManager.setRotation(270);
        mCompassManager.addCompassLister(mCompassLister);

        
    }
    private float normalizeDegree(float degree) {
        return (degree + 720) % 360;
    }

}
