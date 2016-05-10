package com.haloai.hud.hudendpoint.arwaylib.test;

import android.util.Log;

/**
 * 
 * @ClassName: FramesInterpolation
 * @Description: Perform Interpolation and return Interpolation value 
 *               The denominator of interpolation rate is cumulative access of getInterpolationRate() in pre batch
 *               
 * @author 大羽
 * @date 2016-03-03
 * 
 */
public class FramesInterpolation<T> {
	private T mP2Item;
	private T mP1Item;
	private T mInterpolationItem;
	private int mCurrent;
	private long mCurrentFramesCounter;
	private long mPreviousFramesCounter;
	private float mInterpolationRate = 0;
	
	public  FramesInterpolation(){
		
	}
	
	public void addInterpolationItem(T newItem){
		if(newItem == null){
			return ;
		}
		//if prePreLocation is null , set the value to it , and return null
		if(mP2Item == null){
			mP2Item = newItem;
			return ;
		}
		if(mP1Item == null){
			mP1Item = newItem;
		}else {
			if(mInterpolationItem!=null){
				Log.e("hanyu", "replace mInterpolationItem :"+ mInterpolationItem + " p2item: "+ mP2Item);
				mP2Item = mInterpolationItem;
				
			}else{
				mP2Item = mP1Item;
			}
			//mP2Item = mP1Item;
			mP1Item = newItem;
			
		}
		mPreviousFramesCounter = mCurrentFramesCounter;
		mCurrent = 1;
		mCurrentFramesCounter = 0;
	}
	public float getInterpolationRate(){
		mCurrentFramesCounter++;
		if(mPreviousFramesCounter != 0 && mCurrent <= mPreviousFramesCounter){
			mInterpolationRate = 1.0f*mCurrent/mPreviousFramesCounter;
			mCurrent++;
		}
		return mInterpolationRate;
		
	}
	public T getmP2Item() {
		return mP2Item;
	}
	
	public T getmP1Item() {
		return mP1Item;
	}
	
	public void setInterpolationItem(T mInterpolationItem) {
		this.mInterpolationItem = mInterpolationItem;
	}
	public static float performInterpolationAction(FramesInterpolation<Integer> mFramesInterpolation){
		float newValue = 0f;
		float rate = mFramesInterpolation.getInterpolationRate();
		if(rate==0){
			newValue = 0f;
		}else{
			if(mFramesInterpolation.getmP2Item()==null || mFramesInterpolation.getmP2Item() == null){
				return 0f;
			}
			float gap = rate*(mFramesInterpolation.getmP1Item()- mFramesInterpolation.getmP2Item());
			newValue = gap + mFramesInterpolation.getmP2Item();
			mFramesInterpolation.setInterpolationItem(Math.round(newValue));
		}
		//Log.e("hanyu"," Rate:" + rate + " value : "+ newValue);
		return newValue ;
		
	}

}

