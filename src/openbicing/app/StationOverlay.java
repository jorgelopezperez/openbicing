package openbicing.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class StationOverlay extends Overlay {
	private int bikes;
	private int free;
	private String timestamp;
	private int status;
	private String id;
	private Context context;
	
	private boolean selected = false;
	
	private GeoPoint point;
		
	private static final int BLACK_STATE = 0;
	private static final int RED_STATE = 1;
	private static final int YELLOW_STATE = 2;
	private static final int GREEN_STATE = 3;
	
	private int radiusInPixels;
	private int radiusInMeters;
	
	private static final int RED_STATE_MAX = 0;
	private static final int YELLOW_STATE_MAX = 8;
	
	private static final int RED_STATE_RADIUS = 30;
	private static final int YELLOW_STATE_RADIUS = 50;
	private static final int GREEN_STATE_RADIUS = 80;
		
	public StationOverlay(GeoPoint point,Context context, int bikes, int free, String timestamp, String id) {
		this.point = point;
		this.bikes = bikes;
		this.free = free;
		this.id = id;
		this.timestamp = timestamp;
		this.context = context;
		this.updateStatus();
	}
	
	
	public String getId(){
		return this.id;
	}
	
	public int getBikes(){
		return this.bikes;
	}
	
	public int getFree(){
		return this.free;
	}
	
	public GeoPoint getCenter(){
		return this.point;
	}
	
	public void updateStatus(){
		if (this.bikes>YELLOW_STATE_MAX){
			this.status = GREEN_STATE;
			this.radiusInMeters = GREEN_STATE_RADIUS;
		}
		else if (this.bikes>RED_STATE_MAX){
			this.status = YELLOW_STATE;
			this.radiusInMeters = YELLOW_STATE_RADIUS;
	
		}else{
			this.status = RED_STATE;
			this.radiusInMeters = RED_STATE_RADIUS;
		}
	}
	
	public void setSelected(boolean selected){
		this.selected = selected;
	}
	public void update(){
		// TODO Update aviability.. :D
		this.updateStatus();
	}

	private void calculatePixelRadius(MapView mapView){
		this.radiusInPixels = (int) mapView.getProjection().metersToEquatorPixels(this.radiusInMeters);
	}
	
	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
			long when) {
		// TODO Auto-generated method stub
		return super.draw(canvas, mapView, shadow, when);
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		
		calculatePixelRadius(mapView);
		
		Projection astral = mapView.getProjection();
		Point screenPixels = astral.toPixels(this.point, null);
		
		RectF oval = new RectF(screenPixels.x-this.radiusInPixels,screenPixels.y-this.radiusInPixels,screenPixels.x+this.radiusInPixels,screenPixels.y+this.radiusInPixels);
		Paint paint = new Paint();
		if (this.status == RED_STATE)
			paint.setARGB(75,240,35,17);
		else if (this.status == YELLOW_STATE)
			paint.setARGB(75,255,210,72);
		else if (this.status == GREEN_STATE)
			paint.setARGB(75,168,255,87);
		else if (this.status == BLACK_STATE)
			paint.setARGB(75,0,0,0);
				
		Paint paint2 = new Paint();
		paint2.set(paint);
		paint2.setStrokeWidth(4);
		paint2.setAlpha(100);
		paint2.setAntiAlias(true);
		paint2.setStyle(Paint.Style.STROKE);
		canvas.drawCircle(screenPixels.x, screenPixels.y, this.radiusInPixels, paint2);
		canvas.drawOval(oval,paint);
		
		if (this.selected){
			Paint paint3 = new Paint();
			paint3.setARGB(75, 0,0,0);
			paint3.setAntiAlias(true);
			paint3.setStrokeWidth(2);
			paint3.setStyle(Paint.Style.STROKE);
			canvas.drawRect(screenPixels.x-this.radiusInPixels*4/3, screenPixels.y-this.radiusInPixels*4/3, screenPixels.x+this.radiusInPixels*4/3, screenPixels.y+this.radiusInPixels*4/3, paint3);
		}
	}

	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		// TODO Auto-generated method stub
		
		if ((p.getLatitudeE6()<=this.point.getLatitudeE6()+800 && p.getLatitudeE6()>=this.point.getLatitudeE6()-800) 
			&&
			(p.getLongitudeE6()<=this.point.getLongitudeE6()+800 && p.getLongitudeE6()>=this.point.getLongitudeE6()-800)){
			this.setSelected(!this.getSelected());
		}
			
		return super.onTap(p, mapView);
	}

	public boolean getSelected(){
		return this.selected;
	}
	@Override
	public boolean onTouchEvent(MotionEvent e, MapView mapView) {
		// TODO Auto-generated method stub
		
		return super.onTouchEvent(e, mapView);
	}

}
