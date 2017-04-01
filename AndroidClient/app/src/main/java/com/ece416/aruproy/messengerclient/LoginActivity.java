package com.ece416.aruproy.messengerclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;

/**
 * Created by ilikecalculus on 2017-03-22.
 */

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    protected void loginOnClick(View v) {
        Intent i = new Intent(LoginActivity.this, GroupListActivity.class);
        AutoCompleteTextView source = (AutoCompleteTextView) findViewById(R.id.username);
        AutoCompleteTextView ipAddress = (AutoCompleteTextView) findViewById(R.id.ip_address);
        AutoCompleteTextView portNumber = (AutoCompleteTextView) findViewById(R.id.port_number);
        i.putExtra(Constants.USERNAME_KEY, source.getText().toString());

        if (ConnectTask.getInstance().getIp() == null) {
            ConnectTask.setIpAndPort(ipAddress.getText().toString(), portNumber.getText().toString());
        }

        try {
            Thread.sleep(500);
        } catch (Exception e) {
            Log.e("LOGIN ACTIVITY THREAD", "Thread didn't actually sleep???");
        }

        LoginActivity.this.startActivity(i);
    }
}
