package com.haloai.hud.hudendpoint.arwaylib.utils;

import com.haloai.hud.utils.HaloLogger;

import java.util.List;

/**
 * author       : 龙;
 * date         : 2016/10/31;
 * email        : helong@haloai.com;
 * package_name : com.haloai.hud.hudendpoint.arwaylib.utils;
 * project_name : hudlauncher;
 */
public class PrintUtils {
    public static void printList(List list,String tag,String start_endText){
        HaloLogger.logE(tag,"============"+start_endText+" start"+"============");
        for(Object o:list){
            HaloLogger.logE(tag,""+o);
        }
        HaloLogger.logE(tag,"============"+start_endText+" end"+"============");
    }
}
