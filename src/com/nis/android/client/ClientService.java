package com.nis.android.client;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Set;

import com.nis.client.Client;
import com.nis.client.ClientCallbacks;

import android.R.string;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ClientService extends Service {
	
	public interface ClientActivityI {
		public void clientMessage(int message);
	}
	
	public static final int UPDATE_USER_LIST = 1;

	private static final int clientPort = 8082;
	private static final String serverAddress = "192.168.0.5";
	private static final int serverPort = 8081;

	private Intent intent;
	private Set<String> userList = null;
	private ClientCallbacks callbacks;
	private ClientActivityI displayCallback = null;
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
		intent = new Intent(this, ClientService.class);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		clientHandle = intent.getStringExtra("handle");
		callbacks =  new ClientCallbacks() {
			@Override
			public void onClientListReceived(Set<String> clientList) {
				if (displayCallback != null) {
					displayCallback.clientMessage(UPDATE_USER_LIST);
				}
				userList = clientList;
			}
		};
		client =  new Client(clientHandle,getLocalIpAddress(), clientPort,
				serverAddress, serverPort, callbacks);
		
		
		return super.onStartCommand(intent, flags, startId);
	}

	public void setDisplayCallback(ClientActivityI callback){
		displayCallback = callback;
	}

	public void clearDisplayCallback() {
		displayCallback = null;
	}

	public boolean hasUserList() {
		return userList != null;
	}

	public Set<String> getUserList() {
		return userList;
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
