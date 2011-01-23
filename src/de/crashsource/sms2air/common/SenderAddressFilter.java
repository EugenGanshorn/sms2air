package de.crashsource.sms2air.common;

import android.text.InputFilter;
import android.text.Spanned;
import de.crashsource.sms2air.smsgateway.SmsGateway;

/**
 * Use this filter to check if a sender address given by a user is formated
 * correnctly.
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public class SenderAddressFilter implements InputFilter {

	private final int maxLengthNumeric = SmsGateway.LIMIT_SENDER_ADDRESS_NUMERIC;
	private final int maxLengthAlphaNumberic = SmsGateway.LIMIT_SENDER_ADDRESS_ALPHANUMERIC;

	@Override
	public CharSequence filter(CharSequence source, int start, int end,
			Spanned dest, int dstart, int dend) {

		String untilNow = dest.toString(); // this is the text without the new
											// char

		if (dest.length() < maxLengthAlphaNumberic) {
			return null; // let it pass
		} else if (dest.length() < maxLengthNumeric) {
			String sourceStr = (String) source;
			if (untilNow.matches("[0-9]+") && sourceStr.matches("[0-9]+")) {
				return null; // let it pass
			} else {
				return ""; // reject it
			}
		} else {
			return ""; // delete last char, because it is too long
		}
	}

}
