package com.nis.android.client;

import com.nis.android.client.ClientMessages.MessageList;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class MessageViewActivity extends Activity {
	ListView messageListView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_window);
		MessageList messages = new MessageList();
		messages.addMessage("test", "test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test test ");
		messages.addMessage("test", "test2");
		messageListView = (ListView)findViewById(R.id.messageView1);
		
		messageListView.setAdapter(new MessageListAdapter(this, messages));
		
	}
}
