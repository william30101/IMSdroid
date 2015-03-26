package org.doubango.imsdroid.Screens;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

public class ScreenUISildeMenu extends Animation{
	private View mViewTarget;
	private int mDuration, mFromWidth, mToWidth;
	
	public ScreenUISildeMenu(View v, int duration, int fromWidth, int toWidth){
		this.mViewTarget = v;
		this.mDuration   = duration;
		this.mFromWidth  = fromWidth;
		this.mToWidth    = toWidth;
		
	}

	@Override
	public void initialize(int width, int height, int parentWidth,
			int parentHeight) {
		// TODO Auto-generated method stub
		super.initialize(width, height, parentWidth, parentHeight);
		setDuration(mDuration);
		setFillAfter(true);
		setInterpolator(new LinearInterpolator());
		
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		// TODO Auto-generated method stub
		super.applyTransformation(interpolatedTime, t);
		
		LayoutParams params = (LayoutParams) mViewTarget.getLayoutParams();
		params.width = (int)(mFromWidth + interpolatedTime * (mToWidth - mFromWidth));
		mViewTarget.setLayoutParams(params);
	}
	
}
