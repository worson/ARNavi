package demo.opengl.haloai.com.rajawalidemo;

import android.content.Context;

import java.util.Random;
import java.util.Stack;

import javax.microedition.khronos.opengles.GL10;

import rajawali.animation.Animation;
import rajawali.animation.RotateOnAxisAnimation;
import rajawali.lights.DirectionalLight;
import rajawali.materials.Material;
import rajawali.materials.methods.DiffuseMethod;
import rajawali.math.vector.Vector3;
import rajawali.primitives.Cube;
import rajawali.primitives.Line3D;
import rajawali.renderer.RajawaliRenderer;

/**
 * author       : 龙;
 * date         : 2016/6/22;
 * email        : helong@haloai.com;
 * package_name : demo.opengl.haloai.com.rajawalidemo;
 * project_name : RajawaliDemo;
 */
public class DemoRenderer4 extends RajawaliRenderer {

    public DemoRenderer4(Context context) {
        super(context);

        setFrameRate(50);
    }

    private Cube cube = new Cube(0.1f);

    public void initScene() {
        getCurrentCamera().setZ(2);

        DirectionalLight light = new DirectionalLight(0, .2f, -1);
        getCurrentScene().addLight(light);

        Material material = new Material();
        material.enableLighting(true);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());
        cube.setColor(0xff00ff);
        cube.setMaterial(material);
        cube.setPosition(new Vector3());
        getCurrentScene().addChild(cube);

        Stack<Vector3> line = new Stack<>();
        line.add(new Vector3(0,0,0));
        line.add(new Vector3(0,1,0));
        Line3D mLine3D = new Line3D(line, 5, 0xff0000);


        //====================================start========================================//
        //TextureAtlas atlas = new TextureAtlas(0, 0, true);
        //        mLine3D.setAtlasTile("line_atlas", atlas);
//                mLine3D.setBackSided(true);
        //混合
//                mLine3D.setBlendingEnabled(true);
        //深度 掩末
//                mLine3D.setDepthMaskEnabled(true);
        //设置双边
//                mLine3D.setDoubleSided(true);
        //设置绘制模式
        /**
         * GL_POINTS, GL_LINE_STRIP, GL_LINE_LOOP, GL_LINES,
         * GL_TRIANGLE_STRIP, GL_TRIANGLE_FAN, GL_TRIANGLES,
         * GL_QUAD_STRIP, GL_QUADS, 以及GL_POLYGON。
         */
//                mLine3D.setDrawingMode(GLES20.GL_POLYGON);
        //        mLine3D.set
//        mLine3D.setFrustumTest(true);
//        mLine3D.setForcedDepth(true);
//        mLine3D.setPartOfBatch(true);
//        mLine3D.setShowBoundingVolume(true);

        //=====================================end=========================================//


        mLine3D.setMaterial(material);
        mLine3D.setPosition(new Vector3());
        getCurrentScene().addChild(mLine3D);

        /*RotateAnimation3D rotateAnimation3D = new RotateAnimation3D(1000,1000,1000);
        rotateAnimation3D.setDurationMilliseconds(1000);
        rotateAnimation3D.setRepeatMode(Animation.RepeatMode.REVERSE);
        rotateAnimation3D.setTransformable3D(cube);
        rotateAnimation3D.play();*/

        Random random = new Random();
        Vector3 randomAxis = new Vector3(random.nextFloat(), random.nextFloat(), random.nextFloat());
        randomAxis.normalize();
        RotateOnAxisAnimation anim = new RotateOnAxisAnimation(randomAxis, 360);
        anim.setTransformable3D(cube);
        anim.setDurationMilliseconds(3000 + (int)((random.nextDouble()-0.5) * 5000));
        anim.setRepeatMode(Animation.RepeatMode.INFINITE);
        getCurrentScene().registerAnimation(anim);
        anim.play();

        /*CatmullRomCurve3D mCameraCurve;
        mCameraCurve = new CatmullRomCurve3D();
        mCameraCurve.addPoint(new Vector3(0,0,0));
        mCameraCurve.addPoint(new Vector3(0.2,0.1,0));
        mCameraCurve.addPoint(new Vector3(0.3,0.3,0));
        mCameraCurve.addPoint(new Vector3(0.5,0.2,0));
        mCameraCurve.addPoint(new Vector3(0.1,0.2,0));

        SplineTranslateAnimation3D cameraAnim = new SplineTranslateAnimation3D(mCameraCurve);
        cameraAnim.setDurationMilliseconds(3000);
        cameraAnim.setTransformable3D(cube);
        cameraAnim.setInterpolator(new LinearInterpolator());
        cameraAnim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
        getCurrentScene().registerAnimation(cameraAnim);
        cameraAnim.play();*/

        /*CompoundCurve3D compound = new CompoundCurve3D();
        compound.addCurve(new CubicBezierCurve3D(new Vector3(-1, 0, 2),
                                                 new Vector3(-1, 1.3f, 2), new Vector3(-.5f, -1.9f, 2),
                                                 new Vector3(-.5f, 0, 2)));
        compound.addCurve(new LinearBezierCurve3D(new Vector3(-.5f, 0, 2),
                                                  new Vector3(0, 0, 2)));
        compound.addCurve(new QuadraticBezierCurve3D(new Vector3(0, 0, 2),
                                                     new Vector3(.3f, 1, 2), new Vector3(.5f, 0, 2)));

        CatmullRomCurve3D catmull = new CatmullRomCurve3D();
        catmull.addPoint(new Vector3(0, 1, 2)); // control point 1
        catmull.addPoint(new Vector3(.5f, 0, 2)); // start point
        catmull.addPoint(new Vector3(.7f, .3f, 2));
        catmull.addPoint(new Vector3(.75f, -.2f, 2));
        catmull.addPoint(new Vector3(.9f, .5f, 2));
        catmull.addPoint(new Vector3(1, 0, 2)); // end point
        catmull.addPoint(new Vector3(1.5f, -1, 2)); // control point 2

        compound.addCurve(catmull);
        SplineTranslateAnimation3D cameraAnim = new SplineTranslateAnimation3D(compound);
        cameraAnim.setDurationMilliseconds(10000);
        cameraAnim.setTransformable3D(getCurrentCamera());
        cameraAnim.setInterpolator(new LinearInterpolator());
        cameraAnim.setRepeatMode(Animation.RepeatMode.NONE);
        getCurrentScene().registerAnimation(cameraAnim);
        cameraAnim.play();*/
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        super.onDrawFrame(glUnused);

        getCurrentCamera().setZ(getCurrentCamera().getZ()+0.03);
    }

    public void pause() {}

    public void continue_() {}
}