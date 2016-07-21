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
    private static GlDrawNaviInfo mGlDrawNaviInfo = new GlDrawNaviInfo();

    //bean
    private static NaviInfoBean mNaviInfoBean = (NaviInfoBean) BeanFactory.getBean(BeanFactory.BeanType.NAVI_INFO);
    private static RouteBean    mRouteBean    = (RouteBean) BeanFactory.getBean(BeanFactory.BeanType.ROUTE);
    private static CommonBean   mCommonBean   = (CommonBean) BeanFactory.getBean(BeanFactory.BeanType.COMMON);

    //view
    private ImageView mRoadDirectionImageView = null;
    private TextView  mRoadDistanceTextView = null;
    private TextView  mRoadIndicateTextView = null;
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
        if(mCommonBean.isNaviEnd()){
            title = "本次导航结束";
        }else if(mCommonBean.isYaw()){
            title = "正在计算偏航路径...";
        }else if (!isNavingReady()){
            title = "开始导航";//+NOT_DRAW_TEXT_CONTENT
            if(mRouteBean.getGpsNumber()<1){
                title = "GPS 信号弱";
            }
        }if (!mRouteBean.isMatchNaviPath()) {
            HaloLogger.logE("sen_debug_error", "route update ：location 不在path 上");
            title = "正在检测行驶方向";
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
                String display = null;
                int index = text.indexOf("请行驶");
                if(index>=0 && index<=text.length()){
                    display= text.substring(index,text.length());
                }
                if (mCommonBean.isStartOk()){
                    mNaviIndicateTextView.setText("");
                }else if (display != null) {
                    mNaviIndicateTextView.setText(display);
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
            String roadName = mNaviInfoBean.getNextRoadName();
            if (roadName != null) {
                String text = "请沿"+roadName+"行驶";
                mRoadIndicateTextView.setText(text);
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
            mNaviStatusTextView = (TextView) view.findViewById(R.id.navi_status_textiview);

        }

        dafaultViewInit();
    }



}
