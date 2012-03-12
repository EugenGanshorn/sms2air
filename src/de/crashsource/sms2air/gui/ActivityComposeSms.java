package de.crashsource.sms2air.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import de.crashsource.sms2air.R;
import de.crashsource.sms2air.adapter.RecipientAdapter;
import de.crashsource.sms2air.common.AndroidBase;
import de.crashsource.sms2air.common.GsmCharset;
import de.crashsource.sms2air.common.Recipient;
import de.crashsource.sms2air.common.RecipientList;
import de.crashsource.sms2air.db.SmsDatabase;
import de.crashsource.sms2air.smsgateway.SmsGateway;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public class ActivityComposeSms extends Activity {
	public static final String TAG = ActivityComposeSms.class.getSimpleName();

	public static final int PICK_CONTACT = 1300;
	public static final int PICK_CONTACT_NUMBER = 1301;
	public static final int INTENT_SENDING_PREFERENCES_ID = 1004;
	protected final int DIALOG_SAVE = 1401;

	private EditText mBodyText;
	private ListView mRecipientListView;
	private TextView mTitlebarLeft;
	private TextView mTitlebarRight;
	private AlertDialog mAddNumberDialog;

	private SmsDatabase mDb;
	private SmsGateway mSms;
	private int mDraftId = -1;
	private boolean mIsDestroyed = false;
	private boolean mIsDirty;
	private boolean mCustomTitleSupported = false;

	private RecipientList mRecipientList = new RecipientList();
	private RecipientAdapter mRecipientAdapter;

	private TextWatcher mTextBodyWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			mIsDirty = true;

			// display SMS length and number
			if (mCustomTitleSupported) {
				String text = mBodyText.getText().toString();
				StringBuilder sb = new StringBuilder();
				sb.append(mSms.countChars(text));
				sb.append(" / ");
				sb.append(mSms.countSms(text));
				sb.append(" SMS");
				mTitlebarRight.setText(sb);
			}
		}

		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mCustomTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		setContentView(R.layout.activity_compose_sms);

		mSms = new SmsGateway(Preferences.getPreference((ContextWrapper) this,
				Preferences.GATEWAY_KEY));

		// set custom titlebar
		if (mCustomTitleSupported) {
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
					R.layout.custom_titlebar);

			mTitlebarLeft = (TextView) findViewById(R.id.titlebar_left);
			mTitlebarRight = (TextView) findViewById(R.id.titlebar_right);
			mTitlebarLeft.setText(R.string.app_name);
		}

		mBodyText = (EditText) findViewById(R.id.compose_body_text);
		mRecipientListView = (ListView) findViewById(R.id.compose_recipient_list);
		mRecipientAdapter = new RecipientAdapter(this,
				R.layout.item_compose_recipient, mRecipientList);
		mRecipientListView.setAdapter(mRecipientAdapter);

		mBodyText.addTextChangedListener(mTextBodyWatcher);
		mBodyText.setText(""); // call the text changed listener the first time
		// to update titlebar

		Bundle b = null;
		if ((b = getIntent().getBundleExtra(ActivityMainScreen.BUNDLE_DRAFT)) != null) {
			mDraftId = b.getInt("draft_id");

			String messageBody = b.getString("draft_message_body");

			Log.v(TAG, "onCreate(): draft-id: " + mDraftId);

			mBodyText.setText(messageBody);
			mRecipientList = RecipientList.deserialize(b
					.getString("serialized_recipient_list"));
			mRecipientAdapter = new RecipientAdapter(this,
					R.layout.item_compose_recipient, mRecipientList);
			mRecipientListView.setAdapter(mRecipientAdapter);
		}
		/*
		 * This happens if the activity is started via intent
		 */
		else if (getIntent().getData() != null) {
			if (getIntent().getData().toString().startsWith("sms")) {
				String phoneNumber;
				if(getIntent().getData().toString().startsWith("smsto:")) {
					phoneNumber = getIntent().getData().toString().substring(
						"smsto:".length());
				} else {
					phoneNumber = getIntent().getData().toString().substring(
						"sms:".length());
				}
				if (phoneNumber.length() != 0) {
					Log.v(TAG, "onCreate(): phoneNumber: " + phoneNumber);
					if (phoneNumber.startsWith("%2B")) {
						phoneNumber = "+" + phoneNumber.substring(3);
					}
					
					Recipient r = AndroidBase.getRecipientByPhoneNumber(this,
							phoneNumber);
					if (r == null) { // if recipient is unknown, create a temporary new one
						r = new Recipient("", getResources().getString(R.string.compose_unknown_contact), phoneNumber, -1, Recipient.NO_RETURN_CODE);
					}
					mRecipientList.add(r);
					mRecipientAdapter = new RecipientAdapter(this,
							R.layout.item_compose_recipient, mRecipientList);
					mRecipientListView.setAdapter(mRecipientAdapter);
				}
				
				if (getIntent().getExtras() != null) {
					String message = getIntent().getExtras().getString("sms_body");
					if (message.length() != 0) {
						Log.v(TAG, "onCreate(): message: " + message);
						mBodyText.setText(message);
					}
				}
			}
		}
		mIsDirty = false; // no changes yet
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		mIsDestroyed = true;
		super.onSaveInstanceState(outState);
	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_SAVE:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.save_draft_title).setMessage(
					R.string.save_draft_description).setCancelable(true)
					.setPositiveButton(R.string.save_draft_confirm,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									newDraft();
									finish();
								}
							}).setNegativeButton(R.string.save_draft_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									finish();
								}
							});
			AlertDialog alert = builder.create();
			return alert;
		default:
			dialog = null;
		}
		return dialog;
	}

	private void updateDraft() {
		mDb = new SmsDatabase(this);
		ContentValues values = new ContentValues();
		values.put(SmsDatabase.RECIPIENTS, RecipientList
				.serializeForDb(this.mRecipientList));
		values
				.put(SmsDatabase.BODY_TEXT, mBodyText.getText()
						.toString());
		java.util.Date today = new java.util.Date();
		values.put(SmsDatabase.DATE, Long.toString(today.getTime()));

		mDb.getWritableDatabase().update(SmsDatabase.TABLE_NAME, values,
				SmsDatabase.ID + "=?",
				new String[] { Integer.toString(mDraftId) });

		Log.v(TAG, "onPause(): Updated draft with id: " + mDraftId
				+ ". Result: ");

		Toast.makeText(this, R.string.compose_hint_updated_draft,
				Toast.LENGTH_SHORT).show();
	}

	private void newDraft() {
		mDb = new SmsDatabase(this);
		ContentValues values = new ContentValues();
		values.put(SmsDatabase.RECIPIENTS, RecipientList
				.serializeForDb(this.mRecipientList));
		values
				.put(SmsDatabase.BODY_TEXT, mBodyText.getText()
						.toString());
		java.util.Date today = new java.util.Date();
		values.put(SmsDatabase.DATE, Long.toString(today.getTime()));
		values.put(SmsDatabase.TYPE, SmsDatabase.TYPE_DRAFT);
		mDb.getWritableDatabase().insert(SmsDatabase.TABLE_NAME, null,
				values);

		Log.v(TAG, "onPause(): New draft composed");
		Toast.makeText(this, R.string.compose_hint_created_draft,
				Toast.LENGTH_SHORT).show();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (mIsDirty && mDraftId == -1) {
				showDialog(DIALOG_SAVE);
				return true;
			} else if (mIsDirty && mDraftId > -1) {
				updateDraft();
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	public void onClickBrowseContacts(View v) {
		Intent intent = new Intent(Intent.ACTION_PICK, 
				ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(intent, PICK_CONTACT);
	}

	public void onClickAddNumber(View v) {
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_add_recipient_number,
				(ViewGroup) findViewById(R.id.add_recipient_root));

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final EditText phoneNumber = (EditText) layout
				.findViewById(R.id.add_recipient_phone_number);
		builder.setView(layout);
		builder.setTitle(R.string.add_recipient_title);
		builder.setPositiveButton(R.string.add_recipient_confirm,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Log.v(TAG, "Add recipient dialog confirmed");
						String number = phoneNumber.getText().toString();
						if (number.equals("")) {
							dialog.cancel();
						} else {
							addRecipientToList(new Recipient(null, null,
									number, Recipient.NO_TYPE,
									Recipient.NO_RETURN_CODE));
						}
					}
				}).setNegativeButton(R.string.add_recipient_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		mAddNumberDialog = builder.create();
		mAddNumberDialog.show();
	}

	public void onClickRemoveRecipient(View v) {
		Log.v(TAG, "onClickRemoveRecipient() entered");
		View parent = (View) v.getParent();
		Bundle b = (Bundle) parent.getTag();
		int index = b.getInt("position");
		Log.v(TAG, "onClickRemoveRecipient(): delete recipient with index: "
				+ index);

		removeRecipientFromList(index);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case PICK_CONTACT:
			Log.v(TAG, "onActivityResult(): Back from browse contacts.");
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor c = managedQuery(contactData, null, null, null, null);
				Log.v(TAG, "onActivityResult: data: "
						+ data.getData().toString());

				// Setting default values. These will be overwritten later
				String name;
				String lookupKey;

				try {
					c.moveToFirst();
					name = c
							.getString(c
									.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
					lookupKey = c
							.getString(c
									.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY));
				} finally {
					// Closing the cursor resumes in an error in Android 4.0
					// Therefore it will be closed at the very end
					//c.close();
				}

				Log.v(TAG, "onActivityResult(): picked contact: " + name
						+ "(lookupKey: " + lookupKey + ")");

				c = getContentResolver().query(
						Data.CONTENT_URI, // uri
						new String[] { // projection
						Data._ID, Phone.NUMBER, Phone.TYPE, Phone.LABEL },
						Data.LOOKUP_KEY + "=?" + " AND " + Data.MIMETYPE + "='" // selection
								+ Phone.CONTENT_ITEM_TYPE + "'",
						new String[] { lookupKey }, // selection
						// args
						null // sort order
						);

				if (c.getCount() > 1) { // we need to call an intent to let the
					// user decide which number
					RecipientList rl = new RecipientList();

					while (c.moveToNext()) {
						// only the number and number type is different for each
						// number and needs to be reloaded
						String number = c.getString(c
								.getColumnIndexOrThrow(Phone.NUMBER));
						int numberType = c.getInt(c
								.getColumnIndexOrThrow(Phone.TYPE));

						rl.add(new Recipient(lookupKey, name, number,
								numberType, Recipient.NO_RETURN_CODE));
					}

					Intent in = new Intent(this, ActivityListPhoneNumbers.class);
					Bundle b = new Bundle();
					b.putString("serialized_recipientlist", RecipientList
							.serialize(rl));
					in.putExtra("extras", b);
					startActivityForResult(in, PICK_CONTACT_NUMBER);

				} else if (c.getCount() == 1) {
					c.moveToFirst();

					String number = c.getString(c
							.getColumnIndexOrThrow(Phone.NUMBER));
					int type = c.getInt(c.getColumnIndexOrThrow(Phone.TYPE));

					addRecipientToList(new Recipient(lookupKey, name, number,
							type, Recipient.NO_RETURN_CODE));

				} else {
					// inform the user, that the contact has no number to add
					Toast.makeText(
							this,
							Html.fromHtml(this.getString(
									R.string.compose_contact_has_no_numbers,
									"<b>" + name + "</b>")), Toast.LENGTH_LONG)
							.show();
					return; // there is no number set
				}
				// cursor needs to be closed at the end because of Android 4.0
				c.close();
			}
			break;

		case INTENT_SENDING_PREFERENCES_ID:
			if (resultCode == Activity.RESULT_OK) {
				// delete draft, because it has been sent
				if (mDraftId > -1) {
					mDb = new SmsDatabase(this);
					try {
						mDb.delete(mDraftId);
					} finally {
						mDb.close();
					}
				}
				setResult(Activity.RESULT_OK);
				finish();
			}
		}

		switch (resultCode) {
		case Activity.RESULT_OK:
			Log.v(TAG, "onActivityResult(): RESULT_OK");

			if (data != null
					&& data.getStringExtra("chosen_recipient_list") != null) {
				RecipientList rl = RecipientList.deserialize(data
						.getStringExtra("chosen_recipient_list"));

				if (rl.size() != 0) {
					addRecipientToList(rl.get(0)); // there is only one
					// recipient to add
				} else {
					Log.e(TAG, "onActivityResult(): no number returned");
				}
			}

		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public void onClickForward(View v) {
		// if there are not all fields filled out
		if (mRecipientList.size() == 0
				|| mBodyText.getText().toString().length() == 0) {
			Toast t = Toast.makeText(v.getContext(),
					R.string.compose_error_fill, Toast.LENGTH_SHORT);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();
			return;
		}

		// create bundle with the necessary information
		GsmCharset gc = new GsmCharset();
		Bundle b = new Bundle();
		String bodyText = mBodyText.getText().toString();
		b.putString("body_text", bodyText);
		b.putString("serialized_recipient_list", RecipientList
				.serialize(mRecipientList));
		b.putBoolean("is_unicode_text", !gc.isEncodeableInGsm0338(bodyText));
		Intent i = new Intent(this, ActivitySendingPreferences.class);
		i.putExtra("extras", b);
		startActivityForResult(i, INTENT_SENDING_PREFERENCES_ID);
	}

	private void addRecipientToList(Recipient r) {
		mIsDirty = true;
		mRecipientList.add(r);
		mRecipientAdapter.notifyDataSetChanged();
	}

	private void removeRecipientFromList(int index) {
		mIsDirty = true;
		mRecipientList.remove(index);
		mRecipientAdapter.notifyDataSetChanged();
	}
}
