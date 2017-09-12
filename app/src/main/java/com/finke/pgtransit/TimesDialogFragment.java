package com.finke.pgtransit;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Loader;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.finke.pgtransit.adapters.TimesAdapter;
import com.finke.pgtransit.model.Bus;
import com.finke.pgtransit.model.MinorStop;
import com.finke.pgtransit.model.TimeInterface;

/* Displays a list of times given a bus stop location, as well as
 * any important notes on the time slot, in a Dialog
 */
public class TimesDialogFragment extends DialogFragment implements
	LoaderManager.LoaderCallbacks<List<TimeInterface>>, OnShowListener {
	
	// View id for loading indicator in dialog
	private static final int PLACEHOLDER_ID = 343;

	// The weekday for which arrival times are being shown
	private String mWeekday;
	private Bus mBus;
	// Selected minor stop model, whose visit times are being shown
	private MinorStop mMinorStop;
	// The dialog that will display this fragment's content
	private Dialog mDialog;
	private TimesAdapter mAdapter;
	
	public TimesDialogFragment() {
		mWeekday = null;
		mBus = null;
		mMinorStop = null;
		mDialog = null;
		mAdapter = null;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Weekday is automatically set to current weekday
		mWeekday = Utils.getCurrentWeekday();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        // Progress bar shows that the times are being loaded in the
        // background
        ProgressBar loading = new ProgressBar(getActivity());
        loading.setId(PLACEHOLDER_ID);
        builder.setView(loading);
        
        builder.setTitle(mMinorStop.getName());
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
	
	public void setBus(Bus s) { mBus = s; }
	public void setMinorStop(MinorStop ms) { mMinorStop = ms; }

	// Fetches from SQLite database in separate thread
	@Override
	public AsyncTaskLoader<List<TimeInterface>> onCreateLoader(int arg0, Bundle arg1) {
		return new AsyncTaskLoader<List<TimeInterface>>(getActivity()) {
			public List<TimeInterface> loadInBackground() {
				try {
					return mMinorStop.getMinorStopTimes(mWeekday, mBus.getId());
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		};
	}

	// Updates the ListView after database has returned results
	@Override
	public void onLoadFinished(Loader<List<TimeInterface>> loader, List<TimeInterface> result) {
		ProgressBar loading = (ProgressBar)mDialog.findViewById(PLACEHOLDER_ID);
		ViewGroup parent = (ViewGroup)loading.getParent();
		parent.removeView(loading);
		
		if(result != null) {
			ListView list = new ListView(getActivity());
			parent.addView(list);
			mAdapter.setTimes(result);
			list.setAdapter(mAdapter);
		}
	}

	@Override
	public void onLoaderReset(Loader<List<TimeInterface>> arg0) {
		mAdapter.notifyDataSetInvalidated();
	}
	
}
