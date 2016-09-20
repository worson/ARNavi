package com.haloai.hud.hudendpoint.arwaylib.rajawali.object3d;

import java.util.List;

/**
 * Created by wangshengxing on 16/9/9.
 */
public class ObjectElement {

    //顶点数据之间可以共用
    public float[] vertices = null;
    public float[] textureCoords = null;
    public float[] normals = null;
    public float[] colors = null;
    public int[] indices = null;

    public boolean isDataValid(){
        return (vertices != null && textureCoords != null && normals != null && colors != null && indices != null);
    }

    public void free(){
        vertices = null;
        textureCoords = null;
        normals = null;
        colors = null;
        indices = null;
    }

    public static ObjectElement addAllElement(List<ObjectElement> elementList){
        if (elementList == null) {
            return null;
        }
        int verticesSize = 0;
        int textureCoordsSize = 0;
        int normalsSize = 0;
        int colorsSize = 0;
        int indicesSize = 0;
        boolean hasData = false;
        for(ObjectElement element :elementList){
            if(element != null &&  element.isDataValid()){
                verticesSize += element.vertices.length;
                textureCoordsSize += element.textureCoords.length;
                normalsSize += element.normals.length;
                colorsSize += element.colors.length;
                indicesSize += element.indices.length;
                hasData = true;
            }
        }
        if(hasData) {
            ObjectElement totalElement = new ObjectElement();
            //顶点数据之间可以共用
            totalElement.vertices = new float[verticesSize];
            totalElement.textureCoords = new float[textureCoordsSize];
            totalElement.normals = new float[normalsSize];
            totalElement.colors = new float[colorsSize];
            totalElement.indices = new int[indicesSize];

            int verticesIndex = 0;
            int textureCoordsIndex = 0;
            int normalsIndex = 0;
            int colorsIndex = 0;
            int indicesIndex = 0;
            for (ObjectElement element : elementList) {
                if (element != null && element.isDataValid()) {
                    for (int i = 0; i < element.indices.length; i++) {
                        element.indices[i] += indicesIndex;
                    }
                    System.arraycopy(element.vertices, 0, totalElement.vertices, verticesIndex, element.vertices.length);
                    System.arraycopy(element.textureCoords, 0, totalElement.textureCoords, textureCoordsIndex, element.textureCoords.length);
                    System.arraycopy(element.normals, 0, totalElement.normals, normalsIndex, element.normals.length);
                    System.arraycopy(element.colors, 0, totalElement.colors, colorsIndex, element.colors.length);
                    System.arraycopy(element.indices, 0, totalElement.indices, indicesIndex, element.indices.length);

                    verticesIndex += element.vertices.length;
                    textureCoordsIndex += element.textureCoords.length;
                    normalsIndex += element.normals.length;
                    colorsIndex += element.colors.length;
                    indicesIndex += element.indices.length;

                    element.free();
                }
            }
            return totalElement;
        }
        return null;

    }
}