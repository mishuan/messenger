package com.ece416.aruproy.messengerclient;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by ilikecalculus on 2017-03-24.
 */

public class ConnectTask extends AsyncTask<String, String, TcpClient> {

    private static ConnectTask instance = null;
    private static TcpClient mTcpClient;
    private static String ipAddress = null;
    private static Queue<ArrayList<String>> messageQueue = new LinkedList<>();

    private ConnectTask() {}

    public static ConnectTask getInstance() {
        if(instance == null) {
            instance = new ConnectTask();
        }
        return instance;
    }

    public void setIp(String ip){
        ipAddress = ip;
        instance.execute("");
    }

    public String getIp(){
        return ipAddress;
    }

    public TcpClient getTcpClient() {
        return mTcpClient;
    }


    // TODO: finish this up
    public ArrayList<String> getMessageForActivity(String id) {
        return new ArrayList<String>();
    }

    @Override
    protected TcpClient doInBackground(String... message) {

        //we create a TCPClient object
        mTcpClient = new TcpClient(ipAddress, new TcpClient.OnMessageReceived() {
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
