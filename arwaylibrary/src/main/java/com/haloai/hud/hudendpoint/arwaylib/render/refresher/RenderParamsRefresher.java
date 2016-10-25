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

    static final double MinScale = 1.0;
    static final double MaxScale = 2.0;

    private IRefreshDataLevelNotifer mNotifer;

    private int currentLevel;
    private int goalLevel;

    private double suitableRoadWidth;

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

    public void initDefaultRenderParmars(int dateLevel,double angel,double inScreenProportion,double scale,double roadWidth){
        suitableRoadWidth = roadWidth;

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


    public void setGoalRenderParmars(int dateLevel,double angel,double inScreenProportion,double scale,double roadWidth){
        if (currentLevel != goalLevel||currentScale!=goalScale){
            currentLevel = goalLevel;
            currentScale = goalScale;
            mNotifer.onRefreshDataLevel(currentLevel,suitableRoadWidth);
        }
        suitableRoadWidth = roadWidth;

        goalLevel = dateLevel;
        goalScale = scale;

        int levelNum = goalLevel - currentLevel;
        step_Scale = (levelNum + goalScale - currentScale )/FPS;

        goalAngel = angel;
        step_Angel = (goalAngel - currentAngel)/FPS;

        goalInScreenProportion = inScreenProportion;
        step_InScreenProportion = (goalInScreenProportion - currentInScreenProportion)/FPS;



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
                    mNotifer.onRefreshDataLevel(currentLevel,suitableRoadWidth);
                }
            } else {
                if (currentScale <= MinScale){
                    currentLevel --;
                    currentScale = MaxScale;
                    mNotifer.onRefreshDataLevel(currentLevel,suitableRoadWidth);
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

}
