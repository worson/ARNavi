package demo.opengl.haloai.com.rajawalidemo;

import android.graphics.Color;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import rajawali.BufferInfo;
import rajawali.Geometry3D;
import rajawali.Geometry3D.BufferType;
import rajawali.Object3D;
import rajawali.materials.Material;
import rajawali.math.vector.Vector3;

/**
 * 这个例子展示了如何有效地创建大量的纹理的平面。
 * 缓慢的方式是创建2000个平面对象和16个独立的纹理。
 * 优化的方法是创建一个基本对象的三维与顶点数据在一个缓冲区中的2000个平面（
 * 和对于该坐标数据，正常的数据相同，等）。每一个平面都有相同的位置
 * 在（0，0，0）。为每个平面的位置和旋转创建额外的缓冲区。
 * 只有一个纹理被使用。它是一个1024×1024位图，包含256个16×256的图像。这
 * 称为“纹理阿特拉斯”。每个平面都分配了这个纹理的一个特定部分。
 * 这比创建单独的对象和纹理要快得多，因为这个着色程序
 * 需要创建一次，只有一个纹理必须上传，矩阵变换需要
 * 只做一次在中央处理器上，等等。
 *
 * @author dennis.ippel
 */
public class PlanesGalore extends Object3D {
    protected FloatBuffer                mPlanePositions;
    protected FloatBuffer                mRotationSpeeds;
    protected BufferInfo                 mPlanePositionsBufferInfo;
    protected Material                   mGaloreMat;
    protected PlanesGaloreMaterialPlugin mMaterialPlugin;

    private static int   NUM_PLANES   = 2000;
    private static int   NUM_VERTICES = NUM_PLANES * 4;
    private static float planeSize    = .3f;

    private List<Vector3> mPath      = new ArrayList<>();
    private List<Vector3> mPathLeft  = new ArrayList<>();
    private List<Vector3> mPathRight = new ArrayList<>();

    public PlanesGalore() {
        super();
        NUM_PLANES = 2000;
        NUM_VERTICES = NUM_PLANES * 4;
        mPlanePositionsBufferInfo = new BufferInfo();
        init();
    }

    public PlanesGalore(List<Vector3> path, List<Vector3> pathLeft, List<Vector3> pathRight, int numPlanes) {
        super();
        NUM_PLANES = numPlanes;
        NUM_VERTICES = NUM_PLANES * 4;
        mPath.clear();
        mPath.addAll(path);
        mPathLeft.clear();
        mPathLeft.addAll(pathLeft);
        mPathRight.clear();
        mPathRight.addAll(pathRight);
        mPlanePositionsBufferInfo = new BufferInfo();
        init();
    }

    public void init() {
        mGaloreMat = new Material();
        mGaloreMat.enableTime(true);

        mMaterialPlugin = new PlanesGaloreMaterialPlugin();
        mGaloreMat.addPlugin(mMaterialPlugin);

        setMaterial(mGaloreMat);

        float[] vertices = new float[NUM_VERTICES * 3];
        float[] textureCoords = new float[NUM_VERTICES * 2];
        float[] normals = new float[NUM_VERTICES * 3];
        float[] planePositions = new float[NUM_VERTICES * 3];
        float[] rotationSpeeds = new float[NUM_VERTICES];
        float[] colors = new float[NUM_VERTICES * 4];
        int[] indices = new int[NUM_PLANES * 6];

        //这个for循环负责随机生成N个plane对应的位置,旋转颜色纹理等信息
        if (mPath == null || mPath.size() <= 0) {
            //bad data
        } else {
            for (int i = 0; i < mPath.size() - 1; ++i) {
                // TODO: 2016/7/4 宽高都需要乘以2才与Rajawali那边的比例一致,暂时不清楚为什么(不是因为z轴)
                Vector3 p1 = new Vector3(mPath.get(i));
                p1.x *= 2;
                p1.y *= 2;
                p1.z *= 2;
                Vector3 p2 = new Vector3(mPath.get(i + 1));
                p2.x *= 2;
                p2.y *= 2;
                p2.z *= 2;
                Vector3 planeVector3_position = new Vector3((p1.x + p2.x) / 2, (p1.y + p2.y) / 2, (p1.z + p2.z) / 2);
                int randColor = 0xffff0000;

                int vIndex = i * 4 * 3;
                // TODO: 2016/7/4

                /*//p2
                //左上
                vertices[vIndex + 0] = (float) (-planeSize * 4 + (p2.x - planeVector3_position.x));
                vertices[vIndex + 1] = (float) (p2.y - p1.y) / 2 + 0.01f;
                vertices[vIndex + 2] = 0;
                //右上
                vertices[vIndex + 3] = (float) (planeSize * 4 + (p2.x - planeVector3_position.x));
                vertices[vIndex + 4] = (float) (p2.y - p1.y) / 2 + 0.01f;
                vertices[vIndex + 5] = 0;
                //p1
                //右下
                vertices[vIndex + 6] = (float) (planeSize * 4 + (p1.x - planeVector3_position.x));
                vertices[vIndex + 7] = -(float) (p2.y - p1.y) / 2 - 0.01f;
                vertices[vIndex + 8] = 0;
                //左下
                vertices[vIndex + 9] = (float) (-planeSize * 4 + (p1.x - planeVector3_position.x));
                vertices[vIndex + 10] = -(float) (p2.y - p1.y) / 2 - 0.01f;
                vertices[vIndex + 11] = 0;*/

                //p2
                //左上
                Vector3 leftUp = new Vector3(mPathLeft.get(i+1));
                leftUp.x*=2;
                leftUp.y*=2;
                leftUp.z*=2;
                vertices[vIndex + 0] = (float) ((leftUp.x-p2.x)+(p2.x-planeVector3_position.x));
                vertices[vIndex + 1] = (float) (p2.y-p1.y)/2+0.1f;
                vertices[vIndex + 1] = (float) ((leftUp.y-p2.y)+(p2.y-planeVector3_position.y))+0.015f;
                vertices[vIndex + 2] = (float) leftUp.z;
                //右上
                Vector3 rightUp = new Vector3(mPathRight.get(i+1));
                rightUp.x*=2;
                rightUp.y*=2;
                rightUp.z*=2;
                vertices[vIndex + 3] = (float) ((rightUp.x-p2.x)+(p2.x-planeVector3_position.x));
                vertices[vIndex + 4] = (float) (p2.y-p1.y)/2+0.1f;
                vertices[vIndex + 4] = (float) ((rightUp.y-p2.y)+(p2.y-planeVector3_position.y))+0.015f;
                vertices[vIndex + 5] = (float) rightUp.z;
                //p1
                //右下
                Vector3 rightDown = new Vector3(mPathRight.get(i));
                rightDown.x*=2;
                rightDown.y*=2;
                rightDown.z*=2;
                vertices[vIndex + 6] = (float) ((rightDown.x-p1.x) + (p1.x - planeVector3_position.x));
                vertices[vIndex + 7] = -(float) (p2.y-p1.y)/2-0.1f;
                vertices[vIndex + 7] = (float) ((rightDown.y-p2.y)+(p2.y-planeVector3_position.y))+0.015f;
                vertices[vIndex + 8] = (float) rightDown.z;
                //左下
                Vector3 leftDown = new Vector3(mPathLeft.get(i));
                leftDown.x*=2;
                leftDown.y*=2;
                leftDown.z*=2;
                vertices[vIndex + 9]  = (float) ((leftDown.x-p1.x)+ (p1.x - planeVector3_position.x));
                vertices[vIndex + 10] = -(float) (p2.y-p1.y)/2-0.1f;
                vertices[vIndex + 10] = (float) ((leftDown.y-p2.y)+(p2.y-planeVector3_position.y))+0.015f;
                vertices[vIndex + 11] = (float) leftDown.z;

                for (int j = 0; j < 12; j += 3) {
                    normals[vIndex + j] = 0;
                    normals[vIndex + j + 1] = 0;
                    normals[vIndex + j + 2] = 1;

                    planePositions[vIndex + j] = (float) planeVector3_position.x;
                    planePositions[vIndex + j + 1] = (float) planeVector3_position.y;
                    planePositions[vIndex + j + 2] = (float) planeVector3_position.z;
                }

                vIndex = i * 4 * 4;

                for (int j = 0; j < 16; j += 4) {
                    colors[vIndex + j] = Color.red(randColor) / 255f;
                    colors[vIndex + j + 1] = Color.green(randColor) / 255f;
                    colors[vIndex + j + 2] = Color.blue(randColor) / 255f;
                    colors[vIndex + j + 3] = 1.0f;
                }

                vIndex = i * 4 * 2;

                float u1 = .25f * 0;
                float v1 = .25f * 0;
                float u2 = u1 + .25f;
                float v2 = v1 + .25f;

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

                float rotationSpeed = -1f + 0 * 2f;
                rotationSpeeds[vIndex + 0] = rotationSpeed;
                rotationSpeeds[vIndex + 1] = rotationSpeed;
                rotationSpeeds[vIndex + 2] = rotationSpeed;
                rotationSpeeds[vIndex + 3] = rotationSpeed;
            }
        }

        setData(vertices, normals, textureCoords, colors, indices);

        mPlanePositions = ByteBuffer.allocateDirect(planePositions.length * Geometry3D.FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mPlanePositions.put(planePositions);

        //        mRotationSpeeds = ByteBuffer.allocateDirect(rotationSpeeds.length * Geometry3D.FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        //        mRotationSpeeds.put(rotationSpeeds);

        createBuffers();

        //clear memory
        mPath.clear();
        mPathLeft.clear();
        mPathRight.clear();
    }

    private void createBuffers() {
        mGeometry.createBuffer(mPlanePositionsBufferInfo, BufferType.FLOAT_BUFFER, mPlanePositions, GLES20.GL_ARRAY_BUFFER);

        mMaterialPlugin.setPlanePositions(mPlanePositionsBufferInfo.bufferHandle);
    }

    public void reload() {
        super.reload();
        createBuffers();
    }

    public PlanesGaloreMaterialPlugin getMaterialPlugin() {
        return mMaterialPlugin;
    }
}
