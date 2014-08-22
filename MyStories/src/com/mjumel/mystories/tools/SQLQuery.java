package com.mjumel.mystories.tools;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.mjumel.mystories.Event;
import com.mjumel.mystories.Story;

public class SQLQuery {
	
	private static final String ASCENDANT = "asc";
	private static final String DESCENDANT = "desc";

	// Database fields
	private SQLiteDatabase database;
	private SQLite dbHelper;

	public SQLQuery(Context context) {
		dbHelper = new SQLite(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}
	
	public boolean existParam(String userId, String code) {
		return (getParam(userId, code) != null);
	}
	
	public String getParam(String userId, String code) {
		String value = null;
		Cursor cursor = database.query(SQLite.TABLE_PARAM,
				null, SQLite.COLUMN_PARAM_CD + "=" + code
				+ " and " + SQLite.COLUMN_USER_ID + "=" + userId, 
				null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			value = cursor.getString(0);
		}
		cursor.close();
		return value;
	}
	
	public void storeParam(String userId, String code, String value) {
		Gen.appendLog("SQLQuery: storeParam('"+code+"') > Start");
		
		ContentValues values = new ContentValues();
		values.put(SQLite.COLUMN_USER_ID, userId);
		values.put(SQLite.COLUMN_PARAM_CD, code);
		values.put(SQLite.COLUMN_PARAM_VALUE, value);
		
		if (existParam(userId, code)) {
			database.update(SQLite.TABLE_PARAM, values, SQLite.COLUMN_PARAM_CD + " = " + code
					+ " and " + SQLite.COLUMN_USER_ID + "=" + userId, null);
		} else {
			try {
				database.insertOrThrow(SQLite.TABLE_PARAM, null, values);
			} catch(SQLiteException e) {
				Gen.appendError("SQLQuery: storeParam> ", e);
			}
		}
		
		Gen.appendLog("SQLQuery: storeParam > End");
	}
	
	public List<Event> getEvents(String userId, String order, String lastModifiedDate) {
		List<Event> eventList = new ArrayList<Event>();
		
		String filter = SQLite.COLUMN_USER_ID + "=" + userId;
		if (lastModifiedDate != null) {
			filter += " and (" + SQLite.COLUMN_CREATION_DATE + " >= " + lastModifiedDate
					+ " or " + SQLite.COLUMN_UPDATE_DATE + " >= " + lastModifiedDate + ")";
		}
			
		if (order == null) order = ASCENDANT;
		Cursor cursor = database.query(SQLite.TABLE_EVENTS,
				null, filter, null, null, null, 
				SQLite.COLUMN_CREATION_DATE + " " + order);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Event event = cursorToEvent(cursor);
			eventList.add(event);
			cursor.moveToNext();
		}
		cursor.close();
		return eventList;
	}
	
	public List<Event> getStoryEvents(String storyId, String order) {
		List<Event> eventList = new ArrayList<Event>();
		
		if (order == null) order = ASCENDANT;
		Cursor cursor = database.rawQuery("select e.* from "
				+ SQLite.TABLE_EVENTS + " e, " + SQLite.TABLE_LINK_STO_EVT + " l "
				+ "where e." + SQLite.COLUMN_EVENT_ID + " = l." + SQLite.COLUMN_EVENT_ID
				+ "and l." + SQLite.COLUMN_STORY_ID + " = '" + storyId + "' "
				+ "order by l." + SQLite.COLUMN_CREATION_DATE + " " + order,
				null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Event event = cursorToEvent(cursor);
			eventList.add(event);
			cursor.moveToNext();
		}
		cursor.close();
		return eventList;
	}
	
	public void storeEvents(String userId, List<Event> eventList) {
		Gen.appendLog("SQLQuery: storeEvents("+eventList.size()+") > Start");
		for (Event event : eventList) 
			storeEvent(userId, event);
		Gen.appendLog("SQLQuery: storeEvents > End");
	}
	
	public void storeEvent(String userId, Event event) {
		Gen.appendLog("SQLQuery: storeEvent('"+event.getEventId()+"') > Start");
		
		ContentValues values = new ContentValues();
		values.put(SQLite.COLUMN_USER_ID, userId);
		values.put(SQLite.COLUMN_EVENT_ID, event.getEventId());
		values.put(SQLite.COLUMN_EVENT_COMMENT, event.getComment());
		values.put(SQLite.COLUMN_EVENT_PICTURE, event.getOriginalMediaPath());
		values.put(SQLite.COLUMN_EVENT_RATING, event.getRating());
		values.put(SQLite.COLUMN_CAT_ID, event.getCategoryId());
		
		try {
			database.insertOrThrow(SQLite.TABLE_EVENTS, null, values);
		} catch(SQLiteException e) {
			Gen.appendError("SQLQuery: storeEvent> ", e);
		}
		
		Gen.appendLog("SQLQuery: storeEvent > End");
	}
	
	public List<Story> getStories(String userId, String order, String lastModifiedDate) {
		List<Story> storyList = new ArrayList<Story>();
		
		String filter = SQLite.COLUMN_USER_ID + "=" + userId;
		if (lastModifiedDate != null) {
			filter += " and (" + SQLite.COLUMN_CREATION_DATE + " >= " + lastModifiedDate
					+ " or " + SQLite.COLUMN_UPDATE_DATE + " >= " + lastModifiedDate + ")";
		}
			
		if (order == null) order = ASCENDANT;
		Cursor cursor = database.query(SQLite.TABLE_STORIES,
				null, filter, null, null, null, 
				SQLite.COLUMN_CREATION_DATE + " " + order);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Story story = cursorToStory(cursor);
			story.setEvents(getStoryEvents(story.getStoryId(), DESCENDANT));
			storyList.add(story);
			cursor.moveToNext();
		}
		cursor.close();
		return storyList;
	}
	
	public void storeStories(String userId, List<Story> storyList) {
		Gen.appendLog("SQLQuery: storeStories("+storyList.size()+") > Start");
		for (Story story : storyList) 
			storeStory(userId, story);
		Gen.appendLog("SQLQuery: storeStories > End");
	}
	
	public void storeStory(String userId, Story story) {
		Gen.appendLog("SQLQuery: storeStory('"+story.getStoryId()+"') > Start");
		
		ContentValues values = new ContentValues();
		values.put(SQLite.COLUMN_USER_ID, userId);
		values.put(SQLite.COLUMN_STORY_ID, story.getStoryId());
		values.put(SQLite.COLUMN_STORY_TITLE, story.getTitle());
		
		try {
			database.insertOrThrow(SQLite.TABLE_STORIES, null, values);
		} catch(SQLiteException e) {
			Gen.appendError("SQLQuery: storeStory> ", e);
		}
		
		for(Event event : story.getEvents())
			storeStoryEventLink(story.getStoryId(), event.getEventId());
		
		Gen.appendLog("SQLQuery: storeStory > End");
	}
	
	public void storeStoryEventLink(String storyId, String eventId) {
		Gen.appendLog("SQLQuery: storeStoryEventLink('"+storyId+"','"+eventId+"') > Start");
		
		ContentValues values = new ContentValues();
		values.put(SQLite.COLUMN_STORY_ID, storyId);
		values.put(SQLite.COLUMN_EVENT_ID, eventId);
		
		try {
			database.insertOrThrow(SQLite.TABLE_LINK_STO_EVT, null, values);
		} catch(SQLiteException e) {
			Gen.appendError("SQLQuery: storeStoryEventLink> ", e);
		}
		
		Gen.appendLog("SQLQuery: storeStoryEventLink > End");
	}

/*
	public void deleteNodes(List<Node> nodes) {
		Gen.appendLog("DataSource: deleteNodes() > Start");
		for(Node node:nodes) deleteNode(node.GetId());
		Gen.appendLog("DataSource: deleteNodes() > End");
	}
	
	public void deleteNode(Node node) {
		Gen.appendLog("DataSource: deleteNode('"+node.GetName()+"') > Start");
		deleteNode(node.GetId());
		Gen.appendLog("DataSource: deleteNode('"+node.GetName()+"') > End");
	}
	public void deleteNode(long id) {
		Gen.appendLog("DataSource: deleteNode("+id+") > Start");
		
		// Deleting sons beforehand
		deleteNodes(getNodes(id));
		// Deleting node
		System.out.println("Node deleted with id: " + id);
		database.delete(SQLite.TABLE_STORIES, SQLite.COLUMN_ID
				+ " = " + id, null);
		
		Gen.appendLog("DataSource: deleteNode("+id+") > End");
	}
	
	public List<Node> getNodes(long parentId) {
		Gen.appendLog("DataSource: getNodes('"+parentId+"') > Start");
		List<Node> nodes = getNodes(SQLite.COLUMN_PARENT_ID + " = " + parentId);
		Gen.appendLog("DataSource: getNodes('"+parentId+"') > End");
		return nodes;
	}
	public List<Node> getNodes(String filter) {
		Gen.appendLog("DataSource: getNodes('"+filter+"') > Start");
		
		List<Node> nodes = new ArrayList<Node>();
		Cursor cursor = database.query(SQLite.TABLE_STORIES,
				allColumns, 
				filter,
				null, null, null, SQLite.COLUMN_DATE + " desc");
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Node node = cursorToNode(cursor);
			nodes.add(node);
			cursor.moveToNext();
		}
		cursor.close();
		
		Gen.appendLog("DataSource: getNodes('"+filter+"') > End");
		return nodes;
	}
	
	
	public void insertOrUpdateNode(Node node) {
		Gen.appendLog("DataSource: insertOrUpdateNode('"+node.GetName()+"') > Start");
		
		long nodeId;
		Cursor cursor = database.query(SQLite.TABLE_STORIES,
				allColumns, 
				SQLite.COLUMN_TITLE + " = '" + node.GetName() + "'" +
				" and " + SQLite.COLUMN_PARENT_ID + " = " + node.GetParentId(),
				null,
				null, 
				null, 
				null);
		if(!cursor.moveToFirst())
			nodeId = createNode(node);
		else
		{
			updateNode(node);
			nodeId = cursor.getLong(0);
		}
		cursor.close();
		node.SetId(nodeId);
		
		Gen.appendLog("DataSource: insertOrUpdateNode('"+node.GetName()+"') > End");
	}
	
	public long existsNode(Node node) {
		Gen.appendLog("DataSource: existsNode('"+node.GetName()+"') > Start");
		
		long id = -1;
		Cursor cursor;
		
		if (node.GetSize() < 0)
		{
			cursor = database.query(SQLite.TABLE_STORIES,
				allColumns, 
				SQLite.COLUMN_TITLE + " = '" + node.GetName() + "'" +
				" and " + SQLite.COLUMN_PARENT_ID + " = " + node.GetParentId(),
				null,
				null, 
				null, 
				null);
		}
		else
		{
			cursor = database.query(SQLite.TABLE_STORIES,
				allColumns, 
				SQLite.COLUMN_TITLE + " = '" + node.GetName() + "'" +
				" and " + SQLite.COLUMN_DATE + " = '" + node.GetDate() + "'" +
				" and " + SQLite.COLUMN_SIZE + " = " + node.GetSize(),
				null,
				null, 
				null, 
				null);
		}
		
		if(cursor.moveToFirst())
			id = cursor.getLong(0);
		cursor.close();
		
		Gen.appendLog("DataSource: existsNode('"+node.GetName()+"') > End");
		return id;
	}

	public void updateNode(Node node) { updateNode(node, false); }
	public void updateNode(Node node, boolean userChange) {
		Gen.appendLog("DataSource: updateNode('"+node.GetName()+"') > Start");
		ContentValues cv = new ContentValues();
		cv.put(SQLite.COLUMN_PARENT_ID, node.GetParentId());
		cv.put(SQLite.COLUMN_TITLE, node.GetName());
		cv.put(SQLite.COLUMN_AUTHOR, node.GetAuthor());
		cv.put(SQLite.COLUMN_SIZE, node.GetSize());
		cv.put(SQLite.COLUMN_DATE, node.GetDate());
		cv.put(SQLite.COLUMN_URL, node.GetUrl());
		cv.put(SQLite.COLUMN_TEXT, node.GetText());
		
		if (userChange)
		{
			cv.put(SQLite.COLUMN_FAV, node.GetFav());
			cv.put(SQLite.COLUMN_BOOK, node.GetBookmark());
			cv.put(SQLite.COLUMN_RATE, node.GetRating());
		}
		
		database.update(SQLite.TABLE_STORIES, cv, SQLite.COLUMN_ID + " = " + node.GetId(), null);
		Gen.appendLog("DataSource: updateNode('"+node.GetName()+"') > End");
	}
	*/
	
	public int count(String table, String userId) {
		Gen.appendLog("SQLQuery: count('"+table+"') > Start");
		
		String filter = "";
		if (userId != null) filter = " where " + SQLite.COLUMN_USER_ID + "=" + userId;
		Cursor mCount = database.rawQuery("select count(1) from " + table + filter, null);
		mCount.moveToFirst();
		int count = mCount.getInt(0);
		mCount.close();
		Gen.appendLog("SQLQuery: count('"+table+"') > End (Count = "+ count +")");
		return count;
	}
		
	private Event cursorToEvent(Cursor cursor) {
		Gen.appendLog("SQLQuery: cursorToEvent('"+cursor.getString(1)+"') > Start");
		//Gen.appendLog("SQLQuery: cursorToEvent > " + DatabaseUtils.dumpCursorToString(cursor));
		
		if (cursor.getCount() == 0)
			return null;
		
		Event event = new Event();
		event.setUserId(cursor.getString(0));
		event.setEventId(cursor.getString(1));
		event.setComment(cursor.getString(2));
		event.setOriginalMediaPath(cursor.getString(3));
		event.setOriginalMediaPath(cursor.getString(3));
		event.setOriginalMediaPath(cursor.getString(3));
		event.setRating(cursor.getInt(4));
		event.setCategoryId(cursor.getInt(5));
		
		Gen.appendLog("SQLQuery: cursorToEvent('"+cursor.getString(1)+"') > End");
		return event;
	}
	
	private Story cursorToStory(Cursor cursor) {
		Gen.appendLog("SQLQuery: cursorToStory('"+cursor.getString(1)+"') > Start");
		//Gen.appendLog("SQLQuery: cursorToStory > " + DatabaseUtils.dumpCursorToString(cursor));
		
		if (cursor.getCount() == 0)
			return null;
		
		Story story = new Story();
		story.setUserId(cursor.getString(0));
		story.setStoryId(cursor.getString(1));
		story.setTitle(cursor.getString(2));
		
		Gen.appendLog("SQLQuery: cursorToStory('"+cursor.getString(1)+"') > End");
		return story;
	}

}