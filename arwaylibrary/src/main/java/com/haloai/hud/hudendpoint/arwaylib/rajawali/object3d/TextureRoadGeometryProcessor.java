package com.haloai.hud.hudendpoint.arwaylib.rajawali.object3d;

import android.graphics.Color;

import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangshengxing on 16/10/10.
 */
public class TextureRoadGeometryProcessor extends GeometryProcessor{

    public static final int VERTICES_PER_LINE = 8;
    public static final int TRIANGLE_PER_LINE = 6;
    private static final boolean IS_LOG_OUT       = false;
    private static int mColor = Color.RED;

    public static void generateVertices(List<Vector3> path, float[] vertices, float width){
        int size = path.size();
        int index = 0;
        int lineCount = size-1;

        for (int i = 0; i < lineCount; i++) {
            Vector3 a = path.get(i);
            Vector3 b = path.get(i+1);
            Vector3 e = Vector3.subtractAndCreate(b,a);
            e.normalize();
            e.multiply(width);

            Vector3 N = new Vector3(-e.y,e.x,0);
            Vector3 S = new Vector3(-N.x,-N.y,-N.z);
            Vector3 NE = Vector3.addAndCreate(N,e);
            Vector3 NW = Vector3.subtractAndCreate(N,e);
            Vector3 SW = new Vector3(-NE.x,-NE.y,-NE.z);
            Vector3 SE = new Vector3(-NW.x,-NW.y,-NW.z);
            Vector3[] add1 = new Vector3[]{a,a,a,a,b,b,b,b};
            Vector3[] add2 = new Vector3[]{SW,NW,S,N,S,N,SE,NE};
            for (int j = 0; j < VERTICES_PER_LINE; j++) {
                Vector3 v = Vector3.addAndCreate(add1[j],add2[j]);
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
    public static void generateIndices(int lineCount,int[] indices){
        int index = 0;
        int offset = 0;
        final int cnt = lineCount-1;
        for (int i = 0; i <cnt ; i++) {
            offset = i*VERTICES_PER_LINE;
            indices[index++] = offset+0;indices[index++] = offset+1;indices[index++] = offset+2;
            indices[index++] = offset+2;indices[index++] = offset+1;indices[index++] = offset+3;
            indices[index++] = offset+2;indices[index++] = offset+3;indices[index++] = offset+4;
            indices[index++] = offset+4;indices[index++] = offset+3;indices[index++] = offset+5;
            indices[index++] = offset+4;indices[index++] = offset+5;indices[index++] = offset+6;
            indices[index++] = offset+6;indices[index++] = offset+5;indices[index++] = offset+7;
        }
        indices[index++] = offset+0;indices[index++] = offset+1;indices[index++] = offset+2;
        indices[index++] = offset+2;indices[index++] = offset+1;indices[index++] = offset+3;
        indices[index++] = offset+2;indices[index++] = offset+3;indices[index++] = offset+4;
        indices[index++] = offset+4;indices[index++] = offset+3;indices[index++] = offset+5;
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
    public static ObjectElement getObjectElement(List<Vector3> path, float width, int color){
        if (path == null || path.size() <=1) {
            return null;
        }
        mColor = color;
        int lineCount = path.size()-1;
        if(IS_LOG_OUT) {
            print(String.format("path lineCount = %s", lineCount));
        }
        float[] vertexs = new float[(lineCount)*VERTICES_PER_LINE*3];
        float[] coords = new float[(lineCount)*VERTICES_PER_LINE*2];
        int[] indices = new int[(lineCount)*TRIANGLE_PER_LINE*3-6];

        float[] normals = new float[(lineCount)*VERTICES_PER_LINE*3];
        float[] colors = new float[(lineCount)*VERTICES_PER_LINE * NUMBER_OF_COLOR];
        if(IS_LOG_OUT) {
//            print(String.format("data size ,vertexs=%s,coords=%s,indices=%s,normals=%s,colors=%s",
//                    vertexs.length, coords.length, indices.length, normals.length, colors.length));
        }
        generateVertices(path,vertexs,width);
        generateIndices(lineCount,indices);
        generateCoords(lineCount,coords);
        generateNormals(lineCount,normals);
        generateColors(lineCount,colors);

        ObjectElement element = new ObjectElement();
        element.setUseTextureCoords(true);
        element.setUseColors(false);
        element.setUseNormals(false);
        element.vertices = vertexs;
        element.textureCoords = coords;
        element.indices = indices;
        /*element.normals = normals;
        element.colors = colors;*/

        return element;
    }

    public static void main(String[] args) {
        List<Vector3> path = new ArrayList<>();
        path.add(new Vector3(1,0,0));
        path.add(new Vector3(6,0,0));
        path.add(new Vector3(7,0,0));
        path.add(new Vector3(8,0,0));
        path.add(new Vector3(12,0,0));

        ObjectElement element = getObjectElement(path,0.1f, Color.RED);
        System.out.print(element.getDebugInfo());
    }

    private static void print(String msg){
        HaloLogger.logE(ARWayConst.SPECIAL_LOG_TAG,msg);
    }
}
