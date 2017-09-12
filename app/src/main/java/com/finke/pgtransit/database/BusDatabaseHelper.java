package com.finke.pgtransit.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.finke.pgtransit.model.Bus;
import com.finke.pgtransit.model.MinorStop;
import com.finke.pgtransit.model.Stop;

import java.io.IOException;
import java.util.List;

public class BusDatabaseHelper extends DataBaseHelper {

    private static final int DATABASE_VERSION = 10;
    private static BusDatabaseHelper instance;
    
    public static BusDatabaseHelper getInstance() throws Exception {
    	if(instance == null) {
    		throw new Exception("No database helper yet created");
    	}
    	return instance;
    }
    
    // Create a new helper instance, to provide database facilities
    // to the whole app
    public static void createInstance(Context c) {
    	if(instance == null) {
    		instance = new BusDatabaseHelper(c);
    		// Open db if it has been copied from assets
    		if(instance.checkDatabase()) {
        		instance.openDataBase();
    		}
    		else {
    			// Otherwise try to copy it from assets then open
    			try {
					instance.createDataBase();
					instance.openDataBase();
				} catch(IOException ex) {
					System.err.println(ex.getMessage());
				}
    		}
    	}
    }
    
    // Close connection to SQLite db
    public static void destroyInstance() {
    	if(instance != null) {
    		instance.close();
        	instance = null;
    	}
    }
    
	public BusDatabaseHelper(Context context) {
		super(context, "db.sqlite", null, DATABASE_VERSION);
		DB_PATH = "/data/data/com.finke.pgtransit/databases/";
		DB_NAME = "db.sqlite";
	}
	
	/* Following methods wrap db queries */
	public Cursor getRoutesCursor() { return myDataBase.query("bus", Bus.COLUMNS, null, null, null,
			null, "substr(number, 1, 1) ASC, number ASC"); }
	public Cursor getRoutesCursor(int id) {
		return myDataBase.query("bus", Bus.COLUMNS, "_id = ?", new String[] { Integer.toString(id) }, null, null, null);
	}
	public Cursor getStopsCursor(int routeId, String weekday) {
		return myDataBase.query("major_stop", Stop.COLUMNS, "bus_id = ? AND day = ?",
				new String[] { Integer.toString(routeId), weekday }, null, null, null);
	}
	public Cursor getStopCursor(String stopId) {
		return myDataBase.query("major_stop", Stop.COLUMNS, "_id = ?", new String[] { stopId }, null, null, null);
	}
	public Cursor getTimesCursor(String stopId) {
		return myDataBase.rawQuery("SELECT _id, major_stop_id, time, note_code, note FROM major_stop_time " +
				"WHERE major_stop_id = ? " +
				"ORDER BY time ASC", new String[] { stopId });
	}

    /**
     * Get a Cursor over filtered trip data
     * @param busId Filter the results by bus id
     * @param weekday Filter the result by day of week
     * @return A Cursor over filtered trip data
     */
	public Cursor getTripCursor(int busId, String weekday) {
        return myDataBase.rawQuery("SELECT t._id as _id, t.bus_id as bus_id, t.day as day " +
				"FROM trip t " +
				"WHERE t.bus_id = ? AND t.day = ?", new String[] { Integer.toString(busId), weekday });
	}

    /**
     * Get a Cursor over map points for a set of trips
     * @param tripIds Filter the results so the map points are in these trips
     * @return A Cursor over map points for a set of trips
     */
	public Cursor getDistinctMapPointsCursor(List<Integer> tripIds) {
        StringBuilder whereClauseBuilder = new StringBuilder();
        for(int i = 0; i < tripIds.size(); i++) {
            whereClauseBuilder.append(tripIds.get(i));
            if(i < tripIds.size() - 1) {
                whereClauseBuilder.append(",");
            }
        }
        return myDataBase.rawQuery("SELECT _id, trip_id, latitude, longitude " +
                "FROM map_points " +
                "WHERE _id IN (" +
                "SELECT MIN(_id) " +
                "FROM map_points " +
                "WHERE trip_id IN (" + whereClauseBuilder.toString() + ") " +
                "GROUP BY latitude, longitude)", null);
	}

    /**
     * Get a Cursor over minor stops for a set of trips
     * @param tripIds Filter the results so the minor stops are in these trips
     * @return A Cursor over minor stops for a set of trips
     */
    public Cursor getDistinctMinorStopsCursor(List<Integer> tripIds) {
        StringBuilder whereClauseBuilder = new StringBuilder();
        for(int i = 0; i < tripIds.size(); i++) {
            whereClauseBuilder.append(tripIds.get(i));
            if(i < tripIds.size() - 1) {
                whereClauseBuilder.append(",");
            }
        }
        return myDataBase.rawQuery("SELECT * " +
                "FROM minor_stop " +
                "WHERE _id IN ( " +
                "SELECT ms._id " +
                "FROM minor_stop ms " +
                "JOIN minor_stop_time mst ON mst.minor_stop_id = ms._id " +
                "WHERE trip_id IN (" + whereClauseBuilder.toString() + "))", null);
    }
	public Cursor getMinorStopTimesCursorForMinorStop(int minorStopId, String weekday, int busId) {
		return myDataBase.rawQuery("SELECT mst._id as _id, mst.trip_id as trip_id, " + 
				"mst.minor_stop_id as minor_stop_id, mst.arrival_time as arrival_time, mst.departure_time as departure_time " +
				"FROM minor_stop_time mst " +
				"JOIN trip t on t._id = mst.trip_id " +
				"WHERE mst.minor_stop_id = ? AND t.day = ? AND t.bus_id = ?",
				new String[] { Integer.toString(minorStopId), weekday, Integer.toString(busId) });
	}
	public MinorStop getMinorStop(int minorStopId) {
		Cursor c = myDataBase.query("minor_stop", MinorStop.COLUMNS, "_id = ?",
				new String[] { Integer.toString(minorStopId) }, null, null, null);
		c.moveToFirst();
		MinorStop minorStop = new MinorStop(
				c.getInt(c.getColumnIndex(MinorStop.COLUMNS[0])),
				c.getString(c.getColumnIndex(MinorStop.COLUMNS[1])),
				c.getDouble(c.getColumnIndex(MinorStop.COLUMNS[2])),
				c.getDouble(c.getColumnIndex(MinorStop.COLUMNS[3])));
		c.close();
		return minorStop;
	}
	
	// Checks for existence of the database
	public boolean checkDatabase() {
		return super.checkDataBase();
	}
	
	// Connect to the SQLite db and upgrade it if necessary
	public void openDataBase() throws SQLException {
    	super.openDataBase();
    	int old = myDataBase.getVersion();
    	
    	if(old < DATABASE_VERSION) {
    		onUpgrade(myDataBase, old, DATABASE_VERSION);
    	}
    }
	// Connect to database in writeable mode
	public void openWriteableDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }
	
	// Procedure for handling database upgrades.
	// Involves re-copying the db from assets
	// Will not work if there is any user-generated content in future
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(checkDatabase()) {
			myDataBase.close();
			try {
				this.copyDataBase();
				openWriteableDataBase();
				myDataBase.setVersion(newVersion);
				myDataBase.close();
				openDataBase();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
