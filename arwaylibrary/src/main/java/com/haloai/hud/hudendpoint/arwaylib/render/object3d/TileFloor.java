package com.haloai.hud.hudendpoint.arwaylib.render.object3d;

import android.graphics.Color;
import android.opengl.GLES20;

import org.rajawali3d.debug.DebugObject3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;

import java.util.Stack;

public class TileFloor extends DebugObject3D {
    private float mSize;
    private int mNumLines;

    public TileFloor() {
        this(10);
    }

    public TileFloor(float size) {
        this(size, Color.WHITE, 1, 20);
    }

    public TileFloor(float size, int color, int lineThickness, int numLines) {
        super(color, lineThickness);
        mSize = size;
        mNumLines = numLines;
        createGridFloor();
    }

    private void createGridFloor() {
        final float sizeHalf = mSize * 0.5f;
        final float spacing = mSize / mNumLines;

        mPoints = new Stack<>();

        for(float y = -sizeHalf; y <= sizeHalf; y += spacing) {
            mPoints.add(new Vector3(-sizeHalf, y,0));
            mPoints.add(new Vector3(sizeHalf, y,0));
        }

        for(float x = -sizeHalf; x <= sizeHalf; x += spacing) {
            mPoints.add(new Vector3(x, -sizeHalf,0));
            mPoints.add(new Vector3(x, sizeHalf, 0));
        }

        setMaterial(new Material());
        init(true);
        setDrawingMode(GLES20.GL_LINES);
    }
}
