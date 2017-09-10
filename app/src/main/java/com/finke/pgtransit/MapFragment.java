package com.finke.pgtransit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
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
import android.widget.TextView;

import com.finke.pgtransit.adapters.RoutesAdapter;
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
public class MapFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<MapFragment.AsyncTaskResult>, OnItemClickListener {

    private final static int LOCATION_PERMISSIONS_REQUEST_CODE = 1234;
    private final static String MAP_DATA_BUNDLE_KEY = "bus_index";

	// Map defaults position to center of PG when no routes selected
	private final double DEFAULT_LAT = 53.9170641;
	private final double DEFAULT_LON = -122.7496693;

	// List of selected bus routes currently
	private boolean[] mRouteSel;
	private boolean[] mRouteShowing;
	private ArrayList<Bus> mBusses;
	private ArrayList<List<MinorStop>> mMinorStops;
	private ArrayList<List<MinorStopTime>> mMinorStopTimes;
	private ArrayList<ArrayList<Marker>> mMarkers;
	private ArrayList<List<Polyline>> mPolylines;
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

		Bundle mapViewState = state != null ? state.getBundle("mapViewState") : null;
		mMapView.onCreate(mapViewState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		DrawerLayout v = (DrawerLayout) inflater.inflate(R.layout.map, container, false);
		RelativeLayout frame = (RelativeLayout) v.findViewById(R.id.mapFrame);
		// Add the map view
		frame.addView(mMapView, 0);

		// Set the drawer button handler
		mDrawer = (DrawerLayout) v.findViewById(R.id.drawerLayout);
		Button drawerBtn = (Button) v.findViewById(R.id.showDrawerButton);
		drawerBtn.setOnClickListener(new OnClickListener() {
			// Pop out the sidebar
			@Override
			public void onClick(View v) {
				mDrawer.openDrawer(Gravity.END);
			}
		});

		// Needs to call MapsInitializer before doing any CameraUpdateFactory calls
		MapsInitializer.initialize(getActivity());

		// Gets to GoogleMap from the MapView and does initialization stuff
		mMap = mMapView.getMap();
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

        // Only set location enabled if permission is granted
        Activity activity = getActivity();
        if(ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        else {
            ActivityCompat.requestPermissions(activity,
                    new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION },
                    LOCATION_PERMISSIONS_REQUEST_CODE);
        }
        
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
						mDialog.show(getActivity().getFragmentManager(), null);
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
		Bundle mapViewState = new Bundle(state);
		mMapView.onSaveInstanceState(mapViewState);
		state.putBundle("mapViewState", mapViewState);
		super.onSaveInstanceState(state);
	}
	
	public void onLowMemory() {
		super.onLowMemory();
		mMapView.onLowMemory();
	}
	
	public boolean onBackPressed() {
		if(mDrawer.isDrawerOpen(Gravity.END)) {
			mDrawer.closeDrawer(Gravity.END);
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
                    TextView estimatedDepartureValue = (TextView)v.findViewById(R.id.estimated_departure_value);
					MinorStop ms = mMinorStops.get(i).get(index);
					MinorStopTime mst = mMinorStopTimes.get(i).get(index);
					minorStopName.setText(ms.getName());

                    // Null if bus does not stop here anymore today
                    if(mst != null) {
                        Resources res = getResources();
                        Calendar arrivalTime = mst.getCalendarTime();
                        Calendar departureTime = mst.getDepartureCalendarTime();
                        int arrH = arrivalTime.get(Calendar.HOUR);
                        int arrM = arrivalTime.get(Calendar.MINUTE);
                        int depH = departureTime.get(Calendar.HOUR);
                        int depM = departureTime.get(Calendar.MINUTE);
                        estimatedArrivalValue.setText(
                                String.format(res.getString(R.string.estimated_time_format),
                                        arrH,
                                        arrM,
                                        res.getString(
                                                (arrivalTime.get(Calendar.AM_PM) == 0 ? R.string.am : R.string.pm))));

                        // Hide the departure time if it is the same as the
                        // arrival time
                        if(arrH == depH && arrM == depM) {
                            estimatedDepartureValue.setVisibility(View.GONE);
                            v.findViewById(R.id.estimated_departure).setVisibility(View.GONE);
                        }
                        else {
                            estimatedDepartureValue.setText(
                                    String.format(res.getString(R.string.estimated_time_format),
                                            depH,
                                            depM,
                                            res.getString(
                                                    (departureTime.get(Calendar.AM_PM) == 0 ? R.string.am : R.string.pm))));
                        }
                    }
                    else {
                        estimatedArrivalValue.setText(R.string.no_more_arrivals_today);
                        estimatedDepartureValue.setVisibility(View.GONE);
                        v.findViewById(R.id.estimated_departure).setVisibility(View.GONE);
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
				mMinorStops = new ArrayList<>();
				mMinorStopTimes = new ArrayList<>();
				mMarkers = new ArrayList<ArrayList<Marker>>();
				mPolylines = new ArrayList<>();
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
//		ActionBar actionBar = getActivity().getActionBar();
//		actionBar.setTitle(R.string.mapTitle);
//		actionBar.setHomeButtonEnabled(false);
//		actionBar.setDisplayHomeAsUpEnabled(false);
	}

    /**
     * Load the requested route's map data in background
     * @param id Unused
     * @param args Arguments to specify what bus data to load
     * @return Map points, stops, and stop times to use on the map/markers
     */
	@Override
	public AsyncTaskLoader<AsyncTaskResult> onCreateLoader(final int id, final Bundle args) {
		return new AsyncTaskLoader<AsyncTaskResult>(getActivity()) {
			public AsyncTaskResult loadInBackground() {
                AsyncTaskResult result = new AsyncTaskResult();
                int busIndex = args.getInt(MAP_DATA_BUNDLE_KEY);

                // Load data for the chosen bus
                if(mRouteSel[busIndex]) {
                    Bus bus = mAdapter.getBus(busIndex);
                    result.color = bus.getColor();

                    List<Trip> trips = bus.getTrips(mWeekday);
                    result.mapPoints = Trip.getDistinctMapPoints(trips);
                    result.minorStops = Trip.getDistinctMinorStops(trips);
                    result.minorStopTimes = new ArrayList<>();

                    // Find the next time for each stop, and use that for the
                    // marker popup
                    for(MinorStop stop : result.minorStops) {
                        result.minorStopTimes.add(stop.getNextMinorStopTime(mWeekday, bus.getId()));
                    }
                }

				return result;
			}
		};
	}

    /**
     * Render map data onto the map
     * @param loader Loader that loaded data in the background
     * @param result Map points, stops, and stop times to use on the map/
     *               markers
     */
	@Override
	public void onLoadFinished(Loader<AsyncTaskResult> loader, AsyncTaskResult result) {
		boolean empty = true;
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for(int i = 0; i < mRouteSel.length; i++) {
			if(mRouteSel[i] && !mRouteShowing[i]) {
                if(!result.mapPoints.isEmpty()) {
                    mPolylines.set(i, makePolylines(result.mapPoints, result.color));
                    mMarkers.set(i, makeMarkers(result.minorStops, result.color));
                    mMinorStops.set(i, result.minorStops);
                    mMinorStopTimes.set(i, result.minorStopTimes);
                    mRouteShowing[i] = true;
                }
                else {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
					dialogBuilder.setTitle(getResources().getString(R.string.no_routes));
				    dialogBuilder.setMessage(getResources().getString(R.string.route_not_operating) +
				    		" " + mWeekday.toLowerCase() + "s");
				    // Create the AlertDialog object and return it
				    dialogBuilder.create().show();
				    mRouteSel[i] = false;
                }
			}
			else if(!mRouteSel[i] && mRouteShowing[i]) {
                for(Polyline polyline : mPolylines.get(i)) {
                    polyline.remove();
                }
				mPolylines.get(i).clear();
				for(Marker marker : mMarkers.get(i)) {
					marker.remove();
				}
                mMarkers.get(i).clear();
				mRouteShowing[i] = false;
			}

            // Set zoom bounds for zoom adjustment
			if(mRouteSel[i]) {
                for(Marker m : mMarkers.get(i)) {
                    builder.include(m.getPosition());
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

    /**
     * Unused. Reset data acquired by an AsyncTaskLoader
     * @param loader The loader that is being reset
     */
	@Override
	public void onLoaderReset(Loader<AsyncTaskResult> loader) {}

    /**
     * Represents map data returned for a particular bus route
     * @author Daniel Finke
     * @since 2016-09-05
     */
    public class AsyncTaskResult {
        public int color;
        public List<MapPoint> mapPoints;
        public List<MinorStop> minorStops;
        public List<MinorStopTime> minorStopTimes;
    }

    /**
     * Requests an AsyncTaskLoader job to load map data for the clicked route
     * @param i The AdapterView instance containing the item clicked
     * @param v The view that was clicked
     * @param position The position of the clicked view in the AdapterView
     * @param id The id of the item that was clicked
     */
	public void onItemClick(AdapterView<?> i, View v, int position, long id) {
		mRouteSel[position] = !mRouteSel[position];
        // Set parameters for which bus's map data should be loaded
        Bundle args = new Bundle();
        args.putInt(MAP_DATA_BUNDLE_KEY, position);
		getLoaderManager().restartLoader(0, args, this).forceLoad();
	}

    /**
     * Convert a list of map points into individual trip polylines
     * @param mapPoints The map points to make lines from
     * @param color The color the polyline should be
     * @return A list of polylines made from the map points
     */
	public List<Polyline> makePolylines(List<MapPoint> mapPoints, int color) {
		ArrayList<Polyline> polylines = new ArrayList<>();
        int tripId = 0;
        PolylineOptions opts = null;

        for(MapPoint point : mapPoints) {
            // Change to a new polyline for each trip
            if(tripId != point.getTripId()) {
                if(opts != null) {
                    polylines.add(mMap.addPolyline(opts));
                }
                opts = new PolylineOptions();
                opts.zIndex(1);
                opts.color(color);
                tripId = point.getTripId();
            }
            opts.add(new LatLng(point.getLatitude(), point.getLongitude()));
        }

        // Add the last polyline
        if(opts != null) {
            polylines.add(mMap.addPolyline(opts));
        }

		return polylines;
	}
	
	public ArrayList<Marker> makeMarkers(List<MinorStop> minorStops, int color) {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		ArrayList<Marker> markers = new ArrayList<Marker>();
		// For each loaded stop, create a marker at its coordinates
		// and set its marker hex color
		for(MinorStop ms : minorStops) {
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
