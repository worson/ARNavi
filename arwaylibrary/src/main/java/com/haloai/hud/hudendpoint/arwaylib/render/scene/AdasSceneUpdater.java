package com.haloai.hud.hudendpoint.arwaylib.render.scene;

import android.graphics.Color;

import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.render.object3d.ARWayRoadBuffredObject;
import com.haloai.hud.hudendpoint.arwaylib.render.object3d.BaseObject3D;
import com.haloai.hud.hudendpoint.arwaylib.render.shader.TextureAlphaMaterialPlugin;
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

    private static AdasSceneUpdater mAdasSceneUpdater = null;

    public AdasSceneUpdater() {

    }

    public void init(){
        initMaterial();
        initCarObject();
    }

    private void initMaterial(){
        mCarMaterial = createTextureMaterial(R.drawable.adas_car,"adas_car",1,1);
        mLaneLeftMaterial = createTextureMaterial(R.drawable.road_circle_alpha_change,"adas_left_cicrle",1,1);
        mLaneRightMaterial = createTextureMaterial(R.drawable.road_circle_alpha_change,"adas_right_cicrle",1,1);

        mLaneLeftMaterial.addPlugin(new TextureAlphaMaterialPlugin());
        mLaneRightMaterial.addPlugin(new TextureAlphaMaterialPlugin());
    }
    public static AdasSceneUpdater getInstance (){
        if (mAdasSceneUpdater == null) {
            mAdasSceneUpdater = new AdasSceneUpdater();
        }
        return mAdasSceneUpdater;
    }

    @Override
    public void showLaneYawLine(List<Vector3> path, boolean left) {
        mYawLaneObject.clearChildren();
        ARWayRoadBuffredObject road = new ARWayRoadBuffredObject(0.4f);
        road.setColor(Color.YELLOW);
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

    }

    @Override
    public void updateTrafficDetection(double distance, double direction) {
        int id = R.drawable.road_navi_arrow;
        Texture arrowtexture = new Texture(ATexture.TextureType.DIFFUSE,"arrow_texture",id);
        id = R.drawable.road_circle_alpha_change;
        Texture textTexture = new Texture(ATexture.TextureType.DIFFUSE,"text_texture",id);
        Material arrowMmaterial = new Material();
        Material textMmaterial = new Material();
        try {
            arrowMmaterial.addTexture(arrowtexture);
            textMmaterial.addTexture(textTexture);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        arrowMmaterial.addPlugin(new TextureAlphaMaterialPlugin());
        textMmaterial.addPlugin(new TextureAlphaMaterialPlugin());

        Object3D arrowObj = createPlane(0.4f,2.5f,false,arrowMmaterial);
        Object3D textObj = createPlane(0.25f,0.25f,false,textMmaterial);

        arrowObj.setColor(0xffa18743);

        Object3D wholeObj = new Object3D();
        wholeObj.addChild(arrowObj);
        wholeObj.addChild(textObj);

        addObject(wholeObj);
    }

    @Override
    public void hideTrafficDetection() {


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

    private BaseObject3D mCarObject          = new BaseObject3D();
    private BaseObject3D mYawLaneObject    = new BaseObject3D();

    private Material mCarMaterial             = null;
    private Material mLaneLeftMaterial             = null;
    private Material mLaneRightMaterial             = null;

    private void initCarObject(){
        Object3D object = new Plane(1f,0.5f,10,10, Vector3.Axis.Z,
                true,false,1,true);
        object.setColor(0);
        object.setMaterial(mCarMaterial);
        object.setTransparent(true);
        object.setDepthTestEnabled(false);
        object.rotate(Vector3.Axis.Z,90);
        object.setPosition(0,1*0.15,0);
        mCarObject.clearChildren();
        mCarObject.addChild(object);
    }

    public BaseObject3D getCarObject() {
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
        String tname = texturename==null?"create_texture"+textureCreateCnt++:texturename;
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

}
