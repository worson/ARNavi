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
    public static final String  SPECIAL_LOG_TAG = "arway_special";
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

    public static final boolean IS_DEBUG_SCENE = true;

    //NAVING
    public static final boolean IS_AMAP_VIEW = false;//
    public static final int NAVI_CAR_START_DISTANCE = 50;

    public static final boolean NAVI_ENABLE_RESTRICT_DISTANCE  = false;
    public static final int     NAVI_MAX_RESTRICT_POINT_NUMBER = 5000;

    //测试岔路模拟数据
    public static final String BRANCH_LINES =
            "cross starts==========\n" +
                    "cross start==========\n" +
                    "-10.0,-4.4375\n" +
                    "-10.0525,-5.28\n" +
                    "-10.10,-6.27\n" +
                    "-10.158,-8.98\n" +
                    "-10.192,-15.523\n" +
                    "cross end==========\n" +
                    "cross ends==========\n" +
                    "\n" +
                    "cross starts==========\n" +
                    "cross start==========\n" +
                    "-16.0,-3.5625\n" +
                    "-17.00,-6.548\n" +
                    "-17.62,-8.202\n" +
                    "cross end==========\n" +
                    "cross start==========\n" +
                    "-16.0,-3.5625\n" +
                    "-16.23,-5.2412\n" +
                    "-16.863,-7.142\n" +
                    "-17.37,-8.58\n" +
                    "cross end==========\n" +
                    "cross ends==========\n" +
                    "\n" +
                    "cross starts==========\n" +
                    "cross start==========\n" +
                    "-14.625,0.0\n" +
                    "-13.375,3.63\n" +
                    "-12.252,6.8399\n" +
                    "cross end==========\n" +
                    /*"cross start==========\n" +
                    "-13.375,3.63\n" +
                    "-12.006,5.8499\n" +
                    "cross end==========\n" +
                    "cross start==========\n" +
                    "-15.25,0.375\n" +
                    "-12.6402,7.8129\n" +
                    "cross end==========\n" +
                    "cross start==========\n" +
                    "-15.4744,0.07309\n" +
                    "-15.7232,-0.5424\n" +
                    "-16.5005,-1.0882\n" +
                    "-17.714,-1.549\n" +
                    "cross end==========\n" +*/
                    "cross ends==========\n" +
                    "\n" +
                    "cross starts==========\n" +
                    "cross start==========\n" +
                    "-16.75,-3.5625\n" +
                    "-17.694,-4.1887\n" +
                    "-18.739,-6.48\n" +
                    "cross end==========\n" +
                    "cross ends==========\n" +
                    "\n" +
                    "cross starts==========\n" +
                    "cross start==========\n" +
                    "-29.75,-93.3125\n" +
                    "-29.978,-110.042\n" +
                    "cross end==========\n" +
                    "cross start==========\n" +
                    "-29.75,-93.3125\n" +
                    "-35.9765,-93.2174\n" +
                    "cross end==========\n" +
                    "cross ends==========\n" +
                    "\n" +
                    "cross starts==========\n" +
                    "cross start==========\n" +
                    "-0.75,-93.4375\n" +
                    "12.35168,-93.46\n" +
                    "cross end==========\n" +
                    "cross ends==========\n" +
                    "\n" +
                    "cross starts==========\n" +
                    "cross start==========\n" +
                    "2.125,-157.8125\n" +
                    "2.0322,-169.66\n" +
                    "cross end==========\n" +
                    "cross start==========\n" +
                    "2.125,-157.8125\n" +
                    "-1.357,-158.112\n" +
                    "cross end==========\n" +
                    "cross ends==========\n" +
                    "\n" +
                    "cross starts==========\n" +
                    "cross start==========\n" +
                    "16.625,-161.0625\n" +
                    "18.199,-144.076\n" +
                    "cross end==========\n" +
                    "cross ends==========\n" +
                    "\n" +
                    "cross starts==========\n" +
                    "cross start==========\n" +
                    "15.875,-181.0\n" +
                    "15.9805,-183.68\n" +
                    "16.292,-192.24\n" +
                    "16.452,-198.76\n" +
                    "cross end==========\n" +
                    "cross start==========\n" +
                    "15.9805,-183.68\n" +
                    "15.3905,-185.56\n" +
                    "12.2576,-187.28\n" +
                    "8.9231,-188.07\n" +
                    "cross end==========\n" +
                    "cross ends==========\n" +
                    "\n" +
                    "cross starts==========\n" +
                    "cross start==========\n" +
                    "19.75,-159.9375\n" +
                    "19.864,-154.32\n" +
                    "19.963,-150.055\n" +
                    "cross end==========\n" +
                    "cross start==========\n" +
                    "19.864,-154.32\n" +
                    "18.769,-154.32\n" +
                    "cross end==========\n" +
                    "cross ends==========\n" +
                    "\n" +
                    "cross starts==========\n" +
                    "cross start==========\n" +
                    "39.5,-158.3125\n" +
                    "39.5,-170.83\n" +
                    "cross end==========\n" +
                    "cross start==========\n" +
                    "39.225,-158.533\n" +
                    "39.3508,-159.605\n" +
                    "39.492,-161.45\n" +
                    "cross end==========\n" +
                    "cross ends==========\n" +
                    "\n" +
                    "cross starts==========\n" +
                    "cross start==========\n" +
                    "39.875,-147.875\n" +
                    "39.849,-138.64\n" +
                    "cross end==========\n" +
                    "cross ends==========";
    /*//测试岔路模拟数据
    public static final String BRANCH_LINES =
            "cross starts==========\n" +
            "cross start==========\n" +
            "-10.0,-4.4375\n" +
            "-10.0525,-5.28\n" +
            "-10.10,-6.27\n" +
            "-10.158,-8.98\n" +
            "-10.192,-15.523\n" +
            "cross end==========\n" +
            "cross ends==========\n" +
            "\n" +
            "cross starts==========\n" +
            "cross start==========\n" +
            "-16.0,-3.5625\n" +
            "-17.00,-6.548\n" +
            "-17.62,-8.202\n" +
            "cross end==========\n" +
            "cross start==========\n" +
            "-16.0,-3.5625\n" +
            "-16.23,-5.2412\n" +
            "-16.863,-7.142\n" +
            "-17.37,-8.58\n" +
            "cross end==========\n" +
            "cross ends==========\n" +
            "\n" +
            "cross starts==========\n" +
            "cross start==========\n" +
            "-14.625,0.0\n" +
            "-13.375,3.63\n" +
            "-12.252,6.8399\n" +
            "cross end==========\n" +
            "cross ends==========\n" +
            *//*"cross start==========\n" +
            "-13.375,3.63\n" +
            "-12.006,5.8499\n" +
            "cross end==========\n" +
            "cross start==========\n" +
            "-15.25,0.375\n" +
            "-12.6402,7.8129\n" +
            "cross end==========\n" +
            "cross start==========\n" +
            "-15.4744,0.07309\n" +
            "-15.7232,-0.5424\n" +
            "-16.5005,-1.0882\n" +
            "-17.714,-1.549\n" +
            "cross end==========\n" +*//*
            "\n" +
            "cross starts==========\n" +
            "cross start==========\n" +
            "-16.75,-3.5625\n" +
            "-17.694,-4.1887\n" +
            "-18.739,-6.48\n" +
            "cross end==========\n" +
            "cross ends==========\n" +
            "\n" +
            "cross starts==========\n" +
            "cross start==========\n" +
            "-29.75,-93.3125\n" +
            "-29.978,-110.042\n" +
            "cross end==========\n" +
            "cross start==========\n" +
            "-29.75,-93.3125\n" +
            "-35.9765,-93.2174\n" +
            "cross end==========\n" +
            "cross ends==========\n" +
            "\n" +
            "cross starts==========\n" +
            "cross start==========\n" +
            "-0.75,-93.4375\n" +
            "12.35168,-93.46\n" +
            "cross end==========\n" +
            "cross ends==========\n" +
            "\n" +
            "cross starts==========\n" +
            "cross start==========\n" +
            "2.125,-157.8125\n" +
            "2.0322,-169.66\n" +
            "cross end==========\n" +
            "cross start==========\n" +
            "2.125,-157.8125\n" +
            "-1.357,-158.112\n" +
            "cross end==========\n" +
            "cross ends==========\n" +
            "\n" +
            "cross starts==========\n" +
            "cross start==========\n" +
            "16.625,-161.0625\n" +
            "18.199,-144.076\n" +
            "cross end==========\n" +
            "cross ends==========\n" +
            "\n" +
            "cross starts==========\n" +
            "cross start==========\n" +
            "15.875,-181.0\n" +
            "15.9805,-183.68\n" +
            "16.292,-192.24\n" +
            "16.452,-198.76\n" +
            "cross end==========\n" +
            "cross ends==========\n" +
            *//*"cross start==========\n" +
            "15.9805,-183.68\n" +
            "15.3905,-185.56\n" +
            "12.2576,-187.28\n" +
            "8.9231,-188.07\n" +
            "cross end==========\n" +*//*
            "\n" +
            "cross starts==========\n" +
            "cross start==========\n" +
            "19.75,-159.9375\n" +
            "19.864,-154.32\n" +
            "19.963,-150.055\n" +
            "cross end==========\n" +
            "cross ends==========\n" +
            *//*"cross start==========\n" +
            "19.864,-154.32\n" +
            "18.769,-154.32\n" +
            "cross end==========\n" +*//*
            "\n" +
            "cross starts==========\n" +
            "cross start==========\n" +
            "39.5,-158.3125\n" +
            "39.5,-170.83\n" +
            "cross end==========\n" +
            "cross ends==========\n" +
            *//*"cross start==========\n" +
            "39.225,-158.533\n" +
            "39.3508,-159.605\n" +
            "39.492,-161.45\n" +
            "cross end==========\n" +*//*
            "\n" +
            "cross starts==========\n" +
            "cross start==========\n" +
            "39.875,-147.875\n" +
            "39.849,-138.64\n" +
            "cross end==========\n" +
            "cross ends==========";*/
}
