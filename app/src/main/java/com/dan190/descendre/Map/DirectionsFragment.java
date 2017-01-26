package com.dan190.descendre.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dan190.descendre.R;

/**
 * Created by Dan on 25/01/2017.
 */

public class DirectionsFragment extends Fragment {

    private static View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        if(view !=null){
            view = inflater.inflate(R.layout.directions_fragment, container, false);
            return view;
        }
        else{
            return view;
        }
    }
}
