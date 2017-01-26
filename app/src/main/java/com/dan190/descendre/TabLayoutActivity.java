package com.dan190.descendre;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.dan190.descendre.AlarmMonitor.AlarmMonitorFragment;
import com.dan190.descendre.Geofence.MyGeofence;
import com.dan190.descendre.Map.MapFragment;

import java.util.List;
import java.util.Map;

/**
 * Created by Dan on 11/11/2016.
 */

public class TabLayoutActivity extends AppCompatActivity
        implements MapFragment.OnGeofenceListener,
AlarmMonitorFragment.OnAlarmMonitorListener{
    private static String ACTIVITY_NAME = "TabLayoutActivity";
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

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.i(ACTIVITY_NAME, "onTabUnselected()");
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onCreateGeofence(List<MyGeofence> myGeofenceList) {
        Log.d("TabLayoutAcitivty", "onCreateGeofence()");

        AlarmMonitorFragment alarmMonitorFragment = (AlarmMonitorFragment)
                getSupportFragmentManager().findFragmentById(R.id.alarmMonitorFragment);

        if(alarmMonitorFragment == null){
            Log.w(ACTIVITY_NAME, "alarmMonitorFragment is null");

            alarmMonitorFragment = new AlarmMonitorFragment();
            Bundle args = new Bundle();
            //??

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

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
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if(mapFragment == null){
            Log.w(ACTIVITY_NAME, "mapFragment is null");

            mapFragment = new MapFragment();
            Bundle args = new Bundle();
            //??

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.replace(R.id.map, mapFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        else{
            mapFragment.clearAll();
        }
    }
}
