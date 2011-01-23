package de.crashsource.sms2air.adapter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import de.crashsource.sms2air.R;
import de.crashsource.sms2air.common.AndroidBase;
import de.crashsource.sms2air.common.Person;
import de.crashsource.sms2air.common.Recipient;
import de.crashsource.sms2air.common.RecipientList;
import de.crashsource.sms2air.db.SmsDatabase;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public class HistoryAdapter extends CursorAdapter {
	public final static String TAG = HistoryAdapter.class.getSimpleName();
	
	private static final SimpleDateFormat dayMonthFormatter = new SimpleDateFormat("MMM d", Locale.ENGLISH);
	
	public HistoryAdapter(Context context, Cursor c) {
		super(context, c);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView tvRecipients = (TextView) view.findViewById(R.id.history_item_recipients);
		TextView tvDate = (TextView) view.findViewById(R.id.history_item_date);
		TextView tvText = (TextView) view.findViewById(R.id.history_item_text);
		
		// recipient list
		String recipients = cursor.getString(cursor.getColumnIndexOrThrow(SmsDatabase.RECIPIENTS));
		RecipientList recipientList = RecipientList.deserializeFromDb(context, recipients);
		
		Recipient r = recipientList.get(0);
		Person p = AndroidBase.getPersonByLookupKey(context, r.getLookupKey());
		String recipientText = "";
		if (p != null) {
			recipientText = p.getName();
		} else {
			recipientText = r.getNumber();
		}
		if (recipientList.size() > 1) {
			recipientText += " " + context.getResources().getString(R.string.main_hint_some_more, new Object[] {recipientList.size()-1});
		}
		tvRecipients.setText(recipientText);
		
		// body text
		String bodyText = cursor.getString(cursor.getColumnIndexOrThrow(SmsDatabase.BODY_TEXT));
		tvText.setText(bodyText);
		
		// date
		long time = cursor.getLong(cursor.getColumnIndexOrThrow(SmsDatabase.DATE));
		Timestamp timestamp = new Timestamp(time);
		tvDate.setText(dayMonthFormatter.format(timestamp));
		
		// use database ID to identify the list item
		Bundle b = new Bundle();
		b.putInt("id", Integer.parseInt(cursor.getString(cursor.getColumnIndex(SmsDatabase.ID))));
		view.setTag(b);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.item_history, parent, false);
		bindView(v, context, cursor);
		return v;
	}

}
