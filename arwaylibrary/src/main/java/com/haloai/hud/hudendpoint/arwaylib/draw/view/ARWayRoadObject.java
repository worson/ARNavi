package com.haloai.hud.hudendpoint.arwaylib.draw.view;

import android.graphics.Color;

import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Created by Mo Bing(mobing@haloai.com) on 15/7/2016.
 */
public class ARWayRoadObject extends Object3D {
    public static String ARWAY_ROAD_TYPE_MAIN = "route_main";
    public static String ARWAY_ROAD_TYPE_BRANCH = "route_branch";
    public static String ARWAY_ROAD_TYPE_BRANCH_BLACK = "route_branch_black";

    private static double LEFT_ROAD_WIDTH = 0.7;
    private static double RIGHT_ROAD_WIDTH = 0.7;

    private List<Vector3> mRoadShapePoints;
    private List<Vector3> mRoadLeftSide;
    private List<Vector3> mRoadRightSide;
    private int mCountOfPlanes;
    private int mCountOfVerties;
    private static Map<String, Material> sMaterialMap;

    public ARWayRoadObject(List<Vector3> roadPath, double leftWidth, double rightWidth, String roadType) {//, List<Vector3> roadLeftSide, List<Vector3> roadRightSide) {
        super();

        if (sMaterialMap == null) {
            sMaterialMap = new HashMap<>();
            addMaterialViaRoadType(ARWAY_ROAD_TYPE_MAIN, R.drawable.route_new_line);
            addMaterialViaRoadType(ARWAY_ROAD_TYPE_BRANCH, R.drawable.route_new_branch);
            addMaterialViaRoadType(ARWAY_ROAD_TYPE_BRANCH_BLACK, R.drawable.route_new_red);
        }
        setMaterial(sMaterialMap.get(roadType));

        LEFT_ROAD_WIDTH = leftWidth;
        RIGHT_ROAD_WIDTH = rightWidth;

        mRoadShapePoints = new ArrayList<>(roadPath);
        mRoadLeftSide = new ArrayList<>();
        mRoadRightSide = new ArrayList<>();
        MathUtils.points2path(mRoadLeftSide, mRoadRightSide, mRoadShapePoints, LEFT_ROAD_WIDTH, RIGHT_ROAD_WIDTH);

        mCountOfPlanes = mRoadShapePoints.size() - 1;
        mCountOfVerties = mCountOfPlanes * 4;

        generateAllVerties();
    }

    private void addMaterialViaRoadType(String roadType, int textureResourceId) {
        Material material = new Material();
        material.setColor(0);
        material.enableTime(true);
        Texture texture = new Texture(roadType, textureResourceId);
        try {
            material.addTexture(texture);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        sMaterialMap.put(roadType, material);
    }

    /**
     * 在试图释放ARWayObject对象前,需要调用该方法,否则MaterialManager的单例对象会继续持有ARWayObject的Material对象
     * 导致在运行时间增加后,对内存的消耗逐渐增大
     */
    public void removeMaterial(){
        //MaterialManager.getInstance().removeMaterial(mMaterial);
        //MaterialManager.getInstance().taskRemove(mMaterial);
        //mMaterial = null;
        //System.gc();
    }

    public List<Vector3> getmRoadShapePoints() {
        return mRoadShapePoints;
    }

    public List<Vector3> getmRoadLeftSide() {
        return mRoadLeftSide;
    }

    public List<Vector3> getmRoadRightSide() {
        return mRoadRightSide;
    }

    private void generateAllVerties() {

        float[] vertices = new float[mCountOfVerties * 3];
        float[] textureCoords = new float[mCountOfVerties * 2];
        float[] normals = new float[mCountOfVerties * 3];
        float[] colors = new float[mCountOfVerties * 4];
        int[] indices = new int[mCountOfPlanes * 6];

        for (int i = 0; i < mRoadShapePoints.size() - 1; ++i) {

            Vector3 p1 = new Vector3(mRoadShapePoints.get(i));
            Vector3 p2 = new Vector3(mRoadShapePoints.get(i + 1));
            int randColor = 0xffff0000;

            int vIndex = i * 4 * 3;
            //左上
            Vector3 leftUp = new Vector3(mRoadLeftSide.get(i + 1));
            vertices[vIndex + 0] = (float) leftUp.x;
            vertices[vIndex + 1] = (float) leftUp.y;
            vertices[vIndex + 2] = (float) leftUp.z;
            //右上
            Vector3 rightUp = new Vector3(mRoadRightSide.get(i + 1));
            vertices[vIndex + 3] = (float) rightUp.x;
            vertices[vIndex + 4] = (float) rightUp.y;
            vertices[vIndex + 5] = (float) rightUp.z;
            //右下
            Vector3 rightDown = new Vector3(mRoadRightSide.get(i));
            vertices[vIndex + 6] = (float) rightDown.x;
            vertices[vIndex + 7] = (float) rightDown.y;
            vertices[vIndex + 8] = (float) rightDown.z;
            //左下
            Vector3 leftDown = new Vector3(mRoadLeftSide.get(i));
            vertices[vIndex + 9] = (float) leftDown.x;
            vertices[vIndex + 10] = (float) leftDown.y;
            vertices[vIndex + 11] = (float) leftDown.z;

            for (int j = 0; j < 12; j += 3) {
                normals[vIndex + j] = 0;
                normals[vIndex + j + 1] = 0;
                normals[vIndex + j + 2] = 1;
            }

            vIndex = i * 4 * 4;
            for (int j = 0; j < 16; j += 4) {
                colors[vIndex + j] = Color.red(randColor) / 255f;
                colors[vIndex + j + 1] = Color.green(randColor) / 255f;
                colors[vIndex + j + 2] = Color.blue(randColor) / 255f;
                colors[vIndex + j + 3] = 1.0f;
            }

            vIndex = i * 4 * 2;
            float u1 = 0;
            float v1 = 0;
            float u2 = 1;
            float v2 = 1;

            textureCoords[vIndex + 0] = u2;
            textureCoords[vIndex + 1] = v1;
            textureCoords[vIndex + 2] = u1;
            textureCoords[vIndex + 3] = v1;
            textureCoords[vIndex + 4] = u1;
            textureCoords[vIndex + 5] = v2;
            textureCoords[vIndex + 6] = u2;
            textureCoords[vIndex + 7] = v2;

            vIndex = i * 4;
            int iindex = i * 6;
            indices[iindex + 0] = (short) (vIndex + 0);
            indices[iindex + 1] = (short) (vIndex + 1);
            indices[iindex + 2] = (short) (vIndex + 3);
            indices[iindex + 3] = (short) (vIndex + 1);
            indices[iindex + 4] = (short) (vIndex + 2);
            indices[iindex + 5] = (short) (vIndex + 3);
        }

        setData(vertices, normals, textureCoords, colors, indices, false);
        setBlendingEnabled(false);

    }

    /*@Override
    public void destroy() {
        super.destroy();

    }*/
}
