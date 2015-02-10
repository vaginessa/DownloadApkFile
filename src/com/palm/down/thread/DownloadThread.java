package com.palm.down.thread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.palm.down.util.FileUtil;
import com.palm.down.util.LogUtil;

/**
 * 下载线程
 * 
 * @author weixiang.qin
 *
 */
public class DownloadThread extends Thread {
	private static final String APK_URL = "http://o2o.yzfcw.com/ClientO2O_Station.apk";
	private static final String APK_NAME = "HBClient.apk";
	private static final int BUFFER_SIZE = 8192;// 下载缓存
	public static final int MSG_PROGRESS = 1;// 进度
	public static final int MSG_ERROR = 2;// 错误
	public static final int MSG_SUCCESS = 3;// 成功
	private Activity mActivity;
	private Handler handler;

	public DownloadThread(Activity mActivity, Handler handler) {
		this.mActivity = mActivity;
		this.handler = handler;
	}

	@Override
	public void run() {
		super.run();
		try {
			downloadApk(APK_URL, FileUtil.getFilesDirectory(mActivity)
					+ APK_NAME);
		} catch (Exception e) {
			LogUtil.printEx(e);
			Message msg = new Message();
			msg.what = MSG_ERROR;
			handler.sendMessage(msg);
		}
	}

	/**
	 * 下载APK
	 * 
	 * @param url
	 * @param fileName
	 * @throws Exception
	 */
	public void downloadApk(String url, String filePath) throws Exception {
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
		}
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setConnectTimeout(30000);
			connection.setReadTimeout(30000);
			connection.setRequestMethod("GET");
			connection.connect();
			if (isInterrupted()) {
				return;
			}
			if (HttpURLConnection.HTTP_OK != connection.getResponseCode()) {
				throw new Exception("response status is not ok.");
			}
			boolean isSuc = streamToFile(connection.getContentLength(),
					connection.getInputStream(), filePath);
			if (isSuc) {
				Message msg = new Message();
				msg.what = MSG_SUCCESS;
				msg.obj = filePath;
				handler.sendMessage(msg);
			}
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	/**
	 * 输入流写入文件
	 * 
	 * @param totalsize
	 * @param is
	 * @param filePath
	 * @throws Exception
	 */
	public boolean streamToFile(long totalsize, InputStream is, String filePath)
			throws Exception {
		int progress = 0;
		int lastProgress = 0;
		FileOutputStream os = null;
		int bytesRead = 0;
		int finishSize = 0;
		byte[] buffer = new byte[BUFFER_SIZE];
		try {
			os = new FileOutputStream(filePath);
			while ((bytesRead = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
				if (isInterrupted()) {
					break;
				}
				os.write(buffer, 0, bytesRead);
				os.flush();
				finishSize += bytesRead;
				progress = (int) (100 * finishSize / totalsize);
				if(lastProgress != progress){
					Message msg = new Message();
					msg.what = MSG_PROGRESS;
					msg.arg1 = progress;
					handler.sendMessage(msg);
				}
				lastProgress = progress;
			}
			if (progress == 100) {
				return true;
			}
			return false;
		} finally {
			os.close();
			is.close();
		}
	}

}
