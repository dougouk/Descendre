package com.dan190.descendre;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

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
                SettingsFragment tab1 = new SettingsFragment();
                return tab1;
            case 1:
                AlarmMonitorFragment tab2 = new AlarmMonitorFragment();
                return tab2;
            case 2:
                AlarmMonitorFragment tab3 = new AlarmMonitorFragment();
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
