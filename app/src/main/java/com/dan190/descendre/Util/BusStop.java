package com.dan190.descendre.Util;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Dan on 29/04/2017.
 */

public class BusStop {
    LatLng latLng;
    String name;

    public BusStop(){}
    public BusStop(LatLng latLng, String name){
        this.latLng = latLng;
        this.name = name;
    }
    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
