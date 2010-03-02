package openbicing.app;

import java.util.LinkedList;
import java.util.Queue;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

/**
 * 
 * Maintains an updated DB inside an OverlayList.
 * Stores everything on a single JSON String.
 * Unicorns like peeing fast
 * 
 * 
 * @author eskerda
 *
 */
public class OpenBicingFastDbAdapter implements Runnable{
	
	public static final String STATIONS_URL = "http://openbicing.appspot.com/stations.json";
	public static final String PREF_NAME = "openbicing";
	
	public static final int FETCH = 0;
	public static final int UPDATE_MAP = 1;
	public static final int UPDATE_DATABASE = 2;
	
	private StationOverlayList stationsMemoryList;
    
	private RESTHelper mRESTHelper;
    
	private MapView mapView;
    
	private Context mCtx;
    
	private Handler handler;
	
	private Queue<Integer> toDo;
	
	private JSONArray stations;
    
	
    public OpenBicingFastDbAdapter(Context ctx, MapView mapView, Handler handler, StationOverlayList stationsMemoryList){
    	this.mCtx = ctx;
    	this.stationsMemoryList = stationsMemoryList;
    	this.mapView = mapView;
    	this.handler = handler;
    	this.mRESTHelper = new RESTHelper(this.mCtx, false, null, null);
    	
    	toDo = new LinkedList();
    }
    
    public String fetchStations() throws Exception{
    	return mRESTHelper.restGET(STATIONS_URL);
    }
    
    public void populate(JSONArray stations) throws Exception{
    	
    	JSONObject station = null;
    	int lat,lng,bikes,free;
    	String timestamp,id;
    	GeoPoint point;
    	
    	stationsMemoryList.clear();
    	
    	for (int i = 0; i<stations.length(); i++){
    		station = stations.getJSONObject(i);
    		lat = Integer.parseInt(station.getString("y"));
    		lng = Integer.parseInt(station.getString("x"));
    		bikes = station.getInt("bikes");
    		free = station.getInt("free");
    		timestamp = station.getString("timestamp");
    		id = station.getString("name");
    		point = new GeoPoint(lat, lng);
    		
    		StationOverlay memoryStation = new StationOverlay(point,mCtx,bikes,free,timestamp,id);
    	    stationsMemoryList.addStationOverlay(memoryStation);
    	}
    	mapView.postInvalidate();
    }
    
    public void store(JSONArray stations){
    	SharedPreferences settings = this.mCtx.getSharedPreferences(PREF_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putString("stations", stations.toString());
	    editor.commit();
    }
    
    public JSONArray retrieve() throws Exception{
    	SharedPreferences settings = this.mCtx.getSharedPreferences("openbicing", 0);
    	String strStations = settings.getString("stations", "[]");
    	return new JSONArray(strStations);
    }
	
    
    public void syncStations() throws Exception{
    	toDo.add(FETCH);
    	toDo.add(UPDATE_MAP);
    	toDo.add(UPDATE_DATABASE);
    	Thread happyThread = new Thread(this);
    	happyThread.start();
    }
    
    public void updateMap() throws Exception{
    	toDo.add(UPDATE_MAP);
    	Thread happyThread = new Thread(this);
    	happyThread.start();
    }
    
    @Override
	public void run() {
    	while (!toDo.isEmpty()){
    		Integer action = (Integer) toDo.poll();
    		switch(action){
    			case FETCH:
    				try{
    					stations = new JSONArray(fetchStations()); 
    				}catch (Exception fetchError){
    					//Something went wrong (probably no Internet access)
    					//Populate from store
    					try{stations = retrieve();}catch (Exception internalError){
    						//Shit, no store too? then fuck off..
    						stations = new JSONArray();
    					}
    					handler.sendEmptyMessage(FETCH);
    				}
    				break;
    			case UPDATE_MAP:
    				try{populate(stations);}catch (Exception populateError){
    					//I don't know.. report it back?
    					Log.i("openBicing","Something went really wrong!");
    				}
    				handler.sendEmptyMessage(UPDATE_MAP);
    				break;
    			case UPDATE_DATABASE:
    				store(stations);
    				handler.sendEmptyMessage(UPDATE_DATABASE);
    				break;
    		}
    	}
	}
}