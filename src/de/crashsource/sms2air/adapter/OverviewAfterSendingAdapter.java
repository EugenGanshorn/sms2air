package de.crashsource.sms2air.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.content.ContextWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.crashsource.sms2air.R;
import de.crashsource.sms2air.common.Recipient;
import de.crashsource.sms2air.gui.Preferences;
import de.crashsource.sms2air.smsgateway.SmsGateway;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public class OverviewAfterSendingAdapter extends ArrayAdapter<Recipient> {

	private ArrayList<Recipient> mRecipientList;
	
	public OverviewAfterSendingAdapter(Context context, int textViewResourceId,
			ArrayList<Recipient> objects) {
		super(context, textViewResourceId, objects);
		
		mRecipientList = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.item_overview_after_sending, null);
        }
        Recipient receiver = mRecipientList.get(position);
        if (receiver != null) {
        		LinearLayout layoutItem = (LinearLayout) convertView.findViewById(R.id.overview_after_sending_item);
                TextView receiverName = (TextView) convertView.findViewById(R.id.overview_after_sending_item_receiver);
                TextView receiverNumber = (TextView) convertView.findViewById(R.id.overview_after_sending_item_number);
                ImageView status = (ImageView) convertView.findViewById(R.id.overview_after_sending_item_icon);
                TextView summary = (TextView) convertView.findViewById(R.id.overview_after_sending_item_summary);
                
                if (receiver.getName() != null && receiver.getName().length() != 0) {
                	// receiver name
                    if (receiverName != null) {
                    	receiverName.setText(receiver.getName());
                    }
                    // receiver number
                    if (receiverNumber != null) {
                    	receiverNumber.setText(receiver.getNumber());
                    }
                } else {
                	// set receiver number as receiver name because there is no name
                	if (receiverName != null) {
                		receiverName.setText(receiver.getNumber());
                	}
                }
                
                // return code
                if (status != null) {
                	if (receiver.getReturnCode() == SmsGateway.RETURN_CODE_OK || receiver.getReturnCode() == SmsGateway.RETURN_CODE_NOT_FOUND) {
                		status.setImageDrawable(parent.getResources().getDrawable(R.drawable.ic_menu_mark));
                	} else {
                		status.setImageDrawable(parent.getResources().getDrawable(R.drawable.ic_menu_block));
                		layoutItem.setBackgroundColor(convertView.getContext().getResources().getColor(R.color.error_list_item));
                	}
                }
                
                // summary
                String[] summaryTextes = parent.getResources().getStringArray(R.array.smstrade_return_codes);
                SmsGateway s = new SmsGateway(Preferences.getPreference((ContextWrapper) this.getContext(), Preferences.GATEWAY_KEY));
                int index = s.getReturnCodeIndex(receiver.getReturnCode());
                if (summary != null) {
                	if (index == SmsGateway.RETURN_CODE_INTERNAL_ERROR) {
                		summary.setText(R.string.overview_error_internal);
                	} else if (index == SmsGateway.RETURN_CODE_NOT_FOUND) {
                		// this happens if there was a return code used that is not listed yet
                		summary.setText(parent.getResources().getString(R.string.overview_error_undefined) + receiver.getReturnCode());
                	} else {
                		summary.setText(summaryTextes[index]);
                	}
                }
        }
                
        return convertView;
	}

}
