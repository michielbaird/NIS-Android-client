package com.nis.android.client;

import java.util.ArrayList;
import java.util.Set;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.nis.android.client.ClientService.ClientActivityI;
import com.nis.android.client.ClientService.MyBinder;
import com.nis.client.Client;
import com.nis.client.ClientCallbacks;

public class UserListActivity extends ListActivity {

	//private static final int clientPort = 8082;
	//private static final String serverAddress = "192.168.0.5";
	//private static final int serverPort = 8081;
	
	ClientService service;
	private ClientActivityI callback;
	ArrayList<String> clientArrayList = new ArrayList<String>();
	ArrayAdapter<String> adapter;
	//private String clientHandle;
	

	private ProgressDialog dialog;
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_list_screen);
		callback =  new ClientActivityI() {
			@Override
			public void clientMessage(int message) {
				mHandler.sendEmptyMessage(message);
			};
		};

		adapter = new ArrayAdapter<String>(this,R.layout.list_item,clientArrayList);
		setListAdapter(adapter);
		bindService(new Intent(this, ClientService.class), mConnection, Context.BIND_AUTO_CREATE);
		
		listView = getListView();
		
	}

	public void showServiceData(View view) {
		if (service != null) {
			
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			service = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			service = ((ClientService.MyBinder) binder).getService();
			service.setDisplayCallback(callback);
			mHandler.sendEmptyMessage(ClientService.UPDATE_USER_LIST);
			
		}
	};

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
	}

	private Handler mHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ClientService.UPDATE_USER_LIST:
				updateUserList();
				break;

			default:
				break;
			}
		};
	};

	private void updateUserList() {
		final Set<String> userList = service.getUserList();
		clientArrayList.clear();
		for (String handle: userList) {
			clientArrayList.add(handle);
		}
		adapter.notifyDataSetChanged();
	}
}
