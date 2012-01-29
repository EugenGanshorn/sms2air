package de.crashsource.sms2air.gui;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import de.crashsource.sms2air.R;
import de.crashsource.sms2air.adapter.SendingPrefAdapter;
import de.crashsource.sms2air.common.AndroidBase;
import de.crashsource.sms2air.common.Recipient;
import de.crashsource.sms2air.common.RecipientList;
import de.crashsource.sms2air.common.SenderAddressFilter;
import de.crashsource.sms2air.db.Database;
import de.crashsource.sms2air.db.SmsDatabase;
import de.crashsource.sms2air.smsgateway.SmsGateway;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public class ActivitySendingPreferences extends Activity {

	public static final String TAG = ActivitySendingPreferences.class
			.getSimpleName();

	public static final int INTENT_OVERVIEW_ID = 1005;
	public static final String NO_DELAY = "no-delay";
	public static final String NO_SENDER_ADRESS = "no-sender";

	private String mBodyText;
	private RecipientList mRecipientList;
	private boolean mIsUnicodeMessage = false;

	private ListView mPrefList;
	private SendingPrefAdapter mAdapter;
	private List<String> mPreferences = new ArrayList<String>();

	private SmsGateway mSms;

	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int index,
				long arg3) {
			Log.v(TAG, "onItemClick() clicked: item index: " + index);
			switch (index) {
			case SendingPrefAdapter.ROUTE_INDEX:
				displayRouteDialog();
				break;
			case SendingPrefAdapter.SENDER_INDEX:
				if (mSms.isCustomSenderAddressAllowedForRoute(mPreferences
						.get(SendingPrefAdapter.ROUTE_INDEX))) {
					displaySenderDialog();
				} else {
					return;
				}
				break;
			case SendingPrefAdapter.SEND_DELAYED_INDEX:
				displayDelayDialog();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_sending_preferences);

		mSms = new SmsGateway(Preferences.getPreference((ContextWrapper) this,
				Preferences.GATEWAY_KEY));

		mPrefList = (ListView) findViewById(R.id.send_pref_list);

		mPreferences.add(SendingPrefAdapter.ROUTE_INDEX, Preferences
				.getPreference(this, Preferences.PREFERRED_ROUTE));
		String preferredNumber = Preferences.getPreference(this,
				Preferences.PREFERRED_SENDER);
		mPreferences.add(SendingPrefAdapter.SENDER_INDEX, preferredNumber);
		mPreferences.add(SendingPrefAdapter.SEND_DELAYED_INDEX, NO_DELAY);

		mAdapter = new SendingPrefAdapter(this, mPreferences);
		mPrefList.setAdapter(mAdapter);
		mPrefList.setOnItemClickListener(mOnItemClickListener);

		Intent intent;
		if ((intent = getIntent()) != null
				&& (intent.getBundleExtra("extras") != null)) {
			Bundle extras = intent.getBundleExtra("extras");
			mBodyText = extras.getString("body_text");
			mIsUnicodeMessage = extras.getBoolean("is_unicode_text");
			mRecipientList = RecipientList.deserialize(extras
					.getString("serialized_recipient_list"));
		}

	}

	public void onClickSend(View v) {
		Log.v(TAG, "onClickSend() called");

		final SmsGateway s = new SmsGateway(Preferences.getPreference(this,
				Preferences.GATEWAY_KEY));

		final String route = mPreferences.get(SendingPrefAdapter.ROUTE_INDEX);

		if (!AndroidBase.haveNetworkAccess(this)) {
			Toast.makeText(v.getContext(),
					R.string.send_pref_error_no_network_access,
					Toast.LENGTH_LONG).show();
			return;
		}
		if (mIsUnicodeMessage && !s.routeAllowsUnicodeText(route)) {
			Toast.makeText(v.getContext(),
					R.string.send_pref_error_route_does_not_allow_unicode_text,
					Toast.LENGTH_LONG).show();
			return;
		}

		s.setDebugMode(Preferences.isDebugModeOn(this));
		s.setDeliveryReport(Preferences.isDeliveryReportRequested(this));

		// show progress dialog to show the user that we're currently working
		final ProgressDialog progress = ProgressDialog.show(this,
				getResources().getString(
						R.string.send_pref_progress_sending_title),
				getResources().getString(
						R.string.send_pref_progress_sending_body), true, false);

		new Thread() {
			@Override
			public void run() {
				// String route =
				// mPreferences.get(SendingPrefAdapter.ROUTE_INDEX);

				String senderAddress = mPreferences
						.get(SendingPrefAdapter.SENDER_INDEX);
				if (senderAddress
						.equals(ActivitySendingPreferences.NO_SENDER_ADRESS)) {
					senderAddress = ""; // remove sender address
				}

				String timestamp = mPreferences
						.get(SendingPrefAdapter.SEND_DELAYED_INDEX);
				if (timestamp.equals(ActivitySendingPreferences.NO_DELAY)) {
					timestamp = "0"; // send SMS instantaneous
				}

				for (int i = 0; i < mRecipientList.size(); i++) {
					Recipient r = mRecipientList.get(i);
					try {
						// Notice: We need UNIX timestamp here!
						Long t = Long.parseLong(timestamp) / 1000;
						addSendReturnCode(i, s.send(route, r.getNumber(),
								senderAddress, mBodyText, t));

					} catch (Exception e) {
						addSendReturnCode(i,
								SmsGateway.RETURN_CODE_INTERNAL_ERROR);
					}
				}
				progress.dismiss();
				saveToHistory();
				informUserAfterSending();
			};
		}.start();

	}

	private void addSendReturnCode(int index, int returnCode) {
		mRecipientList.get(index).setReturnCode(returnCode);
	}

	private void saveToHistory() {
		// Do only list those recipients that have been valid
		RecipientList rl = new RecipientList();
		for (int i = 0; i < mRecipientList.size(); i++) {
			Recipient r = mRecipientList.get(i);
			if (r.getReturnCode() == SmsGateway.RETURN_CODE_OK) {
				rl.add(r);
			}
		}
		if (rl.size() == 0)
			return;

		Database db = new Database(this);
		ContentValues cv = new ContentValues();
		cv.put(SmsDatabase.BODY_TEXT, mBodyText);
		cv.put(SmsDatabase.DATE, System.currentTimeMillis());
		cv.put(SmsDatabase.RECIPIENTS, RecipientList.serializeForDb(rl));
		cv.put(SmsDatabase.TYPE, SmsDatabase.TYPE_SENT);
		try {
			db.getWritableDatabase().insert(SmsDatabase.TABLE_NAME, null, cv);
		} finally {
			db.close();
		}

	}

	private void informUserAfterSending() {
		Intent i = new Intent(this, ActivityOverviewAfterSending.class);
		Bundle b = new Bundle();
		b.putString("serialized_recipient_list", RecipientList
				.serialize(mRecipientList));
		i.putExtra("extras", b);
		startActivityForResult(i, INTENT_OVERVIEW_ID);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == INTENT_OVERVIEW_ID
				&& resultCode == Activity.RESULT_OK) {
			setResult(Activity.RESULT_OK);
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void displayRouteDialog() {
		final String[] routes = getResources().getStringArray(
				R.array.routes_values);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.send_pref_route_headline);
		builder.setItems(routes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int index) {
				Log.v(TAG, "displayRouteDialog(), onClick(): chosen entry: "
						+ routes[index]);
				mPreferences.set(SendingPrefAdapter.ROUTE_INDEX, routes[index]);
				mAdapter.notifyDataSetChanged();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void displaySenderDialog() {
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_set_sender_address,
				(ViewGroup) findViewById(R.id.set_sender_address_root));

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final EditText senderAddress = (EditText) layout
				.findViewById(R.id.set_sender_address);

		if (!mPreferences.get(SendingPrefAdapter.SENDER_INDEX).equals(
				ActivitySendingPreferences.NO_SENDER_ADRESS)) {
			senderAddress.setText(mPreferences
					.get(SendingPrefAdapter.SENDER_INDEX));
		}

		InputFilter[] FilterArray = new InputFilter[1];
		FilterArray[0] = new SenderAddressFilter();
		senderAddress.setFilters(FilterArray);

		builder.setView(layout);
		builder.setTitle(R.string.set_sender_address_title);
		builder.setPositiveButton(R.string.set_sender_address_confirm,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Log.v(TAG, "Add recipient dialog confirmed");
						String address = senderAddress.getText().toString();
						if (address.equals("")) {
							address = ActivitySendingPreferences.NO_SENDER_ADRESS;
						}
						mPreferences.set(SendingPrefAdapter.SENDER_INDEX,
								address);
						mAdapter.notifyDataSetChanged();
					}
				}).setNegativeButton(R.string.set_sender_address_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		builder.create().show();
	}

	private void displayDelayDialog() {
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_set_delay,
				(ViewGroup) findViewById(R.id.set_delay_root));

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final DatePicker datePicker = (DatePicker) layout
				.findViewById(R.id.set_delay_date);
		final TimePicker timePicker = (TimePicker) layout
				.findViewById(R.id.set_delay_time);
		final Context context = this.getApplicationContext();

		timePicker.setIs24HourView(true);

		builder.setView(layout);
		builder.setTitle(R.string.set_sender_address_title);
		builder.setPositiveButton(R.string.set_sender_address_confirm,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Log.v(TAG, "displayDelayDialog(): dialog accepted");
						int year = datePicker.getYear();
						int month = datePicker.getMonth() + 1;
						int day = datePicker.getDayOfMonth();

						int hour = timePicker.getCurrentHour();
						int minute = timePicker.getCurrentMinute();

						Log.v(TAG, "displayDelayDialog(): year: " + year);
						Log.v(TAG, "displayDelayDialog(): month: " + month);
						Log.v(TAG, "displayDelayDialog(): day: " + day);
						Log.v(TAG, "displayDelayDialog(): hour: " + hour);
						Log.v(TAG, "displayDelayDialog(): minute: " + minute);

						Timestamp t = new Timestamp(year - 1900, month - 1,
								day, hour, minute, 0, 0);
						long currentTimestamp = System.currentTimeMillis();

						Log.v(TAG, "displayDelayDialog(): chosen timestamp: "
								+ t.getTime());
						Log.v(TAG, "displayDelayDialog(): current timestamp: "
								+ currentTimestamp);

						if (t.getTime() < currentTimestamp) {
							Toast.makeText(context,
									R.string.set_delay_error_time_in_past,
									Toast.LENGTH_LONG).show();
							mPreferences.set(
									SendingPrefAdapter.SEND_DELAYED_INDEX,
									ActivitySendingPreferences.NO_DELAY);
						} else {
							mPreferences.set(
									SendingPrefAdapter.SEND_DELAYED_INDEX, Long
											.toString(t.getTime()));
						}
						mAdapter.notifyDataSetChanged();
					}
				}).setNegativeButton(R.string.set_sender_address_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		builder.create().show();
	}
}
