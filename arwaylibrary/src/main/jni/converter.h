//
//  converter.h
//  HaloAIMapData
//
//  Created by HarryMoo on 1/10/2016.
//  Copyright © 2016 HaloAI. All rights reserved.
//

#ifndef converter_h
#define converter_h

#include "types.h"
#include <string>
#include <cstdlib>

//GPS转莫卡托 20级
HA_INLINE HAMapPoint HAMapPointForCoordinate(HALocationCoordinate2D coordinate) {
    double dblMercatorLat = log(tan((90 + coordinate.latitude) * 0.0087266462599716478846184538424431)) / 0.017453292519943295769236907684886;
    
    HAMapPoint mapPoint;
    mapPoint.x = (int)((coordinate.longitude + 180.0) / 360.0 * 268435456);
    mapPoint.y = (int)((180.0 - dblMercatorLat) / 360.0 * 268435456);
    return mapPoint;
}

//莫卡托转GPS
HA_INLINE HALocationCoordinate2D HACoordinateForMapPoint(HAMapPoint mapPoint) {
    double dblMercatorLat = 180.0 - mapPoint.y * 360.0 / 268435456;
    
    HALocationCoordinate2D coordinate;
    coordinate.longitude = mapPoint.x * 360.0 / 268435456 - 180.0;
    coordinate.latitude = atan(exp(dblMercatorLat * 0.017453292519943295769236907684886)) / 0.0087266462599716478846184538424431 - 90;
    return coordinate;
}

//GPS转我们局部的墨卡托坐标
HA_INLINE HAMapPoint HAGpsToLocalMapPoint(HALocationCoordinate2D coordinate) {
    HAMapPoint mapPt = HAMapPointForCoordinate(coordinate);
    mapPt.x -= MAP_X_OFFSET;
    mapPt.y -= MAP_Y_OFFSET;
    return mapPt;
}

//转局部的墨卡托坐标到GPS
HA_INLINE HALocationCoordinate2D HALocalMapPointToGps(HAMapPoint localMapPoint) {
    HAMapPoint globalMapPt = {localMapPoint.x + MAP_X_OFFSET, localMapPoint.y + MAP_Y_OFFSET};
    return HACoordinateForMapPoint(globalMapPt);
}

//像素坐标转经纬
HA_INLINE double HAclientXToLongitude(int clientX)
{
    return clientX * 360.0 / 268435456 - 180.0;
}

//像素坐标转纬度
HA_INLINE double HAclientYToLatitude(int clientY)
{
    double dblMercatorLat = 180.0 - clientY * 360.0 / 268435456;
    return atan(exp(dblMercatorLat * 0.017453292519943295769236907684886)) / 0.0087266462599716478846184538424431 - 90;
}

//经度转像素坐标
HA_INLINE int HAlongitudeToClientX(double longitude)
{
    return (int)((longitude + 180.0) / 360.0 * 268435456);
}

//纬度转像素坐标
HA_INLINE int HAlatitudeToClientY(double latitude)
{
    double dblMercatorLat = log(tan((90 + latitude) * 0.0087266462599716478846184538424431)) / 0.017453292519943295769236907684886;
    return (int)((180.0 - dblMercatorLat) / 360.0 * 268435456);
}

HA_INLINE long HAGPSToMapID(HALocationCoordinate2D gps) {
    int RR = gps.latitude*60.0/40.0;
    int LL = gps.longitude - 60;
    double temp = RR*40.0/60.0;
    double temp1 = gps.latitude - temp;
    double M = temp1 * 60.0 / 5.0;
    double N = (gps.longitude-LL-60.0)*600.0/75.0;
    
    return RR*10000 + LL*100 + M*10 + N;
}

//计算二次网格的起始坐标，参数是字符串形式的网格ID
HA_INLINE HALocationCoordinate2D HAMapIdToGps(string strMapID)
{
    double rrNum, llNum, mNum=0.0, nNum=0.0;
    string RR = strMapID.substr(0, 2);
    rrNum = atof(RR.c_str());
//    sscanf(RR.c_str(), "%d", &rrNum);
    string LL = strMapID.substr(2, 2);
    llNum = atof(LL.c_str());
    if (strMapID.size() == 6) {
        string M = strMapID.substr(4, 1);
        mNum = atof(M.c_str());
        string N = strMapID.substr(5,1);
        nNum = atof(N.c_str());
    }
    
    HALocationCoordinate2D coordinate;
    coordinate.longitude = llNum+60+nNum*(double)(7.5/60);
    coordinate.latitude = (double)(rrNum*40.0/60.0)+mNum*(double)(5.0/60.0);
    return coordinate;
}

//计算二次网格的起始坐标，参数是数字形式的网格ID
HA_INLINE HALocationCoordinate2D HAMapIdToGps(long lMapID)
{
    /*string strMapID = std::to_string(lMapID);
    return HAMapIdToGps(strMapID);*/

	char chTemp[100];
	sprintf(chTemp,"%ld",lMapID);
	string strMapID = chTemp;
	return HAMapIdToGps(strMapID);
}

HA_INLINE HAINT32 getBlockIdxForMapPoint(HAMapPoint mapPt)
{
    HAINT32 blockXIdx = mapPt.y / LEVEL_20_PIXEL_SIZE;
    HAINT32 blockYIdx = mapPt.x / LEVEL_20_PIXEL_SIZE;
    return blockXIdx * MAP_X_BLOCKS_COUNT + blockYIdx;
}

HA_INLINE HASize getBlockCoorinateXYIdx(HAINT32 blockIdx)
{
    HASize xyIdx = {blockIdx / MAP_X_BLOCKS_COUNT, blockIdx % MAP_X_BLOCKS_COUNT};
    return xyIdx;
}

HA_INLINE HAMapRect getBlockRect(HAINT32 blockIdx)
{
    //这里的BlockRect是以原点左下角(0,0)来计算的
    HASize xyIdx = getBlockCoorinateXYIdx(blockIdx);
    HAINT32 xIdx = xyIdx.cx;
    HAINT32 yIdx = xyIdx.cy;
    HAMapRect blockRect;
    blockRect.left = yIdx * LEVEL_20_PIXEL_SIZE;
    blockRect.right = blockRect.left + LEVEL_20_PIXEL_SIZE;
    blockRect.bottom = xIdx * LEVEL_20_PIXEL_SIZE;  //注意坐标原点在左下角
    blockRect.top = blockRect.bottom + LEVEL_20_PIXEL_SIZE;
    
    return blockRect;
}
#endif /* converter_h */
