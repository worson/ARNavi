package com.haloai.hud.hudendpoint.arwaylib.render.object3d;

import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;

import com.haloai.hud.hudendpoint.arwaylib.render.vertices.ObjectElement;
import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;
import com.haloai.hud.hudendpoint.arwaylib.utils.PointD;

import org.rajawali3d.Geometry3D;
import org.rajawali3d.Object3D;
import org.rajawali3d.bounds.BoundingBox;
import org.rajawali3d.cameras.Camera;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cylinder;
import org.rajawali3d.util.RajLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wangshengxing on 16/9/10.
 */
public class ARWayRoadObject extends SuperRoadObject {
    public static final boolean LOG_OUT = false;
    public static final String  TAG     = SuperRoadObject.class.getSimpleName();

    public static final  int     VERTIX_NUMBER_PER_PLANE = 4;
    public static final  int     NUMBER_OF_VERTIX        = 3;
    public static final  int     NUMBER_OF_TEXTURE       = 2;
    public static final  int     NUMBER_OF_NORMAL        = 3;
    public static final  int     NUMBER_OF_COLOR         = 4;
    public static final  int     NUMBER_OF_INDICE        = 3;
    private static final boolean IS_VBOS_MODE            = true;
    private static final boolean IS_CLEAR_BUFFER         = false;
    public static final  String  ARWAY_ROAD_TYPE_MAIN    = "333";


    private float            mRoadWidth     = 0.7f;
    private static final int CIRCLE_SEGMENT = 32;


    private int           mRoadShapePointsCount;
    private List<Vector3> mRoadShapePoints;
    private int           mCountOfPlanes;
    private int           mCountOfVerties;

    private static Map<String, Material> sMaterialMap;

    private final float PI = (float) Math.PI;

    private ObjectElement mObjectElement;
    private static Material mRoadMaterial = new Material();
    static {
        mRoadMaterial.useVertexColors(true);
    }

    //render
    private boolean mNeedRender = false;

    public ARWayRoadObject(List<Vector3> roadPath, float width, int color) {
        super(roadPath, width,color);
        mRoadWidth =  width;
        mRoadShapePoints = roadPath;
        mRoadShapePointsCount = mRoadShapePoints.size();

        //        setDepthMaskEnabled(true);
        setDepthTestEnabled(false);
        Material material = new Material();
        material.useVertexColors(true);
        setMaterial(material);
        setDoubleSided(true);

        /*ObjectElement circleAndPlaneElement = generatePlanAndCircleVerties(mRoadShapePoints,mRoadShapePointsCount-1,CIRCLE_SEGMENT, mRoadWidth /2,0,color);
        addVerties(circleAndPlaneElement);*/

        ObjectElement planeElement = generatePlaneVerties(width/2,-0.001f);
        addVerties(planeElement);

        /*List<Vector3> rotatePath = new ArrayList<>();
        MathUtils.rotatePath(mRoadShapePoints,rotatePath,roadPath.get(0).x,roadPath.get(0).y,PI/2);
        ObjectElement roadCircleAndPlaneElement = generatePlanAndCircleVerties(rotatePath,mRoadShapePointsCount-1,CIRCLE_SEGMENT,0.8f*mRoadWidth/2,0.1f,Color.RED);
        addVerties(roadCircleAndPlaneElement);*/

        applyVerties();
//        generateAllVerties();

        if(IS_VBOS_MODE){
            attachRender();
            if(IS_CLEAR_BUFFER){
                recycleBuffer();
            }
        }


    }

    public ARWayRoadObject(ArrayList<Vector3> vector3s, double leftWidth, double rightWidth, String arwayRoadTypeMain) {
        super();
    }


    public void updateRoad(List<Vector3> roadPath){

    }

    private void releaseGlBuffer(){

    }
    private boolean isGlBufferEnough(){
        return true;
    }
    private boolean createGlBuffer(int shapeSize){

        return true;
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

                PointD circleEdge = new PointD(0, mRoadWidth);
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
            addChildCircle(p,(float) mRoadWidth);
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

    public void render(Camera camera, Matrix4 vpMatrix, Matrix4 projMatrix, Matrix4 vMatrix, Material sceneMaterial) {
//        this.render(camera, vpMatrix, projMatrix, vMatrix, sceneMaterial);
        this.render(camera, vpMatrix, projMatrix, vMatrix, null, sceneMaterial);
    }

    private void attachRender(){
        Material material = mMaterial;
        preRender();
        if (!mIsPartOfBatch) {
            if (material == null) {
                RajLog.e("[" + this.getClass().getName()
                        + "] This object can't render because there's no material attached to it.");
                throw new RuntimeException(
                        "This object can't render because there's no material attached to it.");
            }
            material.useProgram();
            // TODO: 16/9/21
//            setShaderParams(camera);
            material.bindTextures();
            if(mGeometry.hasTextureCoordinates())
                material.setTextureCoords(mGeometry.getTexCoordBufferInfo());
            if(mGeometry.hasNormals())
                material.setNormals(mGeometry.getNormalBufferInfo());
            if(mMaterial.usingVertexColors())
                material.setVertexColors(mGeometry.getColorBufferInfo());

            material.setVertices(mGeometry.getVertexBufferInfo());
        }
        material.setCurrentObject(this);
    }

    private void recycleBuffer(){
        mGeometry.recycleBuffer();
    }
    public void render(Camera camera, Matrix4 vpMatrix, Matrix4 projMatrix, Matrix4 vMatrix, Matrix4 parentMatrix, Material sceneMaterial) {
        if (!mIsVisible && !mRenderChildrenAsBatch)
            return;

        Material material = sceneMaterial == null ? mMaterial : sceneMaterial;
        if(!IS_VBOS_MODE){
            preRender();
        }

        // -- move view matrix transformation first
        boolean modelMatrixWasRecalculated = onRecalculateModelMatrix(parentMatrix);
        // -- calculate model view matrix;
        mMVMatrix.setAll(vMatrix).multiply(mMMatrix);
        //Create MVP Matrix from View-Projection Matrix
        mMVPMatrix.setAll(vpMatrix).multiply(mMMatrix);

        // Transform the bounding volumes if they exist
        if (mGeometry.hasBoundingBox()) mGeometry.getBoundingBox().transform(getModelMatrix());
        if (mGeometry.hasBoundingSphere()) mGeometry.getBoundingSphere().transform(getModelMatrix());

        mIsInFrustum = true; // only if mFrustrumTest == true it check frustum
        if (mFrustumTest && mGeometry.hasBoundingBox()) {
            BoundingBox bbox = mGeometry.getBoundingBox();
            if (!camera.getFrustum().boundsInFrustum(bbox)) {
                mIsInFrustum = false;
            }
        }

        if (!mIsContainerOnly && mIsInFrustum) {
            mPMatrix = projMatrix;
            if (mDoubleSided) {
                GLES20.glDisable(GLES20.GL_CULL_FACE);
            } else {
                GLES20.glEnable(GLES20.GL_CULL_FACE);
                if (mBackSided) {
                    GLES20.glCullFace(GLES20.GL_FRONT);
                } else {
                    GLES20.glCullFace(GLES20.GL_BACK);
                    GLES20.glFrontFace(GLES20.GL_CCW);
                }
            }
            if (mEnableBlending) {
                GLES20.glEnable(GLES20.GL_BLEND);
                GLES20.glBlendFunc(mBlendFuncSFactor, mBlendFuncDFactor);
            }
            if (!mEnableDepthTest) GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            else {
                GLES20.glEnable(GLES20.GL_DEPTH_TEST);
                GLES20.glDepthFunc(GLES20.GL_LESS);
            }

            GLES20.glDepthMask(mEnableDepthMask);
//            attachRender();
            // TODO: 16/9/21
            if(!IS_VBOS_MODE){
                if (!mIsPartOfBatch) {
                    if (material == null) {
                        RajLog.e("[" + this.getClass().getName()
                                + "] This object can't render because there's no material attached to it.");
                        throw new RuntimeException(
                                "This object can't render because there's no material attached to it.");
                    }
                    material.useProgram();

                    setShaderParams(camera);
                    material.bindTextures();
                    if(mGeometry.hasTextureCoordinates())
                        material.setTextureCoords(mGeometry.getTexCoordBufferInfo());
                    if(mGeometry.hasNormals())
                        material.setNormals(mGeometry.getNormalBufferInfo());
                    if(mMaterial.usingVertexColors())
                        material.setVertexColors(mGeometry.getColorBufferInfo());

                    material.setVertices(mGeometry.getVertexBufferInfo());
                }
                material.setCurrentObject(this);
            }

            if(mOverrideMaterialColor) {
                material.setColor(mColor);
            }
            material.applyParams();

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

            material.setMVPMatrix(mMVPMatrix);
            material.setModelMatrix(mMMatrix);
            material.setModelViewMatrix(mMVMatrix);

            if(mIsVisible) {
                int bufferType = mGeometry.getIndexBufferInfo().bufferType == Geometry3D.BufferType.SHORT_BUFFER ? GLES20.GL_UNSIGNED_SHORT : GLES20.GL_UNSIGNED_INT;
                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mGeometry.getIndexBufferInfo().bufferHandle);
                GLES20.glDrawElements(mDrawingMode, mGeometry.getNumIndices(), bufferType, 0);
                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
            }
            if (!mIsPartOfBatch && !mRenderChildrenAsBatch && sceneMaterial == null) {
                material.unbindTextures();
            }

            material.unsetCurrentObject(this);

            if (mEnableBlending) {
                GLES20.glDisable(GLES20.GL_BLEND);
            }

            if (mDoubleSided) {
                GLES20.glEnable(GLES20.GL_CULL_FACE);
            } else if (mBackSided) {
                GLES20.glCullFace(GLES20.GL_BACK);
            }
            if (!mEnableDepthTest) {
                GLES20.glEnable(GLES20.GL_DEPTH_TEST);
                GLES20.glDepthFunc(GLES20.GL_LESS);
            }
        }

        if (mShowBoundingVolume) {
            if (mGeometry.hasBoundingBox())
                mGeometry.getBoundingBox().drawBoundingVolume(camera, vpMatrix, projMatrix, vMatrix, mMMatrix);
            if (mGeometry.hasBoundingSphere())
                mGeometry.getBoundingSphere().drawBoundingVolume(camera, vpMatrix, projMatrix, vMatrix, mMMatrix);
        }
        // Draw children without frustum test
        for (int i = 0, j = mChildren.size(); i < j; i++) {
            Object3D child = mChildren.get(i);
            if(mRenderChildrenAsBatch || mIsPartOfBatch) {
                child.setPartOfBatch(true);
            }
            if(modelMatrixWasRecalculated) child.markModelMatrixDirty();
            child.render(camera, vpMatrix, projMatrix, vMatrix, mMMatrix, sceneMaterial);
        }

        if (mRenderChildrenAsBatch && sceneMaterial == null) {
            material.unbindTextures();
        }
    }

}
