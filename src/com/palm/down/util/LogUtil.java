package com.palm.down.util;

import android.util.Log;

/**
 * 日志工具类
 * 
 * @author weixiang.qin
 * 
 */
public class LogUtil implements Const {
	/**
	 * info日志
	 * 
	 * @param msg
	 */
	public static void info(String msg) {
		if (IS_DEBUG) {
			Log.i("Palm", msg);
		}
	}

	/**
	 * error日志
	 * 
	 * @param message
	 */
	public static void error(String message) {
		if (IS_DEBUG) {
			Log.e("Palm", message);
		}
	}

	/**
	 * printStackTrace
	 * 
	 * @param ex
	 */
	public static void printEx(Throwable ex) {
		if (IS_DEBUG) {
			ex.printStackTrace();
		}
	}

}
