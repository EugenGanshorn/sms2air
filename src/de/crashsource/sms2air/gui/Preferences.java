package de.crashsource.sms2air.gui;

import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.text.InputFilter;
import android.util.Log;
import android.widget.Toast;
import de.crashsource.sms2air.R;
import de.crashsource.sms2air.common.AndroidBase;
import de.crashsource.sms2air.common.SenderAddressFilter;
import de.crashsource.sms2air.smsgateway.SmsGateway;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public class Preferences extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	public static final String TAG = Preferences.class.getSimpleName();

	public static final String PREFERRED_ROUTE = "preferred_route";
	public static final String PREFERRED_SENDER = "preferred_sender";
	public static final String GATEWAY_KEY = "gateway_key";
	public static final String UPDATE_CREDIT_STATUS = "update_credit_status";
	public static final String DEBUG_MODE = "debug_mode";
	public static final String DELIVERY_REPORT = "delivery_report";

	private EditTextPreference mGatewayKey;
	private ListPreference mPreferredRoute;
	private EditTextPreference mPreferredSender;
	private CheckBoxPreference mUpdateCreditStatus;
	private CheckBoxPreference mDebugMode;
	private CheckBoxPreference mDeliveryReport;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);

		mGatewayKey = (EditTextPreference) getPreferenceScreen()
				.findPreference(GATEWAY_KEY);
		mPreferredRoute = (ListPreference) getPreferenceScreen()
				.findPreference(PREFERRED_ROUTE);
		mPreferredSender = (EditTextPreference) getPreferenceScreen()
				.findPreference(PREFERRED_SENDER);
		mUpdateCreditStatus = (CheckBoxPreference) getPreferenceScreen()
				.findPreference(UPDATE_CREDIT_STATUS);
		mDebugMode = (CheckBoxPreference) getPreferenceScreen().findPreference(
				DEBUG_MODE);
		mDeliveryReport = (CheckBoxPreference) getPreferenceScreen()
				.findPreference(DELIVERY_REPORT);

		/*
		 * set a address filter to the preferred sender to limit the length
		 * depending on the value that the user sets
		 */
		InputFilter[] FilterArray = new InputFilter[1];
		FilterArray[0] = new SenderAddressFilter();
		mPreferredSender.getEditText().setFilters(FilterArray);
		
		updateSummary();
	}

	public static boolean isReady(ContextWrapper ctx) {
		SharedPreferences sp = Preferences.getSharedPreferences(ctx);
		return !sp.getString(GATEWAY_KEY, "").equals("");
	}

	public static boolean updateCreditStatus(ContextWrapper ctx) {
		SharedPreferences sp = Preferences.getSharedPreferences(ctx);
		return sp.getBoolean(UPDATE_CREDIT_STATUS, false);
	}

	/**
	 * Checks if debug mode is set in the preferences.
	 * 
	 * @param ctx
	 *            The applications context.
	 * @return True if debug mode is set in the preferences. False otherwise.
	 */
	public static boolean isDebugModeOn(ContextWrapper ctx) {
		SharedPreferences sp = Preferences.getSharedPreferences(ctx);
		return sp.getBoolean(DEBUG_MODE, false);
	}

	public static final SharedPreferences getSharedPreferences(
			final ContextWrapper ctx) {
		return ctx.getSharedPreferences(ctx.getPackageName() + "_preferences",
				MODE_PRIVATE);
	}
	
	public static boolean isDeliveryReportRequested(final ContextWrapper ctx) {
		SharedPreferences sp = Preferences.getSharedPreferences(ctx);
		return sp.getBoolean(DELIVERY_REPORT, false);
	}

	/**
	 * Method to get a preference with a specified identifier.
	 * 
	 * @param ctx
	 *            The applications context.
	 * @param name
	 *            The settings identifier.
	 * @return The preference if set, otherwise an empty string.
	 */
	public static String getPreference(ContextWrapper ctx, String name) {
		return Preferences.getSharedPreferences(ctx).getString(name, "");
	}

	private void updateSummary() {
		String gatewayKey = mGatewayKey.getText().toString().equals("") ? getResources()
				.getString(R.string.pref_gateway_key_summary)
				: mGatewayKey.getText().toString();
		mGatewayKey.setSummary(gatewayKey);

		mPreferredRoute.setSummary(mPreferredRoute.getValue());

		String preferredSender = mPreferredSender.getText().toString().equals(
				"") ? getResources().getString(
				R.string.pref_preferred_sender_summary) : mPreferredSender
				.getText().toString();
		mPreferredSender.setSummary(preferredSender);

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
		// first update summaries
		updateSummary();
		
		// check if gateway key is valid
		if (key.equals(Preferences.GATEWAY_KEY)) {
			if (AndroidBase.haveNetworkAccess(getApplicationContext())) {

				SmsGateway sg = new SmsGateway(sharedPreferences.getString(
						GATEWAY_KEY, ""));
				try {
					sg.getCreditStatus();
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
					Toast.makeText(this.getApplicationContext(),
							R.string.pref_error_invalid_gateway_key,
							Toast.LENGTH_LONG).show();
					return;
				}
				Toast.makeText(this.getApplicationContext(),
						R.string.pref_success_edit_gateway_key,
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this.getApplicationContext(),
						R.string.pref_hint_no_check_gateway_key,
						Toast.LENGTH_LONG).show();
				return;
			}
		}
	}
}
