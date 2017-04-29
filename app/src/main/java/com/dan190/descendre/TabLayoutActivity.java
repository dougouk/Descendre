package com.dan190.descendre;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TableLayout;

import com.dan190.descendre.AlarmMonitor.AlarmMonitorFragment;
import com.dan190.descendre.Geofence.MyGeofence;
import com.dan190.descendre.Map.MapFragment;
import com.dan190.descendre.Map.MapViewFragment;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Dan on 11/11/2016.
 */

public class TabLayoutActivity extends AppCompatActivity
        implements MapFragment.OnGeofenceListener,
                    AlarmMonitorFragment.OnAlarmMonitorListener,
        MapViewFragment.onDirectionRequestedListener
{
    private static String TAG = TabLayoutActivity.class.getName();



    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_layout);
        getSupportActionBar().hide();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.map_icon));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.bus_stop2));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.alarm));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.settings));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new PagerAdapter
                (getFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                Log.i(TAG, tab.toString() + " selected");
//                android.app.Fragment fragment = TabLayoutActivity.this.getFragmentManager()
//                        .findFragmentByTag(MapFragment.mapFragmentKey);
//                if(fragment!= null){
//                    Log.d(TAG, "Fragment is not null");
//                    TabLayoutActivity.this.getFragmentManager().beginTransaction()
//                            .remove(fragment);
//                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.i(TAG, "onTabUnselected()");
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    @Override
    public void onCreateGeofence(List<MyGeofence> myGeofenceList) {
        Log.d("TabLayoutAcitivty", "onCreateGeofence()");

        AlarmMonitorFragment alarmMonitorFragment = (AlarmMonitorFragment)
                getFragmentManager().findFragmentById(R.id.alarmMonitorFragment);

        if(alarmMonitorFragment == null){
            Log.w(TAG, "alarmMonitorFragment is null");

            alarmMonitorFragment = new AlarmMonitorFragment();
            Bundle args = new Bundle();
            //??

            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            transaction.replace(R.id.alarmMonitorFragment, alarmMonitorFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        else{
            alarmMonitorFragment.setListView(myGeofenceList);
        }
    }

    @Override
    public void onRemoveGeofence(MyGeofence myGeofence) {

    }

    @Override
    public void removeAllGeofences() {
        //TODO
//        MapFragment mapFragment = null;
//        if(mapFragment == null){
//            Log.w(TAG, "mapFragment is null");
//
//            mapFragment = new MapFragment();
//            Bundle args = new Bundle();
//            //??
//
//            FragmentTransaction transaction = getFragmentManager().beginTransaction();
//
//            transaction.replace(R.id.frameContainer, mapFragment);
//            transaction.addToBackStack(null);
//            transaction.commit();
//        }
//        else{
//            mapFragment.clearAll();
//        }
    }

    @Override
    public void onDirectionRequested(LatLng origin, Place destination) {
        pagerAdapter.onDirectionRequested(origin, destination);
    }

    @Override
    public void onDirectionRequested(LatLng origin, LatLng destination) {
        pagerAdapter.onDirectionRequested(origin, destination);

    }
}
