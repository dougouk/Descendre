package com.dan190.descendre;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.dan190.descendre.Map.MapViewFragment;
import com.dan190.descendre.Util.Prefs;
import com.google.android.gms.location.places.ui.PlacePicker;

/**
 * Created by Dan on 09/11/2016.
 */

public class InitialPageActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.initial_page);

        Prefs.initialize(this);

        Button myMaps = (Button) findViewById(R.id.button_myMap);
        myMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapViewFragment.class);
                startActivity(intent);
            }
        });
        Button placePicker = (Button) findViewById(R.id.button_placePicker);
        placePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PlacePickerActivity.class);
                startActivity(intent);
            }
        });

        Button slidingView = (Button)findViewById(R.id.button_slidingViews);
        slidingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TabLayoutActivity.class);
                startActivity(intent);
            }
        });
    }


}
