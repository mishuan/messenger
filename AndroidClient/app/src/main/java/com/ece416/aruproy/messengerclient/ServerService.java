package com.ece416.aruproy.messengerclient;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by ilikecalculus on 2017-03-04.
 */

public class ServerService extends Service {

    private ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
    private Intent serverIntent = null;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        if (serverIntent == null) {serverIntent = new Intent(Constants.SERVICE_INTENT_ID);}
        Log.e(Constants.SERVICE_DEBUG_TAG, "CREATING AND STARTING UP THE SERVICE...");
        scheduleTaskExecutor.scheduleAtFixedRate(new ServerRunnable(), 0, 5, TimeUnit.SECONDS);
    }

    private class ServerRunnable implements Runnable {

        @Override
        public void run() {
            Log.e(Constants.SERVICE_DEBUG_TAG, Constants.DEFAULT_TESTING_STRING);
            serverIntent.putExtra("isReachable", String.valueOf(isReachable(Constants.HOST)));
            sendBroadcast(serverIntent);
        }

        public boolean isReachable(String host)
        {
            try {
                //make a URL to a known source
                URL url = new URL("http://www.google.com");

                //open a connection to that source
                HttpURLConnection urlConnect = (HttpURLConnection)url.openConnection();

                //trying to retrieve data from the source. If there
                //is no connection, this line will fail
                Object objData = urlConnect.getContent();

            } catch (Exception e) {
                return false;
            }

            return true;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(Constants.SERVICE_DEBUG_TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }
}