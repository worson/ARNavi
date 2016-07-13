package com.haloai.hud.hudendpoint.arwaylib.calculator.result;

import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.Renderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangshengxing on 16/7/10.
 */
public class SceneResult extends SuperResult {

    public Renderer mRenderer = null;

    //list data
    public List<Vector3> mLeftPath            = new ArrayList<>();
    public List<Vector3> mRightPath           = new ArrayList<>();
    public List<Double>  mOffsetX             = new ArrayList<>();
    public List<Double>  mOffsetY             = new ArrayList<>();
    public List<Double>  mDistance2FinalPoint = new ArrayList<>();
    public List<Double>  mLength2FinalPoint   = new ArrayList<>();
    public List<Vector3> mCalculatePath       = new ArrayList<>();
    public List<Vector3> mOriginalPath       = new ArrayList<>();

    private static SceneResult   mSceneResult = new SceneResult();

    public static SceneResult getInstance() {
        return mSceneResult;
    }

    @Override
    public void release() {
        super.release();
        mRenderer = null;
        mCalculatePath=null;
        mLeftPath=null;
        mRightPath=null;
        mOffsetX=null;
        mOffsetY=null;
        mLength2FinalPoint=null;
        mDistance2FinalPoint=null;
        mOriginalPath = null;
    }

    @Override
    public void reset() {
        super.reset();
        mOriginalPath.clear();
        mCalculatePath.clear();
        mLeftPath.clear();
        mRightPath.clear();
        mOffsetX.clear();
        mOffsetY.clear();
        mLength2FinalPoint.clear();
        mDistance2FinalPoint.clear();
    }

}
