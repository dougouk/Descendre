package com.dan190.descendre;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener{

    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private Circle circle;


    public static final CameraPosition MONTREAL =
            new CameraPosition.Builder().target(new LatLng(45.5, -73.6))
                    .zoom(15.5f)
                    .bearing(0)
                    .tilt(0)
                    .build();

    public static final LatLng MONTREAL_LL = new LatLng(45.5, -73.6);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mUiSettings = mMap.getUiSettings();

        mUiSettings.setZoomControlsEnabled(true);

        startPermission();
        mMap.setMyLocationEnabled(true);

        mUiSettings.setMapToolbarEnabled(true);

        circle = mMap.addCircle(new CircleOptions()
                .center(MONTREAL_LL)
                .radius(200)
                .strokeColor(Color.BLACK)
                .fillColor(0x00000000));

        LatLng montreal = new LatLng(45.5, -73.6);
        // Add a marker in Sydney and move the camera
        /*LatLng montreal = new LatLng(45.5, -73.6);
        mMap.addMarker(new MarkerOptions().position(montreal).title("Marker in Montreal"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(montreal));*/

        mMap.addMarker(new MarkerOptions().position(montreal).title("Marker in Montreal"));

        changeCamera(CameraUpdateFactory.newCameraPosition(MONTREAL));
    }

    @Override
    public void onMapClick(LatLng point){
        Toast.makeText(getApplicationContext(),
                "Clicked " + point.toString(),
                Toast.LENGTH_SHORT).show();
    }

    public void onGoToMontreal(View v){
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(MONTREAL), 1000, null);
    }

    private void changeCamera(CameraUpdate update) {
        changeCamera(update, null);
    }

    /**
     * Change the camera position by moving or animating the camera depending on the state of the
     * animate toggle button.
     */
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
}
