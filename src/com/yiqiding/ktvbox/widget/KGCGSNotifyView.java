package com.yiqiding.ktvbox.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.yiqiding.ktvbox.R;


public class KGCGSNotifyView extends LinearLayout {

	public KGCGSNotifyView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public KGCGSNotifyView(Context context) {
		super(context);
		initView(context);
	}

	private void initView(Context context){
//		inflate(context, R.layout.kgcgs_notify, this);
	}
	
	public Bitmap getSelfBitmap(){
		setDrawingCacheEnabled(true);
		return this.getDrawingCache();
	}
}
