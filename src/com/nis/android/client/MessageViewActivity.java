package com.nis.android.client;

import java.io.File;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.nis.android.client.ClientService.ClientActivityI;
import com.nis.client.ClientCallbacks.ConfirmResult;
import com.nis.shared.requests.SendFile;

public class MessageViewActivity extends Activity {
	private final int SEND_FILE = 1001;

	
	ListView messageListView;
	String activeHandle;
	ClientActivityI callback;
	HashMap<String, MessageListAdapter> userAdapters
		= new HashMap<String, MessageListAdapter>();

	ClientService service;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_window);
		
		callback =  new ClientActivityI() {
			@Override
			public void clientMessage(int message) {
				mHandler.sendEmptyMessage(message);
			}
			@Override
			public ConfirmResult receiveFile(SendFile sendFile) {
				return confirmFileRecieve(sendFile);
			}
		};
		Intent intent = getIntent();
		if (intent.hasExtra("handle")) {
			activeHandle = intent.getStringExtra("handle");
			TextView textView = (TextView)findViewById(R.id.textView1);
			textView.setText(activeHandle);
		}
		
		bindService(new Intent(this, ClientService.class), mConnection,
				Context.BIND_AUTO_CREATE);
		Button buttonSend = (Button)this.findViewById(R.id.buttonSend);
		buttonSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText editMessage =  (EditText) MessageViewActivity.this.
						findViewById(R.id.editMessage);
				String message = editMessage.getText().toString().trim();
				if (service != null && !message.equals("")) {
					service.sendMessage(activeHandle, message);
					editMessage.setText("");
					ClientMessages messages = service.getClientMessages();
					messages.addSentMessage(activeHandle, message);	
					userAdapters.get(activeHandle).notifyDataSetChanged();
				}
			}
		});
	}
	
	protected ConfirmResult confirmFileRecieve(final SendFile sendFile) {
		final ConfirmResult conf =  new ConfirmResult();
		final DialogInterface.OnClickListener dialogClickListener = 
				new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		            //Yes button clicked
		        	synchronized (conf) {
		        		conf.accept = true;
			        	conf.fileName = Environment
			        			.getExternalStorageDirectory()
			        			.getAbsolutePath() + "/SecureFT/" +
			        			sendFile.filename;
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
				AlertDialog.Builder builder = 
						new AlertDialog.Builder(MessageViewActivity.this);
				builder.setMessage("Would you like to receive a file?")
						.setPositiveButton("Yes", dialogClickListener)
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflator = getMenuInflater();
		inflator.inflate(R.menu.chat_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.send_picture:
			sendPicture();
			return true;
		case R.id.send_video:
			sendVideo();
			return true;
		case R.id.send_song:
			sendSong();
			return true;
		default:
			return super.onContextItemSelected(item);
		
		}
		
	}
	
	private void sendPicture() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, SEND_FILE);
		String filePath = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/SecureFT/";
		File path = new File(filePath);
		if (!path.exists()){
			path.mkdir();
		} 
	}
	
	private void sendVideo() {
		Intent viedoPickerIntent = new Intent(Intent.ACTION_PICK);
		viedoPickerIntent.setType("video/*");
		startActivityForResult(viedoPickerIntent, SEND_FILE);
		String filePath = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/SecureFT/";
		File path = new File(filePath);
		if (!path.exists()){
			path.mkdir();
		}
	}
	
	private void sendSong() {
		Intent viedoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
		viedoPickerIntent.setType("audio/*");
		startActivityForResult(viedoPickerIntent, SEND_FILE);
		String filePath = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/SecureFT/";
		File path = new File(filePath);
		if (!path.exists()){
			path.mkdir();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, 
			Intent intent) {
		// TODO Auto-generated method stub
		  super.onActivityResult(requestCode, resultCode, intent);
		  if (requestCode == SEND_FILE) {
			  if (intent != null) {
				  Cursor cursor = getContentResolver().query(intent.getData(),
						  null, null, null, null);
				  cursor.moveToFirst();  
				  int idx = cursor.getColumnIndex(ImageColumns.DATA);
				  String fileSrc = cursor.getString(idx);
				  Log.d("test", "File:" + fileSrc);
				  File file = new File(fileSrc);
				  if (file.exists() && file.isFile() && service != null){
					 service.sendFile(activeHandle, file);
				  }

			  }
			  else {
				  Log.d("test", "idButSelPic Photopicker canceled");
			  }
		  }
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			service.clearMessageCallback();
			service = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			service = ((ClientService.MyBinder) binder).getService();
			service.setMessageCallback(callback);
			messageListView = (ListView) MessageViewActivity.
					this.findViewById(R.id.messageView1);
			if (!userAdapters.containsKey(activeHandle)) {
				ClientMessages messages = service.getClientMessages();
				userAdapters.put(activeHandle,
						new MessageListAdapter(MessageViewActivity.this,
								messages.getMessageList(activeHandle), 
								activeHandle));
			}
			messageListView.setAdapter(userAdapters.get(activeHandle));
		}
	};
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ClientService.MESSAGE_RECEIVED:
					if (userAdapters.containsKey(activeHandle)){
						userAdapters.get(activeHandle).notifyDataSetChanged();
					}
					break;
				default:
					break;
			}
		};
	};

}
