/**
 * 
 */
package com.xjf.filedialog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.xjf.filedialog.PackageAdpater.SimpleInfo;


/**
 * APK管理activity,列出启动,查看,备份,卸载apk软件,
 * */
public class XPackageManager extends Activity {
	private static final String tag = "FileDialog";
	private List<PackageInfo> apkInfos;
	private GridView apkView;
	private PackageAdpater adapter;
	private ArrayList<SimpleInfo> infos;
	private PackageManager pm;
	private String[] apkmenu;
	private int showWhat = ONLY_USER;
	
	
	@Override
	public void onCreate(Bundle saveBundle){
		super.onCreate(saveBundle);
		
		Intent intent = getIntent();
		if (intent != null){
			dstPath = intent.getStringExtra(FileManager.PRE_BACKUP_DIR);
			if (dstPath == null)
				dstPath = FileManager.BACKUPUP_DIR;
		} else {
			dstPath = FileManager.BACKUPUP_DIR;
		}
		
		LayoutInflater inflater = LayoutInflater.from(this);
		
		//apkView = (GridView)findViewById(R.layout.apkview);
		apkView = (GridView) inflater.inflate(R.layout.apkview, null);
		setContentView(apkView);
		apkmenu = getResources().getStringArray(R.array.apkmenu);
		dialog = new ApkDialog(XPackageManager.this);
		infos = new ArrayList<PackageAdpater.SimpleInfo>();
		adapter = new PackageAdpater(XPackageManager.this, infos);
		apkView.setAdapter(adapter);
		pm = getPackageManager();
		apkMenu();
		new Timer().schedule(new APKTimerTask(), 1);
		initDialog = ProgressDialog.show(this, "", "装载中...");
		initDialog.setCancelable(false);
	}
	ProgressDialog initDialog;
	Handler h = new Handler(){
		@Override
		public void handleMessage(Message msg){
			adapter.notifyDataSetChanged();
			initDialog.dismiss();
		}
		
	};
	
	class APKTimerTask extends TimerTask {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			apkInfos = pm.getInstalledPackages(0);
			
			getInfos(apkInfos, infos);
			
			
			h.sendEmptyMessage(0);
		}
	};

    public static final String APP_PKG_PREFIX = "com.android.settings.";
    public static final String APP_PKG_NAME = APP_PKG_PREFIX+"ApplicationPkgName";
	
    ApkDialog dialog;
    
	private void apkMenu(){
		apkView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				/**
				Uri uri = Uri.fromParts("package",
						apkInfos.get(position).packageName, null);
				Intent it = new Intent(Intent.ACTION_VIEW, uri);
				/**/
				/**
                Intent detailsIntent = new Intent();
                detailsIntent.setClassName("com.android.settings", 
                		"com.android.settings.InstalledAppDetails");
                detailsIntent.putExtra(
                		APP_PKG_NAME, 
                		apkInfos.get(position).applicationInfo.packageName);
                detailsIntent.putExtra(
                		"pkg", 
                		apkInfos.get(position).applicationInfo.packageName);
				startActivity(detailsIntent);
				/**/
				pos = position;
				dialog.setTitle(infos.get(position).name());
				dialog.show();
			}
		});
	}
	
	private int pos = 0;
	
	private void  getInfos(List<PackageInfo> apkInfos,
			ArrayList<SimpleInfo> infos){
		if (apkInfos == null)
			return;
		if (infos == null)
			infos = new ArrayList<SimpleInfo>();
		String name;
		Drawable icon;
		PackageInfo apk;
		boolean isSys = false;
		for (int i = 0; i < apkInfos.size(); ){
			apk = apkInfos.get(i);
			isSys = apk.applicationInfo.sourceDir.startsWith("/system");
			
			switch (showWhat) {
			case SYS_AND_USER:
				break;
			case ONLY_SYS:
				if (!isSys) {
					apkInfos.remove(apk);
					continue;
				}
				break;
			case ONLY_USER:
				if (isSys) {
					apkInfos.remove(apk);
					continue;
				}
				break;
				default: break;
			}
			i++;
			name = (String) pm.getApplicationLabel(apk.applicationInfo);
			icon = pm.getApplicationIcon(apk.applicationInfo);
			infos.add(new SimpleInfo(icon, name));
		}
	}
	/*
	private final static int OPEN_APK = 0;
	private final static int UNINSTALL_APK = 1;
	private final static int BACKUP_APK = 2;
	private final static int VIEW_APK = 3;
	/**/
	private final static String apkPath = "/data/app/";
	private String dstPath = "/sdcard/panda/";
	
	public String backupPath() {return dstPath;}
	public void setBackupPath(String path) {
		File directory = new File(path);
		if (!directory.isDirectory() || !directory.exists()){
			Toast.makeText(this, "路径错误或不存在", Toast.LENGTH_SHORT)
				.show();
		} else {
			dstPath  = path;
		}
	}
	
	private final static int SYS_AND_USER = 0;
	private final static int ONLY_SYS = 1;
	private final static int ONLY_USER = 2;
	private final static int REFRESH = 3;

	
	/**
	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		if (getSysApp){
			sys_or_not = "用户程序";
		} else{
			sys_or_not = "系统程序";
		}
		
		return super.onPrepareOptionsMenu(menu);
	}
	/**/
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	/**/
		menu.add(0, 0, SYS_AND_USER, "系统和用户软件");
		menu.add(0, 0, ONLY_SYS, "只系统软件");
		menu.add(0, 0, ONLY_USER, "只用户软件");
		menu.add(0, 0, REFRESH, "刷新");
		return super.onCreateOptionsMenu(menu);
	}
    

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	int what = item.getOrder();
    	switch (what) {
    	case SYS_AND_USER:
    	case ONLY_SYS:
    	case ONLY_USER:
    		showWhat = what;
    	case REFRESH:

    		infos.clear();
    		initDialog.show();
    		new Timer().schedule(new APKTimerTask(), 1);
    		break;
    	}
    	return true;
    }

    LinearLayout layout;
	private class ApkDialog extends Dialog implements DialogInterface{

		public ApkDialog(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			init();
		}
		
		private void init(){
			
			WindowManager m = getWindowManager();
			//Window w = this.getWindow();
			//Display d = m.getDefaultDisplay();	//为获取屏幕宽、高
			
			//w.requestFeature(Window.FEATURE_NO_TITLE);


			this.setCancelable(true);

			
			layout = new LinearLayout(XPackageManager.this);
			layout.setLayoutParams(new LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT));
			Button t1 = new Button(XPackageManager.this);
			Button t2 = new Button(XPackageManager.this);
			Button t3 = new Button(XPackageManager.this);
			Button t4 = new Button(XPackageManager.this);
			t1.setText(apkmenu[0]);
			t2.setText(apkmenu[1]);
			t3.setText(apkmenu[2]);
			t4.setText(apkmenu[3]);
			t1.setTextColor(Color.BLACK);
			t2.setTextColor(Color.BLACK);
			t3.setTextColor(Color.BLACK);
			t4.setTextColor(Color.BLACK);
			t1.setTextSize(30);
			t2.setTextSize(30);
			t3.setTextSize(30);
			t4.setTextSize(30);
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.addView(t1, new LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT));
			layout.addView(t2, new LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT));
			layout.addView(t3, new LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT));
			layout.addView(t4, new LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT));
			setContentView(layout);
			
			t1.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String name = apkInfos.get(pos).packageName;
					Intent intent = pm.getLaunchIntentForPackage(name);
					if (intent == null){
						Log.d(tag, "can not get intent: " +
								name);
						return;
					}
					ApkDialog.this.dismiss();
					startActivity(intent);
				}
			});
			
			t2.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Uri uri = Uri.fromParts("package", 
							apkInfos.get(pos).packageName, 
							null);
					Intent intent = new Intent(Intent.ACTION_DELETE, uri);
					ApkDialog.this.dismiss();
					startActivity(intent);
				}
				
			});
			
			t3.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String backApk = apkInfos.get(pos).applicationInfo.sourceDir;
					try {
						new File(dstPath).mkdirs();
						if (!dstPath.endsWith("/"))
							dstPath = dstPath + "/";
						copyFile(new File(backApk), 
								new File(dstPath +
										apkInfos.get(pos).packageName
										+ ".apk"));
						Toast.makeText(XPackageManager.this, "备份完成", Toast.LENGTH_SHORT)
							.show();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						Log.e(tag, "backup apk error");
						e.printStackTrace();
					}
					ApkDialog.this.dismiss();
				}
			});
			
			
			t4.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent detailsIntent = new Intent();
					PackageInfo pkg = apkInfos.get(pos);
	                detailsIntent.setClassName("com.android.settings", 
	                		"com.android.settings.InstalledAppDetails");
	                detailsIntent.putExtra(
	                		APP_PKG_NAME, 
	                		pkg.applicationInfo.packageName);
	                detailsIntent.putExtra(
	                		"pkg", 
	                		pkg.applicationInfo.packageName);
	                ApkDialog.this.dismiss();
					startActivity(detailsIntent);
				}
			});
			
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event){
			
			int	height = layout.getHeight();
			int width = layout.getWidth();
			int ty = (int) event.getY();
			int tx = (int) event.getX();
			if (ty < 0 || ty > height ||
					tx < 0 || tx > width){
				this.dismiss();
				return true;
			}
			return super.onTouchEvent(event);
		}
		
		
	}
	
	private static final int BUFF_SIZE = 8192;
	private int copyFile(File src, File dst) throws InterruptedException {
		int ret = 0;
		FileInputStream fIn = null;
		FileOutputStream fOut = null;
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		if (src == null){
			Log.d(tag, "null");
			return -1;
		}
		try {
			Log.d(tag, "start-- " + src.getAbsolutePath());
			fIn = new FileInputStream(src);
			fOut = new FileOutputStream(dst);
			in = new BufferedInputStream(fIn, BUFF_SIZE);
			out = new BufferedOutputStream(fOut, BUFF_SIZE);
			byte[] bytes = new byte[BUFF_SIZE];
			int length;
			while ((length = in.read(bytes)) != -1) {
				out.write(bytes, 0, length);
			}
			out.flush();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (fIn != null)
					fIn.close();
				if (fOut != null)
					fOut.close();
				return ret;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ret;
	}
    
}
