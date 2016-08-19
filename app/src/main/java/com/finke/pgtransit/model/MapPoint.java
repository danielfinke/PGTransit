package com.finke.pgtransit.model;

public class MapPoint {

	private int mId;
	private int mTripId;
	private double mLatitude;
	private double mLongitude;
	
	public final static String[] COLUMNS = {"_id", "trip_id", "latitude", "longitude"};
	
	public MapPoint(int id, int tripId, double latitude, double longitude) {
		mId = id;
		mTripId = tripId;
		mLatitude = latitude;
		mLongitude = longitude;
	}
	
	public int getId() { return mId; }
	public int getTripId() { return mTripId; }
	public double getLatitude() { return mLatitude; }
	public double getLongitude() { return mLongitude; }
	
}
