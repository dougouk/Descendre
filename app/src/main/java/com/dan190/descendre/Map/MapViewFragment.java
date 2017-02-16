package com.dan190.descendre.Map;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dan190.descendre.Geofence.MyGeofence;
import com.dan190.descendre.R;
import com.dan190.descendre.TabLayoutActivity;
import com.dan190.descendre.Util.UserState;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by Dan on 16/02/2017.
 */

public class MapViewFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static String TAG = MapViewFragment.class.getName();

    /**
     * Variables to be used on, or for, the map
     */
    private MapView mMapView;
    private GoogleMap googleMap;
    private LocationManager mLocationManager;
    private Location mUserLocation;
    private UiSettings mUiSettings;
    private String mLocationProvider;
    private Button getDirectionsButton;
    private UserState mUserState;
    private Marker chosenMarker;
    private Circle chosenCircle;

    /**
     * Connection variables
     */
    private GoogleApiClient mGoogleAPIClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_maps, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                initializeGoogleAPIClient();
                Log.i(TAG, "onMapReady()");

                googleMap = mMap;

                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    askForLocationPermission();
                    return;
                }

                //Try to get user location so we know where to start the mapView
                mLocationManager  = (LocationManager) getActivity()
                        .getSystemService(LOCATION_SERVICE);
                updateLastKnownLocation();

                googleMap.setMyLocationEnabled(true);

                googleMap.setOnMapClickListener(onMapClickListener);
                googleMap.setOnMapLongClickListener(onMapLongClickListener);
                googleMap.setOnMarkerClickListener(onMarkerClickListener);

                mUiSettings = googleMap.getUiSettings();
                mUiSettings.setMapToolbarEnabled(true);
                mUiSettings.setZoomControlsEnabled(true );

                // For dropping a marker at a point on the Map

                if(mUserLocation != null){
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(
                            new LatLng(mUserLocation.getLatitude(), mUserLocation.getLongitude()), 15)));

                }

                mUserState = UserState.NORMAL;
                assignButtons();
            }
        });

        return rootView;
    }


    private void askForLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Explain to the user why they need to provide location permission
            Log.i(TAG, "Should show permission rationale");
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            }

        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            Log.i(TAG, "Permission Requested");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
    /**
     * Refactored Methods
     */

    private void updateLastKnownLocation() {
        mLocationProvider = mLocationManager.getBestProvider(new Criteria(), true);

        try{
            if(mLocationProvider != null){
                mUserLocation = mLocationManager.getLastKnownLocation(mLocationProvider);
//                Log.i(TAG, "last known location : " + lastKnownLocation.toString());
            }
            else{
                Log.e(TAG, "provider is NULL");
            }
        }catch (SecurityException e){
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Map Modifers
     */

    private void clearRedundant(){
        Log.d(TAG, "State : " + mUserState.toString());

        if(mUserState != UserState.SELECTING_MARKER){
            if(chosenCircle != null) {
                chosenCircle.remove();
            }
            if(chosenMarker != null) {
                chosenMarker.remove();
            }
        }

//        isMarkerClickedOnExistingDestination = false;

        if(getDirectionsButton.getVisibility() == View.VISIBLE){
            getDirectionsButton.setVisibility(View.INVISIBLE);
        }
//        userState = UserState.NORMAL;
        Log.d(TAG, "clearRedundant()");
    }

    private void createDestinationMarker(LatLng latLng) {
        clearRedundant();
        chosenCircle = googleMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(200)
                .strokeColor(Color.BLACK)
                .fillColor(0x00000000));
        chosenMarker = googleMap.addMarker(new MarkerOptions().position(latLng).title("Picked"));
        getDirectionsButton.setVisibility(View.VISIBLE);
        mUserState = UserState.NORMAL;
    }
    /**
     * Listeners
     */

    private View.OnClickListener getDirectionsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TabLayoutActivity tabLayoutActivity = (TabLayoutActivity) getActivity();
            ViewPager viewPager = tabLayoutActivity.getViewPager();
            viewPager.setCurrentItem(1);

        }
    };

    private GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener(){
        @Override
        public void onMapClick(LatLng latLng){
            Log.i(TAG, "Map Clicked at " + latLng.toString());
            clearRedundant();
        }
    };

    private GoogleMap.OnMapLongClickListener onMapLongClickListener = new GoogleMap.OnMapLongClickListener() {
        @Override
        public void onMapLongClick(LatLng latLng) {
            Log.i(TAG, "Marker Clicked");
            createDestinationMarker(latLng);
        }
    };

    private GoogleMap.OnMarkerClickListener onMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
           Log.d(TAG, marker.getTitle() + " clicked");
            return false;
        }
    };
    /** Initialization stuff
     *
     */
    private void initializeGoogleAPIClient() {
        Log.i(TAG, "Initialize Google API Client");
        if(mGoogleAPIClient == null){

            mGoogleAPIClient = new GoogleApiClient.Builder(getContext())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            Log.i(TAG, "Created Google API Client");
        }
        if(!mGoogleAPIClient.isConnected()) {
            mGoogleAPIClient.connect();
            Log.i(TAG, "Connected Google API Client");
        }
    }

    private void assignButtons() {

//        makeGeofenceAtMarker_Button = (Button) getActivity().findViewById(R.id.makeGeofenceAtMarker_button);
//        deleteGeofenceAtMarker_Button = (Button) getActivity().findViewById(R.id.deleteMarker_button);
        getDirectionsButton = (Button) getActivity().findViewById(R.id.getDirectionsButton);
//        usePlacePicker_Button = (Button) getActivity().findViewById(R.id.button_placePicker_activity_maps);

//        makeGeofenceAtMarker_Button.setVisibility(View.INVISIBLE);
//        deleteGeofenceAtMarker_Button.setVisibility(View.INVISIBLE);
        getDirectionsButton.setVisibility(View.INVISIBLE);
//        usePlacePicker_Button.setVisibility(View.INVISIBLE);

//        makeGeofenceAtMarker_Button.setOnClickListener(makeGeofenceAtMarkerListener);
//        deleteGeofenceAtMarker_Button.setOnClickListener(deleteGeofenceAtMarkerListener);
        getDirectionsButton.setOnClickListener(getDirectionsListener);
//        usePlacePicker_Button.setOnClickListener(removeGeofencesListener);
//        usePlacePicker_Button.setText("Remove all Geofences");
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed");
    }
}

