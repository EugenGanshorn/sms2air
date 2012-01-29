package de.crashsource.sms2air.gui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import de.crashsource.sms2air.R;
import de.crashsource.sms2air.adapter.HistoryAdapter;
import de.crashsource.sms2air.db.SmsDatabase;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public class ActivityHistoryOverview extends Activity {
	public static final String TAG = ActivityHistoryOverview.class.getSimpleName();

	private ListView mHistoryList;
	private CursorAdapter mHistoryListAdapter;
	private boolean mCustomTitleSupported;

	private TextView mTitlebarLeft;
	private TextView mTitlebarRight;
	
	private Context mContext;

	private SmsDatabase mDb;
	
	private OnItemClickListener mHistoryListClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int arg2,
				long arg3) {
			Bundle b = (Bundle) view.getTag();
			
			Intent intent = new Intent(mContext, ActivityHistorySingle.class);
			intent.putExtra("extras", b);
			startActivity(intent);
		}
	};
	
	private OnCreateContextMenuListener mHistoryItemContextListener = new OnCreateContextMenuListener() {
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			menu.setHeaderTitle(R.string.con_history_title);
			getMenuInflater().inflate(R.menu.context_menu_history, menu);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = this.getApplicationContext();

		mCustomTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		setContentView(R.layout.activity_history_overview);

		if (mCustomTitleSupported) {
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
					R.layout.custom_titlebar);

			mTitlebarLeft = (TextView) findViewById(R.id.titlebar_left);
			mTitlebarRight = (TextView) findViewById(R.id.titlebar_right);
			mTitlebarLeft.setText(R.string.app_name);
			mTitlebarRight.setText(R.string.history_title);
		}

		mHistoryList = (ListView) findViewById(R.id.history);
		mHistoryList.setAdapter(mHistoryListAdapter);
		mHistoryList.setOnItemClickListener(mHistoryListClickListener);
		mHistoryList.setOnCreateContextMenuListener(mHistoryItemContextListener);
	}

	@Override
	protected void onStart() {
		super.onStart();

		updateHistory();
	}

	private void updateHistory() {
		mDb = new SmsDatabase(this);
		try {
			String[] columns = new String[] { SmsDatabase.ID,
					SmsDatabase.RECIPIENTS,
					SmsDatabase.BODY_TEXT, SmsDatabase.DATE,
					SmsDatabase.TYPE };

			Cursor history = mDb.getReadableDatabase()
					.query(SmsDatabase.TABLE_NAME,
							columns, // columns
							SmsDatabase.TYPE + "=?", // selection
							new String[] { Integer
									.toString(SmsDatabase.TYPE_SENT) }, // selectionArgs
							null, // group by
							null, // having
							SmsDatabase.DATE + " DESC" // order by
					);

			mHistoryList.setAdapter(new HistoryAdapter(this, history));
		} finally {
			mDb.close();
		}
	}
	
	private void clearHistory() {
		mDb = new SmsDatabase(this);
		try {
			mDb.clearHistory();
		} finally {
			mDb.close();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.option_menu_history_overview, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opt_clear_history:
			// TODO: Request if the user is sure to do that
			clearHistory();
			Toast.makeText(this.getApplicationContext(),
					R.string.history_database_cleared, Toast.LENGTH_SHORT)
					.show();
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
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
		case R.id.con_history_delete:
			Log.v(TAG, "onContextItemSelected() - delete record (id: " + info.id
					+ ")");
			mDb = new SmsDatabase(this);
			try {
				mDb.delete(info.id);
				updateHistory();
			} finally {
				mDb.close();
				Toast.makeText(this, R.string.history_deleted_record,
						Toast.LENGTH_LONG).show();
			}
			break;
		}

		return super.onContextItemSelected(item);
	}
}
