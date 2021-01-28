package com.yiqiding.ktvbox.ksong.dafen;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.yiqiding.ktvbox.libutils.KGameLogUtil;
import com.yiqiding.ktvbox.libutils.LogUtil;
import com.yiqiding.ktvbox.rpc.socket.data.SendKGChanllengeResults;
import com.yiqiding.ktvbox.structure.KGameJiepaiEntity;
import com.yiqiding.ktvbox.widget.KGDafengView;

public class KSongDaFenManager extends AbstractKSongDaFen {
	private static KSongDaFenManager mInstance;
	private List<Float> scoreList;
	private SendKGChanllengeResults chanllengeResult;
	private KGameLogUtil logWriter;
	private float[] allScoreArr;
	private KGameJiepaiEntity jiepai;
	
	private KSongDaFenManager(Context con){
		logWriter = KGameLogUtil.getInstance();
		scoreList = new ArrayList<Float>();
		manager = KGameManager.getInstance(con);
//		manager.setDelayTime(700);
		manager.setListener(this);
	}
	
	public static KSongDaFenManager getInstance(Context con){
		if (mInstance == null) {
			synchronized (KSongDaFenManager.class) {
				if (mInstance == null) {
					mInstance = new KSongDaFenManager(con);
				}				
			}
		}
		return mInstance;
	}
	
	@Override
	public float calcAvaerageScore() {
		float score = super.calcAvaerageScore();
		LogUtil.i("score=>" + score + ",mid=" + currentSerialId);
		logWriter.appendLog("[calcAvaerageScore]score=>" + score + ",mid=" + currentSerialId);
		scoreList.add(score);
		return score;
	}
	
	/**
	 * 获取所有积分的总分
	 * @return
	 */
	public float getToatalAverageScore(){
		float totalScore = 0.0f;
		int size = scoreList.size();
		LogUtil.i("size=>" + size);
		for (Float f : scoreList) {
			LogUtil.i("f=" + f);
			totalScore += f;
		}
		scoreList.clear();
		totalScore = totalScore / size;
		logWriter.appendLog("[getToatalAverageScore]size=>" + size + ",averageScore=>" + totalScore);
		return totalScore;
	}
	
	/**
	 * 获取k歌挑战赛的结果
	 * @return  k歌挑战赛的结果
	 */
	public SendKGChanllengeResults getKGameChanllengeResult(){
		return chanllengeResult;
	}
	
	@Override
	public void showProgress(String show) {
		
	}

//	@Override
//	public void KGameResult(float result) {
//		LogUtil.i("result=>" + result + ",mid=" + currentSerialId);
//		scoreList.add(result * 100);
//	}
	
	@Override
	public void KGameResult(SendKGChanllengeResults result) {
		LogUtil.i("result=>" + result + ",mid=" + currentSerialId);
		logWriter.appendLog("[KGameResult]result=>" + result.toString() + ",mid=" + currentSerialId);
		scoreList.add((float)result.score);
		chanllengeResult = result;
	}
	
	@Override
	public void allScoreWhenEnd(float[] scoreArr) {
		logWriter.appendLog("[allScoreWhenEnd]scoreArr length=>" + scoreArr.length);
		allScoreArr = scoreArr;
	}
	
	@Override
	public void jiepaiWhenEnd(KGameJiepaiEntity jiepai) {
		logWriter.appendLog("[jiepaiWhenEnd]jiepai=>" + jiepai.toString());
		this.jiepai = jiepai;
	}
	
	/**
	 * 获取一首k歌的所有点的得分
	 * @return k歌的所有点的得分
	 */
	public float[] getAllScore(){
		if (allScoreArr == null) {
			KGameLogUtil.getInstance().appendLog("[getAllScore]getAllScore is null");
		}else {
			KGameLogUtil.getInstance().appendLog("[getAllScore]getAllScore length=>" + allScoreArr.length);
		}
		float[] resArr = getRealScore(allScoreArr);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < resArr.length; i++) {
			sb.append(resArr[i]);
			sb.append(",");
		}
		KGameLogUtil.getInstance().appendLog("[KSongDaFenManager-getAllScore]25 score content=" + sb.toString());
		return resArr;
	}
	
	/**
	 * 结果页面呈现数据只需要25个点
	 * @return 25个平均值
	 */
	private float[] getRealScore(float[] allScore){
		float[] resultArr = new float[25];
		if (allScore == null || allScore.length == 0) {
			return resultArr;
		}
		int desSize = 25;
		int total = allScore.length;
		
		if (total < desSize) {
			KGameLogUtil.getInstance().appendLog("----warning---- allScore Size lower than 25");
			for (int i = 0; i < desSize; i++) {
				int index = i % total;
				resultArr[i] = allScore[index];
			}
		}else {
			int per = total / desSize;
			for (int i = 0; i < desSize; i++) {
				int start = i * per;
				int end = i * per + per;
				if (end > total) {
					end = total;
				}
				
				float tempTotal = 0.0f;
				for (int j = start; j < end; j++) {
					tempTotal += allScore[j];
				}
				float average = tempTotal / (end - start);
				resultArr[i] = average;
			}
		}
		return resultArr;
	}
	
	/**
	 * 获取当前所K歌曲的节拍
	 * @return 节拍
	 */
	public KGameJiepaiEntity getJiePai(){
		return jiepai;
	}
	
	public boolean hasKGameResult(){
		return manager.hasKGameResult();
	}
	
	@Override
	public boolean startRecordDafenFile(Context mContext, String serialId, int musicCurrentPos) {
		jiepai = null;
		chanllengeResult = null;
		allScoreArr = null;
		return super.startRecordDafenFile(mContext, serialId, musicCurrentPos);
	}
	
	// [added by Carlyle
	// [may adjust later on]
	public interface PitchCallBack {
		// [the pitch is figured by current input voice]
		public void currentPitch(int picth);

		// [read the file ,and out put this array.]
		/*
		 * @ar is the array:are like 264 4 18,
		 * 
		 * @point_per_frame:each point last time, 4 will last 4*point_per_frame
		 * ms;
		 */
		public void onPichArrayLoaded(float[] ar,float point_per_frame, int maxPitch,int minPitch, String sirialId);
		public void onStart(int musicCurrentPos, long cutTime);
		public void onStop();
		public void onPitchMatch();
	}
	
	PitchCallBack pc;

	public void setPitchCallBack(PitchCallBack callback) {
		manager.setPitchCallBack(callback);
	}

	// end carlyle]
	
	public void setKSongResultCallback(KSongResultCallback callback){
		LogUtil.i("callback=>" + callback);
		LogUtil.d("KSongDaFenManager", "setKSongResultCallback", new Throwable("setKSongResultCallback"));
		manager.setKSongResultCallback(callback);
	}
	
	public boolean hasKSongResultCallback(){
		boolean hasCallback = manager.hasKSongResultCallback();
		LogUtil.w("hasCallback=>" + hasCallback);
		LogUtil.d("KSongDaFenManager", "hasKSongResultCallback", new Throwable("hasKSongResultCallback"));
		return hasCallback;
	}
	
	//add by fly begin 
	public interface KSongResultCallback{
		public void realHasKSongResult();
		public void onError(String error);
		public void onLoadKGameTemplateOver();
	}
	
	public void setDafenMode(boolean isDafenMode){
		manager.setDafenMode(isDafenMode);
	}
	//add by fly end 
	
	/**
     * 设置要dump的文件路径
     * @param fileName 要生成的录音文件的绝对路径
     */
	public void setDumpPcmPath(String fullPath){
		if (manager != null) {
			manager.setDumpPcmPath(fullPath);
		}
	}
	
	public void setmMediaPlayer(MediaPlayer mMediaPlayer){
		manager.setmMediaPlayer(mMediaPlayer);
	}
}
