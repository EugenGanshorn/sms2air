package de.crashsource.sms2air.gui;

import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import de.crashsource.sms2air.R;
import de.crashsource.sms2air.adapter.DraftAdapter;
import de.crashsource.sms2air.common.AndroidBase;
import de.crashsource.sms2air.db.SmsDatabase;
import de.crashsource.sms2air.smsgateway.SmsGateway;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public class ActivityMainScreen extends Activity {
	public static final String TAG = ActivityMainScreen.class.getSimpleName();

	public static final int SMS_COMPOSE_CALL = 1001;
	public static final Uri SMS_DRAFT = Uri.parse("content://sms/draft");
	public static final String BUNDLE_DRAFT = "draft";

	private Button mComposeSms;
	private ListView mDrafts;
	private ScrollView mDraftsAltScrollView;
	private RelativeLayout mDraftsAltLayout;
	private SmsDatabase mDb;
	private TextView mTitlebarLeft;
	private TextView mTitlebarRight;

	private boolean mCustomTitleSupported = false;

	private OnItemClickListener mDraftListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int arg2,
				long arg3) {
			Bundle b = (Bundle) view.getTag();
			Log.v(TAG, "onItemClick(): draft-id: "
					+ Integer.toString(b.getInt("draft_id")));

			Intent i = new Intent(getApplicationContext(),
					ActivityComposeSms.class);
			i.putExtra(BUNDLE_DRAFT, b);
			startActivity(i);
		}
	};

	private OnCreateContextMenuListener mDraftContextListener = new OnCreateContextMenuListener() {
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			menu.setHeaderTitle(R.string.con_draft_title);
			getMenuInflater().inflate(R.menu.context_menu_drafts, menu);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mCustomTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		setContentView(R.layout.activity_main_screen);

		// set custom titlebar
		if (mCustomTitleSupported) {
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
					R.layout.custom_titlebar);

			mTitlebarLeft = (TextView) findViewById(R.id.titlebar_left);
			mTitlebarRight = (TextView) findViewById(R.id.titlebar_right);
			mTitlebarLeft.setText(R.string.app_name);
		}

		mComposeSms = (Button) findViewById(R.id.main_compose_sms);
		mDrafts = (ListView) findViewById(R.id.main_drafts);
		mDraftsAltScrollView = (ScrollView) findViewById(R.id.main_drafts_alt_scroll);
		mDraftsAltLayout = (RelativeLayout) findViewById(R.id.main_drafts_alt_layout);

		mDrafts.setOnItemClickListener(mDraftListener);
		mDrafts.setOnCreateContextMenuListener(mDraftContextListener);
	}

	@Override
	protected void onStart() {
		updateDrafts();
		updateCreditStatus();
		super.onStart();
	}

	private void updateCreditStatus() {
		if (Preferences.isReady(this) && Preferences.updateCreditStatus(this)
				&& AndroidBase.haveNetworkAccess(this)) {
			new UpdateCreditStatus().execute();
		} else {
			mTitlebarRight.setText("");
		}
	}

	private class UpdateCreditStatus extends AsyncTask<Object, Object, String> {

		@Override
		protected String doInBackground(Object... params) {
			Log.v(TAG, "UpdateCreditStatus, doInBackground(): entered");
			SmsGateway sms = new SmsGateway(Preferences.getPreference(
					(ContextWrapper) getApplicationContext(),
					Preferences.GATEWAY_KEY));

			float creditStatus = 0;
			try {
				creditStatus = sms.getCreditStatus();
			} catch (NumberFormatException e) {
				Log.e(TAG,
						"UpdateCreditStatus, doInBackground(): format error: "
								+ e.getMessage());
				return "";
			}
			return Float.toString(creditStatus);
		}

		protected void onPostExecute(String credit) {
			Log.v(TAG, "UpdateCreditStatus, onPostExecute(): entered");
			if (credit != "") {
				mTitlebarRight.setText(getString(R.string.main_credit_status,
						credit));
				Log.v(TAG,
						"UpdateCreditStatus, onPostExecute(): new credit status: "
								+ credit);
			} else {
				Toast.makeText(
						getApplicationContext(),
						getApplicationContext().getString(
								R.string.main_error_update_credit_status),
						Toast.LENGTH_LONG).show();
			}
		}

	}

	private void updateDrafts() {
		boolean hasDrafts = false;
		mDb = new SmsDatabase(this);
		try {
			String[] columns = new String[] { SmsDatabase.ID,
					SmsDatabase.RECIPIENTS, SmsDatabase.BODY_TEXT,
					SmsDatabase.DATE, SmsDatabase.TYPE };

			Cursor drafts = mDb.getReadableDatabase().query(
					SmsDatabase.TABLE_NAME, columns, // columns
					SmsDatabase.TYPE + "=?", // selection
					new String[] { Integer.toString(SmsDatabase.TYPE_DRAFT) }, // selectionArgs
					null, // group by
					null, // having
					SmsDatabase.DATE + " DESC" // order by
			);

			if (drafts.getCount() > 0) {
				hasDrafts = true;
			}

			mDrafts.setAdapter(new DraftAdapter(this, drafts));
		} finally {
			mDb.close();
		}

		// if there are drafts -> show drafts, otherwise show alternative text
		if (hasDrafts) {
			mDrafts.setVisibility(View.VISIBLE);
			mDraftsAltScrollView.setVisibility(View.GONE);
		} else {
			mDrafts.setVisibility(View.GONE);
			mDraftsAltScrollView.setVisibility(View.VISIBLE);

			Button preferences = (Button) mDraftsAltLayout
					.findViewById(R.id.main_drafts_alt_preferences);
			WebView webview = (WebView) mDraftsAltLayout
					.findViewById(R.id.main_drafts_alt_webview);
			webview.setBackgroundColor(Color.BLACK);

			if (!Preferences.isReady(this)) {
				preferences.setVisibility(View.VISIBLE);
				webview.loadUrl("file:///android_asset/"
						+ getResources().getString(R.string.html_first_use)
						+ ".html");
			} else {
				preferences.setVisibility(View.GONE);
				webview.loadUrl("file:///android_asset/"
						+ getResources().getString(R.string.html_no_drafts)
						+ ".html");
			}
		}
	}

	public void onClickComposeSms(View v) {
		Intent i = new Intent(this, ActivityComposeSms.class);
		startActivity(i);
	}

	public void onClickPreferences(View v) {
		Intent i = new Intent(this, Preferences.class);
		startActivity(i);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case SMS_COMPOSE_CALL:
			Log.v(TAG, "received sms data");

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onResume() {
		Log.v(TAG, "onResume() called");
		mComposeSms.setEnabled(Preferences.isReady(this));
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.option_menu_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.v(TAG, "onOptionsItemSelected() called");
		Intent intent;
		switch (item.getItemId()) {
		case R.id.opt_preferences:
			intent = new Intent(this, Preferences.class);
			startActivity(intent);
			return true;
		case R.id.opt_exit:
			Log.v(TAG, "opt_exit selected");
			finish();
			return true;
		case R.id.opt_info:
			Log.v(TAG, "opt_info selected");
			intent = new Intent(this, ActivityInformation.class);
			startActivity(intent);
			return true;
//		case R.id.opt_help:
//			Log.v(TAG, "help selected");
//			// TODO: create help site
//			Toast.makeText(this, R.string.temp_help_not_supported,
//					Toast.LENGTH_LONG).show();
//			return true;
		case R.id.opt_history:
			Log.v(TAG, "history selected");
			mDb = new SmsDatabase(this);
			if (mDb.countHistory() <= 0) {
				Toast.makeText(this, R.string.main_error_empty_history,
						Toast.LENGTH_SHORT).show();
				return true;
			}
			intent = new Intent(this, ActivityHistoryOverview.class);
			startActivity(intent);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			Log.e(TAG, "onContextItemSelected(): bad menuInfo", e);
			return false;
		}

		switch (item.getItemId()) {
		case R.id.con_draft_delete:
			Log.v(TAG, "onContextItemSelected() - delete draft (id: " + info.id
					+ ")");
			mDb = new SmsDatabase(this);
			try {
				mDb.delete(info.id);
			} finally {
				mDb.close();
				updateDrafts();
				Toast.makeText(this, R.string.main_hint_deleted_draft,
						Toast.LENGTH_LONG).show();
			}
			break;
		}

		return super.onContextItemSelected(item);
	}

}