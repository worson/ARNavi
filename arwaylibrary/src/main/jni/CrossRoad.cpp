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

// ��ȡ·������
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
	printf("==============translateRoadNet - parameter Error!!==============\n");
#else
	LOGD("==============getCrossLinks - enter!!==============\n");
#endif
	// �����Լ�
	int nNumLink = vecMainRoadGpslinks.size();
	if (nNumLink<=0 || nNumLink!=vecMainRoadGpsLinkInfos.size() ||
		szCover.height<=0 || szCover.width<=0)
	{
		#ifdef _WINDOWS_VER_
				printf("==============translateRoadNet - parameter Error!!==============\n");
		#else
				LOGD("==============getCrossLinks - parameter Error!!==============\n");
		#endif
		return -1;
	}

	int nRet = 0;

	// ��ȡ�ֵ�����
	//HaloNav haloNav;
	if (!m_IsReadDictionary)
	{
#ifdef _WINDOWS_VER_
		printf("==============translateRoadNet - parameter Error!!==============\n");
#else
		LOGD("==============!m_IsReadDictionary - enter!!==============\n");
#endif
		nRet = m_haloNav.readDictionary(strDictPath);
		if (nRet<0)
		{
			return -1;
		}
		m_IsReadDictionary = true;
#ifdef _WINDOWS_VER_
		printf("==============translateRoadNet - parameter Error!!==============\n");
#else
		LOGD("==============m_IsReadDictionary = true; - enter!!==============\n");
#endif
	}
	

	// ����ת������·��γ��ת����
	HAMapPoint hamOffset = m_haloNav.getOffset();		// ƫ����
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
			hamPixelXY.x -= hamOffset.x;		// ��ȥƫ����
			hamPixelXY.y -= hamOffset.y;
			vecTemp.push_back(hamPixelXY);
			vecMainRoadPixelPt.push_back(hamPixelXY);
		}
		vecMainRoadPixelLinks.push_back(vecTemp);
	}

#ifdef _WINDOWS_VER_
	printf("==============translateRoadNet - parameter Error!!==============\n");
#else
	LOGD("==============getCrossLinks - HAMapPointForCoordinate end!!==============\n");
#endif

	// ���ĵ�
	HAMapPoint hamPixelCenter = HAMapPointForCoordinate(halGpsCenterPoint);
	hamPixelCenter.x -= hamOffset.x;
	hamPixelCenter.y -= hamOffset.y;

#ifdef _WINDOWS_VER_
	printf("==============translateRoadNet - parameter Error!!==============\n");
#else
	LOGD("==============getCrossLinks - hamPixelCenter end!!==============\n");
#endif
	
	// ��ȡ·��link	
	std::vector<LinkInfo> vecRoadNetLinkInfo;		
	std::vector<std::vector<HAMapPoint> > vecRoadNetLink;
	m_haloNav.findLinks(hamPixelCenter,szCover.width,szCover.height,vecRoadNetLinkInfo,vecRoadNetLink);

#ifdef _WINDOWS_VER_
	printf("==============translateRoadNet - parameter Error!!==============\n");
#else
	LOGD("==============getCrossLinks - findLinks end!!==============\n");
#endif
	
	// ��·���в�����·���ںϵ�ͼ����
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
		return -1;
	}

#ifdef _WINDOWS_VER_
		printf("==============translateRoadNet - parameter Error!!==============\n");
#else
	LOGD("==============getCrossLinks - matchMainRoadCenterInNet5 end!!==============\n");
#endif

	// ����ת��������ת��γ��
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
	// ��·ת��
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

	// ��·ת��
	nRet = pixel2Gps(vecMainRoadPixelInNet,	hamOffset, vecMainRoadGpsInNet);
	if (nRet < 0)
	{
		return -1;
	}
#endif

#ifdef _WINDOWS_VER_
		printf("==============translateRoadNet - parameter Error!!==============\n");
#else
	LOGD("==============getCrossLinks - pixel2Gps end!!==============\n");
#endif

	return 0;
}


int CrossRoad::pixel2Gps(const std::vector<HAMapPoint> vecPixelPoint,
						 HAMapPoint hamOffset,
						 std::vector<HALocationCoordinate2D>& vecGpsPoint)
{
	// �����Լ�
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