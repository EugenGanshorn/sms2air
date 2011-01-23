package de.crashsource.sms2air.smsgateway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;
import de.crashsource.sms2air.common.Common;
import de.crashsource.sms2air.common.GsmCharset;
import de.crashsource.sms2air.interfaces.ISms;
import de.crashsource.sms2air.interfaces.ISmsGateway;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public class SmsGateway implements ISmsGateway, ISms {
	public static final String TAG = SmsGateway.class.getSimpleName();

	/**
	 * This is the name of the provider.
	 */
	public static final String PROVIDER_NAME = "smstrade.de";

	/**
	 * This is the URL (without <code>http://</code>) for the gateway to send
	 * SMS.
	 */
	public static String GATEWAY = "gateway.smstrade.de";

	/**
	 * These are the codes the service returns. Please note that all codes have
	 * to be greater than zero!
	 */
	public static int[] RETURN_CODES = new int[] { 10, 20, 30, 31, 40, 50, 60,
			70, 71, 80, 100 };

	/**
	 * This code is returned if the SMS was sent successfully. Must be part of
	 * <code>RETURN_CODES</code>.
	 */
	public static final int RETURN_CODE_OK = 100;

	/**
	 * These routes are defined for the service.
	 */
	public static final String[] ROUTES = new String[] { "basic", "economy",
			"gold", "direct" };
	
	/**
	 * These routes allows unicode formated sms.
	 */
	public static final String[] UNICODE_ROUTES = new String[] { ROUTES[1], ROUTES[2], ROUTES[3] };

	/**
	 * This is the key to identify with the smstrade.de service. Needs to be set
	 * before calling {@link #send}.
	 */
	private static String API_KEY = "";

	/**
	 * This is the URL to the API of the smstrade service. Needs to be set on
	 * the constructor.
	 */
	private URL API_URL;

	private final String TO = "to";
	private final String FROM = "from";
	private final String MESSAGE = "message";
	private final String ROUTE = "route";
	private final String KEY = "key";
	private final String DEBUG = "debug";
	private final String COST = "cost";
	private final String COUNT = "count";
	private final String TIMESTAMP = "senddate";
	private final String CONCAT = "concat";
	private final String CHARSET = "charset";
	private final String DELIVERY_REPORT = "dlr";
	private final String MESSAGE_TYPE = "messagetype";

	private boolean mDebugMode = false;
	private boolean mCostRequest = true;
	private boolean mCountRequest = true;
	private boolean mAutoConcat = true;
	private String mCharset = "UTF-8";
	private boolean mDeliveryReport = false;

	private double mCost = 0.0;
	private int mCount = 0;

	public SmsGateway(String apiKey) {
		API_KEY = apiKey;

		try {
			API_URL = new URL("http", GATEWAY, 80, "");
		} catch (MalformedURLException e) {
		} // that never happens, because URL is fix
	}

	@Override
	public String getDefaultRoute() {
		return ROUTES[0];
	}

	@Override
	public void setKey(String key) {
		API_KEY = key;
	}

	@Override
	public String getKey() {
		return API_KEY;
	}

	@Override
	public void setApiUrl(URL url) {
		API_URL = url;
	}

	@Override
	public int getReturnCodeIndex(int returnCode) {
		for (int i = 0; i < RETURN_CODES.length; i++) {
			if (returnCode == RETURN_CODES[i])
				return i;
		}
		return RETURN_CODE_NOT_FOUND;
	}

	@Override
	public boolean getDebugMode() {
		return mDebugMode;
	}

	@Override
	public boolean hasDebugMode() {
		return true;
	}

	@Override
	public void setDebugMode(boolean bool) {
		mDebugMode = bool;
	}

	@Override
	public float getCreditStatus() {
		List<String> keys = new ArrayList<String>();
		List<String> values = new ArrayList<String>();

		keys.add(KEY);
		values.add(API_KEY);

		String data = "";
		try {
			data += prepareDataForRequest(keys, values);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "getCreditStatus(): The data could not be encoded.");
		}
		Log.v(TAG, "getCreditStatus(): data: " + data);

		String retTemp = "";
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet("http://" + GATEWAY + "/credits/?" + data);
		Log.v(TAG, "getCreditStatus(): executing request " + httpget.getURI());

		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		try {
			retTemp = httpclient.execute(httpget, responseHandler);
		} catch (Exception e) {
			Log.e(TAG, "getCreditStatus(): could not get response. Error: "
					+ e.getMessage());
		}

		float ret = Float.parseFloat(retTemp);
		return ret;
	}

	@Override
	public String[] getRoutes() {
		return ROUTES;
	}

	@Override
	public String[] getRoutesWithCustomSenderAddress() {
		return new String[] { ROUTES[1], ROUTES[2], ROUTES[3] };
	}

	@Override
	public boolean hasCreditStatus() {
		return true;
	}

	@Override
	public int send(String route, String to, String from, String message,
			long timestamp) throws IllegalArgumentException {

		if (route == null || route.length() == 0)
			throw new IllegalArgumentException("There must be a route!");
		if (to == null)
			throw new IllegalArgumentException("There must be a recipient!");

		int retCode = 0;

		// reset values
		mCost = 0;
		mCount = 0;

		// filter the phone number
		to = Common.preparePhoneNumber(to);

		// remove sender address if route doesn't allow it
		if (!isCustomSenderAddressAllowedForRoute(route)) {
			from = "";
		}

		// what to do if sms is more than 160 words long?
		int concat = 0;
		if (message.length() > SMS_LENGTH_SINGLE && mAutoConcat) {
			concat = 1;
		}

		List<String> keys = new ArrayList<String>();
		List<String> values = new ArrayList<String>();

		keys.add(KEY);
		values.add(API_KEY);

		keys.add(ROUTE);
		values.add(route);

		keys.add(TO);
		values.add(to);

		keys.add(FROM);
		values.add(from == null ? "" : from);

		keys.add(MESSAGE);
		values.add(message);

		keys.add(TIMESTAMP);
		values.add(Long.toString(timestamp));

		keys.add(COST);
		values.add(mCostRequest ? "1" : "0");

		keys.add(COUNT);
		values.add(mCountRequest ? "1" : "0");

		keys.add(CONCAT);
		values.add(Integer.toString(concat));

		keys.add(DEBUG);
		values.add(mDebugMode ? "1" : "0");

		keys.add(CHARSET);
		values.add(mCharset);

		keys.add(DELIVERY_REPORT);
		values.add(mDeliveryReport ? "1" : "0");

		// String[] keys = new String[] { KEY, ROUTE, TO, FROM, MESSAGE,
		// TIMESTAMP, COST, COUNT, CONCAT, DEBUG, CHARSET, DELIVERY_REPORT };
		// String[] values = new String[] { API_KEY, route, to, from, message,
		// Long.toString(timestamp), mCostRequest ? "1" : "0",
		// mCountRequest ? "1" : "0", Integer.toString(concat),
		// mDebugMode ? "1" : "0", mCharset, mDeliveryReport ? "1" : "0" };

		/*
		 * Check if the message have to be a unicode message.
		 */
		GsmCharset gc = new GsmCharset();
		if (!gc.isEncodeableInGsm0338(message)) {
			keys.add(MESSAGE_TYPE);
			values.add("unicode");
		}

		String data = "";
		try {
			data = prepareDataForRequest(keys, values);
		} catch (UnsupportedEncodingException e) {
			retCode = RETURN_CODE_INTERNAL_ERROR;
			Log.e(TAG, "send(): The data could not be encoded. Err-Msg: "
					+ e.getMessage());
		}

		Log.v(TAG, "send(): data: " + data);

		try {
			// Send data
			URLConnection conn = API_URL.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(conn
					.getOutputStream());
			wr.write(data);
			wr.flush();

			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn
					.getInputStream()));
			String line;
			int i = 0;
			while ((line = rd.readLine()) != null) {
				Log.v(TAG, "send(): response line: " + line);
				if (i == 0) {
					/*
					 * Normally the first line contains the response code but if
					 * the SMS will be sent delayed, there will be something in
					 * the form: "send at 1284209340" so we try to parse the
					 * answer, but catch an error if there is one.
					 * 
					 * 09/11/2010: This case is not officially documented by the
					 * provider yet.
					 */
					try {
						retCode = Integer.parseInt(line);
					} catch (NumberFormatException e) {
						Log
								.v(TAG,
										"send(): response - entered first line exception case");
						if (line.startsWith("send at")) {
							retCode = RETURN_CODE_OK;
						} else {
							retCode = RETURN_CODE_INTERNAL_ERROR;
						}
					}
				}
				if (i == 2)
					mCost = Double.parseDouble(line); // costs
				if (i == 3)
					mCount = Integer.parseInt(line); // count
				i++;
			}
			wr.close();
			rd.close();
		} catch (IOException e) {
			retCode = RETURN_CODE_INTERNAL_ERROR;
			Log.e(TAG, "send(): The data could not be sent. Err-Msg: "
					+ e.getMessage());
		}

		Log.v(TAG, "send(): return code: " + retCode);
		return retCode;

	}

	public boolean isCustomSenderAddressAllowedForRoute(String route) {
		for (String routeAllowed : getRoutesWithCustomSenderAddress()) {
			if (route.equals(routeAllowed))
				return true;
		}
		return false;
	}

	private String prepareDataForRequest(List<String> keys, List<String> values)
			throws UnsupportedEncodingException {
		String data = "";
		GsmCharset gc = new GsmCharset();
		
		for (int i=0; i<keys.size(); i++) {
			if (!(data.length() == 0)) {
				data += "&";
			}
			
			if (values.get(i) == null) {
				values.set(i, "");
			}
			
			/*
			 * If the message must be sent as a unicode message, we have to prepare the message.
			 */
			if (keys.get(i).equals(MESSAGE) && !gc.isEncodeableInGsm0338(values.get(i))) {
				values.set(i, Common.stringToUtf16ByteString(values.get(i)));
			}
			
			data += URLEncoder.encode(keys.get(i), "UTF-8") + "="
				+ URLEncoder.encode(values.get(i), "UTF-8");
		}
		
		return data;
	}

	private int countSms(int textlength, int maxLengthSingle, int maxLengthMulti) {
		int smsCounter = 0;
		if (textlength <= maxLengthSingle) {
			smsCounter = 1;
		} else {
			smsCounter = textlength / maxLengthMulti + 1;
			if (textlength % maxLengthMulti == 0)
				smsCounter--;
		}
		return smsCounter;
	}

	public int countSms(String text) {
		GsmCharset gc = new GsmCharset();
		if (gc.isEncodeableInGsm0338(text)) {
			int textlength = gc.count(text);
			return countSms(textlength, ISms.SMS_LENGTH_SINGLE,
					ISms.SMS_LENGTH_MUTLI);
		} else {
			return countSms(text.length(), 70, 70);
		}
	}

	public int countChars(String text) {
		GsmCharset gc = new GsmCharset();
		if (gc.isEncodeableInGsm0338(text)) {
			return gc.count(text);
		} else {
			return text.length();
		}
	}

	public ISms setCostRequest(boolean bool) {
		this.mCostRequest = bool;
		return this;
	}

	public ISms setCountRequest(boolean bool) {
		this.mCountRequest = bool;
		return this;
	}

	public ISms setAutoConcat(boolean bool) {
		this.mAutoConcat = bool;
		return this;
	}

	public double getCost() {
		return mCost;
	}

	public double getCount() {
		return mCount;
	}

	@Override
	public boolean hasDeliveryReport() {
		return true;
	}

	@Override
	public void setDeliveryReport(boolean bool) {
		mDeliveryReport = bool;
	}
	
	public boolean routeAllowsUnicodeText(String route) {
		for (String unicodeRoute : UNICODE_ROUTES) {
			if (unicodeRoute.equals(route)) {
				return true;
			}
		}
		return false;
	}

}
