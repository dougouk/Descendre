package com.dan190.descendre;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tagmanager.TagManager;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
GoogleMap.OnMapLongClickListener,
        LocationListener{

    /**Members */
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private Circle circle;
    private GoogleApiClient googleApiClient;
    private LocationManager locationManager;

    public static final CameraPosition MONTREAL =
            new CameraPosition.Builder().target(new LatLng(45.5, -73.6))
                    .zoom(15.5f)
                    .bearing(0)
                    .tilt(0)
                    .build();

    public static final LatLng MONTREAL_LL = new LatLng(45.5, -73.6);


    /**Methods */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        /*googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
        .addApi(LocationServices.API)
        .addConnectionCallbacks(googleApiClient)
        .addOnConnectionFailedListener(this)
        .build();*/

        mapFragment.getMapAsync(this);
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
                mMap.addMarker(new MarkerOptions().position(latLng).title("Picked"));

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
    }

    public void onMapSearch(View v){
        EditText locationSearch = (EditText) findViewById(R.id.search_bar);
        String location = locationSearch.getText().toString();
        Button searchButton = (Button) findViewById(R.id.search_button);

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
            mMap.addMarker(new MarkerOptions().position(latLng).title("Searched"));
            circle = mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(200)
                    .strokeColor(Color.BLACK)
                    .fillColor(0x00000000));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

        }
    }
}
