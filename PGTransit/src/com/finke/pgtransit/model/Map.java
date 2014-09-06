package com.finke.pgtransit.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.res.AssetManager;
import android.database.Cursor;

import com.ekito.simpleKML.Serializer;
import com.ekito.simpleKML.model.Coordinate;
import com.ekito.simpleKML.model.Document;
import com.ekito.simpleKML.model.Feature;
import com.ekito.simpleKML.model.Kml;
import com.ekito.simpleKML.model.LineString;
import com.ekito.simpleKML.model.Placemark;
import com.finke.pgtransit.database.BusDatabaseHelper;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

/* Represents data on a map for a specific bus */
public class Map {
	
	private int mId;
	private int mBusId;
	private String mWeekday;
	// Path to KML line data for route
	private String mKmlFile;
	// Distinguishes this map as the primary map
	// for a bus, in case there are several diff. routes
	private boolean mPrimaryFlag;
	
	public final static String[] COLUMNS = {"_id", "bus_id", "weekday", "kml_file", "primary_flag"};

	public Map(int id, int busId, String weekday, String kmlFile, int primaryFlag) {
		mId = id;
		mBusId = busId;
		mWeekday = weekday;
		mKmlFile = kmlFile;
		mPrimaryFlag = primaryFlag == 1;
	}
	
	public int getId() { return mId; }
	public int getBusId() { return mBusId; }
	public String getWeekday() { return mWeekday; }
	public String getKmlFile() { return mKmlFile; }
	public boolean isPrimary() { return mPrimaryFlag; }
	
	/* Parse the contents of KML data
	 * to get Polylines for display on the Google Map
	 * Formatting data for features is ignored - overridden later by
	 * my code
	 */
	public List<PolylineOptions> getLines(AssetManager am) {
		try {
			InputStream stream = am.open(mKmlFile + ".kml");
			Serializer kmlSerializer = new Serializer();
			Kml kml = kmlSerializer.read(stream);
			
			// Fetch line features from KML
			// Non-lines will cause exception here
			List<Feature> featureList = ((Document)kml.getFeature()).getFeatureList();
			ArrayList<PolylineOptions> items = new ArrayList<PolylineOptions>();
			
			// Loop through features
			for(int j = 0; j < featureList.size(); j++) {
				Placemark lineData = (Placemark)((Document)kml.getFeature()).getFeatureList().get(j);
				LineString line = (LineString)lineData.getGeometryList().get(0);
				ArrayList<Coordinate> coords = line.getCoordinates().getList();
				
				PolylineOptions opts = new PolylineOptions();
				opts.zIndex(1);
				
				// Add all coords for current feature to the polyline
				for(int k = 0; k < coords.size(); k++) {
					Coordinate c = coords.get(k);
					opts.add(new LatLng(c.getLatitude(), c.getLongitude()));
				}
				
				items.add(opts);
			}
			
			return items;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/* Fetch the set of maps that belong to a bus
	 * - bus identified by PK
	 */
	public static List<Map> fetchFromDatabase(int busId) throws Exception {
		Cursor c = BusDatabaseHelper.getInstance().getMapsCursor(busId);
		c.moveToFirst();
		
		List<Map> items = new ArrayList<Map>();
		while(!c.isAfterLast()) {
			items.add(new Map(
					c.getInt(c.getColumnIndex(COLUMNS[0])),
					c.getInt(c.getColumnIndex(COLUMNS[1])),
					c.getString(c.getColumnIndex(COLUMNS[2])),
					c.getString(c.getColumnIndex(COLUMNS[3])),
					c.getInt(c.getColumnIndex(COLUMNS[4]))));
			c.moveToNext();
		}
		c.close();
		
		return items;
	}
}
