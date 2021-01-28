package com.yiqiding.ktvbox.config;

import android.text.TextUtils;

import com.yiqiding.ktvbox.libutils.LogUtil;

import java.io.File;
import java.io.IOException;


/**
 * 统一管理点歌系统所有持久化的文件
 * @author zhuruyi
 */
public class KTVBoxPathManager {
	public static final int TYPE_SERIAL_CONFIG = 0x1000;
	public static final int TYPE_DAFEN_TEMPLATE = TYPE_SERIAL_CONFIG + 1;
	public static final int TYPE_FIRE_VIDEO = TYPE_SERIAL_CONFIG + 2;
	public static final int TYPE_FIRE_IMAGE = TYPE_SERIAL_CONFIG + 3;
	public static final int TYPE_SCREEN_GUARD = TYPE_SERIAL_CONFIG + 4;
	public static final int TYPE_UPLOAD_LOG_APP = TYPE_SERIAL_CONFIG + 5;
	public static final int TYPE_UPLOAD_LOG_ROM = TYPE_SERIAL_CONFIG + 6;
	public static final int TYPE_IMG_DISK_CACHE = TYPE_SERIAL_CONFIG + 7;
	public static final int TYPE_DANMU_IMAGE = TYPE_SERIAL_CONFIG + 8;
	public static final int TYPE_TEMP_DIR = TYPE_SERIAL_CONFIG + 9;
	public static final int TYPE_DATA_STORE_DIR = TYPE_SERIAL_CONFIG + 10;
	public static final int TYPE_UPLOAD_BOX_DATA = TYPE_SERIAL_CONFIG + 11;
	public static final int TYPE_GAME_APK = TYPE_SERIAL_CONFIG + 12;
	public static final int TYPE_GAME_ALBUM = TYPE_SERIAL_CONFIG + 13;
	public static final int TYPE_HOT_GAME_ALBUM = TYPE_SERIAL_CONFIG + 14;
	public static final int TYPE_NET_IMG = TYPE_SERIAL_CONFIG + 15;
	public static final int TYPE_RECORD_FILE = TYPE_SERIAL_CONFIG + 16;
	public static final int TYPE_ADS_IMG = TYPE_SERIAL_CONFIG + 17;
	public static final int TYPE_APP_LANDSCAPE_IMAGE = TYPE_SERIAL_CONFIG + 18;
	public static final int TYPE_HOT_RECOMMEND_ALBUM = TYPE_SERIAL_CONFIG + 19;// 热门推荐影片图片 @add by ytxu 2016-1-20
	static String bp;//[other function must be called after this is assigned]
	public static void setBasePath(String basePath){
		bp=basePath;
	}

	/**
	 * 根据类别获取路径
	 * @param type 类别 KTVBoxPathManager中的常量 -1表示获取getExternalCacheDir基础目录
	 * @return 路径  结尾自带/
	 */
	public static String getPathDirByType(int type){
//		String basePath = KTVBoxApplication.getInstance().getExternalCacheDir().toString();
		String basePath = bp;//con.getExternalCacheDir().toString();
		if (!basePath.endsWith("/")) {
			basePath += "/";
		}
		switch (type) {
		case TYPE_SERIAL_CONFIG:
			basePath += "serial_config/";
			break;
		case TYPE_DAFEN_TEMPLATE:
			basePath += "dafen_template/";
			break;
		case TYPE_FIRE_VIDEO:
			basePath += "fire_video/";
			break;
		case TYPE_FIRE_IMAGE:
			basePath += "fire_image/";
			break;
		case TYPE_SCREEN_GUARD:
			basePath += "screen_guard/";
			break;
		case TYPE_UPLOAD_LOG_APP:
		case TYPE_UPLOAD_LOG_ROM:
			basePath += "log/";
			break;
		case TYPE_UPLOAD_BOX_DATA:
			basePath += "analyse/";
			break;
		case TYPE_IMG_DISK_CACHE:
			basePath += "disk_cache/";
			break;
		case TYPE_DANMU_IMAGE:
			basePath += "danmu/";
			break;
		case TYPE_TEMP_DIR:
			basePath += "temp/";
			break;
		case TYPE_DATA_STORE_DIR:
			basePath += "data_store/";
			break;
		case TYPE_GAME_APK:
			basePath += "game_apk/";
			break;
		case TYPE_GAME_ALBUM:
			basePath += "game_album/";
			break;
		case TYPE_HOT_GAME_ALBUM:
			basePath += "hot_game/";
			break;
		case TYPE_NET_IMG:
			basePath += "net_image/";
			break;
		case TYPE_RECORD_FILE:
			basePath += "record_file/";
			break;
		case TYPE_ADS_IMG:
			basePath += "ads_img/";
			break;
		case TYPE_APP_LANDSCAPE_IMAGE:
//			basePath += "app_landscape_image/";
//			if (BuildConfig.DEBUG) {
//				basePath = "/sdcard/yiqichangappimgs/";
//			}
//			basePath = basePath.substring(0, basePath.indexOf("cache/")) + "app_landscape_image/";
			basePath = "/sdcard/Android/data/com.yiqiding.ktvbox/app_landscape_image/";
			break;
		case TYPE_HOT_RECOMMEND_ALBUM:
			basePath += "hot_recommend_album/";
			break;
		default:
			break;
		}
		
		File mFile = new File(basePath);
		if (!mFile.exists()) {
			LogUtil.i("path dir=>" + basePath);
			mFile.mkdirs();
		}
		
		LogUtil.i("dir=>" + basePath);
		return basePath;
	}
	
	/**
	 * 创建一个临时文件 
	 * @param suffix 文件后缀
	 * @return 创建的临时文件路径
	 */
	public static String createTempFile(String suffix){
		long currentFile = System.currentTimeMillis();
		StringBuffer sb = new StringBuffer();
		sb.append(getPathDirByType(TYPE_TEMP_DIR));
		sb.append(String.valueOf(currentFile));
		sb.append(".");
		if (TextUtils.isEmpty(suffix)) {
			sb.append("txt");
		}else {
			sb.append(suffix);
		}
		String filePath = sb.toString();
		File mFile = new File(filePath);
		if (!mFile.exists()) {
			try {
				mFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		LogUtil.i("new file path=>" + filePath);
		return filePath;
	}
	
	/**
	 * 
	 * @param fullName  创建的临时文件全名    名字+后缀
	 * @return
	 */
	public static String createTempFile(String fullName, String suffix){
		StringBuffer sb = new StringBuffer();
		sb.append(getPathDirByType(TYPE_TEMP_DIR));
		sb.append(fullName);
		sb.append(".");
		if (TextUtils.isEmpty(suffix)) {
			sb.append("txt");
		}else {
			sb.append(suffix);
		}
		String filePath = sb.toString();
		File mFile = new File(filePath);
		if (!mFile.exists()) {
			try {
				mFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		LogUtil.i("new file path=>" + filePath);
		return filePath;
	}
	
	/**
	 * 清空临时文件夹
	 */
	public static void clearTempFile(){
		File dir = new File(getPathDirByType(TYPE_TEMP_DIR));
		if (dir != null && dir.isDirectory()) {
			File[] subFiles = dir.listFiles();
			if (subFiles != null && subFiles.length > 0) {
				for (File file : subFiles) {
					if (file.isDirectory()) {
						LogUtil.e("can't delete dir =>" + file.toString());
					}else {
						file.delete();
					}
				}
			}
		}
	}
	
}
