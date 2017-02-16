package com.dan190.descendre.Geofence;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Dan on 24/11/2016.
 */

public class MyGeofence {
    private String key;
    private Marker marker;
    private Circle circle;
    private Geofence geofence;
    private LatLng latLngCenter;
    private String address;

    // distance from user
    private float distance;



    public MyGeofence(LatLng latLng){
//        this.marker = marker;
//        this.circle = circle;
        this.circle = null;
        this.marker = null;
        this.latLngCenter = latLng;
        this.geofence = null;
        setKey();
    }

    private void setKey(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        key = String.format("%s_%#.6f_%#.6f",
                dateFormat.format(Calendar.getInstance().getTime()),
                latLngCenter.latitude,
                latLngCenter.longitude);
    }
    public String getKey(){
        return key;
    }

    public LatLng getCenter(){
        return latLngCenter;
    }

    public Marker getMarker(){
        return marker;
    }
    public void setMarker(Marker marker){ this.marker = marker;}

    public Circle getCircle(){
        return circle;
    }
    public void setCircle(Circle circle){
        this.circle = circle;
    }

    public Geofence getGeofence() {
        return geofence;
    }

    public void setGeofence(Geofence geofence) {
        this.geofence = geofence;
    }

    public String getAddress(){
        return address;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
