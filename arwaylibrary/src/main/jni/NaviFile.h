//
//  Header.h
//  NaviStorage
//
//  Created by hejames on 16/9/26.
//  Copyright © 2016年 hejames. All rights reserved.
//

#ifndef HALO_NAVI_FILE_H
#define HALO_NAVI_FILE_H

#include <vector>
#include <string>
#include "types.h"
#include "LinkFileInfo.hpp"

#define BLOCK_HEIGH 4096
#define BLOCK_WIDTH 4096
#define MAX_LEVEL 18

#ifdef _WINDOWS_VER_

#else
#include <android/log.h>
#define LOGD2(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, "Algorithm", __VA_ARGS__))
#define LOG_TAG_ERROR "Algorithm__"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_ERROR, LOG_TAG_ERROR, __VA_ARGS__))
#endif

//struct LinkInfo{
//    int a;
//    int b;
//    int c;
//};

//struct Axes{
//    int x;
//    int y;
//};

class HaloNav
{
public:
    HaloNav();
    ~HaloNav();
    bool initNaviStorage(int blocksWidth,int blocksHeight,int offset_x,int offset_y,int maxsize = 0);
//    int writeBlock(std::vector<LinkInfo>& vecLinkInfos,std::vector< std::vector<Axes> >& vecAxes,int idx);
    int writeBlock(std::vector<LinkInfo>& vecLinkInfos,std::vector< std::vector<HAMapPoint> >& vecAxes,int idx);
    int writeDictionary(std::string& file);
    int readDictionary(std::string& file);
    int findLinks(HAMapPoint& axs,int width,int height,std::vector<LinkInfo>& vecLinkInfos,std::vector< std::vector<HAMapPoint> >& vecAxes);
    HAMapPoint getOffset();
private:
    unsigned char  _version[8];
    unsigned int   _totalBlocksNum;
   // unsigned int _offset_x;
   // unsigned int _offset_y;
    unsigned int _blocksWidth;
    unsigned int _blocksHeight;
    unsigned int _headOffset;
    unsigned int _indexOffset;
    unsigned int _dataOffset;
    unsigned char* _pDict;
    unsigned int _totalSize;
    unsigned char* _pOffset;
    HAMapPoint     _mptOffset;
};
#endif /* Header_h */
