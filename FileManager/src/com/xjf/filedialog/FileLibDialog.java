package com.xjf.filedialog;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.xjf.utils.Common;
import com.android.xjf.utils.XDialog;
import com.android.xjf.utils.XDialog.InputClick;
import com.xjf.filedialog.FileAdapter.FileInfo;

public class FileLibDialog extends Dialog implements DialogInterface{
	/** 
	 * 文件库数据结构为
	 * parentList 放 文件库名.
	 * childList 放对应文件库里的文件.
	 * 库名在parentList的位置与其文件在childList的位置一样
	 * */
	private ArrayList<String> parentList;
	private ArrayList<ArrayList<String>> childList; 
	private FileManager fileManager;
	private Button addBtn, cancelBtn;
	private String path;
	private static final String close = "关闭";
	RelativeLayout layout;
	ExpandableListView lists;
	ExpandListAdapter adapter;
	public static final char FILE_LIB_ADD = 0;
	public static final char FILE_LIB_OPEN = 1;
	public char doWhat = 0;
	public void setPath(String s) { path = s;}
	public FileLibDialog(final FileManager fm, ArrayList<String> parent,
			ArrayList<ArrayList<String>> child) {
		super((Context) fm);
		// TODO Auto-generated constructor stub
		parentList = parent;
		childList = child;
		this.fileManager = fm;
		LayoutInflater inflater = fm.getLayoutInflater();
		layout = (RelativeLayout) inflater.inflate(R.layout.filelibs, null);
		lists = (ExpandableListView) layout.findViewById(R.id.expandableListView);
		adapter = new ExpandListAdapter();
		lists.setAdapter(adapter);
		setTitle("文件库");
		setContentView(layout);
		
		/** 添加文件到库*/
		lists.setOnGroupClickListener(new OnGroupClickListener() {
			
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				// TODO Auto-generated method stub
				if (doWhat == FILE_LIB_ADD) {
					ArrayList<String> child = childList.get(groupPosition);
					if (!fm.isMultFile()) {
						if (child.contains(path)) {
							Toast.makeText(fm, path + "已存在", Toast.LENGTH_SHORT).show();
							return false;
						}
						child.add(path);
					} else {
						ArrayList<FileInfo> fis = fileManager.currentFileInfo();
						ArrayList<Integer> fTmp = fileManager.selectedItem();
						int size = fTmp.size();
						String tmp;
						for (int i = 0; i < size; i++){
							tmp = fis.get(fTmp.get(i)).path();
							if (child.contains(tmp)) {
								continue;
							}
							child.add(tmp);
						}
					}
					FileLibDialog.this.dismiss();
					return true;
				}
				return false;
			}
		});
		/***/
		lists.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				// TODO Auto-generated method stub
				if (doWhat == FILE_LIB_ADD)
					return false;
				String p = childList.get(groupPosition).get(childPosition);
				File file = new File(p);
				if (file.exists()) {
					if (file.isDirectory()) {
						fm.refreshPath(p, 1);
					} else {
						fm.listListener.doOpenFile(p);
					}
					FileLibDialog.this.dismiss();
				} 
				// add
				return true;
			}
		});
		/***/
		
		lists.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				final int p = (int) ((id >> 32) & 0xffff);
				final int c = (int) (id & 0xffff);
				if (id < 0) {
					// 点击的是字列表
					final String cPath = childList.get(p).get(c);
					final String name = Common.getPathName2(cPath);
					CharSequence[] cs = null;
					cs = new CharSequence[] {"从库中删除", "打开文件目录"};
					AlertDialog.Builder b = new AlertDialog.Builder(fm);
					b.setItems(cs ,
							new AlertDialog.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									switch (which) {
									case 0:
										AlertDialog.Builder b = new AlertDialog.Builder(
												fm);
										OnClickListener cl = new OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												// TODO Auto-generated method
												// stub
												if (which == AlertDialog.BUTTON_POSITIVE) {
													childList.get(p).remove(c);
													adapter.notifyDataSetChanged();
												}
											}
										};
										b.setTitle("从库中删除")
												.setMessage(
														"确定要删除 " + name
																+ " ?")
												.setPositiveButton(
														fm.ok, cl)
												.setNegativeButton(
														fm.cancel, cl)
												.create().show();

										break;

									case 1:
										fm.refreshPath(Common.getParentPath(cPath), 1);
										FileLibDialog.this.dismiss();
										break;
									default:
										break;
									}
								}
							});
					b.setNegativeButton(close,
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub

								}
							}).create().show();
			
				} else {
					// 点击组
					AlertDialog.Builder b = new AlertDialog.Builder(fm);
					b.setItems(new CharSequence[]{"删除", "重命名"}, 
							new AlertDialog.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									final String libName = parentList.get(p);
									switch (which) {
									case 0:
										AlertDialog.Builder b = new AlertDialog.Builder(fm);
										OnClickListener cl = new OnClickListener() {
											
											@Override
											public void onClick(DialogInterface dialog, int which) {
												// TODO Auto-generated method stub
												if (which == AlertDialog.BUTTON_POSITIVE) {
													removeParent(p);
													adapter.notifyDataSetChanged();
												}
											}
										};
										b.setTitle("删除文件库").setMessage("确定要删除 " + libName + " ?")
											.setPositiveButton(fm.ok, cl)
											.setNegativeButton(fm.cancel, cl).create().show();
										
										break;
									case 1:
										XDialog.InputClick ic = new XDialog.InputClick() {
											
											@Override
											public void onClickListener(String str, int which) {
												// TODO Auto-generated method stub
												if (which == AlertDialog.BUTTON_POSITIVE) {
													parentList.remove(p);
													parentList.add(p, str);
													adapter.notifyDataSetChanged();
												}
											}
										};
										XDialog.inputDialog(fm, "重命名库" + libName, 
												"新库名", libName, null, 
												fm.ok, fm.cancel, ic).show();
										break;
									default:
										break;
									}
								}
							});
					b.setNegativeButton(close, new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
					}).create().show();
				}
				return true;
			}
		});
		
		addBtn = (Button) layout.findViewById(R.id.libsadd);
		addBtn.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				InputClick ic = new InputClick() {
					
					@Override
					public void onClickListener(String str, int which) {
						// TODO Auto-generated method stub
						if (which == AlertDialog.BUTTON_POSITIVE) {
							parentList.add(str);
							childList.add(new ArrayList<String>());
							adapter.notifyDataSetChanged();
						}
						//FileLibDialog.this.dismiss();
					}
				};
				
				XDialog.inputDialog(fm, "添加文件库", "文件库名", 
						null, null, fm.ok, fm.cancel, ic).show();
			}
		});
		
		
		cancelBtn = (Button) layout.findViewById(R.id.libscancel);
		cancelBtn.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				FileLibDialog.this.dismiss();
			}
		});
	}
	
	
	
	public void removeParent(int location) {
		parentList.remove(location);
		childList.remove(location);
	}
	
	class ExpandListAdapter extends BaseExpandableListAdapter {

		@Override
		public int getGroupCount() {
			// TODO Auto-generated method stub
			return parentList.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			// TODO Auto-generated method stub
			return childList.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			// TODO Auto-generated method stub
			return parentList.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return childPosition;
		}

		@Override
		public long getGroupId(int groupPosition) {
			// TODO Auto-generated method stub
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return true;
		}

		private View generateView(boolean parent,
				int groupPosition, int childPosition, ArrayList<String> child) {
			TextView tv = new TextView(fileManager);
			int width = 40;
			/**/
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, width);
            tv.setLayoutParams(lp);
            /**/
            tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            tv.setTextColor(Color.WHITE);
            if (parent) {
            	tv.setText(Common.getPathName2(parentList.get(groupPosition)));
                tv.setBackgroundResource(R.drawable.libsbg);
            }else {
                String c = child.get(childPosition);
    			tv.setText(Common.getPathName2(c));
    			//tv.setBackgroundResource(R.drawable.libchild);
            }
			tv.setPadding(36, 0, 0, 0);
			return tv;
		}
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return generateView(true, groupPosition, 0, null);
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ArrayList<String> child = childList.get(groupPosition);
			if (child.isEmpty())
				return null;
			return generateView(false, groupPosition, childPosition, child);
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return true;
		}
		
	}

	//private String tag = "FileDialog";

}
