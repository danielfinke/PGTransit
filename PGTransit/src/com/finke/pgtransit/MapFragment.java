package com.finke.pgtransit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.transition.Visibility;
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
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.finke.pgtransit.adapters.RoutesAdapter;
import com.finke.pgtransit.extensions.Stackable;
import com.finke.pgtransit.model.Bus;
import com.finke.pgtransit.model.MapPoint;
import com.finke.pgtransit.model.MinorStop;
import com.finke.pgtransit.model.MinorStopTime;
import com.finke.pgtransit.model.Trip;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

/* Displays Google Map with bus route lines and sidebar for route
 * selection
 */
public class MapFragment extends SherlockFragment implements
	Stackable, LoaderManager.LoaderCallbacks<Void>, OnItemClickListener {

	// Map defaults position to center of PG when no routes selected
	private final double DEFAULT_LAT = 53.9170641;
	private final double DEFAULT_LON = -122.7496693;
	
	// List of selected bus routes currently
	private boolean[] mRouteSel;
	private boolean[] mRouteShowing;
	private ArrayList<Bus> mBusses;
	private ArrayList<ArrayList<MinorStop>> mMinorStops;
	private ArrayList<ArrayList<MinorStopTime>> mMinorStopTimes;
	private ArrayList<ArrayList<Marker>> mMarkers;
	private ArrayList<Polyline> mPolylines;
	private MapView mMapView;
	private GoogleMap mMap;
	// Time display for the stops when clicked
	private TimesDialogFragment mDialog;
	// Adapter for routes list in sidebar
	private RoutesAdapter mAdapter;
	private DrawerLayout mDrawer;
	private String mWeekday;
	
	public MapFragment() {
		mBusses = null;
		mMinorStops = null;
		mMinorStopTimes = null;
		mMarkers = null;
		mMapView = null;
		mDrawer = null;
		mWeekday = Utils.getCurrentWeekday();
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
		mDrawer = (DrawerLayout)v.findViewById(R.id.drawerLayout);
		Button drawerBtn = (Button)v.findViewById(R.id.showDrawerButton);
		drawerBtn.setOnClickListener(new OnClickListener() {
			// Pop out the sidebar
			@Override
			public void onClick(View v) {
				mDrawer.openDrawer(Gravity.RIGHT);
			}
        });
		
		// Needs to call MapsInitializer before doing any CameraUpdateFactory calls
		MapsInitializer.initialize(getActivity());

		// Gets to GoogleMap from the MapView and does initialization stuff
		mMap = mMapView.getMap();
		mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(DEFAULT_LAT, DEFAULT_LON), 12));
        mMap.setInfoWindowAdapter(new MinorStopInfoWindowAdapter());
		
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
        mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker m) {
				try {
//					mDialog.setStop(mStops.get(mMarkers.indexOf(m)));
//					mDialog.show(getActivity().getSupportFragmentManager(), null);
					m.showInfoWindow();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			}
        });
        mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {
				mDialog = new TimesDialogFragment();
				for(int i = 0; i < mMarkers.size(); i++) {
					int index = mMarkers.get(i).indexOf(marker);
					if(index != -1) {
						mDialog.setMinorStop(mMinorStops.get(i).get(index));
						mDialog.setBus(mBusses.get(i));
						mDialog.show(getActivity().getSupportFragmentManager(), null);
					}
				}
			}
        	
        });
	}
	
	public void onPause() {
		if(mDialog != null) {
			mDialog.dismiss();
		}
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
	
	public boolean onBackPressed() {
		if(mDrawer.isDrawerOpen(Gravity.RIGHT)) {
			mDrawer.closeDrawer(Gravity.RIGHT);
			return true;
		}
		return false;
	}
	
	private class MinorStopInfoWindowAdapter implements InfoWindowAdapter {

		@Override
		public View getInfoContents(Marker marker) {
			View v = getActivity().getLayoutInflater().inflate(R.layout.minor_stop_info_window_contents, null);
			for(int i = 0; i < mMarkers.size(); i++) {
				int index = mMarkers.get(i).indexOf(marker);
				if(index != -1) {
					TextView minorStopName = (TextView)v.findViewById(R.id.minor_stop_name);
					TextView estimatedArrivalValue = (TextView)v.findViewById(R.id.estimated_arrival_value);
					MinorStop ms = mMinorStops.get(i).get(index);
					MinorStopTime mst = mMinorStopTimes.get(i).get(index);
					minorStopName.setText(ms.getName());
					Calendar arrivalTime = mst.getCalendarTime();
					Calendar departureTime = mst.getDepartureCalendarTime();
					int arrH = arrivalTime.get(Calendar.HOUR);
					int arrM = arrivalTime.get(Calendar.MINUTE);
					int depH = departureTime.get(Calendar.HOUR);
					int depM = departureTime.get(Calendar.MINUTE);
					estimatedArrivalValue.setText(arrH +
							":" + String.format("%02d", arrM) +
							" " + (arrivalTime.get(Calendar.AM_PM) == 0 ? "AM" : "PM"));
					TextView estimatedDepartureValue = (TextView)v.findViewById(R.id.estimated_departure_value);
					if(arrH == depH && arrM == depM) {
						estimatedDepartureValue.setVisibility(View.GONE);
						v.findViewById(R.id.estimated_departure).setVisibility(View.GONE);
					}
					else {
						estimatedDepartureValue.setText(depH +
								":" + String.format("%02d", depM) +
								" " + (departureTime.get(Calendar.AM_PM) == 0 ? "AM" : "PM"));
					}
					
					return v;
				}
			}
			return null;
		}

		@Override
		public View getInfoWindow(Marker marker) {
			// TODO Auto-generated method stub
			return null;
		}
		
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
				mRouteShowing = new boolean[result.size()];
				mMinorStops = new ArrayList<ArrayList<MinorStop>>();
				mMinorStopTimes = new ArrayList<ArrayList<MinorStopTime>>();
				mMarkers = new ArrayList<ArrayList<Marker>>();
				mPolylines = new ArrayList<Polyline>();
				for(int i = 0; i < result.size(); i++) {
					mMinorStops.add(new ArrayList<MinorStop>());
					mMinorStopTimes.add(new ArrayList<MinorStopTime>());
					mMarkers.add(new ArrayList<Marker>());
					mPolylines.add(null);
				}
				mAdapter.setItems(result);
				mAdapter.notifyDataSetChanged();
			}
			
			mBusses = (ArrayList<Bus>)result;
		}
	}
	
	private void loadRoutes() {
		mAdapter = new RoutesAdapter(getActivity());
		mAdapter.setColor("#ffffff");
		ListView drawerList = (ListView)getActivity().findViewById(R.id.routesDrawer);
		drawerList.setAdapter(mAdapter);
		drawerList.setOnItemClickListener(this);
		
		// Prepare the loader.  Either re-connect with an existing one,
		// or start a new one.
		RouteLoadTask task = new RouteLoadTask();
		task.execute(new Void[] { });
	}
	
	private void setupActionBar() {
		ActionBar actionBar = ((SherlockFragmentActivity)getActivity()).getSupportActionBar();
		actionBar.setTitle(R.string.mapTitle);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);
	}

	/* Fetches list of stops for each checked off sidebar route */
	@Override
	public AsyncTaskLoader<Void> onCreateLoader(int id, Bundle arg1) {
		return new AsyncTaskLoader<Void>(getActivity()) {
			public Void loadInBackground() {
				for(int i = 0; i < mRouteSel.length; i++) {
					// Only include route, if chosen
					if(mRouteSel[i]) {
						if(mMarkers.get(i).isEmpty()) {
							Bus bus = mAdapter.getBus(i);
							Trip trip = bus.getNextTrip(mWeekday);
							if(trip != null) {
								trip.getMapPoints();
								ArrayList<MinorStopTime> minorStopTimes = trip.getMinorStopTimes();
								mMinorStopTimes.set(i, minorStopTimes);
								for(MinorStopTime minorStopTime : minorStopTimes) {
									MinorStop ms = minorStopTime.getMinorStop();
									mMinorStops.get(i).add(ms);
								}
							}
						}
					}
				}
				return null;
			}
		};
	}

	/* Renders content onto the map (for chosen bus routes) */
	@Override
	public void onLoadFinished(Loader<Void> loader, Void result) {
		boolean empty = true;
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for(int i = 0; i < mRouteSel.length; i++) {
			if(mRouteSel[i] && !mRouteShowing[i]) {
				Bus bus = mAdapter.getBus(i);
				Trip trip = bus.getNextTrip(mWeekday);
				if(trip != null) {
					ArrayList<MapPoint> mapPoints = trip.getMapPoints();
					mPolylines.set(i, makePolyline(mapPoints, bus.getColor()));
					ArrayList<MinorStopTime> minorStopTimes = trip.getMinorStopTimes();
					mMarkers.set(i, makeMarkers(minorStopTimes, bus.getColor()));
					mRouteShowing[i] = true;
				}
				else {
					AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getSherlockActivity());
					dialogBuilder.setTitle(getResources().getString(R.string.no_routes));
				    dialogBuilder.setMessage(getResources().getString(R.string.route_not_operating) +
				    		" " + mWeekday.toLowerCase() + "s");
				    // Create the AlertDialog object and return it
				    dialogBuilder.create().show();
				    mRouteSel[i] = false;
				}
			}
			else if(!mRouteSel[i] && mRouteShowing[i]) {
				mPolylines.get(i).remove();
				ArrayList<Marker> markers = mMarkers.get(i);
				for(Marker marker : markers) {
					marker.remove();
				}
				mMarkers.set(i, markers);
				mRouteShowing[i] = false;
			}
			
			if(mRouteSel[i]) {
				Bus bus = mAdapter.getBus(i);
				Trip trip = bus.getNextTrip(mWeekday);
				if(trip != null) {
					ArrayList<MapPoint> mapPoints = trip.getMapPoints();
					for(MapPoint mapPoint : mapPoints) {
						builder.include(new LatLng(mapPoint.getLatitude(), mapPoint.getLongitude()));
					}
				}
				empty = false;
			}
		}
		
		// Restore default zoom if no routes now selected
		if(empty) {
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(DEFAULT_LAT, DEFAULT_LON), 12));
		}
		// Zoom the map to include all markers in bounds
		else {
			LatLngBounds bounds = builder.build();
			int padding = 100;
			final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
			mMap.animateCamera(cu);
		}
	}

	@Override
	public void onLoaderReset(Loader<Void> arg0) {
		
	}

	/* Refreshes the map view (incl. stops, lines) */
	public void onItemClick(AdapterView<?> i, View v, int position, long id) {
		mRouteSel[position] = !mRouteSel[position];
		getLoaderManager().restartLoader(0, null, this).forceLoad();
	}
	
	public Polyline makePolyline(ArrayList<MapPoint> mapPoints, int color) {
		PolylineOptions opts = new PolylineOptions();
		opts.zIndex(1);
		
		for(MapPoint mapPoint : mapPoints) {
			opts.add(new LatLng(mapPoint.getLatitude(), mapPoint.getLongitude()));
		}
		
		opts.color(color);
		return mMap.addPolyline(opts);
	}
	
	public ArrayList<Marker> makeMarkers(ArrayList<MinorStopTime> minorStopTimes, int color) {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		ArrayList<Marker> markers = new ArrayList<Marker>();
		// For each loaded stop, create a marker at its coordinates
		// and set its marker hex color
		for(MinorStopTime mst : minorStopTimes) {
			MinorStop ms = mst.getMinorStop();
			LatLng pos = new LatLng(ms.getLatitude(), ms.getLongitude());
			builder.include(pos);
			float[] hsv = new float[3];
			Color.RGBToHSV(
					Color.red(color),
					Color.green(color),
					Color.blue(color),
					hsv);
			markers.add(mMap.addMarker(new MarkerOptions().position(pos).title(ms.getName())
					.icon(BitmapDescriptorFactory.defaultMarker(hsv[0]))));
		}
		
		return markers;
	}
}
