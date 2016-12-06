package com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;
import com.haloai.hud.hudendpoint.arwaylib.draw.IViewOperation;
import com.haloai.hud.hudendpoint.arwaylib.utils.DisplayUtil;

/**
 * Created by wangshengxing on 16/11/2016.
 */

public class ARwayAnimationPresenter extends DrawObject implements IViewOperation{


    private static ARwayAnimationPresenter mPresenter         = new ARwayAnimationPresenter();
    private        ViewGroup               mRoadMaskViewgroup = null;
    private        ImageView               mRoadMaskViewLeft  = null;
    private        ImageView               mRoadMaskViewRight = null;
    private ObjectAnimator                 mRoadMaskAnimator  = null;
    private  Context mContext;

    public static ARwayAnimationPresenter getInstance() {
        return mPresenter;
    }

    @Override
    public View getViewInstance(Context context) {
        return null;
    }

    @Override
    public void setView(Context context, View view) {
        mContext = context;
        if (view != null) {
            mRoadMaskViewgroup = (ViewGroup) view.findViewById(R.id.road_mask_viewgroup);
            mRoadMaskViewLeft = (ImageView) view.findViewById(R.id.road_mask_left);
            mRoadMaskViewRight = (ImageView) view.findViewById(R.id.road_mask_right);

            mRoadMaskViewLeft.setBackgroundColor(Color.BLACK);
            mRoadMaskViewRight.setBackgroundColor(Color.BLACK);

            mRoadMaskViewLeft.setScaleX(3.0f/3);
            mRoadMaskViewRight.setScaleX(3.0f/3);
        }
    }

    @Override
    public void resetView() {
        if (mRoadMaskViewgroup != null) {

        }
    }

    public void onNaviStartAnimation(long duration) {
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


}
