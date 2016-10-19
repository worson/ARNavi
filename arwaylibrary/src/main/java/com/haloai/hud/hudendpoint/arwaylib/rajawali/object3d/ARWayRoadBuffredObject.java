package com.haloai.hud.hudendpoint.arwaylib.rajawali.object3d;

import android.graphics.Color;
import android.opengl.GLES20;

import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.Geometry3D;
import org.rajawali3d.Object3D;
import org.rajawali3d.bounds.BoundingBox;
import org.rajawali3d.cameras.Camera;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.util.RajLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangshengxing on 16/9/10.
 */
public class ARWayRoadBuffredObject extends SuperRoadObject {
    //debug
    public static final boolean LOG_OUT = false;
    public static final String  TAG     = SuperRoadObject.class.getSimpleName();

    //configuration
    protected static final boolean IS_VBOS_MODE       = false;
    protected static final boolean IS_CLEAR_BUFFER    = false;
    private static final int CIRCLE_SEGMENT = 32;

    //Road
    private float mRefLineHeight = 0.5f;
    private float mRefLineWidth = 0.5f;
    private float            mRoadWidth     = 0.7f;
    private int mRoadColor = Color.WHITE;
    private ShapeType mShapeType = ShapeType.ROAD;

    public enum ShapeType {
        CIRCLE,
        ROAD,
        REFERENCE_LINE
    };

    //data
    private int           mRoadShapePointsCount;
    private List<Vector3> mRoadShapePoints;
    private int           mCountOfPlanes;
    private int           mCountOfVerties;

    /**
     * 默认道路绘制，只需要指定路宽和颜色
     * @param width
     * @param color
     */
    public ARWayRoadBuffredObject(float width, int color) {
        this(width,color,mRoadMaterial);
        mShapeType = ShapeType.ROAD;
    }

    /**
     * 绘制参考线，指定矩形的长和宽
     */
    public ARWayRoadBuffredObject(float height, float width, int color, Material material) {
        this(width,color,material);
        mShapeType = ShapeType.REFERENCE_LINE;
        mRefLineHeight = height;
        mRefLineWidth = width;
    }

    public ARWayRoadBuffredObject(float height, float width, int color) {
        this(height,width,color,mRoadMaterial);
    }

    public ARWayRoadBuffredObject(float width, int color, Material material) {
        super();
        mRoadWidth =  width;
        mRoadColor = color;
        if (material != null) {
            setMaterial(material);
        }else {
            setMaterial(mRoadMaterial);
        }
        setDepthTestEnabled(false);
        setBlendingEnabled(true);
        setDepthMaskEnabled(false);
        setBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        setDoubleSided(true);
    }

    public boolean updateBufferedRoad(List<Vector3> roadPath){
        generateObjectBuffer(roadPath,mShapeType);
        return true;
    }

    /**
     * 更新道路渲染内容，在初始化中初始化宽度
     * @param roadPath
     * @return
     */
    public boolean updateBufferedRoad(List<Vector3> roadPath, float width){
        mRoadWidth = width;
        return generateObjectBuffer(roadPath,mShapeType);
    }

    /**
     * 更新道路渲染内容
     * @param roadPath
     * @return
     */
    public boolean updateBufferedRoad(List<Vector3> roadPath, ShapeType type){
        return generateObjectBuffer(roadPath,type);

    }

    /**
     *
     * @param path
     * @param stepLength
     * @return
     */
    public boolean updateReferenceLine(List<Vector3> path, double stepLength){
        mNeedRender = false;

        double distStep = stepLength;
        List<Vector3> points = new ArrayList<>();
        List<Float> directions = new ArrayList<>();

        int cnt = path.size();
        if(cnt>=2){
            Vector3 v1 = path.get(0);
            Vector3 v2 = path.get(1);
            Float direction = new Float((float) Math.atan2(v2.y-v1.y,v2.x-v1.x));
            points.add(v1);
            directions.add(direction);
            for (int i = 0; i < cnt - 1; i++) {
                v2 = path.get(i + 1);
                double temp = MathUtils.calculateDistance(v1.x, v1.y, v2.x, v2.y);
                if (temp >= distStep) {
                    double scale = distStep / temp;
                    Vector3 v = new Vector3();
                    v.x = v1.x + (v2.x - v1.x) * scale;
                    v.y = v1.y + (v2.y - v1.y) * scale;
                    v.z = 0;
                    v1 = new Vector3(v);
                    i--;
                    direction = new Float((float) Math.atan2(v2.y-v1.y,v2.x-v1.x));
                    directions.add(direction);
                    points.add(v);
                    distStep = stepLength;
                } else if (temp < distStep) {
                    distStep -= temp;
                    v1 = path.get(i+1);
                }
            }

        }else {
            return false;
        }
        replaceGeometry3D(new Geometry3D());
        ObjectElement referenceLineElement = generateRectangleVerties(points,directions,mRefLineHeight,mRefLineWidth,mRoadColor);
//        ObjectElement referenceLineElement = generatePlaneVerties(points,mRefLineWidth,0,mRoadColor);
        addVerties(referenceLineElement);
        applyVerties();
        mNeedRender = true;
        return true;
    }

    private boolean generateObjectBuffer(List<Vector3> roadPath, ShapeType type){
        mNeedRender = false;
        replaceGeometry3D(new Geometry3D());
        if(roadPath == null || roadPath.size()<1){
            return false;
        }
        mRoadShapePoints = roadPath;
        mRoadShapePointsCount = mRoadShapePoints.size();
        boolean result = true;
        if(false && type== ShapeType.ROAD){
            ObjectElement circleAndPlaneElement = generatePlanAndCircleVerties(mRoadShapePoints,mRoadShapePointsCount-1,CIRCLE_SEGMENT, mRoadWidth /2,0,mRoadColor);
            addVerties(circleAndPlaneElement);
            result = true;
        }else {
            ObjectElement textureElement = TextureRoadGeometryProcessor.getObjectElement(mRoadShapePoints,mRoadWidth,mRoadColor);
            if (textureElement != null) {
                addVerties(textureElement);
                result = true;
                HaloLogger.logE("textureElement","textureElement add ok !");
            }else {
                result = false;
            }

        }
        /*ObjectElement planeElement = generatePlaneVerties(mRoadWidth/2,-0.001f);
        addVerties(planeElement);*/

        /*List<Vector3> rotatePath = new ArrayList<>();
        MathUtils.rotatePath(mRoadShapePoints,rotatePath,roadPath.get(0).x,roadPath.get(0).y,PI/2);
        ObjectElement roadCircleAndPlaneElement = generatePlanAndCircleVerties(rotatePath,mRoadShapePointsCount-1,CIRCLE_SEGMENT,0.8f*mRoadWidth/2,0.1f,Color.RED);
        addVerties(roadCircleAndPlaneElement);*/
        if(result){
            applyVerties();
            if(IS_CLEAR_BUFFER){
                recycleBuffer();
            }
        }
        mNeedRender = true;
        return result;
    }
    private void recycleBuffer(){
        mGeometry.recycleBuffer();
    }

    private ObjectElement generateAntiSawtoothLine(List<Vector3> path, float width, int color) {
        ObjectElement element = new ObjectElement();

        return element;
    }
    /**
     *
     * 单位形状点GPU内存消耗
     * (16+1)*(3+2+3+4+6)
     * @param path
     * @param segmentsL
     * @param segmentsC
     * @param radius
     * @param height
     * @param color
     * @return
     */
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

    /***
     * * 单位形状点GPU内存消耗
     * (4)*(3+2+3+4+1.25)
     * @param points
     * @param directions
     * @param height
     * @param width
     * @param color
     * @return
     */
    private ObjectElement generateRectangleVerties(List<Vector3> points, List<Float> directions, float height, float width, int color) {
        if (points == null || points.size() <= 1|| directions == null) {
            return null;
        }
        mCountOfPlanes = (points.size() - 1);
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

        int randColor = color;
        float z = 0;
        Vector3 p1 = new Vector3();
        Vector3 p2 = new Vector3();
        //填充矩形块
        for (int i = 0; i < points.size() - 1; ++i) {

            float direction = directions.get(i);
//            float direction = (float) Math.random();
            Vector3 point = points.get(i);
            p1.x = point.x+ Math.cos(direction)*(height/2);
            p1.y = point.y+ Math.sin(direction)*(height/2);
            p2.x = point.x+ Math.cos(direction)*(-height/2);
            p2.y = point.y+ Math.sin(direction)*(-height/2);

            MathUtils.expandPath(p1.x,p1.y,p2.x,p2.y,leftDown,leftUp,rightDown,rightUp,width/2);

            int vIndex = i * 4 * 3;
            //左上
            vertices[vIndex + 0] = (float) leftUp.x;
            vertices[vIndex + 1] = (float) leftUp.y;
            vertices[vIndex + 2] = (float) z;
            //右上

            vertices[vIndex + 3] = (float) rightUp.x;
            vertices[vIndex + 4] = (float) rightUp.y;
            vertices[vIndex + 5] = (float) z;
            //右下
            vertices[vIndex + 6] = (float) rightDown.x;
            vertices[vIndex + 7] = (float) rightDown.y;
            vertices[vIndex + 8] = (float) z;

            //左下
            vertices[vIndex + 9] = (float) leftDown.x;
            vertices[vIndex + 10] = (float) leftDown.y;
            vertices[vIndex + 11] = (float) z;

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

    private ObjectElement generatePlaneVerties(List<Vector3> points, float radius, float height, int roadColor) {
        mCountOfPlanes = (points.size() - 1);
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

        int randColor = roadColor;
        //填充矩形块
        for (int i = 0; i < points.size() - 1; ++i) {

            Vector3 p1 = new Vector3(points.get(i));
            Vector3 p2 = new Vector3(points.get(i + 1));
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
        this.render(camera, vpMatrix, projMatrix, vMatrix, null, sceneMaterial);
    }

    private void attachRender(){
        /*Material material = mMaterial;
        preRender();
        if (!mIsPartOfBatch) {
            if (material == null) {
                RajLog.e("[" + this.getClass().getName()
                        + "] This object can't render because there's no material attached to it.");
                throw new RuntimeException(
                        "This object can't render because there's no material attached to it.");
            }
            material.useProgram();
            material.bindTextures();
            if(mGeometry.hasTextureCoordinates())
                material.setTextureCoords(mGeometry.getTexCoordBufferInfo());
            if(mGeometry.hasNormals())
                material.setNormals(mGeometry.getNormalBufferInfo());
            if(mMaterial.usingVertexColors())
                material.setVertexColors(mGeometry.getColorBufferInfo());

            material.setVertices(mGeometry.getVertexBufferInfo());
        }
        material.setCurrentObject(this);*/
    }

    public void render(Camera camera, Matrix4 vpMatrix, Matrix4 projMatrix, Matrix4 vMatrix, Matrix4 parentMatrix, Material sceneMaterial) {
        if(!mNeedRender){
            return;
        }
        if (!mIsVisible && !mRenderChildrenAsBatch)
            return;
        synchronized (mLock){
            Material material = sceneMaterial == null ? mMaterial : sceneMaterial;
            preRender();
            RoadFogMaterialPlugin fogMaterialPlugin = null;
            if(mFogEnable){
               IMaterialPlugin plugin =  material.getPlugin(RoadFogMaterialPlugin.class);
                if (plugin != null) {
                    fogMaterialPlugin =  (RoadFogMaterialPlugin)plugin;
                    fogMaterialPlugin.setFogStartPosition(mFogStart);
                    fogMaterialPlugin.setFogEndPosition(mFogEnd);
                    fogMaterialPlugin.getVertexShaderFragment().applyParams();
                    fogMaterialPlugin.getFragmentShaderFragment().applyParams();
                }
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
                // TODO: 16/9/21
                if(!IS_VBOS_MODE || true){
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



    @Override
    protected void finalize() throws Throwable {
        super.finalize();
//        destroy();
    }
}
