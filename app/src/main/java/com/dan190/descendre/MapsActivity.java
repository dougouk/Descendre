package com.dan190.descendre;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
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
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerClickListener,
        LocationListener,
        ResultCallback{

    /**Members */
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private Circle circle, currentlySelectedCircle;
    private LocationManager locationManager;
    //private EditText locationSearch;
    private String locationString, provider;
    private Button deleteMarkerButton, setMarkerAsDestinationButton;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private GoogleApiClient mGoogleAPIClient;
    private LocationRequest locationRequest;
    private Marker chosenMarker, currentlySelectedMarker;
    private Vibrator vibrator;
    private boolean insideCircle;
    private List<Geofence> mGeofenceList;
    private Map<Marker, Circle> destinationDictionary;
    Location locationLocation;
    private PendingIntent mGeofencePendingIntent;
    private boolean areGeofencesAdded, isMarkerClickedOnExistingDestination;
    private UserState userState;
    private PlaceAutocompleteFragment placeAutocompleteFragment;

    private static MapsActivity instance;

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
        instance = this;
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
        /*locationSearch = (EditText) findViewById(R.id.search_bar);
        locationSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.d(ACTIVITY_NAME, "onKey()");
                if((event.getAction() == KeyEvent.ACTION_DOWN) &&
                (keyCode == KeyEvent.KEYCODE_ENTER)){
                    onMapSearch(v);
                    return true;
                }
                return false;
            }
        });*/

        setMarkerAsDestinationButton = (Button) findViewById(R.id.makeGeofenceAtMarker_button);
        setMarkerAsDestinationButton.setVisibility(View.INVISIBLE);
        setMarkerAsDestinationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(ACTIVITY_NAME, "setMarkerAsDestinationButton clicked");
                AddGeofenceAtLocation(v);
                GeofenceManager.SendGeofence(v, mGeofenceList, mGoogleAPIClient, mGeofencePendingIntent);
                userState = UserState.ADDING_MARKER;
                clearRedundant();
            }
        });
        deleteMarkerButton = (Button) findViewById(R.id.deleteMarker_button);
        deleteMarkerButton.setVisibility(View.INVISIBLE);
        deleteMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userState = UserState.DELETING_MARKER;
                clearRedundant();
            }
        });
        mGeofenceList = new ArrayList<Geofence>();
        destinationDictionary = new HashMap<Marker, Circle>() {};
        mapFragment.getMapAsync(this);
        placeAutocompleteFragment= (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.searchFragment);
        placeAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.d(ACTIVITY_NAME, "Selected place Add is : " + place.getAddress().toString());
                Log.d(ACTIVITY_NAME, "Selected place LatLng is : " + place.getLatLng().toString());
                clearRedundant();
                chosenMarker = mMap.addMarker(new MarkerOptions()
                        .position(place.getLatLng())
                        .title(place.getAddress().toString()));
                circle = mMap.addCircle(new CircleOptions()
                        .center(place.getLatLng())
                        .radius(200)
                        .strokeColor(Color.BLACK)
                        .fillColor(0x00000000));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
            }

            @Override
            public void onError(Status status) {
                Log.e(ACTIVITY_NAME, status.getStatusMessage());
            }
        });



    }
    public static MapsActivity getInstance(){
        return instance;
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
                Log.d(ACTIVITY_NAME, latLng.toString());
                clearRedundant();
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                createDestinationMarker(latLng);
                setMarkerAsDestinationButton.setVisibility(View.VISIBLE);
            }
        });

        mMap.setOnMarkerClickListener(this);
        mUiSettings.setMapToolbarEnabled(true);

        if(locationLocation!= null){
            chosenMarker = mMap.addMarker(new MarkerOptions().position(
                    new LatLng(locationLocation.getLatitude(), locationLocation.getLongitude())).
                    title("You are here!"));

            changeCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(
                                    new LatLng(locationLocation.getLatitude(), locationLocation.getLongitude()), 15)));
            onLocationChanged(locationLocation);
        }
        //changeCamera(CameraUpdateFactory.newCameraPosition(MONTREAL));
    }

    private void clearMap(){
        mMap.clear();
    }

    private void clearRedundant(){
        if(userState != UserState.SELECTING_MARKER){
            if(circle != null) {
                circle.remove();
                //Log.d(ACTIVITY_NAME, "removed circle");
            }
            if(chosenMarker != null) {
                chosenMarker.remove();
                //Log.d(ACTIVITY_NAME, "removed circle");
            }
        }

        isMarkerClickedOnExistingDestination = false;

        if(deleteMarkerButton.getVisibility() == View.VISIBLE){
            deleteMarkerButton.setVisibility(View.INVISIBLE);
        }

        if(setMarkerAsDestinationButton.getVisibility() == View.VISIBLE){
            setMarkerAsDestinationButton.setVisibility(View.INVISIBLE);
        }
        Log.d(ACTIVITY_NAME, "clearRedundant()");
    }
    private void createDestinationMarker(LatLng latLng) {
        clearRedundant();
        circle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(200)
                .strokeColor(Color.BLACK)
                .fillColor(0x00000000));
        chosenMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Picked"));
        userState = UserState.NORMAL;
    }

    public void AddGeofenceAtLocation(View v){
        if(chosenMarker.getPosition() == null){
            Log.e(ACTIVITY_NAME, "chosenMarker is null");
            return;
        }
        GeofenceManager.addGeofence(mMap, chosenMarker.getPosition(), mGeofenceList,destinationDictionary);

        Log.d(ACTIVITY_NAME, "calling addGeofence(chosenMarker.getPosition())");
    }

    private void changeCamera(CameraUpdate update) {
        changeCamera(update, null);
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

    /*public void onMapSearch(View v){
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
            chosenMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Searched"));
            circle = mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(200)
                    .strokeColor(Color.BLACK)
                    .fillColor(0x00000000));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

        }
    }
*/
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
        Log.i(ACTIVITY_NAME, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(ACTIVITY_NAME, "onConnectionFailed");
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.d(ACTIVITY_NAME, "onStart()");

        if(!mGoogleAPIClient.isConnected()){
            mGoogleAPIClient.connect();
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), true);
        try{
            locationLocation = locationManager.getLastKnownLocation(provider);
            locationManager.requestLocationUpdates(provider, 1000, 10, new android.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.d("Background", "Location changed");
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });

        }catch (SecurityException e){
            Log.e(ACTIVITY_NAME, e.getMessage());
        }
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
        //stopLocationUpdates();
//       LocationServices.GeofencingApi.removeGeofences(
//                mGoogleAPIClient,
//                GeofenceManager.getGeofencePendingIntent(mGeofencePendingIntent)
//        ).setResultCallback(this);
        if(mGoogleAPIClient!=null){
            mGoogleAPIClient.disconnect();
        }
        Log.d(ACTIVITY_NAME, "onStop()");
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        boolean marker_exists_in_dictionary = false;

        //check if marker exists in geofence
        for(Marker m : destinationDictionary.keySet()){
            if (m.equals(marker)){
                marker_exists_in_dictionary = true;
                chosenMarker = m;
                circle = destinationDictionary.get(m);
            }
        }

        if(marker_exists_in_dictionary){
            isMarkerClickedOnExistingDestination= true;
            chosenMarker.showInfoWindow();

            deleteMarkerButton.setVisibility(View.VISIBLE);
        }
        else{
            setMarkerAsDestinationButton.setVisibility(View.VISIBLE);
        }
        userState = UserState.SELECTING_MARKER;
        Log.d(ACTIVITY_NAME, marker.getTitle() + " clicked");
//        Log.d(ACTIVITY_NAME, "Cirlce getcenter(): " + circle.getCenter().toString());


        return false;
    }
}
