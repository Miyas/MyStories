package com.mjumel.mystories.tools;

import android.content.Context;
import android.content.SharedPreferences;

public class Contacts {
	
	private static final String MS_PREFS = "MyStoriesPrefs";
	
	public static String getString(Context context, String key) {
		SharedPreferences settings = context.getSharedPreferences(MS_PREFS, 0);
		String sRet = settings.getString(key, null);
		settings = null;
		return sRet;
	}
	
	public static void putString(Context context, String key, String value) {
		SharedPreferences settings = context.getSharedPreferences(MS_PREFS, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
	    editor.commit();
	    editor = null;
	    settings = null;
	}
	
	public static void remove(Context context, String key) {
		SharedPreferences settings = context.getSharedPreferences(MS_PREFS, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(key);
	    editor.commit();
	    editor = null;
	    settings = null;
	}
}
