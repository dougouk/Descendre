package com.dan190.descendre.Helpers;

import com.dan190.descendre.R;
import com.dan190.descendre.Util.BusStop;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dan on 29/04/2017.
 */

public class BusStopJSON {
    public static String getNearbyBusStops(LatLng location, int radius, String key){
        return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + location.latitude + "," + location.longitude +
                "&radius=" + radius + "&types=bus_station&sensor=true&key="+ key;
    }

    public static List<BusStop> parseJSON(String jsonData) throws JSONException {
        JSONObject json = new JSONObject(jsonData);
        JSONArray results = json.getJSONArray("results");
        List<BusStop> busStopList = new ArrayList<>();
        for(int i = 0; i < results.length(); i++){
            JSONObject busStopJSON = results.getJSONObject(i);

            JSONObject geometry = busStopJSON.getJSONObject("geometry");

            JSONObject location = geometry.getJSONObject("location");
            double lat = location.getDouble("lat");
            double lng = location.getDouble("lng");

            String name = busStopJSON.getString("name");
            BusStop busStop = new BusStop(new LatLng(lat, lng), name);
            busStopList.add(busStop);
        }
        return busStopList;
    }
}
