package com.ece416.aruproy.messengerclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupListActivity extends AppCompatActivity implements Observer {

    private TcpClient mTcpClient;
    private ListView mListView;
    private List<String> groupsList;
    private boolean DEBUG = false;
    private ArrayAdapter<String> arrayAdapter;

    private void showJoinLeaveGroupDialogAlert(AdapterView<?> parent, final int position) {
        final String selected = String.valueOf(parent.getItemAtPosition(position));

        AlertDialog.Builder mbuilder = new AlertDialog.Builder(GroupListActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.join_leave_group_alert_dialog_layout, null);
        mbuilder.setView(mView);
        final AlertDialog dialog = mbuilder.create();

        Button mJoin = (Button) mView.findViewById(R.id.join_group);
        Button mLeave = (Button) mView.findViewById(R.id.leave_group);

        mJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("LIST ITEM CLICKED", "JOINING: " + selected);
                dialog.hide();
                //TODO: go to next activity
            }
        });

        mLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("LIST ITEM CLICKED", "LEAVING: " + selected);
                dialog.hide();
                groupsList.remove(position);
                arrayAdapter.notifyDataSetChanged();

                // construct LEAVE_GROUP json to send to server
                Map<String, Object> data = new HashMap<>();
                data.put(Constants.USERNAME_KEY, getIntent().getStringExtra(Constants.USERNAME));
                data.put(Constants.MESSAGE_TYPE_KEY, MessageType.LEAVE_GROUP.getValue());
                data.put(Constants.GROUP_NAME_KEY, selected);
                JSONObject json = new JSONObject(data);
                Log.e("LEAVE_GROUP Dictionary", json.toString());
                mTcpClient.sendMessage(json.toString());
            }
        });

        dialog.show();
    }

    private void populateList(){
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groupsList);
        mListView = (ListView) findViewById(R.id.groups_list);
        mListView.setAdapter(arrayAdapter);
        mListView.setOnItemClickListener(
            new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id){
                    showJoinLeaveGroupDialogAlert(parent, position);
                }
            }
        );
    }

    @Override
    public void update() {
        groupsList = ConnectTask.getGroupList();
        Log.e("GroupListActivity", groupsList.toString());
        populateList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConnectTask.setListObserver(this);
        ConnectTask.setContext(getApplicationContext());

        Intent intent = getIntent();
        Log.e(Constants.USERNAME, intent.getStringExtra(Constants.USERNAME));
        setContentView(R.layout.activity_group_page);
        mTcpClient = ConnectTask.getInstance().getTcpClient();

        // construct LOGIN json to send to server
        Map<String, Object> data = new HashMap<>();
        data.put(Constants.USERNAME_KEY, intent.getStringExtra(Constants.USERNAME));
        data.put(Constants.MESSAGE_TYPE_KEY, MessageType.LIST_GROUP.getValue());
        JSONObject json = new JSONObject(data);
        Log.e("Login JSON Dictionary", json.toString());
        mTcpClient.sendMessage(json.toString());

        if (DEBUG) {
            groupsList = new ArrayList<>(Arrays.asList("apple", "orange", "whatever", "idkm", "running out of things"));
        }
    }

    protected void joinOnClick(View v) {
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(GroupListActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.new_group_alert_dialog_layout, null);
        mbuilder.setView(mView);
        final AlertDialog dialog = mbuilder.create();

        final EditText mGroupName = (EditText) mView.findViewById(R.id.group_name);
        Button mJoin = (Button) mView.findViewById(R.id.join_new_group);

        mJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = mGroupName.getText().toString();
                Log.e(Constants.ACTIVITY_DEBUG_TAG, "Trying to JOIN group: " + groupName);
                dialog.hide();
                groupsList.add(groupName);
                arrayAdapter.notifyDataSetChanged();

                // construct JOIN_GROUP json to send to server
                Map<String, Object> data = new HashMap<>();
                data.put(Constants.USERNAME_KEY, getIntent().getStringExtra(Constants.USERNAME));
                data.put(Constants.MESSAGE_TYPE_KEY, MessageType.JOIN_GROUP.getValue());
                data.put(Constants.GROUP_NAME_KEY, groupName);
                JSONObject json = new JSONObject(data);
                Log.e("JOIN_GROUP Dictionary", json.toString());
                mTcpClient.sendMessage(json.toString());

                //TODO: go to next activity
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
