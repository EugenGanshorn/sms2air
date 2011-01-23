package de.crashsource.sms2air.interfaces;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public interface ISms {
	/**
	 * The length of a single SMS.
	 */
	public static final int SMS_LENGTH_SINGLE = 160;

	/**
	 * The length of a complex SMS. This happens if the length of the SMS is
	 * more than {@link #SMS_LENGTH_SINGLE}, so that two or more SMS have to be
	 * sticked together.
	 */
	public static final int SMS_LENGTH_MUTLI = 153;

	/**
	 * This is the limit for a sender address if it contains chars. It can also
	 * contain digits, but must at least contain one char.
	 */
	public static final int LIMIT_SENDER_ADDRESS_ALPHANUMERIC = 11;

	/**
	 * This is the limit for a sender address if it only contains digits.
	 */
	public static final int LIMIT_SENDER_ADDRESS_NUMERIC = 16;
	
}
