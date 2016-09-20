
//#include "StdAfx.h"

#include "algorithm.h"

static int g_nIndex = 0;
int GetCrossRoadPoint(const Mat& matRoadImg, const vector<Point2i>& vecMainRoadScreenPoint, int nCenterPointIndex,
					  vector<vector<Point2i> >& vecCrossRoadPointSet)
{	
	// 参数自检
	if (!matRoadImg.data || matRoadImg.channels()<3 ||
		vecMainRoadScreenPoint.size()<=0 || nCenterPointIndex==0)
	{
		return -1;
	}

#if IS_PRINT_TIME
	#if 1		// =========test==============
		Mat matTempTest(matRoadImg.rows,matRoadImg.cols,CV_8UC3);
		int nTRet = DrawPoint(vecMainRoadScreenPoint,Scalar(0,0,255),2,matTempTest);
		if (nTRet<0)
		{
			return -1;
		}

		char cTemp[100];
		sprintf(cTemp,"//sdcard//testimage//helong//ScreenPoint-%d.bmp",g_nIndex++);
		imwrite(cTemp,matTempTest);
	#endif

	double uDuration = 0;
	clock_t ckStart, ckStart0, ckEnd;

	ckStart = clock();
	ckStart0 = ckStart;
#endif

	// 获取图像尺寸
	int nRow = matRoadImg.rows;
	int nCol = matRoadImg.cols;

	// BGR分离
	std::vector<cv::Mat>vecBGR;
	split(matRoadImg,vecBGR);

	#if IS_PRINT_TIME
		ckEnd = clock();
		uDuration = (double)(ckEnd-ckStart)/CLOCKS_PER_SEC;
		#ifndef IS_WINDOWS_VER
			LOGD("Preprocess - BGR split： %f ms", uDuration*1000);
		#else
			printf("\nPreprocess - BGR split： %f ms\n", uDuration*1000);
		#endif
	#endif

	#if IS_PRINT_TIME
		ckStart = clock();
	#endif

	// B - R
	Mat matDiffBR = vecBGR[0] - vecBGR[2];	

	// 提取蓝色（主路），及膨胀(使得二值图包含蓝色边界点)
	Mat matBlueBwI,matDilateBlueBwI;
	cv::threshold(matDiffBR,matBlueBwI,0,1,CV_THRESH_OTSU+CV_THRESH_BINARY);
	
	Mat matElement = getStructuringElement(MORPH_RECT,cv::Size(NEIGHBOUR_BOX1,NEIGHBOUR_BOX1));
	dilate(matBlueBwI,matDilateBlueBwI,matElement);

	// 提取蓝白
	Mat matBlueWhiteBwI;
	cv::threshold(vecBGR[0],matBlueWhiteBwI,0,1,CV_THRESH_OTSU+CV_THRESH_BINARY);

	#if IS_PRINT_TIME
		ckEnd = clock();
		uDuration = (double)(ckEnd-ckStart)/CLOCKS_PER_SEC;
	#ifndef IS_WINDOWS_VER
		LOGD("Preprocess - Get B & W & dilate： %f ms", uDuration*1000);
	#else
		printf("Preprocess - Get B & W & dilate： %f ms\n", uDuration*1000);
	#endif
	#endif

	#if IS_PRINT_TIME
		ckStart = clock();
	#endif

	// 开、闭运算	
	Mat matCloseI,matOpenI;	 
	morphologyEx(matBlueWhiteBwI,matCloseI,MORPH_CLOSE,matElement);
	morphologyEx(matBlueWhiteBwI,matOpenI,MORPH_OPEN,matElement);
	
	#if IS_PRINT_TIME
		ckEnd = clock();
		uDuration = (double)(ckEnd-ckStart)/CLOCKS_PER_SEC;
		#ifndef IS_WINDOWS_VER
			LOGD("Preprocess - Open & Close： %f ms", uDuration*1000);
		#else
			printf("Preprocess - Open & Close： %f ms\n", uDuration*1000);
		#endif
	#endif

// 	// 顶帽
// 	Mat matTopHatI,matBlackHatI,matTopBlackI;
// 	morphologyEx(matBlueWhiteBwI,matTopHatI,MORPH_TOPHAT,matElement);
// 	morphologyEx(matBlueWhiteBwI,matBlackHatI,MORPH_BLACKHAT,matElement);
// 	matTopBlackI = (matTopHatI|matBlackHatI);
// 	Mat matErode;
// 	erode(matBlueWhiteBwI,matErode,getStructuringElement(MORPH_RECT,cv::Size(NEIGHBOUR_BOX1,NEIGHBOUR_BOX1)));
	
	#if IS_PRINT_TIME
		ckStart = clock();
	#endif
	// 提取边界线
	Mat matTempI;
	matTempI = matDiffBR - matDiffBR.mul(matDilateBlueBwI);
	Mat matBorderBwI;
	cv::threshold(matTempI,matBorderBwI,0,1,CV_THRESH_OTSU+CV_THRESH_BINARY);
	//dilate(matBorderBwI,matDilateBorderBwI,matElement);

	// 获取虚线（存在部分噪点）
	Mat matDottedLineBwI;
	matDottedLineBwI = ((~matOpenI)&matCloseI&matBorderBwI);


	matElement = getStructuringElement(MORPH_RECT,cv::Size(NEIGHBOUR_BOX2,NEIGHBOUR_BOX2));
	dilate(matDottedLineBwI,matDottedLineBwI,matElement);


#if IS_PRINT_TIME
	ckEnd = clock();
	uDuration = (double)(ckEnd-ckStart)/CLOCKS_PER_SEC;
	double uDuration0 = (double)(ckEnd-ckStart0)/CLOCKS_PER_SEC;
	#ifndef IS_WINDOWS_VER
		LOGD("Preprocess - Get DottedLine & dilate： %f ms", uDuration*1000);
		LOGD("Preprocess total time： %f ms", uDuration0*1000);
		#if 0
			char cTemp[100];
			sprintf(cTemp,"//sdcard//testimage//helong//test-%d.bmp",g_nIndex++);
			imwrite(cTemp,matDottedLineBwI*255);
		#endif
	#else
		printf("Preprocess - Get DottedLine & dilate： %f ms\n", uDuration*1000);
		printf("Preprocess total time： %f ms\n", uDuration0*1000);

	#endif
#endif


#if IS_SHOW_PREPROCESS_IMAGE
	imshow("matRoadImg",matRoadImg);
	imshow("matCloseI",matCloseI*255);
	imshow("matOpenI",matOpenI*255);
// 	imshow("matTopHatI",matTopHatI*255);
// 	imshow("matBlackHatI",matBlackHatI*255);
// 	imshow("matTopHatI|matBlackHatI",(matTopHatI|matBlackHatI)*255);
			
	imshow("matDilateBlueBwI",(matDilateBlueBwI)*255);	
	imshow("matBlueWhiteBwI",(matBlueWhiteBwI)*255);
	imshow("matBorderBwI",(matBorderBwI)*255);
	//imshow("(~matErode)&matBlueWhiteBwI",((~matErode)&matBlueWhiteBwI)*255);

	imshow("(~matOpenI)&matCloseI&matBorderBwI",((~matOpenI)&matCloseI&matBorderBwI)*255);

	imshow("(~matBlueWhiteBwI)&matCloseI",((~matBlueWhiteBwI)&matCloseI)*255);
	imshow("matDottedLineBwI",matDottedLineBwI*255);

	//imshow("matBorderBwI",matBorderBwI*255);
	waitKey(0);
#endif

// 	double minVal = 0, maxVal=0;
//	minMaxLoc(matDottedLineI, &minVal, &maxVal);  


#if IS_PRINT_TIME
	ckStart = clock();	
#endif


	int nRet = 0;
	// 获取主路
	Mat matBlueThinBwI;
	vector<vector<Point2i> > vecMainRoadPointSet;
	vector<Point2i> vecVertexSet, vecFeaturePtSet;	
	nRet = GetMainRoadPoint(matBlueBwI, matBlueThinBwI, vecMainRoadPointSet, vecVertexSet, vecFeaturePtSet);

	if (vecVertexSet.size()<=1 || vecFeaturePtSet.size()<1)
	{
		return -1;
	}
	vector<Point2i> vecMainRoadVertexSet;
	vecMainRoadVertexSet = vecVertexSet;		// 主路顶点赋值
	vecMainRoadVertexSet[1] = vecFeaturePtSet[0];	// 将第1个点赋值为vecFeaturePtSet的第0个点，表示中心点后特征点，用于计算中心点后主路方向

#if IS_PRINT_TIME
	ckEnd = clock();
	uDuration = (double)(ckEnd-ckStart)/CLOCKS_PER_SEC;
	#ifndef IS_WINDOWS_VER
			LOGD("Get Main Road： %f ms", uDuration*1000);
	#else
			printf("Get Main Road： %f ms\n", uDuration*1000);
	#endif
#endif



#if IS_PRINT_TIME
	ckStart = clock();	
#endif

	// 获取辅路
	Mat matDottedLineThinBwI;
	vector<vector<Point2i> > vecAssistRoadPointSet;
	nRet = GetAssistRoadPoint(matDottedLineBwI, matDottedLineThinBwI, vecAssistRoadPointSet);

	if (vecAssistRoadPointSet.size()<=0)
	{
		return -1;
	}

#if IS_PRINT_TIME
	ckEnd = clock();
	uDuration = (double)(ckEnd-ckStart)/CLOCKS_PER_SEC;
	#ifndef IS_WINDOWS_VER
			LOGD("Get Assist Road： %f ms", uDuration*1000);
	#else
			printf("Get Assist Road： %f ms\n", uDuration*1000);
	#endif
#endif


#if IS_PRINT_TIME
	ckStart = clock();	
#endif


	// 求所有路径
	// 图像
	Mat matAllRoadThinBwI;	
	//matAllRoadThinBwI = (matBlueThinBwI|matDottedLineThinBwI);
	matAllRoadThinBwI = (matDilateBlueBwI|matDottedLineBwI);
	//matBlueThinBwI = matBlueThinBwI*255;
	//matDottedLineThinBwI = matDottedLineThinBwI*255;
	//matAllRoadThinBwI = matAllRoadThinBwI*255;
	// 坐标点集
	vector<vector<Point2i> > vecAllRoadPointSet;
	for (int i = 0; i < vecMainRoadPointSet.size(); i++)
	{
		vecAllRoadPointSet.push_back(vecMainRoadPointSet[i]);
	}
	for (int i = 0; i < vecAssistRoadPointSet.size(); i++)
	{
		vecAllRoadPointSet.push_back(vecAssistRoadPointSet[i]);
	}
		
	// 求非零点
	std::vector<cv::Point2i> vecNonZerosLocations; // locations of non-zero pixels 
	cv::findNonZero(matAllRoadThinBwI, vecNonZerosLocations); 

	// 求与主路有交点的路径
	vector<vector<Point2i> > vecCrossAssistSet;
	vector<bool> vecIsCrossRoad;		// 记录当前路径是否属于与主路有交点的路径	
	int nNumAssitRoad = vecAssistRoadPointSet.size();
	vecIsCrossRoad.resize(nNumAssitRoad);
	
	for (int i = 0;  i < nNumAssitRoad; i++)
	{
		vecIsCrossRoad[i] == false;
		int nTempNum = vecAssistRoadPointSet[i].size();
		for (int j = 0; j < nTempNum; j++)
		{
			Point2i pt = vecAssistRoadPointSet[i][j];;
// 			if (j==0)
// 			{
// 				pt = vecAssistRoadPointSet[i][0];
// 			}
// 			else
// 			{
// 				pt = vecAssistRoadPointSet[i][nTempNum-1];
// 			}
			int nX1 = max(0,pt.x-NEIGHBOUR_BOX4);
			int nX2 = min(pt.x+NEIGHBOUR_BOX4,nCol-1);
			int nY1 = max(0,pt.y-NEIGHBOUR_BOX4);
			int nY2 = min(pt.y+NEIGHBOUR_BOX4,nRow-1);
			
			int nCount = countNonZero(matDilateBlueBwI(Range(nY1,nY2+1),Range(nX1,nX2+1)));
						
			if (nCount>0)
			{				
				vecIsCrossRoad[i] = true;
				vecCrossAssistSet.push_back(vecAssistRoadPointSet[i]);
				break;
			}			
		}
	}

#if IS_PRINT_TIME
	ckEnd = clock();
	uDuration = (double)(ckEnd-ckStart)/CLOCKS_PER_SEC;
	#ifndef IS_WINDOWS_VER
		LOGD("Get all cross Road with main road： %f ms", uDuration*1000);
	#else
		printf("Get all cross Road with main road： %f ms\n", uDuration*1000);
	#endif
#endif

#if 1
	#if IS_PRINT_TIME
		ckStart = clock();	
	#endif

	// 针对与主路有交点的路径，在所有路径中，连接方向相同且相邻的连通域
 	vector<Point2i> vecTempAssist, vecTemp;
	
	//vector<int> vecMergeIndex;		// 记录每条与主路有交点路径可合并的候选集
	

	int nNumCrossRoad= vecCrossAssistSet.size();
	for (int i = 0; i < nNumCrossRoad; i++)
	{
		vecTempAssist = vecCrossAssistSet[i];				// 与主路有交点的辅路	
		Point2i ptStart, ptEnd;
		ptStart = vecTempAssist[0];
		ptEnd = vecTempAssist[vecTempAssist.size()-1];


		vector<Vec6i> vecNearestPoint;		// 记录每条与主路有交点路径可合并的候选集、最近点坐标和位置
		// vec5i - [候选集,最近点坐标x、y, 最近点坐标位置,起点标识]
		// 起点标识 - 针对主路有交点的路径，1：起点与候选集最近，0 - 终点
		// 最后一个数字无效

		for (int j = 0; j < nNumAssitRoad; j++)
		{
			if (!vecIsCrossRoad[j])
			{
				int nNum = vecAssistRoadPointSet[j].size();
				for (int k = 0; k < nNum; k++)
				{
					Point2i ptTemp = vecAssistRoadPointSet[j][k];
					if ((ptTemp.x>(ptStart.x-NEIGHBOUR_BOX3)&&ptTemp.x<(ptStart.x+NEIGHBOUR_BOX3)&&
						ptTemp.y>(ptStart.y-NEIGHBOUR_BOX3)&&ptTemp.y<(ptStart.y+NEIGHBOUR_BOX3)))						
					{
						vecNearestPoint.push_back(Vec6i(j,ptTemp.x,ptTemp.y,k,1,0));
						break;
					}

					if ((ptTemp.x>(ptEnd.x-NEIGHBOUR_BOX3)&&ptTemp.x<(ptEnd.x+NEIGHBOUR_BOX3)&&
						ptTemp.y>(ptEnd.y-NEIGHBOUR_BOX3)&&ptTemp.y<(ptEnd.y+NEIGHBOUR_BOX3)))
					{
						vecNearestPoint.push_back(Vec6i(j,ptTemp.x,ptTemp.y,k,0,0));
						break;
					}
				}				
			}
		}

		// 从候选集中找距离小、夹角最小的线合并，扩大	
		int nNum = vecNearestPoint.size();
		int nFlag = 0;
		double uMaxCos = -2; 
		int nSi = 0;
		for (int j = 0; j < nNum; j++)
		{
			int nIndex0 = vecNearestPoint[j].val[0];		// 候选集在总路径中的顺序
			int nIndex1 = vecNearestPoint[j].val[3];
			vecTemp = vecAssistRoadPointSet[nIndex0];
			Point2i pt;
			pt = Point2i(vecNearestPoint[j].val[1],vecNearestPoint[j].val[2]);

			Point2i pt1,pt2,pt3;		//  记录向量，用于求夹角最小的线
			if (vecNearestPoint[j].val[4]==1)		// 与起点最近
			{				
				int nInd = ((vecTempAssist.size()-1)<(NEIGHBOUR_BOX2-1))?(vecTempAssist.size()-1):(NEIGHBOUR_BOX2-1);
				pt1 = vecTempAssist[nInd];
			} 
			else			// 与终点最近
			{
				int nInd = ((vecTempAssist.size()-NEIGHBOUR_BOX2)>0)?(vecTempAssist.size()-NEIGHBOUR_BOX2):0;
				pt1 = vecTempAssist[nInd];
			}
			pt1 = pt1 - ptStart;		// 两点相减得向量

			if (nIndex1==0)
			{				
				int nInd = ((NEIGHBOUR_BOX2-1)<(vecTemp.size()-1))?(NEIGHBOUR_BOX2-1):(vecTemp.size()-1);
				pt2 = vecTemp[nInd];
				pt2 = pt2 - pt;
				pt3 = pt2;
			} 
			else if (nIndex1==vecTemp.size()-1)
			{				
				int nInd = ((vecTemp.size()-NEIGHBOUR_BOX2)>0)?(vecTemp.size()-NEIGHBOUR_BOX2):0;
				pt2 = vecTemp[nInd];
				pt2 = pt2 - pt;
				pt3 = pt2;
			}
			else
			{				
				int nInd1 = ((nIndex1+NEIGHBOUR_BOX2-1)<(vecTemp.size()-1))?(nIndex1+NEIGHBOUR_BOX2-1):(vecTemp.size()-1);
				int nInd2 = ((nIndex1-NEIGHBOUR_BOX2+1)>0)?(nIndex1-NEIGHBOUR_BOX2+1):0;
				pt2 = vecTemp[nInd1];
				pt3 = vecTemp[nInd2];
				pt2 = pt2 - pt;
				pt3 = pt3 - pt;
			}

			double uTemp1 = abs(pt1.dot(pt2))/(sqrtf(powf(pt1.x,2)+powf(pt1.y,2))*sqrtf(powf(pt2.x,2)+powf(pt2.y,2)));
			double uTemp2 = abs(pt1.dot(pt3))/(sqrtf(powf(pt1.x,2)+powf(pt1.y,2))*sqrtf(powf(pt3.x,2)+powf(pt3.y,2)));
			if (max(uTemp1,uTemp2)>uMaxCos)
			{
				uMaxCos = max(uTemp1,uTemp2);
				nSi = nIndex0;
			}
		}

		// 扩大
		if (nNum>0 && uMaxCos>COS_VALUE)
		{
			// 根据端点距离判断插入的位置
			float fTempStart1 = 0.f, fTempStart2=0.f, fTempEnd1 = 0.f, fTempEnd2 = 0.f;
			Point2i ptCrossTempStart, ptCrossTempEnd, ptAssistRoadTempStart, ptAssistRoadTempEnd;
			ptCrossTempStart = vecCrossAssistSet[i][0];
			ptCrossTempEnd = vecCrossAssistSet[i][vecCrossAssistSet[i].size()-1];
			ptAssistRoadTempStart = vecAssistRoadPointSet[nSi][0];
			ptAssistRoadTempEnd = vecAssistRoadPointSet[nSi][vecAssistRoadPointSet[nSi].size()-1];
			fTempStart1 = CalDistance(ptCrossTempStart, ptAssistRoadTempStart);
			fTempStart2 = CalDistance(ptCrossTempStart, ptAssistRoadTempEnd);
			fTempEnd1 = CalDistance(ptCrossTempEnd, ptAssistRoadTempStart);
			fTempEnd2 = CalDistance(ptCrossTempEnd, ptAssistRoadTempEnd);
			if (fTempStart1>fTempEnd1)		// 终点距离近
			{
				if (fTempStart2>fTempEnd2)		// 终点距离终点近
				{
					// 倒序重排
					nRet = DesendVector(vecAssistRoadPointSet[nSi]);
				}				

				vecCrossAssistSet[i].insert(vecCrossAssistSet[i].end(),
					vecAssistRoadPointSet[nSi].begin(),vecAssistRoadPointSet[nSi].end()); 
			} 
			else		// 起点距离近
			{
				if (fTempStart2<fTempEnd2)		// 起点距离起点近
				{
					// 倒序重排
					nRet = DesendVector(vecAssistRoadPointSet[nSi]);
				}	
				vecCrossAssistSet[i].insert(vecCrossAssistSet[i].begin(),
					vecAssistRoadPointSet[nSi].begin(),vecAssistRoadPointSet[nSi].end()); 
			}
						
		}		
	}

	#if IS_PRINT_TIME
		ckEnd = clock();
		uDuration = (double)(ckEnd-ckStart)/CLOCKS_PER_SEC;
		#ifndef IS_WINDOWS_VER
			LOGD("Merge roads： %f ms", uDuration*1000);
		#else
			printf("Merge roads： %f ms\n", uDuration*1000);
		#endif
	#endif


	#if IS_PRINT_TIME
		ckStart = clock();	
	#endif

	// 去除小离散块
	for(vector<vector<Point2i> >::iterator it=vecCrossAssistSet.begin(); it!=vecCrossAssistSet.end(); )
	{
		if((*it).size() <= SAMLL_SET_NUMBER)
		{
			it = vecCrossAssistSet.erase(it); //不能写成vecCrossAssistSet.erase(it);
		}
		else
		{
			++it;
		}
	}

	// 去除主路端点之外的辅路，根据主路垂线判断，若主路与辅路分布在垂线两侧，则删除辅路
	int nDel = NEIGHBOUR_BOX3;	
	for (int i = 0; i < 2; i++)
	{
		Point2i pt0, pt1, pt2;
		vector<Point2i> vecTemp;
		//vector<Point2i> vecTempLine;		// 用于直线拟合
		if (0==i)		// 起点
		{
			vecTemp = vecMainRoadPointSet[0];
			pt1 = vecTemp[0];
			int nInd = (nDel<(vecTemp.size()-1)?nDel:(vecTemp.size()-1));
			pt2 = vecTemp[nInd];
			//vecTempLine = vector<Point2i>(vecTemp.begin(),vecTemp.begin()+nInd);
		} 
		else
		{
			vecTemp = vecMainRoadPointSet[vecMainRoadPointSet.size()-1];
			pt1 = vecTemp[vecTemp.size()-1];
			int nTemp = vecTemp.size()-nDel;
			int nInd = (nTemp>0)?nTemp:0;
			
			pt2 = vecTemp[nInd];
			
			//vecTempLine = vector<Point2i>(vecTemp.begin()+nInd,vecTemp.end());
		}

		// 判断是否分布在垂线两侧，基于是否为钝角或接近直角作判断
		pt0 = pt1;
		Point2i pt = pt2 - pt1;		// 向量

		for (vector<vector<Point2i> >::iterator it=vecCrossAssistSet.begin(); it!=vecCrossAssistSet.end(); )
		{
			pt1 = (*it)[0];
			pt2 = (*it)[(*it).size()-1];
			pt1 = pt1 - pt0;
			pt2 = pt2 - pt0;
			double uTemp1 = (pt.dot(pt1))/(sqrtf(powf(pt.x,2)+powf(pt.y,2))*sqrtf(powf(pt1.x,2)+powf(pt1.y,2)));
			double uTemp2 = (pt.dot(pt2))/(sqrtf(powf(pt.x,2)+powf(pt.y,2))*sqrtf(powf(pt2.x,2)+powf(pt2.y,2)));
			
			if (uTemp1<=COS_ANGEL_THRESH && uTemp2<=COS_ANGEL_THRESH)
			{
				it = vecCrossAssistSet.erase(it);
			} 
			else
			{
				++it;
			}
		}
	}

	#if IS_PRINT_TIME
		ckEnd = clock();
		uDuration = (double)(ckEnd-ckStart)/CLOCKS_PER_SEC;
		#ifndef IS_WINDOWS_VER
			LOGD("Delete roads： %f ms", uDuration*1000);
		#else
			printf("Delete roads： %f ms\n", uDuration*1000);
		#endif
	#endif
#endif

	//vecCrossRoadPointSet = vecCrossAssistSet;


	int nNum = vecCrossAssistSet.size();
	if (nNum<=0)
	{
		return -1;
	}

	/*
	// ================y轴镜像====================
	for(int i=0; i<nNum; i++)
	{
		for(int j=0; j<vecCrossAssistSet[i].size(); j++)
		{
			vecCrossAssistSet[i][j].y = nRow - vecCrossAssistSet[i][j].y;
		}
	}
	//=================end,y轴镜像====================
	*/


		// 在所有中心点之前的形状点中，获取刚好满足要求的形状点下标，要求：距离中心点大于n个像素
	int nCenterPrePtIndex = 0;		// 记录中心点前第一个满足要求的形状点的下标
	float fTemp = 0.f;
	bool bFlag = false;
	Point2i ptCenter = vecMainRoadScreenPoint[nCenterPointIndex];
	for (int i = nCenterPointIndex-1; i >=0; i--)
	{
		fTemp = CalDistance(vecMainRoadScreenPoint[i],ptCenter);
		if (fTemp>=NEIGHBOUR_CENTER_ORDER)
		{
			nCenterPrePtIndex = i;
			bFlag = true;
			break;
		}				
	}
	if (!bFlag)
	{
		return -1;
	}

	Point2i ptCenterPrePt = vecMainRoadScreenPoint[nCenterPrePtIndex];
	Point2i ptScreenPointV(ptCenter.x-ptCenterPrePt.x, ptCenter.y-ptCenterPrePt.y);	// 主路屏幕中心点与前一个形状点的方向向量
	Point2i ptRoadCrossV(CROSSIMAGE_W/2-vecMainRoadVertexSet[1].x,
		CROSSIMAGE_H/2-vecMainRoadVertexSet[1].y);		// 路口放大图上中心点前主路方向向量

	// 旋转主路
	double uAngle = 0.0;
	nRet = GetAngle(ptScreenPointV, ptRoadCrossV, uAngle);		// 计算旋转角
	if (nRet<0)
	{
		return -1;
	}

	Point2i ptRotateCenter = vecMainRoadScreenPoint[nCenterPointIndex];
	nRet = RotateXYSet(vecCrossAssistSet, ptRotateCenter, -uAngle, vecCrossRoadPointSet);
	if (nRet<0)
	{
		return -1;
	}


#if 0		// =========test==============
	Mat matTempTestLine(nRow,nCol,CV_8UC3);
	matTempTestLine.setTo(0);
	nRet = DrawCrossRoadsLine(vecCrossAssistSet,matTempTestLine);
	if (nRet<0)
	{
		return -1;
	}
	Mat matTempTestLine1(nRow,nCol,CV_8UC3);
	nRet = DrawPoint(vecMainRoadScreenPoint,Scalar(0,0,255),2,matTempTestLine1);
	if (nRet<0)
	{
		return -1;
	}

	char cTemp[100];
	sprintf(cTemp,"//sdcard//testimage//helong//test-%d.bmp",g_nIndex++);
	imwrite(cTemp,matTempTestLine+matTempTestLine1);
#endif

#if IS_SHOW_CROSSROAD
	// 保存数据
	//FILE *fp;
	
	// 绘图
	Mat matCrossThinBwI(nRow,nCol,CV_8UC1);
	matCrossThinBwI.setTo(0);
	for (int i = 0; i < vecCrossAssistSet.size(); i++)
	{
		/*char cTemp[100];		
		sprintf(cTemp,"AssistRoad-%d.txt",i);
		fp = fopen(cTemp,"wb+");*/
		vector<Point2i> vecTemp = vecCrossAssistSet[i];
		for (int j = 0; j < vecTemp.size(); j++)
		{
			Point2i pt = vecTemp[j];
			uchar* p = matCrossThinBwI.ptr<uchar>(pt.y);
			*(p+pt.x) = 1;
			
			/*fseek(fp, 0, SEEK_END);
			fprintf(fp, "%d %d\n",pt.x, pt.y);*/
		}

		/*if (fp != NULL)
		{			
			fclose(fp);  
			fp = NULL;
		}*/

		/*imshow("matCrossThinBwI|matBlueThinBwI",(matCrossThinBwI|matBlueThinBwI)*255);
		waitKey(0);*/
	}
	

	Mat matAssistRoadThinBwI(nRow,nCol,CV_8UC1);
	matAssistRoadThinBwI.setTo(0);
	for (int i = 0; i < vecAssistRoadPointSet.size(); i++)
	{		
		vector<Point2i> vecTemp = vecAssistRoadPointSet[i];
		for (int j = 0; j < vecTemp.size(); j++)
		{
			Point2i pt = vecTemp[j];
			uchar* p = matAssistRoadThinBwI.ptr<uchar>(pt.y);
			*(p+pt.x) = 1;
		}
// 		imshow("matAssistRoadThinBwI|matBlueThinBwI",(matAssistRoadThinBwI|matBlueThinBwI)*255);
// 		waitKey(0);
	}

	Mat matTempLine(nRow,nCol,CV_8UC3);
	matTempLine.setTo(0);
	nRet = DrawCrossRoadsLine(vecCrossAssistSet,matTempLine);
	if (nRet<0)
	{
		return -1;
	}

 	imshow("matTempLine",matTempLine);
// 	imshow("matDottedLineBwI",matDottedLineBwI*255);
// 	imshow("matCrossThinBwI|matBlueThinBwI",(matCrossThinBwI|matBlueThinBwI)*255);
	imshow("matCrossThinBwI|matBlueThinBwI",(matCrossThinBwI|matBlueThinBwI)*255);
	imshow("matAssistRoadThinBwI|matBlueThinBwI",(matAssistRoadThinBwI|matBlueThinBwI)*255);
	waitKey(0);
#endif


// 保存路径数据
#if IS_SAVE_ROAD
	// 保存数据
	FILE *fp;
	long nTime = GetTime();
	char cTemp[100];
	#ifdef IS_WINDOWS_VER
		sprintf(cTemp,"AssistRoad-%ld.txt",nTime);
	#else
		sprintf(cTemp,"/sdcard/testimage/AssistRoad-%ld.txt",nTime);
		if(access("/sdcard/testimage/dao",0)==-1)//access函数是查看文件是不是存在
		{
			chmod("/sdcard/testimage", S_IRUSR|S_IWUSR|S_IRGRP|S_IROTH);
			if (mkdir("/sdcard/testimage/dao",0777))//如果不存在就用mkdir函数来创建
			{
				printf("creat file bag failed!!!");
			}
		}
	#endif
	
	fp = fopen(cTemp,"wt+");
	for (int i = 0; i < vecCrossAssistSet.size(); i++)
	{
		
		vector<Point2i> vecTemp = vecCrossAssistSet[i];
		for (int j = 0; j < vecTemp.size(); j++)
		{
			Point2i pt = vecTemp[j];
			fseek(fp, 0, SEEK_END);
			fprintf(fp, "%d  %d\n",pt.x, pt.y);
		}

		// 加入边界标识100000
		fseek(fp, 0, SEEK_END);
		fprintf(fp, "%d  %d\n",100000, 100000);

	}
	if (fp != NULL)
	{			
		fclose(fp);  
		fp = NULL;
	}
#endif

	return 0;
}

// 获取主路
int GetMainRoadPoint(const Mat& matMainRoadBwI, Mat& matMainRoadThinBwI, vector<vector<Point2i> >& vecMainRoadPointSet,
	vector<Point2i>& vecVertexSet, vector<Point2i>& vecFeaturePtSet)
{	
#if IS_PRINT_TIME
	clock_t ckStart,ckEnd;
	double uDuration = 0;
	ckStart = clock();	
#endif

	// 图像尺寸
	int nRow = matMainRoadBwI.rows;
	int nCol = matMainRoadBwI.cols;
	
	// 细化	
	matMainRoadThinBwI = thinImage(matMainRoadBwI);	

#if IS_PRINT_TIME
	ckEnd = clock();
	uDuration = (double)(ckEnd-ckStart)/CLOCKS_PER_SEC;
	#ifndef IS_WINDOWS_VER
		LOGD("GetMainRoadPoint - thinImage： %f ms", uDuration*1000);
	#else
		printf("GetMainRoadPoint - thinImage： %f ms\n", uDuration*1000);
	#endif
#endif

 
#if IS_PRINT_TIME	
	ckStart = clock();	
#endif

	// 求非零点
	std::vector<cv::Point2i> vecNonZerosLocations; // locations of non-zero pixels 
	cv::findNonZero(matMainRoadThinBwI, vecNonZerosLocations); 

	if (vecNonZerosLocations.size()<=0)
	{
		return -1;
	}

	// 第二次细化
	Mat matFilterThinBwI;
	vector<Point2i> vecFilterPoints;
	FilterThinBwI(matMainRoadThinBwI, vecNonZerosLocations, matFilterThinBwI, vecFilterPoints);
	#if IS_PRINT_TIME
		ckEnd = clock();
		uDuration = (double)(ckEnd-ckStart)/CLOCKS_PER_SEC;
		#ifndef IS_WINDOWS_VER
			LOGD("GetMainRoadPoint - second thin： %f ms", uDuration*1000);
		#else
			printf("GetMainRoadPoint - second thin： %f ms\n", uDuration*1000);
		#endif
	#endif

#if IS_SHOW_MAINROAD
	Mat matMergePic;
	Mat	matThinPic(matMainRoadBwI.rows,matMainRoadBwI.cols,CV_8UC3);	
	matThinPic.setTo(0);
	int nTempRet = DrawPoint(vecFilterPoints, Scalar(0,0,255), 1, matThinPic);
	
	vector<Mat> vecMergeMat;
	vecMergeMat.push_back(matMainRoadBwI*255);
	vecMergeMat.push_back(Mat::zeros(matMainRoadBwI.rows,matMainRoadBwI.cols,CV_8UC1));
	vecMergeMat.push_back(Mat::zeros(matMainRoadBwI.rows,matMainRoadBwI.cols,CV_8UC1));
	merge(vecMergeMat,matMergePic);
	matMergePic = matMergePic + matThinPic;
		
	

	namedWindow("matMainRoadThinBwI",0);
	namedWindow("matFilterThinBwI",0);
	namedWindow("matMergeThin",0);
	imshow("matMainRoadThinBwI",matMainRoadThinBwI*255);
	imshow("matFilterThinBwI",matFilterThinBwI*255);

	imshow("matMergeThin",matMergePic);

	waitKey(0);
#endif

#if IS_PRINT_TIME	
	ckStart = clock();	
#endif
	//std::vector<cv::Point2i> vecNonZerosLocations; // locations of non-zero pixels 
	vector<Point2i> vecVertexPoints;
	int nRet = 0;
	nRet = GetVertexPoints(matFilterThinBwI, vecFilterPoints, vecVertexPoints);
	if (nRet!=0)
	{
		return -1;
	}

#if IS_PRINT_TIME
	ckEnd = clock();
	uDuration = (double)(ckEnd-ckStart)/CLOCKS_PER_SEC;
	#ifndef IS_WINDOWS_VER
		LOGD("GetMainRoadPoint - GetVertexPoints： %f ms", uDuration*1000);
	#else
		printf("GetMainRoadPoint - GetVertexPoints： %f ms\n", uDuration*1000);
	#endif
#endif

	int nCount = vecVertexPoints.size();
	if (nCount<=0)
	{
		return -1;
	}

	if (nCount<=2)
	{
		vecMainRoadPointSet.push_back(vecFilterPoints);		// 没排序
		vecVertexSet = vecVertexPoints;
	}
	else
	{
		#if IS_PRINT_TIME	
			ckStart = clock();	
		#endif
		// 确定箭头方向	
		vector<Point2i> vecStartPoint, vecEndPoint, vecMainRoadPoint;		
		int nRet = 0;
		nRet = GetStartEndPoint(matFilterThinBwI,vecFilterPoints,vecVertexPoints, 
			vecStartPoint, vecEndPoint, vecMainRoadPoint);
		if (nRet!=0)
		{
			return -1;
		}

		vecVertexSet.push_back(vecStartPoint[0]);
		vecVertexSet.push_back(vecEndPoint[0]);
		
		matMainRoadThinBwI = matFilterThinBwI;

		nRet = SortPoint(vecMainRoadPoint,nRow,nCol);
		if (nRet<0)
		{
			return -1;
		}

		vecMainRoadPointSet.push_back(vecMainRoadPoint);		// 没排序

		// 从起点开始扫描，按顺序先后排序

		#if IS_PRINT_TIME
			ckEnd = clock();
			uDuration = (double)(ckEnd-ckStart)/CLOCKS_PER_SEC;
			#ifndef IS_WINDOWS_VER
				LOGD("GetMainRoadPoint - GetStartEndPoint： %f ms", uDuration*1000);
			#else
				printf("GetMainRoadPoint - GetStartEndPoint： %f ms\n", uDuration*1000);
			#endif
		#endif

				// --------------------test-----------------------
#if IS_TEST_DEBUG
				Mat_<int> matStatisticMap;
				vector<int> vecStaticValue;
				Point2i ptArrowCenter;
				int nMaxStaticVs = 0;
				int nMaxIndex = 0;

				nRet = GetStatisticValue(vecMainRoadPoint, matMainRoadBwI, matStatisticMap,	vecStaticValue, ptArrowCenter, nMaxStaticVs, nMaxIndex);				
				if (nRet<0)
				{
					return -1;
				}
#endif
				// --------------------end-------------------------
		
	}

	
	#if IS_PRINT_TIME	
		ckStart = clock();	
	#endif
	// 获取离中心点最近的主路点
	int nX1 = 0, nX2 = 0, nY1 = 0, nY2 = 0;
	int nX = nCol/2, nY = nRow/2;	// 中心点
	nX1 = max(nX-NEIGHBOUR_BOX2,0);
	nX2 = min(nX+NEIGHBOUR_BOX2,nCol-1);
	nY1 = max(0,nY-NEIGHBOUR_BOX2);
	nY2 = min(nY+NEIGHBOUR_BOX2,nRow-1);
	
	Mat matTemp = matMainRoadThinBwI(Range(nY1,nY2+1),Range(nX1,nX2+1));
	cv::findNonZero(matTemp, vecNonZerosLocations);
	
	if (vecNonZerosLocations.size()<=0)
	{
		return -1;
	}

	double uDistance = 0, uMinDis = 2*2*NEIGHBOUR_BOX2; 
	int nInd = 0;
	for (int i = 0; i < vecNonZerosLocations.size(); i++)
	{
		Point2i ptTemp = vecNonZerosLocations[i];
		ptTemp.x = nX1 + ptTemp.x;
		ptTemp.y = nY1 + ptTemp.y;

		uDistance = powf((ptTemp.x - nX),2) + powf((ptTemp.y - nY),2);    
		uDistance = sqrtf(uDistance);
		if (uDistance<uMinDis)
		{
			uMinDis = uDistance;
			nInd = i;
		}
	}
	
	Point2i ptStd = vecNonZerosLocations[nInd];		// 离中心最近的细化主路点
	ptStd.x = ptStd.x + nX1;
	ptStd.y = ptStd.y + nY1;
	vector<Point2i> vecMainRoadPoint = vecMainRoadPointSet[0];
	int nRoadPointNum = vecMainRoadPoint.size();
	nInd = 0;
	for (int i = 0; i < nRoadPointNum; i++)
	{
		if (vecMainRoadPoint[i]==ptStd)
		{
			nInd = i;
			break;
		}
	}

	// 取主路上，中心点之前第NEIGHBOUR_CENTER_ORDER个点
	if (vecMainRoadPoint[0]!=vecVertexSet[0])
	{
		nInd = nInd + NEIGHBOUR_CENTER_ORDER - 1;
		nInd = (nInd<(nRoadPointNum-1))?nInd:(nRoadPointNum-1);
	} 
	else
	{
		nInd = nInd - NEIGHBOUR_CENTER_ORDER - 1;
		nInd = (nInd>0)?nInd:0;
	}

	vecFeaturePtSet.push_back(vecMainRoadPoint[nInd]);

	#if IS_PRINT_TIME
		ckEnd = clock();
		uDuration = (double)(ckEnd-ckStart)/CLOCKS_PER_SEC;
		#ifndef IS_WINDOWS_VER
			LOGD("GetMainRoadPoint - vecFeaturePtSet： %f ms", uDuration*1000);
		#else
			printf("GetMainRoadPoint - vecFeaturePtSet： %f ms\n", uDuration*1000);
		#endif
	#endif

	return 0;
}

// 获取辅路
int GetAssistRoadPoint(const Mat& matAssistRoadBwI, Mat& matAssistRoadThinBwI, vector<vector<Point2i> >& vecAssistRoadPointSet)
{
	#if IS_PRINT_TIME
		clock_t ckStart,ckEnd;
		double uDuration = 0;
		ckStart = clock();	
	#endif
	
	// 获取图像尺寸
	int nCol = matAssistRoadBwI.cols;
	int nRow = matAssistRoadBwI.rows;
	
	//vector<int,float> t;
	// 细化	
	matAssistRoadThinBwI = thinImage(matAssistRoadBwI);

	#if IS_PRINT_TIME
		ckEnd = clock();
		uDuration = (double)(ckEnd-ckStart)/CLOCKS_PER_SEC;
		#ifndef IS_WINDOWS_VER
			LOGD("GetAssistRoadPoint - thinImage： %f ms", uDuration*1000);
		#else
			printf("GetAssistRoadPoint - thinImage： %f ms\n", uDuration*1000);
		#endif
	#endif

#if IS_SHOW_ASSISTROAD
	imshow("matAssistRoadThinBwI",matAssistRoadThinBwI*255);
	waitKey(0);
#endif


	#if IS_PRINT_TIME		
		ckStart = clock();	
	#endif
	// 求非零点
	std::vector<cv::Point2i> vecNonZerosLocations; // locations of non-zero pixels 
	cv::findNonZero(matAssistRoadThinBwI, vecNonZerosLocations); 

	if (vecNonZerosLocations.size()<=0)
	{
		return -1;
	}

	// 二次细化
	vector<Point2i> vecFilterPoints;
	Mat matFilterThinBwI;
	int nRet = FilterThinBwI(matAssistRoadThinBwI, vecNonZerosLocations, 
		matFilterThinBwI,  vecFilterPoints);

	#if IS_PRINT_TIME
		ckEnd = clock();
		uDuration = (double)(ckEnd-ckStart)/CLOCKS_PER_SEC;
		#ifndef IS_WINDOWS_VER
			LOGD("GetAssistRoadPoint - second thin： %f ms", uDuration*1000);
		#else
			printf("GetAssistRoadPoint - second thin： %f ms\n", uDuration*1000);
		#endif
	#endif

	#if IS_PRINT_TIME		
		ckStart = clock();	
	#endif
	// 打断交叉
	Mat matSingleLineBwI;
	nRet = GetSingleLine(matFilterThinBwI, vecFilterPoints, matSingleLineBwI);	

	#if IS_PRINT_TIME
		ckEnd = clock();
		uDuration = (double)(ckEnd-ckStart)/CLOCKS_PER_SEC;
		#ifndef IS_WINDOWS_VER
			LOGD("GetAssistRoadPoint - GetSingleLine： %f ms", uDuration*1000);
		#else
			printf("GetAssistRoadPoint - GetSingleLine： %f ms\n", uDuration*1000);
		#endif
	#endif

#if IS_SHOW_ASSISTROAD
	namedWindow("matSingleLineBwI",1);
	imshow("matSingleLineBwI",matSingleLineBwI*255);
	imshow("matAssistRoadBwI",matAssistRoadBwI*255);
	waitKey(0);
#endif

	#if IS_PRINT_TIME		
		ckStart = clock();	
	#endif
	// 求端点
	vector<Point2i> vecVertexPoints;
	nRet = GetVertexPoints(matSingleLineBwI,vecFilterPoints, vecVertexPoints);

	#if IS_PRINT_TIME
		ckEnd = clock();
		uDuration = (double)(ckEnd-ckStart)/CLOCKS_PER_SEC;
		#ifndef IS_WINDOWS_VER
			LOGD("GetAssistRoadPoint - GetVertexPoints： %f ms", uDuration*1000);
		#else
			printf("GetAssistRoadPoint - GetVertexPoints： %f ms\n", uDuration*1000);
		#endif
	#endif


	#if IS_PRINT_TIME		
		ckStart = clock();	
	#endif
	// 求连通域
	int nNumV = vecVertexPoints.size();
	vector<vector<Point2i> > vecSingleLine;
	vector<Point2i> vecEndPoint;
	
	for (int i = 0; i < nNumV; i++)
	{
		bool bFlag = false;
		for (int j = 0; j < vecEndPoint.size(); j++)
		{
			if (vecVertexPoints[i].x == vecEndPoint[j].x && vecVertexPoints[i].y == vecEndPoint[j].y)
			{
				bFlag = true;
				break;
			}
		}

		vector<Point2i> vecTemp;
		if (!bFlag)
		{
			Point2i ptTemp;
			nRet = GetContourPoints(matSingleLineBwI,vecVertexPoints[i],vecTemp,ptTemp);
			//int nNumTemp = vecTemp.size();
			int nDel = NEIGHBOUR_BOX3;
			if (/*!vecTemp.empty()*/ vecTemp.size()>=nDel)
			{
				// 端点外延
				for (int j = 0; j < 2; j++)
				{
					bool bFlagTemp = true;	// 循环退出标志
					Point2i pt1;
					Point2i pt2;
					vector<Point2i> vecTempLine;
					if (0==j)		// 起点
					{
						pt1 = vecVertexPoints[i];
						pt2 = vecTemp[nDel-1];
						vecTempLine = vector<Point2i>(vecTemp.begin(),vecTemp.begin()+nDel);
					} 
					else		// 终点
					{
						pt1 = ptTemp;
						pt2 = vecTemp[vecTemp.size()-nDel];
						vecTempLine = vector<Point2i>(vecTemp.end()-nDel,vecTemp.end());
					}

					// 直线拟合					
					Vec4f vfTempLine;
					fitLine(vecTempLine,vfTempLine,CV_DIST_HUBER,0,0.01,0.01);
					float fCos = vfTempLine[0], fSin = vfTempLine[1];
					float fX = vfTempLine[2], fY = vfTempLine[3];

					while(bFlagTemp && pt1.x>0 && pt1.x<nCol-1 && pt1.y>0 && pt1.y<nRow-1)
					{
						int x=0, y=0;
						if (/*pt1.x==pt2.x*/ fCos>(-1e-6) && fCos<(1e-6))		// 竖直线，斜率不存在
						{
							x = pt1.x;
							y = (pt1.y>pt2.y)?min(pt1.y+1,nRow-1):max(pt1.y-1,0);		// 外延y						
						}
						else			// 非竖直线，斜率存在
						{
							/*float k  = (pt1.y-pt2.y)/(pt1.x-pt2.x);*/
							float k  = fSin/fCos;
							
							if (fabs(k)>1)		// dy > dx
							{
								y = (pt1.y>pt2.y)?min(pt1.y+1,nRow-1):max(pt1.y-1,0);		// 外延y
								x = int(fX+(y-fY)/k);							
							}
							else			// dx >= dy
							{
								x = (pt1.x>pt2.x)?min(pt1.x+1,nCol-1):max(pt1.x-1,0);		// 外延x
								y = int(fY + k*(x-fX));
							}
						}

						int nS = 2;
						int nX1 = max(0,x-nS);
						int nX2 = min(x+nS,nCol-1);
						int nY1 = max(0,y-nS);
						int nY2 = min(y+nS,nRow-1);
						int nCount = countNonZero(matSingleLineBwI(Range(nY1,nY2+1),Range(nX1,nX2+1)));

						if ((x>=0)&&(x<nCol)&&(y>=0)&&(y<nRow)&&
							matAssistRoadBwI.at<uchar>(y,x)>0 && nCount<=1)
						{
							bFlagTemp = true;
							pt1 = Point2i(x, y);							
							pt2 = vecTemp[nDel-1];

							if (0==j)
							{
								vecTemp.insert(vecTemp.begin(),pt1);		// 端点外延
							} 
							else
							{
								vecTemp.push_back(pt1);		// 端点外延
								ptTemp = pt1;
							}
						}
						else
						{
							bFlagTemp = false;
						}
					}
					
				}
				vecSingleLine.push_back(vecTemp);
				vecEndPoint.push_back(ptTemp);
				//=====--------------
				
			}
		}
	}

	#if IS_PRINT_TIME
		ckEnd = clock();
		uDuration = (double)(ckEnd-ckStart)/CLOCKS_PER_SEC;
		#ifndef IS_WINDOWS_VER
			LOGD("GetAssistRoadPoint - GetContourPoints： %f ms", uDuration*1000);
		#else
			printf("GetAssistRoadPoint - GetContourPoints： %f ms\n", uDuration*1000);
		#endif
	#endif
	
	vecAssistRoadPointSet = vecSingleLine;

	nNumV = vecAssistRoadPointSet.size();
	if (nNumV<=0)
	{
		return -1;
	}

	// 排序
	vector<Point2i> vecTemp;
	for (int i = 0; i < nNumV; i++)
	{
		vecTemp = vecAssistRoadPointSet[i];
		nRet = SortPoint(vecTemp, nRow,  nCol);
		if (nRet<0)
		{
			return -1;
		}
	}
	
	return 0;
}

// 基于种子点获取所有连通的点
// Point2i ptSeedPoint - 种子点，要求是端点
int GetContourPoints(const Mat& matThinBwI,Point2i ptSeedPoint,vector<Point2i>& vecContourPoint,Point2i& ptEndPoint)
{
	// 获取图像尺寸
	int nRow = matThinBwI.rows;
	int nCol = matThinBwI.cols;

	Mat matTemp = matThinBwI;
	
	// 扫描点
	int nX = ptSeedPoint.x;
	int nY = ptSeedPoint.y;
	int nPreX = nX;
	int nPreY = nY;
	bool bFlag = true;		// 退出while循环的标志
	int nStep = matThinBwI.step;
	//vector<Point2i> vecContourPoint;	
	while(1)
	{
		if (!bFlag)
		{
			ptEndPoint = Point2i(nX,nY);
			break;
		}

		vecContourPoint.push_back(Point2i(nX,nY));

		bFlag = false;

		uchar* p = matTemp.ptr<uchar>(nY);		
		for (int i = 0; i < 8; i++)
		{
			int nR = i/3;
			int nC = i%3;
			if (i  == 4 || nX+(nC-1)<0 || nX+(nC-1)>=nCol || nY+(nR-1)<0 ||  nY+(nR-1)>=nRow)
			{
				continue;
			}

			int nTempX = nX + (nC-1);
			int nTempY = nY+(nR-1);
			if(*(p+(nR-1)*nStep+nX+(nC-1))>0 && (nPreX!=nTempX||nPreY!=nTempY))
			{
				nPreX = nX;
				nPreY = nY;
				nX = nTempX;
				nY = nTempY;
				bFlag = true;
				break;
			}

		}
	} 

	return 0;
}

// 根据距离，确定起点和箭头点
int GetStartEndPoint(const Mat& matMainRoadThinBwI, const vector<Point2i>& vecAllPoints,const vector<Point2i>& vecVertexPoints,
	vector<Point2i>& vecStartPoint, vector<Point2i>& vecEndPoint, vector<Point2i>& vecMainRoadPoint)
{
	int nCount = vecVertexPoints.size();
	if (nCount<=2)
	{
		return -1;
	}
	
	// 图像尺寸
	int nRow = matMainRoadThinBwI.rows;
	int nCol = matMainRoadThinBwI.cols;

	// 计算距离
	vector<Vec2i> vecDis;		// 记录距离，[dis,id]
	int nTemp = 0;
	int nX = 0, nY = 0;
	int nX0 = vecVertexPoints[0].x;
	int nY0 = vecVertexPoints[0].y;
	for (int i = 1; i < nCount; i++)
	{
		nX = vecVertexPoints[i].x;
		nY = vecVertexPoints[i].y;
		nTemp = (nX-nX0)*(nX-nX0) + (nY-nY0)*(nY-nY0);
		nTemp = int(sqrtf(nTemp)+0.5);
		vecDis.push_back(Vec2i(nTemp,i));
	}
	// 排序(降幂)
	sort(vecDis.begin(),vecDis.end(),sortFun);

	// 确定起点
	vector<Point2i> vecArrowPoint;
	if (vecDis[nCount-2][0]>VERTEX_DISTANCE)
	{		
		vecStartPoint.push_back(vecVertexPoints[0]);
		vecArrowPoint = vecVertexPoints;
		vecArrowPoint.erase(vecArrowPoint.begin());
	}
	else
	{
		int nSi = vecDis[0][1];
		vecStartPoint.push_back(vecVertexPoints[nSi]);
		vecArrowPoint = vecVertexPoints;
		vecArrowPoint.erase(vecArrowPoint.begin()+nSi);
	}

	// 确定箭头区域
	int nX1 = vecArrowPoint[0].x, nX2 = vecArrowPoint[0].x, nY1 = vecArrowPoint[0].y, nY2 = vecArrowPoint[0].y;
	int nTempX = 0, nTempY = 0;
	for (int i = 1; i < nCount-1; i++)
	{	
		nTempX = vecArrowPoint[i].x;
		nTempY = vecArrowPoint[i].y;
		nX1 = (nX1>nTempX)?nTempX:nX1;
		nX2 = (nX2<nTempX)?nTempX:nX2;
		nY1 = (nY1>nTempY)?nTempY:nY1;
		nY2 = (nY2<nTempY)?nTempY:nY2;
	}
	// 箭头区域置0，方便后续取过渡端点
	//Mat matTemp = matMainRoadThinBwI.clone();
	Mat matTemp = matMainRoadThinBwI.clone();
	matTemp(Range(nY1,nY2+1),Range(nX1,nX2+1)).setTo(0);

	// 区域放大一倍，方便取过渡端点
	nX1 = max(nX1-(nX2-nX1)/2,0);
	nX2 = min(nX2+(nX2-nX1)/2,nCol-1);
	nY1 = max(nY1-(nY2-nY1)/2,0);
	nY2 = min(nY2+(nY2-nY1)/2,nRow-1);
	

	// 求非零点
	std::vector<cv::Point2i> vecTempNonZerosLocations; // locations of non-zero pixels
	cv::findNonZero(matTemp(Range(nY1,nY2+1),Range(nX1,nX2+1)), vecTempNonZerosLocations); 

	int nRet = 0;
	vector<Point2i> vecTempVertexPoints;
	nRet = GetVertexPoints(matTemp(Range(nY1,nY2+1),Range(nX1,nX2+1)), vecTempNonZerosLocations, vecTempVertexPoints);

	// 求离区域中心最近的端点
	nCount = vecTempVertexPoints.size();
	if (nCount!=2)
	{
		return -1;
	}
	
	vecTempVertexPoints[0].x = vecTempVertexPoints[0].x+nX1;		// 还原坐标
	vecTempVertexPoints[0].y = vecTempVertexPoints[0].y+nY1;
	Point2i ptTempCenVertex = Point2i(vecTempVertexPoints[0].x,vecTempVertexPoints[0].y);
	int nMinDis = (ptTempCenVertex.x-(nX2-nX1)/2)^2 + (ptTempCenVertex.y-(nY2-nY1)/2)^2;
	//int nTemp = 0;
	for (int i = 1; i < nCount; i++)
	{
		vecTempVertexPoints[i].x = vecTempVertexPoints[i].x + nX1;		// 还原坐标
		vecTempVertexPoints[i].y = vecTempVertexPoints[i].y+nY1; 
		nTemp = (vecTempVertexPoints[i].x-(nX2-nX1)/2)^2 + (vecTempVertexPoints[i].y-(nY2-nY1)/2)^2;
		if (nTemp < nMinDis)
		{
			ptTempCenVertex = vecTempVertexPoints[i];
			nMinDis = nTemp;
		}
	}

	// 确定终点，与两个过渡端点连线的夹角最小，利用余弦计算
	Point2i ptTemp1,ptTemp2;
	double uMaxCos = -2; 
	int nSi = 0;
	for (int i = 0; i < vecArrowPoint.size(); i++)
	{
		ptTemp1 = Point2i(vecArrowPoint[i].x-vecTempVertexPoints[0].x,vecArrowPoint[i].y-vecTempVertexPoints[0].y);
		ptTemp2 = Point2i(vecArrowPoint[i].x-vecTempVertexPoints[1].x,vecArrowPoint[i].y-vecTempVertexPoints[1].y);

		double uTemp = abs(ptTemp1.dot(ptTemp2))/(sqrtf(powf(ptTemp1.x,2)+powf(ptTemp1.y,2))*sqrtf(powf(ptTemp2.x,2)+powf(ptTemp2.y,2)));
		if (uTemp>uMaxCos)
		{
			uMaxCos = uTemp;
			nSi = i;
		}
	}

	vecEndPoint.clear();
	vecEndPoint.push_back(vecArrowPoint[nSi]);

	// test
	line( matTemp, ptTempCenVertex, vecEndPoint[0], Scalar(1), 1, 0/*LINE_AA*/);

// 	//  确定划线的点	
// 	nX1 = min(ptTempCenVertex.x,vecEndPoint[0].x);
// 	nX2 = max(ptTempCenVertex.x,vecEndPoint[0].x);
// 	nY1 = min(ptTempCenVertex.y,vecEndPoint[0].y);
// 	nY2 = max(ptTempCenVertex.y,vecEndPoint[0].y);

// 	vecTempNonZerosLocations.clear();
// 	cv::findNonZero(matTemp(Range(nY1,nY2+1),Range(nX1,nX2+1)), vecTempNonZerosLocations); 
// 
// 	for (int i = 0; i<vecTempNonZerosLocations.size(); i++)
// 	{
// 		vecTempNonZerosLocations[i].x = vecTempNonZerosLocations[i].x+nX1;
// 		vecTempNonZerosLocations[i].y = vecTempNonZerosLocations[i].y+nY1;
// 	}

	// 确保中间点在8邻域内只有两个点

	// 扫描点
	nX = vecStartPoint[0].x;
	nY = vecStartPoint[0].y;
	int nPreX = nX;
	int nPreY = nY;
	bool bFlag = true;		// 退出while循环的标志
	int nStep = matTemp.step;
	/*vector<Point2i> vecMainRoadPoint;	*/

	while(1)
	{
		if (!bFlag)
		{
			break;
		}

		vecMainRoadPoint.push_back(Point2i(nX,nY));		

		bFlag = false;
		
		uchar* p = matTemp.ptr<uchar>(nY);
		p[nX] = 0;
		for (int i = 0; i < 8; i++)
		{
			int nR = i/3;
			int nC = i%3;
			if (i  == 4 || nX+(nC-1)<0 || nX+(nC-1)>=nCol || nY+(nR-1)<0 ||  nY+(nR-1)>=nRow)
			{
				continue;
			}
			
			int nTempX = nX + (nC-1);
			int nTempY = nY+(nR-1);
			if(*(p+(nR-1)*nStep+nX+(nC-1))>0 && (nPreX!=nTempX||nPreY!=nTempY))
			{
				nPreX = nX;
				nPreY = nY;
				nX = nTempX;
				nY = nTempY;					

				bFlag = true;
				break;
			}
		}

// 		if (vecMainRoadPoint.size()>vecAllPoints.size())
// 		{
// 			return -1;
// 		}
	} 

#if IS_SHOW_MAINROAD
	imshow("matT",matMainRoadThinBwI*255);
	imshow("matTemp",matTemp*255);
	waitKey(0);
#endif

	return 0;
}

bool sortFun (Vec2i vec1,Vec2i vec2) 
{ 
	return (vec1[0]>vec2[0]); 
}

// 打断交叉点
int GetSingleLine(const Mat& matThinBwI,const vector<cv::Point2i>& vecNonZerosLocations, Mat& matSingleLineBwI)
{
	// 获取尺寸
	int nRow = matThinBwI.rows;
	int nCol = matThinBwI.cols;
	
	if (vecNonZerosLocations.size()<=0)
	{
		return -1;
	}

	matSingleLineBwI = matThinBwI.clone();

	// 求端点
	//vector<Point2i> vecVertexPoints;
	int nCount = 0;
	std::vector<cv::Point2i> vecTempPoints; 
	for (int i = 0; i < vecNonZerosLocations.size(); i++)
	{
		int x = vecNonZerosLocations[i].x;
		int y = vecNonZerosLocations[i].y;
		int x1 = max(x-1,0);
		int x2 = min(x+1,nCol-1);
		int y1 = max(y-1,0);
		int y2 = min(y+1,nRow-1);
		// 		cv::findNonZero(matThinBwI(Range(y1,y2+1),Range(x1,x2+1)), vecTempPoints); 
		// 		nCount = vecTempPoints.size();
		nCount = countNonZero(matThinBwI(Range(y1,y2+1),Range(x1,x2+1)));

		// 端点
		if (nCount>3/*||x==0||x==(nCol-1)||y==0||y==(nRow-1)*/)
		{
			matSingleLineBwI(Range(y1,y2+1),Range(x1,x2+1)).setTo(Scalar::all(0));
			
		}		
	}

	return 0;
}


// 求顶点
int GetVertexPoints(const Mat& matThinBwI,const vector<cv::Point2i>& vecNonZerosLocations, vector<Point2i>& vecVertexPoints)
{
	// 参数自检
	if (!matThinBwI.data || vecNonZerosLocations.size()<=0)
	{
		return -1;
	}
	
	// 获取尺寸
	int nRow = matThinBwI.rows;
	int nCol = matThinBwI.cols;
	
	// 求端点
	int nCount = 0;
	std::vector<cv::Point2i> vecTempPoints; 
	for (int i = 0; i < vecNonZerosLocations.size(); i++)
	{
		int x = vecNonZerosLocations[i].x;
		int y = vecNonZerosLocations[i].y;
		int x1 = max(x-1,0);
		int x2 = min(x+1,nCol-1);
		int y1 = max(y-1,0);
		int y2 = min(y+1,nRow-1);
// 		cv::findNonZero(matThinBwI(Range(y1,y2+1),Range(x1,x2+1)), vecTempPoints); 
// 		nCount = vecTempPoints.size();
		nCount = countNonZero(matThinBwI(Range(y1,y2+1),Range(x1,x2+1)));

		// 端点
		if (nCount==2/*||x==0||x==(nCol-1)||y==0||y==(nRow-1)*/)
		{
			vecVertexPoints.push_back(Point2i(x,y));
		}
	}

	return 0;
}

// 过滤thinImage操作后的点
int FilterThinBwI(const Mat& matThinBwI, const vector<cv::Point2i>& vecNonZerosLocations, 
	Mat& matFilterThinBwI, vector<Point2i>& vecFilterPoints)
{
	matFilterThinBwI = matThinBwI.clone();
	
	// 获取尺寸
	int nRow = matThinBwI.rows;
	int nCol = matThinBwI.cols;

	int nCount = vecNonZerosLocations.size();
	int nX = 0, nX1 = 0, nX2 = 0, nY = 0, nY1 = 0, nY2 = 0;
	int nCountX = 0, nCountY = 0;
	int nStep = matThinBwI.step;
	for (int i = 0; i < nCount; i++)
	{
		nCountX = 0;
		nCountY = 0;

		nX = vecNonZerosLocations[i].x;
		nY = vecNonZerosLocations[i].y;
		nX1 = nX-1;
		nX2 = nX+1;
		nY1 = nY-1;
		nY2 = nY+1;

		
		if (nX1 >= 0)
		{
			uchar* p = matFilterThinBwI.ptr<uchar>(nY);
			nCountX = nCountX + *(p+nX1);
		}
		if (nX2 < nCol)
		{
			uchar* p = matFilterThinBwI.ptr<uchar>(nY);
			nCountX = nCountX + *(p+nX2);
		}
		if (nY1 >= 0)
		{
			uchar* p = matFilterThinBwI.ptr<uchar>(nY1);
			nCountY = nCountY + *(p+nX);
		}
		if (nY2 < nRow)
		{
			uchar* p = matFilterThinBwI.ptr<uchar>(nY2);
			nCountY = nCountY + *(p+nX);
		}
		
		// 判断是否删除
		if (nCountX>0 && nCountY>0)
		{
			uchar* p = matFilterThinBwI.ptr<uchar>(nY);
			*(p+nX) = 0;
		}
		else
		{
			vecFilterPoints.push_back(vecNonZerosLocations[i]);
		}
	}


	return 0;
}

/**
 * @brief 对输入图像进行细化
 * @param src为输入图像,用cvThreshold函数处理过的8位灰度图像格式，元素中只有0与1,1代表有元素，0代表为空白
 * @param maxIterations限制迭代次数，如果不进行限制，默认为-1，代表不限制迭代次数，直到获得最终结果
 * @return 为对src细化后的输出图像,格式与src格式相同，元素中只有0与1,1代表有元素，0代表为空白
 */
static cv::Mat thinImage(const cv::Mat & src, const int maxIterations)
{
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
				//if (p1 < 1) continue;
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
				//if (p1 < 1) continue;
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
    
    
    return dst;
}

int SortPoint(vector<Point2i>& vecLinePoint,int nRow, int nCol)
{
	// 参数自检
	if (vecLinePoint.size()<=0 || nRow<=0 || nCol<=0)
	{
		return -1;
	}
	
	
	// 初始化图像
	Mat matTemp(nRow,nCol,CV_8UC1);
	matTemp.setTo(0);

	// 将线上的点集赋值到图像上
	int nX = 0, nY = 0;
	uchar* p = NULL;
	int nNum = vecLinePoint.size();
	for (int i = 0; i < nNum; i++)
	{
		nX = vecLinePoint[i].x;
		nY = vecLinePoint[i].y;
		p = matTemp.ptr<uchar>(nY);
		*(p+nX) = 1;
	}

	// 寻找端点
	int nRet = 0;
	vector<Point2i> vecVertexPoints;		// 记录顶点，有且仅有两个
	nRet = GetVertexPoints(matTemp,vecLinePoint, vecVertexPoints);
	if (nRet < 0 || vecVertexPoints.size()!=2)
	{
		return -1;
	}

	// 确定起点(以离中心点远近作为判据，近判为起点，否则为终点)
	Point2i ptCen(nCol/2,nRow/2);
	float fDistance1 = CalDistance(ptCen, vecVertexPoints[0]);
	float fDistance2 = CalDistance(ptCen, vecVertexPoints[1]);
	
	Point2i ptStart;
	ptStart = (fDistance1<fDistance2)?vecVertexPoints[0]:vecVertexPoints[1];

	// 排序
	vector<Point2i> vecSortPoints;
	vecSortPoints.push_back(ptStart);
	nX = ptStart.x;
	nY = ptStart.y;
	int nX1=0, nY1=0, nX2=0, nY2=0;
	int nDel = 1;
	while (true)
	{
		p = matTemp.ptr<uchar>(nY);
		*(p+nX) = 0;
		
		nX1 = max(nX-nDel,0);
		nX2 = min(nX+nDel,nCol-nDel);
		nY1 = max(nY-nDel,0);
		nY2 = min(nY+nDel,nRow-nDel);

		vector<Point2i> vecTemp;
		cv::findNonZero(matTemp(Range(nY1,nY2+1),Range(nX1,nX2+1)), vecTemp);

		if (vecTemp.size()>0)
		{
			nX = vecTemp[0].x + nX1;
			nY = vecTemp[0].y + nY1;
			vecSortPoints.push_back(Point2i(nX,nY));
		}
		else
		{
			break;
		}				
	}

	vecLinePoint.clear();
	vecLinePoint = vecSortPoints;

	return 0;
}

// 逆序重排
int DesendVector(vector<Point2i>& vecPoint)
{
	int nNum = vecPoint.size();

	// 参数自检
	if (nNum<=0)
	{
		return -1;
	}
	
	Point2i ptTemp;
	for (int i = 0; i <= nNum/2; i++)
	{
		ptTemp = vecPoint[i];
		vecPoint[i] = vecPoint[nNum-1-i];
		vecPoint[nNum-1-i] = ptTemp;
	}
	
	return 0;
}

// 计算两点距离
float CalDistance(Point2i pt1, Point2i pt2)
{
	float fDistance = 0.0f;
	//fDistance = (pt1.x-pt2.x)*(pt1.x-pt2.x) + (pt1.y-pt2.y)*(pt1.y-pt2.y);
	fDistance = powf((pt1.x - pt2.x),2) + powf((pt1.y - pt2.y),2);    
	fDistance = sqrt(fDistance);

	return fDistance;
}

long GetTime()
{
#ifdef IS_WINDOWS_VER
	return GetTickCount();
#else
	double uTime = static_cast<double>(getTickCount());

	return (long)uTime;
#endif
	//return 0;
}

// 平移、过滤点
int FilterPoint(const vector<Point2i>& vecPoint, vector<Point2i>& vecTranPoint)
{
	// 参数自检
	if (vecPoint.size()<=0)
	{
		return -1;
	}
	
	int nNum = vecPoint.size();
	Point2i ptTemp;
	for (int i = 0; i < nNum; i++)
	{
		ptTemp = vecPoint[i];
		ptTemp.x = ptTemp.x + CROSSIMAGE_W/2;
		ptTemp.y = ptTemp.y + CROSSIMAGE_H/2;
		if (ptTemp.x>=0 && ptTemp.x<CROSSIMAGE_W &&
			ptTemp.y>=0 && ptTemp.y<CROSSIMAGE_H)
		{
			vecTranPoint.push_back(ptTemp);
		}
	}

	return 0;
}

// 计算两个向量的夹角，逆时针为正，顺时针为负
int GetAngle(Point2i ptV1,Point2i ptV2, double& uAngle)
{
	//uAngle = abs(ptV1.dot(ptV2))/(sqrtf(powf(ptV1.x,2)+powf(ptV1.y,2))*sqrtf(powf(ptV2.x,2)+powf(ptV2.y,2)));
	
	uAngle = atan2(ptV1.y, ptV1.x) - atan2(ptV2.y, ptV2.x);
	uAngle = -uAngle;		// y轴为竖直向下，故旋转方向取负，即顺时针为正，逆时针为负
	
	return 0;
}

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
int RotateMat(const Mat& matSrcI, Mat& matRotateI, double uAngle)
{
	Point2f ptCen(matSrcI.cols/2., matSrcI.rows/2.);		// 中心点
	Mat matR = cv::getRotationMatrix2D(ptCen, uAngle*180/CV_PI, 1.0);		// 旋转矩阵
	cv::warpAffine(matSrcI, matRotateI, matR, matSrcI.size());
	
	return 0;
}

// 绘制路径
int DrawLine(const vector<Point2i>& vecPoint, Scalar scColorV, int nLineW, Mat& matPic)
{
	// 参数自检
	if (vecPoint.size()<=0 || vecPoint.size()<=0 ||
		matPic.rows<=0 || matPic.cols<=0)
	{
		return -1;
	}

	//matMergeI.create(CROSSIMAGE_H,CROSSIMAGE_W,CV_8UC1);
	//matPic.setTo(0);

	// mainRoad
	int nNum = vecPoint.size();	
	Point2i ptStart,ptEnd;
	int nLineType = 8;
	for (int i = 1; i < nNum; i++)
	{
		ptStart = Point2i(vecPoint[i-1].x,vecPoint[i-1].y);
		ptEnd = Point2i(vecPoint[i].x,vecPoint[i].y);
		line(matPic,ptStart,ptEnd,scColorV,nLineW,nLineType);
	}
	
	return 0;
}

int DrawPoint(const vector<Point2i>& vecPoint, Scalar scColorV, int nPointSize, Mat& matPic)
{
	// 参数自检
	if (vecPoint.size()<=0 || vecPoint.size()<=0 ||
		matPic.rows<=0 || matPic.cols<=0)
	{
		return -1;
	}

	
	//matPic.setTo(0);
	
	int nNum = vecPoint.size();	
	Point2i ptTemp;
	int nLineType = 8;
	for (int i = 0; i < nNum; i++)
	{		
		ptTemp = Point2i(vecPoint[i].x,vecPoint[i].y);		
		circle(matPic,ptTemp,nPointSize,scColorV,-1);

	}

	return 0;
}

// 绘制岔路
int DrawCrossRoadsLine(const vector<vector<Point2i> >& vecCrossRoadPoint,
			   Mat& matCrossRoadLine)
{
	// 参数自检
	if (vecCrossRoadPoint.size()<=0 || vecCrossRoadPoint.size()<=0 ||
		matCrossRoadLine.rows<=0 || matCrossRoadLine.cols<=0)
	{
		return -1;
	}
	
	//matMergeI.create(CROSSIMAGE_H,CROSSIMAGE_W,CV_8UC1);
	//matCrossRoadLine.setTo(0);

	// CrossRoad	
	int nNum = vecCrossRoadPoint.size();
	Scalar scColorV(255,255,255);
	int nLineW = 3;
	int nRet = 0;
	vector<Point2i> vecTemp;
	for (int i = 0; i < nNum; i++)
	{		
		vecTemp = vecCrossRoadPoint[i];
		//nRet = DrawLine(vecTemp, scColorV, nLineW, matCrossRoadLine);
		nRet = DrawPoint(vecTemp, scColorV, nLineW, matCrossRoadLine);
		if (nRet<0)
		{
			return -1;
		}
	}

	return 0;
}

// 合并图像
int MergeMat(const vector<Mat>& vecMatImages, Mat& matMergeImage)
{
	// 参数自检
	int nNum = vecMatImages.size();
	if (nNum<=0 || nNum>=IMAGE_SHOW_NUM)
	{
		return -1;
	}

	// 求最大宽、高，最小通道数，用于定义每个单位图的大小及通道
	int nTempH=0, nTempW=0, nTempChannel=0;
	int nMaxRow=vecMatImages[0].rows, nMaxCol=vecMatImages[0].cols;
	int nMinChannel = vecMatImages[0].channels();
	for (int i = 1; i < nNum; i++)
	{
		nTempH = vecMatImages[i].rows;
		nTempW = vecMatImages[i].cols;
		nTempChannel = vecMatImages[i].channels();
		if (nTempH>nMaxRow)
		{
			nMaxRow = nTempH;
		}
		if (nTempW>nMaxCol)
		{
			nMaxCol = nTempW;
		}
		if (nTempChannel<nMinChannel)
		{
			nMinChannel = nTempChannel;
		}
	}

	// 定义大图的尺寸
	int nRow=0, nCol=0;
	if (nNum<IMAGE_SHOW_NUM_BYROW)
	{
		nRow = nMaxRow;
		nCol = nNum * nMaxCol;
	} 
	else
	{
		int nTempNum = ((nNum-1)/IMAGE_SHOW_NUM_BYROW)+1;
		nRow = nTempNum*nMaxRow;
		nCol = nMaxCol*IMAGE_SHOW_NUM_BYROW;
	}
	if (nMinChannel==1)
	{
		matMergeImage.create(nRow,nCol,CV_8UC1);
	} 
	else if(nMinChannel==3)
	{
		matMergeImage.create(nRow,nCol,CV_8UC3);
	}
	else
	{
		return -1;
	}
	
	matMergeImage.setTo(0);

	// 利用ROI赋值
	Mat matROI;
	Mat matTemp;
	Rect rtRoi;
	int nRi=0,nCi=0, nStep=IMAGE_SHOW_NUM_BYROW;
	for (int i = 0; i < nNum; i++)
	{
		nRi = i/nStep;
		nCi = i%nStep;
		rtRoi = Rect(nCi*nMaxCol,nRi*nMaxRow,nMaxRow,nMaxCol);
		//matROI = matMergeImage(Range(nRi*nMaxRow,(nRi+1)*nMaxRow-1),Range(nCi*nMaxCol,(nCi+1)*nMaxCol-1));
		matROI = matMergeImage(rtRoi);
		matTemp = vecMatImages[i];
		if (nMinChannel==1 && matTemp.channels()==3)
		{
			Mat matGrayTemp;
			cvtColor(matTemp,matGrayTemp,CV_BGR2GRAY);
			matTemp = matGrayTemp;
		}
		
		matTemp.copyTo(matROI);

	}
	
	return 0;
}

#ifdef IS_WINDOWS_VER
// write xml
int WriteXML()  
{  
	FileStorage fs("test.xml", FileStorage::WRITE);  

	fs << "frameCount" << 5;  
	time_t rawtime; time(&rawtime);  
	fs << "calibrationDate" << asctime(localtime(&rawtime));  
	Mat cameraMatrix = (Mat_<double>(3,3) << 1000, 0, 320, 0, 1000, 240, 0, 0, 1);  
	Mat distCoeffs = (Mat_<double>(5,1) << 0.1, 0.01, -0.001, 0, 0);  
	fs << "cameraMatrix" << cameraMatrix << "distCoeffs" << distCoeffs;  
	fs << "features" << "[";  
	for( int i = 0; i < 3; i++ )  
	{  
		int x = rand() % 640;  
		int y = rand() % 480;  
		uchar lbp = rand() % 256;  

		fs << "{:" << "x" << x << "y" << y << "lbp" << "[:";  
		for( int j = 0; j < 8; j++ )  
			fs << ((lbp >> j) & 1);  
		fs << "]" << "}";  
	}  
	fs << "]";  
	fs.release();  
	return 0;  
}

int ReadXML(string str)
{
	TiXmlDocument doc(str.c_str());
	if (!doc.LoadFile()) 
	{
		return -1;
	}

	TiXmlHandle hDoc(&doc);
	TiXmlElement* pElem;
	TiXmlHandle hRoot(0);

	//pElem=hDoc.FirstChildElement().Element();
	////pElem=hDoc.Element();

	//// should always have a valid root but handle gracefully if it does
	//if (!pElem)
	//{
	//	return -1;
	//}
	//
	//// save this for later
	//hRoot = TiXmlHandle(pElem);

	hRoot = hDoc;

	pElem=hRoot.FirstChild("crossImages").FirstChild().Element();	
	TiXmlHandle hTemp(0);
	TiXmlElement* pElemTemp;
	for( pElem; pElem; pElem=pElem->NextSiblingElement())
	{		
		hTemp = TiXmlHandle(pElem);
		pElemTemp = hTemp.FirstChild("bitmapFileName").Element();
		const char *pFNameTemp=pElemTemp->GetText();
			
		
		int nSi = 0;
		nSi = str.find_last_of("\\");
		string strPath = str.substr(0,nSi+1);
		string strImage = strPath + "image\\" + pFNameTemp;
		if( (_access(strImage.c_str(), 2)) == -1 )	// 文件不存在
		{
			continue;
		}
		else
		{
			// 导入图像
			cv::Mat matSrcImg = cv::imread(strImage,1);
			if (!matSrcImg.data)
			{
				continue;
			}

			int nRet = 0;	

			// 提取并保存虚线图
		#if IS_TEST_DEBUG
			// 模板过滤图像，获取虚线图			
			vector<Point2i> vecMainRoadPt;
			vector<vector<Point2i> > vecAssistRoadPt;
			Mat matMainRoadImageBw, matDotLinesImageBw;
			nRet = GetAllRoads(matSrcImg, vecMainRoadPt, vecAssistRoadPt, 
				matMainRoadImageBw, matDotLinesImageBw);
			if (nRet<0)
			{
				continue;
			}


			/*Mat matFilterImage;
			nRet = FilterImageByTemplate(matSrcImg, matFilterImage);
			if (nRet<0)
			{
			continue;
			}			
			matFilterImage = matFilterImage*255;*/

			Mat matTempMerge;
			vector<Mat> vecMatTemp;
			vecMatTemp.push_back(matDotLinesImageBw);
			vecMatTemp.push_back(matDotLinesImageBw);
			vecMatTemp.push_back(matDotLinesImageBw);
			merge(vecMatTemp,matTempMerge);

			vecMatTemp.clear();
			vecMatTemp.push_back(matSrcImg);
			vecMatTemp.push_back(matTempMerge);
			nRet = MergeMat(vecMatTemp, matTempMerge);			
			if (nRet<0)
			{
				continue;
			}

			#ifdef IS_WINDOWS_VER				
				// 放在公共的路径，方便统一查看
				string strDotLineTemp = "D:\\work\\光晕\\data\\主路屏幕点&路口放大图\\MergeDotLineImages\\";
				if(access(strDotLineTemp.c_str(),0)==-1)//access函数是查看文件是不是存在
				{
					CreateDirectory(strDotLineTemp.c_str(),NULL); //创建文件夹
				}
				strDotLineTemp = strDotLineTemp + pFNameTemp;
				imwrite(strDotLineTemp,matTempMerge);
				continue;		// 测试用，需删除========
			#endif
		#endif

			// 获取主路中心点			
			pElemTemp = hTemp.FirstChild("centerPoint").FirstChild("screenPoint").FirstChild().Element();			
			Point2i ptCenter;
			nRet = GetNodeXY(pElemTemp,ptCenter);
			if (nRet<0)
			{
				continue;
			}

			// 获取主路中心点后一个相邻的形状点			
			pElemTemp = hTemp.FirstChild("centerNextPoint").FirstChild("screenPoint").FirstChild().Element();			
			Point2i ptCenterNextPt;
			nRet = GetNodeXY(pElemTemp,ptCenterNextPt);
			if (nRet<0)
			{
				continue;
			}

			// 获取中心点下标			
			pElemTemp = hTemp.FirstChild("centerPoint").FirstChild("pointIndex").Element();
			const char* pTemp = NULL;	
			pTemp = pElemTemp->GetText();
			int nCenterPtIndex = atoi(pTemp);
			nCenterPtIndex = nCenterPtIndex - 1;		 // ===测试用，为了纠正数据上的错误，需删除====

			// 获取主路屏幕点
			vector<Point2i> vecMainRoadSreenPoint;
			pElemTemp = hTemp.FirstChild("screenPoints").Element();
			nRet = GetMainRoadScreenPoint(pElemTemp, vecMainRoadSreenPoint);
			if (nRet<0)
			{
				continue;
			}

			// 获取岔路
			vector<vector<Point2i>> vecCrossPointSet;
			vector<Point2i> vecMainRoadVertexSet;

			//vector<vector<Point2i> > vecMainRoadScreenPoint;
			nRet = GetCrossRoadPoint(matSrcImg, vecMainRoadSreenPoint, nCenterPtIndex, vecCrossPointSet);
			if (nRet<0)
			{
				continue;
			}

			//// 旋转主路			
			//Point2i ptV1(ptCenterNextPt.x-ptCenter.x,
			//	ptCenterNextPt.y-ptCenter.y);	// 主路屏幕点中中心点后主路方向向量
			//Point2i ptV2(vecMainRoadVertexSet[1].x-CROSSIMAGE_W/2,
			//	vecMainRoadVertexSet[1].y-CROSSIMAGE_H/2);		// 路口放大图上中心点后主路方向向量
			//
			//double uAngle = 0.0;
			//nRet = GetAngle(ptV1, ptV2, uAngle);		// 计算旋转角
			Mat matMainRoad(CROSSIMAGE_H,CROSSIMAGE_W,CV_8UC3);
			matMainRoad.setTo(0);
			int nLineW = 3;
			nRet = DrawLine(vecMainRoadSreenPoint, Scalar(0,0,255), nLineW, matMainRoad);
			if (nRet<0)
			{
				continue;
			}
			// 画中心点
			vector<Point2i> vecPt;
			vecPt.push_back(ptCenter);			
			nRet = DrawPoint(vecPt, Scalar(0,255,255), nLineW, matMainRoad);
			if (nRet<0)
			{
				continue;
			}
			vecPt.clear();
			if (nCenterPtIndex-1>=0)
			{
				vecPt.push_back(vecMainRoadSreenPoint[nCenterPtIndex-1]);
			}
			if (nCenterPtIndex-2>=0)
			{
				vecPt.push_back(vecMainRoadSreenPoint[nCenterPtIndex-2]);
			}
			nRet = DrawPoint(vecPt, Scalar(255,255,0), nLineW, matMainRoad);
			if (nRet<0)
			{
				continue;
			}

			
			
			// 绘制岔路
			Mat matCrossRoad(CROSSIMAGE_H,CROSSIMAGE_W,CV_8UC3);
			matCrossRoad.setTo(0);
			nRet = DrawCrossRoadsLine(vecCrossPointSet,matCrossRoad);
			if (nRet<0)
			{
				continue;
			}

			Mat matRotateCrossRoad, matRotateMainRoad;
			Mat matMergeRoad;
		//#if 1		// 以路口放大图为参考
		//	nRet = RotateMat(matMainRoad, matRotateMainRoad,  -uAngle);
		//	if (nRet<0)
		//	{
		//		continue;
		//	}
		//	// 叠加主路和岔路
		//	matMergeRoad = matRotateMainRoad + matCrossRoad;
		//#else		// 以屏幕点主路图为参考
		//	nRet = RotateMat(matCrossRoad, matRotateCrossRoad,  uAngle);
		//	if (nRet<0)
		//	{
		//		continue;
		//	}
		//	// 叠加主路和岔路
		//	matMergeRoad = matMainRoad + matRotateCrossRoad;
		//#endif
		//	
			
			
			// 叠加主路和岔路
			matMergeRoad = matMainRoad + matCrossRoad;

			// 同一窗口显示多图			
			vector<Mat> vecMats;
			vecMats.push_back(matSrcImg);
			vecMats.push_back(matCrossRoad);
			vecMats.push_back(matMainRoad);			
			vecMats.push_back(matMergeRoad);

			Mat matMergeI;
			nRet = MergeMat(vecMats, matMergeI);
			if (nRet<0)
			{
				continue;
			}

			#if IS_SHOW_DEBUG_TEST		// 显示				
				imshow("matMergeI",matMergeI);				
				waitKey(0);
			#endif

			// 保存
			#if IS_SAVE_MERGEROAD
				string strSave = strPath + "imageMerge//";
				string strSaveFName = strSave + pFNameTemp;
				#ifdef IS_WINDOWS_VER
					 CreateDirectory(strSave.c_str(),NULL); //创建文件夹

					 // 放在公共的路径，方便统一查看
					 string strTemp = "D:\\work\\光晕\\data\\主路屏幕点&路口放大图\\MergeImages\\";
					 if(access(strTemp.c_str(),0)==-1)//access函数是查看文件是不是存在
					 {
						 CreateDirectory(strTemp.c_str(),NULL); //创建文件夹
					 }
					 strTemp = strTemp + pFNameTemp;
					 imwrite(strTemp,matMergeI);
					 
					 
				#else					
					if(access(strSave.c_str(),0)==-1)//access函数是查看文件是不是存在
					{
						chmod(strPath.c_str(), S_IRUSR|S_IWUSR|S_IRGRP|S_IROTH);
						if (mkdir(strSave.c_str(),0777))//如果不存在就用mkdir函数来创建
						{
							printf("creat file bag failed!!!");
						}
					}
				#endif
				imwrite(strSaveFName,matMergeI);
			#endif
		}
		
	}

	return 0;
}

// 获取一幅路口放大图的主路屏幕点
int GetMainRoadScreenPoint(TiXmlElement* pElement, 
						   vector<Point2i>& vecMainRoadPoint)
{
	TiXmlHandle hTemp(0);
	TiXmlElement* pElemTemp;
	TiXmlElement* pElemTemp1;
	hTemp = TiXmlHandle(pElement);
	pElemTemp = hTemp.FirstChild("screenPoint").Element();
	int nRet = 0;
	int nX=0, nY=0;
	Point2i ptTemp;
	for( pElemTemp; pElemTemp; pElemTemp=pElemTemp->NextSiblingElement())
	{			
		pElemTemp1 = pElemTemp->FirstChildElement();
		nRet = GetNodeXY(pElemTemp1,ptTemp);
		
		vecMainRoadPoint.push_back(ptTemp);
	}

	return 0;
}

// 获取节点的x、y坐标
int GetNodeXY(TiXmlElement* pElement, Point2i& ptXY)
{
	int nX=0, nY=0;
	const char* pTemp = NULL;	
	pTemp = pElement->GetText();
	nX = atoi(pTemp);

	pElement = pElement->NextSiblingElement();
	pTemp = pElement->GetText();
	nY = atoi(pTemp);
	ptXY = Point2i(nX,nY);

	return 0;
}
#endif

/* // 统计每个点邻域内的非零点个数
int GetStatisticValue(const vector<Point2i>& vecPoint, const Mat& matBw, Mat_<int>& matStatisticMap,
					  vector<int>& vecStatisticValue, vector<int>& vecCenterDis,
					  Point2i& ptMaxStatisticPt, int& nMaxStatisticVs, int& nMaxIndex)
{
	int nNum = vecPoint.size();

	// 参数自检
	if (nNum<=0 || (!matBw.data))
	{
		return -1;
	}
	
	int nRet = 0;

	// 图像宽、高，及定义统计表格尺寸、赋初值
	int nRow = matBw.rows, nCol = matBw.cols;
	matStatisticMap = Mat_<int>(nRow, nCol, nRow*nCol);		// 定义尺寸，及赋初值为nRow*nCol，白色像素点对应真实的统计值，黑色像素点不参与统计，为初值（一个很大的值）

	// 计算	
	Point2i ptTemp;
	int nX1=0, nX2=0, nY1=0, nY2=0, nCount=0;
	int* pStatisticMapTemp = NULL;		// 用于指向matStatisticMap的元素

	nMaxStatisticVs = 0;		// 记录最大值
	int nDel = 15;
	vecStatisticValue = vector<int>(nNum,nRow*nCol+1);	// 定义大小，并赋初值，初值为一个很大的值，不可能达到
	vector<float> vecDisCentroid(nNum,nRow*nCol+1);			// 定义大小，并赋初值，记录每个点覆盖窗口的重心离窗口中心的距离
	for (int i = 0;  i < nNum; i++)
	{		
		ptTemp = vecPoint[i];

		if (matBw.ptr<uchar>(ptTemp.y)[ptTemp.x]<=0)
		{
			continue;
		}

		nX1 = max(0,ptTemp.x-nDel);
		nX2 = min(ptTemp.x+nDel,nCol-1);
		nY1 = max(0,ptTemp.y-nDel);
		nY2 = min(ptTemp.y+nDel,nRow-1);
		//nCount = countNonZero(matBw(Range(nY1,nY2+1),Range(nX1,nX2+1)));
		vector<Point2i> vecTemp;
		cv::findNonZero(matBw(Range(nY1,nY2+1),Range(nX1,nX2+1)), vecTemp);
		nCount = vecTemp.size();		// 之前做了判断，nCount不会为0
		
		//vecStatisticValue.push_back(nCount);
		vecStatisticValue[i] = nCount;

		

		// 求重心及重心离窗口中心的距离
		float fTempDis = 0.0f;
		Point2i ptTempCentroid;
		nRet = GetCenterPt(vecTemp, ptTempCentroid);
		if (nRet<0)
		{
			return -1;
		}
		fTempDis = CalDistance(ptTempCentroid,Point2i(nDel,nDel));
		vecDisCentroid[i] = fTempDis;
	
		pStatisticMapTemp = matStatisticMap.ptr<int>(ptTemp.y);
		pStatisticMapTemp[ptTemp.x] = nCount;		// 给统计表格赋值

		if (nCount>0 && nCount>=nMaxStatisticVs)
		{
			if (nCount==nMaxStatisticVs)		// 相等时，求重心，选择距离覆盖中心近的那个点
			{
				nMaxIndex = (vecDisCentroid[i]<vecDisCentroid[nMaxIndex])?i:nMaxIndex;
			} 
			else
			{				
				nMaxIndex = i;
			}
			nMaxStatisticVs = nCount;
		}
		
	}
	ptMaxStatisticPt = vecPoint[nMaxIndex];

	return 0;
} */


// 获取主路辅路
/* int GetAllRoads(const Mat& matSrcImage, vector<Point2i>& vecMainRoadPt, vector<vector<Point2i> >& vecAssistRoadPt, 
				Mat& matMainRoadImageBw, Mat& matDotLinesImageBw)
{
	// 参数自检
	if (!matSrcImage.data)
	{
		return -1;
	}
	
	// ==================================预处理=======================================
	// BGR分离
	std::vector<cv::Mat>vecBGR;
	split(matSrcImage,vecBGR);	

	// 二值化，提取蓝白
	Mat matBlueWhiteBwI;
	cv::threshold(vecBGR[0], matBlueWhiteBwI,0,1, CV_THRESH_OTSU+CV_THRESH_BINARY);

	// B - R
	Mat matDiffBR = vecBGR[0] - vecBGR[2];	

	// 提取蓝色（主路）
	Mat matBlueBwI;
	cv::threshold(matDiffBR,matBlueBwI,0,1,CV_THRESH_OTSU+CV_THRESH_BINARY);
	// ==================================end,预处理=======================================

	int nRet = 0;
	int nRow = matSrcImage.rows, nCol = matSrcImage.cols;

#if IS_SHOW_DEBUG_TEST
	imshow("matBlueWhiteBwI",matBlueWhiteBwI*255);
	imshow("matBlueBwI",matBlueBwI*255);
	waitKey(0);
#endif

	// ==================================获取主路=======================================
	vector<Point2i> vecMainRoadCenterLinePt;
	int nCenterPointIndex = 0;
	nRet = GetMainRoadCenterLine(matBlueBwI, vecMainRoadCenterLinePt, nCenterPointIndex);
	if (nRet<0)
	{
		return -1;
	}

#if IS_SHOW_DEBUG_TEST
	int nLineW = 2;
	Mat matMainRoadPic(nRow, nCol, CV_8UC3);
	matMainRoadPic.setTo(0);
	nRet = DrawLine(vecMainRoadCenterLinePt, Scalar(0,0,255), nLineW, matMainRoadPic);
	if (nRet < 0)
	{
		return -1;
	}
	imshow("matMainRoadPic",matMainRoadPic);
	waitKey(0);
#endif	
	// ==================================end, 获取主路=======================================

	// ==================================获取辅路（虚线）====================================
	Mat matFilterImage;
	nRet = FilterImageByTemplate(matBlueWhiteBwI, matDotLinesImageBw);
	if (nRet<0)
	{
		return -1;
	}

	// ==================================end, 获取辅路（虚线）====================================

	return 0;
}
 */
// 通过模板匹配过滤图像
int FilterImageByTemplate(const Mat& matRoadBw, Mat& matFilterImage)
{
	// 参数自检
	if (!matRoadBw.data)
	{
		return -1;
	}

	// 二值化，提取蓝白
	Mat matBlueWhiteBwI = matRoadBw.clone();
	
	int nRet = 0;

	// 模板过滤、记录每个像素的连通域序号及连通域大小
	int nTempsize = NEIGHBOUR_BOX6;
	int nRow = matBlueWhiteBwI.rows, nCol = matBlueWhiteBwI.cols, nStep = matBlueWhiteBwI.step;
	int nX1=0, nX2=0, nY1=0, nY2=0;
	int nCount = 0;
	float fRate = 0.f;
	matFilterImage.create(nRow,nCol,CV_8UC1);
	matFilterImage.setTo(0);		// 赋初值
	uchar *pTempBlueWhiteBw = NULL, *pTempFilterImage = NULL;
	int *pTempContourMap = NULL;
	
	Mat matTempRoi;
	int nRowCol = nRow*nCol;
	Mat_<int> matContourMap(nRow,nCol,nRowCol);		// 记录每个像素点对应的连通域序号，赋初值为一个很大的数nRow*nCol，表示不连通
	vector<Point2i> vecDotLinePt;		// 记录虚线点坐标
	vector<int> vecContourSize;			// 记录每个连通域的大小，与连通域序号对应
	vector<vector<Point2i> > vecContourSet;	// 记录连通域集
	vector<vector<int> > vecEqualIndex;		// 记录可以合并的连通域
	
	int nCurMaxIndex = -1;					// 记录当前连通域的最大序号，注意初值是-1，在循环中有自加，方便第一次自加结果为0
	for (int i = 0; i < nRow; i++)
	{		
		pTempBlueWhiteBw = matBlueWhiteBwI.ptr<uchar>(i);
		pTempFilterImage = matFilterImage.ptr<uchar>(i);
		pTempContourMap = matContourMap.ptr<int>(i);
		nY1 = max(0,i-nTempsize/2);
		nY2 = min(i+nTempsize/2,nRow-1);		
		for (int j = 0; j < nCol; j++)
		{
			// 过滤点，以黑点作为目标点
			if (pTempBlueWhiteBw[j]==0)
			{
				nX1 = max(j-nTempsize/2,0);
				nX2 = min(j+nTempsize/2,nCol-1);			

				matTempRoi = matBlueWhiteBwI(Range(nY1,nY2+1),Range(nX1,nX2+1));
				nCount = countNonZero(matTempRoi);
				
				// 赋值
				fRate = (float)nCount/(nTempsize*nTempsize);
				if (fRate>=VALID_POINT_RATE)
				{
					pTempFilterImage[j] = 1;
					vecDotLinePt.push_back(Point2i(j,i));		// 记录虚线点
				}
			}

			// 基于8邻域像素点，检测连通域及记录连通域序号，以黑点作为目标点	
			int nPreRow = max(0,i-1);		// 上一行
			int nPreCol = max(0,j-1);		// 上一列
			int nNextRow = min(i+1,nRow-1);		// 下一行
			int nNextCol = min(j+1,nCol-1);		// 下一列
			matTempRoi = matContourMap(Range(nPreRow,nNextRow+1),Range(nPreCol,nNextCol+1));
			double uMinv = 0.0;			
			minMaxIdx(matTempRoi,&uMinv);
			int nMinIndex = cvRound(uMinv);		

			for (int kr = nPreRow; kr <= nNextRow; kr++)
			{
				for (int kc = nPreCol; kc <= nNextCol; kc++)
				{
					// 获取图像点
					if (*(pTempBlueWhiteBw+(kr-i)*nStep+kc)==0)
					{
						if (nMinIndex==nRowCol)		// 判断是否与初值相等，确定新连通域
						{
							nCurMaxIndex = nCurMaxIndex + 1;
							nMinIndex = nCurMaxIndex;

							// 重分配vector空间
							vecContourSize.resize(nCurMaxIndex+1);
							vecContourSet.resize(nCurMaxIndex+1);
							vecEqualIndex.resize(nCurMaxIndex+1);

							vecEqualIndex[nMinIndex].push_back(nMinIndex);		// 第一个值
						}

						if (*(pTempContourMap+(kr-i)*nStep+kc) != nMinIndex)		// 避免重复计算
						{
							if (*(pTempContourMap+(kr-i)*nStep+kc) != nRowCol)
							{
								int nTempMapV = *(pTempContourMap+(kr-i)*nStep+kc);

								if (!IsBelongToVector(vecEqualIndex[nMinIndex],nTempMapV))
								{
									vecEqualIndex[nMinIndex].push_back(nTempMapV);		// 记录需合并的集合序号
									vecEqualIndex[nTempMapV].push_back(nMinIndex);		// 记录需合并的集合序号
								}								
							} 
							
							// 给连通域map赋值
							*(pTempContourMap+(kr-i)*nStep+kc) = nMinIndex;		// 此时nCurIndex>=0，否则程序是错的

							// 更新连通域的元素个数
							vecContourSize[nMinIndex] = vecContourSize[nMinIndex] + 1;
							vecContourSet[nMinIndex].push_back(Point2i(kc,kr));
						}						
					}
				}
			}
		}
	}

	// 合并连通域
	vector<vector<int> > vecMergeSet;
	nRet = MergeVector(vecEqualIndex, vecMergeSet);
	if (nRet<0 || vecMergeSet.size()<=0)
	{
		return -1;
	}

	// 更新连通域
	vector<int> vecContourSizeTemp;			// 记录每个连通域的大小
	vector<vector<Point2i> > vecContourSetTemp;	// 记录连通域集
	int nMergeNums = vecMergeSet.size();
	for (int i = 0; i < nMergeNums; i++)
	{
		vector<Point2i> vecTempPt;
		vector<int> vecTemp = vecMergeSet[i];
		int nTempNum = vecTemp.size();
		int nSi=0, nTempSize=0;
		for (int j = 0; j < nTempNum; j++)
		{
			nSi = vecTemp[j];
			vecTempPt.insert(vecTempPt.end(),vecContourSet[nSi].begin(),vecContourSet[nSi].end());
			nTempSize = nTempSize + vecContourSet[nSi].size();
		}
		vecContourSetTemp.push_back(vecTempPt);
		vecContourSizeTemp.push_back(nTempSize);
	}
	vecContourSet.clear();
	vecContourSize.clear();
	vecContourSet = vecContourSetTemp;
	vecContourSize = vecContourSizeTemp;

	// 更新Map表
	matContourMap.setTo(nRowCol);		// 重新赋值
	int nContourNum = vecContourSet.size();
	for (int i = 0; i < nContourNum; i++)
	{
		vector<Point2i> vecTempPt;
		vecTempPt = vecContourSet[i];
		int nTempNum = vecTempPt.size();
		for (int j = 0; j < nTempNum; j++)
		{
			Point2i ptTemp = vecTempPt[j];
			pTempContourMap = matContourMap.ptr<int>(ptTemp.y);
			pTempContourMap[ptTemp.x] = i;		// 更新
		}
	}


#if IS_SHOW_DEBUG_TEST
	imshow("matBlueWhiteBwI",matBlueWhiteBwI*255);

//	Mat matTempPic(nRow,nCol,CV_8UC1);	
//	matTempPic.setTo(0);
//	for (int i = 0; i < vecContourSet.size(); i++)
//	{
//		vector<Point2i> vecTempPt = vecContourSet[i];
//		for (int j = 0; j < vecTempPt.size(); j++)
//		{
//			Point2i ptTemp = vecTempPt[j];
//			pTempBlueWhiteBw = matTempPic.ptr<uchar>(ptTemp.y);
//			pTempBlueWhiteBw[ptTemp.x] = 1;
//		}
//
//// 		imshow("matTempPic",matTempPic*255);
//// 		waitKey(0);
//	}
//
	//imwrite("D:\\work\\光晕\\data\\主路屏幕点&路口放大图\\testBW.bmp",matBlueWhiteBwI*255);
	//imwrite("D:\\work\\光晕\\data\\主路屏幕点&路口放大图\\test.bmp",matFilterImage*255);

	imshow("matFilterImage0",matFilterImage*255);
#endif

	// 去除道路间的细小夹角、夹线
	Point2i ptTemp;
	int nInd = 0;		// 记录连通域的序号
	nCount = 0;			// 记录连通域的元素个数
	for(vector<Point2i>::iterator it=vecDotLinePt.begin(); it!=vecDotLinePt.end(); )
	{
		ptTemp = (*it);
		pTempContourMap = matContourMap.ptr<int>(ptTemp.y);		// 指向连通域map
		nInd = pTempContourMap[ptTemp.x];		// 连通域的序号
		pTempFilterImage = matFilterImage.ptr<uchar>(ptTemp.y);		// 指向过滤后的图像
		if (nInd>nMergeNums)
		{
			// 删除点
			it = vecDotLinePt.erase(it);
			// 更新图像
			pTempFilterImage[ptTemp.x] = 0;
			continue;
		}
		
		nCount = vecContourSize[nInd];			// 连通域的大小		

		if(nCount>SINGLE_DOTLINE_AREA)
		{
			// 删除点
			it = vecDotLinePt.erase(it);

			// 更新图像
			pTempFilterImage[ptTemp.x] = 0;
		}
		else
		{
			++it;
		}
	}
	
#if IS_SHOW_DEBUG_TEST
	//imwrite("D:\\work\\光晕\\data\\主路屏幕点&路口放大图\\test1.bmp",matFilterImage*255);
	imshow("matFilterImage1",matFilterImage*255);
	waitKey(0);
#endif

	return 0;
}

// 合并vector
int MergeVector(const vector<vector<int> >& vecSrcSet, vector<vector<int> >& vecMergeSet)
{
	// 参数自检
	int nNum = vecSrcSet.size();
	if (nNum<=0)
	{
		return -1;
	}

	// 求每个子集元素的最小值
	vector<int> vecFlag;		// 记录每个子集元素的最小值，用于标识合并序号
	vecFlag.resize(nNum,-nNum);		// 定义大小，且赋初值为负数，方便后续赋值
	vector<int> vecTemp;
	double uMinTemp=0.0;
	for (int i = 0; i < nNum; i++)
	{
		vecTemp = vecSrcSet[i];		
		cv::minMaxIdx(Mat(vecTemp),&uMinTemp);
		vecFlag[i] = cvRound(uMinTemp);
	}

	// 更新，同一连通域使用一个label，使用合并前的最小序号
	for (int i = nNum-1; i >0; i--)
	{
		int nLoop = i;
		int nTempFlag = vecFlag[nLoop];		
		while (nTempFlag!=nLoop)
		{
			nLoop = vecFlag[nTempFlag];
			nTempFlag = vecFlag[nLoop];
		}
		vecFlag[i] = nLoop;
	}
	
	// 合并
	vector<bool> vecIsDo;
	vecIsDo.resize(nNum,false);		// 记录当前位置是否已处理，false - 否，true - 是	
	for (int i = 0; i < nNum; i++)
	{
		if (!vecIsDo[i])
		{	
			vecTemp.clear();
			vecTemp.push_back(i);
			for (int j = i+1; j < nNum; j++)
			{				
				if (vecFlag[j]==vecFlag[i])
				{
					vecTemp.push_back(j);
					vecIsDo[j] = true;
				}			
			}
			vecIsDo[i] = true;
			vecMergeSet.push_back(vecTemp);
		}		
	}


	return 0;
}

// 判断元素是否在vector中，true 在，false - 不在
bool IsBelongToVector(const vector<int> &vecInts, int nElement)
{
	int nNum = vecInts.size();
	if (nNum<=0)
	{
		return false;
	}

	bool bRet = false;
	for (int i = 0; i<nNum; i++)
	{
		if (nElement==vecInts[i])
		{
			bRet = true;
			break;
		}		
	}
	
	return bRet;
}

// 围绕中心点，旋转坐标点集
int RotateXYSet(const vector<vector<Point2i> >& vecPointSet, Point2i ptRotateCenter, 
				double uAngle, vector<vector<Point2i> >& vecRotatePointSet)
{
	// 参数自检
	int nNum = vecPointSet.size();
	if (nNum<=0)
	{
		return -1;
	}

	vector<Point2i> vecTemp;
	int nRet = 0;
	for (int i = 0; i < nNum; i++)
	{
		vecTemp.clear();
		nRet = RotateXY(vecPointSet[i], ptRotateCenter, uAngle, vecTemp);
		if (nRet<0)
		{
			return -1;
		}
		vecRotatePointSet.push_back(vecTemp);
	}

	return 0;
}

// 围绕中心点，旋转坐标
int RotateXY(const vector<Point2i>& vecPoint, Point2i ptRotateCenter, double uAngle, vector<Point2i>& vecRotatePoint)
{
	// 参数自检
	int nNum = vecPoint.size();
	if (nNum<=0)
	{
		return -1;
	}
	
	// 旋转角正余弦值
	float fCosV = cos(uAngle);
	float fSinV = sin(uAngle);
	Point2i ptTemp0, ptTemp1;
	for (int i = 0; i < nNum; i++)
	{
		ptTemp0 = vecPoint[i];

		// 中心化后旋转
		ptTemp1.x = (ptTemp0.x-ptRotateCenter.x)*fCosV - (ptTemp0.y-ptRotateCenter.y)*fSinV;
		ptTemp1.y = (ptTemp0.x-ptRotateCenter.x)*fSinV + (ptTemp0.y-ptRotateCenter.y)*fCosV;

		// 去中心化
		ptTemp1.x = ptTemp1.x + ptRotateCenter.x;
		ptTemp1.y = ptTemp1.y + ptRotateCenter.y;

		// 基于图像尺寸，选择性插入坐标
		if (ptTemp1.x>=0 && ptTemp1.x<CROSSIMAGE_W &&
			ptTemp1.y>=0 && ptTemp1.y<CROSSIMAGE_H)
		{
			vecRotatePoint.push_back(ptTemp1);
		}

	}
	
	return 0;
}

// 获取主路中心线坐标，从起点到终点已排好序
/* int GetMainRoadCenterLine(const Mat& matMainRoadBW, vector<Point2i>& vecCenterLinePt, int& nCenterPointIndex)
{
	// 参数自检
	if (!matMainRoadBW.data)
	{
		return -1;
	}

	// 宽、高
	int nCol = matMainRoadBW.cols;
	int nRow = matMainRoadBW.rows;

	// 获取所有非零点坐标	
	std::vector<cv::Point2i> vecNonZerosPts; // locations of non-zero pixels 
	cv::findNonZero(matMainRoadBW, vecNonZerosPts); 
	int nNonZerosNum = vecNonZerosPts.size();
	if (nNonZerosNum<=0)
	{
		return 1;
	}

	// 滑窗遍历所有非零点，记录窗口覆盖的有效点个数，建立map表，并求有效点个数的最大值，最大值位置处即为箭头的重心，也为中心线终点
	vector<int> vecStatisticValue;
	Mat_<int> matStatisticMap;
	Point2i ptArrowCenter;
	int nMaxStatisticVs = 0, nMaxIndex = 0, nRet = 0;
	nRet = GetStatisticValue(vecNonZerosPts, matMainRoadBW, matStatisticMap, 
		vecStatisticValue, ptArrowCenter, nMaxStatisticVs, nMaxIndex);
	if (nRet<0)
	{
		return -1;
	}
		

	// 从终点开始，间隔采样，查表求最大值，并删除已取过点的区域，保存每次的最大值点位置，得中心线
	int nStep = SAMPLE_STEP;
	Mat matTemp = matMainRoadBW.clone();
	vector<Point2i> vecSampleMaxVPt;		// 记录每次采用所得最大统计值对应的点
	vecSampleMaxVPt.clear();
	vecSampleMaxVPt.push_back(ptArrowCenter);	// 第一次
	uchar* pTempBw = NULL;
	int* pTempMap = NULL;
	//bool bLoopFlag = true;		// 循环标识	
	int nTempMax = 0;
	Point2i ptTempMax = ptArrowCenter;
	while (true)
	{	
		// 记录遍历范围
		int nX1 = max(0, ptTempMax.x-nStep), nX2 = min(ptTempMax.x+nStep, nCol-1);
		int nY1 = max(0, ptTempMax.y-nStep), nY2 = min(ptTempMax.y+nStep, nRow-1);
		
		int nCount = 0;		// 记录满足要求点个数
		nTempMax = 0;
		
		// 在[nX1, nX2]范围内，遍历第nY1、nY2行
		for (int i = 0; i < 2; i++)
		{
			int nSi = 0;
			if (i==0)
			{
				nSi = nY1;		// 第nY1行
			}
			else
			{
				nSi = nY2;		// 第nY2行
			}
			pTempBw = matTemp.ptr<uchar>(nSi);
			pTempMap = matStatisticMap.ptr<int>(nSi);
			for (int j = nX1; j <= nX2; j++)
			{
				if (pTempBw[j]>0)
				{
					if (pTempMap[j]>nTempMax)
					{
						nTempMax = pTempMap[j];
						ptTempMax = Point2i(j,nSi);		// 注意顺序
					}
					nCount = nCount + 1;
				}
			}
		}

		// 在[nY1, nY2]范围内，遍历第nX1、nX2列
		for (int i = nY1; i <= nY2; i++)
		{
			pTempBw = matTemp.ptr<uchar>(i);
			pTempMap = matStatisticMap.ptr<int>(i);
			for (int j = 0; j < 2; j++)
			{
				int nSi = 0;
				if (0==j)
				{
					nSi = nX1;		// 第nX1列
				}
				else
				{
					nSi = nX2;		// 第nX2列
				}
				if (pTempBw[j]>0)
				{
					if (pTempMap[j]>nTempMax)
					{
						nTempMax = pTempMap[j];
						ptTempMax = Point2i(nSi,j);
					}
					nCount = nCount + 1;
				}				
			}
		}

		if (nCount<=0)
		{
			break;
		}

		// 插入值
		vecSampleMaxVPt.push_back(ptTempMax);

		// 将[nX1, nX2], [nY1, nY2]范围内的点置0
		Rect rtRoi(nX1,nY1,nX2-nX1+1,nY2-nY1+1);
		matTemp(rtRoi) = 0;
	}

	// 逆序重排

	nRet = DesendVector(vecSampleMaxVPt);
	if (nRet < 0)
	{
		return -1;
	}
	vecCenterLinePt = vecSampleMaxVPt;

	return 0;
}

// 求vector<Point2i>的中心坐标
int GetCenterPt(const vector<Point2i>& vecPoints, Point2i& ptCenter)
{ 
	int nNum = vecPoints.size();
	// 参数自检
	if (nNum<=0)
	{
		return -1;
	}

	float fTempX = 0.0f, fTempY = 0.0f;
	for (int i = 0; i < nNum; i++)
	{
		fTempX = fTempX + vecPoints[i].x;
		fTempY = fTempY + vecPoints[i].y;
	}
	ptCenter.x = cvRound(fTempX/nNum);
	ptCenter.y = cvRound(fTempY/nNum);

	return 0;
}*/

// Two-Pass算法
// void icvprCcaByTwoPass(const cv::Mat& _binImg, cv::Mat& _lableImg)  
// {  
// 	// connected component analysis (4-component)  
// 	// use two-pass algorithm  
// 	// 1. first pass: label each foreground pixel with a label  
// 	// 2. second pass: visit each labeled pixel and merge neighbor labels  
// 	//   
// 	// foreground pixel: _binImg(x,y) = 1  
// 	// background pixel: _binImg(x,y) = 0  
// 
// 
// 	if (_binImg.empty() ||  
// 		_binImg.type() != CV_8UC1)  
// 	{  
// 		return ;  
// 	}  
// 
// 	// 1. first pass  
// 
// 	_lableImg.release() ;  
// 	_binImg.convertTo(_lableImg, CV_32SC1) ;  
// 
// 	int label = 1 ;  // start by 2  
// 	std::vector<int> labelSet ;  
// 	labelSet.push_back(0) ;   // background: 0  
// 	labelSet.push_back(1) ;   // foreground: 1  
// 
// 	int rows = _binImg.rows - 1 ;  
// 	int cols = _binImg.cols - 1 ;  
// 	for (int i = 1; i < rows; i++)  
// 	{  
// 		int* data_preRow = _lableImg.ptr<int>(i-1) ;  
// 		int* data_curRow = _lableImg.ptr<int>(i) ;  
// 		for (int j = 1; j < cols; j++)  
// 		{  
// 			if (data_curRow[j] == 1)  
// 			{  
// 				std::vector<int> neighborLabels ;  
// 				neighborLabels.reserve(2) ;  
// 				int leftPixel = data_curRow[j-1] ;  
// 				int upPixel = data_preRow[j] ;  
// 				if ( leftPixel > 1)  
// 				{  
// 					neighborLabels.push_back(leftPixel) ;  
// 				}  
// 				if (upPixel > 1)  
// 				{  
// 					neighborLabels.push_back(upPixel) ;  
// 				}  
// 
// 				if (neighborLabels.empty())  
// 				{  
// 					labelSet.push_back(++label) ;  // assign to a new label  
// 					data_curRow[j] = label ;  
// 					labelSet[label] = label ;  
// 				}  
// 				else  
// 				{  
// 					std::sort(neighborLabels.begin(), neighborLabels.end()) ;  
// 					int smallestLabel = neighborLabels[0] ;    
// 					data_curRow[j] = smallestLabel ;  
// 
// 					// save equivalence  
// 					for (size_t k = 1; k < neighborLabels.size(); k++)  
// 					{  
// 						int tempLabel = neighborLabels[k] ;  
// 						int& oldSmallestLabel = labelSet[tempLabel] ;  
// 						if (oldSmallestLabel > smallestLabel)  
// 						{                             
// 							labelSet[oldSmallestLabel] = smallestLabel ;  
// 							oldSmallestLabel = smallestLabel ;  
// 						}                         
// 						else if (oldSmallestLabel < smallestLabel)  
// 						{  
// 							labelSet[smallestLabel] = oldSmallestLabel ;  
// 						}  
// 					}  
// 				}                 
// 			}  
// 		}  
// 	}  
// 
// 	// update equivalent labels  
// 	// assigned with the smallest label in each equivalent label set  
// 	for (size_t i = 2; i < labelSet.size(); i++)  
// 	{  
// 		int curLabel = labelSet[i] ;  
// 		int preLabel = labelSet[curLabel] ;  
// 		while (preLabel != curLabel)  
// 		{  
// 			curLabel = preLabel ;  
// 			preLabel = labelSet[preLabel] ;  
// 		}  
// 		labelSet[i] = curLabel ;  
// 	}  
// 
// 
// 	// 2. second pass  
// 	for (int i = 0; i < rows; i++)  
// 	{  
// 		int* data = _lableImg.ptr<int>(i) ;  
// 		for (int j = 0; j < cols; j++)  
// 		{  
// 			int& pixelLabel = data[j] ;  
// 			pixelLabel = labelSet[pixelLabel] ;   
// 		}  
// 	}  
// }

//void icvprCcaBySeedFill(const cv::Mat& _binImg, cv::Mat& _lableImg)  
//{  
//	// connected component analysis (4-component)  
//	// use seed filling algorithm  
//	// 1. begin with a foreground pixel and push its foreground neighbors into a stack;  
//	// 2. pop the top pixel on the stack and label it with the same label until the stack is empty  
//	//   
//	// foreground pixel: _binImg(x,y) = 1  
//	// background pixel: _binImg(x,y) = 0  
//
//
//	if (_binImg.empty() ||  
//		_binImg.type() != CV_8UC1)  
//	{  
//		return ;  
//	}  
//
//	_lableImg.release() ;  
//	_binImg.convertTo(_lableImg, CV_32SC1) ;  
//
//	int label = 1 ;  // start by 2  
//
//	int rows = _binImg.rows - 1 ;  
//	int cols = _binImg.cols - 1 ;  
//	for (int i = 1; i < rows-1; i++)  
//	{  
//		int* data= _lableImg.ptr<int>(i) ;  
//		for (int j = 1; j < cols-1; j++)  
//		{  
//			if (data[j] == 1)  
//			{  
//				std::stack<std::pair<int,int>> neighborPixels ;     
//				neighborPixels.push(std::pair<int,int>(i,j)) ;     // pixel position: <i,j>  
//				++label ;  // begin with a new label  
//				while (!neighborPixels.empty())  
//				{  
//					// get the top pixel on the stack and label it with the same label  
//					std::pair<int,int> curPixel = neighborPixels.top() ;  
//					int curX = curPixel.first ;  
//					int curY = curPixel.second ;  
//					_lableImg.at<int>(curX, curY) = label ;  
//
//					// pop the top pixel  
//					neighborPixels.pop() ;  
//
//					// push the 4-neighbors (foreground pixels)  
//					if (_lableImg.at<int>(curX, curY-1) == 1)  
//					{// left pixel  
//						neighborPixels.push(std::pair<int,int>(curX, curY-1)) ;  
//					}  
//					if (_lableImg.at<int>(curX, curY+1) == 1)  
//					{// right pixel  
//						neighborPixels.push(std::pair<int,int>(curX, curY+1)) ;  
//					}  
//					if (_lableImg.at<int>(curX-1, curY) == 1)  
//					{// up pixel  
//						neighborPixels.push(std::pair<int,int>(curX-1, curY)) ;  
//					}  
//					if (_lableImg.at<int>(curX+1, curY) == 1)  
//					{// down pixel  
//						neighborPixels.push(std::pair<int,int>(curX+1, curY)) ;  
//					}  
//				}         
//			}  
//		}  
//	}  
//}