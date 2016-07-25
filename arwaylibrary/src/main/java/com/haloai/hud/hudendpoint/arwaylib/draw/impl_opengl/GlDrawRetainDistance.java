package com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.amap.api.maps.model.Text;
import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.NaviInfoBean;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawViewObject;
import com.haloai.hud.hudendpoint.arwaylib.draw.IDriveStateLister;
import com.haloai.hud.hudendpoint.arwaylib.view.RetainDistanceView;

/**
 * Created by wangshengxing on 16/7/15.
 */
public class GlDrawRetainDistance extends DrawViewObject implements IDriveStateLister {
    private static GlDrawRetainDistance mGlDrawRetainDistance = new GlDrawRetainDistance();

    private RetainDistanceView mRetainDistanceView = null;

    //bean
    private static NaviInfoBean mNaviInfoBean = (NaviInfoBean) BeanFactory.getBean(BeanFactory.BeanType.NAVI_INFO);

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
        }
    }

    private TextView mRetainTimeTextView = null;
    private TextView mRetainTimeScaleTextView = null;
    @Override
    public void setView(Context context, View view) {
        RetainDistanceView distanceView = (RetainDistanceView)view.findViewById(R.id.retain_distance_view);
        mRetainTimeTextView = (TextView)view.findViewById(R.id.prefix_time_textview);
        mRetainTimeScaleTextView = (TextView)view.findViewById(R.id.suffix_time_textview);
        if (distanceView != null) {
            mRetainDistanceView = distanceView;
        }
    }

    @Override
    public void resetView() {
        if (mRetainTimeTextView != null) {
            mRetainTimeTextView.setText("");
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
            int remainTime = mNaviInfoBean.getPathRetainTime();
            //min显示
            mRetainTimeTextView.setText(""+(int)((remainTime/6))*1.0/10);
        }
    }

    private void updateRetainDistance() {
        if (mRetainDistanceView != null && mNaviInfoBean != null) {
            mRetainDistanceView.setTotalDistance(mNaviInfoBean.getPathTotalDistance());
            mRetainDistanceView.setRetainDistance(mNaviInfoBean.getPathRetainDistance());
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
                    animator.setRepeatCount(0);
                    animator.start();
                    break;
                case PAUSE:
                    animator = ObjectAnimator.ofFloat(mRetainDistanceView, "RotationX",VIEW_ROTATION_DEGREES,0);
                    animator.setInterpolator(new DecelerateInterpolator(1));
                    animator.setDuration(VIEW_ANIMATION_DURATION);
                    animator.setRepeatCount(0);
                    animator.start();

                    break;
                default:
                    break;
            }
        }
        
    }
}
