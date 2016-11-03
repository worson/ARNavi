package com.haloai.hud.hudendpoint.arwaylib.render.vertices;

import android.graphics.Color;

import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.hudendpoint.arwaylib.utils.TimeRecorder;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.x;
import static com.loc.e.j;
import static org.rajawali3d.math.vector.Vector3.distanceTo;

/**
 * Created by wangshengxing on 16/10/10.
 */
public class TextureRoadGeometryProcessor extends GeometryProcessor{

    public static final int VERTICES_PER_LINE = 8;
    public static final int TRIANGLE_PER_LINE = 6;
    private static final boolean IS_LOG_OUT       = false;
    private static int mColor = Color.RED;

    private GeometryData mGeometryData = null;

    public TextureRoadGeometryProcessor(List<Vector3> path,final Vector3 offset,float width) {
        mGeometryData = getGeometryData(path, offset,width, 0);
        setDateOk(true);
    }

    public static void generateVertices(List<Vector3> path, final Vector3 offset,float[] vertices, float width){
        int size = path.size();
        int index = 0;
        int lineCount = size-1;
        Vector3 sub = new Vector3(offset);
        Vector3 a = new Vector3();
        Vector3 b = new Vector3();
        Vector3 e = new Vector3();

        Vector3 N  = new Vector3();
        Vector3 S  = new Vector3();
        Vector3 NE = new Vector3();
        Vector3 NW = new Vector3();
        Vector3 SW = new Vector3();
        Vector3 SE = new Vector3();

        Vector3 v = new Vector3();

        Vector3[] add1 = new Vector3[]{a,a,a,a,b,b,b,b};
        Vector3[] add2 = new Vector3[]{SW,NW,S,N,S,N,SE,NE};

        for (int i = 0; i < lineCount; i++) {
            Vector3 current = path.get(i);
            Vector3 next = path.get(i+1);
            a.subtractAndSet(current,sub);
            b.subtractAndSet(next,sub);

            e.subtractAndSet(b,a);
            e.normalize();
            e.multiply(width);

            N .setAll(-e.y, e.x, 0);
            S .setAll(-N.x, -N.y, -N.z);
            NE.addAndSet(N, e);
            NW.subtractAndSet(N, e);
            SW.setAll(-NE.x, -NE.y, -NE.z);
            SE.setAll(-NW.x, -NW.y, -NW.z);

            for (int j = 0; j < VERTICES_PER_LINE; j++) {
                v.addAndSet(add1[j],add2[j]);
                vertices[index++] = (float) v.x;
                vertices[index++] = (float) v.y;
                vertices[index++] = (float) 0;
                if (IS_LOG_OUT){
                    print(String.format("%s %s \n",v.x,v.y));
                }
            }
        }
    }

    /**
     * 生成纹理坐标
     * 每根线有8个顶点
     * @param lineCount
     * @param coords
     */
    public static void generateCoords(int lineCount,float[] coords){
        int index = 0;
        for (int i = 0; i <lineCount ; i++) {
            coords[index++] = (float) 0;coords[index++] = (float) 0;
            coords[index++] = (float) 0;coords[index++] = (float) 1;
            coords[index++] = (float) 0.5f;coords[index++] = (float) 0;
            coords[index++] = (float) 0.5f;coords[index++] = (float) 1;
            coords[index++] = (float) 0.5f;coords[index++] = (float) 0;
            coords[index++] = (float) 0.5f;coords[index++] = (float) 1;
            coords[index++] = (float) 1;coords[index++] = (float) 0;
            coords[index++] = (float) 1;coords[index++] = (float) 1;
        }

    }

    /**
     * 生成索引坐标
     * @param lineCount
     * @param indices
     */
    public static void generateIndices(int lineCount,int[] indices,boolean noTail){
        int index = 0;
        int offset = 0;
        int cnt = lineCount;
        if (noTail) {
            cnt = lineCount-1;
        }
        for (int i = 0; i <cnt ; i++) {
            offset = i*VERTICES_PER_LINE;
            indices[index++] = offset+0;indices[index++] = offset+1;indices[index++] = offset+2;
            indices[index++] = offset+2;indices[index++] = offset+1;indices[index++] = offset+3;
            indices[index++] = offset+2;indices[index++] = offset+3;indices[index++] = offset+4;
            indices[index++] = offset+4;indices[index++] = offset+3;indices[index++] = offset+5;
            indices[index++] = offset+4;indices[index++] = offset+5;indices[index++] = offset+6;
            indices[index++] = offset+6;indices[index++] = offset+5;indices[index++] = offset+7;
        }
        if (noTail) {
            offset = cnt*VERTICES_PER_LINE;
            indices[index++] = offset+0;indices[index++] = offset+1;indices[index++] = offset+2;
            indices[index++] = offset+2;indices[index++] = offset+1;indices[index++] = offset+3;
            indices[index++] = offset+2;indices[index++] = offset+3;indices[index++] = offset+4;
            indices[index++] = offset+4;indices[index++] = offset+3;indices[index++] = offset+5;
        }
    }

    private static void generateNormalsAlpha(List<Vector3> path, int lineCount, float[] normals) {
        if (lineCount<=0 || normals == null ){
            return;
        }
        lineCount +=1;
        float[] alphas = new float[lineCount];
        double distSum = 0;
        for (int i = 0; i < lineCount-1; i++) {
            Vector3 p0 = path.get(i);
            Vector3 p1 = path.get(i+1);
            double dist =  Vector3.distanceTo(p0,p1);
            alphas[i] = (float)distSum;
            distSum += dist;
        }
        alphas[alphas.length-1] = (float)distSum;
        int index = 0;
        print("start");
        for (int i = 0; i <lineCount-1 ; i++) {
            float a0 = 1-(float) ((alphas[i])/distSum);
            float a1 = 1-(float) ((alphas[i+1])/distSum);
//            a0 = (float) Math.sqrt((1-Math.pow(1-a0,2)));
//            a1 = (float) Math.sqrt((1-Math.pow(1-a1,2)));
//            print("a="+a0);
            for (int j = 0; j < VERTICES_PER_LINE-4; j++) {
                normals[index++] = a0;
                normals[index++] = a0;
                normals[index++] = a0;
            }
            for (int j = 4; j < VERTICES_PER_LINE; j++) {
                normals[index++] = a1;
                normals[index++] = a1;
                normals[index++] = a1;
            }
        }
        /*for (int j = 0; j < VERTICES_PER_LINE-4; j++) {
            normals[index++] = 0;
            normals[index++] = 0;
            normals[index++] = 0;
        }*/
        for (int i = 0; i < normals.length; i++) {
            print("i="+i+" a="+normals[i]);
        }
        print("end");
    }

    public static void generateNormals(int lineCount,float[] normals){
        int index = 0;
        final int cnt = lineCount*VERTICES_PER_LINE;
        for (int i = 0; i <cnt ; i++) {
            normals[index++] = 0;normals[index++] = 0;normals[index++] = 1;
        }
    }

    public static void generateColors(int lineCount,float[] colors){
        int index = 0;
        float r = Color.red(mColor) / 255.f;
        float g = Color.green(mColor) / 255.f;
        float b = Color.blue(mColor) / 255.f;
        float a = Color.alpha(mColor) / 255.f;
        final int cnt = lineCount*VERTICES_PER_LINE;
        for (int i = 0; i <cnt ; i++) {
            colors[index++] = r;colors[index++] = g;colors[index++] = b;colors[index++] = a;
        }
    }

    /**
     * 成功渲染数据
     * @param path
     * @param width
     * @return
     */
    public static GeometryData getGeometryData(List<Vector3> path,Vector3 offset, float width, int color){
        if (path == null || path.size() <=1) {
            return null;
        }
        boolean isFog = false;
        mColor = color;
        int lineCount = path.size()-1;
        if(IS_LOG_OUT) {
            print(String.format("path lineCount = %s", lineCount));
        }
        boolean noTail = true;
        int removeVertices = 0;
        if (noTail){
            removeVertices = 2;
        }
        float[] vertexs = new float[(lineCount*VERTICES_PER_LINE)*3];
        float[] coords = new float[(lineCount*VERTICES_PER_LINE)*2];
        int[] indices = new int[(lineCount*TRIANGLE_PER_LINE-removeVertices)*3];

        float[] normals = new float[(lineCount)*VERTICES_PER_LINE*3];
//        float[] colors = new float[(lineCount)*VERTICES_PER_LINE * NUMBER_OF_COLOR];

        generateVertices(path,offset,vertexs,width);
        generateIndices(lineCount,indices,noTail);
        generateCoords(lineCount,coords);

//        generateNormals(lineCount,normals);
        if(isFog){
            generateNormalsAlpha(path,lineCount,normals);
        }
//        generateColors(lineCount,colors);

        GeometryData element = new GeometryData();
        element.setUseTextureCoords(true);
        element.setUseColors(false);
        element.setUseNormals(isFog);
        element.vertices = vertexs;
        element.textureCoords = coords;
        element.indices = indices;
        if(isFog){
            element.normals = normals;
        }
        /*element.colors = colors;*/

        return element;
    }



    public static void main(String[] args) {

//        testGetGeometryData();
        testGetGeometryDataTime();

    }

    private static void testGetGeometryDataTime() {
        int testCnt = 500;
        int total = 100000;
        TimeRecorder timeRecorder = new TimeRecorder();
        for (int j = 0; j < testCnt; j++) {
            timeRecorder.start();
            List<Vector3> path = new ArrayList<>(total);
            for (int i = 0; i < total; i++) {
                path.add(new Vector3(i,0,0));
            }
            GeometryData element = getGeometryData(path,path.get(0),0.1f, Color.RED);
            timeRecorder.recordeAndPrint(String.format("testCnt=%s",j));
        }

    }

    private static void testGetGeometryData() {
        List<Vector3> path = new ArrayList<>();
        path.add(new Vector3(1,0,0));
        path.add(new Vector3(6,0,0));
        path.add(new Vector3(7,0,0));
        path.add(new Vector3(8,0,0));
        path.add(new Vector3(12,0,0));

        GeometryData element = getGeometryData(path,path.get(0),0.1f, Color.RED);
        System.out.print(element.getDebugInfo());
    }

    @Override
    public GeometryData getGeometryData() {
        return mGeometryData;
    }

    private static void print(String msg){
//        HaloLogger.logE(ARWayConst.SPECIAL_LOG_TAG,msg);
//        System.out.print(msg);
    }
}
