package com.amap.navi.demo.util;

import android.graphics.Point;

public class PointsLines {

	private static float dot(Point a, Point b)
	{
		return a.x * b.x + a.y * b.y;
	}
	
	private static Point sub(Point a, Point b)
	{
		Point r = new Point();
		r.x = a.x - b.x;
		r.y = a.y - b.y;
		return r;
	}

	private static Point add(Point a, Point b)
	{
		Point r = new Point();
		r.x = a.x + b.x;
		r.y = a.y + b.y;
		return r;
	}

	private static Point mult(Point a, float f)
	{
		Point r = new Point();
		r.x = (int) (a.x * f);
		r.y = (int) (a.y * f);
		return r;
	}
	
	public static Point projection(Point ptLineStart, Point ptLineEnd, Point ptPos) {
		Point e = sub(ptLineEnd,ptLineStart);
		float div = dot(e,e);
		float u = dot(sub(ptPos,ptLineEnd), sub(ptLineStart,ptLineEnd))/div;
		float v = dot(sub(ptPos,ptLineStart), sub(ptLineEnd,ptLineStart))/div;
		return add(mult(ptLineStart,u), mult(ptLineEnd,v));
	}
	
	public static float twoPointsDistance(Point pt1, Point pt2) {
		
		//http://mathworld.wolfram.com/Line.html
		
		float xDist = Math.abs(pt1.x - pt2.x);
		float yDist = Math.abs(pt1.y - pt2.y);
		return (float) Math.pow(xDist*xDist+yDist*yDist, 0.5);
		
	}
	
	public static float minDistanceOfPoint2Line(Point ptLineStart, Point ptLineEnd, Point ptPos) {
		
		//http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html
		
		if (ptLineStart.x == ptLineEnd.x)
			return Math.abs(ptPos.x - ptLineStart.x);
		
		if (ptLineStart.y == ptLineEnd.y)
			return Math.abs(ptPos.y - ptLineStart.y);
	
		int a = Math.abs( (ptLineEnd.x-ptLineStart.x)*(ptLineStart.y-ptPos.y) - (ptLineStart.x-ptPos.x)*(ptLineEnd.y-ptLineStart.y) );
		int b = (int) Math.sqrt( (ptLineEnd.x-ptLineStart.x)*(ptLineEnd.x-ptLineStart.x) + (ptLineEnd.y-ptLineStart.y)*(ptLineEnd.y-ptLineStart.y) );
		
		return a/b;
	}
	
	public static boolean isPointInLine(Point ptLineStart, Point ptLineEnd, Point point){
		
		//判断point是否在ptLineStart和ptLineEnd连线的方向上
//		//y = kx + b
//		double k = (1.0 * ptLineEnd.y - ptLineStart.y)/(1.0 * ptLineEnd.x - ptLineStart.x);
//		double b = (ptLineEnd.y + ptLineStart.y) / (k * (ptLineEnd.x + ptLineStart.x)) / 2;
//		if(Math.abs(point.y - (k*point.x + b)) < 0.001){
//			return true;
//		}
//		return false;
		
		//判断point是否在点ptLineStart和ptLineEnd之间
		int pt2StartX = point.x - ptLineStart.x;
		int pt2StartY = point.y - ptLineStart.y;
		int end2StartX = ptLineEnd.x - ptLineStart.x;
		int end2StartY = ptLineEnd.y - ptLineStart.y;
		
		if(end2StartX < 0){
			if(pt2StartX > 0 || pt2StartX < end2StartX){
				return false;
			}
		}else if(end2StartX == 0){
			if(pt2StartX != 0){
				return false;
			}
		}else{
			if(pt2StartX < 0 || pt2StartX > end2StartX){
				return false;
			}			
		}
		
		if(end2StartY < 0){
			if(pt2StartY > 0 || pt2StartY < end2StartY){
				return false;
			}
		}else if(end2StartY == 0){
			if(pt2StartY != 0){
				return false;
			}
		}else{
			if(pt2StartY < 0 || pt2StartY > end2StartY){
				return false;
			}			
		}
		
		return true;
	}
}








