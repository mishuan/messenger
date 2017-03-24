package com.ece416.aruproy.messengerclient;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GroupListActivity extends AppCompatActivity {

    private TcpClient mTcpClient;
    private ListView mListView;
    private String[] groupsList;

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
            // TODO do something and populateList()
            //process server response here....
        }
    }

    private void populateList(){
        Log.e("GROUP_LIST", groupsList[0]);
        ListAdapter listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groupsList);
        mListView = (ListView) findViewById(R.id.groups_list);
        Log.e("GROUP_LIST", listAdapter.toString());
        Log.e("GROUP LIST", mListView.toString());
        mListView.setAdapter(listAdapter);
    }

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
            Log.e("Login JSON Dictionary", json.toString());
            mTcpClient.sendMessage(json.toString());
            groupsList = new String[]{"apple", "orange", "whatever", "idkm", "running out of things"};
            populateList();
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

    protected void joinOnClick(View v) {
        Log.e(Constants.USERNAME, "floating action button got clicked!!");
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(GroupListActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.new_group_alert_dialog_layout, null);
        mbuilder.setView(mView);
        final AlertDialog dialog = mbuilder.create();

        final EditText mGroupName = (EditText) mView.findViewById(R.id.group_name);
        Button mJoin = (Button) mView.findViewById(R.id.join_group);

        mJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(Constants.ACTIVITY_DEBUG_TAG, "Trying to JOIN group: " + mGroupName.getText().toString());
                dialog.hide();
            }
        });

        dialog.show();
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
