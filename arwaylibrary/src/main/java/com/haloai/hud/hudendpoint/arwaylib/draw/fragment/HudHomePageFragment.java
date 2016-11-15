package com.haloai.hud.hudendpoint.arwaylib.draw.fragment;


import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.haloai.hud.hudendpoint.arwaylib.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class HudHomePageFragment extends Fragment {
    private Context mContext;

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
    private IntentFilter mTimeFilter        = null;

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

        initTimer(mContext);
        return mMainLayout;
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
}
