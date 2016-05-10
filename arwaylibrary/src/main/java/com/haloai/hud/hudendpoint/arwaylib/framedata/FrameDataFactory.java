package com.haloai.hud.hudendpoint.arwaylib.framedata;

import android.content.Context;
import android.graphics.BitmapFactory;

import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl.MusicBean;
import com.haloai.hud.hudendpoint.arwaylib.calculator.CalculatorFactory;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.ScaleFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.AlphaCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.PositionCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.RotateCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.RouteCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.ScaleCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.ScaleResult;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl.MusicFrameData;

/**
 * author       : 龙;
 * date         : 2016/5/5;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.framedata;
 * project_name : hudlauncher;
 */
public class FrameDataFactory {
    //calculator
    private static ScaleCalculator    mScalaCalculator    = (ScaleCalculator) CalculatorFactory.getCalculator(CalculatorFactory.CalculatorType.SCALA);
    private static PositionCalculator mPositionCalculator = (PositionCalculator) CalculatorFactory.getCalculator(CalculatorFactory.CalculatorType.POSITION);
    private static AlphaCalculator    mAlphaCalculator    = (AlphaCalculator) CalculatorFactory.getCalculator(CalculatorFactory.CalculatorType.ALPHA);
    private static RotateCalculator   mRotateCalculator   = (RotateCalculator) CalculatorFactory.getCalculator(CalculatorFactory.CalculatorType.ROTATE);
    private static RouteCalculator    mRouteCalculator    = (RouteCalculator) CalculatorFactory.getCalculator(CalculatorFactory.CalculatorType.ROUTE);

    public enum FrameDataType {
        CROSS_IMAGE,
        EXIT,
        MUSIC,
        NETWORK,
        NEXT_ROAD_NAME,
        ROUTE,
        SATELLITE,
        TURN_INFO
    }

    public static SuperFrameData getFrameDataForUpdate(FrameDataType frameDataType){
        SuperFrameData frameData = null;
        switch (frameDataType) {
            case CROSS_IMAGE:

                break;
            case EXIT:

                break;
            case MUSIC:
                frameData = MusicFrameData.getInstance();
                break;
            case NETWORK:

                break;
            case NEXT_ROAD_NAME:

                break;
            case ROUTE:

                break;
            case SATELLITE:

                break;
            case TURN_INFO:

                break;
            default:
                break;
        }
        return frameData;
    }

    public static SuperFrameData getFrameDataForDraw(Context context, FrameDataType frameDataType) {
        SuperFrameData frameData = null;
        switch (frameDataType) {
            case CROSS_IMAGE:

                break;
            case EXIT:

                break;
            case MUSIC:
                frameData = MusicFrameData.getInstance();
                updateMusic(context, frameData);
                break;
            case NEXT_ROAD_NAME:

                break;
            case ROUTE:

                break;
            case TURN_INFO:

                break;
            case SATELLITE:

            break;
            case NETWORK:

            break;
            default:
                break;
        }
        return frameData;
    }

    private static void updateMusic(Context context, SuperFrameData frameData) {
        MusicBean musicBean = (MusicBean) BeanFactory.getBean(BeanFactory.BeanType.MUSIC);
        MusicFrameData musicFrameData = (MusicFrameData) frameData;
        musicFrameData.setMusicName(musicBean.getMusicName());
        boolean updateMusicFrameData = true;
        switch (musicBean.getMusicStatus()) {
            case START:
                musicFrameData.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.red));
                break;
            case PREV:
                musicFrameData.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.greenline_music_prev));
                break;
            case NEXT:
                musicFrameData.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.greenline_music_next));
                break;
            case PAUSE:
                musicFrameData.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.greenline_music_pause));
                break;
            case STOP:
                musicFrameData.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.greenline_music_play));
                break;
            case PLAYING:
                musicFrameData.setImage(BitmapFactory.decodeResource(context.getResources(), R.drawable.red));
                updateMusicFrameData = false;
                break;
            case UNPLAYING:
                musicFrameData.setImage(null);
                updateMusicFrameData = false;
                break;
        }

        if (updateMusicFrameData) {

            /*//position animation
            PositionFactor positionFactor = new PositionFactor(
                    musicBean.getStartTime(), musicBean.getLastTime(), musicBean.getDuration(), musicFrameData.getPosition(), new Point(200, 200),null, false);
            PositionResult positionResult = mPositionCalculator.calculate(positionFactor);
            musicFrameData.updateWithPosition(positionResult);*/

            //scala animation
            ScaleFactor scalaFactor = new ScaleFactor(
                    musicBean.getStartTime(),musicBean.getLastTime(), musicBean.getDuration(), 1f, 2f,musicFrameData.getScala(), musicFrameData.getPosition(), false);
            ScaleResult scalaResult = mScalaCalculator.calculate(scalaFactor);
            musicFrameData.updateWithScala(scalaResult);

            //if animation is over,set the music current status with playing or unplaying.
            if (scalaResult.mIsOver) {
                if (musicBean.getMusicStatus() == MusicBean.MusicStatus.STOP) {
                    musicBean.setMusicStatus(MusicBean.MusicStatus.UNPLAYING);
                } else {
                    musicBean.setMusicStatus(MusicBean.MusicStatus.PLAYING);
                }
            }
        }
    }
}
