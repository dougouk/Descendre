package com.dan190.descendre;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        LocationListener,
        ResultCallback{

    /**Members */
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private Circle circle;
    private LocationManager locationManager;
    private EditText locationSearch;
    private String locationString;
    private Button searchButton, setAlarmButton;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private GoogleApiClient mGoogleAPIClient;
    private LocationRequest locationRequest;
    private Marker chosenDestination;
    private Vibrator vibrator;
    private boolean insideCircle;
    private List<Geofence> mGeofenceList;
    private List<Circle> listOfDestinations_circles;
    Location locationLocation;
    private PendingIntent mGeofencePendingIntent;
    private boolean areGeofencesAdded;

    private static final String ACTIVITY_NAME = "MapsActivity";

    public static final CameraPosition MONTREAL =
            new CameraPosition.Builder().target(new LatLng(45.495, -73.58))
                    .zoom(15.5f)
                    .bearing(0)
                    .tilt(0)
                    .build();

    public static final LatLng MONTREAL_LL = new LatLng(45.495, -73.58);


    /**Methods */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vibrator = (Vibrator) this.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mGoogleAPIClient = new GoogleApiClient.Builder(getApplicationContext())
        .addApi(LocationServices.API)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build();
        createLocationRequest();

        setAlarmButton = (Button) findViewById(R.id.SetAlarm_button);
        setAlarmButton.setVisibility(View.INVISIBLE);
        setAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddGeofenceAtLocation(v);
                Log.d(ACTIVITY_NAME, "Button clicked, AddGeofenceAtLocation(v) called");
                SendGeofence(v);
                Log.d(ACTIVITY_NAME, "SendingGeofence from ButtonClick");

            }
        });
        mGeofenceList = new ArrayList<Geofence>();
        listOfDestinations_circles = new ArrayList<>();
        mapFragment.getMapAsync(this);
        //if(!isGooglePlayServicesAvailable())finish();

    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mUiSettings = mMap.getUiSettings();

        mUiSettings.setZoomControlsEnabled(true);

        startPermission();
        try {
            mMap.setMyLocationEnabled(true);
        }catch(SecurityException e){
            Log.e(ACTIVITY_NAME, e.getMessage());
        }


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d(ACTIVITY_NAME, "map Clicked");
                clearMap();
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                createDestinationMarker(latLng);
                setAlarmButton.setVisibility(View.VISIBLE);

//                addGeofence(latLng);
            }
        });

        mUiSettings.setMapToolbarEnabled(true);

        circle = mMap.addCircle(new CircleOptions()
                .center(MONTREAL_LL)
                .radius(200)
                .strokeColor(Color.BLACK)
                .fillColor(0x00000000));


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(new Criteria(), true);
        try{
            locationLocation = locationManager.getLastKnownLocation(provider);
        }catch (SecurityException e){
            Log.e(ACTIVITY_NAME, e.getMessage());
        }

        if(locationLocation!= null){
            changeCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(
                                    new LatLng(locationLocation.getLatitude(), locationLocation.getLongitude()), 15)));
            onLocationChanged(locationLocation);
        }

        chosenDestination = mMap.addMarker(new MarkerOptions().position(MONTREAL_LL).title("Marker in Montreal"));

        changeCamera(CameraUpdateFactory.newCameraPosition(MONTREAL));
    }

    private void clearMap(){
        mMap.clear();
        setAlarmButton.setVisibility(View.INVISIBLE);
    }

    private void clearRedundant(){
        if(circle != null) {
            circle.remove();
            Log.d(ACTIVITY_NAME, "removed circle");
        }
        if(chosenDestination != null) {
            chosenDestination.remove();
            Log.d(ACTIVITY_NAME, "removed circle");
        }

        if(setAlarmButton.getVisibility() == View.VISIBLE)
        {
            setAlarmButton.setVisibility(View.INVISIBLE);
            Log.d(ACTIVITY_NAME, "removed circle");
        }
    }
    private void createDestinationMarker(LatLng latLng) {
        clearRedundant();
        circle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(200)
                .strokeColor(Color.BLACK)
                .fillColor(0x00000000));
        chosenDestination = mMap.addMarker(new MarkerOptions().position(latLng).title("Picked"));
    }

    public void AddGeofenceAtLocation(View v){
        if(circle.getCenter() == null){
            Log.e(ACTIVITY_NAME, "Circle.getCenter() is null");
            return;
        }
        addGeofence(circle.getCenter());

        Log.d(ACTIVITY_NAME, "calling addGeofence(circle.getCenter())");
    }

    private void addCircleToList(LatLng latLng){
        Circle newCircle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(250)
                .strokeColor(Color.GREEN)
                .fillColor(0x30CCCCFF));
        listOfDestinations_circles.add(newCircle);
        Log.d(ACTIVITY_NAME, "Added new circle to listOfDestinations");
    }


    private void addGeofence(LatLng latlng){
        mGeofenceList.add(new Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence.
        .setRequestId(String.format("fd_%f", latlng.latitude, latlng.longitude))
        .setCircularRegion(
                latlng.latitude,
                latlng.longitude,
                200
        )
        .setExpirationDuration(1000 * 60 * 60) //1 hour
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
        .build());
//        Toast.makeText(getApplicationContext(), "Added geofence", Toast.LENGTH_SHORT).show();
        Log.d(ACTIVITY_NAME, "Added Geofence");
        //createDestinationMarker(latlng);
        addCircleToList(latlng);
    }


    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    public void SendGeofence(View v){
        Log.d(ACTIVITY_NAME, String.format("Geofence list size : %d", mGeofenceList.size()));
        try{
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleAPIClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        }catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(ACTIVITY_NAME, "Need FINE_ACCESS_LOCATION permission");
//            logSecurityException(securityException);
        }
//        Toast.makeText(getApplicationContext(), "Geofences added to Google Client",Toast.LENGTH_SHORT).show();
        Log.d(ACTIVITY_NAME, "Geofences added to Google Client");
    }
    private PendingIntent getGeofencePendingIntent(){
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            Log.d(ACTIVITY_NAME, "pendingIntent already exists");

            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    private void changeCamera(CameraUpdate update) {
        changeCamera(update, null);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {}

    @Override
    public void onMapClick(LatLng latLng) {

    }

    private void changeCamera(CameraUpdate update, CancelableCallback callback) {
        mMap.animateCamera(update);
    }

    private void startPermission(){
       // if (ContextCompat.checkSelfPermission(this, Manifest.permission.))
        if(ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)){

            //user has previously seen permission dialogue
        }
        else{
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(ACTIVITY_NAME, "Location Changed");

//        Toast.makeText(getApplicationContext(), "Location Changed", Toast.LENGTH_SHORT).show();
        //checkBoundary(location);
    }

    public void onMapSearch(View v){
        locationSearch = (EditText) findViewById(R.id.search_bar);
        locationString = locationSearch.getText().toString();
        searchButton = (Button) findViewById(R.id.search_button);

        List<Address> addressList = null;
        if(locationString != null || !locationString.equals("")){
            Geocoder geocoder = new Geocoder(this);
            try{
                addressList = geocoder.getFromLocationName(locationString,1);
            }catch(IOException e){
                e.printStackTrace();
            }

            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            //mMap.clear();
            //instead of clearing the whole map, clear the previous circle
            clearRedundant();
            chosenDestination = mMap.addMarker(new MarkerOptions().position(latLng).title("Searched"));
            circle = mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(200)
                    .strokeColor(Color.BLACK)
                    .fillColor(0x00000000));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleAPIClient, locationRequest, this);
        }catch(SecurityException e){
            Log.e(ACTIVITY_NAME, e.getMessage());
        }
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleAPIClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onStart(){
        super.onStart();
        mGoogleAPIClient.connect();
        Log.d(ACTIVITY_NAME, "onStart()");

    }

    @Override
    public void onPause(){
        super.onPause();
        //stopLocationUpdates();
//        Toast.makeText(getApplicationContext(), "onPause()", Toast.LENGTH_SHORT).show();
        Log.d(ACTIVITY_NAME, "onPause()");
    }

    @Override
    public void onResume(){
        super.onResume();
        if(mGoogleAPIClient.isConnected()) startLocationUpdates();
        Log.d(ACTIVITY_NAME, "onResume()");
    }
    @Override
    public void onStop(){
        super.onStop();
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleAPIClient,
                getGeofencePendingIntent()
        ).setResultCallback(this);
        mGoogleAPIClient.disconnect();
    }


    float[] distance ;
    private void checkBoundary(Location updatedLocation){
        distance = new float[2];
        Location.distanceBetween(updatedLocation.getLatitude(), updatedLocation.getLongitude(),
                circle.getCenter().latitude, circle.getCenter().longitude, distance);
        if(distance[0] > circle.getRadius()){
            //do nothing
            if(insideCircle)insideCircle=false;
        }else{
            //Toast.makeText(getApplicationContext(), "INSIDE CIRCLE", Toast.LENGTH_SHORT).show();
            if(insideCircle)return;
            insideCircle = true;
            createNotification();
            vibrator.vibrate(800);
        }
    }

    private void createNotification(){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.checkmark)
                        .setContentTitle("Arriving")
                        .setContentText("Close to destination!");
        mBuilder.setAutoCancel(true);
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(001, mBuilder.build());
    }


    @Override
    public void onResult(@NonNull Result result) {
        if(result.getStatus().isSuccess()){
//            areGeofencesAdded = !areGeofencesAdded;
            Log.d(ACTIVITY_NAME, "Geofences successfully added");
        }else
        {
            String message = result.getStatus().toString();
            Log.d(ACTIVITY_NAME, "Geofences not added successfully");
            Log.d(ACTIVITY_NAME, message);

        }
    }
}
