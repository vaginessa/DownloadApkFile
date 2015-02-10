package com.palm.down.activity;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.palm.down.R;
import com.palm.down.thread.DownloadThread;
import com.palm.down.util.FileUtil;

/**
 * 下载
 * 
 * @author weixiang.qin
 *
 */
@SuppressLint("HandlerLeak")
public class DownloadActivity extends Activity implements OnClickListener {
	private static final int ID = 1;// 通知id
	private Activity mActivity;
	private Button downBtn;
	private Button cancelBtn;
	private TextView downTv;
	private ProgressBar progressPb;
	private ProgressHandler handler;
	private Thread thread;
	private NotificationManager manager;
	private Notification notification;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		initView();
	}

	private void initView() {
		mActivity = this;
		handler = new ProgressHandler();
		downBtn = (Button) findViewById(R.id.down_btn);
		cancelBtn = (Button) findViewById(R.id.cancel_btn);
		downTv = (TextView) findViewById(R.id.down_tv);
		progressPb = (ProgressBar) findViewById(R.id.progress_pb);
		showProgress(0);
		downBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
	}

	class ProgressHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case DownloadThread.MSG_PROGRESS:
				int progress = msg.arg1;
				showProgress(progress);
				progressPb.setProgress(progress);
				showDowningNotify(progress);
				break;
			case DownloadThread.MSG_ERROR:
				downTv.setText("下载失败");
				showDownErrorNofify();
				break;
			case DownloadThread.MSG_SUCCESS:
				showDownFinishNotify((String) msg.obj);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 下载进度
	 * 
	 * @param progress
	 */
	private void showProgress(int progress) {
		downTv.setText(progress + "%");
	}

	/**
	 * 开始下载
	 */
	private void startDown() {
		if (thread != null && thread.isAlive()) {
			return;
		}
		thread = new DownloadThread(mActivity, handler);
		thread.start();
	}

	/**
	 * 停止下载
	 */
	private void stopDown() {
		if (thread != null) {
			thread.interrupt();
		}
	}

	/**
	 * 安装apk
	 * 
	 * @param apkFile
	 */
	public void installWithChmod(String filePath) {
		FileUtil.chmod(filePath);
		Uri uri = Uri.fromFile(new File(filePath));
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		mActivity.startActivity(intent);
	}

	/**
	 * 下载通知
	 */
	private void showDownNofify() {
		manager = (NotificationManager) mActivity
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notification = new Notification(R.drawable.logo, null,
				System.currentTimeMillis());
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.defaults = Notification.DEFAULT_LIGHTS;
		notification.contentView = new RemoteViews(getApplication()
				.getPackageName(), R.layout.app_down_notification);
		manager.notify(ID, notification);
	}

	/**
	 * 下载中通知
	 * 
	 * @param progress
	 */
	private void showDowningNotify(int progress) {
		notification.contentView.setProgressBar(R.id.progress_pb, 100,
				progress, false);
		notification.contentView.setTextViewText(R.id.progress_tv, progress
				+ "%");
		manager.notify(ID, notification);
	}

	/**
	 * 下载失败
	 */
	private void showDownErrorNofify() {
		notification.setLatestEventInfo(mActivity, "下载失败", "请重新下载", null);
		manager.notify(ID, notification);
	}

	/**
	 * 下载完成通知
	 */
	private void showDownFinishNotify(String filePath) {
		FileUtil.chmod(filePath);
		Uri uri = Uri.fromFile(new File(filePath));
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		PendingIntent contentIntent = PendingIntent.getActivity(mActivity, 0,
				intent, 0);
		notification.setLatestEventInfo(mActivity, "下载完成", "点击安装冀彩宝",
				contentIntent);
		manager.notify(ID, notification);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == downBtn.getId()) {
			showDownNofify();
			startDown();
		} else if (v.getId() == cancelBtn.getId()) {
			stopDown();
		}
	}
}
