package com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.nfc.Tag;
import android.text.format.Time;
import android.view.View;
import android.view.ViewGroup;
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
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.materials.textures.Texture;

/**
 * Created by wangshengxing on 16/7/15.
 */
public class FlatNaviInfoPanel extends DrawObject implements IViewOperation {
    private static final boolean GPS_DEBUG_MODE       = true;

    private static FlatNaviInfoPanel mGlDrawNaviInfo = new FlatNaviInfoPanel();

    //bean
    private static NaviInfoBean mNaviInfoBean = (NaviInfoBean) BeanFactory.getBean(BeanFactory.BeanType.NAVI_INFO);
    private static RouteBean    mRouteBean    = (RouteBean) BeanFactory.getBean(BeanFactory.BeanType.ROUTE);
    private static CommonBean   mCommonBean   = (CommonBean) BeanFactory.getBean(BeanFactory.BeanType.COMMON);

    //view
    private ImageView mRoadDirectionImageView = null;
    private TextView  mRoadDistanceTextView   = null;
    private TextView  mRoadNameTextView       = null;
//    private TextView  mRoadNameIndicateTextView = null;
//    private TextView  mRoadDirectionIndicateTextView = null;
//    private TextView  mNaviIndicateTextView = null;
//    private TextView  mNaviStatusTextView = null;

    private TextView mNaviSpeedextView;
    private ViewGroup mLimitSpeedextViewGroup;
    private TextView mLimitSpeedextView;
    private TextView mRetainTimeTextView;
    private TextView mRetainDistanceTextView;
    private TextView mRoadNamePrefixTextView;

    private ImageView mSystemTimeHourTenImageview;
    private ImageView mSystemTimeHourOneImageview;
    private ImageView mSystemTimeMinuteTenImageview;
    private ImageView mSystemTimeMinuteOneImageview;

    private RelativeLayout mLaneInfoViewgroup;
    private DriveWayView mDriveWayView;

    private int[] mSpeedNumImg={R.drawable.speed_number_0,R.drawable.speed_number_1,R.drawable.speed_number_2,
            R.drawable.speed_number_3,R.drawable.speed_number_4,R.drawable.speed_number_5,R.drawable.speed_number_6,
            R.drawable.speed_number_7,R.drawable.speed_number_8,R.drawable.speed_number_9};

    class TimeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                refreshDisplayTime();
            }
        }
    }

    private boolean mTimeDisplayEnbale = false;
    private Time mSystemTimer = null;
    private TimeReceiver mTimeReceiver = null;
    private IntentFilter mTimeFilter   = null;

    private Context mContext;

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
            mNaviSpeedextView.setText(""+mNaviInfoBean.getSpeed()%300);
        }
        if (mLimitSpeedextView != null) {
            if(mNaviInfoBean.getLimitSpeed()>0){
                String limitText = ""+mNaviInfoBean.getLimitSpeed();
//            refitText(mLimitSpeedextView,limitText,mLimitSpeedextView.getWidth());
                mLimitSpeedextViewGroup.setVisibility(View.VISIBLE);
                mLimitSpeedextView.setText(limitText);
            }else {
                mLimitSpeedextViewGroup.setVisibility(View.INVISIBLE);
            }

        }
    }

    private void refitText(TextView textView, String text, int textWidth)
    {
        if (textView == null) {
            return;
        }
        HaloLogger.logE("limitspeed","textWidth is"+textWidth);
        Paint testPaint = new Paint();
        float maxTextSize = 28;
        float minTextSize = 8;
        if (textWidth > 0)
        {
            int availableWidth = textWidth - textView.getPaddingLeft() - textView.getPaddingRight();
            float trySize = maxTextSize;
            testPaint.setTextSize(trySize);
            while ((trySize > minTextSize) && (testPaint.measureText(text) > availableWidth))
            {
                trySize -= 1;
                if (trySize <= minTextSize)
                {
                    trySize = minTextSize;
                    break;
                }
                testPaint.setTextSize(trySize);
                HaloLogger.logE("limitspeed","trySize is"+trySize);
            }
            textView.setTextSize(trySize);
        }
    };

    private void updateTimeAbout() {
        if (mRetainTimeTextView != null) {
            int remainTime = 0;
            String tText = "0";
            String tScale = "min";
            if(mNaviInfoBean != null && mCommonBean != null && mCommonBean.isNavingStart() ){
                remainTime = mNaviInfoBean.getPathRetainTime();
            }
            if(remainTime>3*60*60){
                tText = ""+(int)((remainTime/(6*60)))*1.0/10;
                tScale="小时";
            }else {
                tText = ""+remainTime/60;
                tScale = "分钟";
            }
            //min显示
            mRetainTimeTextView.setText(tText+tScale);
        }
    }

    private void initTimeTick(){
        mSystemTimer=new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料。
        mTimeReceiver = new TimeReceiver();
        mTimeFilter = new IntentFilter();
        mTimeFilter.addAction(Intent.ACTION_TIME_TICK);
        mContext.registerReceiver(mTimeReceiver, mTimeFilter);
        mTimeDisplayEnbale = true;
    }

    private void refreshDisplayTime(){
        if(!mTimeDisplayEnbale){
            return;
        }
        mSystemTimer.setToNow(); // 取得系统时间。
        int hour = mSystemTimer.hour;
        int minute = mSystemTimer.minute;
        mSystemTimeHourTenImageview.setImageResource(mSpeedNumImg[(hour/10)%10]);
        mSystemTimeHourOneImageview.setImageResource(mSpeedNumImg[(hour)%10]);
        mSystemTimeMinuteTenImageview.setImageResource(mSpeedNumImg[(minute/10)%10]);
        mSystemTimeMinuteOneImageview.setImageResource(mSpeedNumImg[(minute)%10]);
    }

    private void updateNaviStatus() {
        String statusText = getNaviStatusText();
    }


    /**
     * 显示文本情景：
     * 无网络
     *  未偏航时，能正常导航
     *  偏航时，文字显示
     *无GPS
     *  不能更新速度、里程，指南针正常工作
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
        if(mCommonBean.isYaw()){
            title = "您已偏航\n正在重新规划路径";
        }else if(!mCommonBean.isMatchNaviPath()){
            title = "您已偏离规划路径";
        } else if(!mCommonBean.isGpsWork()){
            title = "无GPS信号\n请开往空旷处";
        }
        return title;
    }

    /**
     * 是否起步超过一定距离
     * @return
     */
    public boolean isNavingReady() {
        int tDistance = mNaviInfoBean.getPathTotalDistance();
        int distance = mNaviInfoBean.getPathRetainDistance();
        return tDistance>distance && (tDistance-distance)> ARWayConst.NAVI_CAR_START_DISTANCE;
    }

    private void showHideChild(ViewGroup viewGroup,boolean show){
        if (viewGroup == null) {
            return;
        }
        int cnt = viewGroup.getChildCount();
        for (int i = 0; i < cnt; i++) {
            View v = viewGroup.getChildAt(i);
            if (v != null) {
                if (show){
                    v.setVisibility(View.VISIBLE);
                }else {
                    v.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void updateNaviIndicate() {

    }

    public void updateDirectionIcon(){
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
            if (roadName != null && roadName.trim() !="" ) {//&& roadName.trim() !="无名路"
                String text = roadName;
                mRoadNamePrefixTextView.setText("进入");
                mRoadNameTextView.setText(text);
            }else {
                mRoadNameTextView.setText("");
                mRoadNamePrefixTextView.setText("");
            }

        }
    }

    private void updateNextRoadDistance() {
        if (mRoadDistanceTextView != null) {
//            int remainDistance = mNaviInfoBean.getPathRetainTime();
            int remainDistance = mNaviInfoBean.getStepRetainDistance();
            String text = null;
            if(remainDistance>1000){
                text = ((remainDistance/100))*1.0/10+ "公里";
            }else if(remainDistance>=0) {
                text = ((remainDistance))+ "米";
            }
            if (text != null) {
                mRoadDistanceTextView.setText(text);
            }
        }
        if (mRetainDistanceTextView != null) {
            int remainDistance = mNaviInfoBean.getPathRetainDistance();
            String text = null;
            if(remainDistance>1000){
                text = ((remainDistance/100))*1.0/10+ "公里";
            }else if(remainDistance>=0) {
                text = ((remainDistance))+ "米";
            }
            if (text != null) {
                mRetainDistanceTextView.setText(text);
            }
        }
    }

    public void showLaneInfo(AMapLaneInfo[] laneInfos, byte[] laneBackgroundInfo, byte[] laneRecommendedInfo) {
        /*if (mDriveWayView == null) {
            if(viewDebug){
                HaloLogger.logE("showLaneInfo","showLaneInfo");
            }
            mDriveWayView.loadDriveWayBitmap(laneBackgroundInfo, laneRecommendedInfo);
            mDriveWayView.setVisibility(View.VISIBLE);
        }*/
    }

    public void hideLaneInfo() {
        //隐藏车道信息
        if (mDriveWayView == null) {
            mDriveWayView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public View getViewInstance(Context context) {
        return null;
    }


    @Override
    public void setView(Context context, View view) {
        if (view != null) {
            mRoadDirectionImageView = (ImageView) view.findViewById(R.id.next_road_direction_imageview);
            mRoadDistanceTextView = (TextView) view.findViewById(R.id.next_road_distance_textview);
            mRoadNamePrefixTextView = (TextView) view.findViewById(R.id.road_name_prefix_textview);
            mRoadNameTextView = (TextView) view.findViewById(R.id.next_road_name_textview);

            mNaviSpeedextView = (TextView) view.findViewById(R.id.speed_textview);
            mLimitSpeedextViewGroup = (ViewGroup) view.findViewById(R.id.speed_limit_viewgroup);
            mLimitSpeedextView = (TextView) view.findViewById(R.id.speed_limit_textview);

            mRetainTimeTextView = (TextView)view.findViewById(R.id.prefix_time_textview);
            mRetainDistanceTextView = (TextView)view.findViewById(R.id.prefix_distance_textview);

            mSystemTimeHourTenImageview = (ImageView)view.findViewById(R.id.hour_ten_imageview);
            mSystemTimeHourOneImageview = (ImageView)view.findViewById(R.id.hour_one_imageview);
            mSystemTimeMinuteTenImageview = (ImageView)view.findViewById(R.id.minute_ten_imageview);
            mSystemTimeMinuteOneImageview = (ImageView)view.findViewById(R.id.minute_one_imageview);


            mLaneInfoViewgroup = (RelativeLayout)view.findViewById(R.id.lane_info_viewgroup);
            mDriveWayView = (DriveWayView)view.findViewById(R.id.lane_info_view);

        }
        if(!viewDebug){
            dafaultViewInit();
        }
    }

    private void dafaultViewInit() {
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
        if (mLimitSpeedextView != null) {
            mLimitSpeedextView.setText("");
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
    }

    public void showHide(boolean show){

    }

    @Override
    public void resetView() {
        dafaultViewInit();
    }
}
