package de.crashsource.sms2air.db;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public class SmsDatabase extends Database {
	public static final String TAG = SmsDatabase.class.getSimpleName();

	public static final String TABLE_NAME = "sms";
	
	public static final String ID = "_id";
	public static final String TYPE = "type";
	public static final String BODY_TEXT = "body_text";
	public static final String DATE = "date";
	public static final String RECIPIENTS = "receipients";
	
	public static final int TYPE_DRAFT = 1;
	public static final int TYPE_SENT = 2;
	public static final int TYPE_RECEIVED = 3; // not used yet
	
	public static final String SQL_CREATE =
	      "CREATE TABLE " + TABLE_NAME + " (" +
	      ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
	      TYPE + " INTEGER NOT NULL," +
	      BODY_TEXT + " TEXT NOT NULL," +
	      DATE + " TEXT NOT NULL," +
	      RECIPIENTS + " TEXT NOT NULL" +
	      ");";

	public static final String SQL_DROP =
		"DROP TABLE IF EXISTS " +
		TABLE_NAME;
	
	public static final String[] ALL_COLUMNS = new String[] {
	      ID,
	      TYPE,
	      BODY_TEXT,
	      DATE,
	      RECIPIENTS
	      };
	
	public SmsDatabase(Context context) {
		super(context);
	}
	
	public int clearDrafts() {
		return getWritableDatabase().delete(
			TABLE_NAME, 
			TYPE + "=?", 
			new String[] {Integer.toString(TYPE_DRAFT)}
			);
	}
	
	public int clearHistory() {
		return getWritableDatabase().delete(
				TABLE_NAME, 
				TYPE + "=?", 
				new String[] {Integer.toString(TYPE_SENT)}
				);
	}
	
	public int countHistory() {
		int ret = 0;
		Cursor c = getWritableDatabase().query(
				TABLE_NAME, 
				new String[] { "count(*)" }, 
				TYPE + "=?", 
				new String[] { Integer.toString(TYPE_SENT) }, 
				null, 
				null, 
				null
			);
		try {
			c.moveToFirst();
			ret = c.getInt(0);
			Log.v(TAG, ""+ ret);
		} finally {
			c.close();
		}
		return ret;
	}
	
	public int delete(long id) {
		return getWritableDatabase().delete(
				TABLE_NAME, 
				ID + "=?", 
				new String[] {Long.toString(id)}
				);
	}
	
	public Cursor getReadable(String[] columns, long id) {
		return getReadableDatabase().query(
				TABLE_NAME, 
				columns, 
				ID + "=?", 
				new String[] { Long.toString(id) }, 
				null, 
				null, 
				null
			);
	}

}
