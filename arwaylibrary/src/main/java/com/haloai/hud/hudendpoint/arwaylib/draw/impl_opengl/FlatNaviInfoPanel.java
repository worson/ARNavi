package com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.view.DriveWayView;
import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.CommonBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.NaviInfoBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.RouteBean;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;
import com.haloai.hud.hudendpoint.arwaylib.draw.IViewOperation;
import com.haloai.hud.hudendpoint.arwaylib.draw.view.SpeedPanelView;
import com.haloai.hud.hudendpoint.arwaylib.modeldataengine.IWalkerADASDataProvider;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.hudendpoint.arwaylib.utils.DisplayUtil;
import com.haloai.hud.utils.HaloLogger;
// TODO: 22/11/2016
//import com.amap.api.col.dy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangshengxing on 16/7/15.
 */
public class FlatNaviInfoPanel extends DrawObject implements IViewOperation ,SensorEventListener, IWalkerADASDataProvider.IWalkerADASNotifier {
    private static final boolean GPS_DEBUG_MODE = true;
    private static final int SHOW_NAVI_INFO_ID  = 1;

    private static FlatNaviInfoPanel mGlDrawNaviInfo = new FlatNaviInfoPanel();

    private float                mTargetDirection         = 0;
    private List<ObjectAnimator> mSpeedPanelHideAnimators = new ArrayList<>();

    //bean
    private static NaviInfoBean mNaviInfoBean = (NaviInfoBean) BeanFactory.getBean(BeanFactory.BeanType.NAVI_INFO);
    private static RouteBean    mRouteBean    = (RouteBean) BeanFactory.getBean(BeanFactory.BeanType.ROUTE);
    private static CommonBean   mCommonBean   = (CommonBean) BeanFactory.getBean(BeanFactory.BeanType.COMMON);

    private        ViewGroup               mRoadMaskViewgroup = null;
    private        ImageView               mRoadMaskViewLeft  = null;
    private        ImageView               mRoadMaskViewRight = null;
    private ObjectAnimator                 mRoadMaskAnimator  = null;

    private ViewGroup      mNaviInfoPanelViewgroup;
    private ViewGroup      mNaviPanelViewgroup;
    private ViewGroup      mCompassViewgroup;
    private TextView       mDirectionTextview;
    private TextView       mSpeedTextview;
    private SpeedPanelView mSpeedPanelTextview;
    private ImageView      mDirectionImageview;
    //view
    private ImageView mRoadDirectionImageView = null;
    private TextView  mRoadDistanceTextView   = null;
    private TextView  mRoadNameTextView       = null;
//    private TextView  mRoadNameIndicateTextView = null;
//    private TextView  mRoadDirectionIndicateTextView = null;
//    private TextView  mNaviIndicateTextView = null;
//    private TextView  mNaviStatusTextView = null;

    private TextView  mNaviSpeedextView;
    private ViewGroup mLimitSpeedextViewGroup;
    private ViewGroup mServiceAreaViewGroup;
    private TextView  mServiceAreatextView;
    private TextView  mLimitSpeedtextView;
    private TextView  mRetainTimeTextView;
    private TextView  mRetainDistanceTextView;
    private TextView  mRoadNamePrefixTextView;
    private View mWalkerADASView;

    private ImageView mSystemTimeHourTenImageview;
    private ImageView mSystemTimeHourOneImageview;
    private ImageView mSystemTimeMinuteTenImageview;
    private ImageView mSystemTimeMinuteOneImageview;

    private RelativeLayout mLaneInfoViewgroup;
    private DriveWayView   mDriveWayView;

    private int[] mSpeedNumImg = {R.drawable.smooth_number_0, R.drawable.smooth_number_1, R.drawable.smooth_number_2,
            R.drawable.smooth_number_3, R.drawable.smooth_number_4, R.drawable.smooth_number_5, R.drawable.smooth_number_6,
            R.drawable.smooth_number_7, R.drawable.smooth_number_8, R.drawable.smooth_number_9};

    @Override
    public void showWalker() {
        mWalkerADASView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideWalker() {
        mWalkerADASView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setWalkerADASDataProvider(IWalkerADASDataProvider adasDataProvider) {

    }

    class TimeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                refreshDisplayTime();
            }
        }
    }

    private boolean      mTimeDisplayEnbale = false;
    private Time         mSystemTimer       = null;
    private TimeReceiver mTimeReceiver      = null;
    private IntentFilter mTimeFilter        = null;

    private Context mContext;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case SHOW_NAVI_INFO_ID:
                    showNaviInfoPanel(1000);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void init(Context context) {
        super.init(context);
        mContext = context;
        initTimeTick();
        refreshDisplayTime();

    }

    public FlatNaviInfoPanel() {

    }

    public static FlatNaviInfoPanel getInstance() {
        return mGlDrawNaviInfo;
    }

    @Override
    public void doDraw() {
        super.doDraw();
        updateServiceIndication();
        updateDirectionIcon();
        updateNextRoadDistance();
        updateNextRoadIndicate();
        updateTimeAbout();
        updateNaviIndicate();
        updateNaviStatus();
        updateSpeedInfo();

    }

    private void updateSpeedInfo() {
        if (mNaviSpeedextView != null) {
            mNaviSpeedextView.setText("" + mNaviInfoBean.getSpeed() % 300);
        }
        if (mLimitSpeedtextView != null) {
            if (mNaviInfoBean.getLimitSpeed() > 0) {
                String limitText = "" + mNaviInfoBean.getLimitSpeed();
//            refitText(mLimitSpeedtextView,limitText,mLimitSpeedtextView.getWidth());
                mLimitSpeedextViewGroup.setVisibility(View.VISIBLE);
                mLimitSpeedtextView.setText(limitText);
            } else {
                mLimitSpeedextViewGroup.setVisibility(View.INVISIBLE);
            }

        }
        updatePanelSpeed(mNaviInfoBean.getSpeed());
    }

    private void updateServiceIndication() {
        if (mServiceAreaViewGroup != null) {
            int remainDistance = mNaviInfoBean.getServiceAreaDistance();
            if(remainDistance>0){
                int dist=0;
                String text = null;
                if (remainDistance >= 1000) {
                    dist = (int)(((remainDistance / 100)) * 1.0 / 10);
                    text = dist + "公里";
                } else if (remainDistance >= 0) {
                    text = ((remainDistance)) + "米";
                }
                if (text != null) {
                    mServiceAreaViewGroup.setVisibility(View.VISIBLE);
                    mServiceAreatextView.setText(text);
                }
            }else {
                mServiceAreaViewGroup.setVisibility(View.INVISIBLE);
            }
        }

    }

    private void updatePanelSpeed(int speed) {
        if (mSpeedPanelTextview != null) {
            mSpeedPanelTextview.setSpeed(speed);
        }
        if (mSpeedTextview != null) {
            mSpeedTextview.setText("" + speed);
        }

    }


    private void refitText(TextView textView, String text, int textWidth) {
        if (textView == null) {
            return;
        }
        HaloLogger.logE("limitspeed", "textWidth is" + textWidth);
        Paint testPaint = new Paint();
        float maxTextSize = 28;
        float minTextSize = 8;
        if (textWidth > 0) {
            int availableWidth = textWidth - textView.getPaddingLeft() - textView.getPaddingRight();
            float trySize = maxTextSize;
            testPaint.setTextSize(trySize);
            while ((trySize > minTextSize) && (testPaint.measureText(text) > availableWidth)) {
                trySize -= 1;
                if (trySize <= minTextSize) {
                    trySize = minTextSize;
                    break;
                }
                testPaint.setTextSize(trySize);
                HaloLogger.logE("limitspeed", "trySize is" + trySize);
            }
            textView.setTextSize(trySize);
        }
    }

    ;

    private void updateTimeAbout() {
        if (mRetainTimeTextView != null) {
            int HOUR = 3600;
            int MINUS = 60;
            int remainTime = 0;
            String tText = "";
            if (mNaviInfoBean != null && mCommonBean != null && mCommonBean.isNavingStart()) {
                remainTime = mNaviInfoBean.getPathRetainTime();
            }
            if (remainTime >HOUR) {
                int hour = (int)(remainTime*1.0 /HOUR);
                int minus = (int)(remainTime%HOUR*1.0/MINUS);
                tText =hour+ "小时"+minus+"分钟";
            } else {
                tText = "" + remainTime / 60+"分钟";
            }
            //min显示
            mRetainTimeTextView.setText(tText);
        }
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

    private void updateNaviStatus() {
        String statusText = getNaviStatusText();
    }


    /**
     * 显示文本情景：
     * 无网络
     * 未偏航时，能正常导航
     * 偏航时，文字显示
     * 无GPS
     * 不能更新速度、里程，指南针正常工作
     *
     * @return
     */
    public String getNaviStatusText() {
        String title = null;
        if (mCommonBean == null) {
            return null;
        }
        /*if(!mCommonBean.isHasNetwork() && mCommonBean.isYaw()){
            title = "无网络信号\n正在搜索...";
        }*/
        if (mCommonBean.isYaw()) {
            title = "您已偏航\n正在重新规划路径";
        } else if (!mCommonBean.isMatchNaviPath()) {
            title = "您已偏离规划路径";
        } else if (!mCommonBean.isGpsWork()) {
            title = "无GPS信号\n请开往空旷处";
        }
        return title;
    }

    /**
     * 是否起步超过一定距离
     *
     * @return
     */
    public boolean isNavingReady() {
        int tDistance = mNaviInfoBean.getPathTotalDistance();
        int distance = mNaviInfoBean.getPathRetainDistance();
        return tDistance > distance && (tDistance - distance) > ARWayConst.NAVI_CAR_START_DISTANCE;
    }

    private void showHideChild(ViewGroup viewGroup, boolean show) {
        if (viewGroup == null) {
            return;
        }
        int cnt = viewGroup.getChildCount();
        for (int i = 0; i < cnt; i++) {
            View v = viewGroup.getChildAt(i);
            if (v != null) {
                if (show) {
                    v.setVisibility(View.VISIBLE);
                } else {
                    v.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void updateNaviIndicate() {

    }

    public void updateDirectionIcon() {
        if (mRoadDirectionImageView != null) {
            Bitmap naviBitmap = mNaviInfoBean.getNaviIconBitmap();
            if (naviBitmap != null) {
                mRoadDirectionImageView.setImageBitmap(naviBitmap);
            }
        }
    }

    private void updateNextRoadIndicate() {
        if (mRoadNameTextView != null) {
            String roadName = mNaviInfoBean.getNextRoadName();
            if (roadName != null && roadName.trim() != "") {//&& roadName.trim() !="无名路"
                String text = roadName;
                if (text.contains("目的地")){
                    mRoadNamePrefixTextView.setText("到达");
                }else {
                    mRoadNamePrefixTextView.setText("进入");
                }
                mRoadNameTextView.setText(text);
            } else {
                mRoadNameTextView.setText("");
                mRoadNamePrefixTextView.setText("");
            }

        }
    }

    private void updateNextRoadDistance() {
        int dist = 0;
        if (mRoadDistanceTextView != null) {
//            int remainDistance = mNaviInfoBean.getPathRetainTime();
            int remainDistance = mNaviInfoBean.getStepRetainDistance();
            String text = null;
            if (remainDistance > 1000) {
                dist = (int)(((remainDistance / 100)) * 1.0 / 10);
                text = dist + "公里";
            } else if (remainDistance >= 0) {
                text = ((remainDistance)) + "米";
            }
            if (text != null) {
                mRoadDistanceTextView.setText(text);
            }
        }
        if (mRetainDistanceTextView != null) {
            int remainDistance = mNaviInfoBean.getPathRetainDistance();
            String text = null;
            if (remainDistance > 1000) {
                dist = (int)(((remainDistance / 100)) * 1.0 / 10);
                text = dist + "公里";
            } else if (remainDistance >= 0) {
                text = ((remainDistance)) + "米";
            }
            if (text != null) {
                mRetainDistanceTextView.setText(text);
            }
        }
    }

    public void showLaneInfo(AMapLaneInfo[] laneInfos, byte[] laneBackgroundInfo, byte[] laneRecommendedInfo) {
        /*if (mDriveWayView != null) {
            if(viewDebug){
                HaloLogger.logE("showLaneInfo","showLaneInfo");
            }
            mDriveWayView.loadDriveWayBitmap(laneBackgroundInfo, laneRecommendedInfo);
            mDriveWayView.setVisibility(View.VISIBLE);
        }*/
    }

    public void hideLaneInfo() {
        //隐藏车道信息
        /*if (mDriveWayView != null) {
            mDriveWayView.setVisibility(View.INVISIBLE);
        }*/
    }

    @Override
    public View getViewInstance(Context context) {
        return null;
    }


    @Override
    public void setView(Context context, View view) {
        if (view != null) {
            View mMainLayout = view;
            mNaviInfoPanelViewgroup = (ViewGroup) view.findViewById(R.id.navi_info_panel_viewgroup);

            mRoadDirectionImageView = (ImageView) view.findViewById(R.id.next_road_direction_imageview);
            mRoadDistanceTextView = (TextView) view.findViewById(R.id.next_road_distance_textview);
            mRoadNamePrefixTextView = (TextView) view.findViewById(R.id.road_name_prefix_textview);
            mRoadNameTextView = (TextView) view.findViewById(R.id.next_road_name_textview);

            mNaviSpeedextView = (TextView) view.findViewById(R.id.speed_textview);
            mLimitSpeedextViewGroup = (ViewGroup) view.findViewById(R.id.speed_limit_viewgroup);
            mLimitSpeedtextView = (TextView) view.findViewById(R.id.speed_limit_textview);

            mServiceAreaViewGroup = (ViewGroup) view.findViewById(R.id.service_area_near_viewgroup);
            mServiceAreatextView = (TextView) view.findViewById(R.id.service_area_near_textview);

            mRetainTimeTextView = (TextView) view.findViewById(R.id.prefix_time_textview);
            mRetainDistanceTextView = (TextView) view.findViewById(R.id.prefix_distance_textview);

            mSystemTimeHourTenImageview = (ImageView) view.findViewById(R.id.hour_ten_imageview);
            mSystemTimeHourOneImageview = (ImageView) view.findViewById(R.id.hour_one_imageview);
            mSystemTimeMinuteTenImageview = (ImageView) view.findViewById(R.id.minute_ten_imageview);
            mSystemTimeMinuteOneImageview = (ImageView) view.findViewById(R.id.minute_one_imageview);

            mWalkerADASView = view.findViewById(R.id.walker_view);

            //此句不能删....HL
//            dy.a(context);
            mLaneInfoViewgroup = (RelativeLayout) view.findViewById(R.id.lane_info_viewgroup);
            mDriveWayView = (DriveWayView) view.findViewById(R.id.lane_info_view);

            mNaviPanelViewgroup = (ViewGroup) mMainLayout.findViewById(R.id.navi_panel_viewgroup);
            mCompassViewgroup = (ViewGroup) mMainLayout.findViewById(R.id.compass_viewgroup);
            mDirectionImageview = (ImageView) mMainLayout.findViewById(R.id.compass_direction_imageview);
            mDirectionImageview.setAlpha(0.5f);
            mSpeedPanelTextview = (SpeedPanelView) mMainLayout.findViewById(R.id.navi_panel_view);
            mDirectionTextview = (TextView) mMainLayout.findViewById(R.id.compass_textview);
            mSpeedTextview = (TextView) mMainLayout.findViewById(R.id.speed_panel_textview);

            //道路渐变
            mRoadMaskViewgroup = (ViewGroup) view.findViewById(R.id.road_mask_viewgroup);
            mRoadMaskViewLeft = (ImageView) view.findViewById(R.id.road_mask_left);
            mRoadMaskViewRight = (ImageView) view.findViewById(R.id.road_mask_right);

            int maskColor = Color.TRANSPARENT;
            mRoadMaskViewLeft.setBackgroundColor(maskColor);
            mRoadMaskViewRight.setBackgroundColor(maskColor);

            mRoadMaskViewLeft.setScaleX(3.0f/3);
            mRoadMaskViewRight.setScaleX(3.0f/3);

            mRoadMaskViewLeft.setVisibility(View.INVISIBLE);
            mRoadMaskViewRight.setVisibility(View.INVISIBLE);

            initCompassSensor(context);
            prepareSpeedPanelAnim();




        }
        if (!viewDebug) {
            defaultViewInit();
        }
    }

    public void prepareSpeedPanelAnim() {
        mSpeedPanelHideAnimators.clear();

        ObjectAnimator anim;
        anim = ObjectAnimator.ofFloat(mNaviPanelViewgroup, "ScaleX",1,0.1f);
        anim.setInterpolator(new LinearInterpolator());
        mSpeedPanelHideAnimators.add(anim);

        anim = ObjectAnimator.ofFloat(mNaviPanelViewgroup, "ScaleY",1,0.1f);
        anim.setInterpolator(new LinearInterpolator());
        mSpeedPanelHideAnimators.add(anim);

        anim = ObjectAnimator.ofFloat(mNaviPanelViewgroup, "alpha",1,0.1f);
        anim.setInterpolator(new LinearInterpolator());
        mSpeedPanelHideAnimators.add(anim);

        anim = ObjectAnimator.ofFloat(mCompassViewgroup, "alpha",1,0.1f);
        anim.setInterpolator(new LinearInterpolator());
        mSpeedPanelHideAnimators.add(anim);
    }

    public void hideSpeedPanelAnim(long duration) {
//        prepareSpeedPanelAnim();
        for (ObjectAnimator a: mSpeedPanelHideAnimators){
            if(a.isStarted()){
                a.cancel();
            }
            a.setDuration(duration);
            a.start();
        }
    }

    public void showSpeedPanel(){
        mNaviPanelViewgroup.setScaleX(1);
        mNaviPanelViewgroup.setScaleY(1);
        mNaviPanelViewgroup.setAlpha(1);
        mCompassViewgroup.setAlpha(1);
    }

    public void hideNaviInfoPanel(){
//        if (mNaviInfoPanelViewgroup != null) {
            mNaviInfoPanelViewgroup.setAlpha(0);
//        }
    }

    public void showNaviInfoPanel(long duration){
        if (mNaviInfoPanelViewgroup != null) {
            ObjectAnimator anim =  ObjectAnimator.ofFloat(mNaviInfoPanelViewgroup,"Alpha",0,1);
            anim.setInterpolator(new LinearInterpolator());
            anim.setDuration(duration);
            anim.start();
        }
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


    private void defaultViewInit() {
        if (mRoadDirectionImageView != null) {
            mRoadDirectionImageView.setImageBitmap(null);
        }
        if (mRoadDistanceTextView != null) {
            mRoadDistanceTextView.setText("");
        }
        if (mRoadNamePrefixTextView != null) {
            mRoadNamePrefixTextView.setText("");
        }
        if (mRoadNameTextView != null) {
            mRoadNameTextView.setText("");
        }
        if (mLimitSpeedextViewGroup == null) {
            mLimitSpeedextViewGroup.setVisibility(View.INVISIBLE);
        }
        if (mNaviSpeedextView != null) {
            mNaviSpeedextView.setText("");
        }
        if (mLimitSpeedtextView != null) {
            mLimitSpeedtextView.setText("");
        }
        if (mRetainTimeTextView != null) {
            mRetainTimeTextView.setText("");
        }
        if (mRetainDistanceTextView != null) {
            mRetainDistanceTextView.setText("");
        }
        if (mDriveWayView != null) {
            mDriveWayView.setVisibility(View.INVISIBLE);
        }
        doDraw();
        updateSpeedInfo();
//        hideNaviInfoPanel();
    }

    public void showHideForMask(boolean isShow) {
        int state = View.VISIBLE;
        if(!isShow){
            state = View.INVISIBLE;
        }
        mRetainTimeTextView.setVisibility(state);
        mRetainDistanceTextView.setVisibility(state);
    }

    public void showHide(boolean show) {

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
//            Log.e("compass", "direction is " + direction);
            mTargetDirection = normalizeDegree(direction);
            updatePanelDirection();
        }
    }

    public void roadFlipAnimation(long duration){
        if (mRoadMaskViewgroup != null) {
            // TODO: 16/11/2016 移动的位置不能保证每次都一样
            float dist = DisplayUtil.dip2px(mContext,-200f);
            if (mRoadMaskAnimator == null) {
                mRoadMaskAnimator = ObjectAnimator.ofFloat(mRoadMaskViewgroup, "TranslationY",mRoadMaskViewgroup.getTranslationY(), mRoadMaskViewgroup.getTranslationY()+ dist);
                mRoadMaskAnimator.setInterpolator(new LinearInterpolator());
            }else {
                mRoadMaskViewgroup.setTranslationY(-dist);
            }
            if(mRoadMaskAnimator.isStarted()) {
                mRoadMaskAnimator.cancel();
            }
            mRoadMaskAnimator.setDuration(duration);
            mRoadMaskAnimator.start();
        }
    }

    public void onNaviStartAnimation(long duration) {

        //复位显示速度表盘、隐藏信息面板
//        showSpeedPanel();
//        hideNaviInfoPanel();
//
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        },duration);
//        //隐藏速度表盘、显示信息面板动画
//        hideSpeedPanelAnim(duration);
//        showNaviInfoPanel(duration);
//        mHandler.sendEmptyMessage(SHOW_NAVI_INFO_ID);
    }

    private float normalizeDegree(float degree) {
        return (degree + 720) % 360;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void resetView() {
        defaultViewInit();
    }
}
