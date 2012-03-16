package com.nis.android.client;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Set;

import org.w3c.dom.Notation;

import com.nis.client.Client;
import com.nis.client.ClientCallbacks;
import com.nis.client.ClientCallbacks.ConfirmResult;
import com.nis.shared.requests.SendFile;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class ClientService extends Service {
	
	public interface ClientActivityI {
		public void clientMessage(int message);
		ConfirmResult receiveFile(SendFile sendfile);
	}
	
	public final Handler handler =  new Handler();
	public static final int UPDATE_USER_LIST = 1;
	public static final int MESSAGE_RECEIVED = 2;

	private static final int clientPort = 8082;
	private static final String serverAddress = "137.158.60.219";
	private static final int serverPort = 8081;

	private Set<String> userList = null;
	private ClientCallbacks clientCallbacks;
	private ClientMessages messages;
	private ClientActivityI userListCallback = null;
	private ClientActivityI messageCallbacks = null;
	private Client client;
	private String clientHandle;
	private final IBinder mBinder = new MyBinder();

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	public class MyBinder extends Binder {
		ClientService getService() {
			return ClientService.this;
		}
		
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		clientHandle = intent.getStringExtra("handle");
		messages = new ClientMessages(clientHandle); 
		clientCallbacks =  new ClientCallbacks() {
			@Override
			public void onClientListReceived(Set<String> clientList) {
				userList = clientList;
				if (userListCallback != null) {
					userListCallback.clientMessage(UPDATE_USER_LIST);
				}
			}

			@Override
			public void onClientMessageRecieved(String handle, String message) {
				messages.addReceivedMessage(handle, message);
				if (userListCallback != null) {
					userListCallback.clientMessage(MESSAGE_RECEIVED);
				} 
				if (messageCallbacks != null) {
					messageCallbacks.clientMessage(MESSAGE_RECEIVED);
				}
			}

			@Override
			public void onFileReceived(String filename) {
				NotificationManager notificationManager 
					= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				Notification fileReceived = new Notification();
				fileReceived.tickerText = "File: " + filename + "received.";
				fileReceived.when = System.currentTimeMillis();
				Intent notificationIntent = new Intent(ClientService.this,
					    ClientService.class);
				PendingIntent contentIntent = PendingIntent.getActivity(ClientService.this, 0,
					    notificationIntent, 0);
				fileReceived.contentIntent = contentIntent;
				notificationManager.notify(1, fileReceived);
				
			}

			@Override
			public ConfirmResult onIncomingFile(SendFile sendFile)  {
				final ConfirmResult conf = new ConfirmResult();
				conf.fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SecureFT/test2.mp3";
				conf.accept = false;
				final String filename = sendFile.filename;
				if (filename.contains("/")) {
					conf.accept = false;
					return conf;
				}
				if (messageCallbacks != null) {
					return messageCallbacks.receiveFile(sendFile);
				} else if (userListCallback != null) {
					return userListCallback.receiveFile(sendFile);
				}
				return conf;
			}
		};
		client =  new Client(clientHandle,getLocalIpAddress(), clientPort,
				serverAddress, serverPort, clientCallbacks);
		
		
		return super.onStartCommand(intent, flags, startId);
	}

	public void setUserListCallback(ClientActivityI callback){
		userListCallback = callback;
	}
	
	public void setMessageCallback(ClientActivityI callback){
		messageCallbacks = callback;
	}

	public void clearUserListCallback() {
		userListCallback = null;
	}
	
	public void clearMessageCallback() {
		messageCallbacks = null;
	}

	public boolean hasUserList() {
		return userList != null;
	}

	public Set<String> getUserList() {
		return userList;
	}
	
	public ClientMessages getClientMessages() {
		return messages;
	}
	
	public void sendMessage(String handle, String message) {
		client.sendMessage(handle, message);
	}
	
	public void sendFile(final String handle, final File file) {
		new Thread() {
			public void run() {
				client.sendFileToClient(handle, file.getAbsolutePath());
			};
		}.start();
	}
	
    // gets the ip address of your phone's network
    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) { return inetAddress.getHostAddress().toString(); }
                }
           }
        } catch (SocketException ex) {
            Log.e("ServerActivity", ex.toString());
        }
        return null;
    }
}
