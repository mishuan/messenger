package com.ece416.aruproy.messengerclient;

import android.os.AsyncTask;
import android.util.Log;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Created by ilikecalculus on 2017-03-24.
 */

public class ConnectTask extends AsyncTask<String, String, TcpClient> {

    private static ConnectTask instance = null;
    private static TcpClient mTcpClient;
    private static String ipAddress = null;
    private static String portNumber = null;
    private static Queue<Map<String, Object>> messageQueue = new LinkedList<>();

    private ConnectTask() {}

    public static ConnectTask getInstance() {
        if(instance == null) {
            instance = new ConnectTask();
        }
        return instance;
    }

    public void setIpAndPort(String ip, String port){
        ipAddress = ip;
        portNumber = port;
        instance.execute("");
    }

    public String getIp(){
        return ipAddress;
    }

    public TcpClient getTcpClient() {
        return mTcpClient;
    }


    public Map<String, Object> getMessageForActivity(MessageType type) {
        while (MessageType.valueOf(String.valueOf(messageQueue.peek().get(Constants.MESSAGE_TYPE_KEY))) != type) {
            messageQueue.poll();
        }
        return messageQueue.poll();
    }

    @Override
    protected TcpClient doInBackground(String... message) {

        //we create a TCPClient object
        mTcpClient = new TcpClient(ipAddress, portNumber, new TcpClient.OnMessageReceived() {
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
        try {
            Map<String, Object> map = JSONUtil.jsonToMap(values[0]);
            messageQueue.add(map);
        } catch (Exception e) {
            Log.e("JSON_EXCEPTION", "Error converting:\n" + values[0] + "\nTo a Java Map");
        }
    }
}
