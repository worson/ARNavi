package com.haloai.hud.hudendpoint.arwaylib.view;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;

/**
 * author       : 龙;
 * date         : 2016/7/6;
 * email        : helong@haloai.com;
 * package_name : demo.opengl.haloai.com.compassdemo;
 * project_name : ComPassDemo;
 */
public class SectorDrawable extends Drawable implements Drawable.Callback {
    private Drawable mDrawable;
    private Path mPath = new Path();
    private float mFromPercent;
    private float mToPercent;

    public SectorDrawable(Drawable drawable) {
        this.mDrawable = drawable;
        if (drawable != null) {
            drawable.setCallback(this);
        }
    }

    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | mDrawable.getChangingConfigurations();
    }

    @Override
    public boolean getPadding(Rect padding) {
        return mDrawable.getPadding(padding);
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        mDrawable.setVisible(visible, restart);
        return super.setVisible(visible, restart);
    }

    @Override
    public void draw(Canvas canvas) {
        mPath.reset();
        RectF rect = new RectF(getBounds());
        double radius = Math.pow(Math.pow(rect.right, 2) + Math.pow(rect.bottom, 2), 0.5);
        mPath.moveTo(rect.right / 2, rect.bottom / 2);
        //mPath.lineTo(rect.right / 2, 0);
        if (mFromPercent > 0.125f) {
            mPath.lineTo(rect.right, 0);
        }
        if (mFromPercent > 0.375f) {
            mPath.lineTo(rect.right, rect.bottom);
        }
        if (mFromPercent > 0.625f) {
            mPath.lineTo(0, rect.bottom);
        }
        if (mFromPercent > 0.875f) {
            mPath.lineTo(0, 0);
        }
        mPath.lineTo((float) (rect.right / 2 + radius * Math.sin(Math.PI * 2 * mFromPercent)),
                     (float) (rect.bottom / 2 - radius * Math.cos(Math.PI * 2 * mFromPercent)));

        if (mToPercent > 0.125f) {
            mPath.lineTo(rect.right, 0);
        }
        if (mToPercent > 0.375f) {
            mPath.lineTo(rect.right, rect.bottom);
        }
        if (mToPercent > 0.625f) {
            mPath.lineTo(0, rect.bottom);
        }
        if (mToPercent > 0.875f) {
            mPath.lineTo(0, 0);
        }
        mPath.lineTo((float) (rect.right / 2 + radius * Math.sin(Math.PI * 2 * mToPercent)),
                     (float) (rect.bottom / 2 - radius * Math.cos(Math.PI * 2 * mToPercent)));
        
        mPath.close();
        if (mFromPercent >= 0 && mFromPercent <= 1) {
            canvas.save();
            canvas.clipPath(mPath);
            mDrawable.draw(canvas);
            canvas.restore();
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mDrawable.setAlpha(alpha);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public int getAlpha() {
        return mDrawable.getAlpha();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mDrawable.setColorFilter(cf);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setTintList(ColorStateList tint) {
        mDrawable.setTintList(tint);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setTintMode(PorterDuff.Mode tintMode) {
        mDrawable.setTintMode(tintMode);
    }

    @Override
    public int getOpacity() {
        // TODO Auto-generated method stub
        return mDrawable.getOpacity();
    }

    @Override
    public boolean isStateful() {
        // TODO Auto-generated method stub
        return mDrawable.isStateful();
    }

    @Override
    protected boolean onStateChange(int[] state) {
        return mDrawable.setState(state);
    }

    @Override
    protected boolean onLevelChange(int level) {
        mDrawable.setLevel(level);
        invalidateSelf();
        return true;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mDrawable.setBounds(bounds);
    }

    @Override
    public int getIntrinsicHeight() {
        return mDrawable.getIntrinsicHeight();
    }

    @Override
    public int getIntrinsicWidth() {
        return mDrawable.getIntrinsicWidth();
    }

    /**
     * 显示的区域范围
     *
     * @param fromParcent 0至1
     */
    public void setPercent(float fromParcent, float toParcent) {
        if (fromParcent > 1) {
            fromParcent = 1;
        } else if (fromParcent < 0) {
            fromParcent = 0;
        }
        if (toParcent > 1) {
            toParcent = 1;
        } else if (toParcent < 0) {
            toParcent = 0;
        }
        if (fromParcent != mFromPercent && toParcent != mToPercent) {
            this.mFromPercent = fromParcent;
            this.mToPercent = toParcent;
            invalidateSelf();
        }
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        final Callback callback = getCallback();
        if (callback != null) {
            callback.invalidateDrawable(this);
        }
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        final Callback callback = getCallback();
        if (callback != null) {
            callback.scheduleDrawable(this, what, when);
        }
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        final Callback callback = getCallback();
        if (callback != null) {
            callback.unscheduleDrawable(this, what);
        }
    }

}