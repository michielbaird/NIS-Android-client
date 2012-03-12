package com.nis.android.client;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AndroidClientActivity extends Activity {
	private EditText handleEdit;
	private Button loginButton;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

    	handleEdit = (EditText)findViewById(R.id.editHandle);
    	loginButton = (Button)findViewById(R.id.connectButton);
    	
        
        loginButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				String handle =  handleEdit.getText().toString();
				Intent serviceIntent = new Intent(AndroidClientActivity.this, ClientService.class);
				serviceIntent.putExtra("handle", handle);
				startService(serviceIntent);
				
				Intent startLogin = new Intent(AndroidClientActivity.this, UserListActivity.class);
				startLogin.putExtra("handle", handle);
				
				startActivity(startLogin);
			}
		});

    }

}