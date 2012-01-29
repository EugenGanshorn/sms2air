package de.crashsource.sms2air.gui;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
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
public class ActivityHistorySingle extends Activity {
	public static final String TAG = ActivityHistorySingle.class
			.getSimpleName();

	private static final SimpleDateFormat dayMonthFormatter = new SimpleDateFormat(
			"EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

	protected SmsDatabase mDb;

	protected int mDbId;

	protected TextView mDate;
	protected TextView mRecipients;
	protected TextView mBodyText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_history_single);

		mDate = (TextView) findViewById(R.id.history_single_date);
		mRecipients = (TextView) findViewById(R.id.history_single_recipients);
		mBodyText = (TextView) findViewById(R.id.history_single_body_text);

		Bundle b = getIntent().getBundleExtra("extras");
		if (b != null) {
			mDbId = b.getInt("id");
			mDb = new SmsDatabase(this);
			try {
				String[] selection = new String[] { SmsDatabase.RECIPIENTS,
						SmsDatabase.DATE, SmsDatabase.BODY_TEXT };
				Cursor c = mDb.getReadable(selection, mDbId);

				try {
					c.moveToFirst();

					// recipients
					RecipientList recipientList = RecipientList
							.deserializeFromDb(this, c.getString(0));
					mRecipients.setText(formateRecipientList(recipientList));

					// date
					long time = c.getLong(1);
					Timestamp timestamp = new Timestamp(time);
					mDate.setText(dayMonthFormatter.format(timestamp));

					// body text
					Log.v(TAG, c.getString(2));
					String bodyText = c.getString(2);
					mBodyText.setText(bodyText);

				} finally {
					c.close();
				}

			} finally {
				mDb.close();
			}
		}
	}
	
	private String formateRecipientList(RecipientList rl) {
		String ret = "";
		for (int i=0; i<rl.size(); i++) {
			if (i != 0) ret += "\n";
			
			Recipient r = rl.get(i);
			Person p = AndroidBase.getPersonByLookupKey(this, r.getLookupKey());
			if (p == null) {
				Recipient rTemp = AndroidBase.getRecipientByPhoneNumber(this, r.getNumber());
				if (rTemp == null) {
					ret += r.getNumber();
				} else {
					ret += rTemp.getName() + " <" + r.getNumber() + ">";
				}
			} else {
				ret += p.getName() + " <" + r.getNumber() + ">";
			}
		}
		return ret;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.option_menu_history_single, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opt_delete_history_single:
			try {
				mDb = new SmsDatabase(this);
				mDb.delete(mDbId);

				Toast.makeText(this, R.string.history_single_record_deleted,
						Toast.LENGTH_SHORT).show();

				finish();
			} finally {
				mDb.close();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
