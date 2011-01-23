package de.crashsource.sms2air.common;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import de.crashsource.sms2air.R;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public class AndroidBase {	
	public static final String TAG = AndroidBase.class.getSimpleName();
	
	public static boolean haveNetworkAccess(Context context) {
		// check if there is a network-connection
		ConnectivityManager cm = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
		NetworkInfo ni = cm.getActiveNetworkInfo();
		// TODO: also test if there is roaming and warn the user ->
		// ni.isRoaming()
		if (ni == null || !ni.isConnectedOrConnecting()) {
			return false;
		}
		return true;
	}
	
	public static String getPhoneType(Context context, int type) {
		switch(type) {
    	case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
    		return context.getString(R.string.phone_type_home);
    	case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
    		return context.getString(R.string.phone_type_mobile);
    	case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
    		return context.getString(R.string.phone_type_work);
    	default:
    		return context.getString(R.string.phone_type_other);
		}
	}
	
	public static Recipient getRecipientByPhoneNumber(Context context, String phoneNumber) {
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		Cursor c = context.getContentResolver().query(
				uri, 
				new String[]{ PhoneLookup.LOOKUP_KEY, PhoneLookup.DISPLAY_NAME, PhoneLookup.TYPE }, 
				null, 
				null, 
				null
			);
		try {
			c.moveToFirst();
			String lookupKey = c.getString(0);
			String name = c.getString(1);
			int numberType = c.getInt(2);
			return new Recipient(lookupKey, name, phoneNumber, numberType, Recipient.NO_RETURN_CODE);
		} catch(IndexOutOfBoundsException e) {
			Log.e(TAG, e.getMessage());
			return null;
		} finally {
			c.close();
		}
	}
	
	public static Person getPersonByLookupKey(Context context, String lookupKey) {
		Uri lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
	    Uri res = ContactsContract.Contacts.lookupContact(context.getContentResolver(), lookupUri);
	    //String id = res.getLastPathSegment();
	    
		Cursor c = context.getContentResolver().query(
				res, 
				new String[] { ContactsContract.Contacts.DISPLAY_NAME }, 
				null, 
				null, 
				null
			);
		try {
			c.moveToFirst();
			String name = c.getString(0);
			return new Person(lookupKey, name);
		}
		catch(IndexOutOfBoundsException e) {
			Log.e(TAG, e.getMessage());
			return null;
		} finally {
			c.close();
		}
	}
}
