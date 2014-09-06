package com.finke.pgtransit;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.finke.pgtransit.adapters.StopsAdapter;
import com.finke.pgtransit.extensions.Stackable;
import com.finke.pgtransit.model.Bus;
import com.finke.pgtransit.model.Stop;

/* Displays a list of stops for a bus route */
public class StopsFragment extends SherlockListFragment implements Stackable, 
	LoaderManager.LoaderCallbacks<List<Stop>> {
	
	// Preserves scroll position through StackController
	private int mScrollIndex;
	private int mScrollOffset;
	// Selected bus model, whose stops are being shown
	private Bus mBus;
	private StopsAdapter mAdapter;
	
	public StopsFragment() {
		mScrollIndex = 0;
		mScrollOffset = 0;
		mBus = null;
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
		
		setupActionBar();
		loadStops();
	}
	
	// StackController will tell Fragment to save state
	// Pseudo saves bus instance by storing its PK in the bundle
	public void saveState(Bundle state) {
		state.putInt("scrollIndex", getListView().getFirstVisiblePosition());
		state.putInt("scrollOffset", getListView().getChildAt(0) == null ? 0 : getListView().getChildAt(0).getTop());
		state.putInt("bus", mBus.getId());
	}
	
	// StackController will request state restores
	// Also restores the Bus instance having stored its PK
	public void restoreState(Bundle state) {
		mScrollIndex = state.getInt("scrollIndex");
		mScrollOffset = state.getInt("scrollOffset");
		try {
			mBus = Bus.fetchFromDatabase(state.getInt("bus"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Set bus model whose stops will be displayed when pushing fragment
	// Since using fragments, no intents used
	public void setBus(Bus bus) {
		mBus = bus;
	}
	
	private void setupActionBar() {
		ActionBar actionBar = ((SherlockFragmentActivity)getActivity()).getSupportActionBar();
		actionBar.setTitle(mBus.getOwner());
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
	
	/*@Override
	public void onCreateOptionsMenu(
	      Menu menu, MenuInflater inflater) {
	   inflater.inflate(R.menu.route_detail, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_detail_map:
			if(isGooglePlayServicesInstalled() && isGoogleMapsInstalled()) {
				Intent intent = new Intent(getActivity(), MapActivity.class);
				intent.putExtra("title", title);
				intent.putExtra("curRoute", curRoute);
				startActivity(intent);
				break;
			}
			else {
				Toast.makeText(getActivity(), "You must have both Google Play Services and Google Maps installed to use this feature.", Toast.LENGTH_LONG).show();
			}
		}
		return super.onOptionsItemSelected(item);
	}*/

	// Fetches from SQLite database in separate thread
	@Override
	public AsyncTaskLoader<List<Stop>> onCreateLoader(int arg0, Bundle arg1) {
		return new AsyncTaskLoader<List<Stop>>(getActivity()) {
			public List<Stop> loadInBackground() {
				try {
					return Stop.fetchFromDatabase(mBus.getId());
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
		getListView().setSelectionFromTop(mScrollIndex, mScrollOffset);
	}

	@Override
	public void onLoaderReset(Loader<List<Stop>> loader) {
		mAdapter.notifyDataSetInvalidated();
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
		((MainActivity)getActivity()).getStackController().push(frag);
	}

}
