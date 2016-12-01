package com.dan190.descendre.Map;

import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Dan on 17/11/2016.
 */

public class Listeners {
    public GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener(){
        @Override
        public void onMapClick(LatLng latLng){
            Log.i("Listener", "Map Clicked");
        }
    };

}
