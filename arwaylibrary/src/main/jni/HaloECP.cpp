#include <jni.h>
#include "com_haloai_hud_hudendpoint_arwaylib_utils_EnlargedCrossProcess.h"
#include <android/log.h>

#include <opencv2/core/cvstd.hpp>
#include <opencv2/core/utility.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/core/mat.hpp>

#include <iostream>
#include <dirent.h>
#include <string.h>

#include "algorithm.h"

using namespace cv;
using namespace std;

//
// Created by HarryMoo on 15/6/2016.
//

#define LOG_TAG "HaloAI_ECP_Lib"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

int searchHopPoint(cv::Mat image, int * x, int * y);

JNIEXPORT jobjectArray JNICALL Java_com_haloai_hud_hudendpoint_arwaylib_utils_EnlargedCrossProcess_nativeGetBranchRoads
        (JNIEnv * env, jobject javaSelf, jlong ecImage, jint centerPointIndex, jobject mainRoadTailEnd)
{
    LOGD("nativeGetBranchRoad enter!!!");

    Mat matRoadImg = *(Mat*)ecImage;
    vector<vector<Point2i> > vecPointSet;
    vector<Point2i> vecMainRoad;
    int res = GetCrossRoadPoint(matRoadImg, vecPointSet, centerPointIndex, vecMainRoad);
    LOGD("vecPointSet.size=%d", (int)vecPointSet.size());
    jobjectArray retBranchRoads = NULL;
    if (!res && vecMainRoad.size() == 2) {
        //Set the main road tail end.
        jclass clazz = env->GetObjectClass(mainRoadTailEnd);
        jfieldID xField = env->GetFieldID(clazz, "x", "I");
        jfieldID yField = env->GetFieldID(clazz, "y", "I");
        env->SetIntField(mainRoadTailEnd, xField, vecMainRoad[1].x);
        env->SetIntField(mainRoadTailEnd, yField, vecMainRoad[1].y);

        //Construct the string of all branch path points
        retBranchRoads = env->NewObjectArray(vecPointSet.size(), env->FindClass("java/lang/String"), 0);
        //用String传递一条路径值，"pt1.x,pt1.y,pt2.x,pt2.y,...,ptN.x,ptN.y"
        std::stringstream branchRoadPrintedValues;
        for (int i = 0; i < vecPointSet.size(); i++) {
            vector<Point2i> branchRoad = vecPointSet.at(i);
            LOGD("branch-%d, the size is %d", i, (int)branchRoad.size());
            for (int j=0; j<branchRoad.size(); j++) {
                if (j != 0) {
                    branchRoadPrintedValues << ",";
                }
                branchRoadPrintedValues << branchRoad[j].x << "," << branchRoad[j].y;
//                branchRoadPrintedValues += ",";
//                branchRoadPrintedValues += branchRoad[j].y;
            }
            LOGD("branchString is %s", branchRoadPrintedValues.str().c_str());
            jstring jstr = env->NewStringUTF(branchRoadPrintedValues.str().c_str());
            env->SetObjectArrayElement(retBranchRoads, i, jstr);
        }
    }

    LOGD("nativeGetBranchRoad leave!!!");
    return retBranchRoads;
}

JNIEXPORT jint JNICALL Java_com_haloai_hud_hudendpoint_arwaylib_utils_EnlargedCrossProcess_nativeGetHopPointInCrossImage
  (JNIEnv * env, jobject javaThis, jlong ecImage, jobject returnObj)
{
    LOGD("nativeGetFirstTurnPointInCrossImage enter!!");

    // Get the class of the input object
    jclass clazz = env->GetObjectClass(returnObj);
    // Get Field references
    jfieldID xField = env->GetFieldID(clazz, "x", "I");
    jfieldID yField = env->GetFieldID(clazz, "y", "I");

//    jbyte* ecImageBytes = env->GetByteArrayElements(aMapECImageBytes, JNI_FALSE);
//    Mat image = Mat(400, 400, CV_8UC3, ecImageBytes);
//    if (image.empty())
//        return -1;
    Mat image = *(Mat*)ecImage;

    int x = 0, y = 0;
    if (!searchHopPoint(image, &x, &y)) {
        // Set fields for object
        env->SetIntField(returnObj, xField, x);
        env->SetIntField(returnObj, yField, y);
    } else {
        LOGD("nativeGetFirstTurnPointInCrossImage leave!!! no found.");
        return -1;
    }

    LOGD("nativeGetFirstTurnPointInCrossImage leave!!!");
    return 0;
}

/**
 * @brief 对输入图像进行细化
 * @param src为输入图像,用cvThreshold函数处理过的8位灰度图像格式，元素中只有0与1,1代表有元素，0代表为空白
 * @param maxIterations限制迭代次数，如果不进行限制，默认为-1，代表不限制迭代次数，直到获得最终结果
 * @return 为对src细化后的输出图像,格式与src格式相同，元素中只有0与1,1代表有元素，0代表为空白
 */
cv::Mat thinImage(const cv::Mat & src, const int maxIterations)
{
    LOGD("thinImage enter");
    assert(src.type() == CV_8UC1);
    cv::Mat dst;
    int width  = src.cols;
    int height = src.rows;
    src.copyTo(dst);
    int count = 0;  //记录迭代次数
    while (true)
    {
        count++;
        if (maxIterations != -1 && count > maxIterations) //限制次数并且迭代次数到达
            break;
        std::vector<uchar *> mFlag; //用于标记需要删除的点
        //对点标记
        for (int i = 0; i < height ;++i)
        {
            uchar * p = dst.ptr<uchar>(i);
            for (int j = 0; j < width; ++j)
            {
                //如果满足四个条件，进行标记
                //  p9 p2 p3
                //  p8 p1 p4
                //  p7 p6 p5
                uchar p1 = p[j];
                if (p1 != 1) continue;
                uchar p4 = (j == width - 1) ? 0 : *(p + j + 1);
                uchar p8 = (j == 0) ? 0 : *(p + j - 1);
                uchar p2 = (i == 0) ? 0 : *(p - dst.step + j);
                uchar p3 = (i == 0 || j == width - 1) ? 0 : *(p - dst.step + j + 1);
                uchar p9 = (i == 0 || j == 0) ? 0 : *(p - dst.step + j - 1);
                uchar p6 = (i == height - 1) ? 0 : *(p + dst.step + j);
                uchar p5 = (i == height - 1 || j == width - 1) ? 0 : *(p + dst.step + j + 1);
                uchar p7 = (i == height - 1 || j == 0) ? 0 : *(p + dst.step + j - 1);
                if ((p2 + p3 + p4 + p5 + p6 + p7 + p8 + p9) >= 2 && (p2 + p3 + p4 + p5 + p6 + p7 + p8 + p9) <= 6)
                {
                    int ap = 0;
                    if (p2 == 0 && p3 == 1) ++ap;
                    if (p3 == 0 && p4 == 1) ++ap;
                    if (p4 == 0 && p5 == 1) ++ap;
                    if (p5 == 0 && p6 == 1) ++ap;
                    if (p6 == 0 && p7 == 1) ++ap;
                    if (p7 == 0 && p8 == 1) ++ap;
                    if (p8 == 0 && p9 == 1) ++ap;
                    if (p9 == 0 && p2 == 1) ++ap;

                    if (ap == 1 && p2 * p4 * p6 == 0 && p4 * p6 * p8 == 0)
                    {
                        //标记
                        mFlag.push_back(p+j);
                    }
                }
            }
        }

        //将标记的点删除
        for (std::vector<uchar *>::iterator i = mFlag.begin(); i != mFlag.end(); ++i)
        {
            **i = 0;
        }

        //直到没有点满足，算法结束
        if (mFlag.empty())
        {
            break;
        }
        else
        {
            mFlag.clear();//将mFlag清空
        }

        //对点标记
        for (int i = 0; i < height; ++i)
        {
            uchar * p = dst.ptr<uchar>(i);
            for (int j = 0; j < width; ++j)
            {
                //如果满足四个条件，进行标记
                //  p9 p2 p3
                //  p8 p1 p4
                //  p7 p6 p5
                uchar p1 = p[j];
                if (p1 != 1) continue;
                uchar p4 = (j == width - 1) ? 0 : *(p + j + 1);
                uchar p8 = (j == 0) ? 0 : *(p + j - 1);
                uchar p2 = (i == 0) ? 0 : *(p - dst.step + j);
                uchar p3 = (i == 0 || j == width - 1) ? 0 : *(p - dst.step + j + 1);
                uchar p9 = (i == 0 || j == 0) ? 0 : *(p - dst.step + j - 1);
                uchar p6 = (i == height - 1) ? 0 : *(p + dst.step + j);
                uchar p5 = (i == height - 1 || j == width - 1) ? 0 : *(p + dst.step + j + 1);
                uchar p7 = (i == height - 1 || j == 0) ? 0 : *(p + dst.step + j - 1);

                if ((p2 + p3 + p4 + p5 + p6 + p7 + p8 + p9) >= 2 && (p2 + p3 + p4 + p5 + p6 + p7 + p8 + p9) <= 6)
                {
                    int ap = 0;
                    if (p2 == 0 && p3 == 1) ++ap;
                    if (p3 == 0 && p4 == 1) ++ap;
                    if (p4 == 0 && p5 == 1) ++ap;
                    if (p5 == 0 && p6 == 1) ++ap;
                    if (p6 == 0 && p7 == 1) ++ap;
                    if (p7 == 0 && p8 == 1) ++ap;
                    if (p8 == 0 && p9 == 1) ++ap;
                    if (p9 == 0 && p2 == 1) ++ap;

                    if (ap == 1 && p2 * p4 * p8 == 0 && p2 * p6 * p8 == 0)
                    {
                        //标记
                        mFlag.push_back(p+j);
                    }
                }
            }
        }

        //将标记的点删除
        for (std::vector<uchar *>::iterator i = mFlag.begin(); i != mFlag.end(); ++i)
        {
            **i = 0;
        }

        //直到没有点满足，算法结束
        if (mFlag.empty())
        {
            break;
        }
        else
        {
            mFlag.clear();//将mFlag清空
        }
    }

    LOGD("thinImage leave");

    return dst;
}

void splitMainRoad(Mat crossRoadImage, Mat mainRoadImage)
{
    LOGD("splitMainRoad enter");
    int ffillMode = 1;
    int loDiff = 20, upDiff = 20;
    int connectivity = 8;
    int isColor = true;
//    bool useMask = false;
    int newMaskVal = 255;

    Vec3b rgb = crossRoadImage.at<Vec3b>(200, 200);
    int r1,g1,b1;
    b1 = rgb[0];
    g1 = rgb[1];
    r1 = rgb[2];
    LOGD("r=%d g=%d b=%d", r1, g1, b1);

    Point seed = Point(200, 200);
    int lo = loDiff;//ffillMode == 0 ? 0 : loDiff;
    int up = upDiff;//ffillMode == 0 ? 0 : upDiff;
    int flags = connectivity + (newMaskVal << 8) + (ffillMode == 1 ? FLOODFILL_FIXED_RANGE | FLOODFILL_MASK_ONLY : 0);
    int b = (unsigned)theRNG() & 255;
    int g = (unsigned)theRNG() & 255;
    int r = (unsigned)theRNG() & 255;
    Rect ccomp;

    Scalar newVal = Scalar(b, g, r) ;//isColor ? Scalar(b, g, r) : Scalar(r*0.299 + g*0.587 + b*0.114);
//    Mat dst = image;//isColor ? image : gray;
    int area;

//    threshold(mainRoadImage, mainRoadImage, 1, 128, THRESH_BINARY);
    LOGD("splitMainRoad 111111");
    area = floodFill(crossRoadImage, mainRoadImage, seed, newVal, &ccomp, Scalar(lo, lo, lo),
                     Scalar(up, up, up), flags);
    LOGD("splitMainRoad leave");
}

int searchHopPoint(cv::Mat image, int *resX, int *resY)
{

    LOGD("searchHopPoint enter");
    //用floodFill找到蓝色主路，得到的是主路像素值为255
    Mat mainRoadImage;
    mainRoadImage.create(image.rows+2, image.cols+2, CV_8UC1);
    mainRoadImage = Scalar::all(0);
    splitMainRoad(image, mainRoadImage);

    //将图像二值化，thinImage只接受二值化图像数据
    //之后改为直接在splitMainRoad中二值化，目前spliteMainRoad会将边缘置为1
    cv::threshold(mainRoadImage, mainRoadImage, 128, 1, cv::THRESH_BINARY);

    //将主路骨骼化
    cv::Mat dst = thinImage(mainRoadImage);
    //得到主路骨骼化的有效像素
    std::vector<cv::Point2i> locations;
    cv::findNonZero(dst, locations);

    //判断角度的跳变所在像素位置
    bool found = false;
    double pi = acos(-1);
    double threshold = tan((pi/360)*5);
    float slopeTo200 = 0;
    int freshNewCounter = 5;
    for (int i=(int)locations.size(); i>=0; i--) {
        int x = locations[i].x;
        int y = locations[i].y;
        if (y < 200) {
            if (freshNewCounter-- > 0) {
                slopeTo200 = (float)abs(y-200.0)/(float)abs(x-200.0);
            } else {
                float newSlopeTo200 = (float)abs(y-200.0)/(float)abs(x-200.0);
                if (newSlopeTo200 != slopeTo200) {
                    float diff = abs(newSlopeTo200 - slopeTo200);
                    if (diff > threshold) {
                        *resX = locations[i+1].x - 200;
                        *resY = locations[i+1].y - 200;
                        found = true;
                        break;
                    }
                }
            }
        }
    }

    if (!found) {
        LOGD("searchHopPoint leave, no found anything.");
        return -1;
    }

    LOGD("searchHopPoint. Found the hop point. x=%d, y=%d", *resX, *resY);
    return 0;
}