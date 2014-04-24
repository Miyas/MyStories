package com.mjumel.mystories;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.mjumel.mystories.tools.Gen;

public class SplashActivity extends Activity {

	private static final String MS_PREFS = "MyStoriesPrefs";
	private static final String MS_PREFS_LOGIN = "MyStories_login";
	private static final String MS_PREFS_PWD = "MyStories_pwd";
	private static final String MS_PREFS_UID = "MyStories_uid";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			Gen.writeLog("SplashActivity::onCreateView> Starting");
			
			View rootView = inflater.inflate(R.layout.fragment_splash, container, false);
			TextView textView = (TextView) rootView.findViewById(R.id.textView1);
			textView.setText("Checking Credentials...");
			
			SharedPreferences settings = getActivity().getSharedPreferences(MS_PREFS, 0);
			String login = settings.getString(MS_PREFS_LOGIN, null);
			String pwd = settings.getString(MS_PREFS_PWD, null);
			String uid = settings.getString(MS_PREFS_UID, null);
			
			Intent intent;
			if ( login != null && pwd != null && uid != null)
			{
				intent = new Intent(getActivity().getApplicationContext(), DrawerActivity.class);
	    		intent.putExtra("uid", uid);
			}
			else
			{
				intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
			}
			
			intent.putExtras(getActivity().getIntent());
			
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			
			Gen.appendLog("SplashActivity::onCreateView> login = " + login);
			Gen.appendLog("SplashActivity::onCreateView> pwd = " + pwd);
			Gen.appendLog("SplashActivity::onCreateView> uid = " + uid);
			startActivity(intent);
			getActivity().finish();
		    
			return rootView;
		}
	}

}
