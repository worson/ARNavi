package demo.opengl.haloai.com.rajawalidemo;

import android.content.Context;

import java.util.Stack;

import rajawali.curves.CatmullRomCurve3D;
import rajawali.curves.ICurve3D;
import rajawali.materials.Material;
import rajawali.math.vector.Vector3;
import rajawali.primitives.Line3D;
import rajawali.renderer.RajawaliRenderer;

/**
 * author       : 龙;
 * date         : 2016/6/22;
 * email        : helong@haloai.com;
 * package_name : demo.opengl.haloai.com.rajawalidemo;
 * project_name : RajawaliDemo;
 */
public class DemoRenderer3 extends RajawaliRenderer {
    private final int NUM_POINTS = 100;

    public DemoRenderer3(Context context) {
        super(context);

        setFrameRate(50);
    }

    public void initScene() {

        getCurrentCamera().setZ(7);

        /*//
        // -- Quadratic Bezier Curve
        //

        ICurve3D curve = new CubicBezierCurve3D(new Vector3(-1, 0, 0),
                                                new Vector3(-1, 1.3f, 0), new Vector3(1, -1.9f, 0),
                                                new Vector3(1, 0, 0));

        drawCurve(curve, 0xffffff, new Vector3(0, 2, 0));

        //
        // -- Linear Bezier Curve
        //

        curve = new LinearBezierCurve3D(new Vector3(-1, 0, 0), new Vector3(
                1, 0, 0));

        drawCurve(curve, 0xffff00, new Vector3(0, 1f, 0));

        //
        // -- Quadratic Bezier Curve
        //

        curve = new QuadraticBezierCurve3D(new Vector3(-1, 0, 0),
                                           new Vector3(.3f, 1, 0), new Vector3(1, 0, 0));

        drawCurve(curve, 0x00ff00, new Vector3(0, 0, 0));*/

        //
        // -- Catmull Rom Curve
        //

        CatmullRomCurve3D catmull = new CatmullRomCurve3D();
        catmull.addPoint(new Vector3(-1.5f, 0, 0)); // control point 1
        catmull.addPoint(new Vector3(-1, 0, 0)); // start point
        catmull.addPoint(new Vector3(-.5f, .3f, 0));
        catmull.addPoint(new Vector3(-.2f, -.2f, 0));
        catmull.addPoint(new Vector3(.1f, .5f, 0));
        catmull.addPoint(new Vector3(.5f, -.3f, 0));
        catmull.addPoint(new Vector3(1, 0, 0)); // end point
        catmull.addPoint(new Vector3(1.5f, -1, 0)); // control point 2

        drawCurve(catmull, 0xff0000, new Vector3(0, -1, 0));

        /*//
        // -- Compound path
        //

        CompoundCurve3D compound = new CompoundCurve3D();
        compound.addCurve(new CubicBezierCurve3D(new Vector3(-1, 0, 0),
                                                 new Vector3(-1, 1.3f, 0), new Vector3(-.5f, -1.9f, 0),
                                                 new Vector3(-.5f, 0, 0)));
        compound.addCurve(new LinearBezierCurve3D(new Vector3(-.5f, 0, 0),
                                                  new Vector3(0, 0, 0)));
        compound.addCurve(new QuadraticBezierCurve3D(new Vector3(0, 0, 0),
                                                     new Vector3(.3f, 1, 0), new Vector3(.5f, 0, 0)));

        catmull = new CatmullRomCurve3D();
        catmull.addPoint(new Vector3(0, 1, 0)); // control point 1
        catmull.addPoint(new Vector3(.5f, 0, 0)); // start point
        catmull.addPoint(new Vector3(.7f, .3f, 0));
        catmull.addPoint(new Vector3(.75f, -.2f, 0));
        catmull.addPoint(new Vector3(.9f, .5f, 0));
        catmull.addPoint(new Vector3(1, 0, 0)); // end point
        catmull.addPoint(new Vector3(1.5f, -1, 0)); // control point 2

        compound.addCurve(catmull);

        drawCurve(compound, 0xff3333, new Vector3(0, -2, 0));*/

    }

    private void drawCurve(ICurve3D curve, int color, Vector3 position) {
        Material lineMaterial = new Material();

        //通过增加NUM_POINTS的值来增加平滑程度
        //只要NUM_POINTS大于点的个数,就能起到平滑效果,只是多平滑而已
        Stack<Vector3> points = new Stack<>();
        for (int i = 0; i <= NUM_POINTS; i++) {
            Vector3 point = new Vector3();
            curve.calculatePoint(point, (float) i / (float) NUM_POINTS);
            points.add(point);
        }

        Line3D line = new Line3D(points, 1, color);
        line.setMaterial(lineMaterial);
        line.setPosition(position);
        getCurrentScene().addChild(line);
    }
}