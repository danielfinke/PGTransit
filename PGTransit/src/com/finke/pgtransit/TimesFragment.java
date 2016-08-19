package com.finke.pgtransit;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.finke.pgtransit.ChangeDayDialogFragment.ChangeDayDialogListener;
import com.finke.pgtransit.adapters.TimesAdapter;
import com.finke.pgtransit.extensions.Stackable;
import com.finke.pgtransit.model.Stop;
import com.finke.pgtransit.model.TimeInterface;
import com.finke.pgtransit.model.TimeSlot;

/* Displays a list of times given a bus stop location, as well as
 * any important notes on the time slot
 */
public class TimesFragment extends SherlockListFragment
	implements Stackable, LoaderManager.LoaderCallbacks<List<TimeInterface>>,
	ChangeDayDialogListener {
	
	// Preserves scroll position through StackController
	private int mScrollIndex;
	private int mScrollOffset;
	// The weekday for which arrival times are being shown
	private String mWeekday;
	// Selected stop model, whose visit times are being shown
	private Stop mStop;
	private TimesAdapter mAdapter;
	// Instance of dialog for changing currently viewed weekday times
	private ChangeDayDialogFragment mChgDayDialog;
	
	public TimesFragment() {
		mScrollIndex = 0;
		mScrollOffset = 0;
		// On view, the current weekday/period is chosen
		mWeekday = Utils.getCurrentWeekday();
		mStop = null;
		mChgDayDialog = null;
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_time, container, false);
    }
	
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		loadTimes();
	}
	
	public void onResume() {
		super.onResume();
		
		setupActionBar();
	}
	
	// StackController will tell Fragment to save state
	// String weekday value is preserved
	// Pseudo saves stop instance by storing its PK in the bundle
	public void saveState(Bundle state) {
		state.putInt("scrollIndex", getListView().getFirstVisiblePosition());
		state.putInt("scrollOffset", getListView().getChildAt(0) == null ? 0 : getListView().getChildAt(0).getTop());
		state.putString("weekday", mWeekday);
		state.putString("stop", mStop.getId());
	}
	
	// StackController will request state restores
	// Also restores the Stop instance having stored its PK
	public void restoreState(Bundle state) {
		mScrollIndex = state.getInt("scrollIndex");
		mScrollOffset = state.getInt("scrollOffset");
		mWeekday = state.getString("weekday");
		try {
			mStop = Stop.fetchFromDatabase(state.getString("stop"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean onBackPressed() {
		return false;
	}
	
	public void setStop(Stop s) { mStop = s; }
	public void setWeekday(String weekday) { mWeekday = weekday; }
	
	private void setupActionBar() {
		ActionBar actionBar = ((SherlockFragmentActivity)getActivity()).getSupportActionBar();
		actionBar.setTitle(Utils.getWeekdayString(mWeekday) + " Schedule");
		actionBar.setDisplayHomeAsUpEnabled(true);
	}
	
	// Initiate time slot loading from SQLite db
	private void loadTimes() {
//		// Create an empty adapter we will use to display the loaded data.
//		// We pass null for the cursor, then update it in onLoadFinished()
		mAdapter = new TimesAdapter(getActivity());
		setListAdapter(mAdapter);
		
		// Prepare the loader.  Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().restartLoader(0, null, this).forceLoad();
	}
	
//	@Override
//	public void onCreateOptionsMenu(
//	      Menu menu, MenuInflater inflater) {
//	   inflater.inflate(R.menu.menu_times, menu);
//	}
//	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		// Opens the dialog for changing viewed day of week
//		case R.id.changeDayMenuItem:
//			mChgDayDialog = new ChangeDayDialogFragment();
//			mChgDayDialog.setWeekday(mWeekday);
//			// Makes the dialog handler this
//			mChgDayDialog.setListener(this);
//			mChgDayDialog.show(getFragmentManager(), null);
//			break;
//		}
//		return super.onOptionsItemSelected(item);
//	}

	// Fetches from SQLite database in separate thread
	@Override
	public AsyncTaskLoader<List<TimeInterface>> onCreateLoader(int arg0, Bundle arg1) {
		return new AsyncTaskLoader<List<TimeInterface>>(getActivity()) {
			public List<TimeInterface> loadInBackground() {
				try {
					return TimeSlot.fetchFromDatabase(mStop.getId());
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
		if(result != null) {
			mAdapter.setTimes(result);
			mAdapter.notifyDataSetChanged();
		}
		// Restores scroll position if state was restored
		// and had triggered a data load, also updating
		// the title of the activity (by day)
		ActionBar actionBar = ((SherlockFragmentActivity)getActivity()).getSupportActionBar();
		actionBar.setTitle(mStop.getBus().getNumber() + " at " +
				mStop.getName() + " (" + Utils.getWeekdayString(mWeekday) + ")");
		getListView().setSelectionFromTop(mScrollIndex, mScrollOffset);
	}

	@Override
	public void onLoaderReset(Loader<List<TimeInterface>> arg0) {
		mAdapter.notifyDataSetInvalidated();
	}

	// Changes the viewed day, so time slots are fetched for another
	public void onDialogPositiveClick(DialogFragment dialog) {
		mWeekday = ((ChangeDayDialogFragment)dialog).getWeekday();
		getLoaderManager().restartLoader(0, null, this).forceLoad();
	}

	public void onDialogNegativeClick(DialogFragment dialog) {
		// Do nothing
	}
}
