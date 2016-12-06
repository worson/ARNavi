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
	void getBlocksSize(int& nBlocksW, int& nBlocksH);		// 获取宽、高方向上block的个数
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
