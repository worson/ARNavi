package demo.opengl.haloai.com.rajawalidemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import java.text.SimpleDateFormat;

import javax.microedition.khronos.opengles.GL10;

import rajawali.animation.Animation;
import rajawali.animation.IAnimationListener;
import rajawali.lights.DirectionalLight;
import rajawali.materials.Material;
import rajawali.materials.methods.DiffuseMethod;
import rajawali.materials.textures.AlphaMapTexture;
import rajawali.math.Quaternion;
import rajawali.math.vector.Vector3;
import rajawali.primitives.Cube;
import rajawali.renderer.RajawaliRenderer;

/**
 * author       : 龙;
 * date         : 2016/6/22;
 * email        : helong@haloai.com;
 * package_name : demo.opengl.haloai.com.rajawalidemo;
 * project_name : RajawaliDemo;
 */
public class DemoRenderer5 extends RajawaliRenderer implements IAnimationListener {

    private Context         mContext = null;
    private AlphaMapTexture mTexture = null;
    private Canvas mTimeCanvas = null;
    private SimpleDateFormat mDateFormat = null;
    private Paint mTextPaint = null;

    public DemoRenderer5(Context context) {
        super(context);

        mContext = context;

        setFrameRate(50);
    }

    private Cube cube = new Cube(0.4f);

    private Bitmap mBm = null;

    public void initScene() {
        getCurrentCamera().setZ(2);

        DirectionalLight light = new DirectionalLight(0, .2f, -1);
        light.setPower(3);
        getCurrentScene().addLight(light);

        Material material = new Material();
        material.enableLighting(true);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());
//
//        mBm = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
//        mTexture = new AlphaMapTexture("timeTexture", mBm);
//        try {
//            material.addTexture(mTexture);
//        } catch (ATexture.TextureException e) {
//            e.printStackTrace();
//        }
//        material.setColorInfluence(1);

//        Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
//        mBm = Bitmap.createBitmap(bm.getWidth(),bm.getHeight(),bm.getConfig());
//        Canvas canvas = new Canvas(mBm);
//        canvas.drawColor(Color.BLACK);
//        Paint paint = new Paint();
//        paint.setTextSize(20);
//        canvas.drawText("Test Helong IMBA", 0, 0, paint);
//
//        try {
//            material.addTexture(new Texture("test",mBm));
//        } catch (ATexture.TextureException e) {
//            e.printStackTrace();
//        }

        cube.setColor(0xff00ff);
        cube.setMaterial(material);
        cube.setPosition(new Vector3());
        getCurrentScene().addChild(cube);

        getCurrentCamera().setLookAt(0,0,0);

        /*RotateAnimation3D rotateAnimation3D = new RotateAnimation3D(1000,1000,1000);
        rotateAnimation3D.setDurationMilliseconds(1000);
        rotateAnimation3D.setRepeatMode(Animation.RepeatMode.REVERSE);
        rotateAnimation3D.setTransformable3D(cube);
        rotateAnimation3D.play();*/

        /*Random random = new Random();
        Vector3 randomAxis = new Vector3(0, 0,random.nextFloat());
        randomAxis.normalize();
        RotateOnAxisAnimation anim = new RotateOnAxisAnimation(randomAxis, 360);
        anim.setTransformable3D(getCurrentCamera());
        anim.setDurationMilliseconds(30000);
        anim.setRepeatMode(Animation.RepeatMode.INFINITE);
        anim.registerListener(this);
        getCurrentScene().registerAnimation(anim);
        anim.play();*/
    }

    int count = 0;
    @Override
    public void onDrawFrame(GL10 glUnused) {
//        if(count++==20){
//            count=0;
//            if (mTimeCanvas == null) {
//
//                mTimeCanvas = new Canvas(mBm);
//                mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//                mTextPaint.setColor(Color.WHITE);
//                mTextPaint.setTextSize(35);
//                mDateFormat = new SimpleDateFormat("HH:mm:ss",
//                                                   Locale.ENGLISH);
//            }
//            mTimeCanvas.drawColor(0xff0000, PorterDuff.Mode.CLEAR);
//            mTimeCanvas.drawText(mDateFormat.format(new Date()), 75,
//                                 128, mTextPaint);
//
//            mTexture.setBitmap(mBm);
//            mTextureManager.replaceTexture(mTexture);
//        }

        super.onDrawFrame(glUnused);

        Vector3 lookAt = getCurrentCamera().getLookAt();
        getCurrentCamera().setLookAt(lookAt.x,lookAt.y,lookAt.z+0.01f);
    }

    @Override
    public void onAnimationEnd(Animation animation) {

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationUpdate(Animation animation, double v) {
        Log.e("helong_debug","rotation:"+cube.getRotation());
        Log.e("helong_debug","position:"+cube.getPosition());
        Quaternion qt = new Quaternion();
        Log.e("helong_debug", "orientation:"+cube.getOrientation(qt));
        Log.e("helong_debug", "orientation:"+qt);
        Log.e("helong_debug", "=========================================");
    }

    public void pause() {}

    public void continue_() {}
}