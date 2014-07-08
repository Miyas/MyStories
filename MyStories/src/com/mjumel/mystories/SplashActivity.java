package com.mjumel.mystories;

import java.util.ArrayList;
import java.util.List;

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
import com.mjumel.mystories.tools.Contacts;
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
		private int userId;
		private ArrayList<Event> eventList;
		private ArrayList<Story> storyList;
		private ArrayList<Contact> contactList;
		
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
			
			new GetContactsTask().execute();
			
			if ( login != null && pwd != null && uid != null) {
	    		(new UserLoginTask()).execute(new String[] { login, pwd, uid });
			} else {
				redirectToLogin();
			}
		    
			return rootView;
		}
		
		private void redirectToLogin() {
			Gen.appendError("SplashActivity::redirectToLogin> Error while login, redirecting to login screen");
			
			Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
			intent.putExtras(getActivity().getIntent());
			intent.putExtra("origin", "splash");
			intent.putParcelableArrayListExtra("contacts", contactList);
			
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			//intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			
			startActivity(intent);
			getActivity().finish();
		}
		
		private void redirectToDrawer() {
			Gen.appendLog("SplashActivity::redirectToDrawer> Login OK, redirecting to drawer");
			
			Prefs.putString(getActivity(), MS_PREFS_UID, String.valueOf(userId));
		    
		    Intent intent = new Intent(getActivity(), DrawerActivity.class);
    		intent.putExtra("uid", String.valueOf(userId));
    		intent.putExtra("origin", "splash");
    		intent.putParcelableArrayListExtra("events", eventList);
    		intent.putParcelableArrayListExtra("stories", storyList);
    		intent.putParcelableArrayListExtra("contacts", contactList);
    		
    		intent.putExtras(getActivity().getIntent());
			
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			//intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			
			Gen.appendLog("SplashActivity::UserLoginTask> uid = " + userId);
			startActivity(intent);
			getActivity().finish();
		}
		
		/***************************************************************************************
		 *
		 *                                UserLoginTask Class
		 * 
		 ***************************************************************************************/
		private class UserLoginTask extends AsyncTask<String, Void, Integer> {
			@Override
			protected void onPreExecute() {
				textView.setText("Login in...");
			}
			
			@Override
			protected Integer doInBackground(String... params) {
				if(!Communication.checkNetState(getActivity())) return -99;
				return Communication.login(params[0], params[1], getActivity());
			}

			@Override
			protected void onPostExecute(final Integer uid) {
				switch(uid) {
					case (-99):
						textView.setText("Communication error, check your internet connexion");
						Gen.appendError("SplashActivity$UserLoginTask::onPostExecute> Communication error, check your internet connexion");
						break;
					case (-1):
						textView.setText("Error while login, redirecting to login screen");
						redirectToLogin();
						break;
					case (-2):
						textView.setText("Incorrect password");
						redirectToLogin();
						break;
					case (-3):
						textView.setText("Incorrect login");
						redirectToLogin();
						break;
					default:
						textView.setText("Login successful");
						userId = uid;
						new DownloadEventsTask().execute();
						break;
				}
			}

			@Override
			protected void onCancelled() {
			}
		}
		
		/***************************************************************************************
		 *
		 *                                DownloadEventsTask Class
		 * 
		 ***************************************************************************************/
		private class DownloadEventsTask extends AsyncTask<String, Integer, List<Event>>
	    {
	          protected void onPreExecute() {     
	        	  textView.setText("Downloading personal events...");
	          } 

	          protected List<Event> doInBackground(String ...params) {
	        	  Gen.appendLog("SplashActivity$DownloadEventsTask::doInBackground> Downloading events");
	              return Communication.getUserEvents(String.valueOf(userId));
	          } 

	          protected void onPostExecute(List<Event> result) {     
	        	  eventList = new ArrayList<Event>();
					if(result != null) {
						eventList.addAll(result);
					}
	                textView.setText("Downloading personal events... OK");
	            	Gen.appendLog("SplashActivity$DownloadEventsTask::onPostExecute> Nb of events downloaded : " + eventList.size());
	            	new DownloadStoriesTask().execute();
	          }
	          
	          @Override
	  		protected void onCancelled() {
	        	  textView.setText("Downloading personal events... Cancelled");
	  		}
	     }
		
		/***************************************************************************************
		 *
		 *                                DownloadStoriesTask Class
		 * 
		 ***************************************************************************************/
		private class DownloadStoriesTask extends AsyncTask<String, Integer, List<Story>>
		{
	        protected void onPreExecute() {
	        	textView.setText("Downloading personal stories...");
	        } 

	        protected List<Story> doInBackground(String ...params) {
	        	Gen.appendLog("SplashActivity$DownloadStoriesTask::doInBackground> Downloading stories");
	        	return Communication.getUserStories(String.valueOf(userId));
	        } 

			protected void onPostExecute(List<Story> result) {
				storyList = new ArrayList<Story>();
				if(result != null) {
					storyList.addAll(result);
				}
				textView.setText("Downloading personal stories... OK");
				Gen.appendLog("SplashActivity::DownloadStoriesTask::onPostExecute> Nb of stories downloaded : " + storyList.size());
				redirectToDrawer();
			}
	         
			@Override
			protected void onCancelled() {
				textView.setText("Downloading personal stories... Cancelled");
			}
		}
		
		/***************************************************************************************
		 *
		 *                                GetContactsTask Class
		 * 
		 ***************************************************************************************/
		private class GetContactsTask extends AsyncTask<String, Integer, List<Contact>>
		{
	        protected void onPreExecute() {
	        	textView.setText("Retrieving personal contacts...");
	        } 

	        protected List<Contact> doInBackground(String ...params) {
	        	Gen.appendLog("SplashActivity$GetContactsTask::doInBackground> Retrieving contacts");
	        	return Contacts.getContacts(getActivity().getApplicationContext());
	        } 

			protected void onPostExecute(List<Contact> result) {
				contactList = new ArrayList<Contact>();
				if(result != null) {
					contactList.addAll(result);
				}
				textView.setText("Retrieving personal contacts... OK");
				Gen.appendLog("SplashActivity::GetContactsTask::onPostExecute> Nb of contacts retrieved : " + contactList.size());
			}
	         
			@Override
			protected void onCancelled() {
				textView.setText("Retrieving personal contacts... Cancelled");
			}
		}
	}
}
