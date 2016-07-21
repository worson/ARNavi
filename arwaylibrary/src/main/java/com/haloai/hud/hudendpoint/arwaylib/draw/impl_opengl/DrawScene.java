package com.haloai.hud.hudendpoint.arwaylib.draw.impl_opengl;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.haloai.hud.hudendpoint.arwaylib.R;
import com.haloai.hud.hudendpoint.arwaylib.bean.BeanFactory;
import com.haloai.hud.hudendpoint.arwaylib.bean.impl_opengl.OpenglRouteBean;
import com.haloai.hud.hudendpoint.arwaylib.calculator.CalculatorFactory;
import com.haloai.hud.hudendpoint.arwaylib.calculator.factor.SceneFactor;
import com.haloai.hud.hudendpoint.arwaylib.calculator.impl.SceneCalculator;
import com.haloai.hud.hudendpoint.arwaylib.calculator.result.SceneResult;
import com.haloai.hud.hudendpoint.arwaylib.draw.DrawObject;
import com.haloai.hud.hudendpoint.arwaylib.draw.IOpenglFrame;
import com.haloai.hud.hudendpoint.arwaylib.draw.IViewOperation;
import com.haloai.hud.hudendpoint.arwaylib.framedata.impl_opengl.SceneFrameData;
import com.haloai.hud.hudendpoint.arwaylib.rajawali.PlanesGalore;
import com.haloai.hud.hudendpoint.arwaylib.rajawali.PlanesGaloreMaterialPlugin;
import com.haloai.hud.hudendpoint.arwaylib.utils.ARWayConst;
import com.haloai.hud.utils.HaloLogger;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.scene.Scene;
import org.rajawali3d.view.TextureView;

import java.util.List;
import java.util.Stack;

/**
 * Created by wangshengxing on 16/7/10.
 */
public class DrawScene extends DrawObject implements IOpenglFrame ,IViewOperation{

    // draw contorl
    private boolean isDrawPlan = false;
    private boolean isPlanesGaloreDraw = true;
    private boolean isDrawPathLine = false;


    private Material mMaterial = null;
    private PlanesGaloreMaterialPlugin mMaterialPlugin = null;
    //constantly data 实时数据
    private long   mStartTime   = 0l;

    //3d object
    private Sphere mSphere = null;

    //
    private        SceneCalculator mSceneCalculator = (SceneCalculator) CalculatorFactory.getCalculator(CalculatorFactory.CalculatorType.GL_SCENE);
    private static DrawScene       mDrawScene       = new DrawScene();

    public DrawScene() {
    }

    public static DrawScene getInstance() {
        return mDrawScene;
    }

    private TextureView mTextureView;

    @Override
    public View getViewInstance(Context context) {
        if (mTextureView == null) {
            mTextureView = new TextureView(context);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mTextureView.setLayoutParams(params);
        }
        return mTextureView;
    }

    @Override
    public void doDraw(Context context, Renderer renderer) {
        SceneFrameData sceneFrameData = SceneFrameData.getInstance();
        SceneFactor sceneFactor = SceneFactor.getInstance();
        OpenglRouteBean openglRouteBean = (OpenglRouteBean)BeanFactory.getBean(BeanFactory.BeanType.GL_ROUTE);
        sceneFactor.init(renderer,openglRouteBean.getPathPoints(),openglRouteBean.getAllLength());
        SceneResult sceneResult = mSceneCalculator.calculate(sceneFactor);
        sceneFrameData.update(sceneResult);
        drawScene(sceneResult);
    }

    @Override
    public void onOpenglFrame(Renderer renderer) {
        Scene scene = renderer.getCurrentScene();
        Camera camera =renderer.getCurrentCamera();
        if ( scene == null||camera == null) {
            return;
        }
        if (mMaterialPlugin != null) {
            mMaterialPlugin.setCameraPosition(renderer.getCurrentCamera().getPosition());
        }
        if (mMaterial != null) {
            mMaterial.setTime((System.currentTimeMillis() - mStartTime) / 1000f);
        }
    }

    private void drawScene(SceneResult result) {

        Scene scene = result.mRenderer.getCurrentScene();
        Camera camera = result.mRenderer.getCurrentCamera();
        if (result == null|| scene == null||camera == null) {
            HaloLogger.logE("sen_debug_gl","CurrentScene is null");
            return;
        }

        List<Vector3> path = result.mCalculatePath;
        HaloLogger.logE("sen_debug_gl","onDrawScene called");
        HaloLogger.logE("helong_debug","start");

        // TODO: 2016/7/1 for camera
        mSphere = new Sphere(0.1f, 24, 24);

        camera.setPosition(path.get(0).x, path.get(0).y, ARWayConst.DEFAULT_CAMERA_Z);

        Material material = new Material();
        mSphere.setColor(0xff0000);
        mSphere.setMaterial(material);
        mSphere.setPosition(new Vector3(camera.getPosition().x, camera.getPosition().y, 0));
        scene.addChild(mSphere);

        if(isDrawPathLine){
            Stack<Vector3> line  = new Stack<>();
            for (Vector3 pos:result.mOriginalPath){
                line.add(new Vector3(pos));
            }
            Material lineMaterial = new Material();
            Line3D line3D = new Line3D(line,30, Color.RED);
            line3D.setMaterial(lineMaterial);
            line3D.setPosition(new Vector3(0,0,0));
            scene.addChild(line3D);
        }
        if(isPlanesGaloreDraw){
            final PlanesGalore planes = new PlanesGalore(path, result.mLeftPath, result.mRightPath, result.mCalculatePath.size() - 1);
            HaloLogger.logE("helong_debug","left_path:"+result.mLeftPath);
            HaloLogger.logE("helong_debug","right_path:"+result.mRightPath);
            mMaterial = planes.getMaterial();
            mMaterial.setColorInfluence(0);
            try {
                mMaterial.addTexture(new Texture("route_new", R.drawable.route_new));
            } catch (ATexture.TextureException e) {
                e.printStackTrace();
            }

            mMaterialPlugin = planes.getMaterialPlugin();

            planes.setDoubleSided(true);
            planes.setPosition(0, 0, 0);
            scene.addChild(planes);
        }else if(isDrawPlan) {
            Material ma = new Material();
            for (int i = 0; i < path.size() - 1; i++) {
                Vector3 v1 = path.get(i);
                Vector3 v2 = path.get(i + 1);
                Plane plane = new Plane(v2.x - v1.x > 1 ? (float) (v2.x - v1.x) : 1, v2.y - v1.y > 1 ? (float) (v2.y - v1.y) : 1, 24, 24);
                plane.setPosition((v1.x + v2.x) / 2, (v1.y + v2.y) / 2, 0);
                plane.setMaterial(ma);
                plane.setDoubleSided(true);
                plane.setColor(0xff3333ff);
//            HaloLogger.logE("helong_debug_","degrees:"+MathUtils.getDegrees(v1.x,v1.y,v2.x,v2.y));
//            plane.setRotZ(MathUtils.getDegrees(v1.x, v1.y, v2.x, v2.y));
                scene.addChild(plane);
            }
        }
    }

    @Override
    public void setView(Context context, View view) {
        if (view != null) {
//            mTextureView =(TextureView) view.findViewById(R.id.rajwali_surface);
        }

    }
}
