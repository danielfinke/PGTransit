package com.finke.pgtransit;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.finke.pgtransit.adapters.RoutesAdapter;
import com.finke.pgtransit.extensions.Stackable;
import com.finke.pgtransit.model.Bus;

/* Displays a list of bus routes */
public class RoutesFragment extends SherlockListFragment
	implements Stackable, LoaderManager.LoaderCallbacks<List<Bus>> {
	
	// Preserves scroll position through StackController
	private int mScrollIndex;
	private int mScrollOffset;
	
	// This is the Adapter being used to display the list's data
	private RoutesAdapter mAdapter;
	
	public RoutesFragment() {
		mScrollIndex = 0;
		mScrollOffset = 0;
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.list_routes, container, false);
    }
	
	public void onViewCreated(View view, Bundle state) {
		super.onViewCreated(view, state);

		setupActionBar();
		loadRoutes();
	}
	
	// StackController will tell Fragment to save state
	public void saveState(Bundle state) {
		state.putInt("scrollIndex", getListView().getFirstVisiblePosition());
		state.putInt("scrollOffset", getListView().getChildAt(0) == null ? 0 : getListView().getChildAt(0).getTop());
	}
	
	// StackController will request state restores
	public void restoreState(Bundle state) {
		mScrollIndex = state.getInt("scrollIndex");
		mScrollOffset = state.getInt("scrollOffset");
	}
	
	private void setupActionBar() {
		ActionBar actionBar = ((SherlockFragmentActivity)getActivity()).getSupportActionBar();
		actionBar.setTitle(R.string.routeListTitle);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);
	}
	
	// Initiate bus route loading from SQLite db
	private void loadRoutes() {
		// Create an empty adapter we will use to display the loaded data.
		// We pass null for the cursor, then update it in onLoadFinished()
		mAdapter = new RoutesAdapter(getActivity());
		setListAdapter(mAdapter);
		
		// Prepare the loader.  Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().restartLoader(0, null, this).forceLoad();
	}
	
	@Override
	public void onCreateOptionsMenu(
	      Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main_menu, menu);
	}
	
	/* Handle menu option interactions */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		switch (item.getItemId()) {
		case R.id.viewBasicMap:
			i = new Intent(getActivity(), MapImageActivity.class);
			startActivity(i);
			break;
		case R.id.viewMore:
			i = new Intent(getActivity(), MoreActivity.class);
			startActivity(i);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/* Activity delegates onKeyUp(...) to this fragment to
	 * allow hardware menu button to open 3dot menu
	 */
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_MENU) {
			View v = getActivity().findViewById(R.id.menuDisclosure);
			v.performClick();
			return true;
		}
		return false;
	}
	
	// Called when a new Loader needs to be created
	public AsyncTaskLoader<List<Bus>> onCreateLoader(int id, Bundle args) {
		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		return new AsyncTaskLoader<List<Bus>>(getActivity()) {
			public List<Bus> loadInBackground() {
				try {
					return Bus.fetchFromDatabase();
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		};
	}
	
	// Called when a previously created loader has finished loading
	public void onLoadFinished(Loader<List<Bus>> loader, List<Bus> result) {
		if(result != null) {
			mAdapter.setItems(result);
			mAdapter.notifyDataSetChanged();
		}
		getListView().setSelectionFromTop(mScrollIndex, mScrollOffset);
	}
	
	// Called when a previously created loader is reset, making the data unavailable
	public void onLoaderReset(Loader<List<Bus>> loader) {
		mAdapter.notifyDataSetInvalidated();
	}
	
	// Choosing a bus route takes you to the stops for that route
	public void onListItemClick(ListView i, View v, int position, long id) {
		StopsFragment frag = new StopsFragment();
		frag.setBus(mAdapter.getBus(position));
		((MainActivity)getActivity()).getStackController().push(frag);
	}
}
