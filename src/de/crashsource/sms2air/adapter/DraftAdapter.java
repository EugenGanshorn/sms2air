package de.crashsource.sms2air.adapter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import de.crashsource.sms2air.R;
import de.crashsource.sms2air.common.Recipient;
import de.crashsource.sms2air.common.RecipientList;
import de.crashsource.sms2air.db.SmsDatabase;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public class DraftAdapter extends CursorAdapter {
	public static final String TAG = CursorAdapter.class.getSimpleName();
	
	private static final SimpleDateFormat dayMonthFormatter = new SimpleDateFormat("MMM d", Locale.ENGLISH);

	public DraftAdapter(Context context, Cursor c) {
		super(context, c);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// Draft ID
		int draftId = cursor.getInt(cursor.getColumnIndex(SmsDatabase.ID));
		Log.v(TAG, "bindView(): Draft-ID: " + Integer.toString(draftId));
		
		// Receiver
		TextView receiver = (TextView) view
				.findViewById(R.id.main_draft_item_receiver);
		String serializedRecipients = cursor.getString(cursor.getColumnIndex(SmsDatabase.RECIPIENTS));
		Log.v(TAG, "bindView: serialized recipients: " + serializedRecipients);
		
		RecipientList receiverList = RecipientList.deserializeFromDb(context, serializedRecipients);
		
		if (receiverList.size() > 0) { // there is at least one receiver
			Recipient r = receiverList.get(0); // get the first entry
			
			String receiverText = "";
			if (r.getLookupKey() == null) { // default value
				receiverText = r.getNumber();
			} else { // receiver is in address book
				receiverText = r.getName();
			}
			int count = 0;
			if ((count = receiverList.size()) > 1) {
				receiverText += " " + context.getString(R.string.main_hint_some_more, count-1);
			}
			receiver.setText(receiverText);
		} else {
			receiver.setText("<" + context.getString(R.string.main_no_recipient) + ">");
		}
		
		
		// Type
		TextView type = (TextView) view.findViewById(R.id.main_draft_item_type);
		String typeText = "";
		switch (Integer.parseInt(cursor.getString(cursor.getColumnIndex(SmsDatabase.TYPE)))) {
		case SmsDatabase.TYPE_RECEIVED: // received
			typeText = context.getResources().getString(R.string.type_received);
			break;
		case SmsDatabase.TYPE_SENT: // sent
			typeText = context.getResources().getString(R.string.type_sent);
			break;
		case SmsDatabase.TYPE_DRAFT: // draft
			typeText = context.getResources().getString(R.string.type_draft);
			break;
		}
		type.setText(typeText);

		// Summary
		TextView summary = (TextView) view
				.findViewById(R.id.main_draft_item_summary);
		summary.setText(cursor.getString(cursor.getColumnIndex(SmsDatabase.BODY_TEXT)));

		// date
		TextView date = (TextView) view.findViewById(R.id.main_draft_item_date);
		long time = cursor.getLong(cursor.getColumnIndex(SmsDatabase.DATE));
		Timestamp timestamp = new Timestamp(time);
		Log.v(TAG, "DraftCursorAdapter - bindView(): timestamp: " + timestamp);
		date.setText(dayMonthFormatter.format(timestamp));
		

		// use database id to identify the draft
		Bundle b = new Bundle();
		b.putInt("draft_id", Integer.parseInt(cursor.getString(cursor.getColumnIndex(SmsDatabase.ID))));
		b.putString("serialized_recipient_list", RecipientList.serialize(receiverList));
		b.putString("draft_message_body", summary.getText().toString());
		view.setTag(b);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.item_draft, parent, false);
		bindView(v, context, cursor);
		return v;
	}

}
