package com.mjumel.mystories;

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
import android.widget.Toast;

import com.mjumel.mystories.tools.Communication;
import com.mjumel.mystories.tools.Gen;
import com.mjumel.mystories.tools.Prefs;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class RegisterActivity extends Activity {
	
	private static final String MS_PREFS_LOGIN = "MyStories_login";
	private static final String MS_PREFS_PWD = "MyStories_pwd";
	private static final String MS_PREFS_UID = "MyStories_uid";
	
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;
	private String mFirstName;
	private String mLastName;
	private String mPhone;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private EditText mFirstNameView;
	private EditText mLastNameView;
	private EditText mPhoneView;
	
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_register);

		// Set up the login form.
		mEmailView = (EditText) findViewById(R.id.register_email);
		mPasswordView = (EditText) findViewById(R.id.register_password);
		mFirstNameView = (EditText) findViewById(R.id.register_firstname);
		mLastNameView = (EditText) findViewById(R.id.register_lastname);
		mPhoneView = (EditText) findViewById(R.id.register_phone);
		mPhoneView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.register || id == EditorInfo.IME_NULL) {
							attemptRegister();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.register_form);
		mLoginStatusView = findViewById(R.id.register_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.register_status_message);

		findViewById(R.id.register_register_button).setOnClickListener (
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						//TODO
						Toast.makeText(getApplicationContext(), "TODO : Registering", Toast.LENGTH_SHORT).show();
						//attemptRegister();
					}
				});
		
		findViewById(R.id.register_sign_in_button).setOnClickListener (
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						startActivity(new Intent(getApplicationContext(), LoginActivity.class));
					}
				});
		
		String phoneNumber = Communication.getPhoneNumber(getApplicationContext());
		if (phoneNumber != "0" && phoneNumber != null)
			mPhoneView.setText(phoneNumber);
		
		if (getIntent().getExtras() != null) {
			Gen.appendLog("RegisterActivity::onCreate> EXTRASSS");
			//if (getIntent().getStringExtra("login_email") != null)
				mEmailView.setText(getIntent().getStringExtra("login_email"));
				//mEmailView.setText("TOTO");
			if (getIntent().getStringExtra("login_pass") != null)
				mPasswordView.setText(getIntent().getStringExtra("login_pass"));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return true;
	}

	
	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	private void attemptRegister() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);
		mFirstNameView.setError(null);
		mLastNameView.setError(null);
		mPhoneView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		mFirstName = mFirstNameView.getText().toString();
		mLastName = mLastNameView.getText().toString();
		mPhone = mPhoneView.getText().toString();

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
		
		// Check for a valid first name.
		if (TextUtils.isEmpty(mFirstName)) {
			mFirstNameView.setError(getString(R.string.error_field_required));
			focusView = mFirstNameView;
			cancel = true;
		}
		
		// Check for a valid last name.
		if (TextUtils.isEmpty(mLastName)) {
			mLastNameView.setError(getString(R.string.error_field_required));
			focusView = mLastNameView;
			cancel = true;
		}
		
		// Check for a valid phone number.
		if (TextUtils.isEmpty(mPhone)) {
			mPhoneView.setError(getString(R.string.error_field_required));
			focusView = mPhoneView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
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

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	private class UserLoginTask extends AsyncTask<Void, Void, Integer> {
		@Override
		protected Integer doInBackground(Void... params) {
			if(!Communication.checkNetState(getApplicationContext())) return -99;
			return Communication.register(mEmail, Gen.md5Encrypt(mPassword), mFirstName, mLastName, mPhone);
		}

		@Override
		protected void onPostExecute(final Integer uid) {
			mAuthTask = null;
			showProgress(false);

			if (uid > 0) {
				Prefs.putString(getApplicationContext(), MS_PREFS_LOGIN, mEmail);
				Prefs.putString(getApplicationContext(), MS_PREFS_PWD, Gen.md5Encrypt(mPassword));
				Prefs.putString(getApplicationContext(), MS_PREFS_UID, String.valueOf(uid));
			    
			    Intent intent = new Intent(getApplicationContext(), DrawerActivity.class);
	    		intent.putExtra("uid", String.valueOf(uid));
	    		intent.putExtra("origin", "login");
	    		intent.putExtras(getIntent());
	    		
	    		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				
				startActivity(intent);
				finish();
			    
			} else if (uid == -99) {
				mEmailView.setError(getString(R.string.error_invalid_connection));
				mEmailView.requestFocus();
			} else if (uid == -1) {
				mEmailView.setError("User already exists. Please sign in or change your login");
				mEmailView.requestFocus();
			} else {
				mEmailView.setError("Registration error. Please try again later");
				mEmailView.requestFocus();
			} 
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}
