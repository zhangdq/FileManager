package com.xjf.filedialog;

import java.util.List;

import com.xjf.filedialog.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
/**
 * APKπ‹¿ÌGridViewµƒAdpater
 * */
public class PackageAdpater extends BaseAdapter {
	private static final String tag = "FileDialog";
	protected Context context;
	protected List<SimpleInfo> infos;
	protected LayoutInflater inflater;
	public PackageAdpater(Context context, List<SimpleInfo> infos) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.infos = infos;
		inflater = LayoutInflater.from(context); 
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return infos.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return infos.get(position).name;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LinearLayout v = (LinearLayout)convertView;
		ViewHolder holder;
		if (v == null){
			v = (LinearLayout)inflater.inflate(R.layout.packageitem, null, false);
			holder = new ViewHolder(v);
			v.setTag(holder);
		} else {
			holder = (ViewHolder)v.getTag();
		}
		
		Drawable ico = infos.get(position).icon();
		if (ico != null)
			holder.icon(R.id.apkicon).setBackgroundDrawable(ico);
		holder.name(R.id.apkname).setText(infos.get(position).name());
		return v;
	}
	
	public static class SimpleInfo implements Comparable<SimpleInfo>{
		public Drawable icon;
		public String name;
		public SimpleInfo(Drawable icon, String name) {
			// TODO Auto-generated constructor stub
			this.icon = icon;
			this.name = name;
		}
		
		public String name(){
			return name;
		}
		
		public Drawable icon(){
			return icon;
		}
		@Override
		public int compareTo(SimpleInfo another) {
			// TODO Auto-generated method stub
			return name.compareTo(another.name);
		}
	}
	
	
	protected class ViewHolder {
		private View base;
		private ImageView icon;
		private TextView name;
		public ViewHolder(View v){
			base = v;
		}
		
		public ImageView icon(int id) {
			if (icon == null){
				icon = (ImageView) base.findViewById(id);
				if (icon == null){
					Log.d(tag, "icon == null");
				}
			}
			return icon;
		}
		
		public TextView name(int id) {
			if (name == null){
				name = (TextView) base.findViewById(id);
			}
			return name;
		}
	}

}
