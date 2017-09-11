package com.finke.pgtransit;

import java.util.List;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
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
import com.finke.pgtransit.extensions.PagerActivityListener;
import com.finke.pgtransit.loader.BusLoader;
import com.finke.pgtransit.model.Bus;
import com.finke.pgtransit.model.Stop;

/* Displays a list of stops for a bus route */
public class StopsFragment extends ListFragment implements
        BusLoader.Callbacks,
        LoaderManager.LoaderCallbacks<List<Stop>>,
        PagerActivityListener,
        ChangeDayDialogListener {
	private static final String BUS_ID_KEY = "busId";
	private static final String WEEKDAY_KEY = "weekday";

	private int mBusId;
    private Bus mBus;
	// The weekday for which arrival times are being shown
	private String mWeekday;
	private StopsAdapter mAdapter;
	// Instance of dialog for changing currently viewed weekday times
	private ChangeDayDialogFragment mChgDayDialog;
	
	public StopsFragment() {
		// On view, the current weekday/period is chosen
		mWeekday = Utils.getCurrentWeekday();
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(savedInstanceState != null) {
			mBusId = savedInstanceState.getInt(BUS_ID_KEY);
			mWeekday = savedInstanceState.getString(WEEKDAY_KEY);
		}
		
		setHasOptionsMenu(true);
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_stops, container, false);
    }

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setupActionBar();
		loadStops();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(BUS_ID_KEY, mBusId);
		outState.putString(WEEKDAY_KEY, mWeekday);
	}

	/**
	 *
	 * @param busId
	 */
	public void setBusId(int busId) {
		mBusId = busId;
	}
	
	private void setupActionBar() {
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();

        if(actionBar != null) {
            actionBar.setSubtitle("");

            if(mBus == null) {
                new BusLoader(this).execute(mBusId);
            }
            else {
                actionBar.setTitle(mBus.getNumber() + " " + mBus.getName() + " (" +
                        Utils.getWeekdayString(mWeekday) + ")");
            }

            actionBar.setDisplayHomeAsUpEnabled(true);
        }
	}
	
	// Initiate stop locn loading from SQLite db
	private void loadStops() {
		mAdapter = new StopsAdapter(getActivity());
		setListAdapter(mAdapter);

		getLoaderManager().initLoader(0, null, this);
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(((MainActivity) getActivity()).getMenuEnabled()) {
            inflater.inflate(R.menu.menu_times, menu);
        }
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

	@Override
	public void onBusLoaded(Bus bus) {
		mBus = bus;
		setupActionBar();
	}

	// Fetches from SQLite database in separate thread
	@Override
	public AsyncTaskLoader<List<Stop>> onCreateLoader(int arg0, Bundle arg1) {
		return new AsyncTaskLoader<List<Stop>>(getActivity()) {
			@Override
			protected void onStartLoading() {
				forceLoad();
			}

			public List<Stop> loadInBackground() {
				try {
					return Stop.fetchFromDatabase(mBusId, mWeekday);
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
	}

	@Override
	public void onLoaderReset(Loader<List<Stop>> loader) {
		mAdapter.notifyDataSetInvalidated();
	}

	// Changes the viewed day, so time slots are fetched for another
	public void onDialogPositiveClick(DialogFragment dialog) {
		mWeekday = ((ChangeDayDialogFragment)dialog).getWeekday();
		getLoaderManager().restartLoader(0, null, this);
        setupActionBar();
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
		frag.setBusId(mBusId);
		frag.setStopId(mAdapter.getStop(position).getId());
		frag.setWeekday(mWeekday);
        ((MainActivity)getActivity()).pushFragment(frag, 0);
	}

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onTabSelected() {
        setupActionBar();
        ((MainActivity) getActivity()).setMenuEnabled(true);
    }
}
