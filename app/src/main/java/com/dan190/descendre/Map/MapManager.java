//package com.dan190.descendre.Map;
//
//import android.app.PendingIntent;
//import android.graphics.Color;
//import android.location.Criteria;
//import android.location.Location;
//import android.location.LocationManager;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.Toast;
//
//import com.dan190.descendre.Geofence.GeofenceManager;
//import com.dan190.descendre.R;
//import com.dan190.descendre.Util.UserState;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.common.api.ResultCallback;
//import com.google.android.gms.common.api.Status;
//import com.google.android.gms.location.Geofence;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.location.places.Place;
//import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
//import com.google.android.gms.location.places.ui.PlaceSelectionListener;
//import com.google.android.gms.maps.CameraUpdate;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.UiSettings;
//import com.google.android.gms.maps.model.CameraPosition;
//import com.google.android.gms.maps.model.Circle;
//import com.google.android.gms.maps.model.CircleOptions;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.Marker;
//import com.google.android.gms.maps.model.MarkerOptions;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static android.content.Context.LOCATION_SERVICE;
//
///**
// * Created by Dan on 16/11/2016.
// */
//
//public class MapManager  implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
//    static final String TAG = "MAP_MANAGER";
//
//    private GoogleMap mMap;
//    private UiSettings mUiSettings;
//    private GoogleApiClient mGoogleAPIClient;
//    private LocationRequest locationRequest;
//    private Location locationLocation;
//    private LocationManager locationManager;
//    private MyLocationListener myLocationListener;
//    private String provider;
//
//    private Marker chosenMarker;
//    private Circle chosenCircle;
//
//    private Button makeGeofenceAtMarker_Button,
//        deleteGeofenceAtMarker_Button,
//        usePlacePicker_Button;
//
//    private UserState userState;
//
//    private List<Geofence> mGeofenceList;
//    private Map<Marker, Circle> destinationDictionary;
//    private boolean isMarkerClickedOnExistingDestination;
//    private PendingIntent mGeofencePendingIntent;
//
//    private PlaceAutocompleteFragment placeAutocompleteFragment;
//
//
//
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        Log.i(TAG, "onMapReady()");
//
//        mMap = googleMap;
//
////        mGoogleAPIClient = new GoogleApiClient.Builder(MapFragment.getFragmentContext())
////                .addApi(LocationServices.API)
////                .addConnectionCallbacks(this)
////                .addOnConnectionFailedListener(this)
////                .build();
//        mGoogleAPIClient = MapFragment.getGoogleAPIClient();
//
//        createLocationRequest();
//
//        myLocationListener = new MyLocationListener();
//        locationManager = (LocationManager) MapFragment.getFragActivity()
//                .getSystemService(LOCATION_SERVICE);
//        provider = locationManager.getBestProvider(new Criteria(), true);
//
//        try{
//            if(provider != null){
//                locationLocation = locationManager.getLastKnownLocation(provider);
//            }
//        }catch (SecurityException e){
//            Log.e(TAG, e.getMessage());
//        }
//
//
//
//        mUiSettings = mMap.getUiSettings();
//
//        mUiSettings.setZoomControlsEnabled(true);
//        mUiSettings.setMapToolbarEnabled(true);
//
//        try{
//            mMap.setMyLocationEnabled(true);
//        }catch(SecurityException e){
//            Log.e(TAG, "Needs Location Permission");
//        }
//
//        mMap.setOnMapClickListener(onMapClickListener);
//        mMap.setOnMapLongClickListener(onMapLongClickListener);
//        mMap.setOnMarkerClickListener(onMarkerClickListener);
//
//        if(locationLocation != null){
//            chosenMarker = mMap.addMarker(new MarkerOptions().position(
//                    new LatLng(locationLocation.getLatitude(), locationLocation.getLongitude())
//            ).title("You are here!"));
//            changeCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(
//                    new LatLng(locationLocation.getLatitude(), locationLocation.getLongitude()), 15)));
//
//        }
//
//        /**
//         * Non Map-Related Instantiations
//         */
//        {
//            connectGoogleAPIClient();
//            mGeofenceList = new ArrayList<>();
//
//            placeAutocompleteFragment = (PlaceAutocompleteFragment) MapFragment.getFragActivity().getFragmentManager().findFragmentById(R.id.searchFragment);
//
//            if(placeAutocompleteFragment != null){
//                placeAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
//                    @Override
//                    public void onPlaceSelected(Place place) {
//                        Log.d(TAG, "Selected place Add is : " + place.getAddress().toString());
//                        Log.d(TAG, "Selected place LatLng is : " + place.getLatLng().toString());
//                        Log.d(TAG, "Selected place Name is : " + place.getName().toString());
//                        clearRedundant();
//                        chosenMarker = mMap.addMarker(new MarkerOptions()
//                                .position(place.getLatLng())
//                                .title(place.getName().toString()));
//                        chosenCircle = mMap.addCircle(new CircleOptions()
//                                .center(place.getLatLng())
//                                .radius(200)
//                                .strokeColor(Color.BLACK)
//                                .fillColor(0x00000000));
//                        mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
//                    }
//
//                    @Override
//                    public void onError(Status status) {
//                        Toast.makeText(MapFragment.getFragmentContext(), "Cannot find place", Toast.LENGTH_SHORT).show();
//                        Log.e(TAG, status.getStatusMessage());
//                    }
//                });
//            }
//            else{
//                Log.e(TAG, "placeAutocomplete Fragment not loaded");
//            }
//            destinationDictionary = new HashMap<Marker, Circle>() {
//            };
//
//            userState = UserState.NORMAL;
//
//            assignButtons();
//        }
//    }
//
//    private void connectGoogleAPIClient() {
//        if(!mGoogleAPIClient.isConnected()){
//            mGoogleAPIClient.connect();
//        }
//    }
//
//    private void assignButtons(){
//
//        makeGeofenceAtMarker_Button = (Button) MapFragment.getFragActivity().findViewById(R.id.makeGeofenceAtMarker_button);
//        deleteGeofenceAtMarker_Button = (Button) MapFragment.getFragActivity().findViewById(R.id.deleteMarker_button);
//        usePlacePicker_Button = (Button) MapFragment.getFragActivity().findViewById(R.id.button_placePicker_activity_maps);
//
//        makeGeofenceAtMarker_Button.setVisibility(View.INVISIBLE);
//        deleteGeofenceAtMarker_Button.setVisibility(View.INVISIBLE);
////        usePlacePicker_Button.setVisibility(View.INVISIBLE);
//
//        makeGeofenceAtMarker_Button.setOnClickListener(makeGeofenceAtMarkerListener);
//        deleteGeofenceAtMarker_Button.setOnClickListener(deleteGeofenceAtMarkerListener);
//        usePlacePicker_Button.setOnClickListener(removeGeofencesListener);
//    }
//
//    public GoogleMap getMap(){
//        return mMap;
//    }
//
//    private void createLocationRequest() {
//        locationRequest = new LocationRequest();
//        locationRequest.setInterval(80000);
//        locationRequest.setFastestInterval(2000);
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//    }
//
//    /**
//     * Geofence
//     */
//
//    private void AddGeofenceAtLocation(View v){
//        if(chosenMarker.getPosition() == null){
//            Log.e(TAG, "chosenMarker is null");
//            return;
//        }
//
//        if(!mGoogleAPIClient.isConnected() || mGoogleAPIClient.isConnecting() ){
//            Log.e(TAG, "Google API Client is not connected yet");
//            connectGoogleAPIClient();
//        }
//        if(!mGoogleAPIClient.isConnected()) return;
//        GeofenceManager.addGeofence(mMap, chosenMarker.getPosition(), mGeofenceList,destinationDictionary);
//        GeofenceManager.SendGeofence(v, mGeofenceList, mGoogleAPIClient, mGeofencePendingIntent, MapFragment.getFragmentContext(), this);
//
//        Log.d(TAG, "calling addGeofence(chosenMarker.getPosition())");
//    }
//
//    private void removeGeofences(){
//        LocationServices.GeofencingApi.removeGeofences(
//                mGoogleAPIClient,
//                GeofenceManager.getGeofencePendingIntent(mGeofencePendingIntent, MapFragment.getFragmentContext())).setResultCallback(this);
//        Log.d(TAG, "Removed Geofences");
//    }
//    /**
//     * Map Modifiers
//     */
//    private void clearRedundant(){
//        if(userState != UserState.SELECTING_MARKER){
//            if(chosenCircle != null) {
//                chosenCircle.remove();
//            }
//            if(chosenMarker != null) {
//                chosenMarker.remove();
//            }
//        }
//
////        isMarkerClickedOnExistingDestination = false;
//
//        if(deleteGeofenceAtMarker_Button.getVisibility() == View.VISIBLE){
//            deleteGeofenceAtMarker_Button.setVisibility(View.INVISIBLE);
//        }
//
//        if(makeGeofenceAtMarker_Button.getVisibility() == View.VISIBLE){
//            makeGeofenceAtMarker_Button.setVisibility(View.INVISIBLE);
//        }
////        userState = UserState.NORMAL;
//        Log.d(TAG, "clearRedundant()");
//    }
//
//    private void createDestinationMarker(LatLng latLng) {
//        clearRedundant();
//        chosenCircle = mMap.addCircle(new CircleOptions()
//                .center(latLng)
//                .radius(200)
//                .strokeColor(Color.BLACK)
//                .fillColor(0x00000000));
//        chosenMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Picked"));
//        makeGeofenceAtMarker_Button.setVisibility(View.VISIBLE);
//        userState = UserState.NORMAL;
//    }
//    private void changeCamera(CameraUpdate update) {
//        changeCamera(update, null);
//    }
//
//
//    private void changeCamera(CameraUpdate update, GoogleMap.CancelableCallback callback) {
//        mMap.animateCamera(update);
//    }
//
//    /** Listeners */
//    private View.OnClickListener makeGeofenceAtMarkerListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            AddGeofenceAtLocation(v);
//            userState = UserState.ADDING_MARKER;
//            clearRedundant();
//        }
//    };
//
//    private View.OnClickListener deleteGeofenceAtMarkerListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            userState = UserState.DELETING_MARKER;
//            clearRedundant();
//        }
//    };
//
//    private View.OnClickListener removeGeofencesListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            removeGeofences();
//            Log.i(TAG, "Remove Geofences()");
//        }
//    };
//    private GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener(){
//        @Override
//        public void onMapClick(LatLng latLng){
//            Log.i(TAG, "Map Clicked at " + latLng.toString());
//            clearRedundant();
//        }
//    };
//
//    private GoogleMap.OnMapLongClickListener onMapLongClickListener = new GoogleMap.OnMapLongClickListener() {
//        @Override
//        public void onMapLongClick(LatLng latLng) {
//            Log.i(TAG, "Marker Clicked");
//            createDestinationMarker(latLng);
//        }
//    };
//
//    private GoogleMap.OnMarkerClickListener onMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
//        @Override
//        public boolean onMarkerClick(Marker marker) {
//            Log.i(TAG, "Marker Clicked");
//            boolean marker_exists_in_dictionary = false;
//
//            //check if marker exists in geofence
//            for(Marker m : destinationDictionary.keySet()){
//                if (m.equals(marker)){
//                    marker_exists_in_dictionary = true;
//                    chosenMarker = m;
//                    chosenCircle = destinationDictionary.get(m);
//                }
//            }
//
//            if(marker_exists_in_dictionary){
//                isMarkerClickedOnExistingDestination= true;
//                chosenMarker.showInfoWindow();
//                userState = UserState.SELECTING_MARKER;
//
//                deleteGeofenceAtMarker_Button.setVisibility(View.VISIBLE);
//            }
//            else{
//                makeGeofenceAtMarker_Button.setVisibility(View.VISIBLE);
//            }
//            Log.d(TAG, marker.getTitle() + " clicked");
////        Log.d(TAG, "Cirlce getcenter(): " + circle.getCenter().toString());
//
//
//            return false;
//        }
//    };
//
//
//    /** Connections */
//    @Override
//    public void onConnected(@Nullable Bundle bundle) {
//        Log.i(TAG, "onConnected");
//        startLocationUpdates();
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//        Log.i(TAG, "onConnectionSuspended");
//        stopLocationUpdates();
//
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        Log.i(TAG, "onConnectionFailed");
//    }
//
//    private void startLocationUpdates(){
//        try{
//            LocationServices.FusedLocationApi.requestLocationUpdates(
//                    mGoogleAPIClient, locationRequest, myLocationListener);
//            Log.i(TAG, "Started Location Updates");
//        }catch (SecurityException e){
//            Log.e(TAG, e.getMessage());
//        }
//    }
//
//    private void stopLocationUpdates(){
//        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleAPIClient, myLocationListener);
//        Log.i(TAG, "Stopped Location Updates");
//
//    }
//
//
//    // For RemoveGeofence onResult Callback
//    @Override
//    public void onResult(@NonNull Status status) {
//
//    }
//}
