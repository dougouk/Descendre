package com.dan190.descendre.Geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Dan on 04/11/2016.
 */

public class GeofenceManager {

    private static String ACTIVITY_NAME = "Geofence Manager";


    public static void addGeofence(GoogleMap map,
                            LatLng latlng,
                            List<Geofence> mGeofenceList,
                            Map<Marker, Circle> destinationDictionary){
        mGeofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(String.format("%f_%f", latlng.latitude, latlng.longitude))
                .setCircularRegion(
                        latlng.latitude,
                        latlng.longitude,
                        200
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER /*|
                            Geofence.GEOFENCE_TRANSITION_EXIT*/)
                .build());
        Log.d("GeofenceManager" , "Added Geofence");
        addDestinationToMyList(map, latlng, destinationDictionary);
    }

    public static void addDestinationToMyList(GoogleMap mMap,
                                              LatLng latLng,
                                              Map<Marker, Circle> destinationDictionary){
        Circle newCircle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(250)
                .strokeColor(Color.GREEN)
                .fillColor(0x30CCCCFF));

        Marker newMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Destination"));

        destinationDictionary.put(newMarker, newCircle);
        Log.d("GeofenceManager", "Added new destination to listOfDestinations");
    }

    private static GeofencingRequest getGeofencingRequest(List<Geofence> mGeofenceList) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }
    public static void SendGeofence(View v,
                                    List<Geofence> mGeofenceList,
                                    GoogleApiClient mGoogleAPIClient,
                                    PendingIntent mGeofencePendingIntent,
                                    Context context,
                                    @NonNull ResultCallback resultCallback){
        Log.d("GeofenceManager", String.format("Geofence list size : %d", mGeofenceList.size()));
        try{
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleAPIClient,
                    getGeofencingRequest(mGeofenceList),
                    getGeofencePendingIntent(mGeofencePendingIntent, context)
            ).setResultCallback(resultCallback);
        }catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(ACTIVITY_NAME, "Need FINE_ACCESS_LOCATION permission");
        }
        Log.d(ACTIVITY_NAME, "Geofences added to Google Client");
    }

    public static PendingIntent getGeofencePendingIntent(PendingIntent mGeofencePendingIntent, Context context){

        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            Log.d(ACTIVITY_NAME, "pendingIntent already exists");

            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(context, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    public static void removeParticularGeofence(GoogleApiClient mGoogleAPIClient, String key){
        LocationServices.GeofencingApi.removeGeofences(
                mGoogleAPIClient,
                new ArrayList<String>(Arrays.asList(key))
        );
    }


}
