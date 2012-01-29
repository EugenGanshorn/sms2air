package de.crashsource.sms2air.gui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import de.crashsource.sms2air.R;

/**
 * 
 * @author Fabian Mueller, .crashsource, 2010
 * 
 */
public class ActivitySplashScreen extends Activity {
	
	protected boolean active = true;
	protected int splashTime = 1000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		
		final Context context = this.getApplicationContext();

		// thread for displaying the SplashScreen
		Thread splashThread = new Thread() {
			@Override
			public void run() {
				try {
					int waited = 0;
					while (active && (waited < splashTime)) {
						sleep(100);
						if (active) {
							waited += 100;
						}
					}
				} catch (InterruptedException e) {
					// do nothing
				} finally {
					finish();
					startActivity(
							new Intent(context, ActivityMainScreen.class));
				}
			}
		};
		splashThread.start();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    if (event.getAction() == MotionEvent.ACTION_DOWN) {
	        active = false;
	    }
	    return true;
	}
}
