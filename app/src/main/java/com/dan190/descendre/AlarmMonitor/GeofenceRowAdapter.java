package com.dan190.descendre.AlarmMonitor;

import android.app.Activity;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dan190.descendre.Geofence.MyGeofence;
import com.dan190.descendre.R;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Exchanger;

/**
 * Created by Dan on 25/11/2016.
 */

public class GeofenceRowAdapter extends BaseAdapter {
    private Button deleteGeofence;
    private TextView addressTextView, distanceTextView;
    private ImageView mapImage;

    private Context context;
    private List<MyGeofence> myGeofenceList;
//    private MapFragment mapFragment;

    private static String sClassName = "GeofenceRowAdapter";

    public GeofenceRowAdapter(Context context, List<MyGeofence> myGeofenceList){
        this.context = context;
        this.myGeofenceList = myGeofenceList;
    }

    @Override
    public int getCount() {
        return myGeofenceList.size();
    }

    @Override
    public Object getItem(int position) {
        return myGeofenceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        Log.w(sClassName, "getItemId(int position) is not implemented");
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyGeofence myGeofence = (MyGeofence) getItem(position);

        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView =inflater.inflate(R.layout.geofence_row, parent, false);

        mapImage = (ImageView) convertView.findViewById(R.id.mapImage);
        addressTextView = (TextView) convertView.findViewById(R.id.address);
        distanceTextView = (TextView) convertView.findViewById(R.id.distance);
        deleteGeofence = (Button) convertView.findViewById(R.id.deleteButton);

        addressTextView.setText(myGeofence.getCenter().toString());
        distanceTextView.setText("Distance: " + Float.toString(myGeofence.getDistance()));

        String url = "http://maps.google.com/maps/api/staticmap?center="
                + myGeofence.getCenter().latitude
                + ","
                + myGeofence.getCenter().longitude
                + "&zoom=20&size=200x200&sensor=false";

        new DownloadImageTask(mapImage).execute(url);
        return convertView;
    }

    /**
     * AsyncTask for loading static images from google maps
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap>{
        ImageView imageView;
        public DownloadImageTask(ImageView imageView){
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            Bitmap icon = null;
            try{
                InputStream in = new java.net.URL(url).openStream();
                icon = BitmapFactory.decodeStream(in);
            }catch (Exception e){
                Log.e(sClassName, e.getMessage());
                e.printStackTrace();
            }
            return icon;
        }

        @Override
        protected void onPostExecute(Bitmap result){
            imageView.setImageBitmap(result);
        }
    }
//    private View.OnClickListener deleteGeofenceListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            mapFragment.deleteGeofenceOnMap();
//        }
//    };
}
