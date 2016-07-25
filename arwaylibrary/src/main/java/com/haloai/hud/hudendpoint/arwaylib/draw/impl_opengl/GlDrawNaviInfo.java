package com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.CommonBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.NaviInfoBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.RouteBean;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;
import com.haloai.hud.hudendpoint.arwaylib.draw.IViewOperation;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.utils.HaloLogger;

/**
 * Created by wangshengxing on 16/7/15.
 */
public class GlDrawNaviInfo extends DrawObject implements IViewOperation {
    private static final boolean GPS_DEBUG_MODE       = true;

    private static GlDrawNaviInfo mGlDrawNaviInfo = new GlDrawNaviInfo();

    //bean
    private static NaviInfoBean mNaviInfoBean = (NaviInfoBean) BeanFactory.getBean(BeanFactory.BeanType.NAVI_INFO);
    private static RouteBean    mRouteBean    = (RouteBean) BeanFactory.getBean(BeanFactory.BeanType.ROUTE);
    private static CommonBean   mCommonBean   = (CommonBean) BeanFactory.getBean(BeanFactory.BeanType.COMMON);

    //view
    private ImageView mRoadDirectionImageView = null;
    private TextView  mRoadDistanceTextView = null;
    private TextView  mRoadIndicateTextView = null;
    private TextView  mRoadNameIndicateTextView = null;
    private TextView  mRoadDirectionIndicateTextView = null;
    private TextView  mNaviIndicateTextView = null;
    private TextView  mNaviStatusTextView = null;
    private ViewGroup mNextRoadViewGroup = null;


    public GlDrawNaviInfo() {

    }
    public static GlDrawNaviInfo getInstance() {
        return mGlDrawNaviInfo;
    }

    @Override
    public void doDraw() {
        super.doDraw();
        updateDirectionIcon();
        updateNextRoadDistance();
        updateNextRoadIndicate();
        updateNaviIndicate();
        switchDisplay();
        updateNaviStatus();
    }

    private void updateNaviStatus() {
        String statusText = getNaviStatusText();
        if (mNaviStatusTextView != null) {
            if (statusText != null) {
                mNaviStatusTextView.setVisibility(View.VISIBLE);
                mNaviStatusTextView.setText(statusText);
            }else {
                mNaviStatusTextView.setVisibility(View.INVISIBLE);
                mNaviStatusTextView.setText("");
            }
        }
    }

    private void dafaultViewInit() {
        if (mNaviStatusTextView != null) {
            mNaviStatusTextView.setText("");
        }
        if (mNaviIndicateTextView != null) {
            mNaviIndicateTextView.setText("");
        }

    }


    public String getNaviStatusText() {
        String title = null;
        if (mRouteBean == null || mCommonBean == null) {
            return null;
        }
        if(mCommonBean.isYaw()){
            title = "已偏航，路径重新规划中...";
        }if(GPS_DEBUG_MODE != true && mRouteBean.getGpsNumber()<1){
            title = "无网络信号，正在搜索...";
        }/*if (!mRouteBean.isMatchNaviPath()) {
            title = "正在检测行驶方向";
        }*/
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

    private void switchDisplay() {

        if (isNavingReady()){
            if (mNaviIndicateTextView != null) {
                mNaviIndicateTextView.setVisibility(View.INVISIBLE);
            }
            if (mNextRoadViewGroup != null) {
                mNextRoadViewGroup.setVisibility(View.VISIBLE);
            }
        }else {
            if (mNaviIndicateTextView != null) {
                mNaviIndicateTextView.setVisibility(View.VISIBLE);
            }
            if (mNextRoadViewGroup != null) {
                mNextRoadViewGroup.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void updateNaviIndicate() {
        if (mNaviIndicateTextView != null) {
            if (mNaviInfoBean != null) {
                String text = mNaviInfoBean.getNaviText();
                HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG,"updateNaviIndicate navi text is "+text);
                String display = "";
                String roadName = "";
                String roadDirection = "";
                int index = text.indexOf("请行驶");
                if(index>=0 && index<=text.length()){
                    display= text.substring(index,text.length());
                    int cIndex = display.indexOf("，");
                    if(display !=null && cIndex>3){
                        roadName= text.substring(3,cIndex-1);
                        int dIndex = display.indexOf("行驶");
                        if(dIndex>cIndex){
                            roadDirection = display.substring(cIndex+1,dIndex+1);
                        }
                    }


                }
                if (mCommonBean.isStartOk()){
                    mNaviIndicateTextView.setText("");
                    mRoadNameIndicateTextView.setText(roadName);
                    mRoadDirectionIndicateTextView.setText(roadDirection);
                }else if (display != null) {
                    mNaviIndicateTextView.setText("进入");
                    mRoadNameIndicateTextView.setText(roadName);
                    mRoadDirectionIndicateTextView.setText(roadDirection);
                }
            }

        }
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
        if (mRoadIndicateTextView != null) {
            String roadName = mNaviInfoBean.getCurrentRoadName();
            if (roadName != null) {
                String text = "沿"+roadName+"行驶";
                mRoadIndicateTextView.setText(text);
            }else {
                mRoadIndicateTextView.setText("");
            }
        }
    }

    private void updateNextRoadDistance() {
        if (mRoadDistanceTextView != null) {
            int remainDistance = mNaviInfoBean.getPathRetainTime();
            String text = null;
            if(remainDistance>1000){
                text = ((remainDistance/100))*1.0/10+ "km";
            }else if(remainDistance>=0) {
                text = ((remainDistance))+ "m";
            }
            if (text != null) {
                mRoadDistanceTextView.setText(text);
            }
        }
    }

    @Override
    public View getViewInstance(Context context) {
        return null;
    }


    @Override
    public void setView(Context context, View view) {
        if (view != null) {
            mNextRoadViewGroup = (ViewGroup) view.findViewById(R.id.next_road_viewgroup);
            mRoadDirectionImageView = (ImageView) view.findViewById(R.id.next_road_direction_imageview);
            mRoadDistanceTextView = (TextView) view.findViewById(R.id.next_road_distance_textview);
            mRoadIndicateTextView = (TextView) view.findViewById(R.id.next_road_indicate_textview);
            mNaviIndicateTextView = (TextView) view.findViewById(R.id.navi_indicate_textview);
            mRoadNameIndicateTextView = (TextView) view.findViewById(R.id.navi_indicate_road_textview);
            mRoadDirectionIndicateTextView = (TextView) view.findViewById(R.id.navi_indicate_direction_textview);
            mNaviStatusTextView = (TextView) view.findViewById(R.id.navi_status_textiview);

        }

        dafaultViewInit();
    }

    @Override
    public void resetView() {
        dafaultViewInit();
    }
}
