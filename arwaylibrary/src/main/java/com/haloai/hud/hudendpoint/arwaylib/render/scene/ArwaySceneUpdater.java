package com.haloai.hud.hudendpoint.arwaylib.render.scene;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;

import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.render.object3d.ARWayRoadBuffredObject;
import com.haloai.hud.hudendpoint.arwaylib.render.object3d.BaseObject3D;
import com.haloai.hud.hudendpoint.arwaylib.render.object3d.TileFloor;
import com.haloai.hud.hudendpoint.arwaylib.render.options.RoadRenderOption;
import com.haloai.hud.hudendpoint.arwaylib.render.shader.RoadFogMaterialPlugin;
import com.haloai.hud.hudendpoint.arwaylib.render.shader.TextureAlphaMaterialPlugin;
import com.haloai.hud.hudendpoint.arwaylib.render.utils.TDrawText;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.hudendpoint.arwaylib.utils.TimeRecorder;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.loader.ALoader;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.async.IAsyncLoaderCallback;
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
public class ArwaySceneUpdater extends SuperArwaySceneUpdater implements IRoadRender,ISceneController {
    private static boolean IS_DEBUG_MODE = true;
    public static  boolean IS_SINGLETON  = true;

    private ARWayRoadBuffredObject mIndicationLine  = null;
    private Plane                  mIndicationArrow = null;

    private BaseObject3D mArwayMap           = null;
    private BaseObject3D mGridfloorLayer     = null;
    private BaseObject3D mCrossRoadBottom    = null;
    private BaseObject3D mCrossRoad          = null;
    private BaseObject3D mNaviRoadBottom     = null;
    private BaseObject3D mNaviRoadTop        = null;
    private BaseObject3D mNaviRoad           = null;
    private BaseObject3D mNaviRoadRefLine    = null;
    private BaseObject3D mNaviGuideLineLayer = null;
    private BaseObject3D mNaviSymbolLayer    = null;
    private BaseObject3D mCarObject          = null;

    private List<List<RoadLayers>>  mCrossRoadList        = new ArrayList();
    private List<RoadLayers>  mNaviRoadList         = new ArrayList();
    private List<ObjectLayer> mFloorObjectLayerList = new ArrayList<>();

    private boolean mIsNaviRoadDirty  = true;
    private boolean mIsCrossRoadDirty = true;
    private boolean mIsFloorDirty     = true;
    private boolean mIsGuideLineDirty = true;


    //basic
    private Context mContext;

    //VERTICE_ROAD
    private RoadRenderOption             mOptions = new RoadRenderOption();
    private RoadRenderOption.LayersColor mColors  = null;

    private Material mRoadMaterial            = null;
    private Material mTestMaterial            = null;
    private Material mCrossRoadBottomMaterial = null;
    private Material mCrossRoadTopMaterial    = null;
    private Material mMainRoadMaterial        = null;
    private Material mRoadReflineMaterial     = null;
    private Material mCommonRoadMaterial      = null;
    private Material mArrowMaterial           = null;
    private Material mFloorMaterial           = null;
    private Material mCarMaterial             = null;
    private Material mNaviSymbolMaterial      = null;
    private List<Material> mMaterialList            = new ArrayList<>();

    private static ArwaySceneUpdater mArwaySceneUpdater    = new ArwaySceneUpdater(null);
    private        TimeRecorder      mSceneUpdaterRecorder = new TimeRecorder();



    private float mSceneAlpha = 1;

    private class ObjectLayer {
//        private Vector3 postion = new Vector3();
        private Object3D object = null;

        public ObjectLayer(Object3D object) {
            this.object = object;
        }
    }

    private class RoadLayers{
//        private Vector3 postion = new Vector3();

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
        mColors = mOptions.getRoadColors();
    }


    public void initScene(){
        RajLog.setDebugEnabled(true);
        initMaterial();
        initAllLayer();

        initCarObject();
    }

    public void reset() {
        mNaviRoadList.clear();
        mCrossRoadList.clear();
        mFloorObjectLayerList.clear();

        mNaviGuideLineLayer.clearChildren();

        mIndicationLine = null;
        mIndicationArrow = null;

        mIsNaviRoadDirty  = true;
        mIsCrossRoadDirty = true;
        mIsFloorDirty     = true;
        mIsGuideLineDirty = true;

        reloadAllLayer();

        commitRender();
    }

    @Override
    public void setScene(Scene scene) {
        super.setScene(scene);
    }

    public void setContext(Context context) {
        mContext = context;
    }


    private void initMaterial(){
        int[] textureIds = new int[]{R.drawable.road_circle_alpha_change,R.drawable.triangle_arrow,
                R.drawable.road_circle_alpha_change,R.drawable.road_navi_arrow,R.drawable.arway_tile_floor,
                R.drawable.arway_tex_car_1};
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
        mCarMaterial =materialList.get(5);
        mCarMaterial.setColorInfluence(0);

        mArrowMaterial = materialList.get(1);
        mArrowMaterial.getTextureList().get(0).setInfluence(1f);

        mRoadMaterial = new Material();
        mTestMaterial = new Material();
        mRoadMaterial.useVertexColors(true);
        mTestMaterial.setColor(Color.GREEN);

        mNaviSymbolMaterial = new Material();

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

    private RoadLayers createNaviRoadLayer() {
        float roadRate = 0.8f;
        ARWayRoadBuffredObject bottom = new ARWayRoadBuffredObject(mOptions.roadWidth);
        ARWayRoadBuffredObject road = new ARWayRoadBuffredObject(mOptions.roadWidth * roadRate);
        bottom.setColor(mColors.bottomRoad);
        road.setColor(mColors.road);

        ARWayRoadBuffredObject refline = new ARWayRoadBuffredObject(mOptions.refLineHeight, mOptions.refLineWidth, mOptions.refLineStepLength);
        refline.setColor(mColors.refLine);

        ARWayRoadBuffredObject navi = new ARWayRoadBuffredObject(mOptions.naviRoadWidth);
        navi.setColor(mColors.naviLine);

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

    @Override
    public int removeNaviPath() {
       return removeNaviPath(0);
    }

    @Override
    public int removeRoadNet() {
        return removeRoadNet(0);
    }

    @Override
    public int removeFloor() {
        return removeFloor(0);
    }

    private int removeNaviPath(int index) {
        if(index<0||  mNaviRoadList.size()<= index){
            return -1;
        }
        mNaviRoadList.remove(index);
        mIsNaviRoadDirty = true;
        commitRender();
        return 0;
    }

    private int removeRoadNet(int index) {
        if(index<0||  mCrossRoadList.size()<= index){
            return -1;
        }
        mCrossRoadList.remove(index);
        mIsCrossRoadDirty = true;
        commitRender();
        return 0;
    }

    private int removeFloor(int index) {
        if(index<0||  mFloorObjectLayerList.size()<= index){
            return -1;
        }
        mFloorObjectLayerList.remove(index);
        mIsFloorDirty = true;
        commitRender();
        return 0;
    }

    private TileFloor createFloor(float width, float height, float spacing, float widthrate){
        if (width<=0 || height<=0 || spacing<=0){
            return null;
        }
        TileFloor tileFloor = new TileFloor(width,height,spacing,widthrate);
        tileFloor.setMaterial(mFloorMaterial);
        tileFloor.setBlendingEnabled(true);
        tileFloor.setBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        tileFloor.setDepthTestEnabled(false);
        tileFloor.setDepthMaskEnabled(false);
        tileFloor.setColor(Color.DKGRAY);
        return  tileFloor;
    }
    private RoadLayers createNetRoadLayer(){
        float roadRate = 0.9f;
        float roadWidth = mOptions.netRoadWidth;
        ARWayRoadBuffredObject bottom = new ARWayRoadBuffredObject(roadWidth, mColors.netRoadBottom);
        bottom.setColor(mColors.netRoadBottom);
        ARWayRoadBuffredObject road = new  ARWayRoadBuffredObject(roadWidth*roadRate, mColors.netRoad);
        road.setColor(mColors.netRoad);
        ARWayRoadBuffredObject.ShapeType type = ARWayRoadBuffredObject.ShapeType.TEXTURE_ROAD;
        if(!ARWayConst.IS_USE_ROAD_TEXTURE){
            type = ARWayRoadBuffredObject.ShapeType.VERTICE_ROAD;
        }
        bottom.setShapeType(type);
        road.setShapeType(type);
        RoadLayers roadLayers = new RoadLayers(bottom, road);
        return roadLayers;
    }

    public RoadRenderOption getRenderOptions() {
        return mOptions;
    }

    @Override
    public void renderFloor(float left,float top,float right,float bottom,float spacing,float widthrate) {
//        mFloorObjectLayerList.clear();
        TileFloor floor = createFloor(right-left,top-bottom,spacing,widthrate);
        if (floor != null) {
            ObjectLayer objectLayer = new ObjectLayer(floor);
            objectLayer.object.setPosition((right+left)/2,(top+bottom)/2,0);
            mFloorObjectLayerList.add(objectLayer);
            mIsFloorDirty = true;
        }
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
        if (IS_DEBUG_MODE) {
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, String.format("renderNaviPath,path size is %s", path.size()));
        }
        if (mSceneUpdaterRecorder != null) {
            mSceneUpdaterRecorder.start();
        }
        RoadLayers roadLayers = createNaviRoadLayer();
        // TODO: 2016/11/2
//        mNaviRoadList.clear();
        mNaviRoadList.add(roadLayers);
        roadLayers.bottom.setFogEnable(false);
        roadLayers.road.setFogEnable(false);

        roadLayers.bottom.setPosition(offset);
        roadLayers.road.setPosition(offset);
        roadLayers.refLine.setPosition(offset);
        roadLayers.navi.setPosition(offset);

        result &= roadLayers.bottom.updateBufferedRoad(path, offset);
        result &= roadLayers.road.updateBufferedRoad(path, offset);
        result &= roadLayers.navi.updateBufferedRoad(path, offset);
        result &= roadLayers.refLine.updateReferenceLine(path, offset);

        mIsNaviRoadDirty = true;

        if (mSceneUpdaterRecorder != null) {
            mSceneUpdaterRecorder.recordeAndLog("performance","renderNaviPath");
        }

        return result;
    }

    public BaseObject3D getCarObject() {
        return mCarObject;
    }

    public void renderBoard() {
        //保持显示效果为垂直
//        mNaviSymbolLayer.setRotation(Vector3.X,90);

        Material boadMaterial = new Material();
        boadMaterial.setColorInfluence(0.1f);

        Bitmap bitmap =  TDrawText.drawBitmapText("halo hud",15,0,new int[4],Color.WHITE,Color.BLUE,
                Color.BLACK,1);
        try {
            boadMaterial.addTexture(new Texture("timeTexture", bitmap));
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }

        Object3D object = new Plane(1f,0.5f,10,10, Vector3.Axis.Z,
                true,false,1,true);
        object.setPosition(0,0,0);
        object.setMaterial(boadMaterial); //mNaviSymbolMaterial
        object.setDoubleSided(true);
        object.setColor(Color.GREEN);
        object.setScale(3);

        mNaviSymbolLayer.clearChildren();
        mNaviSymbolLayer.addChild(object);

        mCarObject.clearChildren();
    }

    /**
     *
     * @param position
     * @param degree 以3点钟为0度，逆时针计算
     */
    public void renderTrafficLight(Vector3 position,float degree){
        final TimeRecorder recorder = new TimeRecorder();
        recorder.start();

        mNaviSymbolLayer.setPosition(position);
        if(mNaviSymbolLayer.getNumChildren()<=0){
            int objectId = R.raw.arway_car4;
            final LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(),
                    mTextureManager, objectId);
            mRenderer.loadModel(objParser, new IAsyncLoaderCallback() {
                @Override
                public void onModelLoadComplete(ALoader aLoader) {
                    final Object3D object = objParser.getParsedObject();
                    object.setScale(0.3f);
                    object.setAlpha(0);
                    object.setColor(Color.BLUE);
                    object.setRotation(Vector3.Axis.X,-90);
                    mNaviSymbolLayer.clearChildren();
                    mNaviSymbolLayer.addChild(object);

                    Animation3D animation = new Animation3D() {
                        @Override
                        protected void applyTransformation() {
                            object.setAlpha((float) getInterpolatedTime());
                        }
                    };
                    animation.setTransformable3D(object);
                    mScene.registerAnimation(animation);
                    animation.setDurationMilliseconds(3000);
//                    animation.play();
                    recorder.recordeAndLog(ARWayConst.ERROR_LOG_TAG,"object parser ");
                }

                @Override
                public void onModelLoadFailed(ALoader aLoader) {
                    HaloLogger.logE(ARWayConst.ERROR_LOG_TAG,"Model load failed: " + aLoader);
                }
            }, objectId);
        }
        mNaviSymbolLayer.setRotation(Vector3.Axis.Z,-90-degree);

    }

    private void initCarObject(){

        Object3D object = new Plane(1f,0.5f,10,10, Vector3.Axis.Z,
                true,false,1,true);
        object.setMaterial(mCarMaterial);
        object.setTransparent(true);
        object.rotate(Vector3.Axis.Z,90);
        object.setPosition(0,1*0.15,0);
        mCarObject.clearChildren();
        mCarObject.addChild(object);

    }

    @Override
    public boolean renderGuideLine(List<Vector3> path) {
        if (path == null || path.size()<2) {
            return false;
        }
        int pathSize = path.size();
        final Vector3 offset = new Vector3(path.get(0));
        if (mIndicationLine != null) {
            mNaviGuideLineLayer.removeChild(mIndicationLine);
            mIndicationLine = null;
        }
        if (mIndicationLine == null) {
            mIndicationLine = new ARWayRoadBuffredObject(mOptions.guideLineWidth,mColors.guideLine, ARWayRoadBuffredObject.ShapeType.TEXTURE_ROAD);
            mIndicationLine.setMaterial(mCommonRoadMaterial);
            mIndicationLine.setColor(mColors.guideLine);
            if (mNaviGuideLineLayer != null) {
                mNaviGuideLineLayer.addChild(mIndicationLine);
            }
        }
        if (mIndicationArrow == null) {
            mIndicationArrow = new Plane(mOptions.guideLineWidth*4, mOptions.guideLineWidth*4,10,10, Vector3.Axis.Z,
                    true,false,1,true);
            mIndicationArrow.setColor(mColors.guideLine);
            mIndicationArrow.setMaterial(mArrowMaterial);
            mIndicationArrow.setBlendingEnabled(true);
            mIndicationArrow.setBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            if (mNaviGuideLineLayer != null) {
                mNaviGuideLineLayer.addChild(mIndicationArrow);
            }
        }
        mNaviGuideLineLayer.setPosition(0,0,0);
        mIndicationLine.setPosition(offset);
        mIndicationLine.updateBufferedRoad(path,offset);

        Vector3 start = path.get(pathSize-2);
        Vector3 end = path.get(pathSize-1);
        float cDegree = (float) Math.toDegrees(Math.atan2((end.y-start.y),(end.x-start.x)));
//        mIndicationArrow.setPosition(Vector3.subtractAndCreate(end,offset));
        mIndicationArrow.setPosition(end);
        mIndicationArrow.setRotation(Vector3.Axis.Z,-(cDegree-90));

        return true;
    }

    @Override
    public boolean renderRoadNet(List<List<Vector3>> cross) {
        if (cross == null || cross.size()<1) {
            return false;
        }
        boolean result = true;
        // TODO: 2016/11/2
//        mCrossRoadList.clear();
        int crossSize = cross.size();
        Vector3 offset = new Vector3();
        List<RoadLayers> netList = new ArrayList<>();
        for (int i = crossSize-1; i >= 0; i--) {
            List<Vector3> road = cross.get(i);
            if (road != null && road.size()>0) {
                offset.setAll(road.get(0));
                RoadLayers roadLayers = createNetRoadLayer();
                netList.add(roadLayers);
                Vector3 fogStart = road.get(0);
                Vector3 fogEng = road.get(road.size()-1);
                if(true && i==0){
                    fogStart = new Vector3(0,0,0);
                    fogEng = new Vector3(0.01,0,0);
                }
                boolean isFog = mOptions.isRoadFog;
                roadLayers.road.setFogEnable(isFog);
                roadLayers.bottom.setFogEnable(isFog);
                roadLayers.road.updateBufferedRoad(road,offset);
                roadLayers.bottom.updateBufferedRoad(road,offset);
                roadLayers.road.setFogStart(fogStart);
                roadLayers.road.setFogEnd(fogEng);
                roadLayers.bottom.setFogStart(fogStart);
                roadLayers.bottom.setFogEnd(fogEng);
                roadLayers.road.setPosition(offset);
                roadLayers.bottom.setPosition(offset);
            }
        }
        mCrossRoadList.add(netList);
        mIsCrossRoadDirty = true;
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
    public void commitRender() {
        if(IS_DEBUG_MODE){
            HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, String.format("commitRender called"));
        }
        if (mIsNaviRoadDirty){
            mIsNaviRoadDirty = false;
            if(mNaviRoadList.size()>0){
                mNaviRoadBottom.clearChildren();
                mNaviRoad.clearChildren();
                mNaviRoadTop.clearChildren();
                mNaviRoadRefLine.clearChildren();

                for(RoadLayers roadLayers:mNaviRoadList) {
                    roadLayers.bottom.setMaterial(mCrossRoadBottomMaterial);
                    roadLayers.navi.setMaterial(mMainRoadMaterial);
                    roadLayers.road.setMaterial(mCrossRoadTopMaterial);
                    roadLayers.refLine.setMaterial(mRoadReflineMaterial);

                    mNaviRoadBottom.addChild(roadLayers.bottom);
                    mNaviRoad.addChild(roadLayers.navi);
                    mNaviRoadTop.addChild(roadLayers.road);
                    mNaviRoadRefLine.addChild(roadLayers.refLine);
                }
            }
        }
        if (mIsCrossRoadDirty){
            mIsCrossRoadDirty = false;
            if(mCrossRoadList.size()>0){
                Vector3 offset = new Vector3(0);
                mCrossRoadBottom.setPosition(offset);
                mCrossRoad.setPosition(offset);
                mCrossRoadBottom.clearChildren();
                mCrossRoad.clearChildren();
                for ( List<RoadLayers> roadNet:mCrossRoadList) {
                    for(RoadLayers roadLayers:roadNet) {
                        roadLayers.bottom.setMaterial(mCrossRoadBottomMaterial);
                        roadLayers.road.setMaterial(mCrossRoadTopMaterial);
                        mCrossRoadBottom.addChild(roadLayers.bottom);
                        mCrossRoad.addChild(roadLayers.road);
                    }
                }
            }
        }
        if (mIsFloorDirty){
            mIsFloorDirty = true;
            if(mFloorObjectLayerList.size()>0){
                mGridfloorLayer.clearChildren();
                for(ObjectLayer layer:mFloorObjectLayerList){
                    mGridfloorLayer.addChild(layer.object);
                }
            }
        }
    }

    private void reloadAllLayer(){
        mScene.clearChildren();
        addObject(mArwayMap);
    }

    private boolean initAllLayer(){
        boolean result = true;
        mScene.clearChildren();

        mArwayMap = new BaseObject3D();
        mGridfloorLayer = new BaseObject3D();
        mCrossRoadBottom = new BaseObject3D();
        mCrossRoad = new BaseObject3D();
        mNaviRoadBottom = new BaseObject3D();
        mNaviRoadTop = new BaseObject3D();
        mNaviRoad = new BaseObject3D();
        mNaviRoadRefLine = new BaseObject3D();
        mNaviGuideLineLayer = new BaseObject3D();
        mNaviSymbolLayer = new BaseObject3D();
        mCarObject = new BaseObject3D();

        Object3D[] layers = new Object3D[]{mGridfloorLayer,mCrossRoadBottom,mCrossRoad,
                mNaviRoadBottom,mNaviRoadTop,mNaviRoad,mNaviRoadRefLine,
                mNaviGuideLineLayer,mNaviSymbolLayer,mCarObject};
        for(Object3D layer:layers){
            if (layer != null) {
                mArwayMap.addChild(layer);
            }
        }
        mScene.addChild(mArwayMap);
        HaloLogger.logE(TAG,String.format("initAllLayer,Scene child =%s,mArwayMap child =%s",mScene.getNumChildren(),mArwayMap.getNumChildren()));
        return result;
    }

    private void clearSceneObjects(){
        mGridfloorLayer.clearChildren();
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
    private boolean clearRoadnetwork(){
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
    private boolean clearNaviRoad(){
        boolean result = true;
        BaseObject3D[] layers = new BaseObject3D[]{mNaviRoadBottom,mNaviRoadTop,mNaviRoad,mNaviRoadRefLine, mNaviGuideLineLayer};
        for(BaseObject3D layer:layers){
            if (layer != null) {
                layer.clearChildren();
            }
        }
        return result;
    }

    public void onRender(long ellapsedRealtime, double deltaTime) {
//        HaloLogger.logE("onRender",String.format("postion is %s",mNaviSymbolLayer.getPosition()));
    }
}

