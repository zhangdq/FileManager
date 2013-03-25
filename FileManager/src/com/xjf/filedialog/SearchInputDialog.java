package com.xjf.filedialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
/**
 * 搜索对话框,要搜索时,在这个对话框里输入要搜索的内容和设置
 * */
public class SearchInputDialog extends Dialog implements DialogInterface  {
	RelativeLayout layout;
	private onSearchListener listener = null;
	public SearchInputDialog(Context context) {
		super(context);
		layout = (RelativeLayout) LayoutInflater.
									from(context).inflate(R.layout.searchinput, null);
		Button btn = (Button) layout.findViewById(R.id.searchok);
		btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismiss();
				if (listener == null) {
					return;
				}
				CheckBox cb = (CheckBox) layout.findViewById(R.id.searchcase);
				boolean caseSense = cb.isChecked();
				cb = (CheckBox) layout.findViewById(R.id.searchall);
				boolean allMatch = cb.isChecked();
				EditText edit = (EditText) layout.findViewById(R.id.searchedit);
				String expr = edit.getText().toString();
				//
				listener.onSearch(expr, allMatch, caseSense);
			}
		});
		
		btn = (Button) layout.findViewById(R.id.searchcancel);
		btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismiss();
			}
		});
		setContentView(layout);
		setTitle(context.getString(R.string.search));
		// TODO Auto-generated constructor stub
	}

	public void setOnSearchListener(onSearchListener sl){
		listener = sl;
	}
	public interface onSearchListener{
		public void onSearch(String expr, boolean allMatch, boolean caseSense);
	}
}
