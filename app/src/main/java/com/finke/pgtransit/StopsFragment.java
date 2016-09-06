package com.finke.pgtransit;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.finke.pgtransit.ChangeDayDialogFragment.ChangeDayDialogListener;
import com.finke.pgtransit.adapters.StopsAdapter;
import com.finke.pgtransit.extensions.Stackable;
import com.finke.pgtransit.model.Bus;
import com.finke.pgtransit.model.Stop;

/* Displays a list of stops for a bus route */
public class StopsFragment extends ListFragment implements Stackable,
	LoaderManager.LoaderCallbacks<List<Stop>>, ChangeDayDialogListener {
	
	// Preserves scroll position through StackController
	private int mScrollIndex;
	private int mScrollOffset;
	// True if content view has been created
	// Prevents saving scroll position if not yet ready
	private boolean mContentViewCreated;
	// Selected bus model, whose stops are being shown
	private Bus mBus;
	// The weekday for which arrival times are being shown
	private String mWeekday;
	private StopsAdapter mAdapter;
	// Instance of dialog for changing currently viewed weekday times
	private ChangeDayDialogFragment mChgDayDialog;
	
	public StopsFragment() {
		mScrollIndex = 0;
		mScrollOffset = 0;
		mBus = null;
		// On view, the current weekday/period is chosen
		mWeekday = Utils.getCurrentWeekday();
		mChgDayDialog = null;
	}
	
	public void onCreate(Bundle state) {
		super.onCreate(state);
		
		setHasOptionsMenu(true);
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_stops, container, false);
    }
	
	public void onViewCreated(View view, Bundle state) {
		super.onViewCreated(view, state);
		mContentViewCreated = true;
		
		setupActionBar();
		loadStops();
	}

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContentViewCreated = false;
    }

    // StackController will tell Fragment to save state
	// Pseudo saves bus instance by storing its PK in the bundle
	public void saveState(Bundle state) {
		int scrollIndex;
		int scrollOffset;
		if(mContentViewCreated) {
			scrollIndex = getListView().getFirstVisiblePosition();
			scrollOffset = getListView().getChildAt(0) == null ? 0 : getListView().getChildAt(0).getTop();
		}
		else {
			scrollIndex = mScrollIndex;
			scrollOffset = mScrollOffset;
		}
		state.putInt("scrollIndex", scrollIndex);
		state.putInt("scrollOffset", scrollOffset);
		state.putInt("bus", mBus.getId());
		state.putString("weekday", mWeekday);
	}
	
	// StackController will request state restores
	// Also restores the Bus instance having stored its PK
	public void restoreState(Bundle state) {
		mScrollIndex = state.getInt("scrollIndex");
		mScrollOffset = state.getInt("scrollOffset");
		mWeekday = state.getString("weekday");
		try {
			mBus = Bus.fetchFromDatabase(state.getInt("bus"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean onBackPressed() {
		return false;
	}
	
	// Set bus model whose stops will be displayed when pushing fragment
	// Since using fragments, no intents used
	public void setBus(Bus bus) {
		mBus = bus;
	}
	
	private void setupActionBar() {
		ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
		actionBar.setTitle(mBus.getNumber() + " " + mBus.getName() + " (" + Utils.getWeekdayString(mWeekday) + ")");
		actionBar.setDisplayHomeAsUpEnabled(true);
	}
	
	// Initiate stop locn loading from SQLite db
	private void loadStops() {
		mAdapter = new StopsAdapter(getActivity());
		setListAdapter(mAdapter);
		
		// Prepare the loader.  Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().restartLoader(0, null, this).forceLoad();
	}
	
	/* No longer used since maps have been moved to their own tab */
	/*public boolean isGooglePlayServicesInstalled() {
		return GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity()) ==
    		  ConnectionResult.SUCCESS;
	}
	
	public boolean isGoogleMapsInstalled() {
	    try
	    {
	        getActivity().getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0 );
	        return true;
	    } 
	    catch(PackageManager.NameNotFoundException e)
	    {
	        return false;
	    }
	}*/
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	   inflater.inflate(R.menu.menu_times, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Opens the dialog for changing viewed day of week
		case R.id.changeDayMenuItem:
			mChgDayDialog = new ChangeDayDialogFragment();
			mChgDayDialog.setWeekday(mWeekday);
			// Makes the dialog handler this
			mChgDayDialog.setListener(this);
			mChgDayDialog.show(getFragmentManager(), null);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// Fetches from SQLite database in separate thread
	@Override
	public AsyncTaskLoader<List<Stop>> onCreateLoader(int arg0, Bundle arg1) {
		return new AsyncTaskLoader<List<Stop>>(getActivity()) {
			public List<Stop> loadInBackground() {
				try {
					return Stop.fetchFromDatabase(mBus.getId(), mWeekday);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		};
	}

	// Updates the ListView after database has returned results
	@Override
	public void onLoadFinished(Loader<List<Stop>> loader, List<Stop> result) {
		if(result != null) {
			mAdapter.setItems(result);
			mAdapter.notifyDataSetChanged();
		}
		// Restores scroll position if state was restored
		// and had triggered a data load
		ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
		actionBar.setTitle(mBus.getNumber() + " " + mBus.getName() + " (" + Utils.getWeekdayString(mWeekday) + ")");
		getListView().setSelectionFromTop(mScrollIndex, mScrollOffset);
	}

	@Override
	public void onLoaderReset(Loader<List<Stop>> loader) {
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
	
	public void onListItemClick(ListView i, View v, int position, long id) {
		// Display big-ass interstitial ad if it is ready and not disabled
		if(((MainActivity)getActivity()).adsEnabled()) {
			((MainActivity)getActivity()).getAdManager().displayInterstitialIfReady();
		}
		
		// Behind ad (if applicable), push the times fragment for the
		// chosen stop
		TimesFragment frag = new TimesFragment();
		frag.setStop(mAdapter.getStop(position));
		frag.setWeekday(mWeekday);
		((MainActivity)getActivity()).getStackController().push(frag);
	}

}
