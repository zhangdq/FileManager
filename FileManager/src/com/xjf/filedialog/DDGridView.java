package com.xjf.filedialog;


import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.location.GpsStatus.Listener;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.GridView;
import android.widget.TextView;

import com.xjf.filedialog.DDListView.DragListener;
import com.xjf.filedialog.DDListView.DropListener;
import com.xjf.filedialog.DDListView.DropOutListener;
import com.xjf.filedialog.DDListView.StartDragListener;
/**
 * GridView 扩展,可拖拉,带弹性, DDListView的注释比较详细.
 * */
public class DDGridView extends GridView{

	public final static String tag = "FileDialog";
	
	private Context context;
	private FileManager fileManager;
	private android.view.WindowManager.LayoutParams mWindowParams;
	private TextView dragView;
	private Bitmap dragBitmap = null; // 要Drag的项的Bitmap
	private WindowManager mWindowManager;

	private View currentPosView = null;

	private DragListener dragListener = null;
	private DropListener dropListener = null;
	private DropOutListener dropOutListener = null;
	private StartDragListener startDragListener = null;

	private int dragItemFrom = -1; // 拉动的目标项的位置
	private int dragCurPos = -1; // 拖动时当前的项.

	public int dragMinX = 0;
	public int dragMaxX = 0;
	
	private boolean waitMoveDrag = false;
	private boolean dragging = false;
	
	//private View parentView;
	private boolean outBound = false;
	private int distance, firstOut;

	private boolean downing = false;
	//private int dragY = 0;
	private boolean doTask = false;
	private boolean timerRun = false;
	private final static int ROLL_HEIGHT_DP = 50;
	//private int ROLL_HEIGHT;
	
	public DDGridView(Context context){
		super(context);
		init(context);
	}
	
	
	public DDGridView(Context context, AttributeSet attrs){
		super(context, attrs);
		init(context);
	}
	
	public DDGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context){
		this.context = context;
		fileManager = (FileManager) context;
		initDragWindowParams();
		this.setSmoothScrollbarEnabled(true);
		//ROLL_HEIGHT = ROLL_HEIGHT_DP * (int)fileManager.getDensity();
	}
	
	public void setDragable(boolean b){ waitMoveDrag = b;}
	public boolean isDragging() { return dragging;}

	public void clearDragBG() {
		if (currentPosView != null) {
			currentPosView.setBackgroundDrawable(null);
			Log.w(tag, "clear");
		}
	}
	
	
	//public void setParentView(View v) { parentView = v;}
	GestureDetector mGestureDetector = new GestureDetector(context, 
			new GestureDetector.OnGestureListener() {
				
				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					// TODO Auto-generated method stub
					return false;
				}
				
				@Override
				public void onShowPress(MotionEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
						float distanceY) {
					// TODO Auto-generated method stub
					/**
					if (parentView == null){
						return false;
					}/**/
					/**
					 *  弹性的实现, 主要用scrollTo(int x, int y)方法移动listView
					 *  根据点击位置离listView的距离,不断地用scrollTo调整listView的位置.
					 */
					int firstPos = getFirstVisiblePosition();
					int lastPos = getLastVisiblePosition();
					int itemCount = getCount();

					if (outBound && firstPos != 0
							&& lastPos != (itemCount - 1)) {
						scrollTo(0, 0);
						//DDGridView.this.computeScroll();
						return false;
					}
					View firstView = getChildAt(firstPos);
					
					if (!outBound)
						firstOut = (int) e2.getRawY();
					//在上面
					if (firstView != null && 
						(outBound || (firstPos == 0 && firstView.getTop() == 0 && distanceY < 0))){
						distance = firstOut - (int) e2.getRawY();
						scrollTo(0, distance);
						outBound = true;
						return true;
					} else {
					}


					View lastView = getChildAt(lastPos - firstPos);
					if (lastPos != (itemCount - 1))
						return false;
					int GridHeight = getHeight();
					if (lastView != null && (outBound || 
						(lastView.getBottom() == GridHeight && distanceY > 0))) {
						distance = firstOut - (int) e2.getRawY();
						scrollTo(0, distance);
						outBound = true;
						return true;
					}

					
					//在下面
					/**
					if (outBound || (lastPos == itemCount &&
							lastView.get))
							/**/
					return false;
				}
				
				@Override
				public void onLongPress(MotionEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
						float velocityY) {
					// TODO Auto-generated method stub
					return false;
				}
				
				@Override
				public boolean onDown(MotionEvent e) {
					// TODO Auto-generated method stub
					return false;
				}
			});
	@Override
	public boolean dispatchTouchEvent(MotionEvent event){

		if (dragging)
			return super.dispatchTouchEvent(event);
		int act = event.getAction();
		/**/
		if ((act == MotionEvent.ACTION_UP || act == MotionEvent.ACTION_CANCEL) 
				&& outBound) {	
			//DDGridView.this.computeScroll();
			reback();
		}
		/**/
		if (!mGestureDetector.onTouchEvent(event)) {
			outBound = false;
		} else {
		}
		return super.dispatchTouchEvent(event);
	}
	public boolean isOutBound() {return outBound;}
	public void reback() {
		outBound = false;
		/**/
		Rect rect = new Rect();
		getLocalVisibleRect(rect);
		TranslateAnimation am = new TranslateAnimation( 0, 0, -rect.top, 0);
		am.setDuration(300);
		startAnimation(am);
		/**/
		scrollTo(0, 0);	
	}
	/**/
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		/**/
		if (!fileManager.pre_Dragable || 
				(dragListener == null && dropListener == null))
			return super.onInterceptTouchEvent(event);
		
		if (!waitMoveDrag){
			return super.onInterceptTouchEvent(event);
		}
		
		fileManager.clearClickTime();
		int act = event.getAction();
		waitMoveDrag = false;
		switch (act) {
		case MotionEvent.ACTION_DOWN:
			int x = (int) event.getX();
			int y = (int) event.getY();
			int itemNum = pointToPosition(x, y);
			if (itemNum == INVALID_POSITION)
				break;
			dragItemFrom = dragCurPos = itemNum;
			View item = (View) getChildAt(itemNum - getFirstVisiblePosition());
			if (item == null) {
				break;
			}
			dragging = true;
			//itemHeight = item.getHeight();
			// item.setBackgroundColor(Color.BLUE);
			item.setDrawingCacheEnabled(true);
			Bitmap bm = Bitmap.createBitmap(item.getDrawingCache());
			startDrag(bm, (int) event.getRawX(), (int) event.getRawY());
			startDragListener.startDrag(itemNum);
			return false;
		}
		/***/
		return super.onInterceptTouchEvent(event);
	}
	/***/
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (dragging) {
			//doTask = false;
			int action = event.getAction();
			//downHeight = getHeight() - itemHeight;
			switch (action) {
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				
				dragging = false;
				if (currentPosView != null)
					currentPosView.setBackgroundDrawable(null);
					//currentPosView.setBackgroundColor(0x00ffffff);
				stopDrag();
				//Log.d(tag, "stop " + dragCurPos);
				if (dropOutListener != null && 
						(event.getY() < 0
						|| (event.getY() + getTop()) > getBottom())
						|| dragCurPos == DDGridView.INVALID_POSITION) {
					// 在没有文件的位置或超出listView的地方放手.
					//Log.d(tag, "out");
					dropOutListener.dropOut(dragItemFrom,
							(int) event.getX(), (int) event.getY());
				} else if (dropListener != null && dragCurPos < getCount()
						&& dragCurPos >= 0) {
					dropListener.drop(dragItemFrom, dragCurPos);
				}
				break;
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				int x = (int) event.getX();
				int y = (int) event.getY();
				
				dragView(x, y);
				int itemnum = pointToPosition(x, y);
				if (itemnum == INVALID_POSITION) {
					dragCurPos = itemnum;
					break;
				}
				if (itemnum != dragCurPos && currentPosView != null) {
					currentPosView.setBackgroundDrawable(null);
					//currentPosView.setBackgroundColor(0x00ffffff);
				}
				currentPosView = getChildAt(itemnum - getFirstVisiblePosition());
				if (currentPosView != null)
					currentPosView.setBackgroundResource(
							R.drawable.list_drag_background);
				if ((action == MotionEvent.ACTION_DOWN || itemnum != dragCurPos)) {
					if (dragListener != null) {
						dragListener.drag(dragCurPos, itemnum,
								action == MotionEvent.ACTION_DOWN);
					}
					dragCurPos = itemnum;
				}
				
				/**
				 * 在边缘时可以上下滚动
				 * */
				//if (!doTask) {
					//dragY = y;
					int viewHeight = getHeight() / 6;
					if (y >= viewHeight * 5) {
						if (itemnum == getCount() - 1) {
							doTask = false;
							break;
						}
						downing = true;
						doTask = true;
						sendMessage();
						
					} else if (y <= viewHeight) {
						if (itemnum == 0) {
							//setSelectionFromTop(itemnum, 0);
							doTask = false;
							break;
						}
						downing = false;
						doTask = true;
						sendMessage();
					} else {
						doTask = false;
					}
				//}
				/**/
				break;
			}
			return true;
		}
		return super.onTouchEvent(event);
	}
	
	// 下面是实现上下滚动
	Timer timer = new Timer();;
	class XTimerTask extends TimerTask {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				timerRun = true;
				while (doTask) {
					ha.sendEmptyMessage(0);
					Thread.sleep(450);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				doTask = false;
				timerRun = false;
			}
		}

	};
	private void sendMessage() {
		if (timerRun)
			return;
			if (doTask == false)
				return;
			timer.cancel();
			timer = new Timer();
			timer.schedule(new XTimerTask(), 100);
	}
	Handler ha = new Handler() {
		public void handleMessage(Message msg) {
			if (!doTask)
				return;
			/**
			currentPosView = getChildAt(dragCurPos - getFirstVisiblePosition());
			if (currentPosView != null)
				currentPosView.setBackgroundDrawable(null);
				/**/
			int first = getFirstVisiblePosition();
			if (!downing) {
				if (first == 0) {
					doTask = false;
				} else {
					setSelection(first - 5);
				}
			} else {
				int last = getLastVisiblePosition();
				if (last == getCount() - 1){
					doTask = false;
				} else {
					setSelection(first + 5);
				}
			}
		}
	};
	
	
	private void initDragWindowParams() {
		mWindowParams = new WindowManager.LayoutParams();
		mWindowParams.gravity = Gravity.TOP | Gravity.LEFT;
		mWindowParams.alpha = 1.0f;
		mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		mWindowParams.format = PixelFormat.TRANSLUCENT;
		mWindowParams.windowAnimations = 0;
	}
	private void startDrag(Bitmap bm, int x, int y) {
		stopDrag();
		mWindowParams.x = x;
		mWindowParams.y = y;
		TextView v = new TextView(context);
		
		v.setBackgroundDrawable(new BitmapDrawable(bm));
		dragBitmap = bm;

		mWindowManager = (WindowManager) context.getSystemService("window");
		mWindowManager.addView(v, mWindowParams);
		
		dragView = v;
		if (fileManager.multFile) {
			dragView.setTextSize(35);
			dragView.setTextColor(Color.RED);
			dragView.setGravity(Gravity.CENTER);
			dragView.setText("" + fileManager.getCurrentSelectedCount());
		} //else
			//dragView.setBackgroundResource(R.drawable.list_drag_background);
		
	}

	private void stopDrag() {
		//doTask = false;
		if (dragView != null) {
			mWindowManager.removeView(dragView);
			dragView.setBackgroundDrawable(null);
			dragView = null;
		}
		if (dragBitmap != null) {
			if (!dragBitmap.isRecycled())
				dragBitmap.recycle();
			dragBitmap = null;
		}
	}

	private void dragView(int x, int y) {
		// Log.d(tag, "dragView: (" + x + "," + y + ")");
		mWindowParams.x = x;
		mWindowParams.y = y + (dragView.getHeight());
		mWindowManager.updateViewLayout(dragView, mWindowParams);
	}

	public void setDragListener(DragListener l) {
		dragListener = l;
	}

	public void setDropListener(DropListener l) {
		dropListener = l;
	}
	
	public void setDropOutListener(DropOutListener l){
		dropOutListener = l;
	}

	public void setStartDragListener(StartDragListener l){
		startDragListener = l;
	}
	
	
	
	/**
	 * 
					int itemCount = getCount();
					Log.d(tag, "count: " + itemCount + " last: " + lastPos);
					if (lastPos != (itemCount - 1))
						return false;
					int la = lastPos - firstPos;
					Log.d(tag, "laLL: " + la);
					View lastView = getChildAt(la);

					Log.d(tag, "Last: " + "h: " + lastView.getHeight() + "  " + 
							"top: " + lastView.getTop() + " bottom: " + lastView.getBottom());
					Log.d(tag, "MeasuredHeight: " + lastView.getMeasuredHeight());
					Log.d(tag, "Grid Hieght: " + getHeight());
					Rect outRect = new Rect();
					if (outRect != null)
						Log.d(tag, "jj: " + (outRect.top - outRect.bottom));
					lastView.getHitRect(outRect);
	 * */

}
