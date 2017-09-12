package com.finke.pgtransit;

import java.util.List;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.finke.pgtransit.adapters.TimesAdapter;
import com.finke.pgtransit.loader.BusLoader;
import com.finke.pgtransit.loader.StopLoader;
import com.finke.pgtransit.model.Bus;
import com.finke.pgtransit.model.Stop;
import com.finke.pgtransit.model.TimeInterface;
import com.finke.pgtransit.model.TimeSlot;

/* Displays a list of times given a bus stop location, as well as
 * any important notes on the time slot
 */
public class TimesFragment extends ListFragment implements
        BusLoader.Callbacks,
        StopLoader.Callbacks,
        LoaderManager.LoaderCallbacks<List<TimeInterface>> {
    private static final String BUS_ID_KEY = "busId";
    private static final String STOP_ID_KEY = "stopId";
    private static final String WEEKDAY_KEY = "weekday";

	// Selected bus, whose stop is being displayed
	private int mBusId;
	private Bus mBus;
	// Selected stop model, whose visit times are being shown
	private String mStopId;
	private Stop mStop;
	// The weekday for which arrival times are being shown
	private String mWeekday;
	private TimesAdapter mAdapter;
	
	public TimesFragment() {
		// On view, the current weekday/period is chosen
		mWeekday = Utils.getCurrentWeekday();
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(savedInstanceState != null) {
			mBusId = savedInstanceState.getInt(BUS_ID_KEY);
			mStopId = savedInstanceState.getString(STOP_ID_KEY);
			mWeekday = savedInstanceState.getString(WEEKDAY_KEY);
		}
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_time, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupActionBar();
        loadTimes();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(BUS_ID_KEY, mBusId);
        outState.putString(STOP_ID_KEY, mStopId);
        outState.putString(WEEKDAY_KEY, mWeekday);
    }

    public void setBusId(int busId) {
        mBusId = busId;
    }

    public void setStopId(String stopId) {
        mStopId = stopId;
    }

    public void setWeekday(String weekday) {
        mWeekday = weekday;
    }

    private void setupActionBar() {
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();

        if(actionBar != null) {
            if(mBus == null) {
                new BusLoader(this).execute(mBusId);
            }
            else if(mStop != null) {
                actionBar.setTitle(mBus.getNumber() + " " + mBus.getName() + " (" +
                        Utils.getWeekdayString(mWeekday) + ")");
                actionBar.setSubtitle(mStop.getName());
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

    @Override
    public void onBusLoaded(Bus bus) {
        mBus = bus;
        new StopLoader(this).execute(mStopId);
    }

    @Override
    public void onStopLoaded(Stop stop) {
        mStop = stop;
        setupActionBar();
    }

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
					return TimeSlot.fetchFromDatabase(mStopId);
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
}
