package com.r3studio.KGame;

public class ItemInfo {
	// 盘符.名字.serialid.original_track.sound_track.kgame
	// 04.77174304.1780500253.0.1.kgame
	private boolean DEBUG = false;
	public String fn;
	public String pan;
	public String name;
	public String serialid;
	public int original = 0;
	public int sound = 1;
	public String chin;
	
	public static String resourceIp;
	
	public ItemInfo(String serialid) {
		this.serialid = serialid;
	}
	
	public ItemInfo(String file, String pan, String name, String serialid, String original, String sound,String ch) {
		this.fn = file;
		this.pan = pan;
		this.name = name;
		this.serialid = serialid;
		chin=ch;
		check(original, sound);
	}
	
	public ItemInfo(String file, String pan, String name, String serialid, String original, String sound) {
		this.fn = file;
		this.pan = pan;
		this.name = name;
		this.serialid = serialid;
		check(original, sound);
	}
	
	private void check(String o, String s) {
		int ori = original = 0;
		try {
			ori = Integer.valueOf(o);
		} catch (Exception e) {
		}
		int acc = sound = 1;
		try {
			acc = Integer.valueOf(s);
		} catch (Exception e) {
		}

		if (ori > 1 || acc > 1) {
			if (ori == 1) {
				original = 0;
				sound = 1;
			} else {
				original = 1;
				sound = 0;
			}
		} else {
			if (ori == 0) {
				original = 0;
				sound = 1;
			} else {
				original = 1;
				sound = 0;
			}
		}
	}

	public String getFileName() {
		fn = fn.replace("\n", "");
		fn = fn.replace("\r", "");
		return fn;
	}
	
	public String getUrl() {
		if (DEBUG) {
//			return "/sdcard/mp4/" + name + ".mp3";
			return "/sdcard/wozhizaihuni.mp3";
		}
		if (resourceIp.startsWith("http://")) {
			return resourceIp + "/" + pan + "/" + name + ".mp4";
		}else {
			return "http://" + resourceIp + "/" + pan + "/" + name + ".mp4";
		}
//		return "/sdcard/ggh.mp4";
	}
	
	public long getSerialid() {
		return Long.valueOf(serialid);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ItemInfo)) {
			return false;
		}
		if (serialid != null || serialid.equals(((ItemInfo) o).serialid)) {
			return true;
		}
		return false;
	}
}
