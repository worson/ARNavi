package com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.NaviInfoBean;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawViewObject;
import com.haloai.hud.hudendpoint.arwaylib.draw.IDriveStateLister;
import com.haloai.hud.hudendpoint.arwaylib.view.ComPassView;

/**
 * Created by wangshengxing on 16/7/15.
 */
public class GlDrawCompass extends DrawViewObject implements IDriveStateLister {


    public float VIEW_BOTTOM_ERROR  = 1f;
    public int   ANIMA_WIDTH_SCALE  = (int) (VIEW_DRIVING_WIDTH * 1f / VIEW_NOT_DRIVING_WIDTH);
    public int   ANIMA_HEIGHT_SCALE = (int) (VIEW_DRIVING_HEIGHT * 1f / VIEW_NOT_DRIVING_HEIGHT);

    private static NaviInfoBean mNaviInfoBean = (NaviInfoBean) BeanFactory.getBean(BeanFactory.BeanType.NAVI_INFO);


    private  static GlDrawCompass mGlDrawCompass = new GlDrawCompass();

    public GlDrawCompass() {

    }
    public static GlDrawCompass getInstance() {
        return mGlDrawCompass;
    }

    public ViewGroup   mComPassViewGroup  = null;
    public ComPassView mComPassView       = null;
    public TextView    mDirectionTextView = null;

    @Override
    protected void initLayout(Context context, ViewGroup parent, View view) {
        super.initLayout(context, parent, view);
        if (mResources != null) {
            VIEW_DRIVING_WIDTH = (int)(mResources.getDimension(R.dimen.compass_driving_width));
            VIEW_DRIVING_HEIGHT = (int)(mResources.getDimension(R.dimen.compass_driving_height));
            VIEW_NOT_DRIVING_WIDTH = (int)(mResources.getDimension(R.dimen.compass_pause_width));
            VIEW_NOT_DRIVING_HEIGHT = (int)(mResources.getDimension(R.dimen.compass_pause_height));
            VIEW_TOP_NOT_DRIVING_Y = 0;
            VIEW_TOP_DRIVING_Y = 10;
//            VIEW_TOP_DRIVING_Y = VIEW_NOT_DRIVING_WIDTH*0.036f;
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
            mDirectionTextView = (TextView) view.findViewById(R.id.diretion_textview);
//            mComPassViewGroup = (ViewGroup) view.findViewById(R.id.compass_viewgroup);
            initLayout(context,null,mComPassView);
            resetView();
//            mComPassView.setY(100);
//            bringToFront();
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
                    animator.setDuration(VIEW_ANIMATION_DURATION-50);
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
    @Override
    public void changeDriveState(DriveState state) {
        ObjectAnimator animator = null;
        if (mViewParent != null) {
            VIEW_PARRENT_WIDTH = mViewParent.getMeasuredWidth();
            VIEW_PARRENT_HEIGHT = mViewParent.getMeasuredHeight();
        }
        switch (state){
            case DRIVING:
                showHide(false);
                animator = ObjectAnimator.ofFloat(mComPassView, "RotationX", 0, VIEW_ROTATION_DEGREES);
                animator.setInterpolator(new DecelerateInterpolator(1));
                animator.setDuration(VIEW_ANIMATION_DURATION);
                animator.setRepeatCount(0);
                animator.start();

                break;
            case PAUSE:
                showHide(true);
                animator = ObjectAnimator.ofFloat(mComPassView, "RotationX", VIEW_ROTATION_DEGREES, 0);
                animator.setInterpolator(new DecelerateInterpolator(1));
                animator.setDuration(VIEW_ANIMATION_DURATION);
                animator.setRepeatCount(0);
                animator.start();
                break;
            default:
                break;
        }
    }

//    public void changeDriveState(DriveState state) {
//        if (mViewParent != null) {
//            VIEW_PARRENT_WIDTH = mViewParent.getMeasuredWidth();
//            VIEW_PARRENT_HEIGHT = mViewParent.getMeasuredHeight();
//        }
//
//        AnimationSet animationSet = new AnimationSet(true);
//        switch (state){
//            case DRIVING:
//                showHide(false);
//                if (mComPassView != null) {
//                    ObjectAnimator animator = null;
//
//                    animationSet.setDuration(VIEW_ANIMATION_DURATION);
//
//                    animator = ObjectAnimator.ofFloat(mComPassView,"ScaleY",1,0.5f);
//                    animator.setDuration(1000);
//                    animator.setRepeatCount(0);
////                    animator.start();
//
//                    animator = ObjectAnimator.ofFloat(mComPassView,"ScaleX",1,0.754f);
//                    animator.setDuration(VIEW_ANIMATION_DURATION/4);
//                    animator.setInterpolator(new DecelerateInterpolator(4));
//                    animator.setRepeatCount(0);
////                    animator.start();
//
//                    animator = ObjectAnimator.ofFloat(mComPassView, "RotationX", 0, VIEW_ROTATION_DEGREES);
//                    animator.setInterpolator(new DecelerateInterpolator(1));
//                    animator.setDuration(VIEW_ANIMATION_DURATION);
//                    animator.setRepeatCount(0);
//                    animator.start();
//
//                    animator = ObjectAnimator.ofFloat(mDirectionTextView, "Alpha", 1,0);
//                    animator.setInterpolator(new DecelerateInterpolator(1));
//                    animator.setDuration(VIEW_ANIMATION_DURATION/2);
//                    animator.setRepeatCount(0);
//                    animator.start();
//
//                    mComPassView.enableCut(true);
//                    mComPassView.setCut(60);
//
//                    animator = ObjectAnimator.ofFloat(mDirectionTextView, "Cut", -180,60);
//                    animator.setInterpolator(new LinearInterpolator());
//                    animator.setDuration(VIEW_ANIMATION_DURATION/2);
//                    animator.setRepeatCount(0);
////                    animator.start();
//
//                    /*animator = ObjectAnimator.ofFloat(mComPassView, "X", 0, 50);
//                    animator.setDuration(1000);
//                    animator.setRepeatCount(0);
//                    animator.start();*/
//                    Log.e("compassdraw", VIEW_PARRENT_HEIGHT +",   "+ VIEW_NOT_DRIVING_HEIGHT +",   "+ VIEW_DRIVING_HEIGHT);
//                    if (mComPassView != null) {
////                        animator = ObjectAnimator.ofFloat(mComPassView, "Y", VIEW_TOP_NOT_DRIVING_Y, VIEW_TOP_DRIVING_Y);
//                        animator = ObjectAnimator.ofFloat(mComPassView, "TranslationY", VIEW_TOP_NOT_DRIVING_Y, VIEW_TOP_DRIVING_Y);
////                        animator = ObjectAnimator.ofFloat(mComPassView, "TranslationY", 40);
////                        animator = ObjectAnimator.ofFloat(mComPassView, "TranslationY",10);
//                        animator.setInterpolator(new LinearInterpolator());
//                        animator.setDuration(300);
//                        animator.setRepeatCount(0);
////                        animator.start();
//                    }
//                }
//                break;
//            case PAUSE:
//                showHide(true);
//                if (mComPassView != null) {
//                    ObjectAnimator animator = null;
//
//                    animator = ObjectAnimator.ofFloat(mComPassView,"ScaleY",0.5f,1);
//                    animator.setDuration(500);
//                    animator.setRepeatCount(0);
////                    animator.start();
//
//                    animator = ObjectAnimator.ofFloat(mComPassView,"ScaleX",0.754f,1);
//                    animator.setDuration(500);
//                    animator.setRepeatCount(0);
////                    animator.start();
//
//                    animator = ObjectAnimator.ofFloat(mComPassView, "RotationX", VIEW_ROTATION_DEGREES,0);
//                    animator.setInterpolator(new DecelerateInterpolator(4));
//                    animator.setDuration(500);
//                    animator.setRepeatCount(0);
//                    animator.start();
//
//                    animator = ObjectAnimator.ofFloat(mDirectionTextView, "Alpha",0,1);
//                    animator.setInterpolator(new DecelerateInterpolator(1));
//                    animator.setDuration(VIEW_ANIMATION_DURATION);
//                    animator.setRepeatCount(0);
//                    animator.start();
//
//                    mComPassView.enableCut(false);
////                    mComPassView.setCut(60);
//                    if (mViewParent != null) {
////                        animator = ObjectAnimator.ofFloat(mComPassView, "Y",VIEW_TOP_DRIVING_Y, VIEW_TOP_NOT_DRIVING_Y);
//                        animator = ObjectAnimator.ofFloat(mComPassView, "TranslationY", VIEW_TOP_NOT_DRIVING_Y, -VIEW_TOP_DRIVING_Y);
////                        animator = ObjectAnimator.ofFloat(mComPassView, "TranslationY",VIEW_TOP_DRIVING_Y, VIEW_TOP_NOT_DRIVING_Y);
////                        animator = ObjectAnimator.ofFloat(mComPassView, "TranslationY",-40);
//                        animator.setInterpolator(new LinearInterpolator());
////                        mComPassView.setTranslationY();
//                        animator.setDuration(300);
//                        animator.setRepeatCount(0);
////                        animator.start();
//                    }
//                }
//                break;
//            default:
//                break;
//        }
//    }

    @Override
    public void doDraw() {
        super.doDraw();
        updateDirectionView();
    }

    private void updateDirectionView() {
        if (mDirectionTextView != null && mNaviInfoBean != null) {
            String text = mNaviInfoBean.getNaviText();
            String[] keywords = new String[]{"东","南","西","北"};
            String[] actionwords = new String[]{"往","向"};

        }
    }


    public void showInstant(boolean show) {
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
}
