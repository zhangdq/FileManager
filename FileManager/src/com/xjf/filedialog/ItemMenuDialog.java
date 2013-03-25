package com.xjf.filedialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
/**
 * 文件长按对话框
 * */
public class ItemMenuDialog extends Dialog implements DialogInterface {
	//private static final String tag = "FileDialog";
	Context context;
	FileManager file;
	LinearLayout layout;
	GridView gv;
	int height = -1;
	String filePath;
	String[] menuNames;
	
	protected ItemMenuDialog(Context context) {
		super(context);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.context = context;
		file = (FileManager)context;
		menuNames = file.getResources().getStringArray(R.array.longClickListMenu);
		LayoutInflater inflater = LayoutInflater.from(context);
		layout = (LinearLayout) inflater.inflate(R.layout.gridview_menu, null);
		gv = (GridView) layout.findViewById(R.id.gridview);
		gv.setAdapter( new MenuAdater());
		gv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (position == FileManager.MENU_ITEM_ADD_LIB) {
					file.showAddFileLibDialog();
				} else {
					file.listListener.onClick(null, position);
				}
				ItemMenuDialog.this.dismiss();
				
			}
		});
		setContentView(layout);
		//setupMenuListener();
	}	
	
	public void selectedFile(String f) { filePath = f;}
	/**
	View.OnClickListener listener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int selection = v.getId() - ID_OFFSET;
			if (file != null){
				//file.listListener.onClick(selectView, selection);
			}
			ItemMenuDialog.this.dismiss();
		}
	};
	/**
	private void setupMenuListener(){
		int[] tvId = {R.id.topen, R.id.topenmanner, R.id.tcopy,
					R.id.tcut, R.id.tdelete, R.id.trename, 
					R.id.tselall, R.id.tper};
		int[] clickId = {FileManager.MENU_ITEM_OPEN, 
				FileManager.MENU_ITEM_OPEN_IN_OTHER,
				FileManager.MENU_ITEM_COPY, FileManager.MENU_ITEM_CUT,
				FileManager.MENU_ITEM_DELETE, FileManager.MENU_ITEM_RENAME,
				FileManager.MENU_ITEM_SELECT_ALL, FileManager.MENU_ITEM_PROPERTIES};
		TextView tv = null;
		for (int i = 0; i < tvId.length; i++){
			tv = (TextView) layout.findViewById(tvId[i]);
			tv.setOnClickListener(listener);
			tv.setId(clickId[i] + ID_OFFSET);
		}
	}
		/***/
	public boolean onTouchEvent(MotionEvent event){
		if (height < 0)
			height = layout.getHeight();
		int ty = (int) event.getY();
		if (ty < 0 || ty > height){
			this.dismiss();
			return true;
		}
		return super.onTouchEvent(event);
	}
	
	class MenuAdater extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return menuNames.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			TextView v = new TextView(context);
			v.setTextColor(Color.WHITE);
			v.setEllipsize(TruncateAt.MIDDLE);
			v.setGravity(Gravity.CENTER);
			v.setBackgroundResource(R.drawable.menu_d);
			v.setText(menuNames[position]);
			return v;
		}


	}
}
