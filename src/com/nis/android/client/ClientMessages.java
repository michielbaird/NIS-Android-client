package com.nis.android.client;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class ClientMessages {
	public static class DisplayMessage {
		private final Date timestamp;
		private final String sender;
		private final String message;
		public DisplayMessage(Date timeStamp, String sender, String message) {
			this.timestamp = timeStamp;
			this.sender = sender;
			this.message = message;
		}
		public String getMessages() {
			return message;
		}
		
		public Date getTimeStamp() {
			return timestamp;
		}
		
		public String getSender() {
			return sender;
		}
	}

	public static class MessageList implements Iterable<DisplayMessage> {
		private final ArrayList<DisplayMessage> messages;
		public MessageList() {
			messages = new ArrayList<ClientMessages.DisplayMessage>();
		}
		@Override
		public Iterator<DisplayMessage> iterator() {
			return messages.iterator();
		}
	
		public void addMessage(String sender, String message) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			dateFormat.format(date);
			messages.add(new DisplayMessage(date, sender, message));
			
		}
	}

	private HashMap<String, MessageList> messageLists;
	private String clienthandle;
	
	public ClientMessages(String clientHandle) {
		messageLists = new HashMap<String, ClientMessages.MessageList>();
		this.clienthandle = clientHandle;
	}

	public void addReceivedMessage(String sender, String message) {
		if (!messageLists.containsKey(sender)) {
			messageLists.put(sender, new MessageList());
		}
		messageLists.get(sender).addMessage(sender, message);
	}

	public void addSentMessage(String recipient, String message) {
		if (!messageLists.containsKey(recipient)) {
			messageLists.put(recipient, new MessageList());
		}
		messageLists.get(recipient).addMessage(clienthandle, message);
	}

	public MessageList getMessageList(String client) {
		if (!messageLists.containsKey(client)) {
			messageLists.put(client, new MessageList());
		}
		return messageLists.get(client);
	}
}
