package com.dan190.descendre;

import android.Manifest;
import android.app.NotificationManager;
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
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
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
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        LocationListener{

    /**Members */
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private Circle circle;
    private LocationManager locationManager;
    private EditText locationSearch;
    private String location;
    private Button searchButton;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
    private GoogleApiClient mGoogleAPIClient;
    private LocationRequest locationRequest;
    private Marker chosenDestination;
    private Vibrator vibrator;
    private boolean insideCircle;



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
        if(!isGooglePlayServicesAvailable())finish();
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
        mapFragment.getMapAsync(this);
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
        mMap.setMyLocationEnabled(true); //will stay underlined red
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Toast.makeText(getApplicationContext(),
                        "Clicked " + latLng.toString(),
                        Toast.LENGTH_LONG).show();
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mMap.clear();
                circle = mMap.addCircle(new CircleOptions()
                        .center(latLng)
                        .radius(200)
                        .strokeColor(Color.BLACK)
                        .fillColor(0x00000000));
                chosenDestination = mMap.addMarker(new MarkerOptions().position(latLng).title("Picked"));

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

        Location location = locationManager.getLastKnownLocation(provider);
        if(location!= null){
            changeCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(
                                    new LatLng(location.getLatitude(), location.getLongitude()), 15)));
            onLocationChanged(location);
        }

        mMap.addMarker(new MarkerOptions().position(MONTREAL_LL).title("Marker in Montreal"));

        changeCamera(CameraUpdateFactory.newCameraPosition(MONTREAL));
    }


    public void onGoToMontreal(View v){
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(MONTREAL), 1000, null);
    }

    private void changeCamera(CameraUpdate update) {
        changeCamera(update, null);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

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
        Toast.makeText(getApplicationContext(), "Location Changed", Toast.LENGTH_SHORT).show();
        checkBoundary(location);
    }

    public void onMapSearch(View v){
        locationSearch = (EditText) findViewById(R.id.search_bar);
        location = locationSearch.getText().toString();
        searchButton = (Button) findViewById(R.id.search_button);

        List<Address> addressList = null;
        if(location != null || !location.equals("")){
            Geocoder geocoder = new Geocoder(this);
            try{
                addressList = geocoder.getFromLocationName(location,1);
            }catch(IOException e){
                e.printStackTrace();
            }

            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.clear();
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
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleAPIClient, locationRequest, this);
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
    }

    @Override
    public void onStop(){
        super.onStop();
        mGoogleAPIClient.disconnect();
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
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

}
