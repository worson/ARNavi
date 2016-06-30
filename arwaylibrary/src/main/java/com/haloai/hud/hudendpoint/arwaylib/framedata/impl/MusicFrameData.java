package com.haloai.hud.hudendpoint.arwaylib.framedata.impl;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Rect;

import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.MusicBean;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.AlphaResult;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.PositionResult;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.RotateResult;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.ScaleResult;
import com.haloai.hud.hudendpoint.arwaylib.framedata.SuperFrameData;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.framedata.impl;
 * project_name : hudlauncher;
 */
public class MusicFrameData extends SuperFrameData {
    private static final float IMAGE_WIDTH  = 100;
    private static final float IMAGE_HEIGHT = 100;
    private final static int   X            = 100;
    private final static int   Y            = 100;

    private Rect   mDrawRect         = new Rect(100, 100, 200, 200);
    private String mMusicName        = "";
    private double mWidthScala       = 1f;
    private double mHeightScala      = 1f;
    //这两个变量用于保存在设置position时的小数部分,如果不保存会直接被剪掉,导致误差越来越多.
    private double mWidthFloatValue  = 0f;
    private double mHeightFloatValue = 0f;
    //0-1f
    private float  mAlpha            = 1f;
    private double mRotateX          = 0f;
    private double mRotateY          = 0f;
    private double mRotateZ          = 0f;


    private static MusicFrameData mMusicFrameData = new MusicFrameData();

    private MusicFrameData() {
        setPosition(X, Y);
        setAnimStartPosition(X, Y);
    }

    public static MusicFrameData getInstance() {
        return mMusicFrameData;
    }

    public void setImageWithMusicStatus(Context context,MusicBean.MusicStatus musicStatus) {
        switch (musicStatus) {
            case START:
                setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.red));
                break;
            case PREV:
                setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.greenline_music_prev));
                break;
            case NEXT:
                setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.greenline_music_next));
                break;
            case PAUSE:
                setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.greenline_music_pause));
                break;
            case STOP:
                setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.greenline_music_play));
                break;
            case PLAYING:
                setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.greenline_music_play));
                break;
            case UNPLAYING:
                setImage(null);
                break;
        }
    }

    /**
     * scalaResult.mOffsetPosition为本次动画相对于上次动画结束位置的偏移量
     * 而不是本次动画相对于最开始时候的偏移量
     *
     * @param scalaResult scala变换后的结果
     */
    public void updateWithScala(ScaleResult scalaResult) {
        this.mWidthScala += scalaResult.mOffsetWidthScala;
        this.mHeightScala += scalaResult.mOffsetHeightScala;

        double offsetX = this.mPosition.x - scalaResult.mOffsetWidthScala * IMAGE_WIDTH / 2 + this.mWidthFloatValue;
        this.mWidthFloatValue = offsetX - (int) offsetX;
        this.mPosition.x = (int) offsetX;
        double offsetY = this.mPosition.y - scalaResult.mOffsetHeightScala * IMAGE_HEIGHT / 2 + this.mHeightFloatValue;
        this.mHeightFloatValue = offsetY - (int) offsetY;
        this.mPosition.y = (int) offsetY;
        this.mDrawRect.set(mPosition.x, mPosition.y, (int) (mPosition.x + IMAGE_WIDTH + ((this.mWidthScala - 1) * IMAGE_WIDTH)),
                           (int) (mPosition.y + IMAGE_HEIGHT + ((this.mHeightScala - 1) * IMAGE_HEIGHT)));
    }

    public void updateWithPosition(PositionResult positionResult) {
        this.mPosition.x += positionResult.mOffsetPosition.x;
        this.mPosition.y += positionResult.mOffsetPosition.y;
        this.mDrawRect.set(mPosition.x, mPosition.y, (int) (mPosition.x + IMAGE_WIDTH + ((this.mWidthScala - 1) * IMAGE_WIDTH)),
                           (int) (mPosition.y + IMAGE_HEIGHT + ((this.mHeightScala - 1) * IMAGE_HEIGHT)));
    }

    public void updateWithAlpha(AlphaResult alphaResult) {
        this.mAlpha += alphaResult.mOffsetAlpha;
        //如果alpha值大于1,那么paint.setAlpha方法将会有意外情况发生.
        //因此此处需要将该值限定在1以内.
        this.mAlpha = this.mAlpha>1?1:this.mAlpha;
        //0-255
        this.mPaint.setAlpha((int) (this.mAlpha * 255));
    }

    public void updateWithRotate(RotateResult rotateResult) {
        this.mRotateX += rotateResult.mOffsetRotateX;
        this.mRotateY += rotateResult.mOffsetRotateY;
        this.mRotateZ += rotateResult.mOffsetRotateZ;

        Camera camera = new Camera();
        camera.save();
        camera.rotateX((float) this.mRotateX);
        camera.rotateY((float) this.mRotateY);
        camera.rotateZ((float) this.mRotateZ);
        camera.getMatrix(this.mMatrix);
        camera.restore();
        float centerX = (float) (this.mPosition.x + (this.mWidthScala * IMAGE_WIDTH) / 2);
        float centerY = (float) (this.mPosition.y + (this.mHeightScala * IMAGE_HEIGHT) / 2);
        this.mMatrix.preTranslate(-centerX, -centerY);
        this.mMatrix.postTranslate(centerX, centerY);
    }

    public Rect getDrawRect() {
        return mDrawRect;
    }

    public void setMusicName(String musicName) {
        this.mMusicName = musicName;
    }

    public String getMusicName() {
        return this.mMusicName;
    }

    public double getWidthScala() {
        return this.mWidthScala;
    }

    public double getHeightScala() {
        return this.mHeightScala;
    }

    @Override
    public void animOver() {
        this.mAnimStartPosition.set(this.mPosition.x, this.mPosition.y);
        this.mWidthScala = 1f;
        this.mHeightScala = 1f;
        this.mAlpha = 1f;
        this.mRotateX = 0f;
        this.mRotateY = 0f;
        this.mRotateZ = 0f;
    }

    public float getAlpha() {
        return this.mAlpha;
    }
}
