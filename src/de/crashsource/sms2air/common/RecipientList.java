package de.crashsource.sms2air.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.util.Log;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public class RecipientList extends ArrayList<Recipient> {
	private static final long serialVersionUID = 1L;

	public static final String TAG = RecipientList.class.getSimpleName();

	private List<Recipient> list = new Vector<Recipient>();

	private static final String SERIALIZE_RECEIVER_SEPARATOR = "-sepRec-";
	private static final String SERIALIZE_INFORMATION_SEPARATOR = "-sepInf-";

	public void add(String lookupKey, String name, String number,
			int numberType, int returnCode) {
		list
				.add(new Recipient(lookupKey, name, number, numberType,
						returnCode));
	}

	@Override
	public boolean add(Recipient r) {
		list.add(r);
		return true;
	}

	public Recipient getReceiverByLookupKey(String lookupKey) {
		for (Recipient item : list) {
			if (item.getLookupKey().equals(lookupKey)) {
				return item;
			}
		}
		return null;
	}

	public Recipient get(int i) {
		return list.get(i);
	}

	public List<Recipient> getList() {
		List<Recipient> clone = new ArrayList<Recipient>(list.size());
		for (Recipient item : list) {
			try {
				clone.add(item.clone());
			} catch (CloneNotSupportedException e) {
				Log.e(TAG, e.getMessage());
			}
		}

		return clone;
	}

	public int size() {
		return list.size();
	}

	@Override
	public Recipient remove(int index) {
		Recipient r = this.list.get(index);
		list.remove(index);
		return r;
	}

	/**
	 * Extracts the phone numbers that were listed in the the receiver list and
	 * returns an array that contains all receiver numbers
	 * 
	 * @return array with only the receiver numbers
	 */
	public String[] getReceiverNumbersAsArray() {
		String[] res = new String[this.list.size()];
		for (int i = 0; i < this.list.size(); i++) {
			res[i] = this.list.get(i).number;
		}
		return res;
	}

	/**
	 * Extracts the names of the receiver that were listed in the the receiver
	 * list and returns an array that contains all receiver names
	 * 
	 * @return array with only the receiver names
	 */
	public String[] getReceiverNamesAsArray() {
		String[] res = new String[this.list.size()];
		for (int i = 0; i < this.list.size(); i++) {
			res[i] = this.list.get(i).name;
		}
		return res;
	}

	/**
	 * Creates a new receiver list containing the receivers that were given in a
	 * serialized form.
	 * 
	 * @param serialized
	 * @return new ReceiverList
	 */
	public static RecipientList deserialize(String serialized) {
		RecipientList rl = new RecipientList();
		if (serialized.equals(""))
			return rl; // this happens if no recipient was set

		String[] temp = serialized
				.split(RecipientList.SERIALIZE_RECEIVER_SEPARATOR);
		for (String temp2 : temp) {
			String[] temp3 = temp2
					.split(RecipientList.SERIALIZE_INFORMATION_SEPARATOR);

			if (temp3.length != 5)
				throw new IllegalArgumentException(
						"Could not deserialize the given serialized code. Given string: "
								+ serialized);
			String lookupKey = temp3[0].equals("null") ? null : temp3[0];
			String name = temp3[1].equals("null") ? null : temp3[1];
			String number = temp3[2].equals("null") ? null : temp3[2];
			int numberType = Integer.parseInt(temp3[3]);
			int returnCode = Integer.parseInt(temp3[4]);

			rl.add(lookupKey, name, number, numberType, returnCode);
		}
		return rl;
	}

	/**
	 * Deserializes a recipient list from a database entry. Always use this
	 * method if the recipient list was stored in a database.
	 * 
	 * @param context
	 *            The applications context
	 * @param serialized
	 * @return new RecipientList
	 */
	public static RecipientList deserializeFromDb(Context context,
			String serialized) {
		RecipientList rl = new RecipientList();
		if (serialized.equals(""))
			return rl; // this happens if no recipient was set

		String[] temp = serialized
				.split(RecipientList.SERIALIZE_RECEIVER_SEPARATOR);
		for (String temp2 : temp) {
			String[] temp3 = temp2
					.split(RecipientList.SERIALIZE_INFORMATION_SEPARATOR);

			if (temp3.length != 3)
				throw new IllegalArgumentException(
						"Could not deserialize the given serialized code. Given string: "
								+ serialized);
			String lookupKey = temp3[0].equals("null") ? null : temp3[0];
			String number = temp3[1].equals("null") ? null : temp3[1];
			int numberType = Integer.parseInt(temp3[2]);

			/*
			 * First we try to get the contact via the lookup key. If that
			 * doesn't work we use the phone number to identify the contact. If
			 * that also doesn't work we remove everything and display only the
			 * phone number.
			 * 
			 * If the lookup key is not valid, we need to look after a new one.
			 */
			Person person = AndroidBase
					.getPersonByLookupKey(context, lookupKey);
			if (person == null) {
				Recipient rTemp = AndroidBase.getRecipientByPhoneNumber(
						context, number);
				if (rTemp != null) {
					rl.add(new Recipient(rTemp.getLookupKey(), rTemp.getName(),
							number, numberType, Recipient.NO_RETURN_CODE));
				} else {
					rl.add(new Recipient(null, null, number, numberType,
							Recipient.NO_RETURN_CODE));
				}
			} else {
				rl.add(new Recipient(person, number, numberType,
						Recipient.NO_RETURN_CODE));
			}

		}
		return rl;
	}

	public static String serialize(RecipientList rl) {
		String ret = "";
		for (int i = 0; i < rl.size(); i++) {
			Recipient r = rl.get(i);
			ret += r.lookupKey + RecipientList.SERIALIZE_INFORMATION_SEPARATOR
					+ r.name + RecipientList.SERIALIZE_INFORMATION_SEPARATOR
					+ r.number + RecipientList.SERIALIZE_INFORMATION_SEPARATOR
					+ r.numberType
					+ RecipientList.SERIALIZE_INFORMATION_SEPARATOR
					+ r.returnCode + RecipientList.SERIALIZE_RECEIVER_SEPARATOR;
		}
		return ret;
	}

	public static String serializeForDb(RecipientList rl) {
		String ret = "";
		for (int i = 0; i < rl.size(); i++) {
			Recipient r = rl.get(i);
			ret += r.lookupKey + RecipientList.SERIALIZE_INFORMATION_SEPARATOR
					+ r.number + RecipientList.SERIALIZE_INFORMATION_SEPARATOR
					+ r.numberType + RecipientList.SERIALIZE_RECEIVER_SEPARATOR;
		}
		return ret;
	}

	@Override
	public String toString() {
		String ret = "RecipientList: \n ------------ \n";

		for (int i = 0; i < this.list.size(); i++) {
			Recipient r = this.list.get(i);
			ret += r.toString();
		}

		ret += "------------";

		return ret;
	}
}
