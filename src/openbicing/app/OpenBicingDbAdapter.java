/* Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package openbicing.app;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class OpenBicingDbAdapter {

    
	public static final String KEY_ROWID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_COORDINATES = "coordinates";
    public static final String KEY_X = "x";
    public static final String KEY_Y = "y";
    public static final String KEY_BIKE = "bike";
    public static final String KEY_FREE = "free";
    public static final String KEY_TIMESTAMP = "timestamp";
    
    public static final String STATIONS_URL = "http://openbicing.appspot.com/stations.json";
    

    private static final String TAG = "OpenBicingDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    private RESTHelper mRESTHelper;
    
    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
    		"create table stations (_id integer primary key autoincrement, "
    			+ "name text not null, coordinates text not null, x text not null, "
    			+ "y text not null, bike integer not null, free integer not null, timestamp text not null);";
    
    private static final String DATABASE_NAME = "openbicing_data";
    private static final String STATIONS_TABLE = "stations";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS stations");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public OpenBicingDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public OpenBicingDbAdapter open() throws Exception {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        this.mRESTHelper = new RESTHelper(this.mCtx,false,null,null);
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }
    
    public void syncStations() throws Exception{
    	String listStations = mRESTHelper.restGET(STATIONS_URL);
    	JSONArray stations = new JSONArray(listStations);
    	JSONObject station = null;
    	mDb.execSQL("DELETE FROM "+STATIONS_TABLE);
    	for (int i = 0; i<stations.length(); i++){
    		station = stations.getJSONObject(i);
    		createStation(station.get("name").toString(),station.get("coordinates").toString(),station.get("x").toString(),station.get("y").toString(),Integer.parseInt(station.get("bikes").toString()),Integer.parseInt(station.get("free").toString()),station.get("timestamp").toString());
    	}
    }
    
    public long createStation(String name, String coordinates, String x, String y, Integer bike, Integer free, String timestamp){
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(KEY_NAME, name);
    	initialValues.put(KEY_COORDINATES, coordinates);
    	initialValues.put(KEY_X, x);
    	initialValues.put(KEY_Y, y);
    	initialValues.put(KEY_BIKE, bike);
    	initialValues.put(KEY_FREE, free);
    	initialValues.put(KEY_TIMESTAMP, timestamp);
    	return mDb.insert(STATIONS_TABLE, null, initialValues);
    }
    
    
    public void sync() throws Exception {
    	this.syncStations();
    }
    
    public Cursor fetchAllStations() {
        return mDb.query(STATIONS_TABLE, new String[] {KEY_ROWID, KEY_NAME, KEY_X, KEY_Y, KEY_BIKE, KEY_FREE}, null, null, null, null, KEY_NAME);
    }
}