#ifndef CrossRoad_h
#define CrossRoad_h

#include <vector>
//#include "core.hpp"
#include "opencv2/core/core.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "types.h"
#include "LinkFileInfo.hpp"
#include "NaviFile.h"

using namespace std;
//using namespace cv;

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
		[in]int nCrossRoadLen - 渲染时绘制岔路的长度
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
						int nCrossRoadLen,
						std::vector<std::vector<HALocationCoordinate2D> >& vecCrossGpsLinks,
						std::vector<HALocationCoordinate2D>& vecMainRoadGpsInNet,
						std::vector<int>& vecCrossPointIndex,
						int& nCenterIndex);

	// 清空历史岔路起点
	void clearHistoryCrossPoint();


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

	/*
	功能：
		在指定文件夹中获取符合后缀要求的文件
	参数：
		[in]std::string strFolder - 文件夹
		[in]std::string strSuffix - 后缀名（如：hmd）
		[out]std::vector<std::string>& vecFileNames - 符合后缀要求的文件
	返回：
		0 - 正常，其他 - 异常
	*/
	int getSuffixFiles(std::string strFolder, std::string strSuffix, std::vector<std::string>& vecFileNames);

	
	/*
	功能：
		获取GPS点对应的字典文件名
	参数：
		[in]const std::vector<std::string>& vecFileNames - 符合后缀要求的文件		
		[out]std::string strDictFileName - 字典文件名
	返回：
		true - 获取成功，false - 获取失败
	*/
	bool getDictFileName(HALocationCoordinate2D halGpsCenterPoint,
						const std::vector<std::string>& vecFileNames,
						std::string& strDictFileName);

	

private:
	bool m_IsReadDictionary;		// 标记是否已读数字字典
	HaloNav m_haloNav;		// 记录数字字典映射
	vector<HAMapPoint> m_vecHistoryCrossPt;		// 记录历史岔路起点

public:
#ifdef _WINDOWS_VER_
	cv::Mat m_matImage;
#endif

};
#endif
