package com.yiqiding.ktvbox.ksong.dafen;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by so898 on 14-7-22.
 */
public class KGameFileInfo {
    private String title;
    private String artist;
    private long sid;
    private long mid;
    private float bpm;
    private long countTime;
    private long gap;
    private ArrayList<KGamePitchInfo> pitchInfos;

    public KGameFileInfo(BufferedReader br) throws IOException {
        String line;
        pitchInfos = new ArrayList<KGamePitchInfo>();
        while ((line = br.readLine()) != null) {
            if (line.substring(0, 1).equals("#")){
                if (line.contains("#TITLE")){
                    title = line.split(":")[1];
                } else if (line.contains("#ARTIST")){
                    artist = line.split(":")[1];
                } else if (line.contains("#SID")){
                    sid = Integer.parseInt(line.split(":")[1]);
                } else if (line.contains("#MID")){
                    mid = Integer.parseInt(line.split(":")[1]);
                } else if (line.contains("#COUNTTIME")) {
					countTime = Integer.parseInt(line.split(":")[1]);
				} else if (line.contains("#BPM")){
                    if (line.contains(",")){
                        String tmp1 = line.split(":")[1];
                        tmp1 = tmp1.replace(",", ".");
                        bpm = Float.valueOf(tmp1);
                    } else {
                        bpm = 1/Float.valueOf(line.split(":")[1]);
                    }
                } else if (line.contains("#GAP")){
                    gap = Integer.parseInt(line.split(":")[1]);
                }
            } else if (line.substring(0, 1).equals("E")){
                break;
            } else {
                pitchInfos.add(new KGamePitchInfo(line));
            }
        }
    }

    public String getTitle(){
        return title;
    }

    public String getArtist(){
        return artist;
    }

    public long getSid(){
        return sid;
    }

    public long getMid(){
        return mid;
    }

    public long getGap(){
        return gap;
    }

    public float getBpm(){
        return bpm;
    }

    public ArrayList<KGamePitchInfo> getPitchInfos(){
        return pitchInfos;
    }

	@Override
	public String toString() {
		return "KGameFileInfo [title=" + title + ", artist=" + artist
				+ ", sid=" + sid + ", mid=" + mid + ", bpm=" + bpm + ", gap="
				+ gap + ", pitchInfos=" + pitchInfos + "]";
	}

	public long getCountTime() {
		return countTime;
	}

	public void setCountTime(long countTime) {
		this.countTime = countTime;
	}
}
