package com.dan190.descendre;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.dan190.descendre.Map.MapFragment;
import com.dan190.descendre.Settings.SettingsFragment;

/**
 * Created by Dan on 11/11/2016.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fragmentManager, int mNumOfTabs){
        super(fragmentManager);
        this.mNumOfTabs = mNumOfTabs;
    }
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                MapFragment tab1 = new MapFragment();
                return tab1;
            case 1:
                AlarmMonitorFragment tab2 = new AlarmMonitorFragment();
                return tab2;
            case 2:
                SettingsFragment tab3 = new SettingsFragment();
                return tab3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

}
