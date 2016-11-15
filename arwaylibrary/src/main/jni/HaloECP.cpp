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
#include <stdlib.h>
#include <vector>

#include "algorithm.h"
#include "types.h"
#include "CrossRoad.h"
#include "JniUtils.h"

using namespace cv;
using namespace std;

//
// Created by HarryMoo on 15/6/2016.
//

#define LOG_TAG "HaloAI_ECP_Lib"
#define LOG_TAG_ERROR "branch_handle"
#define LOGD_ANDROID(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#define LOGE_ANDROID(...) ((void)__android_log_print(ANDROID_LOG_ERROR, LOG_TAG_ERROR, __VA_ARGS__))

int searchHopPoint(cv::Mat image, int *x, int *y);

static int g_nIndex = 0;
CrossRoad crossRoad;
JNIEXPORT jobjectArray
JNICALL Java_com_haloai_hud_hudendpoint_arwaylib_utils_EnlargedCrossProcess_nativeGetBranchRoads
        (JNIEnv *env, jobject javaSelf, jlong ecImage, jint centerPointIndex,
         jobjectArray mainRoadArr) {
    LOGD_ANDROID("nativeGetBranchRoad enter!!!");

    Mat matRoadImg = *(Mat *) ecImage;


    vector <vector<Point2i> > vecPointSet;
    vector <Point2i> vecMainRoad;
    int mainRoadArrSize = env->GetArrayLength(mainRoadArr);
    LOGD_ANDROID("mainRoadArr.size=%d", (int) mainRoadArrSize / 2);
    LOGD_ANDROID("centerPointIndex=%d", (int) centerPointIndex);
    //LOGE_ANDROID("==============haloecp start==========");
    for (int j = 0; j < mainRoadArrSize / 2; j++) {
        jstring string_x = (jstring)(env->GetObjectArrayElement(mainRoadArr, j * 2));
        const char *chars_x = env->GetStringUTFChars(string_x, 0);
        jstring string_y = (jstring)(env->GetObjectArrayElement(mainRoadArr, j * 2 + 1));
        const char *chars_y = env->GetStringUTFChars(string_y, 0);
        Point2i point;
        point.x = atoi(chars_x);
        point.y = atoi(chars_y);
        vecMainRoad.push_back(point);
        env->ReleaseStringUTFChars(string_x, chars_x);
        env->ReleaseStringUTFChars(string_y, chars_y);
        LOGD_ANDROID("point.x = %d,point.y = %d", (int) point.x, (int) point.y);
        //LOGE_ANDROID("%d,%d",(int)point.x,(int)point.y);
    }


    //======test==========
#if 1
    Mat matTestScreen(matRoadImg.rows, matRoadImg.cols, CV_8UC3);
    matTestScreen.setTo(0);
    int nRet = DrawPoint(vecMainRoad, Scalar(0, 0, 255), 2, matTestScreen);


    Mat matTempMerge;
    vector <Mat> vecMatTemp;
    vecMatTemp.clear();
    vecMatTemp.push_back(matRoadImg);
    vecMatTemp.push_back(matTestScreen);
    nRet = MergeMat(vecMatTemp, matTempMerge);


    char cTemp[100];
    sprintf(cTemp, "//sdcard//testimage//helong//MergeImg-%d.bmp", g_nIndex++);
    imwrite(cTemp, matTempMerge);
#endif

    //======================

    //LOGE_ANDROID("==============haloecp end============");
    int res = GetCrossRoadPoint(matRoadImg, vecMainRoad, centerPointIndex, vecPointSet);
    LOGD_ANDROID("vecPointSet.size=%d", (int) vecPointSet.size());
    /*for(int j=0;j<(int)vecPointSet.size();j++){
        LOGE_ANDROID("==============return start==========");
        vector<Point2i> temp = vecPointSet.at(j);
        for(int k=0;k<(int)temp.size();k++){
            LOGE_ANDROID("%d,%d",temp[k].x,temp[k].y);
        }
        LOGE_ANDROID("==============return end============");
    }*/
    jobjectArray retBranchRoads = NULL;
    if (!res/* && vecMainRoad.size() == 2*/) {
        //Set the main road tail end.
        /*jclass clazz = env->GetObjectClass(mainRoadTailEnd);
        jfieldID xField = env->GetFieldID(clazz, "x", "I");
        jfieldID yField = env->GetFieldID(clazz, "y", "I");
        env->SetIntField(mainRoadTailEnd, xField, vecMainRoad[1].x);
        env->SetIntField(mainRoadTailEnd, yField, vecMainRoad[1].y);*/

        //Construct the string of all branch path points
        retBranchRoads = env->NewObjectArray(vecPointSet.size(), env->FindClass("java/lang/String"), 0);
        //用String传递一条路径值，"pt1.x,pt1.y,pt2.x,pt2.y,...,ptN.x,ptN.y"
        std::stringstream branchRoadPrintedValues;
        for (int i = 0; i < vecPointSet.size(); i++) {
            vector <Point2i> branchRoad = vecPointSet.at(i);
            LOGD_ANDROID("branch-%d, the size is %d", i, (int) branchRoad.size());
            for (int j = 0; j < branchRoad.size(); j++) {
                if (i + j != 0) {
                    branchRoadPrintedValues << ",";
                }
                branchRoadPrintedValues << branchRoad[j].x << "," << branchRoad[j].y;
//                branchRoadPrintedValues += ",";
//                branchRoadPrintedValues += branchRoad[j].y;
            }
            LOGD_ANDROID("branchString is %s", branchRoadPrintedValues.str().c_str());
            jstring jstr = env->NewStringUTF(branchRoadPrintedValues.str().c_str());
            env->SetObjectArrayElement(retBranchRoads, i, jstr);
        }
    }

    LOGD_ANDROID("nativeGetBranchRoad leave!!!");
    return retBranchRoads;
}

JNIEXPORT jint

JNICALL Java_com_haloai_hud_hudendpoint_arwaylib_utils_EnlargedCrossProcess_nativeGetHopPointInCrossImage
        (JNIEnv *env, jobject javaThis, jlong ecImage, jobject returnObj) {
    LOGE_ANDROID("nativeGetFirstTurnPointInCrossImage enter!!");

    jclass cls_ArrayList = env->FindClass("java/util/ArrayList");
    jmethodID construct_arrayList = env->GetMethodID(cls_ArrayList, "<init>", "()V");
    jobject obj_array_ = env->NewObject(cls_ArrayList,construct_arrayList);

    // Get the class of the input object
    jclass clazz = env->GetObjectClass(returnObj);
    // Get Field references
    jfieldID xField = env->GetFieldID(clazz, "x", "I");
    jfieldID yField = env->GetFieldID(clazz, "y", "I");

//    jbyte* ecImageBytes = env->GetByteArrayElements(aMapECImageBytes, JNI_FALSE);
//    Mat image = Mat(400, 400, CV_8UC3, ecImageBytes);
//    if (image.empty())
//        return -1;
    Mat image = *(Mat *) ecImage;

    int x = 0, y = 0;
    if (!searchHopPoint(image, &x, &y)) {
        // Set fields for object
        env->SetIntField(returnObj, xField, x);
        env->SetIntField(returnObj, yField, y);
    } else {
        LOGE_ANDROID("nativeGetFirstTurnPointInCrossImage leave!!! no found.");
        return -1;
    }

    LOGE_ANDROID("nativeGetFirstTurnPointInCrossImage x=%d,y=%d",x,y);
    LOGE_ANDROID("nativeGetFirstTurnPointInCrossImage leave!!!");
    return 0;
}

/**
 *将java层传递过来的数据解析成C++层使用的数据,并调用getCrosslinks返回结果
 */
JNIEXPORT jint JNICALL Java_com_haloai_hud_hudendpoint_arwaylib_utils_EnlargedCrossProcess_nativeGetCrossLinks
        (JNIEnv *env, jobject javaThis, jobject links, jobject linkInfos,
         jobject latlng, jobject szCover, jstring strDictPath, jint crossRoadLen, jobject crossLinks,
        jobject mainRoad,jobject crossPointIndexs){
    LOGE_ANDROID("nativeGetCrossLinks start");
    LOGE_ANDROID("long size = %d",sizeof(long));
    //about ArrayList
    jclass cls_ArrayList = env->FindClass("java/util/ArrayList");
    if (cls_ArrayList == NULL) {
        return -1;
    }
    jmethodID construct_arrayList = env->GetMethodID(cls_ArrayList, "<init>", "()V");
    jmethodID arrayList_add = env->GetMethodID(cls_ArrayList, "add", "(Ljava/lang/Object;)Z");
    jmethodID arrayList_get = env->GetMethodID(cls_ArrayList, "get", "(I)Ljava/lang/Object;");
    jmethodID arrayList_size = env->GetMethodID(cls_ArrayList, "size", "()I");
    LOGE_ANDROID("nativeGetCrossLinks -- about ArrayList");

    //about LatLngOutside
    jclass cls_LatLng = env->GetObjectClass(latlng);
    if (cls_LatLng == NULL) {
        return -1;
    }
    jmethodID construct_latlng = env->GetMethodID(cls_LatLng, "<init>", "()V");
    jfieldID latlng_lat = env->GetFieldID(cls_LatLng,"lat","D");
    jfieldID latlng_lng = env->GetFieldID(cls_LatLng,"lng","D");
    jdouble lat_value = env->GetDoubleField(latlng,latlng_lat);
    jdouble lng_value = env->GetDoubleField(latlng,latlng_lng);
    LOGE_ANDROID("nativeGetCrossLinks -- about LatLngOutside");

    //about Size2iOutSide
    jclass cls_Size2i = env->GetObjectClass(szCover);
    if(cls_Size2i == NULL){
        return -1;
    }
    jfieldID size2i_width = env->GetFieldID(cls_Size2i,"width","I");
    jfieldID size2i_height = env->GetFieldID(cls_Size2i,"height","I");
    jint width_value = env->GetIntField(szCover,size2i_width);
    jint height_value = env->GetIntField(szCover,size2i_height);
    LOGE_ANDROID("nativeGetCrossLinks -- about Size2iOutSide");

    vector <vector<HALocationCoordinate2D> > _links;
    jint linksSize = env->CallIntMethod(links,arrayList_size);
    for(int i=0;i<linksSize;i++){
        jobject link = env->CallObjectMethod(links,arrayList_get,i);
        jint linkSize = env->CallIntMethod(link,arrayList_size);
        vector<HALocationCoordinate2D> _link;
        for(int j=0;j<linkSize;j++){
            jobject latLng_Outside = env->CallObjectMethod(link,arrayList_get,j);
            jdouble link_lat_value = env->GetDoubleField(latLng_Outside,latlng_lat);
            jdouble link_lng_value = env->GetDoubleField(latLng_Outside,latlng_lng);
            HALocationCoordinate2D coordinate2D;
            coordinate2D.latitude = link_lat_value;
            coordinate2D.longitude = link_lng_value;
            _link.push_back(coordinate2D);
        }
        _links.push_back(_link);
    }
    LOGE_ANDROID("links size = %d",_links.size());
    for(int i=0;i<_links.size();i++){
        vector<HALocationCoordinate2D>* _linkPot = &(_links[i]);
        for(int j=0;j<(*_linkPot).size();j++) {
            if (j % 3 == 0) {
                HALocationCoordinate2D *coordinate2DPot = &((*_linkPot)[j]);
                LOGE_ANDROID("lat=%lf,lng=%lf",(*coordinate2DPot).latitude,(*coordinate2DPot).longitude);
            }
        }
    }
    vector <LinkInfo> _mainRoadLinkInfos;
    LinkInfo linkInfo;
    _mainRoadLinkInfos.push_back(linkInfo);
    LOGE_ANDROID("_mainRoadLinkInfos.size = %d",_mainRoadLinkInfos.size());
    //113.936913,22.523966 软件基地4A
    HALocationCoordinate2D _centerPoint;
    _centerPoint.latitude = lat_value;
    _centerPoint.longitude = lng_value;
    LOGE_ANDROID("center point lat=%lf,lng=%lf",lat_value,lng_value);
    cv::Size2i _szCover(width_value, height_value);
    //string _filePath = "/sdcard/haloaimapdata_32.hmd";
    string _filePath = jstringToStr(env,strDictPath);
    LOGE_ANDROID("file path = %s",_filePath.c_str());
    int _crossRoadLen = crossRoadLen;
    vector <vector<HALocationCoordinate2D> > _crossLinks;
    vector<HALocationCoordinate2D> _mainRoad;
    vector<int> _vecCrossPointIndex;
    int _centerPointInMainRoad;
    //CrossRoad crossRoad;
    LOGE_ANDROID("into crossRoad.getCrossLinks");
    int res = crossRoad.getCrossLinks(_links, _mainRoadLinkInfos, _centerPoint, _szCover,
                                      _filePath, _crossRoadLen, _crossLinks, _mainRoad,
                                      _vecCrossPointIndex,_centerPointInMainRoad);
    LOGE_ANDROID("outto crossRoad.getCrossLinks");
    if(res == 0) {
        LOGE_ANDROID("res=%d,crossLinks.size=%d",res,_crossLinks.size());
        //使用_crossLinks初始化crossLinks数据
        for (int i = 0; i < _crossLinks.size(); i++) {
            vector <HALocationCoordinate2D> *crossLinkPot = &(_crossLinks[i]);
            jobject obj_arrayList = env->NewObject(cls_ArrayList, construct_arrayList);
            for (int j = 0; j < (*crossLinkPot).size(); j++) {
                HALocationCoordinate2D *coordPot = &(*crossLinkPot)[j];
                jobject obj_latlng = env->NewObject(cls_LatLng, construct_latlng);
                env->SetDoubleField(obj_latlng, latlng_lat, coordPot->latitude);
                env->SetDoubleField(obj_latlng, latlng_lng, coordPot->longitude);
                env->CallBooleanMethod(obj_arrayList, arrayList_add, obj_latlng);
                if (obj_latlng) {
                    env->DeleteLocalRef(obj_latlng);
                }
            }
            env->CallBooleanMethod(crossLinks, arrayList_add, obj_arrayList);
            if (obj_arrayList) {
                env->DeleteLocalRef(obj_arrayList);
            }
        }
        //使用_mainRoad初始化路网中主路部分数据
        for (int j = 0; j < _mainRoad.size(); j++) {
            HALocationCoordinate2D *coordPot = &(_mainRoad[j]);
            jobject obj_latlng = env->NewObject(cls_LatLng, construct_latlng);
            env->SetDoubleField(obj_latlng, latlng_lat, coordPot->latitude);
            env->SetDoubleField(obj_latlng, latlng_lng, coordPot->longitude);
            env->CallBooleanMethod(mainRoad, arrayList_add, obj_latlng);
            if (obj_latlng) {
                env->DeleteLocalRef(obj_latlng);
            }
        }
        //使用_vecCrossPointIndex初始化交点数据
        jclass cls_int = env->FindClass("java/lang/Integer");
        if (cls_int==NULL) {
            return -1;
        }
        jmethodID construct_int = env->GetMethodID(cls_int, "<init>", "(I)V");
        if (construct_int==NULL){
            return -1;
        }
        for (int j = 0; j < _vecCrossPointIndex.size(); j++) {
            int index = _vecCrossPointIndex[j];
            jobject obj_int = env->NewObject(cls_int, construct_int, index);
            env->CallBooleanMethod(crossPointIndexs, arrayList_add, obj_int);
            if (obj_int) {
                env->DeleteLocalRef(obj_int);
            }
        }
        //使用_centerPointInMainRoad初始化,作为crossPointIndexs的最后一个点
        jobject obj_int = env->NewObject(cls_int, construct_int, _centerPointInMainRoad);
        env->CallBooleanMethod(crossPointIndexs, arrayList_add, obj_int);
        if (obj_int) {
            env->DeleteLocalRef(obj_int);
        }
    }
    return res;
}

JNIEXPORT void JNICALL Java_com_haloai_hud_hudendpoint_arwaylib_utils_EnlargedCrossProcess_nativeClearRoadNetStatus
(JNIEnv *, jobject){
    crossRoad.clearHistoryCrossPoint();
}

// jstring To String
string jstringToStr(JNIEnv* env, jstring jstr)
{
    char* str = jstringToCharArr(env, jstr);
    string value(str);
    free(str);
    return value;
}

// jstring To char*
char* jstringToCharArr(JNIEnv* env, jstring jstr)
{
    char* rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("utf-8");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray)env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0)
    {
        rtn = (char*)malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}

/**
 * @brief 对输入图像进行细化
 * @param src为输入图像,用cvThreshold函数处理过的8位灰度图像格式，元素中只有0与1,1代表有元素，0代表为空白
 * @param maxIterations限制迭代次数，如果不进行限制，默认为-1，代表不限制迭代次数，直到获得最终结果
 * @return 为对src细化后的输出图像,格式与src格式相同，元素中只有0与1,1代表有元素，0代表为空白
 */
cv::Mat thinImage(const cv::Mat &src, const int maxIterations) {
    LOGD_ANDROID("thinImage enter");
    assert(src.type() == CV_8UC1);
    cv::Mat dst;
    int width = src.cols;
    int height = src.rows;
    src.copyTo(dst);
    int count = 0;  //记录迭代次数
    while (true) {
        count++;
        if (maxIterations != -1 && count > maxIterations) //限制次数并且迭代次数到达
            break;
        std::vector < uchar * > mFlag; //用于标记需要删除的点
        //对点标记
        for (int i = 0; i < height; ++i) {
            uchar *p = dst.ptr<uchar>(i);
            for (int j = 0; j < width; ++j) {
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
                if ((p2 + p3 + p4 + p5 + p6 + p7 + p8 + p9) >= 2 &&
                    (p2 + p3 + p4 + p5 + p6 + p7 + p8 + p9) <= 6) {
                    int ap = 0;
                    if (p2 == 0 && p3 == 1) ++ap;
                    if (p3 == 0 && p4 == 1) ++ap;
                    if (p4 == 0 && p5 == 1) ++ap;
                    if (p5 == 0 && p6 == 1) ++ap;
                    if (p6 == 0 && p7 == 1) ++ap;
                    if (p7 == 0 && p8 == 1) ++ap;
                    if (p8 == 0 && p9 == 1) ++ap;
                    if (p9 == 0 && p2 == 1) ++ap;

                    if (ap == 1 && p2 * p4 * p6 == 0 && p4 * p6 * p8 == 0) {
                        //标记
                        mFlag.push_back(p + j);
                    }
                }
            }
        }

        //将标记的点删除
        for (std::vector<uchar *>::iterator i = mFlag.begin(); i != mFlag.end(); ++i) {
            **i = 0;
        }

        //直到没有点满足，算法结束
        if (mFlag.empty()) {
            break;
        }
        else {
            mFlag.clear();//将mFlag清空
        }

        //对点标记
        for (int i = 0; i < height; ++i) {
            uchar *p = dst.ptr<uchar>(i);
            for (int j = 0; j < width; ++j) {
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

                if ((p2 + p3 + p4 + p5 + p6 + p7 + p8 + p9) >= 2 &&
                    (p2 + p3 + p4 + p5 + p6 + p7 + p8 + p9) <= 6) {
                    int ap = 0;
                    if (p2 == 0 && p3 == 1) ++ap;
                    if (p3 == 0 && p4 == 1) ++ap;
                    if (p4 == 0 && p5 == 1) ++ap;
                    if (p5 == 0 && p6 == 1) ++ap;
                    if (p6 == 0 && p7 == 1) ++ap;
                    if (p7 == 0 && p8 == 1) ++ap;
                    if (p8 == 0 && p9 == 1) ++ap;
                    if (p9 == 0 && p2 == 1) ++ap;

                    if (ap == 1 && p2 * p4 * p8 == 0 && p2 * p6 * p8 == 0) {
                        //标记
                        mFlag.push_back(p + j);
                    }
                }
            }
        }

        //将标记的点删除
        for (std::vector<uchar *>::iterator i = mFlag.begin(); i != mFlag.end(); ++i) {
            **i = 0;
        }

        //直到没有点满足，算法结束
        if (mFlag.empty()) {
            break;
        }
        else {
            mFlag.clear();//将mFlag清空
        }
    }

    LOGD_ANDROID("thinImage leave");

    return dst;
}

void splitMainRoad(Mat crossRoadImage, Mat mainRoadImage) {
    LOGD_ANDROID("splitMainRoad enter");
    int ffillMode = 1;
    int loDiff = 20, upDiff = 20;
    int connectivity = 8;
    int isColor = true;
//    bool useMask = false;
    int newMaskVal = 255;

    Vec3b rgb = crossRoadImage.at<Vec3b>(200, 200);
    int r1, g1, b1;
    b1 = rgb[0];
    g1 = rgb[1];
    r1 = rgb[2];
    LOGD_ANDROID("r=%d g=%d b=%d", r1, g1, b1);

    Point seed = Point(200, 200);
    int lo = loDiff;//ffillMode == 0 ? 0 : loDiff;
    int up = upDiff;//ffillMode == 0 ? 0 : upDiff;
    int flags = connectivity + (newMaskVal << 8) +
                (ffillMode == 1 ? FLOODFILL_FIXED_RANGE | FLOODFILL_MASK_ONLY : 0);
    int b = (unsigned) theRNG() & 255;
    int g = (unsigned) theRNG() & 255;
    int r = (unsigned) theRNG() & 255;
    Rect ccomp;

    Scalar newVal = Scalar(b, g,
                           r);//isColor ? Scalar(b, g, r) : Scalar(r*0.299 + g*0.587 + b*0.114);
//    Mat dst = image;//isColor ? image : gray;
    int area;

//    threshold(mainRoadImage, mainRoadImage, 1, 128, THRESH_BINARY);
    LOGD_ANDROID("splitMainRoad 111111");
    area = floodFill(crossRoadImage, mainRoadImage, seed, newVal, &ccomp, Scalar(lo, lo, lo),
                     Scalar(up, up, up), flags);
    LOGD_ANDROID("splitMainRoad leave");
}

int searchHopPoint(cv::Mat image, int *resX, int *resY) {

    LOGD_ANDROID("searchHopPoint enter");
    //用floodFill找到蓝色主路，得到的是主路像素值为255
    Mat mainRoadImage;
    mainRoadImage.create(image.rows + 2, image.cols + 2, CV_8UC1);
    mainRoadImage = Scalar::all(0);
    splitMainRoad(image, mainRoadImage);

    //将图像二值化，thinImage只接受二值化图像数据
    //之后改为直接在splitMainRoad中二值化，目前spliteMainRoad会将边缘置为1
    cv::threshold(mainRoadImage, mainRoadImage, 128, 1, cv::THRESH_BINARY);

    //将主路骨骼化
    cv::Mat dst = thinImage(mainRoadImage);
    //得到主路骨骼化的有效像素
    std::vector <cv::Point2i> locations;
    cv::findNonZero(dst, locations);

    //判断角度的跳变所在像素位置
    bool found = false;
    double pi = acos(-1);
    double threshold = tan((pi / 360) * 5);
    float slopeTo200 = 0;
    int freshNewCounter = 5;
    for (int i = (int) locations.size(); i >= 0; i--) {
        int x = locations[i].x;
        int y = locations[i].y;
        if (y < 200) {
            if (freshNewCounter-- > 0) {
                slopeTo200 = (float) abs(y - 200.0) / (float) abs(x - 200.0);
            } else {
                float newSlopeTo200 = (float) abs(y - 200.0) / (float) abs(x - 200.0);
                if (newSlopeTo200 != slopeTo200) {
                    float diff = abs(newSlopeTo200 - slopeTo200);
                    if (diff > threshold) {
                        *resX = locations[i + 1].x - 200;
                        *resY = locations[i + 1].y - 200;
                        found = true;
                        break;
                    }
                }
            }
        }
    }

    if (!found) {
        LOGD_ANDROID("searchHopPoint leave, no found anything.");
        return -1;
    }

    LOGD_ANDROID("searchHopPoint. Found the hop point. x=%d, y=%d", *resX, *resY);
    return 0;
}