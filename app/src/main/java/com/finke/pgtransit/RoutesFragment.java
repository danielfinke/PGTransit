package com.finke.pgtransit;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.finke.pgtransit.adapters.RoutesAdapter;
import com.finke.pgtransit.extensions.PagerActivityListener;
import com.finke.pgtransit.model.Bus;

/* Displays a list of bus routes */
public class RoutesFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<AsyncTaskResult<List<Bus>>>,
        PagerActivityListener {
    // This is the Adapter being used to display the list's data
    private RoutesAdapter mAdapter;
    
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupActionBar();
        loadRoutes();
    }

    // Initiate bus route loading from SQLite db
    private void loadRoutes() {
        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        mAdapter = new RoutesAdapter(getActivity());
        setListAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, this);
    }

    private void setupActionBar() {
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();

        if(actionBar != null) {
            actionBar.setTitle(R.string.app_name);
            actionBar.setSubtitle("");
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(((MainActivity) getActivity()).getMenuEnabled()) {
            inflater.inflate(R.menu.main_menu, menu);
        }
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
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        if(keyCode == KeyEvent.KEYCODE_MENU) {
//            View v = getActivity().findViewById(R.id.overflowMenu);
//            v.performClick();
//            return true;
//        }
//        return false;
//    }
    
    // Called when a new Loader needs to be created
    public AsyncTaskLoader<AsyncTaskResult<List<Bus>>> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new AsyncTaskLoader<AsyncTaskResult<List<Bus>>>(getActivity()) {
            @Override
            protected void onStartLoading() {
                forceLoad();
            }

            public AsyncTaskResult<List<Bus>> loadInBackground() {
                try {
                    return new AsyncTaskResult<>(Bus.fetchFromDatabase());
                } catch (Exception e) {
                    e.printStackTrace();
                    AsyncTaskResult<List<Bus>> result = new AsyncTaskResult<>(null);
                    result.setException(e);
                    return result;
                }
            }
        };
    }
    
    // Called when a previously created loader has finished loading
    public void onLoadFinished(Loader<AsyncTaskResult<List<Bus>>> loader, AsyncTaskResult<List<Bus>> result) {
        if(!result.hasException()) {
            mAdapter.setItems(result.getData());
            mAdapter.notifyDataSetChanged();
        }
        else {
            getLoaderManager().restartLoader(loader.getId(), null, this);
        }
    }

    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<AsyncTaskResult<List<Bus>>> loader) {
        mAdapter.notifyDataSetInvalidated();
    }
    
    // Choosing a bus route takes you to the stops for that route
    public void onListItemClick(ListView i, View v, int position, long id) {
        StopsFragment frag = new StopsFragment();
        frag.setBusId((int)id);
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
