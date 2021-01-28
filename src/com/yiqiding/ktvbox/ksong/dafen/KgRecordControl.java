package com.yiqiding.ktvbox.ksong.dafen;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import com.aac.AacEncoder;
import com.yiqiding.ktvbox.config.KTVBoxPathManager;
import com.yiqiding.ktvbox.libutils.LogUtil;

import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

public class KgRecordControl {
	public static KgRecordControl  kgRecord = new KgRecordControl();
	
	public Queue<byte[]> queue = new LinkedList<byte[]>(); 
	
	public static KgRecordControl getInstance() {
		return kgRecord;
	}
	private String currSongSaveName;
    FileOutputStream fos = null;  

	DataOutputStream dos = null;
    private boolean isToStop = false;
    private boolean isNeedToRecoding = false;
    protected int sample = 11025;
    private long aacEncoderId ;
	public boolean isToStop() {
		return isToStop;
	}
	public void setToStop(boolean isToStop) {
		this.isToStop = isToStop;
	}
	private Thread thread;
	private File currFile;
	int inputSample;
	int outPutSize ;
	private Runnable runnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			start_();
		}
	};
	
	public void start(){
		aacEncoderId =	AacEncoder.getInstance().encodeInit(sample, 2,16);
		 inputSample = AacEncoder.getInputSample(aacEncoderId);
		 outPutSize =  AacEncoder.getMaxOutputBytes(aacEncoderId);
		LogUtil.i("aacEncoderId ==>" + aacEncoderId+"inputSample ==>" + inputSample+"outPutSize ==>" + outPutSize);
		if(aacEncoderId==0){
			try {
				throw new Exception("AacEncoder init fail");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			isToStop = false;
			thread = new Thread(runnable,"record");
			thread.start();
		}
		
	}
	private void start_() {
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
				int size = queue.size();
				for(int i = 0 ;i<size;i++){
					byte[] temp = queue.poll();
					if(temp==null){
						return;
					}
					byte[] out = new byte[outPutSize*2];
					//LogUtil.i("in"+in.length+"out"+out.length);
					int leng= AacEncoder.getInstance().aacEncode(temp, inputSample*2,out, aacEncoderId);
					
					if(leng!=-1){
						dos.write(out, 0, leng);
						dos.flush();
					}
				}
				//如果暂停了，再做最后一次写判断
				if(isToStop){
					release();
				}
				thread.sleep(1000);
			
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
//		currSongSaveName = "kid_serialid_" + System.currentTimeMillis()
//				+ ".pcm";
//		File pcmFile = new File(
//				KTVBoxPathManager
//						.getPathDirByType(KTVBoxPathManager.TYPE_RECORD_FILE),
//				currSongSaveName);
		File pcmFile = new File("/sdcard/" + currSongSaveName);
		LogUtil.i("record song path ==>" + pcmFile.getAbsolutePath());
		return pcmFile;
	}
	
	public synchronized void release() {
		// 释放资源
		LogUtil.i("MRecorder release");
		queue.clear();
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
		if(aacEncoderId!=0){
			AacEncoder.getInstance().encodeExit(aacEncoderId);
		}
	}
	public void stop(){
		isToStop = true;
		isNeedToRecoding = false;
	}
    public String getCurrSongSaveName() {
		return currSongSaveName;
	}
	public void setCurrSongSaveName(String currSongSaveName) {
		this.currSongSaveName = currSongSaveName;
		isNeedToRecoding = true; 
	}
	public boolean isNeedToRecoding() {
		return isNeedToRecoding;
	}
	public void setNeedToRecoding(boolean isNeedToRecoding) {
		this.isNeedToRecoding = isNeedToRecoding;
	}
	public int getSample() {
		return sample;
	}
	public void setSample(int sample) {
		this.sample = sample;
	}
	
}
