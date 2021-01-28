package com.yiqiding.ktvbox.ksong.dafen;

/**
 * 用于k歌闯关赛时候实时上报当前的分数 perfect比例 miss比例
 * 
 * @author zhuruyi
 */

public class EventDafenProcess {
	private float score;
	private float perfectRate;
	private float missRate;

	/**
	 * 当前的音程分数
	 * 
	 * @return 例如 80
	 */
	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	/**
	 * 当前的perfect比例
	 * 
	 * @return 例如 80
	 */
	public float getPerfectRate() {
		return perfectRate;
	}

	public void setPerfectRate(float perfectRate) {
		this.perfectRate = perfectRate;
	}

	/**
	 * 当前的miss比例
	 * 
	 * @return 例如 80
	 */
	public float getMissRate() {
		return missRate;
	}

	public void setMissRate(float missRate) {
		this.missRate = missRate;
	}

	@Override
	public String toString() {
		return "EventDafenProcess [score=" + score + ", perfectRate="
				+ perfectRate + ", missRate=" + missRate + "]";
	}

}
