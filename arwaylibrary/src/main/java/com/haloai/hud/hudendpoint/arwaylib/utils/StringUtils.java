package com.haloai.hud.hudendpoint.arwaylib.utils;

import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * 处理将字符串转换成其它类型
 * author       : wangshengxing;
 * date         : 16/02/2017;
 * email        : wangshengxing@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.utils;
 * project_name : TestARWay;
 */
public class StringUtils {

    /**
     * 转换vect2数据类型
     * @param vect2Str
     * @return
     */
    public  static List<Vector3> parseVect3s(String vect2Str){
        List<Vector3> vector3List = new ArrayList<>();
        String[] lines = vect2Str.split("\n");
        for (String line : lines) {
            String[] split_line = line.split(",");
            vector3List.add(new Vector3(Double.valueOf(split_line[0]),Double.valueOf(split_line[1]),0));
        }
        return vector3List;
    }
}
