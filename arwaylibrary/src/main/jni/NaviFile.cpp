//
//  HaloNavi.cpp
//  NaviStorage
//
//  Created by hejames on 16/9/28.
//  Copyright © 2016年 hejames. All rights reserved.
//

#include <stdio.h>
#include <fstream>
#include <istream>
#include <iostream>

#include <sys/stat.h>
#include <sys/types.h>
#include <fcntl.h>
#include "NaviFile.h"
#include "stdlib.h"

#ifdef _WINDOWS_VER_
	//#include <windows.h>
	#include <io.h>
	#include "mman.h"
	//#include <unistd.h>	
#else
	#include <sys/mman.h>
#endif

using namespace std;
#define HALO_VER "halov1.0"

HaloNav::HaloNav()
{
    memcpy(_version,HALO_VER, sizeof(HALO_VER)) ;
    _totalBlocksNum = 0;
    //_offset_x = 0;
    //_offset_y = 0;
    _blocksWidth = 0;
    _blocksHeight = 0;
    _pDict = 0;
    _headOffset = sizeof(_version) + 16;
    
    _indexOffset = 0;
    _dataOffset = 0;
    _totalSize = 0;
}

HaloNav::~HaloNav()
{
    
}

bool HaloNav::initNaviStorage(int blocksWidth,int blocksHeight,int offset_x,int offset_y,int maxsize)
{
	cout << "initNaviStorage enter." << " blocksSize=(" << blocksWidth << "," << blocksHeight << ") ";
	cout << "offset=" << "(" << offset_x << "," << offset_y << ")" << endl;
    _blocksWidth = blocksWidth;
    _blocksHeight = blocksHeight;
   // _offset_x = offset_x;
   // _offset_y =  offset_y ;
    _mptOffset.x = offset_x;
    _mptOffset.y = offset_y;
    _totalBlocksNum = blocksWidth*blocksHeight;
    _indexOffset = 4*_totalBlocksNum + _headOffset;
    if(_pDict)
        delete _pDict;
    _totalSize = _indexOffset;
    if(maxsize)
        _totalSize += maxsize*_totalBlocksNum;
    else
        _totalSize += 1024*1024*512;
    _pDict = (unsigned char*)malloc(_totalSize);
    _pOffset = _pDict;
    memcpy(_pOffset,_version,sizeof(_version));
    _pOffset += sizeof(_version);
    *(unsigned int*)_pOffset = _blocksWidth;
    _pOffset += 4;
    *(unsigned int*)_pOffset = _blocksHeight;
    _pOffset += 4;
    *(HAINT32*)_pOffset = _mptOffset.x;
    _pOffset += sizeof(HAINT32);
    *(HAINT32*)_pOffset = _mptOffset.y;
    _pOffset += sizeof(HAINT32);
    /*
     *(unsigned int*)_pOffset = offset_x;
    _pOffset += 4;
    *(unsigned int*)_pOffset = offset_y;
    _pOffset += 4;
    */
	cout << "initNaviStorage leave." << endl;

    return true;
}

// 获取宽、高方向上block的个数
void HaloNav::getBlocksSize(int& nBlocksW, int& nBlocksH)
{
	nBlocksW = _blocksWidth;
	nBlocksH = _blocksHeight;
}
HAMapPoint HaloNav::getOffset()
{
    return _mptOffset;
}
int HaloNav::writeBlock(std::vector<LinkInfo>& vecLinkInfos,std::vector< std::vector<HAMapPoint> >& vecAxes,int idx)
{
	cout << idx << ":" << vecLinkInfos.size() << ";";
    if(idx == 0)
    {
        *(unsigned int *)(_pDict +_headOffset ) = _indexOffset;
        _pOffset = _pDict + _indexOffset;
    
    }else
        *(unsigned int *)(_pDict +_headOffset + 4*idx ) = (_pOffset- _pDict);
    
    //unsigned int offset = *(unsigned int *)(_pDict +_headOffset + 4*idx );
    //_pOffset = _pDict + offset;

//     size_t count = vecLinkInfos.size();
//     *(size_t *)_pOffset = count;
//     _pOffset += sizeof(size_t);

	HAUINT64 count = vecLinkInfos.size();
	*(HAUINT64 *)_pOffset = count;	
	_pOffset += sizeof(HAUINT64);
	
    for(int i = 0;i< count;i++)
    {
        LinkInfo& info = vecLinkInfos[i];
        memcpy(_pOffset,&info,sizeof(LinkInfo));
        _pOffset += sizeof(LinkInfo);
        //size_t cAxes = vecAxes[i].size();
        //*(size_t*)(_pOffset) = cAxes;//*sizeof(Axes);
        //_pOffset += sizeof(size_t);

		HAUINT64 cAxes = vecAxes[i].size();
        *(HAUINT64*)(_pOffset) = cAxes;//*sizeof(Axes);
        _pOffset += sizeof(HAUINT64);

        for(int j = 0;j<cAxes;j++)
        {
           HAMapPoint& axes = vecAxes[i][j];
           memcpy(_pOffset,&axes,sizeof(axes));
            _pOffset += sizeof(HAMapPoint);
        }
    }
    return 0;
}

int HaloNav::writeDictionary(std::string& file)
{
	cout << endl << "writeDictionary enter." << endl;
    if(!_pDict)
        return -1;
    std::ofstream stream(file.c_str(), std::ios_base::binary);
    stream.write((const char*)(_pDict), _pOffset - _pDict);
    free(_pDict);
    _pDict = 0;
	cout << "writeDiectionary leave." << endl;

    return 0;
}
int HaloNav::readDictionary(std::string& file)
{
	int fd = open(file.c_str(), O_RDONLY);
	if(fd == -1) {
		return -1;
	}

	struct stat sb;
	fstat(fd, &sb);
	_totalSize = sb.st_size;
	_pDict = (unsigned char* )mmap(NULL, sb.st_size, PROT_READ, MAP_SHARED, fd, 0);
	if (_pDict == MAP_FAILED) {
		return -1;
	}

    unsigned char  version[8];
    _pOffset = _pDict;
    memcpy(version, _pDict, 8);
    if(memcmp(version,_version,8)!=0)
        return -1;
    _pOffset +=sizeof(_version);
    _blocksWidth = *(unsigned int*)_pOffset;
    _pOffset +=sizeof(unsigned int);
    _blocksHeight =*(unsigned int*)_pOffset;
    _pOffset +=sizeof(unsigned int);
    
    _mptOffset.x = *(HAINT32*)_pOffset;
    _pOffset += sizeof(HAINT32);
    _mptOffset.y = *(HAINT32*)_pOffset;
    _pOffset += sizeof(HAINT32);;

    /*
    _offset_x =*(unsigned int*)_pOffset;
    _pOffset +=sizeof(unsigned int);
    _offset_y =*(unsigned int*)_pOffset;
    _pOffset +=sizeof(unsigned int);
     */
    return 0;
}

int HaloNav::findLinks(HAMapPoint& axs,int width,int height,std::vector<LinkInfo>& vecLinkInfos,std::vector< std::vector<HAMapPoint> >& vecAxes)
{
    if(!_pDict)
        return -1;

	// 判断输入点是否在范围内
	//int nX1 = _mptOffset.x, nX2 = nX1 + _blocksWidth*BLOCK_WIDTH;
	//int nY1 = _mptOffset.y, nY2 = nY1 + _blocksHeight*BLOCK_HEIGH;
	int nX1 = 0, nX2 = nX1 + _blocksWidth*BLOCK_WIDTH;
	int nY1 = 0, nY2 = nY1 + _blocksHeight*BLOCK_HEIGH;

	if (axs.x<nX1 || axs.x>=nX2 || 
		axs.y<nY1 || axs.y>=nY2)
	{
		return -1;
	}

    
    int offset_x = width/2;
    int offset_y = height/2;

    /*unsigned int min_x = (axs.x - offset_x)<0?0:axs.x - offset_x;
    unsigned int max_x = (axs.x + offset_x) >_blocksWidth*BLOCK_WIDTH?_blocksWidth*BLOCK_WIDTH:axs.x + offset_x;
    unsigned int min_y = (axs.y - offset_y)<0?0:axs.y - offset_y;
    unsigned int max_y = (axs.y + offset_y) > _blocksHeight*BLOCK_HEIGH?_blocksHeight*BLOCK_HEIGH:axs.y + offset_y;
    unsigned int min_idx_x = min_y/BLOCK_HEIGH; 
    unsigned int max_idx_x = max_y/BLOCK_HEIGH;
    unsigned int min_idx_y = min_x/BLOCK_WIDTH;
    unsigned int max_idx_y = max_x/BLOCK_WIDTH;*/

	int min_x = (axs.x - offset_x)<0?0:axs.x - offset_x;
	int max_x = (axs.x + offset_x) >_blocksWidth*BLOCK_WIDTH?_blocksWidth*BLOCK_WIDTH:axs.x + offset_x;
	int min_y = (axs.y - offset_y)<0?0:axs.y - offset_y;
	int max_y = (axs.y + offset_y) > _blocksHeight*BLOCK_HEIGH?_blocksHeight*BLOCK_HEIGH:axs.y + offset_y;
	int min_idx_x = min_y/BLOCK_HEIGH; 
	int max_idx_x = max_y/BLOCK_HEIGH;
	int min_idx_y = min_x/BLOCK_WIDTH;
	int max_idx_y = max_x/BLOCK_WIDTH;
    if(min_idx_x > _blocksWidth || max_idx_x > _blocksWidth)
        return -1;
    
    if(min_idx_y > _blocksHeight || max_idx_y > _blocksHeight)
        return -1;
    
    for(int i=min_idx_x;i <= max_idx_x;i++)
    {
        for(int j = min_idx_y;j <= max_idx_y;j++)
        {
            unsigned int idx = i*_blocksWidth + j;
            unsigned int offset = *(unsigned int*)(_pDict +_headOffset + 4*idx );
            _pOffset = _pDict + offset;
            /*size_t linkCount = *(size_t*)_pOffset;
            _pOffset += sizeof(size_t);
            for(int l=0;l < linkCount;l++)
            {
                LinkInfo info;
                memcpy(&info,_pOffset,sizeof(LinkInfo));
                _pOffset += sizeof(LinkInfo);
                
                size_t axs_count = *(size_t*)_pOffset;
                _pOffset+=sizeof(size_t);*/

			HAUINT64 linkCount = *(HAUINT64*)_pOffset;
			_pOffset += sizeof(HAUINT64);
			for(int l=0;l < linkCount;l++)
			{
				LinkInfo info;
				memcpy(&info,_pOffset,sizeof(LinkInfo));
				_pOffset += sizeof(LinkInfo);

				HAUINT64 axs_count = *(HAUINT64*)_pOffset;
				_pOffset+=sizeof(HAUINT64);
                
                vecLinkInfos.push_back(info);
                std::vector<HAMapPoint> ptLst;
                for(int a=0;a < axs_count;a ++)
                {                    
					HAMapPoint hmp;
                    memcpy(&hmp,_pOffset,sizeof(HAMapPoint));
                    _pOffset += sizeof(HAMapPoint);
                    ptLst.push_back(hmp);

					//printf("a=%d\n",a);
                }
                vecAxes.push_back(ptLst);
            }
        }
    }
    return 0;
}
