package de.crashsource.sms2air.common;

import java.io.UnsupportedEncodingException;

import android.util.Log;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public class Common {
	public static final String TAG = Common.class.getSimpleName();

	/**
	 * Method that filters out everything out of a phone number except the
	 * digits.
	 * 
	 * @param phoneNumber
	 *            the given "dirty" phone number
	 * @return the "clean" version of the given phone number
	 */
	public static String preparePhoneNumber(String phoneNumber) {
		return phoneNumber.replaceAll("[^0-9]", "");
	}

	public static String stringToUtf16ByteString(String string) {
		try {
			byte[] stringBytes = string.getBytes("UTF-16BE");
			String ret = "";
			for (byte currentByte : stringBytes) {
				String currentHexCode = Integer.toHexString(currentByte);
				if (currentHexCode.length() == 1) {
					ret += "0";
				}

				/*
				 * Workaround for some cases in which six "f" stands before the
				 * interesting part.
				 */
				if (currentHexCode.startsWith("ffffff")) {
					ret += currentHexCode.substring(6);
				} else {
					ret += currentHexCode;
				}

			}
			return ret.toUpperCase();
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Unsupported Encoding: " + e.getMessage());
			return "";
		}
	}
}
