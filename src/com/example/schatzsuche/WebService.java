package com.example.schatzsuche;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.util.Log;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;


public class WebService {
	String URL;
	String group_id;
	
	public WebService() {
		super();
		URL = "http://milestone.if.itb.ac.id/pbd/index.php";
		group_id = "e6260c852b5b9f063bb83c549e7e7c28";
	}
	
	/* 
	 * HTTP Method		: POST
	 * HTTP Response	: {status : success} 
	 * 					  {status : “failed”, description : “<error_message>”}
	 */
	public void resetChest() throws InterruptedException {
		final HttpClient httpclient = new DefaultHttpClient();
		final HttpPost httppost = new HttpPost(URL);
		
		final List<NameValuePair> requestParameter = new ArrayList<NameValuePair>(2);
		requestParameter.add(new BasicNameValuePair("group_id", group_id));
		requestParameter.add(new BasicNameValuePair("action", "reset"));
		
		Thread thread = new Thread(new Runnable(){
		    @Override
		    public void run() {
				try {
					httppost.setEntity(new UrlEncodedFormEntity(requestParameter));
					HttpResponse response = httpclient.execute(httppost);
					
					// Read HTTP Post Request
					BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					String Response = in.readLine();
					Log.w("resetChese response", Response);
					MainActivity.jsonResponseString = Response;
				} catch (ClientProtocolException e) {
					Log.w("ClientProtocolException", e.toString());
					MainActivity.jsonResponseString = "NULL";
				}
				catch (IOException e) {
					Log.w("IOException", e.toString());
					Log.w("IOException", "No Internet Connection");
					MainActivity.jsonResponseString = "NULL";
				}
		    }
		});
		thread.start();
		thread.join();
	}
	
	/* 
	 * HTTP Method		: GET
	 * HTTP Response	: {status : “success”, data : [{id : “<chest_id>”, distance : <distance>, degree: <degree>}, …… ]}
	 *					  {status : “failed”, description : “<error_message>”}
	 */
	public void retrieveChestLocation(double lat, double lon) throws InterruptedException {
		double latitude = lat;//-6.890608;
		double longitude = lon;//107.610008;
		String parameter = URL+"?group_id="+group_id+"&action=retrieve&latitude="+latitude+"&longitude="+longitude;
		Log.w("Retrieve HTTP GET", parameter);
		
		final HttpClient httpclient = new DefaultHttpClient();
	    final HttpGet httpget = new HttpGet(parameter);
	    
		Thread thread = new Thread(new Runnable() {
		    @Override
		    public void run() {
			    try {
			    	HttpResponse response = httpclient.execute(httpget);
			    	System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			    	HttpEntity entity = response.getEntity();
			        if (entity != null){
			            InputStream inputStream = entity.getContent();
			            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			            String line = bufferedReader.readLine();
			            inputStream.close();
			            MainActivity.jsonResponseString = line;
			        } else {
			        	MainActivity.jsonResponseString = "NULL";
			        }
			    } catch (Exception e) {
			    	Log.w("Retrieve Chest Location Exception", e);
			    } 
		    }
		});

		thread.start(); 
		thread.join();
	}
	
	/* 
	 * HTTP Method 		: GET
	 * HTTP Response 	: {status : “success”,  data : <chest_count>} 
	 *					  {status : “failed”, description : “<error_message>”}
	 */
	public void getUnachievedChestCount() {
		HttpClient httpclient = new DefaultHttpClient();
	    HttpGet httpget = new HttpGet(URL+"?group_id="+group_id+"&action=number"); 
	    HttpResponse response;
	    try {
	        response = httpclient.execute(httpget);
	        Log.i("Retrieve Chest Location Response",response.getStatusLine().toString());

	        // Get hold of the response entity
	        HttpEntity entity = response.getEntity();
	        if (entity != null){
	            InputStream inputStream = entity.getContent();
	            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	            StringBuilder stringBuilder = new StringBuilder();
	            String line = null;
	            while ((line = bufferedReader.readLine()) != null) {
	            	stringBuilder.append(line + "\n");
	            }
	            inputStream.close();
	            Log.w("Retrieve Chest Location Response", ""+stringBuilder);
	        }
	    } catch (Exception e) {}
	}
	
	/* HTTP Method		: POST
	 * HTTP Response	: {status : “success”} 
	 *					  {status : “failed”, description : “<error_message>”}
	 * HTTP Req Parameter :
	 *		chest_id        : <id chest yang akan dicek>
	 *		bssid           : <bssid dari wifi yang merepresentasikan chest>
	 *		wifi            : <nilai kekuatan sinyal wifi dalam satuan db>
	 *		latitude        : <posisi lintang group peserta>
	 *		longitude       : <posisi bujur group peserta>
	 *		action          : check
	 */
	public void checkChest(String chest_id, String bssid, String wifi, double latitude, double longitude) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(URL);
		
		List<NameValuePair> requestParameter = new ArrayList<NameValuePair>(6);
		requestParameter.add(new BasicNameValuePair("chest_id", chest_id));
		requestParameter.add(new BasicNameValuePair("bssid", bssid));
		requestParameter.add(new BasicNameValuePair("wifi", wifi));
		requestParameter.add(new BasicNameValuePair("latitude", ""+latitude));
		requestParameter.add(new BasicNameValuePair("longitude", ""+longitude));
		requestParameter.add(new BasicNameValuePair("action", "check"));
		
		try {
			httppost.setEntity(new UrlEncodedFormEntity(requestParameter));
			
			HttpResponse response = httpclient.execute(httppost);
			
			// Read HTTP Post Request
			BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String Response = in.readLine();
			Log.w("resetChese response", Response);
			
		} catch (ClientProtocolException e) {
			Log.w("ClientProtocolException", e.toString());
		}
		catch (IOException e) {
			Log.w("IOException", e.toString());
			Log.w("IOException", "No Internet Connection");
		}
	}
	
	/* 
	 * HTTP Method			: POST
	 * HTTP Response		: {status : “success”} 
	 * 						  {status : “failed”, description : “<error_message>”}
	 * HTTP Req Parameter 	:
	 *	group id         	: <id group peserta>
	 *	chest_id         	: <id chest yang akan diambil>
	 *	file            	: <file image dengan geotag didalamnya>
	 *	bssid            	: <bssid dari wifi yang merepresentasikan chest>
	 *	wifi             	: <nilai kekuatan sinyal wifi dalam satuan db>
	 *	action				: acquire
	 */
	public void acquireChest(final String chest_id, final File image, final String bssid, final String wifi) {
		final HttpClient httpclient = new DefaultHttpClient();
		final HttpContext localContext = new BasicHttpContext();
		final HttpPost httppost = new HttpPost(URL);
		
		final MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		
		Thread thread = new Thread(new Runnable() {
		    @Override
		    public void run() {
			    try {
			    	entity.addPart("file", new FileBody(image));
					entity.addPart("group_id", new StringBody(group_id));
					entity.addPart("chest_id", new StringBody(chest_id));
					entity.addPart("bssid", new StringBody(bssid));
					entity.addPart("wifi", new StringBody(wifi));
					entity.addPart("action", new StringBody("acquire"));
					
					httppost.setEntity(entity);
					
					HttpResponse response = httpclient.execute(httppost, localContext);
					
					// Read HTTP Post Request
					BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					String Response = in.readLine();
					Log.w("resetChese response", Response);
						
					JSONObject jsonResponse;
			        try {
			  			jsonResponse = new JSONObject(Response);
			  			if ("success".equals(jsonResponse.optString("status").toString())) {
			  				System.out.println("sukses!");
			  				MainActivity.CollectedChest += 1;
			  	        }
			  	        else {
			  	        	String fail_desc = jsonResponse.optString("description").toString();
			  	        	System.out.println("Request failed : " + fail_desc);
			  	        	// TODO : kasih notif gagal
			  	        }
			  		} catch (JSONException e) {
			  			e.printStackTrace();
			  		}
					
				} catch (ClientProtocolException e) {
					Log.w("ClientProtocolException", e.toString());
				}
				catch (IOException e) {
					Log.w("IOException", e.toString());
					Log.w("IOException", "No Internet Connection");
				}
		    }
		});
		thread.start(); 
		//thread.join();
	}
}
