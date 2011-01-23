package de.crashsource.sms2air.interfaces;

import java.net.URL;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public interface ISmsGateway {

	/**
	 * This is the code to return if something internally gets wrong.
	 */
	public static int RETURN_CODE_INTERNAL_ERROR = -1;

	/**
	 * This is the code to return if the return code given by the online service
	 * is not implemented in the class.
	 */
	public static int RETURN_CODE_NOT_FOUND = -2;

	/**
	 * Main function to send SMS via the SMS-Gateway.
	 * 
	 * Make sure you've called {@link #setKey(String)} and
	 * {@link #setApiUrl(Url)} before.
	 * 
	 * @param route
	 *            The route you want to choose.
	 * @param to
	 *            The recipient of this SMS
	 * @param from
	 *            The sender address
	 * @param message
	 *            The message body
	 * @param timestamp
	 *            The timestamp for time shift sending
	 * 
	 * @return The return code given by the SMS-Gateway.
	 */
	public int send(String route, String to, String from, String message,
			long timestamp);

	/**
	 * This contains all valid routes that are provided by the SMS-Gateway.
	 * 
	 * @return Valid routes provided by the SMS-Gateway.
	 */
	public String[] getRoutes();

	/**
	 * Used to get the SMS-Gateway's default route.
	 * 
	 * @return The default route
	 */
	public String getDefaultRoute();

	/**
	 * Some SMS-Gateways allows to add a own sender address, but most of them
	 * only allow that in special routes. All the routes that method returns are
	 * part of {@link #getRoutes()} and can be used with a customized sender
	 * address.
	 * 
	 * @return String array with routes that allow a custom sender address.
	 *         <code>null</code> if this is not allowed for any route.
	 */
	public String[] getRoutesWithCustomSenderAddress();

	/**
	 * This function sets the key for the SMS-Gateway.
	 */
	public void setKey(String key);

	/**
	 * This function returns the SMS-Gateway-Key.
	 * 
	 * @return The SMS-Gateway-Key.
	 */
	public String getKey();

	/**
	 * Setter method for the API URL that have to be specified by the class.
	 */
	public void setApiUrl(URL url);

	/**
	 * This method is used for l10n. Enter a return code and get the index of
	 * the return code in the language file.
	 * 
	 * @param returnCode
	 * @return The index of the given return code in the language file. Return
	 *         {@link #RETURN_CODE_NOT_FOUND} if the return code is not listed
	 *         in the file.
	 */
	public int getReturnCodeIndex(int returnCode);

	/**
	 * Checks if the SMS-Gateway supports a debugging mode. See also
	 * {@link #setDebugMode(boolean)} and {@link #getDebugMode()}.
	 * 
	 * @return True if the SMS-Gateway supports a debugging mode. Otherwise
	 *         false.
	 */
	public boolean hasDebugMode();

	/**
	 * Setter method for the debug mode. See {@link #hasDebugMode()} to test if
	 * the SMS-Gateway supports debugging.
	 * 
	 * @param bool
	 *            True if you want to switch debug mode on. False otherwise.
	 */
	public void setDebugMode(boolean bool);

	/**
	 * Getter method for the debug mode. See {@link #hasDebugMode()} to test if
	 * the SMS-Gateway supports debugging.
	 * 
	 * @return True if debug mode is currently activated. False otherwise.
	 */
	public boolean getDebugMode();

	/**
	 * Checks if the SMS-Gateway supports checking the credit status. See also
	 * 
	 * @return True if the SMS-Gateway supports checking the credit status.
	 *         False otherwise.
	 */
	public boolean hasCreditStatus();

	/**
	 * Check the credit status.
	 * 
	 * Make sure you've called {@link #setKey(String)} and
	 * {@link #setApiUrl(Url)} before.
	 * 
	 * @return The credit status without the currency.
	 */
	public float getCreditStatus();

	/**
	 * Method to check if the gateway is able to deliver reports
	 * 
	 * @return True if the possibility to deliver reports exists; false
	 *         otherwise
	 */
	public boolean hasDeliveryReport();

	/**
	 * Setter method for getting a delivery report
	 * 
	 * @param bool
	 *            True if a delivery report is requested, false otherwise
	 */
	public void setDeliveryReport(boolean bool);
}
