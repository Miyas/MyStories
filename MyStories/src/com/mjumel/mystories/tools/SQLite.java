package com.mjumel.mystories.tools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLite extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "storiesme.db";
	private static final int DATABASE_VERSION = 3;

	public static final String TABLE_PARAM = "param";
	public static final String COLUMN_USER_ID = "user_id";
	public static final String COLUMN_PARAM_CD = "code";
	public static final String COLUMN_PARAM_VALUE = "value";
	
	public static final String TABLE_CONTACT = "contact";
	public static final String COLUMN_PHONE_ID = "phone_id";
	public static final String COLUMN_APP_ID = "app_id";
	public static final String COLUMN_REG_ID = "reg_id";
	public static final String COLUMN_FIRSTNAME = "first_name";
	public static final String COLUMN_PHONE = "phone";
	public static final String COLUMN_MAIL = "mail";
	
	public static final String TABLE_STORIES = "stories";
	public static final String COLUMN_STORY_ID = "story_id";
	public static final String COLUMN_STORY_TITLE = "title";
	
	public static final String TABLE_EVENTS = "events";
	public static final String COLUMN_EVENT_ID = "event_id";
	public static final String COLUMN_EVENT_COMMENT = "comment";
	public static final String COLUMN_EVENT_PICTURE = "picture";
	public static final String COLUMN_EVENT_RATING = "rating";
	
	public static final String TABLE_LINK_STO_EVT = "link";
	
	public static final String TABLE_CATS = "cats";
	public static final String COLUMN_CAT_ID = "cat_id";
	public static final String COLUMN_CAT_TYPE = "type";
	public static final String COLUMN_CAT_NAME = "name";
	public static final String COLUMN_CREATION_DATE = "dt_creation";
	public static final String COLUMN_UPDATE_DATE = "dt_update";

	private static final String DROP_TABLE_PARAM = "DROP TABLE IF EXISTS " + TABLE_PARAM;
	private static final String CREATE_TABLE_PARAM =			
			"create table " + TABLE_PARAM + "( " 
			+ COLUMN_USER_ID + " text not null, "
			+ COLUMN_PARAM_CD + " text not null, "
			+ COLUMN_PARAM_VALUE + " text not null)";
	
	private static final String DROP_TABLE_CONTACT = "DROP TABLE IF EXISTS " + TABLE_CONTACT;
	private static final String CREATE_TABLE_CONTACT =
			"create table " + TABLE_CONTACT + "( " 
			+ COLUMN_USER_ID + " text not null, "
			+ COLUMN_PHONE_ID + " text not null, "
			+ COLUMN_APP_ID + " text null, "
			+ COLUMN_REG_ID + " text null, "
			+ COLUMN_FIRSTNAME + " text null, "
			+ COLUMN_PHONE + " text null, "
			+ COLUMN_MAIL + " text null, "
			+ COLUMN_CREATION_DATE + " text not null default CURRENT_TIMESTAMP, "
			+ COLUMN_UPDATE_DATE + " text null)";

	private static final String DROP_TABLE_STORIES = "DROP TABLE IF EXISTS " + TABLE_STORIES;
	private static final String CREATE_TABLE_STORIES =
			"create table " + TABLE_STORIES + "( " 
			+ COLUMN_USER_ID + " text not null, "
			+ COLUMN_STORY_ID + " text not null, "
			+ COLUMN_STORY_TITLE + " text null, "
			+ COLUMN_CREATION_DATE + " text not null default CURRENT_TIMESTAMP, "
			+ COLUMN_UPDATE_DATE + " text null)";
			
	private static final String DROP_TABLE_EVENTS = "DROP TABLE IF EXISTS " + TABLE_EVENTS;
	private static final String CREATE_TABLE_EVENTS =
			"create table " + TABLE_EVENTS + "( " 
			+ COLUMN_USER_ID + " text not null, "
			+ COLUMN_EVENT_ID + " text not null, "
			+ COLUMN_EVENT_COMMENT + " text null, "
			+ COLUMN_EVENT_PICTURE + " text null, "
			+ COLUMN_EVENT_RATING + " integer null, "
			+ COLUMN_CAT_ID + " integer null, "
			+ COLUMN_CREATION_DATE + " text not null default CURRENT_TIMESTAMP, "
			+ COLUMN_UPDATE_DATE + " text null)";
			
	private static final String DROP_TABLE_LINK = "DROP TABLE IF EXISTS " + TABLE_LINK_STO_EVT;
	private static final String CREATE_TABLE_LINK =
			"create table " + TABLE_LINK_STO_EVT + "( " 
			+ COLUMN_STORY_ID + " text not null, "
			+ COLUMN_EVENT_ID + " text not null, "
			+ COLUMN_CREATION_DATE + " text not null default CURRENT_TIMESTAMP)";
			
	private static final String DROP_TABLE_CATS = "DROP TABLE IF EXISTS " + TABLE_CATS;
	private static final String CREATE_TABLE_CATS =
			"create table " + TABLE_CATS + "( " 
			+ COLUMN_CAT_ID + " integer not null, "
			+ COLUMN_USER_ID + " text not null, "
			+ COLUMN_CAT_TYPE + " text null, "
			+ COLUMN_CAT_NAME + " text null, "
			+ COLUMN_CREATION_DATE + " text not null default CURRENT_TIMESTAMP, "
			+ COLUMN_UPDATE_DATE + " text null)";

	public SQLite(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		Gen.appendLog("SQLite::Constructor> Ending");
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE_PARAM);
		database.execSQL(CREATE_TABLE_CONTACT);
		database.execSQL(CREATE_TABLE_STORIES);
		database.execSQL(CREATE_TABLE_EVENTS);
		database.execSQL(CREATE_TABLE_LINK);
		database.execSQL(CREATE_TABLE_CATS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Gen.appendLog("SQLite::onUpgrade> Upgrading database from version " + oldVersion + " to "
						+ newVersion);
		database.execSQL(DROP_TABLE_PARAM);
		database.execSQL(DROP_TABLE_CONTACT);
		database.execSQL(DROP_TABLE_STORIES);
		database.execSQL(DROP_TABLE_EVENTS);
		database.execSQL(DROP_TABLE_LINK);
		database.execSQL(DROP_TABLE_CATS);
		onCreate(database);
	}

}