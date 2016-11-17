package com.dan190.descendre.Map;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

/**
 * Created by Dan on 16/11/2016.
 */

public class MapManager implements OnMapReadyCallback {
    static final String ACTIVIT_NAME = "MAP_MANAGER";

    GoogleMap mMap;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(ACTIVIT_NAME, "onMapReady()");
    }

    public GoogleMap getMap(){
        return mMap;
    }
}
