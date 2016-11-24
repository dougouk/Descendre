package com.dan190.descendre.Map;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.dan190.descendre.Geofence.GeofenceManager;
import com.dan190.descendre.Geofence.MyGeofence;
import com.dan190.descendre.R;
import com.dan190.descendre.Util.UserState;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by Dan on 16/11/2016.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback, ResultCallback<Status>, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    static final String ACTIVITY_NAME = "MAP_FRAGMENT";

    private static View view;
    private  Context context;
    private  Activity activity;

    private LocationManager locationManager;
    private Location locationLocation;
    private LocationRequest locationRequest;
    private MyLocationListener myLocationListener;

    private String provider;

//    private MapManager mapManager;
    private SupportMapFragment mapFragment;
    private FragmentManager fragmentManager;

    private static GoogleApiClient mGoogleAPIClient;

    private  GoogleMap mMap;

    /**
     * Used by Map
     */
    private Button makeGeofenceAtMarker_Button,
            deleteGeofenceAtMarker_Button,
            usePlacePicker_Button;

    private UserState userState;
    private UiSettings mUiSettings;

    private Marker chosenMarker;
    private Circle chosenCircle;

//    private List<Geofence> mGeofenceList;
    private List<MyGeofence> myGeofenceList;
    private Map<Marker, Circle> destinationDictionary;
    private boolean isMarkerClickedOnExistingDestination;
    private PendingIntent mGeofencePendingIntent;

    private PlaceAutocompleteFragment placeAutocompleteFragment;




    private void initializeGoogleAPIClient() {
        Log.i(ACTIVITY_NAME, "Initialize Google API Client");
        if(mGoogleAPIClient == null){

            mGoogleAPIClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            Log.i(ACTIVITY_NAME, "Created Google API Client");
        }
        if(!mGoogleAPIClient.isConnected()) {
            mGoogleAPIClient.connect();
            Log.i(ACTIVITY_NAME, "Connected Google API Client");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(ACTIVITY_NAME, "onMapReady()");

        mMap = googleMap;

        mGoogleAPIClient = MapFragment.getGoogleAPIClient();

        createLocationRequest();

        myLocationListener = new MyLocationListener();
        locationManager = (LocationManager) getActivity()
                .getSystemService(LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), true);

        try{
            if(provider != null){
                locationLocation = locationManager.getLastKnownLocation(provider);
            }
        }catch (SecurityException e){
            Log.e(ACTIVITY_NAME, e.getMessage());
        }



        mUiSettings = mMap.getUiSettings();

        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setMapToolbarEnabled(true);

        try{
            mMap.setMyLocationEnabled(true);
        }catch(SecurityException e){
            Log.e(ACTIVITY_NAME, "Needs Location Permission");
        }

        mMap.setOnMapClickListener(onMapClickListener);
        mMap.setOnMapLongClickListener(onMapLongClickListener);
        mMap.setOnMarkerClickListener(onMarkerClickListener);

        if(locationLocation != null){
            chosenMarker = mMap.addMarker(new MarkerOptions().position(
                    new LatLng(locationLocation.getLatitude(), locationLocation.getLongitude())
            ).title("You are here!"));
            changeCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(
                    new LatLng(locationLocation.getLatitude(), locationLocation.getLongitude()), 15)));

        }

        /**
         * Non Map-Related Instantiations
         */
        {
            initializeGoogleAPIClient();
            myGeofenceList = new ArrayList<>();

            placeAutocompleteFragment = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.searchFragment);

            if(placeAutocompleteFragment != null){
                placeAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(Place place) {
                        Log.d(ACTIVITY_NAME, "Selected place Add is : " + place.getAddress().toString());
                        Log.d(ACTIVITY_NAME, "Selected place LatLng is : " + place.getLatLng().toString());
                        Log.d(ACTIVITY_NAME, "Selected place Name is : " + place.getName().toString());
                        clearRedundant();
                        chosenMarker = mMap.addMarker(new MarkerOptions()
                                .position(place.getLatLng())
                                .title(place.getName().toString()));
                        chosenCircle = mMap.addCircle(new CircleOptions()
                                .center(place.getLatLng())
                                .radius(200)
                                .strokeColor(Color.BLACK)
                                .fillColor(0x00000000));
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                    }

                    @Override
                    public void onError(Status status) {
                        Toast.makeText(getContext(), "Cannot find place", Toast.LENGTH_SHORT).show();
                        Log.e(ACTIVITY_NAME, status.getStatusMessage());
                    }
                });
            }
            else{
                Log.e(ACTIVITY_NAME, "placeAutocomplete Fragment not loaded");
            }
            destinationDictionary = new HashMap<Marker, Circle>() {
            };

            userState = UserState.NORMAL;

            assignButtons();
        }
    }

    private void assignButtons(){

        makeGeofenceAtMarker_Button = (Button) getActivity().findViewById(R.id.makeGeofenceAtMarker_button);
        deleteGeofenceAtMarker_Button = (Button) getActivity().findViewById(R.id.deleteMarker_button);
        usePlacePicker_Button = (Button) getActivity().findViewById(R.id.button_placePicker_activity_maps);

        makeGeofenceAtMarker_Button.setVisibility(View.INVISIBLE);
        deleteGeofenceAtMarker_Button.setVisibility(View.INVISIBLE);
//        usePlacePicker_Button.setVisibility(View.INVISIBLE);

        makeGeofenceAtMarker_Button.setOnClickListener(makeGeofenceAtMarkerListener);
        deleteGeofenceAtMarker_Button.setOnClickListener(deleteGeofenceAtMarkerListener);
        usePlacePicker_Button.setOnClickListener(removeGeofencesListener);
        usePlacePicker_Button.setText("Remove all Geofences");
    }
    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public static GoogleApiClient getGoogleAPIClient(){return mGoogleAPIClient;}

    private void askForLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            //has already seen permission
            Log.i(ACTIVITY_NAME, "Has Already seen permission");
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            }

        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            Log.i(ACTIVITY_NAME, "Permission Granted");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //mapFragment.isVisible();
        Log.i(ACTIVITY_NAME, "onAttach()");
    }
    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        Log.i(ACTIVITY_NAME, "onCreate()");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(ACTIVITY_NAME, "onCreateView()");

        if(view != null){
            ViewGroup parent = (ViewGroup) view.getParent();
            if(parent != null){
                parent.removeView(view);
                Log.d(ACTIVITY_NAME, "Removing pre-existing view from parent");
            }
        }
        try{
            view = inflater.inflate(R.layout.activity_maps, container, false);

        }catch(InflateException e){
            /**
             * Map is already there, just return the view as it is
             */
        }

        activity = getActivity();
        context = getContext();

        askForLocationPermission();


        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(ACTIVITY_NAME, "onActivityCreated()");

        fragmentManager = getFragmentManager();

        if(mapFragment == null){
            mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);
            Log.i(ACTIVITY_NAME, "Assigned mapFragment");
        }

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            fragmentManager.beginTransaction().replace(R.id.map, mapFragment).commit();
            Log.d(ACTIVITY_NAME, "Replaced Map Fragment");
        }
//        if (mapManager == null) mapManager = new MapManager();
        initializeGoogleAPIClient();

        mapFragment.getMapAsync(this);
        Log.i(ACTIVITY_NAME, "Called getMapAsync");

        if(savedInstanceState == null){
            mapFragment.setRetainInstance(true);
        }
    }
    @Override
    public void onStart() {
        super.onStart();
/*        if (!mGoogleAPIClient.isConnected()) {
            mGoogleAPIClient.connect();
        }
        locationManager = (LocationManager) getFragActivity().getSystemService(LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), true);
        try {
            if (provider != null) {
                if (ActivityCompat.checkSelfPermission(getFragActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getFragActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                    askForLocationPermission();
                    return;
                }
                locationLocation = locationManager.getLastKnownLocation(provider);
            }
        } catch (SecurityException e) {
            Log.e(ACTIVITY_NAME, e.getMessage());
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(ACTIVITY_NAME, "onResume()");
        if(!mGoogleAPIClient.isConnected()){
            mGoogleAPIClient.connect();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i(ACTIVITY_NAME, "onPause()");
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.i(ACTIVITY_NAME, "onStop()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(ACTIVITY_NAME, "onDestroy()");
        if(mGoogleAPIClient != null) mGoogleAPIClient.disconnect();
    }
    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(ACTIVITY_NAME, "onDetach()");

    }



    @Override
    public void onDestroyView(){
        Log.i(ACTIVITY_NAME, "onDestroyView()");
        super.onDestroyView();
//        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
//        if(mapFragment != null){
//            getFragmentManager().beginTransaction().remove(mapFragment).commit();
//            Log.d(ACTIVITY_NAME, "Removed Map Fragment");
//        }
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
                    Toast.makeText(getContext(), "Permission Granted", Toast.LENGTH_SHORT).show();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getContext(), "Permission Denied", Toast.LENGTH_SHORT).show();

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    /**
     * Map Modifiers
     */
    private void clearRedundant(){
        if(userState != UserState.SELECTING_MARKER){
            if(chosenCircle != null) {
                chosenCircle.remove();
            }
            if(chosenMarker != null) {
                chosenMarker.remove();
            }
        }

//        isMarkerClickedOnExistingDestination = false;

        if(deleteGeofenceAtMarker_Button.getVisibility() == View.VISIBLE){
            deleteGeofenceAtMarker_Button.setVisibility(View.INVISIBLE);
        }

        if(makeGeofenceAtMarker_Button.getVisibility() == View.VISIBLE){
            makeGeofenceAtMarker_Button.setVisibility(View.INVISIBLE);
        }
//        userState = UserState.NORMAL;
        Log.d(ACTIVITY_NAME, "clearRedundant()");
    }

    private void createDestinationMarker(LatLng latLng) {
        clearRedundant();
        chosenCircle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(200)
                .strokeColor(Color.BLACK)
                .fillColor(0x00000000));
        chosenMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Picked"));
        makeGeofenceAtMarker_Button.setVisibility(View.VISIBLE);
        userState = UserState.NORMAL;
    }
    private void changeCamera(CameraUpdate update) {
        changeCamera(update, null);
    }


    private void changeCamera(CameraUpdate update, GoogleMap.CancelableCallback callback) {
        mMap.animateCamera(update);
    }

    /** Listeners */
    private View.OnClickListener makeGeofenceAtMarkerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AddGeofenceAtLocation(v);
            userState = UserState.ADDING_MARKER;
            clearRedundant();
        }
    };

    private View.OnClickListener deleteGeofenceAtMarkerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            userState = UserState.DELETING_MARKER;
            clearRedundant();
            String key = null;
            for(MyGeofence chosenGeo : myGeofenceList){
                if(chosenGeo.getMarker().equals(chosenMarker)){
                    key = chosenGeo.getKey();
                }
            }
            if(key == null) {
                Log.w(ACTIVITY_NAME, "Key is null");
                return;
            }
            GeofenceManager.removeParticularGeofence(mGoogleAPIClient, key);
        }
    };

    private View.OnClickListener removeGeofencesListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            removeAllGeofences(mGoogleAPIClient);
            Log.i(ACTIVITY_NAME, "Remove Geofences()");
        }
    };
    private GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener(){
        @Override
        public void onMapClick(LatLng latLng){
            Log.i(ACTIVITY_NAME, "Map Clicked at " + latLng.toString());
            clearRedundant();
        }
    };

    private GoogleMap.OnMapLongClickListener onMapLongClickListener = new GoogleMap.OnMapLongClickListener() {
        @Override
        public void onMapLongClick(LatLng latLng) {
            Log.i(ACTIVITY_NAME, "Marker Clicked");
            createDestinationMarker(latLng);
        }
    };

    private GoogleMap.OnMarkerClickListener onMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            Log.i(ACTIVITY_NAME, "Marker Clicked");
            boolean marker_exists_in_dictionary = false;

            //check if marker exists in geofence
//            for(Marker m : destinationDictionary.keySet()){
//                if (m.equals(marker)){
//                    marker_exists_in_dictionary = true;
//                    chosenMarker = m;
//                    chosenCircle = destinationDictionary.get(m);
//                }
//            }
            for(MyGeofence myG : myGeofenceList){
                if(myG.getCenter().equals(marker.getPosition())){
                    marker_exists_in_dictionary = true;
                    chosenMarker = myG.getMarker();
                    chosenCircle = myG.getCircle();
                }
            }

            if(marker_exists_in_dictionary){
                Log.d(ACTIVITY_NAME, "Marker exists in dictionary");
                isMarkerClickedOnExistingDestination= true;
                chosenMarker.showInfoWindow();
                userState = UserState.SELECTING_MARKER;

                deleteGeofenceAtMarker_Button.setVisibility(View.VISIBLE);
            }
            else{
                Log.d(ACTIVITY_NAME, "Marker does not exist in dictionary");
                makeGeofenceAtMarker_Button.setVisibility(View.VISIBLE);
            }
            Log.d(ACTIVITY_NAME, marker.getTitle() + " clicked");
//        Log.d(ACTIVITY_NAME, "Cirlce getcenter(): " + circle.getCenter().toString());


            return false;
        }
    };

    /**
     * Geofence
     */

    private void AddGeofenceAtLocation(View v){
        if(chosenMarker.getPosition() == null){
            Log.e(ACTIVITY_NAME, "chosenMarker is null");
            return;
        }

        if(!mGoogleAPIClient.isConnected() || mGoogleAPIClient.isConnecting() ){
            Log.e(ACTIVITY_NAME, "Google API Client is not connected yet");
            initializeGoogleAPIClient();
        }
        if(!mGoogleAPIClient.isConnected()) return;

        GeofenceManager.addGeofence(mMap, new MyGeofence(chosenCircle.getCenter()), myGeofenceList,destinationDictionary);
        GeofenceManager.SendGeofence(v, myGeofenceList, mGoogleAPIClient, mGeofencePendingIntent, getContext(), this);

        Log.d(ACTIVITY_NAME, "calling addGeofence(chosenMarker.getPosition())");
    }

    private void removeAllGeofences(GoogleApiClient mGoogleAPIClient){
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleAPIClient,
                GeofenceManager.getGeofencePendingIntent(mGeofencePendingIntent, getContext())).setResultCallback(this);
        Log.d(ACTIVITY_NAME, "Removed Geofences");
    }


    @Override
    public void onResult(@NonNull Status status) {

    }

    /**
     * Connection stuff
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(ACTIVITY_NAME, "onConnected()");
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(ACTIVITY_NAME, "onConnectionSuspended()");
        stopLocationUpdates();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(ACTIVITY_NAME, "onConnectionFailed" +
                "()");

    }

    private void startLocationUpdates() {
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleAPIClient, locationRequest, myLocationListener);
        }catch(SecurityException e){
            Log.e(ACTIVITY_NAME, e.getMessage());
        }
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleAPIClient, myLocationListener);
        Log.d(ACTIVITY_NAME, "stopLocationUpdates");
    }
}
