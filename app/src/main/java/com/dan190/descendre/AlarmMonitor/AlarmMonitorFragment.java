package com.dan190.descendre.AlarmMonitor;

import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dan190.descendre.Geofence.MyGeofence;
import com.dan190.descendre.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Dan on 11/11/2016.
 */

public class AlarmMonitorFragment extends Fragment {
    private ListView listView;
    private static View view;
    private static String sFragmentName = "ALARM_MONITOR_FRAGMENT";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        view = inflater.inflate(R.layout.alarm_monitor_fragment, container, false);



        return view;
    }

    @UiThread
    public void setListView(List<MyGeofence> mGeofenceList){
        if(listView == null) listView = (ListView) view.findViewById(R.id.geofenceList);
        Log.i(sFragmentName, "setListView()");

        MyGeofence testG = new MyGeofence(new LatLng(00, 00));
        final GeofenceRowAdapter adapter = new GeofenceRowAdapter(getContext(), mGeofenceList);
        listView.post(new Runnable() {
            @Override
            public void run() {
                listView.setAdapter(adapter);
            }
        });
    }
}
