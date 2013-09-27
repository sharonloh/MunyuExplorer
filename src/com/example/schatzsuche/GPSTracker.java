package com.example.schatzsuche;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class GPSTracker extends Service implements LocationListener{
	private final Context context;
	boolean isGPSEnabled = false;
	boolean isNetworkEnabled = false;
	boolean canGetLocation = false;
	Location location;
	double latitude;
	double longitude;
	protected LocationManager locationManager;
	
	public GPSTracker(Context context){
		this.context = context;
		getLocation();
	}
	
	public Location getLocation(){
		try {
			locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			
			if (!isGPSEnabled && !isNetworkEnabled) {
				Log.w("GPS Tracker", "GPS and network not enabled");
			} else {
				this.canGetLocation = true;
				if (isNetworkEnabled){
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,30000,1,this);
					Log.d("Network","Network");
					if (locationManager != null){
						location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (location != null){
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}
				
				if (isGPSEnabled){
					if (location == null) {
						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,30000,1,this);
						Log.d("GPS Enabled","GPS Enabled");
						if (locationManager != null){
							location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
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
		
		return location;
	}
	
	public void stopUsingGPS(){
		if (locationManager != null){
			locationManager.removeUpdates(GPSTracker.this);
		}
	}
	
	public double getLatitude(){
		if (location != null){
			latitude = location.getLatitude();
		}
		
		return latitude;
	}
	
	public double getLongitude(){
		if (location != null){
			longitude = location.getLongitude();
		}
		
		return longitude;
	}
	
	public boolean canGetLocation(){
		return this.canGetLocation;
	}
	
	public void showSettingsAlert(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
		alertDialog.setTitle("GPS is settings");
		alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
		alertDialog.setPositiveButton("Settings",new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog,int which){
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				context.startActivity(intent);
			}
		});
		alertDialog.setNegativeButton("Cancel",new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog,int which){
				dialog.cancel();
			}
		});
		
		alertDialog.show();
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		Log.e("POSISI",String.valueOf(latitude));
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

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
