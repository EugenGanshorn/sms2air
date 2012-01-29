package de.crashsource.sms2air.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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
public class RecipientAdapter extends ArrayAdapter<Recipient> {
	public static final String TAG = RecipientAdapter.class.getSimpleName();

	private ArrayList<Recipient> mRecipientList;
	
	private TextView mName;
	private TextView mNumber;
	private TextView mNumberType;
	
	public RecipientAdapter(Context context,
			int textViewResourceId, ArrayList<Recipient> objects) {
		super(context, textViewResourceId, objects);
		
		mRecipientList = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.v(TAG, "getView(): entered");
		if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.item_compose_recipient, null);
        }
		
		Recipient r = mRecipientList.get(position);
		
		Log.v(TAG, "getView(): Recipient:\n" + r.toString());
		
		mName = (TextView) convertView.findViewById(R.id.compose_recipient_item_name);
		mNumber = (TextView) convertView.findViewById(R.id.compose_recipient_item_number);
		mNumberType = (TextView) convertView.findViewById(R.id.compose_recipient_item_number_type);
		
		if (mName != null) {
			String name = "";
			if (r.getName() == null) {
				name = convertView.getContext().getString(R.string.compose_unknown_contact);
			} else {
				name = r.getName();
			}
			mName.setText(name);
		}
		if (mNumber != null) {
			mNumber.setText(r.getNumber());
		}
		if (mNumberType != null) {
			mNumberType.setText("- " + AndroidBase.getPhoneType(this.getContext(), r.getNumberType()));
		}
		
		Bundle b = new Bundle();
		b.putInt("position", position);
		convertView.setTag(b);

		return convertView;
	}
}
