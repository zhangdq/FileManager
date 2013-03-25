package com.xjf.filedialog;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.ListView;
import android.widget.TextView;
/**
 * ListView 扩展, 可拖拉, 带弹性
 * */
public class DDListView extends ListView  {
	//private final static String tag = "FileDialog";

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
	private int itemHeight;
	private int downHeight;

	public int dragMinX = 0;
	public int dragMaxX = 0;
	
	private int touchX = 0;
	private int touchY = 0;
	//private int touchRawY = 0;
	
	private boolean waitMoveDrag = false;
	private boolean dragging = false;

	public DDListView(Context context) {
		super(context);
		this.context = context;
		this.fileManager = (FileManager) context;
		// TODO Auto-generated constructor stub
		init();
	}

	public DDListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		this.fileManager = (FileManager) context;
		// TODO Auto-generated constructor stub
		init();
	}

	public DDListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		this.fileManager = (FileManager) context;
		// TODO Auto-generated constructor stub
		init();
	}

	private void init() {
		initDragWindowParams();
		//this.setDividerHeight(4);
	}
	public boolean isDragging() { return dragging;}

	private boolean downing = false;
	private int dragY = 0;
	private boolean doTask = false;
	private boolean timerRun = false;

	// 下面是实现上下滚动
	Timer timer = new Timer();

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
			currentPosView = getChildAt(dragCurPos - getFirstVisiblePosition());
			if (currentPosView != null)
				currentPosView.setBackgroundDrawable(null);
			if (downing) {
				if (dragCurPos < getCount())
					dragCurPos++;
				else {
					dragY = downHeight;
					doTask = false;
				}
			} else {
				if (dragCurPos > 0) {
					dragCurPos--;
				} else {
					dragY = 0;
					doTask = false;
				}
			}
			currentPosView = getChildAt(dragCurPos - getFirstVisiblePosition());
			if (currentPosView != null)
				currentPosView.setBackgroundResource(
						R.drawable.list_drag_background);
			setSelectionFromTop(dragCurPos, dragY);
		}
	};

	private boolean outBound = false;
	private int distance;
	private int firstOut;
	
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
						//DDListView.this.computeScroll();
						return false;
					}
					View firstView = getChildAt(firstPos);

					if (!outBound)
						firstOut = (int) e2.getRawY();
					//在上面
					if (firstView != null && (outBound || 
							(firstPos == 0 && firstView.getTop() == 0 && distanceY < 0))){
						
						distance = firstOut - (int) e2.getRawY();
						scrollTo(0, distance);
						return true;
					}

					
					if (lastPos != (itemCount - 1))
						return false;
					View lastView = getChildAt(lastPos - firstPos);
					int GridHeight = getHeight();
					/**
					Log.d(tag,  "last: " + lastView.getBottom() + "\n" +
							"view: " + DDListView.this.getHeight() + "\n" + 
							"--: " + DDListView.this.getDividerHeight());
					/**/
					if (lastView != null && (outBound || 
						((lastView.getBottom() + 8) >= GridHeight && distanceY > 0))) {
						
						distance = firstOut - (int) e2.getRawY();
						scrollTo(0, distance);
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
		if ((act == MotionEvent.ACTION_UP || act == MotionEvent.ACTION_CANCEL) 
				&& outBound) {
			outBound = false;
			/**/
			// 弹性恢复位置
			Rect rect = new Rect();
			getLocalVisibleRect(rect);
			TranslateAnimation am = new TranslateAnimation( 0, 0, -rect.top, 0);
			am.setDuration(300);
			startAnimation(am);
			/**/
			scrollTo(0, 0);
			//DDListView.this.computeScroll();
			
			//
		}
		if (!mGestureDetector.onTouchEvent(event)) {
			outBound = false;
		} else {
			outBound = true;
		}
		return super.dispatchTouchEvent(event);
	}
	/**/
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		/**/
		if (!fileManager.pre_Dragable || 
				(dragListener == null && dropListener == null))
			return super.onInterceptTouchEvent(event);
		if (!waitMoveDrag){
			super.onInterceptTouchEvent(event);
		}
		waitMoveDrag = false;
		int act = event.getAction();

		switch (act) {
		case MotionEvent.ACTION_DOWN:
			// 判断是否进入拖曳的位置, 这里是listitem的图标
			int x = (int) event.getX();
			int y = (int) event.getY();
			if (dragMinX < x && x < dragMaxX) {
				int itemNum = pointToPosition(x, y);
				if (itemNum == INVALID_POSITION)
					break;
				dragItemFrom = dragCurPos = itemNum;
				View item = (View) getChildAt(itemNum
						- getFirstVisiblePosition());
				if (item == null) {
					break;
				}
				dragging = true;
				itemHeight = item.getHeight();
				// item.setBackgroundColor(Color.BLUE);
				item.setDrawingCacheEnabled(true);
				Bitmap bm = Bitmap.createBitmap(item.getDrawingCache());
				startDrag(bm, (int) event.getRawX(), (int) event.getRawY());
				startDragListener.startDrag(itemNum);
				return false;
			}
			dragView = null;
			break;
		}
		/**
		if (dragListener == null && dropListener == null)
			return super.onInterceptTouchEvent(event);

		if (dragging){
			int itemNum = pointToPosition(0, touchY);
			if (itemNum == INVALID_POSITION) {
				dragging = false;
				return false;
			}
			dragItemFrom = dragCurPos = itemNum;
			return false;
		}
		touchX = (int) event.getRawX();
		touchRawY = (int) event.getRawY();
		touchY = (int) event.getY();
		/**
		int act = event.getAction();

		switch (act) {
		case MotionEvent.ACTION_DOWN:
			if (dragMinX < touchX && touchX < dragMaxX) {
				super.onInterceptTouchEvent(event);
				return false;
			}
		}
		/***/
		return super.onInterceptTouchEvent(event);
	}
	/***/
	
	public void startToDrag(int top){
		if (dragListener == null && dropListener == null)
			return;
		waitMoveDrag = true;
		dragging = true;
		onInterceptTouchEvent( MotionEvent.obtain(18583914, 18583914, 
				MotionEvent.ACTION_DOWN, touchX, touchY, 0));
	}
	
	public void clearDragBG() {
		if (currentPosView != null)
			currentPosView.setBackgroundDrawable(null);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (dragging) {
			doTask = false;
			int action = event.getAction();
			downHeight = getHeight() - itemHeight;
			switch (action) {
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				
				dragging = false;
				if (currentPosView != null)
					currentPosView.setBackgroundDrawable(null);
					//currentPosView.setBackgroundColor(0x00ffffff);
				
				stopDrag();
				if (dropOutListener != null && (
						event.getY() > DDListView.this.getHeight()
						|| dragCurPos == DDListView.INVALID_POSITION)){

					// 在没有文件的位置或超出listView的地方放手.
					dropOutListener.dropOut(dragItemFrom, 
							(int)event.getX(), 
							(int)event.getY());
				} else if (dropListener != null && dragCurPos < getCount()
						&& dragCurPos >= 0) {
					dropListener.drop(dragItemFrom, dragCurPos);
				} 
				break;
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				int x = (int) event.getX();
				int y = (int) event.getY();
				/**
				if (waitMoveDrag) {

					waitMoveDrag = false;
					View item = (View) getChildAt(dragItemFrom
							- getFirstVisiblePosition());
					if (item == null) {
						break;
					}
					itemHeight = item.getHeight();
					// item.setBackgroundColor(Color.BLUE);
					item.setDrawingCacheEnabled(true);
					Bitmap bm = Bitmap.createBitmap(item.getDrawingCache());
					startDrag(bm, x, y);
					currentPosView = getChildAt(dragItemFrom - getFirstVisiblePosition());
					if (currentPosView != null)
						currentPosView.setBackgroundResource(
								R.drawable.list_drag_background);
					if (dragListener != null)
						dragListener.drag(dragCurPos, dragItemFrom, true);
				}
				/**/
				
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
				dragY = y;
				//if (!doTask) {
					if (y >= downHeight) {
						if (itemnum == getCount() - 1) {
							setSelectionFromTop(itemnum, downHeight);
							break;
						}
						downing = true;
						doTask = true;
						sendMessage();
					} else if (y <= itemHeight) {
						if (itemnum == 0) {
							setSelectionFromTop(itemnum, 0);
							break;
						}
						downing = false;
						doTask = true;
						sendMessage();
					} else {
						doTask = false;
					}
				//}
				break;
			}
			return true;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 拖曳时,被拖曳的文件的显示参数
	 * */
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
		// 在当前界面添加View, 被害人拖曳的图标
		mWindowManager = (WindowManager) context.getSystemService("window");
		mWindowManager.addView(v, mWindowParams);

		dragView = v;

		// 拖曳多个文件时, 在View里添加显示文件数目
		if (fileManager.multFile) {
			dragView.setTextSize(35);
			dragView.setTextColor(Color.RED);
			dragView.setGravity(Gravity.FILL_HORIZONTAL);
			dragView.setText("" + fileManager.getCurrentSelectedCount());
		}
		
		//dragView.setBackgroundResource(R.drawable.list_drag_background);
	}

	private void stopDrag() {
		doTask = false;
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

	public interface DragListener {
		/**
		 * Drag from {@code from} item to {@code to} item,
		 * 
		 * @param down
		 *            down action touch or not(move)
		 * */
		void drag(int from, int to, boolean down);
	}

	public interface DropListener {
		/**
		 * Drop from {@code from} item to {@code to} item,
		 * */
		void drop(int from, int to);
	}
	
	public interface DropOutListener {
		void dropOut(int from, int x, int y);
	}

	public interface StartDragListener {
		void startDrag(int from);
	}
}