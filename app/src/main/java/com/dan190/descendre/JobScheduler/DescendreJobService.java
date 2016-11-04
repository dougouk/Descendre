package com.dan190.descendre.JobScheduler;

import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.util.Log;

/**
 * Created by Dan on 04/11/2016.
 */

public class DescendreJobService extends JobService{
    private final String CLASS_NAME = "JobService";
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(CLASS_NAME, String.valueOf(params.getJobId()));
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(CLASS_NAME, "onStopJob");
        return false;
    }
}
