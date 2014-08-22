package com.mjumel.mystories;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Contacts;
import com.mjumel.mystories.tools.Gen;
import com.mjumel.mystories.tools.Prefs;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
	
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;

	// UI references.
	private TextView mTitleView;
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	
	// Bundle values
	private int userId;
	private ArrayList<Event> eventList;
	private ArrayList<Story> storyList;
	private ArrayList<Story> sharedStoryList;
	private ArrayList<Contact> contactList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_login);

		// Set up the login form.
		mTitleView = (TextView) findViewById(R.id.sign_in_title);
		mTitleView.setText(Gen.bicolorSpan("storiesme",7));
		
		mEmailView = (EditText) findViewById(R.id.sign_in_email);

		mPasswordView = (EditText) findViewById(R.id.sign_in_password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.sign_in_login_form);
		mLoginStatusView = findViewById(R.id.sign_in_login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.sign_in_login_status_message);

		findViewById(R.id.sign_in_login_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
		
		findViewById(R.id.sign_in_register_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
						Bundle bundle = new Bundle();
						bundle.putString("login_email", mEmailView.getText().toString());
						bundle.putString("login_pass", mPasswordView.getText().toString());
						bundle.putAll(getIntent().getExtras());
						startActivity(intent, bundle);
					}
				});
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			//Gen.appendLog("LoginActivity::onCreate> EXTRAS");
			if (extras.containsKey("login_email"))
				mEmailView.setText(extras.getString("login_email"));
			if (extras.containsKey("login_pass"))
				mPasswordView.setText(extras.getString("login_pass"));
			if (extras.containsKey("contacts"))
				contactList = extras.getParcelableArrayList("contacts");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		//getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	
	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			if(!Communication.checkNetState(getApplicationContext())) {
				mEmailView.setError(getString(R.string.error_invalid_connection));
				mEmailView.requestFocus();
			}
			else {
				// Show a progress spinner, and kick off a background task to
				// perform the user login attempt.
				mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
				showProgress(true);
				mAuthTask = new UserLoginTask();
				mAuthTask.execute((Void) null);
			}
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	private void redirectToDrawer() {
		Gen.appendLog("LoginActivity::redirectToDrawer> Login OK, redirecting to drawer");
		
		
		Prefs.storeUserLogin(getApplicationContext(), mEmail);
		Prefs.storeUserPassword(getApplicationContext(), Gen.md5Encrypt(mPassword));
	    
	    Intent intent = new Intent(getApplicationContext(), DrawerActivity.class);
		intent.putExtra("uid", String.valueOf(userId));
		intent.putExtra("origin", "login");
		intent.putParcelableArrayListExtra("events", eventList);
		intent.putParcelableArrayListExtra("stories", storyList);
		intent.putParcelableArrayListExtra("shared_stories", sharedStoryList);
		intent.putParcelableArrayListExtra("contacts", contactList);
		intent.putExtras(getIntent());
		
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		
		startActivity(intent);
		finish();
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, JSONObject> {
		@Override
		protected JSONObject doInBackground(Void... params) {
			Gen.appendLog("LoginActivity$UserLoginTask::doInBackground> Logging in");
			return Communication.login(mEmail, Gen.md5Encrypt(mPassword), getApplicationContext());
		}

		@Override
		protected void onPostExecute(final JSONObject res) {
			if (res.opt("error_msg") != null) {
				showProgress(false);
				Gen.appendError("LoginActivity$UserLoginTask::onPostExecute> Error logging : " + (String)res.opt("error_msg"));
				if (res.optInt("error", 1) == 2) {
					mPasswordView.setError((String)res.opt("error_msg"));
					mPasswordView.requestFocus();
				} else {
					mEmailView.setError((String)res.opt("error_msg"));
					mEmailView.requestFocus();
				}
			} else {
				Gen.appendLog("LoginActivity$UserLoginTask::onPostExecute> Logging successfull");
				userId = res.optInt("user_id", -1);
				Prefs.storeUserId(getApplicationContext(), String.valueOf(userId));
				Prefs.storeUserFirstName(getApplicationContext(), res.optString("first_name", "Noone"));
				new DownloadEventsTask().execute();
				if (contactList.size() > 0) 
					new GetContactsRegsTask().execute();
				else
					new GetContactsTask().execute();
			}
			mAuthTask = null;
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
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
        	mLoginStatusMessageView.setText("Downloading personal events...");
        } 

        protected List<Event> doInBackground(String ...params) {
			Gen.appendLog("LoginActivity$DownloadEventsTask::doInBackground> Downloading events");
			return Communication.getUserEvents(String.valueOf(userId));
        } 

		protected void onPostExecute(List<Event> result) {     
			eventList = new ArrayList<Event>();
			if(result != null) {
				eventList.addAll(result);
			}
			mLoginStatusMessageView.setText("Downloading personal events... OK");
			Gen.appendLog("LoginActivity$DownloadEventsTask::onPostExecute> Nb of events downloaded : " + eventList.size());
			new DownloadStoriesTask().execute();
		}
 
		@Override
		protected void onCancelled() {
			mLoginStatusMessageView.setText("Downloading personal events... Cancelled");
			showProgress(false);
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
    	   mLoginStatusMessageView.setText("Downloading personal stories...");
       } 

       protected List<Story> doInBackground(String ...params) {
       	Gen.appendLog("LoginActivity$DownloadStoriesTask::doInBackground> Downloading stories");
       	return Communication.getUserStories(String.valueOf(userId));
       } 

		protected void onPostExecute(List<Story> result) {
			storyList = new ArrayList<Story>();
			if(result != null) {
				storyList.addAll(result);
			}
			mLoginStatusMessageView.setText("Downloading personal stories... OK");
			Gen.appendLog("LoginActivity::DownloadStoriesTask::onPostExecute> Nb of stories downloaded : " + storyList.size());
			new DownloadSharedStoriesTask().execute();
		}
        
		@Override
		protected void onCancelled() {
			mLoginStatusMessageView.setText("Downloading personal stories... Cancelled");
			showProgress(false);
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
    	   mLoginStatusMessageView.setText("Retrieving personal contacts...");
       }

       protected List<Contact> doInBackground(String ...params) {
       	Gen.appendLog("SplashActivity$GetContactsTask::doInBackground> Retrieving contacts");
       	return Contacts.getContacts(getApplicationContext());
       } 

		protected void onPostExecute(List<Contact> result) {
			contactList = new ArrayList<Contact>();
			if(result != null) {
				contactList.addAll(result);
			}
			mLoginStatusMessageView.setText("Retrieving personal contacts... OK");
			Gen.appendLog("SplashActivity::GetContactsTask::onPostExecute> Nb of contacts retrieved : " + contactList.size());
			new GetContactsRegsTask().execute();
		}
        
		@Override
		protected void onCancelled() {
			mLoginStatusMessageView.setText("Retrieving personal contacts... Cancelled");
			showProgress(false);
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
    	   mLoginStatusMessageView.setText("Retrieving MyStories contacts...");
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
			mLoginStatusMessageView.setText("Retrieving MyStories contacts... OK");
			Gen.appendLog("SplashActivity::GetContactsRegsTask::onPostExecute> Nb of contacts retrieved : " + contactList.size());
		}
        
		@Override
		protected void onCancelled() {
			mLoginStatusMessageView.setText("Retrieving MyStories contacts... Cancelled");
			showProgress(false);
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
    	   mLoginStatusMessageView.setText("Downloading shared stories...");
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
			mLoginStatusMessageView.setText("Downloading shared stories... OK");
			Gen.appendLog("SplashActivity::DownloadSharedStoriesTask::onPostExecute> Nb of shared stories downloaded : " + sharedStoryList.size());
			showProgress(false);
			redirectToDrawer();
		}
        
		@Override
		protected void onCancelled() {
			mLoginStatusMessageView.setText("Downloading shared stories... Cancelled");
			showProgress(false);
		}
	}
}
