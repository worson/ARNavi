package com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.CommonBean;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.NaviInfoBean;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawViewObject;
import com.haloai.hud.hudendpoint.arwaylib.draw.IDriveStateLister;
import com.haloai.hud.hudendpoint.arwaylib.utils.DisplayUtil;
import com.haloai.hud.hudendpoint.arwaylib.view.ComPassView;
import com.haloai.hud.hudendpoint.arwaylib.view.CompassOutletView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by wangshengxing on 16/7/15.
 */
public class GlDrawCompass extends DrawViewObject implements IDriveStateLister {


    public float VIEW_BOTTOM_ERROR  = 1f;
    public int   ANIMA_WIDTH_SCALE  = (int) (VIEW_DRIVING_WIDTH * 1f / VIEW_NOT_DRIVING_WIDTH);
    public int   ANIMA_HEIGHT_SCALE = (int) (VIEW_DRIVING_HEIGHT * 1f / VIEW_NOT_DRIVING_HEIGHT);

    private static NaviInfoBean mNaviInfoBean = (NaviInfoBean) BeanFactory.getBean(BeanFactory.BeanType.NAVI_INFO);
    private static CommonBean mCommonBean = (CommonBean) BeanFactory.getBean(BeanFactory.BeanType.COMMON);


    private  static GlDrawCompass mGlDrawCompass = new GlDrawCompass();

    public GlDrawCompass() {

    }
    public static GlDrawCompass getInstance() {
        return mGlDrawCompass;
    }

    private  ViewGroup         mComPassViewGroup  = null;
    private  ComPassView       mComPassView       = null;
    private CompassOutletView mCompassOutletView = null;
    private  TextView          mDirectionTextView = null;

    private List<ObjectAnimator> mDrivingStateAnimators = new LinkedList<>();
    private List<ObjectAnimator> mPauseStateAnimators   = new LinkedList<>();

    @Override
    protected void initLayout(Context context, ViewGroup parent, View view) {
        super.initLayout(context, parent, view);
        if (mResources != null) {
            VIEW_DRIVING_WIDTH = (int)(mResources.getDimension(R.dimen.compass_driving_width));
            VIEW_DRIVING_HEIGHT = (int)(mResources.getDimension(R.dimen.compass_driving_height));
            VIEW_NOT_DRIVING_WIDTH = (int)(mResources.getDimension(R.dimen.compass_pause_width));
            VIEW_NOT_DRIVING_HEIGHT = (int)(mResources.getDimension(R.dimen.compass_pause_height));
            if (context != null) {
                VIEW_TOP_DRIVING_Y = DisplayUtil.dip2px(context,50);//上一版50刚好
            }else {
                VIEW_TOP_DRIVING_Y = 30;
            }
//            VIEW_TOP_DRIVING_Y = VIEW_NOT_DRIVING_WIDTH*0.036f;
            VIEW_GOAL_SCALE_X = 0.70f;
            VIEW_GOAL_SCALE_Y = 0.70f;
            VIEW_ROTATION_DEGREES = 50;
        }
    }

    @Override
    public View getViewInstance(Context context) {
        if (mComPassView == null) {
            initLayout(context,null,null);
            mComPassView=new ComPassView(context, VIEW_NOT_DRIVING_WIDTH, VIEW_NOT_DRIVING_HEIGHT);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(VIEW_NOT_DRIVING_WIDTH, VIEW_NOT_DRIVING_HEIGHT);
            mComPassView.setLayoutParams(layoutParams);
//            mComPassView=new ComPassView(context, 300, 300);
        }
        /*ComPassView mComPassView = new ComPassView(mContext, 254, 254);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(254, 254);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mComPassView.setLayoutParams(layoutParams);
        mComPassView.setDestDegree(180 - 90);*/
        return mComPassView;
    }

    @Override
    public void setView(Context context, View view) {
        if (view != null) {
            mComPassView = (ComPassView)view.findViewById(R.id.compass_view);
            mCompassOutletView = (CompassOutletView) view.findViewById(R.id.compass_ouletview);
            mDirectionTextView = (TextView) view.findViewById(R.id.diretion_textview);
            mComPassViewGroup = (ViewGroup) view.findViewById(R.id.compass_viewgroup);
            initLayout(context,null,mComPassView);
            resetView();
//            mComPassView.setY(100);
//            bringToFront();
            mComPassView.addLister(mCompassOutletView);
        }

    }

    @Override
    public void resetView() {
        if (mDirectionTextView != null) {
            mDirectionTextView.setText("");
        }
        if (mComPassView != null) {
            mComPassView.setRotationX(0);
        }
    }
    public void showHide(boolean show) {
        if (mComPassView != null) {
            if(show){
                mComPassView.setAlpha(1);
                mComPassView.setVisibility(View.VISIBLE);
            }else {
                mComPassView.setAlpha(0);
                mComPassView.setVisibility(View.INVISIBLE);
            }

        }
    }

    public void animShowHide(boolean show) {
        View[] views = new View[]{mComPassView};//
        ObjectAnimator animator = null;
        VIEW_ANIMATION_DURATION=1000;
        for (int i = 0; i <views.length ; i++) {
            View v = views[i];
            if (v != null) {
                if (show){
                    v.setVisibility(View.VISIBLE);
                    animator = ObjectAnimator.ofFloat(v, "Alpha", 0,1);
                    animator.setDuration(VIEW_ANIMATION_DURATION);

                }else {
                    animator = ObjectAnimator.ofFloat(v, "Alpha", 1,0);
                    animator.setDuration(VIEW_ANIMATION_DURATION-100);
//                    v.setVisibility(View.INVISIBLE);
                }
                if (animator != null) {
                    animator.setInterpolator(new LinearInterpolator());
                    animator.setRepeatCount(0);
                    animator.start();
                }
            }
        }
    }
    public void changeDriveState(DriveState state) {
        changeDriveState(state,VIEW_ANIMATION_DURATION);
    }


    public void changeDriveState(DriveState state,int duration) {
        if (mViewParent != null) {
            VIEW_PARRENT_WIDTH = mViewParent.getMeasuredWidth();
            VIEW_PARRENT_HEIGHT = mViewParent.getMeasuredHeight();
        }
        View animView = mComPassViewGroup;
        List<ObjectAnimator> animators = null;
        ObjectAnimator animator = null;
        switch (state){
            case DRIVING:
//                animShowHide(false);
//                mComPassView.enableCut(true);
                if (mCompassOutletView != null) {
                    mCompassOutletView.enableSloping(true);
                }
                onNaving();
                animators = mDrivingStateAnimators;
                if (animators != null && animators.size()<1) {
                    animator = ObjectAnimator.ofFloat(animView, "RotationX", 0, VIEW_ROTATION_DEGREES);
                    animator.setInterpolator(new DecelerateInterpolator(1));
                    animator.setDuration((int)(duration*0.66f));
                    animator.setRepeatCount(0);
                    animators.add(animator);

                    animator = ObjectAnimator.ofFloat(animView, "TranslationY", VIEW_TOP_NOT_DRIVING_Y, VIEW_TOP_DRIVING_Y);
                    animator.setInterpolator(new LinearInterpolator());
                    animator.setDuration((int)(duration*0.66f));
                    animators.add(animator);

                    animator = ObjectAnimator.ofFloat(animView, "ScaleX", 1, VIEW_GOAL_SCALE_X);
                    animator.setInterpolator(new LinearInterpolator());
                    animator.setDuration((int)(duration*0.66f));
                    animators.add(animator);

                    animator = ObjectAnimator.ofFloat(animView, "ScaleY", 1, VIEW_GOAL_SCALE_Y);
                    animator.setInterpolator(new LinearInterpolator());
                    animator.setDuration((int)(duration*0.66f));
                    animators.add(animator);
                }



                break;
            case PAUSE:
//                animShowHide(true);
//                mComPassView.enableCut(false);
                if (mCompassOutletView != null) {
                    mCompassOutletView.enableSloping(false);
                }
                animators = mPauseStateAnimators;
                if (animators != null && animators.size()<1) {
                    animator = ObjectAnimator.ofFloat(animView, "RotationX", VIEW_ROTATION_DEGREES, 0);
                    animator.setInterpolator(new DecelerateInterpolator(1));
                    animator.setDuration(duration);
                    animator.setRepeatCount(0);
                    animators.add(animator);

                    animator = ObjectAnimator.ofFloat(animView, "TranslationY",VIEW_TOP_DRIVING_Y, VIEW_TOP_NOT_DRIVING_Y);
                    animator.setInterpolator(new LinearInterpolator());
                    animator.setDuration(duration);
                    animators.add(animator);

                    animator = ObjectAnimator.ofFloat(animView, "ScaleX", VIEW_GOAL_SCALE_X, 1);
                    animator.setInterpolator(new LinearInterpolator());
                    animator.setDuration(duration);
                    animators.add(animator);

                    animator = ObjectAnimator.ofFloat(animView, "ScaleY", VIEW_GOAL_SCALE_Y, 1);
                    animator.setInterpolator(new LinearInterpolator());
                    animator.setDuration(duration);
                    animators.add(animator);
                }

                break;
            default:
                break;
        }
        if (animators != null) {
            for (ObjectAnimator a: animators){
                if(a.isStarted()){
                    a.cancel();
                }
                a.setDuration(duration);
                a.start();
            }
        }
    }

    @Override
    public void doDraw() {
        super.doDraw();
        if(!mCommonBean.isStartOk()){
            String direction = parseDirection();
            updateDirectionView(direction);
        }
    }

    private String parseDirection(){
        if(mNaviInfoBean != null){
            String text = mNaviInfoBean.getNaviText();
            String[] keywords = new String[]{"东","南","西","北"};
            String[] actionwords = new String[]{"往","向"};
            return text;
        }else {
            return null;
        }

    }

    private void updateDirectionView(String text) {
        if (mDirectionTextView != null) {
            if (text != null) {
                String direction = null;
                if(text.contains("向东")){
                    direction = "东";
                    mComPassView.setDestDegree(0);
                }else if(text.contains("向南")){
                    direction = "南";
                    mComPassView.setDestDegree(90);
                }else if(text.contains("向西")){
                    direction = "西";
                    mComPassView.setDestDegree(180);
                }else if(text.contains("向北")){
                    direction = "北";
                    mComPassView.setDestDegree(270);
                }else {
                    mComPassView.setDestDegree(270);
                }
                if (direction != null) {
                    mDirectionTextView.setText("向"+direction+"\n行驶");
                }
            }


        }
    }

    /**
     * 导航开始
     */
    public void onNaviStart() {
        if (mDirectionTextView != null) {
            mDirectionTextView.setVisibility(View.VISIBLE);
            mDirectionTextView.setAlpha(1);
        }
        if (mComPassView != null) {
            mComPassView.enableDirectionArrow(true);
        }
    }

    /**
     * 起步成功
     */
    private void onNaving(){
        if (mComPassView != null) {
            mComPassView.enableDirectionArrow(false);
        }
        if (mDirectionTextView != null) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mDirectionTextView, "Alpha", 1, 0);
            animator.setInterpolator(new DecelerateInterpolator(1));
            animator.setDuration(VIEW_ANIMATION_DURATION);
            animator.setRepeatCount(0);
            animator.start();
        }
    }
}
