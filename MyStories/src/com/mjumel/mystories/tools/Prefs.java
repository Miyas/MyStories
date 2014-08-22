package com.mjumel.mystories.tools;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
	
	private static final String MS_PREFS = "MyStoriesPrefs";
	private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    
    private static final String MS_PREFS_LOGIN = "MyStories_login";
    private static final String MS_PREFS_PASSWORD   = "MyStories_pwd";
    private static final String MS_PREFS_USER_ID   = "MyStories_uid";
    private static final String MS_PREFS_FIRST_NAME = "MyStories_fname";
    
    private static final String MS_PREFS_FIRST_RUN_WELCOME   = "MyStories_first_run_welcome";
    private static final String MS_PREFS_FIRST_RUN_NEW_EVENT = "MyStories_first_run_new_event";
    private static final String MS_PREFS_FIRST_RUN_NEW_STORY = "MyStories_first_run_new_story";

	
	public static String getString(Context context, String key) {
		final SharedPreferences settings = getPreferences(context);
		String sRet = settings.getString(key, null);
		return sRet;
	}
	private static void putString(Context context, String key, String value) {
		final SharedPreferences settings = getPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
	    editor.commit();
	}
	
	public static void remove(Context context, String key) {
		final SharedPreferences settings = getPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(key);
	    editor.commit();
	}
	
	public static void removeAll(Context context) {
		final SharedPreferences settings = getPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(PROPERTY_REG_ID);
		editor.remove(PROPERTY_APP_VERSION);
		editor.remove(MS_PREFS_LOGIN);
		editor.remove(MS_PREFS_PASSWORD);
		editor.remove(MS_PREFS_USER_ID);
		editor.remove(MS_PREFS_FIRST_NAME);
		editor.remove(MS_PREFS_FIRST_RUN_WELCOME);
		editor.remove(MS_PREFS_FIRST_RUN_NEW_EVENT);
		editor.remove(MS_PREFS_FIRST_RUN_NEW_STORY);
	    editor.commit();
	}
	
	public static void removeAllExceptFirstRun(Context context) {
		final SharedPreferences settings = getPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(PROPERTY_REG_ID);
		editor.remove(PROPERTY_APP_VERSION);
		editor.remove(MS_PREFS_LOGIN);
		editor.remove(MS_PREFS_PASSWORD);
		editor.remove(MS_PREFS_USER_ID);
		editor.remove(MS_PREFS_FIRST_NAME);
	    editor.commit();
	}
	
	public static void storeUserId(Context context, String userId) {
		putString(context, MS_PREFS_USER_ID, userId);
    }
	public static String getUserId(Context context) {
		return getString(context, MS_PREFS_USER_ID);
    }
	
	public static void storeUserLogin(Context context, String userLogin) {
		putString(context, MS_PREFS_LOGIN, userLogin);
    }
	public static String getUserLogin(Context context) {
		return getString(context, MS_PREFS_LOGIN);
    }
	
	public static void storeUserPassword(Context context, String userPassword) {
		putString(context, MS_PREFS_PASSWORD, userPassword);
    }
	public static String getUserPassword(Context context) {
		return getString(context, MS_PREFS_PASSWORD);
    }
	
	public static void storeUserFirstName(Context context, String userFirstName) {
		putString(context, MS_PREFS_FIRST_NAME, userFirstName);
    }
	public static String getUserFirstName(Context context) {
		return getString(context, MS_PREFS_FIRST_NAME);
    }
	
	public static void storeFirstRunWelcome(Context context, boolean isFirstRun) {
		final SharedPreferences settings = getPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(MS_PREFS_FIRST_RUN_WELCOME, isFirstRun);
	    editor.commit();
    }
	public static boolean getFirstRunWelcome(Context context) {
		final SharedPreferences settings = getPreferences(context);
		boolean sRet = settings.getBoolean(MS_PREFS_FIRST_RUN_WELCOME, true);
		return sRet;
    }
	
	public static void storeFirstRunNewEvent(Context context, boolean isFirstRun) {
		final SharedPreferences settings = getPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(MS_PREFS_FIRST_RUN_WELCOME, isFirstRun);
	    editor.commit();
    }
	public static boolean getFirstRunNewEvent(Context context) {
		final SharedPreferences settings = getPreferences(context);
		boolean sRet = settings.getBoolean(MS_PREFS_FIRST_RUN_WELCOME, true);
		return sRet;
    }
	
	public static void storeFirstRunNewStory(Context context, boolean isFirstRun) {
		final SharedPreferences settings = getPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(MS_PREFS_FIRST_RUN_WELCOME, isFirstRun);
	    editor.commit();
    }
	public static boolean getFirstRunNewStory(Context context) {
		final SharedPreferences settings = getPreferences(context);
		boolean sRet = settings.getBoolean(MS_PREFS_FIRST_RUN_WELCOME, true);
		return sRet;
    }
	
	/**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
	public static void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getPreferences(context);
        int appVersion = Gen.getAppVersion(context);
        Gen.appendLog( "Prefs::storeRegistrationId > Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    public static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Gen.appendError( "Prefs::getRegistrationId > Registration not found");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = Gen.getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Gen.appendError( "Prefs::getRegistrationId > App version changed");
            return "";
        }
        return registrationId;
    }
    
    /**
     * @return Application's {@code SharedPreferences}.
     */
    private static SharedPreferences getPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return context.getSharedPreferences(MS_PREFS,
                Context.MODE_PRIVATE);
    }

}
