package com.r3studio.KGame;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;

public class KGameApplication extends Application {
	private HandlerThread workThread;
	private Handler workHandler;
	private static KGameApplication instance;
	
	@Override
	public void onCreate() {
		super.onCreate();
		workThread = new HandlerThread("work_thread");
		workThread.start();
		workHandler = new Handler(workThread.getLooper());
		instance = this;
	}
	
	public static KGameApplication getInstance(){
		return instance;
	}
	
	public Handler getWorkHandler(){
		return workHandler;
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		instance = null;
		workHandler.getLooper().quit();
		workHandler = null;
	}
}
