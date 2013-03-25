package com.android.xjf.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class XDialog {
	
		
	/**
	 * Create a Input Dialog AlertDialog.Builder, what and eidt is create by you.
	 * So you can get the edit text.
	 * @param
	 * 		what above eidt, show messgae.
	 * @param
	 * 		edit input box.
	 * */
	public static AlertDialog.Builder createInputDialog(Context context, TextView what, 
			EditText edit){
		LinearLayout layout = new LinearLayout(context);
		layout.setLayoutParams(new LinearLayout.
        		LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 0, 30, 10);
        if (edit == null)
        	edit = new EditText(context);
        edit.setSingleLine();
        if (what != null)
        	layout.addView(what, new LinearLayout.
        		LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        layout.addView(edit, new LinearLayout.
        		LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        
		AlertDialog.Builder dialog = new AlertDialog.Builder(context).setView(layout);
		return dialog;
	}
	

	public static AlertDialog inputDialog(Context context, 
			String title, String message, 
			String edits, String hint,
			String ok, String cancel, 
			final InputClick clickListener){
		LinearLayout layout = new LinearLayout(context);
		layout.setLayoutParams(new LinearLayout.
        		LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 0, 30, 10);
        layout.setMinimumWidth(200);
        final EditText edit = new EditText(context);
        edit.setSingleLine();
        if (message != null) {
        	TextView tv = new TextView(context);
        	tv.setText(message);
        	layout.addView(tv, new LinearLayout.
        		LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        }
        layout.addView(edit, new LinearLayout.
        		LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        if (edits != null) {
        	edit.setText(edits);
        } else if (hint != null) {
        	edit.setHint(hint);
        }

        
		AlertDialog.Builder dialog = new AlertDialog.Builder(context).setView(layout);
        if (title != null)
        	dialog.setTitle(title);
		AlertDialog.OnClickListener listener = 
			new AlertDialog.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if (clickListener != null)
						clickListener.onClickListener(edit.getText().toString(), which);
				}
				
			};
		dialog.setCancelable(false);
		if (ok != null )
			dialog.setPositiveButton(ok, listener);
		if (cancel != null)
			dialog.setNegativeButton(cancel, listener);
		return dialog.create();
	}	
	public static AlertDialog.Builder createListDialog(Context context){
		return  (new AlertDialog.Builder(context));
	}
	
	public static AlertDialog.Builder createSingalListDialog(Context context){
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		/**
		 * new AlertDialog.Builder(AlertDialogSamples.this)
                .setIcon(R.drawable.ic_popup_reminder)
                .setTitle(R.string.alert_dialog_multi_choice)
                .setMultiChoiceItems(R.array.select_dialog_items3,
                        new boolean[]{false, true, false, true, false, false, false},
                        new DialogInterface.OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton,
                                    boolean isChecked) {

                                /* User clicked on a check box do some stuff 
                            }
                        })
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked Yes so do some stuff 
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked No so do some stuff 
                    }
                })
		 * */
		return dialog;
	}

	public interface InputClick {
		public void onClickListener(String str, int which);
	}
}
