package com.ece416.aruproy.messengerclient;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import java.util.List;

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
        update();
        setupChat();
    }

    protected void setupChat() {
        Intent intent = getIntent();
        TextView tv = (TextView)findViewById(R.id.group_joined);
        groupName = intent.getStringExtra(Constants.GROUP_NAME_KEY);

        // set group first
        ConnectTask.setCurrGroup(groupName);

        // then send message
        ConnectTask.tcpSendMessage("", groupName);
        tv.setText("Group: " + groupName);

        TextView tvMessageLog= (TextView) findViewById(R.id.message_log);
        tvMessageLog.setMovementMethod(new ScrollingMovementMethod());
    }

    protected void sendOnClick(View v) {
        AutoCompleteTextView messageTextView = (AutoCompleteTextView) findViewById(R.id.message);
        String message = messageTextView.getText().toString();

        if (message.equals("")) return;

        ConnectTask.tcpSendMessage(message, groupName);
        messageTextView.setText("");

        // signal this message is sending
        TextView tvMessageLog= (TextView) findViewById(R.id.message_log);
        String messageLog = tvMessageLog.getText().toString();
        messageLog += "SENDING >> " + ConnectTask.getUsername() + ":  " + message + "\n";
        tvMessageLog.setText(messageLog);
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
            String currMessage = m.get(0) + ":  " + m.get(3) + "\n";
            if (m.get(0).equals(ConnectTask.getUsername())) {
                messageLog = messageLog.replace("SENDING >> " + currMessage, "");
            }
            messageLog += currMessage;
        }

        Log.e("CURRENT CHAT", messageLog);
        tvMessageLog.setText(messageLog);
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
    public void sendMessage() {
        ConnectTask.tcpSendMessage("", groupName);
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
