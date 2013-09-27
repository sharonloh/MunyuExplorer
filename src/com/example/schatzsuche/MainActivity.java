package com.example.schatzsuche;

import java.util.List;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.schatzsuche.TrackingActivity;
import com.example.schatzsuche.R;
import com.example.schatzsuche.treasure;

public class MainActivity extends Activity implements Runnable,LocationListener{
	
	private final double Radius = 6371;
	private final double Rds = Math.PI/180;
	private boolean isGPSEnabled = false;
	private boolean isNetworkEnabled = false;
	private boolean canGetLocation = false;
	private Location location;
	protected LocationManager locationManager;
	private Button refresh;
	private Button reset;
	private SurfaceView surface;
	private SurfaceHolder holder;
	private boolean locker = true;
	private boolean reload = false;
	private int radarLine = 0;
	private Thread thread;
	private TextView collected;
	private TextView latitudeField;
	private TextView longitudeField;
	private WebService ws = new WebService();
	//GPSTracker gps;
	treasure[] chest = new treasure[30];
	int numberOfChest = 0;
	static String jsonResponseString = "";
	private double latitude = 0;
	private double longitude = 0;
	private Bitmap boy;
	private Bitmap chestIcon;
	private SpriteAnimation boy2;
	private String signalWifi = "";
	public static int CollectedChest = 0; //jumlah chest yang sudah di peroleh
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //gps = new GPSTracker(MainActivity.this);
        //Log.e("CREATE","CREATEEEEEEEEEEEEEEEEEEE");
        setContentView(R.layout.activity_main);
        loadRadarMode();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    protected void onPause() {
    	//Log.e("PAUSE","PAUSEEEEEEEEEEEEEEEEEEE");
    	super.onPause();
    	pause();
    }
    
    private void pause() {
    	locker = false;
    	while(true){
    		try {
          thread.join();
        } catch (InterruptedException e) {e.printStackTrace();
        }
        break;
      }
      thread = null;
    }
    
    @Override
    protected void onResume() {
    	//Log.e("RESUME","RESUMEEEEEEEEEEEEEEEEEE");
    	super.onResume();
    	resume();    
    }

    private void resume() {
    	locker = true;
    	thread = new Thread(this);
    	thread.start();
    }
    @Override
  	public void onBackPressed() {
  		super.onBackPressed();
  		Intent homeIntent = new Intent(Intent.ACTION_MAIN);
  		homeIntent.addCategory(Intent.CATEGORY_HOME);
  		homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  		startActivity(homeIntent);
  	}
    

    public void loadRadarMode(){
    	refresh = (Button) findViewById(R.id.buttonrefresh);
    	reset = (Button) findViewById(R.id.buttonreset);
    	collected = (TextView)findViewById(R.id.textView2);
    	latitudeField = (TextView)findViewById(R.id.textView4);
    	longitudeField = (TextView)findViewById(R.id.textView6);
    	boy = BitmapFactory.decodeResource(getResources(), R.drawable.boy);
    	boy2 = new SpriteAnimation(boy,0,0,48,48,4,4);
    	chestIcon = BitmapFactory.decodeResource(getResources(), R.drawable.chest);
    	surface = (SurfaceView) findViewById(R.id.mysurface);
        holder = surface.getHolder();
        getLocation();
        if (canGetLocation) {
        	//latitudeField.setText(String.valueOf(latitude));
			//longitudeField.setText(String.valueOf(longitude));
			//chest = new treasure[30]; 
			//dummyChest();
        	try {
				ws.retrieveChestLocation(latitude,longitude);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				//System.out.println("json Response = " + jsonResponseString);
				parseRetrieveJSON();
				reload = true;
			}
        } 
        else 
        {
			showSettingsAlert();
		}
        thread = new Thread(this);
        thread.start();
        refresh.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (canGetLocation) {
					//latitude = location.getLatitude();
					//longitude = location.getLongitude();
					//latitudeField.setText(String.valueOf(latitude));
					//longitudeField.setText(String.valueOf(longitude));
					//chest = new treasure[30]; 
					//dummyChest();
					//reload = true;
					try {
						ws.retrieveChestLocation(latitude,longitude);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
						//System.out.println("json Response = " + jsonResponseString);
						parseRetrieveJSON();
						reload = true;
					}
				} else {
					Log.w("GPS Get Location", "NO!");
					showSettingsAlert();
				}
				cekWifi();
			}
		});
        
        reset.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showResetAlert();
			}
		});
    }
    
    private void parseRetrieveJSON() {
        JSONObject jsonResponse;
        try {
  			jsonResponse = new JSONObject(jsonResponseString);
  	        if ("success".equals(jsonResponse.optString("status").toString())) {
  	        	chest = new treasure[30]; 
  	            JSONArray chestArray = jsonResponse.optJSONArray("data");
  	            System.out.println("chestArray : " + chestArray);
  	            if ((chestArray != null) && (!"null".equals(chestArray)) ){
  	            	numberOfChest = chestArray.length();
  	            	for (int i=0; i<chestArray.length(); i++) {
	  	            	JSONObject chestNode = chestArray.getJSONObject(i);
	  	            	String id = chestNode.optString("id").toString();
	  	            	String bssid = chestNode.optString("bssid").toString();
	  	            	double distance = Double.parseDouble(chestNode.optString("distance").toString());
	  	            	double degree = Double.parseDouble(chestNode.optString("degree").toString());
	  	            	chest[i] = new treasure();
	  	            	chest[i].setId(id);
	  	            	chest[i].setBssid(bssid);
	  	            	chest[i].setDistance(distance/1000);
	  	            	chest[i].setDegree(degree); 
	  	            	double lat2 = Math.asin(Math.sin(latitude*Rds)*Math.cos(chest[i].getDistance()/Radius*Rds) + Math.cos(latitude*Rds)*Math.sin(chest[i].getDistance()/Radius*Rds)*Math.cos(chest[i].getDegree()*Rds)) * 180 / Math.PI;
		  	      	  	chest[i].setLatitude(lat2);
		  	      	  	double lon2 = longitude + Math.atan2(Math.sin(chest[i].getDegree()*Rds)*Math.sin(chest[i].getDistance()/Radius*Rds)*Math.cos(latitude*Rds),Math.cos(chest[i].getDistance()/Radius*Rds)-Math.sin(latitude*Rds)*Math.sin(chest[i].getLatitude()*Rds)) * 180 / Math.PI;
		  	      	  	chest[i].setLongitude(lon2);
	  	            	Log.w("JSON Object ke-", ""+i);
	  	            	Log.w("id",id);
	  	            	Log.w("bssid",bssid);
	  	            	Log.w("distance",""+distance);
	  	            	Log.w("degree",""+degree);
	  	            }	
  	        	}
  	        }
  	        else {
  	        	String fail_desc = jsonResponse.optString("description").toString();
  	        	System.out.println("Request failed : " + fail_desc);
  	        }
  		} catch (JSONException e) {
  			e.printStackTrace();
  		}
  	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (locker){
			if (!holder.getSurface().isValid()){
				continue;
			}
			boy2.update(System.currentTimeMillis());
			Canvas canvas = holder.lockCanvas();
			draw(canvas);
			holder.unlockCanvasAndPost(canvas);
			
		}
	}
    
	private void draw(Canvas canvas) {
		//canvas.drawColor(android.R.color.background_dark);
		float total = 0;
		Paint paint = new Paint();
		
		RectF r = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
		paint.setARGB(200, 0, 0, 0);
		canvas.drawRect(r , paint );
		
		paint.setARGB(200, 11, 223, 25);
		
		while (total < canvas.getWidth() || total < canvas.getHeight()){
			canvas.drawLine(total, 0, total, canvas.getHeight(), paint);
			canvas.drawLine(0, total, canvas.getHeight(), total, paint);
			total += 20;
		}
		
		if (reload){
	    	updatePos(canvas);
	    	reload = false;
	    }
		
		paint.setARGB(200, 255, 255, 0);
		paint.setTextSize(20); 
		int i = 0;
	    while(chest[i] != null)
	    {
	    	/*
	    	if (reload)
	    	{
		    	double x = canvas.getWidth()/2 + calcuDistance(chest[i].getLatitude(),chest[i].getLongitude())*1000 * Math.sin(calcuDegree(chest[i].getLatitude(),chest[i].getLongitude())*Rds) * 2.5; 
		    	double y = canvas.getHeight()/2 - calcuDistance(chest[i].getLatitude(),chest[i].getLongitude())*1000 * Math.cos(calcuDegree(chest[i].getLatitude(),chest[i].getLongitude())*Rds) * 2.5;		
		    	chest[i].setX((float) x);
		    	chest[i].setY( (float) canvas.getHeight() - (float) y);
		    }*/
	    	double x = chest[i].getX();
	    	double y = canvas.getHeight() - chest[i].getY();
	    	canvas.drawBitmap(chestIcon, (float)x - chestIcon.getWidth()/2 , (float)y - chestIcon.getHeight()/2, null);
	    	canvas.drawText(String.valueOf(x),(float) x, (float)y-50, paint);
	    	canvas.drawText(String.valueOf(y),(float) x, (float)y-30, paint);
	    	canvas.drawText(String.valueOf(calcuDistance(chest[i].getLatitude(),chest[i].getLongitude())*1000),(float) x+20, (float)y, paint);
	    	i++;
	    }
	    
	    //canvas.drawBitmap(boy, canvas.getWidth()/2-boy.getWidth()/2, canvas.getHeight()/2-boy.getHeight()/2, null);
	    boy2.setX((int) canvas.getWidth()/2 - 24);
	    boy2.setY((int) canvas.getHeight()/2 - 24);
	    boy2.draw(canvas);
	    
	    canvas.drawText(String.valueOf(latitude), 10, 25, paint);
	    canvas.drawText(String.valueOf(longitude), 10, 45, paint);
	    
	    paint.setARGB(200, 11, 223, 25);
	    paint.setStrokeWidth(5);
	    canvas.drawLine(0, radarLine, canvas.getWidth(), radarLine, paint);
	    
	    if (radarLine < canvas.getHeight()){
	    	radarLine++;
	    } else {
	    	radarLine = 0;
	    	//reload = true;
	    }
	    
	}
	
	private void updatePos(Canvas canvas){
		int i = 0;
	    while(chest[i] != null)
	    {
    		double x = canvas.getWidth()/2 + calcuDistance(chest[i].getLatitude(),chest[i].getLongitude())*1000 * Math.sin(calcuDegree(chest[i].getLatitude(),chest[i].getLongitude())*Rds) * 2.5; 
	    	double y = canvas.getHeight()/2 - calcuDistance(chest[i].getLatitude(),chest[i].getLongitude())*1000 * Math.cos(calcuDegree(chest[i].getLatitude(),chest[i].getLongitude())*Rds) * 2.5;		
	    	chest[i].setX((float) x);
	    	chest[i].setY( (float) canvas.getHeight() - (float) y);
	    	i++;
	    }
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
    	return Radius * c;
  	}
  	private double calcuDegree(double lan,double lon)
  	{
  		double dLat = (lan-latitude)*Rds;
  	  	double dLon = (lon-longitude)*Rds;
  	  	double lati1 = latitude * Rds;
  	  	double lati2 = lan*Rds;
  		double y = Math.sin(dLon) * Math.cos(lati2);
  	  	double x = Math.cos(lati1)*Math.sin(lati2) -
  	  	        Math.sin(lati1)*Math.cos(lati2)*Math.cos(dLon);
  	  	return Math.atan2(y, x) * 180 / Math.PI;
  	}
	
	private void dummyChest(){
		chest[0] = new treasure();
	  	chest[0].setId("05c71bc4e5c6a24978412fe85930b164");
	  	chest[0].setBssid("f8:d1:11:41:46:42");
	  	chest[0].setDistance(0.052011639337075);
	  	chest[0].setDegree(53); 
	  	double lat2 = Math.asin(Math.sin(latitude*Rds)*Math.cos(chest[0].getDistance()/Radius*Rds) + Math.cos(latitude*Rds)*Math.sin(chest[0].getDistance()/Radius*Rds)*Math.cos(chest[0].getDegree()*Rds)) * 180 / Math.PI;
	  	chest[0].setLatitude(lat2);
	  	double lon2 = longitude + Math.atan2(Math.sin(chest[0].getDegree()*Rds)*Math.sin(chest[0].getDistance()/Radius*Rds)*Math.cos(latitude*Rds),Math.cos(chest[0].getDistance()/Radius*Rds)-Math.sin(latitude*Rds)*Math.sin(chest[0].getLatitude()*Rds)) * 180 / Math.PI;
	  	chest[0].setLongitude(lon2);
	  	
			chest[1] = new treasure();
	  	chest[1].setId("0e5149f385686626c2fe092be1514b2a");
	  	chest[1].setBssid("58:93:96:15:b9:68");
	  	chest[1].setDistance(0.081021641334646);
	  	chest[1].setDegree(177); 
	  	lat2 = Math.asin(Math.sin(latitude*Rds)*Math.cos(chest[1].getDistance()/Radius*Rds) + Math.cos(latitude*Rds)*Math.sin(chest[1].getDistance()/Radius*Rds)*Math.cos(chest[1].getDegree()*Rds)) * 180 / Math.PI;
	  	chest[1].setLatitude(lat2);
	  	lon2 = longitude + Math.atan2(Math.sin(chest[1].getDegree()*Rds)*Math.sin(chest[1].getDistance()/Radius*Rds)*Math.cos(latitude*Rds),Math.cos(chest[1].getDistance()/Radius*Rds)-Math.sin(latitude*Rds)*Math.sin(chest[1].getLatitude()*Rds)) * 180 / Math.PI;
	  	chest[1].setLongitude(lon2);
	  	
	  	chest[2] = new treasure();
	  	chest[2].setId("90bcf2b7558f8817bc2369314bc2bbae");
	  	chest[2].setBssid("c0:c1:c0:36:8f:5b");
	  	chest[2].setDistance(0.073452433817486);
	  	chest[2].setDegree(238); 
	  	lat2 = Math.asin(Math.sin(latitude*Rds)*Math.cos(chest[2].getDistance()/Radius*Rds) + Math.cos(latitude*Rds)*Math.sin(chest[2].getDistance()/Radius*Rds)*Math.cos(chest[2].getDegree()*Rds)) * 180 / Math.PI;
	  	chest[2].setLatitude(lat2);
	  	lon2 = longitude + Math.atan2(Math.sin(chest[2].getDegree()*Rds)*Math.sin(chest[2].getDistance()/Radius*Rds)*Math.cos(latitude*Rds),Math.cos(chest[2].getDistance()/Radius*Rds)-Math.sin(latitude*Rds)*Math.sin(chest[2].getLatitude()*Rds)) * 180 / Math.PI;	
	  	chest[2].setLongitude(lon2);
	  	
	  	chest[3] = new treasure();
	  	chest[3].setId("a39a86e56dfd724e977a90a520f6a693");
	  	chest[3].setBssid("68:7f:74:70:11:1c");
	  	chest[3].setDistance(0.057672292137238);
	  	chest[3].setDegree(259);
	  	lat2 = Math.asin(Math.sin(latitude*Rds)*Math.cos(chest[3].getDistance()/Radius*Rds) + Math.cos(latitude*Rds)*Math.sin(chest[3].getDistance()/Radius*Rds)*Math.cos(chest[3].getDegree()*Rds)) * 180 / Math.PI;
	  	chest[3].setLatitude(lat2);
	  	lon2 = longitude + Math.atan2(Math.sin(chest[3].getDegree()*Rds)*Math.sin(chest[3].getDistance()/Radius*Rds)*Math.cos(latitude*Rds),Math.cos(chest[3].getDistance()/Radius*Rds)-Math.sin(latitude*Rds)*Math.sin(chest[3].getLatitude()*Rds)) * 180 / Math.PI;
	  	chest[3].setLongitude(lon2);
		
	}
	
	public void getLocation(){
		try {
			locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			
			if (!isGPSEnabled && !isNetworkEnabled) {
				Log.w("GPS Tracker", "GPS and network not enabled");
			} else {
				this.canGetLocation = true;
				
				if (isGPSEnabled){
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
					Log.d("GPS Enabled","GPS Enabled");
					if (locationManager != null){
						location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						if (location != null){
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}
				/*
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
				}*/
			}
			
			latitudeField.setText(String.valueOf(latitude));
			longitudeField.setText(String.valueOf(longitude));
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onLocationChanged(Location arg0) {
		latitudeField.setText(String.valueOf(arg0.getLatitude()));
		longitudeField.setText(String.valueOf(arg0.getLongitude()));
		//collected.setText(String.valueOf(arg0.getAccuracy()));
		if (arg0.getAccuracy() < 10.0 && arg0.getSpeed() < 3.0)
		{
			// TODO Auto-generated method stub
			latitude = arg0.getLatitude();
			longitude = arg0.getLongitude();
			latitudeField.setText(String.valueOf(arg0.getLatitude()));
			longitudeField.setText(String.valueOf(arg0.getLongitude()));
		}
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
	
	public void showSettingsAlert(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("GPS is settings");
		alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
		alertDialog.setPositiveButton("Settings",new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog,int which){
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
			}
		});
		alertDialog.setNegativeButton("Cancel",new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog,int which){
				dialog.cancel();
			}
		});
		
		alertDialog.show();
	}
	
	public void showResetAlert(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("Reset Chest");
		alertDialog.setMessage("Do you want to reset all chest?");
		alertDialog.setPositiveButton("Reset coi",new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog,int which){
				try {
					ws.resetChest();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					if (!"NULL".equals(jsonResponseString)) {
						JSONObject jsonResponse;
						try {
							jsonResponse = new JSONObject(jsonResponseString);
							System.out.println("RESET status : " + jsonResponse.optString("status").toString());
							if ("success".equals(jsonResponse.optString("status").toString())) {
								collected.setText("0");
								chest = new treasure[30];
								CollectedChest = 0;
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
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
  	public boolean onTouchEvent(MotionEvent e){
  	    float x = 0;
  	    float y = 0;
  	    
	  	if(e.getAction() == MotionEvent.ACTION_DOWN){
	        //Log.e("TouchUI", ">>>>>>>>>>>>>> "+e.getX()+" "+e.getY());
	        Display display = getWindowManager().getDefaultDisplay(); 
	        x = e.getX() - 10;
	        y = display.getHeight() - 10 - e.getY();
	        
	        //Log.e("TouchUI", ">>>>>>>>>>>>>> "+x+" "+y);
	        int i = 0;
	        while (chest[i] != null){
	        	if (colision(i,x,y))
	        	{
	        		//Log.e("KEPILIH","XXXXXXXXXXXXXXXXX "+i);
	        		Intent goToTrackingActivity = new Intent(getApplicationContext(), TrackingActivity.class);
	        		goToTrackingActivity.putExtra("id", chest[i].getId());
	        		goToTrackingActivity.putExtra("bssid", chest[i].getBssid());
	        		goToTrackingActivity.putExtra("latitude", chest[i].getLatitude());
	        		goToTrackingActivity.putExtra("longitude", chest[i].getLongitude());
	        		goToTrackingActivity.putExtra("degree", chest[i].getDegree());
	        		goToTrackingActivity.putExtra("distance", chest[i].getDistance());
	        		
	        		startActivity(goToTrackingActivity);
	        	}
	        	i++;
	        }
	  	}
  	    return true;
  	}
	
	public boolean colision(int i, float x, float y){
		if (x > (chest[i].getX() - chestIcon.getWidth()/2) && x < (chest[i].getX() + chestIcon.getWidth()/2)
			&& y > (chest[i].getY() - chestIcon.getHeight()/2) && y < (chest[i].getY() + chestIcon.getHeight()/2))
		{
			return true;
		}
		else
			return false;
	}
	
	public void cekWifi(){
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		List<ScanResult> scanResult = wifiManager.getScanResults();
		//Hasil scanResul itu bssid tiap wifi yang ke detect sama wifi
		if (scanResult != null){
			for (int i = 0; i<scanResult.size(); i++){
				// ini ngeprint hasil kecepatan wifi dalam db
				Log.e("SCANRESULT","Speed of wifi = "+scanResult.get(i).level);
				// ini ngeprint hasil bssid tiap wifi
				Log.e("BSSI",scanResult.get(i).BSSID);
				// jd untuk webservice nya ntar bandingin bssid ny
				// kalo sama ambil kecepatannya trus kirim ke webservice ny gitu hehe
			}
		}
	}
	
}
