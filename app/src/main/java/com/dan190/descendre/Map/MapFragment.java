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
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by Dan on 16/11/2016.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback, ResultCallback<Status>, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    static final String TAG = MapFragment.class.getName();

    public static String mapFragmentKey = "map";

    private static View view;
    private  Context context;
    private  Activity activity;

    private LocationManager locationManager;
    private static Location lastKnownLocation;


    private LocationRequest locationRequest;
    private MyLocationListener myLocationListener;

    private String provider;

//    private MapManager mapManager;
    private SupportMapFragment supportMapFragment;
    private FragmentManager fragmentManager;

    private static GoogleApiClient mGoogleAPIClient;
    private static MapFragment mapFragment;
    private  GoogleMap mMap;

    /**
     * Used by Map
     */
    private Button makeGeofenceAtMarker_Button,
            deleteGeofenceAtMarker_Button, getDirectionsButton;

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

    /**
     * Member variable for sharing data between fragments
     */
    OnGeofenceListener mOnGeofenceListener;

    /**
     * Interface for sharing data between fragments
     */

    public interface OnGeofenceListener{
        public void onCreateGeofence(List<MyGeofence> myGeofencesList);
        public void onRemoveGeofence(MyGeofence myGeofence);
    }
    private void initializeGoogleAPIClient() {
        Log.i(TAG, "Initialize Google API Client");
        if(mGoogleAPIClient == null){

            mGoogleAPIClient = new GoogleApiClient.Builder(context)
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

    /** Getters and Setters
     *
     */


    public static Location getLastKnownLocation() {
        return lastKnownLocation;
    }

    public static void setLastKnownLocation(Location lastKnownLocation) {
        MapFragment.lastKnownLocation = lastKnownLocation;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        initializeGoogleAPIClient();

        Log.i(TAG, "onMapReady()");

        mMap = googleMap;

        mGoogleAPIClient = MapFragment.getGoogleAPIClient();

        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            askForLocationPermission();
        }
        createLocationRequest();

        myLocationListener = new MyLocationListener();
        locationManager  = (LocationManager) getActivity()
                .getSystemService(LOCATION_SERVICE);
        updateLastKnownLocation();



        mUiSettings = mMap.getUiSettings();

        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setMapToolbarEnabled(true);
        mUiSettings.setCompassEnabled(true);
//        mUiSettings.setMyLocationButtonEnabled(true);

        try{
            mMap.setMyLocationEnabled(true);
            Log.d(TAG, "set my location");
        }catch(SecurityException e){
            Log.e(TAG, "Needs Location Permission");
            Snackbar snackbar = Snackbar.make(view, "Needs location permission to get your location",
                    Snackbar.LENGTH_LONG);
            snackbar.show();
        }

        mMap.setOnMapClickListener(onMapClickListener);
        mMap.setOnMapLongClickListener(onMapLongClickListener);
        mMap.setOnMarkerClickListener(onMarkerClickListener);

        if(lastKnownLocation != null){
//            chosenMarker = mMap.addMarker(new MarkerOptions().position(
//                    new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude())
//            ).title("You are here!"));
            changeCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(
                    new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), 15)));

        }

        /**
         * Non Map-Related Instantiations
         */
        {
            myGeofenceList = new ArrayList<>();

            placeAutocompleteFragment = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.searchFragment);

            if(placeAutocompleteFragment != null){
                placeAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(Place place) {
                        Log.d(TAG, "Selected place Add is : " + place.getAddress().toString());
                        Log.d(TAG, "Selected place LatLng is : " + place.getLatLng().toString());
                        Log.d(TAG, "Selected place Name is : " + place.getName().toString());
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
                        Log.e(TAG, status.getStatusMessage());
                    }
                });
            }
            else{
                Log.e(TAG, "placeAutocomplete Fragment not loaded");
            }
            destinationDictionary = new HashMap<Marker, Circle>() {
            };

            userState = UserState.NORMAL;

            assignButtons();
        }
    }

    private void updateLastKnownLocation() {
        provider = locationManager.getBestProvider(new Criteria(), true);

        try{
            if(provider != null){
                lastKnownLocation = locationManager.getLastKnownLocation(provider);
//                Log.i(TAG, "last known location : " + lastKnownLocation.toString());
            }
            else{
                Log.e(TAG, "provider is NULL");
            }
        }catch (SecurityException e){
            Log.e(TAG, e.getMessage());
        }
    }

    private void assignButtons(){

//        makeGeofenceAtMarker_Button = (Button) getActivity().findViewById(R.id.makeGeofenceAtMarker_button);
//        deleteGeofenceAtMarker_Button = (Button) getActivity().findViewById(R.id.deleteMarker_button);
        getDirectionsButton = (Button) getActivity().findViewById(R.id.getDirectionsButton);
//        usePlacePicker_Button = (Button) getActivity().findViewById(R.id.button_placePicker_activity_maps);

        makeGeofenceAtMarker_Button.setVisibility(View.INVISIBLE);
        deleteGeofenceAtMarker_Button.setVisibility(View.INVISIBLE);
        getDirectionsButton.setVisibility(View.INVISIBLE);
//        usePlacePicker_Button.setVisibility(View.INVISIBLE);

        makeGeofenceAtMarker_Button.setOnClickListener(makeGeofenceAtMarkerListener);
        deleteGeofenceAtMarker_Button.setOnClickListener(deleteGeofenceAtMarkerListener);
        getDirectionsButton.setOnClickListener(getDirectionsListener);
//        usePlacePicker_Button.setOnClickListener(removeGeofencesListener);
//        usePlacePicker_Button.setText("Remove all Geofences");
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
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach()");
        try{
            mOnGeofenceListener = (OnGeofenceListener) context;
        }catch(ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement OnGeofenceListener");
        }
    }
    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        Log.i(TAG, "onCreate()");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");

        if(view != null){
            ViewGroup parent = (ViewGroup) view.getParent();
            if(parent != null){
                parent.removeView(view);
                Log.d(TAG, "Removing pre-existing view from parent");
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
        mapFragment = this;


//        askForLocationPermission();


        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated()");

        fragmentManager = getFragmentManager();

        if (supportMapFragment == null) {
//            supportMapFragment = SupportMapFragment.newInstance();
//            supportMapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.maps);
//            com.google.android.gms.maps.MapFragment mapFragment = new com.google.android.gms.maps.MapFragment();
//            Log.d(TAG, supportMapFragment.toString());
            fragmentManager.beginTransaction()
                    .add(R.id.frameContainer, supportMapFragment, mapFragmentKey)
                    .commit();
            supportMapFragment.getMapAsync(this);
            Log.i(TAG, "Called getMapAsync");
        }
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        initializeGoogleAPIClient();
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG, "onPause()");
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.i(TAG, "onStop()");
        removeMapFragmentFromContainer();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
        if(mGoogleAPIClient != null) mGoogleAPIClient.disconnect();

    }

    private void removeMapFragmentFromContainer() {
        android.app.Fragment fragment = getActivity().getFragmentManager().findFragmentByTag(mapFragmentKey);
        if(fragment!= null){
            Log.d(TAG, "Fragment is null");
            getActivity().getFragmentManager().beginTransaction()
                    .remove(fragment);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "onDetach()");

    }

    @Override
    public void onDestroyView(){
        Log.i(TAG, "onDestroyView()");
        super.onDestroyView();
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
                    try{
                        mMap.setMyLocationEnabled(true);
                    }catch(SecurityException e){
                        e.printStackTrace();
                    }
                    Toast.makeText(getContext(), "Location Permission Granted", Toast.LENGTH_SHORT).show();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getContext(), "Location Permission Denied", Toast.LENGTH_SHORT).show();

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
    public void clearAll(){
        mMap.clear();
    }
    private void clearRedundant(){
        Log.d(TAG, "State : " + userState.toString());

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

        if(getDirectionsButton.getVisibility() == View.VISIBLE){
            getDirectionsButton.setVisibility(View.INVISIBLE);
        }
//        userState = UserState.NORMAL;
        Log.d(TAG, "clearRedundant()");
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
        getDirectionsButton.setVisibility(View.VISIBLE);
        userState = UserState.NORMAL;
    }
    private void changeCamera(CameraUpdate update) {
        changeCamera(update, null);
    }


    private void changeCamera(CameraUpdate update, GoogleMap.CancelableCallback callback) {
        mMap.animateCamera(update);
    }

    /** Listeners */
    private View.OnClickListener getDirectionsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            DirectionsFragment dr = new DirectionsFragment();
//            TabLayoutActivity tabLayoutActivity = (TabLayoutActivity) getActivity();
//            ViewPager viewPager = tabLayoutActivity.getViewPager();
//            viewPager.setCurrentItem(1);

        }
    };

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
            deleteGeofenceOnMap();
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
            Log.i(TAG, "Marker Clicked");
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
                Log.d(TAG, "Marker exists in dictionary");
                isMarkerClickedOnExistingDestination= true;
                chosenMarker.showInfoWindow();
                userState = UserState.SELECTING_MARKER;

                deleteGeofenceAtMarker_Button.setVisibility(View.VISIBLE);
            }
            else{
                Log.d(TAG, "Marker does not exist in dictionary");
                makeGeofenceAtMarker_Button.setVisibility(View.VISIBLE);
                getDirectionsButton.setVisibility(View.VISIBLE);
            }
            Log.d(TAG, marker.getTitle() + " clicked");
//        Log.d(TAG, "Cirlce getcenter(): " + circle.getCenter().toString());


            return false;
        }
    };

    /**
     * Geofence
     */

    public static MapFragment getMapFragment(){
        return mapFragment;
    }
    public void deleteGeofenceOnMap() {
        userState = UserState.DELETING_MARKER;
        clearRedundant();
        String key = null;
        for(MyGeofence chosenGeo : myGeofenceList){
            if(chosenGeo.getMarker().equals(chosenMarker)){
                key = chosenGeo.getKey();
            }
        }
        if(key == null) {
            Log.w(TAG, "Key is null");
            return;
        }
        GeofenceManager.removeParticularGeofence(mGoogleAPIClient, key);
    }

    private void AddGeofenceAtLocation(View v){
        if(chosenMarker.getPosition() == null){
            Log.e(TAG, "chosenMarker is null");
            return;
        }

        if(!mGoogleAPIClient.isConnected() || mGoogleAPIClient.isConnecting() ){
            Log.e(TAG, "Google API Client is not connected yet");
            initializeGoogleAPIClient();
        }
        if(!mGoogleAPIClient.isConnected()) return;

        MyGeofence newGeofence = new MyGeofence(chosenCircle.getCenter());

        updateLastKnownLocation();
        float[] distanceArray = new float[1];
        Location.distanceBetween(chosenCircle.getCenter().latitude, chosenCircle.getCenter().longitude,
                lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(),
                distanceArray);
        float distance = distanceArray[0];

        newGeofence.setDistance(distance);

        GeofenceManager.addGeofence(mMap, newGeofence, myGeofenceList,destinationDictionary);
        GeofenceManager.SendGeofence(v, myGeofenceList, mGoogleAPIClient, mGeofencePendingIntent, getContext(), this);

        mOnGeofenceListener.onCreateGeofence(myGeofenceList);
        Log.d(TAG, "calling addGeofence(chosenMarker.getPosition())");
    }

    private void removeAllGeofences(GoogleApiClient mGoogleAPIClient){
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleAPIClient,
                GeofenceManager.getGeofencePendingIntent(mGeofencePendingIntent, getContext())).setResultCallback(this);
        Log.d(TAG, "Removed Geofences");
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
        Log.i(TAG, "onConnected()");
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended()");
        stopLocationUpdates();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed" +
                "()");

    }

    private void startLocationUpdates() {
        if(locationRequest != null && myLocationListener != null){
            try{
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleAPIClient, locationRequest, myLocationListener);
            }catch(SecurityException e){
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleAPIClient, myLocationListener);
        Log.d(TAG, "stopLocationUpdates");
    }
}
