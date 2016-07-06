package demo.opengl.haloai.com.rajawalidemo;

import android.content.Context;

import javax.microedition.khronos.opengles.GL10;

import rajawali.materials.Material;
import rajawali.primitives.Plane;
import rajawali.renderer.RajawaliRenderer;

/**
 * author       : 龙;
 * date         : 2016/6/22;
 * email        : helong@haloai.com;
 * package_name : demo.opengl.haloai.com.rajawalidemo;
 * project_name : RajawaliDemo;
 */
public class DemoRenderer6 extends RajawaliRenderer {

    private Material material = new Material();

    public DemoRenderer6(Context context) {
        super(context);

        mContext = context;

        setFrameRate(50);
    }
    public void initScene() {
        material = new Material();
        Plane plane = new Plane(1f,1f,24,24);
        plane.setMaterial(material);
        plane.setDoubleSided(true);
        plane.setColor(0xff0000ff);
        getCurrentScene().addChild(plane);

        material = new Material();
        plane = new Plane(1f,1f,24,24);
        plane.setMaterial(material);
        plane.setDoubleSided(true);
        plane.setColor(0xffff00ff);
        plane.setPosition(0,-1,0);
        getCurrentScene().addChild(plane);

        material = new Material();
        plane = new Plane(1f,1f,24,24);
        plane.setMaterial(material);
        plane.setDoubleSided(true);
        plane.setColor(0xff00ffff);
        plane.setPosition(0,1,0);
        getCurrentScene().addChild(plane);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        super.onDrawFrame(glUnused);
    }
}