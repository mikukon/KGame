package com.yiqiding.ktvbox.ksong.dafen;


import android.util.Log;


/**
 * pitch过滤器 旨在优化小球跳动到平滑状态
 * @author zhuruyi
 */
public class PitchFilter {
	private static final String TAG = "PitchFilter";
	private long lastPastTime = 0;
	private int minP;
	private int maxP;
	
	private int lastPitch;
	private int lastLastPitch;
	
	private int zeroHitTimes;
	private int returnPitch = 0;
	
	/**
	 * 每30s上报一次pitch 150ms上报5次 求一次平均值返回
	 */
	private int[] latestPitchArr = new int[5];
	
//	private BufferedWriter bw;
//	public void setBw(BufferedWriter bw) {
//		this.bw = bw;
//	}
	
	public PitchFilter(int maxPitch, int minPitch){
		minP = minPitch;
		maxP = maxPitch;
	}
	
	public int filterPitch(int pastTime, int currentPitch){
		if(currentPitch < 0) currentPitch = 0;
		if(currentPitch > maxP){ 
			log("pitch 83, may be breath, reset it to lastLastPitch=>" + lastLastPitch);
			currentPitch = lastLastPitch;
		}
//		if(currentPitch >= maxP){
//			currentPitch = lastPitch + lastPitch / 5;
//			if(currentPitch >= maxP) currentPitch = maxP;
//		}
		
		//150ms改变一次pitch  
		//如果第一次pitch 不是0 第二次 pitch忽然跳跃到0 则依然使用上次pitch
		//第三次如果不是0 则使用第三次的值 如果第三次是0 那么第三次的pitch使用递减算法来获取
		long deltaTime = pastTime - lastPastTime;
		if(deltaTime >= 150 || lastPitch == 0){
			logAverage(deltaTime + "ms bigger than 150ms or lastPitch=0, set latestPitchArr[0]" + currentPitch);
			latestPitchArr[0] = currentPitch;
			currentPitch = calcAverage();
			/**
			 * 如果平均最近150ms后的pitch值仍然小于最小值 则直接置为0
			 */
			if(currentPitch < minP) {
				logAverage("currentPitch " + currentPitch + " lower than minPitch " + minP + ", reset to 0");
				currentPitch = 0;
			}
			
			returnPitch = getPitchByZeroHitTimes(currentPitch);
			
//			if(lastPitch !=0 && currentPitch == 0) {
//				//先保留用于返回值的上一次pitch作为首次出现0时候使用的pitch
//				returnPitch = lastLastPitch;
//				//同时记录最近一次pitch上报的pitch是什么
//				lastPitch = currentPitch;
//				log("first time occur 0 pitch, will return =>" + returnPitch);
//			}else if(lastPitch == 0 && currentPitch == 0){//连续两次出现0 pitch上报了 需要使用递减算法
//				if(zeroHitTimes == 2){
//					returnPitch = lastLastPitch - lastLastPitch / 5;
//					log("second time occur 0 pitch, will return 80% lastLastPitch=>" + returnPitch);
//				}else if(zeroHitTimes == 3){
//					returnPitch = lastLastPitch - lastLastPitch / 2;
//					log("third time occur 0 pitch, will return 50% lastLastPitch=>" + returnPitch);
//				}else {
//					lastLastPitch = lastPitch = 0;
//					returnPitch = 0;
//					log("fourth time occur 0 pitch, will return 0,zeroHitTimes=>" + zeroHitTimes);
//				}
//			}else {//current pitch不为0  直接返回 同时更新历史pitch
//				resetZeroHitTimes();
//				//仅仅在最近上报的pitch不为0的时候改变上上次pitch
//				lastLastPitch = lastPitch = currentPitch;
//				returnPitch = currentPitch;
//				log("current Pitch not 0, derect return " + returnPitch);
//			}
			if(deltaTime >= 150){
				log("update pastTime, reset deltaTime 0");
				//时间大于150ms 才会更新时间
				lastPastTime = pastTime;
			}
		}else if(deltaTime >= 120){
			latestPitchArr[4] = currentPitch;
//			logAverage(deltaTime + "ms bigger than 120ms, set latestPitchArr[4]" + currentPitch);
//			returnPitch = getPitchByZeroHitTimes(currentPitch, true);
		}else if(deltaTime >= 90){
			latestPitchArr[3] = currentPitch;
//			logAverage(deltaTime + "ms bigger than 90ms, set latestPitchArr[3]" + currentPitch);
//			returnPitch = getPitchByZeroHitTimes(currentPitch, true);
		}else if(deltaTime >= 60){
			latestPitchArr[2] = currentPitch;
//			logAverage(deltaTime + "ms bigger than 60ms, set latestPitchArr[2]" + currentPitch);
//			returnPitch = getPitchByZeroHitTimes(currentPitch, true);
		}else if(deltaTime >= 30){
			latestPitchArr[1] = currentPitch;
//			logAverage(deltaTime + "ms bigger than 30ms, set latestPitchArr[1]" + currentPitch);
//			returnPitch = getPitchByZeroHitTimes(currentPitch, true);
		}
		
		return returnPitch;
	}
	
	/**
	 * 
	 * @param currentPitch
	 * @param between150Delta 150ms之前的话不接受任何pitch
	 * @return
	 */
	private int getPitchByZeroHitTimes(int currentPitch){
		int returnPitch = 0;
		
		judgeZeroHitTimes(currentPitch);
		
		switch (zeroHitTimes) {
		case 0:
			//仅仅在最近上报的pitch不为0的时候改变上上次pitch
			lastLastPitch = lastPitch = currentPitch;
			returnPitch = currentPitch;
			log("current Pitch not 0, derect return " + returnPitch);
			break;
			
		case 1:
			//先保留用于返回值的上一次pitch作为首次出现0时候使用的pitch
			returnPitch = lastLastPitch;
//			//同时记录最近一次pitch上报的pitch是什么
//			lastPitch = currentPitch;
			log("first time occur 0 pitch, will return =>" + returnPitch);
			break;
			
		case 2:
//			returnPitch = lastLastPitch - lastLastPitch / 5;
			returnPitch = lastLastPitch - (lastLastPitch - minP) / 5;
			log("second time occur 0 pitch, will return 80% lastLastPitch=>" + returnPitch);
			break;
			
		case 3:
//			returnPitch = lastLastPitch - lastLastPitch / 2;
			returnPitch = lastLastPitch - (lastLastPitch - minP) / 2;
			log("third time occur 0 pitch, will return 50% lastLastPitch=>" + returnPitch);
			break;

		default:
			lastLastPitch = lastPitch = 0;
			returnPitch = 0;
			log("fourth time occur 0 pitch, will return 0,zeroHitTimes=>" + zeroHitTimes);
			break;
		}
		
		return returnPitch;
	}
	
	StringBuffer sb = new StringBuffer();
	private int calcAverage(){
//		sb.delete(0, sb.length());
//		
//		int total = 0;
//		for (int i = 0; i < 5; i++) {
//			sb.append("latestPitch["+ i +"]=" + latestPitchArr[i] + ";;;");
//			total += latestPitchArr[i];
//		}
//		sb.append("total=" + total + ", average=" + (total / 5));
//		logAverage(sb.toString());
//		return total / 5;
		return latestPitchArr[0];
	}
	
	private void judgeZeroHitTimes(int currentPitch){
		if(currentPitch == 0){
			zeroHitTimes++;
			log("hit 0, zeroHitTimes + 1=>" + zeroHitTimes);
		}else {
			log("hit not 0, resetZeroHitTimes 0");
			resetZeroHitTimes();
		}
	}
	
	private void resetZeroHitTimes(){
		zeroHitTimes = 0;
	}
	
	
	private void logAverage(String msg){
//		Log.d(TAG, msg);
//		try {
//			bw.write(msg + "\n");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}	
	
	private void log(String msg){
//		Log.d(TAG, msg);
//		System.out.println(msg);
//		try {
//			bw.write(msg + "\n");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
}
