
package com.yiqiding.ktvbox.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;


/**
 * When image resource id was set, animate show, then hide quickly.
 * 		
 * Note:If animating, the same resid passed in will do nothing; 
 * 
 * @author nero
 *
 */
public class imageViewWithAnimation extends ImageView {
	private int imgResId;

	private Animator anim;
	
	public imageViewWithAnimation(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayerType(LAYER_TYPE_HARDWARE, null);//启用硬件加速，提高view property animation性能
		anim = initAnim();
	}
	
	private Animator initAnim() {
		int dur = getResources().getInteger(android.R.integer.config_shortAnimTime);
		
		AnimatorSet show = new AnimatorSet();
		ObjectAnimator scaleXShow = ObjectAnimator.ofFloat(this, "scaleX", 0f, 1.0f);
		ObjectAnimator scaleYShow = ObjectAnimator.ofFloat(this, "scaleY", 0f, 1.0f);
		ObjectAnimator alphaShow = ObjectAnimator.ofFloat(this, "alpha", 0f, 1.0f);
		show.playTogether(scaleXShow, scaleYShow, alphaShow);
		show.setDuration(dur);
		show.setInterpolator(new OvershootInterpolator());
		
		ObjectAnimator hide = ObjectAnimator.ofFloat(this, "alpha", 1.0f, 0f);
		hide.setDuration(dur);
		
		AnimatorSet set = new AnimatorSet();
		set.playSequentially(show, hide);
		return set;
	}
	
	
	@Override
	public void setImageResource(int resId) {
		if (anim.isStarted()) {
			if (imgResId == resId) {
				return;
			} else {
				anim.cancel();
			}
		}
		
		super.setImageResource(resId);
		imgResId = resId;
		anim.start();
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (anim.isRunning()) {
			anim.cancel();
		}
	}
}
