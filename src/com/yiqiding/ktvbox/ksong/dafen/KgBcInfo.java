package com.yiqiding.ktvbox.ksong.dafen;


public class KgBcInfo {
	private float start;
	private float end;
	private float res;
	

	public enum  compareType{
		EQUALS,
		LESSTHAN,
		MORETHAN
	}
	public float getStart() {
		return start;
	}

	public void setStart(float start) {
		this.start = start;
	}

	public float getEnd() {
		return end;
	}

	public void setEnd(float end) {
		this.end = end;
	}

	public float getRes() {
		return res;
	}

	public void setRes(float res) {
		this.res = res;
	}
	
	public compareType isContain(float res ){
		if(res<start){
			return compareType.LESSTHAN;
		}
		if(res>end){
			return compareType.MORETHAN;
		}
		return compareType.EQUALS;
	}
}
