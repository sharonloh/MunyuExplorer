package com.example.schatzsuche;

public class treasure {
	String id;
	String bssid;
	double distance;
	double degree;
	double latitude; 
	double longitude;
	float x;
	float y;
	
	public treasure() {
		id = "";
		bssid ="";
		distance = 0;
		degree = 0;
	}
	
	public treasure(String id, String bssid, double distance, double degree) {
		super();
		this.id = id;
		this.bssid = bssid;
		this.distance = distance;
		this.degree = degree;
		x = -50;
		y = -50;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getBssid() {
		return bssid;
	}
	public void setBssid(String bssid) {
		this.bssid = bssid;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public double getDegree() {
		return degree;
	}
	public void setDegree(double degree) {
		this.degree = degree;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public void setX(float x){
		this.x = x;
	}
	
	public void setY(float y){
		this.y = y;
	}
	
	public float getX(){
		return x;
	}
	
	public float getY(){
		return y;
	}
}
