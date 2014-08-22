package com.mjumel.mystories;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Contacts;
import com.mjumel.mystories.tools.Gen;
import com.mjumel.mystories.tools.Prefs;
import com.mjumel.mystories.tools.SQLQuery;
import com.mjumel.mystories.tools.SQLite;

public class SplashActivity extends Activity {

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private final static int LOGS_RETENTION_DAYS = 7;
	
	private Context context;
	
	private TextView textView;
	private TextView tvVersion;
	private TextView tvTitle;
	
	private int userId;
	private String storyId = null;
	private ArrayList<Event> eventList;
	private ArrayList<Story> storyList;
	private ArrayList<Story> sharedStoryList;
	private ArrayList<Contact> contactList;
    
	private GoogleCloudMessaging gcm;
	
	private SQLQuery sqlQuery;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.fragment_splash);

		context = getApplicationContext();
		Gen.purgeLogFiles(LOGS_RETENTION_DAYS);
	
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			if (bundle.containsKey("story_id"))
				storyId = bundle.getString("story_id");
		}
		
		textView = (TextView) findViewById(R.id.splash_desc);
		tvVersion = (TextView) findViewById(R.id.splash_version);
		tvTitle = (TextView) findViewById(R.id.splash_logo_stories);
		
		tvTitle.setText(Gen.bicolorSpan("storiesme",7));
		
		if (MyStoriesApp.VERSION_NAME != null) {
			tvVersion.setText("v" + MyStoriesApp.VERSION_NAME);
			tvVersion.setVisibility(TextView.VISIBLE);
		} else
			tvVersion.setVisibility(TextView.INVISIBLE);
		
		String login = Prefs.getUserLogin(context);
		String pwd = Prefs.getUserPassword(context);
		String uid = Prefs.getUserId(context);
		
        // Check device for Play Services APK.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(context);
            if (Prefs.getRegistrationId(context).isEmpty())
                registerInBackground();
        } else {
            Gen.appendLog("DrawerActivity::onCreate> No valid Google Play Services APK found");
        }
		
		new GetContactsTask().execute();
		// TODO with SQLite
		
		sqlQuery = new SQLQuery(context);
		sqlQuery.open();
		
		if(!Communication.checkNetState(context))
			textView.setText("Communication error, check your internet connexion");
		else {
			textView.setText("Checking Credentials...");
			if ( login != null && pwd != null && uid != null) {
	    		(new UserLoginTask()).execute(new String[] { login, pwd, uid });
			} else {
				redirectToLogin();
			}
		}
	}
	
	private void redirectToLogin() {
		Gen.appendError("SplashActivity::redirectToLogin> Error while login, redirecting to login screen");
		
		Intent intent = new Intent(context, LoginActivity.class);
		intent.putExtras(getIntent());
		intent.putExtra("origin", "splash");
		if (storyId != null)
			intent.putExtra("story_id", storyId);
		intent.putParcelableArrayListExtra("contacts", contactList);
		
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		//intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		
		startActivity(intent);
		finish();
	}
	
	private void redirectToDrawer() {
		Gen.appendLog("SplashActivity::redirectToDrawer> Login OK, redirecting to drawer");
		
		Intent intent = new Intent(context, DrawerActivity.class);
		intent.putExtra("uid", String.valueOf(userId));
		intent.putExtra("origin", "splash");
		intent.putParcelableArrayListExtra("events", eventList);
		intent.putParcelableArrayListExtra("stories", storyList);
		intent.putParcelableArrayListExtra("shared_stories", sharedStoryList);
		intent.putParcelableArrayListExtra("contacts", contactList);
		if (storyId != null)
			intent.putExtra("story_id", storyId);
		
		intent.putExtras(getIntent());
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}
	
	/**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
            	Gen.appendLog("DrawerActivity::checkPlayServices> This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
    
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    String regid = gcm.register(MyStoriesApp.SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    Prefs.storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Gen.appendLog("DrawerActivity::registerInBackground> " + msg);
            }
        }.execute(null, null, null);
    }
	
	/***************************************************************************************
	 *
	 *                                UserLoginTask Class
	 * 
	 ***************************************************************************************/
	private class UserLoginTask extends AsyncTask<String, Void, JSONObject> {
		@Override
		protected void onPreExecute() {
			textView.setText("Login in...");
		}
		
		@Override
		protected JSONObject doInBackground(String... params) {
			return Communication.login(params[0], params[1], context);
		}

		@Override
		protected void onPostExecute(final JSONObject res) {
			if (res.opt("error_msg") != null) {
				Gen.appendError("SplashActivity$UserLoginTask::onPostExecute> Error logging : " + (String)res.opt("error_msg"));
				textView.setText((String)res.opt("error_msg"));
				redirectToLogin();
			} else {
				Gen.appendLog("SplashActivity$UserLoginTask::onPostExecute> Logging successfull");
				textView.setText("Login successful");
				userId = res.optInt("user_id", -1);
				Prefs.storeUserId(context, String.valueOf(userId));
				Prefs.storeUserFirstName(context, res.optString("first_name", "Noone"));
				if (sqlQuery.count(SQLite.TABLE_EVENTS, String.valueOf(userId)) <= 0)
					new DownloadEventsTask().execute();
				else if (sqlQuery.count(SQLite.TABLE_STORIES, String.valueOf(userId)) <= 0)
					new DownloadStoriesTask().execute();
				else
					new DownloadSharedStoriesTask().execute();
				if (contactList.size() > 0) 
					new GetContactsRegsTask().execute();
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
			//List<Event> eventList = new SQLQuery(getApplicationContext()).getEvents(String.valueOf(userId), null, null);
			//if (eventList.size() <= 0)
				return Communication.getUserEvents(String.valueOf(userId));
			//else
			//	return eventList;
		} 

		protected void onPostExecute(List<Event> result) {     
			eventList = new ArrayList<Event>();
			if(result != null) {
				eventList.addAll(result);
			}
			textView.setText("Downloading personal events... OK");
			Gen.appendLog("SplashActivity$DownloadEventsTask::onPostExecute> Nb of events downloaded : " + eventList.size());
			
			if (sqlQuery.count(SQLite.TABLE_STORIES, String.valueOf(userId)) <= 0)
				new DownloadStoriesTask().execute();
			else
				new DownloadSharedStoriesTask().execute();
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
			new DownloadSharedStoriesTask().execute();
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
        	return Contacts.getContacts(context);
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
	
	/***************************************************************************************
	 *
	 *                                GetContactsRegsTask Class
	 * 
	 ***************************************************************************************/
	private class GetContactsRegsTask extends AsyncTask<String, Integer, List<Contact>>
	{
        protected void onPreExecute() {
        	textView.setText("Retrieving MyStories contacts...");
        }

        protected List<Contact> doInBackground(String ...params) {
        	Gen.appendLog("SplashActivity$GetContactsRegsTask::doInBackground> Retrieving MyStories contacts");
        	return Communication.getRegContacts(String.valueOf(userId), contactList);
        } 

		protected void onPostExecute(List<Contact> result) {
			contactList = new ArrayList<Contact>();
			if(result != null) {
				contactList.clear(); 
				contactList.addAll(result);
			}
			textView.setText("Retrieving MyStories contacts... OK");
			Gen.appendLog("SplashActivity::GetContactsRegsTask::onPostExecute> Nb of contacts retrieved : " + contactList.size());
		}
         
		@Override
		protected void onCancelled() {
			textView.setText("Retrieving MyStories contacts... Cancelled");
		}
	}
	
	/***************************************************************************************
	 *
	 *                                DownloadSharedStoriesTask Class
	 * 
	 ***************************************************************************************/
	private class DownloadSharedStoriesTask extends AsyncTask<String, Integer, List<Story>>
	{
        protected void onPreExecute() {
        	textView.setText("Downloading shared stories...");
        } 

        protected List<Story> doInBackground(String ...params) {
        	Gen.appendLog("SplashActivity$DownloadSharedStoriesTask::doInBackground> Downloading shared stories");
        	return Communication.getSharedStories(String.valueOf(userId));
        } 

		protected void onPostExecute(List<Story> result) {
			sharedStoryList = new ArrayList<Story>();
			if(result != null) {
				sharedStoryList.addAll(result);
			}
			textView.setText("Downloading shared stories... OK");
			Gen.appendLog("SplashActivity::DownloadSharedStoriesTask::onPostExecute> Nb of shared stories downloaded : " + sharedStoryList.size());
			redirectToDrawer();
		}
         
		@Override
		protected void onCancelled() {
			textView.setText("Downloading shared stories... Cancelled");
		}
	}
	
	@Override
    protected void onDestroy() {
		sqlQuery.close();
		super.onDestroy();
	}
}

