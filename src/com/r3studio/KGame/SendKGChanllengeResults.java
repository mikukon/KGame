package com.r3studio.KGame;

import com.yiqiding.ktvbox.libutils.LogUtil;

import android.text.TextUtils;

public class SendKGChanllengeResults {
	public int normal; // 正常结束或放弃比赛或超时无积分(1 或 0 或-1)
	public int score;
	public int yinzhun;
	public int wending;
	public int biaoxianli;
	public int jiezou;
	public int jiqiao;
	public Jiepai jiepai;
	
	public String getJsonStr() {
		String json = JsonUtils.toJson(this);
		LogUtil.d("SendKGChanllengeResults", "#getJsonStr " + json);
//		KGameLogUtil.getInstance().appendLog("[SendKGChanllengeResults-getJsonStr]" + json);
		return json;
	}
	
	
	public int[] getAbilityValue() {
		return new int[] { yinzhun, wending, biaoxianli, jiezou, jiqiao };
	}
	
	public static SendKGChanllengeResults convertByJson(String json) {
		if (TextUtils.isEmpty(json)) {
			return null;
		}
		try {
			return JsonUtils.toObject(json, SendKGChanllengeResults.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void initJiepai(int perfect, int great, int good, int cool,int bad, int miss){
		Jiepai obj = new Jiepai(perfect, great, good, cool,bad, miss);
		jiepai = obj;
	}

	@Override
	public String toString() {
		return "SendKGChanllengeResults [normal=" + normal + ", score=" + score
				+ ", yinzhun=" + yinzhun + ", wending=" + wending
				+ ", biaoxianli=" + biaoxianli + ", jiezou=" + jiezou
				+ ", jiepai=" + jiepai + ", jiqiao=" + jiqiao + "]";
	}
	
	public static SendKGChanllengeResults getErrorResults() {
		SendKGChanllengeResults params = new SendKGChanllengeResults();
		params.normal = 0;
		params.score = 0;
		params.yinzhun = 0;
		params.wending = 0;
		params.biaoxianli = 0;
		params.jiezou = 0;
		params.jiepai = new Jiepai(0, 0, 0, 0, 0, 0);
		params.jiqiao = 0;
		return params;
	}
	
	public static class Jiepai {
		public int perfect;
		public int great;
		public int good;
		public int cool;
		public int bad;
		public int miss;
		public Jiepai(int perfect, int great, int good, int cool,int bad, int miss) {
			super();
			this.perfect = perfect;
			this.great = great;
			this.good = good;
			this.cool = cool;
			this.bad = bad;
			this.miss = miss;
		}
		@Override
		public String toString() {
			return "Jiepai [perfect=" + perfect + ", great=" + great
					+ ", good=" + good + ", cool=" + cool +  ", bad=" + bad+", miss=" + miss + "]";
		}
	}
	
}
