package com.dan190.descendre.Map;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.dan190.descendre.R;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.Map;

/**
 * Created by Dan on 16/11/2016.
 */

public class MapFragment extends Fragment {
    static final String ACTIVITY_NAME = "MAP_FRAGMENT";

    static View view;

    MapManager mapManager;
    SupportMapFragment mapFragment;
    FragmentManager fragmentManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        if(view == null) {
            view = inflater.inflate(R.layout.activity_maps, container, false);
        }




        Log.i(ACTIVITY_NAME, "Creating MapFragment");

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Log.i(ACTIVITY_NAME, "onActivityCreated()");

        fragmentManager = getChildFragmentManager();

        mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);
        if(mapFragment == null){
            mapFragment = SupportMapFragment.newInstance();
            fragmentManager.beginTransaction().replace(R.id.map, mapFragment).commit();
            Log.d(ACTIVITY_NAME, "Replaced Map Fragment");
        }
        if(mapManager == null) mapManager = new MapManager();

        mapFragment.getMapAsync(mapManager);
        mapFragment.setRetainInstance(true);
    }

    @Override
    public void onDetach(){
        super.onDetach();
        Log.i(ACTIVITY_NAME, "onDetach()");
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        //mapFragment.isVisible();
        Log.i(ACTIVITY_NAME, "onAttach()");
    }
    @Override
    public void onDestroyView(){
        super.onDestroyView();
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        if(mapFragment != null){
            getFragmentManager().beginTransaction().remove(mapFragment).commit();
            Log.d(ACTIVITY_NAME, "Removed Map Fragment");
        }
    }
}
