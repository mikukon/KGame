package com.yiqiding.ktvbox.ksong.dafen;

import android.content.Context;
import android.text.TextUtils;

import com.yiqiding.ktvbox.config.KTVBoxPathManager;
import com.yiqiding.ktvbox.libutils.LogUtil;

/**
 * k歌大赛打分逻辑的骨架实现类  大体实现了 标准分数文件的下载  打分模版的解析  
 * 录制 生成用户打分文件 以及 打分算法计算分数。
 * @author zhuruyi
 *
 */
public abstract class AbstractKSongDaFen implements IKSongDaFen, KGameResultListener{
	protected String currentSerialId;
	protected KGameManager manager;
	
	@Override
	public boolean downloadStandardKSongDafenFiles(String[] mids) {
		LogUtil.i(".........");
		return true;
	}
	
	@Override
	public String generateDaFenTemplate(String mid) {
		return null;
	}
	
	@Override
	public void parseDafenTemplate(Context con,String serialId) {
		currentSerialId = serialId;
		manager.loadKGameOfSid(con,Long.parseLong(serialId));
	}
	
	@Override
	public boolean startRecordDafenFile(Context mContext, String serialId, int musicCurrentPos) {
		LogUtil.i(".........");
//		parseDafenTemplate(mContext,serialId);
		manager.start(musicCurrentPos);
		return false;
	}

	@Override
	public boolean cancelRecordDafenFile() {
		LogUtil.i(".............");
		manager.stop();
//		Log.d("AbstractKSongDaFen", "debug cancel", new Throwable("debug cancel"));
		return true;
	}

	@Override
	public boolean endRecordDafenFile(String mid) {
		if (TextUtils.isEmpty(mid)) {
			mid = currentSerialId;
			LogUtil.w("param is null, so use currentMid=>" + currentSerialId);
		}
		cancelRecordDafenFile();
		return true;
	}

	@Override
	public float calcAvaerageScore() {
		return 0.0f;
	}
	
	/**
	 * 根据mid获取用于做参照比较的路径
	 * @param serialId 歌曲的serialId
	 * @return 路径
	 */
	public String getCompareDafenFileFromSerialId(String serialId){
		return  KTVBoxPathManager.getPathDirByType(KTVBoxPathManager.TYPE_DAFEN_TEMPLATE) + serialId + ".kgame";
	}
	
	/**
	 * 根据mid获取打分模版的的路径
	 * @param mid 歌曲的mid
	 * @deprecated
	 * @return 路径
	 */
	public String getTemplateDafenFileFromMid(String mid){
		return  KTVBoxPathManager.getPathDirByType(KTVBoxPathManager.TYPE_DAFEN_TEMPLATE) + mid + "_template.txt";
	}
	
	/**
	 * 根据mid获取用户生成的打分文件的路径
	 * @param mid 歌曲的mid
	 * @deprecated
	 * @return 路径
	 */
	public String getUserDafenFileFromMid(String mid){
		return null;
	}
	

	@Override
	public boolean downloadKSongDafenFile(String tem_path, String serialId) {
		if (tem_path == null || serialId == null) {
			return false;
		}
		return true;	
	}
	
}
