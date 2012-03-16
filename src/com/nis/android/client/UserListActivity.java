package com.nis.android.client;

import java.util.ArrayList;
import java.util.Set;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.nis.android.client.ClientService.ClientActivityI;
import com.nis.client.ClientCallbacks.ConfirmResult;
import com.nis.shared.requests.SendFile;

public class UserListActivity extends ListActivity {

	
	ClientService service;
	private ClientActivityI callback;
	ArrayList<String> clientArrayList = new ArrayList<String>();
	ArrayAdapter<String> adapter;

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
			}

			@Override
			public ConfirmResult receiveFile(SendFile sendFile) {
				return confirmFileRecieve(sendFile);
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
			service.setUserListCallback(callback);
			mHandler.sendEmptyMessage(ClientService.UPDATE_USER_LIST);
			
		}
	};

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent startLogin = new Intent(UserListActivity.this, MessageViewActivity.class);
		startLogin.putExtra("handle", clientArrayList.get(position));
		
		startActivity(startLogin);
	}

	private Handler mHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ClientService.UPDATE_USER_LIST:
				updateUserList();
				break;
			case ClientService.MESSAGE_RECEIVED:
				messageRecieved();
				break;
			default:
				break;
			}
		};
	};
	
	protected ConfirmResult confirmFileRecieve(final SendFile sendFile) {
		final ConfirmResult conf =  new ConfirmResult();
		final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		            //Yes button clicked
		        	synchronized (conf) {
		        		conf.accept = true;
			        	conf.fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SecureFT/" + sendFile.filename;
			        	conf.notify();
					}
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		        	synchronized (conf) {
		        		conf.accept = false;
		        		conf.notify();
		        	}
		            //No button clicked
		            break;
		        }
		    }
		};
		mHandler.post( new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(UserListActivity.this);
				builder.setMessage("Would you like to receive a file?").setPositiveButton("Yes", dialogClickListener)
				    .setNegativeButton("No", dialogClickListener).show();
			}
		});
		synchronized (conf) {
			try {
				conf.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return conf;
	}
	

	private void updateUserList() {
		final Set<String> userList = service.getUserList();
		clientArrayList.clear();
		for (String handle: userList) {
			clientArrayList.add(handle);
		}
		adapter.notifyDataSetChanged();
	}
	
	private void messageRecieved() {
		
	}
}
