package com.dan190.descendre.Settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.dan190.descendre.R;
import com.dan190.descendre.Util.Prefs;

/**
 * Created by Dan on 11/11/2016.
 */

public class SettingsFragment extends Fragment {

    CompoundButton mLight, mVibrate, mSound;
    final static String ACTIVITY_NAME = "SETTINGS_FRAGMENT";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.settings_fragment, container, false);


        mLight = (CompoundButton)view.findViewById(R.id.light_setting);
        mVibrate = (CompoundButton)view.findViewById(R.id.vibrate_setting);
        mSound = (CompoundButton)view.findViewById(R.id.sound_setting);

        mLight.setOnCheckedChangeListener(onCheckedChangeListener);
        mVibrate.setOnCheckedChangeListener(onCheckedChangeListener);
        mSound.setOnCheckedChangeListener(onCheckedChangeListener);

        return view;
    }

    private final CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            boolean light = mLight.isChecked();
            boolean vibrate = mVibrate.isChecked();
            boolean sound = mSound.isChecked();

            Log.i(ACTIVITY_NAME, "buttonChecked");
            UserSettings.save(new UserSettings(light, sound, vibrate));
        }
    };

    public void syncSettings(){
        UserSettings settings = UserSettings.get();
        mLight.setChecked(settings.shouldLight());
        mSound.setChecked(settings.shouldSound());
        mVibrate.setChecked(settings.shouldVibrate());
    }



}
