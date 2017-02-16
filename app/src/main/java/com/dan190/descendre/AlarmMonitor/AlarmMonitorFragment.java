package com.dan190.descendre.AlarmMonitor;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.dan190.descendre.Geofence.MyGeofence;
import com.dan190.descendre.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Dan on 11/11/2016.
 */

public class AlarmMonitorFragment extends Fragment {
    private static String sFragmentName = "ALARM_MONITOR_FRAGMENT";

    private ListView listView;
    private static View view;
    private Button clearAllButton;
    OnAlarmMonitorListener mAlarmMonitorListener;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mAlarmMonitorListener = (OnAlarmMonitorListener) context;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        view = inflater.inflate(R.layout.alarm_monitor_fragment, container, false);
        clearAllButton = (Button) view.findViewById(R.id.removeAllGeofenceButton);
        clearAllButton.setOnClickListener(removeAllListener);
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


    /**
     * Listener
     */

    private View.OnClickListener removeAllListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            removeAllGeofences(mGoogleAPIClient);
//            mMap.clear();
//            Log.i(ACTIVITY_NAME, "Remove ALL Geofences()");
            mAlarmMonitorListener.removeAllGeofences();
        }
    };

    public interface OnAlarmMonitorListener {
        public void removeAllGeofences();
    }
}
