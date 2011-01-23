package de.crashsource.sms2air.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import de.crashsource.sms2air.R;
import de.crashsource.sms2air.adapter.OverviewAfterSendingAdapter;
import de.crashsource.sms2air.common.RecipientList;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public class ActivityOverviewAfterSending extends Activity {
	public static final String TAG = ActivityOverviewAfterSending.class.getSimpleName();

	private ListView mList;
	private RelativeLayout mNoticeLayout;
	private ImageView mNoticeIcon;
	private TextView mNoticeText;
	private OverviewAfterSendingAdapter mAdapter;
	private RecipientList mRecipientList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_overview_after_sending);

		mList = (ListView) findViewById(R.id.overview_list);
		mNoticeLayout = (RelativeLayout) findViewById(R.id.overview_notice_layout);
		mNoticeIcon = (ImageView) mNoticeLayout.findViewById(R.id.overview_notice_icon);
		mNoticeText = (TextView) mNoticeLayout.findViewById(R.id.overview_notice_text);

		Intent intent;
		if ((intent = getIntent()) != null
				&& (intent.getBundleExtra("extras") != null)) {
			Bundle extras = intent.getBundleExtra("extras");
			mRecipientList = RecipientList.deserialize(extras
					.getString("serialized_recipient_list"));

			mAdapter = new OverviewAfterSendingAdapter(this,
					R.layout.item_overview_after_sending, mRecipientList);
			mList.setAdapter(mAdapter);
			
			if (Preferences.isDebugModeOn(this)) {
				mNoticeLayout.setVisibility(View.VISIBLE);
				mNoticeIcon.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.ic_menu_stop));
				mNoticeText.setText(R.string.overview_warning_debug_mode_on);
			}
		}
	}

	public void onClickClose(View v) {
		setResult(Activity.RESULT_OK);
		finish();
	}
}
