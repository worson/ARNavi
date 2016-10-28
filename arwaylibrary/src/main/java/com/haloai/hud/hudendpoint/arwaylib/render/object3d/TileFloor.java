package com.haloai.hud.hudendpoint.arwaylib.render.object3d;

import com.haloai.hud.hudendpoint.arwaylib.render.vertices.GeometryData;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.utils.HaloLogger;

public class TileFloor extends BaseObject3D {
    private float mWidthRate = 0f;

    public TileFloor(float width,float height,float spacing) {
        this(width, height, spacing, 0);
    }
    public TileFloor(float width,float height,float spacing,float widthrate) {
        super();
        mWidthRate = widthrate;
        initGridFloor(width, height, spacing, mWidthRate);
    }

    private void initGridFloor(float width,float height,float spacing,float widthrate) {
        GeometryData geometryData = getGeometryData(width, height, spacing,widthrate);
        addVerties(geometryData);
        applyVerties();
    }
    public static GeometryData getGeometryData(float width,float height,float spacing,float widthrate){
        int widthNum = (int) (width/spacing)+1;
        int heightNum = (int) (height/spacing)+1;
        print(String.format("widthNum = %s,heightNum=%s \n",widthNum,heightNum));
        if(widthNum <= 0 || heightNum <= 0){
            return null;
        }
        int vertexsCnt = (widthNum)*(heightNum)*4;

        float[] vertices = new float[(vertexsCnt)*3];
        float[] coords = new float[(vertexsCnt)*2];
        int[] indices = new int[(widthNum*heightNum)*2*3];
        int index = 0;
        float x = -width/2;
        float y = height/2+spacing;
        float z = 0;
        float textureWidth = 1-widthrate;
        float s0=0.f,s1=textureWidth,t0=0.f,t1=textureWidth;
        for (int i = 0; i < heightNum; i++) {
            for (int j = 0; j < widthNum; j++) {

                vertices[index++] = x+spacing;
                vertices[index++] = y;
                vertices[index++] = z;

                vertices[index++] = x;
                vertices[index++] = y;
                vertices[index++] = z;

                vertices[index++] = x;
                vertices[index++] = y-spacing;
                vertices[index++] = z;

                vertices[index++] = x+spacing;
                vertices[index++] = y-spacing;
                vertices[index++] = z;

                /*print(String.format("vertices x = %s,y=%s \n",x,y));
                print(String.format("vertices x = %s,y=%s \n",x+spacing,y));
                print(String.format("vertices x = %s,y=%s \n",x+spacing,y-spacing));*/

                x += spacing;
            }
            x = -width/2;
            y -= spacing;
        }
        print(String.format("vertices index = %s,length=%s \n",index,vertices.length));

        index = 0;
        for (int i = 0; i < heightNum; i++) {
            for (int j = 0; j < widthNum; j++) {
                coords[index++] = s1;coords[index++] = t1;
                coords[index++] = s0;coords[index++] = t1;
                coords[index++] = s0;coords[index++] = t0;
                coords[index++] = s1;coords[index++] = t0;
            }
        }
        print(String.format("coords index = %s,length=%s\n",index,coords.length));
        index = 0;
        for (int i = 0; i < vertexsCnt; i += 4) {
            indices[index++] = i+0;
            indices[index++] = i+1;
            indices[index++] = i+2;

            indices[index++] = i+2;
            indices[index++] = i+3;
            indices[index++] = i+0;
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
        float x = -width/2;
        float y = height/2+spacing;
        float z = 0;

        for (int i = 0; i < heightNum; i++) {
            for (int j = 0; j < widthNum; j++) {

                vertices[index++] = x+spacing;
                vertices[index++] = y;
                vertices[index++] = z;

                vertices[index++] = x;
                vertices[index++] = y;
                vertices[index++] = z;

                vertices[index++] = x;
                vertices[index++] = y-spacing;
                vertices[index++] = z;

                /*print(String.format("vertices x = %s,y=%s \n",x,y));
                print(String.format("vertices x = %s,y=%s \n",x+spacing,y));
                print(String.format("vertices x = %s,y=%s \n",x+spacing,y-spacing));*/

                x += spacing;
            }
            x = -width/2;
            y -= spacing;
        }
        print(String.format("vertices index = %s,length=%s \n",index,vertices.length));

        index = 0;
        for (int i = 0; i < heightNum; i++) {
            for (int j = 0; j < widthNum; j++) {
                coords[index++] = 1;coords[index++] = 1;
                coords[index++] = 0;coords[index++] = 1;
                coords[index++] = 0;coords[index++] = 0;
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

    private static void print(String msg){
        HaloLogger.logE(ARWayConst.SPECIAL_LOG_TAG,msg);
//        System.out.print(ARWayConst.SPECIAL_LOG_TAG+msg);
    }

}
