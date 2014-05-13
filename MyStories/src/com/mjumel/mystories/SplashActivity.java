package com.mjumel.mystories;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Gen;
import com.mjumel.mystories.tools.Prefs;

public class SplashActivity extends Activity {

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

		private TextView textView;
		
		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			Gen.writeLog("SplashActivity::onCreateView> Starting");
			
			View rootView = inflater.inflate(R.layout.fragment_splash, container, false);
			textView = (TextView) rootView.findViewById(R.id.event_imageView_text);
			textView.setText("Checking Credentials...");
			
			String login = Prefs.getString(getActivity(), MS_PREFS_LOGIN);
			String pwd = Prefs.getString(getActivity(), MS_PREFS_PWD);
			String uid = Prefs.getString(getActivity(), MS_PREFS_UID);
			
			if ( login != null && pwd != null && uid != null) {
	    		(new UserLoginTask()).execute(new String[] { login, pwd, uid });
			} else {
				Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
				intent.putExtras(getActivity().getIntent());
				intent.putExtra("origin", "splash");
				
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				//intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				
				Gen.appendLog("SplashActivity::onCreateView> login = " + login);
				Gen.appendLog("SplashActivity::onCreateView> pwd = " + pwd);
				Gen.appendLog("SplashActivity::onCreateView> uid = " + uid);
				startActivity(intent);
				getActivity().finish();
			}
		    
			return rootView;
		}
		
		/**
		 * Represents an asynchronous login/registration task used to authenticate
		 * the user.
		 */
		public class UserLoginTask extends AsyncTask<String, Void, Integer> {
			@Override
			protected void onPreExecute() {
				textView.setText("Login in...");
			}
			
			@Override
			protected Integer doInBackground(String... params) {
				if(!Communication.checkNetState(getActivity())) return -2;
				return Communication.login(params[0], params[1]);
			}

			@Override
			protected void onPostExecute(final Integer uid) {
				if (uid > 0) {
					textView.setText("Login successful");
					Prefs.putString(getActivity(), MS_PREFS_UID, String.valueOf(uid));
				    
				    Intent intent = new Intent(getActivity(), DrawerActivity.class);
		    		intent.putExtra("uid", String.valueOf(uid));
		    		intent.putExtra("origin", "splash");
		    		
		    		intent.putExtras(getActivity().getIntent());
					
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
					//intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
					
					Gen.appendLog("SplashActivity::UserLoginTask> uid = " + uid);
					startActivity(intent);
					getActivity().finish();				    
				} else if (uid == -2) {
					textView.setText("Communication error, check your internet connexion");
					Gen.appendLog("SplashActivity::UserLoginTask> Communication error, check your internet connexion");
				} else {
					textView.setText("Error while login, redirecting to login screen");
					Gen.appendLog("SplashActivity::UserLoginTask> Error while login, redirecting to login screen");
					
					Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
					intent.putExtras(getActivity().getIntent());
					intent.putExtra("origin", "splash");
					
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
					//intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
					
					startActivity(intent);
					getActivity().finish();
				} 
			}

			@Override
			protected void onCancelled() {
			}
		}
	}
}
