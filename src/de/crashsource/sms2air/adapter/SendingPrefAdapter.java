package de.crashsource.sms2air.adapter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import de.crashsource.sms2air.R;
import de.crashsource.sms2air.gui.Preferences;
import de.crashsource.sms2air.gui.ActivitySendingPreferences;
import de.crashsource.sms2air.smsgateway.SmsGateway;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public class SendingPrefAdapter extends BaseAdapter {
	public final static String TAG = SendingPrefAdapter.class.getSimpleName();
	
	public static final int ROUTE_INDEX = 0;
	public static final int SENDER_INDEX = 1;
	public static final int SEND_DELAYED_INDEX = 2;

	private List<String> mPreferences;
	private Context context;
	
	private TextView mHeadline;
	private TextView mSummary;
	private ImageView mIcon;
	private ImageView mIconType;
	
	private SimpleDateFormat delayFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
	
	public SendingPrefAdapter(Context c, List<String> preferences) {
		this.mPreferences = preferences;
		this.context = c;
	}
	
	@Override
	public int getCount() {
		return mPreferences.size();
	}

	@Override
	public String getItem(int position) {
		return mPreferences.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		RelativeLayout layout;
		if (convertView == null) {
            layout = (RelativeLayout)LayoutInflater.from(context).inflate
                      (R.layout.item_sending_pref, parent, false);
		} else {
			layout = (RelativeLayout) convertView;
		}   
		
        mHeadline = (TextView) layout.findViewById(R.id.sending_pref_item_headline);
        mSummary = (TextView) layout.findViewById(R.id.sending_pref_item_summary);
        mIcon = (ImageView) layout.findViewById(R.id.sending_pref_item_icon);
        mIconType = (ImageView) layout.findViewById(R.id.sending_pref_item_icon_type);
        
        switch(position) {
        case ROUTE_INDEX:
        	mHeadline.setText(R.string.send_pref_route_headline);
        	mSummary.setText(mPreferences.get(SendingPrefAdapter.ROUTE_INDEX));
        	mIcon.setBackgroundDrawable(layout.getContext().getResources().getDrawable(android.R.drawable.ic_menu_directions));
        	mIconType.setBackgroundDrawable(layout.getContext().getResources().getDrawable(android.R.drawable.ic_menu_more));
        	
        	break;
        case SENDER_INDEX:
        	String summaryText = mPreferences.get(SendingPrefAdapter.SENDER_INDEX);
        	if (summaryText.equals(ActivitySendingPreferences.NO_SENDER_ADRESS)) {
        		summaryText = "<" + layout.getResources().getString(R.string.send_pref_no_sender_address) + ">";
        	}
        	mSummary.setText(summaryText);
        	
        	SmsGateway s = new SmsGateway(Preferences.getPreference((ContextWrapper) this.context , Preferences.GATEWAY_KEY));
        	if (s.isCustomSenderAddressAllowedForRoute(mPreferences.get(SendingPrefAdapter.ROUTE_INDEX))) {
        		layout.setEnabled(true);
        	} else {
        		layout.setEnabled(false);
        		mSummary.setText("<" + layout.getResources().getString(R.string.send_pref_sender_address_not_allowed) + ">");
        	}
        	
        	mIcon.setBackgroundDrawable(layout.getContext().getResources().getDrawable(android.R.drawable.ic_menu_edit));
        	mHeadline.setText(R.string.send_pref_custom_sender_adress);
        	
        	mIconType.setBackgroundDrawable(layout.getContext().getResources().getDrawable(android.R.drawable.ic_menu_more));
        	break;
        case SEND_DELAYED_INDEX:
        	long timestamp = 0;
        	try {
        		timestamp = Long.parseLong(mPreferences.get(SendingPrefAdapter.SEND_DELAYED_INDEX));
        	} catch (NumberFormatException e) {
        		// nothing to do here
        	}
        	mIcon.setBackgroundDrawable(layout.getContext().getResources().getDrawable(android.R.drawable.ic_menu_today));
        	mHeadline.setText(R.string.send_pref_send_delayed);
        	String delay = mPreferences.get(SendingPrefAdapter.SEND_DELAYED_INDEX);
        	String delayText = "<" + layout.getResources().getString(R.string.send_pref_no_delay_set) + ">";
        	if (!delay.equals(ActivitySendingPreferences.NO_DELAY)) {
        		Log.v(TAG, "set delayed to timestamp: " + timestamp);
        		Timestamp t = new Timestamp(timestamp);
        		delayText = delayFormat.format(t);
        	}
        	mSummary.setText(delayText);
        	mIconType.setBackgroundDrawable(layout.getContext().getResources().getDrawable(android.R.drawable.ic_menu_more));
        	break;
		}
		
		
//		layout.setTag(position);
		
		return layout;
	}

	

}
