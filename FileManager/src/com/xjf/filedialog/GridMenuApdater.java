package com.xjf.filedialog;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class GridMenuApdater extends PackageAdpater {

	private static final String tag = "FileDialog";
	
	private FileManager file;
	
	public GridMenuApdater(FileManager context, List<SimpleInfo> infos){
		super(context, infos);
		file = context;
	}
	
	
	
	@Override
	public Object getItem(int position){
		return infos.get(position);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		RelativeLayout v = (RelativeLayout)convertView;
		ViewHolder holder = null;
		if (v == null){
			v = (RelativeLayout)inflater.inflate(R.layout.appmenuitem, null, false);
			holder = new ViewHolder(v);
			v.setTag(holder);
		} else {
			holder = (ViewHolder) v.getTag();
		}
		SimpleInfo info = infos.get(position);
		if (info == null){
			Log.e(tag, "info == null");
		}
		holder.icon(R.id.menuicon).setBackgroundDrawable(info.icon());
		holder.name(R.id.menuname).setText(info.name());
		return v;
	}
	public void onItemClick(int position){
		
	}
}
