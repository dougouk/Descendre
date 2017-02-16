package com.dan190.descendre.Directions;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dan190.descendre.R;

/**
 * Created by Dan on 25/01/2017.
 */

public class DirectionsFragment extends Fragment {

    private String TAG = DirectionsFragment.class.getName();
    public static String DirectionsFragmentKey = "dir";

    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        if(view == null){
            Log.d(TAG, "view is null");

            view = inflater.inflate(R.layout.directions_fragment, container, false);
            return view;
        }
        else{
            return view;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

//        getFragmentManager().beginTransaction().replace(
//                R.id.frameContainer, this, DirectionsFragmentKey).commit();

    }

}
