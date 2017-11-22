package com.example.heavymentaldelection.my_utils;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;

public class MyAnimationUtils {
	
	/**设置淡入的旋转动画
	 * 以自身的中心为圆心
	 * @param imbt_refresh 需要刷新ImageButton控件
	 */
	public static void setAnimation(ImageButton imbt_refresh) {
		RotateAnimation position_animation = new RotateAnimation(
				0f,
				360f,
				Animation.RELATIVE_TO_SELF, 0.5f, 
				Animation.RELATIVE_TO_SELF, 0.5f);
		position_animation.setDuration(230);
		position_animation.setRepeatCount(46);			
		position_animation.setRepeatMode(Animation.RESTART);
		position_animation.setFillAfter(true);
		position_animation.setInterpolator(new LinearInterpolator());		
		
		AlphaAnimation alphaAnimation = new AlphaAnimation(0.2f, (float) 1);
		alphaAnimation.setDuration(2000);
		alphaAnimation.setFillAfter(true);
		alphaAnimation.setInterpolator(new LinearInterpolator());

		AnimationSet animationSet = new AnimationSet(false);
		animationSet.addAnimation(alphaAnimation);
		animationSet.addAnimation(position_animation);
		imbt_refresh.setAnimation(animationSet);
		animationSet.startNow();	
	}
}
