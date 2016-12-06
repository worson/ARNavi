//
//  PlainLinkLineRecord.cpp
//  HaloAIMapData
//
//  Created by HarryMoo on 1/10/2016.
//  Copyright © 2016 HaloAI. All rights reserved.
//

#include "MergeMapData.hpp"
#include <numeric>

using namespace cv;

#ifdef _WINDOWS_VER_
#include "highgui.hpp"
#endif

MergeMapData::MergeMapData(){}
MergeMapData::~MergeMapData()
{
    
}

int MergeMapData::findMainRoadInRoadNet(const vector<HAMapPoint>& vecMainRoad,
						  HAMapPoint haMainRoadCenterPt,
						  const vector<LinkInfo>& vecRoadNetLinkInfo,
						  const std::vector<std::vector<HAMapPoint> >& vecRoadNetLink,
						  vector<LinkInfo>& vecOutLinkInfo,
						  vector<vector<HAMapPoint> >& vecOutLink,
						  vector<HAMapPoint>& vecMainRoadinNet)
{
	// 参数自检
	int nMainRoadPtNum = vecMainRoad.size();
	int nRoadNetLinkInfoNum = vecRoadNetLinkInfo.size();
	if (nMainRoadPtNum<=0 || nRoadNetLinkInfoNum<=0 ||
		nRoadNetLinkInfoNum!=vecRoadNetLink.size())
	{
		return -1;
	}

	int nRet = 0;
	
	// 匹配中心点
	HAMapPoint hamCenterInNet;
	vector<LinkInfo> vecMainRoadLinkInfosInNet;
	std::vector<std::vector<HAMapPoint> > vecMainRoadLinksInNet;
	nRet = matchMainRoadCenterInNet(vecMainRoad,
		haMainRoadCenterPt,
		vecRoadNetLinkInfo,
		vecRoadNetLink,
		hamCenterInNet,
		vecMainRoadLinkInfosInNet,
		vecMainRoadLinksInNet);
	if (nRet<0)
	{
		return -1;
	}

	// 判断主路转折点及方向向量
	vector<Point2i> vecMainRoadCv;
	for (int i=0; i<nMainRoadPtNum; i++)
	{
		Point2i pt = Point2i(vecMainRoad[i].x,vecMainRoad[i].y);
		vecMainRoadCv.push_back(pt);
	}
	Point2i ptMainRoadCenter = Point2i(haMainRoadCenterPt.x, haMainRoadCenterPt.y);
	vector<Point2i> vecBreakPoint;		// 转折点
	vector<int> vecBreakPointID;
	vector<cv::Vec4f> vecMainRoadDirections;	// 方向向量
	cv::Vec4f vefPreCenterLine;
	Point2i ptPreCenter;	
	nRet = getBreakPoint(vecMainRoadCv,ptMainRoadCenter,
						vecBreakPoint, vecBreakPointID,
						vecMainRoadDirections,
						vefPreCenterLine,
						ptPreCenter);
	if (nRet<0)
	{
		return -1;
	}

	int nMRDirections = vecMainRoadDirections.size();
	if (nMRDirections<=0)
	{
		return -1;
	}
	


	// 判断路网方向，保留相同方向link
	vector<int> vecLinkId;		// 记录同向link ID
	for (int i=0; i<nRoadNetLinkInfoNum; i++)
	{
		int nNum = vecRoadNetLink[i].size();
		float fDx = vecRoadNetLink[i][nNum-1].x - vecRoadNetLink[i][0].x;
		float fDy = vecRoadNetLink[i][nNum-1].y - vecRoadNetLink[i][0].y;
		float fDis = sqrt(fDx*fDx+fDy*fDy);
		fDx = fDx/fDis;		// 单位化
		fDy = fDy/fDis;
		Vec2f vDirection(fDx,fDy);		// 方向向量
		
		// 结合link自身方向属性
		int nLinkDir = vecRoadNetLinkInfo[i].direction;
		//int nLinkDir = 1;

	//#ifdef _WINDOWS_VER_
	//	// 绘图
	//	int thickness = -1;
	//	int lineType = 8;
	//	//int offset_x =0,offset_y = 0;
	//	int offset_x = haMainRoadCenterPt.x - m_matImage.cols/2;
	//	int offset_y = haMainRoadCenterPt.y - m_matImage.rows/2;
	//	int nLinkPtNum = vecRoadNetLink[i].size();
	//	std::vector<HAMapPoint> hamPts = vecRoadNetLink[i];
	//	cv::Scalar color((rand() % (255+1)),(rand() % (255+1)),250);
	//	bool bDrawFlag = false;
	//	for (int j = 1; j < nLinkPtNum; j++)
	//	{
	//		cv::Point ptPre = cv::Point(hamPts[j-1].x, hamPts[j-1].y) - cv::Point(offset_x,offset_y);
	//		cv::Point ptCur = cv::Point(hamPts[j].x, hamPts[j].y) - cv::Point(offset_x,offset_y);		

	//		cv::circle( m_matImage,ptPre,2,cv::Scalar( 0, 0, 255 ),thickness,lineType );
	//		
	//		line(m_matImage,ptPre,ptCur,color,1);

	//		if (Rect(0,0,m_matImage.cols,m_matImage.rows).contains(ptPre) || 
	//			Rect(0,0,m_matImage.cols,m_matImage.rows).contains(ptCur))
	//		{
	//			bDrawFlag = true;
	//		}
	//	}
	//	if (bDrawFlag)
	//	{
	//		imshow("m_matImage",m_matImage);
	//		waitKey(0);
	//	}
	//	
	//	//drawImage(m_matImage,haMainRoadCenterPt, vecRoadNetLink[i]);
	//	
	//#endif

		// 点积
		//float fDotM = 0.f;
		for (int j=0; j<nMRDirections; j++)
		{
			// 主路方向
			Vec2f vMainRoadDir(vecMainRoadDirections[j][0],vecMainRoadDirections[j][1]);
			Vec2f vDel(vecBreakPoint[j+1].x-vecBreakPoint[j].x,
						vecBreakPoint[j+1].y-vecBreakPoint[j].y);
			float fTemp = vMainRoadDir[0]*vDel[0] + vMainRoadDir[1]*vDel[1];
			if (fTemp<0)
			{
				vMainRoadDir = -vMainRoadDir;
			} 
			
			
			// 点积，单位向量相乘
			float fDotTemp = vDirection[0]*vMainRoadDir[0] + vDirection[1]*vMainRoadDir[1];			
// 			if (abs(fDotM)<=abs(fDotTemp))
// 			{
// 				fDotM = fDotTemp;
// 			}

			bool bFlag = false;		// 循环退出标志标志

			// 结合斜率判断
			//if (abs(fDotM)>=DOTMULTI_TH)	
			//if (abs(fDotTemp)>=DOTMULTI_TH)	
			//{
				// 结合link方向判断	
				
				switch (nLinkDir)
				{
				case 1:		// 双向
					if (abs(fDotTemp)>=DOTMULTI_TH)
					{
						vecLinkId.push_back(i);
						vecOutLink.push_back(vecRoadNetLink[i]);
						vecOutLinkInfo.push_back(vecRoadNetLinkInfo[i]);
						bFlag = true;
					}
					break;
				case 2:		// 正向
					if (fDotTemp>=DOTMULTI_TH)
					{
						vecLinkId.push_back(i);
						vecOutLink.push_back(vecRoadNetLink[i]);
						vecOutLinkInfo.push_back(vecRoadNetLinkInfo[i]);
						bFlag = true;
					}					
					break;
				case 3:		// 反向
					if (-fDotTemp>=0)
					{
						vecLinkId.push_back(i);
						vecOutLink.push_back(vecRoadNetLink[i]);
						vecOutLinkInfo.push_back(vecRoadNetLinkInfo[i]);
						bFlag = true;
					}	
					break;
				default:
					break;
				}
			//}
			if (bFlag)
			{
				break;
			}

		}

		

	}

	// 选择
// 	int nIdNum = vecLinkId.size();
// 	vecMainRoadinNet.clear();
// 	for (int i=0; i<nIdNum; i++)
// 	{
// 
// 	}


	return 0;
}

// 求拐点
int MergeMapData::getBreakPoint(const vector<cv::Point2i>& vecPointSet, cv::Point2i haCenterPt,
								vector<cv::Point2i>& vecBreakPoint, vector<int>& vecBreakPointID,
								vector<cv::Vec4f>& vecLines,
								cv::Vec4f& vefPreCenterLine,
								cv::Point2i& ptPreCenter)
{
	int nNum = vecPointSet.size();
	// 参数自检
	if (nNum<=0)
	{
		return -1;
	}

	vecBreakPoint.clear();
	vecBreakPointID.clear();
	vecLines.clear();

	// 加入起点
	vecBreakPoint.push_back(vecPointSet[0]);
	vecBreakPointID.push_back(0);

	int nPreID = 0;			// 记录前一个拐点的编号
	vector<Point2i> vecPointTemp;		// 记录临时用于直线拟合的点集
	Vec4f vPreLine;		// 记录直线(v1,v2,x,y), [v1,v2] - 对应方向向量
	Vec2i vNextLine;	// 记录当前点与后续相邻点连成向量
	float fPreAngle=0.f, fNextAngle=0.f, fDelAngle=0.f;		// 相对当前点，记录前、后向量与x轴夹角，及两向量夹角
	float fCosV=0.f;	// 记录余弦值
	float fPreCosV = 0.f;	// 记录前一次的余弦值
	for (int i=1; i<nNum-1; i++)		// 从第3个点开始判断
	{
		// 直线拟合当前点之前的直线
		vecPointTemp.clear();
		vecPointTemp.assign(vecPointSet.begin()+nPreID,vecPointSet.begin()+i+1);		
		cv::fitLine(vecPointTemp,vPreLine,CV_DIST_L2/*CV_DIST_WELSCH*/, 0, 0.01, 0.01);		

		// 当前点与后续相邻点连成向量
		vNextLine = cv::Vec2i(vecPointSet[i+1].x-vecPointSet[i].x, vecPointSet[i+1].y-vecPointSet[i].y);

		// 求中心点与前一个拐点的连线方向
		//if (Point2i(vecPointSet[i].x,vecPointSet[i].y)==Point2i(haCenterPt.x,haCenterPt.y))
		if (vecPointSet[i]==haCenterPt)
		{
			vefPreCenterLine = vPreLine;
			ptPreCenter.x = vPreLine[2];
			ptPreCenter.y = vPreLine[3];
		}

		// 点积求余弦
		fPreAngle = atan2(vPreLine[1], vPreLine[0]);
		fNextAngle = atan2(vNextLine[1], vNextLine[0]);
		fPreAngle = (fPreAngle<0)?(fPreAngle+2*CV_PI):fPreAngle;		// 范围转成[0,2*pi]
		fNextAngle = (fNextAngle<0)?(fNextAngle+2*CV_PI):fNextAngle;
		fDelAngle = fNextAngle - fPreAngle;

		fCosV = cos(fDelAngle);		
		fCosV = abs(fCosV);

		// 判断
		if (fCosV<PARALLEL_COS_VALUE)
		{
			//// 判断是否与前一个拐点相邻，若相邻，二取一，取夹角较大的拐点
			//if (i==(nPreID+1)&&nPreID!=0)
			//{
			//	if (fCosV<fPreCosV)
			//	{
			//		vecBreakPoint.erase(vecBreakPoint.end()-1);		// 舍前一个拐点
			//		vecBreakPointID.erase(vecBreakPointID.end()-1);
			//		vecLines.erase(vecLines.end()-1);

			//		vecBreakPoint.push_back(vecPointSet[i]);			// 取当前拐点
			//		vecBreakPointID.push_back(i);
			//		vecLines.push_back(vPreLine);

			//		nPreID = i;
			//		fPreCosV = fCosV;					
			//	}
			//	continue;
			//}

			vecBreakPoint.push_back(vecPointSet[i]);
			vecBreakPointID.push_back(i);
			vecLines.push_back(vPreLine);

			nPreID = i;
			fPreCosV = fCosV;
		}
	}

	// 最后一条线
	vecPointTemp.clear();
	vecPointTemp.assign(vecPointSet.begin()+nPreID,vecPointSet.end());		
	cv::fitLine(vecPointTemp,vPreLine,CV_DIST_L2/*CV_DIST_WELSCH*/, 0, 0.01, 0.01);	

	// 加入终点
	vecBreakPoint.push_back(vecPointSet[nNum-1]);
	vecBreakPointID.push_back(nNum-1);
	vecLines.push_back(vPreLine);	

	return 0;
}


// 在路网中匹配主路中心点
int MergeMapData::matchMainRoadCenterInNet(const vector<HAMapPoint>& vecMainRoad,
							HAMapPoint haMainRoadCenterPt,
							const vector<LinkInfo>& vecRoadNetLinkInfos,
							const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
							HAMapPoint& hamCenterInNet,
							vector<LinkInfo>& vecMainRoadLinkInfosInNet,
							std::vector<std::vector<HAMapPoint> >& vecMainRoadLinksInNet)
{
	// 参数自检
	int nNumMainRoadPt = vecMainRoad.size();
	int nNumLink = vecRoadNetLinks.size();
	if (nNumMainRoadPt<=0 || nNumLink<=0 ||
		nNumLink!=vecRoadNetLinkInfos.size())
	{
		return -1;
	}

	int nRet = 0;

	// 求中心点位置
	int nCenterSi = -1;
	for (int i = 0; i<nNumMainRoadPt; i++)
	{
		if (vecMainRoad[i].x==haMainRoadCenterPt.x && 
			vecMainRoad[i].y==haMainRoadCenterPt.y)
		{
			nCenterSi = i;
			break;
		}
	}
	if (nCenterSi<0)	// 主路中不包含中心点
	{
		return -1;
	}
	
	// 取中心点一定范围内的主路子集（如上下左右各200像素范围），并按顺序填满点与点之间的所有点
	int nDel = CENTER_COVER;		// 
	vector<HAMapPoint> vecSubMainRoad;
	vecSubMainRoad.push_back(haMainRoadCenterPt);
	for (int i=nCenterSi-1; i>=0; i--)
	{
		if (vecMainRoad[i].x==haMainRoadCenterPt.x && vecMainRoad[i].y==haMainRoadCenterPt.y)
		{
			continue;
		}
		if (abs(vecMainRoad[i].x-haMainRoadCenterPt.x)<=nDel &&
			abs(vecMainRoad[i].y-haMainRoadCenterPt.y)<=nDel)
		{
			// 在直线上，求两点间范围内的所有点
			vector<HAMapPoint> vecPointsInLine;			
			nRet = getPointsBetweenTwoPoints(vecMainRoad[i], vecSubMainRoad[0], vecPointsInLine);			
			if (nRet==0)
			{
				vecSubMainRoad.insert(vecSubMainRoad.begin(),vecPointsInLine.begin(),vecPointsInLine.end());				
			}
			vecSubMainRoad.insert(vecSubMainRoad.begin(),vecMainRoad[i]);
		}
		else
		{
			// 在直线上，求两点间范围内的所有点
			vector<HAMapPoint> vecPointsInLine;			
			nRet = getPointsBetweenTwoPoints(vecMainRoad[i], vecSubMainRoad[0], vecPointsInLine);			
			if (nRet==0)
			{
				for (size_t j=0; j<vecPointsInLine.size(); j++)
				{
					if (abs(vecPointsInLine[j].x-haMainRoadCenterPt.x)<=nDel &&
						abs(vecPointsInLine[j].y-haMainRoadCenterPt.y)<=nDel)
					{
						vecSubMainRoad.insert(vecSubMainRoad.begin(),vecPointsInLine[j]);
					}					
				}				
			}
			//vecSubMainRoad.insert(vecSubMainRoad.begin(),vecMainRoad[i]);
			break;
		}
	}
	for (int i=nCenterSi+1; i<nNumMainRoadPt; i++)
	{
		if (vecMainRoad[i].x==haMainRoadCenterPt.x && vecMainRoad[i].y==haMainRoadCenterPt.y)
		{
			continue;
		}
		if (abs(vecMainRoad[i].x-haMainRoadCenterPt.x)<=nDel &&
			abs(vecMainRoad[i].y-haMainRoadCenterPt.y)<=nDel)
		{
			// 在直线上，求两点间范围内的所有点
			vector<HAMapPoint> vecPointsInLine;			
			nRet = getPointsBetweenTwoPoints(vecSubMainRoad[vecSubMainRoad.size()-1], vecMainRoad[i], vecPointsInLine);
			if (nRet==0)
			{
				vecSubMainRoad.insert(vecSubMainRoad.end(),vecPointsInLine.begin(),vecPointsInLine.end());				
			}			
			vecSubMainRoad.push_back(vecMainRoad[i]);
		}
		else
		{
			// 在直线上，求两点间范围内的所有点
			vector<HAMapPoint> vecPointsInLine;			
			nRet = getPointsBetweenTwoPoints(vecSubMainRoad[vecSubMainRoad.size()-1], vecMainRoad[i], vecPointsInLine);	
			if (nRet==0)
			{
				for (int j=0; j<vecPointsInLine.size(); j++)
				{
					if (abs(vecPointsInLine[j].x-haMainRoadCenterPt.x)<=nDel &&
						abs(vecPointsInLine[j].y-haMainRoadCenterPt.y)<=nDel)
					{
						vecSubMainRoad.push_back(vecPointsInLine[j]);
					}					
				}				
			}			
			break;
		}
	}

	//// 填满路网每个link中的点
	//std::vector<std::vector<HAMapPoint> > vecRoadNetFullLinks;
	//
	//for (int i=0; i<nNumLink; i++)
	//{
	//	vector<HAMapPoint> vecLinkPts = vecRoadNetLinks[i];
	//	int nNumLinkPt = vecLinkPts.size();
	//	std::vector<HAMapPoint> vecHamFullpts;
	//	vecHamFullpts.push_back(vecLinkPts[0]);
	//	for (int j=1; j<nNumLinkPt; j++)
	//	{			
	//		// 在直线上，求两点间范围内的所有点
	//		vector<HAMapPoint> vecPointsInLine;
	//		nRet = getPointsBetweenTwoPoints(vecLinkPts[j-1], vecLinkPts[j], vecPointsInLine);
	//		if (nRet==0)
	//		{
	//			vecHamFullpts.insert(vecHamFullpts.end(),vecPointsInLine.begin(),vecPointsInLine.end());				
	//		}
	//		vecHamFullpts.push_back(vecLinkPts[j]);
	//	}
	//	vecRoadNetFullLinks.push_back(vecHamFullpts);
	//}
		
	
	// 求所有落在中心点一定范围内link的端点，不重复，并求link的范围
	int nX1=-1, nX2=0, nY1=-1, nY2=0;
	vector<Point2i> vecEndPt;
	for (int i=0; i<nNumLink; i++)
	{
		vector<HAMapPoint> vecLinkPts = vecRoadNetLinks[i];

		// 判断是否重复
		Point2i ptTemp = Point2i(vecLinkPts[0].x,vecLinkPts[0].y);
		if (abs(ptTemp.x-haMainRoadCenterPt.x)>CENTER_COVER || 
			abs(ptTemp.y-haMainRoadCenterPt.y)>CENTER_COVER)
		{
			continue;
		}
		vector<Point2i>::iterator iter = std::find(vecEndPt.begin(),vecEndPt.end(),ptTemp);//返回的是一个迭代器指针
		if (iter==vecEndPt.end())	// 不重复
		{
			vecEndPt.push_back(ptTemp);
			nX1 = (nX1<0)?ptTemp.x:min(nX1,ptTemp.x);
			nY1 = (nY1<0)?ptTemp.y:min(nY1,ptTemp.y);
			nX2 = max(nX2,ptTemp.x);
			nY2 = max(nY2,ptTemp.y);
		}
		ptTemp = Point2i(vecLinkPts[vecLinkPts.size()-1].x,vecLinkPts[vecLinkPts.size()-1].y);
		if (abs(ptTemp.x-haMainRoadCenterPt.x)>CENTER_COVER || 
			abs(ptTemp.y-haMainRoadCenterPt.y)>CENTER_COVER)
		{
			continue;
		}
		iter = std::find(vecEndPt.begin(),vecEndPt.end(),
			Point2i(vecLinkPts[vecLinkPts.size()-1].x,vecLinkPts[vecLinkPts.size()-1].y));//返回的是一个迭代器指针
 		if (iter==vecEndPt.end())	// 不重复
 		{
 			vecEndPt.push_back(ptTemp);
			nX1 = (nX1<0)?ptTemp.x:min(nX1,ptTemp.x);
			nY1 = (nY1<0)?ptTemp.y:min(nY1,ptTemp.y);
			nX2 = max(nX2,ptTemp.x);
			nY2 = max(nY2,ptTemp.y);
 		}
	}
	
	if (vecEndPt.size()<=0)
	{
		return -1;
	}

	// 匹配中心点，（在路网中，移动主路中心点，通过最小平方和判断
	int nSubMainRoadNumPt = vecSubMainRoad.size();	
	float fMinSumQuare = -1;		// 记录最小距离和
	int nMinSi = 0;		// 与fMinSumQuare对应的位置
	int nNumEndPt = vecEndPt.size();
	for (int i=0; i<nNumEndPt; i++)
	{
		Point2i ptNewCenter(vecEndPt[i].x,vecEndPt[i].y);		
		
		float fDisSum = 0.f;		// 记录距离和
		for (int j = 0; j< nSubMainRoadNumPt; j++)
		{
			Point2i ptDel = ptNewCenter - Point2i(haMainRoadCenterPt.x,haMainRoadCenterPt.y);		// 增量			
			Point2i pt = Point2i(vecSubMainRoad[j].x,vecSubMainRoad[j].y) + ptDel;		// 移动			

			// 求最小距离
			float fMinDis = -1;
			for (int k=0; k<nNumLink; k++)
			{
				vector<HAMapPoint> vecHamPt = vecRoadNetLinks[k];

				if ((abs(vecHamPt[0].x-haMainRoadCenterPt.x)>CENTER_COVER || 
					abs(vecHamPt[0].y-haMainRoadCenterPt.y)>CENTER_COVER) &&
					(abs(vecHamPt[vecHamPt.size()-1].x-haMainRoadCenterPt.x)>CENTER_COVER || 
					abs(vecHamPt[vecHamPt.size()-1].y-haMainRoadCenterPt.y)>CENTER_COVER))
				{
					continue;
				}

				Line lin;
				lin.pt1 = Point2d(vecHamPt[0].x, vecHamPt[0].y);
				lin.pt2 = Point2d(vecHamPt[vecHamPt.size()-1].x, vecHamPt[vecHamPt.size()-1].y);
								
				// 判断垂点是否位于lin两端点之间，通过求三角形两夹角判断
				Vec2f v1(pt.x-lin.pt1.x,pt.y-lin.pt1.y);
				Vec2f v2(pt.x-lin.pt2.x,pt.y-lin.pt2.y);
				Vec2f v3(lin.pt2.x-lin.pt1.x,lin.pt2.y-lin.pt1.y);
				float fDot1 = v1[0]*v3[0] + v1[1]*v3[1];	// 内积
				float fDot2 = v2[0]*(-v3[0])+v2[1]*(-v3[1]); // 内积
				float fDis = 0.f;
				if (fDot1<0 || fDot2<0)
				{
					fDis = (fDot1<fDot2)?getDistancePoint2Point(pt.x,pt.y,(int)lin.pt1.x,(int)lin.pt1.y):
						getDistancePoint2Point(pt.x,pt.y,(int)lin.pt2.x,(int)lin.pt2.y);					
				} 
				else
				{					
					fDis = getDistancePoint2Line(pt,lin);
				}
				fMinDis = (fMinDis<0)?fDis:min(fMinDis,fDis);
			}// end k
			fDisSum += fMinDis;
		}// end j
		
		if (fMinSumQuare<0)
		{
			nMinSi = i;
			fMinSumQuare = fDisSum;
		}
		else
		{
			nMinSi = (fMinSumQuare<fDisSum)?nMinSi:i;
			fMinSumQuare = min(fMinSumQuare,fDisSum);
		}		
	}// end i

	// 输出
	hamCenterInNet.x = vecEndPt[nMinSi].x;
	hamCenterInNet.y = vecEndPt[nMinSi].y;

	// 画图
#ifdef _WINDOWS_VER_
	//Mat matNavi(800,800,CV_8UC3);
	//matNavi.setTo(0);
	Mat matNavi = m_matImage;
	drawImage(matNavi, haMainRoadCenterPt, vecSubMainRoad,Scalar(255,0,0));		// 原始主路
	for (int i=0; i<nNumLink; i++)
	{
		drawImage(matNavi,haMainRoadCenterPt, vecRoadNetLinks[i],Scalar(0,0,0));
	}
	
	// 匹配的主路
	vector<HAMapPoint> vecNewMainRoad(nSubMainRoadNumPt);
	int nDx = hamCenterInNet.x - haMainRoadCenterPt.x;
	int nDy = hamCenterInNet.y - haMainRoadCenterPt.y;
	for (int i=0; i<nSubMainRoadNumPt; i++)
	{
		vecNewMainRoad[i].x = vecSubMainRoad[i].x+nDx;
		vecNewMainRoad[i].y = vecSubMainRoad[i].y+nDy;
	}
	drawImage(matNavi, haMainRoadCenterPt, vecNewMainRoad,Scalar(0,255,0));		// 新主路

	// 两个中心点
	int offset_x =0,offset_y = 0;
	offset_x = haMainRoadCenterPt.x - matNavi.cols/2;
	offset_y = haMainRoadCenterPt.y - matNavi.rows/2;
	cv::Point ptOld = cv::Point(haMainRoadCenterPt.x, haMainRoadCenterPt.y) - cv::Point(offset_x,offset_y);
	cv::Point ptNew = cv::Point(hamCenterInNet.x, hamCenterInNet.y) - cv::Point(offset_x,offset_y);
	cv::circle( matNavi,ptOld,2,cv::Scalar( 0, 0, 255 ),2,8);
	cv::circle( matNavi,ptNew,2,cv::Scalar( 0, 0, 255 ),2,8);

	//imshow("matNavi",matNavi);
	//waitKey(0);
#endif

	return 0;
}


// 利用角度匹配中心点
int MergeMapData::matchMainRoadCenterInNet1(const vector<HAMapPoint>& vecMainRoad,
											HAMapPoint haMainRoadCenterPt,
											const vector<LinkInfo>& vecRoadNetLinkInfos,
											const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
											HAMapPoint& hamCenterInNet,
											vector<LinkInfo>& vecMainRoadLinkInfosInNet,
											std::vector<std::vector<HAMapPoint> >& vecMainRoadLinksInNet)
{
	// 参数自检
	int nNumMainRoadPt = vecMainRoad.size();
	int nNumLink = vecRoadNetLinks.size();
	if (nNumMainRoadPt<=0 || nNumLink<=0 ||
		nNumLink!=vecRoadNetLinkInfos.size())
	{
		return -1;
	}

	int nRet = 0;

	// 求中心点位置
	int nCenterSi = -1;
	nRet = getPointSite(vecMainRoad, haMainRoadCenterPt, nCenterSi);
	if (nRet<0)
	{
		return -1;
	}

	// 求中心点前后各一个节点（与中心点的距离有要求，如大于10个像素），用于计算夹角
	HAMapPoint hamPrePt, hamNextPt;
	nRet = getNearestPoint(vecMainRoad,	nCenterSi, true, hamPrePt);
	if (nRet<0)
	{
		return -1;
	}
	nRet = getNearestPoint(vecMainRoad,	nCenterSi, false, hamNextPt);
	if (nRet<0)
	{
		return -1;
	}

	// 取中心点与前、后临近点连线上的所有点
	vector<HAMapPoint> vecSubMainRoad;
	vecSubMainRoad.push_back(hamPrePt);
	nRet = getPointsBetweenTwoPoints(hamPrePt,haMainRoadCenterPt,vecSubMainRoad);
	if (nRet<0)
	{
		return -1;
	}
	vecSubMainRoad.push_back(haMainRoadCenterPt);
	int nCenterSiInSubMainRoad = vecSubMainRoad.size() - 1;		// 中心点在子路中的位置
	nRet = getPointsBetweenTwoPoints(haMainRoadCenterPt,hamNextPt,vecSubMainRoad);
	if (nRet<0)
	{
		return -1;
	}
	vecSubMainRoad.push_back(hamNextPt);

	// 缩短子路，使得中心点两侧点数相等，保证两边比重一致
	int nDel = vecSubMainRoad.size() - nCenterSiInSubMainRoad - 1;
	vector<HAMapPoint> vecTemp;
	if (nDel>nCenterSiInSubMainRoad)	// 中心点之后的点数多
	{
		vecTemp.insert(vecTemp.begin(),vecSubMainRoad.begin(),vecSubMainRoad.begin()+2*nCenterSiInSubMainRoad+1);
	} 
	else	// 中心点之前的点数多
	{
		vecTemp.insert(vecTemp.begin(),vecSubMainRoad.begin()+nCenterSiInSubMainRoad-nDel,vecSubMainRoad.end());
	}
	vecSubMainRoad.clear();
	vecSubMainRoad = vecTemp;
	vecTemp.clear();
	nCenterSiInSubMainRoad = vecSubMainRoad.size()/2;

	// 求中心夹角
	Vec2f vMainRoad1(hamPrePt.x-haMainRoadCenterPt.x, hamPrePt.y-haMainRoadCenterPt.y);		// 前一个点与中心点构成的向量
	Vec2f vMainRoad2(hamNextPt.x-haMainRoadCenterPt.x, hamNextPt.y-haMainRoadCenterPt.y);	// 后一个点与中心点构成的向量	
	float fMainAngle = getAngle(vMainRoad1, vMainRoad2);

	// 构造link端点节点
	vector<LinkEndPointNode> vecLinkEndPtnode;
	nRet = formLinkEndPointNode(vecRoadNetLinks, vecRoadNetLinkInfos, vecLinkEndPtnode);
	if (nRet<0)
	{
		return -1;
	}


	// =================绘图==================
#ifdef _WINDOWS_VER_
#if IS_DRAW1	// 绘图
	Mat matTemp1(m_matImage.rows,m_matImage.cols,CV_8UC3);
	matTemp1.setTo(0);
	int offset_x1 =0,offset_y1 = 0;
	offset_x1 = haMainRoadCenterPt.x - matTemp1.cols/2;
	offset_y1 = haMainRoadCenterPt.y - matTemp1.rows/2;
	cv::Point ptTemp = cv::Point(haMainRoadCenterPt.x, haMainRoadCenterPt.y) - cv::Point(offset_x1,offset_y1);
	cv::circle( matTemp1,ptTemp,2,cv::Scalar( 0, 255, 0),2,8);	
	for (int i=0; i<vecLinkEndPtnode.size(); i++)
	{
		LinkEndPointNode node = vecLinkEndPtnode[i];
		HAMapPoint hamEndPt = node.hamEndPoint;

		// 区域限制
		if ((abs(hamEndPt.x-haMainRoadCenterPt.x)>CENTER_COVER || 
			abs(hamEndPt.y-haMainRoadCenterPt.y)>CENTER_COVER))
		{
			continue;
		}

		cv::Point ptTemp = cv::Point(hamEndPt.x, hamEndPt.y) - cv::Point(offset_x1,offset_y1);
		cv::circle( matTemp1,ptTemp,2,cv::Scalar( 0, 0, 255 ),2,8);
		for (int j=0; j<node.vecNeighborPoint.size(); j++)
		{
			vector<HAMapPoint> vecTemp;
			vecTemp.push_back(hamEndPt);
			vecTemp.push_back(node.vecNeighborPoint[j]);
			drawImage(matTemp1,haMainRoadCenterPt,vecTemp,cv::Scalar(0,0,0));
			cv::circle( matTemp1,Point2i(node.vecNeighborPoint[j].x,node.vecNeighborPoint[j].y),
				1,cv::Scalar( 0, 255, 0),1,8);
		}
		imshow("matTemp1",matTemp1);
		waitKey(0);		
	}
#endif
#endif
	// =====================================

#ifdef _WINDOWS_VER_
	#if IS_DRAW || IS_DRAW2	// 绘图
		Mat matTemp(m_matImage.rows,m_matImage.cols,CV_8UC3);
		matTemp.setTo(0);
		int offsetx =0,offsety = 0;
		offsetx = haMainRoadCenterPt.x - matTemp.cols/2;
		offsety = haMainRoadCenterPt.y - matTemp.rows/2;
		cv::Point ptOffset(offsetx,offsety);
		cv::Point ptTemp = cv::Point(haMainRoadCenterPt.x, haMainRoadCenterPt.y) - ptOffset;
		cv::circle( matTemp,ptTemp,2,cv::Scalar( 0, 255, 0),2,8);	
		drawImage(matTemp,haMainRoadCenterPt,/*vecSubMainRoad*/vecMainRoad,cv::Scalar(255,0,0));
	#endif
	#if IS_DRAW2	// 绘图
		for (int i=0; i<nNumLink; i++)
		{
			drawImage(matTemp,haMainRoadCenterPt,vecRoadNetLinks[i],cv::Scalar(0,0,255),1);
		}
		
	#endif
#endif


	// 角度匹配
	int nMatchSi = -1, nPreSonSi = -1, nNextSonSi = -1;		// 记录匹配位置
	float fMinDelAngle = 360.f;		// 记录夹角之差的最小绝对值
	int nEndPtNum = vecLinkEndPtnode.size();
	LinkEndPointNode node;
	double uMinError= -1.f;
	for (int i=0; i<nEndPtNum; i++)
	{
		node = vecLinkEndPtnode[i];
		int nNodeIDNum = node.vecLinkId.size();
		if (nNodeIDNum<=2)
		{
			continue;
		}

		HAMapPoint hamEndPt = node.hamEndPoint;	

		// 区域限制
		if ((abs(hamEndPt.x-haMainRoadCenterPt.x)>CENTER_COVER || 
			abs(hamEndPt.y-haMainRoadCenterPt.y)>CENTER_COVER))
		{
			continue;
		}

#ifdef _WINDOWS_VER_
	#if IS_DRAW1	// 绘图
		cv::Point ptTemp = cv::Point(hamEndPt.x, hamEndPt.y) - cv::Point(offset_x1,offset_y1);
		cv::circle( matTemp1,ptTemp,2,cv::Scalar( 0, 255, 255 ),2,8);
		for (int j=0; j<node.vecNeighborPoint.size(); j++)
		{
			vector<HAMapPoint> vecTemp;
			vecTemp.push_back(hamEndPt);
			vecTemp.push_back(node.vecNeighborPoint[j]);
			drawImage(matTemp1,haMainRoadCenterPt,vecTemp,cv::Scalar(0,255,255));
			cv::circle( matTemp1,Point2i(node.vecNeighborPoint[j].x,node.vecNeighborPoint[j].y),
				1,cv::Scalar( 0, 255, 255),1,8);
		}
		imshow("matTemp1",matTemp1);
		waitKey(0);
	#endif

	#if IS_DRAW2	// 绘图
		cv::Point ptTemp = cv::Point(hamEndPt.x, hamEndPt.y) - ptOffset;
		cv::circle( matTemp,ptTemp,2,cv::Scalar( 0, 255, 255),2,8);
	#endif
#endif

		// 求与主路中心点前后两向量最接近的两向量		
		int nSi0=-1, nSi1=-1;
		Vec2f v0, v1;
		for (int j=0; j<2; j++)
		{
			Vec2f vMainRoad = vMainRoad1;	// 中心点之前的向量
			if (j>0)
			{				
				vMainRoad = vMainRoad2;	// 中心点之后的向量
			}

			float fMinAngle = 360.f;
			int nTempSi=0;
			Vec2f vTemp;

			for (int k=0; k<nNodeIDNum; k++)
			{
				HAMapPoint hamPt = node.vecNeighborPoint[k];
				Vec2f v(hamPt.x-hamEndPt.x,hamPt.y-hamEndPt.y);

			#if 1
				// ===================方向限制==============
				// 方向判断 int direction;//道路方向:0未调查,默认双向,1双向,2正方向(link的起点到终点),3反方向
				int nLinkId = node.vecLinkId[k];
				int nDirection = vecRoadNetLinkInfos[nLinkId].direction;
				//bool bIsEnd = (hamEndPt==(HAMapPoint)vecRoadNetLinks[nLinkId][0])?false:true;		// 标识端点是否属于link的终点
				bool bIsEnd = ((hamEndPt.x==vecRoadNetLinks[nLinkId][0].x)&&
								(hamEndPt.y==vecRoadNetLinks[nLinkId][0].y))?false:true;		// 标识端点是否属于link的终点
				if (j==0)	// 标识端点应为link终点
				{
					if ((bIsEnd==true&&nDirection==3) || (bIsEnd==false&&nDirection==2))
					{
						continue;
					}
				} 
				else		// 标识端点应为link起点
				{
					if ((bIsEnd==true&&nDirection==2) || (bIsEnd==false&&nDirection==3))
					{
						continue;
					}
				}
				// =====================================
			#endif

				float fAngle = getAngle(vMainRoad, v);				
				if (fAngle<fMinAngle)
				{
					fMinAngle = fAngle;
					nTempSi = k;
					vTemp = v;
				}				
			}	// End k
			if (fMinAngle>ANGLE_ALLOWANCE)
			{
				break;
			}
			else
			{				
				if (j==0)
				{
					nSi0 = nTempSi;
					v0 = vTemp;
				} 
				else
				{
					nSi1 = nTempSi;
					v1 = vTemp;
				}

				#if IS_DRAW1		// 绘图
					Mat matT(m_matImage.rows, m_matImage.cols,CV_8UC3);
					matT.setTo(0);
					vector<HAMapPoint> vecT;
					vecT.push_back(hamEndPt);
					vecT.push_back(node.vecNeighborPoint[nTempSi]);
					drawImage(matT,haMainRoadCenterPt,vecT,Scalar(0,0,0));
					imshow("matT",matT);
					waitKey(0);
				#endif

				//// 保存每个节点的方向数据，绿-与主路同向，红-与主路反向
				//#ifdef _WINDOWS_VER_
				//	#if IS_DRAW2	// 绘图								
				//		cv::Point ptV = Point(hamEndPt.x,hamEndPt.y) - ptOffset;
				//		cv::Point ptTemp = Point(node.vecNeighborPoint[nTempSi].x,node.vecNeighborPoint[nTempSi].y) - ptOffset;						
				//		cv::circle( matTemp,ptV,1,cv::Scalar( 0, 255, 255),2,8);
				//		cv::circle( matTemp,ptTemp,1,cv::Scalar( 0, 255, 255),2,8);
				//		Scalar color(0,255,0);
				//		line(matTemp,ptTemp,ptV,color,1);						
				//	#endif
				//#endif

			}
		}	// End j
		if (nSi0<0 || nSi1<0 || nSi0==nSi1)
		{
			continue;
		}

		// 保存每个节点的方向数据，绿-与主路同向，红-与主路反向
#ifdef _WINDOWS_VER_
	#if IS_DRAW2	// 绘图								
			cv::Point ptV = Point(hamEndPt.x,hamEndPt.y) - ptOffset;
			cv::Point ptTemp0 = Point(node.vecNeighborPoint[nSi0].x,node.vecNeighborPoint[nSi0].y) - ptOffset;		
			cv::Point ptTemp1 = Point(node.vecNeighborPoint[nSi1].x,node.vecNeighborPoint[nSi1].y) - ptOffset;						
			cv::circle( matTemp,ptV,1,cv::Scalar( 0, 255, 255),2,8);
			cv::circle( matTemp,ptTemp0,1,cv::Scalar( 0, 255, 255),2,8);
			cv::circle( matTemp,ptTemp1,1,cv::Scalar( 0, 255, 255),2,8);
			Scalar color(0,255,0);
			line(matTemp,ptTemp0,ptV,color,1);	
			line(matTemp,ptV,ptTemp1,color,1);	
	#endif
#endif

#if 1
		// 求夹角		
		float fAngle = getAngle(v0,v1);
		float fDelAngle = abs(fMainAngle-fAngle);
		if (fDelAngle<fMinDelAngle)
		{
			fMinDelAngle = fDelAngle;
			nMatchSi = i;
			nPreSonSi = nSi0;
			nNextSonSi = nSi1;
		}
#endif


		// =================绘图==================
#ifdef _WINDOWS_VER_
#if IS_DRAW	// 绘图
		//Mat matTemp(m_matImage.rows,m_matImage.cols,CV_8UC3);
		//matTemp.setTo(0);
		//int offset_x =0,offset_y = 0;
		offsetx = haMainRoadCenterPt.x - matTemp.cols/2;
		offsety = haMainRoadCenterPt.y - matTemp.rows/2;
		cv::Point ptTemp = cv::Point(hamEndPt.x, hamEndPt.y) - cv::Point(offsetx,offsety);
		cv::circle( matTemp,ptTemp,2,cv::Scalar( 0, 0, 255 ),2,8);
		vector<HAMapPoint> vecTemp;
		vecTemp.push_back(node.vecNeighborPoint[nSi0]);
		vecTemp.push_back(hamEndPt);
		vecTemp.push_back(node.vecNeighborPoint[nSi1]);
		drawImage(matTemp,haMainRoadCenterPt,vecTemp,cv::Scalar(0,0,0));
		cv::circle( matTemp,Point2i(node.vecNeighborPoint[nSi0].x,node.vecNeighborPoint[nSi0].y),
			1,cv::Scalar( 0, 255, 0),1,8);
		cv::circle( matTemp,Point2i(node.vecNeighborPoint[nSi1].x,node.vecNeighborPoint[nSi1].y),
			1,cv::Scalar( 0, 255, 0),1,8);
		imshow("matTemp",matTemp);
		waitKey(0);
#endif
#endif
		// =====================================
	#if 0
		// 求距离
		double uError = 0.f;
		nRet = getMainSubLine2SubNetDis(vecSubMainRoad, 
			nCenterSiInSubMainRoad,
			node.vecNeighborPoint[nSi0],
			hamEndPt,
			node.vecNeighborPoint[nSi1],										
			uError);
		if (nRet<0)
		{
			continue;
		}
		if (uMinError<0)
		{
			uMinError = uError;
			nMatchSi = i;
		}
		else
		{
			nMatchSi = (uError<uMinError)?i:nMatchSi;
			uMinError = (uError<uMinError)?uError:uMinError;
		}
	#endif
	}		// End i

	if (nMatchSi<0)
	{
		return -1;
	}


	// 输出
	//nMatchSi = 29;
	hamCenterInNet = vecLinkEndPtnode[nMatchSi].hamEndPoint;


	// 画图
#ifdef _WINDOWS_VER_
	#if IS_DRAW
			//Mat matNavi(800,800,CV_8UC3);
			//matNavi.setTo(0);
			Mat matNavi = m_matImage;
			drawImage(matNavi, haMainRoadCenterPt, vecMainRoad,Scalar(255,0,0));		// 原始主路	
			for (int i=0; i<nNumLink; i++)
			{
				drawImage(matNavi,haMainRoadCenterPt, vecRoadNetLinks[i],Scalar(0,0,0));
			}

			// 匹配的主路
		#if 0
			vector<HAMapPoint> vecNewMainRoad(nNumMainRoadPt);
			int nDx = hamCenterInNet.x - haMainRoadCenterPt.x;
			int nDy = hamCenterInNet.y - haMainRoadCenterPt.y;
			for (int i=0; i<nNumMainRoadPt; i++)
			{
				vecNewMainRoad[i].x = vecMainRoad[i].x+nDx;
				vecNewMainRoad[i].y = vecMainRoad[i].y+nDy;
			}
			drawImage(matNavi, haMainRoadCenterPt, vecNewMainRoad,Scalar(0,255,0));		// 新主路
		#else
			int nSubMainRoadNum = vecSubMainRoad.size();
			vector<HAMapPoint> vecNewMainRoad(nSubMainRoadNum);
			int nDx = hamCenterInNet.x - haMainRoadCenterPt.x;
			int nDy = hamCenterInNet.y - haMainRoadCenterPt.y;
			for (int i=0; i<nSubMainRoadNum; i++)
			{
				vecNewMainRoad[i].x = vecSubMainRoad[i].x+nDx;
				vecNewMainRoad[i].y = vecSubMainRoad[i].y+nDy;
			}
			drawImage(matNavi, haMainRoadCenterPt, vecNewMainRoad,Scalar(0,255,0));		// 新主路
		#endif


			// 两个中心点
			int offset_x =0,offset_y = 0;
			offset_x = haMainRoadCenterPt.x - matNavi.cols/2;
			offset_y = haMainRoadCenterPt.y - matNavi.rows/2;
			cv::Point ptOld = cv::Point(haMainRoadCenterPt.x, haMainRoadCenterPt.y) - cv::Point(offset_x,offset_y);
			cv::Point ptNew = cv::Point(hamCenterInNet.x, hamCenterInNet.y) - cv::Point(offset_x,offset_y);
			cv::circle( matNavi,ptOld,2,cv::Scalar( 0, 0, 255 ),2,8);
			cv::circle( matNavi,ptNew,2,cv::Scalar( 0, 0, 255 ),2,8);

		#if IS_DRAW
			imshow("matNavi",matNavi);
			waitKey(0);
		#endif
	#endif

	#if IS_DRAW2	// 绘图
			node = vecLinkEndPtnode[nMatchSi];
			cv::Point ptV = Point(node.hamEndPoint.x,node.hamEndPoint.y) - ptOffset;
			cv::Point ptTemp0 = Point(node.vecNeighborPoint[nPreSonSi].x,node.vecNeighborPoint[nPreSonSi].y) - ptOffset;		
			cv::Point ptTemp1 = Point(node.vecNeighborPoint[nNextSonSi].x,node.vecNeighborPoint[nNextSonSi].y) - ptOffset;						
			cv::circle( matTemp,ptV,1,cv::Scalar( 0, 255, 255),3,8);
			cv::circle( matTemp,ptTemp0,1,cv::Scalar( 0, 255, 255),3,8);
			cv::circle( matTemp,ptTemp1,1,cv::Scalar( 0, 255, 255),3,8);
			Scalar color(255,255,0);
			line(matTemp,ptTemp0,ptV,color,2);	
			line(matTemp,ptV,ptTemp1,color,2);
			m_matImage = matTemp.clone();

			// 保存
			/*string strTemp = "D:\\Halo\\ArWay\\output\\direction\\";
			strTemp += m_strImageName.substr(m_strImageName.find_last_of("\\")+1);
			cv::imwrite(strTemp, matTemp);*/
	#endif
#endif

	return 0;
}

// 利用距离匹配中心点
int MergeMapData::matchMainRoadCenterInNet2(const vector<HAMapPoint>& vecMainRoad,
										   HAMapPoint haMainRoadCenterPt,
										   const vector<LinkInfo>& vecRoadNetLinkInfos,
										   const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
										   HAMapPoint& hamCenterInNet,
										   vector<LinkInfo>& vecMainRoadLinkInfosInNet,
										   std::vector<std::vector<HAMapPoint> >& vecMainRoadLinksInNet)
{
	// 参数自检
	int nNumMainRoadPt = vecMainRoad.size();
	int nNumLink = vecRoadNetLinks.size();
	if (nNumMainRoadPt<=0 || nNumLink<=0 ||
		nNumLink!=vecRoadNetLinkInfos.size())
	{
		return -1;
	}

	int nRet = 0;

	// 求中心点位置
	int nCenterSi = -1;
	nRet = getPointSite(vecMainRoad, haMainRoadCenterPt, nCenterSi);
	if (nRet<0)
	{
		return -1;
	}
	
	// 求中心点前后各一个节点（与中心点的距离有要求，如大于10个像素），用于计算夹角
	HAMapPoint hamPrePt, hamNextPt;
	nRet = getNearestPoint(vecMainRoad,	nCenterSi, true, hamPrePt);
	if (nRet<0)
	{
		return -1;
	}
	nRet = getNearestPoint(vecMainRoad,	nCenterSi, false, hamNextPt);
	if (nRet<0)
	{
		return -1;
	}

	// 取中心点与前、后临近点连线上的所有点
	vector<HAMapPoint> vecSubMainRoad;
	vecSubMainRoad.push_back(hamPrePt);
	nRet = getPointsBetweenTwoPoints(hamPrePt,haMainRoadCenterPt,vecSubMainRoad);
	if (nRet<0)
	{
		return -1;
	}
	vecSubMainRoad.push_back(haMainRoadCenterPt);
	int nCenterSiInSubMainRoad = vecSubMainRoad.size() - 1;		// 中心点在子路中的位置
	nRet = getPointsBetweenTwoPoints(haMainRoadCenterPt,hamNextPt,vecSubMainRoad);
	if (nRet<0)
	{
		return -1;
	}
	vecSubMainRoad.push_back(hamNextPt);

	// 缩短子路，使得中心点两侧点数相等，保证两边比重一致
	int nDel = vecSubMainRoad.size() - nCenterSiInSubMainRoad - 1;
	vector<HAMapPoint> vecTemp;
	if (nDel>nCenterSiInSubMainRoad)	// 中心点之后的点数多
	{
		vecTemp.insert(vecTemp.begin(),vecSubMainRoad.begin(),vecSubMainRoad.begin()+2*nCenterSiInSubMainRoad+1);
	} 
	else	// 中心点之前的点数多
	{
		vecTemp.insert(vecTemp.begin(),vecSubMainRoad.begin()+nCenterSiInSubMainRoad-nDel,vecSubMainRoad.end());
	}
	vecSubMainRoad.clear();
	vecSubMainRoad = vecTemp;
	vecTemp.clear();
	nCenterSiInSubMainRoad = vecSubMainRoad.size()/2;

	// 求中心夹角
	Vec2f vMainRoad1(hamPrePt.x-haMainRoadCenterPt.x, hamPrePt.y-haMainRoadCenterPt.y);		// 前一个点与中心点构成的向量
	Vec2f vMainRoad2(hamNextPt.x-haMainRoadCenterPt.x, hamNextPt.y-haMainRoadCenterPt.y);	// 后一个点与中心点构成的向量	
	float fMainAngle = getAngle(vMainRoad1, vMainRoad2);

	// 构造link端点节点
	vector<LinkEndPointNode> vecLinkEndPtnode;
	nRet = formLinkEndPointNode(vecRoadNetLinks, vecRoadNetLinkInfos, vecLinkEndPtnode);
	if (nRet<0)
	{
		return -1;
	}


	// =================绘图==================
#ifdef _WINDOWS_VER_
	#if IS_DRAW1	// 绘图
		Mat matTemp1(m_matImage.rows,m_matImage.cols,CV_8UC3);
		matTemp1.setTo(0);
		int offset_x1 =0,offset_y1 = 0;
		offset_x1 = haMainRoadCenterPt.x - matTemp1.cols/2;
		offset_y1 = haMainRoadCenterPt.y - matTemp1.rows/2;
		cv::Point ptTemp = cv::Point(haMainRoadCenterPt.x, haMainRoadCenterPt.y) - cv::Point(offset_x1,offset_y1);
		cv::circle( matTemp1,ptTemp,2,cv::Scalar( 0, 255, 0),2,8);	
		for (int i=0; i<vecLinkEndPtnode.size(); i++)
		{
			LinkEndPointNode node = vecLinkEndPtnode[i];
			HAMapPoint hamEndPt = node.hamEndPoint;

			// 区域限制
			if ((abs(hamEndPt.x-haMainRoadCenterPt.x)>CENTER_COVER || 
				abs(hamEndPt.y-haMainRoadCenterPt.y)>CENTER_COVER))
			{
				continue;
			}

			cv::Point ptTemp = cv::Point(hamEndPt.x, hamEndPt.y) - cv::Point(offset_x1,offset_y1);
			cv::circle( matTemp1,ptTemp,2,cv::Scalar( 0, 0, 255 ),2,8);
			for (int j=0; j<node.vecNeighborPoint.size(); j++)
			{
				vector<HAMapPoint> vecTemp;
				vecTemp.push_back(hamEndPt);
				vecTemp.push_back(node.vecNeighborPoint[j]);
				drawImage(matTemp1,haMainRoadCenterPt,vecTemp,cv::Scalar(0,0,0));
				cv::circle( matTemp1,Point2i(node.vecNeighborPoint[j].x,node.vecNeighborPoint[j].y),
					1,cv::Scalar( 0, 255, 0),1,8);
			}
			imshow("matTemp1",matTemp1);
			waitKey(0);
		}
	#endif
#endif
	// =====================================

#ifdef _WINDOWS_VER_
	#if IS_DRAW	// 绘图
		Mat matTemp(m_matImage.rows,m_matImage.cols,CV_8UC3);
		matTemp.setTo(0);
		int offsetx =0,offsety = 0;
		offsetx = haMainRoadCenterPt.x - matTemp.cols/2;
		offsety = haMainRoadCenterPt.y - matTemp.rows/2;
		cv::Point ptTemp = cv::Point(haMainRoadCenterPt.x, haMainRoadCenterPt.y) - cv::Point(offsetx,offsety);
		cv::circle( matTemp,ptTemp,2,cv::Scalar( 0, 255, 0),2,8);	
		drawImage(matTemp,haMainRoadCenterPt,vecSubMainRoad,cv::Scalar(0,0,255));
	#endif
#endif

	// 角度匹配
	int nMatchSi = -1;		// 记录匹配位置
	float fMinDelAngle = 360.f;		// 记录夹角之差的最小绝对值
	int nEndPtNum = vecLinkEndPtnode.size();
	LinkEndPointNode node;
	double uMinError= -1.f;
	for (int i=0; i<nEndPtNum; i++)
	{
		node = vecLinkEndPtnode[i];
		int nNodeIDNum = node.vecLinkId.size();
		if (nNodeIDNum<2)
		{
			continue;
		}

		HAMapPoint hamEndPt = node.hamEndPoint;	

		// 区域限制
		if ((abs(hamEndPt.x-haMainRoadCenterPt.x)>CENTER_COVER || 
			abs(hamEndPt.y-haMainRoadCenterPt.y)>CENTER_COVER))
		{
			continue;
		}

		// 求与主路中心点前后两向量最接近的两向量		
		int nSi0=-1, nSi1=-1;
		Vec2f v0, v1;
		for (int j=0; j<2; j++)
		{
			Vec2f vMainRoad = vMainRoad1;	// 中心点之前的向量
			if (j>0)
			{				
				vMainRoad = vMainRoad2;	// 中心点之后的向量
			}
			
			float fMinAngle = 360.f;
			int nTempSi=0;
			Vec2f vTemp;
			
			for (int k=0; k<nNodeIDNum; k++)
			{
				HAMapPoint hamPt = node.vecNeighborPoint[k];
				Vec2f v(hamPt.x-hamEndPt.x,hamPt.y-hamEndPt.y);
			
			#if 0
				// ===================方向限制==============
				// 方向判断 int direction;//道路方向:0未调查,默认双向,1双向,2正方向(link的起点到终点),3反方向
				int nLinkId = node.vecLinkId[k];
				int nDirection = vecRoadNetLinkInfos[nLinkId].direction;
				bool bIsEnd = (hamEndPt==(HAMapPoint)vecRoadNetLinks[nLinkId][0])?false:true;		// 标识端点是否属于link的终点
				if ((bIsEnd==true&&nDirection==3) || (bIsEnd==false&&nDirection==2))
				{
					continue;
				}
				// =====================================
			#endif

				float fAngle = getAngle(vMainRoad, v);				
				if (fAngle<fMinAngle)
				{
					fMinAngle = fAngle;
					nTempSi = k;
					vTemp = v;
				}				
			}	// End k
			if (fMinAngle>ANGLE_ALLOWANCE)
			{
				break;
			}
			else
			{				
				if (j==0)
				{
					nSi0 = nTempSi;
					v0 = vTemp;
				} 
				else
				{
					nSi1 = nTempSi;
					v1 = vTemp;
				}
			}
		}	// End j
		if (nSi0<0 || nSi1<0 || nSi0==nSi1)
		{
			continue;
		}
		
	#if 0
		// 求夹角		
		float fAngle = getAngle(v0,v1);
		float fDelAngle = abs(fMainAngle-fAngle);
		if (fDelAngle<fMinDelAngle)
		{
			fMinDelAngle = fDelAngle;
			nMatchSi = i;
		}
	#endif


		// =================绘图==================
		#ifdef _WINDOWS_VER_
			#if IS_DRAW	// 绘图
					//Mat matTemp(m_matImage.rows,m_matImage.cols,CV_8UC3);
					//matTemp.setTo(0);
					//int offset_x =0,offset_y = 0;
					offsetx = haMainRoadCenterPt.x - matTemp.cols/2;
					offsety = haMainRoadCenterPt.y - matTemp.rows/2;
					cv::Point ptTemp = cv::Point(hamEndPt.x, hamEndPt.y) - cv::Point(offsetx,offsety);
					cv::circle( matTemp,ptTemp,2,cv::Scalar( 0, 0, 255 ),2,8);
					vector<HAMapPoint> vecTemp;
					vecTemp.push_back(node.vecNeighborPoint[nSi0]);
					vecTemp.push_back(hamEndPt);
					vecTemp.push_back(node.vecNeighborPoint[nSi1]);
					drawImage(matTemp,haMainRoadCenterPt,vecTemp,cv::Scalar(0,0,0));
					cv::circle( matTemp,Point2i(node.vecNeighborPoint[nSi0].x,node.vecNeighborPoint[nSi0].y),
								1,cv::Scalar( 0, 255, 0),1,8);
					cv::circle( matTemp,Point2i(node.vecNeighborPoint[nSi1].x,node.vecNeighborPoint[nSi1].y),
								1,cv::Scalar( 0, 255, 0),1,8);
					imshow("matTemp",matTemp);
					waitKey(0);
			#endif
		#endif
		// =====================================

	#if 1
		// 求距离
		double uError = 0.f;
		nRet = getMainSubLine2SubNetDis(vecSubMainRoad, 
										nCenterSiInSubMainRoad,
										node.vecNeighborPoint[nSi0],
										hamEndPt,
										node.vecNeighborPoint[nSi1],										
										uError);
		if (nRet<0)
		{
			continue;
		}
		if (uMinError<0)
		{
			uMinError = uError;
			nMatchSi = i;
		}
		else
		{
			nMatchSi = (uError<uMinError)?i:nMatchSi;
			uMinError = (uError<uMinError)?uError:uMinError;
		}
	#endif

	}		// End i

	if (nMatchSi<0)
	{
		return -1;
	}

	// 输出
	//nMatchSi = 5;
	hamCenterInNet = vecLinkEndPtnode[nMatchSi].hamEndPoint;
	

	// 画图
#ifdef _WINDOWS_VER_
	//Mat matNavi(800,800,CV_8UC3);
	//matNavi.setTo(0);
	Mat matNavi = m_matImage;
	drawImage(matNavi, haMainRoadCenterPt, vecMainRoad,Scalar(255,0,0));		// 原始主路	
	for (int i=0; i<nNumLink; i++)
	{
		drawImage(matNavi,haMainRoadCenterPt, vecRoadNetLinks[i],Scalar(0,0,0));
	}

	// 匹配的主路
#if 0
	vector<HAMapPoint> vecNewMainRoad(nNumMainRoadPt);
	int nDx = hamCenterInNet.x - haMainRoadCenterPt.x;
	int nDy = hamCenterInNet.y - haMainRoadCenterPt.y;
	for (int i=0; i<nNumMainRoadPt; i++)
	{
		vecNewMainRoad[i].x = vecMainRoad[i].x+nDx;
		vecNewMainRoad[i].y = vecMainRoad[i].y+nDy;
	}
	drawImage(matNavi, haMainRoadCenterPt, vecNewMainRoad,Scalar(0,255,0));		// 新主路
#else
	int nSubMainRoadNum = vecSubMainRoad.size();
	vector<HAMapPoint> vecNewMainRoad(nSubMainRoadNum);
	int nDx = hamCenterInNet.x - haMainRoadCenterPt.x;
	int nDy = hamCenterInNet.y - haMainRoadCenterPt.y;
	for (int i=0; i<nSubMainRoadNum; i++)
	{
		vecNewMainRoad[i].x = vecSubMainRoad[i].x+nDx;
		vecNewMainRoad[i].y = vecSubMainRoad[i].y+nDy;
	}
	drawImage(matNavi, haMainRoadCenterPt, vecNewMainRoad,Scalar(0,255,0));		// 新主路
#endif


	// 两个中心点
	int offset_x =0,offset_y = 0;
	offset_x = haMainRoadCenterPt.x - matNavi.cols/2;
	offset_y = haMainRoadCenterPt.y - matNavi.rows/2;
	cv::Point ptOld = cv::Point(haMainRoadCenterPt.x, haMainRoadCenterPt.y) - cv::Point(offset_x,offset_y);
	cv::Point ptNew = cv::Point(hamCenterInNet.x, hamCenterInNet.y) - cv::Point(offset_x,offset_y);
	cv::circle( matNavi,ptOld,2,cv::Scalar( 0, 0, 255 ),2,8);
	cv::circle( matNavi,ptNew,2,cv::Scalar( 0, 0, 255 ),2,8);

	#if IS_DRAW
		imshow("matNavi",matNavi);
		waitKey(0);
	#endif

#endif

	return 0;
}

// 利用整个屏幕内的主路起点、中心点、终点进行匹配
int MergeMapData::matchMainRoadCenterInNet3(const vector<HAMapPoint>& vecMainRoad,
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
											std::vector<std::vector<HAMapPoint> >& vecCrossGpsLinks)
{
	// 参数自检
	int nNumMainRoadPt = vecMainRoad.size();
	int nNumLink = vecRoadNetLinks.size();
	if (nNumMainRoadPt<=0 || nNumLink<=0 ||
		nNumLink!=vecRoadNetLinkInfos.size())
	{
		// 打印log		
		#ifdef _WINDOWS_VER_
			printf("==============matchMainRoadCenterInNet3 - parameter Error!!==============\n");
			m_strErrorLog = "matchMainRoadCenterInNet3 - parameter Error!!";
		#else
			LOGD("==============matchMainRoadCenterInNet3 - parameter Error!!==============\n");
		#endif
		return -1;
	}

	int nRet = 0;

	// 求中心点位置
	int nCenterSi = -1;
	nRet = getPointSite(vecMainRoad, haMainRoadCenterPt, nCenterSi);
	if (nRet<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet3 - getPointSite Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet3 - getPointSite Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet3 - getPointSite Error!!==============\n");
	#endif
		return -1;
	}
		
	// 获取屏幕边界上的主路起点、终点
	HAMapPoint hamPrePt, hamNextPt;
#if 1
	nRet = getStartEndPoint(vecMainRoad, nCenterSi, rtScreen, hamPrePt, hamNextPt);
	if (nRet<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet3 - getStartEndPoint Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet3 - getStartEndPoint Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet3 - getStartEndPoint Error!!==============\n");
	#endif
		return -1;
	}
#else
	hamPrePt = vecMainRoad[0];
	hamNextPt = vecMainRoad[nNumMainRoadPt-1];
#endif


#if 0
	// 求中心点前后各一个节点（与中心点的距离有要求，如大于10个像素），用于计算夹角
	HAMapPoint hamPrePt, hamNextPt;
	nRet = getNearestPoint(vecMainRoad,	nCenterSi, true, hamPrePt);
	if (nRet<0)
	{
		return -1;
	}
	nRet = getNearestPoint(vecMainRoad,	nCenterSi, false, hamNextPt);
	if (nRet<0)
	{
		return -1;
	}

	// 取中心点与前、后临近点连线上的所有点
	vector<HAMapPoint> vecSubMainRoad;
	vecSubMainRoad.push_back(hamPrePt);
	nRet = getPointsBetweenTwoPoints(hamPrePt,haMainRoadCenterPt,vecSubMainRoad);
	if (nRet<0)
	{
		return -1;
	}
	vecSubMainRoad.push_back(haMainRoadCenterPt);
	int nCenterSiInSubMainRoad = vecSubMainRoad.size() - 1;		// 中心点在子路中的位置
	nRet = getPointsBetweenTwoPoints(haMainRoadCenterPt,hamNextPt,vecSubMainRoad);
	if (nRet<0)
	{
		return -1;
	}
	vecSubMainRoad.push_back(hamNextPt);

	// 缩短子路，使得中心点两侧点数相等，保证两边比重一致
	int nDel = vecSubMainRoad.size() - nCenterSiInSubMainRoad - 1;
	vector<HAMapPoint> vecTemp;
	if (nDel>nCenterSiInSubMainRoad)	// 中心点之后的点数多
	{
		vecTemp.insert(vecTemp.begin(),vecSubMainRoad.begin(),vecSubMainRoad.begin()+2*nCenterSiInSubMainRoad+1);
	} 
	else	// 中心点之前的点数多
	{
		vecTemp.insert(vecTemp.begin(),vecSubMainRoad.begin()+nCenterSiInSubMainRoad-nDel,vecSubMainRoad.end());
	}
	vecSubMainRoad.clear();
	vecSubMainRoad = vecTemp;
	vecTemp.clear();
	nCenterSiInSubMainRoad = vecSubMainRoad.size()/2;
#endif

	// 求中心夹角
	Vec2f vMainRoad1(hamPrePt.x-haMainRoadCenterPt.x, hamPrePt.y-haMainRoadCenterPt.y);		// 前一个点与中心点构成的向量
	Vec2f vMainRoad2(hamNextPt.x-haMainRoadCenterPt.x, hamNextPt.y-haMainRoadCenterPt.y);	// 后一个点与中心点构成的向量	
	float fMainAngle = getAngle(vMainRoad1, vMainRoad2);

	// 构造link端点节点
	vector<LinkEndPointNode> vecLinkEndPtnode;
	nRet = formLinkEndPointNode(vecRoadNetLinks, vecRoadNetLinkInfos, vecLinkEndPtnode);
	if (nRet<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet3 - formLinkEndPointNode Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet3 - formLinkEndPointNode Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet3 - formLinkEndPointNode Error!!==============\n");
	#endif
		return -1;
	}


	// =================绘图==================
#ifdef _WINDOWS_VER_
#if IS_DRAW1	// 绘图
	Mat matTemp1(m_matImage.rows,m_matImage.cols,CV_8UC3);
	matTemp1.setTo(0);
	int offset_x1 =0,offset_y1 = 0;
	offset_x1 = haMainRoadCenterPt.x - matTemp1.cols/2;
	offset_y1 = haMainRoadCenterPt.y - matTemp1.rows/2;
	cv::Point ptTemp = cv::Point(haMainRoadCenterPt.x, haMainRoadCenterPt.y) - cv::Point(offset_x1,offset_y1);
	cv::circle( matTemp1,ptTemp,2,cv::Scalar( 0, 255, 0),2,8);	
	for (int i=0; i<vecLinkEndPtnode.size(); i++)
	{
		LinkEndPointNode node = vecLinkEndPtnode[i];
		HAMapPoint hamEndPt = node.hamEndPoint;

		// 区域限制
		if ((abs(hamEndPt.x-haMainRoadCenterPt.x)>CENTER_COVER || 
			abs(hamEndPt.y-haMainRoadCenterPt.y)>CENTER_COVER))
		{
			continue;
		}

		cv::Point ptTemp = cv::Point(hamEndPt.x, hamEndPt.y) - cv::Point(offset_x1,offset_y1);
		cv::circle( matTemp1,ptTemp,2,cv::Scalar( 0, 0, 255 ),2,8);
		for (int j=0; j<node.vecNeighborPoint.size(); j++)
		{
			vector<HAMapPoint> vecTemp;
			vecTemp.push_back(hamEndPt);
			vecTemp.push_back(node.vecNeighborPoint[j]);
			drawImage(matTemp1,haMainRoadCenterPt,vecTemp,cv::Scalar(0,0,0));
			cv::circle( matTemp1,Point2i(node.vecNeighborPoint[j].x,node.vecNeighborPoint[j].y),
				1,cv::Scalar( 0, 255, 0),1,8);
		}
		imshow("matTemp1",matTemp1);
		waitKey(0);		
	}
#endif
#endif
	// =====================================

#ifdef _WINDOWS_VER_
#if IS_DRAW || IS_DRAW2	// 绘图
	Mat matTemp(m_matImage.rows,m_matImage.cols,CV_8UC3);
	matTemp.setTo(0);
	int offsetx =0,offsety = 0;
	offsetx = haMainRoadCenterPt.x - matTemp.cols/2;
	offsety = haMainRoadCenterPt.y - matTemp.rows/2;
	cv::Point ptOffset(offsetx,offsety);
	//m_ptOffset = ptOffset;		// 成员赋值
	cv::Point ptTemp = cv::Point(haMainRoadCenterPt.x, haMainRoadCenterPt.y) - ptOffset;
	cv::circle( matTemp,ptTemp,2,cv::Scalar( 0, 255, 0),2,8);	
	drawImage(matTemp,haMainRoadCenterPt,/*vecSubMainRoad*/vecMainRoad,cv::Scalar(255,0,0));
	vector<HAMapPoint> vecFirstHalfMainRoad, vecSecondHalfMainRoad;		// 记录前后半段主路
	vecFirstHalfMainRoad.insert(vecFirstHalfMainRoad.begin(),vecMainRoad.begin(),vecMainRoad.begin()+nCenterSi+1);
	vecSecondHalfMainRoad.insert(vecSecondHalfMainRoad.begin(),vecMainRoad.begin()+nCenterSi+1,vecMainRoad.end());
	drawImage(matTemp,haMainRoadCenterPt,vecFirstHalfMainRoad,cv::Scalar(255,0,255),1);		// 前半段，紫色
	drawImage(matTemp,haMainRoadCenterPt,vecSecondHalfMainRoad,cv::Scalar(0,255,255),1);	// 后半段，黄色
#endif
#if IS_DRAW2	// 绘图
	for (int i=0; i<nNumLink; i++)
	{
		drawImage(matTemp,haMainRoadCenterPt,vecRoadNetLinks[i],cv::Scalar(0,0,255),1);
	}
	cv::Point ptPre = cv::Point(hamPrePt.x, hamPrePt.y) - ptOffset;
	cv::Point ptNext = cv::Point(hamNextPt.x, hamNextPt.y) - ptOffset;
	Scalar colorMainRoad(255,255,0);
	//line(matTemp,ptTemp,ptPre,colorMainRoad,1);	
	//line(matTemp,ptTemp,ptNext,colorMainRoad,1);	

	// 绘制所有端点
	for (int i=0; i<vecLinkEndPtnode.size(); i++)
	{
		HAMapPoint hamPtTemp = vecLinkEndPtnode[i].hamEndPoint;
		cv::Point ptTemp = cv::Point(hamPtTemp.x,hamPtTemp.y) - ptOffset;
		cv::circle( matTemp,ptTemp,2,cv::Scalar( 255, 255, 255),2,8);
	}

	#if IS_SON_DRAW
		imshow("matTemp",matTemp);
		cv::waitKey(0);
	#endif
#endif
#endif


	// 角度匹配
	int nMatchSi = -1, nPreSonSi = -1, nNextSonSi = -1;		// 记录匹配位置
	float fMinDelAngle = 360.f;		// 记录夹角之差的最小绝对值
	int nEndPtNum = vecLinkEndPtnode.size();
	LinkEndPointNode node;
	double uMinError= -1.f;
	HAMapPoint hamMatchPrePt, hamMatchPt, hamMatchNextPt;	// 匹配点之前与边界点交点、匹配点、匹配点之后与边界点交点
	//vector<vector<int> > vecMatchPath;		// 记录匹配的路径
	//vector<vector<int> > vecMatchPathNodeId;		// 记录匹配路径上的Node Id
	vector<int> vecMatchPath;		// 记录匹配的路径
	vector<int> vecMatchPathNodeId;		// 记录匹配路径上的Node Id
	vector<int> vecOptionNodeId;		// 记录备选点集合
	double uMinBorderDis = -1;	// 记录对应边界点间最小距离
	double uMinCenterDis = -1;	// 记录匹配点到中心点的最小距离
	double uMinPathDis = -1;
	for (int i=0; i<nEndPtNum; i++)
	{
		node = vecLinkEndPtnode[i];
		int nNeighborNodeNum = node.vecNeighborNodeId.size();
		if (nNeighborNodeNum<=2)		// 邻居点限制
		{
			continue;
		}

		HAMapPoint hamEndPt = node.hamEndPoint;	

		// 区域限制
		if ((!isRectInside(hamEndPt,rtScreen))&&((abs(hamEndPt.x-haMainRoadCenterPt.x)>CENTER_COVER || 
			abs(hamEndPt.y-haMainRoadCenterPt.y)>CENTER_COVER)))
		{
			continue;
		}

#ifdef _WINDOWS_VER_
#if IS_DRAW1	// 绘图
		cv::Point ptTemp = cv::Point(hamEndPt.x, hamEndPt.y) - cv::Point(offset_x1,offset_y1);
		cv::circle( matTemp1,ptTemp,2,cv::Scalar( 0, 255, 255 ),2,8);
		for (int j=0; j<node.vecNeighborPoint.size(); j++)
		{
			vector<HAMapPoint> vecTemp;
			vecTemp.push_back(hamEndPt);
			vecTemp.push_back(node.vecNeighborPoint[j]);
			drawImage(matTemp1,haMainRoadCenterPt,vecTemp,cv::Scalar(0,255,255));
			cv::circle( matTemp1,Point2i(node.vecNeighborPoint[j].x,node.vecNeighborPoint[j].y),
				1,cv::Scalar( 0, 255, 255),1,8);
		}
		imshow("matTemp1",matTemp1);
		waitKey(0);
#endif

//#if IS_DRAW2	// 绘图
//		cv::Point ptTemp = cv::Point(hamEndPt.x, hamEndPt.y) - ptOffset;
//		cv::circle( matTemp,ptTemp,2,cv::Scalar( 0, 255, 255),2,8);
//// 		imshow("matTemp",matTemp);
//// 		waitKey(0);
//#endif
#endif
		

		// 延伸link直到超出屏幕边界
		vector<HAMapPoint> vecBorderPt;
		vector<int> vecBorderPtDirection;
		vector<vector<int> > vecPathLinkId;
		vector<vector<int> > vecPathNodeId;

		/*nRet = extendLink(vecRoadNetLinks,vecLinkEndPtnode,	i, rtScreen, vecBorderPt, 
							vecBorderPtDirection, vecPathLinkId, vecPathNodeId);*/
		nRet = extendLink1(vecRoadNetLinks,vecLinkEndPtnode,	i, rtScreen, vecBorderPt, 
 			vecBorderPtDirection, vecPathLinkId, vecPathNodeId);
		if (nRet<0 || vecBorderPt.size()<=0)
		{
			continue;
		}
		
	#ifdef _WINDOWS_VER_
		#if IS_DRAW	// 绘图
			cv::Point ptTemp = cv::Point(hamEndPt.x, hamEndPt.y) - ptOffset;
			cv::circle( matTemp,ptTemp,2,cv::Scalar( 0, 255, 255),2,8);

			for (int j=0; j<vecBorderPt.size(); j++)
			{
				cv::Point ptBorder = cv::Point(vecBorderPt[j].x, vecBorderPt[j].y) - ptOffset;
				Scalar color(0,255,0);
				line(matTemp,ptTemp,ptBorder,color,1);	
			}

			imshow("matTemp",matTemp);
			cv::waitKey(0);
		#endif
	#endif

		// 求与主路中心点前后两向量最接近的两向量
		int nBorderPtNum = vecBorderPt.size();
		int nSi0=-1, nSi1=-1;
		double uDis0 = -1, uDis1 = -1;
		Vec2f v0, v1;
		for (int j=0; j<2; j++)
		{
			Vec2f vMainRoad = vMainRoad1;	// 中心点之前的向量
			if (j>0)
			{				
				vMainRoad = vMainRoad2;	// 中心点之后的向量
			}

			float fMinAngle = 360.f;
			int nTempSi=0;
			Vec2f vTemp;

			for (int k=0; k<nBorderPtNum; k++)
			{
				if (j==0)
				{
					if (vecBorderPtDirection[k]==2)
					{
						continue;
					}
				}
				else
				{
					if (vecBorderPtDirection[k]==3)
					{
						continue;
					}
				}
				
				//LinkEndPointNode nextNode = vecLinkEndPtnode[node.vecNeighborNodeId[k]];
				//HAMapPoint hamPt = nextNode.hamEndPoint;
				HAMapPoint hamPt = vecBorderPt[k];
				Vec2f v(hamPt.x-hamEndPt.x,hamPt.y-hamEndPt.y);

#if 0
				// ===================方向限制==============
				// 方向判断 int direction;//道路方向:0未调查,默认双向,1双向,2正方向(link的起点到终点),3反方向
				int nLinkId = node.vecLinkId[k];
				int nDirection = vecRoadNetLinkInfos[nLinkId].direction;
				bool bIsEnd = (hamEndPt==(HAMapPoint)vecRoadNetLinks[nLinkId][0])?false:true;		// 标识端点是否属于link的终点
				if (j==0)	// 标识端点应为link终点
				{
					if ((bIsEnd==true&&nDirection==3) || (bIsEnd==false&&nDirection==2))
					{
						continue;
					}
				} 
				else		// 标识端点应为link起点
				{
					if ((bIsEnd==true&&nDirection==2) || (bIsEnd==false&&nDirection==3))
					{
						continue;
					}
				}
				// =====================================
#endif

				float fAngle = getAngle(vMainRoad, v);				
				if (fAngle<fMinAngle)
				{
					fMinAngle = fAngle;
					nTempSi = k;
					vTemp = v;
				}				
			}	// End k
			if (fMinAngle>ANGLE_ALLOWANCE)
			{
				break;
			}
			else
			{				
				if (j==0)
				{
					nSi0 = nTempSi;
					v0 = vTemp;
					uDis0 = sqrt((vecBorderPt[nSi0].x-hamPrePt.x)*(vecBorderPt[nSi0].x-hamPrePt.x)+
								(vecBorderPt[nSi0].y-hamPrePt.y)*(vecBorderPt[nSi0].y-hamPrePt.y));
				} 
				else
				{
					nSi1 = nTempSi;
					v1 = vTemp;
					uDis1 = sqrt((vecBorderPt[nSi1].x-hamNextPt.x)*(vecBorderPt[nSi1].x-hamNextPt.x)+
						(vecBorderPt[nSi1].y-hamNextPt.y)*(vecBorderPt[nSi1].y-hamNextPt.y));
				}

#if IS_DRAW1		// 绘图
				Mat matT(m_matImage.rows, m_matImage.cols,CV_8UC3);
				matT.setTo(0);
				vector<HAMapPoint> vecT;
				vecT.push_back(hamEndPt);
				vecT.push_back(node.vecNeighborPoint[nTempSi]);
				drawImage(matT,haMainRoadCenterPt,vecT,Scalar(0,0,0));
				imshow("matT",matT);
				waitKey(0);
#endif

				//// 保存每个节点的方向数据，绿-与主路同向，红-与主路反向
				//#ifdef _WINDOWS_VER_
				//	#if IS_DRAW2	// 绘图								
				//		cv::Point ptV = Point(hamEndPt.x,hamEndPt.y) - ptOffset;
				//		cv::Point ptTemp = Point(node.vecNeighborPoint[nTempSi].x,node.vecNeighborPoint[nTempSi].y) - ptOffset;						
				//		cv::circle( matTemp,ptV,1,cv::Scalar( 0, 255, 255),2,8);
				//		cv::circle( matTemp,ptTemp,1,cv::Scalar( 0, 255, 255),2,8);
				//		Scalar color(0,255,0);
				//		line(matTemp,ptTemp,ptV,color,1);						
				//	#endif
				//#endif

			}
		}	// End j
		if (nSi0<0 || nSi1<0 || nSi0==nSi1)
		{
			continue;
		}

		// 保存每个节点的方向数据，绿-与主路同向，红-与主路反向
#ifdef _WINDOWS_VER_
#if 0//IS_DRAW2	// 绘图								
		cv::Point ptV = Point(hamEndPt.x,hamEndPt.y) - ptOffset;
		cv::Point ptTemp0 = Point(vecBorderPt[nSi0].x,vecBorderPt[nSi0].y) - ptOffset;		
		cv::Point ptTemp1 = Point(vecBorderPt[nSi1].x,vecBorderPt[nSi1].y) - ptOffset;						
		cv::circle( matTemp,ptV,1,cv::Scalar( 0, 255, 255),2,8);
		cv::circle( matTemp,ptTemp0,1,cv::Scalar( 0, 255, 255),2,8);
		cv::circle( matTemp,ptTemp1,1,cv::Scalar( 0, 255, 255),2,8);
		Scalar color(255,255,255);
		cv::line(matTemp,ptTemp0,ptV,color,1);	
		cv::line(matTemp,ptV,ptTemp1,color,1);	
#endif
#endif

#if 0	// 利用最佳匹配角
		// 求夹角		
		float fAngle = getAngle(v0,v1);
		float fDelAngle = abs(fMainAngle-fAngle);
		if (fDelAngle<fMinDelAngle)
		{
			fMinDelAngle = fDelAngle;
			nMatchSi = i;
			hamMatchPrePt = vecBorderPt[nSi0];
			hamMatchNextPt = vecBorderPt[nSi1];
			hamMatchPt = hamEndPt;

			vecMatchPath.clear();
			vecMatchPath.push_back(vecPathLinkId[nSi0]);
			vecMatchPath.push_back(vecPathLinkId[nSi1]);

			vecMatchPathNodeId.clear();
			vecMatchPathNodeId.push_back(vecPathNodeId[nSi0]);
			vecMatchPathNodeId.push_back(vecPathNodeId[nSi1]);
		}
#else	// 记录候选点，利用边界点间距离最短做判据
		float fAngle = getAngle(v0,v1);
		float fDelAngle = abs(fMainAngle-fAngle);
		if (fDelAngle<ANGLE_ALLOWANCE)
		{
			vecOptionNodeId.push_back(i);			

			// 匹配点与中心点距离
			double uCenterDis = sqrt((haMainRoadCenterPt.x-hamEndPt.x)*(haMainRoadCenterPt.x-hamEndPt.x)+
				(haMainRoadCenterPt.y-hamEndPt.y)*(haMainRoadCenterPt.y-hamEndPt.y));

			double uPathDis0 = -1, uPathDis1 = -1, uPathDis = -1;
			nRet = getDisBorder2MatchPt(vecRoadNetLinks, vecPathLinkId[nSi0], rtScreen,
										vecBorderPt[nSi0], hamEndPt, uPathDis0);
			if (nRet<0)
			{
				continue;
			}
			nRet = getDisBorder2MatchPt(vecRoadNetLinks, vecPathLinkId[nSi1], rtScreen,
										vecBorderPt[nSi0], hamEndPt, uPathDis1);
			if (nRet<0)
			{
				continue;
			}
			uPathDis = uPathDis0 + uPathDis1;


			// 边界点间距离
			double uDis = uDis0 + uDis1;
			if (uMinBorderDis<0)
			{
				uMinBorderDis = uDis;
				nMatchSi = i;

				hamMatchPrePt = vecBorderPt[nSi0];
				hamMatchNextPt = vecBorderPt[nSi1];

				//vecMatchPath.clear();
				//vecMatchPath.push_back(vecPathLinkId[nSi0]);
				//vecMatchPath.push_back(vecPathLinkId[nSi1]);

				/*vecMatchPathNodeId.clear();
				vecMatchPathNodeId.push_back(vecPathNodeId[nSi0]);
				vecMatchPathNodeId.push_back(vecPathNodeId[nSi1]);*/

				vecMatchPath.clear();
				vecMatchPath = vecPathLinkId[nSi0];
				reverseOrder(vecMatchPath);		// 倒序重排，与主路方向一致				
				vecMatchPath.insert(vecMatchPath.end(),vecPathLinkId[nSi1].begin(),vecPathLinkId[nSi1].end());

				vecMatchPathNodeId.clear();
				vecMatchPathNodeId = vecPathNodeId[nSi0];
				reverseOrder(vecMatchPathNodeId);		// 倒序重排，与主路方向一致
				vecMatchPathNodeId.erase(vecMatchPathNodeId.end()-1);
				vecMatchPathNodeId.insert(vecMatchPathNodeId.end(),vecPathNodeId[nSi1].begin(),vecPathNodeId[nSi1].end());				

				uMinCenterDis = uCenterDis;
				uMinPathDis = uPathDis;
			} 
			else
			{
				if (uDis<uMinBorderDis)
				{
					uMinBorderDis = uDis;
					nMatchSi = i;

					hamMatchPrePt = vecBorderPt[nSi0];
					hamMatchNextPt = vecBorderPt[nSi1];

					/*vecMatchPath.clear();
					vecMatchPath.push_back(vecPathLinkId[nSi0]);
					vecMatchPath.push_back(vecPathLinkId[nSi1]);

					vecMatchPathNodeId.clear();
					vecMatchPathNodeId.push_back(vecPathNodeId[nSi0]);
					vecMatchPathNodeId.push_back(vecPathNodeId[nSi1]);*/

					vecMatchPath.clear();
					vecMatchPath = vecPathLinkId[nSi0];
					reverseOrder(vecMatchPath);		// 倒序重排，与主路方向一致				
					vecMatchPath.insert(vecMatchPath.end(),vecPathLinkId[nSi1].begin(),vecPathLinkId[nSi1].end());

					vecMatchPathNodeId.clear();
					vecMatchPathNodeId = vecPathNodeId[nSi0];
					reverseOrder(vecMatchPathNodeId);		// 倒序重排，与主路方向一致
					vecMatchPathNodeId.erase(vecMatchPathNodeId.end()-1);
					vecMatchPathNodeId.insert(vecMatchPathNodeId.end(),vecPathNodeId[nSi1].begin(),vecPathNodeId[nSi1].end());	

					uMinCenterDis = uCenterDis;
					uMinPathDis = uPathDis;
				}
				else if (abs(uDis-uMinBorderDis)<1e-10)		// 相等时，基于路径长度
				{
					if (uPathDis<uMinPathDis)
					{
						uMinBorderDis = uDis;
						nMatchSi = i;

						hamMatchPrePt = vecBorderPt[nSi0];
						hamMatchNextPt = vecBorderPt[nSi1];

						/*vecMatchPath.clear();
						vecMatchPath.push_back(vecPathLinkId[nSi0]);
						vecMatchPath.push_back(vecPathLinkId[nSi1]);

						vecMatchPathNodeId.clear();
						vecMatchPathNodeId.push_back(vecPathNodeId[nSi0]);
						vecMatchPathNodeId.push_back(vecPathNodeId[nSi1]);*/

						vecMatchPath.clear();
						vecMatchPath = vecPathLinkId[nSi0];
						reverseOrder(vecMatchPath);		// 倒序重排，与主路方向一致				
						vecMatchPath.insert(vecMatchPath.end(),vecPathLinkId[nSi1].begin(),vecPathLinkId[nSi1].end());

						vecMatchPathNodeId.clear();
						vecMatchPathNodeId = vecPathNodeId[nSi0];
						reverseOrder(vecMatchPathNodeId);		// 倒序重排，与主路方向一致
						vecMatchPathNodeId.erase(vecMatchPathNodeId.end()-1);
						vecMatchPathNodeId.insert(vecMatchPathNodeId.end(),vecPathNodeId[nSi1].begin(),vecPathNodeId[nSi1].end());	

						uMinCenterDis = uCenterDis;
						uMinPathDis = uPathDis;
					}
				}
			}			
		}

#endif


		// =================绘图==================
#ifdef _WINDOWS_VER_
#if IS_DRAW	// 绘图
		//Mat matTemp(m_matImage.rows,m_matImage.cols,CV_8UC3);
		//matTemp.setTo(0);
		//int offset_x =0,offset_y = 0;
		offsetx = haMainRoadCenterPt.x - matTemp.cols/2;
		offsety = haMainRoadCenterPt.y - matTemp.rows/2;
		cv::Point ptTemp = cv::Point(hamEndPt.x, hamEndPt.y) - cv::Point(offsetx,offsety);
		cv::circle( matTemp,ptTemp,2,cv::Scalar( 0, 0, 255 ),2,8);
		vector<HAMapPoint> vecTemp;
		vecTemp.push_back(node.vecNeighborPoint[nSi0]);
		vecTemp.push_back(hamEndPt);
		vecTemp.push_back(node.vecNeighborPoint[nSi1]);
		drawImage(matTemp,haMainRoadCenterPt,vecTemp,cv::Scalar(0,0,0));
		cv::circle( matTemp,Point2i(node.vecNeighborPoint[nSi0].x,node.vecNeighborPoint[nSi0].y),
			1,cv::Scalar( 0, 255, 0),1,8);
		cv::circle( matTemp,Point2i(node.vecNeighborPoint[nSi1].x,node.vecNeighborPoint[nSi1].y),
			1,cv::Scalar( 0, 255, 0),1,8);
		imshow("matTemp",matTemp);
		waitKey(0);
#endif
#endif
		// =====================================
#if 0
		// 求距离
		double uError = 0.f;
		nRet = getMainSubLine2SubNetDis(vecSubMainRoad, 
			nCenterSiInSubMainRoad,
			node.vecNeighborPoint[nSi0],
			hamEndPt,
			node.vecNeighborPoint[nSi1],										
			uError);
		if (nRet<0)
		{
			continue;
		}
		if (uMinError<0)
		{
			uMinError = uError;
			nMatchSi = i;
		}
		else
		{
			nMatchSi = (uError<uMinError)?i:nMatchSi;
			uMinError = (uError<uMinError)?uError:uMinError;
		}
#endif
	}		// End i

	if (nMatchSi<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet3 - no Match Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet3 - no Match Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet3 - no Match Error!!==============\n");
	#endif
		return -1;
	}


	// 输出
	//nMatchSi = 29;
	hamCenterInNet = vecLinkEndPtnode[nMatchSi].hamEndPoint;
	

	// 由一个点构造路网
	//vector<int> vecMainRoadNodeId;
// 	for (int i=0; i<vecMatchPathNodeId.size(); i++)
// 	{
// 		vecMainRoadNodeId.insert(vecMainRoadNodeId.end(),vecMatchPathNodeId[i].begin(),vecMatchPathNodeId[i].end());
// 	}

	vector<int> vecRoadNetDirection2MainRoad;
	nRet = formRoadNet(vecRoadNetLinks,
		vecLinkEndPtnode,
		/*vecMainRoadNodeId*/vecMatchPathNodeId,
		rtScreen,
		vecRoadNetDirection2MainRoad);
	if (nRet < 0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet3 - formRoadNet Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet3 - formRoadNet Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet3 - formRoadNet Error!!==============\n");
	#endif
		return -1;
	}

	// 过滤路网
	/*vector<int> vecMainRoadLinkId;
	for (int i=0; i<vecMatchPath.size(); i++)
	{
		vecMainRoadLinkId.insert(vecMainRoadLinkId.end(),vecMatchPath[i].begin(),vecMatchPath[i].end());
	}*/
	nRet = filterRoadNet(vecRoadNetLinks,
						vecRoadNetDirection2MainRoad,
						/*vecMainRoadLinkId*/vecMatchPath,
						vecCrossGpsLinks);
	if (nRet<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet3 - filterRoadNet Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet3 - filterRoadNet Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet3 - filterRoadNet Error!!==============\n");
	#endif
		return -1;
	}

	// 添加匹配的主路点，及与岔路交点在主路中的下标
	vector<HAMapPoint> vecMatchPathPt;

	vecMatchPathPt.push_back(hamMatchPrePt);		// 第一个点
	for (int i=0; i<vecMatchPath.size(); i++)
	{
		int nLinkId = vecMatchPath[i];
		if (nLinkId<0)
		{
			continue;
		}

		// link两个端点对应的node Id
		int nPathStartNodeId = vecMatchPathNodeId[i];
 		int nPathEndNodeId = vecMatchPathNodeId[i+1];

		vector<HAMapPoint> vecTemp = vecRoadNetLinks[nLinkId];
		// 判断方向
		if (vecTemp[0]==vecLinkEndPtnode[nPathEndNodeId].hamEndPoint)
		{
			reverseOrder(vecTemp);
		}
		
		int nTempNum = vecTemp.size();
		for (int j=0; j<nTempNum-1; j++)
		{
			HAMapPoint hamPt = vecTemp[j];			
			if (isRectInside(hamPt, rtScreen) && 
				hamPt!=vecMatchPathPt[vecMatchPathPt.size()-1])
			{
				// 求岔路与主路交点在主路中的位置
				if (j==0 && vecLinkEndPtnode[nPathStartNodeId].vecNeighborNodeId.size()>2)
				{
					vecCrossPointIndex.push_back(vecMatchPathPt.size());
				}
				
				vecMatchPathPt.push_back(hamPt);					
			}
		} // end j
	}	// end i
	
	vecMatchPathPt.push_back(hamMatchNextPt);	// 最后一个点

	
	// 求匹配点在集合中的位置
	nMatchCenterIndex = -1;
	for (int i=0; i<vecMatchPathPt.size(); i++)
	{
		if (hamCenterInNet==vecMatchPathPt[i])
		{
			nMatchCenterIndex = i;
			break;
		}
	}
	if (nMatchCenterIndex<0)
	{
		return -1;
	}


	//vecMatchPathPt.push_back(hamMatchPrePt);		// 第一个点
	//for (int i=0; i<vecMatchPath.size(); i++)
	//{
	//	vector<int> vecSinglePathLink = vecMatchPath[i];
	//	vector<int> vecSinglePathNode = vecMatchPathNodeId[i];		
	//	for (int j=0; j<vecSinglePathLink.size(); j++)
	//	{
	//		int nLinkId = vecSinglePathLink[j];
	//		if (nLinkId<0)
	//		{
	//			continue;
	//		}

	//		// link两个端点对应的node Id
	//		int nPathStartNodeId = vecSinglePathNode[j];
	//		int nPathEndNodeId = vecSinglePathNode[j+1];

	//		int nTempNum = vecRoadNetLinks[nLinkId].size();
	//		for (int k=0; k<nTempNum; k++)
	//		{
	//			HAMapPoint hamPt = vecRoadNetLinks[nLinkId][k];
	//			if (IsRectInside(hamPt, rtScreen))
	//			{
	//				if (k==0 && ((vecLinkEndPtnode[nPathStartNodeId].hamEndPoint==hamPt && vecLinkEndPtnode[nPathStartNodeId].vecNeighborNodeId.size()>2))||
	//					(vecLinkEndPtnode[nPathEndNodeId].hamEndPoint==hamPt && vecLinkEndPtnode[nPathEndNodeId].vecNeighborNodeId.size()>2))
	//				{
	//					vecCrossPointIndex.push_back(vecMatchPathPt.size());		// 添加下标
	//				}
	//				vecMatchPathPt.push_back(hamPt);
	//			}
	//		} // end k			
	//	}	// end j	
	//} // end i
	//vecMatchPathPt.push_back(hamMatchNextPt);	// 最后一个点

	//vecCrossGpsLinks.insert(vecCrossGpsLinks.begin(),vecMatchPathPt);		// 添加，主路作为第0个
	vecCrossGpsLinks.push_back(vecMatchPathPt);		// 添加，主路作为最后一个，方便平移

	// 计算从匹配路径移至主路的最佳平移距离
	vector<HAMapPoint> vecMainRoadVertex, vecMatchRoadVertex;
	vecMainRoadVertex.push_back(hamPrePt);
	vecMainRoadVertex.push_back(hamNextPt);
	vecMatchRoadVertex.push_back(hamMatchPrePt);
	vecMatchRoadVertex.push_back(hamMatchNextPt);
	HAMapPoint hamOffset;
	nRet = getOffset2MainRoad(vecMainRoadVertex, vecMatchRoadVertex, hamOffset);
	if (nRet<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet3 - getOffset2MainRoad Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet3 - getOffset2MainRoad Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet3 - getOffset2MainRoad Error!!==============\n");
	#endif
		return -1;
	}

	// 平移
	nRet = translateRoadNet(vecCrossGpsLinks, hamOffset);
	if (nRet<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet3 - translateRoadNet Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet3 - translateRoadNet Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet3 - translateRoadNet Error!!==============\n");
	#endif
		return -1;
	}

	// 获取平移后的主路匹配点
	vecMainRoadPtInNet = vecCrossGpsLinks[vecCrossGpsLinks.size()-1];
	vecCrossGpsLinks.erase(vecCrossGpsLinks.end()-1);		// 去掉最后一个vector元素，除去主路

	hamCenterInNet = vecMainRoadPtInNet[nMatchCenterIndex];

	// 画图
#ifdef _WINDOWS_VER_
#if IS_DRAW
	//Mat matNavi(800,800,CV_8UC3);
	//matNavi.setTo(0);
	Mat matNavi = m_matImage;
	drawImage(matNavi, haMainRoadCenterPt, vecMainRoad,Scalar(255,0,0));		// 原始主路	
	for (int i=0; i<nNumLink; i++)
	{
		drawImage(matNavi,haMainRoadCenterPt, vecRoadNetLinks[i],Scalar(0,0,0));
	}

	// 匹配的主路
#if 0
	vector<HAMapPoint> vecNewMainRoad(nNumMainRoadPt);
	int nDx = hamCenterInNet.x - haMainRoadCenterPt.x;
	int nDy = hamCenterInNet.y - haMainRoadCenterPt.y;
	for (int i=0; i<nNumMainRoadPt; i++)
	{
		vecNewMainRoad[i].x = vecMainRoad[i].x+nDx;
		vecNewMainRoad[i].y = vecMainRoad[i].y+nDy;
	}
	drawImage(matNavi, haMainRoadCenterPt, vecNewMainRoad,Scalar(0,255,0));		// 新主路
#else
	int nSubMainRoadNum = vecSubMainRoad.size();
	vector<HAMapPoint> vecNewMainRoad(nSubMainRoadNum);
	int nDx = hamCenterInNet.x - haMainRoadCenterPt.x;
	int nDy = hamCenterInNet.y - haMainRoadCenterPt.y;
	for (int i=0; i<nSubMainRoadNum; i++)
	{
		vecNewMainRoad[i].x = vecSubMainRoad[i].x+nDx;
		vecNewMainRoad[i].y = vecSubMainRoad[i].y+nDy;
	}
	drawImage(matNavi, haMainRoadCenterPt, vecNewMainRoad,Scalar(0,255,0));		// 新主路
#endif


	// 两个中心点
	int offset_x =0,offset_y = 0;
	offset_x = haMainRoadCenterPt.x - matNavi.cols/2;
	offset_y = haMainRoadCenterPt.y - matNavi.rows/2;
	cv::Point ptOld = cv::Point(haMainRoadCenterPt.x, haMainRoadCenterPt.y) - cv::Point(offset_x,offset_y);
	cv::Point ptNew = cv::Point(hamCenterInNet.x, hamCenterInNet.y) - cv::Point(offset_x,offset_y);
	cv::circle( matNavi,ptOld,2,cv::Scalar( 0, 0, 255 ),2,8);
	cv::circle( matNavi,ptNew,2,cv::Scalar( 0, 0, 255 ),2,8);

#if IS_SON_DRAW
	imshow("matNavi",matNavi);
	waitKey(0);
#endif
#endif

#if IS_DRAW2	// 绘图
	node = vecLinkEndPtnode[nMatchSi];
	cv::Point ptV = Point(node.hamEndPoint.x,node.hamEndPoint.y) - ptOffset;
	//cv::Point ptTemp0 = Point(node.vecNeighborPoint[nPreSonSi].x,node.vecNeighborPoint[nPreSonSi].y) - ptOffset;		
	//cv::Point ptTemp1 = Point(node.vecNeighborPoint[nNextSonSi].x,node.vecNeighborPoint[nNextSonSi].y) - ptOffset;
	cv::Point ptTemp0 = Point(hamMatchPrePt.x,hamMatchPrePt.y) - ptOffset;		
	cv::Point ptTemp1 = Point(hamMatchNextPt.x,hamMatchNextPt.y) - ptOffset;
	cv::circle( matTemp,ptV,1,cv::Scalar( 255, 0, 255),4,8);
	cv::circle( matTemp,ptTemp0,1,cv::Scalar( 0, 255, 255),3,8);
	cv::circle( matTemp,ptTemp1,1,cv::Scalar( 0, 255, 255),3,8);
	Scalar color(100,100,0);
	cv::line(matTemp,ptTemp0,ptV,color,1);	
	cv::line(matTemp,ptV,ptTemp1,color,1);

	// 绘制匹配路径
	for (int i=0; i<vecMatchPath.size(); i++)
	{
		int nLinkId = vecMatchPath[i];
		if (nLinkId>=0)
		{
			drawImage(matTemp, haMainRoadCenterPt, vecRoadNetLinks[nLinkId],cv::Scalar(255,255,0),2);
		}		
	}

	/*for (int i=0; i<vecMatchPath.size(); i++)
	{
	vector<int> vecSinglePath = vecMatchPath[i];
	for (int j=0; j<vecSinglePath.size(); j++)
	{
	int nLinkId = vecSinglePath[j];
	if (nLinkId>=0)
	{
	drawImage(matTemp, haMainRoadCenterPt, vecRoadNetLinks[nLinkId],cv::Scalar(255,255,0),2);
	}			
	}		
	}*/
	
	// 基于主路绘制路网
	for (int i=0; i<nNumLink; i++)
	{
		if (vecRoadNetDirection2MainRoad[i]!=OPPOSITE_DIRECTION)
		{
			drawImage(matTemp, haMainRoadCenterPt, vecRoadNetLinks[i],cv::Scalar(0,255,0),1);
		}
	}

	// 绘制主路与岔路的交点
	for (int i=0; i<vecCrossPointIndex.size(); i++)
	{
		cv::Point ptTemp = Point(vecMatchPathPt[vecCrossPointIndex[i]].x,vecMatchPathPt[vecCrossPointIndex[i]].y) - ptOffset;		
		cv::circle( matTemp,ptTemp,1,cv::Scalar( 0, 0, 255),5,8);
	}

	m_matImage = matTemp.clone();	

	#if IS_SON_DRAW
		cv::imshow("m_matImage",m_matImage);
		cv::waitKey(0);
	#endif

	#if 0
		// 保存
		string strTemp = "D:\\Halo\\ArWay\\output\\direction\\";
		strTemp += m_strImageName.substr(m_strImageName.find_last_of("\\")+1);
		cv::imwrite(strTemp, matTemp);
	#endif
#endif
#endif

	return 0;
}


// 利用整个屏幕内的主路起点、中心点、终点进行匹配
int MergeMapData::matchMainRoadCenterInNet4(const vector<HAMapPoint>& vecMainRoad,
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
											std::vector<std::vector<HAMapPoint> >& vecCrossGpsLinks)
{
	// 参数自检
	int nNumMainRoadPt = vecMainRoad.size();
	int nNumLink = vecRoadNetLinks.size();
	if (nNumMainRoadPt<=0 || nNumLink<=0 ||
		nNumLink!=vecRoadNetLinkInfos.size())
	{
		// 打印log		
		#ifdef _WINDOWS_VER_
			printf("==============matchMainRoadCenterInNet3 - parameter Error!!==============\n");
			m_strErrorLog = "matchMainRoadCenterInNet3 - parameter Error!!";
		#else
			LOGD("==============matchMainRoadCenterInNet3 - parameter Error!!==============\n");
		#endif
		return -1;
	}

	int nRet = 0;

	// 求中心点位置
	int nCenterSi = -1;
	nRet = getPointSite(vecMainRoad, haMainRoadCenterPt, nCenterSi);
	if (nRet<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet3 - getPointSite Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet3 - getPointSite Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet3 - getPointSite Error!!==============\n");
	#endif
		return -1;
	}
		
	// 获取屏幕边界上的主路起点、终点
	HAMapPoint hamPrePt, hamNextPt;
#ifdef _WINDOWS_VER_
	nRet = getStartEndPoint(vecMainRoad, nCenterSi, rtScreen, hamPrePt, hamNextPt);
	if (nRet<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet3 - getStartEndPoint Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet3 - getStartEndPoint Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet3 - getStartEndPoint Error!!==============\n");
	#endif
		return -1;
	}
#else
	hamPrePt = vecMainRoad[0];
	hamNextPt = vecMainRoad[nNumMainRoadPt-1];
#endif


#if 0
	// 求中心点前后各一个节点（与中心点的距离有要求，如大于10个像素），用于计算夹角
	HAMapPoint hamPrePt, hamNextPt;
	nRet = getNearestPoint(vecMainRoad,	nCenterSi, true, hamPrePt);
	if (nRet<0)
	{
		return -1;
	}
	nRet = getNearestPoint(vecMainRoad,	nCenterSi, false, hamNextPt);
	if (nRet<0)
	{
		return -1;
	}

	// 取中心点与前、后临近点连线上的所有点
	vector<HAMapPoint> vecSubMainRoad;
	vecSubMainRoad.push_back(hamPrePt);
	nRet = getPointsBetweenTwoPoints(hamPrePt,haMainRoadCenterPt,vecSubMainRoad);
	if (nRet<0)
	{
		return -1;
	}
	vecSubMainRoad.push_back(haMainRoadCenterPt);
	int nCenterSiInSubMainRoad = vecSubMainRoad.size() - 1;		// 中心点在子路中的位置
	nRet = getPointsBetweenTwoPoints(haMainRoadCenterPt,hamNextPt,vecSubMainRoad);
	if (nRet<0)
	{
		return -1;
	}
	vecSubMainRoad.push_back(hamNextPt);

	// 缩短子路，使得中心点两侧点数相等，保证两边比重一致
	int nDel = vecSubMainRoad.size() - nCenterSiInSubMainRoad - 1;
	vector<HAMapPoint> vecTemp;
	if (nDel>nCenterSiInSubMainRoad)	// 中心点之后的点数多
	{
		vecTemp.insert(vecTemp.begin(),vecSubMainRoad.begin(),vecSubMainRoad.begin()+2*nCenterSiInSubMainRoad+1);
	} 
	else	// 中心点之前的点数多
	{
		vecTemp.insert(vecTemp.begin(),vecSubMainRoad.begin()+nCenterSiInSubMainRoad-nDel,vecSubMainRoad.end());
	}
	vecSubMainRoad.clear();
	vecSubMainRoad = vecTemp;
	vecTemp.clear();
	nCenterSiInSubMainRoad = vecSubMainRoad.size()/2;
#endif

	// 求中心夹角
	Vec2f vMainRoad1(hamPrePt.x-haMainRoadCenterPt.x, hamPrePt.y-haMainRoadCenterPt.y);		// 前一个点与中心点构成的向量
	Vec2f vMainRoad2(hamNextPt.x-haMainRoadCenterPt.x, hamNextPt.y-haMainRoadCenterPt.y);	// 后一个点与中心点构成的向量	
	float fMainAngle = getAngle(vMainRoad1, vMainRoad2);

	// 构造link端点节点
	vector<LinkEndPointNode> vecLinkEndPtnode;
	nRet = formLinkEndPointNode(vecRoadNetLinks, vecRoadNetLinkInfos, vecLinkEndPtnode);
	if (nRet<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet3 - formLinkEndPointNode Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet3 - formLinkEndPointNode Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet3 - formLinkEndPointNode Error!!==============\n");
	#endif
		return -1;
	}


	// =================绘图==================
#ifdef _WINDOWS_VER_
#if IS_DRAW1	// 绘图
	Mat matTemp1(m_matImage.rows,m_matImage.cols,CV_8UC3);
	matTemp1.setTo(0);
	int offset_x1 =0,offset_y1 = 0;
	offset_x1 = haMainRoadCenterPt.x - matTemp1.cols/2;
	offset_y1 = haMainRoadCenterPt.y - matTemp1.rows/2;
	cv::Point ptTemp = cv::Point(haMainRoadCenterPt.x, haMainRoadCenterPt.y) - cv::Point(offset_x1,offset_y1);
	cv::circle( matTemp1,ptTemp,2,cv::Scalar( 0, 255, 0),2,8);	
	for (int i=0; i<vecLinkEndPtnode.size(); i++)
	{
		LinkEndPointNode node = vecLinkEndPtnode[i];
		HAMapPoint hamEndPt = node.hamEndPoint;

		// 区域限制
		if ((abs(hamEndPt.x-haMainRoadCenterPt.x)>CENTER_COVER || 
			abs(hamEndPt.y-haMainRoadCenterPt.y)>CENTER_COVER))
		{
			continue;
		}

		cv::Point ptTemp = cv::Point(hamEndPt.x, hamEndPt.y) - cv::Point(offset_x1,offset_y1);
		cv::circle( matTemp1,ptTemp,2,cv::Scalar( 0, 0, 255 ),2,8);
		for (int j=0; j<node.vecNeighborPoint.size(); j++)
		{
			vector<HAMapPoint> vecTemp;
			vecTemp.push_back(hamEndPt);
			vecTemp.push_back(node.vecNeighborPoint[j]);
			drawImage(matTemp1,haMainRoadCenterPt,vecTemp,cv::Scalar(0,0,0));
			cv::circle( matTemp1,Point2i(node.vecNeighborPoint[j].x,node.vecNeighborPoint[j].y),
				1,cv::Scalar( 0, 255, 0),1,8);
		}
		imshow("matTemp1",matTemp1);
		waitKey(0);		
	}
#endif
#endif
	// =====================================

#ifdef _WINDOWS_VER_
#if IS_DRAW || IS_DRAW2	// 绘图
	Mat matTemp(m_matImage.rows,m_matImage.cols,CV_8UC3);
	matTemp.setTo(0);
	int offsetx =0,offsety = 0;
	offsetx = haMainRoadCenterPt.x - matTemp.cols/2;
	offsety = haMainRoadCenterPt.y - matTemp.rows/2;
	cv::Point ptOffset(offsetx,offsety);
	//m_ptOffset = ptOffset;		// 成员赋值
	cv::Point ptTemp = cv::Point(haMainRoadCenterPt.x, haMainRoadCenterPt.y) - ptOffset;
	cv::circle( matTemp,ptTemp,2,cv::Scalar( 0, 255, 0),2,8);	
	drawImage(matTemp,haMainRoadCenterPt,/*vecSubMainRoad*/vecMainRoad,cv::Scalar(255,0,0));
	vector<HAMapPoint> vecFirstHalfMainRoad, vecSecondHalfMainRoad;		// 记录前后半段主路
	vecFirstHalfMainRoad.insert(vecFirstHalfMainRoad.begin(),vecMainRoad.begin(),vecMainRoad.begin()+nCenterSi+1);
	vecSecondHalfMainRoad.insert(vecSecondHalfMainRoad.begin(),vecMainRoad.begin()+nCenterSi+1,vecMainRoad.end());
	drawImage(matTemp,haMainRoadCenterPt,vecFirstHalfMainRoad,cv::Scalar(255,0,255),1);		// 前半段，紫色
	drawImage(matTemp,haMainRoadCenterPt,vecSecondHalfMainRoad,cv::Scalar(0,255,255),1);	// 后半段，黄色
#endif
#if IS_DRAW2	// 绘图
	for (int i=0; i<nNumLink; i++)
	{
		drawImage(matTemp,haMainRoadCenterPt,vecRoadNetLinks[i],cv::Scalar(0,0,255),1);
	}
	cv::Point ptPre = cv::Point(hamPrePt.x, hamPrePt.y) - ptOffset;
	cv::Point ptNext = cv::Point(hamNextPt.x, hamNextPt.y) - ptOffset;
	Scalar colorMainRoad(255,255,0);
	//line(matTemp,ptTemp,ptPre,colorMainRoad,1);	
	//line(matTemp,ptTemp,ptNext,colorMainRoad,1);	

	// 绘制所有端点
	for (int i=0; i<vecLinkEndPtnode.size(); i++)
	{
		HAMapPoint hamPtTemp = vecLinkEndPtnode[i].hamEndPoint;
		cv::Point ptTemp = cv::Point(hamPtTemp.x,hamPtTemp.y) - ptOffset;
		cv::circle( matTemp,ptTemp,2,cv::Scalar( 255, 255, 255),2,8);
	}

	#if IS_SON_DRAW
		imshow("matTemp",matTemp);
		cv::waitKey(0);
	#endif
#endif
#endif


	// 角度匹配
	int nMatchSi = -1, nPreSonSi = -1, nNextSonSi = -1;		// 记录匹配位置
	float fMinDelAngle = 360.f;		// 记录夹角之差的最小绝对值
	int nEndPtNum = vecLinkEndPtnode.size();
	LinkEndPointNode node;
	double uMinError= -1.f;
	HAMapPoint hamMatchPrePt, hamMatchNextPt;	// 匹配点
	//vector<vector<int> > vecMatchPath;		// 记录匹配的路径
	//vector<vector<int> > vecMatchPathNodeId;		// 记录匹配路径上的Node Id
	vector<int> vecMatchPath;		// 记录匹配的路径
	vector<int> vecMatchPathNodeId;		// 记录匹配路径上的Node Id
	vector<int> vecOptionNodeId;		// 记录备选点集合
	double uMinBorderDis = -1;	// 记录对应边界点间最小距离
	double uMinCenterDis = -1;	// 记录匹配点到中心点的最小距离
	double uMinPathDis = -1;
	
	for (int i=0; i<nEndPtNum; i++)
	{
		node = vecLinkEndPtnode[i];
		int nNeighborNodeNum = node.vecNeighborNodeId.size();
		if (nNeighborNodeNum<=2)		// 邻居点限制
		{
			continue;
		}

		HAMapPoint hamEndPt = node.hamEndPoint;	

		// 区域限制
		if ((!isRectInside(hamEndPt,rtScreen))&&((abs(hamEndPt.x-haMainRoadCenterPt.x)>CENTER_COVER || 
			abs(hamEndPt.y-haMainRoadCenterPt.y)>CENTER_COVER)))
		{
			continue;
		}

#ifdef _WINDOWS_VER_
#if IS_DRAW1	// 绘图
		cv::Point ptTemp = cv::Point(hamEndPt.x, hamEndPt.y) - cv::Point(offset_x1,offset_y1);
		cv::circle( matTemp1,ptTemp,2,cv::Scalar( 0, 255, 255 ),2,8);
		for (int j=0; j<node.vecNeighborPoint.size(); j++)
		{
			vector<HAMapPoint> vecTemp;
			vecTemp.push_back(hamEndPt);
			vecTemp.push_back(node.vecNeighborPoint[j]);
			drawImage(matTemp1,haMainRoadCenterPt,vecTemp,cv::Scalar(0,255,255));
			cv::circle( matTemp1,Point2i(node.vecNeighborPoint[j].x,node.vecNeighborPoint[j].y),
				1,cv::Scalar( 0, 255, 255),1,8);
		}
		imshow("matTemp1",matTemp1);
		waitKey(0);
#endif

//#if IS_DRAW2	// 绘图
//		cv::Point ptTemp = cv::Point(hamEndPt.x, hamEndPt.y) - ptOffset;
//		cv::circle( matTemp,ptTemp,2,cv::Scalar( 0, 255, 255),2,8);
//// 		imshow("matTemp",matTemp);
//// 		waitKey(0);
//#endif
#endif
		

		// 延伸link直到超出屏幕边界
		vector<HAMapPoint> vecBorderPt;
		vector<int> vecBorderPtDirection;
		vector<vector<int> > vecPathLinkId;
		vector<vector<int> > vecPathNodeId;

		/*nRet = extendLink(vecRoadNetLinks,vecLinkEndPtnode,	i, rtScreen, vecBorderPt, 
							vecBorderPtDirection, vecPathLinkId, vecPathNodeId);*/
		nRet = extendLink1(vecRoadNetLinks,vecLinkEndPtnode,	i, rtScreen, vecBorderPt, 
 			vecBorderPtDirection, vecPathLinkId, vecPathNodeId);
		if (nRet<0 || vecBorderPt.size()<=0)
		{
			continue;
		}
		
	#ifdef _WINDOWS_VER_
		#if IS_DRAW	// 绘图
			cv::Point ptTemp = cv::Point(hamEndPt.x, hamEndPt.y) - ptOffset;
			cv::circle( matTemp,ptTemp,2,cv::Scalar( 0, 255, 255),2,8);

			for (int j=0; j<vecBorderPt.size(); j++)
			{
				cv::Point ptBorder = cv::Point(vecBorderPt[j].x, vecBorderPt[j].y) - ptOffset;
				Scalar color(0,255,0);
				line(matTemp,ptTemp,ptBorder,color,1);	
			}

			imshow("matTemp",matTemp);
			cv::waitKey(0);
		#endif
	#endif

		// 求与主路中心点前后两向量最接近的两向量
		int nBorderPtNum = vecBorderPt.size();
		int nSi0=-1, nSi1=-1;
		double uDis0 = -1, uDis1 = -1;
		Vec2f v0, v1;
		vector<int> vecStartOptionPtId, vecEndOptionPtId;		// 记录边界起点、终点的候选集
		for (int j=0; j<2; j++)
		{
			Vec2f vMainRoad = vMainRoad1;	// 中心点之前的向量
			if (j>0)
			{				
				vMainRoad = vMainRoad2;	// 中心点之后的向量
			}

			float fMinAngle = 360.f;
			int nTempSi=0;
			Vec2f vTemp;

			for (int k=0; k<nBorderPtNum; k++)
			{
				if (j==0)
				{
					if (vecBorderPtDirection[k]==2)
					{
						continue;
					}
				}
				else
				{
					if (vecBorderPtDirection[k]==3)
					{
						continue;
					}
				}
				
				//LinkEndPointNode nextNode = vecLinkEndPtnode[node.vecNeighborNodeId[k]];
				//HAMapPoint hamPt = nextNode.hamEndPoint;
				HAMapPoint hamPt = vecBorderPt[k];
				Vec2f v(hamPt.x-hamEndPt.x,hamPt.y-hamEndPt.y);

#if 0
				// ===================方向限制==============
				// 方向判断 int direction;//道路方向:0未调查,默认双向,1双向,2正方向(link的起点到终点),3反方向
				int nLinkId = node.vecLinkId[k];
				int nDirection = vecRoadNetLinkInfos[nLinkId].direction;
				bool bIsEnd = (hamEndPt==(HAMapPoint)vecRoadNetLinks[nLinkId][0])?false:true;		// 标识端点是否属于link的终点
				if (j==0)	// 标识端点应为link终点
				{
					if ((bIsEnd==true&&nDirection==3) || (bIsEnd==false&&nDirection==2))
					{
						continue;
					}
				} 
				else		// 标识端点应为link起点
				{
					if ((bIsEnd==true&&nDirection==2) || (bIsEnd==false&&nDirection==3))
					{
						continue;
					}
				}
				// =====================================
#endif

				float fAngle = getAngle(vMainRoad, v);				
				if (fAngle<VECTOR_NEAR_ANGLE/*fMinAngle*/)
				{
					fMinAngle = fAngle;
					nTempSi = k;
					vTemp = v;

					if (j==0)
					{
						vecStartOptionPtId.push_back(nTempSi);
					} 
					else
					{
						vecEndOptionPtId.push_back(nTempSi);
					}
				}				
			}	// End k

		}	// End j
		/*if (nSi0<0 || nSi1<0 || nSi0==nSi1)
		{
			continue;
		}*/
		if (vecStartOptionPtId.size()<=0 || vecEndOptionPtId.size()<=0)
		{
			continue;
		}

		// 关于候选路径，求路径起点、终点离主路起点、终点距离最近的路径
		double uMinOptionDis = 2*(rtScreen.width+rtScreen.height);				// 设定一个达不到的初值，便于循环比较
		for (int j=0; j<vecStartOptionPtId.size(); j++)
		{
			HAMapPoint hamStartOptionPt = vecBorderPt[vecStartOptionPtId[j]];
			Vec2f vStart(hamStartOptionPt.x-hamEndPt.x,hamStartOptionPt.y-hamEndPt.y);
			for (int k=0; k<vecEndOptionPtId.size(); k++)
			{
				HAMapPoint hamEndOptionPt = vecBorderPt[vecEndOptionPtId[k]];
				Vec2f vEnd(hamEndOptionPt.x-hamEndPt.x,hamEndOptionPt.y-hamEndPt.y);
				float fAngle = getAngle(vStart,vEnd);
				float fDelAngle = abs(fMainAngle-fAngle);
				if (fDelAngle<ANGLE_ALLOWANCE)
				{
					// 求距离
					float fStartDis = getDistancePoint2Point(hamStartOptionPt.x,hamStartOptionPt.y,hamPrePt.x,hamPrePt.y);
					float fEndDis = getDistancePoint2Point(hamEndOptionPt.x,hamEndOptionPt.y,hamNextPt.x,hamNextPt.y);
					float fDis = fStartDis + fEndDis;
					
					if (fDis<uMinOptionDis)
					{
						uMinOptionDis = fDis;
						nSi0 = vecStartOptionPtId[j];
						nSi1 = vecEndOptionPtId[k];
						v0 = vStart;
						v1 = vEnd;
					}
				}

			}
		}

		// 保存每个节点的方向数据，绿-与主路同向，红-与主路反向
#ifdef _WINDOWS_VER_
#if 0//IS_DRAW2	// 绘图								
		cv::Point ptV = Point(hamEndPt.x,hamEndPt.y) - ptOffset;
		cv::Point ptTemp0 = Point(vecBorderPt[nSi0].x,vecBorderPt[nSi0].y) - ptOffset;		
		cv::Point ptTemp1 = Point(vecBorderPt[nSi1].x,vecBorderPt[nSi1].y) - ptOffset;						
		cv::circle( matTemp,ptV,1,cv::Scalar( 0, 255, 255),2,8);
		cv::circle( matTemp,ptTemp0,1,cv::Scalar( 0, 255, 255),2,8);
		cv::circle( matTemp,ptTemp1,1,cv::Scalar( 0, 255, 255),2,8);
		Scalar color(255,255,255);
		cv::line(matTemp,ptTemp0,ptV,color,1);	
		cv::line(matTemp,ptV,ptTemp1,color,1);	
#endif
#endif

#if 0	// 利用最佳匹配角
		// 求夹角		
		float fAngle = getAngle(v0,v1);
		float fDelAngle = abs(fMainAngle-fAngle);
		if (fDelAngle<fMinDelAngle)
		{
			fMinDelAngle = fDelAngle;
			nMatchSi = i;
			hamMatchPrePt = vecBorderPt[nSi0];
			hamMatchNextPt = vecBorderPt[nSi1];

			vecMatchPath.clear();
			vecMatchPath.push_back(vecPathLinkId[nSi0]);
			vecMatchPath.push_back(vecPathLinkId[nSi1]);

			vecMatchPathNodeId.clear();
			vecMatchPathNodeId.push_back(vecPathNodeId[nSi0]);
			vecMatchPathNodeId.push_back(vecPathNodeId[nSi1]);
		}
#else	// 记录候选点，利用边界点间距离最短做判据
		float fAngle = getAngle(v0,v1);
		float fDelAngle = abs(fMainAngle-fAngle);
		if (fDelAngle<ANGLE_ALLOWANCE)
		{
			vecOptionNodeId.push_back(i);			

			// 匹配点与中心点距离
			double uCenterDis = sqrt((haMainRoadCenterPt.x-hamEndPt.x)*(haMainRoadCenterPt.x-hamEndPt.x)+
				(haMainRoadCenterPt.y-hamEndPt.y)*(haMainRoadCenterPt.y-hamEndPt.y));

			double uPathDis0 = -1, uPathDis1 = -1, uPathDis = -1;
			nRet = getDisBorder2MatchPt(vecRoadNetLinks, vecPathLinkId[nSi0], rtScreen,
										vecBorderPt[nSi0], hamEndPt, uPathDis0);
			if (nRet<0)
			{
				continue;
			}
			nRet = getDisBorder2MatchPt(vecRoadNetLinks, vecPathLinkId[nSi1], rtScreen,
										vecBorderPt[nSi0], hamEndPt, uPathDis1);
			if (nRet<0)
			{
				continue;
			}
			uPathDis = uPathDis0 + uPathDis1;


			// 边界点间距离
			double uDis = uDis0 + uDis1;
			if (uMinBorderDis<0)
			{
				uMinBorderDis = uMinOptionDis/*uDis*/;
				nMatchSi = i;

				hamMatchPrePt = vecBorderPt[nSi0];
				hamMatchNextPt = vecBorderPt[nSi1];
								
				vecMatchPath.clear();
				vecMatchPath = vecPathLinkId[nSi0];
				reverseOrder(vecMatchPath);		// 倒序重排，与主路方向一致				
				vecMatchPath.insert(vecMatchPath.end(),vecPathLinkId[nSi1].begin(),vecPathLinkId[nSi1].end());

				vecMatchPathNodeId.clear();
				vecMatchPathNodeId = vecPathNodeId[nSi0];
				reverseOrder(vecMatchPathNodeId);		// 倒序重排，与主路方向一致
				vecMatchPathNodeId.erase(vecMatchPathNodeId.end()-1);
				vecMatchPathNodeId.insert(vecMatchPathNodeId.end(),vecPathNodeId[nSi1].begin(),vecPathNodeId[nSi1].end());				

				uMinCenterDis = uCenterDis;
				uMinPathDis = uPathDis;
			} 
			else
			{
				if (/*uDis*/uMinOptionDis<uMinBorderDis)
				{
					uMinBorderDis = uMinOptionDis/*uDis*/;
					nMatchSi = i;

					hamMatchPrePt = vecBorderPt[nSi0];
					hamMatchNextPt = vecBorderPt[nSi1];

				
					vecMatchPath.clear();
					vecMatchPath = vecPathLinkId[nSi0];
					reverseOrder(vecMatchPath);		// 倒序重排，与主路方向一致				
					vecMatchPath.insert(vecMatchPath.end(),vecPathLinkId[nSi1].begin(),vecPathLinkId[nSi1].end());

					vecMatchPathNodeId.clear();
					vecMatchPathNodeId = vecPathNodeId[nSi0];
					reverseOrder(vecMatchPathNodeId);		// 倒序重排，与主路方向一致
					vecMatchPathNodeId.erase(vecMatchPathNodeId.end()-1);
					vecMatchPathNodeId.insert(vecMatchPathNodeId.end(),vecPathNodeId[nSi1].begin(),vecPathNodeId[nSi1].end());	

					uMinCenterDis = uCenterDis;
					uMinPathDis = uPathDis;
				}
				else if (abs(/*uDis*/uMinOptionDis-uMinBorderDis)<1e-10)		// 相等时，基于路径长度
				{
					if (uPathDis<uMinPathDis)
					{
						uMinBorderDis = uMinOptionDis/*uDis*/;
						nMatchSi = i;

						hamMatchPrePt = vecBorderPt[nSi0];
						hamMatchNextPt = vecBorderPt[nSi1];
						

						vecMatchPath.clear();
						vecMatchPath = vecPathLinkId[nSi0];
						reverseOrder(vecMatchPath);		// 倒序重排，与主路方向一致				
						vecMatchPath.insert(vecMatchPath.end(),vecPathLinkId[nSi1].begin(),vecPathLinkId[nSi1].end());

						vecMatchPathNodeId.clear();
						vecMatchPathNodeId = vecPathNodeId[nSi0];
						reverseOrder(vecMatchPathNodeId);		// 倒序重排，与主路方向一致
						vecMatchPathNodeId.erase(vecMatchPathNodeId.end()-1);
						vecMatchPathNodeId.insert(vecMatchPathNodeId.end(),vecPathNodeId[nSi1].begin(),vecPathNodeId[nSi1].end());	

						uMinCenterDis = uCenterDis;
						uMinPathDis = uPathDis;
					}
					else if (abs(uPathDis-uMinPathDis)<1e-10)		// 相等时，基于路径长度
					{
						if (uCenterDis<uMinCenterDis)
						{
							uMinBorderDis = uMinOptionDis/*uDis*/;
							nMatchSi = i;

							hamMatchPrePt = vecBorderPt[nSi0];
							hamMatchNextPt = vecBorderPt[nSi1];


							vecMatchPath.clear();
							vecMatchPath = vecPathLinkId[nSi0];
							reverseOrder(vecMatchPath);		// 倒序重排，与主路方向一致				
							vecMatchPath.insert(vecMatchPath.end(),vecPathLinkId[nSi1].begin(),vecPathLinkId[nSi1].end());

							vecMatchPathNodeId.clear();
							vecMatchPathNodeId = vecPathNodeId[nSi0];
							reverseOrder(vecMatchPathNodeId);		// 倒序重排，与主路方向一致
							vecMatchPathNodeId.erase(vecMatchPathNodeId.end()-1);
							vecMatchPathNodeId.insert(vecMatchPathNodeId.end(),vecPathNodeId[nSi1].begin(),vecPathNodeId[nSi1].end());	

							uMinCenterDis = uCenterDis;
							uMinPathDis = uPathDis;
						}
					}
				}
			}			
		}

#endif


		// =================绘图==================
#ifdef _WINDOWS_VER_
#if IS_DRAW	// 绘图
		//Mat matTemp(m_matImage.rows,m_matImage.cols,CV_8UC3);
		//matTemp.setTo(0);
		//int offset_x =0,offset_y = 0;
		offsetx = haMainRoadCenterPt.x - matTemp.cols/2;
		offsety = haMainRoadCenterPt.y - matTemp.rows/2;
		cv::Point ptTemp = cv::Point(hamEndPt.x, hamEndPt.y) - cv::Point(offsetx,offsety);
		cv::circle( matTemp,ptTemp,2,cv::Scalar( 0, 0, 255 ),2,8);
		vector<HAMapPoint> vecTemp;
		vecTemp.push_back(node.vecNeighborPoint[nSi0]);
		vecTemp.push_back(hamEndPt);
		vecTemp.push_back(node.vecNeighborPoint[nSi1]);
		drawImage(matTemp,haMainRoadCenterPt,vecTemp,cv::Scalar(0,0,0));
		cv::circle( matTemp,Point2i(node.vecNeighborPoint[nSi0].x,node.vecNeighborPoint[nSi0].y),
			1,cv::Scalar( 0, 255, 0),1,8);
		cv::circle( matTemp,Point2i(node.vecNeighborPoint[nSi1].x,node.vecNeighborPoint[nSi1].y),
			1,cv::Scalar( 0, 255, 0),1,8);
		imshow("matTemp",matTemp);
		waitKey(0);
#endif
#endif
		// =====================================
#if 0
		// 求距离
		double uError = 0.f;
		nRet = getMainSubLine2SubNetDis(vecSubMainRoad, 
			nCenterSiInSubMainRoad,
			node.vecNeighborPoint[nSi0],
			hamEndPt,
			node.vecNeighborPoint[nSi1],										
			uError);
		if (nRet<0)
		{
			continue;
		}
		if (uMinError<0)
		{
			uMinError = uError;
			nMatchSi = i;
		}
		else
		{
			nMatchSi = (uError<uMinError)?i:nMatchSi;
			uMinError = (uError<uMinError)?uError:uMinError;
		}
#endif
	}		// End i

	if (nMatchSi<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet3 - no Match Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet3 - no Match Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet3 - no Match Error!!==============\n");
	#endif
		return -1;
	}


	// 输出
	//nMatchSi = 29;
	hamCenterInNet = vecLinkEndPtnode[nMatchSi].hamEndPoint;
	

	// 由一个点构造路网	
	vector<int> vecRoadNetDirection2MainRoad;
 	//nRet = formRoadNet(vecRoadNetLinks,
 	//	vecLinkEndPtnode,
 	//	/*vecMainRoadNodeId*/vecMatchPathNodeId,
 	//	rtScreen,
 	//	vecRoadNetDirection2MainRoad);

	
	nRet = formRoadNet1(vecRoadNetLinks,vecRoadNetLinkInfos,
						vecLinkEndPtnode,
						vecMatchPathNodeId,
						rtScreen,
						vecRoadNetDirection2MainRoad);
	if (nRet < 0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet3 - formRoadNet Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet3 - formRoadNet Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet3 - formRoadNet Error!!==============\n");
	#endif
		return -1;
	}

	// 过滤路网
	
	nRet = filterRoadNet(vecRoadNetLinks,
						vecRoadNetDirection2MainRoad,
						/*vecMainRoadLinkId*/vecMatchPath,
						vecCrossGpsLinks);
	if (nRet<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet3 - filterRoadNet Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet3 - filterRoadNet Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet3 - filterRoadNet Error!!==============\n");
	#endif
		return -1;
	}

	// 添加匹配的主路点，及与岔路交点在主路中的下标
	vector<HAMapPoint> vecMatchPathPt;

	vecMatchPathPt.push_back(hamMatchPrePt);		// 第一个点
	for (int i=0; i<vecMatchPath.size(); i++)
	{
		int nLinkId = vecMatchPath[i];
		if (nLinkId<0)
		{
			continue;
		}

		// link两个端点对应的node Id
		int nPathStartNodeId = vecMatchPathNodeId[i];
		int nPathEndNodeId = vecMatchPathNodeId[i+1];

		vector<HAMapPoint> vecTemp = vecRoadNetLinks[nLinkId];
		// 判断方向
		if (vecTemp[0]==vecLinkEndPtnode[nPathEndNodeId].hamEndPoint)
		{
			reverseOrder(vecTemp);
		}

		int nTempNum = vecTemp.size();
		for (int j=0; j<nTempNum-1; j++)
		{
			HAMapPoint hamPt = vecTemp[j];			
			if (isRectInside(hamPt, rtScreen) && 
				hamPt!=vecMatchPathPt[vecMatchPathPt.size()-1])
			{
				// 求岔路与主路交点在主路中的位置
				if (j==0 && vecLinkEndPtnode[nPathStartNodeId].vecNeighborNodeId.size()>2)
				{
					vecCrossPointIndex.push_back(vecMatchPathPt.size());
				}

				vecMatchPathPt.push_back(hamPt);					
			}
		} // end j
	}	// end i

	vecMatchPathPt.push_back(hamMatchNextPt);	// 最后一个点


	// 求匹配点在集合中的位置
	nMatchCenterIndex = -1;
	for (int i=0; i<vecMatchPathPt.size(); i++)
	{
		if (hamCenterInNet==vecMatchPathPt[i])
		{
			nMatchCenterIndex = i;
			break;
		}
	}
	if (nMatchCenterIndex<0)
	{
		return -1;
	}

	//vecCrossGpsLinks.insert(vecCrossGpsLinks.begin(),vecMatchPathPt);		// 添加，主路作为第0个
	vecCrossGpsLinks.push_back(vecMatchPathPt);		// 添加，主路作为最后一个，方便平移

	// 计算从匹配路径移至主路的最佳平移距离
	vector<HAMapPoint> vecMainRoadVertex, vecMatchRoadVertex;
	vecMainRoadVertex.push_back(hamPrePt);
	vecMainRoadVertex.push_back(hamNextPt);
	vecMatchRoadVertex.push_back(hamMatchPrePt);
	vecMatchRoadVertex.push_back(hamMatchNextPt);
	HAMapPoint hamOffset;
	nRet = getOffset2MainRoad(vecMainRoadVertex, vecMatchRoadVertex, hamOffset);
	if (nRet<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet3 - getOffset2MainRoad Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet3 - getOffset2MainRoad Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet3 - getOffset2MainRoad Error!!==============\n");
	#endif
		return -1;
	}

	// 平移
	nRet = translateRoadNet(vecCrossGpsLinks, hamOffset);
	if (nRet<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet3 - translateRoadNet Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet3 - translateRoadNet Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet3 - translateRoadNet Error!!==============\n");
	#endif
		return -1;
	}

	// 获取平移后的主路匹配点
	vecMainRoadPtInNet = vecCrossGpsLinks[vecCrossGpsLinks.size()-1];
	vecCrossGpsLinks.erase(vecCrossGpsLinks.end()-1);		// 去掉最后一个vector元素，除去主路

	hamCenterInNet = vecMainRoadPtInNet[nMatchCenterIndex];

	// 画图
#ifdef _WINDOWS_VER_
#if IS_DRAW
	//Mat matNavi(800,800,CV_8UC3);
	//matNavi.setTo(0);
	Mat matNavi = m_matImage;
	drawImage(matNavi, haMainRoadCenterPt, vecMainRoad,Scalar(255,0,0));		// 原始主路	
	for (int i=0; i<nNumLink; i++)
	{
		drawImage(matNavi,haMainRoadCenterPt, vecRoadNetLinks[i],Scalar(0,0,0));
	}

	// 匹配的主路
#if 0
	vector<HAMapPoint> vecNewMainRoad(nNumMainRoadPt);
	int nDx = hamCenterInNet.x - haMainRoadCenterPt.x;
	int nDy = hamCenterInNet.y - haMainRoadCenterPt.y;
	for (int i=0; i<nNumMainRoadPt; i++)
	{
		vecNewMainRoad[i].x = vecMainRoad[i].x+nDx;
		vecNewMainRoad[i].y = vecMainRoad[i].y+nDy;
	}
	drawImage(matNavi, haMainRoadCenterPt, vecNewMainRoad,Scalar(0,255,0));		// 新主路
#else
	int nSubMainRoadNum = vecSubMainRoad.size();
	vector<HAMapPoint> vecNewMainRoad(nSubMainRoadNum);
	int nDx = hamCenterInNet.x - haMainRoadCenterPt.x;
	int nDy = hamCenterInNet.y - haMainRoadCenterPt.y;
	for (int i=0; i<nSubMainRoadNum; i++)
	{
		vecNewMainRoad[i].x = vecSubMainRoad[i].x+nDx;
		vecNewMainRoad[i].y = vecSubMainRoad[i].y+nDy;
	}
	drawImage(matNavi, haMainRoadCenterPt, vecNewMainRoad,Scalar(0,255,0));		// 新主路
#endif


	// 两个中心点
	int offset_x =0,offset_y = 0;
	offset_x = haMainRoadCenterPt.x - matNavi.cols/2;
	offset_y = haMainRoadCenterPt.y - matNavi.rows/2;
	cv::Point ptOld = cv::Point(haMainRoadCenterPt.x, haMainRoadCenterPt.y) - cv::Point(offset_x,offset_y);
	cv::Point ptNew = cv::Point(hamCenterInNet.x, hamCenterInNet.y) - cv::Point(offset_x,offset_y);
	cv::circle( matNavi,ptOld,2,cv::Scalar( 0, 0, 255 ),2,8);
	cv::circle( matNavi,ptNew,2,cv::Scalar( 0, 0, 255 ),2,8);

#if IS_SON_DRAW
	imshow("matNavi",matNavi);
	waitKey(0);
#endif
#endif

#if IS_DRAW2	// 绘图
	node = vecLinkEndPtnode[nMatchSi];
	cv::Point ptV = Point(node.hamEndPoint.x,node.hamEndPoint.y) - ptOffset;
	//cv::Point ptTemp0 = Point(node.vecNeighborPoint[nPreSonSi].x,node.vecNeighborPoint[nPreSonSi].y) - ptOffset;		
	//cv::Point ptTemp1 = Point(node.vecNeighborPoint[nNextSonSi].x,node.vecNeighborPoint[nNextSonSi].y) - ptOffset;
	cv::Point ptTemp0 = Point(hamMatchPrePt.x,hamMatchPrePt.y) - ptOffset;		
	cv::Point ptTemp1 = Point(hamMatchNextPt.x,hamMatchNextPt.y) - ptOffset;
	cv::circle( matTemp,ptV,1,cv::Scalar( 255, 0, 255),4,8);
	cv::circle( matTemp,ptTemp0,1,cv::Scalar( 0, 255, 255),3,8);
	cv::circle( matTemp,ptTemp1,1,cv::Scalar( 0, 255, 255),3,8);
	Scalar color(100,100,0);
	cv::line(matTemp,ptTemp0,ptV,color,1);	
	cv::line(matTemp,ptV,ptTemp1,color,1);

	// 绘制匹配路径
	for (int i=0; i<vecMatchPath.size(); i++)
	{
		int nLinkId = vecMatchPath[i];
		if (nLinkId>=0)
		{
			drawImage(matTemp, haMainRoadCenterPt, vecRoadNetLinks[nLinkId],cv::Scalar(255,255,0),2);
		}		
	}

	/*for (int i=0; i<vecMatchPath.size(); i++)
	{
	vector<int> vecSinglePath = vecMatchPath[i];
	for (int j=0; j<vecSinglePath.size(); j++)
	{
	int nLinkId = vecSinglePath[j];
	if (nLinkId>=0)
	{
	drawImage(matTemp, haMainRoadCenterPt, vecRoadNetLinks[nLinkId],cv::Scalar(255,255,0),2);
	}			
	}		
	}*/
	
	// 基于主路绘制路网
	for (int i=0; i<nNumLink; i++)
	{
		if (vecRoadNetDirection2MainRoad[i]!=OPPOSITE_DIRECTION)
		{
			drawImage(matTemp, haMainRoadCenterPt, vecRoadNetLinks[i],cv::Scalar(0,255,0),1);
		}
	}

	// 绘制主路与岔路的交点
	for (int i=0; i<vecCrossPointIndex.size(); i++)
	{
		cv::Point ptTemp = Point(vecMatchPathPt[vecCrossPointIndex[i]].x,vecMatchPathPt[vecCrossPointIndex[i]].y) - ptOffset;		
		cv::circle( matTemp,ptTemp,1,cv::Scalar( 0, 0, 255),5,8);
	}

	m_matImage = matTemp.clone();	

	#if IS_SON_DRAW
		cv::imshow("m_matImage",m_matImage);
		cv::waitKey(0);
	#endif

	#if 0
		// 保存
		string strTemp = "D:\\Halo\\ArWay\\output\\direction\\";
		strTemp += m_strImageName.substr(m_strImageName.find_last_of("\\")+1);
		cv::imwrite(strTemp, matTemp);
	#endif
#endif
#endif

	return 0;
}

// 利用整个屏幕内的主路起点、中心点、终点进行匹配
int MergeMapData::matchMainRoadCenterInNet5(const vector<HAMapPoint>& vecMainRoad,
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
											vector<HAMapPoint>& vecHistoryCrossPt)		// 历史岔路起点[in/out]
{
	// 参数自检
	int nNumMainRoadPt = vecMainRoad.size();
	int nNumLink = vecRoadNetLinks.size();
	if (nNumMainRoadPt<=0 || nNumLink<=0 ||
		nNumLink!=vecRoadNetLinkInfos.size())
	{
		// 打印log		
		#ifdef _WINDOWS_VER_
			printf("==============matchMainRoadCenterInNet5 - parameter Error!!==============\n");
			m_strErrorLog = "matchMainRoadCenterInNet5 - parameter Error!!";
		#else
			LOGD("==============matchMainRoadCenterInNet5 - parameter Error!!==============\n");
		#endif
		return -1;
	}

	int nRet = 0;

	// 求中心点位置
	int nCenterSi = -1;
	nRet = getPointSite(vecMainRoad, haMainRoadCenterPt, nCenterSi);
	if (nRet<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet - getPointSite Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet - getPointSite Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet - getPointSite Error!!==============\n");
	#endif
	
		return -1;
	}
	
#if IS_PRINT_LOG
	#ifdef _WINDOWS_VER_
		printf("=========matchMainRoadCenterInNet - nCenterSi = %d=========\n",nCenterSi);		
	#else
		LOGD("=========matchMainRoadCenterInNet - nCenterSi = %d=========\n",nCenterSi);
	#endif
#endif

	// 获取屏幕边界上的主路起点、终点
	HAMapPoint hamPrePt, hamNextPt;
#if 1
	nRet = getStartEndPoint(vecMainRoad, nCenterSi, rtScreen, hamPrePt, hamNextPt);
	if (nRet==-1)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet3 - getStartEndPoint Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet3 - getStartEndPoint Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet3 - getStartEndPoint Error!!==============\n");
	#endif
		return -1;
	}

	if (nRet==-2)
	{
		hamPrePt = vecMainRoad[0];		
	}
	if (nRet==-3)
	{
		hamNextPt = vecMainRoad[nNumMainRoadPt-1];
	}
	if (nRet == -4)
	{
		hamPrePt = vecMainRoad[0];
		hamNextPt = vecMainRoad[nNumMainRoadPt-1];
	}
#else
	#ifdef _WINDOWS_VER_	
		nRet = getStartEndPoint(vecMainRoad, nCenterSi, rtScreen, hamPrePt, hamNextPt);
		if (nRet==-1)
		{
		#ifdef _WINDOWS_VER_
			printf("==============matchMainRoadCenterInNet3 - getStartEndPoint Error!!==============\n");
			m_strErrorLog = "matchMainRoadCenterInNet3 - getStartEndPoint Error!!";
		#else
			LOGD("==============matchMainRoadCenterInNet3 - getStartEndPoint Error!!==============\n");
		#endif
			return -1;
		}

		if (nRet<-1)
		{
			hamPrePt = vecMainRoad[0];
			hamNextPt = vecMainRoad[nNumMainRoadPt-1];
		}
	#else
		hamPrePt = vecMainRoad[0];
		hamNextPt = vecMainRoad[nNumMainRoadPt-1];
	#endif
#endif

#if IS_PRINT_LOG
	#ifdef _WINDOWS_VER_
		printf("	rtScreen: w=%d, h=%d\n",rtScreen.width,rtScreen.height);
		printf("matchMainRoadCenterInNet - vecMainRoad: \n");	
		for (int i=0; i<vecMainRoad.size(); i++)
		{
			printf("%d, %d; ",
				vecMainRoad[i].x, vecMainRoad[i].y);	
		}
		printf("\n	hamPrePt.x=%d, hamPrePt.y=%d, hamNextPt.x=%d,hamNextPt.y=%d\n",
				hamPrePt.x, hamPrePt.y, hamNextPt.x,hamNextPt.y);		
	#else
		LOGD("	rtScreen: w=%d, h=%d\n",rtScreen.width,rtScreen.height);
		LOGD("matchMainRoadCenterInNet - vecMainRoad: \n");	
		for (int i=0; i<vecMainRoad.size(); i++)
		{
			LOGD("%d, %d; ",
				vecMainRoad[i].x, vecMainRoad[i].y);	
		}
		LOGD("\n	hamPrePt.x=%d, hamPrePt.y=%d, hamNextPt.x=%d,hamNextPt.y=%d\n",
				hamPrePt.x, hamPrePt.y, hamNextPt.x,hamNextPt.y);
	#endif
#endif

#if 0
	// 求中心点前后各一个节点（与中心点的距离有要求，如大于10个像素），用于计算夹角
	HAMapPoint hamPrePt, hamNextPt;
	nRet = getNearestPoint(vecMainRoad,	nCenterSi, true, hamPrePt);
	if (nRet<0)
	{
		return -1;
	}
	nRet = getNearestPoint(vecMainRoad,	nCenterSi, false, hamNextPt);
	if (nRet<0)
	{
		return -1;
	}

	// 取中心点与前、后临近点连线上的所有点
	vector<HAMapPoint> vecSubMainRoad;
	vecSubMainRoad.push_back(hamPrePt);
	nRet = getPointsBetweenTwoPoints(hamPrePt,haMainRoadCenterPt,vecSubMainRoad);
	if (nRet<0)
	{
		return -1;
	}
	vecSubMainRoad.push_back(haMainRoadCenterPt);
	int nCenterSiInSubMainRoad = vecSubMainRoad.size() - 1;		// 中心点在子路中的位置
	nRet = getPointsBetweenTwoPoints(haMainRoadCenterPt,hamNextPt,vecSubMainRoad);
	if (nRet<0)
	{
		return -1;
	}
	vecSubMainRoad.push_back(hamNextPt);

	// 缩短子路，使得中心点两侧点数相等，保证两边比重一致
	int nDel = vecSubMainRoad.size() - nCenterSiInSubMainRoad - 1;
	vector<HAMapPoint> vecTemp;
	if (nDel>nCenterSiInSubMainRoad)	// 中心点之后的点数多
	{
		vecTemp.insert(vecTemp.begin(),vecSubMainRoad.begin(),vecSubMainRoad.begin()+2*nCenterSiInSubMainRoad+1);
	} 
	else	// 中心点之前的点数多
	{
		vecTemp.insert(vecTemp.begin(),vecSubMainRoad.begin()+nCenterSiInSubMainRoad-nDel,vecSubMainRoad.end());
	}
	vecSubMainRoad.clear();
	vecSubMainRoad = vecTemp;
	vecTemp.clear();
	nCenterSiInSubMainRoad = vecSubMainRoad.size()/2;
#endif

	// 求中心夹角
	Vec2f vMainRoad1(hamPrePt.x-haMainRoadCenterPt.x, hamPrePt.y-haMainRoadCenterPt.y);		// 前一个点与中心点构成的向量
	Vec2f vMainRoad2(hamNextPt.x-haMainRoadCenterPt.x, hamNextPt.y-haMainRoadCenterPt.y);	// 后一个点与中心点构成的向量	
	float fMainAngle = getAngle(vMainRoad1, vMainRoad2);

#if IS_PRINT_LOG
	#ifdef _WINDOWS_VER_
		printf("	fMainAngle=%f\n",fMainAngle);		
	#else
		LOGD("	fMainAngle=%f\n",fMainAngle);
	#endif
#endif

	// 构造link端点节点
	vector<LinkEndPointNode> vecLinkEndPtnode;
	nRet = formLinkEndPointNode(vecRoadNetLinks, vecRoadNetLinkInfos, vecLinkEndPtnode);
	if (nRet<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet3 - formLinkEndPointNode Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet3 - formLinkEndPointNode Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet3 - formLinkEndPointNode Error!!==============\n");
	#endif
		return -1;
	}

#if IS_PRINT_LOG
	#ifdef _WINDOWS_VER_
		printf("	vecLinkEndPtnode.size=%d\n",vecLinkEndPtnode.size());		
	#else
		LOGD("	vecLinkEndPtnode.size=%d\n",vecLinkEndPtnode.size());
	#endif
#endif
	// =================绘图==================
#ifdef _WINDOWS_VER_
#if IS_DRAW1	// 绘图
	Mat matTemp1(m_matImage.rows,m_matImage.cols,CV_8UC3);
	matTemp1.setTo(0);
	int offset_x1 =0,offset_y1 = 0;
	offset_x1 = haMainRoadCenterPt.x - matTemp1.cols/2;
	offset_y1 = haMainRoadCenterPt.y - matTemp1.rows/2;
	cv::Point ptTemp = cv::Point(haMainRoadCenterPt.x, haMainRoadCenterPt.y) - cv::Point(offset_x1,offset_y1);
	cv::circle( matTemp1,ptTemp,2,cv::Scalar( 0, 255, 0),2,8);	
	for (int i=0; i<vecLinkEndPtnode.size(); i++)
	{
		LinkEndPointNode node = vecLinkEndPtnode[i];
		HAMapPoint hamEndPt = node.hamEndPoint;

		// 区域限制
		if ((abs(hamEndPt.x-haMainRoadCenterPt.x)>CENTER_COVER || 
			abs(hamEndPt.y-haMainRoadCenterPt.y)>CENTER_COVER))
		{
			continue;
		}

		cv::Point ptTemp = cv::Point(hamEndPt.x, hamEndPt.y) - cv::Point(offset_x1,offset_y1);
		cv::circle( matTemp1,ptTemp,2,cv::Scalar( 0, 0, 255 ),2,8);
		for (int j=0; j<node.vecNeighborPoint.size(); j++)
		{
			vector<HAMapPoint> vecTemp;
			vecTemp.push_back(hamEndPt);
			vecTemp.push_back(node.vecNeighborPoint[j]);
			drawImage(matTemp1,haMainRoadCenterPt,vecTemp,cv::Scalar(0,0,0));
			cv::circle( matTemp1,Point2i(node.vecNeighborPoint[j].x,node.vecNeighborPoint[j].y),
				1,cv::Scalar( 0, 255, 0),1,8);
		}
		imshow("matTemp1",matTemp1);
		waitKey(0);		
	}
#endif
#endif
	// =====================================

#ifdef _WINDOWS_VER_
	#if IS_DRAW || IS_DRAW2	// 绘图
		Mat matTemp(m_matImage.rows,m_matImage.cols,CV_8UC3);
		matTemp.setTo(0);
		int offsetx =0,offsety = 0;
		offsetx = haMainRoadCenterPt.x - matTemp.cols/2;
		offsety = haMainRoadCenterPt.y - matTemp.rows/2;
		cv::Point ptOffset(offsetx,offsety);
		//m_ptOffset = ptOffset;		// 成员赋值
		cv::Point ptTemp = cv::Point(haMainRoadCenterPt.x, haMainRoadCenterPt.y) - ptOffset;
		cv::circle( matTemp,ptTemp,2,cv::Scalar( 0, 255, 0),2,8);	
		drawImage(matTemp,haMainRoadCenterPt,/*vecSubMainRoad*/vecMainRoad,cv::Scalar(255,0,0));
		vector<HAMapPoint> vecFirstHalfMainRoad, vecSecondHalfMainRoad;		// 记录前后半段主路
		vecFirstHalfMainRoad.insert(vecFirstHalfMainRoad.begin(),vecMainRoad.begin(),vecMainRoad.begin()+nCenterSi+1);
		vecSecondHalfMainRoad.insert(vecSecondHalfMainRoad.begin(),vecMainRoad.begin()+nCenterSi+1,vecMainRoad.end());
		drawImage(matTemp,haMainRoadCenterPt,vecFirstHalfMainRoad,cv::Scalar(255,0,255),1);		// 前半段，紫色
		drawImage(matTemp,haMainRoadCenterPt,vecSecondHalfMainRoad,cv::Scalar(0,255,255),1);	// 后半段，黄色
	#endif
	#if IS_DRAW2	// 绘图
		for (int i=0; i<nNumLink; i++)
		{
			drawImage(matTemp,haMainRoadCenterPt,vecRoadNetLinks[i],cv::Scalar(0,0,255),1);
		}
		cv::Point ptPre = cv::Point(hamPrePt.x, hamPrePt.y) - ptOffset;
		cv::Point ptNext = cv::Point(hamNextPt.x, hamNextPt.y) - ptOffset;
		Scalar colorMainRoad(255,255,0);
		//line(matTemp,ptTemp,ptPre,colorMainRoad,1);	
		//line(matTemp,ptTemp,ptNext,colorMainRoad,1);	

		// 绘制所有端点
		for (int i=0; i<vecLinkEndPtnode.size(); i++)
		{
			HAMapPoint hamPtTemp = vecLinkEndPtnode[i].hamEndPoint;
			cv::Point ptTemp = cv::Point(hamPtTemp.x,hamPtTemp.y) - ptOffset;
			cv::circle( matTemp,ptTemp,2,cv::Scalar( 255, 255, 255),2,8);
		}

		#if IS_SON_DRAW
			imshow("matTemp",matTemp);
			cv::waitKey(0);
		#endif
		m_matImage = matTemp.clone();
	#endif
#endif


	// 角度匹配
	int nMatchSi = -1, nPreSonSi = -1, nNextSonSi = -1;		// 记录匹配位置
	float fMinDelAngle = 360.f;		// 记录夹角之差的最小绝对值
	int nEndPtNum = vecLinkEndPtnode.size();
	LinkEndPointNode node;
	double uMinError= -1.f;
	HAMapPoint hamMatchPrePt, hamMatchNextPt;	// 匹配点
	//vector<vector<int> > vecMatchPath;		// 记录匹配的路径
	//vector<vector<int> > vecMatchPathNodeId;		// 记录匹配路径上的Node Id
	vector<int> vecMatchPath;		// 记录匹配的路径
	vector<int> vecMatchPathNodeId;		// 记录匹配路径上的Node Id
	vector<int> vecOptionNodeId;		// 记录备选点集合
	double uMinBorderDis = -1;	// 记录对应边界点间最小距离
	double uMinCenterDis = -1;	// 记录匹配点到中心点的最小距离
	double uMinPathDis = -1;
	
	for (int i=0; i<nEndPtNum; i++)
	{
		node = vecLinkEndPtnode[i];
		int nNeighborNodeNum = node.vecNeighborNodeId.size();
		if (nNeighborNodeNum<=2)		// 邻居点限制
		{
			continue;
		}

		HAMapPoint hamEndPt = node.hamEndPoint;	

		// 区域限制
		if ((!isRectInside(hamEndPt,rtScreen))&&((abs(hamEndPt.x-haMainRoadCenterPt.x)>CENTER_COVER || 
			abs(hamEndPt.y-haMainRoadCenterPt.y)>CENTER_COVER)))
		{
			continue;
		}

#ifdef _WINDOWS_VER_
#if IS_DRAW1	// 绘图
		cv::Point ptTemp = cv::Point(hamEndPt.x, hamEndPt.y) - cv::Point(offset_x1,offset_y1);
		cv::circle( matTemp1,ptTemp,2,cv::Scalar( 0, 255, 255 ),2,8);
		for (int j=0; j<node.vecNeighborPoint.size(); j++)
		{
			vector<HAMapPoint> vecTemp;
			vecTemp.push_back(hamEndPt);
			vecTemp.push_back(node.vecNeighborPoint[j]);
			drawImage(matTemp1,haMainRoadCenterPt,vecTemp,cv::Scalar(0,255,255));
			cv::circle( matTemp1,Point2i(node.vecNeighborPoint[j].x,node.vecNeighborPoint[j].y),
				1,cv::Scalar( 0, 255, 255),1,8);
		}
		imshow("matTemp1",matTemp1);
		waitKey(0);
#endif

//#if IS_DRAW2	// 绘图
//		cv::Point ptTemp = cv::Point(hamEndPt.x, hamEndPt.y) - ptOffset;
//		cv::circle( matTemp,ptTemp,2,cv::Scalar( 0, 255, 255),2,8);
//// 		imshow("matTemp",matTemp);
//// 		waitKey(0);
//#endif
#endif
		

		// 延伸link直到超出屏幕边界
		vector<HAMapPoint> vecBorderPt;
		vector<int> vecBorderPtDirection;
		vector<vector<int> > vecPathLinkId;
		vector<vector<int> > vecPathNodeId;

		/*nRet = extendLink(vecRoadNetLinks,vecLinkEndPtnode,	i, rtScreen, vecBorderPt, 
							vecBorderPtDirection, vecPathLinkId, vecPathNodeId);*/
		nRet = extendLink1(vecRoadNetLinks,vecLinkEndPtnode,	i, rtScreen, vecBorderPt, 
 			vecBorderPtDirection, vecPathLinkId, vecPathNodeId);
		if (nRet<0 || vecBorderPt.size()<=0)
		{
			continue;
		}
		
	#ifdef _WINDOWS_VER_
		#if IS_DRAW	// 绘图
			cv::Point ptTemp = cv::Point(hamEndPt.x, hamEndPt.y) - ptOffset;
			cv::circle( matTemp,ptTemp,2,cv::Scalar( 0, 255, 255),2,8);

			for (int j=0; j<vecBorderPt.size(); j++)
			{
				cv::Point ptBorder = cv::Point(vecBorderPt[j].x, vecBorderPt[j].y) - ptOffset;
				Scalar color(0,255,0);
				line(matTemp,ptTemp,ptBorder,color,1);	
			}

			imshow("matTemp",matTemp);
			cv::waitKey(0);
		#endif
	#endif

		// 求与主路中心点前后两向量最接近的两向量
		int nBorderPtNum = vecBorderPt.size();
		int nSi0=-1, nSi1=-1;
		double uDis0 = -1, uDis1 = -1;
		Vec2f v0, v1;
		vector<int> vecStartOptionPtId, vecEndOptionPtId;		// 记录边界起点、终点的候选集
		for (int j=0; j<2; j++)
		{
			Vec2f vMainRoad = vMainRoad1;	// 中心点之前的向量
			if (j>0)
			{				
				vMainRoad = vMainRoad2;	// 中心点之后的向量
			}

			float fMinAngle = 360.f;
			int nTempSi=0;
			Vec2f vTemp;

			for (int k=0; k<nBorderPtNum; k++)
			{
				if (j==0)
				{
					if (vecBorderPtDirection[k]==2)
					{
						continue;
					}
				}
				else
				{
					if (vecBorderPtDirection[k]==3)
					{
						continue;
					}
				}
				
				//LinkEndPointNode nextNode = vecLinkEndPtnode[node.vecNeighborNodeId[k]];
				//HAMapPoint hamPt = nextNode.hamEndPoint;
				HAMapPoint hamPt = vecBorderPt[k];
				Vec2f v(hamPt.x-hamEndPt.x,hamPt.y-hamEndPt.y);

#if 0
				// ===================方向限制==============
				// 方向判断 int direction;//道路方向:0未调查,默认双向,1双向,2正方向(link的起点到终点),3反方向
				int nLinkId = node.vecLinkId[k];
				int nDirection = vecRoadNetLinkInfos[nLinkId].direction;
				bool bIsEnd = (hamEndPt==(HAMapPoint)vecRoadNetLinks[nLinkId][0])?false:true;		// 标识端点是否属于link的终点
				if (j==0)	// 标识端点应为link终点
				{
					if ((bIsEnd==true&&nDirection==3) || (bIsEnd==false&&nDirection==2))
					{
						continue;
					}
				} 
				else		// 标识端点应为link起点
				{
					if ((bIsEnd==true&&nDirection==2) || (bIsEnd==false&&nDirection==3))
					{
						continue;
					}
				}
				// =====================================
#endif

				float fAngle = getAngle(vMainRoad, v);				
				if (fAngle<VECTOR_NEAR_ANGLE/*fMinAngle*/)
				{
					fMinAngle = fAngle;
					nTempSi = k;
					vTemp = v;

					if (j==0)
					{
						vecStartOptionPtId.push_back(nTempSi);
					} 
					else
					{
						vecEndOptionPtId.push_back(nTempSi);
					}
				}				
			}	// End k

		}	// End j
		/*if (nSi0<0 || nSi1<0 || nSi0==nSi1)
		{
			continue;
		}*/
		if (vecStartOptionPtId.size()<=0 || vecEndOptionPtId.size()<=0)
		{
			continue;
		}

		// 关于候选路径，求路径起点、终点离主路起点、终点距离最近的路径
		double uMinOptionDis = 2*(rtScreen.width+rtScreen.height);				// 设定一个达不到的初值，便于循环比较
		double uMaxOptionDis = -1;
		for (int j=0; j<vecStartOptionPtId.size(); j++)
		{
			HAMapPoint hamStartOptionPt = vecBorderPt[vecStartOptionPtId[j]];
			Vec2f vStart(hamStartOptionPt.x-hamEndPt.x,hamStartOptionPt.y-hamEndPt.y);
			for (int k=0; k<vecEndOptionPtId.size(); k++)
			{
				HAMapPoint hamEndOptionPt = vecBorderPt[vecEndOptionPtId[k]];

				if (hamStartOptionPt==hamEndOptionPt)
				{
					continue;
				}

				Vec2f vEnd(hamEndOptionPt.x-hamEndPt.x,hamEndOptionPt.y-hamEndPt.y);
				float fAngle = getAngle(vStart,vEnd);
				float fDelAngle = abs(fMainAngle-fAngle);
				if (fDelAngle<ANGLE_ALLOWANCE)
				{
					// 求距离
					float fStartDis = getDistancePoint2Point(hamStartOptionPt.x,hamStartOptionPt.y,hamPrePt.x,hamPrePt.y);
					float fEndDis = getDistancePoint2Point(hamEndOptionPt.x,hamEndOptionPt.y,hamNextPt.x,hamNextPt.y);
					float fDis = fStartDis + fEndDis;
					
					if (fDis<uMinOptionDis)
					{
						uMinOptionDis = fDis;
						nSi0 = vecStartOptionPtId[j];
						nSi1 = vecEndOptionPtId[k];
						v0 = vStart;
						v1 = vEnd;
					}
				}
			}
		}

		if (nSi0<0 || nSi1<0)
		{
			continue;
		}

		// 保存每个节点的方向数据，绿-与主路同向，红-与主路反向
#ifdef _WINDOWS_VER_
#if 0//IS_DRAW2	// 绘图								
		cv::Point ptV = Point(hamEndPt.x,hamEndPt.y) - ptOffset;
		cv::Point ptTemp0 = Point(vecBorderPt[nSi0].x,vecBorderPt[nSi0].y) - ptOffset;		
		cv::Point ptTemp1 = Point(vecBorderPt[nSi1].x,vecBorderPt[nSi1].y) - ptOffset;						
		cv::circle( matTemp,ptV,1,cv::Scalar( 0, 255, 255),2,8);
		cv::circle( matTemp,ptTemp0,1,cv::Scalar( 0, 255, 255),2,8);
		cv::circle( matTemp,ptTemp1,1,cv::Scalar( 0, 255, 255),2,8);
		Scalar color(255,255,255);
		cv::line(matTemp,ptTemp0,ptV,color,1);	
		cv::line(matTemp,ptV,ptTemp1,color,1);	
#endif
#endif

#if 0	// 利用最佳匹配角
		// 求夹角		
		float fAngle = getAngle(v0,v1);
		float fDelAngle = abs(fMainAngle-fAngle);
		if (fDelAngle<fMinDelAngle)
		{
			fMinDelAngle = fDelAngle;
			nMatchSi = i;
			hamMatchPrePt = vecBorderPt[nSi0];
			hamMatchNextPt = vecBorderPt[nSi1];

			vecMatchPath.clear();
			vecMatchPath.push_back(vecPathLinkId[nSi0]);
			vecMatchPath.push_back(vecPathLinkId[nSi1]);

			vecMatchPathNodeId.clear();
			vecMatchPathNodeId.push_back(vecPathNodeId[nSi0]);
			vecMatchPathNodeId.push_back(vecPathNodeId[nSi1]);
		}
#else	// 记录候选点，利用边界点间距离最短做判据
		float fAngle = getAngle(v0,v1);
		float fDelAngle = abs(fMainAngle-fAngle);
		if (fDelAngle<ANGLE_ALLOWANCE)
		{
			vecOptionNodeId.push_back(i);			

			// 匹配点与中心点距离
			double uCenterDis = sqrt((haMainRoadCenterPt.x-hamEndPt.x)*(haMainRoadCenterPt.x-hamEndPt.x)+
				(haMainRoadCenterPt.y-hamEndPt.y)*(haMainRoadCenterPt.y-hamEndPt.y));

			double uPathDis0 = -1, uPathDis1 = -1, uPathDis = -1;
			nRet = getDisBorder2MatchPt(vecRoadNetLinks, vecPathLinkId[nSi0], rtScreen,
										vecBorderPt[nSi0], hamEndPt, uPathDis0);
			if (nRet<0)
			{
				continue;
			}
			nRet = getDisBorder2MatchPt(vecRoadNetLinks, vecPathLinkId[nSi1], rtScreen,
										vecBorderPt[nSi0], hamEndPt, uPathDis1);
			if (nRet<0)
			{
				continue;
			}
			uPathDis = uPathDis0 + uPathDis1;


			// 边界点间距离
			double uDis = uDis0 + uDis1;
			if (uMinBorderDis<0)
			{
				uMinBorderDis = uMinOptionDis/*uDis*/;
				nMatchSi = i;

				hamMatchPrePt = vecBorderPt[nSi0];
				hamMatchNextPt = vecBorderPt[nSi1];
								
				vecMatchPath.clear();
				vecMatchPath = vecPathLinkId[nSi0];
				reverseOrder(vecMatchPath);		// 倒序重排，与主路方向一致				
				vecMatchPath.insert(vecMatchPath.end(),vecPathLinkId[nSi1].begin(),vecPathLinkId[nSi1].end());

				vecMatchPathNodeId.clear();
				vecMatchPathNodeId = vecPathNodeId[nSi0];
				reverseOrder(vecMatchPathNodeId);		// 倒序重排，与主路方向一致
				vecMatchPathNodeId.erase(vecMatchPathNodeId.end()-1);
				vecMatchPathNodeId.insert(vecMatchPathNodeId.end(),vecPathNodeId[nSi1].begin(),vecPathNodeId[nSi1].end());				

				uMinCenterDis = uCenterDis;
				uMinPathDis = uPathDis;
			} 
			else
			{
				if (/*uDis*/uMinOptionDis<uMinBorderDis)
				{
					uMinBorderDis = uMinOptionDis/*uDis*/;
					nMatchSi = i;

					hamMatchPrePt = vecBorderPt[nSi0];
					hamMatchNextPt = vecBorderPt[nSi1];

				
					vecMatchPath.clear();
					vecMatchPath = vecPathLinkId[nSi0];
					reverseOrder(vecMatchPath);		// 倒序重排，与主路方向一致				
					vecMatchPath.insert(vecMatchPath.end(),vecPathLinkId[nSi1].begin(),vecPathLinkId[nSi1].end());

					vecMatchPathNodeId.clear();
					vecMatchPathNodeId = vecPathNodeId[nSi0];
					reverseOrder(vecMatchPathNodeId);		// 倒序重排，与主路方向一致
					vecMatchPathNodeId.erase(vecMatchPathNodeId.end()-1);
					vecMatchPathNodeId.insert(vecMatchPathNodeId.end(),vecPathNodeId[nSi1].begin(),vecPathNodeId[nSi1].end());	

					uMinCenterDis = uCenterDis;
					uMinPathDis = uPathDis;
				}
				else if (abs(/*uDis*/uMinOptionDis-uMinBorderDis)<1e-10)		// 相等时，基于路径长度
				{
					if (uPathDis<uMinPathDis)
					{
						uMinBorderDis = uMinOptionDis/*uDis*/;
						nMatchSi = i;

						hamMatchPrePt = vecBorderPt[nSi0];
						hamMatchNextPt = vecBorderPt[nSi1];
						

						vecMatchPath.clear();
						vecMatchPath = vecPathLinkId[nSi0];
						reverseOrder(vecMatchPath);		// 倒序重排，与主路方向一致				
						vecMatchPath.insert(vecMatchPath.end(),vecPathLinkId[nSi1].begin(),vecPathLinkId[nSi1].end());

						vecMatchPathNodeId.clear();
						vecMatchPathNodeId = vecPathNodeId[nSi0];
						reverseOrder(vecMatchPathNodeId);		// 倒序重排，与主路方向一致
						vecMatchPathNodeId.erase(vecMatchPathNodeId.end()-1);
						vecMatchPathNodeId.insert(vecMatchPathNodeId.end(),vecPathNodeId[nSi1].begin(),vecPathNodeId[nSi1].end());	

						uMinCenterDis = uCenterDis;
						uMinPathDis = uPathDis;
					}
					else if (abs(uPathDis-uMinPathDis)<1e-10)		// 相等时，基于路径长度
					{
						if (uCenterDis<uMinCenterDis)
						{
							uMinBorderDis = uMinOptionDis/*uDis*/;
							nMatchSi = i;

							hamMatchPrePt = vecBorderPt[nSi0];
							hamMatchNextPt = vecBorderPt[nSi1];


							vecMatchPath.clear();
							vecMatchPath = vecPathLinkId[nSi0];
							reverseOrder(vecMatchPath);		// 倒序重排，与主路方向一致				
							vecMatchPath.insert(vecMatchPath.end(),vecPathLinkId[nSi1].begin(),vecPathLinkId[nSi1].end());

							vecMatchPathNodeId.clear();
							vecMatchPathNodeId = vecPathNodeId[nSi0];
							reverseOrder(vecMatchPathNodeId);		// 倒序重排，与主路方向一致
							vecMatchPathNodeId.erase(vecMatchPathNodeId.end()-1);
							vecMatchPathNodeId.insert(vecMatchPathNodeId.end(),vecPathNodeId[nSi1].begin(),vecPathNodeId[nSi1].end());	

							uMinCenterDis = uCenterDis;
							uMinPathDis = uPathDis;
						}
					}
				}
			}			
		}

#endif


		// =================绘图==================
#ifdef _WINDOWS_VER_
#if IS_DRAW	// 绘图
		//Mat matTemp(m_matImage.rows,m_matImage.cols,CV_8UC3);
		//matTemp.setTo(0);
		//int offset_x =0,offset_y = 0;
		offsetx = haMainRoadCenterPt.x - matTemp.cols/2;
		offsety = haMainRoadCenterPt.y - matTemp.rows/2;
		cv::Point ptTemp = cv::Point(hamEndPt.x, hamEndPt.y) - cv::Point(offsetx,offsety);
		cv::circle( matTemp,ptTemp,2,cv::Scalar( 0, 0, 255 ),2,8);
		vector<HAMapPoint> vecTemp;
		vecTemp.push_back(node.vecNeighborPoint[nSi0]);
		vecTemp.push_back(hamEndPt);
		vecTemp.push_back(node.vecNeighborPoint[nSi1]);
		drawImage(matTemp,haMainRoadCenterPt,vecTemp,cv::Scalar(0,0,0));
		cv::circle( matTemp,Point2i(node.vecNeighborPoint[nSi0].x,node.vecNeighborPoint[nSi0].y),
			1,cv::Scalar( 0, 255, 0),1,8);
		cv::circle( matTemp,Point2i(node.vecNeighborPoint[nSi1].x,node.vecNeighborPoint[nSi1].y),
			1,cv::Scalar( 0, 255, 0),1,8);
		imshow("matTemp",matTemp);
		waitKey(0);
#endif
#endif
		// =====================================
#if 0
		// 求距离
		double uError = 0.f;
		nRet = getMainSubLine2SubNetDis(vecSubMainRoad, 
			nCenterSiInSubMainRoad,
			node.vecNeighborPoint[nSi0],
			hamEndPt,
			node.vecNeighborPoint[nSi1],										
			uError);
		if (nRet<0)
		{
			continue;
		}
		if (uMinError<0)
		{
			uMinError = uError;
			nMatchSi = i;
		}
		else
		{
			nMatchSi = (uError<uMinError)?i:nMatchSi;
			uMinError = (uError<uMinError)?uError:uMinError;
		}
#endif
	}		// End i
	
	if (nMatchSi<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet3 - no Match Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet3 - no Match Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet3 - no Match Error!!==============\n");
	#endif
		return -1;
	}

#if IS_PRINT_LOG
	#ifdef _WINDOWS_VER_
		printf("	nMatchSi=%d\n",nMatchSi);		
	#else
		LOGD("	nMatchSi=%d\n",nMatchSi);
	#endif
#endif

	// 输出
	//nMatchSi = 29;
	hamCenterInNet = vecLinkEndPtnode[nMatchSi].hamEndPoint;
	

	// 由一个点构造路网	
	vector<int> vecRoadNetDirection2MainRoad;
	vector<vector<int> > vecCrossPathLinkID;		// 与主路相交的每条岔路的link Id
	vector<vector<int> > vecCrossPathNodeId;
 	//nRet = formRoadNet(vecRoadNetLinks,
 	//	vecLinkEndPtnode,
 	//	/*vecMainRoadNodeId*/vecMatchPathNodeId,
 	//	rtScreen,
 	//	vecRoadNetDirection2MainRoad);

	int nMatchCenterSite = 0;
	if(!isBelongToVector(vecMatchPathNodeId,nMatchSi,nMatchCenterSite))
	{
		return -1;
	}
	
	// =========== Error: 此处写死，只为保暂时演示效果，演示完一定要删除==========
	if (hamCenterInNet.x==831136 && hamCenterInNet.y==1074731)
	{		
		// temp
		vector<int> vecMatchPathTemp;
		vector<int> vecMatchPathNodeIdTemp;
		for (int i=0; i<=nMatchCenterSite-1; i++)
		{
			vecMatchPathTemp.push_back(vecMatchPath[i]);
			vecMatchPathNodeIdTemp.push_back(vecMatchPathNodeId[i]);
		}
		vecMatchPathNodeIdTemp.push_back(vecMatchPathNodeId[nMatchCenterSite]);

		vecMatchPathTemp.push_back(29);
		vecMatchPathTemp.push_back(14);
		vecMatchPathTemp.push_back(71);
		vecMatchPathTemp.push_back(72);

		vecMatchPathNodeIdTemp.push_back(12);
		vecMatchPathNodeIdTemp.push_back(19);
		vecMatchPathNodeIdTemp.push_back(70);
		vecMatchPathNodeIdTemp.push_back(23);

		
		// 修改主路
		vecMatchPath = vecMatchPathTemp;
		vecMatchPathNodeId = vecMatchPathNodeIdTemp;

		
		// 修改终点
		HAMapPoint hamPreTemp = hamCenterInNet;
		bool bFlag = false;
		for (int i=nMatchCenterSite; i<vecMatchPath.size(); i++)
		{			
			int nTempLinkId = vecMatchPath[i];
			bool bR1 = isRectInside(vecRoadNetLinks[nTempLinkId][0],rtScreen);
			bool bR2 = isRectInside(vecRoadNetLinks[nTempLinkId][vecRoadNetLinks[nTempLinkId].size()-1],rtScreen);
			if (!(bR1&bR2))
			{
				nRet = getCrossPointLink2Rect(vecRoadNetLinks[nTempLinkId],
					rtScreen, hamMatchNextPt);
				if (nRet<0)
				{
					break;
				}
				bFlag = true;
				break;
			}
		}
		
		if (!bFlag)
		{
			hamMatchNextPt = vecLinkEndPtnode[vecMatchPathNodeId[vecMatchPathNodeId.size()-1]].hamEndPoint;
		}		
	}
	// ============================================================================


	cv::Rect rtExScreen(rtScreen.x-nCrossRoadLen,rtScreen.y-nCrossRoadLen,
						rtScreen.width+2*nCrossRoadLen,rtScreen.height+2*nCrossRoadLen);		// 扩大的窗口，用于延伸岔路
// 	nRet = formRoadNet4(vecRoadNetLinks,vecRoadNetLinkInfos,
// 						vecLinkEndPtnode,
// 						vecMatchPathNodeId,
// 						nMatchCenterSite,
// 						rtExScreen/*rtScreen*/,
// 						vecRoadNetDirection2MainRoad,
// 						vecCrossPathLinkID,
// 						vecCrossPathNodeId);

	nRet = formRoadNet5(vecRoadNetLinks,vecRoadNetLinkInfos,
		vecLinkEndPtnode,
		vecMatchPathNodeId,
		nMatchCenterSite,
		rtScreen,
		rtExScreen,
		vecRoadNetDirection2MainRoad,
		vecCrossPathLinkID,
		vecCrossPathNodeId,
		vecHistoryCrossPt);
	if (nRet < 0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet5 - formRoadNet3 Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet5 - formRoadNet3 Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet5 - formRoadNet3 Error!!==============\n");
	#endif
		return -1;
	}

#if IS_PRINT_LOG
	#ifdef _WINDOWS_VER_
		printf("	vecMatchPathNodeId.size=%d,nMatchCenterSite=%d\n",
			vecMatchPathNodeId.size(),nMatchCenterSite);	
		printf("matchMainRoadCenterInNet-vecMatchPathNodeId: \n");	
		for (int i=0; i<vecMatchPathNodeId.size(); i++)
		{
			printf("%d, ",	vecMatchPathNodeId[i]);	
		}
		printf("\n	vecCrossPathLinkID.size=%d,vecCrossPathNodeId.size=%d\n",
			vecCrossPathLinkID.size(),vecCrossPathNodeId.size());		
	#else
		LOGD("	vecMatchPathNodeId.size=%d,nMatchCenterSite=%d\n",
			vecMatchPathNodeId.size(),nMatchCenterSite);	
		LOGD("matchMainRoadCenterInNet-vecMatchPathNodeId: \n");	
		for (int i=0; i<vecMatchPathNodeId.size(); i++)
		{
			LOGD("%d, ",	vecMatchPathNodeId[i]);	
		}
		LOGD("\n	vecCrossPathLinkID.size=%d,vecCrossPathNodeId.size=%d\n",
			vecCrossPathLinkID.size(),vecCrossPathNodeId.size());
	#endif
#endif

	// 过滤路网，不包括主路
	vector<vector<HAMapPoint> > vecAllCrossPathPt;
	nRet = filterRoadNet1(vecRoadNetLinks, vecLinkEndPtnode, nCrossRoadLen,vecCrossPathLinkID,
							vecCrossPathNodeId,	rtExScreen/*rtScreen*/, vecAllCrossPathPt);
 	if (nRet<0)
 	{
 	#ifdef _WINDOWS_VER_
 		printf("==============matchMainRoadCenterInNet5 - filterRoadNet1 Error!!==============\n");
 		m_strErrorLog = "matchMainRoadCenterInNet5 - filterRoadNet1 Error!!";
 	#else
 		LOGD("==============matchMainRoadCenterInNet5 - filterRoadNet Error!!==============\n");
 	#endif
 		return -1;
 	}
	vecCrossPathPt = vecAllCrossPathPt;

#if IS_PRINT_LOG
	#ifdef _WINDOWS_VER_
		printf("	vecAllCrossPathPt.size=%d\n", vecAllCrossPathPt.size());		
	#else
		LOGD("	vecAllCrossPathPt.size=%d\n", vecAllCrossPathPt.size());	
	#endif
#endif

	// 添加匹配的主路点，及与岔路交点在主路中的下标
	vector<HAMapPoint> vecMatchPathPt;

	vecMatchPathPt.push_back(hamMatchPrePt);		// 第一个点
	for (int i=0; i<vecMatchPath.size(); i++)
	{
		int nLinkId = vecMatchPath[i];
		if (nLinkId<0)
		{
			continue;
		}

		// link两个端点对应的node Id
		int nPathStartNodeId = vecMatchPathNodeId[i];
		int nPathEndNodeId = vecMatchPathNodeId[i+1];

		vector<HAMapPoint> vecTemp = vecRoadNetLinks[nLinkId];
		// 判断方向
		if (vecTemp[0]==vecLinkEndPtnode[nPathEndNodeId].hamEndPoint)
		{
			reverseOrder(vecTemp);
		}

		int nTempNum = vecTemp.size();
		for (int j=0; j<nTempNum-1; j++)
		{
			HAMapPoint hamPt = vecTemp[j];			
			if (isRectInside(hamPt, rtScreen) && 
				hamPt!=vecMatchPathPt[vecMatchPathPt.size()-1])
			{
				// 求岔路与主路交点在主路中的位置
				if (j==0 && vecLinkEndPtnode[nPathStartNodeId].vecNeighborNodeId.size()>2)
				{
					vecCrossPointIndex.push_back(vecMatchPathPt.size());
				}

				vecMatchPathPt.push_back(hamPt);					
			}
		} // end j
	}	// end i

	vecMatchPathPt.push_back(hamMatchNextPt);	// 最后一个点


	// 求匹配点在集合中的位置
	nMatchCenterIndex = -1;
	for (int i=0; i<vecMatchPathPt.size(); i++)
	{
		if (hamCenterInNet==vecMatchPathPt[i])
		{
			nMatchCenterIndex = i;
			break;
		}
	}
	if (nMatchCenterIndex<0)
	{
		return -1;
	}

	//vecCrossGpsLinks.insert(vecCrossGpsLinks.begin(),vecMatchPathPt);		// 添加，主路作为第0个
	vecCrossPathPt.push_back(vecMatchPathPt);		// 添加，主路作为最后一个，方便平移

	// 计算从匹配路径移至主路的最佳平移距离
	vector<HAMapPoint> vecMainRoadVertex, vecMatchRoadVertex;
	vecMainRoadVertex.push_back(hamPrePt);
	vecMainRoadVertex.push_back(hamNextPt);
	vecMatchRoadVertex.push_back(hamMatchPrePt);
	vecMatchRoadVertex.push_back(hamMatchNextPt);
	HAMapPoint hamOffset;
	nRet = getOffset2MainRoad(vecMainRoadVertex, vecMatchRoadVertex, hamOffset);
	if (nRet<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet3 - getOffset2MainRoad Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet3 - getOffset2MainRoad Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet3 - getOffset2MainRoad Error!!==============\n");
	#endif
		return -1;
	}

#if IS_PRINT_LOG
	#ifdef _WINDOWS_VER_
		printf("	hamOffset.x=%d,hamOffset.y=%d\n", hamOffset.x,hamOffset.y);		
	#else
		LOGD("	hamOffset.x=%d,hamOffset.y=%d\n", hamOffset.x,hamOffset.y);
	#endif
#endif

	// 平移
	nRet = translateRoadNet(vecCrossPathPt, hamOffset);
	if (nRet<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet3 - translateRoadNet Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet3 - translateRoadNet Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet3 - translateRoadNet Error!!==============\n");
	#endif
		return -1;
	}

	// 获取平移后的主路匹配点
	vecMainRoadPtInNet = vecCrossPathPt[vecCrossPathPt.size()-1];
	vecCrossPathPt.erase(vecCrossPathPt.end()-1);		// 去掉最后一个vector元素，除去主路

	hamCenterInNet = vecMainRoadPtInNet[nMatchCenterIndex];

#if IS_PRINT_LOG
	#ifdef _WINDOWS_VER_
		printf("	hamCenterInNet.x=%d,hamCenterInNet.y=%d\n", hamCenterInNet.x,hamCenterInNet.y);		
	#else
		LOGD("	hamCenterInNet.x=%d,hamCenterInNet.y=%d\n", hamCenterInNet.x,hamCenterInNet.y);	
	#endif
#endif

	// 画图
#ifdef _WINDOWS_VER_
#if IS_DRAW
	//Mat matNavi(800,800,CV_8UC3);
	//matNavi.setTo(0);
	Mat matNavi = m_matImage;
	drawImage(matNavi, haMainRoadCenterPt, vecMainRoad,Scalar(255,0,0));		// 原始主路	
	for (int i=0; i<nNumLink; i++)
	{
		drawImage(matNavi,haMainRoadCenterPt, vecRoadNetLinks[i],Scalar(0,0,0));
	}

	// 匹配的主路
#if 0
	vector<HAMapPoint> vecNewMainRoad(nNumMainRoadPt);
	int nDx = hamCenterInNet.x - haMainRoadCenterPt.x;
	int nDy = hamCenterInNet.y - haMainRoadCenterPt.y;
	for (int i=0; i<nNumMainRoadPt; i++)
	{
		vecNewMainRoad[i].x = vecMainRoad[i].x+nDx;
		vecNewMainRoad[i].y = vecMainRoad[i].y+nDy;
	}
	drawImage(matNavi, haMainRoadCenterPt, vecNewMainRoad,Scalar(0,255,0));		// 新主路
#else
	int nSubMainRoadNum = vecSubMainRoad.size();
	vector<HAMapPoint> vecNewMainRoad(nSubMainRoadNum);
	int nDx = hamCenterInNet.x - haMainRoadCenterPt.x;
	int nDy = hamCenterInNet.y - haMainRoadCenterPt.y;
	for (int i=0; i<nSubMainRoadNum; i++)
	{
		vecNewMainRoad[i].x = vecSubMainRoad[i].x+nDx;
		vecNewMainRoad[i].y = vecSubMainRoad[i].y+nDy;
	}
	drawImage(matNavi, haMainRoadCenterPt, vecNewMainRoad,Scalar(0,255,0));		// 新主路
#endif


	// 两个中心点
	int offset_x =0,offset_y = 0;
	offset_x = haMainRoadCenterPt.x - matNavi.cols/2;
	offset_y = haMainRoadCenterPt.y - matNavi.rows/2;
	cv::Point ptOld = cv::Point(haMainRoadCenterPt.x, haMainRoadCenterPt.y) - cv::Point(offset_x,offset_y);
	cv::Point ptNew = cv::Point(hamCenterInNet.x, hamCenterInNet.y) - cv::Point(offset_x,offset_y);
	cv::circle( matNavi,ptOld,2,cv::Scalar( 0, 0, 255 ),2,8);
	cv::circle( matNavi,ptNew,2,cv::Scalar( 0, 0, 255 ),2,8);

#if IS_SON_DRAW
	imshow("matNavi",matNavi);
	waitKey(0);
#endif
#endif

#if IS_DRAW2	// 绘图
	node = vecLinkEndPtnode[nMatchSi];
	cv::Point ptV = Point(node.hamEndPoint.x,node.hamEndPoint.y) - ptOffset;
	//cv::Point ptTemp0 = Point(node.vecNeighborPoint[nPreSonSi].x,node.vecNeighborPoint[nPreSonSi].y) - ptOffset;		
	//cv::Point ptTemp1 = Point(node.vecNeighborPoint[nNextSonSi].x,node.vecNeighborPoint[nNextSonSi].y) - ptOffset;
	cv::Point ptTemp0 = Point(hamMatchPrePt.x,hamMatchPrePt.y) - ptOffset;		
	cv::Point ptTemp1 = Point(hamMatchNextPt.x,hamMatchNextPt.y) - ptOffset;
	cv::circle( matTemp,ptV,1,cv::Scalar( 255, 0, 255),4,8);
	cv::circle( matTemp,ptTemp0,1,cv::Scalar( 0, 255, 255),3,8);
	cv::circle( matTemp,ptTemp1,1,cv::Scalar( 0, 255, 255),3,8);
	Scalar color(100,100,0);

	// 绘制匹配路径起点、匹配点、终点连线
 	cv::line(matTemp,ptTemp0,ptV,color,1);	
 	cv::line(matTemp,ptV,ptTemp1,color,1);

	// 绘制匹配路径
	for (int i=0; i<vecMatchPath.size(); i++)
	{
		int nLinkId = vecMatchPath[i];
		if (nLinkId>=0)
		{
			drawImage(matTemp, haMainRoadCenterPt, vecRoadNetLinks[nLinkId],cv::Scalar(255,255,0),2);
		}		
	}

	/*for (int i=0; i<vecMatchPath.size(); i++)
	{
	vector<int> vecSinglePath = vecMatchPath[i];
	for (int j=0; j<vecSinglePath.size(); j++)
	{
	int nLinkId = vecSinglePath[j];
	if (nLinkId>=0)
	{
	drawImage(matTemp, haMainRoadCenterPt, vecRoadNetLinks[nLinkId],cv::Scalar(255,255,0),2);
	}			
	}		
	}*/
	
	// 基于主路绘制路网
	for (int i=0; i<nNumLink; i++)
	{
		if (vecRoadNetDirection2MainRoad[i]!=OPPOSITE_DIRECTION)
		{
			drawImage(matTemp, haMainRoadCenterPt, vecRoadNetLinks[i],cv::Scalar(0,255,255),1);
		}
	}

	// 绘制提取到的岔路
	for (int i=0; i<vecAllCrossPathPt.size(); i++)
	{
		drawImage(matTemp, haMainRoadCenterPt, vecAllCrossPathPt[i],cv::Scalar(0,255,0),1);
	}


	// 绘制主路与岔路的交点
	for (int i=0; i<vecCrossPointIndex.size(); i++)
	{
		cv::Point ptTemp = Point(vecMatchPathPt[vecCrossPointIndex[i]].x,vecMatchPathPt[vecCrossPointIndex[i]].y) - ptOffset;		
		cv::circle( matTemp,ptTemp,1,cv::Scalar( 0, 0, 255),5,8);
	}

	m_matImage = matTemp.clone();

	#if IS_SON_DRAW
		cv::imshow("m_matImage",m_matImage);
		cv::waitKey(0);
	#endif

	#if 0
		// 保存
		string strTemp = "D:\\Halo\\ArWay\\output\\direction\\";
		strTemp += m_strImageName.substr(m_strImageName.find_last_of("\\")+1);
		cv::imwrite(strTemp, matTemp);
	#endif
#endif
#endif

	return 0;
}

int MergeMapData::matchMainRoadCenterInNet6(const vector<HAMapPoint>& vecMainRoad,
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
											std::vector<std::vector<HAMapPoint> >& vecCrossPathPt)
{
	// 参数自检
	int nNumMainRoadPt = vecMainRoad.size();
	int nNumLink = vecRoadNetLinks.size();
	if (nNumMainRoadPt<=0 || nNumLink<=0 ||
		nNumLink!=vecRoadNetLinkInfos.size())
	{
		// 打印log		
		#ifdef _WINDOWS_VER_
			printf("==============matchMainRoadCenterInNet - parameter Error!!==============\n");
			m_strErrorLog = "matchMainRoadCenterInNet - parameter Error!!";
		#else
			LOGD("==============matchMainRoadCenterInNet - parameter Error!!==============\n");
		#endif
		return -1;
	}

	int nRet = 0;

	// 求中心点位置
	int nCenterSi = -1;
	nRet = getPointSite(vecMainRoad, haMainRoadCenterPt, nCenterSi);
	if (nRet<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet - getPointSite Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet - getPointSite Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet - getPointSite Error!!==============\n");
	#endif
		return -1;
	}
		
	// 获取屏幕边界上的主路起点、终点
	HAMapPoint hamPrePt, hamNextPt;
#if 1
	nRet = getStartEndPoint(vecMainRoad, nCenterSi, rtScreen, hamPrePt, hamNextPt);
	if (nRet==-1)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet - getStartEndPoint Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet - getStartEndPoint Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet - getStartEndPoint Error!!==============\n");
	#endif
		return -1;
	}

	if (nRet<-1)
	{
		hamPrePt = vecMainRoad[0];
		hamNextPt = vecMainRoad[nNumMainRoadPt-1];
	}
#else
	#ifdef _WINDOWS_VER_	
		nRet = getStartEndPoint(vecMainRoad, nCenterSi, rtScreen, hamPrePt, hamNextPt);
		if (nRet==-1)
		{
		#ifdef _WINDOWS_VER_
			printf("==============matchMainRoadCenterInNet - getStartEndPoint Error!!==============\n");
			m_strErrorLog = "matchMainRoadCenterInNet - getStartEndPoint Error!!";
		#else
			LOGD("==============matchMainRoadCenterInNet - getStartEndPoint Error!!==============\n");
		#endif
			return -1;
		}

		if (nRet<-1)
		{
			hamPrePt = vecMainRoad[0];
			hamNextPt = vecMainRoad[nNumMainRoadPt-1];
		}
	#else
		hamPrePt = vecMainRoad[0];
		hamNextPt = vecMainRoad[nNumMainRoadPt-1];
	#endif
#endif

	// 求中心夹角
	Vec2f vMainRoad1(hamPrePt.x-haMainRoadCenterPt.x, hamPrePt.y-haMainRoadCenterPt.y);		// 前一个点与中心点构成的向量
	Vec2f vMainRoad2(hamNextPt.x-haMainRoadCenterPt.x, hamNextPt.y-haMainRoadCenterPt.y);	// 后一个点与中心点构成的向量	
	float fMainAngle = getAngle(vMainRoad1, vMainRoad2);

	// 构造link端点节点
	vector<LinkEndPointNode> vecLinkEndPtnode;
	nRet = formLinkEndPointNode(vecRoadNetLinks, vecRoadNetLinkInfos, vecLinkEndPtnode);
	if (nRet<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet - formLinkEndPointNode Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet - formLinkEndPointNode Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet - formLinkEndPointNode Error!!==============\n");
	#endif
		return -1;
	}
	
#ifdef _WINDOWS_VER_
	#if IS_DRAW || IS_DRAW2	// 绘图
		Mat matTemp(m_matImage.rows,m_matImage.cols,CV_8UC3);
		matTemp.setTo(0);
		int offsetx =0,offsety = 0;
		offsetx = haMainRoadCenterPt.x - matTemp.cols/2;
		offsety = haMainRoadCenterPt.y - matTemp.rows/2;
		cv::Point ptOffset(offsetx,offsety);
		//m_ptOffset = ptOffset;		// 成员赋值
		cv::Point ptTemp = cv::Point(haMainRoadCenterPt.x, haMainRoadCenterPt.y) - ptOffset;
		cv::circle( matTemp,ptTemp,2,cv::Scalar( 0, 255, 0),2,8);	
		drawImage(matTemp,haMainRoadCenterPt,/*vecSubMainRoad*/vecMainRoad,cv::Scalar(255,0,0));
		vector<HAMapPoint> vecFirstHalfMainRoad, vecSecondHalfMainRoad;		// 记录前后半段主路
		vecFirstHalfMainRoad.insert(vecFirstHalfMainRoad.begin(),vecMainRoad.begin(),vecMainRoad.begin()+nCenterSi+1);
		vecSecondHalfMainRoad.insert(vecSecondHalfMainRoad.begin(),vecMainRoad.begin()+nCenterSi+1,vecMainRoad.end());
		drawImage(matTemp,haMainRoadCenterPt,vecFirstHalfMainRoad,cv::Scalar(255,0,255),1);		// 前半段，紫色
		drawImage(matTemp,haMainRoadCenterPt,vecSecondHalfMainRoad,cv::Scalar(0,255,255),1);	// 后半段，黄色
	#endif
	#if IS_DRAW2	// 绘图
		for (int i=0; i<nNumLink; i++)
		{
			drawImage(matTemp,haMainRoadCenterPt,vecRoadNetLinks[i],cv::Scalar(0,0,255),1);
		}
		cv::Point ptPre = cv::Point(hamPrePt.x, hamPrePt.y) - ptOffset;
		cv::Point ptNext = cv::Point(hamNextPt.x, hamNextPt.y) - ptOffset;
		Scalar colorMainRoad(255,255,0);
		//line(matTemp,ptTemp,ptPre,colorMainRoad,1);	
		//line(matTemp,ptTemp,ptNext,colorMainRoad,1);	

		// 绘制所有端点
		for (int i=0; i<vecLinkEndPtnode.size(); i++)
		{
			HAMapPoint hamPtTemp = vecLinkEndPtnode[i].hamEndPoint;
			cv::Point ptTemp = cv::Point(hamPtTemp.x,hamPtTemp.y) - ptOffset;
			cv::circle( matTemp,ptTemp,2,cv::Scalar( 255, 255, 255),2,8);
		}

		#if IS_SON_DRAW
			imshow("matTemp",matTemp);
			cv::waitKey(0);
		#endif
		m_matImage = matTemp.clone();
	#endif
#endif


	// 角度匹配
	int nMatchSi = -1, nPreSonSi = -1, nNextSonSi = -1;		// 记录匹配位置
	float fMinDelAngle = 360.f;		// 记录夹角之差的最小绝对值
	int nEndPtNum = vecLinkEndPtnode.size();
	LinkEndPointNode node;
	double uMinError= -1.f;
	HAMapPoint hamMatchPrePt, hamMatchNextPt;	// 匹配点
	//vector<vector<int> > vecMatchPath;		// 记录匹配的路径
	//vector<vector<int> > vecMatchPathNodeId;		// 记录匹配路径上的Node Id
	vector<int> vecMatchPath;		// 记录匹配的路径
	vector<int> vecMatchPathNodeId;		// 记录匹配路径上的Node Id
	vector<int> vecOptionNodeId;		// 记录备选点集合
	double uMinBorderDis = -1;	// 记录对应边界点间最小距离
	double uMinCenterDis = -1;	// 记录匹配点到中心点的最小距离
	double uMinPathDis = -1;
	
	for (int i=0; i<nEndPtNum; i++)
	{
		node = vecLinkEndPtnode[i];
		int nNeighborNodeNum = node.vecNeighborNodeId.size();
		if (nNeighborNodeNum<=2)		// 邻居点限制
		{
			continue;
		}

		HAMapPoint hamEndPt = node.hamEndPoint;	

		// 区域限制
		if ((!isRectInside(hamEndPt,rtScreen))&&((abs(hamEndPt.x-haMainRoadCenterPt.x)>CENTER_COVER || 
			abs(hamEndPt.y-haMainRoadCenterPt.y)>CENTER_COVER)))
		{
			continue;
		}

		// 延伸link直到超出屏幕边界
		vector<HAMapPoint> vecBorderPt;
		vector<int> vecBorderPtDirection;
		vector<vector<int> > vecPathLinkId;
		vector<vector<int> > vecPathNodeId;

		/*nRet = extendLink(vecRoadNetLinks,vecLinkEndPtnode,	i, rtScreen, vecBorderPt, 
							vecBorderPtDirection, vecPathLinkId, vecPathNodeId);*/
		nRet = extendLink1(vecRoadNetLinks,vecLinkEndPtnode,	i, rtScreen, vecBorderPt, 
 			vecBorderPtDirection, vecPathLinkId, vecPathNodeId);
		if (nRet<0 || vecBorderPt.size()<=0)
		{
			continue;
		}
	
		// 求与主路中心点前后两向量最接近的两向量
		int nBorderPtNum = vecBorderPt.size();
		int nSi0=-1, nSi1=-1;
		double uDis0 = -1, uDis1 = -1;
		Vec2f v0, v1;
		vector<int> vecStartOptionPtId, vecEndOptionPtId;		// 记录边界起点、终点的候选集
		for (int j=0; j<2; j++)
		{
			Vec2f vMainRoad = vMainRoad1;	// 中心点之前的向量
			if (j>0)
			{				
				vMainRoad = vMainRoad2;	// 中心点之后的向量
			}

			float fMinAngle = 360.f;
			int nTempSi=0;
			Vec2f vTemp;

			for (int k=0; k<nBorderPtNum; k++)
			{
				if (j==0)
				{
					if (vecBorderPtDirection[k]==2)
					{
						continue;
					}
				}
				else
				{
					if (vecBorderPtDirection[k]==3)
					{
						continue;
					}
				}
				
				//LinkEndPointNode nextNode = vecLinkEndPtnode[node.vecNeighborNodeId[k]];
				//HAMapPoint hamPt = nextNode.hamEndPoint;
				HAMapPoint hamPt = vecBorderPt[k];
				Vec2f v(hamPt.x-hamEndPt.x,hamPt.y-hamEndPt.y);

				float fAngle = getAngle(vMainRoad, v);				
				if (fAngle<VECTOR_NEAR_ANGLE/*fMinAngle*/)
				{
					fMinAngle = fAngle;
					nTempSi = k;
					vTemp = v;

					if (j==0)
					{
						vecStartOptionPtId.push_back(nTempSi);
					} 
					else
					{
						vecEndOptionPtId.push_back(nTempSi);
					}
				}				
			}	// End k

		}	// End j
		
		if (vecStartOptionPtId.size()<=0 || vecEndOptionPtId.size()<=0)
		{
			continue;
		}

		// 关于候选路径，求路径起点、终点离主路起点、终点距离最近的路径
		double uMinOptionDis = 2*(rtScreen.width+rtScreen.height);				// 设定一个达不到的初值，便于循环比较
		for (int j=0; j<vecStartOptionPtId.size(); j++)
		{
			HAMapPoint hamStartOptionPt = vecBorderPt[vecStartOptionPtId[j]];
			Vec2f vStart(hamStartOptionPt.x-hamEndPt.x,hamStartOptionPt.y-hamEndPt.y);
			for (int k=0; k<vecEndOptionPtId.size(); k++)
			{
				HAMapPoint hamEndOptionPt = vecBorderPt[vecEndOptionPtId[k]];
				if (hamStartOptionPt==hamEndOptionPt)
				{
					continue;
				}

				Vec2f vEnd(hamEndOptionPt.x-hamEndPt.x,hamEndOptionPt.y-hamEndPt.y);
				float fAngle = getAngle(vStart,vEnd);
				float fDelAngle = abs(fMainAngle-fAngle);
				if (fDelAngle<ANGLE_ALLOWANCE)
				{
					// 求距离
					float fStartDis = getDistancePoint2Point(hamStartOptionPt.x,hamStartOptionPt.y,hamPrePt.x,hamPrePt.y);
					float fEndDis = getDistancePoint2Point(hamEndOptionPt.x,hamEndOptionPt.y,hamNextPt.x,hamNextPt.y);
					float fDis = fStartDis + fEndDis;
					
					if (fDis<uMinOptionDis)
					{
						uMinOptionDis = fDis;
						nSi0 = vecStartOptionPtId[j];
						nSi1 = vecEndOptionPtId[k];
						v0 = vStart;
						v1 = vEnd;
					}
				}

			}
		}

#if 0	// 利用最佳匹配角
		// 求夹角		
		float fAngle = getAngle(v0,v1);
		float fDelAngle = abs(fMainAngle-fAngle);
		if (fDelAngle<fMinDelAngle)
		{
			fMinDelAngle = fDelAngle;
			nMatchSi = i;
			hamMatchPrePt = vecBorderPt[nSi0];
			hamMatchNextPt = vecBorderPt[nSi1];

			vecMatchPath.clear();
			vecMatchPath.push_back(vecPathLinkId[nSi0]);
			vecMatchPath.push_back(vecPathLinkId[nSi1]);

			vecMatchPathNodeId.clear();
			vecMatchPathNodeId.push_back(vecPathNodeId[nSi0]);
			vecMatchPathNodeId.push_back(vecPathNodeId[nSi1]);
		}
#else	// 记录候选点，利用边界点间距离最短做判据
		float fAngle = getAngle(v0,v1);
		float fDelAngle = abs(fMainAngle-fAngle);
		if (fDelAngle<ANGLE_ALLOWANCE)
		{
			vecOptionNodeId.push_back(i);			

			// 匹配点与中心点距离
			double uCenterDis = sqrt((haMainRoadCenterPt.x-hamEndPt.x)*(haMainRoadCenterPt.x-hamEndPt.x)+
				(haMainRoadCenterPt.y-hamEndPt.y)*(haMainRoadCenterPt.y-hamEndPt.y));

			double uPathDis0 = -1, uPathDis1 = -1, uPathDis = -1;
			nRet = getDisBorder2MatchPt(vecRoadNetLinks, vecPathLinkId[nSi0], rtScreen,
										vecBorderPt[nSi0], hamEndPt, uPathDis0);
			if (nRet<0)
			{
				continue;
			}
			nRet = getDisBorder2MatchPt(vecRoadNetLinks, vecPathLinkId[nSi1], rtScreen,
										vecBorderPt[nSi0], hamEndPt, uPathDis1);
			if (nRet<0)
			{
				continue;
			}
			uPathDis = uPathDis0 + uPathDis1;


			// 边界点间距离
			double uDis = uDis0 + uDis1;
			bool bIsUpdate = false;
			if (uMinBorderDis<0)
			{
				bIsUpdate = true;
			} 
			else
			{
				if (/*uDis*/uMinOptionDis<uMinBorderDis)
				{
					bIsUpdate = true;
				}
				else if (abs(/*uDis*/uMinOptionDis-uMinBorderDis)<1e-10)		// 相等时，基于路径长度
				{
					if (uPathDis<uMinPathDis)
					{
						bIsUpdate = true;						
					}
					else if (abs(uPathDis-uMinPathDis)<1e-10)		// 相等时，基于路径长度
					{
						if (uCenterDis<uMinCenterDis)
						{
							bIsUpdate = true;
						}
					}
				}
			}	
			// 更新
			if (bIsUpdate)
			{
				uMinBorderDis = uMinOptionDis/*uDis*/;
				nMatchSi = i;

				hamMatchPrePt = vecBorderPt[nSi0];
				hamMatchNextPt = vecBorderPt[nSi1];


				vecMatchPath.clear();
				vecMatchPath = vecPathLinkId[nSi0];
				reverseOrder(vecMatchPath);		// 倒序重排，与主路方向一致				
				vecMatchPath.insert(vecMatchPath.end(),vecPathLinkId[nSi1].begin(),vecPathLinkId[nSi1].end());

				vecMatchPathNodeId.clear();
				vecMatchPathNodeId = vecPathNodeId[nSi0];
				reverseOrder(vecMatchPathNodeId);		// 倒序重排，与主路方向一致
				vecMatchPathNodeId.erase(vecMatchPathNodeId.end()-1);
				vecMatchPathNodeId.insert(vecMatchPathNodeId.end(),vecPathNodeId[nSi1].begin(),vecPathNodeId[nSi1].end());	

				uMinCenterDis = uCenterDis;
				uMinPathDis = uPathDis;
			}

		}

#endif

	}		// End i

	if (nMatchSi<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet - no Match Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet - no Match Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet - no Match Error!!==============\n");
	#endif
		return -1;
	}


	// 输出
	//nMatchSi = 29;
	hamCenterInNet = vecLinkEndPtnode[nMatchSi].hamEndPoint;
	

	// 由一个点构造路网	
	vector<int> vecRoadNetDirection2MainRoad;
	vector<vector<int> > vecCrossPathLinkID;		// 与主路相交的每条岔路的link Id
	vector<vector<int> > vecCrossPathNodeId;
 	int nMatchCenterSite = 0;
	if(!isBelongToVector(vecMatchPathNodeId,nMatchSi,nMatchCenterSite))
	{
		return -1;
	}
	
	nRet = formRoadNet3(vecRoadNetLinks,vecRoadNetLinkInfos,
						vecLinkEndPtnode,
						vecMatchPathNodeId,
						nMatchCenterSite,
						rtScreen,
						vecRoadNetDirection2MainRoad,
						vecCrossPathLinkID,
						vecCrossPathNodeId);
	if (nRet < 0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet - formRoadNet3 Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet - formRoadNet3 Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet - formRoadNet3 Error!!==============\n");
	#endif
		return -1;
	}

	// 过滤路网，不包括主路
	vector<vector<HAMapPoint> > vecAllCrossPathPt;
	nRet = filterRoadNet1(vecRoadNetLinks, vecLinkEndPtnode, nCrossRoadLen, vecCrossPathLinkID,
							vecCrossPathNodeId,	rtScreen, vecAllCrossPathPt);
 	if (nRet<0)
 	{
 	#ifdef _WINDOWS_VER_
 		printf("==============matchMainRoadCenterInNet - filterRoadNet1 Error!!==============\n");
 		m_strErrorLog = "matchMainRoadCenterInNet - filterRoadNet1 Error!!";
 	#else
 		LOGD("==============matchMainRoadCenterInNet - filterRoadNet Error!!==============\n");
 	#endif
 		return -1;
 	}
	vecCrossPathPt = vecAllCrossPathPt;

	// 添加匹配的主路点，及与岔路交点在主路中的下标
	vector<HAMapPoint> vecMatchPathPt;

	vecMatchPathPt.push_back(hamMatchPrePt);		// 第一个点
	for (int i=0; i<vecMatchPath.size(); i++)
	{
		int nLinkId = vecMatchPath[i];
		if (nLinkId<0)
		{
			continue;
		}

		// link两个端点对应的node Id
		int nPathStartNodeId = vecMatchPathNodeId[i];
		int nPathEndNodeId = vecMatchPathNodeId[i+1];

		vector<HAMapPoint> vecTemp = vecRoadNetLinks[nLinkId];
		// 判断方向
		if (vecTemp[0]==vecLinkEndPtnode[nPathEndNodeId].hamEndPoint)
		{
			reverseOrder(vecTemp);
		}

		int nTempNum = vecTemp.size();
		for (int j=0; j<nTempNum-1; j++)
		{
			HAMapPoint hamPt = vecTemp[j];			
			if (isRectInside(hamPt, rtScreen) && 
				hamPt!=vecMatchPathPt[vecMatchPathPt.size()-1])
			{
				// 求岔路与主路交点在主路中的位置
				if (j==0 && vecLinkEndPtnode[nPathStartNodeId].vecNeighborNodeId.size()>2)
				{
					vecCrossPointIndex.push_back(vecMatchPathPt.size());
				}

				vecMatchPathPt.push_back(hamPt);					
			}
		} // end j
	}	// end i

	vecMatchPathPt.push_back(hamMatchNextPt);	// 最后一个点


	// 求匹配点在集合中的位置
	nMatchCenterIndex = -1;
	for (int i=0; i<vecMatchPathPt.size(); i++)
	{
		if (hamCenterInNet==vecMatchPathPt[i])
		{
			nMatchCenterIndex = i;
			break;
		}
	}
	if (nMatchCenterIndex<0)
	{
		return -1;
	}

	//vecCrossGpsLinks.insert(vecCrossGpsLinks.begin(),vecMatchPathPt);		// 添加，主路作为第0个
	vecCrossPathPt.push_back(vecMatchPathPt);		// 添加，主路作为最后一个，方便平移

	// 计算从匹配路径移至主路的最佳平移距离
	vector<HAMapPoint> vecMainRoadVertex, vecMatchRoadVertex;
	vecMainRoadVertex.push_back(hamPrePt);
	vecMainRoadVertex.push_back(hamNextPt);
	vecMatchRoadVertex.push_back(hamMatchPrePt);
	vecMatchRoadVertex.push_back(hamMatchNextPt);
	HAMapPoint hamOffset;
	nRet = getOffset2MainRoad(vecMainRoadVertex, vecMatchRoadVertex, hamOffset);
	if (nRet<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet - getOffset2MainRoad Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet - getOffset2MainRoad Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet - getOffset2MainRoad Error!!==============\n");
	#endif
		return -1;
	}

	// 平移
	nRet = translateRoadNet(vecCrossPathPt, hamOffset);
	if (nRet<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============matchMainRoadCenterInNet - translateRoadNet Error!!==============\n");
		m_strErrorLog = "matchMainRoadCenterInNet - translateRoadNet Error!!";
	#else
		LOGD("==============matchMainRoadCenterInNet - translateRoadNet Error!!==============\n");
	#endif
		return -1;
	}

	// 获取平移后的主路匹配点
	vecMainRoadPtInNet = vecCrossPathPt[vecCrossPathPt.size()-1];
	vecCrossPathPt.erase(vecCrossPathPt.end()-1);		// 去掉最后一个vector元素，除去主路

	hamCenterInNet = vecMainRoadPtInNet[nMatchCenterIndex];

	// 画图
#ifdef _WINDOWS_VER_
	#if IS_DRAW2	// 绘图
		node = vecLinkEndPtnode[nMatchSi];
		cv::Point ptV = Point(node.hamEndPoint.x,node.hamEndPoint.y) - ptOffset;
		//cv::Point ptTemp0 = Point(node.vecNeighborPoint[nPreSonSi].x,node.vecNeighborPoint[nPreSonSi].y) - ptOffset;		
		//cv::Point ptTemp1 = Point(node.vecNeighborPoint[nNextSonSi].x,node.vecNeighborPoint[nNextSonSi].y) - ptOffset;
		cv::Point ptTemp0 = Point(hamMatchPrePt.x,hamMatchPrePt.y) - ptOffset;		
		cv::Point ptTemp1 = Point(hamMatchNextPt.x,hamMatchNextPt.y) - ptOffset;
		cv::circle( matTemp,ptV,1,cv::Scalar( 255, 0, 255),4,8);
		cv::circle( matTemp,ptTemp0,1,cv::Scalar( 0, 255, 255),3,8);
		cv::circle( matTemp,ptTemp1,1,cv::Scalar( 0, 255, 255),3,8);
		Scalar color(100,100,0);
		cv::line(matTemp,ptTemp0,ptV,color,1);	
		cv::line(matTemp,ptV,ptTemp1,color,1);

		// 绘制匹配路径
		for (int i=0; i<vecMatchPath.size(); i++)
		{
			int nLinkId = vecMatchPath[i];
			if (nLinkId>=0)
			{
				drawImage(matTemp, haMainRoadCenterPt, vecRoadNetLinks[nLinkId],cv::Scalar(255,255,0),2);
			}		
		}

		// 基于主路绘制路网
		for (int i=0; i<nNumLink; i++)
		{
			if (vecRoadNetDirection2MainRoad[i]!=OPPOSITE_DIRECTION)
			{
				drawImage(matTemp, haMainRoadCenterPt, vecRoadNetLinks[i],cv::Scalar(0,255,255),1);
			}
		}

		// 绘制提取到的岔路
		for (int i=0; i<vecAllCrossPathPt.size(); i++)
		{
			drawImage(matTemp, haMainRoadCenterPt, vecAllCrossPathPt[i],cv::Scalar(0,255,0),1);
		}


		// 绘制主路与岔路的交点
		for (int i=0; i<vecCrossPointIndex.size(); i++)
		{
			cv::Point ptTemp = Point(vecMatchPathPt[vecCrossPointIndex[i]].x,vecMatchPathPt[vecCrossPointIndex[i]].y) - ptOffset;		
			cv::circle( matTemp,ptTemp,1,cv::Scalar( 0, 0, 255),5,8);
		}

		m_matImage = matTemp.clone();	

		#if IS_SON_DRAW
			cv::imshow("m_matImage",m_matImage);
			cv::waitKey(0);
		#endif
	#endif
#endif

	return 0;
}


// 判断元素是否属于集合
//template<typename T>
//bool MergeMapData::isEelmentBelongToSet(vector<T>& vecElement, T element, int& nSite)
//{
//	bool bRes = false;	
//	vector<T>::iterator iter = std::find(vecElement.begin(),vecElement.end(), element);	//返回的是一个迭代器指针
//	if (iter==vecElement.end())	// 不存在
//	{
//		return false;
//	}
//	else
//	{
//		nSite = std::distance(vecElement.begin(), iter);
//		return true;
//	}
//
//}

// vector倒序重排
template<typename T>
void MergeMapData::reverseOrder(vector<T>& vecSet)
{
	int nNum = vecSet.size();

	for (int i=0; i<nNum/2; i++)
	{
		T tElementTemp = vecSet[i];
		vecSet[i] = vecSet[nNum-i-1];
		vecSet[nNum-i-1] = tElementTemp;
	}
}


// 平移路网
int MergeMapData::translateRoadNet(std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
									HAMapPoint hamOffset)
{
	// 参数自检
	int nNum = vecRoadNetLinks.size();
	if (nNum<=0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============translateRoadNet - parameter Error!!==============\n");
	#else
		LOGD("==============translateRoadNet - parameter Error!!==============\n");
	#endif
		return -1;
	}
	
	HAMapPoint* pHamPt;
	for (int i=0; i<nNum; i++)
	{		
		int nNumPt = vecRoadNetLinks[i].size();
		for (int j=0; j<nNumPt; j++)
		{
			pHamPt = &vecRoadNetLinks[i][j];
			pHamPt->x += hamOffset.x;
			pHamPt->y += hamOffset.y;
		}
	}

	return 0;
}

// 计算从匹配路径移至主路的最佳平移距离(包括正负)
int MergeMapData::getOffset2MainRoad(const vector<HAMapPoint>& vecMainRoadVertex,
									 const vector<HAMapPoint>& vecMatchRoadVertex,
									 HAMapPoint& hamOffset)
{
	// 参数自检
	int nNum = vecMainRoadVertex.size();
	if (nNum<=0 || nNum!=vecMatchRoadVertex.size())
	{
	#ifdef _WINDOWS_VER_
		printf("==============getOffset2MainRoad - parameter Error!!==============\n");
	#else
		LOGD("==============getOffset2MainRoad - parameter Error!!==============\n");
	#endif
		return -1;
	}

	// 计算两个中心点
	HAMapPoint hamMainRoadCenter = {vecMainRoadVertex[0].x, vecMainRoadVertex[0].y};
	HAMapPoint hamMatchRoadCenter = {vecMatchRoadVertex[0].x, vecMatchRoadVertex[0].y};		// 记录两个中心点
	for (int i=1; i<nNum; i++)
	{
		hamMainRoadCenter.x += vecMainRoadVertex[i].x;
		hamMainRoadCenter.y += vecMainRoadVertex[i].y;
		hamMatchRoadCenter.x += vecMatchRoadVertex[i].x;
		hamMatchRoadCenter.y += vecMatchRoadVertex[i].y;
	}
	hamMainRoadCenter.x /= nNum;
	hamMainRoadCenter.y /= nNum;
	hamMatchRoadCenter.x /= nNum;
	hamMatchRoadCenter.y /= nNum;

	// 计算平移量，将中心点移至重合，即为平移距离
	hamOffset.x = hamMainRoadCenter.x - hamMatchRoadCenter.x;
	hamOffset.y = hamMainRoadCenter.y - hamMatchRoadCenter.y;

	return 0;
}

// 过滤路网，不包括主路
int MergeMapData::filterRoadNet(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
								const vector<int>& vecRoadNetDirection2MainRoad,
								const vector<int>& vecMainRoadLinkId,
								std::vector<std::vector<HAMapPoint> >& vecCrossGpsLinks)
{
	// 参数自检
	int nLinkNum = vecRoadNetLinks.size();
	if (nLinkNum<=0 || nLinkNum!=vecRoadNetDirection2MainRoad.size())
	{
	#ifdef _WINDOWS_VER_
		printf("==============filterRoadNet - parameter Error!!==============\n");
	#else
		LOGD("==============filterRoadNet - parameter Error!!==============\n");
	#endif
		return -1;
	}

	vecCrossGpsLinks.clear();
	vector<int> vecMainLinkId = vecMainRoadLinkId;
	for (int i=0; i<nLinkNum; i++)
	{
		// 判断是否是主路Link		
		vector<int>::iterator iter = std::find(vecMainLinkId.begin(),vecMainLinkId.end(),i);	//返回的是一个迭代器指针
				
		if ((iter==vecMainLinkId.end())&&(vecRoadNetDirection2MainRoad[i]!=OPPOSITE_DIRECTION))
		{
			vecCrossGpsLinks.push_back(vecRoadNetLinks[i]);
		}
	}

	return 0;
}

// 过滤路网，不包括主路
int MergeMapData::filterRoadNet1(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
								const vector<LinkEndPointNode> vecAllEndPtnode,
								int nCrossRoadLen,		// 岔路长度
								const vector<vector<int> >& vecCrossPathLinkID,		// 与主路相交的每条岔路的link Id
								const vector<vector<int> >& vecCrossPathNodeId,
								cv::Rect rtScreen,
								std::vector<std::vector<HAMapPoint> >& vecCrossPathPt)
{
	// 参数自检
	int nLinkNum = vecRoadNetLinks.size();
	int nCrossPathNum = vecCrossPathLinkID.size();
	int nAllNodeNum = vecAllEndPtnode.size();
	if (nLinkNum<=0 || nCrossPathNum>nLinkNum ||
		nCrossPathNum!=vecCrossPathNodeId.size() ||	nAllNodeNum<0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============filterRoadNet1 - parameter Error!!==============\n");
	#else
		LOGD("==============filterRoadNet1 - parameter Error!!==============\n");
	#endif
		return -1;
	}

	int nRet = 0;

	// 获取每条路径的点及按主路方向排好序
	vector<int> vecSinglePathLinkId, vecSinglePathNodeId;
	vector<HAMapPoint> vecSinglePathPt;
	for (int i=0; i<nCrossPathNum; i++)
	{
		vecSinglePathLinkId = vecCrossPathLinkID[i];
		vecSinglePathNodeId = vecCrossPathNodeId[i];
		int nPathLinkNum = vecSinglePathLinkId.size();
		float fDis = 0;
		vecSinglePathPt.clear();
		for (int j=0; j<nPathLinkNum; j++)
		{
			int nLinkId = vecSinglePathLinkId[j];
			int nNodeId = vecSinglePathNodeId[j];
			vector<HAMapPoint> vecLinkPt = vecRoadNetLinks[nLinkId];
			int nPtNum = vecLinkPt.size();
			HAMapPoint hamNodePt = vecAllEndPtnode[nNodeId].hamEndPoint;
			if (vecLinkPt[0]==hamNodePt)
			{
				// 逆序重排
				reverseOrder(vecLinkPt);		// 倒序重排，与主路方向一致
			}
			HAMapPoint hamPrePt = vecLinkPt[0];
			HAMapPoint hamCurPt;
			vecSinglePathPt.push_back(hamPrePt);
			if (!isRectInside(hamPrePt,rtScreen))
			{
				break;
			}
			bool bIsBreak = false;
			for (int k=1; k<nPtNum; k++)
			{
				float fTempDis = 0.f;
				hamCurPt = vecLinkPt[k];
				if (!isRectInside(hamCurPt,rtScreen))
				{
					// 计算与窗口的交点
					HAMapPoint hamCrossPt;
					std::vector<HAMapPoint> vecTemp;
					vecTemp.push_back(vecLinkPt[k-1]);
					vecTemp.push_back(hamCurPt);
					nRet = getCrossPointLink2Rect(vecTemp, rtScreen, hamCrossPt);
					if (nRet<0)
					{
						bIsBreak = true;
						break;
					}
					fTempDis = getDistancePoint2Point(vecLinkPt[k-1].x,vecLinkPt[k-1].y,hamCrossPt.x,hamCrossPt.y);
					fDis += fTempDis;

					if (fDis>=nCrossRoadLen)
					{
						// 控制岔路长度严格等于nCrossRoadLen						
						float fDel = fDis - nCrossRoadLen;		// 多余的长度
						fDel = fTempDis - fDel;			// 需要的长度
						float fX = vecLinkPt[k-1].x + fDel*(vecLinkPt[k].x-vecLinkPt[k-1].x)/fTempDis;
						float fY = vecLinkPt[k-1].y + fDel*(vecLinkPt[k].y-vecLinkPt[k-1].y)/fTempDis;					
						HAMapPoint hamRailPt = {fX,fY};
						vecSinglePathPt.push_back(hamRailPt);

						break;
					}
					else
					{
						vecSinglePathPt.push_back(hamCrossPt);
					}
					
					bIsBreak = true;
					break;
				}
				fTempDis = getDistancePoint2Point(vecLinkPt[k-1].x,vecLinkPt[k-1].y,vecLinkPt[k].x,vecLinkPt[k].y);	
				fDis += fTempDis;				
				if (fDis>CROSSROAD_LENGTH/*nCrossRoadLen*/)
				{					
					bIsBreak = true;					
				}
								
				if (fDis>=nCrossRoadLen)
				{
					// 控制岔路长度严格等于nCrossRoadLen
					float fDel = fDis - nCrossRoadLen;		// 多余的长度
					fDel = fTempDis - fDel;			// 需要的长度
					float fX = vecLinkPt[k-1].x + fDel*(vecLinkPt[k].x-vecLinkPt[k-1].x)/fTempDis;
					float fY = vecLinkPt[k-1].y + fDel*(vecLinkPt[k].y-vecLinkPt[k-1].y)/fTempDis;					
					HAMapPoint hamRailPt = {fX,fY};
					vecSinglePathPt.push_back(hamRailPt);
					break;
				}
				else
				{
					vecSinglePathPt.push_back(hamCurPt);
				}
			}	// end k

			if (!isRectInside(hamCurPt,rtScreen) || (bIsBreak && fDis>nCrossRoadLen))
			{
				break;
			}

		}	// end j
		if (fDis>CROSSROAD_LENGTH/*nCrossRoadLen*/)
		{
			vecCrossPathPt.push_back(vecSinglePathPt);
		}
	} // end i
	
	return 0;
}



// 由一个点构造路网
/*
	vecRoadNetDirection2MainRoad - 相对主路的方向，由主路可到达-0、1、2，不能到达-3
*/
int MergeMapData::formRoadNet(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
							  const vector<LinkEndPointNode> vecAllEndPtnode,							  
							  const vector<int>& vecMainRoadNodeId,
							  cv::Rect rtScreen,
							  vector<int>& vecRoadNetDirection2MainRoad)
{
	// 参数自检	
	int nNumLink = vecRoadNetLinks.size();	
	int nNodeNum = vecAllEndPtnode.size();
	int nMainNodeNum = vecMainRoadNodeId.size();
	if (nNumLink<=0 || nNodeNum<=0 || nMainNodeNum<=0 || nNodeNum<nMainNodeNum)
	{
	#ifdef _WINDOWS_VER_
		printf("==============formRoadNet - parameter Error!!==============\n");
	#else
		LOGD("==============formRoadNet - parameter Error!!==============\n");
	#endif
		
		return -1;
	}

	int nRet = 0;

	// 方向
	vecRoadNetDirection2MainRoad = vector<int>(nNumLink,3);		// 赋初值，都定义为不能到达

	// 基于主路的每个Node向外拓展
	vector<int> vecLoopNodeId;		// 表示栈，记录node的Id，用于循环取舍值
	LinkEndPointNode curNode;	
	for (int i=0; i<nMainNodeNum; i++)
	{
		int nNodeId = vecMainRoadNodeId[i];
		curNode = vecAllEndPtnode[nNodeId];

		// 判断是否重复
		vector<int>::iterator iter = std::find(vecLoopNodeId.begin(),
												vecLoopNodeId.end(),nNodeId);	//返回的是一个迭代器指针
		if (iter!=vecLoopNodeId.end())	// 重复
		{
			continue;
		}

		// 判断点是否在屏幕范围内
		if (isRectInside(curNode.hamEndPoint, rtScreen))
		{
			vecLoopNodeId.push_back(nNodeId);
		}		
	}

	// 利用栈遍历
	vector<int> vecIsLinkDo(nNumLink,0);		// 标识link是否已处理，0-未处理，1-已处理	
	while (vecLoopNodeId.size()>0)
	{
		// 当前node		
		int nCurNodeId = vecLoopNodeId[vecLoopNodeId.size()-1];
		curNode = vecAllEndPtnode[nCurNodeId];

		// 出栈
		vecLoopNodeId.erase(vecLoopNodeId.end()-1);

		// 判断点是否在屏幕范围内
		if (!isRectInside(curNode.hamEndPoint, rtScreen))
		{
			continue;
		}
		
		// 进栈
		int nNeighborNum = curNode.vecNeighborNodeId.size();
		for (int i=0; i<nNeighborNum; i++)
		{
			int nDirection = curNode.vecDirection[i];
			int nLinkId = curNode.vecLinkId[i];
			if (vecIsLinkDo[nLinkId]==0 && nDirection!=OPPOSITE_DIRECTION)
			{
				vecLoopNodeId.push_back(curNode.vecNeighborNodeId[i]);				
				vecRoadNetDirection2MainRoad[nLinkId] = nDirection;		

				vecIsLinkDo[nLinkId] = 1;		// 标识为已处理
			}			
		}
	}
	
	//// 去除直接指向主路的岔路，即方向标记为3
	//for (int i=0; i<nMainNodeNum; i++)
	//{
	//	int nNodeId = vecMainRoadNodeId[i];
	//	curNode = vecAllEndPtnode[nNodeId];
	//	int nNeighborNum = curNode.vecNeighborNodeId.size();
	//	for (int j=0; j<nNeighborNum; j++)
	//	{
	//		int nLinkId = curNode.vecLinkId[j];
	//		if (curNode.vecDirection[j]==OPPOSITE_DIRECTION)
	//		{
	//			vecRoadNetDirection2MainRoad[nLinkId] = OPPOSITE_DIRECTION;
	//		}
	//	}
	//}

	return 0;
}

int MergeMapData::formRoadNet1(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
								const vector<LinkInfo>& vecLinkInfos,
								const vector<LinkEndPointNode> vecAllEndPtnode,							  
								const vector<int>& vecMainRoadNodeId,
								cv::Rect rtScreen,
								vector<int>& vecRoadNetDirection2MainRoad)
{
	// 参数自检	
	int nNumLink = vecRoadNetLinks.size();	
	int nNodeNum = vecAllEndPtnode.size();
	int nMainNodeNum = vecMainRoadNodeId.size();
	if (nNumLink<=0 || nNodeNum<=0 || nMainNodeNum<=0 || 
		nNodeNum<nMainNodeNum || nNumLink!=vecLinkInfos.size())
	{
	#ifdef _WINDOWS_VER_
		printf("==============formRoadNet1 - parameter Error!!==============\n");
	#else
		LOGD("==============formRoadNet1 - parameter Error!!==============\n");
	#endif
		return -1;
	}

#ifdef _WINDOWS_VER_
	for (int i=0; i<vecLinkInfos.size(); i++)
	{
		printf("vecLinkInfos：size = %d, i = %d, routeId = %.4f\n",vecLinkInfos.size(),i,vecLinkInfos[i].routeId);
	}
	
#endif

	int nRet = 0;

	// 方向
	vecRoadNetDirection2MainRoad = vector<int>(nNumLink,OPPOSITE_DIRECTION);		// 赋初值，都定义为不能到达

	//// 基于主路的每个Node向外拓展
	//vector<int> vecLoopNodeId;		// 表示栈，记录node的Id，用于循环取舍值
	//LinkEndPointNode curNode;	
	//for (int i=0; i<nMainNodeNum; i++)
	//{
	//	int nNodeId = vecMainRoadNodeId[i];
	//	curNode = vecAllEndPtnode[nNodeId];

	//	// 判断是否重复
	//	vector<int>::iterator iter = std::find(vecLoopNodeId.begin(),
	//		vecLoopNodeId.end(),nNodeId);	//返回的是一个迭代器指针
	//	if (iter!=vecLoopNodeId.end())	// 重复
	//	{
	//		continue;
	//	}

	//	// 判断点是否在屏幕范围内
	//	if (IsRectInside(curNode.hamEndPoint, rtScreen))
	//	{
	//		vecLoopNodeId.push_back(nNodeId);
	//	}		
	//}

	//// 利用栈遍历
	//vector<int> vecIsLinkDo(nNumLink,0);		// 标识link是否已处理，0-未处理，1-已处理	
	//while (vecLoopNodeId.size()>0)
	//{
	//	// 当前node		
	//	int nCurNodeId = vecLoopNodeId[vecLoopNodeId.size()-1];
	//	curNode = vecAllEndPtnode[nCurNodeId];

	//	// 出栈
	//	vecLoopNodeId.erase(vecLoopNodeId.end()-1);

	//	// 判断点是否在屏幕范围内
	//	if (!IsRectInside(curNode.hamEndPoint, rtScreen))
	//	{
	//		continue;
	//	}

	//	// 进栈
	//	int nNeighborNum = curNode.vecNeighborNodeId.size();
	//	for (int i=0; i<nNeighborNum; i++)
	//	{
	//		int nDirection = curNode.vecDirection[i];
	//		int nLinkId = curNode.vecLinkId[i];
	//		if (vecIsLinkDo[nLinkId]==0 && nDirection!=OPPOSITE_DIRECTION)
	//		{
	//			vecLoopNodeId.push_back(curNode.vecNeighborNodeId[i]);				
	//			vecRoadNetDirection2MainRoad[nLinkId] = nDirection;		

	//			vecIsLinkDo[nLinkId] = 1;		// 标识为已处理
	//		}			
	//	}
	//}

	// ============
	// 获取主路LinkId
	vector<int> vecMainRoadLinkId;			
	nRet = getPathLinkId(vecAllEndPtnode, vecMainRoadNodeId, vecMainRoadLinkId);
	if (nRet<0)
	{
		return -1;
	}

	// 

	//// 去除与主路同向，但不同路的道路（根据道路名称判断）
	//for (int i=0; i<nMainNodeNum-1; i++)	// 注意nMainNodeNum-1，保持与vecMainRoadLinkId的大小一致
	//{
	//	int nLinkId = vecMainRoadLinkId[i];
	//	LinkInfo linkInfo = vecLinkInfos[nLinkId];
	//}


	//// 保留与主路同路，但方向相反的道路
	//for (int i=0; i<nMainNodeNum; i++)
	//{
	//	int nNodeId = vecMainRoadNodeId[i];
	//	curNode = vecAllEndPtnode[nNodeId];
	//	int nNeighborNum = curNode.vecNeighborNodeId.size();
	//}

	// 保留窗口内与主路同名且同路的路径，方向不管	
	vector<int> vecExtendLinksId, vecExtendNodesId;		// 延伸的link Id、node Id
	for (int i=0; i<nMainNodeNum-1; i++)
	{
		int nMainLinkId = vecMainRoadLinkId[i];
		int nMainNodeId = vecMainRoadNodeId[i];

		#ifdef _WINDOWS_VER_
			printf("\n=============== i=%d, nMainLinkId=%d, nMainNodeId=%d ================\n",i,nMainLinkId,nMainNodeId);
		#endif

		// 延伸同名路
		nRet = extendSameNameRoad(vecLinkInfos,	vecAllEndPtnode,vecMainRoadLinkId,
							nMainLinkId, nMainNodeId, rtScreen, 
							vecExtendLinksId, vecExtendNodesId);
		if (nRet<0)
		{
			continue;
		}

		
	}// end i

	// 去除与主路同名，但不同路的路径

	// ===============

	// 标识要保留的link	
	#ifdef _WINDOWS_VER_
		#if IS_DRAW2
			Mat matTemp = m_matImage.clone();			
		#endif
	#endif
	for (int i=0; i<vecMainRoadLinkId.size(); i++)
	{
		int nLinkId = vecMainRoadLinkId[i];
		vecRoadNetDirection2MainRoad[nLinkId] = SAME_DIRECTION;

	#ifdef _WINDOWS_VER_
		#if IS_DRAW2			
			drawImage(matTemp, m_hamMainRoadCenter, vecRoadNetLinks[nLinkId],cv::Scalar(255,255,0),2);
		#endif
	#endif
	}

	for (int i=0; i<vecExtendLinksId.size(); i++)
	{
		int nLinkId = vecExtendLinksId[i];
		vecRoadNetDirection2MainRoad[nLinkId] = SAME_DIRECTION;

	#ifdef _WINDOWS_VER_
		#if IS_DRAW2			
			drawImage(matTemp, m_hamMainRoadCenter, vecRoadNetLinks[nLinkId],cv::Scalar(0,255,0),1);
			/*imshow("matTemp",matTemp);
			waitKey(0);*/
		#endif
	#endif

	}

	return 0;
}


// 过滤link
int MergeMapData::formRoadNet2(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
							   const vector<LinkInfo>& vecLinkInfos,
							   const vector<LinkEndPointNode> vecAllEndPtnode,							  
							   const vector<int>& vecMainRoadNodeId,
							   int nMatchCenterSite,
							   cv::Rect rtScreen,
							   vector<int>& vecRoadNetDirection2MainRoad)
{
	// 参数自检
	int nLinkNum = vecRoadNetLinks.size();
	int nNodeNum = vecAllEndPtnode.size();
	int nMainRoadNodeNum = vecMainRoadNodeId.size();
	if (nLinkNum<=0 || nLinkNum!=vecLinkInfos.size() || nNodeNum<=0 ||
		nMainRoadNodeNum<=0 || nMainRoadNodeNum>nNodeNum ||
		nMatchCenterSite<0 || nMatchCenterSite>nMainRoadNodeNum-1)
	{
		return -1;
	}

	// 针对主路，基于角度，确定关键点
	float fMainAngel = 0.f;
	float fMainCosV = 2;
	int nKeySi = 0;
	bool bIsHaveKeyPt = false;		// 记录是否有关键点
	for (int i=1; i<nMainRoadNodeNum-1; i++)
	{		
		HAMapPoint hamPre = vecAllEndPtnode[vecMainRoadNodeId[i-1]].hamEndPoint;
		HAMapPoint hamCur = vecAllEndPtnode[vecMainRoadNodeId[i]].hamEndPoint;
		HAMapPoint hamNext = vecAllEndPtnode[vecMainRoadNodeId[i+1]].hamEndPoint;
		if (isRectInside(hamCur,rtScreen))
		{
			Vec2i v1 = Point(hamPre.x,hamPre.y) - Point(hamCur.x,hamCur.y);
			Vec2i v2 = Point(hamNext.x,hamNext.y) - Point(hamCur.x,hamCur.y);
			float fAngelTemp = 0.f;
			float fCosVTemp = v1.dot(v2);
			fCosVTemp = fCosVTemp/(getDistancePoint2Point(v1[0],v1[1],0,0)*getDistancePoint2Point(v2[0],v2[1],0,0));
			if (abs(fCosVTemp)<fMainCosV)
			{
				fMainCosV = abs(fCosVTemp);
				nKeySi = i;
				fMainAngel = acosf(fCosVTemp);
			}
		}
	}

	float fCosThV = KEYPOINT_COSV_TH; 
	if (nKeySi!=0 && fMainCosV>fCosThV)
	{
		nMatchCenterSite = nKeySi;
		bIsHaveKeyPt = true;
	}

	// 赋初值
	vecRoadNetDirection2MainRoad = vector<int>(nLinkNum,OPPOSITE_DIRECTION);		// 需要保留的边，之后赋值为SAME_DIRECTION

#ifdef _WINDOWS_VER_
#if IS_DRAW_ROADNET
	// 基于主路绘制路网
	Mat matTemp = m_matImage.clone();
	cv::Point ptTemp = Point(vecAllEndPtnode[vecMainRoadNodeId[nMatchCenterSite]].hamEndPoint.x,
		vecAllEndPtnode[vecMainRoadNodeId[nMatchCenterSite]].hamEndPoint.y)	- m_ptOffset;		
	cv::circle( matTemp,ptTemp,1,cv::Scalar( 0, 0, 255),5,8);
	for (int i=0; i<nLinkNum; i++)
	{
		if (vecRoadNetDirection2MainRoad[i]!=OPPOSITE_DIRECTION)
		{
			drawImage(matTemp, m_hamMainRoadCenter, vecRoadNetLinks[i],cv::Scalar(0,255,0),1);
		}
	}
	cv::imshow("matTemp",matTemp);
	cv::waitKey(0);

#endif
#endif

	int nRet = 0;

	// 获取主路LinkId
	vector<int> vecMainRoadLinkId;			
	nRet = getPathLinkId(vecAllEndPtnode, vecMainRoadNodeId, vecMainRoadLinkId);
	if (nRet<0)
	{
		return -1;
	}

	// 记录延伸的link、node Id，及link上的方向
	vector<int> vecExtendLinksId, vecExtendNodesId, vecExtendLinkDirection;

	// 确定关键点有关的向量，用于取舍邻居
	int nCenterNodeId = vecMainRoadNodeId[nMatchCenterSite];		// 中心点Node Id
	LinkEndPointNode centerNode = vecAllEndPtnode[nCenterNodeId];	// 中心点对应的Node
	Point ptCenter = Point(centerNode.hamEndPoint.x, centerNode.hamEndPoint.y);	
	int nNeighborNum = centerNode.vecNeighborNodeId.size();
	vector<Vec2i> vecVef(nNeighborNum);	// 记录方向向量
	for (int i=0; i<nNeighborNum; i++)
	{
		int nNextLinkId = centerNode.vecLinkId[i];
		int nNextNodeId = centerNode.vecNeighborNodeId[i];
		Point ptNext = Point(centerNode.vecNeighborPoint[i].x, centerNode.vecNeighborPoint[i].y);
		vecVef[i] = ptNext - ptCenter;

		vecRoadNetDirection2MainRoad[nNextLinkId] = SAME_DIRECTION;		// 保留

		// 延伸
		int nSi = 0;
		if (!isBelongToVector(vecExtendLinksId,nNextLinkId,nSi) && !isBelongToVector(vecMainRoadLinkId,nNextLinkId,nSi))
		{
			// 延伸道路，nCurNodeId表示Link的尾巴Node
			vecExtendLinksId.push_back(nNextLinkId);
			vecExtendNodesId.push_back(nNextNodeId);
			int nDirection = centerNode.vecDirection[i];
			int nLinkId = centerNode.vecLinkId[i];
			int nNodeId = centerNode.vecNeighborNodeId[i];
			nRet = extendRoad(vecRoadNetLinks, vecLinkInfos, vecAllEndPtnode, vecMainRoadLinkId, 
				nDirection,	nLinkId, nNodeId, rtScreen,	
				vecExtendLinksId, vecExtendNodesId, vecExtendLinkDirection);
			if (nRet<0)
			{
				continue;
			}
		}


#ifdef _WINDOWS_VER_
#if IS_DRAW_ROADNET
		// 基于主路绘制路网
		drawImage(matTemp, m_hamMainRoadCenter, vecRoadNetLinks[nNextLinkId],cv::Scalar(0,255,0),1);
		cv::imshow("matTemp",matTemp);
		cv::waitKey(0);
#endif
#endif
	}

	float fDelAngel = VECTOR_PARALLEL_ANGLE;
	fDelAngel = fDelAngel*CV_PI/180;


	// 处理关键点及前后邻居，关键点处，保留主路正、反延长线，邻居点删除主路平行线；若存在关键点到主路平行线的link，也删除



	// 处理正常点，保留正方向
	for (int i=0; i<nMainRoadNodeNum; i++)
	{
		int nCurMainNodeId = vecMainRoadNodeId[i];
		LinkEndPointNode curMainNode = vecAllEndPtnode[nCurMainNodeId];	

		if (!isRectInside(curMainNode.hamEndPoint,rtScreen))
		{
			continue;
		}
		int nNeighborNum = curMainNode.vecNeighborNodeId.size();
		if (i!=nMatchCenterSite && i!=nMatchCenterSite-1 && i!=nMatchCenterSite+1)
		{		
			for (int j=0; j<nNeighborNum; j++)
			{
				if (curMainNode.vecDirection[j]!=OPPOSITE_DIRECTION)
				{
					int nLinkId = curMainNode.vecLinkId[j];
					int nNodeId = curMainNode.vecNeighborNodeId[j];
					vecRoadNetDirection2MainRoad[nLinkId] = SAME_DIRECTION;

					// 延伸
					int nSi = 0;
					if (!isBelongToVector(vecExtendLinksId,nLinkId,nSi)&&!isBelongToVector(vecMainRoadLinkId,nLinkId,nSi))
					{
						// 延伸道路，nCurNodeId表示Link的尾巴Node
						vecExtendLinksId.push_back(nLinkId);
						vecExtendNodesId.push_back(nNodeId);
						int nDirectionTemp = curMainNode.vecDirection[j];
						int nLinkIdTemp = curMainNode.vecLinkId[j];
						int nNodeIdTemp = curMainNode.vecNeighborNodeId[j];
						nRet = extendRoad(vecRoadNetLinks, vecLinkInfos, vecAllEndPtnode, vecMainRoadLinkId, 
							nDirectionTemp,	nLinkIdTemp, nNodeIdTemp, rtScreen,	
							vecExtendLinksId, vecExtendNodesId,vecExtendLinkDirection);
						if (nRet<0)
						{
							continue;
						}
					}

#ifdef _WINDOWS_VER_
#if IS_DRAW_ROADNET
					// 基于主路绘制路网
					drawImage(matTemp, m_hamMainRoadCenter, vecRoadNetLinks[nLinkId],cv::Scalar(0,255,0),1);
					cv::imshow("matTemp",matTemp);
					cv::waitKey(0);
#endif
#endif
				}
			}
		}

		// 相邻点，另外处理
		if (bIsHaveKeyPt && (i==nMatchCenterSite-1 || i==nMatchCenterSite+1))
		{
			Point ptCurMainPt = Point(curMainNode.hamEndPoint.x, curMainNode.hamEndPoint.y);
			for (int j=0; j<nNeighborNum; j++)
			{				
				if (curMainNode.vecDirection[j]!=OPPOSITE_DIRECTION)
				{
					int nNodeId = curMainNode.vecNeighborNodeId[j];
					int nLinkId = curMainNode.vecLinkId[j];
					int nSi = 0;
					if (isBelongToVector(vecMainRoadNodeId,nNodeId,nSi))
					{
						vecRoadNetDirection2MainRoad[nLinkId] = SAME_DIRECTION;

						// 延伸
						int nSi = 0;
						if (!isBelongToVector(vecExtendLinksId,nLinkId,nSi)&&!isBelongToVector(vecMainRoadLinkId,nLinkId,nSi))
						{
							// 延伸道路，nCurNodeId表示Link的尾巴Node
							vecExtendLinksId.push_back(nLinkId);
							vecExtendNodesId.push_back(nNodeId);
							int nDirectionTemp = curMainNode.vecDirection[j];
							int nLinkIdTemp = curMainNode.vecLinkId[j];
							int nNodeIdTemp = curMainNode.vecNeighborNodeId[j];
							nRet = extendRoad(vecRoadNetLinks, vecLinkInfos, vecAllEndPtnode, vecMainRoadLinkId, 
								nDirectionTemp,	nLinkIdTemp, nNodeIdTemp, rtScreen,
								vecExtendLinksId, vecExtendNodesId, vecExtendLinkDirection);
							if (nRet<0)
							{
								continue;
							}
						}

						continue;
					}					

					Point ptTemp = Point(curMainNode.vecNeighborPoint[j].x,curMainNode.vecNeighborPoint[j].y);
					Vec2i vTemp = ptTemp - ptCurMainPt;
					int nMinSi = -1;
					float fCosV = -2;
					for (int k=0; k<vecVef.size(); k++)
					{
						float fCosTemp = vTemp.dot(vecVef[k]);
						fCosTemp = fCosTemp/(getDistancePoint2Point(vTemp[0],vTemp[1],0,0)*
							getDistancePoint2Point(vecVef[k][0],vecVef[k][1],0,0));
						if (abs(fCosTemp)>fCosV)
						{
							fCosV = abs(fCosTemp);
							nMinSi = j;
						}
					}
					if (fCosV<cos(fDelAngel))
					{
						vecRoadNetDirection2MainRoad[nLinkId] = SAME_DIRECTION;

						// 延伸
						int nSi = 0;
						if (!isBelongToVector(vecExtendLinksId,nLinkId,nSi) && !isBelongToVector(vecMainRoadLinkId,nLinkId,nSi))
						{
							// 延伸道路，nCurNodeId表示Link的尾巴Node
							vecExtendLinksId.push_back(nLinkId);
							vecExtendNodesId.push_back(nNodeId);
							int nDirectionTemp = curMainNode.vecDirection[j];
							int nLinkIdTemp = curMainNode.vecLinkId[j];
							int nNodeIdTemp = curMainNode.vecNeighborNodeId[j];
							nRet = extendRoad(vecRoadNetLinks, vecLinkInfos, vecAllEndPtnode, vecMainRoadLinkId, 
								nDirectionTemp,	nLinkIdTemp, nNodeIdTemp, rtScreen,	
								vecExtendLinksId, vecExtendNodesId,vecExtendLinkDirection);
							if (nRet<0)
							{
								continue;
							}
						}

#ifdef _WINDOWS_VER_
#if IS_DRAW_ROADNET
						// 基于主路绘制路网
						drawImage(matTemp, m_hamMainRoadCenter, vecRoadNetLinks[nLinkId],cv::Scalar(0,255,0),1);
						cv::imshow("matTemp",matTemp);
						cv::waitKey(0);
#endif
#endif
					}
				}
			}
		}
	}

	// 延伸路加上方向
	int nExtendNum = vecExtendLinksId.size();
	for (int i=0; i<nExtendNum; i++)
	{
		int nExtendId = vecExtendLinksId[i];
		vecRoadNetDirection2MainRoad[nExtendId] = SAME_DIRECTION;
	}

	// 去除主路关键点过渡到对向同名路的中间线段

	return 0;
}

// 过滤link
int MergeMapData::formRoadNet3(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
							   const vector<LinkInfo>& vecLinkInfos,
							   const vector<LinkEndPointNode> vecAllEndPtnode,							  
							   const vector<int>& vecMainRoadNodeId,
							   int nMatchCenterSite,
							   cv::Rect rtScreen,
							   vector<int>& vecRoadNetDirection2MainRoad,
							   vector<vector<int> >& vecCrossPathLinkID,
							   vector<vector<int> >& vecCrossPathNodeId)
{
	// 参数自检
	int nLinkNum = vecRoadNetLinks.size();
	int nNodeNum = vecAllEndPtnode.size();
	int nMainRoadNodeNum = vecMainRoadNodeId.size();
	if (nLinkNum<=0 || nLinkNum!=vecLinkInfos.size() || nNodeNum<=0 ||
		nMainRoadNodeNum<=0 || nMainRoadNodeNum>nNodeNum ||
		nMatchCenterSite<0 || nMatchCenterSite>nMainRoadNodeNum-1)
	{
		return -1;
	}

#if IS_PRINT_LOG
#ifdef _WINDOWS_VER_
	printf("	formRoadNet3: nLinkNum=%d, nNodeNum=%d, nMainRoadNodeNum=%d\n",
		nLinkNum, nNodeNum,nMainRoadNodeNum);		
#else
	LOGD("	formRoadNet3: nLinkNum=%d, nNodeNum=%d, nMainRoadNodeNum=%d\n",
		nLinkNum, nNodeNum,nMainRoadNodeNum);			
#endif
#endif

	// 针对主路，基于角度，确定关键点
	float fMainAngel = 0.f;
	float fMainCosV = 2;
	int nKeySi = 0;
	bool bIsHaveKeyPt = false;		// 记录是否有关键点
	for (int i=1; i<nMainRoadNodeNum-1; i++)
	{		
		HAMapPoint hamPre = vecAllEndPtnode[vecMainRoadNodeId[i-1]].hamEndPoint;
		HAMapPoint hamCur = vecAllEndPtnode[vecMainRoadNodeId[i]].hamEndPoint;
		HAMapPoint hamNext = vecAllEndPtnode[vecMainRoadNodeId[i+1]].hamEndPoint;
		if (isRectInside(hamCur,rtScreen))
		{
			Vec2i v1 = Point(hamPre.x,hamPre.y) - Point(hamCur.x,hamCur.y);
			Vec2i v2 = Point(hamNext.x,hamNext.y) - Point(hamCur.x,hamCur.y);
			float fAngelTemp = 0.f;
			float fCosVTemp = v1.dot(v2);
			fCosVTemp = fCosVTemp/(getDistancePoint2Point(v1[0],v1[1],0,0)*getDistancePoint2Point(v2[0],v2[1],0,0));
			if (abs(fCosVTemp)<fMainCosV)
			{
				fMainCosV = abs(fCosVTemp);
				nKeySi = i;
				fMainAngel = acosf(fCosVTemp);
			}
		}
	}

#if IS_PRINT_LOG
#ifdef _WINDOWS_VER_
	printf("	formRoadNet3: nKeySi=%d, fMainCosV=%f, fMainAngel=%f\n", nKeySi, fMainCosV, fMainAngel);
#else
	LOGD("	formRoadNet3: nKeySi=%d, fMainCosV=%f, fMainAngel=%f\n", nKeySi, fMainCosV, fMainAngel);	
#endif
#endif

	float fCosThV = KEYPOINT_COSV_TH; 
	if (nKeySi!=0 && fMainCosV<fCosThV)
	{
		nMatchCenterSite = nKeySi;
		bIsHaveKeyPt = true;
	}

	vector<int> vecKeyIndex;		// 记录关键点、前、后点在vecMainRoadNodeId的索引
	vector<int> vecNormalIndex;		// 记录正常节点在vecMainRoadNodeId的索引	
	for (int i=0; i<nMainRoadNodeNum; i++)
	{
		if (bIsHaveKeyPt && (i==nMatchCenterSite-1 || i==nMatchCenterSite || i==nMatchCenterSite+1))
		{
			vecKeyIndex.push_back(i);
		}
		else
		{
			vecNormalIndex.push_back(i);
		}
	}

	// 赋初值
	vecRoadNetDirection2MainRoad = vector<int>(nLinkNum,OPPOSITE_DIRECTION);		// 需要保留的边，之后赋值为SAME_DIRECTION

	int nRet = 0;

	// 获取主路LinkId
	vector<int> vecMainRoadLinkId;			
	nRet = getPathLinkId(vecAllEndPtnode, vecMainRoadNodeId, vecMainRoadLinkId);
	if (nRet<0)
	{
		return -1;
	}

#if IS_PRINT_LOG
#ifdef _WINDOWS_VER_		
	printf("	formRoadNet3-vecMainRoadLinkId: \n");
	for (int i=0; i<vecMainRoadLinkId.size(); i++)
	{
		printf("%d, ",vecMainRoadLinkId[i]);
	}
	printf("\n	formRoadNet3: vecAllEndPtnode.size=%d, vecMainRoadNodeId.size=%d, vecMainRoadLinkId.size=%d\n", 
		vecAllEndPtnode.size(), vecMainRoadNodeId.size(), vecMainRoadLinkId.size());
#else
	LOGD("	formRoadNet3-vecMainRoadLinkId: \n");
	for (int i=0; i<vecMainRoadLinkId.size(); i++)
	{
		LOGD("%d, ",vecMainRoadLinkId[i]);
	}
	LOGD("\n	formRoadNet3: vecAllEndPtnode.size=%d, vecMainRoadNodeId.size=%d, vecMainRoadLinkId.size=%d\n", 
		vecAllEndPtnode.size(), vecMainRoadNodeId.size(), vecMainRoadLinkId.size());		
#endif
#endif

	// 记录延伸的link、node Id
	vector<int> vecNeedExtendLinkId, vecNeedExtendNodeId, vecNeedExtendDirection;		// NodeId对应Link的末端点
	vector<int> vecExtendLinksId, vecExtendNodesId, vecExtendLinkDirection;
	vector<Vec2i> vecDirectionV;		// 记录方向向量

	// 处理关键节点及前后邻居节点
	float fDelAngel = VECTOR_PARALLEL_ANGLE;
	fDelAngel = fDelAngel*CV_PI/180;
	if (bIsHaveKeyPt)		// 处理关键节点及邻居
	{
		// 关键节点
		int nCenterNodeId = vecMainRoadNodeId[nMatchCenterSite];		// 中心点Node Id
		LinkEndPointNode centerNode = vecAllEndPtnode[nCenterNodeId];	// 中心点对应的Node
		Point ptCenter = Point(centerNode.hamEndPoint.x, centerNode.hamEndPoint.y);	
		int nNeighborNum = centerNode.vecNeighborNodeId.size();
		vecDirectionV = vector<Vec2i>(nNeighborNum);	// 记录方向向量
		for (int i=0; i<nNeighborNum; i++)
		{
			int nNextLinkId = centerNode.vecLinkId[i];
			int nNextNodeId = centerNode.vecNeighborNodeId[i];
			Point ptNext = Point(centerNode.vecNeighborPoint[i].x, centerNode.vecNeighborPoint[i].y);
			vecDirectionV[i] = ptNext - ptCenter;

			vecRoadNetDirection2MainRoad[nNextLinkId] = SAME_DIRECTION;		// 保留

			// 记录需要延伸的linkId、nodeId
			int nSi = 0;
			if (!isBelongToVector(vecMainRoadLinkId,nNextLinkId,nSi))
			{
				vecNeedExtendNodeId.push_back(nNextNodeId);
				vecNeedExtendLinkId.push_back(nNextLinkId);
				vecNeedExtendDirection.push_back(centerNode.vecDirection[i]);
			}
		}	// end i

		// 前、后相邻节点
		for (int i=0; i<vecKeyIndex.size(); i++)
		{
			int nIndex = vecKeyIndex[i];
			if (nIndex==nMatchCenterSite)
			{
				continue;
			}

			int nCurNodeId = vecMainRoadNodeId[nIndex];
			LinkEndPointNode curMainNode = vecAllEndPtnode[nCurNodeId];
			Point ptCurMainPt = Point(curMainNode.hamEndPoint.x, curMainNode.hamEndPoint.y);	
			int nNeighborNum = curMainNode.vecNeighborNodeId.size();

			if (!isRectInside(curMainNode.hamEndPoint,rtScreen))
			{
				continue;
			}

			for (int j=0; j<nNeighborNum; j++)
			{				
				if (curMainNode.vecDirection[j]!=OPPOSITE_DIRECTION)
				{
					int nNextNodeId = curMainNode.vecNeighborNodeId[j];
					int nNextLinkId = curMainNode.vecLinkId[j];
					int nSi = 0;
					if (isBelongToVector(vecMainRoadNodeId,nNextNodeId,nSi))
					{
						vecRoadNetDirection2MainRoad[nNextLinkId] = SAME_DIRECTION;						
						continue;
					}					

					Point ptTemp = Point(curMainNode.vecNeighborPoint[j].x,curMainNode.vecNeighborPoint[j].y);
					Vec2i vTemp = ptTemp - ptCurMainPt;
					int nMinSi = -1;
					float fCosV = -2;
					for (int k=0; k<vecDirectionV.size(); k++)
					{
						float fCosTemp = vTemp.dot(vecDirectionV[k]);
						fCosTemp = fCosTemp/(getDistancePoint2Point(vTemp[0],vTemp[1],0,0)*
							getDistancePoint2Point(vecDirectionV[k][0],vecDirectionV[k][1],0,0));
						if (abs(fCosTemp)>fCosV)
						{
							fCosV = abs(fCosTemp);
							nMinSi = j;
						}
					}		// end k
					if (fCosV<cos(fDelAngel))
					{
						vecRoadNetDirection2MainRoad[nNextLinkId] = SAME_DIRECTION;

						// 记录需要延伸的linkId、nodeId
						int nSi = 0;
						if (!isBelongToVector(vecMainRoadLinkId,nNextLinkId,nSi))
						{
							vecNeedExtendNodeId.push_back(nNextNodeId);
							vecNeedExtendLinkId.push_back(nNextLinkId);
							vecNeedExtendDirection.push_back(curMainNode.vecDirection[j]);
						}
					}
				}			
			}	// end j
		}		// end i
	}

	// 处理其他正常点
	for (int i=0; i<vecNormalIndex.size(); i++)
	{
		int nIndex = vecNormalIndex[i];			

		int nCurNodeId = vecMainRoadNodeId[nIndex];
		LinkEndPointNode curMainNode = vecAllEndPtnode[nCurNodeId];
		Point ptCurMainPt = Point(curMainNode.hamEndPoint.x, curMainNode.hamEndPoint.y);	
		int nNeighborNum = curMainNode.vecNeighborNodeId.size();

		if (!isRectInside(curMainNode.hamEndPoint,rtScreen))
		{
			continue;
		}

		for (int j=0; j<nNeighborNum; j++)
		{	
			if (curMainNode.vecDirection[j]!=OPPOSITE_DIRECTION)
			{
				int nNextLinkId = curMainNode.vecLinkId[j];
				int nNextNodeId = curMainNode.vecNeighborNodeId[j];
				vecRoadNetDirection2MainRoad[nNextLinkId] = SAME_DIRECTION;

				// 记录需要延伸的linkId、nodeId
				int nSi = 0;
				if (!isBelongToVector(vecMainRoadLinkId,nNextLinkId,nSi))
				{
					vecNeedExtendNodeId.push_back(nNextNodeId);
					vecNeedExtendLinkId.push_back(nNextLinkId);
					vecNeedExtendDirection.push_back(curMainNode.vecDirection[j]);
				}
			}	
		}	// end j
	}	// end i

#if IS_PRINT_LOG
#ifdef _WINDOWS_VER_		
	printf("	formRoadNet3-vecNeedExtendNodeId, vecNeedExtendLinkId, vecNeedExtendDirection: \n");
	for (int i=0; i<vecNeedExtendNodeId.size(); i++)
	{
		//printf("%d, %d, %d; ",vecNeedExtendNodeId[i],vecMainRoadLinkId[i],vecNeedExtendDirection[i]);
		printf("%d, %d; ",vecNeedExtendNodeId[i],vecNeedExtendDirection[i]);
	}
	printf("\n	formRoadNet3: vecNeedExtendNodeId.size=%d, vecNeedExtendLinkId.size=%d, vecNeedExtendDirection.size=%d\n", 
		vecNeedExtendNodeId.size(), vecNeedExtendLinkId.size(), vecNeedExtendDirection.size());
#else
	LOGD("	formRoadNet3-vecNeedExtendNodeId, vecNeedExtendLinkId, vecNeedExtendDirection: \n");
	for (int i=0; i<vecNeedExtendNodeId.size(); i++)
	{
		LOGD("%d, %d, %d; ",vecNeedExtendNodeId[i],vecMainRoadLinkId[i],vecNeedExtendDirection[i]);
	}
	LOGD("\n	formRoadNet3: vecNeedExtendNodeId.size=%d, vecNeedExtendLinkId.size=%d, vecNeedExtendDirection.size=%d\n", 
		vecNeedExtendNodeId.size(), vecNeedExtendLinkId.size(), vecNeedExtendDirection.size());		
#endif
#endif


	// 处理关键点及前后邻居，关键点处，保留主路正、反延长线，邻居点删除主路平行线；若存在关键点到主路平行线的link，也删除


	// 延伸	
	vector<int> vecSinglePathLinkId, vecSinglePathNodeId;
	for (int i=0; i<vecNeedExtendNodeId.size(); i++)
	{
		int nStartSi = vecExtendLinksId.size();		// 用于记录单条路径在vecExtendLinksId中的起始位置
		int nLinkId = vecNeedExtendLinkId[i];
		int nNodeId = vecNeedExtendNodeId[i];
		int nDirection = vecNeedExtendDirection[i];
		vecExtendLinksId.push_back(nLinkId);
		vecExtendNodesId.push_back(nNodeId);
		vecExtendLinkDirection.push_back(nDirection);

		nRet = extendRoad(vecRoadNetLinks, vecLinkInfos, vecAllEndPtnode, vecMainRoadLinkId, 
			nDirection,	nLinkId, nNodeId, rtScreen,
			vecExtendLinksId, vecExtendNodesId, vecExtendLinkDirection);
#if IS_PRINT_LOG
		LOGD(" extendRoad:	nRet=%d \n",nRet);
#endif
		if (nRet<0)
		{			
#if IS_PRINT_LOG
#ifdef _WINDOWS_VER_		
			printf(" extendRoad:	nRet<0 \n");
#else
			LOGD(" Android extendRoad:	nRet<0 \n");					
#endif
#endif
			continue;
		}
		int nEndSi = vecExtendLinksId.size();		// 用于记录单条路径在vecExtendLinksId中的终止位置
		vecSinglePathLinkId.clear();
		vecSinglePathNodeId.clear();
		vecSinglePathLinkId.insert(vecSinglePathLinkId.end(),vecExtendLinksId.begin()+nStartSi,vecExtendLinksId.begin()+nEndSi);
		vecSinglePathNodeId.insert(vecSinglePathNodeId.end(),vecExtendNodesId.begin()+nStartSi,vecExtendNodesId.begin()+nEndSi);
#if IS_PRINT_LOG
#ifdef _WINDOWS_VER_		
		printf("	vecSinglePathNodeId.size=%d \n",vecSinglePathNodeId.size());
		for (int i=0; i<vecSinglePathNodeId.size(); i++)
		{
			printf("%d; ",vecSinglePathNodeId[i]);				
		}	
		printf("\n nStartSi=%d, nEndSi=%d \n",nStartSi,nEndSi);
#else
		LOGD("	vecSinglePathNodeId.size=%d \n",vecSinglePathNodeId.size());
		for (int i=0; i<vecSinglePathNodeId.size(); i++)
		{
			LOGD("%d; ",vecSinglePathNodeId[i]);				
		}	
		LOGD("\n nStartSi=%d, nEndSi=%d \n",nStartSi,nEndSi);
#endif
#endif


		vecCrossPathLinkID.push_back(vecSinglePathLinkId);
		vecCrossPathNodeId.push_back(vecSinglePathNodeId);
	}

#if IS_PRINT_LOG
#ifdef _WINDOWS_VER_		
	printf("	formRoadNet3-vecCrossPathNodeId, vecCrossPathLinkID: \n");
	for (int i=0; i<vecCrossPathLinkID.size(); i++)
	{
		for (int j=0; j<vecCrossPathLinkID[i].size(); j++)
		{
			printf("%d, %d; ",vecCrossPathLinkID[i][j],vecCrossPathLinkID[i][j]);
		}
		printf("\n");
	}
	printf("\n	formRoadNet3: vecCrossPathNodeId.size=%d, vecCrossPathLinkID.size=%d\n", 
		vecCrossPathNodeId.size(), vecCrossPathLinkID.size());
#else
	LOGD("	formRoadNet3-vecCrossPathNodeId, vecCrossPathLinkID: \n");
	for (int i=0; i<vecCrossPathLinkID.size(); i++)
	{
		for (int j=0; j<vecCrossPathLinkID[i].size(); j++)
		{
			LOGD("%d, %d; ",vecCrossPathLinkID[i][j],vecCrossPathLinkID[i][j]);
		}
		LOGD("\n");
	}
	LOGD("\n	formRoadNet3: vecCrossPathNodeId.size=%d, vecCrossPathLinkID.size=%d\n", 
		vecCrossPathNodeId.size(), vecCrossPathLinkID.size());	
#endif
#endif

	// 延伸路加上方向
	int nExtendNum = vecExtendLinksId.size();
	for (int i=0; i<nExtendNum; i++)
	{
		int nExtendId = vecExtendLinksId[i];
		vecRoadNetDirection2MainRoad[nExtendId] = SAME_DIRECTION;
	}

	// 去除主路关键点过渡到对向同名路的中间线段

	return 0;
}

// 过滤link
int MergeMapData::formRoadNet4(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
							   const vector<LinkInfo>& vecLinkInfos,
							   const vector<LinkEndPointNode> vecAllEndPtnode,							  
							   const vector<int>& vecMainRoadNodeId,
							   int nMatchCenterSite,
							   cv::Rect rtScreen,
							   vector<int>& vecRoadNetDirection2MainRoad,
							   vector<vector<int> >& vecCrossPathLinkID,
							   vector<vector<int> >& vecCrossPathNodeId)
{
	// 参数自检
	int nLinkNum = vecRoadNetLinks.size();
	int nNodeNum = vecAllEndPtnode.size();
	int nMainRoadNodeNum = vecMainRoadNodeId.size();
	if (nLinkNum<=0 || nLinkNum!=vecLinkInfos.size() || nNodeNum<=0 ||
		nMainRoadNodeNum<=0 || nMainRoadNodeNum>nNodeNum ||
		nMatchCenterSite<0 || nMatchCenterSite>nMainRoadNodeNum-1)
	{
		return -1;
	}

#if IS_PRINT_LOG
	LOGD("	formRoadNet4: nLinkNum=%d, nNodeNum=%d, nMainRoadNodeNum=%d\n",
		nLinkNum, nNodeNum,nMainRoadNodeNum);	
#endif

	// 针对主路，基于角度，确定关键点
	float fMainAngel = 0.f;
	float fMainCosV = 2;
	int nKeySi = 0;
	bool bIsHaveKeyPt = false;		// 记录是否有关键点
	for (int i=1; i<nMainRoadNodeNum-1; i++)
	{		
		HAMapPoint hamPre = vecAllEndPtnode[vecMainRoadNodeId[i-1]].hamEndPoint;
		HAMapPoint hamCur = vecAllEndPtnode[vecMainRoadNodeId[i]].hamEndPoint;
		HAMapPoint hamNext = vecAllEndPtnode[vecMainRoadNodeId[i+1]].hamEndPoint;
		if (isRectInside(hamCur,rtScreen))
		{
			Vec2i v1 = Point(hamPre.x,hamPre.y) - Point(hamCur.x,hamCur.y);
			Vec2i v2 = Point(hamNext.x,hamNext.y) - Point(hamCur.x,hamCur.y);
			float fAngelTemp = 0.f;
			float fCosVTemp = v1.dot(v2);
			fCosVTemp = fCosVTemp/(getDistancePoint2Point(v1[0],v1[1],0,0)*getDistancePoint2Point(v2[0],v2[1],0,0));
			if (abs(fCosVTemp)<fMainCosV)
			{
				fMainCosV = abs(fCosVTemp);
				nKeySi = i;
				fMainAngel = acosf(fCosVTemp);
			}
		}
	}

	float fCosThV = KEYPOINT_COSV_TH; 
	if (nKeySi!=0 && fMainCosV<fCosThV)
	{
		nMatchCenterSite = nKeySi;
		bIsHaveKeyPt = true;
	}

	vector<int> vecKeyIndex;		// 记录关键点、前、后点在vecMainRoadNodeId的索引
	vector<int> vecNormalIndex;		// 记录正常节点在vecMainRoadNodeId的索引

	if (bIsHaveKeyPt)		// 找关键点、前、后点在vecMainRoadNodeId的索引
	{		
		// 找前一个节点（要求邻居个数>=3）
		for (int i=nMatchCenterSite-1; i>=0; i--)
		{
			int nNodeIdTemp = vecMainRoadNodeId[i];
			if (vecAllEndPtnode[nNodeIdTemp].vecNeighborNodeId.size()>=3)
			{
				vecKeyIndex.push_back(i);
				break;
			}
		}

		vecKeyIndex.push_back(nMatchCenterSite);

		// 找后一个节点（要求邻居个数>=3）
		for (int i=nMatchCenterSite+1; i<nMainRoadNodeNum; i++)
		{
			int nNodeIdTemp = vecMainRoadNodeId[i];
			if (vecAllEndPtnode[nNodeIdTemp].vecNeighborNodeId.size()>=3)
			{
				vecKeyIndex.push_back(i);
				break;
			}
		}
	}

	for (int i=0; i<nMainRoadNodeNum; i++)		// 找正常节点在vecMainRoadNodeId的索引
	{
		int nSi = -1;
		if (!isBelongToVector(vecKeyIndex,i,nSi))
		{
			vecNormalIndex.push_back(i);
		}
	}

	// 赋初值
	vecRoadNetDirection2MainRoad = vector<int>(nLinkNum,OPPOSITE_DIRECTION);		// 需要保留的边，之后赋值为SAME_DIRECTION

	int nRet = 0;

	// 获取主路LinkId
	vector<int> vecMainRoadLinkId;			
	nRet = getPathLinkId(vecAllEndPtnode, vecMainRoadNodeId, vecMainRoadLinkId);
	if (nRet<0)
	{
		return -1;
	}


	// 记录延伸的link、node Id
	vector<int> vecNeedExtendLinkId, vecNeedExtendNodeId, vecNeedExtendDirection;		// NodeId对应Link的末端点
	vector<int> vecExtendLinksId, vecExtendNodesId, vecExtendLinkDirection;
	vector<Vec2i> vecDirectionV;		// 记录方向向量

	// 处理关键节点及前后邻居节点
	float fDelAngel = VECTOR_PARALLEL_ANGLE;
	fDelAngel = fDelAngel*CV_PI/180;
	if (bIsHaveKeyPt)		// 处理关键节点及邻居
	{
		// 关键节点
		int nCenterNodeId = vecMainRoadNodeId[nMatchCenterSite];		// 中心点Node Id
		LinkEndPointNode centerNode = vecAllEndPtnode[nCenterNodeId];	// 中心点对应的Node
		Point ptCenter = Point(centerNode.hamEndPoint.x, centerNode.hamEndPoint.y);	
		int nNeighborNum = centerNode.vecNeighborNodeId.size();
		vecDirectionV = vector<Vec2i>(nNeighborNum);	// 记录方向向量
		for (int i=0; i<nNeighborNum; i++)
		{
			int nNextLinkId = centerNode.vecLinkId[i];
			int nNextNodeId = centerNode.vecNeighborNodeId[i];
			Point ptNext = Point(centerNode.vecNeighborPoint[i].x, centerNode.vecNeighborPoint[i].y);
			vecDirectionV[i] = ptNext - ptCenter;

			vecRoadNetDirection2MainRoad[nNextLinkId] = SAME_DIRECTION;		// 保留

			// 记录需要延伸的linkId、nodeId
			int nSi = 0;
			if (!isBelongToVector(vecMainRoadLinkId,nNextLinkId,nSi))
			{
				vecNeedExtendNodeId.push_back(nNextNodeId);
				vecNeedExtendLinkId.push_back(nNextLinkId);
				vecNeedExtendDirection.push_back(centerNode.vecDirection[i]);
			}
		}	// end i

		// 前、后相邻节点
		for (int i=0; i<vecKeyIndex.size(); i++)
		{
			int nIndex = vecKeyIndex[i];
			if (nIndex==nMatchCenterSite)
			{
				continue;
			}

			int nCurNodeId = vecMainRoadNodeId[nIndex];
			LinkEndPointNode curMainNode = vecAllEndPtnode[nCurNodeId];
			Point ptCurMainPt = Point(curMainNode.hamEndPoint.x, curMainNode.hamEndPoint.y);	
			int nNeighborNum = curMainNode.vecNeighborNodeId.size();

			if (!isRectInside(curMainNode.hamEndPoint,rtScreen))
			{
				continue;
			}

			for (int j=0; j<nNeighborNum; j++)
			{				
				if (curMainNode.vecDirection[j]!=OPPOSITE_DIRECTION)
				{
					int nNextNodeId = curMainNode.vecNeighborNodeId[j];
					int nNextLinkId = curMainNode.vecLinkId[j];
					int nSi = 0;
					if (isBelongToVector(vecMainRoadNodeId,nNextNodeId,nSi))
					{
						vecRoadNetDirection2MainRoad[nNextLinkId] = SAME_DIRECTION;						
						continue;
					}					

					Point ptTemp = Point(curMainNode.vecNeighborPoint[j].x,curMainNode.vecNeighborPoint[j].y);
					Vec2i vTemp = ptTemp - ptCurMainPt;
					int nMinSi = -1;
					float fCosV = -2;
					for (int k=0; k<vecDirectionV.size(); k++)
					{
						float fCosTemp = vTemp.dot(vecDirectionV[k]);
						fCosTemp = fCosTemp/(getDistancePoint2Point(vTemp[0],vTemp[1],0,0)*
							getDistancePoint2Point(vecDirectionV[k][0],vecDirectionV[k][1],0,0));
						if (abs(fCosTemp)>fCosV)
						{
							fCosV = abs(fCosTemp);
							nMinSi = j;
						}
					}		// end k
					if (fCosV<cos(fDelAngel))
					{
						vecRoadNetDirection2MainRoad[nNextLinkId] = SAME_DIRECTION;

						// 记录需要延伸的linkId、nodeId
						int nSi = 0;
						if (!isBelongToVector(vecMainRoadLinkId,nNextLinkId,nSi))
						{
							vecNeedExtendNodeId.push_back(nNextNodeId);
							vecNeedExtendLinkId.push_back(nNextLinkId);
							vecNeedExtendDirection.push_back(curMainNode.vecDirection[j]);
						}
					}
				}			
			}	// end j
		}		// end i
	}

	// 处理其他正常点
	for (int i=0; i<vecNormalIndex.size(); i++)
	{
		int nIndex = vecNormalIndex[i];			

		int nCurNodeId = vecMainRoadNodeId[nIndex];
		LinkEndPointNode curMainNode = vecAllEndPtnode[nCurNodeId];
		Point ptCurMainPt = Point(curMainNode.hamEndPoint.x, curMainNode.hamEndPoint.y);	
		int nNeighborNum = curMainNode.vecNeighborNodeId.size();

		if (!isRectInside(curMainNode.hamEndPoint,rtScreen))
		{
			continue;
		}

		for (int j=0; j<nNeighborNum; j++)
		{	
			if (curMainNode.vecDirection[j]!=OPPOSITE_DIRECTION)
			{
				int nNextLinkId = curMainNode.vecLinkId[j];
				int nNextNodeId = curMainNode.vecNeighborNodeId[j];
				vecRoadNetDirection2MainRoad[nNextLinkId] = SAME_DIRECTION;

				// 记录需要延伸的linkId、nodeId
				int nSi = 0;
				if (!isBelongToVector(vecMainRoadLinkId,nNextLinkId,nSi))
				{
					vecNeedExtendNodeId.push_back(nNextNodeId);
					vecNeedExtendLinkId.push_back(nNextLinkId);
					vecNeedExtendDirection.push_back(curMainNode.vecDirection[j]);
				}
			}	
		}	// end j
	}	// end i

#if IS_PRINT_LOG
#ifdef _WINDOWS_VER_		
	printf("	formRoadNet3-vecNeedExtendNodeId, vecNeedExtendLinkId, vecNeedExtendDirection: \n");
	for (int i=0; i<vecNeedExtendNodeId.size(); i++)
	{
		//printf("%d, %d, %d; ",vecNeedExtendNodeId[i],vecMainRoadLinkId[i],vecNeedExtendDirection[i]);
		printf("%d, %d; ",vecNeedExtendNodeId[i],vecNeedExtendDirection[i]);
	}
	printf("\n	formRoadNet3: vecNeedExtendNodeId.size=%d, vecNeedExtendLinkId.size=%d, vecNeedExtendDirection.size=%d\n", 
		vecNeedExtendNodeId.size(), vecNeedExtendLinkId.size(), vecNeedExtendDirection.size());
#else
	LOGD("	formRoadNet3-vecNeedExtendNodeId, vecNeedExtendLinkId, vecNeedExtendDirection: \n");
	for (int i=0; i<vecNeedExtendNodeId.size(); i++)
	{
		LOGD("%d, %d, %d; ",vecNeedExtendNodeId[i],vecMainRoadLinkId[i],vecNeedExtendDirection[i]);
	}
	LOGD("\n	formRoadNet3: vecNeedExtendNodeId.size=%d, vecNeedExtendLinkId.size=%d, vecNeedExtendDirection.size=%d\n", 
		vecNeedExtendNodeId.size(), vecNeedExtendLinkId.size(), vecNeedExtendDirection.size());		
#endif
#endif


	// 处理关键点及前后邻居，关键点处，保留主路正、反延长线，邻居点删除主路平行线；若存在关键点到主路平行线的link，也删除


	// 延伸	
	vector<int> vecSinglePathLinkId, vecSinglePathNodeId;
	for (int i=0; i<vecNeedExtendNodeId.size(); i++)
	{
		int nStartSi = vecExtendLinksId.size();		// 用于记录单条路径在vecExtendLinksId中的起始位置
		int nLinkId = vecNeedExtendLinkId[i];
		int nNodeId = vecNeedExtendNodeId[i];
		int nDirection = vecNeedExtendDirection[i];
		vecExtendLinksId.push_back(nLinkId);
		vecExtendNodesId.push_back(nNodeId);
		vecExtendLinkDirection.push_back(nDirection);

		nRet = extendRoad(vecRoadNetLinks, vecLinkInfos, vecAllEndPtnode, vecMainRoadLinkId, 
			nDirection,	nLinkId, nNodeId, rtScreen,
			vecExtendLinksId, vecExtendNodesId, vecExtendLinkDirection);
#if IS_PRINT_LOG
		LOGD(" extendRoad:	nRet=%d \n",nRet);
#endif
		if (nRet<0)
		{			
#if IS_PRINT_LOG
#ifdef _WINDOWS_VER_		
			printf(" extendRoad:	nRet<0 \n");
#else
			LOGD(" Android extendRoad:	nRet<0 \n");					
#endif
#endif
			continue;
		}
		int nEndSi = vecExtendLinksId.size();		// 用于记录单条路径在vecExtendLinksId中的终止位置
		vecSinglePathLinkId.clear();
		vecSinglePathNodeId.clear();
		vecSinglePathLinkId.insert(vecSinglePathLinkId.end(),vecExtendLinksId.begin()+nStartSi,vecExtendLinksId.begin()+nEndSi);
		vecSinglePathNodeId.insert(vecSinglePathNodeId.end(),vecExtendNodesId.begin()+nStartSi,vecExtendNodesId.begin()+nEndSi);
#if IS_PRINT_LOG
#ifdef _WINDOWS_VER_		
		printf("	vecSinglePathNodeId.size=%d \n",vecSinglePathNodeId.size());
		for (int i=0; i<vecSinglePathNodeId.size(); i++)
		{
			printf("%d; ",vecSinglePathNodeId[i]);				
		}	
		printf("\n nStartSi=%d, nEndSi=%d \n",nStartSi,nEndSi);
#else
		LOGD("	vecSinglePathNodeId.size=%d \n",vecSinglePathNodeId.size());
		for (int i=0; i<vecSinglePathNodeId.size(); i++)
		{
			LOGD("%d; ",vecSinglePathNodeId[i]);				
		}	
		LOGD("\n nStartSi=%d, nEndSi=%d \n",nStartSi,nEndSi);
#endif
#endif


		vecCrossPathLinkID.push_back(vecSinglePathLinkId);
		vecCrossPathNodeId.push_back(vecSinglePathNodeId);
	}

#if IS_PRINT_LOG
#ifdef _WINDOWS_VER_		
	printf("	formRoadNet3-vecCrossPathNodeId, vecCrossPathLinkID: \n");
	for (int i=0; i<vecCrossPathLinkID.size(); i++)
	{
		for (int j=0; j<vecCrossPathLinkID[i].size(); j++)
		{
			printf("%d, %d; ",vecCrossPathLinkID[i][j],vecCrossPathLinkID[i][j]);
		}
		printf("\n");
	}
	printf("\n	formRoadNet3: vecCrossPathNodeId.size=%d, vecCrossPathLinkID.size=%d\n", 
		vecCrossPathNodeId.size(), vecCrossPathLinkID.size());
#else
	LOGD("	formRoadNet3-vecCrossPathNodeId, vecCrossPathLinkID: \n");
	for (int i=0; i<vecCrossPathLinkID.size(); i++)
	{
		for (int j=0; j<vecCrossPathLinkID[i].size(); j++)
		{
			LOGD("%d, %d; ",vecCrossPathLinkID[i][j],vecCrossPathLinkID[i][j]);
		}
		LOGD("\n");
	}
	LOGD("\n	formRoadNet3: vecCrossPathNodeId.size=%d, vecCrossPathLinkID.size=%d\n", 
		vecCrossPathNodeId.size(), vecCrossPathLinkID.size());	
#endif
#endif

	// 延伸路加上方向
	int nExtendNum = vecExtendLinksId.size();
	for (int i=0; i<nExtendNum; i++)
	{
		int nExtendId = vecExtendLinksId[i];
		vecRoadNetDirection2MainRoad[nExtendId] = SAME_DIRECTION;
	}

	// 去除主路关键点过渡到对向同名路的中间线段

	return 0;
}


// 过滤link，删除历史上已出现的岔路，并更新历史岔路起点
int MergeMapData::formRoadNet5(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
							   const vector<LinkInfo>& vecLinkInfos,
							   const vector<LinkEndPointNode> vecAllEndPtnode,							  
							   const vector<int>& vecMainRoadNodeId,
							   int nMatchCenterSite,
							   cv::Rect rtMainRoadScreen,		// 主路窗口
							   cv::Rect rtCrossRoadScreen,		// 岔路窗口
							   vector<int>& vecRoadNetDirection2MainRoad,
							   vector<vector<int> >& vecCrossPathLinkID,
							   vector<vector<int> >& vecCrossPathNodeId,
							   vector<HAMapPoint>& vecHistoryCrossPt)		// 历史岔路起点[in/out]
{
	// 参数自检
	int nLinkNum = vecRoadNetLinks.size();
	int nNodeNum = vecAllEndPtnode.size();
	int nMainRoadNodeNum = vecMainRoadNodeId.size();
	if (nLinkNum<=0 || nLinkNum!=vecLinkInfos.size() || nNodeNum<=0 ||
		nMainRoadNodeNum<=0 || nMainRoadNodeNum>nNodeNum ||
		nMatchCenterSite<0 || nMatchCenterSite>nMainRoadNodeNum-1)
	{
		return -1;
	}

#if IS_PRINT_LOG
	LOGD("	formRoadNet4: nLinkNum=%d, nNodeNum=%d, nMainRoadNodeNum=%d\n",
		nLinkNum, nNodeNum,nMainRoadNodeNum);	
#endif

	// 针对主路，基于角度，确定关键点
	float fMainAngel = 0.f;
	float fMainCosV = 2;
	int nKeySi = 0;
	bool bIsHaveKeyPt = false;		// 记录是否有关键点
	for (int i=1; i<nMainRoadNodeNum-1; i++)
	{		
		HAMapPoint hamPre = vecAllEndPtnode[vecMainRoadNodeId[i-1]].hamEndPoint;
		HAMapPoint hamCur = vecAllEndPtnode[vecMainRoadNodeId[i]].hamEndPoint;
		HAMapPoint hamNext = vecAllEndPtnode[vecMainRoadNodeId[i+1]].hamEndPoint;
		if (isRectInside(hamCur,rtMainRoadScreen))
		{
			Vec2i v1 = Point(hamPre.x,hamPre.y) - Point(hamCur.x,hamCur.y);
			Vec2i v2 = Point(hamNext.x,hamNext.y) - Point(hamCur.x,hamCur.y);
			float fAngelTemp = 0.f;
			float fCosVTemp = v1.dot(v2);
			fCosVTemp = fCosVTemp/(getDistancePoint2Point(v1[0],v1[1],0,0)*getDistancePoint2Point(v2[0],v2[1],0,0));
			if (abs(fCosVTemp)<fMainCosV)
			{
				fMainCosV = abs(fCosVTemp);
				nKeySi = i;
				fMainAngel = acosf(fCosVTemp);
			}
		}
	}

	float fCosThV = KEYPOINT_COSV_TH; 
	if (nKeySi!=0 && fMainCosV<fCosThV)
	{
		nMatchCenterSite = nKeySi;
		bIsHaveKeyPt = true;
	}

	vector<int> vecKeyIndex;		// 记录关键点、前、后点在vecMainRoadNodeId的索引
	vector<int> vecNormalIndex;		// 记录正常节点在vecMainRoadNodeId的索引

	if (bIsHaveKeyPt)		// 找关键点、前、后点在vecMainRoadNodeId的索引
	{		
		// 找前一个节点（要求邻居个数>=3）
		for (int i=nMatchCenterSite-1; i>=0; i--)
		{
			int nNodeIdTemp = vecMainRoadNodeId[i];
			if (vecAllEndPtnode[nNodeIdTemp].vecNeighborNodeId.size()>=3)
			{
				vecKeyIndex.push_back(i);
				break;
			}
		}

		vecKeyIndex.push_back(nMatchCenterSite);

		// 找后一个节点（要求邻居个数>=3）
		for (int i=nMatchCenterSite+1; i<nMainRoadNodeNum; i++)
		{
			int nNodeIdTemp = vecMainRoadNodeId[i];
			if (vecAllEndPtnode[nNodeIdTemp].vecNeighborNodeId.size()>=3)
			{
				vecKeyIndex.push_back(i);
				break;
			}
		}
	}

	for (int i=0; i<nMainRoadNodeNum; i++)		// 找正常节点在vecMainRoadNodeId的索引
	{
		int nSi = -1;
		if (!isBelongToVector(vecKeyIndex,i,nSi))
		{
			vecNormalIndex.push_back(i);
		}
	}

	// 赋初值
	vecRoadNetDirection2MainRoad = vector<int>(nLinkNum,OPPOSITE_DIRECTION);		// 需要保留的边，之后赋值为SAME_DIRECTION

	int nRet = 0;

	// 获取主路LinkId
	vector<int> vecMainRoadLinkId;			
	nRet = getPathLinkId(vecAllEndPtnode, vecMainRoadNodeId, vecMainRoadLinkId);
	if (nRet<0)
	{
		return -1;
	}


	// 记录延伸的link、node Id
	vector<int> vecNeedExtendLinkId, vecNeedExtendNodeId, vecNeedExtendDirection;		// NodeId对应Link的末端点
	vector<int> vecExtendLinksId, vecExtendNodesId, vecExtendLinkDirection;
	vector<Vec2i> vecDirectionV;		// 记录方向向量

	// 处理关键节点及前后邻居节点
	float fDelAngel = VECTOR_PARALLEL_ANGLE;
	fDelAngel = fDelAngel*CV_PI/180;
	vector<HAMapPoint> vecTempHistoryPt = vecHistoryCrossPt;		// 记录本次对应的历史数据
	if (bIsHaveKeyPt)		// 处理关键节点及邻居
	{
		// 关键节点
		int nCenterNodeId = vecMainRoadNodeId[nMatchCenterSite];		// 中心点Node Id
		LinkEndPointNode centerNode = vecAllEndPtnode[nCenterNodeId];	// 中心点对应的Node
		Point ptCenter = Point(centerNode.hamEndPoint.x, centerNode.hamEndPoint.y);	
		int nNeighborNum = centerNode.vecNeighborNodeId.size();
		vecDirectionV = vector<Vec2i>(nNeighborNum);	// 记录方向向量
		for (int i=0; i<nNeighborNum; i++)
		{
			int nNextLinkId = centerNode.vecLinkId[i];
			int nNextNodeId = centerNode.vecNeighborNodeId[i];
			Point ptNext = Point(centerNode.vecNeighborPoint[i].x, centerNode.vecNeighborPoint[i].y);
			vecDirectionV[i] = ptNext - ptCenter;

			vecRoadNetDirection2MainRoad[nNextLinkId] = SAME_DIRECTION;		// 保留

			// 记录需要延伸的linkId、nodeId
			int nSi = 0, nSi1 = 0;
			if (!isBelongToVector(vecMainRoadLinkId,nNextLinkId,nSi) && 
				!isHamapPtBelongToSet(vecTempHistoryPt,centerNode.hamEndPoint,nSi1))
			{
				vecNeedExtendNodeId.push_back(nNextNodeId);
				vecNeedExtendLinkId.push_back(nNextLinkId);
				vecNeedExtendDirection.push_back(centerNode.vecDirection[i]);
				if (!isHamapPtBelongToSet(vecHistoryCrossPt,centerNode.hamEndPoint,nSi1))
				{
					vecHistoryCrossPt.push_back(centerNode.hamEndPoint);		// 更新
				}				
			}
		}	// end i

		// 前、后相邻节点
		for (int i=0; i<vecKeyIndex.size(); i++)
		{
			int nIndex = vecKeyIndex[i];
			if (nIndex==nMatchCenterSite)
			{
				continue;
			}

			int nCurNodeId = vecMainRoadNodeId[nIndex];
			LinkEndPointNode curMainNode = vecAllEndPtnode[nCurNodeId];
			Point ptCurMainPt = Point(curMainNode.hamEndPoint.x, curMainNode.hamEndPoint.y);	
			int nNeighborNum = curMainNode.vecNeighborNodeId.size();

			if (!isRectInside(curMainNode.hamEndPoint,rtMainRoadScreen))
			{
				continue;
			}

			for (int j=0; j<nNeighborNum; j++)
			{				
				if (curMainNode.vecDirection[j]!=OPPOSITE_DIRECTION)
				{
					int nNextNodeId = curMainNode.vecNeighborNodeId[j];
					int nNextLinkId = curMainNode.vecLinkId[j];
					int nSi = 0;
					if (isBelongToVector(vecMainRoadNodeId,nNextNodeId,nSi))
					{
						vecRoadNetDirection2MainRoad[nNextLinkId] = SAME_DIRECTION;						
						continue;
					}					

					Point ptTemp = Point(curMainNode.vecNeighborPoint[j].x,curMainNode.vecNeighborPoint[j].y);
					Vec2i vTemp = ptTemp - ptCurMainPt;
					int nMinSi = -1;
					float fCosV = -2;
					for (int k=0; k<vecDirectionV.size(); k++)
					{
						float fCosTemp = vTemp.dot(vecDirectionV[k]);
						fCosTemp = fCosTemp/(getDistancePoint2Point(vTemp[0],vTemp[1],0,0)*
							getDistancePoint2Point(vecDirectionV[k][0],vecDirectionV[k][1],0,0));
						if (abs(fCosTemp)>fCosV)
						{
							fCosV = abs(fCosTemp);
							nMinSi = j;
						}
					}		// end k
					if (fCosV<cos(fDelAngel))
					{
						vecRoadNetDirection2MainRoad[nNextLinkId] = SAME_DIRECTION;

						// 记录需要延伸的linkId、nodeId
						int nSi = 0, nSi1 = 0;
						if (!isBelongToVector(vecMainRoadLinkId,nNextLinkId,nSi) &&
							!isHamapPtBelongToSet(vecTempHistoryPt,curMainNode.hamEndPoint,nSi1))
						{
							vecNeedExtendNodeId.push_back(nNextNodeId);
							vecNeedExtendLinkId.push_back(nNextLinkId);
							vecNeedExtendDirection.push_back(curMainNode.vecDirection[j]);

							if (!isHamapPtBelongToSet(vecHistoryCrossPt,curMainNode.hamEndPoint,nSi1))
							{
								vecHistoryCrossPt.push_back(curMainNode.hamEndPoint);		// 更新
							}
						}
					}
				}			
			}	// end j
		}		// end i
	}

	// 处理其他正常点
	for (int i=0; i<vecNormalIndex.size(); i++)
	{
		int nIndex = vecNormalIndex[i];			

		int nCurNodeId = vecMainRoadNodeId[nIndex];
		LinkEndPointNode curMainNode = vecAllEndPtnode[nCurNodeId];
		Point ptCurMainPt = Point(curMainNode.hamEndPoint.x, curMainNode.hamEndPoint.y);	
		int nNeighborNum = curMainNode.vecNeighborNodeId.size();

		if (!isRectInside(curMainNode.hamEndPoint,rtMainRoadScreen))
		{
			continue;
		}

		for (int j=0; j<nNeighborNum; j++)
		{	
			if (curMainNode.vecDirection[j]!=OPPOSITE_DIRECTION)
			{
				int nNextLinkId = curMainNode.vecLinkId[j];
				int nNextNodeId = curMainNode.vecNeighborNodeId[j];
				vecRoadNetDirection2MainRoad[nNextLinkId] = SAME_DIRECTION;

				// 记录需要延伸的linkId、nodeId
				int nSi = 0, nSi1 = 0;
				if (!isBelongToVector(vecMainRoadLinkId,nNextLinkId,nSi) &&
					!isHamapPtBelongToSet(vecTempHistoryPt,curMainNode.hamEndPoint,nSi1))
				{
					vecNeedExtendNodeId.push_back(nNextNodeId);
					vecNeedExtendLinkId.push_back(nNextLinkId);
					vecNeedExtendDirection.push_back(curMainNode.vecDirection[j]);

					if (!isHamapPtBelongToSet(vecHistoryCrossPt,curMainNode.hamEndPoint,nSi1))
					{
						vecHistoryCrossPt.push_back(curMainNode.hamEndPoint);		// 更新
					}					
				}
			}	
		}	// end j
	}	// end i


	// 处理关键点及前后邻居，关键点处，保留主路正、反延长线，邻居点删除主路平行线；若存在关键点到主路平行线的link，也删除


	// 延伸	
	vector<int> vecSinglePathLinkId, vecSinglePathNodeId;
	for (int i=0; i<vecNeedExtendNodeId.size(); i++)
	{
		int nStartSi = vecExtendLinksId.size();		// 用于记录单条路径在vecExtendLinksId中的起始位置
		int nLinkId = vecNeedExtendLinkId[i];
		int nNodeId = vecNeedExtendNodeId[i];
		int nDirection = vecNeedExtendDirection[i];

		// ==================此处写死，仅用作发布会，完后需删除===============
		if (vecAllEndPtnode[nNodeId].hamEndPoint.x==824201 && vecAllEndPtnode[nNodeId].hamEndPoint.y==1064960)
		{
			continue;
		}
		// ==========================end======================

		vecExtendLinksId.push_back(nLinkId);
		vecExtendNodesId.push_back(nNodeId);
		vecExtendLinkDirection.push_back(nDirection);

		nRet = extendRoad(vecRoadNetLinks, vecLinkInfos, vecAllEndPtnode, vecMainRoadLinkId, 
			nDirection,	nLinkId, nNodeId, rtCrossRoadScreen,
			vecExtendLinksId, vecExtendNodesId, vecExtendLinkDirection);

		if (nRet<0)
		{	
			continue;
		}
		int nEndSi = vecExtendLinksId.size();		// 用于记录单条路径在vecExtendLinksId中的终止位置
		vecSinglePathLinkId.clear();
		vecSinglePathNodeId.clear();
		vecSinglePathLinkId.insert(vecSinglePathLinkId.end(),vecExtendLinksId.begin()+nStartSi,vecExtendLinksId.begin()+nEndSi);
		vecSinglePathNodeId.insert(vecSinglePathNodeId.end(),vecExtendNodesId.begin()+nStartSi,vecExtendNodesId.begin()+nEndSi);

		vecCrossPathLinkID.push_back(vecSinglePathLinkId);
		vecCrossPathNodeId.push_back(vecSinglePathNodeId);
	}


	// 延伸路加上方向
	int nExtendNum = vecExtendLinksId.size();
	for (int i=0; i<nExtendNum; i++)
	{
		int nExtendId = vecExtendLinksId[i];
		vecRoadNetDirection2MainRoad[nExtendId] = SAME_DIRECTION;
	}

	// 去除主路关键点过渡到对向同名路的中间线段

	return 0;
}


// 延伸道路，nCurNodeId表示Link的尾巴Node
int MergeMapData::extendRoad(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLinks,
							const vector<LinkInfo>& vecRoadNetLinkInfo,
							const vector<LinkEndPointNode> vecLinkEndPtnode,
							const vector<int>& vecMainRoadLinkId, 
							int nCurDirection,	int nCurLinkId, int nCurNodeId, cv::Rect rtScreen,									 
							vector<int>& vecExtendLinksId, vector<int>& vecExtendNodesId,
							vector<int>& vecExtendLinkDirection)
{
	// 参数自检
	if (vecRoadNetLinkInfo.size()<0 || vecLinkEndPtnode.size()<0 ||
		nCurLinkId<0 || nCurNodeId<0 || rtScreen.width<0 || rtScreen.height<0)
	{
		return -1;
	}

#if IS_PRINT_LOG
	#ifdef _WINDOWS_VER_
		printf("extendRoad - enter!! \n");
		printf("	vecRoadNetLinks.size=%d, vecLinkEndPtnode.size=%d, vecMainRoadLinkId.size=%d\n",
			vecRoadNetLinks.size(), vecLinkEndPtnode.size(),vecMainRoadLinkId.size());
		printf("	nCurDirection=%d, nCurLinkId=%d, nCurNodeId=%d\n",
			nCurDirection, nCurLinkId,nCurNodeId);
		printf("rtScreen.w=%d, rtScreen.h=%d\n",rtScreen.width, rtScreen.height);
	#else
		LOGD("extendRoad - enter!! \n");
		LOGD("	vecRoadNetLinks.size=%d, vecLinkEndPtnode.size=%d, vecMainRoadLinkId.size=%d\n",
			vecRoadNetLinks.size(), vecLinkEndPtnode.size(),vecMainRoadLinkId.size());
		LOGD("	nCurDirection=%d, nCurLinkId=%d, nCurNodeId=%d\n",
			nCurDirection, nCurLinkId,nCurNodeId);
		LOGD("rtScreen.w=%d, rtScreen.h=%d\n",rtScreen.width, rtScreen.height);
	#endif
#endif

	int nRet = -1;

	// 延伸
	LinkEndPointNode curNode = vecLinkEndPtnode[nCurNodeId];
	
#if IS_PRINT_LOG
	#ifdef _WINDOWS_VER_
		printf("extendRoad - curNode!! \n");
		for (int i=0; i<curNode.vecLinkId.size(); i++)
		{
			printf("curNode.vecLinkId[%d]=%d,  ", i, curNode.vecLinkId[i]);
		}
		printf("\n");
	#else
		LOGD("extendRoad - curNode!! \n");
		for (int i=0; i<curNode.vecLinkId.size(); i++)
		{
			LOGD("curNode.vecLinkId[%d]=%d,  ", i, curNode.vecLinkId[i]);
		}
		LOGD("\n");
	#endif
#endif

	// 判断当前点是否在窗口内
	if (!isRectInside(curNode.hamEndPoint,rtScreen))		// 终止条件
	{
	#if IS_PRINT_LOG
		#ifdef _WINDOWS_VER_			
				printf("extendRoad - isRectInside: false\n");
		#else	
				LOGD("extendRoad Android - isRectInside: false\n");
		#endif
	#endif
		return 0;
	}
	int nSi = -1;
	if (!isBelongToVector(curNode.vecLinkId,nCurLinkId,nSi))
	{		
	#if IS_PRINT_LOG
		#ifdef _WINDOWS_VER_			
			printf("extendRoad - isBelongToVector: false\n");
		#else	
			LOGD("extendRoad - isBelongToVector: false\n");		
		#endif
	#endif

		return -1;
	}	
	
	// 当前方向向量
	Vec2i vCur = Point(curNode.vecNeighborPoint[nSi].x,curNode.vecNeighborPoint[nSi].y) - 
					Point(curNode.hamEndPoint.x,curNode.hamEndPoint.y);	// 方向向量
	vCur = -vCur;		// 按路径方向定向量方向

	LinkInfo curlinkInfo = vecRoadNetLinkInfo[nCurLinkId];
	//float fCurRouteId = curlinkInfo.routeId;
	int nNeighborNum = curNode.vecNeighborNodeId.size();
	bool bIsOn = false;		// 标识，表示是否继续往下延伸
	int nNextLinkId = -1, nNextNodeId = -1, nNextDirection = -1;		// 记录下一个link和node的Id，以及方向
	int nMaxSi = -1;
	float fMaxCosV = -2; 
	for (int i=0; i<nNeighborNum; i++)
	{
		nNextLinkId = curNode.vecLinkId[i];
		LinkInfo nextlinkInfo = vecRoadNetLinkInfo[nNextLinkId];
		nNextNodeId = curNode.vecNeighborNodeId[i];
		LinkEndPointNode nextNode = vecLinkEndPtnode[nNextNodeId];

		//vector<HAMapPoint> vecNextLink = vecRoadNetLinks[nNextLinkId];
		//Vec2i vNext = Point(vecNextLink[0].x,vecNextLink[0].y) - 
		//				Point(vecNextLink[vecNextLink.size()-1].x,vecNextLink[vecNextLink.size()-1].y);	// 方向向量

		Vec2i vNext;		// 记录外延link在窗口内的方向
		Point ptNext = Point(curNode.vecNeighborPoint[i].x,curNode.vecNeighborPoint[i].y);
		// 判断相邻node是否在窗口内 
		if (!isRectInside(curNode.vecNeighborPoint[i],rtScreen))
		{
			// 计算link与窗口的交点
			HAMapPoint hamCrossPtTemp;
			nRet = getCrossPointLink2Rect(vecRoadNetLinks[nNextLinkId],rtScreen,hamCrossPtTemp);
			if (nRet<0)
			{
				break;
			}

			ptNext = Point(hamCrossPtTemp.x, hamCrossPtTemp.y);		// 更新
		}

		vNext = ptNext - Point(curNode.hamEndPoint.x,curNode.hamEndPoint.y);	// 方向向量

		nNextDirection = curNode.vecDirection[i];
		//float fNextRouteId = nextlinkInfo.routeId;
		int nSite = -1;
		if ((!isBelongToVector(vecExtendLinksId,nNextLinkId,nSite)) &&
			(!isBelongToVector(vecMainRoadLinkId,nNextLinkId,nSite)) &&			
			(nCurDirection<=1 || nNextDirection<=1 || nNextDirection==nCurDirection))
		{
			// 寻找同向且夹角最小的邻居
			float fCosTemp = vNext.dot(vCur);
			fCosTemp = fCosTemp/(getDistancePoint2Point(vCur[0],vCur[1],0,0)*getDistancePoint2Point(vNext[0],vNext[1],0,0));
			if (fCosTemp>fMaxCosV)
			{
				fMaxCosV = fCosTemp;
				nMaxSi = i;
			}
		}
	}

#if IS_PRINT_LOG
	#ifdef _WINDOWS_VER_		
		printf("	fMaxCosV=%f\n",fMaxCosV);		
	#else
		LOGD("	fMaxCosV=%f\n",fMaxCosV);	
	#endif
#endif

	if (fMaxCosV>=cos(EXTEND_ROAD_ANGLE*CV_PI/180))
	{
		// 延伸
		nNextLinkId = curNode.vecLinkId[nMaxSi];
		nNextNodeId = curNode.vecNeighborNodeId[nMaxSi];
		nNextDirection = curNode.vecDirection[nMaxSi];
		vecExtendLinksId.push_back(nNextLinkId);
		vecExtendNodesId.push_back(nNextNodeId);
		vecExtendLinkDirection.push_back(nNextDirection);
		bIsOn = true;		
	}

	if ((!bIsOn)&&nNeighborNum<=2)
	{
		for (int i=0; i<nNeighborNum; i++)
		{
			int nNextLinkTempId = curNode.vecLinkId[i];
			LinkInfo nextlinkInfo = vecRoadNetLinkInfo[nNextLinkTempId];
			int nNextNodeTempId = curNode.vecNeighborNodeId[i];
			LinkEndPointNode nextNode = vecLinkEndPtnode[nNextNodeTempId];

			int nNextDirectionTemp = curNode.vecDirection[i];
			//float fNextRouteId = nextlinkInfo.routeId;
			int nSite = -1;
			if ((!isBelongToVector(vecExtendLinksId,nNextLinkTempId,nSite)) &&
				(!isBelongToVector(vecMainRoadLinkId,nNextLinkTempId,nSite)) &&			
				(nCurDirection<=1 || nNextDirectionTemp<=1 || nNextDirectionTemp==nCurDirection))
			{
				// 延伸
				nNextLinkId = nNextLinkTempId;
				nNextNodeId = nNextNodeTempId;
				nNextDirection = nNextDirectionTemp;
				vecExtendLinksId.push_back(nNextLinkId);
				vecExtendNodesId.push_back(nNextNodeId);
				vecExtendLinkDirection.push_back(nNextDirection);
				bIsOn = true;
			}
		}
	}

	// 递归
	//int nRet = -1;
	nRet = -1;
	if (bIsOn)
	{
		nRet = extendRoad(vecRoadNetLinks,vecRoadNetLinkInfo, vecLinkEndPtnode, 
							vecMainRoadLinkId, nNextDirection, nNextLinkId, nNextNodeId, rtScreen,	
							vecExtendLinksId, vecExtendNodesId,vecExtendLinkDirection);
		if (nRet<0)
		{
		#if IS_PRINT_LOG
			#ifdef _WINDOWS_VER_		
				printf("extendRoad - extendRoad: nRet<0\n");		
			#else
				LOGD("extendRoad - extendRoad: nRet<0\n");
			#endif
		#endif
			return -1;
		}
		return 0;
	}
	else		// 终止条件
	{
		return 0;
	}	
}

// 延伸同名路
int MergeMapData::extendSameNameRoad(const vector<LinkInfo>& vecRoadNetLinkInfo,
									 const vector<LinkEndPointNode> vecLinkEndPtnode,
									 const vector<int>& vecMainRoadLinkId, 
									 int nCurLinkId, int nCurNodeId, cv::Rect rtScreen,									 
									 vector<int>& vecExtendLinksId, vector<int>& vecExtendNodesId)
{
	// 参数自检
	if (vecRoadNetLinkInfo.size()<0 || vecLinkEndPtnode.size()<0 ||
		nCurLinkId<0 || nCurNodeId<0 || rtScreen.width<0 || rtScreen.height<0)
	{
		return -1;
	}
	
	// 延伸
	LinkEndPointNode curNode = vecLinkEndPtnode[nCurNodeId];

	// 判断当前点是否在窗口内
	if (!isRectInside(curNode.hamEndPoint,rtScreen))		// 终止条件
	{
		return 0;
	}

	LinkInfo curlinkInfo = vecRoadNetLinkInfo[nCurLinkId];
	float fCurRouteId = curlinkInfo.routeId;
	int nNeighborNum = curNode.vecNeighborNodeId.size();
	bool bIsOn = false;		// 标识，表示是否继续往下延伸
	int nNextLinkId = -1, nNextNodeId = -1;		// 记录下一个link和node的Id
	for (int i=0; i<nNeighborNum; i++)
	{
		nNextLinkId = curNode.vecLinkId[i];
		LinkInfo nextlinkInfo = vecRoadNetLinkInfo[nNextLinkId];
		nNextNodeId = curNode.vecNeighborNodeId[i];
		LinkEndPointNode nextNode = vecLinkEndPtnode[nNextNodeId];

		float fNextRouteId = nextlinkInfo.routeId;
		int nSite = -1;
		if ((!isBelongToVector(vecExtendLinksId,nNextLinkId,nSite)) &&
			(!isBelongToVector(vecMainRoadLinkId,nNextLinkId,nSite)) &&
			abs(fNextRouteId-fCurRouteId)<1e-6)		// 同名、同路
		{
		#ifdef _WINDOWS_VER_
			printf("	nCurLinkId=%d, nCurNodeId=%d,fCurRouteId=%.4f;\n", nCurLinkId, nCurNodeId, fCurRouteId);
			printf("		nNextLinkId=%d, nNextNodeId=%d, fNextRouteId=%.4f,\n", nNextLinkId, nNextNodeId, fNextRouteId);
		#endif
			
			// 延伸
			vecExtendLinksId.push_back(nNextLinkId);
			vecExtendNodesId.push_back(nNextNodeId);
			bIsOn = true;
			break;
		}
	}

	// 递归
	int nRet = -1;
	if (bIsOn)
	{
		nRet = extendSameNameRoad(vecRoadNetLinkInfo, vecLinkEndPtnode, vecMainRoadLinkId, nNextLinkId, 
								nNextNodeId, rtScreen,	vecExtendLinksId, vecExtendNodesId);
		if (nRet<0)
		{
			return -1;
		}
	}
// 	else		// 终止条件
// 	{
// 		return 0;
// 	}	
	return 0;
}

// 延伸link直到超出屏幕边界
// vecNodeIdInNet - 路径Node Id
int MergeMapData::extendLink(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLink,
							 const vector<LinkEndPointNode> vecLinkEndPtnode,
							 int nCurEndPtSite,
							 cv::Rect rtScreen,
							 vector<HAMapPoint>& vecBorderPt,
							 vector<int>& vecBorderPtDirection,
							 vector<vector<int> >& vecPathLinkId,
							 vector<vector<int> >& vecPathNodeIdInNet)
{
	// 参数自检
	int nLinkNum = vecRoadNetLink.size();
	int nNodeNum = vecLinkEndPtnode.size();
	if (nLinkNum<=0 || nNodeNum<=0 || nCurEndPtSite<0 || nCurEndPtSite>=nNodeNum)
	{
	#ifdef _WINDOWS_VER_
		printf("==============extendLink - parameter Error!!==============\n");
	#else
		LOGD("==============extendLink - parameter Error!!==============\n");
	#endif
		return -1;
	}

	int nRet = 0;

	// 边界范围
	int nX1=rtScreen.x, nX2=nX1+rtScreen.width-1;
	int nY1=rtScreen.y, nY2=nY1+rtScreen.height-1;

	// 遍历，利用栈
	vector<int> vecNextNodeId;		// 下一个邻居节点的Id
	vector<int> vecNextLinkId;	// 当前节点与下一个邻居节点连接对应的link Id
	vector<int> vecCurNodeId;	// 记录当前节点Id
	//vector<HAMapPoint> vecCurPt;		// 当前端点
	LinkEndPointNode curNode = vecLinkEndPtnode[nCurEndPtSite];
	// 当前节点的邻居入栈
	for (int i=0; i<curNode.vecNeighborPoint.size(); i++)
	{
		vecNextNodeId.push_back(curNode.vecNeighborNodeId[i]);
		vecNextLinkId.push_back(curNode.vecLinkId[i]);
		vecCurNodeId.push_back(curNode.nNodeId);
		//vecCurPt.push_back(curNode.hamEndPoint);
	}

	vector<int> vecIsDo(nNodeNum,0);	// 0 - 未处理，1 - 处理
	vecIsDo[nCurEndPtSite] = 1;

	
	HAMapPoint hamCurPt = curNode.hamEndPoint;
	HAMapPoint hamNextPt;
	//std::vector<HAMapPoint> vecLink;
	LinkEndPointNode nextNode;

#ifdef _WINDOWS_VER_
#if IS_DRAW2	// 绘图
	Mat matTemp(m_matImage.rows,m_matImage.cols,CV_8UC3);
	//matTemp.setTo(0);
	matTemp = m_matImage.clone();	
	int offsetx =0,offsety = 0;
	offsetx = m_hamMainRoadCenter.x - matTemp.cols/2;
	offsety = m_hamMainRoadCenter.y - matTemp.rows/2;
	cv::Point ptOffset(offsetx,offsety);
	cv::Point ptTemp = cv::Point(hamCurPt.x, hamCurPt.y) - ptOffset;
	cv::circle( matTemp,ptTemp,2,cv::Scalar( 255, 255, 0),2,8);	
#endif
#endif

	vector<int> vecPath;		// 记录路径，元素为link Id
	vector<int> vecNodeId;		// 记录路径，元素Node Id
	
	int nFirstNodeId = curNode.nNodeId;
	vecNodeId.push_back(nFirstNodeId);
	int nCurDirection=0, nNextDirection=0;
	while (vecNextNodeId.size()>0)
	{
		// 获取栈中最后一个元素
		int nNextNodeId = vecNextNodeId[vecNextNodeId.size()-1];
		int nLinkId = vecNextLinkId[vecNextLinkId.size()-1];
		int nCurNodeId = vecCurNodeId[vecCurNodeId.size()-1];
		vecIsDo[nNextNodeId] = 1;		// 标识已处理

		vecPath.push_back(nLinkId);		// 路径
		vecNodeId.push_back(nNextNodeId);

		// 出栈
		vecNextNodeId.erase(vecNextNodeId.end()-1);
		vecNextLinkId.erase(vecNextLinkId.end()-1);
		vecCurNodeId.erase(vecCurNodeId.end()-1);

		// 当前节点
		curNode = vecLinkEndPtnode[nCurNodeId];

		// 邻居节点
		nextNode = vecLinkEndPtnode[nNextNodeId];

		// 邻居端点
		hamNextPt = nextNode.hamEndPoint;
		
		// 判断当前点和下一个点是否为同一个点
		if (curNode.hamEndPoint.x==hamNextPt.x && curNode.hamEndPoint.y==hamNextPt.y)
		{
			vecPath.erase(vecPath.end()-1);
			vecNodeId.erase(vecNodeId.end()-1);
			//nCurDirection = 0;
			continue;
		}

		// 获取方向
		vector<int>::iterator iter = std::find(curNode.vecNeighborNodeId.begin(),
			curNode.vecNeighborNodeId.end(),nNextNodeId);	//返回的是一个迭代器指针
		if (iter==curNode.vecNeighborNodeId.end())	// 不重复
		{
			vecPath.erase(vecPath.end()-1);
			vecNodeId.erase(vecNodeId.end()-1);
			//nCurDirection = 0;
			continue;
		}

		int nSi = std::distance(curNode.vecNeighborNodeId.begin(), iter);			
		int nDirection = curNode.vecDirection[nSi];		// 方向
		if (nCurDirection<=1 || nCurDirection==nDirection || nDirection<=1)
		{
			nNextDirection = nDirection;
		}
		else
		{			
			vecPath.erase(vecPath.end()-1);
			vecNodeId.erase(vecNodeId.end()-1);
			//nCurDirection = 0;
			continue;
		}
		

		if (hamNextPt.x<=nX1 || hamNextPt.x>= nX2 ||
			hamNextPt.y<=nY1 || hamNextPt.y>=nY2)
		{			
			// 计算link内部点按序连成的折线与矩形框边界的交点
			HAMapPoint hamCrossPt;
			nRet = getCrossPointLink2Rect(vecRoadNetLink[nLinkId], rtScreen, hamCrossPt);
			if (nRet<0)
			{
				vecPath.erase(vecPath.end()-1);
				vecNodeId.erase(vecNodeId.end()-1);
				nCurDirection = 0;
				continue;
			}						

			HAMapPoint hamPtTemp = hamCrossPt;				

			vecBorderPt.push_back(hamPtTemp);		// 插入边界点			

			vecBorderPtDirection.push_back(nNextDirection/*nDirection*/);		// 插入方向

			// 记录路径
			vecPathLinkId.push_back(vecPath);
			vecPathNodeIdInNet.push_back(vecNodeId);
			//vecPath.clear();
			//vecNodeId.clear();
			//vecNodeId.push_back(nFirstNodeId);

			// 对保持的路径做出栈处理
			vecPath.erase(vecPath.end()-1);
			vecNodeId.erase(vecNodeId.end()-1);
			while (vecNodeId.size()>0 && vecPath.size()>0)
			{
				int nNodeIdTemp = vecNodeId[vecNodeId.size()-1];
				LinkEndPointNode nodeTemp = vecLinkEndPtnode[nNodeIdTemp];
				int nNeighborNumTemp = nodeTemp.vecNeighborNodeId.size();
				bool bIsOut = true;
				for (int i=0; i<nNeighborNumTemp; i++)
				{
					int nNeigborId = nodeTemp.vecNeighborNodeId[i];
					LinkEndPointNode nextTempNode = vecLinkEndPtnode[nNodeIdTemp];
					if (vecIsDo[nNeigborId]==0 && 
						isRectInside(nextTempNode.hamEndPoint,rtScreen) && 
						(nextTempNode.vecDirection[i]<=1 || nextTempNode.vecDirection[i]==nNextDirection))
					{
						bIsOut = false;
						break;
					}
				}	// end for
				if (bIsOut)
				{
					vecPath.erase(vecPath.end()-1);
					vecNodeId.erase(vecNodeId.end()-1);					
				}
				else
				{
					break;
				}
			}	// end while			

			nCurDirection = 0;

#ifdef _WINDOWS_VER_
	#if IS_DRAW2	// 绘图	
			cv::Point ptTemp = cv::Point(hamNextPt.x, hamNextPt.y) - ptOffset;
			cv::circle( matTemp,ptTemp,2,cv::Scalar( 255, 255, 0),1,8);	
			cv::Point ptPre = cv::Point(hamCurPt.x, hamCurPt.y) - ptOffset;
			cv::Point ptNext = cv::Point(hamNextPt.x, hamNextPt.y) - ptOffset;
			Scalar colorMainRoad(0,128,0);			
			line(matTemp,ptPre,ptNext,colorMainRoad,1);
		#if IS_SON_DRAW
			imshow("extend",matTemp);
			waitKey(0);
		#endif
	#endif
#endif
			continue;
		}			

		// 入栈，通过方向判断
		for (int i=0; i<nextNode.vecNeighborNodeId.size(); i++)
		{
			int nNodeId = nextNode.vecNeighborNodeId[i];
			int nDirectionTemp = nextNode.vecDirection[i];
			int nLinkIdTemp = nextNode.vecLinkId[i];
			if (vecIsDo[nNodeId]==0 && ((nDirectionTemp<=1)||
				(nNextDirection<=1)||(nDirectionTemp==nNextDirection/*nDirection*/)))
			{				
				vecNextNodeId.push_back(nNodeId);
				vecNextLinkId.push_back(nLinkIdTemp);
				vecCurNodeId.push_back(nextNode.nNodeId);
			}
		}	// end for

#ifdef _WINDOWS_VER_
#if IS_DRAW2	// 绘图	
		cv::Point ptTemp = cv::Point(hamNextPt.x, hamNextPt.y) - ptOffset;
		cv::circle( matTemp,ptTemp,2,cv::Scalar( 255, 255, 0),1,8);	
		cv::Point ptPre = cv::Point(hamCurPt.x, hamCurPt.y) - ptOffset;
		cv::Point ptNext = cv::Point(hamNextPt.x, hamNextPt.y) - ptOffset;
		Scalar colorMainRoad(0,128,0);
		line(matTemp,ptPre,ptNext,colorMainRoad,1);	
	#if IS_SON_DRAW
		imshow("extend",matTemp);
		waitKey(0);
	#endif
		
#endif
#endif

		// 迭代
// 		hamCurPt = hamNextPt;
// 		curNode = nextNode;
		nCurDirection = nNextDirection;
	}	// end while

	return 0;
}


int MergeMapData::extendLink1(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLink,
							 const vector<LinkEndPointNode> vecLinkEndPtnode,
							 int nCurEndPtSite,
							 cv::Rect rtScreen,
							 vector<HAMapPoint>& vecBorderPt,
							 vector<int>& vecBorderPtDirection,
							 vector<vector<int> >& vecPathLinkIdInNet,
							 vector<vector<int> >& vecPathNodeIdInNet)
{
	// 参数自检
	int nLinkNum = vecRoadNetLink.size();
	int nNodeNum = vecLinkEndPtnode.size();
	if (nLinkNum<=0 || nNodeNum<=0 || nCurEndPtSite<0 || nCurEndPtSite>=nNodeNum)
	{
	#ifdef _WINDOWS_VER_
		printf("==============extendLink1 - parameter Error!!==============\n");
	#else
		LOGD("==============extendLink1 - parameter Error!!==============\n");
	#endif
		return -1;
	}

	int nRet = 0;
	
	// 获取指向中心点的边界点及路径，即入度
	vector<HAMapPoint> vecIntoBorderPt;
	vector<vector<int> > vecIntoPathLinkIdInNet;
	vector<vector<int> > vecIntoPathNodeIdInNet;
	nRet = traverseMap1(vecRoadNetLink, vecLinkEndPtnode, nCurEndPtSite,	rtScreen,
						false, vecIntoBorderPt,	vecIntoPathLinkIdInNet,	vecIntoPathNodeIdInNet);
	if (nRet<0)
	{
		return -1;
	}

	// 获取中心点指向的边界点及路径，即出度
	vector<HAMapPoint> vecOutBorderPt;
	vector<vector<int> > vecOutPathLinkIdInNet;
	vector<vector<int> > vecOutPathNodeIdInNet;
	nRet = traverseMap1(vecRoadNetLink, vecLinkEndPtnode, nCurEndPtSite,	rtScreen,
					true, vecOutBorderPt,	vecOutPathLinkIdInNet,	vecOutPathNodeIdInNet);
	if (nRet<0)
	{
		return -1;
	}

	// 输出
	vecBorderPt.insert(vecBorderPt.end(),vecIntoBorderPt.begin(),vecIntoBorderPt.end());
	vecBorderPt.insert(vecBorderPt.end(),vecOutBorderPt.begin(),vecOutBorderPt.end());
	vector<int> vecIntoBorderPtDirection(vecIntoBorderPt.size(),OPPOSITE_DIRECTION);
	vector<int> vecOutBorderPtDirection(vecOutBorderPt.size(),SAME_DIRECTION);
	vecBorderPtDirection.insert(vecBorderPtDirection.end(),vecIntoBorderPtDirection.begin(),vecIntoBorderPtDirection.end());
	vecBorderPtDirection.insert(vecBorderPtDirection.end(),vecOutBorderPtDirection.begin(),vecOutBorderPtDirection.end());
	vecPathLinkIdInNet.insert(vecPathLinkIdInNet.end(),vecIntoPathLinkIdInNet.begin(),vecIntoPathLinkIdInNet.end());
	vecPathLinkIdInNet.insert(vecPathLinkIdInNet.end(),vecOutPathLinkIdInNet.begin(),vecOutPathLinkIdInNet.end());		
	vecPathNodeIdInNet.insert(vecPathNodeIdInNet.end(),vecIntoPathNodeIdInNet.begin(),vecIntoPathNodeIdInNet.end());
	vecPathNodeIdInNet.insert(vecPathNodeIdInNet.end(),vecOutPathNodeIdInNet.begin(),vecOutPathNodeIdInNet.end());

	return 0;
}


//int MergeMapData::extendLink2(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLink,
//							  const vector<LinkEndPointNode> vecLinkEndPtnode,
//							  int nCurEndPtSite,
//							  cv::Rect rtScreen,
//							  vector<HAMapPoint>& vecBorderPt,
//							  vector<int>& vecBorderPtDirection,
//							  vector<vector<int> >& vecPathLinkIdInNet,
//							  vector<vector<int> >& vecPathNodeIdInNet)
//{
//	// 参数自检
//	int nLinkNum = vecRoadNetLink.size();
//	int nNodeNum = vecLinkEndPtnode.size();
//	if (nLinkNum<=0 || nNodeNum<=0 || nCurEndPtSite<0 || nCurEndPtSite>=nNodeNum)
//	{
//#ifdef _WINDOWS_VER_
//		printf("==============extendLink1 - parameter Error!!==============\n");
//#else
//		LOGD("==============extendLink1 - parameter Error!!==============\n");
//#endif
//		return -1;
//	}
//
//	int nRet = 0;
//
//	// 获取指向中心点的边界点及路径，即入度
//	vector<HAMapPoint> vecIntoBorderPt;
//	vector<vector<int> > vecIntoPathLinkIdInNet;
//	vector<vector<int> > vecIntoPathNodeIdInNet;
//	nRet = traverseMap1(vecRoadNetLink, vecLinkEndPtnode, nCurEndPtSite,	rtScreen,
//		false, vecIntoBorderPt,	vecIntoPathLinkIdInNet,	vecIntoPathNodeIdInNet);
//	if (nRet<0)
//	{
//		return -1;
//	}
//
//	// 获取中心点指向的边界点及路径，即出度
//	vector<HAMapPoint> vecOutBorderPt;
//	vector<vector<int> > vecOutPathLinkIdInNet;
//	vector<vector<int> > vecOutPathNodeIdInNet;
//	nRet = traverseMap1(vecRoadNetLink, vecLinkEndPtnode, nCurEndPtSite,	rtScreen,
//		true, vecOutBorderPt,	vecOutPathLinkIdInNet,	vecOutPathNodeIdInNet);
//	if (nRet<0)
//	{
//		return -1;
//	}
//
//	// 输出
//	vecBorderPt.insert(vecBorderPt.end(),vecIntoBorderPt.begin(),vecIntoBorderPt.end());
//	vecBorderPt.insert(vecBorderPt.end(),vecOutBorderPt.begin(),vecOutBorderPt.end());
//	vector<int> vecIntoBorderPtDirection(vecIntoBorderPt.size(),OPPOSITE_DIRECTION);
//	vector<int> vecOutBorderPtDirection(vecOutBorderPt.size(),SAME_DIRECTION);
//	vecBorderPtDirection.insert(vecBorderPtDirection.end(),vecIntoBorderPtDirection.begin(),vecIntoBorderPtDirection.end());
//	vecBorderPtDirection.insert(vecBorderPtDirection.end(),vecOutBorderPtDirection.begin(),vecOutBorderPtDirection.end());
//	vecPathLinkIdInNet.insert(vecPathLinkIdInNet.end(),vecIntoPathLinkIdInNet.begin(),vecIntoPathLinkIdInNet.end());
//	vecPathLinkIdInNet.insert(vecPathLinkIdInNet.end(),vecOutPathLinkIdInNet.begin(),vecOutPathLinkIdInNet.end());		
//	vecPathNodeIdInNet.insert(vecPathNodeIdInNet.end(),vecIntoPathNodeIdInNet.begin(),vecIntoPathNodeIdInNet.end());
//	vecPathNodeIdInNet.insert(vecPathNodeIdInNet.end(),vecOutPathNodeIdInNet.begin(),vecOutPathNodeIdInNet.end());
//
//	return 0;
//}


// 按顺序计算vector内部相邻点间的距离和
int MergeMapData::getDistanceInsideVector(const vector<HAMapPoint>& vecHamPt, double& uDis)
{
	// 参数自检
	int nNumPt = vecHamPt.size();
	if (nNumPt<=1)
	{
		return -1;
	}

	uDis = 0;
	HAMapPoint hamPtPre = vecHamPt[0];
	for (int i=0; i<nNumPt; i++)
	{
		HAMapPoint hamPtCur = vecHamPt[i];
		float fTempDis = getDistancePoint2Point(hamPtPre.x, hamPtPre.y, hamPtCur.x, hamPtCur.y);
		uDis += fTempDis;
		hamPtPre = hamPtCur;
	}

	return 0;
}

// 获取边界点到匹配点的距离
// vecPathLinkId保证顺序，从匹配点到边界点
int MergeMapData::getDisBorder2MatchPt(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLink,
									   const vector<int>& vecPathLinkId, 
									   cv::Rect rtScreen,
									   HAMapPoint hamBorderPt, 
									   HAMapPoint hamMatchPt, 
									   double& uDis)
{
	// 参数自检
	int nNumLink = vecRoadNetLink.size();
	int nNumPathLink = vecPathLinkId.size();
	if (nNumLink<=0 || nNumPathLink<=0 || nNumLink<nNumPathLink)
	{
		return -1;
	}
	
	if ((hamMatchPt.x!=vecRoadNetLink[vecPathLinkId[0]][0].x || hamMatchPt.y!=vecRoadNetLink[vecPathLinkId[0]][0].y) &&
		(hamMatchPt.x!=vecRoadNetLink[vecPathLinkId[0]][vecRoadNetLink[vecPathLinkId[0]].size()-1].x ||
		hamMatchPt.y!=vecRoadNetLink[vecPathLinkId[0]][vecRoadNetLink[vecPathLinkId[0]].size()-1].y))
	{
		return -1;
	}

	int nRet = 0;

	uDis = -1.0;
	for (int i=0; i<nNumPathLink-1; i++)
	{
		vector<HAMapPoint> vecHamPt = vecRoadNetLink[vecPathLinkId[i]];
		double uDisTemp;
		nRet = getDistanceInsideVector(vecHamPt, uDisTemp);
		if (nRet<0)
		{
			return -1;
		}
		uDis += uDisTemp;		
	}

	// 最后一个link点
	vector<HAMapPoint> vecHamPt = vecRoadNetLink[vecPathLinkId[nNumPathLink-1]];
	HAMapPoint hamPrePt = vecHamPt[0];
	for (int i=1; i<vecHamPt.size(); i++)
	{
		HAMapPoint hamCurPt = vecHamPt[i];
		float fDisTemp = 0;
		if (isRectInside(hamCurPt,rtScreen))
		{
			fDisTemp = getDistancePoint2Point(hamPrePt.x,hamPrePt.y,hamCurPt.x,hamCurPt.y);
			uDis += fDisTemp;			
		}
		else
		{
			fDisTemp = getDistancePoint2Point(hamBorderPt.x,hamBorderPt.y,hamCurPt.x,hamCurPt.y);
			uDis += fDisTemp;
			break;
		}
		hamPrePt = hamCurPt;
	}

	if (uDis<0)
	{
		return -1;
	}

	return 0;
}

// 遍历图，获取边界点
// bool bIsFrontTrave - 表示遍历方向，true - 朝正方向遍历，false - 朝反方向遍历
//int g_nSav = 0;
int MergeMapData::traverseMap1(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLink,
							  const vector<LinkEndPointNode> vecLinkEndPtnode,
							  int nCurEndPtSite,
							  cv::Rect rtScreen,
							  bool bIsFrontTrave,
							  vector<HAMapPoint>& vecBorderPt,							  
							  vector<vector<int> >& vecPathLinkIdInNet,
							  vector<vector<int> >& vecPathNodeIdInNet)
{
	int nLinkNum = vecRoadNetLink.size();
	int nNodeNum = vecLinkEndPtnode.size();
	if (nLinkNum<=0 || nNodeNum<=0 || nCurEndPtSite<0 || nCurEndPtSite>=nNodeNum)
	{
	#ifdef _WINDOWS_VER_
		printf("==============traverseMap - parameter Error!!==============\n");
	#else
		LOGD("==============traverseMap - parameter Error!!==============\n");
	#endif
		return -1;
	}

	// 绘制节点图
#ifdef _WINDOWS_VER_
	#if IS_DRAW_NODE
		cv::Mat matImg = m_matImage.clone();
		drawNode(matImg, vecLinkEndPtnode, vecRoadNetLink);
		Point ptCurPt = Point(vecLinkEndPtnode[nCurEndPtSite].hamEndPoint.x,vecLinkEndPtnode[nCurEndPtSite].hamEndPoint.y)-m_ptOffset;
		cv::circle( matImg,ptCurPt,3,cv::Scalar( 0, 255, 0),-1,8);		
		cv::namedWindow("matImg",WINDOW_AUTOSIZE);
		cv::imshow("matImg",matImg);
		cv::waitKey(0);

/* 		char chSaveNum[20];
 		itoa(g_nSav++,chSaveNum,10);
 		string str = "D:\\Halo\\ArWay\\output\\gimage\\roadNet\\" + string(chSaveNum) + "_1.bmp";
 		cv::imwrite(str,matImg);*/		
	#endif
#endif

	int nRet = 0;

	// 根据遍历方向，判断不能等的方向
	int nNotEqualDirection = (bIsFrontTrave)?OPPOSITE_DIRECTION:SAME_DIRECTION;

	// 利用栈遍历
	vector<int> vecPathNodeId;		// 记录路径迭代中每层的Node Id
	vector<int> vecNodeLoopId;		// 记录栈中的Node Id
	vector<int> vecNodeLoopLayer;		// 记录栈中的Node的层次信息
	
	vector<int> vecBorderPointLinkId;	// 记录边界点对应的Link Id

	// 深度遍历
	int nCurNodeId = nCurEndPtSite;		// 记录当前Node的Id
	int nCurLinkId = -1;				// 记录当前Link Id
	int nCurLayer = 0;					// 记录当前Node在栈中的层次
	LinkEndPointNode curNode;			// 记录当前Nodes	
	vecNodeLoopId.push_back(nCurNodeId);
	vecNodeLoopLayer.push_back(0);		// 第0层，对应最顶层
	bool bIsFirst = true;
	while (vecNodeLoopId.size()>0)
	{
		// 当前节点
		nCurNodeId = vecNodeLoopId[vecNodeLoopId.size()-1];
		curNode = vecLinkEndPtnode[nCurNodeId];		// 当前Nodes
		nCurLayer = vecNodeLoopLayer[vecNodeLoopLayer.size()-1];		// 层次
		
		// 迭代路径
		if (vecPathNodeId.size()<(nCurLayer+1))
		{
			vecPathNodeId.resize(nCurLayer+1);			
		}
		vecPathNodeId[nCurLayer] = nCurNodeId;
		
		// 出栈
		vecNodeLoopId.erase(vecNodeLoopId.end()-1);
		vecNodeLoopLayer.erase(vecNodeLoopLayer.end()-1);

		// 判断是否是边界
		HAMapPoint hamCurPoint = curNode.hamEndPoint;
		if (!isRectInside(hamCurPoint,rtScreen) && nCurLayer>0)
		{
			// 查找当前Link Id
			int nPreNodeId = vecPathNodeId[nCurLayer-1];
			HAMapPoint hamPrePoint = vecLinkEndPtnode[nPreNodeId].hamEndPoint;
			nRet = getLinkInfo(vecRoadNetLink, hamPrePoint, hamCurPoint, nCurLinkId);
			if (nRet<0)
			{
				continue;
			}

			int nSi = 0;
			if (isBelongToVector(vecBorderPointLinkId,nCurLinkId,nSi))
			{
				// 求长度
				vector<int> vecPathLinkTemp;				
				nRet = getPathLinkId(vecLinkEndPtnode,	vecPathNodeIdInNet[nSi], vecPathLinkTemp);
				if (nRet<0)
				{
					continue;
				}
				double uPathDis0;
				nRet = getDisBorder2MatchPt(vecRoadNetLink, vecPathLinkTemp, rtScreen,
					vecBorderPt[nSi], vecLinkEndPtnode[nCurEndPtSite].hamEndPoint, uPathDis0);
				if (nRet<0)
				{
					continue;
				}

				vector<int> vecCurPathNode;
				vecCurPathNode.insert(vecCurPathNode.begin(),vecPathNodeId.begin(),vecPathNodeId.begin()+nCurLayer+1);
				vecPathLinkTemp.clear();				
				nRet = getPathLinkId(vecLinkEndPtnode,	vecCurPathNode, vecPathLinkTemp);
				if (nRet<0)
				{
					continue;
				}
				double uPathDis1;
				nRet = getDisBorder2MatchPt(vecRoadNetLink, vecPathLinkTemp, rtScreen,
					vecBorderPt[nSi], vecLinkEndPtnode[nCurEndPtSite].hamEndPoint, uPathDis1);
				if (nRet<0)
				{
					continue;
				}
				
				if (uPathDis1<uPathDis0/*vecPathNodeIdInNet[nSi].size()>=(nCurLayer+1)*/)		// 判断是否是最短路径，根据路径实际长度
				{
					// 更新
					vecPathNodeIdInNet[nSi].clear();
					vecPathNodeIdInNet[nSi].insert(vecPathNodeIdInNet[nSi].end(),vecPathNodeId.begin(),vecPathNodeId.begin()+nCurLayer+1);					
				}
			}
			else
			{
				// 计算link内部点按序连成的折线与矩形框边界的交点
				HAMapPoint hamCrossPt;
				nRet = getCrossPointLink2Rect(vecRoadNetLink[nCurLinkId], rtScreen, hamCrossPt);
				if (nRet==0)
				{
					vecBorderPt.push_back(hamCrossPt);		// 插入边界点
					vecBorderPointLinkId.push_back(nCurLinkId);		// 插入边界点对应的Link Id

					// 添加路径
					vecPathNodeIdInNet.resize(vecPathNodeIdInNet.size()+1);
					vecPathNodeIdInNet[vecPathNodeIdInNet.size()-1].insert(vecPathNodeIdInNet[vecPathNodeIdInNet.size()-1].end(),
													vecPathNodeId.begin(),vecPathNodeId.begin()+nCurLayer+1);
				}
			}			
			continue;
		}	// end if

		//// 判断是否已到达路径的尽头
		//if (curNode.vecNeighborNodeId.size()<=1)
		//{
		//	// 出栈，用于记录到达边界点的路径			
		//	
		//	continue;
		//}


		// 邻居入栈，用于找边界点	
		int nSi = 0;
		vector<int> vecCurPath;
		vecCurPath.insert(vecCurPath.begin(),vecPathNodeId.begin(),vecPathNodeId.begin()+nCurLayer+1);
		for (int i=0; i<curNode.vecNeighborNodeId.size(); i++)
		{
			int nSi = 0;
			if ((curNode.vecDirection[i]!=nNotEqualDirection) && 
				!isBelongToVector(vecCurPath,curNode.vecNeighborNodeId[i],nSi))	// 判断方向且不存在回圈
			{
				vecNodeLoopId.push_back(curNode.vecNeighborNodeId[i]);
				vecNodeLoopLayer.push_back(nCurLayer+1);				
			}
		}	// end for		

	}	// end while

	// 获取每条路径的link Id
	vecPathLinkIdInNet.resize(vecPathNodeIdInNet.size());
	for (int i=0; i<vecPathNodeIdInNet.size(); i++)
	{
		nRet = getPathLinkId(vecLinkEndPtnode,	vecPathNodeIdInNet[i], vecPathLinkIdInNet[i]);
		if (nRet<0)
		{
			return -1;
		}
	}	

	return 0;
}

// 遍历图，获取边界点
// bool bIsFrontTrave - 表示遍历方向，true - 朝正方向遍历，false - 朝反方向遍历
int MergeMapData::traverseMap(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLink,
							  const vector<LinkEndPointNode> vecLinkEndPtnode,
							  int nCurEndPtSite,
							  cv::Rect rtScreen,
							  bool bIsFrontTrave,
							  vector<HAMapPoint>& vecBorderPt,							  
							  vector<vector<int> >& vecPathLinkIdInNet,
							  vector<vector<int> >& vecPathNodeIdInNet)
{
	int nLinkNum = vecRoadNetLink.size();
	int nNodeNum = vecLinkEndPtnode.size();
	if (nLinkNum<=0 || nNodeNum<=0 || nCurEndPtSite<0 || nCurEndPtSite>=nNodeNum)
	{
#ifdef _WINDOWS_VER_
		printf("==============traverseMap - parameter Error!!==============\n");
#else
		LOGD("==============traverseMap - parameter Error!!==============\n");
#endif
		return -1;
	}

	// 绘制节点图
#ifdef _WINDOWS_VER_
#if IS_DRAW_NODE
	cv::Mat matImg = m_matImage.clone();
	drawNode(matImg, vecLinkEndPtnode, vecRoadNetLink);
	Point ptCurPt = Point(vecLinkEndPtnode[nCurEndPtSite].hamEndPoint.x,vecLinkEndPtnode[nCurEndPtSite].hamEndPoint.y)-m_ptOffset;
	cv::circle( matImg,ptCurPt,3,cv::Scalar( 0, 255, 0),-1,8);		
	cv::imshow("matImg",matImg);
	cv::waitKey(0);
#endif
#endif

	int nRet = 0;

	// 边界范围
	int nX1=rtScreen.x, nX2=nX1+rtScreen.width-1;
	int nY1=rtScreen.y, nY2=nY1+rtScreen.height-1;

	// 根据遍历方向，判断不能等的方向
	int nNotEqualDirection = (bIsFrontTrave)?OPPOSITE_DIRECTION:SAME_DIRECTION;

	// 利用栈遍历
	vector<int> vecPathLinkId, vecPathNodeId;		// 记录起点到终点的路径Link Id、Node Id
	vector<int> vecNeighborLinkId;	// 记录当前Node到邻居Node的Link Id
	vector<int> vecNodeLoopId;		// 记录循环中的Node Id
	vector<int> vecBorderPointLinkId;	// 记录边界点对应的Link Id，用于取最短路径
	//vector<int> vecIsDo(nNodeNum,0);	// 记录Node是否经过处理，0 - 未处理，1 - 处理
	vector<vector<int> > vecIsDo(nNodeNum);	// 记录Node对应的Link是否经过处理
	//vecIsDo[nCurEndPtSite] = 1;

	// 深度遍历
	int nCurNodeId = nCurEndPtSite, nPreNodeId = nCurNodeId;		// 记录当前Node和前一个Node的Id	
	int nCurLinkId = -1;	// 记录当前Link的Id，即连接当前Node与前一个Node的Link Id
	LinkEndPointNode curNode = vecLinkEndPtnode[nCurNodeId];		// 当前Nodes
	//vecPathNodeId.push_back(nCurNodeId);
	//vecPathLinkIdInNet.push_back(-1);	// -1  表示此时没有link，即路径中只有一个顶点
	vecNodeLoopId.push_back(nCurNodeId);
	vecNeighborLinkId.push_back(-1);	// -1  表示此时没有link，即遍历刚开始
	bool bIsFirst = true;
	while (vecNodeLoopId.size()>0)
	{
		// 当前节点
		nCurNodeId = vecNodeLoopId[vecNodeLoopId.size()-1];
		curNode = vecLinkEndPtnode[nCurNodeId];		// 当前Nodes

		// 迭代替换
		if (bIsFirst)
		{
			nCurLinkId = -1;		// 路径中只有一个点，此时无link，用-1表示
			bIsFirst = false;
		}
		else
		{
			nCurLinkId = vecNeighborLinkId[vecNeighborLinkId.size()-1];
			int nTempId = vecPathNodeId[vecPathNodeId.size()-1];
			vecIsDo[nTempId].push_back(nCurLinkId);		// 标识已处理
		}

		// 出栈，用于找边界点	
		vecNodeLoopId.erase(vecNodeLoopId.end()-1);
		vecNeighborLinkId.erase(vecNeighborLinkId.end()-1);

		//vecIsDo[nCurNodeId] = 1;		// 标识已处理


		// 路径入栈，用于记录到达边界点的路径
		vecPathNodeId.push_back(nCurNodeId);
		vecPathLinkId.push_back(nCurLinkId);

		// 判断是否是边界
		HAMapPoint hamCurPoint = curNode.hamEndPoint;
		// 		if (hamCurPoint.x<=nX1 || hamCurPoint.x>= nX2 ||
		// 			hamCurPoint.y<=nY1 || hamCurPoint.y>=nY2)
		if (!isRectInside(hamCurPoint,rtScreen))
		{
			// 去掉为-1的linkId
			vector<int> vecTempLinkId = vecPathLinkId;
			if (vecPathLinkId[0]<0)		// 针对第0个特殊处理
			{
				vecTempLinkId.erase(vecTempLinkId.begin());
			}

			if (vecTempLinkId.size()<=0)
			{
				continue;
			}

			int nSi = 0;
			if (isBelongToVector(vecBorderPointLinkId,nCurLinkId,nSi))
			{
				if (vecPathNodeIdInNet[nSi].size()>=vecPathNodeId.size())		// 判断是否是最短路径
				{
					// 更新
					vecPathNodeIdInNet[nSi] = vecPathNodeId;
					vecPathLinkIdInNet[nSi] = vecTempLinkId/*vecPathLinkId*/;
				}
			}
			else
			{
				// 计算link内部点按序连成的折线与矩形框边界的交点
				HAMapPoint hamCrossPt;
				nRet = getCrossPointLink2Rect(vecRoadNetLink[nCurLinkId], rtScreen, hamCrossPt);
				if (nRet==0)
				{
					vecBorderPt.push_back(hamCrossPt);		// 插入边界点
					vecBorderPointLinkId.push_back(nCurLinkId);		// 插入边界点对应的Link Id

					// 添加路径
					vecPathLinkIdInNet.push_back(vecTempLinkId/*vecPathLinkId*/);
					vecPathNodeIdInNet.push_back(vecPathNodeId);
				}
			}

			// 出栈，用于记录到达边界点的路径			
			nRet = popPath(vecRoadNetLink, vecLinkEndPtnode, vecIsDo, rtScreen,bIsFrontTrave, vecPathNodeId, vecPathLinkId);
			if (nRet<0)
			{
				//continue;
				return -1;
			}
			continue;
		}	// end if

		// 判断是否已到达路径的尽头
		if (curNode.vecNeighborNodeId.size()<=1)
		{
			// 出栈，用于记录到达边界点的路径			
			nRet = popPath(vecRoadNetLink, vecLinkEndPtnode, vecIsDo, rtScreen, bIsFrontTrave, vecPathNodeId, vecPathLinkId);
			if (nRet<0)
			{
				//continue;
				return -1;
			}
			continue;
		}


		// 邻居入栈，用于找边界点	
		int nSi = 0;
		for (int i=0; i<curNode.vecNeighborNodeId.size(); i++)
		{
			if (curNode.vecDirection[i]!=nNotEqualDirection && 
				!isBelongToVector(vecPathNodeId, curNode.vecNeighborNodeId[i], nSi))	// 判断方向且不存在回圈
			{
				vecNodeLoopId.push_back(curNode.vecNeighborNodeId[i]);
				vecNeighborLinkId.push_back(curNode.vecLinkId[i]);
			}
		}	// end for	

		nPreNodeId = nCurNodeId;

	}	// end while

	return 0;
}

// 基于两个端点，查找Link Id
int MergeMapData::getLinkInfo(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLink,
							  HAMapPoint hamEndPt1, HAMapPoint hamEndPt2,
							  int& nLinkId)
{
	nLinkId = -1;

	int nLinkNum = vecRoadNetLink.size();
	for (int i=0; i<nLinkNum; i++)
	{
		vector<HAMapPoint> vecTemp = vecRoadNetLink[i];

		if ((hamEndPt1==vecTemp[0]&&hamEndPt2==vecTemp[vecTemp.size()-1]) ||
			(hamEndPt2==vecTemp[0]&&hamEndPt1==vecTemp[vecTemp.size()-1]))
		{
			nLinkId = i;
			break;
		}
	}

	if (nLinkId<0)
	{
		return -1;
	}
	return 0;
}

// 基于路径的Node Id，获取路径的Link Id
int MergeMapData::getPathLinkId(const vector<LinkEndPointNode> vecLinkEndPtnode,
								const vector<int>& vecPathNodeId,
								vector<int>& vecPathLinkId)
{
	// 参数自检
	int nNodeNum = vecLinkEndPtnode.size();
	int nPathNodeNum = vecPathNodeId.size();
	if (nNodeNum<=0 || nPathNodeNum<=1)
	{
		return -1;
	}

	LinkEndPointNode preNode, curNode;
	preNode = vecLinkEndPtnode[vecPathNodeId[0]];	
	for (int i=1; i<nPathNodeNum; i++)
	{
		curNode = vecLinkEndPtnode[vecPathNodeId[i]];

		int nSi = -1;
		for (int j=0; j<curNode.vecNeighborPoint.size(); j++)
		{
			if ((curNode.vecNeighborPoint[j].x==preNode.hamEndPoint.x) &&
				(curNode.vecNeighborPoint[j].y==preNode.hamEndPoint.y))
			{
				nSi = j;
				break;
			}
		}
		if (nSi>=0 && nSi<curNode.vecNeighborPoint.size())
		{
			int nLinkId = curNode.vecLinkId[nSi];
			vecPathLinkId.push_back(nLinkId);
		}
		else
		{
			return -1;
		}

		// 迭代
		preNode = curNode;
	}


	return 0;
}

// 路径回溯
int MergeMapData::popPath(const std::vector<std::vector<HAMapPoint> >& vecRoadNetLink,
						  const vector<LinkEndPointNode> vecLinkEndPtnode,
						  const vector<vector<int> >& vecIsNodeDo, cv::Rect rtScreen, bool bIsFrontTrave,
						  vector<int>& vecPathNodeId, vector<int>& vecPathLinkId)
{
	// 参数自检
	int nLinkNum = vecRoadNetLink.size();
	int nNodeNum = vecLinkEndPtnode.size();
	int nPathLinkNum = vecPathLinkId.size();
	if (nLinkNum<=0 || nNodeNum<=0 || vecIsNodeDo.size()!=nNodeNum ||
		nPathLinkNum<0 || nPathLinkNum!=vecPathNodeId.size())
	{
	#ifdef _WINDOWS_VER_
		printf("==============popPath - parameter Error!!==============\n");
	#else
		LOGD("==============popPath - parameter Error!!==============\n");
	#endif
		return -1;
	}

	// 根据遍历方向，判断不能等的方向
	int nNotEqualDirection = (bIsFrontTrave)?OPPOSITE_DIRECTION:SAME_DIRECTION;

	// 出栈，一直退到存在孩子未遍历的节点为止
	vecPathLinkId.erase(vecPathLinkId.end()-1);
	vecPathNodeId.erase(vecPathNodeId.end()-1);		
	while (vecPathNodeId.size()>0 && vecPathLinkId.size()>0)
	{
		int nNodeIdTemp = vecPathNodeId[vecPathNodeId.size()-1];
		LinkEndPointNode nodeTemp = vecLinkEndPtnode[nNodeIdTemp];
		int nNeighborNumTemp = nodeTemp.vecNeighborNodeId.size();
		bool bIsOut = true;
		for (int i=0; i<nNeighborNumTemp; i++)
		{
			int nNeigborId = nodeTemp.vecNeighborNodeId[i];
			LinkEndPointNode nextTempNode = vecLinkEndPtnode[nNodeIdTemp];
			//LinkEndPointNode nextTempNode = vecLinkEndPtnode[nNeigborId];
			int nSi = 0;
			if (/*vecIsNodeDo[nNeigborId]==0*/ !isBelongToVector(vecIsNodeDo[nNodeIdTemp], nodeTemp.vecLinkId[i], nSi) && 
				!isBelongToVector(vecPathNodeId,nNeigborId,nSi) &&
				isRectInside(nextTempNode.hamEndPoint,rtScreen) && 
				(nextTempNode.vecDirection[i]!=nNotEqualDirection))
			{
				bIsOut = false;
				break;
			}
		}	// end for
		if (bIsOut)
		{
			vecPathLinkId.erase(vecPathLinkId.end()-1);
			vecPathNodeId.erase(vecPathNodeId.end()-1);
		}
		else
		{
			break;
		}
	}	// end while	

	return 0;
}

// 判断元素是否在vector中，true 在，false - 不在
bool MergeMapData::isBelongToVector(const vector<int> &vecInts, int nElement, int& nSite)
{
	vector<int> vecTemp = vecInts;
	vector<int>::iterator iter = std::find(vecTemp.begin(),vecTemp.end(), nElement);	//返回的是一个迭代器指针
	if (iter==vecTemp.end())	// 不存在
	{
		return false;
	}
	else
	{
		nSite = std::distance(vecTemp.begin(), iter);
		return true;
	}

}

// 判断点是否属于集合
bool MergeMapData::isHamapPtBelongToSet(vector<HAMapPoint>& vecHamPt, HAMapPoint hamPt, int& nSite)
{
	bool bRet = false;
	
	int nNumPt = vecHamPt.size();
	for (int i = 0; i < nNumPt; i++)
	{
		if (vecHamPt[i]==hamPt)
		{
			nSite = i;
			bRet = true;
			break;
		}
	}

	return bRet;
}

// 计算link内部点按序连成的折线与矩形框边界的交点
int MergeMapData::getCrossPointLink2Rect(const std::vector<HAMapPoint>& vecLinkPt,
										 cv::Rect rt, HAMapPoint& hamCrossPt)
{
	// 参数自检
	int nNumPt = vecLinkPt.size();
	if (nNumPt<=0)
	{
		return -1;
	}

	// 矩形框范围
	int nX1 = rt.x, nX2 = nX1 + rt.width - 1;
	int nY1 = rt.y, nY2 = nY1 + rt.height - 1;

	// 寻找与矩形框相交的折线
	HAMapPoint hamPrePt = vecLinkPt[0];
	int nPreSign = (isRectInside(hamPrePt,rt))?1:-1;		// 1 - 内部点，-1 - 外部
	int nCurSign = 1;
	bool bFlag = false;		// 标识是否找到交点，false - 没找到，true - 找到
	Line lin;		// 记录与矩形框相交的折线
	HAMapPoint hamCurPt;
	for (int i=1; i<nNumPt; i++)
	{
		hamCurPt = vecLinkPt[i];
		nCurSign = (isRectInside(hamCurPt, rt))?1:-1;

		if (nCurSign*nPreSign<=0)	// 当前点与前一个点分属于矩形框内外部
		{
			//if (nCurSign<0)
			//{
			//	lin.pt1 = Point2d(hamPrePt.x, hamPrePt.y);		// 矩形内部点
			//	lin.pt2 = Point2d(hamCurPt.x, hamCurPt.y);		// 矩形外部点
			//}
			//else
			//{
			//	lin.pt1 = Point2d(hamCurPt.x, hamCurPt.y);		// 矩形内部点
			//	lin.pt2 = Point2d(hamPrePt.x, hamPrePt.y);		// 矩形外部点				
			//}
			
			lin.pt1 = Point2d(hamPrePt.x, hamPrePt.y);
			lin.pt2 = Point2d(hamCurPt.x, hamCurPt.y);

			// 求交点
			Point2d ptTemp;
			Line linInRect;
			if ((hamPrePt.x-nX1)*(hamCurPt.x-nX1)<=0)			// 跨上边界
			{				
				linInRect.pt1 = Point2d(nX1,nY1);
				linInRect.pt2 = Point2d(nX1,nY2);

				// 求交点
				ptTemp = getCrossPoint(&lin,&linInRect);
				if ((ptTemp.y-nY1)*(ptTemp.y-nY2)<=0)
				{
					hamCrossPt.x = ptTemp.x;
					hamCrossPt.y = ptTemp.y;
					bFlag = true;
					break;
				}
			}
			if ((hamPrePt.x-nX2)*(hamCurPt.x-nX2)<=0)			// 跨下边界
			{
				linInRect.pt1 = Point2d(nX2,nY1);
				linInRect.pt2 = Point2d(nX2,nY2);

				// 求交点
				ptTemp = getCrossPoint(&lin,&linInRect);
				if ((ptTemp.y-nY1)*(ptTemp.y-nY2)<=0)
				{
					hamCrossPt.x = ptTemp.x;
					hamCrossPt.y = ptTemp.y;
					bFlag = true;
					break;
				}
			}
			if ((hamPrePt.y-nY1)*(hamCurPt.y-nY1)<=0)			// 跨左边界
			{
				linInRect.pt1 = Point2d(nX1,nY1);
				linInRect.pt2 = Point2d(nX2,nY1);

				// 求交点
				ptTemp = getCrossPoint(&lin,&linInRect);
				if ((ptTemp.x-nX1)*(ptTemp.x-nX2)<=0)
				{
					hamCrossPt.x = ptTemp.x;
					hamCrossPt.y = ptTemp.y;
					bFlag = true;
					break;
				}
			}
			if ((hamPrePt.y-nY2)*(hamCurPt.y-nY2)<=0)			// 跨右边界
			{
				linInRect.pt1 = Point2d(nX1,nY2);
				linInRect.pt2 = Point2d(nX2,nY2);

				// 求交点
				ptTemp = getCrossPoint(&lin,&linInRect);
				if ((ptTemp.x-nX1)*(ptTemp.x-nX2)<=0)
				{
					hamCrossPt.x = ptTemp.x;
					hamCrossPt.y = ptTemp.y;
					bFlag = true;
					break;
				}
			}			
			break;
		}

		// 迭代
		hamPrePt = hamCurPt;
		nPreSign = nCurSign;
	}

	if (!bFlag)
	{
		return -1;
	}

	return 0;
}


// 计算link内部点按序连成的折线与已知直线的交点
int MergeMapData::getCrossPointLink2Line(const std::vector<HAMapPoint>& vecLinkPt,
										 Line ln, HAMapPoint& hamCrossPt)
{
	// 参数自检
	int nNumPt = vecLinkPt.size();
	if (nNumPt<=0)
	{
		return -1;
	}

	// 获取直线参数
	getLinePara(&ln);

	// 计算
	HAMapPoint hamPrePt = vecLinkPt[0];
	double uPreV = ln.a*hamPrePt.x+ln.b*hamPrePt.y+ln.c;
	if (abs(uPreV)<1e-6)		// 判断是否为0
	{
		hamCrossPt = hamPrePt;
		return 0;
	}
	int nPreSign = (uPreV>0)?1:-1;		// 记录之前一个点与主路的关系
	HAMapPoint hamCurPt;
	int nCurSign=0;
	double uCurV = 0;
	bool bFlag = false;		// 标识是否找到交点，false - 没找到，true - 找到
	for (int i=1; i<nNumPt; i++)
	{
		hamCurPt = vecLinkPt[i];
		uCurV = ln.a*hamCurPt.x+ln.b*hamCurPt.y+ln.c;
		if (abs(uCurV)<1e-6)		// 判断是否为0
		{
			hamCrossPt = hamCurPt;
			return 0;
		}
		int nCurSign = (uCurV>0)?1:-1;		// 记录之前一个点与主路的关系

		if (nCurSign*nPreSign<=0)	// 当前点与前一个点分属于直线两边
		{
			Line lnTemp;
			lnTemp.pt1 = Point2d(hamPrePt.x, hamPrePt.y);
			lnTemp.pt2 = Point2d(hamCurPt.x, hamCurPt.y);
			
			Point2d ptTemp;
			ptTemp = getCrossPoint(&ln, &lnTemp);
			hamCrossPt.x = ptTemp.x;
			hamCrossPt.y = ptTemp.y;
			bFlag = true;
			break;
		}

		//hamPrePt = hamCurPt;
		nPreSign = nCurSign;
	}
	
	if (bFlag)
	{
		return 0;
	} 
	else
	{
		return -1;
	}	
}


// 判断点是否在矩形范围内
bool MergeMapData::isRectInside(HAMapPoint hamPoint, cv::Rect rect)
{
	int nX1 = rect.x, nX2 = nX1 + rect.width - 1;
	int nY1 = rect.y, nY2 = nY1 + rect.height - 1;

	if (hamPoint.x>=nX1 && hamPoint.x<=nX2 &&
		hamPoint.y>=nY1 && hamPoint.y<=nY2)
	{
		return true;
	} 
	else
	{
		return false;
	}
}


// 寻找主路进入屏幕的起点、中心点、终点
int MergeMapData::findKeyPoint(const vector<HAMapPoint>& vecMainRoad,
							   HAMapPoint haMainRoadCenterPt,
							   cv::Rect rtScreen,
							   vector<HAMapPoint>& vecKeyPoint)
{
	// 参数自检
	int nMainRoadNumPt = vecMainRoad.size();
	if (nMainRoadNumPt<=0 || rtScreen.width<=0 || rtScreen.height<=0)
	{
		return -1;
	}

	int nRet = 0;

	// 获取中心点位置
	int nCenterSi = -1;
	nRet = getPointSite(vecMainRoad, haMainRoadCenterPt, nCenterSi);
	if (nRet<0)
	{
		return -1;
	}

	// 获取屏幕边界上的主路起点
	//int nStartSi = -1;
	HAMapPoint hamStartPt, hamEndPt;	
	nRet = getStartEndPoint(vecMainRoad, nCenterSi, rtScreen, hamStartPt, hamEndPt);
	if (nRet<0)
	{
		return -1;
	}

	// 输出，注意顺序
	vecKeyPoint.clear();
	vecKeyPoint.push_back(hamStartPt);
	vecKeyPoint.push_back(haMainRoadCenterPt);
	vecKeyPoint.push_back(hamEndPt);

	return 0;
}

// 获取屏幕边界上的主路起点、终点
/*
int MergeMapData::getStartEndPoint(const vector<HAMapPoint>& vecMainRoad,
								   int nCenterSite,
								   cv::Rect rtScreen,
								   HAMapPoint& hamStartPt,
								   HAMapPoint& hamEndPt)
{
	// 参数自检
	int nMainRoadNumPt = vecMainRoad.size();
	if (nMainRoadNumPt<=0 || rtScreen.width<=0 || rtScreen.height<=0 ||
		nCenterSite<=0)
	{
		return -1;
	}

	// 屏幕范围
	int nX1 = rtScreen.x, nX2 = nX1+rtScreen.width-1;
	int nY1 = rtScreen.y, nY2 = nY1+rtScreen.height-1;

	// 获取起点、终点
	int nSign = 0;		// 标记是获取起点还是终点，-1 - 获取起点，1 - 获取终点
	for (int i=0; i<2; i++)
	{
		nSign = (i==0)?-1:1;
		
		HAMapPoint hamPrePt = vecMainRoad[nCenterSite];
		HAMapPoint hamCurPt;
		int j = nCenterSite+nSign;
		int nSi = -1;		// 记录位置
		Point2i ptKeyPt;
		while (j>=0 && j<nMainRoadNumPt)
		{
			hamCurPt = vecMainRoad[j];

			// 判断边界
// 			if (hamCurPt.x<=nX1 || hamCurPt.x>=nX2 ||
// 				hamCurPt.y<=nY1 || hamCurPt.y>=nY2)
			if (!IsRectInside(hamCurPt,rtScreen))			
			{
				nSi = j;

				// 构造两线
				Line lin0;		// 边界直线
				if (hamCurPt.x<=nX1)
				{
					lin0.pt1 = Point2d(nX1,nY1);
					lin0.pt2 = Point2d(nX1,nY2);
				}
				if (hamCurPt.x>=nX2)
				{
					lin0.pt1 = Point2d(nX2,nY1);
					lin0.pt2 = Point2d(nX2,nY2);
				}
				if (hamCurPt.y<=nY1)
				{
					lin0.pt1 = Point2d(nX1,nY1);
					lin0.pt2 = Point2d(nX2,nY1);
				}
				if (hamCurPt.y>=nY2)
				{
					lin0.pt1 = Point2d(nX1,nY2);
					lin0.pt2 = Point2d(nX2,nY2);
				}

				Line lin1;		// 前后两点形成的直线
				lin1.pt1 = Point2d(hamPrePt.x, hamPrePt.y);
				lin1.pt2 = Point2d(hamCurPt.x, hamCurPt.y);

				// 求两线交点
				Point2d ptCross = getCrossPoint(&lin0,&lin1);
				ptKeyPt = ptCross;
				nSi = j;

				break;
			}

			hamPrePt = vecMainRoad[j];

			// 移动自变量
			j = j + nSign;
		}
		if (nSi<0)
		{
			return -1;		// 注意，此处表示无交点时则直接退出
		}
		
		if (i==0)	// 起点
		{
			hamStartPt.x = ptKeyPt.x;
			hamStartPt.y = ptKeyPt.y;
		} 
		else		// 终点
		{
			hamEndPt.x = ptKeyPt.x;
			hamEndPt.y = ptKeyPt.y;
		}
	}	

	return 0;
}
*/

int MergeMapData::getStartEndPoint(const vector<HAMapPoint>& vecMainRoad,
								   int nCenterSite,
								   cv::Rect rtScreen,
								   HAMapPoint& hamStartPt,
								   HAMapPoint& hamEndPt)
{
	// 参数自检
	int nMainRoadNumPt = vecMainRoad.size();
	if (nMainRoadNumPt<=0 || rtScreen.width<=0 || rtScreen.height<=0 ||
		nCenterSite<=0)
	{
		return -1;
	}

	int nRet = 0;
	vector<HAMapPoint> vecHamPt;

	// 求起点	
	vecHamPt.insert(vecHamPt.end(),vecMainRoad.begin(),vecMainRoad.begin()+nCenterSite+1);
	reverseOrder(vecHamPt);		// 倒序重排，与主路方向一致
	int nRet0 = getCrossPointLink2Rect(vecHamPt, rtScreen, hamStartPt);
	/*if (nRet<0)
	{
		return -2;
	}*/

	// 求终点
	vecHamPt.clear();
	vecHamPt.insert(vecHamPt.end(),vecMainRoad.begin()+nCenterSite,vecMainRoad.end());
	int nRet1 = getCrossPointLink2Rect(vecHamPt, rtScreen, hamEndPt);
	/*if (nRet<0)
	{
		return -3;
	}*/

	// 返回
	if (nRet0<0)
	{
		if (nRet1==0)
		{
			return -2;		// 起点与窗口不相交
		}
		else
		{
			return -4;		// 起点、终点都与窗口不相交
		}		
	}
	else
	{
		if (nRet1==0)
		{
			return 0;		// 正常
		}
		else
		{
			return -3;		// 终点与窗口不相交
		}	
	}

	//return 0;
}

// 获取屏幕边界上的主路终点


// 将主路与路网另一子路的接近程度，用距离和表示
int MergeMapData::getMainSubLine2SubNetDis(const vector<HAMapPoint>& vecSubMainRoad, 
										   int nCenterSite,
										   HAMapPoint hamPrePtInRoadNet,
										   HAMapPoint hamFixPtInRoadNet,
										   HAMapPoint hamNextPtInRoadNet,
										   double& uError)
{
	// 参数自检
	int nNum = vecSubMainRoad.size();
	if (nNum<=0)
	{
		return -1;
	}

	int nRet = 0;

	// 构造两条直线
	Line lin0, lin1;
	lin0.pt1 = Point2d(hamPrePtInRoadNet.x, hamPrePtInRoadNet.y);
	lin0.pt2 = Point2d(hamFixPtInRoadNet.x, hamFixPtInRoadNet.y);
	lin1.pt1 = Point2d(hamFixPtInRoadNet.x, hamFixPtInRoadNet.y);
	lin1.pt2 = Point2d(hamNextPtInRoadNet.x, hamNextPtInRoadNet.y);
	getLinePara(&lin0);
	getLinePara(&lin1);

	// 偏移
	HAMapPoint hamMainRoadCenterPt = vecSubMainRoad[nCenterSite];
	int nOffsetX = hamFixPtInRoadNet.x - hamMainRoadCenterPt.x;
	int nOffsetY = hamFixPtInRoadNet.y - hamMainRoadCenterPt.y;

	// 计算
	uError = 0.f;
	double uDis = 0.f;
	HAMapPoint hamPtTemp;
	vector<double> vecDis;
	for (int i=0; i<nNum; i++)
	{
		hamPtTemp = vecSubMainRoad[i];
		hamPtTemp.x += nOffsetX;
		hamPtTemp.y += nOffsetY;
		if (i<nCenterSite)
		{
			uDis = getDistancePoint2Line(Point2i(hamPtTemp.x,hamPtTemp.y),lin0);
		}
		else if (i==nCenterSite)
		{			
			uDis = 0;
		} 
		else
		{
			uDis = getDistancePoint2Line(Point2i(hamPtTemp.x,hamPtTemp.y),lin1);
		}
		uError += uDis;
		vecDis.push_back(uDis);
	}
	
	//// 求均值和标准差
	//double uMean=0, uStd=0;
	//nRet = getStatistic(vecDis, uMean, uStd);
	//uError = uStd;

	return 0;
}

// 取中心点一定范围内的主路子集，并按主路顺序排列
int MergeMapData::getSubMainRoadNearCenter(const vector<HAMapPoint>& vecMainRoad,												   
										int nCenterSite,
										cv::Size2i szOffset,
										vector<HAMapPoint>& vecSubMainRoad)
{
	// 参数自检
	int nNumMainRoadPt = vecMainRoad.size();
	if (nNumMainRoadPt<=0 || nCenterSite<0 || szOffset.height<0 || szOffset.width<0)
	{
		return -1;
	}
	
	int nRet = 0;
	// 取中心点一定范围内的主路子集（如上下左右各200像素范围），并按顺序填满点与点之间的所有点
	//int nDel = CENTER_COVER;		// 
	//vector<HAMapPoint> vecSubMainRoad;
	
	int nOffsetX = szOffset.width, nOffsetY = szOffset.height;
	HAMapPoint haMainRoadCenterPt = vecMainRoad[nCenterSite];
	vecSubMainRoad.clear();
	vecSubMainRoad.push_back(haMainRoadCenterPt);
	for (int i=nCenterSite-1; i>=0; i--)
	{
		if (vecMainRoad[i].x==haMainRoadCenterPt.x && vecMainRoad[i].y==haMainRoadCenterPt.y)
		{
			continue;
		}
		if (abs(vecMainRoad[i].x-haMainRoadCenterPt.x)<=nOffsetX &&
			abs(vecMainRoad[i].y-haMainRoadCenterPt.y)<=nOffsetY)
		{
			// 在直线上，求两点间范围内的所有点
			vector<HAMapPoint> vecPointsInLine;			
			nRet = getPointsBetweenTwoPoints(vecMainRoad[i], vecSubMainRoad[0], vecPointsInLine);			
			if (nRet==0)
			{
				vecSubMainRoad.insert(vecSubMainRoad.begin(),vecPointsInLine.begin(),vecPointsInLine.end());				
			}
			vecSubMainRoad.insert(vecSubMainRoad.begin(),vecMainRoad[i]);
		}
		else
		{
			// 在直线上，求两点间范围内的所有点
			vector<HAMapPoint> vecPointsInLine;			
			nRet = getPointsBetweenTwoPoints(vecMainRoad[i], vecSubMainRoad[0], vecPointsInLine);			
			if (nRet==0)
			{
				for (int j=0; j<vecPointsInLine.size(); j++)
				{
					if (abs(vecPointsInLine[j].x-haMainRoadCenterPt.x)<=nOffsetX &&
						abs(vecPointsInLine[j].y-haMainRoadCenterPt.y)<=nOffsetY)
					{
						vecSubMainRoad.insert(vecSubMainRoad.begin(),vecPointsInLine[j]);
					}					
				}				
			}
			//vecSubMainRoad.insert(vecSubMainRoad.begin(),vecMainRoad[i]);
			break;
		}
	}
	for (int i=nCenterSite+1; i<nNumMainRoadPt; i++)
	{
		if (vecMainRoad[i].x==haMainRoadCenterPt.x && vecMainRoad[i].y==haMainRoadCenterPt.y)
		{
			continue;
		}
		if (abs(vecMainRoad[i].x-haMainRoadCenterPt.x)<=nOffsetX &&
			abs(vecMainRoad[i].y-haMainRoadCenterPt.y)<=nOffsetY)
		{
			// 在直线上，求两点间范围内的所有点
			vector<HAMapPoint> vecPointsInLine;			
			nRet = getPointsBetweenTwoPoints(vecSubMainRoad[vecSubMainRoad.size()-1], vecMainRoad[i], vecPointsInLine);
			if (nRet==0)
			{
				vecSubMainRoad.insert(vecSubMainRoad.end(),vecPointsInLine.begin(),vecPointsInLine.end());				
			}			
			vecSubMainRoad.push_back(vecMainRoad[i]);
		}
		else
		{
			// 在直线上，求两点间范围内的所有点
			vector<HAMapPoint> vecPointsInLine;			
			nRet = getPointsBetweenTwoPoints(vecSubMainRoad[vecSubMainRoad.size()-1], vecMainRoad[i], vecPointsInLine);	
			if (nRet==0)
			{
				for (int j=0; j<vecPointsInLine.size(); j++)
				{
					if (abs(vecPointsInLine[j].x-haMainRoadCenterPt.x)<=nOffsetX &&
						abs(vecPointsInLine[j].y-haMainRoadCenterPt.y)<=nOffsetY)
					{
						vecSubMainRoad.push_back(vecPointsInLine[j]);
					}					
				}				
			}			
			break;
		}
	}


	return 0;
}

/*
// 求均值和方差
int MergeMapData::getStatistic(const vector<double>& vecDisSet, double& uMean, double& uStd)
{
	// 参数自检
	if (vecDisSet.size()<=0)
	{
		return -1;
	}
	
	double uSum = std::accumulate(std::begin(vecDisSet), std::end(vecDisSet), 0.0);  
	uMean =  uSum / vecDisSet.size(); //均值  

	double uAccum  = 0.0;  
	std::for_each (std::begin(vecDisSet), std::end(vecDisSet), [&](const double uD) 
	{  
		uAccum  += (uD-uMean)*(uD-uMean);  
	});  

	uStd = sqrt(uAccum/(vecDisSet.size()-1)); //方差  

	return 0;
}
*/

// 求两向量夹角，角度制
float MergeMapData::getAngle(cv::Vec2f v1, cv::Vec2f v2)
{
	// 单位化
	v1 = v1/sqrt(v1[0]*v1[0]+v1[1]*v1[1]);	// 单位化
	v2 = v2/sqrt(v2[0]*v2[0]+v2[1]*v2[1]);
	
	float fCosV = v1[0]*v2[0] + v1[1]*v2[1];

	return acosf(fCosV)*180/CV_PI;
}

// 求某一个指定点在集合中的位置
int MergeMapData::getPointSite(const vector<HAMapPoint>& vecHamPts,
								HAMapPoint hamPt,
								int& nSite)
{
	// 参数自检
	int nNum = vecHamPts.size();
	if (nNum<=0)
	{
	#ifdef _WINDOWS_VER_
		printf("==============getPointSite - parameter Error!!==============");
	#else
		LOGD("==============getPointSite - parameter Error!!==============");
	#endif
		return -1;
	}

	// 求中心点位置
	nSite = -1;
	for (int i = 0; i<nNum; i++)
	{
		if (vecHamPts[i].x==hamPt.x && 
			vecHamPts[i].y==hamPt.y)
		{
			nSite = i;
			break;
		}
	}
	if (nSite<0)	// 集合中不包含指定点
	{
	#ifdef _WINDOWS_VER_
		printf("==============getPointSite - not exist point!!===========");
	#else
		LOGD("==============getPointSite - not exist point!!===========");
	#endif
		return -1;
	}

	return 0;
}

// 求集合中指定位置向前或向后满足距离要求的最近点
int MergeMapData::getNearestPoint(const vector<HAMapPoint>& vecHamPts,
								   int nSite,
								   bool bIsPre,
								   HAMapPoint& hamNearestPt)
{
	// 参数自检
	int nNum = vecHamPts.size();
	if (nNum<=0 || nSite<0 || nSite>=nNum)
	{
		return -1;
	}
	
	int nDel = NEAREST_POINT_DIS;
	HAMapPoint hamPt = vecHamPts[nSite];
	if (bIsPre)		// 向前找点
	{
		if (nSite==0)		// 前面无点
		{
			return -1;
		}
		int nNearSi = -1;
		for (int i=nSite-1; i>=0; i--)
		{
			if (getDistancePoint2Point(hamPt.x,hamPt.y,vecHamPts[i].x,vecHamPts[i].y)>=nDel)
			{
				nNearSi = i;
				break;
			}
		}
		if (nNearSi<0)
		{
			return -1;
		}
		hamNearestPt = vecHamPts[nNearSi];
	} 
	else		// 向后找点
	{
		if (nSite==nNum)		// 后面无点
		{
			return -1;
		}
		int nNearSi = -1;
		for (int i=nSite+1; i<nNum; i++)
		{
			if (getDistancePoint2Point(hamPt.x,hamPt.y,vecHamPts[i].x,vecHamPts[i].y)>=nDel)
			{
				nNearSi = i;
				break;
			}
		}
		if (nNearSi<0)
		{
			return -1;
		}
		hamNearestPt = vecHamPts[nNearSi];
	}

	return 0;
}

// 获取直线上两点之间的所有点，不包括两点
int MergeMapData::getPointsBetweenTwoPoints(HAMapPoint hamStartPoint,HAMapPoint hamEndPoint, 
							  vector<HAMapPoint>& vecPointsInLine)
{
	int nDx = hamEndPoint.x - hamStartPoint.x;
	int nDy = hamEndPoint.y - hamStartPoint.y;	
	if (abs(nDx)>abs(nDy))		// 用x表示y，y = y0 + k*(x-x0)
	{
		int nSign = (nDx>0)?1:-1;		// 记录符号
		int nCount = 0;
		while (true)
		{
			if (nCount>abs(nDx)-2)
			{
				break;
			}

			float fK = (float)nDy/nDx;
			int nX = hamStartPoint.x + nSign*(nCount+1);
			int nY = hamStartPoint.y + fK*(nX-hamStartPoint.x);
			HAMapPoint hamTemp;
			hamTemp.x = nX;
			hamTemp.y = nY;
			vecPointsInLine.push_back(hamTemp);

			nCount++;
		}
	} 
	else		// 用y表示x，x = x0 + k*(y-y0)
	{
		int nSign = (nDy>0)?1:-1;
		int nCount = 0;
		while (true)
		{
			if (nCount>abs(nDy)-2)
			{
				break;
			}

			float fK = (float)nDx/nDy;
			int nY = hamStartPoint.y + nSign*(nCount+1);
			int nX = hamStartPoint.x + fK*(nY-hamStartPoint.y);
			HAMapPoint hamTemp;
			hamTemp.x = nX;
			hamTemp.y = nY;
			vecPointsInLine.push_back(hamTemp);

			nCount++;
		}
	}
	
	return 0;
}

// 获取直线参数
void MergeMapData::getLinePara(Line *lin)  
{  
	double fX1=lin->pt1.x, fY1=lin->pt1.y;
	double fX2=lin->pt2.x, fY2=lin->pt2.y;
	lin->a = fY1 - fY2;			// a = y1 - y2
	lin->b = fX2 - fX1;			// b = x2 - x1
	lin->c = fX1*fY2 - fX2*fY1;		// c = x1*y2 - x2*y1
	//double uT = fX1*fY2 - fX2*fY1;
	//lin->a=lin->ptf1.y-lin->ptf2.y;			// a = y1 - y2
	//lin->b=lin->ptf2.x-lin->ptf1.x;			// b = x2 - x1
	//lin->c=lin->ptf1.x*lin->ptf2.y-lin->ptf2.x*lin->ptf1.y;		// c = x1*y2 - x2*y1
}

// 获取两直线交点
cv::Point2d MergeMapData::getCrossPoint(Line *lin1,Line *lin2)  
{  
	getLinePara(lin1);  
	getLinePara(lin2);
	double uD=lin1->a*lin2->b-lin2->a*lin1->b;  
	Point2d p;  
	p.x=(lin1->b*lin2->c-lin2->b*lin1->c)/uD;  
	p.y=(lin1->c*lin2->a-lin2->c*lin1->a)/uD;  
	return p;  
}

// 计算点到直线的距离，│AXo＋BYo＋C│／√（A²＋B²）
double MergeMapData::getDistancePoint2Line(cv::Point2i pt, Line lin)
{	
	double uDis = abs(lin.a*pt.x+lin.b*pt.y+lin.c)/sqrt(lin.a*lin.a+lin.b*lin.b);
	return uDis;
}

// 计算两点距离
template<typename T>
float MergeMapData::getDistancePoint2Point(T tX1,T tY1,T tX2, T tY2)
{
	return sqrt((tX1-tX2)*(tX1-tX2) + (tY1-tY2)*(tY1-tY2));
}

// 构造link端点节点
int MergeMapData::formLinkEndPointNode(const std::vector<std::vector<HAMapPoint> >& vecLinks,
									   const vector<LinkInfo>& vecLinkInfos,
						  vector<LinkEndPointNode>& vecLinkEndPtnode)
{
	// 参数自检
	int nLinkNum = vecLinks.size();
	if (nLinkNum<=0 || nLinkNum!=vecLinkInfos.size())
	{
	#ifdef _WINDOWS_VER_
		printf("==============formLinkEndPointNode - parameter Error!!==============");
	#else
		LOGD("==============formLinkEndPointNode - parameter Error!!==============");
	#endif
		return -1;
	}

	vector<Point2i> vecEndPt;		// 记录已添加的端点	
	for (int i=0; i<nLinkNum; i++)
	{
		vector<HAMapPoint> vechamPts = vecLinks[i];
		

		// 处理两个端点
		for (int j=0; j<2; j++)
		{
			Point2i ptTemp0, ptTemp1;
			if (j==0)
			{
				ptTemp0 = Point2i(vechamPts[0].x,vechamPts[0].y);
				//ptTemp1 = Point2i(vechamPts[1].x,vechamPts[1].y);	
				ptTemp1 = Point2i(vechamPts[vechamPts.size()-1].x,vechamPts[vechamPts.size()-1].y);	
			}
			else
			{
				ptTemp0 = Point2i(vechamPts[vechamPts.size()-1].x,vechamPts[vechamPts.size()-1].y);	
				//ptTemp1 = Point2i(vechamPts[vechamPts.size()-2].x,vechamPts[vechamPts.size()-2].y);	
				ptTemp1 = Point2i(vechamPts[0].x,vechamPts[0].y);
			}

			// 判断是否重复
			vector<Point2i>::iterator iter = std::find(vecEndPt.begin(),vecEndPt.end(),ptTemp0);//返回的是一个迭代器指针
			if (iter==vecEndPt.end())	// 不重复
			{			
				vecEndPt.push_back(ptTemp0);
				HAMapPoint hamTemp0 = {ptTemp0.x, ptTemp0.y};
				HAMapPoint hamTemp1 = {ptTemp1.x, ptTemp1.y};
				LinkEndPointNode node;
				node.hamEndPoint = hamTemp0;
				node.vecLinkId.push_back(i);
				node.vecNeighborPoint.push_back(hamTemp1);

				vecLinkEndPtnode.push_back(node);
			}
			else		// 重复
			{				
				int nSi = std::distance(vecEndPt.begin(), iter);

				// ========test======
				if (nSi==13)
				{
					int a;
					a = 0;
				}
				//====================

				HAMapPoint hamTemp1 = {ptTemp1.x, ptTemp1.y};
				if (hamTemp1!=vecLinkEndPtnode[nSi].hamEndPoint)		// 去重复
				{
					LinkEndPointNode* pNode = &vecLinkEndPtnode[nSi];
					pNode->vecLinkId.push_back(i);
					pNode->vecNeighborPoint.push_back(hamTemp1);
				}				
			}
		}		
	}

	// 建立node之间的邻居关系
	int nNodeNum = vecLinkEndPtnode.size();
	LinkEndPointNode* pNodeTemp;
	HAMapPoint hamEndPtTemp;
	vector<HAMapPoint> vecLinkTemp;
	for (int i=0; i<nNodeNum; i++)
	{
		pNodeTemp = &vecLinkEndPtnode[i];
		//HAMapPoint hamEndPtTemp = pNodeTemp->hamEndPoint;
		int nNeighborNum = pNodeTemp->vecNeighborPoint.size();
		vector<int> vecNeighborNodeId(nNeighborNum,-1);		// 记录node Id
		vector<int> vecDirection2NeighborNode(nNeighborNum,-1);		// 记录当前节点到邻居节点的方向		
		for (int j=0; j<nNeighborNum; j++)
		{
			HAMapPoint hamNextPt = pNodeTemp->vecNeighborPoint[j];
			//hamEndPtTemp = pNodeTemp->vecNeighborPoint[j];
			Point2i ptTemp(hamNextPt.x, hamNextPt.y);
			vector<Point2i>::iterator iter = std::find(vecEndPt.begin(),vecEndPt.end(),ptTemp);
			if (iter==vecEndPt.end())	// 不重复
			{
				continue;
			}
			int nSi = std::distance(vecEndPt.begin(), iter);
			vecNeighborNodeId[j] = nSi;
			int nLinkId = pNodeTemp->vecLinkId[j];
						
			//// =======test==========
			//int routeId = vecLinkInfos[nLinkId].routeId;
			//int routeName = vecLinkInfos[nLinkId].routeNameHash;
			//printf("nLinkId=%d, routeId=%d, routeName=%d\n", nLinkId, routeId, routeName);
			//// =====================

			// 获取方向
			int nDirection = vecLinkInfos[nLinkId].direction;
			int nDirectionTemp = nDirection;
			vecLinkTemp = vecLinks[nLinkId];
			if (hamNextPt==vecLinkTemp[0])
			{
				if (nDirection==2)		// 2-正方向
				{
					nDirectionTemp = 3;
				}
				if (nDirection==3)		// 3-反方向 
				{
					nDirectionTemp = 2;
				}
			}
			vecDirection2NeighborNode[j] = nDirectionTemp;
		}
		pNodeTemp->vecNeighborNodeId = vecNeighborNodeId;
		pNodeTemp->vecDirection = vecDirection2NeighborNode;
		pNodeTemp->nNodeId = i;
	}

	return 0;
}

// 在link中指定点向前或向后依次寻找离指定点最近且距离大于固定值的点
int MergeMapData::findNeighborPoint(const std::vector<HAMapPoint>& vecPt, int nSi, int nDisDel, 
									bool bIsFront, HAMapPoint& hamResultPt)
{
	// 参数自检
	int nNum = vecPt.size();
	if (nNum<=0)
	{
		return -1;
	}

	HAMapPoint hamFixPoint = vecPt[nSi];

	int nRi = -1;	// 满足要求点的位置
	if (bIsFront)		// 朝前搜索
	{
		HAMapPoint hamTempPt;
		for (int i=nSi-1; i>=0; i--)
		{
			hamTempPt = vecPt[i];
			float fDis = getDistancePoint2Point(hamTempPt.x, hamTempPt.y, hamFixPoint.x, hamFixPoint.y);
			if (fDis>nDisDel)
			{
				nRi = i;
				hamResultPt = hamTempPt;
				break;
			}
		}
	} 
	else	// 往后搜索
	{
		HAMapPoint hamTempPt;
		for (int i=nSi+1; i<nNum; i++)
		{
			hamTempPt = vecPt[i];
			float fDis = getDistancePoint2Point(hamTempPt.x, hamTempPt.y, hamFixPoint.x, hamFixPoint.y);
			if (fDis>nDisDel)
			{
				nRi = i;
				hamResultPt = hamTempPt;
				break;
			}
		}
	}
	if (nRi<0)
	{
		return -1;
	}

	return 0;
}

#ifdef _WINDOWS_VER_
// ld add
void MergeMapData::drawImage(cv::Mat& matNavi,HAMapPoint hptCenter, vector<HAMapPoint> hamPts, 
								  cv::Scalar scColor,int nLineWidth)
{	
	//matNavi.create(600,600,CV_8UC3);
	
	int thickness = -1;
	int lineType = 8;
	int offset_x =0,offset_y = 0;
	offset_x = hptCenter.x - matNavi.cols/2;
	offset_y = hptCenter.y - matNavi.rows/2;
	int nNum = hamPts.size();
	//cv::Scalar color((rand() % (255+1)),(rand() % (255+1)),250);
	cv::Scalar color = (scColor==Scalar(0,0,0))?Scalar((rand() % (255+1)),(rand() % (255+1)),250):scColor;
	for (int i = 1; i < nNum; i++)
	{

		cv::Point ptPre = cv::Point(hamPts[i-1].x, hamPts[i-1].y) - cv::Point(offset_x,offset_y);
		cv::Point ptCur = cv::Point(hamPts[i].x, hamPts[i].y) - cv::Point(offset_x,offset_y);		

		if (scColor==Scalar(0,0,0))
		{
			line(matNavi,ptPre,ptCur,color,1);	
			cv::circle( matNavi,ptPre,2,cv::Scalar( 0, 0, 255 ),thickness,lineType );
		}
		else
		{
			line(matNavi,ptPre,ptCur,color,nLineWidth);
			//cv::circle( matNavi,ptPre,2,cv::Scalar( 0, 0, 255 ),thickness,lineType );
		}
			
	}
}

// 图像上显示字符，方便查看结构关系
void MergeMapData::drawNode(cv::Mat matImg, const vector<LinkEndPointNode> vecLinkEndPtnode,
							const std::vector<std::vector<HAMapPoint> >& vecRoadNetLink)
{
	int thickness = -1;
	int lineType = 8;
	
	// 绘制路网，1-黄，2-绿，3-红，0-白色
	/*size_t linkSize = vecLinkInfos.size();*/
	int nNodeNum = vecLinkEndPtnode.size();	

	Rect rt(0,0,matImg.cols,matImg.rows);

	for(int i = 0; i < nNodeNum; i++)
	{	
		LinkEndPointNode curNode = vecLinkEndPtnode[i];
		int nCurNodeId = curNode.nNodeId;
		HAMapPoint hamPt = curNode.hamEndPoint;
		Point2i ptCurPt = Point2i(hamPt.x,hamPt.y)-m_ptOffset;
		hamPt.x = ptCurPt.x;
		hamPt.y = ptCurPt.y;
		if (!isRectInside(hamPt,rt))
		{
			continue;
		}	

	
		//插入字符
		//参数为：承载的图片，插入的文字，文字的位置（文本框左下角），字体，大小，颜色
		char chNodeId[20];
		itoa(nCurNodeId,chNodeId,10);
		putText( matImg, chNodeId, ptCurPt,CV_FONT_HERSHEY_COMPLEX, 0.5, Scalar(255, 255, 255));  
		//imshow("底板",picture); 

	    // 绘制顶点
		cv::circle( matImg,ptCurPt,2,cv::Scalar( 255, 255, 255),thickness,lineType );

		// 绘制路径
		for (int j = 0; j < curNode.vecNeighborNodeId.size(); j++)
		{
			int nDirection = curNode.vecDirection[j];
			cv::Scalar scColor;		
			switch (nDirection)
			{
			case 0:
				scColor = Scalar(255,255,255);
				break;
			case 1:
				scColor = Scalar(0,255,255);
				break;
			case 2:
				scColor = Scalar(0,255,0);
				break;
			case 3:
				scColor = Scalar(0,0,255);
				break;
			default:
				break;
			}

			int nLinkId = curNode.vecLinkId[j];			
			drawImage(matImg, m_hamMainRoadCenter, vecRoadNetLink[nLinkId], scColor, 1);

		}
	}
	
}

// 绘制路网路径方向，1-黄，2-绿，3-红，0-白色
// 主路 - 中心点前蓝色，中心点后蓝绿
void MergeMapData::drawNavi2(cv::Mat& matNavi,HAMapPoint hptCenter, vector<HAMapPoint> mptGPSSet,
			   std::vector<LinkInfo>& vecLinkInfos, std::vector< std::vector<HAMapPoint> > & vecAxes)
{	
	int thickness = -1;
	int lineType = 8;
	int offset_x =0,offset_y = 0;
	offset_x = hptCenter.x - matNavi.cols/2;
	offset_y = hptCenter.y - matNavi.rows/2;
	int nGpsNum = mptGPSSet.size();

	// 绘制主路
	bool bIsCenterDrawed = false;		// 标识中心点是否已画，true - 已画，false - 未画
	bIsCenterDrawed = (mptGPSSet[0].x==hptCenter.x&&mptGPSSet[0].y==hptCenter.y)?true:false;
	for (int i = 1; i < nGpsNum; i++)
	{

		cv::Point ptPre = cv::Point(mptGPSSet[i-1].x, mptGPSSet[i-1].y) - cv::Point(offset_x,offset_y);
		cv::Point ptCur = cv::Point(mptGPSSet[i].x, mptGPSSet[i].y) - cv::Point(offset_x,offset_y);		

		cv::circle( matNavi,ptPre,2,cv::Scalar( 0, 0, 255 ),thickness,lineType );
		cv::circle( matNavi,ptCur,2,cv::Scalar( 0, 0, 255 ),thickness,lineType );

		bIsCenterDrawed = (mptGPSSet[i].x==hptCenter.x&&mptGPSSet[i].y==hptCenter.y)?true:false;

		if (bIsCenterDrawed)
		{
			line(matNavi,ptPre,ptCur,cv::Scalar( 255, 255, 0 ),2);
		} 
		else
		{
			line(matNavi,ptPre,ptCur,cv::Scalar( 255, 0, 0 ),2);
		}

	}


	// 绘制路网，1-黄，2-绿，3-红，0-白色
	/*size_t linkSize = vecLinkInfos.size();*/
	size_t linkSize = vecAxes.size();
	printf("total :%d\n",linkSize);

	for(int l = 0;l < linkSize;l++)
	{
		std::vector<HAMapPoint> axs = vecAxes[l];
		if(axs.size() < 2)
			continue;

		cv::Scalar color;
		int nDirection = vecLinkInfos[l].direction;
		switch (nDirection)
		{
		case 0:
			color = Scalar(255,255,255);
			break;
		case 1:
			color = Scalar(0,255,255);
			break;
		case 2:
			color = Scalar(0,255,0);
			break;

		case 3:
			color = Scalar(0,0,255);
			break;
		default:
			break;
		}


		cv::circle( matNavi,cv::Point(axs[0].x-offset_x,axs[0].y-offset_y),2,cv::Scalar( 128, 255, 128),thickness,lineType );

		//printf("=========link:%d,size:%d=============\n",l,axs.size());
		for(int a=1;a< axs.size();a++)
		{
			cv::Point pt1(axs[a-1].x-offset_x,axs[a-1].y-offset_y);
			cv::Point pt2(axs[a].x-offset_x,axs[a].y-offset_y);

			line(matNavi,pt1,pt2,color);

			cv::circle( matNavi,pt2,2,cv::Scalar(128, 255,128),thickness,lineType );
			//printf("(%d,%d){%d,%d} ",axs[a-1].x,axs[a-1].y,axs[a-1].x-offset_x,axs[a-1].y-offset_y);
		}
		//printf("\n");
	}

}


// 绘制路网
void MergeMapData::drawRoadNet(const std::vector< std::vector<HAMapPoint> >& vecRoadNetLink,
							cv::Size2i szCover,	HAMapPoint hamPixelCenter,cv::Mat& matRoadNetImg)
{
	int nNumLink = vecRoadNetLink.size();
	// 基于主路绘制路网
	matRoadNetImg.create(szCover.height,szCover.width,CV_8UC3); 
	matRoadNetImg.setTo(0);	
	for (int i=0; i<nNumLink; i++)
	{		
		drawImage(matRoadNetImg, hamPixelCenter, vecRoadNetLink[i],cv::Scalar(255,255,255),20);
	}
		

	// 绘制岔路
	vector<int> veCrossRoadLinkId1;	
	veCrossRoadLinkId1.push_back(182);
	//veCrossRoadLinkId1.push_back(175);
	//veCrossRoadLinkId1.push_back(207);
	for (int i=0; i<veCrossRoadLinkId1.size(); i++)
	{		
		drawImage(matRoadNetImg, hamPixelCenter, vecRoadNetLink[veCrossRoadLinkId1[i]],cv::Scalar(0,255,0),20);
	}

	vector<int> veCrossRoadLinkId2;	
	veCrossRoadLinkId2.push_back(406);
	veCrossRoadLinkId2.push_back(407);	
	for (int i=0; i<veCrossRoadLinkId2.size(); i++)
	{		
		drawImage(matRoadNetImg, hamPixelCenter, vecRoadNetLink[veCrossRoadLinkId2[i]],cv::Scalar(0,255,0),20);
	}

	vector<int> veCrossRoadLinkId3;	
	veCrossRoadLinkId3.push_back(184);
	veCrossRoadLinkId3.push_back(178);	
	veCrossRoadLinkId3.push_back(177);
	for (int i=0; i<veCrossRoadLinkId3.size(); i++)
	{		
		drawImage(matRoadNetImg, hamPixelCenter, vecRoadNetLink[veCrossRoadLinkId3[i]],cv::Scalar(0,255,0),20);
	}

	vector<int> veCrossRoadLinkId4;	
	veCrossRoadLinkId4.push_back(233);
	veCrossRoadLinkId4.push_back(232);	
	veCrossRoadLinkId4.push_back(206);
	for (int i=0; i<veCrossRoadLinkId4.size(); i++)
	{		
		drawImage(matRoadNetImg, hamPixelCenter, vecRoadNetLink[veCrossRoadLinkId4[i]],cv::Scalar(0,255,0),20);
	}

	vector<int> veCrossRoadLinkId5;	
	veCrossRoadLinkId5.push_back(169);	
	for (int i=0; i<veCrossRoadLinkId5.size(); i++)
	{		
		drawImage(matRoadNetImg, hamPixelCenter, vecRoadNetLink[veCrossRoadLinkId5[i]],cv::Scalar(0,255,0),20);
	}

	vector<int> veCrossRoadLinkId6;	
	veCrossRoadLinkId6.push_back(186);	
	for (int i=0; i<veCrossRoadLinkId6.size(); i++)
	{		
		drawImage(matRoadNetImg, hamPixelCenter, vecRoadNetLink[veCrossRoadLinkId6[i]],cv::Scalar(0,255,0),20);
	}

	vector<int> veCrossRoadLinkId7;	
	veCrossRoadLinkId7.push_back(31);	
	veCrossRoadLinkId7.push_back(32);
	for (int i=0; i<veCrossRoadLinkId7.size(); i++)
	{		
		drawImage(matRoadNetImg, hamPixelCenter, vecRoadNetLink[veCrossRoadLinkId7[i]],cv::Scalar(0,255,0),20);
	}

	vector<int> veCrossRoadLinkId8;	
	veCrossRoadLinkId8.push_back(34);	
	veCrossRoadLinkId8.push_back(33);
	for (int i=0; i<veCrossRoadLinkId8.size(); i++)
	{		
		drawImage(matRoadNetImg, hamPixelCenter, vecRoadNetLink[veCrossRoadLinkId8[i]],cv::Scalar(0,255,0),20);
	}

	vector<int> veCrossRoadLinkId9;	
	veCrossRoadLinkId9.push_back(28);	
	for (int i=0; i<veCrossRoadLinkId9.size(); i++)
	{		
		drawImage(matRoadNetImg, hamPixelCenter, vecRoadNetLink[veCrossRoadLinkId9[i]],cv::Scalar(0,255,0),20);
	}
	
	vector<int> veCrossRoadLinkId10;	
	veCrossRoadLinkId10.push_back(48);	
	for (int i=0; i<veCrossRoadLinkId10.size(); i++)
	{		
		drawImage(matRoadNetImg, hamPixelCenter, vecRoadNetLink[veCrossRoadLinkId10[i]],cv::Scalar(0,255,0),20);
	}

	vector<int> veCrossRoadLinkId11;	
	veCrossRoadLinkId11.push_back(126);	
	for (int i=0; i<veCrossRoadLinkId11.size(); i++)
	{		
		drawImage(matRoadNetImg, hamPixelCenter, vecRoadNetLink[veCrossRoadLinkId11[i]],cv::Scalar(0,255,0),20);
	}

	// 绘制主路
	vector<int> vecMainRoadLinkId;
	vecMainRoadLinkId.push_back(71);
	vecMainRoadLinkId.push_back(130);
	vecMainRoadLinkId.push_back(238);
	vecMainRoadLinkId.push_back(237);
	vecMainRoadLinkId.push_back(201);
	vecMainRoadLinkId.push_back(412);
	vecMainRoadLinkId.push_back(413);
	vecMainRoadLinkId.push_back(425);
	vecMainRoadLinkId.push_back(426);
	vecMainRoadLinkId.push_back(203);
	vecMainRoadLinkId.push_back(197);
	vecMainRoadLinkId.push_back(185);
	vecMainRoadLinkId.push_back(234);
	vecMainRoadLinkId.push_back(235);
	vecMainRoadLinkId.push_back(244);
	vecMainRoadLinkId.push_back(245);
	vecMainRoadLinkId.push_back(134);
	vecMainRoadLinkId.push_back(22);
	vecMainRoadLinkId.push_back(74);
	vecMainRoadLinkId.push_back(75);
	vecMainRoadLinkId.push_back(44);
	for (int i=0; i<vecMainRoadLinkId.size(); i++)
	{		
		drawImage(matRoadNetImg, hamPixelCenter, vecRoadNetLink[vecMainRoadLinkId[i]],cv::Scalar(255,0,0),50);
	}

	// 保存
	cv::imwrite("D:\\Halo\\ArWay\\output\\gimage\\roadNet\\0.bmp",matRoadNetImg);

	// 筛选
	vector<vector<int> > vecCrossMerge;
	vecCrossMerge.push_back(veCrossRoadLinkId1);
	vecCrossMerge.push_back(veCrossRoadLinkId2);
	vecCrossMerge.push_back(veCrossRoadLinkId3);
	vecCrossMerge.push_back(veCrossRoadLinkId4);
	vecCrossMerge.push_back(veCrossRoadLinkId5);
	vecCrossMerge.push_back(veCrossRoadLinkId6);
	vecCrossMerge.push_back(veCrossRoadLinkId7);
	vecCrossMerge.push_back(veCrossRoadLinkId8);
	vecCrossMerge.push_back(veCrossRoadLinkId9);
	vecCrossMerge.push_back(veCrossRoadLinkId10);
	vecCrossMerge.push_back(veCrossRoadLinkId11);


	Mat matSimple(matRoadNetImg.rows,matRoadNetImg.cols,CV_8UC3);
	matSimple.setTo(0);	
	for (int i=0; i<vecCrossMerge.size(); i++)
	{
		vector<int> veCrossRoadLinkId = vecCrossMerge[i];
		for (int j=0; j<veCrossRoadLinkId.size(); j++)
		{		
			drawImage(matSimple, hamPixelCenter, vecRoadNetLink[veCrossRoadLinkId[j]],cv::Scalar(0,255,0),20);
		}
	}
	for (int i=0; i<vecMainRoadLinkId.size(); i++)
	{		
		drawImage(matSimple, hamPixelCenter, vecRoadNetLink[vecMainRoadLinkId[i]],cv::Scalar(255,0,0),50);
	}
	
	

	// 保存
	cv::imwrite("D:\\Halo\\ArWay\\output\\gimage\\roadNet\\1.bmp",matSimple);
}

#endif
