package com.example.schatzsuche;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class TrackingActivity extends Activity implements SensorEventListener,LocationListener {
  	/* Tracking Mode */
  	private Button checkin; 
  	private Button back;

	private ImageView image;
	private float currentDegree = 0f;
	private SensorManager mSensorManager;
	TextView tvHeading;
	TextView tvDistance;
	TextView tvLat;
	TextView tvLong; 
	
	private treasure target;
  	private final double Radius = 6371;
  	private final double Rds = 0.0174532925;
  	private Bundle extra;
	double latitude; //= -6.890608;
	double longitude; //= 107.610008;
	private LocationManager locationManager;
	private Location location;
	private double distance = 0;
	
	int state = 0;
	
  	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        getLocation();
        loadTrackingMode();
  	}
  	
    public void loadTrackingMode() {
    	setContentView(R.layout.activity_tracking);
    	t.start(); //for Beep Sound
    	extra = getIntent().getExtras();
		target = new treasure();
		target.setId(extra.getString("id"));
		target.setBssid(extra.getString("bssid"));
		//target.setDistance(extra.getDouble("distance"));
		//target.setDegree(extra.getDouble("degree")); 
		target.setLatitude(extra.getDouble("latitude"));
		target.setLongitude(extra.getDouble("longitude"));

		//target.setLatitude(Math.asin(Math.sin(latitude)*Math.cos(target.getDistance()/Radius) + Math.cos(latitude)*Math.sin(target.getDistance()/Radius)*Math.cos(target.getDegree()*Rds)));
		//target.setLongitude(longitude + Math.atan2(Math.sin(target.getDegree())*Math.sin(target.getDistance()/Radius)*Math.cos(latitude),Math.cos(target.getDistance()/Radius)-Math.sin(latitude)*Math.sin(target.getLatitude())));
		//double lat2 = Math.asin(Math.sin(latitude*Rds)*Math.cos(target.getDistance()/Radius*Rds) + Math.cos(latitude*Rds)*Math.sin(target.getDistance()/Radius*Rds)*Math.cos(target.getDegree()*Rds)) * 180 / Math.PI;
	  	//target.setLatitude(lat2);
	  	//double lon2 = longitude + Math.atan2(Math.sin(target.getDegree()*Rds)*Math.sin(target.getDistance()/Radius*Rds)*Math.cos(latitude*Rds),Math.cos(target.getDistance()/Radius*Rds)-Math.sin(latitude*Rds)*Math.sin(target.getLatitude()*Rds)) * 180 / Math.PI;
	  	//target.setLongitude(lon2);
		
    	checkin = (Button) findViewById(R.id.button_checkin);
    	image = (ImageView) findViewById(R.id.imageViewCompass);
		tvHeading = (TextView) findViewById(R.id.tvHeading);
		tvDistance = (TextView) findViewById(R.id.tvDistance);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		tvLat = (TextView) findViewById(R.id.tvLat);
		tvLong = (TextView) findViewById(R.id.tvLong);
		
    	// TODO : onclick listener utk button checkin
    	
    	back = (Button) findViewById(R.id.button_backtoradar);
    	back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				state = 1;
				Intent goToMainActivity = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(goToMainActivity);
			}
		});
    }

    @Override
	public void onBackPressed() {
    	state = 1;
		Intent goToMainActivity = new Intent(getApplicationContext(), MainActivity.class);
		startActivity(goToMainActivity);
	}
    
	@Override
	protected void onResume() {
		super.onResume();
		// for the system's orientation sensor registered listeners
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		tvLat.setText("Lat : " + latitude);
		tvLong.setText("Long : " + longitude);
		float degree = Math.round(event.values[0]); //Acceleration minus Gx on the x-axis
		
		// deg adalah degree from North dari titik asal ke Lat Long harta karun
		float deg = getDegrees(latitude,longitude, target.getLatitude(), target.getLongitude());
		tvHeading.setText("Harta Karun : " + deg + " degrees from North");
		
		//float finalDegree = (degree + deg) % 360;
		float finalDegree = (degree - deg) % 360;
		RotateAnimation ra = new RotateAnimation(currentDegree, -finalDegree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f); 
		ra.setDuration(210);	// how long the animation will take place
		ra.setFillAfter(true);	// set the animation after the end of the reservation status
		image.startAnimation(ra);
		currentDegree = -finalDegree;
		
		/* Calculate Distance */
		distance = calcuDistance(target.getLatitude(), target.getLongitude()) * 1000;
		if (distance < 10) {
			image.setImageResource(R.drawable.arrow_green);
		}
		else if (distance < 20) {
			image.setImageResource(R.drawable.arrow_blue);
		}
		else if (distance < 30) {
			image.setImageResource(R.drawable.arrow_yellow);
		}
		else if (distance < 40 ){
			image.setImageResource(R.drawable.arrow_orange);
		}
		else {
			image.setImageResource(R.drawable.arrow_red);
		}
		tvDistance.setText("Distance : " + distance + " m");
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// not in use
	}

	//http://stackoverflow.com/questions/8502795/get-direction-compass-with-two-longitude-latitude-points
	float getDegrees(double lat1,double long1, double lat2, double long2) {
	    //double dLat = DegreesToRadians(lat2-lat1);
	    double dLon = Math.toRadians(long2-long1);
	    lat1 = Math.toRadians(lat1);
	    lat2 = Math.toRadians(lat2);
	    double y = Math.sin(dLon) * Math.cos(lat2);
	    double x = Math.cos(lat1)*Math.sin(lat2) - Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
	    double brng = Math.toDegrees(Math.atan2(y, x));
	    if(brng<0) { 
	        brng=360 - Math.abs(brng);
	    }
	    return (float) brng;
	}
	
	private double calcuDistance(double lan, double lon)
  	{
  		double dLat = (lan-latitude)*Rds;
  	  	double dLon = (lon-longitude)*Rds;
  	  	double lati1 = latitude * Rds;
  	  	double lati2 = lan*Rds;
  	  	double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
    	        Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lati1) * Math.cos(lati2); 
    	double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)) * 180 / Math.PI; 
    	//System.out.println("DISTANCE : " + Radius * c + " km");
    	return Radius * c;
  	}

	Thread t = new Thread(new Runnable() {
	    public void run() {
	    	int sleepTime = 2000;
	    	while (state == 0) {
		    	if (distance < 10) {
		    		sleepTime = 300;
		    	}
		    	else 
	    		if (distance < 20) {
	    			sleepTime = 500;
		    	}
	    		else 
	    		if (distance < 30) {
	    			sleepTime = 1000;
		    	}
	    		else 
	    		if (distance < 40) {
	    			sleepTime = 2000;
		    	}
	    		else {
	    			sleepTime = 4000;
	    		}
		    	
		    	try {
		    		MediaPlayer player = null;
		    		player = MediaPlayer.create(TrackingActivity.this,R.raw.beep);
	        		player.start();
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	}
	    }
	});
	
	public void getLocation(){
		try {
			locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
			boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			
			if (!isGPSEnabled && !isNetworkEnabled) {
				Log.w("GPS Tracker", "GPS and network not enabled");
			} else {
				if (isGPSEnabled){
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,2,this);
					Log.d("GPS Enabled","GPS Enabled");
					if (locationManager != null){
						location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						if (location != null){
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}
				
				if (isNetworkEnabled){
					if (location == null) {
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,5000,2,this);
						Log.d("Network","Network");
						if (locationManager != null){
							location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
							if (location != null){
								latitude = location.getLatitude();
								longitude = location.getLongitude();
							}
						}
					}
				}
			}
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		if (arg0.getAccuracy() < 2.0 && arg0.getSpeed() < 6.95)
		{
			latitude = arg0.getLatitude();
			longitude = arg0.getLongitude();
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}
}
