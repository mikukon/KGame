package com.r3studio.KGame;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import com.yiqiding.ktvbox.libutils.LogUtil;

public class KgRecordControl {
	public static KgRecordControl  kgRecord = new KgRecordControl();
	
	public  Queue<byte[]> queue = new LinkedList<byte[]>(); 
	
	public static KgRecordControl getInstance() {
		return kgRecord;
	}
	private String currSongSaveName;
    public String getCurrSongSaveName() {
		return currSongSaveName;
	}
	public void setCurrSongSaveName(String currSongSaveName) {
		this.currSongSaveName = currSongSaveName;
	}
	FileOutputStream fos = null;  
    DataOutputStream dos = null;
    private boolean isToStop = false;
	private Thread thread;
	private File currFile;
	private Runnable runnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			start_();
		}
	};
	
	public void start(){
		queue.clear();
		thread = new Thread(runnable,"record");
		thread.start();
	}
	private void start_() {
	//	handler.removeCallbacks(runnable);
		try {
			File pcmFile = getRecSongFilepath();
			currFile = pcmFile;
			if (pcmFile.exists()) {
				pcmFile.delete();
			}
			LogUtil.i("will create pcm file");
			fos = new FileOutputStream(pcmFile);// 建立一个可存取字节的文件
			dos = new DataOutputStream(fos);
		
			while(thread!=null){
			try {
				byte[] temp = queue.poll();
				if(temp!=null){
					dos.write(temp, 0, temp.length);
					dos.flush();
				}
				if(queue.size()==0&&isToStop){
					release();
				}
			
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
			release();
		}
		
	}
	
	private File getRecSongFilepath() {
		currSongSaveName = "kid_serialid_" + System.currentTimeMillis()
				+ ".pcm";
		File pcmFile = new File("/sdcard/",
				currSongSaveName);
		LogUtil.i("record song path ==>" + pcmFile.getAbsolutePath());
		return pcmFile;
	}
	
	public synchronized void release() {
		// 释放资源
		LogUtil.i("MRecorder release");
		thread = null;
		if(fos!=null){
			try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fos =null;
		}
		if(dos!=null){
			try {
				dos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dos = null;
		}
	}
	public void stop(){
		isToStop = true;
	}
}
