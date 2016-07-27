package com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.amap.api.maps.model.Text;
import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.CommonBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.NaviInfoBean;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawViewObject;
import com.haloai.hud.hudendpoint.arwaylib.draw.IDriveStateLister;
import com.haloai.hud.hudendpoint.arwaylib.utils.DisplayUtil;
import com.haloai.hud.hudendpoint.arwaylib.view.RetainDistanceView;

/**
 * Created by wangshengxing on 16/7/15.
 */
public class GlDrawRetainDistance extends DrawViewObject implements IDriveStateLister {
    private static GlDrawRetainDistance mGlDrawRetainDistance = new GlDrawRetainDistance();

    //view
    private TextView mRetainTimeTextView = null;
    private TextView mRetainTimeScaleTextView = null;
    private ViewGroup mRetainTimeViewGroup = null;
    private RetainDistanceView mRetainDistanceView = null;

    //bean
    private static NaviInfoBean mNaviInfoBean = (NaviInfoBean) BeanFactory.getBean(BeanFactory.BeanType.NAVI_INFO);
    private static CommonBean mCommonBean = (CommonBean) BeanFactory.getBean(BeanFactory.BeanType.COMMON);

    public GlDrawRetainDistance() {
        
    }
    public static GlDrawRetainDistance getInstance() {
        return mGlDrawRetainDistance;
    }

    @Override
    public View getViewInstance(Context context) {
        if (mRetainDistanceView == null) {
            mRetainDistanceView = new RetainDistanceView(context, 118, 118);
        }
        return mRetainDistanceView;
    }

    @Override
    protected void initLayout(Context context, ViewGroup parent, View view) {
        super.initLayout(context, parent, view);
        if (mResources != null) {
            VIEW_DRIVING_WIDTH = (int)(mResources.getDimension(R.dimen.distance_view_driving_width));
            VIEW_DRIVING_HEIGHT = (int)(mResources.getDimension(R.dimen.distance_view_driving_height));
            VIEW_NOT_DRIVING_WIDTH = (int)(mResources.getDimension(R.dimen.distance_view_pause_width));
            VIEW_NOT_DRIVING_HEIGHT = (int)(mResources.getDimension(R.dimen.distance_view_pause_height));

            VIEW_TOP_NOT_DRIVING_Y = 0;
            if (context != null) {
                VIEW_TOP_DRIVING_Y = DisplayUtil.dip2px(context,20);
            }else {
                VIEW_TOP_DRIVING_Y = 30;
            }
        }
    }


    @Override
    public void setView(Context context, View view) {
        initLayout(context,null,null);
        if (view != null) {
            RetainDistanceView distanceView = (RetainDistanceView)view.findViewById(R.id.retain_distance_view);
            mRetainTimeTextView = (TextView)view.findViewById(R.id.prefix_time_textview);
            mRetainTimeScaleTextView = (TextView)view.findViewById(R.id.suffix_time_textview);
            mRetainTimeViewGroup = (ViewGroup)view.findViewById(R.id.retain_time_viewgroup);
            if (distanceView != null) {
                mRetainDistanceView = distanceView;
            }
        }
    }

    @Override
    public void resetView() {
        if (mRetainTimeTextView != null) {
            mRetainTimeTextView.setText("");
            mRetainDistanceView.setRotationX(0);
        }
        updateRetainDistance();
        updateRetainTime();
    }

    @Override
    public void doDraw() {
        super.doDraw();
        updateRetainDistance();
        updateRetainTime();


    }
    public void showHide(boolean show){
        View[] views = new View[]{mRetainTimeTextView,mRetainTimeScaleTextView,mRetainDistanceView};
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

    private void updateRetainTime() {
        if (mRetainTimeTextView != null) {
            int remainTime = 0;
            String tText = "0";
            String tScale = "min";
            if(mNaviInfoBean != null && mCommonBean != null && mCommonBean.isNavingStart() ){
                remainTime = mNaviInfoBean.getPathRetainTime();
            }
            if(remainTime>3*60*60){
                tText = ""+(int)((remainTime/(6*60)))*1.0/10;
                tScale="h";
            }else {
                tText = ""+remainTime/60;
                tScale = "min";
            }
            //min显示
            mRetainTimeTextView.setText(tText);
            if (mRetainTimeScaleTextView != null) {
                mRetainTimeScaleTextView.setText(tScale);
            }


        }
    }

    private void updateRetainDistance() {
        if (mRetainDistanceView != null ) {
            if( mNaviInfoBean != null && mCommonBean != null && mCommonBean.isNavingStart() ){//mCommonBean.isNavingStart()
                mRetainDistanceView.setTotalDistance(mNaviInfoBean.getPathTotalDistance());
                mRetainDistanceView.setRetainDistance(mNaviInfoBean.getPathTotalDistance()-mNaviInfoBean.getPathRetainDistance());//逆向成长
            }else {
                mRetainDistanceView.setTotalDistance(1);
                mRetainDistanceView.setRetainDistance(0);
            }
        }

    }

    @Override
    public void changeDriveState(DriveState state) {
        if (mRetainDistanceView != null) {
            ObjectAnimator animator = null;
            switch (state){
                case DRIVING:
                    animator = ObjectAnimator.ofFloat(mRetainDistanceView, "RotationX", 0, VIEW_ROTATION_DEGREES);
                    animator.setInterpolator(new DecelerateInterpolator(1));
                    animator.setDuration(VIEW_ANIMATION_DURATION);
                    animator.start();

                    animator = ObjectAnimator.ofFloat(mRetainDistanceView, "TranslationY",VIEW_TOP_NOT_DRIVING_Y,VIEW_TOP_DRIVING_Y);
                    animator.setInterpolator(new LinearInterpolator());
                    animator.setDuration(VIEW_ANIMATION_DURATION);
                    animator.start();

                    animator = ObjectAnimator.ofFloat(mRetainTimeViewGroup, "TranslationY",VIEW_TOP_NOT_DRIVING_Y,VIEW_TOP_DRIVING_Y);
                    animator.setInterpolator(new LinearInterpolator());
                    animator.setDuration(VIEW_ANIMATION_DURATION);
                    animator.start();



                    break;
                case PAUSE:
                    animator = ObjectAnimator.ofFloat(mRetainDistanceView, "RotationX",VIEW_ROTATION_DEGREES,0);
                    animator.setInterpolator(new DecelerateInterpolator(1));
                    animator.setDuration(VIEW_ANIMATION_DURATION);
                    animator.start();

                    animator = ObjectAnimator.ofFloat(mRetainDistanceView, "TranslationY",VIEW_TOP_DRIVING_Y, VIEW_TOP_NOT_DRIVING_Y);
                    animator.setInterpolator(new LinearInterpolator());
                    animator.setDuration(VIEW_ANIMATION_DURATION);
                    animator.start();

                    animator = ObjectAnimator.ofFloat(mRetainTimeViewGroup, "TranslationY",VIEW_TOP_DRIVING_Y, VIEW_TOP_NOT_DRIVING_Y);
                    animator.setInterpolator(new LinearInterpolator());
                    animator.setDuration(VIEW_ANIMATION_DURATION);
                    animator.start();

                    break;
                default:
                    break;
            }
        }
        
    }
}
