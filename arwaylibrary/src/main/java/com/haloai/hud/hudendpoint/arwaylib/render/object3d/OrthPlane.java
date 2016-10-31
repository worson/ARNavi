package com.haloai.hud.hudendpoint.arwaylib.render.object3d;

import android.util.Log;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Plane;

/**
 * Created by wangshengxing on 2016/10/29.
 */

public class OrthPlane extends Plane {


    private double mZAngel = 0.0;

    private double mXangel = 0.0;

    private double mYangel = 0.0;

    private double mScalee = 0.1;

    public OrthPlane(float width, float height, int segmentsW, int segmentsH, Vector3.Axis upAxis, boolean createTextureCoordinates,
                     boolean createVertexColorBuffer, int numTextureTiles, boolean createVBOs){
        super(width,height,segmentsW,segmentsH,upAxis,createTextureCoordinates,createVertexColorBuffer,numTextureTiles,createVBOs);
    }

    @Override
    public void render(Camera camera, Matrix4 vpMatrix, Matrix4 projMatrix, Matrix4 vMatrix, Material sceneMaterial) {
        double p = Math.sqrt(Math.pow(camera.getPosition().x - getPosition().x,2.0)
                + Math.pow(camera.getPosition().y - getPosition().y,2.0)
                +Math.pow(camera.getPosition().z - getPosition().z,2.0));


        double scale = camera.getNearPlane()/p;

        double realscal = mScalee/scale;
        setScale(realscal);

        mScalee = scale;
        // this.setRotation(Vector3.Axis.X,camera.getRotX()+Math.toRadians(90));
        //this.setRotation(Vector3.Axis.X,67.5);

        Vector3 position = camera.getPosition();
        Vector3 lookat = camera.getLookAt();


        this.rotate(Vector3.Axis.X,Math.toDegrees(camera.getRotX()-mXangel));
        mXangel = camera.getRotX();

//        this.rotate(Vector3.Axis.Y,Math.toDegrees(camera.getRotY()-mYangel));
//        mYangel = camera.getRotY();

        this.rotate(Vector3.Axis.Z, Math.toDegrees(camera.getRotZ()-mZAngel));
        mZAngel = camera.getRotZ();

        Log.e("ylq","camera.Y:"+camera.getRotY()+" CAMERA.X:"+camera.getRotX());
        super.render(camera, vpMatrix, projMatrix, vMatrix, sceneMaterial);
    }

    @Override
    public void render(Camera camera, Matrix4 vpMatrix, Matrix4 projMatrix, Matrix4 vMatrix, Matrix4 parentMatrix, Material sceneMaterial) {


        super.render(camera, vpMatrix, projMatrix, vMatrix, parentMatrix, sceneMaterial);//parentMatrix
    }

}
