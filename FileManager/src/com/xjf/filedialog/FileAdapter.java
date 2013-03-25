package com.xjf.filedialog;


import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.android.xjf.utils.Common;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 
 * */
public class FileAdapter extends BaseAdapter{
	
	static String tag = "FileDialog";
	
	public final static int DIRECTORY = 1;
	public final static int TXT = 2;
	public final static int HTM = 3;
	public final static int MOVIE = 4;
	public final static int MUSIC = 5;
	public final static int PHOTO = 6;
	public final static int PKG = 7;
	public final static int ZIP = 8;
	public final static int UNKNOW = 9;
	
	public static Bitmap dDirectory;
	public static Bitmap dTxt;
	public static Bitmap dHtm;
	public static Bitmap dMovie;
	public static Bitmap dMusic;
	public static Bitmap dPhoto;
	public static Bitmap dPkg;
	public static Bitmap dZip;
	public static Bitmap dUnknow;
	
	private static final float ICON_DIP = 32f;
	private final int ICON_PIX;
	public final float PIX_SCALE;
	protected final static int COLOR_NAME = Color.BLACK;
	protected final static int COLOR_SELECTED = 0xff009500;
	
	
	//private Context context;
	private Resources res;
	protected FileManager fileManager;
	protected FileData fData;
	protected LayoutInflater inflater;
	protected PackageManager packageManager;
	protected PackageInfo info;
	//protected String currentPath = "/";
	
	public static final int STYLE_LIST = 1;
	public static final int STYLE_GRID = 2;
	private int style;
	
	public FileAdapter(FileManager context, FileData info, int style) {
		// TODO Auto-generated constructor stub
		//this.context = context;
		fileManager = (FileManager)context;
		fData = info;
		if (fData == null)
			fData = new FileData(new ArrayList<FileAdapter.FileInfo>(),
					null, FileManager.SDCARD_PATH);
		res = context.getResources();
		packageManager = context.getPackageManager();
		PIX_SCALE = res.getDisplayMetrics().density;
		ICON_PIX = (int)(ICON_DIP * PIX_SCALE + 0.5f);
		inflater = LayoutInflater.from(context); 
		this.style = style;
		initFileBitmap();
	}
	

	public void setCurrenPath(String p) { fData.path = p;}
	public FileData getData(){return fData;}
	public void setData(FileData data) {fData = data;} 
	public void recycleBitmap() {
		
	}
	/**
	 * Initialize file icon for fit size {@code Bitmap}
	 * */
	private void initFileBitmap(){
		if (res == null || dDirectory != null)
			return;
		dDirectory = BitmapFactory.decodeResource(res, R.drawable.folder);
		dTxt = BitmapFactory.decodeResource(res, R.drawable.txt);
		dHtm = BitmapFactory.decodeResource(res, R.drawable.htm);
		dMovie = BitmapFactory.decodeResource(res, R.drawable.movie);
		dMusic = BitmapFactory.decodeResource(res, R.drawable.music);
		dPhoto = BitmapFactory.decodeResource(res, R.drawable.photo);
		dPkg = BitmapFactory.decodeResource(res, R.drawable.pkg);
		dZip = BitmapFactory.decodeResource(res, R.drawable.zip);
		dUnknow = BitmapFactory.decodeResource(res, R.drawable.unknow);
		dTxt = Bitmap.createScaledBitmap(dTxt, ICON_PIX, ICON_PIX, true);
		dHtm = Bitmap.createScaledBitmap(dHtm, ICON_PIX, ICON_PIX, true);
		dMovie = Bitmap.createScaledBitmap(dMovie, ICON_PIX, ICON_PIX, true);
		dMusic = Bitmap.createScaledBitmap(dMusic, ICON_PIX, ICON_PIX, true);
		dPhoto = Bitmap.createScaledBitmap(dPhoto, ICON_PIX, ICON_PIX, true);
		dPkg = Bitmap.createScaledBitmap(dPkg, ICON_PIX, ICON_PIX, true);
		dZip = Bitmap.createScaledBitmap(dZip, ICON_PIX, ICON_PIX, true);
		dUnknow = Bitmap.createScaledBitmap(dUnknow, ICON_PIX, ICON_PIX, true);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return fData.fileInfos.size();
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
		if (fData.fileInfos.isEmpty())
			return null;
		Viewholder holder = null;
		RelativeLayout v = (RelativeLayout) convertView;
		/**/
		FileInfo fInfo = fData.fileInfos.get(position);
		// ListView
		if (v == null) {
			v = (RelativeLayout) inflater.inflate(R.layout.fileitem, null,
					false);
			holder = new Viewholder(v);
			v.setTag(holder);
		} else {
			holder = (Viewholder) v.getTag();
		}
		if (!fInfo.directory)
			holder.getSize().setText(fInfo.size());
		else
			holder.getSize().setText("");
		holder.getDate().setText(fInfo.date());

		holder.getName(R.id.filename).setText(fInfo.name);
		holder.setIcon(R.id.fileicon, fInfo);

		if (fileManager.multFile) {
			if (fData.selectedId.contains(position)) {
				holder.getName(R.id.filename).setTextColor(COLOR_SELECTED);
				holder.changed();
				return v;
			} else if (holder.isChanged()){
				holder.clearChanged();
				holder.getName(R.id.filename).setTextColor(Color.BLACK);
			}
		} else {
			if (holder.isChanged()){
				holder.clearChanged();
				holder.getName(R.id.filename).setTextColor(Color.BLACK);
			}
		}
		return v;
	}
	
	
	public static Bitmap getIconBitmap(int type){
		Bitmap m = null;
		switch (type) {
		case DIRECTORY:
			m = dDirectory;
			break;
		case TXT:
			m = dTxt;
			break;
		case HTM:
			m = dHtm;
			break;
		case MOVIE:
			m = dMovie;
			break;
		case MUSIC:
			m = dMusic;
			break;
		case PHOTO:
			m = dPhoto;
			break;
		case PKG:
			m = dPkg;
			break;
		case ZIP:
			m = dZip;
			break;
		default:
			m = dUnknow;
			break;
		}
		return m;
	}

	/**
	boolean dragFlag = false;
	View.OnLongClickListener longClickListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			// TODO Auto-generated method stub
			if (style == STYLE_GRID)
				return false;
			fileDialog.fileViewList.startToDrag(v.getHeight());
			dragFlag = true;
			return false;
		}
	};
	/***/
	/**
	 * File infomation, about name ,icon, size, folder or not.
	 * */
	public static class FileInfo implements Comparable<FileInfo>{

		int type;
		/**
		 * 最后的文件名
		 * */
		String name = null;
		String size = null;
		/**
		 * 完整路径
		 * */
		String path = null;
		String date = null;
		boolean directory = false;
		boolean selected = false;
		Drawable dr = null;
	
		//String date,
		public FileInfo(String name, String path, int type, String size,
				 boolean directory) {
			this.name = name;
			this.size = size;
			this.type = type;
			this.path = path;
			//this.date = date;
			this.directory = directory;
		}
		
		public String name(){ return this.name;}
		public String path(){ return this.path;}
		public void invertSelected() { selected = !selected;}
		public boolean selectted() {return selected;}
		public void setSelected(boolean s) {
			selected = s;
		}
		public String size(){ 
			if (size == null){
				File file = new File(path);
				this.size = Common.formatString(String.valueOf(file.length()));
				this.date = new Date(file.lastModified()).toLocaleString();
			}
			return this.size;
		}
		public String date(){ 
			if (date == null){
				File file = new File(path);
				this.size = Common.formatString(String.valueOf(file.length()));
				this.date = new Date(file.lastModified()).toLocaleString();
			}
			return this.date;
		}
		
		public Drawable getAPKDrawable(PackageManager pm){
			if (dr == null){
				if (pm == null){
					Log.w(FileAdapter.tag, "pm == null");
					return null;
				}
				PackageInfo pi = pm.getPackageArchiveInfo(path, 
						PackageManager.GET_ACTIVITIES);
				if (pi == null){
					Log.w(FileAdapter.tag, "pi == null");
					return null;
				}
				dr = pm.getApplicationIcon(pi.applicationInfo);
			}
			return dr;
		}
		
		public boolean directory() {return this.directory;}
		public int type(){return type;}

		@Override
		public int compareTo(FileInfo another) {
			// TODO Auto-generated method stub
			if (another.directory) {
				if (!directory)
					return 1;
				return this.name.compareTo(another.name);
			}
			if (directory)
				return -1;
			return this.name.compareTo(another.name);
		}
		
	
	}
	
	protected class Viewholder {
		private View base;
		private ImageView icon;
		private TextView name;
		private TextView size;
		private TextView date;
		
		private boolean changed;
		
		Viewholder(View view){
			base = view;
		}
		
		public boolean isChanged() {return changed;}
		public void changed() { changed = true;}
		public void clearChanged() { changed = false;}
		
		public ImageView getIcon(int id){
			if (icon == null){
					icon = (ImageView) base.findViewById(id);
					//icon.setOnLongClickListener(longClickListener);
			}
			return icon;
		}
		
		public TextView getName(int id){
			if (name == null) {
					name = (TextView) base.findViewById(id);
			}
			return name;
		}
		
		public TextView getSize() {
			if (size == null){
				size = (TextView)base.findViewById(R.id.filesize);
			}
			return size;
		}
		
		public TextView getDate(){
			if (date == null){
				date = (TextView)base.findViewById(R.id.filedate);
			}
			return date;
		}
		
		public void setIcon(int id, FileInfo fInfo){
			if (fInfo.type != PKG) {
				/**
				if (fInfo.type == PHOTO){
					BitmapFactory.Options opt = new BitmapFactory.Options(); 
					opt.inPreferredConfig = Bitmap.Config.RGB_565;
					//fileDialog.getResources().getDisplayMetrics().
					opt.outHeight = 50;
					opt.outWidth = 50;
					opt.inJustDecodeBounds = true;
					Bitmap bm = BitmapFactory.decodeFile(fInfo.path(), opt);
					Log.d(tag, "w: " + bm.getWidth());
					Log.d(tag, "h: " + bm.getHeight());
					
					Bitmap bm2 = Bitmap.createScaledBitmap(bm, 50, 50, true);
					bm.recycle();
					getIcon(id).setImageBitmap(bm2);
					return;
				}
				/**/
				getIcon(id).setImageBitmap(getIconBitmap(fInfo.type));
			} else {
				Drawable d = fInfo.getAPKDrawable(packageManager);
				if (d != null) {
					getIcon(id).setImageDrawable(d);
				} else {
					getIcon(id).setImageBitmap(getIconBitmap(fInfo.type));
				}
			}
		}
	}
}






















