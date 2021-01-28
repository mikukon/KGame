package com.yiqiding.ktvbox.ksong.dafen;

import android.content.Context;

public interface IKSongDaFen {
	/**
	 * 下载k歌比赛需要的参考模版
	 * @param mids 需要下载的文件的mid 
	 * @return 下载是否成功
	 */
	public boolean downloadStandardKSongDafenFiles(String[] mids);
	
	/**
	 * 第一版打分算法中的实现需要  第二版已经废弃
	 * 根据已录制过的打分文件 生成一个仅仅具有时间戳的模版
	 * @param mid 已录制过的打分歌曲的mid 
	 * @return 生成的模版路径
	 */
	@Deprecated 
	public String generateDaFenTemplate(String mid); 
	
	/**
	 * 解析打分文件 获取时间戳
	 * 第一版打分算法中的实现需要  第二版已经废弃
	 * @param serialId 打分歌曲的mid 
	 * @return
	 */
	public void parseDafenTemplate(Context con,String serialId);
	
	/**
	 * 开始录制
	 * @param mContext
	 * @param serialId 需要录制的歌曲的serialId
	 * @return
	 */
	public boolean startRecordDafenFile(Context mContext, String serialId, int musicCurrentPos);
	
	/**
	 * 取消录制打分文件
	 * @return
	 */
	public boolean cancelRecordDafenFile();
	
	/**
	 * 录制打分文件结束
	 * @param mid 需要录制的歌曲的mid
	 * @return 录制是否成功
	 */
	public boolean endRecordDafenFile(String mid);
	
	/**
	 * 获取k歌比赛的积分 
	 * 该函数仅在第一版计算方式中使用 第二版已经废弃
	 * @return
	 */
	@Deprecated
	public float calcAvaerageScore();
	
	/**
	 * 下载K歌打分文件
	 * @param tem_path 打份文件模板Url
	 * @param serialId  歌曲serialId
	 * @return
	 */
	public boolean downloadKSongDafenFile(String tem_path, String serialId);
}
