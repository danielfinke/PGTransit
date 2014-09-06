package com.finke.pgtransit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.finke.pgtransit.adapters.RoutesAdapter;
import com.finke.pgtransit.extensions.Stackable;
import com.finke.pgtransit.model.Bus;
import com.finke.pgtransit.model.Stop;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

/* Displays Google Map with bus route lines and sidebar for route
 * selection
 */
public class MapFragment extends SherlockFragment implements
	Stackable, LoaderManager.LoaderCallbacks<List<Stop>>, OnItemClickListener {

	// Map defaults position to center of PG when no routes selected
	private final double DEFAULT_LAT = 53.9170641;
	private final double DEFAULT_LON = -122.7496693;
	
	// List of selected bus routes currently
	private boolean[] mRouteSel;
	// List of stops to display on map
	private ArrayList<Stop> mStops;
	// Corresponding list of markers actually rendered on map
	private ArrayList<Marker> mMarkers;
	private MapView mMapView;
	private GoogleMap mMap;
	// Time display for the stops when clicked
	private TimesDialogFragment mDialog;
	// Adapter for routes list in sidebar
	private RoutesAdapter mAdapter;
	
	public MapFragment() {
		mRouteSel = null;
		mStops = null;
		mMarkers = new ArrayList<Marker>();
		mMapView = null;
	}
	
	/* Sets up the initial Google Map */
	public void onCreate(Bundle state) {
		super.onCreate(state);
		mMapView = new MapView(getActivity());
		mMapView.onCreate(state);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
        // Inflate the layout for this fragment
		DrawerLayout v = (DrawerLayout)inflater.inflate(R.layout.map, container, false);
		RelativeLayout frame = (RelativeLayout)v.findViewById(R.id.mapFrame);
		// Add the map view
		frame.addView(mMapView, 0);
		
		// Set the drawer button handler
		Button drawerBtn = (Button)v.findViewById(R.id.showDrawerButton);
		drawerBtn.setOnClickListener(new OnClickListener() {
			// Pop out the sidebar
			@Override
			public void onClick(View v) {
				DrawerLayout drawer = (DrawerLayout)getActivity().findViewById(R.id.drawerLayout);
				drawer.openDrawer(Gravity.RIGHT);
			}
        });
		
		// Needs to call MapsInitializer before doing any CameraUpdateFactory calls
		MapsInitializer.initialize(getActivity());

		// Gets to GoogleMap from the MapView and does initialization stuff
		mMap = mMapView.getMap();
		mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(DEFAULT_LAT, DEFAULT_LON), 12));
		
		return v;
    }
	
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setupActionBar();
        loadRoutes();
	}
	
	public void onResume() {
		super.onResume();
		
		mMapView.onResume();
        
        // Show times of each stop
		mDialog = new TimesDialogFragment();
        mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker m) {
				try {
					mDialog.setStop(mStops.get(mMarkers.indexOf(m)));
					mDialog.show(getActivity().getSupportFragmentManager(), null);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			}
        });
	}
	
	public void onPause() {
		super.onPause();
		mMapView.onPause();
	}
	
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}
	
	public void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		mMapView.onSaveInstanceState(state);
	}
	
	public void onLowMemory() {
		super.onLowMemory();
		mMapView.onLowMemory();
	}
	
	public void saveState(Bundle state) {
		
	}
	
	public void restoreState(Bundle state) {
		
	}
	
	/* Loads various Bus Routes for sidebar population */
	private class RouteLoadTask extends AsyncTask<Void, Void, List<Bus>> {
		@Override
		protected List<Bus> doInBackground(Void... params) {
			try {
				return Bus.fetchFromDatabase();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(List<Bus> result) {
			// Check if it failed to load
			if(result != null) {
				mRouteSel = new boolean[result.size()];
				mAdapter.setItems(result);
				mAdapter.notifyDataSetChanged();
			}
		}
	}
	
	private void loadRoutes() {
		mAdapter = new RoutesAdapter(getActivity());
		ListView drawerList = (ListView)getActivity().findViewById(R.id.routesDrawer);
		drawerList.setAdapter(mAdapter);
		drawerList.setOnItemClickListener(this);
		
		// Prepare the loader.  Either re-connect with an existing one,
		// or start a new one.
		RouteLoadTask task = new RouteLoadTask();
		task.execute(new Void[] { });
	}
	
	private void loadStopMarkers() {
		getLoaderManager().restartLoader(0, null, this).forceLoad();
	}

	/* Draw KML data for each selected bus route */
	private void drawKMLData() {
		for(int i = 0; i < mRouteSel.length; i++) {
			if(mRouteSel[i]) {
				Bus b = mAdapter.getBus(i);
				// Fetch each set of line data per bus
				for(com.finke.pgtransit.model.Map m : b.getMaps()) {
					List<PolylineOptions> lines = m.getLines(getResources().getAssets());
					for(PolylineOptions line : lines) {
						line.color(b.getColor());
						mMap.addPolyline(line);
					}
				}
			}
		}
	}
	
	private void setupActionBar() {
		ActionBar actionBar = ((SherlockFragmentActivity)getActivity()).getSupportActionBar();
		actionBar.setTitle(R.string.mapTitle);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);
	}

	/* Fetches list of stops for each checked off sidebar route */
	@Override
	public AsyncTaskLoader<List<Stop>> onCreateLoader(int id, Bundle arg1) {
		return new AsyncTaskLoader<List<Stop>>(getActivity()) {
			public List<Stop> loadInBackground() {
				ArrayList<Integer> busIds = new ArrayList<Integer>();
				for(int i = 0; i < mRouteSel.length; i++) {
					// Only include route, if chosen
					if(mRouteSel[i]) {
						busIds.add(mAdapter.getBus(i).getId());
					}
				}
				try {
					return Stop.fetchFromDatabase(busIds);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		};
	}

	/* Renders content onto the map (for chosen bus routes) */
	@Override
	public void onLoadFinished(Loader<List<Stop>> loader, List<Stop> result) {
		// Restore default zoom if no routes now selected
		if(result.isEmpty()) {
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(DEFAULT_LAT, DEFAULT_LON), 12));
			return;
		}
		
		mStops = (ArrayList<Stop>)result;
		
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		HashMap<Integer, Integer> colorMap = new HashMap<Integer, Integer>();
		// Fetch hex colors for each route, link them to the bus PK
		for(int i = 0; i < mAdapter.getCount(); i++) {
			Bus b = mAdapter.getBus(i);
			colorMap.put(b.getId(), b.getColor());
		}
		// For each loaded stop, create a marker at its coordinates
		// and set its marker hex color
		for(Stop s : result) {
			LatLng pos = new LatLng(s.getLat(), s.getLon());
			builder.include(pos);
			int color = colorMap.get(s.getBusId());
			float[] hsv = new float[3];
			Color.RGBToHSV(
					Color.red(color),
					Color.green(color),
					Color.blue(color),
					hsv);
			mMarkers.add(mMap.addMarker(new MarkerOptions().position(pos).title(s.getName())
					.icon(BitmapDescriptorFactory.defaultMarker(hsv[0]))));
		}
		
		// Zoom the map to include all markers in bounds
		LatLngBounds bounds = builder.build();
		int padding = 100;
		final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
		mMap.animateCamera(cu);
	}

	@Override
	public void onLoaderReset(Loader<List<Stop>> arg0) {
		
	}

	/* Refreshes the map view (incl. stops, lines) */
	public void onItemClick(AdapterView<?> i, View v, int position, long id) {
		mRouteSel[position] = !mRouteSel[position];
		mMarkers.clear();
		mMap.clear();
		loadStopMarkers();
		drawKMLData();
	}
}
