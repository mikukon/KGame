package com.r3studio.KGame.gl;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MGLSurfaceView extends GLSurfaceView
{
	public static int SCREEN_WIDTH = 1280;
	public static int SCREEN_HEIGHT = 800;
	
	private MGLRender mGLRender = null;
	
	public MGLSurfaceView(Context context)
	{
		this(context, SCREEN_WIDTH, SCREEN_HEIGHT);
	}
	
	public MGLSurfaceView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}
	
	public MGLSurfaceView(Context context, int width, int height)
    {
		super(context);
		SCREEN_WIDTH = width;
		SCREEN_HEIGHT = height;
		init(context);
	}
	
	private void init(Context context){
		setEGLContextClientVersion(2);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderMediaOverlay(true);
        setZOrderOnTop(true);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

		mGLRender = new MGLRender(context, SCREEN_WIDTH, SCREEN_HEIGHT);
		setRenderer(mGLRender);
	}

    private void dispose()
    {
    	queueEvent(new Runnable() {
			@Override
			public void run() {
				if (mGLRender != null) {
					mGLRender.dispose();					
				}
			}
		});
    }
    
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		System.out.println("surfaceDestroyed" + Thread.currentThread());
		dispose();
		super.surfaceDestroyed(holder);
	}
	
	public void prepare(final float [] dataArr, final int minPitch, final int macPitch){
		queueEvent(new Runnable() {
			@Override
			public void run() {
				if (mGLRender != null) {
					mGLRender.daFenView.prepare(dataArr, minPitch, macPitch);					
				}
			}
		});
	}
	
	public void start(){
		queueEvent(new Runnable() {
			@Override
			public void run() {
				if (mGLRender != null) {
					mGLRender.daFenView.start();					
				}
			}
		});
	}
	
	public void updataCurPitch(int pitch){
		mGLRender.daFenView.updataCurPitch(pitch);
	}
}
