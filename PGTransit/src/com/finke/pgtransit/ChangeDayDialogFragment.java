package com.finke.pgtransit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.actionbarsherlock.app.SherlockDialogFragment;

/* Provides the option to change which time frame the
 * app is displaying the bus schedules for
 */
public class ChangeDayDialogFragment extends SherlockDialogFragment {
	
	private String mWeekday;
    // Use this instance of the interface to deliver action events
    ChangeDayDialogListener mListener;
    
    public ChangeDayDialogFragment() {
    	mWeekday = null;
    	mListener = null;
    }
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      // Use the Builder class for convenient dialog construction

      AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
      // Dialog offers each weekend and weekdays as general option
      builder.setItems(new String[] { "Weekdays", "Saturday", "Sunday" },
    		new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch(which) {
					case 0:
						mWeekday = "Weekday";
						break;
					case 1:
						mWeekday = "Saturday";
						break;
					case 2:
						mWeekday = "Sunday";
						break;
					}
					// Listener is expected to be the TimesFragment for
					// the selected route
		            mListener.onDialogPositiveClick(ChangeDayDialogFragment.this);
				}
    	  
      });
      builder.setTitle(R.string.menuChangeDayTitle);
      // Create the AlertDialog object and return it
      return builder.create();
	}
	
	// Set the current weekday so the dialog selects it by default
	public void setWeekday(String selectedDay) {
		mWeekday = selectedDay;
	}
	// Make this dialog aware of its listener
	public void setListener(ChangeDayDialogListener listener) {
		mListener = listener;
	}
	
	public String getWeekday() { return mWeekday; }
	
	/* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface ChangeDayDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
}
