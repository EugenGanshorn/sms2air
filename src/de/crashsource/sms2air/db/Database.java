package de.crashsource.sms2air.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public class Database extends SQLiteOpenHelper {
	private static final String TAG = Database.class.getSimpleName();
	
	private static final String DATABASE_NAME = "database.db";
	private static final int DATABASE_VERSION = 1;
	
	public Database(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.v(TAG, "onCreate() called");
		db.execSQL(SmsDatabase.SQL_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.v(TAG, "onUpgrade() called");
		db.execSQL(SmsDatabase.SQL_DROP);
	    onCreate(db);
	}

}
