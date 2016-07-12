package demo.opengl.haloai.com.rajawalidemo;

import android.content.Context;
import android.graphics.Color;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import rajawali.Object3D;
import rajawali.animation.Animation;
import rajawali.animation.SplineTranslateAnimation3D;
import rajawali.curves.CatmullRomCurve3D;
import rajawali.lights.DirectionalLight;
import rajawali.materials.Material;
import rajawali.materials.textures.ATexture;
import rajawali.materials.textures.Texture;
import rajawali.math.vector.Vector3;
import rajawali.primitives.Line3D;
import rajawali.primitives.Sphere;
import rajawali.renderer.RajawaliRenderer;

/**
 * author       : 龙;
 * date         : 2016/7/4;
 * email        : helong@haloai.com;
 * package_name : demo.opengl.haloai.com.rajawalidemo;
 * project_name : RajawaliDemo;
 */
public class DemoRenderer7 extends RajawaliRenderer {


    private static final int NUM_POINTS = 50;
    private static final int NUM_CATMULL_DESIRED_PT = 10;
    private long                       mStartTime;
    private Material                   mMaterial;
    private PlanesGaloreMaterialPlugin mMaterialPlugin;
    private SplineTranslateAnimation3D anim;

    public DemoRenderer7(Context context) {
        super(context);

        setFrameRate(50);
    }

    Sphere       sphere  = new Sphere(0.1f, 24, 24);
    Sphere       sphere2 = new Sphere(0.1f, 24, 24);
    PlanesGalore planes  = null;

    protected void initScene() {
        DirectionalLight light = new DirectionalLight(0, 0, 1);

        getCurrentScene().addLight(light);

        getCurrentCamera().setZ(2);
//        getCurrentCamera().setLookAt(0, 6, 0);


        sphere.setColor(0xffff00);
        sphere.setMaterial(new Material());
        sphere.setPosition(0, 0, 0);
        getCurrentScene().addChild(sphere);

        sphere2.setColor(0xff00ff);
        sphere2.setMaterial(new Material());
        sphere2.setPosition(0, 0, 0);
        getCurrentScene().addChild(sphere2);

        List<Vector3> pathOfSp = new ArrayList<>();
        CatmullRomCurve3D catmull = new CatmullRomCurve3D();
        for (int i = 0; i < NUM_POINTS; i++) {
            // -- generate a random point within certain limits
            Vector3 pos = null;
            if (i == 0) {
                pos = new Vector3(0, 0, 0);
            } else {
                pos = new Vector3((Math.random() - 0.5) * 2, (i + 1) * 1, 0);
            }
            catmull.addPoint(new Vector3(pos));
            pathOfSp.add(pos);
        }
        //        catmull.addPoint(new Vector3(-3,3,0));
        //        catmull.addPoint(new Vector3(-3,0,0));
        //        catmull.addPoint(new Vector3(0,0,0));
        //        catmull.addPoint(new Vector3(3,0,0));
        //        catmull.addPoint(new Vector3(2,-4,0));

        List<Vector3> mRoad = new ArrayList<>();
        mRoad.clear();
        for (int i = 0; i < NUM_POINTS * NUM_CATMULL_DESIRED_PT; i++) {//4;i++) {//
            Vector3 pos = new Vector3();
            catmull.calculatePoint(pos, (1.0 * i) / (1.0 * NUM_POINTS * NUM_CATMULL_DESIRED_PT));//4);//
            mRoad.add(pos);
        }

        Material material = new Material();
        for (int i = 0; i < mRoad.size(); i++) {
            Sphere sp = new Sphere(0.02f, 24, 24);
            sp.setColor(0x00ffff);
            sp.setMaterial(material);
            sp.setPosition(mRoad.get(i));
            getCurrentScene().addChild(sp);
        }

        Material material1 = new Material();
        for (int i = 0; i < pathOfSp.size(); i++) {
            Sphere sp = new Sphere(0.02f, 24, 24);
            sp.setColor(0xff0000);
            sp.setMaterial(material1);
            sp.setPosition(pathOfSp.get(i));
            getCurrentScene().addChild(sp);
        }

        List<Vector3> pathLeft = new ArrayList<>();
        List<Vector3> pathRight = new ArrayList<>();

        MathUtils.points2path(pathLeft, pathRight, mRoad, 0.5);

        /*for(int i=0;i<pathLeft.size();i++){
            pathLeft.get(i).x-=5;
        }
        for(int i=0;i<pathRight.size();i++){
            pathRight.get(i).x+=5;
        }*/
        //绘制的plane的个数比点的个数少一个
        planes = new PlanesGalore(mRoad, pathLeft, pathRight, mRoad.size() - 1);
        mMaterial = planes.getMaterial();
        mMaterial.setColorInfluence(0);
        //        mMaterial.enableLighting(true);
        //        mMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());

        try {
            ATexture texture = new Texture("route_new", R.mipmap.roadm);//.route_new);
//                        texture.setMipmap(false);
//                        texture.setFilterType(ATexture.FilterType.LINEAR);
            mMaterial.addTexture(texture);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
//        mMaterial.setColor(Color.GREEN);

        mMaterialPlugin = planes.getMaterialPlugin();

        planes.setDoubleSided(true);
        planes.setPosition(0, 0, 0);
        getCurrentScene().addChild(planes);

        Object3D empty = new Object3D();
        getCurrentScene().addChild(empty);


        CatmullRomCurve3D catmull2 = new CatmullRomCurve3D();
        for (int i = 0; i < mRoad.size(); i++) {
            catmull2.addPoint(new Vector3(mRoad.get(i).x, mRoad.get(i).y, 2));
        }
        anim = new SplineTranslateAnimation3D(catmull2);
        anim.setDurationMilliseconds(100000);
        anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
        anim.setTransformable3D(getCurrentCamera());
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        getCurrentScene().registerAnimation(anim);
        anim.play();

        //getCurrentCamera().setLookAt(new Vector3(0, 0, 5));

        //        drawCurve(mRoad, 0xff00ff, new Vector3());
        //        drawCurve(pathLeft, 0xff00ff, new Vector3());
        //        drawCurve(pathRight, 0xff00ff, new Vector3());

        //        Object3D empty = new Object3D();
        //        getCurrentScene().addChild(empty);

        //        CatmullRomCurve3D path = new CatmullRomCurve3D();
        //        path.addPoint(new Vector3(-4, 0, -20));
        //        path.addPoint(new Vector3(2, 1, -10));
        //        path.addPoint(new Vector3(-2, 0, 10));
        //        path.addPoint(new Vector3(0, -4, 20));
        //        path.addPoint(new Vector3(5, 10, 30));
        //        path.addPoint(new Vector3(-2, 5, 40));
        //        path.addPoint(new Vector3(3, -1, 60));
        //        path.addPoint(new Vector3(5, -1, 70));
        //
        //        final SplineTranslateAnimation3D anim = new SplineTranslateAnimation3D(path);
        //        anim.setDurationMilliseconds(20000);
        //        anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
        //        anim.setTransformable3D(getCurrentCamera());
        //        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        //        getCurrentScene().registerAnimation(anim);
        //        anim.play();

    }

    Stack<Vector3> mTemp = new Stack<>();
    Stack<Vector3> mLine = new Stack<>();
    private Line3D mLine3D = null;

    private void drawCurve(List<Vector3> roads, int color, Vector3 position) {
        Material lineMaterial = new Material();

        CatmullRomCurve3D catmull = new CatmullRomCurve3D();
        for (int i = 0; i < roads.size(); i++) {
            catmull.addPoint(new Vector3(roads.get(i)));
        }

        mTemp.clear();
        mTemp.addAll(roads);
        mLine.clear();
        for (int i = 0; i < roads.size()/**50*/; i++) {
            //Vector3 pos = new Vector3();
            //catmull.calculatePoint(pos,(1.0*i)/(1.0*roads.size()*50));
            Vector3 pos = roads.get(i);
            mLine.add(new Vector3(pos));
        }

        mLine3D = new Line3D(mLine, 2, color);
        mLine3D.setMaterial(lineMaterial);
        mLine3D.setPosition(position);
        getCurrentScene().addChild(mLine3D);


        /*Random random = new Random();
        Vector3 randomAxis = new Vector3(0, 0,random.nextFloat());
        randomAxis.normalize();
        RotateOnAxisAnimation anim = new RotateOnAxisAnimation(randomAxis, 360);
        anim.setTransformable3D(getCurrentCamera());
        anim.setDurationMilliseconds(30000);
        anim.setRepeatMode(Animation.RepeatMode.INFINITE);
        getCurrentScene().registerAnimation(anim);
        anim.play();*/
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        mStartTime = System.currentTimeMillis();
    }

    public void onDrawFrame(GL10 glUnused) {
        try {
            super.onDrawFrame(glUnused);
        }catch (Throwable t) {
            t.printStackTrace();;
        }
        //mMaterial.setTime((System.currentTimeMillis() - mStartTime) / 1000f);
        //mMaterialPlugin.setCameraPosition(getCurrentCamera().getPosition());

        if (mRunning) {
            //getCurrentCamera().setY(getCurrentCamera().getY() + .01f);
            Vector3 cameraPosition = getCurrentCamera().getPosition();
//            mMaterialPlugin.setCameraPosition(cameraPosition);

//            getCurrentCamera().setLookAt(cameraPosition.x,cameraPosition.y+5,0);

//            getCurrentCamera().setRotation(0, );
            //sphere.setPosition(cameraPosition.x,cameraPosition.y,0);
            //sphere2.setPosition(getCurrentCamera().getLookAt());
            //Log.e("helong_debug","rotation:"+planes.getRotation());
            //Log.e("helong_debug","quaternion:"+planes.getOrientation(new Quaternion()));
            //Log.e("helong_debug","position:"+planes.getPosition());
        }
    }

    private static boolean mRunning = true;

    public void pause() {
        mRunning = false;
        if(anim!=null && anim.isPlaying()){
            anim.pause();
        }
    }

    public void continue_() {
        mRunning = true;
        if(anim!=null && anim.isPaused()){
            anim.play();
        }
    }
}
