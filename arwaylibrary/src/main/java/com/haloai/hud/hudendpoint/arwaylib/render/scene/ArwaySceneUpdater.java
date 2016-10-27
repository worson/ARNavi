package com.haloai.hud.hudendpoint.arwaylib.render.scene;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.opengl.GLES20;

import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.render.object3d.ARWayRoadBuffredObject;
import com.haloai.hud.hudendpoint.arwaylib.render.object3d.BaseObject3D;
import com.haloai.hud.hudendpoint.arwaylib.render.object3d.TileFloor;
import com.haloai.hud.hudendpoint.arwaylib.render.shader.RoadFogMaterialPlugin;
import com.haloai.hud.hudendpoint.arwaylib.render.shader.TextureAlphaMaterialPlugin;
import com.haloai.hud.hudendpoint.arwaylib.render.strategy.IRenderStrategy;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.hudendpoint.arwaylib.utils.TimeRecorder;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.scene.Scene;
import org.rajawali3d.util.RajLog;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.rajawali3d.util.RajLog.TAG;

/**
 * Created by wangshengxing on 16/9/22.
 */
public class ArwaySceneUpdater extends SuperArwaySceneUpdater implements IARwayRoadRender{

    private static final boolean IS_DEBUG_MODE          = true;
    private static final boolean IS_DRAW_RFERENCE_LINT  = true;
    public static        boolean IS_SINGLETON           = true;
    private static final boolean IS_ROAD_FOG            = false;

    private ARWayRoadBuffredObject mIndicationLine  = null;
    private Plane                  mIndicationArrow = null;

    private BaseObject3D mArwayMap          = null;
    private BaseObject3D mGridfloorLayer    = null;
    private BaseObject3D mCrossRoadBottom   = null;
    private BaseObject3D mCrossRoad         = null;
    private BaseObject3D mNaviRoadBottom    = null;
    private BaseObject3D mNaviRoadTop       = null;
    private BaseObject3D mNaviRoad          = null;
    private BaseObject3D mNaviRoadRefLine   = null;
    private BaseObject3D mNaviDirectorLayer = null;


    //basic
    private Context mContext;

    //VERTICE_ROAD
    private static final float    ROAD_WIDTH                 = 0.8f;
    //render configuration
    private float mRoadLevel         = 20;
    private float mNaviRoadWidth     = ROAD_WIDTH;
    private float mRoadWidth         = ROAD_WIDTH*1f;
    private float mIndicationWidth   = ROAD_WIDTH*0.3f;
    private float mRefLineHeight     = ROAD_WIDTH;
    private float mRefLineWidth      = ROAD_WIDTH;
    private float mRefLineStepLength = ROAD_WIDTH;

    private int ALPHA_MASK            = 0xffffffff;
    private int mIndicationColor      = Color.GREEN&ALPHA_MASK;
    private int mRefLineColor         = Color.BLACK;//Color.argb(0xff,0,160,233)
    private int mNaviLineColor        = Color.argb(0xff,0,174,195);
    private int mCrossRoadBottomColor = Color.WHITE&ALPHA_MASK;
    private int mCrossRoadColor       = Color.GRAY&ALPHA_MASK;
    private int mMainRoadColor        = Color.BLUE&ALPHA_MASK;
    private int mRoadBottomColor      = Color.GRAY;
    private int mRoadColor            = Color.BLACK;

    private Material mRoadMaterial            = null;
    private Material mTestMaterial            = null;
    private Material mMaterial                = null;
    private Material mCrossRoadBottomMaterial = null;
    private Material mCrossRoadTopMaterial    = null;
    private Material mMainRoadMaterial        = null;
    private Material mRoadReflineMaterial     = null;
    private Material mCommonRoadMaterial      = null;
    private Material mArrowMaterial           = null;
    private Material mFloorMaterial           = null;
    private List<Material> mMaterialList            = new ArrayList<>();

    private static ArwaySceneUpdater mArwaySceneUpdater    = new ArwaySceneUpdater(null);
    private        TimeRecorder      mSceneUpdaterRecorder = new TimeRecorder();

    private Object3D mCarObject;

    private IRenderStrategy.RenderParams mRenderParams;
    private float mSceneAlpha = 1;

    private class RoadLayers{
        private ARWayRoadBuffredObject bottom  = null;
        private ARWayRoadBuffredObject road    = null;
        private ARWayRoadBuffredObject refLine = null;
        private ARWayRoadBuffredObject navi    = null;

        public RoadLayers() {
        }

        public RoadLayers(ARWayRoadBuffredObject bottom, ARWayRoadBuffredObject road) {
            this.bottom = bottom;
            this.road = road;
        }

    }

    public static ArwaySceneUpdater getInstance(){
        if ( !IS_SINGLETON|| mArwaySceneUpdater == null) {
            mArwaySceneUpdater = new ArwaySceneUpdater();
        }
        return mArwaySceneUpdater;
    }

    private ArwaySceneUpdater() {
        this(null);
    }

    private ArwaySceneUpdater(Scene scene) {
        super(scene);
    }


    public void initScene(){
        RajLog.setDebugEnabled(false);
        initMaterial();
        initAllLayer();
    }


    /**
     *
     */
    public void reset() {
        reloadAllLayer();
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

    private void initMaterial(){
        int[] textureIds = new int[]{R.drawable.road_circle_alpha_change,R.drawable.triangle_arrow,
                R.drawable.road_circle_alpha_change,R.drawable.road_navi_arrow,R.drawable.arway_tile_floor};
        List<Material> materialList = new LinkedList();
        float colorInfluence = 1;
        float textureInfluence = 1;
        boolean useVertexColor = false;
        if(!ARWayConst.IS_USE_ROAD_TEXTURE){
            colorInfluence=1;
            textureInfluence=0;
            useVertexColor = true;
        }
        for (int i = 0; i < textureIds.length; i++) {
            Material material = new Material();
            material.useVertexColors(useVertexColor);
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
                HaloLogger.logE("initMaterial","initMaterial addTexture failed");
                e.printStackTrace();
            }
        }
        mCommonRoadMaterial = materialList.get(0);
        mMainRoadMaterial = mCommonRoadMaterial;

        mCrossRoadBottomMaterial = materialList.get(2);
        mCrossRoadTopMaterial = mCrossRoadBottomMaterial;

        mRoadReflineMaterial = materialList.get(3);

        mFloorMaterial =materialList.get(4);

        mArrowMaterial = materialList.get(1);
        mArrowMaterial.getTextureList().get(0).setInfluence(1f);

        mMaterial = new Material();

        mRoadMaterial            = new Material();
        mTestMaterial            = new Material();
        mRoadMaterial.useVertexColors(true);
        mTestMaterial.setColor(Color.GREEN);

        RoadFogMaterialPlugin fogMaterialPlugin = new RoadFogMaterialPlugin();
        mCrossRoadBottomMaterial.addPlugin(fogMaterialPlugin);
        mCrossRoadTopMaterial.addPlugin(fogMaterialPlugin);

        TextureAlphaMaterialPlugin textureAlphaPlugin = new TextureAlphaMaterialPlugin();
        mMainRoadMaterial.addPlugin(textureAlphaPlugin);
        mRoadReflineMaterial.addPlugin(textureAlphaPlugin);
        mArrowMaterial.addPlugin(textureAlphaPlugin);
        mFloorMaterial.addPlugin(textureAlphaPlugin);

        {
            mMaterialList.add(mCrossRoadBottomMaterial);
            mMaterialList.add(mCrossRoadTopMaterial);
            mMaterialList.add(mMainRoadMaterial);
            mMaterialList.add(mRoadReflineMaterial);
            mMaterialList.add(mCommonRoadMaterial);
            mMaterialList.add(mArrowMaterial);
            mMaterialList.add(mFloorMaterial);
        }
    }

    private RoadLayers createNaviRoadLayer(float width) {

        float roadWidth = width*1.1f;
        float roadRate = 0.8f;
        float refLineHegiht = roadWidth*1.35f;
        float naviScale = 0.5f;
        float stepLength = roadWidth*2.5f;
        ARWayRoadBuffredObject bottom = new ARWayRoadBuffredObject(roadWidth, mCrossRoadBottomColor, mCrossRoadBottomMaterial);
        ARWayRoadBuffredObject road = new ARWayRoadBuffredObject(roadWidth * roadRate , mCrossRoadColor, mCrossRoadTopMaterial);
        bottom.setColor(mRoadBottomColor);
        road.setColor(mRoadColor);

        ARWayRoadBuffredObject refline = new ARWayRoadBuffredObject(refLineHegiht, roadWidth,stepLength, mRefLineColor, mRoadReflineMaterial);
        refline.setColor(mRefLineColor);

        ARWayRoadBuffredObject navi = new ARWayRoadBuffredObject(roadWidth * naviScale, mMainRoadColor, mMainRoadMaterial);
        navi.setColor(mNaviLineColor);

        ARWayRoadBuffredObject.ShapeType type = ARWayRoadBuffredObject.ShapeType.TEXTURE_ROAD;
        if (!ARWayConst.IS_USE_ROAD_TEXTURE) {
            type = ARWayRoadBuffredObject.ShapeType.VERTICE_ROAD;
        }
        navi.setShapeType(type);
        road.setShapeType(type);
        bottom.setShapeType(type);
        RoadLayers roadLayers = new RoadLayers();
        roadLayers.refLine = refline;
        roadLayers.navi = navi;
        roadLayers.bottom = bottom;
        roadLayers.road = road;
        return roadLayers;
    }

    private TileFloor createFloor(float width,float height,float spacing){
        TileFloor tileFloor = new TileFloor(width,height,spacing);
        tileFloor.setMaterial(mFloorMaterial);
        tileFloor.setBlendingEnabled(true);
        tileFloor.setBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        tileFloor.setDepthTestEnabled(false);
        tileFloor.setDepthMaskEnabled(false);
        tileFloor.setColor(Color.DKGRAY);
        return  tileFloor;
    }
    private RoadLayers createCrossRoadLayer(float roadWidth, float roadRate,Material material){
        ARWayRoadBuffredObject bottom = new ARWayRoadBuffredObject(roadWidth, mCrossRoadBottomColor, mCrossRoadBottomMaterial);
        bottom.setColor(mCrossRoadBottomColor);
        ARWayRoadBuffredObject road = new  ARWayRoadBuffredObject(roadWidth*roadRate, mCrossRoadColor, mCrossRoadTopMaterial);
        road.setColor(mCrossRoadColor);
        ARWayRoadBuffredObject.ShapeType type = ARWayRoadBuffredObject.ShapeType.TEXTURE_ROAD;
        if(!ARWayConst.IS_USE_ROAD_TEXTURE){
            type = ARWayRoadBuffredObject.ShapeType.VERTICE_ROAD;
        }
        bottom.setShapeType(type);
        road.setShapeType(type);
        RoadLayers roadLayers = new RoadLayers(bottom, road);
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

    @Override
    public void renderFloor(float left,float top,float right,float bottom,float spacing) {
        mGridfloorLayer.clearChildren();
        mGridfloorLayer.addChild(createFloor(right-left,right-bottom,spacing));
        mGridfloorLayer.setPosition((right+left)/2,(right+bottom)/2,0);
    }

    /**
     * 渲染当前显示的道路
     * @param path
     * @return
     */
    public boolean renderNaviPath(List<Vector3> path) {
        if (path == null || path.size()<2) {
            return false;
        }
        boolean result = true;
        final Vector3 offset = new Vector3(path.get(0));
        if(IS_DEBUG_MODE){
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, String.format("renderNaviPath,path size is %s",path.size()));
        }
        if (mSceneUpdaterRecorder != null) {
            mSceneUpdaterRecorder.start();
        }
        float roadWidth = mRoadWidth;
        RoadLayers roadLayers = createNaviRoadLayer(1*roadWidth);
        mNaviRoadBottom.clearChildren();
        mNaviRoad.clearChildren();
        mNaviRoadTop.clearChildren();
        mNaviRoadRefLine.clearChildren();

        mNaviRoadBottom.addChild(roadLayers.bottom);
        mNaviRoad.addChild(roadLayers.navi);
        mNaviRoadTop.addChild(roadLayers.road);
        mNaviRoadRefLine.addChild(roadLayers.refLine);

        mNaviRoadBottom.setPosition(offset);
        mNaviRoad.setPosition(offset);
        mNaviRoadTop.setPosition(offset);
        mNaviRoadRefLine.setPosition(offset);

        Vector3 postion = new Vector3(0);
        roadLayers.bottom.setFogEnable(false);
        roadLayers.road.setFogEnable(false);

        roadLayers.bottom.setPosition(postion);
        roadLayers.road.setPosition(postion);
        roadLayers.refLine.setPosition(postion);
        roadLayers.navi.setPosition(postion);

        result &= roadLayers.bottom.updateBufferedRoad(path,offset);
        result &= roadLayers.road.updateBufferedRoad(path,offset);
        result &= roadLayers.navi.updateBufferedRoad(path,offset);

        if(IS_DRAW_RFERENCE_LINT){
            result &= roadLayers.refLine.updateReferenceLine(path,offset);
        }
        if (mSceneUpdaterRecorder != null) {
            mSceneUpdaterRecorder.recordeAndLog("performance","renderNaviPath");
        }
        return result;
    }

    @Override
    public boolean renderDirectorLine(List<Vector3> path) {
        if (path == null || path.size()<2) {
            return false;
        }
        int pathSize = path.size();
        final Vector3 offset = new Vector3(path.get(0));
        if (mIndicationLine != null) {
            mNaviDirectorLayer.removeChild(mIndicationLine);
            mIndicationLine = null;
        }
        if (mIndicationLine == null) {
            mIndicationLine = new ARWayRoadBuffredObject(mIndicationWidth,mIndicationColor, ARWayRoadBuffredObject.ShapeType.TEXTURE_ROAD);
            mIndicationLine.setMaterial(mCommonRoadMaterial);
            mIndicationLine.setColor(mIndicationColor);
            if (mNaviDirectorLayer != null) {
                mNaviDirectorLayer.addChild(mIndicationLine);
            }
        }
        if (mIndicationArrow == null) {
            mIndicationArrow = new Plane(mIndicationWidth*4,mIndicationWidth*4,10,10, Vector3.Axis.Z,
                    true,false,1,true);
            mIndicationArrow.setColor(mIndicationColor);
            mIndicationArrow.setMaterial(mArrowMaterial);
            mIndicationArrow.setBlendingEnabled(true);
            mIndicationArrow.setBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            if (mNaviDirectorLayer != null) {
                mNaviDirectorLayer.addChild(mIndicationArrow);
            }
        }
        mNaviDirectorLayer.setPosition(offset);
        mIndicationLine.setPosition(0,0,0);
        mIndicationLine.updateBufferedRoad(path,offset);

        Vector3 start = path.get(pathSize-2);
        Vector3 end = path.get(pathSize-1);
        float cDegree = (float) Math.toDegrees(Math.atan2((end.y-start.y),(end.x-start.x)));
        mIndicationArrow.setPosition(Vector3.subtractAndCreate(end,offset));
        mIndicationArrow.setRotation(Vector3.Axis.Z,-(cDegree-90));

        return true;
    }

    @Override
    public boolean renderRoadNet(List<List<Vector3>> cross) {
        if (cross == null || cross.size()<1) {
            return false;
        }
        boolean result = true;
        mCrossRoadBottom.clearChildren();
        mCrossRoad.clearChildren();
        int crossSize = cross.size();
        final Vector3 offset = new Vector3();
        Vector3 crossStart = cross.get(0).get(0);
        if (crossStart != null) {
            offset.setAll(offset);
        }
        mCrossRoadBottom.setPosition(offset);
        mCrossRoad.setPosition(offset);
        for (int i = crossSize-1; i >= 0; i--) {
            List<Vector3> road = cross.get(i);
            if (road != null && road.size()>0) {
                RoadLayers roadLayers = createCrossRoadLayer(mRoadWidth,0.9f,mRoadMaterial);
                mCrossRoadBottom.addChild(roadLayers.bottom);
                mCrossRoad.addChild(roadLayers.road);
                Vector3 fogStart = road.get(0);
                Vector3 fogEng = road.get(road.size()-1);
                if(true && i==0){
                    fogStart = new Vector3(0,0,0);
                    fogEng = new Vector3(0.01,0,0);
                }
                boolean isFog = IS_ROAD_FOG;
                roadLayers.road.setFogEnable(isFog);
                roadLayers.bottom.setFogEnable(isFog);
                roadLayers.road.updateBufferedRoad(road,offset);
                roadLayers.bottom.updateBufferedRoad(road,offset);
                roadLayers.road.setFogStart(fogStart);
                roadLayers.road.setFogEnd(fogEng);
                roadLayers.bottom.setFogStart(fogStart);
                roadLayers.bottom.setFogEnd(fogEng);
            }
        }
        return result;
    }

    public void setAlpha(float alpha) {
        mSceneAlpha = alpha;
        for (Material m :mMaterialList){
            if (m != null) {
                m.setColorInfluence(alpha);
                for(ATexture texture: m.getTextureList()){
                    texture.setInfluence(alpha);
                }
            }

        }
    }

    @Override
    public void onRender(long ellapsedRealtime, double deltaTime) {

    }

    private void reloadAllLayer(){
        mScene.clearChildren();
        addObject(mArwayMap);
    }

    private boolean initAllLayer(){
        boolean result = true;
        mScene.clearChildren();

        mArwayMap          = new BaseObject3D();
        mGridfloorLayer    = new BaseObject3D();
        mCrossRoadBottom   = new BaseObject3D();
        mCrossRoad         = new BaseObject3D();
        mNaviRoadBottom    = new BaseObject3D();
        mNaviRoadTop       = new BaseObject3D();
        mNaviRoad          = new BaseObject3D();
        mNaviRoadRefLine   = new BaseObject3D();
        mNaviDirectorLayer = new BaseObject3D();

        Object3D[] layers = new Object3D[]{mGridfloorLayer,mCrossRoadBottom,mCrossRoad,
                mNaviRoadBottom,mNaviRoadTop,mNaviRoad,mNaviRoadRefLine,
                mNaviDirectorLayer,mCarObject};
        mScene.addChild(mArwayMap);
        for(Object3D layer:layers){
            if (layer != null) {
                mArwayMap.addChild(layer);
            }
        }
        HaloLogger.logE(TAG,String.format("initAllLayer,Scene child =%s,mArwayMap child =%s",mScene.getNumChildren(),mArwayMap.getNumChildren()));
        return result;
    }

    public void clearSceneObjects(){
        clearRoadnetwork();
        clearNaviRoad();
    }
    @Override
    public void setCurrentPosition(Vector3 curPosition) {
        super.setCurrentPosition(curPosition);

    }

    /**
     * 清除显示的路网
     * @return
     */
    public boolean clearRoadnetwork(){
        boolean result = true;
        BaseObject3D[] layers = new BaseObject3D[]{mCrossRoadBottom,mCrossRoad};
        for(BaseObject3D layer:layers){
            if (layer != null) {
                layer.clearChildren();
            }
        }
        return result;
    }

    /**
     * 清除显示的导航路
     * @return
     */
    public boolean clearNaviRoad(){
        boolean result = true;
        BaseObject3D[] layers = new BaseObject3D[]{mNaviRoadBottom,mNaviRoadTop,mNaviRoad,mNaviRoadRefLine, mNaviDirectorLayer};
        for(BaseObject3D layer:layers){
            if (layer != null) {
                layer.clearChildren();
            }
        }
        return result;
    }


}

