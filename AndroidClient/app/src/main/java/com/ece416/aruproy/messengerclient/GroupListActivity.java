package com.ece416.aruproy.messengerclient;

import android.content.Intent;
import android.graphics.Color;
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
import android.widget.TextView;

public class GroupListActivity extends AppCompatActivity implements ListObserver, MessageObserver{

    private ListView mListView;
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

                // start message chat activity
                joinGroupStartActivity(selected);
            }
        });

        mLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("LIST ITEM CLICKED", "LEAVING: " + selected);
                dialog.hide();
                ConnectTask.getGroupList().remove(position);
                arrayAdapter.notifyDataSetChanged();

                // construct LEAVE_GROUP json to send to server
                ConnectTask.tcpGroupAction(MessageType.LEAVE_GROUP, selected);
            }
        });

        dialog.show();
    }

    private void populateList(){
        arrayAdapter = new GroupArrayAdapter(this, ConnectTask.getGroupList());
        mListView = (ListView) findViewById(R.id.groups_list);
        mListView.setAdapter(arrayAdapter);
        mListView.setOnItemClickListener(
            new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id){showJoinLeaveGroupDialogAlert(parent, position);
                }
            }
        );
    }

    @Override
    public void updateList() {
        Log.e("GroupListActivity", ConnectTask.getGroupList().toString());
        populateList();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConnectTask.setListObserver(this);
        setContentView(R.layout.activity_group_page);
        populateList();
    }
    protected void joinGroupStartActivity(String groupName) {
        Intent i = new Intent(GroupListActivity.this, ChatActivity.class);
        i.putExtra(Constants.USERNAME_KEY, ConnectTask.getUsername());
        i.putExtra(Constants.GROUP_NAME_KEY, groupName);
        GroupListActivity.this.startActivity(i);
    }

    protected void joinOnClick(View v) {
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(GroupListActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.new_group_alert_dialog_layout, null);
        mbuilder.setView(mView);
        final AlertDialog dialog = mbuilder.create();

        final EditText mGroupName = (EditText) mView.findViewById(R.id.join_group_name);
        Button mJoin = (Button) mView.findViewById(R.id.join_new_group);

        mJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = mGroupName.getText().toString();
                Log.e(Constants.ACTIVITY_DEBUG_TAG, "Trying to JOIN group: " + groupName);
                dialog.hide();
                ConnectTask.getGroupList().add(groupName);
                arrayAdapter.notifyDataSetChanged();

                // construct JOIN_GROUP json to send to server
                ConnectTask.tcpGroupAction(MessageType.JOIN_GROUP, groupName);

                // start message chat activity
                joinGroupStartActivity(groupName);
            }
        });

        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        ConnectTask.tcpLogin();
        ConnectTask.setContext(getApplicationContext());
        ConnectTask.setCurrGroup("");
        ConnectTask.setMessageObserver(this);
        ConnectTask.startTcpPing();
        update();
        if (arrayAdapter != null) arrayAdapter.notifyDataSetChanged();
    }

    private void update()
    {
        TextView tv = (TextView)findViewById(R.id.status);
        if (ConnectTask.isServerOnline()) {
            tv.setTextColor(Color.parseColor("#3F51B5"));
            tv.setText("Server Status: ONLINE");
        } else {
            tv.setTextColor(Color.parseColor("#FF4081"));
            tv.setText("Server Status: OFFLINE");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void updateMessage() {
        // TODO do something about new messages
        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void sendMessage() {
        ConnectTask.tcpSendMessage("","");
    }

    @Override
    public void updateStatus() {
        this.runOnUiThread(new Runnable()
        {
            public void run()
            {
                update();
            }
        });
    }
}
