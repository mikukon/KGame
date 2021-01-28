package com.yiqiding.ktvbox.structure;

public class KGameJiepaiEntity {
	private int perfectNum;
	private int greatNum;
	private int goodNum;
	private int coolNum;
	private int badNum;
	private int okNum;
	private int missNum;

	public int getPerfectNum() {
		return perfectNum;
	}

	public void setPerfectNum(int perfectNum) {
		this.perfectNum = perfectNum;
	}

	public int getGreatNum() {
		return greatNum;
	}

	public void setGreatNum(int greatNum) {
		this.greatNum = greatNum;
	}

	public int getGoodNum() {
		return goodNum;
	}

	public void setGoodNum(int goodNum) {
		this.goodNum = goodNum;
	}

	public int getOkNum() {
		return okNum;
	}

	public void setOkNum(int okNum) {
		this.okNum = okNum;
	}

	public int getMissNum() {
		return missNum;
	}

	public void setMissNum(int missNum) {
		this.missNum = missNum;
	}

	public int getCoolNum() {
		return coolNum;
	}

	public void setCoolNum(int coolNum) {
		this.coolNum = coolNum;
	}

	public int getBadNum() {
		return badNum;
	}

	public void setBadNum(int badNum) {
		this.badNum = badNum;
	}

	@Override
	public String toString() {
		return "KGameJiepaiEntity [perfectNum=" + perfectNum + ", greatNum="
				+ greatNum + ", goodNum=" + goodNum + ", okNum=" + okNum
				+ ", missNum=" + missNum + "]";
	}

}
