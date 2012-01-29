package de.crashsource.sms2air.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import de.crashsource.sms2air.R;
import de.crashsource.sms2air.adapter.ListPhoneNumbersAdapter;
import de.crashsource.sms2air.common.RecipientList;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public class ActivityListPhoneNumbers extends Activity {
	public static final String TAG = ActivityListPhoneNumbers.class.getSimpleName();

	private ListView mList;
	private RecipientList mRecipientList;
	private ListPhoneNumbersAdapter mAdapter;
	private TextView mHeadline;
	private TextView mDescription;

	private OnItemClickListener mListItemOnClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int index,
				long arg3) {
			RecipientList returnRl = new RecipientList();
			returnRl.add(mRecipientList.get(index));

			Log.v(TAG, "OnItemClick: chosen recipient list: "
					+ returnRl.toString());

			Intent i = new Intent();
			i.putExtra("chosen_recipient_list", RecipientList
					.serialize(returnRl));
			setResult(Activity.RESULT_OK, i);
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_list_phone_numbers);

		mList = (ListView) findViewById(R.id.list_phone_numbers_list);
		mHeadline = (TextView) findViewById(R.id.list_phone_numbers_headline);
		mDescription = (TextView) findViewById(R.id.list_phone_numbers_description);

		Intent intent;
		Bundle b;
		if ((intent = getIntent()) != null
				&& ((b = intent.getBundleExtra("extras")) != null)) {
			mRecipientList = RecipientList.deserialize(b
					.getString("serialized_recipientlist"));

			if (mRecipientList.size() == 0)
				throw new IllegalArgumentException(
						"The given serialized recipient list must contain at least one recipient.");
			Log.v(TAG, mRecipientList.toString());

			mAdapter = new ListPhoneNumbersAdapter(this,
					R.layout.item_overview_after_sending, mRecipientList);
			mList.setAdapter(mAdapter);

			mList.setOnItemClickListener(mListItemOnClickListener);

			String name = mRecipientList.get(0).getName();
			mHeadline.setText(name); // the name is always the same - no matter which recipient
			mDescription.setText(Html.fromHtml(getString(R.string.list_numbers_description, "<b>" + name + "</b>")));
		}
	}
}
