package com.dan190.descendre.JobScheduler;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.dan190.descendre.Map.MapViewFragment;

/**
* Created by Dan on 04/11/2016.
*/

public class AutoUpdate {
   private final long MILLISECOND_INTERVAL = 1000;

//   public static void scheduleJob(){
//       String APP_NAME = "AutoUpdate";
//       Log.d(APP_NAME, "scheduleJob()");
//       Context context = MapViewFragment.class;
//       JobScheduler jobScheduler =(JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
//       jobScheduler.cancelAll();
//
//       ComponentName componentName= new ComponentName(context, DescendreJobService.class);
//       JobInfo backgroundUpdates = new JobInfo.Builder(1, componentName)
//               .setPeriodic(1000)
//               .build();
//
//       int code = jobScheduler.schedule(backgroundUpdates);
//       if(code <= 0){
//           Log.e("AutoUpdate", "Failed to schedule background job");
//       }else
//       {
//           Log.d(APP_NAME, "Successfully scheduled");
//       }
//
//   }
//
//   public static void cancelJob(){
//       String APP_NAME = "AutoUpdate";
//
//       Context context = MapsActivity.getInstance();
//
//       JobScheduler jobScheduler =(JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
//       jobScheduler.cancelAll();
//       Log.d(APP_NAME, "Jobs cancelled");
//   }
}
