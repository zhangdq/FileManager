package com.xjf.filedialog;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.TextView;

/**
 * µØÖ·À¸ GalleryµÄadapter
 * */
public class TextGalleryAdapter extends BaseAdapter{
	//private String tag = "FileDialog";
	
	private Context context;
	private int currentPosition = 0;
	private Resources res;
	private String absolutePath;
	private String[] paths = null; 
	private int count = 1;
	private final int TEXT_WIDTH;
	private final int TEXT_SIZE;
	private final int TEXT_HEIGHT;
	private final int TEXT_MAX_WIDTH;
	private final float PIX_SCALE;
	private static final int TEXT_COLOR = 0xff05294d;
	private static final int CUREENT_TEXT_COLOR = 0xffffffff;
	
	public TextGalleryAdapter(Context ct, String path){
		context = ct;
		res = context.getResources();
		PIX_SCALE = res.getDisplayMetrics().density;
		TEXT_HEIGHT = (int)(30*PIX_SCALE+0.5f);
		TEXT_MAX_WIDTH = (int)(90*PIX_SCALE+0.5f);
		TEXT_SIZE = (int)(25*PIX_SCALE+0.5f); 
		TEXT_WIDTH  = (int)(13*PIX_SCALE+0.5f);
		refreshPath(path);
	}
	
	public int getCurrentPosition() {return currentPosition;}
	public void setCurrentPosition(int i) {currentPosition = i;}
	public String getAbsolutePath() {return absolutePath;}
	
	public String getPath(int position){
		String path = new String("/");
		if (position == 0)
			return path;
		for (int i = 1; i < position; i++)
			path = path + paths[i] + "/";
		
		return path + paths[position];
	}
	
	/** Refresh path, reset {@code paths}*/
	public void refreshPath(String path){
		absolutePath = path;
		paths = path.split("/");
		count = paths.length;
		currentPosition = paths.length - 1;
		//Log.d(tag, "count " + count);
		this.notifyDataSetChanged();
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return count;
	}

	/**Return String*/
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return paths[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		TextView v = (TextView)convertView;
		//Log.d(tag, "gallery adapter getView, position:" + position);
		if (v == null){
			v = new TextView(context);
    		v.setSingleLine(true);
    		v.setEllipsize(TruncateAt.MARQUEE);
    		v.setTextSize(TEXT_SIZE);
    		if (position == currentPosition)
    			v.setTextColor(CUREENT_TEXT_COLOR);
    		else
    			v.setTextColor(TEXT_COLOR);

    		//v.setBackgroundResource(R.drawable.t4);
    		v.setMaxWidth(TEXT_MAX_WIDTH);
    		v.setGravity(Gravity.CENTER_HORIZONTAL);
		}
		if (position != 0){
			v.setText(paths[position]);
			int le = v.getText().length();
			if (le <= 3)
				le = 4;
    		v.setLayoutParams(new Gallery.LayoutParams(le *
    				TEXT_WIDTH, TEXT_HEIGHT));
    		//dra.setBounds(0, 0, v.getText().length() *
    		//		TEXT_WIDTH, TEXT_HEIGHT);
		} else {
			v.setText("/");
    		v.setLayoutParams(new Gallery.LayoutParams(
    				TEXT_WIDTH + 20, TEXT_HEIGHT));
    		//dra.setBounds(0, 0, TEXT_WIDTH, TEXT_HEIGHT);
		}
		//v.setBackgroundColor(0x8045cfcf);

		//v.setBackgroundDrawable(dra);
		v.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					v.setBackgroundColor(0xf0ffaa00);
				return false;
			}
		});
		return v;
	}

}
