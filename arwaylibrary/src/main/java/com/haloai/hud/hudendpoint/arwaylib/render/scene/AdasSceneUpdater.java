package com.haloai.hud.hudendpoint.arwaylib.render.scene;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.nfc.Tag;

import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.render.object3d.ARWayRoadBuffredObject;
import com.haloai.hud.hudendpoint.arwaylib.render.object3d.BaseObject3D;
import com.haloai.hud.hudendpoint.arwaylib.render.options.RoadRenderOption;
import com.haloai.hud.hudendpoint.arwaylib.render.shader.TextureAlphaMaterialPlugin;
import com.haloai.hud.hudendpoint.arwaylib.render.utils.TDrawText;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.Object3D;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.primitives.Sphere;

import java.util.List;

/**
 * Created by wangshengxing on 19/11/2016.
 */

public class AdasSceneUpdater extends SuperArwaySceneUpdater implements IAdasSceneUpdater {
    public static final String TAG = AdasSceneUpdater.class.getSimpleName();

    public static  boolean IS_SINGLETON  = true;
    private static AdasSceneUpdater mAdasSceneUpdater = null;

    private RoadRenderOption mOptions = null;

    private Object3D mCarObject;
    private BaseObject3D mYawLaneObject;
    private BaseObject3D mTrafficDetectionLayer        = null;

    private Material mCarMaterial             = null;
    private Material mLaneLeftMaterial             = null;
    private Material mLaneRightMaterial             = null;

    private Texture mDistanceBoardTexture;

    private Object3D mTrafficPlaneArrow;
    private Object3D mDistanceBoardObj;

    private double mAdasdistance = 0;
    private volatile boolean mNeedUpdateDistance = false;
    private volatile boolean mResetAdasDistanceDetect = true;
    private Bitmap mAdasDistanceBitmap;


    public AdasSceneUpdater() {
        initObject();
    }

    private void initObject() {
        mCarObject          = new BaseObject3D();
        mYawLaneObject    = new BaseObject3D();
    }


    public void initScene(){
        initMaterial();
//        initCarObject();
        initObject3DCarObject();
    }

    public void reset(){
        mResetAdasDistanceDetect = true;
    }

    private void initMaterial(){
        mCarMaterial = createTextureMaterial(R.drawable.arway_tex_car_1,"adas_car_texture",1,1);
        mLaneLeftMaterial = createTextureMaterial(R.drawable.lane_yaw_left_half_circle,"adas_left_cicrle_texture",1,1);
        mLaneRightMaterial = createTextureMaterial(R.drawable.lane_yaw_right_half_circle,"adas_right_cicrle_texture",1,1);

        mLaneLeftMaterial.addPlugin(new TextureAlphaMaterialPlugin());
        mLaneRightMaterial.addPlugin(new TextureAlphaMaterialPlugin());

        initTrafficDetectionMaterial();
    }
    public static AdasSceneUpdater getInstance (){
        if (!IS_SINGLETON || mAdasSceneUpdater == null) {
            mAdasSceneUpdater = new AdasSceneUpdater();
        }
        return mAdasSceneUpdater;
    }

    public RoadRenderOption getOptions() {
        return mOptions;
    }

    public void setOptions(RoadRenderOption options) {
        mOptions = options;
    }

    public void setYawLaneObject(BaseObject3D yawLaneObject) {
        mYawLaneObject = yawLaneObject;
    }

    public void setTrafficDetectionLayer(BaseObject3D trafficDetectionLayer) {
        mTrafficDetectionLayer = trafficDetectionLayer;
    }

    @Override
    public void showLaneYawLine(List<Vector3> path, boolean left) {
        mYawLaneObject.clearChildren();
        ARWayRoadBuffredObject road = new ARWayRoadBuffredObject(mOptions.roadWidth);
        ARWayRoadBuffredObject.ShapeType type = ARWayRoadBuffredObject.ShapeType.TEXTURE_ROAD;
        road.setColor(0xff955410);
        // TODO: 22/11/2016
//        road.setColor(Color.RED);
        road.setShapeType(type);
        if (!ARWayConst.IS_USE_ROAD_TEXTURE) {
            type = ARWayRoadBuffredObject.ShapeType.VERTICE_ROAD;
        }
        final Vector3 offset = new Vector3(path.get(0));
        road.setPosition(offset);
        road.updateBufferedRoad(path, offset);
        if (left){
            road.setMaterial(mLaneLeftMaterial);
        }else {
            road.setMaterial(mLaneRightMaterial);
        }
        road.setPosition(offset);
        mYawLaneObject.addChild(road);
    }

    @Override
    public void hideLaneYawLine() {
        mYawLaneObject.clearChildren();
    }

    @Override
    public void updateTrafficDetection(double distance, double direction) {
        mNeedUpdateDistance = false;
        mAdasdistance = distance;
        if (mResetAdasDistanceDetect || mTrafficPlaneArrow == null || mDistanceBoardObj == null) {
            mResetAdasDistanceDetect = false;
            initTrafficObject();
        }else {
            mAdasDistanceBitmap = TDrawText.drawBitmapText((int)mAdasdistance+"m",25,0,new int[4],Color.WHITE,Color.BLUE,
                    Color.BLACK,1);
        }


        /*
        float width = mOptions.naviRoadWidth;
        Material boadMaterial = new Material();
        boadMaterial.setColorInfluence(0.1f);
        mAdasDistanceBitmap =  TDrawText.drawBitmapText((int)mAdasdistance+"m",25,0,new int[4],Color.WHITE,Color.BLUE,
                Color.BLACK,1);
        try {
            mDistanceBoardTexture = new Texture("timeTexture", mAdasDistanceBitmap);
//            mDistanceBoardTexture.shouldRecycle(true);
            boadMaterial.addTexture(mDistanceBoardTexture);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        mDistanceBoardObj = createPlane(width,width,false,boadMaterial);*/


        mTrafficDetectionLayer.clearChildren();
        mTrafficDetectionLayer.addChild(mTrafficPlaneArrow);
        mTrafficDetectionLayer.addChild(mDistanceBoardObj);

        mTrafficDetectionLayer.setRotation(Vector3.Axis.Z,direction);
        mNeedUpdateDistance=true;
        HaloLogger.logE(TAG,"updateTrafficDetection");
    }

    public void onRender(long ellapsedRealtime, double deltaTime) {
        if(mNeedUpdateDistance){
            mNeedUpdateDistance = false;
            mDistanceBoardTexture.setBitmap(mAdasDistanceBitmap);
            mRenderer.getTextureManager().replaceTexture(mDistanceBoardTexture);
        }
    }

    private void initTrafficObject(){

        float width = 0.27f;
        float height = width*4.5f;
        float arrowHeight = 0.4f*width;
        mTrafficPlaneArrow =  createPlaneArrow(width,height,arrowHeight);
        Material boadMaterial = new Material();
        boadMaterial.setColorInfluence(0.1f);
        mAdasDistanceBitmap =  TDrawText.drawBitmapText((int)mAdasdistance+"m",25,0,new int[4],Color.WHITE,Color.BLUE,
                Color.BLACK,1);
        try {
            mDistanceBoardTexture = new Texture("timeTexture", mAdasDistanceBitmap);
            mDistanceBoardTexture.shouldRecycle(true);
            boadMaterial.addTexture(mDistanceBoardTexture);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        mDistanceBoardObj = createPlane(width,width,false,boadMaterial);
        mDistanceBoardObj.setPosition(0,-0.3*(height-2*arrowHeight)/2,0);
        HaloLogger.logE(TAG,"initTrafficObject");


    }
    @Override
    public void hideTrafficDetection() {


    }

    private Material mTriangleArrowMaretial;
    private Material mTailArrowMaretial;
    private Material mPlaneMaretial;
    private Material mArrowIconMaterial;

    private void  initTrafficDetectionMaterial(){
        mTriangleArrowMaretial = createTextureMaterial(R.drawable.triangle_half,"triangle_half_road",1,1);
        mTailArrowMaretial = createTextureMaterial(R.drawable.arrow_tail,"triangle_tail_road",1,1);
        mArrowIconMaterial = createTextureMaterial(R.drawable.adas_dirction_arrow,"mArrowIconMaterial",1,1);
        mArrowIconMaterial.setColorInfluence(0);
        mPlaneMaretial = new Material();
        mPlaneMaretial.setColorInfluence(1f);
        mTriangleArrowMaretial.addPlugin(new TextureAlphaMaterialPlugin());
        mTailArrowMaretial.addPlugin(new TextureAlphaMaterialPlugin());
    }

    private Object3D createPlaneArrow(float totalWidth, float totalHeight, float arrowHeight){
        int color = 0xffa08843;
        float width = totalWidth;
        float iconWidth = width*0.3f;
        float iconHeight = width*0.85f;
        float planeHeight = totalHeight-2*arrowHeight;
        float errorHeight = -planeHeight*0.01f;

        Object3D textArrowObj = createPlane(iconWidth,iconHeight,false,mArrowIconMaterial);
        textArrowObj.setPosition(0,(width+planeHeight/3)/2,0);

        Object3D tailArrowObj = createPlane(width,arrowHeight,false,mTailArrowMaretial);
        tailArrowObj.setColor(color);
        Object3D headArrowObj = createPlane(width,arrowHeight,false,mTriangleArrowMaretial);
        headArrowObj.setColor(color);
//        Object3D textObj = createPlane(0.25f,0.25f,false,textMmaterial);
        Object3D planeObj = createPlane(width,planeHeight,false,mPlaneMaretial);
        planeObj.setColor(color);

        planeObj.setPosition(0,0,0);
        headArrowObj.setPosition(0,(planeHeight+arrowHeight+errorHeight)/2,0);
        tailArrowObj.setPosition(0,-(planeHeight+arrowHeight+errorHeight)/2,0);

        tailArrowObj.setColor(0xffa18743);

        Object3D wholeObj = new Object3D();
        wholeObj.addChild(tailArrowObj);
        wholeObj.addChild(headArrowObj);
        wholeObj.addChild(planeObj);
        wholeObj.addChild(textArrowObj);

        return wholeObj;

    }

    private Object3D createPlane(float width, float height, boolean orth, Material material){
        Plane plane = new Plane(width,height,10,10, Vector3.Axis.Z,
                true,false,1,true);//,Color.BLACK
        plane.setTransparent(true);
        plane.setMaterial(material);
        if(!orth){
            return plane;
        }
        plane.setPosition(0,0,height/2);
        BaseObject3D baseObject3D = new BaseObject3D();
        baseObject3D.setOrthographic(true,0.04f);
        baseObject3D.addChild(plane);
        return baseObject3D;
    }


    private void initCarObject(){
        mCarObject = new Plane(1f,0.5f,10,10, Vector3.Axis.Z,
                true,false,1,true);
        mCarObject.setColor(0);
        mCarObject.setMaterial(mCarMaterial);
        mCarObject.setTransparent(true);
        mCarObject.setDepthTestEnabled(false);
        mCarObject.setRotation(Vector3.Axis.Z,90);
        mCarObject.setPosition(0,1*0.15,0);
    }

    private void initObject3DCarObject(){
        boolean debug = false;
        Object3D carObj = null;
        LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(),
                mTextureManager,R.raw.white);//R.raw.qizi_obj multiobjects_obj
        try {
            objParser.parse();
            carObj = objParser.getParsedObject();
            float oScale = 0.31f;
            if(debug){
                oScale = 0.8f;
            }
            carObj.setScale(new Vector3(oScale,oScale,oScale));
        } catch (ParsingException e) {
            e.printStackTrace();
        }
        //旋转的
        if(!debug) {
            carObj.rotate(Vector3.Axis.X,90);
            carObj.rotate(Vector3.Axis.Z,90);
            carObj.rotate(Vector3.Axis.X,180);
            carObj.setDepthTestEnabled(false);
            carObj.setDepthMaskEnabled(false);
        }else {
            carObj.rotate(Vector3.Axis.Y,-90);
        }
        mCarObject = carObj;
    }

    public Object3D getCarObject() {
        return mCarObject;
    }

    public BaseObject3D getYawLaneObject() {
        return mYawLaneObject;
    }

    private int textureCreateCnt = 0;

    private Material createTextureMaterial(int sourceid,String texturename,float colorInfluence,float textureInfluence){
        boolean useVertexColor = false;
        Material material = new Material();
        material.useVertexColors(useVertexColor);
        material.setColorInfluence(colorInfluence);
        String tname = texturename==null?"create_new_texture"+textureCreateCnt++:texturename+textureCreateCnt;
        Texture texture = new Texture(ATexture.TextureType.DIFFUSE,tname,sourceid);
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
        return material;
    }

    public void setAdasCarObject(BaseObject3D adasCarObject) {
        adasCarObject.clearChildren();
        adasCarObject.addChild(mCarObject);
    }
}
