package com.haloai.hud.hudendpoint.arwaylib.rajawali.object3d;

import android.graphics.Color;
import android.util.Log;

import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cylinder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by wangshengxing on 16/9/10.
 */
public class ARWayRoadObject extends SuperRoadObject {
    public static final boolean LOG_OUT = false;
    public static final String  TAG     = SuperRoadObject.class.getSimpleName();

    public static final int VERTIX_NUMBER_PER_PLANE = 4;
    public static final int NUMBER_OF_VERTIX  = 3;
    public static final int NUMBER_OF_TEXTURE = 2;
    public static final int NUMBER_OF_NORMAL  = 3;
    public static final int NUMBER_OF_COLOR   = 4;
    public static final int NUMBER_OF_INDICE  = 3;

    public static String ARWAY_ROAD_TYPE_MAIN         = "route_main";
    public static String ARWAY_ROAD_TYPE_BRANCH       = "route_branch";
    public static String ARWAY_ROAD_TYPE_BRANCH_BLACK = "route_branch_black";

    private float ROAD_WIDTH = 0.7f;
    private static int CIRCLE_SEGMENT = 16;


    private        int                   mRoadShapePointsCount;
    private        List<Vector3>         mRoadShapePoints;
    private        int                   mCountOfPlanes;
    private        int                   mCountOfVerties;
    private static Map<String, Material> sMaterialMap;

    private final float PI = (float) Math.PI;

    private Material mMaterial = null;

    private ObjectElement mObjectElement;


    public ARWayRoadObject(List<Vector3> roadPath, float width, int color) {
        super(roadPath, width,color);
        ROAD_WIDTH =  width;
        mRoadShapePoints = new ArrayList<>(roadPath);
        mRoadShapePointsCount = mRoadShapePoints.size();

        ObjectElement circleAndPlaneElement = generatePlanAndCircleVerties(roadPath,mRoadShapePointsCount-1,CIRCLE_SEGMENT,ROAD_WIDTH/2,0,color);
        addVerties(circleAndPlaneElement);

        /*ObjectElement planeElement = generatePlaneVerties(width/2,-0.001f);
        addVerties(planeElement);*/

        /*List<Vector3> rotatePath = new ArrayList<>();
        MathUtils.rotatePath(mRoadShapePoints,rotatePath,roadPath.get(0).x,roadPath.get(0).y,PI/2);
        ObjectElement roadCircleAndPlaneElement = generatePlanAndCircleVerties(rotatePath,mRoadShapePointsCount-1,CIRCLE_SEGMENT,0.8f*ROAD_WIDTH/2,0.1f,Color.RED);
        addVerties(roadCircleAndPlaneElement);*/

        applyVerties();
//        generateAllVerties();

//        setDepthMaskEnabled(true);
        setDepthTestEnabled(false);
    }

    public void addCrossPath(List<Vector3> crossPath, double width, Material material){

    }

    public void addRoad(List<Vector3> roadPath, double width){
//        ObjectElement circleAndPlaneElement = generatePlanAndCircleVerties(roadPath,roadPath.size(),CIRCLE_SEGMENT,(float) width/2,0);
//        addVerties(circleAndPlaneElement);
    }

    private ObjectElement generatePlanAndCircleVerties(List<Vector3> path, int segmentsL, int segmentsC, float radius, float height, int color) {
        int numVertices = (segmentsC + 1) * (segmentsL + 1);
        int numIndices = 2 * segmentsC * segmentsL * 3;
        //+(mRoadShapePoints.size()*CIRCLE_SEGMENT
        int numUvs = (segmentsL + 1) * (segmentsC + 1) * 2;
        int numColors = numVertices * 4;
        //顶点数据之间可以共用
        float[] vertices = new float[numVertices * NUMBER_OF_VERTIX];
        float[] textureCoords = new float[numUvs];
        float[] normals = new float[numVertices * NUMBER_OF_NORMAL];
        float[] colors = new float[numVertices * NUMBER_OF_COLOR];
        //每个三角形的对应三个下标
        int[] indices = new int[numIndices];

        ObjectElement element = new ObjectElement();
        element.vertices = vertices;
        element.textureCoords = textureCoords;
        element.normals = normals;
        element.colors = colors;
        element.indices = indices;

        int randColor = 0xffff0000;
        int IndexOffset = 0;
        int vIndexOffset = IndexOffset*VERTIX_NUMBER_PER_PLANE * NUMBER_OF_VERTIX;
        int colorIndexOffset = IndexOffset*VERTIX_NUMBER_PER_PLANE * NUMBER_OF_COLOR;
        int indiceIndexOffset = IndexOffset*0;


        int i, j;
        int vertIndex = 0, index = 0;
        final float normLen = 1.0f / radius;

        float z = height;
        int cnt = path.size();
        for (j = 0; j < cnt; ++j) {

            Vector3 p = path.get(j);

            for (i = 0; i <= segmentsC; ++i) {
                float verAngle = 2.0f * PI * i / segmentsC;
                float x = (float) p.x+ radius * (float) Math.cos(verAngle);
                float y = (float) p.y+radius * (float) Math.sin(verAngle);

                normals[vertIndex] = 0;
                vertices[vertIndex++] = x;
                normals[vertIndex] = 0;
                vertices[vertIndex++] = y;
                normals[vertIndex] = 1;
                vertices[vertIndex++] = z;
                if (i > 0 && j > 0) {
                    int a = (segmentsC + 1) * j + i;
                    int b = (segmentsC + 1) * j + i - 1;
                    int c = (segmentsC + 1) * (j - 1) + i - 1;
                    int d = (segmentsC + 1) * (j - 1) + i;
                    indices[index++] = a;
                    indices[index++] = b;
                    indices[index++] = c;
                    indices[index++] = a;
                    indices[index++] = c;
                    indices[index++] = d;
//                    Log.e(TAG,String.format("generatePlanAndCircleVerties,a=%s,b=%s,b=%s,d=%s",a,b,c,d));
                }
            }
        }

        if (true) {
            numUvs = 0;
            for (j = 0; j <= segmentsL; ++j) {
                for (i = segmentsC; i >= 0; --i) {
                    textureCoords[numUvs++] = (float) i / segmentsC;
                    textureCoords[numUvs++] = (float) j / segmentsL;
                }
            }
        }

        if (true)
        {
            float red = Color.red(color) / 255.f;
            float green = Color.green(color) / 255.f;
            float blue = Color.blue(color) / 255.f;
            float alpha = Color.alpha(color) / 255.f;
            for (j = 0; j < numColors; j += 4)
            {
                colors[j] = red;
                colors[j + 1] = green;
                colors[j + 2] = blue;
                colors[j + 3] = alpha;
            }
        }

        return element;

    }

    private void addVerties(ObjectElement element){
        if(element == null || !element.isDataValid()){
            return;
        }
        List<ObjectElement> elements = new LinkedList<>();
        if (mObjectElement != null && mObjectElement.isDataValid()){
            elements.add(mObjectElement);
        }
        elements.add(element);
        ObjectElement totalElement = ObjectElement.addAllElement(elements);
        if (totalElement != null && totalElement.isDataValid()) {
            if(LOG_OUT){
                Log.e(TAG, String.format("addVerties called ,verties size is %s",totalElement.vertices.length));
            }
            mObjectElement = totalElement;
//            setData(totalElement.vertices, totalElement.normals, totalElement.textureCoords, totalElement.colors, totalElement.indices, false);
        }
    }
    private void applyVerties(){
        ObjectElement totalElement = mObjectElement;
        if (totalElement != null && totalElement.isDataValid()) {
            if(LOG_OUT){
                Log.e(TAG, String.format("applyVerties called ,verties size is %s",totalElement.vertices.length));
            }
            mObjectElement = totalElement;
            setData(totalElement.vertices, totalElement.normals, totalElement.textureCoords, totalElement.colors, totalElement.indices, false);
        }
    }

    private void generateAllVerties() {
        List<ObjectElement> allElement = new ArrayList<>();

//        ObjectElement palneElement = generatePlaneVerties();
//        allElement.add(palneElement);
//        generateCircleObject();

        if(LOG_OUT){
            Log.e(TAG,"generateAllVerties called ");
        }
//        ObjectElement circleAndPlaneElement = generatePlanAndCircleVerties(0f);
//        allElement.add(circleAndPlaneElement);
        if(LOG_OUT){
            Log.e(TAG,"generatePlanAndCircleVerties called ");
        }
        ObjectElement totalElement = ObjectElement.addAllElement(allElement);
        if (totalElement != null && totalElement.isDataValid()) {
            if(LOG_OUT){
                Log.e(TAG,"setData called ");
            }
            mObjectElement = totalElement;
            setData(totalElement.vertices, totalElement.normals, totalElement.textureCoords, totalElement.colors, totalElement.indices, false);
            setBlendingEnabled(false);
        }

    }

    private ObjectElement generateCircleVerties(int segmentsL,int segmentsC,float radius,float heigth) {

        int countOfVertice = mRoadShapePointsCount*(CIRCLE_SEGMENT+1);
        int numIndices = 2 * segmentsL * mRoadShapePointsCount * 3;
        //+(mRoadShapePoints.size()*CIRCLE_SEGMENT

        //顶点数据之间可以共用
        float[] vertices = new float[countOfVertice * NUMBER_OF_VERTIX];
        float[] textureCoords = new float[countOfVertice * NUMBER_OF_TEXTURE];
        float[] normals = new float[countOfVertice * NUMBER_OF_NORMAL];
        float[] colors = new float[countOfVertice * NUMBER_OF_COLOR];
        //每个三角形的对应三个下标
        int[] indices = new int[numIndices*NUMBER_OF_INDICE];

        ObjectElement element = new ObjectElement();
        element.vertices = vertices;
        element.textureCoords = textureCoords;
        element.normals = normals;
        element.colors = colors;
        element.indices = indices;

        int randColor = 0xffff0000;
        double height = 0;
        int IndexOffset = 0;
        int vIndexOffset = IndexOffset*VERTIX_NUMBER_PER_PLANE * NUMBER_OF_VERTIX;
        int colorIndexOffset = IndexOffset*VERTIX_NUMBER_PER_PLANE * NUMBER_OF_COLOR;
        int indiceIndexOffset = IndexOffset*0;
        //填充圆
        for (int i = 0; i < mRoadShapePoints.size(); ++i) {
            Vector3 circle = new Vector3(mRoadShapePoints.get(i));
            int vCircleIndex = i *CIRCLE_SEGMENT*NUMBER_OF_VERTIX+vIndexOffset;
            int circleColorIndex = i *CIRCLE_SEGMENT*NUMBER_OF_COLOR+colorIndexOffset;
            int circleIndiceIndex = i *CIRCLE_SEGMENT*NUMBER_OF_INDICE+indiceIndexOffset;

            int vIndex = vCircleIndex;
            {
                //圆心
                vertices[vIndex + 0] = (float) circle.x;
                vertices[vIndex + 1] = (float) circle.y;
                vertices[vIndex + 2] = (float) height;

                normals[vIndex ] = 0;
                normals[vIndex + 1] = 0;
                normals[vIndex + 2] = 1;

                vIndex = circleColorIndex;
                colors[vIndex ] = Color.red(randColor) / 255f;
                colors[vIndex + 1] = Color.green(randColor) / 255f;
                colors[vIndex + 2] = Color.blue(randColor) / 255f;
                colors[vIndex + 3] = 1.0f;

            }
            for (int k = 0; k < CIRCLE_SEGMENT; k++) {

                PointD circleEdge = new PointD(0,ROAD_WIDTH);
                double stepDegree = k* Math.PI*2/CIRCLE_SEGMENT;

                vIndex = vCircleIndex+(k+1)*NUMBER_OF_VERTIX;
                //圆心
                vertices[vIndex + 0] = (float) circle.x;
                vertices[vIndex + 1] = (float) circle.y;
                vertices[vIndex + 2] = (float) height;
                //边缘点顶点
                MathUtils.rotateAround(circle.x,circle.y,circleEdge.x,circleEdge.y,circleEdge,stepDegree);
                vertices[vIndex + 3] = (float) circleEdge.x;
                vertices[vIndex + 4] = (float) circleEdge.y;
                vertices[vIndex + 5] = (float) height;
                MathUtils.rotateAround(circle.x,circle.y,circleEdge.x,circleEdge.y,circleEdge,stepDegree);
                vertices[vIndex + 6] = (float) circleEdge.x;
                vertices[vIndex + 7] = (float) circleEdge.y;
                vertices[vIndex + 8] = (float) height;

                //法线向量，每个顶点存在一个
                normals[vIndex ] = 0;
                normals[vIndex+ 1] = 0;
                normals[vIndex+ 2] = 1;
                normals[vIndex + 3] = 0;
                normals[vIndex + 4] = 0;
                normals[vIndex + 5] = 1;
                normals[vIndex + 6] = 0;
                normals[vIndex + 7] = 0;
                normals[vIndex + 8] = 1;

                //颜色值，有四个
                vIndex = circleColorIndex+(k+1)*NUMBER_OF_COLOR;
                colors[vIndex ] = Color.red(randColor) / 255f;
                colors[vIndex + 1] = Color.green(randColor) / 255f;
                colors[vIndex + 2] = Color.blue(randColor) / 255f;
                colors[vIndex + 3] = 1.0f;


                /*vIndex = i * 4 * 2;
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
                textureCoords[vIndex + 7] = v2;*/

                int iindex = k * NUMBER_OF_INDICE+circleIndiceIndex;
                indices[iindex + 0] = (short) (vIndex + 0);
                indices[iindex + 1] = (short) (vIndex + 1);
                indices[iindex + 3] = (short) (vIndex + 3);
            }

        }

        return element;

    }

    private void generateCircleObject() {
        for (int i = 0; i < mRoadShapePoints.size(); ++i) {
            Vector3 p = mRoadShapePoints.get(i);
            addChildCircle(p,(float) ROAD_WIDTH);
        }
    }

    private void addChildCircle(Vector3 position, float radius){
        float height = 0.01f;
        Cylinder circle = new Cylinder(height,radius,8,8);
        circle.setPosition(position);
        circle.setColor(Color.WHITE);
        circle.setMaterial(mMaterial);
        addChild(circle);
    }


    public void addChildRoad(List<Vector3> roadPath, double width){

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


    public static void reset(){
        if (sMaterialMap != null) {
            sMaterialMap.clear();
        }
    }


    private ObjectElement generatePlaneVerties(float radius,float height) {
        mCountOfPlanes = (mRoadShapePoints.size() - 1);
        mCountOfVerties = mCountOfPlanes * 4;
        //顶点数据之间可以共用
        float[] vertices = new float[mCountOfVerties * NUMBER_OF_VERTIX];
        float[] textureCoords = new float[mCountOfVerties * NUMBER_OF_TEXTURE];
        float[] normals = new float[mCountOfVerties * NUMBER_OF_NORMAL];
        float[] colors = new float[mCountOfVerties * NUMBER_OF_COLOR];
        //每个三角形的对应三个下标
        int[] indices = new int[mCountOfPlanes * (6)];

        ObjectElement element = new ObjectElement();
        element.vertices = vertices;
        element.textureCoords = textureCoords;
        element.normals = normals;
        element.colors = colors;
        element.indices = indices;

        PointD leftUp = new PointD();
        PointD leftDown = new PointD();
        PointD rightUp = new PointD();
        PointD rightDown = new PointD();

        int randColor = 0xffff0000;
        //填充矩形块
        for (int i = 0; i < mRoadShapePoints.size() - 1; ++i) {

            Vector3 p1 = new Vector3(mRoadShapePoints.get(i));
            Vector3 p2 = new Vector3(mRoadShapePoints.get(i + 1));
            MathUtils.expandPath(p1.x,p1.y,p2.x,p2.y,leftDown,leftUp,rightDown,rightUp,radius);

            int vIndex = i * 4 * 3;
            //左上
            vertices[vIndex + 0] = (float) leftUp.x;
            vertices[vIndex + 1] = (float) leftUp.y;
            vertices[vIndex + 2] = (float) height;
            //右上

            vertices[vIndex + 3] = (float) rightUp.x;
            vertices[vIndex + 4] = (float) rightUp.y;
            vertices[vIndex + 5] = (float) height;
            //右下
            vertices[vIndex + 6] = (float) rightDown.x;
            vertices[vIndex + 7] = (float) rightDown.y;
            vertices[vIndex + 8] = (float) height;

            //左下
            vertices[vIndex + 9] = (float) leftDown.x;
            vertices[vIndex + 10] = (float) leftDown.y;
            vertices[vIndex + 11] = (float) height;

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
        return element;
    }
}
