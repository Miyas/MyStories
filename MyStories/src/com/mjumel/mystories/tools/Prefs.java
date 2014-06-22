package com.mjumel.mystories.tools;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
	
	private static final String MS_PREFS = "MyStoriesPrefs";
	private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

	
	public static String getString(Context context, String key) {
		SharedPreferences settings = getPreferences(context);
		String sRet = settings.getString(key, null);
		settings = null;
		return sRet;
	}
	
	public static void putString(Context context, String key, String value) {
		SharedPreferences settings = getPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
	    editor.commit();
	    editor = null;
	    settings = null;
	}
	
	public static void remove(Context context, String key) {
		SharedPreferences settings = getPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(key);
	    editor.commit();
	    editor = null;
	    settings = null;
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
        Gen.appendLog( "Prefs::getRegistrationId > Saving regId on app version " + appVersion);
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
