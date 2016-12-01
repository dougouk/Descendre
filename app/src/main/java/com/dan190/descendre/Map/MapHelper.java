package com.dan190.descendre.Map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import 	android.support.v13.app.FragmentCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.dan190.descendre.MapsActivity;

/**
 * Created by Dan on 17/11/2016.
 */

public class MapHelper {
    public static void askLocationPermission(final @NonNull Activity activity, final Context context){
        /*if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            Log.i("Map Helper", "Permission Already Granted");
            return;
        }*/
        if(ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.ACCESS_FINE_LOCATION)){

            //user has previously seen permission dialogue
        }
        else{
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    public static void askLocationPermission(Context  context) {
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED){
            return;
        }


    }
    /*public static void askLocationPermission_Fragment(Fragment fragment){
        if(FragmentCompat.shouldShowRequestPermissionRationale(fragment, Manifest.permission.ACCESS_FINE_LOCATION)){
            //user has already seen permission dialogue before
        }
        else{
            FragmentCompat.requestPermissions(fragment, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }*/

}
