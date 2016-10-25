package com.haloai.hud.hudendpoint.arwaylib.render.vertices;

import android.graphics.Color;

import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.hudendpoint.arwaylib.utils.TimeRecorder;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by wangshengxing on 16/10/10.
 */
public class TileFloorGeometryProcessor extends GeometryProcessor{

    private static final boolean IS_LOG_OUT       = false;
    private static int mColor = Color.RED;

    private GeometryData mGeometryData = null;

    public TileFloorGeometryProcessor(float width,float height,float spacing) {
        mGeometryData = getGeometryData( width, height, spacing);
        setDateOk(true);
    }


    /**
     *
     * @param width
     * @param height
     * @param spacing
     * @return
     */
    public static GeometryData getGeometryData(float width,float height,float spacing){
        int widthNum = (int) (width/spacing)+1;
        int heightNum = (int) (height/spacing)+1;
        print(String.format("widthNum = %s,heightNum=%s \n",widthNum,heightNum));
        if(widthNum <= 0 || heightNum <= 0){
            return null;
        }
        int vertexsCnt = (widthNum)*(heightNum)*3;

        float[] vertices = new float[(vertexsCnt)*3];
        float[] coords = new float[(vertexsCnt)*2];
        int[] indices = new int[(widthNum*heightNum)*3];
        int index = 0;
        float x = -width/2-spacing;
        float y = height/2;
        float z = 0;

        for (int i = 0; i < heightNum; i++) {
            for (int j = 0; j < widthNum; j++) {
                vertices[index++] = x;
                vertices[index++] = y;
                vertices[index++] = z;

                vertices[index++] = x+spacing;
                vertices[index++] = y;
                vertices[index++] = z;

                vertices[index++] = x+spacing;
                vertices[index++] = y-spacing;
                vertices[index++] = z;

                print(String.format("vertices x = %s,y=%s \n",x,y));
                print(String.format("vertices x = %s,y=%s \n",x+spacing,y));
                print(String.format("vertices x = %s,y=%s \n",x+spacing,y-spacing));
                x += spacing;
            }
            x = -width/2-spacing;
            y -= spacing;
        }
        print(String.format("vertices index = %s,length=%s \n",index,vertices.length));

        index = 0;
        for (int i = 0; i < heightNum; i++) {
            for (int j = 0; j < widthNum; j++) {
                coords[index++] = 0;coords[index++] = 1;
                coords[index++] = 1;coords[index++] = 1;
                coords[index++] = 1;coords[index++] = 0;
            }
        }
        print(String.format("coords index = %s,length=%s\n",index,coords.length));

        for (index = 0; index < indices.length; index++) {
            indices[index] = index;
        }
        print(String.format("indices index = %s,length=%s\n",index,indices.length));

        GeometryData element = new GeometryData();
        element.setUseTextureCoords(true);
        element.setUseColors(false);
        element.setUseNormals(false);
        element.vertices = vertices;
        element.textureCoords = coords;
        element.indices = indices;

        return element;
    }

    public static void main(String[] args) {

        testGetGeometryData();
//        testGetGeometryDataTime();

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
            timeRecorder.recordeAndPrint(String.format("testCnt=%s",j));
        }

    }

    private static void testGetGeometryData() {

        GeometryData element = getGeometryData(2,2,1);
        System.out.print(element.getDebugInfo());
    }

    @Override
    public GeometryData getGeometryData() {
        return mGeometryData;
    }

    private static void print(String msg){
        HaloLogger.logE(ARWayConst.SPECIAL_LOG_TAG,msg);
//        System.out.print(ARWayConst.SPECIAL_LOG_TAG+msg);
    }
}
