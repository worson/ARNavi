package com.haloai.hud.hudendpoint.arwaylib.calculator.factor;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.Renderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangshengxing on 16/7/10.
 */
public class CameraFactor extends SuperFactor {
    public Renderer mRenderer;
    public int      mPathRetainDistance;//获取路线剩余距离 m
    public List<Vector3> mPath = new ArrayList<>();
    public List<Double>  mLength2FinalPoint;

    private static CameraFactor  mCameraFactor = new CameraFactor();

    public static CameraFactor getInstance() {
        return mCameraFactor;
    }


    public void init(Renderer renderer, List<Vector3> path, int distance,List<Double> length2FinalPoint){
        mRenderer = renderer;
        mPath = path;
        mPathRetainDistance = distance;
        mLength2FinalPoint = length2FinalPoint;
    }
}
