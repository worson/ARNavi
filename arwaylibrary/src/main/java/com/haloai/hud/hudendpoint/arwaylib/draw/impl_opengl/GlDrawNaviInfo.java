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
    private ViewGroup mIndicateViewGroup = null;


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
        updateSatusText(statusText);
    }

    /**
     * 更新导航异常状态指示
     * @param text
     */
    private void updateSatusText(String text) {
//        text = "注意,前方有龙在飞";
        if (mNaviStatusTextView != null) {
            if (text != null) {
                if(!mNaviStatusTextView.isShown()){
                    mNaviStatusTextView.setVisibility(View.VISIBLE);
                }
                mNaviStatusTextView.setText(text);
            }else {
                if(mNaviStatusTextView.isShown()){
                    mNaviStatusTextView.setVisibility(View.INVISIBLE);
                }
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
            mRoadNameIndicateTextView.setText("");
            mRoadDirectionIndicateTextView.setText("");
        }
        if (mRoadIndicateTextView != null) {
            mRoadIndicateTextView.setText("");
        }

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

    private void switchDisplay() {
        if (true){//isNavingReady()
            showHideChild(mIndicateViewGroup,false);
            showHideChild(mNextRoadViewGroup,true);
        }else {
            showHideChild(mIndicateViewGroup,true);
            showHideChild(mNextRoadViewGroup,false);
        }
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
        if (mNaviIndicateTextView != null) {
            if (mNaviInfoBean != null) {
                String text = mNaviInfoBean.getNaviText();
                if(ARWayConst.ENABLE_LOG_OUT && ARWayConst.ENABLE_FAST_LOG){
                    HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG,"updateNaviIndicate navi text is "+text);
                }
                String display = "";
                String roadName = "";
                String roadDirection = "";
                if (text != null && text.trim()!="") {
                    int index = text.indexOf("请行驶");
                    if(index>=0 && index<=text.length()){
                        display= text.substring(index,text.length());
                        int cIndex = display.indexOf("，");
                        if(display !=null && cIndex>3){
                            roadName= display.substring(4,cIndex);
                            int dIndex = display.indexOf("行驶",cIndex);
                            if(dIndex>cIndex){
                                roadDirection = display.substring(cIndex+1,dIndex+2);
                            }
                        }
                        if(ARWayConst.ENABLE_LOG_OUT && ARWayConst.ENABLE_FAST_LOG){
                            HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG,"updateNaviIndicate display text is "+display+" ,roadName is "+roadName+"  ,roadDirection is"+roadDirection);
                        }

                    }
                }
                if (display.trim() != "" && roadName.trim() != "" && roadDirection.trim() !="") {//(!mCommonBean.isStartOk()) &&
                    mNaviIndicateTextView.setText("请行驶到");
                    mRoadNameIndicateTextView.setText(roadName);
                    mRoadDirectionIndicateTextView.setText(roadDirection);
                }else {
                    mNaviIndicateTextView.setText("");
                    mRoadNameIndicateTextView.setText("");
                    mRoadDirectionIndicateTextView.setText("");
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
            if (roadName != null && roadName.trim() !="" ) {//&& roadName.trim() !="无名路"
                String text = "沿"+roadName+"行驶";
                mRoadIndicateTextView.setText(text);
            }else {
                mRoadIndicateTextView.setText("");
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
    }

    @Override
    public View getViewInstance(Context context) {
        return null;
    }


    @Override
    public void setView(Context context, View view) {
        if (view != null) {
            mNextRoadViewGroup = (ViewGroup) view.findViewById(R.id.next_road_viewgroup);
            mIndicateViewGroup = (ViewGroup) view.findViewById(R.id.navi_indicate_viewgroup);
            mRoadDirectionImageView = (ImageView) view.findViewById(R.id.next_road_direction_imageview);
            mRoadDistanceTextView = (TextView) view.findViewById(R.id.next_road_distance_textview);
            mRoadIndicateTextView = (TextView) view.findViewById(R.id.next_road_indicate_textview);
            mNaviIndicateTextView = (TextView) view.findViewById(R.id.navi_indicate_textview);
            mRoadNameIndicateTextView = (TextView) view.findViewById(R.id.navi_indicate_road_textview);
            mRoadDirectionIndicateTextView = (TextView) view.findViewById(R.id.navi_indicate_direction_textview);
            mNaviStatusTextView = (TextView) view.findViewById(R.id.navi_status_textiview);
            mNaviStatusTextView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            if (mNaviStatusTextView != null) {
                HaloLogger.logE(ARWayConst.ERROR_LOG_TAG," navi info ,setView ok");
            }

        }
        dafaultViewInit();
    }

    public void showHide(boolean show){
        View[] views = new View[]{mNextRoadViewGroup,mIndicateViewGroup};
        for (int i = 0; i <views.length ; i++) {
            View v = views[i];
            if (v != null) {
                if (show){
                    v.setVisibility(View.VISIBLE);
                }else {
                    v.setVisibility(View.INVISIBLE);
                }
            }
        }
    }


    @Override
    public void resetView() {
        dafaultViewInit();
    }
}
