package com.haloai.hud.hudendpoint.arwaylib.utils;

/**
 * Created by wangshengxing on 16/7/12.
 */
public class ARWayConst {

    public static boolean IS_DARW_ARWAY = false; //是否偏航时清空显示数据
    //disiplay
    public static boolean IS_YAW_CLEAR_DISPLAY = false; //是否偏航时清空显示数据

    // debug log
    public static String  INDICATE_LOG_TAG = "sen_debug_gl";
    public static String  ERROR_LOG_TAG = "sen_debug_gl";
    public static String  SPECIAL_LOG_TAG = "matlab";
    public static boolean ENABLE_LOG_OUT = true;//ARWAY 输出log
    public static boolean ENABLE_FAST_LOG = false;//ARWAY 输出log
    public static boolean ENABLE_SPECIAL_LOG = true;//ARWAY 输出紧要log
    public static boolean ENABLE_TEST_LOG = true;//ARWAY 输出测试log


    //open gl
    public static final double DEFAULT_CAMERA_Z = 15;
    public static final boolean IS_DEBUG_SCENE = false;

    //NAVING
    public static final int NAVI_CAR_START_DISTANCE = 50;

    public static final boolean NAVI_ENABLE_RESTRICT_DISTANCE  = false;
    public static final int     NAVI_MAX_RESTRICT_POINT_NUMBER = 5000;
}
