package com.haloai.hud.hudendpoint.arwaylib.utils;

/**
 * Created by wangshengxing on 16/7/12.
 */
public class ARWayConst {

    public static boolean IS_DARW_ARWAY = true; //是否偏航时清空显示数据
    //disiplay
    public static final boolean IS_YAW_CLEAR_DISPLAY = false; //是否偏航时清空显示数据
    public static final boolean IS_NEW_ROADOBJECT = true; //是否偏航时清空显示数据

    //data
    public static final boolean IS_FILTER_PATH_LITTLE_DISTANCE = false; //
    public static final boolean IS_CAT_MULL_ROM = false; //

    // debug log
    public static final String  INDICATE_LOG_TAG = "sen_debug_gl";
    public static final String  ERROR_LOG_TAG = "sen_debug_gl";
    public static final String  SPECIAL_LOG_TAG = "matlab";
    public static final boolean ENABLE_LOG_OUT = true;//ARWAY 输出log
    public static final boolean ENABLE_FAST_LOG = false;//ARWAY 输出log
    public static final boolean ENABLE_SPECIAL_LOG = true;//ARWAY 输出紧要log
    public static final boolean ENABLE_TEST_LOG = true;//ARWAY 输出测试log
    public static final boolean ENABLE_PERFORM_TEST = true;//ARWAY 输出测试log


    //open gl
    public static final int     FRAME_RATE             = 30;
    public static final double DEFAULT_CAMERA_Z           = 15;
    public static final float  REAL_TO_AMAP_GL_RATE       = 9027.669311696285f;//61.720116/0.0068367719141013686
    public static final float  AMAP_TO_ARWAY_GL_RATE      = 1170.1428832954605F;
    public static final float  ROAD_WIDTH                 = 0.4f;
    public static final float  REFERENCE_LINE_STEP_LENGTH = ROAD_WIDTH*10;

    public static final boolean IS_DEBUG_SCENE = false;

    //NAVING
    public static final boolean IS_AMAP_VIEW = true;//
    public static final int NAVI_CAR_START_DISTANCE = 50;

    public static final boolean NAVI_ENABLE_RESTRICT_DISTANCE  = false;
    public static final int     NAVI_MAX_RESTRICT_POINT_NUMBER = 5000;

}
