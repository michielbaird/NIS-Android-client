package com.nis.android.client;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nis.android.client.ClientMessages.DisplayMessage;
import com.nis.android.client.ClientMessages.MessageList;

public class MessageListAdapter extends ArrayAdapter<DisplayMessage> {
	private final MessageList messageList;
	private Activity context;
	
	static class MessageTag {
		TextView messageView;
		TextView timeView;
	}
	
	public MessageListAdapter(Activity context, MessageList messageList) {
		super(context, R.layout.client_message, messageList.getRawList());
		this.messageList = messageList;
		this.context = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.client_message, null);
			MessageTag tag = new MessageTag();
			tag.messageView = (TextView)rowView.findViewById(R.id.messageView1);
			tag.timeView = (TextView)rowView.findViewById(R.id.timeView1);
			rowView.setTag(tag);
		}
		
		MessageTag tag = (MessageTag)rowView.getTag();
		DisplayMessage message = messageList.getMessage(position);
		tag.messageView.setText(message.getMessages());
		return rowView;
	}
}
