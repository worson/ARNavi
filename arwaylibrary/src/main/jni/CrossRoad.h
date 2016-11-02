#pragma once

#include <vector>
//#include "core.hpp"
#include "opencv2/core/core.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "types.h"
#include "LinkFileInfo.hpp"

using namespace std;
using namespace cv;

class CrossRoad
{
public:
	CrossRoad(void);
	~CrossRoad(void);

	/*
	���ܣ�
		��ȡ·������
	������
		[in]const std::vector<std::vector<HALocationCoordinate2D> >& vecMainRoadlinks - ��·link��(�ߵµ�ͼ��γ�����ݣ�
		[in]const vector<vector<LinkInfo>>& vecMainRoadLinkInfos - ��·linkInfo��		
		[in]HALocationCoordinate2D halCenterPoint - ��·���ĵ㾭γ������
		[in]cv::Size2i szCover - ���ȡ·�����ݵĳߴ緶Χ
		[in]string strDictPath - �ֵ�·��
		[out]vector<vector<HALocationCoordinate2D> >& vecCrossGpsLinks - ·�����ݣ���·����
		[out]std::vector<HALocationCoordinate2D>& vecMainRoadGpsInNet - ·����ƥ�����·
		[out]std::vector<int>& vecCrossPointIndex - ��·���·��������·�е��±�
	���أ�
		0 - ���������� - �쳣
	*/
	int getCrossLinks(const std::vector<std::vector<HALocationCoordinate2D> >& vecMainRoadGpslinks,
					const std::vector<LinkInfo>& vecMainRoadGpsLinkInfos,					
					HALocationCoordinate2D halGpsCenterPoint,
					cv::Size2i szCover,
					string strDictPath,
					std::vector<std::vector<HALocationCoordinate2D> >& vecCrossGpsLinks,
					std::vector<HALocationCoordinate2D>& vecMainRoadGpsInNet,
					std::vector<int>& vecCrossPointIndex);

private:
	/*
	���ܣ�
		��������תgps����
	������
		[in]const std::vector<HAMapPoint> vecPixelPoint - ��������㼯
		[in]HAMapPoint hamOffset - ƫ����		
		[out]std::vector<HALocationCoordinate2D>& vecGpsPoint - ת��������
	���أ�
		0 - ���������� - �쳣
	*/
	int pixel2Gps(const std::vector<HAMapPoint> vecPixelPoint,
				HAMapPoint hamOffset,
				std::vector<HALocationCoordinate2D>& vecGpsPoint);
};

