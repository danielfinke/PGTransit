package com.finke.pgtransit.database;

import java.io.IOException;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.finke.pgtransit.model.Bus;
import com.finke.pgtransit.model.Map;
import com.finke.pgtransit.model.Stop;

public class BusDatabaseHelper extends DataBaseHelper {

	private static final int DATABASE_VERSION = 5;
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
	public Cursor getRoutesCursor() { return myDataBase.query("bus", Bus.COLUMNS, null, null, null, null, null); }
	public Cursor getRoutesCursor(int id) {
		return myDataBase.query("bus", Bus.COLUMNS, "_id = ?", new String[] { Integer.toString(id) }, null, null, null);
	}
	public Cursor getStopsCursor(int routeId) {
		return myDataBase.query("location", Stop.COLUMNS, "bus_id = ?", new String[] { Integer.toString(routeId) }, null, null, null);
	}
	public Cursor getStopCursor(String stopId) {
		return myDataBase.query("location", Stop.COLUMNS, "_id = ?", new String[] { stopId }, null, null, null);
	}
	public Cursor getTimesCursor(String stopId, String weekday) {
		return myDataBase.rawQuery("SELECT _id, location_id, weekday, time, friday_flag, next_day_flag FROM time_slot " +
				"WHERE location_id = ?" +
				"AND weekday = ?", new String[] { stopId, weekday });
	}
	public Cursor getTripNotesCursor(int timeSlotId) {
		return myDataBase.rawQuery("SELECT _id, time_slot_id, name, description FROM trip_note " +
			"WHERE time_slot_id = ?", new String[] { Integer.toString(timeSlotId) });
	}
	public Cursor getMapsCursor(int routeId) {
		return myDataBase.query("map", Map.COLUMNS, "bus_id = ?", new String[] { Integer.toString(routeId) }, null, null, null);
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
