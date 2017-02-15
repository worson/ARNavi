package com.haloai.hud.hudendpoint.arwaylib.render.scene;

import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;

import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.render.object3d.ARWayRoadBuffredObject;
import com.haloai.hud.hudendpoint.arwaylib.render.object3d.BaseObject3D;
import com.haloai.hud.hudendpoint.arwaylib.render.object3d.SimpleArrowObject;
import com.haloai.hud.hudendpoint.arwaylib.render.object3d.TileFloor;
import com.haloai.hud.hudendpoint.arwaylib.render.options.RoadRenderOption;
import com.haloai.hud.hudendpoint.arwaylib.render.shader.RoadFogMaterialPlugin;
import com.haloai.hud.hudendpoint.arwaylib.render.shader.TextureAlphaMaterialPlugin;
import com.haloai.hud.hudendpoint.arwaylib.render.vertices.GeometryData;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;
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
import org.rajawali3d.renderer.AFrameTask;
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
    private BaseObject3D mCrossRefLine       = null;
    private BaseObject3D mNaviRoadBottom     = null;
    private BaseObject3D mNaviRoadTop        = null;
    private BaseObject3D mNaviRoad           = null;
    private BaseObject3D mNaviRoadRefLine    = null;
    private BaseObject3D mNaviGuideLineLayer = null;
    private BaseObject3D mNaviSymbolLayer    = null;
    private BaseObject3D mNaviCameraLayer    = null;
    private BaseObject3D mCarObject          = null;
    private BaseObject3D mStarEndLayer       = null;

    //plugin layer
    private BaseObject3D mYawLaneLayer          = null;
    private BaseObject3D mTrafficDetectionLayer = null;
    private BaseObject3D mAdasCarObject;


    private List<List<RoadLayers>> mCrossRoadList   = new ArrayList();
    private List<RoadLayers>       mNaviRoadList    = new ArrayList();
    private List<RoadLayers>       mEndNaviRoadList = new ArrayList();
//    private List<ObjectLayer> mFloorObjectLayerList = new ArrayList<>();

    private List<Object3D> mTrafficLightObjects = new ArrayList<>();

    private boolean mIsNaviTailRoadDirty = true;
    private boolean mIsNaviRoadDirty     = true;
    private boolean mIsCrossRoadDirty    = true;
    private boolean mIsFloorDirty        = true;
    private boolean mIsGuideLineDirty    = true;

    //VERTICE_ROAD
    private RoadRenderOption             mOptions = new RoadRenderOption();
    private RoadRenderOption.LayersColor mColors  = null;

    private Material       mRoadMaterial            = null;
    private Material       mTestMaterial            = null;
    private Material       mCrossRoadBottomMaterial = null;
    private Material       mCrossRoadTopMaterial    = null;
    private Material       mCrossRefMaterial        = null;
    private Material       mMainRoadMaterial        = null;
    private Material       mRoadReflineMaterial     = null;
    private Material       mCommonRoadMaterial      = null;
    private Material       mArrowMaterial           = null;
    private Material       mFloorMaterial           = null;
    private Material       mCarMaterial             = null;
    private Material       mNaviSymbolMaterial      = null;
    private Material       mNaviIconMaterial        = null;
    private Material       mCameraIconMaterial      = null;
    private List<Material> mMaterialList            = new ArrayList<>();

    private Texture mCrossNetTexture = null;

    private static ArwaySceneUpdater mArwaySceneUpdater    = null;//new ArwaySceneUpdater(null)
    private        TimeRecorder      mSceneUpdaterRecorder = new TimeRecorder();

    private boolean mMaskRenderGuideLine = false;
    private boolean mMaskRenderFloor     = false;
    private boolean mMaskRenderNet       = false;
    private boolean mNaviPathMask        = false;

    private class ObjectLayer {
        //        private Vector3 postion = new Vector3();
        private Object3D object = null;

        public ObjectLayer(Object3D object) {
            this.object = object;
        }
    }

    private class RoadLayers {
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

    public static ArwaySceneUpdater getInstance() {
        if (!IS_SINGLETON || mArwaySceneUpdater == null) {
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


    public void initScene() {
        RajLog.setDebugEnabled(false);
        initRoadMaterial();
        initTextureMaterial();

        initAllLayer();
//                initStaticLayer();
        initCarObject();
    }

    public BaseObject3D getYawLaneLayer() {
        return mYawLaneLayer;
    }

    public BaseObject3D getTrafficDetectionLayer() {
        return mTrafficDetectionLayer;
    }

    public BaseObject3D getAdasCarObject() {
        return mAdasCarObject;
    }

    public void reset() {
        AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mNaviRoadList.clear();
                mCrossRoadList.clear();
//                mFloorObjectLayerList.clear();

                mNaviGuideLineLayer.clearChildren();
                mStarEndLayer.clearChildren();

                mIndicationLine = null;
                mIndicationArrow = null;

                mIsNaviRoadDirty = true;
                mIsCrossRoadDirty = true;
                mIsFloorDirty = true;
                mIsGuideLineDirty = true;

                mGridfloorLayer.setPosition(0, 0, 0);
                reloadAllLayer();
            }
        };
        internalOfferTask(task);

        // TODO: 11/01/2017 在task内会造成死锁
        commitRender();
    }

    @Override
    public void setScene(Scene scene) {
        super.setScene(scene);
    }

    private void initTextureMaterial() {
        int[] textureIds = new int[]{};
        List<Material> materialList = new LinkedList();
        Material m = new Material();
        m.getTextureList().clear();
    }

    private void initRoadMaterial() {
        int[] textureIds = new int[]{R.drawable.road_circle_alpha_change, R.drawable.triangle_arrow,
                R.drawable.road_circle_alpha_change, R.drawable.road_navi_arrow, R.drawable.arway_tile_floor,
                R.drawable.arway_tex_car_1, R.drawable.road_circle_alpha_change, R.drawable.scene_traffic_light,
                R.drawable.road_circle_alpha_change, R.drawable.scene_camera_iocn};
        List<Material> materialList = new LinkedList();
        List<Texture> texturelList = new LinkedList();
        float colorInfluence = 1;
        float textureInfluence = 1;
        boolean useVertexColor = false;
        if (!ARWayConst.IS_USE_ROAD_TEXTURE) {
            colorInfluence = 1;
            textureInfluence = 0;
            useVertexColor = true;
        }
        for (int i = 0; i < textureIds.length; i++) {
            Material material = new Material();
            material.useVertexColors(useVertexColor);
            material.setColorInfluence(colorInfluence);
            materialList.add(material);
            int id = textureIds[i];
            Texture texture = new Texture(ATexture.TextureType.DIFFUSE, "road_texture" + i, id);
            texture.setFilterType(ATexture.FilterType.LINEAR);
            texture.setWrapType(ATexture.WrapType.REPEAT);
            texture.setMipmap(false);
            texture.setInfluence(textureInfluence);
            texturelList.add(texture);
            try {
                material.addTexture(texture);
            } catch (ATexture.TextureException e) {
                HaloLogger.logE("initRoadMaterial", "initRoadMaterial addTexture failed");
                e.printStackTrace();
            }
        }
        ATexture texture = null;
        mCommonRoadMaterial = materialList.get(0);
        mMainRoadMaterial = mCommonRoadMaterial;

        mArrowMaterial = materialList.get(1);
        mArrowMaterial.getTextureList().get(0).setInfluence(1f);

        mCrossRoadBottomMaterial = materialList.get(2);
        mCrossRoadTopMaterial = mCrossRoadBottomMaterial;

        mRoadReflineMaterial = materialList.get(3);

        mFloorMaterial = materialList.get(4);

        texture = mFloorMaterial.getTextureList().get(0);
        if (texture != null) {
            texture.setInfluence(0.25f);
        }
        mCarMaterial = materialList.get(5);
        mCarMaterial.setColorInfluence(0);

        mCrossRefMaterial = materialList.get(6);

        mNaviIconMaterial = materialList.get(7);
        mNaviIconMaterial.setColorInfluence(0);

        mCrossNetTexture = texturelList.get(8);

        mCameraIconMaterial = materialList.get(9);
        mCameraIconMaterial.setColorInfluence(0);

        mRoadMaterial = new Material();
        mTestMaterial = new Material();
        mRoadMaterial.useVertexColors(true);
        mTestMaterial.setColor(Color.GREEN);

        mNaviSymbolMaterial = new Material();

        mCrossRoadBottomMaterial.addPlugin(new RoadFogMaterialPlugin());
        // TODO: 2016/11/8 不能共用一个plugin
//        mCrossRefMaterial.addPlugin(new RoadFogMaterialPlugin());

        mMainRoadMaterial.addPlugin(new TextureAlphaMaterialPlugin());
        mRoadReflineMaterial.addPlugin(new TextureAlphaMaterialPlugin());
        mArrowMaterial.addPlugin(new TextureAlphaMaterialPlugin());
        mFloorMaterial.addPlugin(new TextureAlphaMaterialPlugin());
        mFloorMaterial.setColorInfluence(0.7f);
        mCrossRefMaterial.addPlugin(new TextureAlphaMaterialPlugin());
        {
            mMaterialList.add(mCrossRoadBottomMaterial);
            mMaterialList.add(mCrossRoadTopMaterial);
            mMaterialList.add(mMainRoadMaterial);
            mMaterialList.add(mRoadReflineMaterial);
            mMaterialList.add(mCommonRoadMaterial);
            mMaterialList.add(mArrowMaterial);
            mMaterialList.add(mFloorMaterial);
            mMaterialList.add(mCrossRefMaterial);
        }

    }

    private RoadLayers createNaviRoadLayer() {
        float roadRate = 0.8f;
        ARWayRoadBuffredObject bottom = new ARWayRoadBuffredObject(mOptions.roadWidth);
        ARWayRoadBuffredObject road = new ARWayRoadBuffredObject(mOptions.roadWidth * roadRate);
        bottom.setColor(mColors.bottomRoad);
        road.setColor(mColors.road);

        ARWayRoadBuffredObject refline = new ARWayRoadBuffredObject(mOptions.refLineHeight, mOptions.refLineWidth, mOptions.refLineStepLength, false);
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
        if (mNaviRoadList.size() <= 0) {
            return -1;
        }
        AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mNaviRoadList.clear();
            }
        };
        internalOfferTask(task);
        return 0;
    }

    @Override
    public int removeRoadNet() {
        return removeRoadNet(0);
    }

    @Override
    public int removeFloor() {
        AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mGridfloorLayer.clearChildren();
            }
        };
        internalOfferTask(task);

        return 0;
    }

    @Override
    public void moveCenterFloor(float x, float y) {
        mGridfloorLayer.setPosition(x, y, 0);
    }

    private int removeRoadNet(final int index) {
        if (index < 0 || mCrossRoadList.size() <= index) {
            return -1;
        }
        AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mCrossRoadList.remove(index);
            }
        };
        internalOfferTask(task);
        /*mIsCrossRoadDirty = true;
        commitRender();*/
        return 0;
    }

    private int removeFloor(final int index) {
        if (index < 0 || mGridfloorLayer.getNumChildren() <= index) {
            return -1;
        }
        AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mGridfloorLayer.removeChild(mGridfloorLayer.getChildAt(index));
            }
        };
        internalOfferTask(task);
        return 0;
    }

    private TileFloor createFloor(float width, float height, float spacing, float widthrate) {
        if (width <= 0 || height <= 0 || spacing <= 0) {
            return null;
        }

        TileFloor tileFloor = new TileFloor(width, height, spacing, widthrate);
        tileFloor.setMaterial(mFloorMaterial);
        tileFloor.setBlendingEnabled(true);
        tileFloor.setBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        tileFloor.setDepthTestEnabled(false);
        tileFloor.setDepthMaskEnabled(false);
        tileFloor.setColor(Color.DKGRAY);
        return tileFloor;
    }

    private RoadLayers createNetRoadLayer() {
        float roadRate = 0.8f;
        float roadWidth = mOptions.netRoadWidth;
        ARWayRoadBuffredObject bottom = new ARWayRoadBuffredObject(roadWidth, mColors.netRoadBottom);
        bottom.setColor(mColors.netRoadBottom);
        ARWayRoadBuffredObject road = new ARWayRoadBuffredObject(roadWidth * roadRate, mColors.netRoad);
        road.setColor(mColors.netRoad);
        ARWayRoadBuffredObject refline = new ARWayRoadBuffredObject(mOptions.netRefLineHeight, mOptions.netRrefLineWidth, mOptions.refLineStepLength, true);
        refline.setColor(mColors.netRefLine);
        refline.setTransparent(true);
        ARWayRoadBuffredObject.ShapeType type = ARWayRoadBuffredObject.ShapeType.TEXTURE_ROAD;
        if (!ARWayConst.IS_USE_ROAD_TEXTURE) {
            type = ARWayRoadBuffredObject.ShapeType.VERTICE_ROAD;
        }
        bottom.setShapeType(type);
        road.setShapeType(type);
        RoadLayers roadLayers = new RoadLayers(bottom, road);
        roadLayers.refLine = refline;
        return roadLayers;
    }

    public RoadRenderOption getRenderOptions() {
        return mOptions;
    }

    public void renderFloor(final float left, final float top, final float right, final float bottom, final float spacing, final float widthrate) {
        if (mMaskRenderFloor) {
            return;
        }
        final float width = right - left;
        final float height = top - bottom;
        if (width <= 0 || height <= 0 || spacing <= 0) {
            return;
        }
        final GeometryData geometryData = TileFloor.getGeometryData(width, height, spacing, widthrate);

        AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                TileFloor tileFloor = new TileFloor(geometryData);
                tileFloor.setMaterial(mFloorMaterial);
                tileFloor.setBlendingEnabled(true);
                tileFloor.setBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
                tileFloor.setDepthTestEnabled(false);
                tileFloor.setDepthMaskEnabled(false);
                tileFloor.setColor(Color.DKGRAY);
                tileFloor.setPosition((right + left) / 2, (top + bottom) / 2, 0);
                mGridfloorLayer.addChild(tileFloor);
            }
        };
        internalOfferTask(task);

    }

    private RoadLayers rRenderNaviPath(RoadLayers roadLayers, List<Vector3> path) {
        HaloLogger.postI(ARWayConst.NECESSARY_LOG_TAG, String.format("renderNaviPath enter,path size is %s,origin child is %s", path.size(), mNaviRoadList.size()));
        // TODO: 2016/11/2
        final Vector3 offset = new Vector3(path.get(0));
//        mNaviRoadList.clear();
        roadLayers.bottom.setFogEnable(false);
        roadLayers.road.setFogEnable(false);

        roadLayers.bottom.setPosition(offset);
        roadLayers.road.setPosition(offset);
        roadLayers.refLine.setPosition(offset);
        roadLayers.navi.setPosition(offset);

        roadLayers.bottom.updateBufferedRoad(path, offset);
        roadLayers.road.updateBufferedRoad(path, offset);
        roadLayers.navi.updateBufferedRoad(path, offset);
        roadLayers.refLine.updateReferenceLine(path, offset);

        HaloLogger.postI(ARWayConst.NECESSARY_LOG_TAG, String.format("renderNaviPath eixt"));
        return roadLayers;
    }

    /**
     * 渲染当前显示的道路
     *
     * @param path
     * @return
     */
    public boolean renderNaviPath(List<Vector3> path) {
        return renderNaviPath(path, mOptions.refLineStepLength);
    }

    public boolean renderNaviPath(List<Vector3> path, float stepLength) {
        if (mNaviPathMask || path == null || path.size() < 2) {
            HaloLogger.logE(ARWayConst.NECESSARY_LOG_TAG, "renderNaviPath error path!");
            return false;
        }
        final float fStepLength = stepLength;
        final List<Vector3> fPath = new ArrayList<>(path);
        HaloLogger.logE(ARWayConst.NECESSARY_LOG_TAG, "renderNaviPath init ,called");
        AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                if (IS_DEBUG_MODE) {
                    HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, String.format("renderNaviPath,path size is %s,origin child is %s", fPath.size(), mNaviRoadList.size()));
                }
                if (mSceneUpdaterRecorder != null) {
                    mSceneUpdaterRecorder.start();
                }
                RoadLayers roadLayers = createNaviRoadLayer();
                roadLayers.refLine.setStepLength(fStepLength);
                mNaviRoadList.add(rRenderNaviPath(roadLayers, fPath));
                mIsNaviRoadDirty = true;

                if (mSceneUpdaterRecorder != null) {
                    mSceneUpdaterRecorder.recordeAndLog("performance", "renderNaviPath");
                }
            }
        };
        internalOfferTask(task);
        boolean result = true;
        return result;
    }


    public Object3D getCarObject() {
        return mCarObject;
    }

    public void setCarVisiable(final boolean visiable) {
        if (mCarObject != null) {
            mCarObject.setVisible(visiable);
        }

    }

    private Object3D createPlane(float width, float height, boolean orth, Material material) {
        Plane plane = new Plane(width, height, 10, 10, Vector3.Axis.Z,
                true, false, 1, true);//,Color.BLACK
//            plane.setDoubleSided(true);
        plane.setTransparent(true);
        plane.setPosition(0, 0, height / 2);
        // plane.setAlpha(0.1f);//不作用于顶点颜色
        plane.setMaterial(material);
        if (!orth) {
            return plane;
        }

        BaseObject3D baseObject3D = new BaseObject3D();
        baseObject3D.setOrthographic(true, 0.04f);
        baseObject3D.addChild(plane);

        return baseObject3D;
    }

    private Object3D createSymbolPlane(Material material) {
        Plane plane = new Plane(1f, 1f, 10, 10, Vector3.Axis.Z,
                true, false, 1, true);//,Color.BLACK
//            plane.setDoubleSided(true);
        plane.setTransparent(true);
        plane.setPosition(0, 0, 0.5);
        // plane.setAlpha(0.1f);//不作用于顶点颜色
        plane.setMaterial(material);

        BaseObject3D baseObject3D = new BaseObject3D();
        baseObject3D.setOrthographic(true, 0.04f);
        baseObject3D.addChild(plane);


        return baseObject3D;
    }

    private List<Vector3> findStepPoint(Vector3 start, double angle, float distStep, float length) {
        Vector3 v = new Vector3();
        List<Vector3> list = new ArrayList<>();
        boolean over = true;
        list.add(start);
        double totalLength = distStep;
        while (over) {
            if (totalLength <= length) {
                MathUtils.longerPoint(v, start, angle, totalLength);
                totalLength += distStep;
                list.add(new Vector3(v));
            } else {
                over = false;
            }
        }
        return list;
    }
    @Override
    public void renderEndScene(List<Vector3> list) {

        final List<Vector3> path = new ArrayList<>(list);
        AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                Vector3 start = new Vector3(path.get(path.size() - 2));
                Vector3 end = new Vector3(path.get(path.size() - 1));
                double angle = Math.atan2((end.y - start.y), (end.x - start.x));
                float roadWidth = mOptions.netRoadWidth * 2;
                float floorDis = 0.3f;
                float lineDis = floorDis + 2.5f;
                Vector3 position = new Vector3();
                Vector3 poiPos = new Vector3();

                MathUtils.longerPoint(position, start, end, floorDis);

                List<Vector3> floorPoss = findStepPoint(end, angle, roadWidth / 4, floorDis);
                List<Vector3> extendRoad = findStepPoint(end, angle, lineDis / 2, lineDis);

                List<Vector3> lines = floorPoss;
                Vector3 line0 = new Vector3(lines.get(lines.size() - 2));
                Vector3 line1 = new Vector3(lines.get(lines.size() - 1));

                MathUtils.crossLlongerPoint(poiPos, line1, line0, RoadRenderOption.ROAD_DEVIATION_DISTANCE);

                float direction = (float) Math.atan2(end.y - start.y, end.x - start.x);

                Material material = new Material();
                Texture texture = new Texture(ATexture.TextureType.DIFFUSE, "end_scene_texture", R.drawable.arway_destination_floor);
                material.setColorInfluence(0);
                try {
                    material.addTexture(texture);
                } catch (ATexture.TextureException e) {
                    e.printStackTrace();
                }

                for (Vector3 pos : floorPoss) {
                    Plane plane = new Plane(roadWidth, roadWidth / 4, 10, 10, Vector3.Axis.Z,
                            true, false, 1, true);//,Color.BLACK
                    plane.setDoubleSided(true);
                    plane.setTransparent(true);
                    plane.setMaterial(material);
                    plane.setPosition(pos);
                    plane.setRotation(Vector3.Axis.Z, 90 - Math.toDegrees(direction));
                    plane.setDepthMaskEnabled(false);
                    plane.setDepthTestEnabled(false);
                    mStarEndLayer.addChild(plane);
                }

                Material poiMat = new Material();
                Texture poTtexture = new Texture(ATexture.TextureType.DIFFUSE, "poTtexture", R.drawable.arway_destination_poi);
                poiMat.setColorInfluence(0);
                try {
                    poiMat.addTexture(poTtexture);
                } catch (ATexture.TextureException e) {
                    e.printStackTrace();
                }
                Object3D endPoi = createPlane(1, 1, true, poiMat);
                endPoi.setPosition(poiPos);
                mStarEndLayer.addChild(endPoi);

                mEndNaviRoadList.clear();
                HaloLogger.logE(ARWayConst.INDICATE_LOG_TAG, "renderEndScene road " + extendRoad);
                RoadLayers roadLayers = createNaviRoadLayer();
                roadLayers.refLine.setStepLength(5);
                rRenderNaviPath(roadLayers, extendRoad);
                mEndNaviRoadList.add(roadLayers);
                mIsNaviRoadDirty = true;
                mIsNaviTailRoadDirty = true;
            }
        };
        internalOfferTask(task);

    }

    public void renderTrafficCamera(Vector3 pos, int type) {
        final Vector3 postion = new Vector3(pos);
        AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                HaloLogger.logE(ARWayConst.NECESSARY_LOG_TAG, String.format("renderTrafficCamera , postion %s ", postion));
                mNaviCameraLayer.setPosition(0, 0, 0);
                mNaviCameraLayer.clearChildren();
                Object3D object3D = createSymbolPlane(mCameraIconMaterial);
                object3D.setPosition(postion);
                mNaviCameraLayer.addChild(object3D);
            }
        };
        internalOfferTask(task);


    }

    public void renderTrafficLight(List<Vector3> lights) {
        if (lights == null || lights.size() <= 0) {
            return;
        }
        final List<Vector3> flights = new ArrayList<>(lights);
        AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                HaloLogger.logE(ARWayConst.NECESSARY_LOG_TAG, "renderTrafficLight");
                mNaviSymbolLayer.setPosition(0, 0, 0);
                mNaviSymbolLayer.clearChildren();
                Object3D object3D = null;
                int cnt = flights.size();
                // TODO: 15/11/2016 清除动作
                mTrafficLightObjects.clear();
                for (int i = 0; i < cnt; i++) {
                    Vector3 position = flights.get(i);
                    object3D = createSymbolPlane(mNaviIconMaterial);
                    mTrafficLightObjects.add(object3D);
                    object3D.setPosition(position);//高度与红绿红的大小有关
                    mNaviSymbolLayer.addChild(object3D);
                }
            }
        };
        internalOfferTask(task);


    }

    /**
     * @param position
     * @param degree   以3点钟为0度，逆时针计算
     */
    private void renderModelTrafficLight(Vector3 position, float degree) {
        final TimeRecorder recorder = new TimeRecorder();
        recorder.start();

        mNaviSymbolLayer.setPosition(position);
        if (mNaviSymbolLayer.getNumChildren() <= 0) {
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
                    object.setRotation(Vector3.Axis.X, -90);
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
                    recorder.recordeAndLog(ARWayConst.ERROR_LOG_TAG, "object parser ");
                }

                @Override
                public void onModelLoadFailed(ALoader aLoader) {
                    HaloLogger.logE(ARWayConst.ERROR_LOG_TAG, "Model load failed: " + aLoader);
                }
            }, objectId);
        }
        mNaviSymbolLayer.setRotation(Vector3.Axis.Z, -90 - degree);

    }

    private void initCarObject() {

        Object3D object = new Plane(1f, 0.5f, 10, 10, Vector3.Axis.Z,
                true, false, 1, true);
        object.setMaterial(mCarMaterial);
        object.setTransparent(true);
        object.setDepthTestEnabled(false);
        object.rotate(Vector3.Axis.Z, 90);
        object.setPosition(0, 1 * 0.15, 0);
        mCarObject.clearChildren();
        mCarObject.addChild(object);

    }

    @Override
    public boolean renderGuideLine(List<Vector3> points) {
        if (mMaskRenderGuideLine || points == null || points.size() < 2) {
            return false;
        }
        final List<Vector3> path = new ArrayList<>(points);
        AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                if (mNaviGuideLineLayer != null) {
                    SimpleArrowObject arrowObject = new SimpleArrowObject(path,mOptions.guideLineWidth,mColors.guideLine);
                    arrowObject.getBody().setMaterial(mCommonRoadMaterial);
                    arrowObject.getArrow().setMaterial(mArrowMaterial);
                    mNaviGuideLineLayer.clearChildren();
                    mNaviGuideLineLayer.addChild(arrowObject.getBody());
                    mNaviGuideLineLayer.addChild(arrowObject.getArrow());
                }
            }
        };
        internalOfferTask(task);
        return true;
    }

    @Override
    public boolean renderRoadNet(List<List<Vector3>> path) {
        if (path == null || path.size() < 1) {
            return false;
        }
        boolean result = true;

        final List<List<Vector3>> cross = new ArrayList<>(path);
        AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {

                // TODO: 2016/11/2 动态加载不清除
//        mCrossRoadList.clear();
                int crossSize = cross.size();
                Vector3 offset = new Vector3();
                List<RoadLayers> netList = new ArrayList<>();
                Vector3 fogStart = new Vector3();
                Vector3 fogEng = new Vector3();

                for (int i = crossSize - 1; i >= 0; i--) {
                    List<Vector3> road = cross.get(i);
                    if (road != null && road.size() > 0) {
                        offset.setAll(road.get(0));
                        RoadLayers roadLayers = createNetRoadLayer();
                        netList.add(roadLayers);

                        Material material = new Material();
                        material.useVertexColors(false);
                        material.setColorInfluence(1);

                        try {
                            material.addTexture(mCrossNetTexture);
                        } catch (ATexture.TextureException e) {
                            e.printStackTrace();
                        }

                        boolean isFog = mOptions.isRoadFog;
                        if (isFog) {
                            RoadFogMaterialPlugin fogPlugin = new RoadFogMaterialPlugin();
                            calculateRoadFogPara(road,fogStart,fogEng );

                            fogPlugin.setFogStartPosition(fogStart);
                            fogPlugin.setFogEndPosition(fogEng);
                            fogPlugin.setIsFog(isFog);
                            material.addPlugin(fogPlugin);
                        }

                        roadLayers.road.setMaterial(material);
                        roadLayers.bottom.setMaterial(material);

                        roadLayers.road.updateBufferedRoad(road, offset);
                        roadLayers.bottom.updateBufferedRoad(road, offset);

                        roadLayers.road.setPosition(offset);
                        roadLayers.bottom.setPosition(offset);

                        if (RoadRenderOption.IS_ROAD_NET_REFLINE) {
                            roadLayers.refLine.setMaterial(material);
                            roadLayers.refLine.updateReferenceLine(road, offset);
                            roadLayers.refLine.setPosition(offset);

                        }

                    }
                }
                mCrossRoadList.add(netList);
                mIsCrossRoadDirty = true;
            }
        };
        internalOfferTask(task);
        return result;
    }


    /**
     * 计算道路渐变的起点和终点位置
     * @param road
     * @param fogStart
     * @param fogEng
     */
    private void calculateRoadFogPara(List<Vector3> road, Vector3 fogStart, Vector3 fogEng) {
        String tag = "renderRoadNet";
        Vector3 offset = new Vector3();
        offset.setAll(road.get(0));
        fogEng.setAll(Vector3.subtractAndCreate(road.get(road.size() - 1), offset));
        float distStep = mOptions.fogDistance;
        float totalDist = 0;
        boolean found = false;
        Vector3 c1 = road.get(road.size() - 1);
        float rate = (mOptions.fogRate >= 0 && mOptions.fogRate < 1) ? mOptions.fogRate : 0;
        Vector3 v = new Vector3(road.get((int) (road.size() * rate)));
        for (int j = road.size(); j > 0; j--) {
            Vector3 c2 = road.get(j - 1);
            double temp = MathUtils.calculateDistance(c1.x, c1.y, c2.x, c2.y);
            if (temp >= distStep) {
                double scale = distStep / temp;
                v.x = c1.x + (c2.x - c1.x) * scale;
                v.y = c1.y + (c2.y - c1.y) * scale;
                v.z = 0;
                totalDist += distStep;
                found = true;
                break;
            } else if (temp < distStep) {
                distStep -= temp;
                c1 = road.get(j - 1);
                totalDist += temp;
//
            }
        }
        if (!found) {
            v = new Vector3(road.get(0));
            float drawDist = mOptions.fogDistance;
            MathUtils.longerPoint(fogEng,v,fogEng,drawDist);
        }
        HaloLogger.logE(tag, "road net totalDist " + totalDist);
        fogStart.setAll(Vector3.subtractAndCreate(v, offset));
    }

    public void setAlpha(float alpha) {
        for (Material m : mMaterialList) {
            if (m != null) {
                m.setColorInfluence(alpha);
                for (ATexture texture : m.getTextureList()) {
                    texture.setInfluence(alpha);
                }
            }

        }
    }

    @Override
    public void commitRender() {
        AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                if (mIsNaviRoadDirty) {
                    mIsNaviRoadDirty = false;

                    mNaviRoadBottom.clearChildren();
                    mNaviRoad.clearChildren();
                    mNaviRoadTop.clearChildren();
                    mNaviRoadRefLine.clearChildren();
                    if (mNaviRoadList.size() > 0 || mEndNaviRoadList.size() > 0) {
                        HaloLogger.postI(ARWayConst.NECESSARY_LOG_TAG, String.format("render commitNaviPath enter"));
                        mNaviRoadBottom.setPosition(0, 0, 0);
                        mNaviRoad.setPosition(0, 0, 0);
                        mNaviRoadTop.setPosition(0, 0, 0);
                        mNaviRoadRefLine.setPosition(0, 0, 0);

                        for (RoadLayers roadLayers : mNaviRoadList) {
                            commitNaviPath(roadLayers);
                        }
                        for (RoadLayers roadLayers : mEndNaviRoadList) {
                            commitNaviPath(roadLayers);
                        }
                        Log.e(ARWayConst.NECESSARY_LOG_TAG, "render commitNaviPath exit");
                        HaloLogger.postI(ARWayConst.NECESSARY_LOG_TAG, String.format("render commitNaviPath exit ,scene size %s, object size %s", mScene.getNumChildren(), mNaviRoadTop.getNumChildren()));
                    } else {
                        HaloLogger.postI(ARWayConst.ERROR_LOG_TAG, "commitNaviPath error !!");
                    }
                }
                if (mIsCrossRoadDirty) {
                    mIsCrossRoadDirty = false;
                    mCrossRoadBottom.clearChildren();
                    mCrossRoad.clearChildren();
                    mCrossRefLine.clearChildren();
                    if (mCrossRoadList.size() > 0) {
                        Vector3 offset = new Vector3(0);
                        mCrossRoadBottom.setPosition(offset);
                        mCrossRoad.setPosition(offset);
                        mCrossRefLine.setPosition(offset);
                        for (List<RoadLayers> roadNet : mCrossRoadList) {
                            for (RoadLayers roadLayers : roadNet) {
                                mCrossRoadBottom.addChild(roadLayers.bottom);
                                mCrossRoad.addChild(roadLayers.road);
                                if (RoadRenderOption.IS_ROAD_NET_REFLINE) {
                                    mCrossRefLine.addChild(roadLayers.refLine);
                                }
                            }
                        }
                    }
                }

            }
        };
        internalOfferTask(task);

    }

    private void commitNaviPath(RoadLayers roadLayers) {
        roadLayers.bottom.setMaterial(mCrossRoadBottomMaterial);
        roadLayers.navi.setMaterial(mMainRoadMaterial);
        roadLayers.road.setMaterial(mCrossRoadTopMaterial);
        roadLayers.refLine.setMaterial(mRoadReflineMaterial);

        mNaviRoadBottom.addChild(roadLayers.bottom);
        mNaviRoad.addChild(roadLayers.navi);
        mNaviRoadTop.addChild(roadLayers.road);
        mNaviRoadRefLine.addChild(roadLayers.refLine);
    }

    private boolean initAllLayer() {
        boolean result = true;
        mGridfloorLayer = new BaseObject3D();
        mCrossRoadBottom = new BaseObject3D();
        mCrossRoad = new BaseObject3D();
        mCrossRefLine = new BaseObject3D();
        mNaviRoadBottom = new BaseObject3D();
        mNaviRoadTop = new BaseObject3D();
        mNaviRoad = new BaseObject3D();
        mNaviRoadRefLine = new BaseObject3D();
        mNaviGuideLineLayer = new BaseObject3D();
        mNaviSymbolLayer = new BaseObject3D();
        mNaviCameraLayer = new BaseObject3D();
        mCarObject = new BaseObject3D();
        mStarEndLayer = new BaseObject3D();

        mYawLaneLayer = new BaseObject3D();
        mTrafficDetectionLayer = new BaseObject3D();
        mAdasCarObject = new BaseObject3D();
        mAdasCarObject.setOrthographic(true);

        reloadAllLayer();
        HaloLogger.logE(TAG, String.format("initAllLayer,Scene child =%s,mArwayMap child =%s", mScene.getNumChildren(), mArwayMap.getNumChildren()));
        return result;
    }

    private void reloadAllLayer() {
        mArwayMap = new BaseObject3D();
        mScene.clearChildren();
        Object3D[] layers = new Object3D[]{
                mGridfloorLayer, mCrossRoadBottom, mNaviRoadBottom, mYawLaneLayer, mCrossRoad,
                mCrossRefLine, mNaviRoadTop, mNaviRoad, mNaviRoadRefLine,
                mNaviGuideLineLayer, mNaviCameraLayer, mNaviSymbolLayer,
                mStarEndLayer, mTrafficDetectionLayer, mAdasCarObject, mCarObject,};

        for (Object3D layer : layers) {
            if (layer != null) {
                mArwayMap.addChild(layer);
            }
        }
        mScene.addChild(mArwayMap);
    }

    @Override
    public void setCurrentPosition(Vector3 curPosition) {
        super.setCurrentPosition(curPosition);

    }
    private TimeRecorder mRenderTimeRecorder = null;

    {
        mRenderTimeRecorder = new TimeRecorder();
        mRenderTimeRecorder.enableTimeFilter(true);
        mRenderTimeRecorder.setLogFilterTime(5000);
    }

    public void onRender(long ellapsedRealtime, double deltaTime) {
        performFrameTasks();
        if (ARWayConst.IS_FRAME_LOG) {
            if (mIsNaviRoadDirty | mIsCrossRoadDirty | mIsFloorDirty) {//|mIsGuideLineDirty
                mRenderTimeRecorder.timerLog(ARWayConst.ERROR_LOG_TAG, String.format("updater , scene %s ", mArwayMap.getNumChildren()
                        + String.format("updater need commit %s ,%s , %s, %s", mIsNaviRoadDirty, mIsCrossRoadDirty, mIsFloorDirty, mIsGuideLineDirty)));
            }
            HaloLogger.logE(TAG, "mCrossRoadBottom size " + mCrossRoadBottom.getNumChildren());
        }
    }
}


