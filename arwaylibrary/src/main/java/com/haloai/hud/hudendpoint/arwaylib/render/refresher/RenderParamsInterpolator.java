package com.haloai.hud.hudendpoint.arwaylib.render.refresher;

import android.util.Log;

import com.haloai.hud.hudendpoint.arwaylib.render.camera.ARWayCameraCaculatorY;
import com.haloai.hud.hudendpoint.arwaylib.render.camera.CameraParam;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.math.vector.Vector3;

/**
 * Created by ylq on 16/10/25.
 */
public class RenderParamsInterpolator {

    static final int FPS = 30;//帧数


    static final double MaxScale = 4.0;
    static final double MinScale = 1.0;

    private RenderParamsInterpolatorListener mListener;

    private int currentLevel;
    private int goalLevel;


    private double currentAngel;
    private double mgoalAngel;
    private double step_Angel = 0.0;

    private double currentInScreenProportion;
    private double mgoalInScreenProportion;
    private double step_InScreenProportion = 0.0;

    private double currentScale;
    private double mgoalScale;
    private double step_Scale = 0.0;


    public void setRenderParamsInterpolatorListener(RenderParamsInterpolatorListener listener){
        this.mListener = listener;
    }

    public void initDefaultRenderParmars(int dateLevel,double angel,double inScreenProportion,double scale){
        currentLevel = dateLevel;
        currentAngel = angel;
        currentInScreenProportion = inScreenProportion;
        currentScale = scale;

        goalLevel = dateLevel;
        mgoalAngel = angel;
        mgoalScale = scale;
        mgoalInScreenProportion = inScreenProportion;

        Log.e("ylq","initDefault");
    }


    public void doScaleAnimation(int goalDateLevel,double goalScale,double duration){
        goalLevel = goalDateLevel;
        mgoalScale = goalScale;

        int levelNum = goalLevel - currentLevel;
        step_Scale = (levelNum + goalScale - currentScale )/(FPS*duration);
    }

    public void doAngelAnimation(double goalAngel,double duration){
        mgoalAngel = goalAngel;
        step_Angel = (goalAngel - currentAngel)/(FPS * duration);
    }

    public void doInScreenProportion(double goalInScreenProportion,double duration){
        mgoalInScreenProportion = goalInScreenProportion;
        step_InScreenProportion = (goalInScreenProportion - currentInScreenProportion)/(FPS *duration);
    }

    public void cameraRefresh(Camera currentCamera,Vector3 location,double rotZ){
        currentAngel += step_Angel;
        currentInScreenProportion += step_InScreenProportion;
        currentScale += step_Scale;

        if (step_Angel >= 0){
            if (currentAngel >= mgoalAngel){
                currentAngel = mgoalAngel;
                step_Angel = 0;
            }
        }else {
            if (currentAngel <= mgoalAngel){
                currentAngel = mgoalAngel;
                step_Angel = 0;
            }
        }

        if (step_InScreenProportion >= 0){
            if (currentInScreenProportion >= mgoalInScreenProportion){
                currentInScreenProportion = mgoalInScreenProportion;
                step_InScreenProportion = 0;
            }
        }else {
            if (currentInScreenProportion <= mgoalInScreenProportion){
                currentInScreenProportion = mgoalInScreenProportion;
                step_InScreenProportion = 0;
            }
        }



        if (currentLevel != goalLevel) {
            if (step_Scale >= 0.0) {
                if (currentScale >= MaxScale) {
                    currentLevel++;
                    currentScale = MinScale;
                    mListener.onRefreshDataLevel(currentLevel,getSuitableRoadWidth(currentLevel));
                }
            } else {
                if (currentScale <= MinScale){
                    currentLevel --;
                    currentScale = MaxScale;
                    mListener.onRefreshDataLevel(currentLevel,getSuitableRoadWidth(currentLevel));
                }
            }
        }else {
            if (step_Scale >= 0.0){
                if (currentScale >= mgoalScale){
                    currentScale = mgoalScale;
                    step_Scale = 0.0;
                }
            }else {
                if (step_Scale < 0.0){
                    if (currentScale <=mgoalScale){
                        currentScale = mgoalScale;
                        step_Scale = 0.0;
                    }
                }
            }
        }


        Vector3 position = new Vector3();
        Vector3 lookAt = new Vector3();
        CameraParam param = new CameraParam(location,rotZ,currentScale,currentAngel,currentInScreenProportion);
        ARWayCameraCaculatorY.calculateCameraPositionAndLookAtPoint(param,position,lookAt);
        currentCamera.setPosition(position);
        currentCamera.setLookAt(lookAt);
    }


    public double getInitializtionRoadWidth(){
        return getSuitableRoadWidth(currentLevel);
    }

    public int getInitializtionLevel(){
        return currentLevel;
    }


    private double getSuitableRoadWidth(int dataLevel){
        double roadWidth = 0;
        switch (dataLevel){
            case 20:
                roadWidth = 0.32;
            break;
            case 19:
                roadWidth = 0.35;
                break;
            case 18:
                roadWidth = 1.0/8;
                break;
            case 17:
                roadWidth = 1.0/16;
                break;
            case 16:
                roadWidth = 1.0/32;
                break;
            case 15:
                roadWidth = 1.0/64;
                break;
        }
        return roadWidth;
    }



}
