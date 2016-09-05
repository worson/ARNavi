
//#define IS_WINDOWS_VER					// 是否为windows平台


#include "opencv2/core/core.hpp"
#include "opencv2/features2d/features2d.hpp"
#include "opencv2/imgproc/imgproc.hpp"	
#include <vector>
#include <math.h>
#include <algorithm>


#ifdef IS_WINDOWS_VER
	#include "opencv2/highgui/highgui.hpp"
	#include <io.h>
	#include <iostream>
	#include <mxml.h>
	#include "tinyxml/tinystr.h"
	#include "tinyxml/tinyxml.h"
	#include <stdlib.h>
#else
	#include <android/log.h>
	#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "Algorithm", __VA_ARGS__)

	#include <fstream>

	#include <sys/stat.h>
	#include<stdio.h>
	#include<sys/types.h>
	#include <unistd.h>

	#include <stdlib.h>
	#include <time.h>
#endif



using namespace cv;
using namespace std;


#define NEIGHBOUR_BOX1 3                      // 阈值，邻域窗口大小
#define NEIGHBOUR_BOX2 5                      // 阈值，邻域窗口大小
#define NEIGHBOUR_BOX3 7                      // 阈值，邻域窗口大小
#define NEIGHBOUR_BOX4 9                      // 邻域窗口大小
#define NEIGHBOUR_BOX5 17                     // 邻域窗口大小
#define NEIGHBOUR_BOX6 15                     // 邻域窗口大小

#define SAMPLE_STEP 5                     // 间隔采样的步长，用于获取主路中心线


#define VERTEX_DISTANCE 40              // 端点距离阈值，小于该阈值判定为箭头点，否则判定为起点
#define SAMLL_SET_NUMBER 30				// 离散块阈值
#define COS_ANGEL_THRESH 0.25			// 两线夹角余弦阈值，用于删除主路两端点之外的辅路

#define NEIGHBOUR_CENTER_ORDER 10			// 取中心点前第n个点，用于判断主路从中心点后的方向

#define COS_VALUE	0.85				// 余弦值阈值，用于判断两条线是否要合并
#define VALID_POINT_RATE	0.70		// 有效点比率，用于检测虚线点
#define SINGLE_DOTLINE_AREA 300			// 单个虚线点的面积，用于去除道路夹角、夹线的干扰

#define IS_SHOW_PREPROCESS_IMAGE	0				// 控制是否显示图像，预处理时的图像
#define IS_SHOW_CROSSROAD	0						// 控制是否显示图像，画与主路相交时的图像
#define IS_SHOW_MAINROAD	0						// 控制是否显示图像，画主路时的图像
#define IS_SHOW_ASSISTROAD	0						// 控制是否显示图像，画辅路时的图像

#define IS_SHOW_DEBUG_TEST	0						// 控制是否显示图像，调试时的图像

#define IS_SAVE_ROAD		0						// 控制是否保存路径
#define IS_SAVE_MERGEROAD	0					// 控制是否保存主路和岔路的合成路径
#define IS_PRINT_TIME		0						// 控制是否打印时间

#define CROSSIMAGE_W	400			// 路口放大图宽度
#define CROSSIMAGE_H	400			// 路口放大图高度

#define IMAGE_SHOW_NUM	20			// 控制一幅大图中可显示的小图个数
#define IMAGE_SHOW_NUM_BYROW	2			// 控制一幅大图中一排可显示的小图个数


#define IS_TEST_DEBUG	0			// 测试标签


/*
功能：
	获取岔路（主接口）
参数：
	[in]const Mat& matRoadImg - 导航图像，RGB图像
	[in]const vector<Point2i>& vecMainRoadScreenPoint - 主路屏幕点
	[in]int nCenterPointIndex - 主路屏幕点中，中心点位置
	[out]vector<vector<Point2i>>& vecCrossRoadPointSet - 岔路点集，每个子集代表一条岔路
返回：
	0 - 正常，其他 - 异常
*/
int GetCrossRoadPoint(const Mat& matRoadImg, const vector<Point2i>& vecMainRoadScreenPoint, int nCenterPointIndex,
					  vector<vector<Point2i> >& vecCrossRoadPointSet);

/*
功能：
	获取主路
参数：
	[in]const Mat& matMainRoadBw - 主路二值图
	[out]Mat& matMainRoadThinBwI - 主路细化图
	[out]vector<vector<Point2i>>& vecMainRoadPointSet - 主路点集
	[out]vector<Point2i>& vecVertexSet - 顶点坐标
	[out]vector<Point2i>& vecFeaturePtSet - 主路中心点后特征点集，拐点（未实现，待完善）
返回：
	0 - 正常，其他 - 异常
*/
int GetMainRoadPoint(const Mat& matMainRoadBwI, Mat& matMainRoadThinBwI, vector<vector<Point2i> >& vecMainRoadPointSet,
	vector<Point2i>& vecVertexSet, vector<Point2i>& vecFeaturePtSet);


/*
功能：
	获取辅路
参数：
	[in]const Mat& matAssistRoadBw - 辅路二值图
	[out]Mat& matAssistRoadThinBwI - 辅路细化图	
	[out]vector<vector<Point2i>>& vecAssistRoadPointSet - 辅路点集（包括部分干扰）
返回：
	0 - 正常，其他 - 异常
*/
int GetAssistRoadPoint(const Mat& matAssistRoadBwI, Mat& matAssistRoadThinBwI, vector<vector<Point2i> >& vecAssistRoadPointSet);

int GetStartEndPoint(const Mat& matMainRoadThinBwI,const vector<Point2i>& vecAllPoints,const vector<Point2i>& vecVertexPoints,
	vector<Point2i>& vecStartPoint, vector<Point2i>& vecEndPoint, vector<Point2i>& vecMainRoadPoint);
bool sortFun (Vec2i vec1,Vec2i vec2);

int GetVertexPoints(const Mat& matThinBwI, const vector<cv::Point2i>& vecNonZerosLocations, vector<Point2i>& vecVertexPoints);

static cv::Mat thinImage(const cv::Mat & src, const int maxIterations = -1);

// 把4邻域上均存在相邻点的点去掉
int FilterThinBwI(const Mat& matThinBwI, const vector<cv::Point2i>& vecNonZerosLocations, 
	Mat& matFilterThinBwI, vector<Point2i>& vecFilterPoints);

int GetSingleLine(const Mat& matThinBwI,const vector<cv::Point2i>& vecNonZerosLocations, Mat& matSingleLineBwI);

int GetContourPoints(const Mat& matThinBwI,Point2i ptSeedPoint,vector<Point2i>& vecContourPoint,Point2i& ptEndPoint);

/*
功能：
	对细化线上的点按顺序排序
参数：
	[in&out]vector<Point2i>& vecLinePoint - 排序前后路径点集
	[in]int nRow - 图像高度
	[in]int nCol - 图像宽度
返回：
	0 - 正常，其他 - 异常
*/
int SortPoint(vector<Point2i>& vecLinePoint,int nRow, int nCol);

/*
功能：
	统计每个点邻域内的非零点个数
参数：
	[in]const vector<Point2i>& vecPoint - 参与统计的点集
	[in]const Mat& matBw - 参与统计的图像(二值图)
	[out]Mat_<int>& matStatisticMap - 二值图上每个像素点对应的非零点个数，即统计表格数据
	[out]vector<int>& vecStaticValue - 统计结果
	[out]vector<int>& vecCenterDis - 记录每个点覆盖窗口有效像素点的中心离窗口中心的距离
	[out]Point2i& ptMaxStatisticPt - 最大统计值对应的坐标点，即箭头点
	[out]int& nMaxStaticVs - 最大统计值
	[out]int& nMaxIndex - 最大统计值在点集中的位置
返回：
	0 - 正常，其他 - 异常
备注：
	在统计表格数据中，每个白色像素点对应真实的统计值，黑色像素点不参与统计，对应值一律置为初值nRow*nCol，
	nCol、nRow对应图像的宽、高
*/
/* int GetStatisticValue(const vector<Point2i>& vecPoint, const Mat& matBw, Mat_<int>& matStatisticMap,
				   vector<int>& vecStatisticValue, vector<int>& vecCenterDis,
				   Point2i& ptMaxStatisticPt, int& nMaxStatisticVs, int& nMaxIndex); */


/*
功能：
	对vector中的点按逆序重排，即第一个点放在最后，最后一个点放在最前
参数：
	[in&out]vector<Point2i>& vecPoint - 改变顺序前后的点集	
返回：
	0 - 正常，其他 - 异常
*/
int DesendVector(vector<Point2i>& vecPoint);

// 计算平面内两点距离
float CalDistance(Point2i pt1, Point2i pt2);

long GetTime();

/*
功能：
	平移、过滤点
参数：
	[in]const vector<Point2i>& vecPoint - 转换、过滤前的点
	[out]vector<Point2i>& vecTranPoint - 转换、过滤后的点
返回：
	0 - 正常，其他 - 异常
备忘：
	此种情况适用于中心点为原点，目的是将原点平移为400x400图像的左上顶点
*/
int FilterPoint(const vector<Point2i>& vecPoint, vector<Point2i>& vecTranPoint);

/*
功能：
	计算两个向量的夹角
参数：
	[in]Point2i ptV1 - 向量V1
	[in]Point2i ptV2 - 向量V2
	[out]double& uAngle - 两向量的夹角，弧度制，逆时针为正
返回：
	0 - 正常，其他 - 异常
*/
int GetAngle(Point2i ptV1,Point2i ptV2, double& uAngle);

/*
功能：
	旋转图像
参数：
	[in]const Mat& matSrcI - 旋转前图像
	[in]Mat& matRotateI - 旋转后图像
	[out]double uAngle - 旋转角度，弧度制，逆时针为正
返回：
	0 - 正常，其他 - 异常
*/
int RotateMat(const Mat& matSrcI, Mat& matRotateI, double uAngle);

/*
功能：
	围绕中心点，旋转坐标
参数：
	[in]const vector<Point2i>& vecPoint - 旋转前坐标点
	[in]Point2i ptRotateCenter - 旋转中心
	[in]double uAngle - 旋转角度，弧度制，逆时针为正
	[out]vector<Point2i>& vecRotatePoint - 旋转后坐标点
返回：
	0 - 正常，其他 - 异常
*/
int RotateXY(const vector<Point2i>& vecPoint, Point2i ptRotateCenter, double uAngle, vector<Point2i>& vecRotatePoint);

/*
功能：
	围绕中心点，旋转坐标点集
参数：
	[in]const vector<vector<Point2i> >& vecPointSet - 旋转前坐标点集
	[in]Point2i ptRotateCenter - 旋转中心
	[in]double uAngle - 旋转角度，弧度制，逆时针为正
	[out]vector<vector<Point2i> >& vecRotatePointSet - 旋转后坐标点集
返回：
	0 - 正常，其他 - 异常
*/
int RotateXYSet(const vector<vector<Point2i> >& vecPointSet, Point2i ptRotateCenter, double uAngle, vector<vector<Point2i> >& vecRotatePointSet);

/*
功能：
	将线绘制于图像上
参数：
	[in]const vector<Point2i>& vecPoint - 要绘制到图像上的点
	[in]Scalar scColorV - 颜色值
	[in]int nLineW - 线宽
	[out]Mat& matPic - 绘制后的图像
返回：
	0 - 正常，其他 - 异常
*/
int DrawLine(const vector<Point2i>& vecPoint, Scalar scColorV, int nLineW, Mat& matPic);

/*
功能：
	将点绘制于图像上
参数：
	[in]const vector<Point2i>& vecPoint - 要绘制到图像上的点
	[in]Scalar scColorV - 颜色值
	[in]int nPointSize - 点大小
	[out]Mat& matPic - 绘制后的图像
返回：
	0 - 正常，其他 - 异常
*/
int DrawPoint(const vector<Point2i>& vecPoint, Scalar scColorV, int nPointSize, Mat& matPic);

/*
功能：
	绘制岔路
参数：
	[in]const vector<vector<Point2i>>& vecCrossRoadPoint - 岔路坐标
	[out]Mat& matCrossRoadLine - 岔路图像
返回：
	0 - 正常，其他 - 异常
*/
int DrawCrossRoadsLine(const vector<vector<Point2i> >& vecCrossRoadPoint,
	Mat& matCrossRoadLine);

/*
功能：
	合并图像，将多图合并为一大图，方便同一窗口显示
参数：
	[in]const vector<Mat>& vecMatImage - 需合并的图像集	
	[out]Mat& matMergeI - 合并后图像
返回：
	0 - 正常，其他 - 异常
备注：
	
*/
int MergeMat(const vector<Mat>& vecMatImages, Mat& matMergeImage);

#ifdef IS_WINDOWS_VER
// 读、写xml文件
int WriteXML();
int ReadXML(string str);


/*
功能：
	获取一幅路口放大图的主路屏幕点
参数：
	[in]TiXmlElement* pElement - 主路xml节点元素指针
	[out]vector<Point2i>& vecMainRoadPoint - 主路屏幕点
返回：
	0 - 正常，其他 - 异常
*/
int GetMainRoadScreenPoint(TiXmlElement* pElement, 
						   vector<Point2i>& vecMainRoadPoint);

/*
功能：
	获取节点的x、y坐标
参数：
	[in]TiXmlElement* pElement - 主路xml节点元素指针
	[out]Point2i& ptXY - 坐标点
返回：
	0 - 正常，其他 - 异常
*/
int GetNodeXY(TiXmlElement* pElement, Point2i& ptXY);
#endif


/*
功能：
	获取主路辅路
参数：
	[in]const Mat& matSrcImage - 导航图像，RGB图像
	[out]vector<Point2i>& vecMainRoadPt - 主路点
	[out]vector<vector<Point2i> >& vecAssistRoadPt - 辅路点
	[out]Mat& matMainRoadImageBw - 主路二值图
	[out]Mat& matDotLinesImageBw - 虚线点二值图
返回：
	0 - 正常，其他 - 异常
*/
/* int GetAllRoads(const Mat& matSrcImage, vector<Point2i>& vecMainRoadPt, vector<vector<Point2i> >& vecAssistRoadPt, 
				Mat& matMainRoadImageBw, Mat& matDotLinesImageBw); */

/*
功能：
	通过模板过滤图像
参数：
	[in]const Mat& matRoadBw - 导航道路图像，二值图像
	[out]Mat& matFilterImage - 过滤后的图像
返回：
	0 - 正常，其他 - 异常
*/
int FilterImageByTemplate(const Mat& matRoadBw, Mat& matFilterImage);

// 判断元素是否在vector中，true 在，false - 不在
bool IsBelongToVector(const vector<int> &vecInts, int nElement);

/*
功能：
	合并vector
参数：
	[in]const vector<vector<int>>& vecSrcSet - 合并前的vector集
	[out]vector<vector<int>>& vecMergeSet - 合并后的vector集
返回：
	0 - 正常，其他 - 异常
*/
int MergeVector(const vector<vector<int> >& vecSrcSet, vector<vector<int> >& vecMergeSet);

/*
功能：
	获取主路中心线
参数：
	[in]const Mat& matMainRoadBW - 主路二值图
	[out]vector<Point2i>& vecCenterLinePt - 主路中心线坐标点，起点到终点依次排序，终点对应箭头点
	[out]int& nCenterPointIndex - 中心点位置
返回：
	0 - 正常，1 - 无中心线，即图全黑，其他 - 异常
*/
//int GetMainRoadCenterLine(const Mat& matMainRoadBW, vector<Point2i>& vecCenterLinePt, int& nCenterPointIndex);

/*
功能：
	获取中心点坐标
参数：
	[in]vector<Point2i> vecPoints - 点集坐标
	[out]Point2i& ptCenter - 中心点坐标	
返回：
	0 - 正常，其他 - 异常
*/
//int GetCenterPt(const vector<Point2i>& vecPoints, Point2i& ptCenter);
