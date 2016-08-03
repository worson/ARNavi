package com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.SpeedBean;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawViewObject;
import com.haloai.hud.hudendpoint.arwaylib.draw.IDriveStateLister;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.hudendpoint.arwaylib.utils.DisplayUtil;
import com.haloai.hud.hudendpoint.arwaylib.view.SpeedView;
import com.haloai.hud.utils.HaloLogger;

/**
 * Created by wangshengxing on 16/7/15.
 */
public class GlDrawSpeedDial extends DrawViewObject implements IDriveStateLister{
    private static GlDrawSpeedDial mGlDrawSpeedDial = new GlDrawSpeedDial();

    //view
    private SpeedView mSpeedView = null;
    private TextView mSpeedValueView = null;
    private TextView mSpeedScaleView = null;

    private ViewGroup mDigitalSpeedViewgroup=null;
    private ImageView mSpeedNumHun;
    private ImageView mSpeedNumTen;
    private ImageView mSpeedNumOne;
    private int[] mSpeedNumImg={R.drawable.speed_number_0,R.drawable.speed_number_1,R.drawable.speed_number_2,
            R.drawable.speed_number_3,R.drawable.speed_number_4,R.drawable.speed_number_5,R.drawable.speed_number_6,
            R.drawable.speed_number_7,R.drawable.speed_number_8,R.drawable.speed_number_9};

    //bean
    private static SpeedBean mSpeedBean = (SpeedBean) BeanFactory.getBean(BeanFactory.BeanType.SPEED);

    public GlDrawSpeedDial() {
        mSpeedBean = (SpeedBean) BeanFactory.getBean(BeanFactory.BeanType.SPEED);
    }

    public static GlDrawSpeedDial getInstance() {
        return mGlDrawSpeedDial;
    }

    @Override
    public View getViewInstance(Context context) {
        if (mSpeedView == null) {
            mSpeedView = new SpeedView(context, 118, 118);
        }
        return mSpeedView;
    }

    public void updateSpeedText(int speed){
        /*if (mSpeedValueView != null) {
            mSpeedValueView.setText(speed+"");
        }*/
        if (mSpeedView != null) {
            mSpeedView.setSpeed(speed);
        }
    }

    public void updateSpeed(int speed){
        if(mSpeedNumHun==null || mSpeedNumTen==null ||mSpeedNumOne==null){
            HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG,"updateSpeed view is null");
            return;
        }
        mSpeedNumHun.setVisibility(View.INVISIBLE);
        mSpeedNumTen.setVisibility(View.INVISIBLE);
        int one=speed%10;
        mSpeedNumOne.setImageResource(mSpeedNumImg[one]);
        if((speed/10)!=0){
            mSpeedNumTen.setVisibility(View.VISIBLE);
            int ten=(speed/10)%10;
            mSpeedNumTen.setImageResource(mSpeedNumImg[ten]);
            if((speed/100)!=0){
                mSpeedNumHun.setVisibility(View.VISIBLE);
                int hun=(speed/100)%10;
                mSpeedNumHun.setImageResource(mSpeedNumImg[hun]);
            }
        }
    }

    @Override
    public void setView(Context context, View view) {
        if (view != null) {
            mSpeedView = (SpeedView)view.findViewById(R.id.speed_view);
            mDigitalSpeedViewgroup = (ViewGroup) view.findViewById(R.id.digital_speed_viewgroup);
            initLayout(context,null,mSpeedView);
            mSpeedNumHun=(ImageView)view.findViewById(R.id.speed_num_hun);
            mSpeedNumTen=(ImageView)view.findViewById(R.id.speed_num_ten);
            mSpeedNumOne=(ImageView)view.findViewById(R.id.speed_num_one);
        }
    }

    @Override
    public void resetView() {
        if (mSpeedView != null) {
            mSpeedView.setRotationX(0);
        }
        updateSpeed(0);
        updateSpeedDisplay();
    }

    @Override
    public void doDraw() {
        super.doDraw();
        updateSpeedDisplay();
    }



    private void updateSpeedDisplay() {
        if (mSpeedBean != null) {
            int speed = mSpeedBean.getSpeed();
//            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG,"updateSpeedDisplay is ok ,speed is "+speed);
            updateSpeed(speed);
            updateSpeedText(speed);
        }else {
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG,"updateSpeedDisplay is null !");
        }
    }

    public void showHide(boolean show){
        View[] views = new View[]{mSpeedView,mSpeedValueView,mSpeedScaleView,mDigitalSpeedViewgroup};
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
    protected void initLayout(Context context, ViewGroup parent, View view) {
        super.initLayout(context, parent, view);
        if (mResources != null) {
            VIEW_DRIVING_WIDTH = (int)(mResources.getDimension(R.dimen.speed_view_driving_width));
            VIEW_DRIVING_HEIGHT = (int)(mResources.getDimension(R.dimen.speed_view_driving_height));
            VIEW_NOT_DRIVING_WIDTH = (int)(mResources.getDimension(R.dimen.speed_view_pause_width));
            VIEW_NOT_DRIVING_HEIGHT = (int)(mResources.getDimension(R.dimen.speed_view_pause_height));

            VIEW_TOP_NOT_DRIVING_Y = 0;
            if (context != null) {
                VIEW_TOP_DRIVING_Y = DisplayUtil.dip2px(context,20);
            }else {
                VIEW_TOP_DRIVING_Y = 30;
            }

        }
    }
    

    @Override
    public void changeDriveState(IDriveStateLister.DriveState state) {
        if (mViewParent != null) {
            VIEW_PARRENT_WIDTH = mViewParent.getMeasuredWidth();
            VIEW_PARRENT_HEIGHT = mViewParent.getMeasuredHeight();
        }

        switch (state){
            case DRIVING:
                if (mSpeedView != null) {
                    ObjectAnimator animator = null;

                    animator = ObjectAnimator.ofFloat(mSpeedView, "RotationX", 0, VIEW_ROTATION_DEGREES);
                    animator.setInterpolator(new DecelerateInterpolator(1));
                    animator.setDuration(VIEW_ANIMATION_DURATION);
                    animator.start();

                    animator = ObjectAnimator.ofFloat(mSpeedView, "TranslationY", VIEW_TOP_NOT_DRIVING_Y, VIEW_TOP_DRIVING_Y);
                    animator.setInterpolator(new LinearInterpolator());
                    animator.setDuration(VIEW_ANIMATION_DURATION);
                    animator.start();

                    animator = ObjectAnimator.ofFloat(mDigitalSpeedViewgroup, "TranslationY", VIEW_TOP_NOT_DRIVING_Y, VIEW_TOP_DRIVING_Y);
                    animator.setInterpolator(new LinearInterpolator());
                    animator.setDuration(VIEW_ANIMATION_DURATION);
                    animator.start();

                }
                break;
            case PAUSE:
                if (mSpeedView != null) {
                    ObjectAnimator animator = null;

                    animator = ObjectAnimator.ofFloat(mSpeedView, "RotationX", VIEW_ROTATION_DEGREES,0);
                    animator.setInterpolator(new DecelerateInterpolator(4));
                    animator.setDuration(VIEW_ANIMATION_DURATION);
                    animator.start();

                    animator = ObjectAnimator.ofFloat(mSpeedView, "TranslationY",VIEW_TOP_DRIVING_Y, VIEW_TOP_NOT_DRIVING_Y);
                    animator.setInterpolator(new LinearInterpolator());
                    animator.setDuration(VIEW_ANIMATION_DURATION);
                    animator.start();

                    animator = ObjectAnimator.ofFloat(mDigitalSpeedViewgroup, "TranslationY",VIEW_TOP_DRIVING_Y, VIEW_TOP_NOT_DRIVING_Y);
                    animator.setInterpolator(new LinearInterpolator());
                    animator.setDuration(VIEW_ANIMATION_DURATION);
                    animator.start();

                }
                break;
            default:
                break;
        }
    }

}
