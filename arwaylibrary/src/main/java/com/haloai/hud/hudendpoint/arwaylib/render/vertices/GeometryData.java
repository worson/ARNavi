package com.haloai.hud.hudendpoint.arwaylib.render.vertices;

import java.util.List;

/**
 * Created by wangshengxing on 16/9/9.
 */
public class GeometryData {

    //顶点数据之间可以共用
    public float[] vertices = null;
    public float[] textureCoords = null;
    public float[] normals = null;
    public float[] colors = null;
    public int[] indices = null;

    private boolean mUseNormals       = false;
    private boolean mUseColors        = false;
    private boolean mUseTextureCoords = false;

    public boolean isDataValid(){
        return (vertices != null && (!mUseNormals || normals != null)
                && (!mUseColors || colors != null) && indices != null
                && (!mUseTextureCoords || textureCoords != null));
    }

    public String getDebugInfo(){
        StringBuilder sb = new StringBuilder();
        //顶点数据之间可以共用
        if(mUseTextureCoords) {
            sb.append(String.format("textureCoords size is %s\n",textureCoords.length));
        }
        if (mUseNormals) {
            sb.append(String.format("normals size is %s\n",normals.length));
        }
        if(mUseColors) {
            sb.append(String.format("colors size is %s\n",colors.length));
        }
        sb.append(String.format("vertices size is %s\n",vertices.length));
        sb.append(String.format("indices size is %s\n",indices.length));
        return sb.toString();
    }
    public void free(){
        vertices = null;
        textureCoords = null;
        normals = null;
        colors = null;
        indices = null;
    }

    public boolean isUseNormals() {
        return mUseNormals;
    }

    public void setUseNormals(boolean useNormals) {
        mUseNormals = useNormals;
    }

    public boolean isUseColors() {
        return mUseColors;
    }

    public void setUseColors(boolean useColors) {
        mUseColors = useColors;
    }

    public boolean isUseTextureCoords() {
        return mUseTextureCoords;
    }

    public void setUseTextureCoords(boolean useTextureCoords) {
        mUseTextureCoords = useTextureCoords;
    }

    public  static GeometryData addAllElement(List<GeometryData> elementList){
        if (elementList == null || elementList.size()<1) {
            return null;
        }
        if(elementList.size() == 1){
            return elementList.get(0);
        }
        int verticesSize = 0;
        int textureCoordsSize = 0;
        int normalsSize = 0;
        int colorsSize = 0;
        int indicesSize = 0;
        boolean hasData = false;
        boolean useTextureCoords = true;
        boolean useNormals = true;
        boolean useColors = true;
        for(GeometryData element :elementList){
            if (element != null) {
                useTextureCoords &= element.mUseTextureCoords;
                useNormals &= element.mUseNormals;
                useColors &= element.mUseColors;
            }
        }
        for(GeometryData element :elementList){
            if(element != null &&  element.isDataValid()){
                verticesSize += element.vertices.length;
                indicesSize += element.indices.length;
                if(useTextureCoords){
                    textureCoordsSize += element.textureCoords.length;
                }
                if (useNormals){
                    normalsSize += element.normals.length;
                }
                if(useColors){
                    colorsSize += element.colors.length;
                }
                hasData = true;
            }
        }
        if(hasData) {
            GeometryData totalElement = new GeometryData();
            //顶点数据之间可以共用
            totalElement.vertices = new float[verticesSize];
            totalElement.indices = new int[indicesSize];
            if(useTextureCoords) {
                totalElement.textureCoords = new float[textureCoordsSize];
            }
            if (useNormals) {
                totalElement.normals = new float[normalsSize];
            }
            if(useColors) {
                totalElement.colors = new float[colorsSize];
            }

            int verticesIndex = 0;
            int textureCoordsIndex = 0;
            int normalsIndex = 0;
            int colorsIndex = 0;
            int indicesIndex = 0;
            for (GeometryData element : elementList) {
                if (element != null && element.isDataValid()) {
                    for (int i = 0; i < element.indices.length; i++) {
                        element.indices[i] += indicesIndex;
                    }
                    System.arraycopy(element.vertices, 0, totalElement.vertices, verticesIndex, element.vertices.length);
                    System.arraycopy(element.indices, 0, totalElement.indices, indicesIndex, element.indices.length);
                    if(useTextureCoords) {
                        System.arraycopy(element.textureCoords, 0, totalElement.textureCoords, textureCoordsIndex, element.textureCoords.length);
                        textureCoordsIndex += element.textureCoords.length;
                    }
                    if (useNormals) {
                        System.arraycopy(element.normals, 0, totalElement.normals, normalsIndex, element.normals.length);
                        normalsIndex += element.normals.length;
                    }
                    if(useColors) {
                        System.arraycopy(element.colors, 0, totalElement.colors, colorsIndex, element.colors.length);
                        colorsIndex += element.colors.length;
                    }
                    verticesIndex += element.vertices.length;
                    indicesIndex += element.indices.length;

                    element.free();
                }
            }
            return totalElement;
        }
        return null;

    }
}