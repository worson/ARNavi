//
//  types.h
//  HaloAIMapData
//
//  Created by HarryMoo on 28/9/2016.
//  Copyright © 2016 HaloAI. All rights reserved.
//

#ifndef types_h
#define types_h
#include <math.h>

#include<assert.h>
#include <vector>

#define HA_MIN( a, b )  ( (a) < (b) ? (a) : (b) )
#define HA_MAX( a, b )  ( (a) > (b) ? (a) : (b) )



// =====liudao========
#define DOTMULTI_TH	0.8		// 点乘门限，用于判断两向量方向是否相近
#define PARALLEL_COS_VALUE	0.98				// 余弦值阈值，用于判断两条线是否平行
// ===================

typedef unsigned char HAUINT8;
typedef	char HAINT8;
typedef unsigned short HAUINT16;
typedef short HAINT16;
typedef unsigned int HAUINT32;
typedef int HAINT32;

//typedef unsigned __int64 size_t;

typedef double HADOUBLE;

#define DATA_VER_NUL	(0)
#define DATA_VER_GD		(1)
#define DATA_VER_SW		(2)
#define DATA_VER_THRESHOD (14)

#if defined(_MSC_VER)
typedef unsigned __int64 HAUINT64;
typedef __int64 HAINT64;
#else
typedef unsigned long long HAUINT64;
typedef long long HAINT64;
#endif

typedef int HA_BOOL;
#define HA_YES	(1)
#define HA_NO	(0)

#define HA_NULL (0)

#if !defined(HA_INLINE)
# if defined(__STDC_VERSION__) && __STDC_VERSION__ >= 199901L
#  define HA_INLINE static inline
# elif defined(__cplusplus)
#  define HA_INLINE static inline
# elif defined(__GNUC__)
#  define HA_INLINE static __inline__
# elif defined(_MSC_VER)
#  define HA_INLINE static __inline
# else
#  define HA_INLINE static
# endif
#endif


typedef  unsigned int HAColor;

HA_INLINE HAColor HAColorMake(unsigned char r, unsigned char g, unsigned char b, unsigned char a) {
    return (((r) << 0) | ((g) << 8) | ((b) << 16) | ((a) << 24));
}

HA_INLINE HAUINT8 HAColorGetR(HAColor color) {
    return ((HAUINT8)(((color) >> 0) & 0xFF));
}

HA_INLINE HAUINT8 HAColorGetG(HAColor color) {
    return ((HAUINT8)(((color) >> 8) & 0xFF));
}

HA_INLINE HAUINT8 HAColorGetB(HAColor color) {
    return ((HAUINT8)(((color) >> 16) & 0xFF));
}

HA_INLINE HAUINT8 HAColorGetA(HAColor color) {
    return ((HAUINT8)(((color) >> 24) & 0xFF));
}


typedef struct _HAPoint
{
    HAINT32 x;
    HAINT32 y;
}HAPoint;

// typedef struct _HAPoint2D
// {
//     double x;
//     double y;
// 	HAPoint2D(){};
// 	HAPoint2D(double _x, double _y){x = _x;y = _y;};
// 	bool operator ==(HAPoint2D haP2Dother){return x==haP2Dother.x && y==haP2Dother.y;};
// 	bool operator !=(HAPoint2D haP2Dother){return x!=haP2Dother.x || y!=haP2Dother.y;};
// }HAPoint2D;



typedef struct _HASize
{
    HAINT32 cx;
    HAINT32 cy;
}HASize;

typedef struct _HAGCRect {
    HAPoint origin;
    HASize size;
}HAGCRect;

typedef struct _HARect
{
    HAINT32 left;
    HAINT32 top;
    HAINT32 right;
    HAINT32 bottom;
}HARect;

HA_INLINE HAPoint HAPointMake(int x, int y) {
    HAPoint point = {x, y};
    return point;
}

HA_INLINE HASize HASizeMake(int cx, int cy) {
    HASize size = {cx, cy};
    return size;
}

HA_INLINE HARect HARectMake(int left, int top, int right, int bottom) {
    HARect rect = {left, top, right, bottom};
    return rect;
}

HA_INLINE HAGCRect HACGRectMake(int x, int y, int width, int height)
{
    HAGCRect rect;
    rect.origin.x = x; rect.origin.y = y;
    rect.size.cx = width; rect.size.cy = height;
    return rect;
}

HA_INLINE bool HAGCRectIsEmpty(HAGCRect rect) {
    return rect.size.cx == 0 || rect.size.cy == 0;
}

HA_INLINE void HACGRectOffset(HAGCRect *rect,int dx,int dy)
{
    rect->origin.x += dx;
    rect->origin.y += dy;
}

HA_INLINE int HARectIsEmpty(HARect rect) {
    return rect.left == rect.right && rect.top == rect.bottom;
}

HA_INLINE void HARectInset(HARect *rect, int dx, int dy) {
    rect->left += dx;
    rect->top += dy;
    rect->right -= dx;
    rect->bottom -= dy;
}

HA_INLINE int HARectIntersectsRect(HARect rect1, HARect rect2) {
    return !(rect1.left > rect2.right || rect1.right < rect2.left || rect1.top > rect2.bottom || rect1.bottom < rect2.top);
}

HA_INLINE bool HARectContainsPoint(HARect rect1, HAPoint point) {
    return point.x >= rect1.left && point.x <= rect1.right && point.y >= rect1.top && point.y <= rect1.bottom;
}

HA_INLINE int HARectContainsRect(HARect rect1, HARect rect2) {
    return (rect1.left <= rect2.left && rect1.right >= rect2.right && rect1.top <= rect2.top && rect1.bottom >= rect2.bottom);
}


typedef struct _HAMapPoint
{
    HAINT32 x;
    HAINT32 y;
	//_HAMapPoint(){};
	//_HAMapPoint(HAINT32 _x, HAINT32 _y){x = _x;y = _y;};

	/*void operator = (_HAMapPoint &hamPt)
	{		
		x = hamPt.x;
		y = hamPt.y;		
	};*/

	bool operator ==(_HAMapPoint& hamOther){return x==hamOther.x && y==hamOther.y;};
	bool operator !=(_HAMapPoint& hamOther){return x!=hamOther.x || y!=hamOther.y;};
} HAMapPoint;

HA_INLINE HAMapPoint HAMapPointMake(HAINT32 x, HAINT32 y)
{
    HAMapPoint mapPoint;
    mapPoint.x = x;
    mapPoint.y = y;
    return mapPoint;
}

typedef struct _HAMapRect
{
    HAINT32 left;
    HAINT32 top;
    HAINT32 right;
    HAINT32 bottom;
} HAMapRect;

typedef struct _HALocationCoordinate2D
{
    double longitude;
    double latitude;
} HALocationCoordinate2D;

HA_INLINE HAMapRect HAMapRectMake(int left, int top, int right, int bottom) {
    HAMapRect rect = {left, top, right, bottom};
    return rect;
}

HA_INLINE HALocationCoordinate2D HALocationCoordinate2DMake(double longitude, double latitude) {
    HALocationCoordinate2D coordinate = {longitude, latitude};
    return coordinate;
}

HA_INLINE int HAMapRectIntersectsMapRect(HAMapRect rect1, HAMapRect rect2) {
    return !(rect1.left > rect2.right || rect1.right < rect2.left || rect1.top > rect2.bottom || rect1.bottom < rect2.top);
}


//#define HAOBJECT_INIT(obj) do { (obj)->retainCount = 1; } while(0)
//
//#define HAOBJECT_RETAIN(obj) do { ++(obj)->retainCount; } while(0)
//
//#define HAOBJECT_RELEASE(obj) do { \
//if (--(obj)->retainCount == 0) {\
//SysFree(obj);             \
//}                             \
//} while(0)
//
//#define HAOBJECT_RETAIN_COUNT(obj) ((obj)->retainCount)
#define kCoordinateToInteger (100000)

typedef struct _HARoutePoint
{
    int x;
    int y;
} HARoutePoint;

typedef struct _HARouteRect
{
    int left;
    int top;
    int right;
    int bottom;
} HARouteRect;

HA_INLINE HARoutePoint HARoutePointForCoordinate(HALocationCoordinate2D coordinate)
{
    HARoutePoint point;
    point.x = (int)(coordinate.longitude * kCoordinateToInteger);
    point.y = (int)(coordinate.latitude * kCoordinateToInteger);
    
    return point;
}

HA_INLINE HALocationCoordinate2D HACoordinateForRoutePoint(HARoutePoint point)
{
    HALocationCoordinate2D coordinate;
    coordinate.longitude = point.x * (1.0 / kCoordinateToInteger);
    coordinate.latitude = point.y * (1.0 / kCoordinateToInteger);
    
    return coordinate;
}

HA_INLINE bool HARouteRectIntersectsRouteRect(HARouteRect rect1, HARouteRect rect2)
{
    return !(rect1.left > rect2.right || rect1.right < rect2.left || rect1.top > rect2.bottom || rect1.bottom < rect2.top);
}

typedef struct _HAMapRtic
{
    int mapId;
    short middle;
    char kind;
    char status;
} HAMapRtic; //道路交通信息系统, 详细路段编码, 中国实时交通信息

typedef struct _HABound
{
    double left;
    double right;
    double top;
    double bottom;
} HABound;

typedef struct LINK_BLOCK_ID
{
    int nBlockX;
    int nBlockY;
    char cLinkLevel;
    HAMapRect geoRect;
} link_block_id;

typedef struct _HAMapID
{
//    int
} HAMaPID;
//HAVECTOR_DECLARE(HAVectorRtic, HAMapRtic);
//HAVECTOR_DECLARE(HAVectorLinkBlockID, link_block_id);



const static int LEVEL_20_PIXEL_SIZE = 4096;
//根据数据文件统计出来的常量，暂时固定
static HAMapPoint LEFTBOTTOM_MAP_PT = HAMapPointMake(218849462, 114542844); //左下角起始位置的墨卡托坐标
static HAMapPoint RIGHTUP_MAP_PT = HAMapPointMake(221645664,117320617); //右上角结束位置的墨卡托坐标
static HALocationCoordinate2D LEFTBOTTOM_GPS_COOR = HALocationCoordinate2DMake(113.5, 22.0921);//左下角起始位置的GPS坐标
static HALocationCoordinate2D RIGHTUP_GPS_COOR = HALocationCoordinate2DMake(117.25,25.5);//右上角的结束位置的GPS坐标
static HAINT32 MAP_X_DIFF = RIGHTUP_MAP_PT.x - LEFTBOTTOM_MAP_PT.x;//横轴差
static HAINT32 MAP_Y_DIFF = RIGHTUP_MAP_PT.y - LEFTBOTTOM_MAP_PT.y;//纵轴差
static HAINT16 MAP_X_BLOCKS_COUNT = (MAP_X_DIFF / LEVEL_20_PIXEL_SIZE) + 1;//横轴Block个数
static HAINT16 MAP_Y_BLOCKS_COUNT = (MAP_Y_DIFF / LEVEL_20_PIXEL_SIZE) + 1;//纵轴Block个数
static HAINT32 MAP_X_OFFSET = LEFTBOTTOM_MAP_PT.x;
static HAINT32 MAP_Y_OFFSET = LEFTBOTTOM_MAP_PT.y;

#endif /* types_h */
