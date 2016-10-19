package com.haloai.hud.hudendpoint.arwaylib.scene;

import android.content.Context;
import android.graphics.Color;

import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.rajawali.object3d.ARWayRoadBuffredObject;
import com.haloai.hud.hudendpoint.arwaylib.rajawali.object3d.RoadFogMaterialPlugin;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.hudendpoint.arwaylib.utils.TimeRecorder;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.scene.Scene;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by wangshengxing on 16/9/22.
 */
public class ArwaySceneUpdater extends SuperArwaySceneUpdater implements IARwayRoadRender{

    private static final boolean IS_DEBUG_PATH_LINE     = false;
    private static final boolean IS_DEBUG_SHIPE_POINT   = false;
    private static final int     ROAD_OBJECT_SIZE       = 2;
    private static final int     MAX_CROSS_ROAD_DISPLAY = 3;
    private static final boolean IS_DEBUG_MODE          = true;
    private static final boolean IS_DRAW_RFERENCE_LINT  = true;
    private static final boolean IS_SINGLETON           = true;
    private static final boolean IS_ROAD_FOG            = true;

    private List<RoadLayers> mRoadLayersList      = new LinkedList<>();
    private List<RoadLayers> mCrossRoadLayersList = new ArrayList<>();
    private int              mRoadLayersIndex     = 0;
    private int              mCrossRoadLayersCnt  = 0; //显示的路口放大图计算


    //basic
    private Context mContext;

    //ROAD
    private static final float    ROAD_WIDTH                 = 0.8f;
    private              double   REFERENCE_LINE_STEP_LENGTH = ARWayConst.REFERENCE_LINE_STEP_LENGTH* ROAD_WIDTH; //参考线间的长度
    private              Material mRoadMaterial              = new Material();
    private              Material mTestMaterial              = new Material();

    //render configuration
    private float mRoadLevel         = 20;
    private float mNaviRoadWidth     = ROAD_WIDTH;
    private float mRoadWidth         = ROAD_WIDTH*1f;
    private float mRefLineHeight     = ROAD_WIDTH;
    private float mRefLineWidth      = ROAD_WIDTH;
    private float mRefLineStepLength = ROAD_WIDTH;


    private int mRefLineColor = Color.WHITE;
    private int mRoadBottomColor = Color.WHITE;
    private int mRoadColor = Color.DKGRAY;
    private int mMainRoadColor = Color.BLUE;

    private Texture mRoadBottomTexture = null;
    private Texture mRoadMidleTexture  = null;
    private Texture mMainRoadTexture   = null;

    private Material mRoadBottomMaterial  = null;
    private Material mRoadMidleMaterial   = null;
    private Material mMainRoadMaterial    = null;
    private Material mRoadReflineMaterial = null;

    private static ArwaySceneUpdater mArwaySceneUpdater    = new ArwaySceneUpdater(null);
    private        TimeRecorder      mSceneUpdaterRecorder = new TimeRecorder();

    private Object3D mCarObject;

    private class RoadLayers{
        private ARWayRoadBuffredObject white   = null;
        private ARWayRoadBuffredObject black   = null;
        private ARWayRoadBuffredObject refLine = null;
        private ARWayRoadBuffredObject lead    = null;

        public RoadLayers(ARWayRoadBuffredObject white, ARWayRoadBuffredObject black, ARWayRoadBuffredObject lead,ARWayRoadBuffredObject refLine) {
            this.white = white;
            this.black = black;
            this.refLine = refLine;
            this.lead = lead;
        }
        public RoadLayers(ARWayRoadBuffredObject white, ARWayRoadBuffredObject black,ARWayRoadBuffredObject refLine) {
            this.white = white;
            this.black = black;
            this.refLine = refLine;
        }
        public RoadLayers(ARWayRoadBuffredObject white, ARWayRoadBuffredObject black) {
            this.white = white;
            this.black = black;
        }

    }

    public static ArwaySceneUpdater getInstance(){
        if ( !IS_SINGLETON|| mArwaySceneUpdater == null) {
            mArwaySceneUpdater = new ArwaySceneUpdater(null);
        }
        return mArwaySceneUpdater;
    }

    private ArwaySceneUpdater() {
        this(null);
    }

    private ArwaySceneUpdater(Scene scene) {
        super(scene);
        mRoadMaterial.useVertexColors(true);
        mTestMaterial.setColor(Color.GREEN);

        initRoadMaterial();
    }

    public void initScene() {

    }

    @Override
    public void setScene(Scene scene) {
        super.setScene(scene);
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public float getRoadLevel() {
        return mRoadLevel;
    }

    public void setRoadLevel(float roadLevel) {
        mRoadLevel = roadLevel;
    }

    public void setCarObject(Object3D carObject) {
        mCarObject = carObject;
    }

    private void initRoadMaterial(){
        int[] textureIds = new int[]{R.drawable.road_circle_white, R.drawable.road_circle_grey,
                R.drawable.road_circle_blue,R.drawable.road_white_change};
        List<Material> materialList = new LinkedList();
        float colorInfluence = 0;
        float textureInfluence = 1;
        if(!ARWayConst.IS_USE_ROAD_TEXTURE){
            colorInfluence=1;
            textureInfluence=0;
        }
        for (int i = 0; i < textureIds.length; i++) {
            Material material = new Material();
            material.useVertexColors(true);
            material.setColorInfluence(colorInfluence);
            materialList.add(material);
            int id = textureIds[i];
            Texture texture = new Texture(ATexture.TextureType.DIFFUSE,"road_texture"+i,id);
            texture.setFilterType(ATexture.FilterType.LINEAR);
            texture.setWrapType(ATexture.WrapType.REPEAT);
            texture.setMipmap(false);
            texture.setInfluence(textureInfluence);
            try {
                material.addTexture(texture);
            } catch (ATexture.TextureException e) {
                HaloLogger.logE("initRoadMaterial","initRoadMaterial addTexture failed");
                e.printStackTrace();
            }
        }
        mRoadBottomMaterial = materialList.get(0);
        mRoadMidleMaterial = materialList.get(1);
        mMainRoadMaterial = materialList.get(2);
        mRoadReflineMaterial = materialList.get(3);

        if(IS_ROAD_FOG) {
            RoadFogMaterialPlugin fogMaterialPlugin = new RoadFogMaterialPlugin();
            mRoadBottomMaterial.addPlugin(fogMaterialPlugin);
            mRoadMidleMaterial.addPlugin(fogMaterialPlugin);
        }

//        setAlpha(0.5f);

    }

    private RoadLayers createRoadLayer(float roadWidth, float roadRate, float refLineHegiht, float refLineWidth, Material material){


        ARWayRoadBuffredObject reflineObject = new ARWayRoadBuffredObject(refLineHegiht,refLineWidth, mRefLineColor,mRoadReflineMaterial);

        RoadLayers roadLayers = new RoadLayers(new ARWayRoadBuffredObject(roadWidth, mRoadBottomColor,mRoadBottomMaterial),
                new  ARWayRoadBuffredObject(roadWidth*roadRate*0.8f, mRoadColor,mRoadMidleMaterial),
                new ARWayRoadBuffredObject(roadWidth*roadRate, mMainRoadColor,mMainRoadMaterial),
                reflineObject);
        return roadLayers;
    }

    private RoadLayers createCrossRoadLayer(float roadWidth, float roadRate,Material material){
        RoadLayers roadLayers = new RoadLayers(new ARWayRoadBuffredObject(roadWidth, mRoadBottomColor,mRoadBottomMaterial),
                new  ARWayRoadBuffredObject(roadWidth*roadRate, mRoadColor,mRoadMidleMaterial));
        return roadLayers;
    }

    /**
     * 每次绽放级别渲染前，需要重新设置路宽
     * @param roadWidth
     */
    public void setRoadWidth(float roadWidth) {
        mRoadWidth = roadWidth;
        mNaviRoadWidth = roadWidth;
    }

    /**
     * 渲染当前显示的道路
     * @param path
     * @return
     */
    public boolean renderNaviPath(List<Vector3> path) {
        if (path == null) {
            return false;
        }
        boolean result = true;
        if(IS_DEBUG_MODE){
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, String.format("renderNaviPath,path size is %s",path.size()));
        }
        if (mSceneUpdaterRecorder != null) {
            mSceneUpdaterRecorder.start();
        }
        float roadscale = mRoadWidth;
        RoadLayers roadLayers = createRoadLayer(1*roadscale,0.9f,0.4f*roadscale,0.15f*roadscale,mRoadMaterial);
        mRoadLayersList.clear();
        mRoadLayersList.add(roadLayers);

        Vector3 postion = new Vector3(0,0,0);
        roadLayers.white.setFogEnable(false);
        roadLayers.black.setFogEnable(false);
        roadLayers.white.setPosition(postion);
        roadLayers.black.setPosition(postion);
        roadLayers.refLine.setPosition(postion);
        roadLayers.lead.setPosition(postion);
        result &= roadLayers.white.updateBufferedRoad(path);
        result &= roadLayers.black.updateBufferedRoad(path);
        result &= roadLayers.lead.updateBufferedRoad(path);

        if(IS_DRAW_RFERENCE_LINT){
            result &= roadLayers.refLine.updateReferenceLine(path,REFERENCE_LINE_STEP_LENGTH);
        }
        if (mSceneUpdaterRecorder != null) {
            mSceneUpdaterRecorder.recordeAndLog("performance","renderNaviPath");
        }

        reloadAllRoadLayer();
        return result;
    }

    @Override
    public boolean renderIndicationLine(List<Vector3> path) {
        return false;
    }

    @Override
    public boolean renderCrossRoad(List<List<Vector3>> cross) {
        boolean result = true;
        Material rMaterial = new Material();
        rMaterial.useVertexColors(true);
        if(++mCrossRoadLayersCnt>MAX_CROSS_ROAD_DISPLAY){
            mCrossRoadLayersCnt=0;
            mCrossRoadLayersList.clear();
        }

        int crossSize = cross.size();
        for (int i = 0; i < crossSize; i++) {
            List<Vector3> road = cross.get(i);
            if (road != null && road.size()>0) {
                RoadLayers roadLayers = createCrossRoadLayer(mRoadWidth,0.9f,mRoadMaterial);
                mCrossRoadLayersList.add(roadLayers);
                Vector3 fogStart = road.get(0);
                Vector3 fogEng = road.get(road.size()-1);
                if(true && i==0){
                    fogStart = new Vector3(0,0,0);
                    fogEng = new Vector3(0.01,0,0);
                }
                boolean isFog = IS_ROAD_FOG;
                roadLayers.black.setFogEnable(isFog);
                roadLayers.white.setFogEnable(isFog);
                roadLayers.black.updateBufferedRoad(road);
                roadLayers.white.updateBufferedRoad(road);
                roadLayers.black.setFogStart(fogStart);
                roadLayers.black.setFogEnd(fogEng);
                roadLayers.white.setFogStart(fogStart);
                roadLayers.white.setFogEnd(fogEng);
            }
        }

        HaloLogger.logE("onRenderFrame","onRenderFrame,renderCrossRoad called");

        /*for(RoadLayers roadLayers:mCrossRoadLayersList){
            result &= addObject(roadLayers.white);
        }
        for(RoadLayers roadLayers:mCrossRoadLayersList){
            result &= addObject(roadLayers.black);
        }*/

        reloadAllRoadLayer();
        return result;
    }

    public void setAlpha(float alpha) {
        Material[] materials = new Material[]{mRoadBottomMaterial,mRoadMidleMaterial,mMainRoadMaterial,mRoadReflineMaterial};
        for (Material m :materials){
            for(ATexture texture: m.getTextureList()){
                texture.setInfluence(alpha);
            }
        }
    }

    /**
     * 重新按图层显示顺序加载需要显示的图层
     * @return
     */
    public boolean reloadAllRoadLayer(){
        boolean result = true;
        mScene.clearChildren();
        //1
        for(RoadLayers roadLayers:mCrossRoadLayersList){
            result &= addObject(roadLayers.white);
        }
        for(RoadLayers roadLayers:mRoadLayersList){
//            result &= addObject(roadLayers.white);
        }
        //2
        for(RoadLayers roadLayers:mCrossRoadLayersList){
            result &= addObject(roadLayers.black);
        }
        /*for(RoadLayers roadLayers:mCrossRoadLayersList){
            result &= addObject(roadLayers.refLine);
        }*/
        for(RoadLayers roadLayers:mRoadLayersList){
            result &= addObject(roadLayers.lead);
        }
        for(RoadLayers roadLayers:mRoadLayersList){
            result &= addObject(roadLayers.black);
        }

        //3
        for(RoadLayers roadLayers:mRoadLayersList){
            result &= addObject(roadLayers.refLine);
        }

        //4
        if (mCarObject != null) {
            result &= addObject(mCarObject);
        }
        return result;
    }
    /**
     * 清除显示的路网
     * @return
     */
    public boolean clearRoadnetwork(){
        boolean result = true;
        mCrossRoadLayersList.clear();
        reloadAllRoadLayer();
        return result;
    }

    /**
     * 清除显示的导航路
     * @return
     */
    public boolean clearNaviRoad(){
        boolean result = true;
        mRoadLayersList.clear();
        reloadAllRoadLayer();
        return result;
    }

}

