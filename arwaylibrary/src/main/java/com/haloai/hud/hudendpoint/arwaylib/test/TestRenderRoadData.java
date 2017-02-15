package com.haloai.hud.hudendpoint.arwaylib.test;

import com.haloai.hud.hudendpoint.arwaylib.utils.MathUtils;

import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * author       : wangshengxing;
 * date         : 13/02/2017;
 * email        : wangshengxing@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.test;
 * project_name : TestARWay;
 */
public class TestRenderRoadData {

    public static void main(String[] args) {

        String tag = "testMovePath";
        String tagtest = "tagtest";
        String filter = " python ";

        List<Vector3> road= new ArrayList<>();

        road.add(new Vector3(22.526880448727823,113.92929956316948,0));
        road.add(new Vector3(22.526840807823206,113.92949938774109,0));
        road.add(new Vector3(22.526840807823206,113.92949938774109,0));
        road.add(new Vector3(22.526830897595275,113.92956912517548,0));

        print(tag, filter + "origin path start \n");
        for (Vector3 p : road) {
            print(tag, String.format("%s %s , %s ,%s \n", filter, p.x, p.y, p.z));
        }
        print(tag, filter + "origin path end \n");

        Vector3 offset = new Vector3();
        TestRoadRenderOption mOptions = new TestRoadRenderOption();

        Vector3 fogEng = Vector3.subtractAndCreate(road.get(road.size()-1),offset);
        float distStep = mOptions.fogDistance;
        float totalDist = 0;
        boolean found = false;
        Vector3 c1 = road.get(road.size()-1);
        float rate = (mOptions.fogRate>=0 && mOptions.fogRate<1)?mOptions.fogRate:0;
        Vector3 v = new Vector3(road.get((int)(road.size()*rate)));
        for (int j = road.size(); j >0; j--) {
            Vector3 c2 = road.get(j - 1);
            double temp = MathUtils.calculateDistance(c1.x, c1.y, c2.x, c2.y);
            if (temp >= distStep) {
                double scale = distStep / temp;
                v.x = c1.x + (c2.x - c1.x) * scale;
                v.y = c1.y + (c2.y - c1.y) * scale;
                v.z = 0;
                totalDist+= distStep;
                found = true;
                break;
            } else if (temp < distStep) {
                distStep -= temp;
                c1 = road.get(j-1);
                totalDist+=temp;
                print(tag,String.format("totalDist %s \n",totalDist));
            }
        }
        /**
         * 找不到起点：
         * 整个path太短
         *
         */
        if (!found){
            v = new Vector3(road.get(0));
        }
        Vector3 fogStart = Vector3.subtractAndCreate(v,offset);

        List<Vector3> result = new ArrayList<>();
        result.add(fogStart);
        result.add(fogEng);

        print(tag, filter + "fog path start \n");
        for (Vector3 p : result) {
            print(tag, String.format("%s %s , %s ,%s \n", filter, p.x, p.y, p.z));
        }
        print(tag, filter + "fog path end \n");

    }

    private static void print(String tag,String msg){
        System.out.print(tag+" : "+msg);
    }
}
