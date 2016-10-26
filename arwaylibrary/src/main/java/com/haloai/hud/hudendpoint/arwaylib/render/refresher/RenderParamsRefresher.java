package com.haloai.hud.hudendpoint.arwaylib.render.refresher;

import android.util.Log;

import com.haloai.hud.hudendpoint.arwaylib.render.camera.ARWayCameraCaculatorY;
import com.haloai.hud.hudendpoint.arwaylib.render.camera.CameraParam;
import com.haloai.hud.hudendpoint.arwaylib.render.strategy.IRenderStrategy;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.math.vector.Vector3;

/**
 * Created by ylq on 16/10/25.
 */
public class RenderParamsRefresher {

    static final int FPS = 30;//帧数


    static final double MaxScale = 2.0;
    static final double MinScale = 1.0;

    private IRefreshDataLevelNotifer mNotifer;

    private int currentLevel;
    private int goalLevel;


    private double currentAngel;
    private double goalAngel;
    private double step_Angel = 0;

    private double currentInScreenProportion;
    private double goalInScreenProportion;
    private double step_InScreenProportion = 0;

    private double currentScale;
    private double goalScale;
    private double step_Scale = 0;


    public void setIRefeshDataLevelNotifer(IRefreshDataLevelNotifer notifer){
        this.mNotifer = notifer;
    }

    public void initDefaultRenderParmars(int dateLevel,double angel,double inScreenProportion,double scale){


        currentLevel = dateLevel;
        currentAngel = angel;
        currentInScreenProportion = inScreenProportion;
        currentScale = scale;

        goalLevel = dateLevel;
        goalAngel = angel;
        goalInScreenProportion = inScreenProportion;
        goalScale = scale;
        Log.e("ylq","initDefault");
    }


    public void setGoalRenderParmars(int dateLevel,double angel,double inScreenProportion,double scale){
        if (currentLevel != goalLevel||currentScale!=goalScale){
            currentLevel = goalLevel;
            currentScale = goalScale;
            mNotifer.onRefreshDataLevel(currentLevel,getSuitableRoadWidth(currentLevel));
        }

        goalLevel = dateLevel;
        goalScale = scale;

        int levelNum = goalLevel - currentLevel;

        int timeLength = Math.abs(levelNum);
        if (timeLength == 0){
            timeLength = 1;
        }
        step_Scale = (levelNum + goalScale - currentScale )/(FPS*timeLength);

        goalAngel = angel;
        step_Angel = (goalAngel - currentAngel)/(FPS * timeLength);

        goalInScreenProportion = inScreenProportion;
        step_InScreenProportion = (goalInScreenProportion - currentInScreenProportion)/(FPS *timeLength);



    }

    public void cameraRefresh(Camera currentCamera,Vector3 location,double rotZ){
                                                                                        currentAngel += step_Angel;
        currentInScreenProportion += step_InScreenProportion;
        currentScale += step_Scale;

        Vector3 position = new Vector3();
        Vector3 lookAt = new Vector3();
        CameraParam param = new CameraParam(location,rotZ,currentScale,currentAngel,currentInScreenProportion);
        ARWayCameraCaculatorY.calculateCameraPositionAndLookAtPoint(param,position,lookAt);
        currentCamera.setPosition(position);
        currentCamera.setLookAt(lookAt);

        if (currentLevel != goalLevel) {
            if (step_Scale >= 0) {
                if (currentScale >= MaxScale) {
                    currentLevel++;
                    currentScale = MinScale;
                    mNotifer.onRefreshDataLevel(currentLevel,getSuitableRoadWidth(currentLevel));
                }
            } else {
                if (currentScale <= MinScale){
                    currentLevel --;
                    currentScale = MaxScale;
                    mNotifer.onRefreshDataLevel(currentLevel,getSuitableRoadWidth(currentLevel));
                }
            }
        }else {
            if (step_Scale >= 0){
                if (currentScale >= goalScale){
                    currentScale = goalScale;
                    step_Scale = 0;
                    step_Angel = 0;
                    step_InScreenProportion = 0;
                }
            }else {
                if (step_Scale < 0){
                    if (currentScale <=goalScale){
                        currentScale = goalScale;
                        step_Scale = 0;
                        step_Angel = 0;
                        step_InScreenProportion = 0;
                    }
                }
            }
        }
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
                roadWidth = 1.0/2;
            break;
            case 19:
                roadWidth = 1.0/4;
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
