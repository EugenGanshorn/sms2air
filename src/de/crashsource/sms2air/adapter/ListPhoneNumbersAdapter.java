package de.crashsource.sms2air.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import de.crashsource.sms2air.R;
import de.crashsource.sms2air.common.AndroidBase;
import de.crashsource.sms2air.common.Recipient;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public class ListPhoneNumbersAdapter extends ArrayAdapter<Recipient> {

	private ArrayList<Recipient> mRecipientList;
	
	public ListPhoneNumbersAdapter(Context context, int textViewResourceId,
			ArrayList<Recipient> objects) {
		super(context, textViewResourceId, objects);
		
		mRecipientList = objects;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.item_list_phone_numbers, null);
        }
		
		Recipient r = mRecipientList.get(position);
		
		if (r != null) {
			TextView type = (TextView) convertView.findViewById(R.id.list_phone_numbers_item_type);
            TextView number = (TextView) convertView.findViewById(R.id.list_phone_numbers_item_number);
            
            if (type != null) {
            	type.setText(AndroidBase.getPhoneType(this.getContext(), r.getNumberType()));
            }
            
            if (number != null) {
            	number.setText(r.getNumber());
            }
		}
		
		return convertView;
	}

}
