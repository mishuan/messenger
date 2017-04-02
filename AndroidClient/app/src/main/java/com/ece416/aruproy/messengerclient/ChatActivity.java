package com.ece416.aruproy.messengerclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity implements MessageObserver {

    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
    }

    @Override
    public void onResume() {
        super.onResume();
        ConnectTask.setContext(getApplicationContext());
        ConnectTask.setMessageObserver(this);
        setupChat();
    }

    protected void setupChat() {
        Intent intent = getIntent();
        TextView tv = (TextView)findViewById(R.id.group_joined);
        groupName = intent.getStringExtra(Constants.GROUP_NAME_KEY);
        ConnectTask.tcpSendMessage("", groupName);
        tv.setText("Group: " + groupName);

        TextView tvMessageLog= (TextView) findViewById(R.id.message_log);
        tvMessageLog.setMovementMethod(new ScrollingMovementMethod());
    }

    protected void sendOnClick(View v) {
        AutoCompleteTextView messageTextView = (AutoCompleteTextView) findViewById(R.id.message);
        String message = messageTextView.getText().toString();
        ConnectTask.tcpSendMessage(message, groupName);
        messageTextView.setText("");
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
        TextView tv = (TextView)findViewById(R.id.group_members);
        tv.setText("Members: " + ConnectTask.getMembersList().toString());
        List<List<String>> messages = ConnectTask.getMessagesForGroup(groupName);

        if (messages == null) return;
        TextView tvMessageLog= (TextView) findViewById(R.id.message_log);
        String messageLog = tvMessageLog.getText().toString();
        for (List<String> m : messages) {
            messageLog += m.get(0) + ":  " + m.get(3) + "\n";
        }

        Log.e("CURRENT CHAT", messageLog);
        tvMessageLog.setText(messageLog);
    }


    @Override
    public void sendMessage() {
        ConnectTask.tcpSendMessage("",groupName);
    }
}
