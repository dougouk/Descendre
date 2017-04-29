package com.dan190.descendre.Directions;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dan190.descendre.Helpers.BusStopJSON;
import com.dan190.descendre.Map.MapViewFragment;
import com.dan190.descendre.R;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Dan on 25/01/2017.
 */

public class DirectionsFragment extends Fragment
                                    implements MapViewFragment.onDirectionRequestedListener{

    private String TAG = DirectionsFragment.class.getName();
    private TextView test;

    public static String DirectionsFragmentKey = "dir";

    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        if(view == null){
            Log.d(TAG, "view is null");

            view = inflater.inflate(R.layout.directions_fragment, container, false);
            test = (TextView) view.findViewById(R.id.test);
            LatLng fort = new LatLng(45.4911845, -73.5814331);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(getNearbyBusStops(fort, 300));
            return view;
        }
        else{
            return view;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

//        getFragmentManager().beginTransaction().replace(
//                R.id.frameContainer, this, DirectionsFragmentKey).commit();

    }

    @Override
    public void onDirectionRequested(LatLng origin, Place destination) {
        Log.i(TAG, "onDirectionRequested received at DirectionsFragment");
    }

    @Override
    public void onDirectionRequested(LatLng origin, LatLng destination) {
        Log.i(TAG, "onDirectionRequested received at DirectionsFragment");

    }

    private String getStaticHTTPSRequest(LatLng origin, LatLng destination){
        return "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + origin.latitude + "," + origin.longitude + "&" +
                "destination=" + destination.latitude + "," + destination.longitude + "&" +
                "key=AIzaSyDRwAzRVQcyETDA6dyJeUBjbw-5C018dqQ";
    }

    private String getNearbyBusStops(LatLng location, int radius){
        return BusStopJSON.getNearbyBusStops(location, radius, getResources().getString(R.string.google_maps_key));
    }

    private String retrieveJSON(String inputURL) throws IOException {
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        String data = "";
        try{
            URL url = new URL(inputURL);

            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();
            inputStream = httpURLConnection.getInputStream();
            BufferedReader bfr = new BufferedReader(new InputStreamReader(inputStream));

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while((line = bfr.readLine()) != null){
                stringBuffer.append(line);
            }

            data = stringBuffer.toString();
            bfr.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(inputStream!=null) inputStream.close();
            httpURLConnection.disconnect();
        }
        return data;
    }


    class ParserTask extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... params) {
            String data = "";
            try {
                data = retrieveJSON(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);
            test.setText(result);
        }
    }
}
