package com.haloai.hud.hudendpoint.arwaylib.utils;

import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.List;

import static com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils.getLineIntersection;

/**
 * author       : wangshengxing;
 * date         : 09/02/2017;
 * email        : wangshengxing@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.utils;
 * project_name : TestARWay;
 */
public class PointLineUtils {
    public static void main(String[] args) {
        String tag = "testMovePath";
        String tagtest = "tagtest";
        String filter = " python ";

        List<Vector3> path1 = new ArrayList<>();
        List<Vector3> path2 = new ArrayList<>();
        List<Vector3> path3 = new ArrayList<>();

        path1.add(new Vector3(1, 0, 0));
        path1.add(new Vector3(1, 1, 0));
        path1.add(new Vector3(1.2, 2, 0));
        path1.add(new Vector3(1.5, 2.2, 0));
        path1.add(new Vector3(2.5, 2.0, 0));
        path1.add(new Vector3(3, 2.5, 0));
        path1.add(new Vector3(3.3, 3, 0));
//        path1.add(new Vector3(3.5, 4, 0));
        path1.add(new Vector3(3.7, 5, 0));
        path1.add(new Vector3(4, 5, 0));

        print(tag, filter + "origin path start \n");
        for (Vector3 p : path1) {
            print(tag, String.format("%s %s , %s ,%s \n", filter, p.x, p.y, p.z));
        }
        print(tag, filter + "origin path end \n");


        List<Vector3> path = path1;
        int cnt = path.size();
        Vector3 r0 = new Vector3();
        Vector3 r1 = new Vector3();
        Vector3 r2 = new Vector3();
        Vector3 r3 = new Vector3();
        Vector3 v = new Vector3();

        double dist = 0.2;
        int inter = 0;
        for (int i = 0; i < cnt; i++) {
            if (i== (cnt-1)){
                Vector3 p0 = path.get(i-1);
                Vector3 p1 = path.get(i);

                MathUtils.translateLine(p0.x,p0.y,p1.x,p1.y,r0,r1,dist);
                v = r1;
                inter = 1;
            } else {
                if(i==0){
                    Vector3 p0 = path.get(i);
                    Vector3 p1 = path.get(i+1);
                    MathUtils.translateLine(p0.x,p0.y,p1.x,p1.y,r0,r1,dist);
                    v = r0;
                    inter = 1;
                }else {
                    Vector3 p0 = path.get(i-1);
                    Vector3 p1 = path.get(i);
                    Vector3 p2 = path.get(i+1);
                    MathUtils.translateLine(p0.x,p0.y,p1.x,p1.y,r0,r1,dist);
                    MathUtils.translateLine(p1.x,p1.y,p2.x,p2.y,r2,r3,dist);
                    v=new Vector3();
                    inter = getLineIntersection(r0,r1,r2,r3,v);
                }
            }
            if (inter != 0) {
            }else {
//                HaloLogger.postE(ARWayConst.ERROR_LOG_TAG,"translateLine no intersection ");
            }
        }



        MathUtils.translatePath(path1, path2, dist);
        MathUtils.translatePath(path1, path3, -dist);
        print(tag, filter + "translate path start \n");
        for (Vector3 p : path2) {
            print(tag, String.format("%s %s , %s ,%s \n", filter, p.x, p.y, p.z));
        }
        print(tag, filter + "translate path end \n");

        print(tag, filter + "translate1 path start \n");
        for (Vector3 p : path3) {
            print(tag, String.format("%s %s , %s ,%s \n", filter, p.x, p.y, p.z));
        }
        print(tag, filter + "translate1 path end \n");



    }

    private static void print(String tag,String msg){
        System.out.print(tag+" : "+msg);
    }
}
