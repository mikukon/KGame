package com.r3studio.KGame;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.os.Handler;
import android.os.HandlerThread;

import com.yiqiding.ktvbox.libutils.LogUtil;

public class UploadUtil {
	private Handler workder = null;
	private HandlerThread workT = null;
	private Queue<String> logCache;
	
	private static final int STATE_IDLE = 0;
	private static final int STATE_UPLOADING = 1;
	private int currentState = STATE_IDLE;
	
	private int uploadCount = 0;
	
	public UploadUtil(){
		workT = new HandlerThread("worker");
		workT.start();
		workder = new Handler(workT.getLooper());
		
		logCache = new ConcurrentLinkedQueue<String>();
	}
	
	public void clearUploadTime(){
		uploadCount = 0;
	}
	
	public void addLog(String msg){
		logCache.offer(msg);
		if (currentState == STATE_IDLE) {
			LogUtil.i("STATE_IDLE post uploadTask");
			currentState = STATE_UPLOADING;
			workder.post(uploadTask);
		}
	}
	
	private Runnable uploadTask = new Runnable() {
		
		@Override
		public void run() {
			int size = logCache.size();
			if(size > 0){
				StringBuffer sb = new StringBuffer();
				for(int i=0; i < size; i++){
					sb.append(logCache.poll());
				}
				uploadCount += size;
				LogUtil.i("this time upload " + size + " msg once time,total  upload " + uploadCount + " msg");
				realDoWriteToFile(sb.toString());
				realUpload();
				workder.post(this);
			}else {
				currentState = STATE_IDLE;
				LogUtil.i("no upload msg");
			}
		}
	};
	
	private void realDoWriteToFile(String msg){
		LogUtil.i("......");
	}
	
	private void realUpload(){
		LogUtil.i("......");
//		try {
//			Thread.sleep(10);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		LogUtil.i("*********over*********");
	}
}
