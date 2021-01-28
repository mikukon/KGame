package com.r3studio.KGame.gl;

/**
 * Created by double on 16/1/6.
 */
public class GifAnimation {
    private float [] frameDurs;
    private int count;
    private float frameDur;
    private long time;
    private int showIndex = 0;
    private int repeatCount = -1;

    public GifAnimation(int count, float frameDur){
        this.count = count;
        this.frameDur = frameDur;
    }

    public GifAnimation(int count, float frameDur, int repeatCount){
        this.count = count;
        this.frameDur = frameDur;
        this.repeatCount = repeatCount;
    }

    public void setRepeatCount(int repeatCount){
        this.repeatCount = repeatCount;
    }

    public int getKeyFrameIndex() {
        if (repeatCount >= 0 && showIndex == count - 1){
            if (repeatCount > 0){
                repeatCount--;
            }else{
                return showIndex;
            }
        }

        long curTime = System.currentTimeMillis();
        if (time == 0 || count == 1){
            showIndex = 0;
            time = curTime;
        }
        else{
            if (curTime - time >= frameDur) {
                showIndex = (showIndex + 1) % count;
                time = curTime;
            }
        }

        return showIndex;
    }

}
