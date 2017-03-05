package com.ece416.aruproy.messengerclient;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by ilikecalculus on 2017-03-04.
 */

public class MessengerService extends Service {

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return 1;
    }

    @Override
    public void onCreate()
    {

    }

    @Override
    public void onDestroy() {}
}
