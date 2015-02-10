package com.palm.down.util;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.os.Environment;

/**
 * 文件工具类
 * 
 * @author weixiang.qin
 *
 */
public class FileUtil {
	/**
	 * 外置存储卡是否可写
	 * 
	 * @return
	 */
	public static boolean isExternalAvailable() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	/**
	 * 获取files目录
	 * 
	 * @param mActivity
	 * @return
	 */
	public static String getFilesDirectory(Activity mActivity) {
		if (isExternalAvailable()) {
			return getExternalFilesDirectory(mActivity);
		} else {
			return getInternalFilesDireactory(mActivity);
		}
	}

	/**
	 * 内存卡files路径(/mnt/sdcard/Android/data/package/files/)
	 * 
	 * @param mActivity
	 * @return
	 */
	public static String getExternalFilesDirectory(Activity mActivity) {
		return mActivity.getExternalFilesDir(null).getAbsolutePath()
				+ File.separator;
	}

	/**
	 * ROM中files路径(/data/data/package/files/)
	 * 
	 * @param mActivity
	 * @param fileName
	 * @return
	 */
	public static String getInternalFilesDireactory(Activity mActivity) {
		return mActivity.getFilesDir().getAbsolutePath() + File.separator;
	}
	
	/**
	 * 更改文件权限
	 * 
	 * @param path
	 * @return
	 */
	public static boolean chmod(String path) {
		String command = "chmod 777 " + path;
		try {
			Runtime.getRuntime().exec(command);
			return true;
		} catch (IOException e) {
			LogUtil.printEx(e);
			return false;
		}
	}
}
