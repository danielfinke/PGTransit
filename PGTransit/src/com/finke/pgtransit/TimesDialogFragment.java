package com.finke.pgtransit;

import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.finke.pgtransit.adapters.TimesAdapter;
import com.finke.pgtransit.model.Stop;
import com.finke.pgtransit.model.TimeSlot;

/* Displays a list of times given a bus stop location, as well as
 * any important notes on the time slot, in a Dialog
 */
public class TimesDialogFragment extends SherlockDialogFragment implements
	LoaderManager.LoaderCallbacks<List<TimeSlot>>, OnShowListener {
	
	// View id for loading indicator in dialog
	private static final int PLACEHOLDER_ID = 343;

	// The weekday for which arrival times are being shown
	private String mWeekday;
	// Selected stop model, whose visit times are being shown
	private Stop mStop;
	// The dialog that will display this fragment's content
	private Dialog mDialog;
	private TimesAdapter mAdapter;
	
	public TimesDialogFragment() {
		mWeekday = null;
		mStop = null;
		mDialog = null;
		mAdapter = null;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Weekday is automatically set to current weekday
		mWeekday = getWeekday();

        AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
        
        // Progress bar shows that the times are being loaded in the
        // background
        ProgressBar loading = new ProgressBar(getActivity());
        loading.setId(PLACEHOLDER_ID);
        builder.setView(loading);
        
        builder.setTitle(mStop.getName());
        builder.setNeutralButton("Dismiss", new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.cancel();
			}
        });
        
        mAdapter = new TimesAdapter(getActivity());

        // Create the AlertDialog object and return it
        // Its handler is this fragment
        mDialog = builder.create();
        mDialog.setOnShowListener(this);
        return mDialog;
    }

	// Showing dialog triggers load of time slot data to populate ListView
	@Override
	public void onShow(DialogInterface dialog) {
		getLoaderManager().restartLoader(0, null, this).forceLoad();
	}
	
	public void setStop(Stop s) { mStop = s; }
	
	/* Gets current weekday option based on current Calendar day of week
	 * All weekdays are bundled together since schedule is consistent
	 */
	private static String getWeekday() {
		int weekdayNo = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		String weekday;
		
		if(weekdayNo == Calendar.SATURDAY) {
			weekday = "saturday";
		}
		else if(weekdayNo == Calendar.SUNDAY) {
			weekday = "sunday";
		}
		else {
			weekday = "weekdays";
		}
		
		return weekday;
	}

	// Fetches from SQLite database in separate thread
	@Override
	public AsyncTaskLoader<List<TimeSlot>> onCreateLoader(int arg0, Bundle arg1) {
		return new AsyncTaskLoader<List<TimeSlot>>(getActivity()) {
			public List<TimeSlot> loadInBackground() {
				try {
					return TimeSlot.fetchFromDatabase(mStop.getId(), mWeekday);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		};
	}

	// Updates the ListView after database has returned results
	@Override
	public void onLoadFinished(Loader<List<TimeSlot>> loader, List<TimeSlot> result) {
		ProgressBar loading = (ProgressBar)mDialog.findViewById(PLACEHOLDER_ID);
		ViewGroup parent = (ViewGroup)loading.getParent();
		parent.removeView(loading);
		
		if(result != null) {
			ListView list = new ListView(getActivity());
			parent.addView(list);
			mAdapter.setSlots(result);
			list.setAdapter(mAdapter);
		}
	}

	@Override
	public void onLoaderReset(Loader<List<TimeSlot>> arg0) {
		
	}
	
}
