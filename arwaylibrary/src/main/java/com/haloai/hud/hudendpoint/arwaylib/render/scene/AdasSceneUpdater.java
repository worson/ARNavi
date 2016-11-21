package com.haloai.hud.hudendpoint.arwaylib.render.scene;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.render.object3d.ARWayRoadBuffredObject;
import com.haloai.hud.hudendpoint.arwaylib.render.object3d.BaseObject3D;
import com.haloai.hud.hudendpoint.arwaylib.render.options.RoadRenderOption;
import com.haloai.hud.hudendpoint.arwaylib.render.shader.TextureAlphaMaterialPlugin;
import com.haloai.hud.hudendpoint.arwaylib.render.utils.TDrawText;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.utils.HaloLogger;

import org.opencv.core.Mat;
import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.AlphaMaskMaterialPlugin;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Plane;

import java.util.List;

/**
 * Created by wangshengxing on 19/11/2016.
 */

public class AdasSceneUpdater extends SuperArwaySceneUpdater implements IAdasSceneUpdater {

    public static  boolean IS_SINGLETON  = true;
    private static AdasSceneUpdater mAdasSceneUpdater = null;

    private RoadRenderOption mOptions = null;

    private Object3D mCarObject;
    private BaseObject3D mYawLaneObject;
    private BaseObject3D mTrafficDetectionLayer        = null;

    private Material mCarMaterial             = null;
    private Material mLaneLeftMaterial             = null;
    private Material mLaneRightMaterial             = null;


    public AdasSceneUpdater() {
        initObject();
    }

    private void initObject() {
        mCarObject          = new BaseObject3D();
        mYawLaneObject    = new BaseObject3D();
    }


    public void initScene(){
        initMaterial();
        initCarObject();
    }

    private void initMaterial(){
        mCarMaterial = createTextureMaterial(R.drawable.adas_car,"adas_car_texture",1,1);
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
        mTrafficDetectionLayer.clearChildren();
        float width = mOptions.roadWidth;
        Material boadMaterial = new Material();
        boadMaterial.setColorInfluence(0.1f);

        Bitmap bitmap =  TDrawText.drawBitmapText((int)distance+"m",25,0,new int[4],Color.WHITE,Color.BLUE,
                Color.BLACK,1);
        try {
            boadMaterial.addTexture(new Texture("timeTexture", bitmap));
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        Object3D board = createPlane(width,width,false,boadMaterial);
        Object3D object3D =  createPlaneArrow(width,3,0.4f);
        object3D.addChild(board);

        object3D.setRotation(Vector3.Axis.Z,90-direction);

        mTrafficDetectionLayer.addChild(object3D);
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
        float iconHeight = width*0.5f;
        float planeHeight = totalHeight-2*arrowHeight;
        float errorHeight = -planeHeight*0.01f;

        Object3D textArrowObj = createPlane(iconWidth,iconHeight,false,mArrowIconMaterial);
        textArrowObj.setPosition(0,(width+planeHeight/2)/2,0);

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
        plane.setPosition(0,0,height/2);
        plane.setMaterial(material);
        if(!orth){
            return plane;
        }
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
        mCarObject.setPosition(0,1*0.15,0);
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
