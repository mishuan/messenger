package com.ece416.aruproy.messengerclient;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private TcpClient mTcpClient;

    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        @Override
        protected TcpClient doInBackground(String... message) {

            //we create a TCPClient object
            mTcpClient = new TcpClient(getIntent().getStringExtra(Constants.IP), new TcpClient.OnMessageReceived() {
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

    private EditText etGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Log.e(Constants.USERNAME, intent.getStringExtra(Constants.USERNAME));

        setContentView(R.layout.activity_group_page);
        new ConnectTask().execute("");
        try {
            Thread.sleep(1337);
            Map<String, Object> data = new HashMap<>();
            data.put(Constants.USERNAME_KEY, intent.getStringExtra(Constants.USERNAME));
            data.put(Constants.MESSAGE_TYPE_KEY, MessageType.LIST_GROUP);
            JSONObject json = new JSONObject(data);
            Log.e("JSON Dictionary", json.toString());
            mTcpClient.sendMessage(json.toString());
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

    protected void joinOnClick(View v) {
        Log.e(Constants.USERNAME, "floating action button got clicked!!");
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}
