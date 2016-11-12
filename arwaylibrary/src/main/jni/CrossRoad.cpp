#include "CrossRoad.h"
#include "converter.h"
#include "NaviFile.h"
#include "MergeMapData.hpp"

CrossRoad::CrossRoad(void)
{
	m_IsReadDictionary = false;
}


CrossRoad::~CrossRoad(void)
{
}

// 获取路网数据
int CrossRoad::getCrossLinks(const std::vector<std::vector<HALocationCoordinate2D> >& vecMainRoadGpslinks,
							 const std::vector<LinkInfo>& vecMainRoadGpsLinkInfos,					
							 HALocationCoordinate2D halGpsCenterPoint,
							 cv::Size2i szCover,
							 string strDictPath,							 
							 std::vector<std::vector<HALocationCoordinate2D> >& vecCrossGpsLinks,
							 std::vector<HALocationCoordinate2D>& vecMainRoadGpsInNet,
							 std::vector<int>& vecCrossPointIndex,
							 int& nCenterIndex)
{
#ifdef _WINDOWS_VER_
	printf("===========getCrossLinks - enter!!=========\n");
#else
	LOGD("===========getCrossLinks - enter!!=========\n");	
#endif
	// 参数自检
	int nNumLink = vecMainRoadGpslinks.size();
	if (nNumLink<=0 || nNumLink!=vecMainRoadGpsLinkInfos.size() ||
		szCover.height<=0 || szCover.width<=0)
	{
		return -1;
	}

#if IS_PRINT_LOG
	#ifdef _WINDOWS_VER_
		printf("getCrossLinks - vecMainRoadGpslinks: \n");
		printf("	vecMainRoadGpslinks.size=%d\n", vecMainRoadGpslinks[0].size());		
		for (int i=0; i<vecMainRoadGpslinks[0].size(); i++)
		{
			printf("%lf, %lf;  ", vecMainRoadGpslinks[0][i].latitude, vecMainRoadGpslinks[0][i].longitude);		
		}
		printf("\n	szCover.w = %d, szCover.h = %d\n", szCover.width, szCover.height);	
		printf("halGpsCenterPoint.latitude=%lf, halGpsCenterPoint.longitude=%lf\n",halGpsCenterPoint.latitude, halGpsCenterPoint.longitude);
	#else
		LOGD("getCrossLinks - vecMainRoadGpslinks: \n");
		LOGD("	vecMainRoadGpslinks.size=%d\n", vecMainRoadGpslinks[0].size());		
		for (int i=0; i<vecMainRoadGpslinks[0].size(); i++)
		{
			LOGD("%lf, %lf;  ", vecMainRoadGpslinks[0][i].latitude, vecMainRoadGpslinks[0][i].longitude);		
		}
		LOGD("\n	szCover.w = %d, szCover.h = %d\n", szCover.width, szCover.height);	
		LOGD("halGpsCenterPoint.latitude=%lf, halGpsCenterPoint.longitude=%lf\n",halGpsCenterPoint.latitude, halGpsCenterPoint.longitude);		
	#endif
#endif

	int nRet = 0;

	// 读取字典数据
	if (!m_IsReadDictionary)
	{
		nRet = m_haloNav.readDictionary(strDictPath);
		if (nRet<0)
		{
			return -1;
		}
		m_IsReadDictionary = true;
	}
	
#ifdef _WINDOWS_VER_
	printf("===========getCrossLinks - readDictionary end!!=========\n");
#else
	LOGD("===========getCrossLinks - readDictionary end!!=========\n");
#endif
	// 坐标转换，主路经纬度转像素
	HAMapPoint hamOffset = m_haloNav.getOffset();		// 偏移量
	vector<vector<HAMapPoint> > vecMainRoadPixelLinks;
	vector<HAMapPoint> vecMainRoadPixelPt;
	for (int i=0; i<nNumLink; i++)
	{
		vector<HALocationCoordinate2D> vecLink = vecMainRoadGpslinks[i];
		int nNumPt = vecLink.size();
		HAMapPoint hamPixelXY;
		vector<HAMapPoint> vecTemp;
		for (int j=0; j<nNumPt; j++)
		{			
			hamPixelXY = HAMapPointForCoordinate(vecLink[j]);
			hamPixelXY.x -= hamOffset.x;		// 减去偏移量
			hamPixelXY.y -= hamOffset.y;
			vecTemp.push_back(hamPixelXY);
			vecMainRoadPixelPt.push_back(hamPixelXY);
		}
		vecMainRoadPixelLinks.push_back(vecTemp);
	}

	// 中心点
	HAMapPoint hamPixelCenter = HAMapPointForCoordinate(halGpsCenterPoint);
	hamPixelCenter.x -= hamOffset.x;
	hamPixelCenter.y -= hamOffset.y;
#ifdef _WINDOWS_VER_	
	printf("===========getCrossLinks - HAMapPointForCoordinate end!!=========\n");	
#else
	LOGD("===========getCrossLinks - HAMapPointForCoordinate end!!=========\n");	
#endif
	
	// 获取路网link	
	std::vector<LinkInfo> vecRoadNetLinkInfo;		
	std::vector<std::vector<HAMapPoint> > vecRoadNetLink;
	nRet = m_haloNav.findLinks(hamPixelCenter,szCover.width,szCover.height,vecRoadNetLinkInfo,vecRoadNetLink);
	if (nRet<0)
	{
		return -1;
	}

#if IS_PRINT_LOG
	#ifdef _WINDOWS_VER_
		printf("	vecRoadNetLink.size=%d\n", vecRoadNetLink.size());		
	#else
		LOGD("	vecRoadNetLink.size=%d\n", vecRoadNetLink.size());	
	#endif
#endif

#ifdef _WINDOWS_VER_
	printf("===========getCrossLinks - findLinks end!!=========\n");
#else
	LOGD("===========getCrossLinks - findLinks end!!=========\n");
#endif
	
	// 在路网中查找主路，融合地图数据
	MergeMapData merMapdata;
#ifdef _WINDOWS_VER_
	merMapdata.m_matImage.create(szCover.height,szCover.width,CV_8UC3);
#endif

	HAMapPoint hamCenterInNet;
	vector<LinkInfo> vecMainRoadLinkInfosInNet;
	std::vector<std::vector<HAMapPoint> > vecMainRoadLinksInNet;
	std::vector<HAMapPoint> vecMainRoadPixelInNet;
	std::vector<std::vector<HAMapPoint> > vecCrossPixelLinks;
	//std::vector<int> vecCrossPointIndex;
	int nScrW = szCover.width;
	int nScrH = szCover.height;
	cv::Rect rtScreen(hamPixelCenter.x-nScrW/2, hamPixelCenter.y-nScrH/2,nScrW,nScrH);	
	nRet = merMapdata.matchMainRoadCenterInNet5(vecMainRoadPixelPt,
												hamPixelCenter,
												vecRoadNetLinkInfo,
												vecRoadNetLink,
												rtScreen,
												hamCenterInNet,
												vecMainRoadLinkInfosInNet,
												vecMainRoadLinksInNet,
												vecMainRoadPixelInNet,
												nCenterIndex,
												vecCrossPointIndex,
												vecCrossPixelLinks);
	if (nRet<0)
	{
		#ifdef _WINDOWS_VER_
			m_matImage = merMapdata.m_matImage.clone();
		#endif
		return -1;
	}

#if IS_PRINT_LOG
	#ifdef _WINDOWS_VER_
		printf("	getCrossLinks - vecCrossPixelLinks\n");	
		printf("	getCrossLinks: vecCrossPixelLinks.size=%d\n", vecCrossPixelLinks.size());		
	#else
		LOGD("	getCrossLinks - vecCrossPixelLinks\n");	
		LOGD("	getCrossLinks: vecCrossPixelLinks.size=%d\n", vecCrossPixelLinks.size());		
	#endif
#endif

#ifdef _WINDOWS_VER_
	printf("===========getCrossLinks - matchMainRoadCenterInNet5 end!!=========\n");
#else
	LOGD("===========getCrossLinks - matchMainRoadCenterInNet5 end!!=========\n");
#endif
	// 坐标转换，像素转经纬度
#if 0
	int nNumRoadNetLink = vecRoadNetLink.size();
	vecCrossGpsLinks.clear();
	
	for (int i=0; i<nNumRoadNetLink; i++)
	{
		vector<HAMapPoint> vecPixelTemp = vecRoadNetLink[i];
		vector<HALocationCoordinate2D> vecGpsTemp;
		int nNumTempPt = vecPixelTemp.size();
		HALocationCoordinate2D halTempGpsPt;
		for (int j=0; j<nNumTempPt; j++)
		{
			HAMapPoint hamPixelTemp = vecPixelTemp[j];
			hamPixelTemp.x += hamOffset.x;
			hamPixelTemp.y += hamOffset.y;

			halTempGpsPt = HACoordinateForMapPoint(hamPixelTemp/*vecPixelTemp[j]*/);
			vecGpsTemp.push_back(halTempGpsPt);
		}
		vecCrossGpsLinks.push_back(vecGpsTemp);
	}
#else
	// 岔路转换
	int nCrossLinkNum = vecCrossPixelLinks.size();
	vecCrossGpsLinks.clear();
	for (int i=0; i<nCrossLinkNum; i++)
	{
		vector<HAMapPoint> vecPixelTemp = vecCrossPixelLinks[i];
		vector<HALocationCoordinate2D> vecGpsTemp;
		
		nRet = pixel2Gps(vecPixelTemp,	hamOffset, vecGpsTemp);
		if (nRet < 0)
		{
			return -1;
		}
		
		vecCrossGpsLinks.push_back(vecGpsTemp);
	}

	// 主路转换
	nRet = pixel2Gps(vecMainRoadPixelInNet,	hamOffset, vecMainRoadGpsInNet);
	if (nRet < 0)
	{
		return -1;
	}
#endif

#if IS_PRINT_LOG
	#ifdef _WINDOWS_VER_
		printf("	getCrossLinks: vecCrossGpsLinks.size=%d, vecMainRoadGpsInNet.size=%d\n",
			vecCrossGpsLinks.size(), vecMainRoadGpsInNet.size());		
	#else
		LOGD("	getCrossLinks: vecCrossGpsLinks.size=%d, vecMainRoadGpsInNet.size=%d\n",
			vecCrossGpsLinks.size(), vecMainRoadGpsInNet.size());	
	#endif
#endif

#ifdef _WINDOWS_VER_
	m_matImage = merMapdata.m_matImage.clone();
	printf("===========getCrossLinks - leave!!=========\n");
	printf("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
#else
	LOGD("===========getCrossLinks - leave!!=========\n\n");
	LOGD("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
#endif
	return 0;
}


int CrossRoad::pixel2Gps(const std::vector<HAMapPoint> vecPixelPoint,
						 HAMapPoint hamOffset,
						 std::vector<HALocationCoordinate2D>& vecGpsPoint)
{
	// 参数自检
	int nNumPt = vecPixelPoint.size();
	if (nNumPt<=0)
	{
		return -1;
	}

	HALocationCoordinate2D halTempGpsPt;
	for (int i=0; i<nNumPt; i++)
	{
		HAMapPoint hamPixelTemp = vecPixelPoint[i];
		hamPixelTemp.x += hamOffset.x;
		hamPixelTemp.y += hamOffset.y;

		halTempGpsPt = HACoordinateForMapPoint(hamPixelTemp/*vecPixelTemp[j]*/);
		vecGpsPoint.push_back(halTempGpsPt);
	}

	return 0;
}