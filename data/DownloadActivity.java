package com.palm.down.activity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;

import com.palm.down.R;

public class DownloadActivity extends Activity implements OnClickListener {
	private Button downBtn;
	private ProgressBar progressPb;
	public static final int FETECH_CONNECTION_TIME_OUT = 10000;// 从连接池中取连接的超时时间(单位毫秒)
	public static final int CONNECTION_TIME_OUT = 10000;// 设置连接超时时间(单位毫秒)
	public static final int SO_TIME_OUT = 20000;// 设置读数据超时时间(单位毫秒)
	public final static int MAX_TOTAL_CONNECTIONS = 800;// 最大连接数
	public final static int MAX_ROUTE_CONNECTIONS = 400;// 每个路由最大连接数
	public static final String CHARSET_UTF8 = "UTF-8";
	public static final String STATUSCODE_ERROR = "返回错误码异常";
	private static final String HTTP_GET = "get";
	private static final String HTTP_POST = "post";
//	private static HttpClientUtil httpClientUtil;
	private HttpClient httpClient;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		initView();
	}

	private void initView() {
		handler = new ProgressHandler();
		downBtn = (Button) findViewById(R.id.down_btn);
		downBtn.setOnClickListener(this);
		progressPb = (ProgressBar) findViewById(R.id.progress_pb);
	}
	
	class ProgressHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			progressPb.setProgress(msg.arg1);
		}
	}
	
	public void downloadByHttpGet(String url, String fileName) throws Exception{
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setConnectTimeout(30000);
			connection.setReadTimeout(30000);
			connection.setRequestMethod("GET");
			connection.connect();
			if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
				bufferToFile(connection.getContentLength(), connection.getInputStream(), new File(fileName));
			}
			
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
	
	public void downloadFile(String url, String fileName) throws Exception {
		HttpClient httpClient = getHttpClient();
		HttpGet httpGet = new HttpGet(url);
		HttpResponse response = httpClient.execute(httpGet);
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != HttpStatus.SC_OK) {
			throw new Exception();
		}
		HttpEntity entity = response.getEntity();
		long size = entity.getContentLength();
		streamToFile(size, response.getEntity().getContent(), new File(fileName));
//		httpGet.abort();
	}
	
	public void bufferToFile(long size, InputStream ins, File file) throws Exception{
		FileOutputStream fos = null;
		BufferedInputStream bis = null;
		bis = new BufferedInputStream(ins);
		fos = new FileOutputStream(file);
		int bytesRead = 0;
		int finishSize = 0;
		byte[] buf = new byte[8192];
		while ((bytesRead = bis.read(buf)) != -1) {
			fos.write(buf, 0, bytesRead);
			finishSize += bytesRead;
			int progress = (int) (100 * finishSize / size);
			Message msg = new Message();
			msg.arg1 = progress;
			handler.sendMessage(msg);
		}
		fos.flush();
		bis.close();
		fos.close();
	}
	
	public void streamToFile(long size, InputStream ins, File file)
			throws Exception {
		FileOutputStream os = new FileOutputStream(file);
		int bytesRead = 0;
		int finishSize = 0;
		byte[] buffer = new byte[8192];
		while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
			os.write(buffer, 0, bytesRead);
			finishSize += bytesRead;
			int progress = (int) (100 * finishSize / size);
			Message msg = new Message();
			msg.arg1 = progress;
			handler.sendMessage(msg);
		}
		os.close();
		ins.close();
	}
	
	/**
	 * 获取httpclient
	 * 
	 * @return
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 * @throws IOException
	 * @throws CertificateException
	 */
	private synchronized HttpClient getHttpClient() throws Exception {
		if (httpClient == null) {
			HttpParams params = new BasicHttpParams();
			// 最大连接数
			ConnManagerParams.setMaxTotalConnections(params,
					MAX_TOTAL_CONNECTIONS);
			// 设置每个路由最大连接数
			ConnManagerParams.setMaxConnectionsPerRoute(params,
					new ConnPerRouteBean(MAX_ROUTE_CONNECTIONS));
			// 从连接池中取连接的超时时间
			ConnManagerParams.setTimeout(params, FETECH_CONNECTION_TIME_OUT);
			// 连接超时
			HttpConnectionParams.setConnectionTimeout(params,
					CONNECTION_TIME_OUT);
			// 请求超时
			HttpConnectionParams.setSoTimeout(params, SO_TIME_OUT);
			// 设置参数
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, CHARSET_UTF8);
			HttpProtocolParams.setUseExpectContinue(params, true);
			// 设置我们的HttpClient支持HTTP和HTTPS两种模式
			SchemeRegistry schReg = new SchemeRegistry();
			schReg.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
//			schReg.register(new Scheme("https", getSSLSocketFactory(), 443));
			// 使用线程安全的连接管理来创建HttpClient
			ClientConnectionManager manager = new ThreadSafeClientConnManager(
					params, schReg);
			httpClient = new DefaultHttpClient(manager, params);
		}
		return httpClient;
	}
	
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
	 * 外置存储卡路径(/mnt/sdcard/)
	 * 
	 * @return
	 */
	public static String getExternalDirectory() {
		return Environment.getExternalStorageDirectory().getAbsoluteFile()
				+ File.separator;
	}
	
	public static String getInternalDireactory(Activity mActivity,
			String fileName) {
		String path = mActivity.getFilesDir().getParent() + File.separator
				+ fileName;
		File file = new File(path);
		if (file.exists()) {
			return path;
		} else {
			if(file.mkdirs()){
				return path;
			}
			return null;
		}
	}
	
	/**
	 * chmod
	 * 
	 * @param path
	 * @return
	 */
	public static boolean chmod(String path) {
		String command = "chmod " + path + " " + "777" + " && busybox chmod "
				+ path + " " + "777";
		try {
			Runtime.getRuntime().exec(command);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * 创建目录
	 * 
	 * @param dir
	 * @return
	 */
	public static boolean createDir(String dir) {
		File file = new File(dir);
		return file.mkdirs();
	}
	
	private static final String PATH = "palm" + File.separator + "down";
	
	/**
	 * 保存图片
	 * 
	 * @param bitmap
	 * @param fileName
	 * @return
	 */
	public String getPath(String fileName) {
		if (!isExternalAvailable()) {
			return null;
		}
//		String path = getExternalDirectory() + PATH + File.separator + fileName;
		String path = getInternalDireactory(this, "down") + File.separator + fileName;
		File file = new File(path);
//		File parent = file.getParentFile();
//		boolean isExists = parent.exists();
//		if (!isExists) {
//			createDir(parent.getAbsolutePath());
//		}
		if(file.exists()){
			file.delete();
		}
		return path;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == downBtn.getId()) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
//						downloadFile("http://o2o.yzfcw.com/ClientO2O_Station.apk", getPath("client.apk"));
						downloadByHttpGet("http://o2o.yzfcw.com/ClientO2O_Station.apk", getPath("client.apk"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			new Thread(runnable).start();
		}
	}
}
