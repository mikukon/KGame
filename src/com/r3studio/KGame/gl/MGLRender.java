package com.r3studio.KGame.gl;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;

public class MGLRender extends BaseGLRender
{
	
	private long mLastTime;
	public GLDaFenView daFenView;
	
	public MGLRender(Context context, int width, int height)
	{
		super(width, height);
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig egl)
	{
		super.onSurfaceCreated(gl, egl);
		System.out.println("onSurfaceCreated" + Thread.currentThread());
		daFenView = new GLDaFenView(this);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		super.onSurfaceChanged(gl, width, height);
		System.out.println("onSurfaceChanged" + Thread.currentThread());
	}
	
	@Override
	public void onDrawFrame(GL10 gl)
	{
        super.onDrawFrame(gl);
        if(mLastTime > 0)
        {
        	long currentTime = System.currentTimeMillis();
        	float deltaTime = (currentTime - mLastTime) / 1000f;
        	
        	if (daFenView != null) {
				daFenView.paint();
			}

            mLastTime = currentTime;
        }
        else
        {
        	mLastTime = System.currentTimeMillis();
		}
	}
	
	public void dispose() {
		System.out.println("dispose" + Thread.currentThread());
		daFenView.dispose();
	}
}
