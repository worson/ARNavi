//
//  PlainLinkLineRecord.hpp
//  HaloAIMapData
//
//  Created by Liudao on 17/10/2016.
//  Copyright © 2016 HaloAI. All rights reserved.
//



// 融合高德与四维地图数据
#ifndef MergeMapData_hpp
#define MergeMapData_hpp

#define CENTER_COVER 400		// 中心点覆盖范围
#define NEAREST_POINT_DIS	30	// 临近点距离阈值
#define ANGLE_ALLOWANCE	45			// 角度夹角余量（角度制），用于判断两角度是否接近
#define VECTOR_NEAR_ANGLE	45		// 向量相近，角度余量
#define VECTOR_PARALLEL_ANGLE	20		// 向量平行，角度余量
#define EXTEND_ROAD_ANGLE	25		// 角度阈值，用于拓展link
#define KEYPOINT_COSV_TH		0.75	// 寻找主路关键点，余弦阈值	

#define CROSSROAD_LENGTH	200		// 岔路长度

#define IS_DRAW		0		// 是否绘图，1-是，0-否
#define IS_DRAW1	0		// 是否绘图，1-是，0-否
#define IS_DRAW2	1		// 是否绘图，1-是，0-否
#define IS_SON_DRAW 0		// 是否显示图像，1-是，0-否
#define IS_DRAW_NODE 0		// 是否绘制节点图像，1-是，0-否
#define IS_DRAW_ROADNET 0		// 是否绘制路网图像，1-是，0-否
#define IS_PRINT_LOG	0	// 是否打印log

// 定义link方向
#define UNCERTAIN_DIRECTION 0	// 不确定
#define DOUBLE_DIRECTION	1	// 双向
#define SAME_DIRECTION	2	// 同向
#define OPPOSITE_DIRECTION 3	//反向



#include <vector>
#include "LinkFileInfo.hpp"
//#include "core.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/core/core.hpp"

#ifdef _WINDOWS_VER_
	#define LOGD(...) ((void)printf(__VA_ARGS__))
#else
	#include <android/log.h>
	//#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "Algorithm", __VA_ARGS__)
	#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_ERROR, "Algorithm", __VA_ARGS__))
#endif


using namespace std;
//using namespace cv;

// 定义直线类型，a*x + b*y + c = 0
struct Line  
{  
	cv::Point2d pt1,pt2;  
	double a,b,c;  
};  

// 定义节点
struct LinkEndPointNode
{
	HAMapPoint hamEndPoint;		// link端点
	vector<int> vecLinkId;		// 邻居点与端点对应link的Id
	vector<HAMapPoint> vecNeighborPoint;	// 邻居点
	vector<int> vecDirection;		// 方向，端点为起点，与道路方向保持一致，道路方向:0未调查,默认双向,1双向,2正方向(link的起点到终点),3反方向
	vector<int> vecNeighborNodeId;		// 邻居node Id
	int nNodeId;	// node Id
};

class MergeMapData{
public:
    MergeMapData();
    ~MergeMapData();
   
	
	/*
	功能：
		在路网中寻找主路，即在四维数据中寻找与高德主路匹配的路径
	参数：
		[in]const vector<HAMapPoint>& vecMainRoad - 主路数据（高德地图数据），单位：像素
		[in]HAMapPoint haMainRoadCenterPt - 主路中心点
		[in]const vector<LinkInfo>& vecRoadNetLinkInfo - 路网link信息（四维地图数据）
		[in]const std::vector<std::vector<HAMapPoint> >& vecRoadNetLink - 路网link
		[out]vector<HAMapPoint> vecMainRoadinRoadNet - 路网上对应的主路，vecRoadNet子集
	返回：
		0 - 正常，其他 - 异常
	*/
	int findMainRoadInRoadNet(const vector<HAMapPoint>& vecMainRoad,
										HAMapPoint haMainRoadCenterPt,
										const vector<LinkInfo>& vecRoadNetLinkInfo,
										const std::vector<std::vector<HAMapPoint> >& vecRoadNetLink,
										vector<LinkInfo>& vecOutLinkInfo,
										vector<vector<HAMapPoint> >& vecOutLink,
										vector<HAMapPoint>& vecMainRoadinNet);

	/*
	功能：
		寻找拐点
	参数：
		[in]const vector<Point2i>& vecPointSet - 点集	
		[out]vector<Point2i>& ptBreakPoint - 拐点集，包括首尾端点
		[out]vector<int>& vecBreakPointID - 拐点在原集合中的编号，包括首尾端点
		[out]vector<Vec4f>& vecLines - 直线方向，Vec4f - [dx,dy,x0,y0]，(dx,dy)表示方向单位向量，模长等于1
		[out]Vec4f& vefPreCenterLine - 中心点与前一个拐点的方向
		[out]Point2i& PtPreCenter - 中心点与前一个拐点连线上的一个点，用于计算方向向量
	返回：
		0 - 正常，其他 - 异常
	*/
	int getBreakPoint(const vector<cv::Point2i>& vecPointSet, 
					  cv::Point2i haCenterPt,
					  vector<cv::Point2i>& vecBreakPoint, 
					  vector<int>& vecBreakPointID,
					  vector<cv::Vec4f>& vecLines,
					  cv::Vec4f& vefPreCenterLine,
					  cv::Point2i& ptPreCenter);

	/*
	功能：
		在路网中匹配主路中心点
	参数：
		[in]const vector<HAMapPoint>& vecMainRoad - 主路点集
		[in]HAMapPoint haMainRoadCenterPt - 主路中心点，单位：像素
		[in]const vector<LinkInfo>& vecRoadNetLinkInfos - 路网linkInfo集
		[in]const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks - 路网link集
		[out]HAMapPoint& hamCenterInNet - 主路中心点的匹配点
		[out]vector<LinkInfo>& vecMainRoadLinkInfosInNet - 主路匹配线对应的linkInfo集
		[out]std::vector<std::vector<HAMapPoint> >& vecMainRoadLinksInNet - 主路匹配线对应的link集
	返回：
		0 - 正常，其他 - 异常
	*/
	int matchMainRoadCenterInNet(const vector<HAMapPoint>& vecMainRoad,
								HAMapPoint haMainRoadCenterPt,
								const vector<LinkInfo>& vecRoadNetLinkInfos,
								const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
								HAMapPoint& hamCenterInNet,
								vector<LinkInfo>& vecMainRoadLinkInfosInNet,
								std::vector<std::vector<HAMapPoint> >& vecMainRoadLinksInNet);

	// 利用角度匹配中心点
	int matchMainRoadCenterInNet1(const vector<HAMapPoint>& vecMainRoad,
								HAMapPoint haMainRoadCenterPt,
								const vector<LinkInfo>& vecRoadNetLinkInfos,
								const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
								HAMapPoint& hamCenterInNet,
								vector<LinkInfo>& vecMainRoadLinkInfosInNet,
								std::vector<std::vector<HAMapPoint> >& vecMainRoadLinksInNet);

	// 利用距离匹配中心点
	int matchMainRoadCenterInNet2(const vector<HAMapPoint>& vecMainRoad,
								HAMapPoint haMainRoadCenterPt,
								const vector<LinkInfo>& vecRoadNetLinkInfos,
								const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
								HAMapPoint& hamCenterInNet,
								vector<LinkInfo>& vecMainRoadLinkInfosInNet,
								std::vector<std::vector<HAMapPoint> >& vecMainRoadLinksInNet);

	// 利用整个屏幕内的主路起点、中心点、终点进行匹配
	int matchMainRoadCenterInNet3(const vector<HAMapPoint>& vecMainRoad,
								HAMapPoint haMainRoadCenterPt,
								const vector<LinkInfo>& vecRoadNetLinkInfos,
								const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
								cv::Rect rtScreen,
								HAMapPoint& hamCenterInNet,
								vector<LinkInfo>& vecMainRoadLinkInfosInNet,
								std::vector<std::vector<HAMapPoint> >& vecMainRoadLinksInNet,
								std::vector<HAMapPoint>& vecMainRoadPtInNet,
								int& nMatchCenterIndex,	// 中心点匹配点在vecMainRoadPtInNet的下标
								std::vector<int>& vecCrossPointIndex,		// 主路与岔路交点在主路中的下标
								std::vector<std::vector<HAMapPoint> >& vecCrossGpsLinks);

	int matchMainRoadCenterInNet4(const vector<HAMapPoint>& vecMainRoad,
								HAMapPoint haMainRoadCenterPt,
								const vector<LinkInfo>& vecRoadNetLinkInfos,
								const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
								cv::Rect rtScreen,
								HAMapPoint& hamCenterInNet,
								vector<LinkInfo>& vecMainRoadLinkInfosInNet,
								std::vector<std::vector<HAMapPoint> >& vecMainRoadLinksInNet,
								std::vector<HAMapPoint>& vecMainRoadPtInNet,
								int& nMatchCenterIndex,	// 中心点匹配点在vecMainRoadPtInNet的下标
								std::vector<int>& vecCrossPointIndex,
								std::vector<std::vector<HAMapPoint> >& vecCrossGpsLinks);

	int matchMainRoadCenterInNet5(const vector<HAMapPoint>& vecMainRoad,
								HAMapPoint haMainRoadCenterPt,
								const vector<LinkInfo>& vecRoadNetLinkInfos,
								const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,								
								cv::Rect rtScreen,
								int nCrossRoadLen,											
								HAMapPoint& hamCenterInNet,
								vector<LinkInfo>& vecMainRoadLinkInfosInNet,
								std::vector<std::vector<HAMapPoint> >& vecMainRoadLinksInNet,
								std::vector<HAMapPoint>& vecMainRoadPtInNet,
								int& nMatchCenterIndex,	// 中心点匹配点在vecMainRoadPtInNet的下标
								std::vector<int>& vecCrossPointIndex,
								std::vector<std::vector<HAMapPoint> >& vecCrossPathPt,
								vector<HAMapPoint>& vecHistoryCrossPt);		// 历史岔路起点[in/out]

	int matchMainRoadCenterInNet6(const vector<HAMapPoint>& vecMainRoad,
								HAMapPoint haMainRoadCenterPt,
								const vector<LinkInfo>& vecRoadNetLinkInfos,
								const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
								cv::Rect rtScreen,
								int nCrossRoadLen,
								HAMapPoint& hamCenterInNet,
								vector<LinkInfo>& vecMainRoadLinkInfosInNet,
								std::vector<std::vector<HAMapPoint> >& vecMainRoadLinksInNet,
								std::vector<HAMapPoint>& vecMainRoadPtInNet,
								int& nMatchCenterIndex,	// 中心点匹配点在vecMainRoadPtInNet的下标
								std::vector<int>& vecCrossPointIndex,		// 岔路与主路交点在主路中的下标
								std::vector<std::vector<HAMapPoint> >& vecCrossPathPt);

	// 取中心点一定范围内的主路子集，并按主路顺序排列
	int getSubMainRoadNearCenter(const vector<HAMapPoint>& vecMainRoad,												   
								int nCenterSite,
								cv::Size2i szOffset,
								vector<HAMapPoint>& vecSubMainRoad);

	// 将主路与路网另一子路的接近程度，用距离和表示
	int getMainSubLine2SubNetDis(const vector<HAMapPoint>& vecSubMainRoad, 
								int nCenterSite,
								HAMapPoint hamPrePtInRoadNet,
								HAMapPoint hamFixPtInRoadNet,
								HAMapPoint hamNextPtInRoadNet,
								double& uError);

	// 获取直线上两点之间的所有点
	int getPointsBetweenTwoPoints(HAMapPoint hamStartPoint,HAMapPoint hamEndPoint, 
								vector<HAMapPoint>& vecPointsInLine);

	// 获取直线参数
	void getLinePara(Line *l);

	// 获取两直线交点
	cv::Point2d getCrossPoint(Line *l1,Line *l2); 

	// 计算点到直线的距离
	double getDistancePoint2Line(cv::Point2i pt, Line lin);

	// 计算两点距离
	template<typename T>
	float getDistancePoint2Point(T tX1,T tY1,T tX2, T tY2);
		
	/*
	功能：
		构造link端点节点
	参数：
		[in]const std::vector<std::vector<HAMapPoint> >& vecLink - link集	
		[in]const vector<LinkInfo>& vecLinkInfos -linkInfo集	
		[out]vector<LinkEndPointNode>& vecLinkEndPtnode - 端点节点集
	返回：
		0 - 正常，其他 - 异常
	*/
	int formLinkEndPointNode(const std::vector<std::vector<HAMapPoint> >& vecLink,
							const vector<LinkInfo>& vecLinkInfos,
							vector<LinkEndPointNode>& vecLinkEndPtnode);

	
	// 求某一个指定点在集合中的位置
	int getPointSite(const vector<HAMapPoint>& vecHamPts,
					HAMapPoint hamPt,
					int& nSite);

	// 求集合中指定位置向前或向后满足距离要求的最近点
	int getNearestPoint(const vector<HAMapPoint>& vecHamPts,
						int nSite,
						bool bIsPre,
						HAMapPoint& hamNearestPt);

	// 求两向量夹角
	float getAngle(cv::Vec2f v1, cv::Vec2f v2);

	//// 求均值和方差
	//int getStatistic(const vector<double>& vecDisSet, double& uMean, double& uStd);

	// 在link中指定点向前或向后依次寻找离指定点最近且距离大于固定值的点
	int findNeighborPoint(const std::vector<HAMapPoint>& vecPt, int nSi, int nDisDel, 
		bool bIsFront, HAMapPoint& hamResultPt);


	// 寻找主路进入屏幕的起点、中心点、终点
	int findKeyPoint(const vector<HAMapPoint>& vecMainRoad,
					HAMapPoint haMainRoadCenterPt,
					cv::Rect rtScreen,
					vector<HAMapPoint>& vecKeyPoint);

	// 获取屏幕边界上的主路起点、终点
	int getStartEndPoint(const vector<HAMapPoint>& vecMainRoad,
						int nCenterSite,
						cv::Rect rtScreen,
						HAMapPoint& hamStartPt,
						HAMapPoint& hamEndPt);

	// 延伸link直到超出屏幕边界
	int extendLink(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLink,
					const vector<LinkEndPointNode> vecLinkEndPtnode,
					int nCurEndPtSite,
					cv::Rect rtScreen,
					vector<HAMapPoint>& vecBorderPt,
					vector<int>& vecBorderPtDirection,
					vector<vector<int> >& vecPathLinkId,
					vector<vector<int> >& vecPathNodeIdInNet);

	int extendLink1(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLink,
					const vector<LinkEndPointNode> vecLinkEndPtnode,
					int nCurEndPtSite,
					cv::Rect rtScreen,
					vector<HAMapPoint>& vecBorderPt,
					vector<int>& vecBorderPtDirection,
					vector<vector<int> >& vecPathLinkIdInNet,
					vector<vector<int> >& vecPathNodeIdInNet);

	// 由一个点构造路网
	int formRoadNet(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
					const vector<LinkEndPointNode> vecAllEndPtnode,							  
					const vector<int>& vecMainRoadNodeId,
					cv::Rect rtScreen,
					vector<int>& vecRoadNetDirection2MainRoad);

	int formRoadNet1(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
					const vector<LinkInfo>& vecLinkInfos,
					const vector<LinkEndPointNode> vecAllEndPtnode,							  
					const vector<int>& vecMainRoadNodeId,
					cv::Rect rtScreen,
					vector<int>& vecRoadNetDirection2MainRoad);

	// 过滤link
	int formRoadNet2(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
					const vector<LinkInfo>& vecLinkInfos,
					const vector<LinkEndPointNode> vecAllEndPtnode,							  
					const vector<int>& vecMainRoadNodeId,
					int nMatchCenterSite,		// 匹配点在vecMainRoadNodeId中的位置
					cv::Rect rtScreen,
					vector<int>& vecRoadNetDirection2MainRoad);

	int formRoadNet3(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
					const vector<LinkInfo>& vecLinkInfos,
					const vector<LinkEndPointNode> vecAllEndPtnode,							  
					const vector<int>& vecMainRoadNodeId,
					int nMatchCenterSite,		// 匹配点在vecMainRoadNodeId中的位置
					cv::Rect rtScreen,
					vector<int>& vecRoadNetDirection2MainRoad,
					vector<vector<int> >& vecCrossPathLinkID,		// 与主路相交的每条岔路的link Id
					vector<vector<int> >& vecCrossPathNodeId);

	int formRoadNet4(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
					const vector<LinkInfo>& vecLinkInfos,
					const vector<LinkEndPointNode> vecAllEndPtnode,							  
					const vector<int>& vecMainRoadNodeId,
					int nMatchCenterSite,		// 匹配点在vecMainRoadNodeId中的位置
					cv::Rect rtScreen,
					vector<int>& vecRoadNetDirection2MainRoad,
					vector<vector<int> >& vecCrossPathLinkID,		// 与主路相交的每条岔路的link Id
					vector<vector<int> >& vecCrossPathNodeId);

	// 过滤link，删除历史上已出现的岔路，并更新历史岔路起点
	int formRoadNet5(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
					const vector<LinkInfo>& vecLinkInfos,
					const vector<LinkEndPointNode> vecAllEndPtnode,							  
					const vector<int>& vecMainRoadNodeId,
					int nMatchCenterSite,
					cv::Rect rtMainRoadScreen,		// 主路窗口
					cv::Rect rtCrossRoadScreen,		// 岔路窗口
					vector<int>& vecRoadNetDirection2MainRoad,
					vector<vector<int> >& vecCrossPathLinkID,
					vector<vector<int> >& vecCrossPathNodeId,
					vector<HAMapPoint>& vecHistoryCrossPt);		// 历史岔路起点[in/out]

	// 判断点是否在矩形范围内
	bool isRectInside(HAMapPoint hamPoint, cv::Rect rect);

	// 计算link内部点按序连成的折线与已知直线的交点
	int getCrossPointLink2Line(const std::vector<HAMapPoint>& vecLinkPt,
								Line ln, HAMapPoint& hamCrossPt);

	// 计算link内部点按序连成的折线与矩形框边界的交点
	int getCrossPointLink2Rect(const std::vector<HAMapPoint>& vecLinkPt,
								cv::Rect rt, HAMapPoint& hamCrossPt);
		
	// 过滤路网
	int filterRoadNet(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
					const vector<int>& vecRoadNetDirection2MainRoad,
					const vector<int>& vecMainRoadLinkId,
					std::vector<std::vector<HAMapPoint> >& vecCrossGpsLinks);

	// 过滤路网，不包括主路
	int filterRoadNet1(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
						const vector<LinkEndPointNode> vecAllEndPtnode,
						int nCrossRoadLen,		// 岔路长度
						const vector<vector<int> >& vecCrossPathLinkID,		// 与主路相交的每条岔路的link Id
						const vector<vector<int> >& vecCrossPathNodeId,
						cv::Rect rtScreen,
						std::vector<std::vector<HAMapPoint> >& vecCrossGpsLinks);

	
	// 计算从匹配路径移至主路的最佳平移距离(包括正负)
	int getOffset2MainRoad(const vector<HAMapPoint>& vecMainRoadVertex,
						const vector<HAMapPoint>& vecMatchRoadVertex,
						HAMapPoint& hamOffset);

	// 平移路网
	int translateRoadNet(std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
		HAMapPoint hamOffset);

	// 遍历图，获取边界点
	// bool bIsFrontTrave - 表示遍历方向，true - 朝正方向遍历，false - 朝反方向遍历
	int traverseMap(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLink,
					const vector<LinkEndPointNode> vecLinkEndPtnode,
					int nCurEndPtSite,
					cv::Rect rtScreen,
					bool bIsFrontTrave,
					vector<HAMapPoint>& vecBorderPt,							  
					vector<vector<int> >& vecPathLinkIdInNet,
					vector<vector<int> >& vecPathNodeIdInNet);

	// 遍历图，获取边界点
	// bool bIsFrontTrave - 表示遍历方向，true - 朝正方向遍历，false - 朝反方向遍历
	int traverseMap1(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLink,
					const vector<LinkEndPointNode> vecLinkEndPtnode,
					int nCurEndPtSite,
					cv::Rect rtScreen,
					bool bIsFrontTrave,
					vector<HAMapPoint>& vecBorderPt,							  
					vector<vector<int> >& vecPathLinkIdInNet,
					vector<vector<int> >& vecPathNodeIdInNet);

	// 判断元素是否在vector中，true 在，false - 不在
	bool isBelongToVector(const vector<int> &vecInts, int nElement, int& nSite);

	// 判断点是否属于集合
	bool isHamapPtBelongToSet(vector<HAMapPoint>& vecHamPt, HAMapPoint hamPt, int& nSite);

	//template<typename T>
	//bool isEelmentBelongToSet(vector<T>& vecElement, T element, int& nSite);

	// 路径回溯
	int popPath(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLink,
				const vector<LinkEndPointNode> vecLinkEndPtnode,
				const vector<vector<int> >& vecIsNodeDo, cv::Rect rtScreen, bool bIsFrontTrave,
				vector<int>& vecPathNodeId, vector<int>& vecPathLinkId);

	// 基于两个端点，查找Link Id
	int getLinkInfo(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLink,
					HAMapPoint hamEndPt1, HAMapPoint hamEndPt2,
					int& nLinkId);

	// 基于路径的Node Id，获取路径的Link Id
	int getPathLinkId(const vector<LinkEndPointNode> vecLinkEndPtnode,
						const vector<int>& vecPathNodeId,
						vector<int>& vecPathLinkId);

	// 按顺序计算vector内部相邻点间的距离和
	int getDistanceInsideVector(const vector<HAMapPoint>& vecHamPt, double& uDis);

	// 获取边界点到匹配点的距离
	int getDisBorder2MatchPt(HAMapPoint hamBorderPt, HAMapPoint hamMatchPt, double uDis);
	
	// 获取边界点到匹配点的距离
	int getDisBorder2MatchPt(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLink,
							const vector<int>& vecPathLinkId, 
							cv::Rect rtScreen,
							HAMapPoint hamBorderPt, 
							HAMapPoint hamMatchPt, 
							double& uDis);

	// vector倒序重排，第1个放到最后，最后一个放到第1
	template<typename T>
	void reverseOrder(vector<T>& vecSet);

	// 延伸同名路
	int extendSameNameRoad(const vector<LinkInfo>& vecRoadNetLinkInfo,
							const vector<LinkEndPointNode> vecLinkEndPtnode,
							const vector<int>& vecMainRoadLinkId, 
							int nCurLinkId, int nCurNodeId, cv::Rect rtScreen,		
							vector<int>& vecExtendLinksId, vector<int>& vecExtendNodesId);

	// 延伸道路
	int extendRoad(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
					const vector<LinkInfo>& vecRoadNetLinkInfo,
					const vector<LinkEndPointNode> vecLinkEndPtnode,
					const vector<int>& vecMainRoadLinkId, 
					int nCurDirection,	int nCurLinkId, int nCurNodeId, cv::Rect rtScreen,
					vector<int>& vecExtendLinksId, vector<int>& vecExtendNodesId,
					vector<int>& vecExtendLinkDirection);

#ifdef _WINDOWS_VER_
	void drawImage(cv::Mat& matNavi,HAMapPoint hptCenter, vector<HAMapPoint> hamPts, 
		cv::Scalar scColor,int nLineWidth=2);
	void drawNavi2(cv::Mat& matNavi,HAMapPoint hptCenter, vector<HAMapPoint> mptGPSSet,
		std::vector<LinkInfo>& vecLinkInfos, std::vector< std::vector<HAMapPoint> > & vecAxes);

	// 图像上显示字符，方便查看结构关系
	void drawNode(cv::Mat matImg, const vector<LinkEndPointNode> vecLinkEndPtnode,
				const std::vector<std::vector<HAMapPoint> >& vecRoadNetLink);

	// 绘制路网
	void drawRoadNet(const std::vector< std::vector<HAMapPoint> >& vecRoadNetLink,
		cv::Size2i szCover,	HAMapPoint hamPixelCenter,cv::Mat& matRoadNetImg);
#endif

public:
	#ifdef _WINDOWS_VER_
		HAMapPoint m_hamMainRoadCenter;
		cv::Mat m_matImage;
		string m_strImageName;
		
		string m_strErrorLog;
	#endif
    
public:
    cv::Point m_ptOffset;
};
#endif /* MergeMapData_hpp */
