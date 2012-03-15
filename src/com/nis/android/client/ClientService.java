package com.nis.android.client;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Set;

import com.nis.client.Client;
import com.nis.client.ClientCallbacks;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ClientService extends Service {
	
	public interface ClientActivityI {
		public void clientMessage(int message);
	}
	
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
