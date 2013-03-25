package com.xjf.filedialog;



import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class FileGridAdapter extends FileAdapter{

	public FileGridAdapter(FileManager context, FileData infos, int style) {
		super(context, infos, style);
		// TODO Auto-generated constructor stub
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		if (fData.fileInfos.isEmpty())
			return null;
		LinearLayout v = (LinearLayout)convertView;
		Viewholder holder = null;
		if (v == null){
			v = (LinearLayout) inflater.inflate(R.layout.gridfileitem, null, false);
			holder = new Viewholder(v);
			v.setTag(holder);
		} else {
			holder = (Viewholder) v.getTag();
		}

		FileInfo fInfo = fData.fileInfos.get(position);
		holder.getName(R.id.gridname).setText(fInfo.name);
		/**
		if (fInfo.type != PKG) {
			if (fInfo.type == PHOTO){
				Drawable d = new BitmapDrawable(fInfo.path());
				holder.getIcon(R.id.gridicon).setImageDrawable(d);
			}else
				holder.getIcon(R.id.gridicon).setImageBitmap(getIconBitmap(fInfo.type));
		} else {
			Drawable d = fInfo.getAPKDrawable(packageManager);
			if (d != null) {
				holder.getIcon(R.id.gridicon).setImageDrawable(d);
			} else {
				holder.getIcon(R.id.gridicon).setImageBitmap(getIconBitmap(fInfo.type));
			}
		}
		/**/
		holder.setIcon(R.id.gridicon, fInfo);
		if (fileManager.multFile) {
			if (fData.selectedId.contains(position)) {
				holder.getName(R.id.filename).setTextColor(COLOR_SELECTED);
				holder.changed();
				return v;
			} else if (holder.isChanged()){
				holder.clearChanged();
				holder.getName(R.id.filename).setTextColor(COLOR_NAME);
			}
		} else {
			if (holder.isChanged()){
				holder.clearChanged();
				holder.getName(R.id.filename).setTextColor(COLOR_NAME);
			}
		}
		return v;
	}
}
