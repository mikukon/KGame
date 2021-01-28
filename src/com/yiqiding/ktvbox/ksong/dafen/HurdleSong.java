package com.yiqiding.ktvbox.ksong.dafen;

/**
 * Created by nero on 16/3/17.
 */
public class HurdleSong {
    public int song_id;
    public String song_name;
    public String singer;
    public String serial_id;
    public long song_start;  //歌曲开始点：毫秒
    public int song_last;    //歌曲持续时间：秒

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HurdleSong song = (HurdleSong) o;

        return song_id == song.song_id;

    }

    @Override
    public int hashCode() {
        return song_id;
    }
}
