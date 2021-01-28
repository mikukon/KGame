package com.yiqiding.ktvbox.libutils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.yiqiding.ktvbox.BuildConfig;


public class KGameLogUtil {
	private static KGameLogUtil instance;
	private boolean debugMode = BuildConfig.DEBUG;
	private static final String PATH_KGAME = "/sdcard/kgame_log.txt";
	
	private KGameLogUtil(){
		try {
			File mFile = new File(PATH_KGAME);
			if (!mFile.exists()) {
				mFile.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static KGameLogUtil getInstance(){
		if (instance == null) {
			synchronized (KGameLogUtil.class) {
				if (instance == null) {
					instance = new KGameLogUtil();
				}
			}
		}
		return instance;
	}
	
	public void appendLog(String content){
		if (debugMode) {
			try {
				FileWriter fw = new FileWriter(new File(PATH_KGAME), true);
				fw.write(DateTimeUtils.now() + "--" + content + "\n");
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			};
		}
	}
	
	public void appendError(String content){
		try {
			FileWriter fw = new FileWriter(new File(PATH_KGAME), true);
			fw.write(DateTimeUtils.now() + "--" + content + "\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		};
	}
}
