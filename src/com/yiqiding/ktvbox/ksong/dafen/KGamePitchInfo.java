package com.yiqiding.ktvbox.ksong.dafen;

/**
 * Created by so898 on 14-7-22.
 */
public class KGamePitchInfo {
	private float startTime;
	private float length;
	private int frequency;
	private int pitch;
	private Type type;

	public enum Type {
		SUMMARY, DETAIL, ERROR
	};

//	#COUNTTIME:60
//	+ start:16173 length:312 fre:369 pitch:65
//	* start: 16173 length:60 fre:371 pitch:66
//	* start: 16233 length:60 fre:369 pitch:65
//	* start: 16293 length:60 fre:369 pitch:65
//	* start: 16353 length:60 fre:369 pitch:65
	
	public KGamePitchInfo(String string) {
		try {
			String[] tmp = string.split(" ");

			// make type
			if (tmp[0].startsWith("+")) {
				type = Type.SUMMARY;
			}else if (tmp[0].startsWith("*")) {
				type = Type.DETAIL;
			}
//			if(type==Type.SUMMARY){
//				startTime = Float.parseFloat(tmp[1].split(":")[1])+150;	
//			}else{
//				startTime = Float.parseFloat(tmp[1].split(":")[1]);
//			}
//			if(type==Type.SUMMARY){
//				length = Integer.parseInt(tmp[2].split(":")[1])+100;
//			}else{
//				length = Integer.parseInt(tmp[2].split(":")[1]);
//			}
			startTime = Float.parseFloat(tmp[1].split(":")[1]);
			length = Integer.parseInt(tmp[2].split(":")[1]);
			frequency = Integer.parseInt(tmp[3].split(":")[1]);
			pitch = Integer.parseInt(tmp[4].split(":")[1]);
		} catch (Exception e) {
			e.printStackTrace();
			type = Type.ERROR;
		}
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public int getPitch() {
		return pitch;
	}

	public void setPitch(int pitch) {
		this.pitch = pitch;
	}

	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}

	public void setStartTime(float startTime) {
		this.startTime = startTime;
	}

	public float getStartTime() {
		return startTime;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String convert2line(){
    	StringBuffer sb = new StringBuffer();
    	if(type == Type.SUMMARY){
    		sb.append("+ ");
    	}else if (type == Type.DETAIL) {
    		sb.append("* ");
		}
    	sb.append("\n");
    	return sb.toString();
    }
}
