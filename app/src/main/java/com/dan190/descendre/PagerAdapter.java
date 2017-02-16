package com.dan190.descendre;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import com.dan190.descendre.AlarmMonitor.AlarmMonitorFragment;
import com.dan190.descendre.Directions.DirectionsFragment;
import com.dan190.descendre.Map.MapViewFragment;
import com.dan190.descendre.Settings.SettingsFragment;

/**
 * Created by Dan on 11/11/2016.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    Fragment[] fragments = new Fragment[4];

    public PagerAdapter(FragmentManager fragmentManager, int mNumOfTabs){
        super(fragmentManager);
        this.mNumOfTabs = mNumOfTabs;
    }
    @Override
    public Fragment getItem(int position) {
        if(fragments[position] == null){
            switch (position){
                case 0:
                    MapViewFragment tab1 = new MapViewFragment();
                    return tab1;
                case 1:
                    DirectionsFragment tab2 = new DirectionsFragment();
                    return tab2;
                case 2:
                    AlarmMonitorFragment tab3 = new AlarmMonitorFragment();
                    return tab3;
                case 3:
                    SettingsFragment tab4 = new SettingsFragment();
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

}
