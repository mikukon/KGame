package com.yiqiding.ktvbox.ksong.dafen;

import java.io.BufferedWriter;

import android.util.Log;

public class ScoreFilter2 {
	private long filterTotalTime = 0;
	private long lastPastTime = 0;
	private boolean debugMode = false;
	
	public ScoreFilter2(long filterTotalTime) {
		this.filterTotalTime = filterTotalTime;
	}

	public float filterScore(float newScore, long pastTime) {
		if (debugMode) {
			return newScore;
		}
		if (lastPastTime == 0) {
			lastPastTime = pastTime;
		}
		long deltaTime = pastTime - lastPastTime;
		loge("deltaTime==>" + deltaTime);
		if (deltaTime >= 0 && deltaTime <= 1000) {
			log("pastTime 0s~1s, use 50%");
			return newScore * 0.5f;
		} else if (deltaTime > 1000 && deltaTime <= 2000) {
			log("pastTime 1s~2s, use 55%");
			return newScore * 0.55f;
		} else if (deltaTime > 2000 && deltaTime <= 3000) {
			log("pastTime 2s~3s, use 60%");
			return newScore * 0.6f;
		} else if (deltaTime > 3000 && deltaTime <= 4000) {
			log("pastTime 3s~4s, use 65%");
			return newScore * 0.65f;
		} else if (deltaTime > 4000 && deltaTime <= 5000) {
			log("pastTime 4s~5s, use 70%");
			return newScore * 0.7f;
		} else if (deltaTime > 5000 && deltaTime <= 6000) {
			log("pastTime 5s~6s, use 75%");
			return newScore * 0.75f;
		} else if (deltaTime > 6000 && deltaTime <= 7000) {
			log("pastTime 6s~7s, use 80%");
			return newScore * 0.8f;
		} else if (deltaTime > 7000 && deltaTime <= 8000) {
			log("pastTime 7s~8s, use 85%");
			return newScore * 0.85f;
		} else if (deltaTime > 8000 && deltaTime <= 9000) {
			log("pastTime 8s~9s, use 90%");
			return newScore * 0.9f;
		} else if (deltaTime > 9000 && deltaTime <= 10000) {
			log("pastTime 9s~10s, use 95%");
			return newScore * 0.95f;
		} else {
			loge("bigger 10s,just return score " + newScore);
			return newScore;
		}
	}

	private void loge(String msg) {
		System.out.println(msg);
	}

	private void log(String msg) {
		Log.d("ScoreFilter", msg);
		// System.out.println(msg);
		// try {
		// bw.write(msg + "\n");
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

	private BufferedWriter bw;

	public void setBw(BufferedWriter bw) {
		this.bw = bw;
	}
}
