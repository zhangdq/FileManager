package com.xjf.filedialog;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.xjf.utils.Common;
import com.android.xjf.utils.XDialog;
import com.android.xjf.utils.ZipUtils;
import com.xjf.filedialog.CopyFileService.CopyFileBinder;
import com.xjf.filedialog.FileAdapter.FileInfo;

/**
 * 文件选项处理, 当点击文件长按菜单时, 根据选项做相应的处理
 * 
 * */
public class FileItemClickListener {
	
	

	private String tag = "FileDialog";
	
	/** file absolute path */
	public String absPath;			
	/** file name, last part */
	public String name;		
	/** 点击paste时的路径 */
	public String pastePath;
	/** 选择的View*/
	private View selectView = null;
	public int position = 0;
	public AlertDialog deleteDialog;
	public AlertDialog renameDialog;
	public AlertDialog.Builder copyWarningDialog;
	public AlertDialog renameWarningDialog;
	public AlertDialog createFileDialog;
	public AlertDialog openMannerDialog;
	public ProgressDialog copyProgressDialog;
	public String renamePath = null;
	public EditText inputEdit;
	public EditText createFileEdit;
	public boolean isCut = false;
	public boolean allDoLikeThis = false;
	public boolean createDirectory = false;
	public int copyWarningSelection;
	public BufferedReader br;
	public String bTmp;
	
	FileManager fileManager;
	private boolean isCopying = false;		//正在复制文件
	
	private CopyFileService copyFileService;

	ArrayList<String> multPaste; 
	java.lang.Process deleProgress = null;
	ProgressDialog doDeleDialog;
	private boolean doSelected = false;;
	
	private ArrayAdapter<String> adapter;
	private Spinner spuser, spgroup, spother;
	private CheckBox allDo;
	
	public final static int COPY_APPEND = 0;
	public final static int COPY_REPLACED = 1;
	public final static int COPY_CANCEL = 2;
	public final static int COPY_SKIP = 3;
	
	public final static int OPEN_TEXT = 0;
	public final static int OPEN_IMAGE = 1;
	public final static int OPEN_AUDIO = 2;
	public final static int OPEN_VIDEO = 3;
	
	private final static int PER_NULL = 0;
	private final static int PER_ONLY_READ = 1;
	private final static int PER_ONLY_EXEC = 2;
	private final static int PER_READ_WRITE = 3;
	private final static int PER_READ_EXEC = 4;
	private final static int PER_WRITE_EXEC = 5;
	

	
	
	
	/**
    class CopyWarnThread implements Runnable{
    	
    	private String name;
    	
    	public CopyWarnThread(String n) {
			// TODO Auto-generated constructor stub
    		name = n;
		}
    	@Override
    	public void run(){
    		synchronized (this){
    			copyWarningDialog.setTitle(name + " 已存在!");
    			copyWarningDialog.setMultiChoiceItems(new CharSequence[]{
    					"文件名后面加(2)", "替换当前目录文件", "取消操作",
    					"跳过", "以同样方式处理后面的文件"}, 
    					new boolean[]{false, false, false, false, false},
    					new AlertDialog.OnMultiChoiceClickListener(){
    						@Override
    						public void onClick(DialogInterface dialog, int which,
    								boolean isChecked) {
    							// TODO Auto-generated method stub
    							if (which != 4) {
    								copyWarningSelection = which;
    								synchronized (fileManager){
    									fileManager.notify();
    								}
    								dialog.dismiss();
    							}
    							if (which == 4)
    								allDoLikeThis = isChecked;
    						}
    			}).create().show();
    		}
    	}
    };
    /**/
    //Thread copyWarnThread = null;
    

	public final static int HANDLER_SHOW_COPY_PROGRESS_DIALOG = 20;
	public final static int HANDLER_INCREMENT_COPY_PROGRESS = 21;
	public final static int HANDLER_REFRESH_LIST = 22;
	public final static int HANDLER_SHOW_COPY_WARN = 23;
	public final static int HANDLER_COPY_FINISHED = 24;
	public final static int HANDLER_PROCESS_SET_MAX = 25;
	public final static int HANDLER_PROCESS_SET_VALUE = 26;
	public final static int HANDLER_PROCESS_SET_MESSAGE = 27;
	
	public final static int HANDLER_SHOW_DELETE_ERROR = 28;
	public final static int HANDLER_COPY_FAILURE = 29;
	public final static int HANDLER_SHOW_CUT_DIALOG = 30;
	public final static int HANDLER_COPY_CANCEL = 31;
	public final static int HANDLER_FILE_SIZE = 32;
	public final static int HANDLER_CUT_FINISH = 33;

	public final static String BUNDLE_MULT_FILE_PATH = "mf";
	public final static String BUNDLE_IS_FILE = "sf";
	public final static String BUNDLE_FROM_PATH = "fp";
	public final static String BUNDLE_TO_PATH = "tp";
	
	private ProgressDialog cutProgressDialog = null; 
	
	public boolean copying() {return isCopying;}
	public boolean isCut() {return isCut;}
	public FileItemClickListener(FileManager fileManager){
		super();
		this.fileManager= fileManager; 
		init();
		initSpinner();
		zip = new ZipUtils();
	}
	
	public void onClick(View v, int which) {
		// TODO Auto-generated method stub
		selectView = v;
		switch (which){
		case FileManager.MENU_ITEM_COPY:
			if (!fileManager.multFile) {
				isCut = false;
				doCopy();
				break;
			}
		case FileManager.MENU_ITEM_COPY_SELECTED:
			isCut = false;
			doMultCopy();
			break;
			
		case FileManager.MENU_ITEM_CUT:
			if (!fileManager.multFile) {
				isCut = true;
				doCopy();
				break;
			}
		case FileManager.MENU_ITEM_CUT_SELECTED:
			isCut = true;
			doMultCopy();
			break;
			
		case FileManager.MENU_ITEM_PASTE:
			if (isCopying) {
				Toast.makeText(fileManager, 
						fileManager.getString(R.string.copying), 
						Toast.LENGTH_LONG).show();
				return;
			}
			isCopying = true;
			doPaste();
			break;
		case FileManager.MENU_ITEM_DELETE:
			if (fileManager.selectedItem().isEmpty()) {
				//Log.d(tag, "delete selected isEmpty")
				doSelected = false;
				deleteDialog.setMessage(
						fileManager.getString(R.string.sure_delete_files) 
						+ name + "?");
				deleteDialog.show();
				break;
			}
			doSelected = true;
			deleteDialog.setMessage("确定删除  " + fileManager.selectedItem().size() + " 个文件?");
			deleteDialog.show();
			break;
		case FileManager.MENU_ITEM_CREATE_DIRECTORY:
			createFileDialog.setTitle(fileManager.getString(R.string.new_directory));
			doCreateFile(true);
			break;
		case FileManager.MENU_ITEM_CREATE_FILE:
			createFileDialog.setTitle(fileManager.getString(R.string.new_file));
			doCreateFile(false);
			break;
		case FileManager.MENU_ITEM_RENAME:
			inputEdit.setText(name);
			renameDialog.setMessage(fileManager.getString(R.string.rename) + name);
			renameDialog.show();
			break;
		case FileManager.MENU_ITEM_PROPERTIES:
			doGetProperties(absPath);
			break;
		case FileManager.MENU_ITEM_OPEN:
			/**
			Log.d(tag, "zip: "  + absPath);
			ZipUtils.zip(absPath, absPath + ".zip");
			/**/
			if (fileManager.currentFileInfo().get(position).directory){
				fileManager.refreshPath(absPath, 1);
				return;
			}
			doOpenFile(absPath);
			/**/
			break;
		case FileManager.MENU_ITEM_OPEN_IN_OTHER:
			/**
			ZipUtils.unZip(absPath, absPath + "2");
			/**/
			if (fileManager.currentFileInfo().get(position).directory){
				fileManager.refreshPath(absPath, 1);
				return;
			}
			doOpenInOtherManner();
			/**/
			break;
		case FileManager.MENU_ITEM_SELECT_ALL:
			doSelectAll();
			break;
			
			/** zip 解压 **/
		case FileManager.MENU_ITEM_ZIP:
			doCompressZip();
			break;
		case FileManager.MENU_ITEM_UNZIP:
			doDecompressZip();
			break;
			
		default:
				break;
		}
		if (selectView != null) {
			selectView.setSelected(true);
			selectView = null;
		}
	}

	
	private Handler xHandler = new Handler() {
		@Override
		public void handleMessage(Message msg){
			switch (msg.what) {
			// 显示复制进度对话框, arg1要最大值
			case HANDLER_SHOW_COPY_PROGRESS_DIALOG:
				if (!fileManager.isRoot()) {
					initCopyProgressDialog();
					copyProgressDialog.setMax(msg.arg1);
					copyProgressDialog.setMessage("---");
					copyProgressDialog.show();
				} else {
					initRootCopyDialog();
				}
				break;
			case HANDLER_SHOW_CUT_DIALOG:
				initCutProgressDialog();
				cutProgressDialog.show();
				break;
			// 增加复制进度对话框的植, msg.arg1
			case HANDLER_INCREMENT_COPY_PROGRESS:
				if (copyProgressDialog != null)
					copyProgressDialog.incrementProgressBy(msg.arg1);
				break;
				
			case HANDLER_PROCESS_SET_VALUE:
				copyProgressDialog.setProgress(msg.arg1);
				break;
				
			case HANDLER_PROCESS_SET_MESSAGE:
				Bundle b = msg.getData();
				if (b == null || copyProgressDialog == null)
					return;
				copyProgressDialog.setMessage("从\t" + 
						b.getString(FileItemClickListener.BUNDLE_FROM_PATH) +
						"\t到\t" + 
						b.getString(FileItemClickListener.BUNDLE_TO_PATH) + 
						"\n大小: " + copyProgressDialog.getMax() + " KB");
				break;
				
			case HANDLER_REFRESH_LIST:
				Message msg2 = fileManager.listViewHandler.obtainMessage();
				msg2.what = FileManager.HANDLER_SET_LISTVIEW_SELECTED;
				msg2.arg1 = position - 1;
				msg2.arg2= FileManager.HANDLER_REFRESH_LISTVIEW;
				fileManager.listViewHandler.sendMessage(msg2);
				break;
			case HANDLER_PROCESS_SET_MAX:
				copyProgressDialog.setMax(msg.arg1);
				break;
			case HANDLER_SHOW_COPY_WARN:
				String name = "";
				if (msg.getData() != null)
					name = msg.getData().getString(BUNDLE_MULT_FILE_PATH);
				showCopyWarnDialog(name);
				break;

			case HANDLER_CUT_FINISH:
				dealCopyBack(HANDLER_CUT_FINISH);
				break;
			case HANDLER_COPY_FINISHED:
				dealCopyBack(HANDLER_COPY_FINISHED);
				break;
			case HANDLER_COPY_FAILURE:
				dealCopyBack(HANDLER_COPY_FAILURE);
				isCopying = false;
				break;
			case HANDLER_COPY_CANCEL:
				dealCopyBack(HANDLER_COPY_CANCEL);
				break;
			
			case HANDLER_SHOW_DELETE_ERROR:
				Toast.makeText(fileManager, 
						fileManager.getString(R.string.dele_file_error), 
						Toast.LENGTH_SHORT).show();
				break;
			case HANDLER_FILE_SIZE:
				if (perText != null && perString != null) {
					long size = msg.arg1 + (msg.arg2 << 32);
					perText.setText(perString + "大小:\t\t\t" + 
							Common.formatString(String.valueOf(size)) + " bytes\n");
				}
				break;
			}
		}
	};
	
	
	private void dealCopyBack(int which){
		if (copyProgressDialog != null) {
			copyProgressDialog.dismiss();
			copyProgressDialog = null;
		} else if ( cutProgressDialog != null){
			cutProgressDialog.dismiss();
			cutProgressDialog = null;
		} else if (rootCopyDialog != null) {
			rootCopyDialog.dismiss();
			rootCopyDialog = null;
		}
		
		String result = "";
		switch (which){
		case HANDLER_COPY_CANCEL:
			result = fileManager.getString(R.string.cancel_copy);
			break;
		case HANDLER_COPY_FAILURE:
			result = fileManager.getString(R.string.fail_to_copy);
			break;
		case HANDLER_COPY_FINISHED:
		case HANDLER_CUT_FINISH:
			result = fileManager.getString(R.string.done_copy);
			break;
		}
		Toast.makeText(fileManager, result, Toast.LENGTH_SHORT)
				.show();
		if (pastePath.equals(fileManager.currentPath())) {
			fileManager.listViewHandler.sendEmptyMessage(
					FileManager.HANDLER_REFRESH_LISTVIEW);
		} 
		if (isCut)
			clearPaste();
		isCopying = false;
	}
	
	private void initCutProgressDialog(){
		cutProgressDialog = new ProgressDialog(fileManager);
		cutProgressDialog.setIndeterminate(true);
		cutProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		cutProgressDialog.setTitle(fileManager.getString(R.string.moving_files));
		cutProgressDialog.setCancelable(false);
		cutProgressDialog.setButton(
				ProgressDialog.BUTTON_NEGATIVE, 
				fileManager.cancel, 
				new ProgressDialog.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						copyFileService.cancelCopy();
						isCopying = false;
					}
				});
		
	}
	
	DialogInterface.OnClickListener listener = 
		new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				switch (which) {
				case ProgressDialog.BUTTON_POSITIVE:
					break;
				case ProgressDialog.BUTTON_NEUTRAL:
					isHidden = true;
					((Dialog) dialog).hide();
					copyFileService.setHidden(true);
					break;
				case ProgressDialog.BUTTON_NEGATIVE:
					copyFileService.cancelCopy();
					Toast.makeText(fileManager, "正在取消复制", Toast.LENGTH_SHORT).show();
					dialog.dismiss();
					isCopying = false;
					//Hide
					return;
				}
			}

	};
	private void initCopyProgressDialog() {
		copyProgressDialog = new ProgressDialog(
				fileManager); //, R.style.dialog); //, ProgressDialog.STYLE_HORIZONTAL);
		copyProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		copyProgressDialog.setCancelable(false);
		//copyProgressDialog.setOwnerActivity(fileDialog);
		copyProgressDialog.setTitle(fileManager
								.getString(R.string.copyFile));

		copyProgressDialog.setButton(ProgressDialog.BUTTON_NEUTRAL,
				fileManager.getString(R.string.hide), listener);
		copyProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
				fileManager.cancel, listener);
		
	}
	
	
	public class CopyWarnData {
		public String file = null;
		public int selection = -1;
		public boolean allDoSame = false;
	}
	
	
	public interface MultFileWarn {
		public void clickCallback(int which, boolean ckecked);
	}
	private void showCopyWarnDialog(String name){
		synchronized (this){
			String[] items = fileManager.getResources().getStringArray(R.array.copyWarning);
			copyWarningDialog.setTitle(name + fileManager.getString(R.string.existed));
			copyWarningDialog.setMultiChoiceItems(items, 
					new boolean[]{false, false, false, false, false},
					new AlertDialog.OnMultiChoiceClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which,
								boolean isChecked) {
							// TODO Auto-generated method stub
							if (which != 4) {
								copyFileService.selection = which;
								synchronized (fileManager){
									fileManager.notify();
								}
								dialog.dismiss();
							}
							if (which == 4)
								copyFileService.allDoSame = isChecked;
						}
			}).create().show();
		}
	}
	/**
	private void showMultFileWarn(String name, final MultFileWarn warn){
		synchronized (this){
			String[] items = fileManager.getResources().getStringArray(R.array.copyWarning);
			copyWarningDialog.setTitle(name + fileManager.getString(R.string.existed));
			copyWarningDialog.setMultiChoiceItems(items, 
					new boolean[]{false, false, false, false, false},
					new AlertDialog.OnMultiChoiceClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which,
								boolean isChecked) {
							// TODO Auto-generated method stub
							if (which != 4) {
								copyFileService.selection = which;
								synchronized (fileManager){
									fileManager.notify();
								}
								dialog.dismiss();
								if (warn != null)
									warn.clickCallback(which, isChecked);
							}
							if (which == 4)
								copyFileService.allDoSame = isChecked;
						}
			}).create().show();
		}
	}
	/**/
	private ServiceConnection xSConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			//Log.d(tag, "Service Connected");
			copyFileService = ((CopyFileBinder)service).getService();
			copyFileService.setHandler(xHandler);
			copyFileService.setContext(fileManager);
			copyFileService.dialog = copyProgressDialog;
		}
	};
	
	
	private void init(){
		absPath = new String();
		name = new String();
		bTmp = new String();
		renamePath = new String();
		multPaste = new ArrayList<String>();
		AlertDialog.Builder b = new AlertDialog.Builder(fileManager);
		doBindCopyService();
		
		initDeleDialog();
		inputEdit = new EditText(fileManager);
		b = XDialog.createInputDialog(fileManager, null, inputEdit);
		renameDialog = b.setTitle(fileManager.getString(R.string.rename))
			.setPositiveButton(fileManager.ok, new AlertDialog.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					String newName = inputEdit.getText().toString();
					renamePath =  fileManager.currentPath() + "/" + newName;
					if (renamePath.equals(absPath))
						return;
					File newFile = new File(renamePath);
					if (newFile.exists()) {
						renameWarningDialog.show();
						renameWarningDialog.setMessage(newName + 
								fileManager.getString(R.string.existed));
					}
					else
						doRename();
				}
				
			}).setNegativeButton(fileManager.cancel, 
					new AlertDialog.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
				
			}).setCancelable(false).create();
		
		initCopyWarningDialog();
		initRenameWarningDialog();
		initCreateFileDialog();
		initOpenMannerDialog();
		//initCopyProgressDialog();
	} /* init()*/
	
	
	public void setFile(String file) { this.absPath = file;}
	public void setPosition(int pos) { this.position = pos;}
	public void setName(String n) {this.name = n;}
	public String getFile() {return absPath;}
	
	
	private void initSpinner(){
		AlertDialog.Builder b = new AlertDialog.Builder(fileManager);
		LayoutInflater inflater = (LayoutInflater) fileManager
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dView = (ScrollView) inflater.inflate(R.layout.filepermission,
				null);

		adapter = new ArrayAdapter<String>(fileManager,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spuser = (Spinner) dView.findViewById(R.id.perusersp);
		spgroup = (Spinner) dView.findViewById(R.id.pergoupsp);
		spother = (Spinner) dView.findViewById(R.id.perotherssp);
		
		allDo = (CheckBox) dView.findViewById(R.id.perdirectory);

		adapter.add(fileManager.getString(R.string.per_null));
		adapter.add(fileManager.getString(R.string.per_only_read));
		adapter.add(fileManager.getString(R.string.per_only_exec));
		adapter.add(fileManager.getString(R.string.per_read_write));
		adapter.add(fileManager.getString(R.string.per_read_exec));
		adapter.add(fileManager.getString(R.string.per_write_exec));
		
		
		spother.setAdapter(adapter);
		spuser.setAdapter(adapter);
		spgroup.setAdapter(adapter);
		
		b.setTitle(fileManager.getString(R.string.permission)).setView(dView);		//
		AlertDialog.OnClickListener lt = new AlertDialog.OnClickListener() {
			/** 对话框点击 */
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				perDismiss = true;
				if (which == AlertDialog.BUTTON_NEGATIVE)
					return;
				java.lang.Process p = null;
				int user = idToPer(spuser.getSelectedItemPosition());
				int group = idToPer(spgroup.getSelectedItemPosition());
				int other = idToPer(spother.getSelectedItemPosition());
				int per = (user << 8) + (group << 4) + (other);
				if ((permission == per) && !allDo.isChecked())
					return;
				String all = "";
				if (allDo.isChecked())
					all = " -R ";
				try {
					
					if (fileManager.isRoot()){
						p = fileManager.linux.shell.exec("su");
						DataOutputStream shell = new DataOutputStream(p.getOutputStream());
						String cmd = (String.format("chmod %s %x ", all, per)
								+ absPath +"\n");
						//Log.d(tag, cmd);
						shell.write(cmd.getBytes());
						shell.writeBytes("exit\n");
						shell.flush();
						shell.close();
					} else {
						p = fileManager.linux.shell.exec(
								String.format("chmod %s %x ", all, per) + absPath);
					}
					int w = p.waitFor();
					BufferedReader err = new BufferedReader(new InputStreamReader
							(p.getErrorStream()));
					if (w != 0){
						if (err.ready())
							Toast.makeText(fileManager, "修改失败\n" + err.readLine(), 
								Toast.LENGTH_LONG).show();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally{
					p.destroy();
				}
			}
		};
		b.setPositiveButton(fileManager.ok, lt)
			.setNegativeButton(fileManager.cancel, lt)
			.setCancelable(false);
		
		//dView.setBackgroundColor(0xff77d7ff);
		//b.setIcon(R.drawable.t11);
		propertyDialog = b.create();
	}
	private ScrollView dView;

	

	private void spinnerSelected(int per){
		int p = (per & 0xf00) >> 8;
		spuser.setSelection(perToId(p));
		p = (per & 0xf0) >> 4;
		spgroup.setSelection(perToId(p));
		p = (per & 0xf);
		spother.setSelection(perToId(p));
	}
	
	AlertDialog propertyDialog;
	int permission;
	TextView perText = null;
	String perString = null;
	boolean perDismiss = true;
	/** 
	 * 获取文件的基本属性,并显示
	 * null 	0	0
	 * read		4	1
 	 * write 	6	2
	 * readE	5	3
	 * writeE	7	4
	 * */
	private void doGetProperties(String f){
		/**/
		java.lang.Process p = null;
		BufferedReader br = null ,ber = null;
		String[] cmds = null;
		try {
			File fl = new File(f);
			boolean directory = fl.isDirectory();
			
			if (fileManager.isRoot() == false) {
				if (directory) {
					cmds = new String[]{"ls", "-l", "-a", f + "/.."};
					
				} else {
					cmds = new String[]{"ls", "-l", "-a", f };
				}
				p = fileManager.linux.shell.exec(cmds);
				br = new BufferedReader(
						new InputStreamReader(p.getErrorStream()));
				ber = new BufferedReader(
						new InputStreamReader(p.getInputStream()));
			} else {
				// root
				
				p = fileManager.linux.shell.exec("su");
				DataOutputStream shell = new DataOutputStream(p.getOutputStream());
				String cmd;
				if (directory)
					cmd = "ls -l -a \"" + f + "/..\"\nexit\n";
				else
					cmd = "ls -l -a \"" + f + "\"\nexit\n";
				shell.write(cmd.getBytes());
				shell.flush();
				shell.close();
				br = new BufferedReader(
						new InputStreamReader(p.getErrorStream()));
				ber = new BufferedReader(
						new InputStreamReader(p.getInputStream()));
			}
			int w = p.waitFor();
			//String con = ber.readLine();
			if (w != 0){
				Toast.makeText(fileManager,  br.readLine(), 
						Toast.LENGTH_LONG).show();
				br.close();
				ber.close();
				return;
			}
			
			//从shell中获取内容
			String con;
			while ((con = ber.readLine()) != null){
				if (con.endsWith(fileManager.currentFileInfo().get(position).name())){
					break;
				}
			}
			
			if (con == null){
				propertyDialog.show();
				return;
			}
			String[] ss = con.split(" +");
			String fName = "文件:\n\t\t" + f;
			String fStyle = "文件类型:\t\t" + fileStyle(ss[0].charAt(0));
			String fSize;
			if (fl.isDirectory()) {
				/**
				fSize = "大小:\t\t\t" + Common.formatString(String.valueOf(
						FileOperation.getDirectorySize(fl)));
						/**/
				fSize = "大小:\t\t\t 计算中...";
				perDismiss = false;
				new Thread(new FileSizeThread(fl, getSizeListener)).start();
			} else {
				fSize = "大小:\t\t\t" + Common.formatString(String.valueOf(fl.length()));
			}
			String fUser = "用户:\t\t\t" + ss[1];
			String fGroup = "用户群:\t\t\t" + ss[2];
			String createDate = "修改日期:\t\t"
					+ (new Date(fl.lastModified()).toLocaleString());			
			permission = filePermissions(ss[0]);
			spinnerSelected(permission);
			
			perText = (TextView)dView.findViewById(R.id.permessage);
			perString = fName + "\n" + fStyle + "\n" + fUser + "\n"
							+ fGroup + "\n" + createDate + "\n" ;
			perText.setText(perString + fSize);
			propertyDialog.show();
			
		} catch (IOException e){
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			p.destroy();
			try {
				if (br != null)
					br.close();
				if (ber != null)
					ber.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public class FileSizeThread implements Runnable {
		private File parent;
		private onComputeEndListener listener;
		public boolean forceTerminal = false;
		public FileSizeThread(File f, onComputeEndListener l) {
			this.parent = f;
			this.listener = l;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				long size = getDirectorySize(parent, this);
				if (!forceTerminal && listener != null) {
					listener.onComputeEnd(size);
					/**
					/**/
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	public interface onComputeEndListener {
		public void onComputeEnd(long size);
	}
	private onComputeEndListener getSizeListener = new onComputeEndListener() {
		
		@Override
		public void onComputeEnd(long size) {
			// TODO Auto-generated method stub
			Message msg = xHandler.obtainMessage(HANDLER_FILE_SIZE, 
					(int)(size & 0xffffffff), (int)(size >> 32));
			xHandler.sendMessage(msg);
		}
	}; 
	private long getDirectorySize(File f, FileSizeThread ft) throws IOException{
		long size = 0;
		File flist[] = f.listFiles();
		if (flist == null)
			return f.length();
		int length = flist.length;
		for (int i = 0; i < length; i++) {
			if (ft.forceTerminal)
				return 0;
			if (flist[i].isDirectory()) {
				size = size + getDirectorySize(flist[i], ft);
			} else {
				size = size + flist[i].length();
			}
		}
		return size;
	}
	/** 根据　ls　命令的输出判断文件类型　*/
	private String fileStyle(char c){
		switch(c){
		case 'd':
			return fileManager.getString(R.string.folder);
		case '-':
			return fileManager.getString(R.string.normal_file);
		case 'l':
			return fileManager.getString(R.string.link_file);
		case 'b':
			return fileManager.getString(R.string.dev_file);
		case 'c':
			return fileManager.getString(R.string.chars_file);
		}
		return fileManager.getString(R.string.normal_file);
	}
	
	/**
	 * linux ls 输出第一部分: -rwxrwxrwx,  user, group, other
	 * */
	public static int filePermissions(String s){
		int p = 0;
		char c = '-';
		for (int i = 0; i < 3; i++){
			int d = 4 * (2 - i);
			for (int j = 0; j < 3; j++){
				c = s.charAt(1 + i * 3 + j);
				if (c != '-'){
					p += (1 << (2 - j + d));
				}
				
			}
		}
		return p;
	}
	
	
	private void doCreateFile(boolean directory){
		createDirectory = directory;
		createFileDialog.show();
	}
	
	
	/**
	 * 
	 * 用root权限拷贝
	 * *

	Process rootProgress = null;
	boolean rootCopying = false;
	public void rootCopy(){
        Runnable r = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
		        DataOutputStream shell;
		        BufferedReader err;
		        
		        if (multPaste.isEmpty()) {
		        	Log.d(tag, "empty");
		        	return;
		        }
		        rootCopying = true;
		        isCopying = true;
		        int size = multPaste.size();
		        try {
					rootProgress = Runtime.getRuntime().exec("su");
					shell = new DataOutputStream(rootProgress.getOutputStream());
					String cmd = " " ; 
					String fPath = "";
				
					err = new BufferedReader(new InputStreamReader
							(rootProgress.getErrorStream()));
					
					File dst = null;
					String dstFile = null;
					String dstName = null;
					String currentPath = fileManager.currentPath();
					for (int i = 0; i < size; i++) {
						fPath = multPaste.get(i);
						dstName = Common.getPathName(fPath);
						dstFile = currentPath + "/" + dstName;
						dst = new File(dstFile);
						if (dst.exists()){
							synchronized (fileManager){
								if (!copyFileService.allDoSame){
									Message msg = xHandler.obtainMessage(
											HANDLER_SHOW_COPY_WARN);
									Bundle bundle = new Bundle();
									bundle.putString(BUNDLE_MULT_FILE_PATH, 
											dstName);
									bundle.putBoolean(BUNDLE_IS_FILE, dst.isFile());
									msg.setData(bundle);
									xHandler.sendMessage(msg);
									fileManager.wait();
								}
								
								switch (copyFileService.selection) {
								case COPY_APPEND:
									dstFile = Common.fileNameAppend(dstFile, "(2)");
									break;
									
								case COPY_REPLACED:
									break;

								case COPY_SKIP:
									continue;
									
								case COPY_CANCEL:
								default:
									rootProgress.destroy();
									return ;
								}
							}
						}
							
						cmd += "cp -r \"" + fPath + 
							"\" \"" + dstFile +
							"\"\n";
					}
					Log.d(tag, cmd);
					cmd += "exit\n";
					shell.write(cmd.getBytes());
					shell.flush();
					if (isCut)
						multPaste.clear();
					//Log.d("FileDialog", "re: " + rootProgress.waitFor());
					if (err.ready()){
						Log.d(tag, "" + err.readLine());
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					isCopying = false;
					rootCopying = false;
				}
			}
		};        
        new Thread(r).start();
	}
	
	
	/**/
	ProgressDialog rootCopyDialog;
	private void initRootCopyDialog(){
		rootCopyDialog = new ProgressDialog(fileManager);
		rootCopyDialog.setTitle(fileManager.getString(R.string.copyFile));
		rootCopyDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		rootCopyDialog.setIndeterminate(true);
		rootCopyDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
				fileManager.cancel,
				new ProgressDialog.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						copyFileService.cancelCopy();
						isCopying = false;
					}
				});
		rootCopyDialog.show();
	}
	/**/
	
	AlertDialog czipDialog = null;
	Thread czipThread = null;
	public boolean zipTerminal = false;
	ZipUtils zip = null;
	
	private class DoCompressZip implements Runnable{
		private String from = null, to = null;
		public DoCompressZip(String from, String to) {
			this.from = from;
			this.to = to;
		}
		public void run() {
			if (!fileManager.isMultFile()) {
				zip.zip(from, to);
			} else {
				int size = fileManager.selectedItem().size();
				ArrayList<FileInfo> fis = fileManager.currentFileInfo();
				ArrayList<String> selString = new ArrayList<String>();
				for (int i = 0; i < size; i++){
					selString.add(fis.get(fileManager.selectedItem().get(i)).path());
				}
				zip.zipMult(fileManager.currentPath(), selString, to);
			}
			if (!zip.isTerminal()) {
				Message msg2 = fileManager.listViewHandler.obtainMessage();
				msg2.what = FileManager.HANDLER_SET_LISTVIEW_SELECTED;
				msg2.arg1 = position;
				msg2.arg2= FileManager.HANDLER_REFRESH_LISTVIEW;
				fileManager.listViewHandler.sendMessage(msg2);
			}
			czipDialog.dismiss();
			czipDialog = null;
		}
	}
	private void doCompressZip() {
		XDialog.InputClick click = new XDialog.InputClick() {
			
			@Override
			public void onClickListener(String str, int which) {
				// TODO Auto-generated method stub
				if (which == AlertDialog.BUTTON_NEGATIVE)
					return;
				String s = Common.getParentPath(str);
				File f = new File(s);
				if (!f.exists()) {
					Toast.makeText((Context)fileManager, s + " 不存在", 
							Toast.LENGTH_SHORT).show();
					return;
				}
				f = new File(str);
				if (f.exists()) {
					Toast.makeText((Context)fileManager, str + " 已存在", 
							Toast.LENGTH_SHORT).show();
					return;
				}
				czipThread = new Thread(new DoCompressZip(absPath, str));
				AlertDialog.Builder b = new AlertDialog.Builder(fileManager);
				b.setView(fileManager.getLayoutInflater()
							.inflate(R.layout.cziplayout, null)).setCancelable(false);
				b.setTitle("压缩").setMessage("压缩 " + absPath + " 到  " + str);
				b.setNegativeButton("取消", new AlertDialog.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						try {
							zip.terminal();
							czipThread.join();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				});
				czipDialog = b.create();
				czipDialog.show();
				czipThread.start();
			}
		};
		
		XDialog.inputDialog((Context)fileManager, 
				"压缩zip", "压缩 " + absPath + " 到",
				 absPath + ".zip", null, 
				fileManager.ok, fileManager.cancel,
				click).show();
		
		
	}
	
	
	private class DecZip implements Runnable {
		private String from = null, to = null;
		public DecZip(String from, String to) {
			this.from = from;
			this.to = to;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			zip.unZip(from, to);
			if (!zip.isTerminal()) {
				Message msg2 = fileManager.listViewHandler.obtainMessage();
				msg2.what = FileManager.HANDLER_SET_LISTVIEW_SELECTED;
				msg2.arg1 = position;
				msg2.arg2= FileManager.HANDLER_REFRESH_LISTVIEW;
				fileManager.listViewHandler.sendMessage(msg2);
			}
			czipDialog.dismiss();
			czipDialog = null;
		}
		
	}
	
	private void doDecompressZip() {
		XDialog.InputClick click = new XDialog.InputClick() {
			
			@Override
			public void onClickListener(String str, int which) {
				// TODO Auto-generated method stub
				if (which == AlertDialog.BUTTON_NEGATIVE) {
					return;
				}
				File toPath = new File(str);
				if (!toPath.exists() && !toPath.mkdirs()) {
					Toast.makeText(fileManager, toPath + " 不存在或创建不了", 
							Toast.LENGTH_SHORT).show();
					return;
				}
				
				czipThread = new Thread(new DecZip(absPath, str));
				AlertDialog.Builder b = new AlertDialog.Builder(fileManager);
				b.setView(fileManager.getLayoutInflater()
							.inflate(R.layout.cziplayout, null)).setCancelable(false);
				b.setTitle("解压 ").setMessage("解压 " + absPath + " 到  " + str);
				b.setNegativeButton("取消", new AlertDialog.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						try {
							zip.terminal();
							czipThread.join();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				});
				czipDialog = b.create();
				czipDialog.show();
				czipThread.start();
			}
		};
		
		int index = absPath.lastIndexOf(".");
		int i = absPath.lastIndexOf("/");
		String name = null;
		if (index > i)
			name = absPath.substring(0, index);
		else
			name = absPath;
		File f = new File(name);
		while (f.exists()) {
			name = name + "2";
			f = new File(name);
		}
		XDialog.inputDialog(fileManager, "解压", 
				"解压 " + absPath + " 到" , 
				name	, null, fileManager.ok, 
				fileManager.cancel, 
				click).show();
	}
	
	private void doRename(){
		BufferedReader br = null;
		java.lang.Process p = null;
		try {
			if (fileManager.isRoot() == false){
				p = fileManager.linux.moveFile(absPath, renamePath);
			} else {
				// root
				p = fileManager.linux.shell.exec("su");
				DataOutputStream shell = new DataOutputStream(p.getOutputStream());
				String cmd = "mv \"" + absPath + "\" \"" + 
								renamePath + "\"\nexit\n";
				shell.write(cmd.getBytes());
				shell.flush();
				shell.close();
			}
			br = new BufferedReader(
					new InputStreamReader(p.getErrorStream()));
			if (p.waitFor() != 0) {
				Toast.makeText(fileManager, br.readLine(), 
						Toast.LENGTH_LONG).show();
				//return;
			}else{
				Message msg = fileManager.listViewHandler.obtainMessage();
				msg.what = FileManager.HANDLER_REFRESH_LISTVIEW;
				/**
				msg.arg1 = FileDialog.HANDLER_SET_LISTVIEW_SELECTED;
				msg.arg2 = position;
				/**/
				fileManager.listViewHandler.sendMessage(msg);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			p.destroy();
			try {
				if (br != null)
					br.close();
			} catch (IOException e){
				e.printStackTrace();
			}
		}
	}
	
	private void clearPaste(){
		multPaste.clear();
	}
	
	class DeleThread implements Runnable {
		boolean select = false;
		public DeleThread(boolean s) {
			// TODO Auto-generated constructor stub
			select = s;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			doDelete(select);
		}
		
	}
	
	
	
	/**
	 * 删除文件
	 * */
	
	private void initDeleDialog(){
		AlertDialog.Builder b = new AlertDialog.Builder(fileManager);
		deleteDialog = b.setTitle(fileManager.getString(R.string.dele_file))
		.setPositiveButton(fileManager.ok, new AlertDialog.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				doDeleDialog = new ProgressDialog(fileManager);
				doDeleDialog.setMessage(fileManager.getString(R.string.dele_file));
				doDeleDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				doDeleDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, 
						fileManager.cancel, 
						new ProgressDialog.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								if (deleProgress != null)
									deleProgress.destroy();
								deleProgress = null;
							}
				});
				new Thread(new DeleThread(doSelected)).start();
				if (doDeleDialog != null)
					doDeleDialog.show();
			}
		}).setNegativeButton(fileManager.cancel, new AlertDialog.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
			
		}).setCancelable(false).create();
	}
	
	
	private void doDelete(boolean select){
		
       
        /**/
		BufferedReader br = null;
		String fl = " ";
		int ret = -1;
		ArrayList<FileInfo> fis = fileManager.currentFileInfo();
		ArrayList<String> selString = new ArrayList<String>();
		ArrayList<Integer> fTmp = fileManager.selectedItem();
		int size = fTmp.size();
		for (int i = 0; i < size; i++){
			selString.add(fis.get(fTmp.get(i)).path());
		}
		try {
			if (fileManager.isRoot()){ 
				if (fileManager.multFile && doSelected) {
					for (int i = 0; i < size; i++){
						fl = fl + "\"" + selString.get(i) + "\" "; 
					}
				} else {
					fl = "\"" + absPath + "\"";
				}
				deleProgress = fileManager.linux.shell.exec("su");
				DataOutputStream shell = new DataOutputStream(deleProgress.getOutputStream());
				String cmd = "rm -r " + fl + "\n" + "exit\n";
				//String cmd = "cp /sdcard/ji /data/xjf\nexit\n";
				shell.write(cmd.getBytes());
				shell.flush();
				shell.close();
				// end root
			} else {
				if (fileManager.multFile && doSelected) {
					selString.add(0, "rm");
					selString.add(1, "-r");
					deleProgress = fileManager.linux.shell.exec(
							selString.toArray(new String[size + 2]));
				} else {
					deleProgress = fileManager.linux.delete(absPath);
				}
			}
			br = new BufferedReader(
					new InputStreamReader(deleProgress.getErrorStream()));
			ret = deleProgress.waitFor();
			if (ret != 0) {
				//Log.d(tag, "Error(code = " + ret + "): " + br.readLine());
				if (ret != 9)
					xHandler.sendEmptyMessage(HANDLER_SHOW_DELETE_ERROR);
				else
					Log.d(tag, "Error(code = " + ret + "): " + br.readLine());
			}else;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
			// 刷新文件列表
			if (ret == 0) {
				Message msg = fileManager.listViewHandler.obtainMessage();
				msg.what = FileManager.HANDLER_REFRESH_LISTVIEW;
				/**
				msg.arg1 = FileDialog.HANDLER_SET_LISTVIEW_SELECTED;
				msg.arg2 = position - 1;
				/**/
				fileManager.listViewHandler.sendMessage(msg);
			}
			
			doDeleDialog.dismiss();
			doDeleDialog = null;
			if (deleProgress != null)
				deleProgress.destroy();
			deleProgress = null;
			try {
				if (br != null)
					br.close();
			} catch (IOException e){
				e.printStackTrace();
			}
		}
		
		/**/
        
	}
	
	
	
	
	public void doCopy(){
		multPaste.clear();
		multPaste.add(absPath);
	}
	
	/***
	 * 选择 但 未复制, FileDialog.fileDialog.selectedItem() 的引用
	 * */
	//ArrayList<Integer> fileDialog.selectedItem();
	/**
	 * 已在粘贴板
	 * */
	public void doMultCopy(){
		multPaste.clear();
		int size = fileManager.selectedItem().size();
		for (int i = 0; i < size; i++){
			multPaste.add(fileManager.currentFileInfo()
					.get(fileManager.selectedItem().get(i)).path());
		}
	}
	/**寻找适合的程序打开文件*/
	public void doOpenFile(String file){
		
		/**
		Log.d(tag, "zip: "  + file);
		ZipUtils.zip(file, file + "2"); 
		/**/
		/***/
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri uri = Uri.parse("file://" + file); 
		String type = null;
		type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
						MimeTypeMap.getFileExtensionFromUrl(file));
		if (type == null) {
			String tmp = file.toLowerCase();
			if (tmp.endsWith("mp3") || tmp.endsWith("wav") || tmp.endsWith("wma"))
				type = "audio/*";
		}
		if(type != null){
			//intent.setType(type);
			//Log.d(tag, type);
			intent.setDataAndType(uri, type);
			try {
				fileManager.startActivityForResult(intent, 1);
			} catch (ActivityNotFoundException e){
				Toast.makeText(fileManager, 
						fileManager.getString(R.string.can_not_open_file),
						Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(fileManager, 
					fileManager.getString(R.string.can_not_find_a_suitable_program_to_open_this_file), 
					Toast.LENGTH_SHORT).show();
		}
		
		/** */
	}
	
	/** 用其它程序打开*/
	void doOpenInOtherManner(){
		openMannerDialog.show();
	}
	
	void doSelectAll() {
		int count = fileManager.currentFileInfo().size();
		ArrayList<Integer> seleted = fileManager.selectedItem();
		seleted.clear();
		for (int i = 0; i < count; i++) {
			seleted.add(i);
		}
		fileManager.selectedAllEle();
		fileManager.fileAdapter.notifyDataSetChanged();
	}
	
	/**
	 * 完成从粘贴板中取所有文件复制
	 * */
	public void doPaste() {
		if (multPaste.isEmpty()) {
			// Toast.makeText(FileDialog.this, "Clipboard is empty",
			// Toast.LENGTH_SHORT).show();
			Message msg = fileManager.listViewHandler.obtainMessage();
			msg.what = FileManager.HANDLER_CLIP_BOARD_EMPTY;
			fileManager.listViewHandler.sendMessage(msg);
			isCopying = false;
			return;
		}
		/**
		int ret = checkMultFile(from, to);
		if (ret == -1 || ret == COPY_CANCEL)
			return;
		/***
		if (fileManager.currentPath().equals("/")){
			File toFile = new File(fileManager.currentPath());
			if (!toFile.canWrite()) {
				Toast.makeText(fileManager, "不支持root复制到根目录('/')", 
						Toast.LENGTH_LONG).show();
				return;
			}
		}
		/***/
		startCopyService(multPaste, fileManager.currentPath(), isCut);
		if (isCut)
			multPaste = new ArrayList<String>();
		/***/
	}
	
	private final void doBindCopyService(){
		//Log.d(tag, "bindService");
		fileManager.bindService(new Intent(fileManager, CopyFileService.class), 
				xSConnection,
				Context.BIND_AUTO_CREATE);
	}
	
	public void stopCopyService(){
		fileManager.stopService(new Intent(fileManager, CopyFileService.class));
		fileManager.unbindService(xSConnection);
	}

	void startCopyService(ArrayList<String> from,
			String to, boolean cut){
		/**
		if (copyFileService.isDestroyed()) {
			doBindCopyService();
		}
		if (copyFileService != null)
			copyFileService.startCopy(from, to);
		else
			Log.d(tag, "==null");
		/**
		
		Process p = null;
		BufferedReader ber = null;
		try {

				p = fileManager.linux.shell.exec("su");
				DataOutputStream shell = new DataOutputStream(p.getOutputStream());
				String cmd = "ls -l -a \"" + to + "/..\"\nexit\n";
				shell.write(cmd.getBytes());
				shell.flush();
				shell.close();
				br = new BufferedReader(
						new InputStreamReader(p.getErrorStream()));
				ber = new BufferedReader(
						new InputStreamReader(p.getInputStream()));
			int w = p.waitFor();
			//String con = ber.readLine();
			if (w != 0){
				Toast.makeText(fileManager,  br.readLine(), 
						Toast.LENGTH_LONG).show();
				br.close();
				ber.close();
				return;
			}
			
			//从shell中获取内容
			String con;
			while ((con = ber.readLine()) != null){
				if (con.endsWith(Common.getPathName2(to))){
					break;
				}
			}
			if (con == null) {
				Toast.makeText(fileManager, "无法获取目录的权限", Toast.LENGTH_LONG).show();
				return;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (p != null)
				p.destroy();
			if (br != null) {
				try {
					br.close();
					if (ber != null)
						ber.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		/**/
		pastePath = fileManager.currentPath();
		allDoLikeThis = false;
		copyWarningSelection = -1;
		copyFileService.isCut = cut;
		copyFileService.root = fileManager.isRoot();
		copyFileService.setFrom(from);
		copyFileService.setToParentPath(to);
		fileManager.startService(new Intent(fileManager, CopyFileService.class));
		/**/
	}
	
	
	/**
	 * 取消复制
	 * */
	public void cancelCopy() {
		if (!isCopying)
			return;
		isCopying = false;
		/**
		if (rootCopying){
			if (rootProgress != null)
				rootProgress.destroy();
			return;
		}
		/**/
		copyFileService.cancelCopy();
	}
	
	
	private boolean isHidden = false;
	/**
	 * 显示正在复制文件的对话框, (如果正在复制的话)
	 * */
	public void showHiddenCopyDialog(){
		isHidden = false;
		copyFileService.setHidden(false);
		if (copyProgressDialog != null ) {
			copyProgressDialog.show();
			copyProgressDialog.show();
			copyProgressDialog.setProgress((int) copyFileService.copyedLength);
		}
	}
	
	public void incrementCopyProgressBy(int diff) {
		copyProgressDialog.incrementProgressBy(diff);
	}
	
	private void initCopyWarningDialog(){
		copyWarningDialog = new AlertDialog.Builder(fileManager);
		copyWarningDialog.setTitle(fileManager.getString(R.string.copyFile))
			.setCancelable(false);
	}
	

	/**
	 * 根据粘贴板 fileDialog.paste 的内容, 是否有文件名同当前文件相同. 相同则弹出对话框由用户决定, 重命名, 覆盖,
	 * 取消, 路过之类.
	 * @return
	 * 	1 	 正常返回,已筛选出要复制的文件名,
	 *  2(COPY_CANCEL)	返回 取消  复制文件
	 *  -1	出错
	 * */

	
	private void initRenameWarningDialog(){
		AlertDialog.Builder b = new AlertDialog.Builder(fileManager);
		b.setTitle(fileManager.getString(R.string.rename)).setItems(
				fileManager.getResources().getStringArray(R.array.rename_mult),
				new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				switch(which){
				case 0:		//append (2)
					renamePath = Common.fileNameAppend(renamePath, "(2)");
					doRename();
					break;
				case 1:		//replace
					doRename();
					break;
				case 2:		//do nothing
					break;
				default:
						break;
				}
			}
		}).setCancelable(false);
		
		renameWarningDialog = b.create();
		
	}
	
	/**
	 * 创建 `创建文件或文件夹` 对话框
	 * */
	private void initCreateFileDialog(){
		createFileEdit = new EditText(fileManager);
		AlertDialog.Builder b = XDialog.createInputDialog(fileManager, null,
				createFileEdit);
		b.setCancelable(false).setPositiveButton(
				fileManager.ok, new AlertDialog.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				String n = createFileEdit.getText().toString();
				String cFile = fileManager.currentPath() + "/" + n;
				File mFile = new File(cFile);
				boolean success = false;
				if (mFile.exists()) {
					Toast.makeText(fileManager, n + fileManager.getString(R.string.existed),
							Toast.LENGTH_SHORT).show();
					return;
				}
				java.lang.Process p = null;
				BufferedReader br = null;
				try {
					if (fileManager.isRoot()){
						// 带 root
						p = fileManager.linux.shell.exec("su");
						DataOutputStream shell = new DataOutputStream(p.getOutputStream());
						String cmd;
						if (createDirectory)
							cmd = "mkdir \"" + cFile + "\"\n" + "exit\n";
						else 
							cmd = "echo > \"" + cFile + "\"\n" + "exit\n";
						shell.write(cmd.getBytes());
						shell.flush();
						shell.close();
						br = new BufferedReader(
								new InputStreamReader(p.getErrorStream()));
						if (p.waitFor() != 0) {
							Toast.makeText(fileManager, br.readLine(), Toast.LENGTH_LONG).show();
							//return;
						} else
							success = true;
					}else{
						// 不带 root
						if (createDirectory){
							if (!(success = mFile.mkdirs()))
								Toast.makeText(fileManager, 
										fileManager.getString(R.string.creat_folder_failth), 
										Toast.LENGTH_SHORT).show();
						} else {
							if (!(success =mFile.createNewFile()))
								Toast.makeText(fileManager, 
										fileManager.getString(R.string.creat_file_failth), 
										Toast.LENGTH_SHORT).show();
						}
					}
					if ( success) {
						fileManager.refreshPath(fileManager.currentPath(), 0);
						fileManager.fileViewList.setSelection(fileManager.listListener.position);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					if (p != null)
						p.destroy();
					try {
						if (br != null)
							br.close();
					} catch (IOException e){
						e.printStackTrace();
					}
				}
				createFileEdit.setText("");
			}
		}).setNegativeButton(fileManager.cancel, new AlertDialog.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				createFileEdit.setText("");
			}
		});
		
		createFileDialog = b.create();
	}
	
	private void initOpenMannerDialog(){
		AlertDialog.Builder b = new AlertDialog.Builder(fileManager);
		openMannerDialog = b.setTitle(
				fileManager.getString(R.string.choice_program))
				.setItems(fileManager.getResources().getStringArray(R.array.programs),
				new AlertDialog.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(Intent.ACTION_VIEW);
						Uri uri = Uri.parse("file://" + absPath);
						switch (which){
						case OPEN_AUDIO:
							intent.setDataAndType(uri, "video/*");		//视频
							break;
						case OPEN_TEXT:
							intent.setDataAndType(uri, "text/*");		//文本
							break;
						case OPEN_IMAGE:
							intent.setDataAndType(uri, "image/*");		//图片
							break;
						case OPEN_VIDEO:
							intent.setDataAndType(uri, "audio/*");		//音乐
							break;
						default:
								return;
						}
						try {
							fileManager.startActivityForResult(intent, 2);
						} catch (ActivityNotFoundException e){
							Toast.makeText(fileManager, 
									fileManager.getString(R.string.can_not_open_file), 
									Toast.LENGTH_SHORT).show();
						}
					}
				}).setPositiveButton(fileManager.ok, null)
				.setNegativeButton(fileManager.cancel, new AlertDialog.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
	}
	


	/** spinner 的ID 转到 权限值*/
	final static int idToPer(int i){
		switch (i){
		case PER_NULL:
			return 0;
		case PER_ONLY_READ:
			return 4;
		case PER_ONLY_EXEC:
			return 1;
		case PER_READ_WRITE:
			return 6;
		case PER_READ_EXEC:
			return 5;
		case PER_WRITE_EXEC:
			return 7;
		default: return 0;
		}
	}
	/** 权限值 转到  spinner 的ID**/
	final static int perToId(int p){
		switch (p){
		case 0:
			return PER_NULL;
		case 1:
			return PER_ONLY_EXEC;
		case 4:
			return PER_ONLY_READ;
		case 6:
			return PER_READ_WRITE;
		case 5:
			return PER_READ_EXEC;
		case 7:
			return PER_WRITE_EXEC;
		default: return 0;
		}
	}

}/**ListDialogOnClickListener*/







