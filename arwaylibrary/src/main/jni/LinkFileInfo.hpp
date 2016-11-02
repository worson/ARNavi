//
//  LinkFileInfo.hpp
//  HaloAIMapData
//
//  Created by HarryMoo on 28/9/2016.
//  Copyright © 2016 HaloAI. All rights reserved.
//

#ifndef LinkFileInfo_hpp
#define LinkFileInfo_hpp

#include <stdio.h>
#include <string>
#include <vector>
#include "types.h"

//typedef enum _RoadLevel {
//    Highway = 0x00, //高速
//    HighWayCity = 0x01 //城市高速
//    /*......*/
//} ROAD_LEVEL;
//
//typedef enum _RoadCharacter {
//    NONE = 0x01,    //无属性
//    UPDOWN = 0x02   //上下行分离
//} ROAD_CHARACTER;

struct LinkInfo{
    long mapId;
    float linkId;
    int roadLevels[6]; //按顺序摆放道路等级，当值为0时结束，不超过6个
    int roadCharacters[6]; //按顺序摆放道路等级，当值为0时结束，不超过6个
	float routeId; //当前Link所在的道路ID,全国唯一
	int direction;//道路方向:0未调查,默认双向,1双向,2正方向(link的起点到终点),3反方向
	unsigned int routeNameHash;//道路名字的hash值
};

#endif /* LinkFileInfo_hpp */
