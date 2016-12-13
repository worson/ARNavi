package com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;
import com.haloai.hud.hudendpoint.arwaylib.draw.IViewOperation;

import org.rajawali3d.view.TextureView;

/**
 * Created by wangshengxing on 16/7/10.
 */
public class DrawScene extends DrawObject implements IViewOperation{

    private ViewGroup mGlViewgroup = null;
    private static DrawScene       mDrawScene       = new DrawScene();
    private float mLastArwayAlpha = 1;

    public DrawScene() {
    }

    public static DrawScene getInstance() {
        return mDrawScene;
    }

    private TextureView mTextureView;

    @Override
    public View getViewInstance(Context context) {
        if (mTextureView == null) {
            mTextureView = new TextureView(context);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mTextureView.setLayoutParams(params);
            mTextureView.setBackgroundColor(Color.TRANSPARENT);
            mTextureView.setAlpha(mLastArwayAlpha);
        }
        return mTextureView;
    }

    @Override
    public void setView(Context context, View view) {
        if (view != null) {
            mGlViewgroup = (ViewGroup)view.findViewById(R.id.opengl_viewgroup);
//            mTextureView =(TextureView) view.findViewById(R.id.rajwali_surface);
        }

    }

    /**
     * 设置的alpha值,被用来做动画时采用
     * @param alpha
     */
    public void setAlpha(float alpha){
        mLastArwayAlpha=alpha;
        if (mTextureView != null) {
            mTextureView.setAlpha(mLastArwayAlpha);
        }
    }

    /**
     * 设置临时的alpha值
     * @param alpha
     */
    public void setTempAlpha(float alpha){
        if (mTextureView != null) {
            mTextureView.setAlpha(alpha);
        }
    }
    @Override
    public void resetView() {

    }

    public void animShowHide(boolean show) {
        animShowHide(show,1000);
    }
    public void animShowHide(boolean show,long duration) {
        View[] views = new View[]{mTextureView};//
        ObjectAnimator animator = null;
        for (int i = 0; i <views.length ; i++) {
            View v = views[i];
            if (v != null) {
                if (show){
                    if(!v.isShown()){
                        v.setVisibility(View.VISIBLE);
                    }
                    animator = ObjectAnimator.ofFloat(v, "Alpha", 0,mLastArwayAlpha);
                    animator.setDuration(duration);
                }else {
                    animator = ObjectAnimator.ofFloat(v, "Alpha", mLastArwayAlpha,0);
                    animator.setDuration(duration);
                }
                if (animator != null) {
                    animator.setInterpolator(new LinearInterpolator());
                    animator.setRepeatCount(0);
                    animator.start();
                }
            }
        }
    }

    public void showHide(boolean show) {
        View[] views = new View[]{mGlViewgroup,mTextureView};//
        for (int i = 0; i <views.length ; i++) {
            View v = views[i];
            if (v != null) {
                if (show){
                    v.setVisibility(View.VISIBLE);
                }else {
                    v.setVisibility(View.INVISIBLE);
                }
                v.invalidate();

            }
        }
    }
    public boolean isShow(){

        return mTextureView.isShown() && mTextureView.getAlpha()>0;
    }

}
