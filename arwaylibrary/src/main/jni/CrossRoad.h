#pragma once

#include <vector>
//#include "core.hpp"
#include "opencv2/core/core.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "types.h"
#include "LinkFileInfo.hpp"
#include "NaviFile.h"

using namespace std;
using namespace cv;

class CrossRoad
{
public:
	CrossRoad(void);
	~CrossRoad(void);

	/*
	功能：
		获取路网数据
	参数：
		[in]const std::vector<std::vector<HALocationCoordinate2D> >& vecMainRoadlinks - 主路link集(高德地图经纬度数据）
		[in]const vector<vector<LinkInfo>>& vecMainRoadLinkInfos - 主路linkInfo集		
		[in]HALocationCoordinate2D halCenterPoint - 主路中心点经纬度坐标
		[in]cv::Size2i szCover - 需获取路网数据的尺寸范围
		[in]string strDictPath - 字典路径
		[out]vector<vector<HALocationCoordinate2D> >& vecCrossGpsLinks - 路网数据，主路除外
		[out]std::vector<HALocationCoordinate2D>& vecMainRoadGpsInNet - 路网中匹配的主路
		[out]std::vector<int>& vecCrossPointIndex - 主路与岔路交点在主路中的下标
		[out]int& nCenterIndex - 匹配点在主路中的位置
	返回：
		0 - 正常，其他 - 异常
	*/
	int getCrossLinks(const std::vector<std::vector<HALocationCoordinate2D> >& vecMainRoadGpslinks,
					const std::vector<LinkInfo>& vecMainRoadGpsLinkInfos,					
					HALocationCoordinate2D halGpsCenterPoint,
					cv::Size2i szCover,
					string strDictPath,					
					std::vector<std::vector<HALocationCoordinate2D> >& vecCrossGpsLinks,
					std::vector<HALocationCoordinate2D>& vecMainRoadGpsInNet,
					std::vector<int>& vecCrossPointIndex,
					int& nCenterIndex);

	// 读取字典数据


private:
	/*
	功能：
		像素坐标转gps坐标
	参数：
		[in]const std::vector<HAMapPoint> vecPixelPoint - 像素坐标点集
		[in]HAMapPoint hamOffset - 偏移量		
		[out]std::vector<HALocationCoordinate2D>& vecGpsPoint - 转换后坐标
	返回：
		0 - 正常，其他 - 异常
	*/
	int pixel2Gps(const std::vector<HAMapPoint> vecPixelPoint,
				HAMapPoint hamOffset,
				std::vector<HALocationCoordinate2D>& vecGpsPoint);
private:
	bool m_IsReadDictionary;		// 标记是否已读数字字典
	HaloNav m_haloNav;		// 记录数字字典映射

public:
#ifdef _WINDOWS_VER_
	Mat m_matImage;
#endif

};

