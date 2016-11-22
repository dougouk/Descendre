package com.dan190.descendre.Map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dan190.descendre.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by Dan on 16/11/2016.
 */

public class MapFragment extends Fragment {
    static final String ACTIVITY_NAME = "MAP_FRAGMENT";

    private static View view;
    private static Context context;
    private static Activity activity;

    private LocationManager locationManager;
    private Location locationLocation;
    private LocationRequest locationRequest;

    private String provider;

    private MapManager mapManager;
    private SupportMapFragment mapFragment;
    private FragmentManager fragmentManager;

    private static GoogleApiClient mGoogleAPIClient;

    private  GoogleMap mMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.activity_maps, container, false);
        }

        activity = getActivity();
        context = getContext();

        askForLocationPermission();
//        if (mapManager == null) mapManager = new MapManager();


//        createLocationRequest();

        Log.i(ACTIVITY_NAME, "Creating MapFragment View");

        return view;
    }

    private void initializeGoogleAPIClient() {
        Log.i(ACTIVITY_NAME, "Initialize Google API Client");
        if(mGoogleAPIClient == null){

            mGoogleAPIClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(mapManager)
                    .addOnConnectionFailedListener(mapManager)
                    .build();
            Log.i(ACTIVITY_NAME, "Created Google API Client");
        }
        if(!mGoogleAPIClient.isConnected()) {
            mGoogleAPIClient.connect();
            Log.i(ACTIVITY_NAME, "Connected Google API Client");
        }
    }

//    private void createLocationRequest() {
//        locationRequest = new LocationRequest();
//        locationRequest.setInterval(80000);
//        locationRequest.setFastestInterval(2000);
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//    }

    public static Activity getFragActivity() {
        return activity;
    }
    public static Context getFragmentContext() {
        return context;
    }
    public static GoogleApiClient getGoogleAPIClient(){return mGoogleAPIClient;}

    private void askForLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            //has already seen permission
            Log.i(ACTIVITY_NAME, "Has Already seen permission");
            if (ActivityCompat.checkSelfPermission(getFragActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getFragActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            }

        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            Log.i(ACTIVITY_NAME, "Permission Granted");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(ACTIVITY_NAME, "onActivityCreated()");

        fragmentManager = getChildFragmentManager();

        if(mapFragment == null){
            mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);
            Log.i(ACTIVITY_NAME, "Assigned mapFragment");
        }

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            fragmentManager.beginTransaction().replace(R.id.map, mapFragment).commit();
            Log.d(ACTIVITY_NAME, "Replaced Map Fragment");
        }
        if (mapManager == null) mapManager = new MapManager();
        initializeGoogleAPIClient();

        Log.d(ACTIVITY_NAME, "MapFragment: " + mapFragment.toString());
        Log.d(ACTIVITY_NAME, "MapManager: " + mapManager.toString());

        mapFragment.getMapAsync(mapManager);
        Log.i(ACTIVITY_NAME, "Called getMapAsync");

        if(savedInstanceState == null){
            mapFragment.setRetainInstance(true);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(ACTIVITY_NAME, "onDetach()");

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //mapFragment.isVisible();
        Log.i(ACTIVITY_NAME, "onAttach()");
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
    }

    @Override
    public void onDestroyView(){
        Log.i(ACTIVITY_NAME, "onDestroyView");
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
}
