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
import android.view.View;
import android.view.ViewGroup;

import com.finke.pgtransit.ChangeDayDialogFragment.ChangeDayDialogListener;
import com.finke.pgtransit.adapters.TimesAdapter;
import com.finke.pgtransit.model.Stop;
import com.finke.pgtransit.model.TimeInterface;
import com.finke.pgtransit.model.TimeSlot;

/* Displays a list of times given a bus stop location, as well as
 * any important notes on the time slot
 */
public class TimesFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<List<TimeInterface>>, ChangeDayDialogListener {
    private static final String STOP_ID_KEY = "stopId";
    private static final String WEEKDAY_KEY = "weekday";

	// The weekday for which arrival times are being shown
	private String mWeekday;
	// Selected stop model, whose visit times are being shown
    private int mStopId;
	private Stop mStop;
	private TimesAdapter mAdapter;
	// Instance of dialog for changing currently viewed weekday times
	private ChangeDayDialogFragment mChgDayDialog;
	
	public TimesFragment() {
		// On view, the current weekday/period is chosen
		mWeekday = Utils.getCurrentWeekday();
		mStop = null;
		mChgDayDialog = null;
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(savedInstanceState != null) {
			mStopId = savedInstanceState.getInt(STOP_ID_KEY);
			mWeekday = savedInstanceState.getString(WEEKDAY_KEY);
		}
		
		setHasOptionsMenu(true);
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_time, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadTimes();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(STOP_ID_KEY, mStopId);
        outState.putString(WEEKDAY_KEY, mWeekday);
    }
	
	public void setStop(Stop s) { mStop = s; }
	public void setWeekday(String weekday) { mWeekday = weekday; }

    private void setupActionBar() {
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();

        if(actionBar != null) {
            if(mBus == null) {
                new StopsFragment.BusLoader().execute(mBusId);
            }
            else {
                actionBar.setTitle(mBus.getNumber() + " " + mBus.getName() + " (" +
                        Utils.getWeekdayString(mWeekday) + ")");
            }

            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
	
	// Initiate time slot loading from SQLite db
	private void loadTimes() {
		mAdapter = new TimesAdapter(getActivity());
		setListAdapter(mAdapter);

		getLoaderManager().initLoader(0, null, this);
	}

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
            @Override
            protected void onStartLoading() {
                forceLoad();
            }

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
	}

	@Override
	public void onLoaderReset(Loader<List<TimeInterface>> arg0) {
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
}
