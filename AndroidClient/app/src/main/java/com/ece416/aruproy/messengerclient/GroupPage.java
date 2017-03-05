package com.ece416.aruproy.messengerclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class GroupPage extends AppCompatActivity {

    private BroadcastReceiver mBroadcastReceiver;
    private IntentFilter mIntentFilter;
    private TcpClient mTcpClient;

    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        @Override
        protected TcpClient doInBackground(String... message) {

            //we create a TCPClient object
            mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //response received from server
            Log.d("test", "response " + values[0]);
            //process server response here....
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // broadcast receiver registration
        mBroadcastReceiver = new LocalBroadcastReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constants.SERVICE_INTENT_ID);
        registerReceiver(mBroadcastReceiver, mIntentFilter);

        // Server service registration
        Intent serverIntent = new Intent(this, ServerService.class);
        startService(serverIntent);

        new ConnectTask().execute("");
    }

    private class LocalBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(Constants.ACTIVITY_DEBUG_TAG, intent.getStringExtra("isReachable"));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            unregisterReceiver(mBroadcastReceiver);
        } catch (java.lang.IllegalArgumentException e) {
            Log.e(Constants.ACTIVITY_DEBUG_TAG, "receiver already unregistered", e);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        try {
            unregisterReceiver(mBroadcastReceiver);
        } catch (java.lang.IllegalArgumentException e) {
            Log.e(Constants.ACTIVITY_DEBUG_TAG, "receiver already unregistered", e);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
