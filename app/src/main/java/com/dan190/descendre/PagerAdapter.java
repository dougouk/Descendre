package com.dan190.descendre;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.dan190.descendre.AlarmMonitor.AlarmMonitorFragment;
import com.dan190.descendre.Directions.DirectionsFragment;
import com.dan190.descendre.Map.MapViewFragment;
import com.dan190.descendre.Settings.SettingsFragment;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Dan on 11/11/2016.
 */

public class PagerAdapter extends FragmentStatePagerAdapter
                            implements MapViewFragment.onDirectionRequestedListener{
    private static String TAG = PagerAdapter.class.getName();
    int mNumOfTabs;

    Fragment[] fragments = new Fragment[4];

    //tabs
    MapViewFragment tab1;
    DirectionsFragment tab2;
    AlarmMonitorFragment tab3;
    SettingsFragment tab4;

    public PagerAdapter(FragmentManager fragmentManager, int mNumOfTabs){
        super(fragmentManager);
        this.mNumOfTabs = mNumOfTabs;
    }
    @Override
    public Fragment getItem(int position) {
        if(fragments[position] == null){
            switch (position){
                case 0:
                    tab1 = new MapViewFragment();
                    return tab1;
                case 1:
                    tab2 = new DirectionsFragment();
                    return tab2;
                case 2:
                    tab3 = new AlarmMonitorFragment();
                    return tab3;
                case 3:
                    tab4 = new SettingsFragment();
                    return tab4;
                default:
                    return null;
            }
        }
        else{
            return fragments[position];
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

    @Override
    public void onDirectionRequested(LatLng origin, Place destination) {

        if(tab2 == null){
            tab2 = new DirectionsFragment();
        }
        tab2.onDirectionRequested(origin, destination);
    }

    @Override
    public void onDirectionRequested(LatLng origin, LatLng destination) {
        if(tab2 == null){
            tab2 = new DirectionsFragment();
        }
        tab2.onDirectionRequested(origin, destination);
    }
}
