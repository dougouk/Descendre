package com.dan190.descendre.Settings;

import android.util.Log;

import com.dan190.descendre.Util.Keys;
import com.dan190.descendre.Util.Prefs;

/**
 * Created by Dan on 16/11/2016.
 */

public class UserSettings {
    private final boolean light;
    private final boolean vibration;
    private final boolean sound;

    private final static String ACTIVITY_NAME = "USER_SETTINGS";

    public UserSettings(boolean light, boolean sound, boolean vibration){
        this.light = light;
        this.vibration = vibration;
        this.sound = sound;
    }

    public boolean shouldLight(){
        return light;
    }
    public boolean shouldVibrate() {
        return vibration;
    }

    public boolean shouldSound() {
        return sound;
    }

    public static UserSettings get(){
        int status = Prefs.getInt(Keys.NOTIFICATION_STATUS, -1);
        if (status == -1){
            Log.i(ACTIVITY_NAME, "Creating default settings");
            return new UserSettings(false, false, false);
        }
        else{
            boolean light = (status & 1) > 0;
            boolean vibrate = (status & 2) > 0;
            boolean sound = (status & 4) > 0;
            Log.i(ACTIVITY_NAME, "Retrieved settings");
            return new UserSettings(light, sound, vibrate);
        }

    }

    public static void save(UserSettings settings){
        int status = 0;
        status += settings.shouldLight() ? 1 : 0;
        status += settings.shouldVibrate() ? 1 : 0;
        status += settings.shouldSound() ? 1 : 0;

        Prefs.putInt(Keys.NOTIFICATION_STATUS, status);
        Log.i(ACTIVITY_NAME, "Saving settings");
    }

}
