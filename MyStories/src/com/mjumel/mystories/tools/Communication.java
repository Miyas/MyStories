package com.mjumel.mystories.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.NetworkOnMainThreadException;
import android.telephony.TelephonyManager;

import com.mjumel.mystories.Contact;
import com.mjumel.mystories.Event;
import com.mjumel.mystories.Story;

public class Communication {
	
	private static final String AUTH_URI = "http://anizoo.info/mystories/include/auth.php";
	private static final String POST_URI = "http://anizoo.info/mystories/post/userevents.php";

	private static final String ACTION_AUTH_LOGIN = "1";
	private static final String ACTION_AUTH_REGISTER = "3";
	
	private static final String ACTION_POST_EVENT_GET_ALL = "1";
	private static final String ACTION_POST_STORY_NEW = "2";
	private static final String ACTION_POST_STORY_EDIT = "3";
	private static final String ACTION_POST_STORY_DELETE = "4";
	private static final String ACTION_POST_STORY_GET_ALL = "5";
	private static final String ACTION_POST_EVENT_NEW_OR_EDIT = "6";
	private static final String ACTION_POST_EVENT_DELETE = "7";
	private static final String ACTION_POST_EVENT_LINK_TO_STORY = "8";
	private static final String ACTION_POST_REGISTRATION_ID = "9";
	private static final String ACTION_POST_STORY_SHARE = "10";
	private static final String ACTION_POST_CONTACTS = "11";
	private static final String ACTION_POST_STORY_GET_SHARED = "12";
	private static final String ACTION_POST_REMOVE_EVENT_LINK_TO_STORY = "13";
	
	
	public static JSONObject login(String login, String pwd, Context c) 
	{
		Gen.appendLog("Communication::login> Starting");
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", ACTION_AUTH_LOGIN));
        params.add(new BasicNameValuePair("l", login));
        params.add(new BasicNameValuePair("p", pwd));
        params.add(new BasicNameValuePair("phone", getPhoneNumber(c)));
        
		/*
		String[] res = postText(AUTH_URI, params);
		if (res[0].equals("200"))
		{
        	String[] message = res[1].split(":");
        	try {
				return Integer.parseInt(message[1]);
			} catch(NumberFormatException e) {
				Gen.appendError("Communication::login> ", e);
       		}
        }
        return -1;
        */
		
        JSONObject json = postTextgetJSON(AUTH_URI, params);
        Gen.appendLog("Communication::login> Json : " + json.toString());
        
		return json;
	}
	
	public static int register(String login, String pwd, String fname, String lname, String phone) 
	{
		Gen.appendLog("Communication::register> Starting");
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", ACTION_AUTH_REGISTER));
        params.add(new BasicNameValuePair("l", login));
        params.add(new BasicNameValuePair("p", pwd));
        params.add(new BasicNameValuePair("fname", fname));
        params.add(new BasicNameValuePair("lname", lname));
        params.add(new BasicNameValuePair("phone", phone));
        
		String[] res = postText(AUTH_URI, params);
		if (res[0].equals("200"))
		{
        	String[] message = res[1].split(":");
        	try {
				return Integer.parseInt(message[1]);
			} catch(NumberFormatException e) {
				Gen.appendError("Communication::register> ", e);
				return -2;
       		}

        }
       	return -2;
	}
	
	public static JSONObject newEvent(String userid, String comment, int rating, String mediaUri, int cat) {
		return postEvent(userid, null, comment, rating, mediaUri, cat);
	}
	public static JSONObject editEvent(String userid, String eventId, String comment, int rating, String mediaUri, int cat) {
		return postEvent(userid, eventId, comment, rating, mediaUri, cat);
	}
	
	
	public static boolean deleteEvents(String sUserId, List<Event> events) 
	{
		Gen.appendLog("Communication::deleteEvents> Starting");
		
		String sEvents = eventsToString(events, true);
		Gen.appendLog("Communication::deleteEvents> sEvents = " + sEvents);
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", ACTION_POST_EVENT_DELETE));
        params.add(new BasicNameValuePair("ui", sUserId));
        params.add(new BasicNameValuePair("ids", sEvents));
        
        String[] res = postText(POST_URI, params);
		if (res[0].equals("200"))
		{
			//Gen.appendLog("Communication::deleteEvents> Response = \n" + res[1] + "\n");
			try {
				return (Integer.parseInt(res[1]) == sEvents.split(";").length);
			} catch(NumberFormatException e) {
				Gen.appendError("Communication::deleteEvents> ", e);
       		}
        }
        return false;
	}
	
	public static boolean deleteEvent(String sUserId, Event event) 
	{
		Gen.appendLog("Communication::deleteEvent> Starting");
		
		List<Event> events = new ArrayList<Event>();
		events.add(event);
		return deleteEvents(sUserId, events);
	}
	
	public static List<Event> getUserEvents(String userId)//, String userSession) 
	{
		Gen.appendLog("Communication::getUserEvents> Starting");
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", ACTION_POST_EVENT_GET_ALL));
        params.add(new BasicNameValuePair("ui", userId));
        //params.add(new BasicNameValuePair("us", userSession));
        
        String[] res = postText(POST_URI, params);
		if (res[0].equals("200"))
		{
			//Gen.appendLog("Communication::getUserEvents> Response = \n" + res[1] + "\n");
       		try {
				return (new XmlParser()).parseEvents((Reader)new StringReader(res[1]));
			} catch (XmlPullParserException e) {
				Gen.appendError("Communication::getUserEvents> ", e);
			} catch (IOException e) {
				Gen.appendError("Communication::getUserEvents> ", e);
			}
        }
        return null;
	}
	
	/**
	 * Get list of detailed user stories
	 *
	 * @param userId The user ID.
	 * @author maxime.jumel
	 */
	public static List<Story> getUserStories(String userId) {
		return getUserStories(userId, false);
	}
	
	/**
	 * Get list of user stories
	 *
	 * @param userId The user ID.
	 * @param light Indicates if you want to get the complete details of stories or only the light version (title + id).
	 * @author maxime.jumel
	 */
	public static List<Story> getUserStories(String userId, boolean light)//, String userSession) 
	{
		Gen.appendLog("Communication::getUserStories> Starting");
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", ACTION_POST_STORY_GET_ALL));
        params.add(new BasicNameValuePair("ui", userId));
        params.add(new BasicNameValuePair("light", (light?"1":"0")));
        //params.add(new BasicNameValuePair("us", userSession));
        
        String[] res = postText(POST_URI, params);
		if (res[0].equals("200"))
		{
			//Gen.appendLog("Communication::getUserStories> Response = \n" + res[1] + "\n");
       		try {
				return (new XmlParser()).parseStories((Reader)new StringReader(res[1]));
			} catch (XmlPullParserException e) {
				Gen.appendError("Communication::getUserStories> ", e);
			} catch (IOException e) {
				Gen.appendError("Communication::getUserStories> ", e);
			}
        }
        return null;
	}
	
	/**
	 * Get list of the stories other users have shared for us
	 *
	 * @param userId The user ID.
	 * @author maxime.jumel
	 */
	public static List<Story> getSharedStories(String userId)//, String userSession) 
	{
		List<Story> storyList = new ArrayList<Story>();
		Gen.appendLog("Communication::getSharedStories> Starting");
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", ACTION_POST_STORY_GET_SHARED));
        params.add(new BasicNameValuePair("ui", userId));
        
        JSONObject json = postTextgetJSON(POST_URI, params);
        Gen.appendLog("Communication::getSharedStories> json = " + json.toString());
        
        JSONArray jsonStories;
		try {
			jsonStories = json.getJSONArray("stories");
		} catch (JSONException e) {
			Gen.appendError("Communication::getSharedStories> ", e);
			return null;
		}
        
        for (int s = 0; s < jsonStories.length() ; s++) {
			try {
				JSONObject jsonStory = jsonStories.getJSONObject(s);
				Story story = new Story(jsonStory.getString("story_id"),
						jsonStory.getString("title"),
						userId);
				
				JSONArray jsonEvents = jsonStory.getJSONArray("events");
				List<Event> eventList = new ArrayList<Event>();
				for (int e = 0; e < jsonEvents.length() ; e++) {
					JSONObject jsonEvent = jsonEvents.getJSONObject(e);
					Event event = new Event(
							jsonEvent.optString("comment"),
							jsonEvent.optInt("rating"),
							jsonEvent.optInt("category"),
							jsonEvent.optString("path_thumb"),
							jsonEvent.optString("path_resized"),
							jsonEvent.optString("path_original"),
							userId,
							jsonStory.getString("story_id"),
							jsonEvent.optString("event_id")
							);
					eventList.add(event);
				}
				
				story.setEvents(eventList);
				storyList.add(story);
			} catch (JSONException e) {
				Gen.appendError("Communication::getSharedStories> ", e);
			}
		}
        
        return storyList;
	}	
	
	public static int addStory(Story story) 
	{
		Gen.appendLog("Communication::addStory> Starting");
		
		String sEvents = eventsToString(story.getEvents(), false);
			
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", ACTION_POST_STORY_NEW));
        params.add(new BasicNameValuePair("ui", story.getUserId()));
        params.add(new BasicNameValuePair("title", story.getTitle()));
        params.add(new BasicNameValuePair("events", sEvents));
        
        String[] res = postText(POST_URI, params);
		if (res[0].equals("200"))
		{
			//Gen.appendLog("Communication::addStory> Response = \n" + res[1] + "\n");
			try {
				return Integer.parseInt(res[1]);
			} catch(NumberFormatException e) {
				Gen.appendError("Communication::addStory> ", e);
       		}
        }
        return -1;
	}
	
	public static int editStory(Story story) 
	{
		Gen.appendLog("Communication::editStory> Starting");
		
		String sEvents = eventsToString(story.getEvents(), false);
			
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", ACTION_POST_STORY_EDIT));
        params.add(new BasicNameValuePair("ui", story.getUserId()));
        params.add(new BasicNameValuePair("sid", story.getStoryId()));
        params.add(new BasicNameValuePair("title", story.getTitle()));
        params.add(new BasicNameValuePair("events", sEvents));
        
        String[] res = postText(POST_URI, params);
		if (res[0].equals("200"))
		{
			//Gen.appendLog("Communication::addStory> Response = \n" + res[1] + "\n");
			try {
				return Integer.parseInt(res[1]);
			} catch(NumberFormatException e) {
				Gen.appendError("Communication::editStory> ", e);
       		}
        }
        return -1;
	}
	
	public static boolean deleteStories(String sUserId, List<Story> stories) 
	{
		Gen.appendLog("Communication::deleteStories> Starting");
		
		String sStories = storiesToString(stories, true);
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", ACTION_POST_STORY_DELETE));
        params.add(new BasicNameValuePair("ui", sUserId));
        params.add(new BasicNameValuePair("ids", sStories));
        
        String[] res = postText(POST_URI, params);
		if (res[0].equals("200"))
		{
			//Gen.appendLog("Communication::deleteStories> Response = \n" + res[1] + "\n");
			try {
				return (Integer.parseInt(res[1]) == sStories.split(";").length);
			} catch(NumberFormatException e) {
				Gen.appendError("Communication::deleteStories> ", e);
       		}
        }
        return false;
	}
	
	public static boolean deleteStory(String sUserId, Story story) 
	{
		Gen.appendLog("Communication::deleteStory> Starting");
		
		List<Story> stories = new ArrayList<Story>();
		stories.add(story);
		return deleteStories(sUserId, stories);
	}
	
	public static boolean linkEvent(String sUserId, Event event, List<Story> stories) 
	{
		List<Event> events = new ArrayList<Event>();
		events.add(event);
		return linkEvents(sUserId, events, stories);
	}
	
	public static boolean linkEvents(String sUserId, List<Event> events, List<Story> stories) 
	{
		Gen.appendLog("Communication::linkEvents> Starting");
		
		String sEvents = eventsToString(events, true);
		String sStories = storiesToString(stories, true);
		
		Gen.appendLog("Communication::linkEvents> sEvents = " + sEvents);
		Gen.appendLog("Communication::linkEvents> sStories = " + sStories);
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", ACTION_POST_EVENT_LINK_TO_STORY));
        params.add(new BasicNameValuePair("ui", sUserId));
        params.add(new BasicNameValuePair("events", sEvents));
        params.add(new BasicNameValuePair("stories", sStories));
        
        String[] res = postText(POST_URI, params);
		if (res[0].equals("200"))
		{
			//Gen.appendLog("Communication::linkEvents> Response = \n" + res[1] + "\n");
			try {
				return (Integer.parseInt(res[1]) == 1);
			} catch(NumberFormatException e) {
				Gen.appendError("Communication::linkEvents> ", e);
       		}
        }
        return false;
	}
	
	public static JSONObject removeLink(String sUserId, Event event, Story story) 
	{
		Gen.appendLog("Communication::removeLink> Starting");
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", ACTION_POST_REMOVE_EVENT_LINK_TO_STORY));
        params.add(new BasicNameValuePair("ui", sUserId));
        params.add(new BasicNameValuePair("event", event.getEventId()));
        params.add(new BasicNameValuePair("story", story.getStoryId()));
        
        JSONObject json = postTextgetJSON(POST_URI, params);
		return json;
	}
	
	public static boolean sendRegId(String sUserId, String regId)
	{
		Gen.appendLog("Communication::sendRegId> Starting");
		
		Gen.appendLog("Communication::sendRegId> sUserId = " + sUserId);
		Gen.appendLog("Communication::sendRegId> regId = " + regId);
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", ACTION_POST_REGISTRATION_ID));
        params.add(new BasicNameValuePair("ui", sUserId));
        params.add(new BasicNameValuePair("regid", regId));
        
        String[] res = postText(POST_URI, params);
		if (res[0].equals("200"))
			return true;
        return false;
	}
	
	public static boolean shareStory(String sUserId, HashSet<String> to, Story story)	{
		Gen.appendLog("Communication::shareStory> Starting");
		
		Gen.appendLog("Communication::shareStory> sUserId = " + sUserId);
		Gen.appendLog("Communication::shareStory> to = " + to);
		Gen.appendLog("Communication::shareStory> story = " + story.getStoryId());
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", ACTION_POST_STORY_SHARE));
        params.add(new BasicNameValuePair("ui", sUserId));
        params.add(new BasicNameValuePair("to", (new JSONArray(to).toString())));
        params.add(new BasicNameValuePair("sid", story.getStoryId()));
        
        JSONObject json = postTextgetJSON(POST_URI, params);
        Gen.appendLog("Communication::shareStory> json = " + json.toString());
        if (json.optInt("success", -1) >= 1 && json.optInt("failure", 0) <= 0)
        	return true;
        
        return false;
	}
	
	/**
	 * Get list of registered matching contacts
	 * 
	 *
	 * @author maxime.jumel
	 */
	public static List<Contact> getRegContacts(String sUserId, List<Contact> contacts) 
	{
		Gen.appendLog("Communication::getRegContacts> Starting");
		
		String contactsXml = XmlParser.constructContactsXml(contacts);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", ACTION_POST_CONTACTS));
        params.add(new BasicNameValuePair("ui", sUserId));
        params.add(new BasicNameValuePair("contacts", contactsXml));
        
        JSONObject json = postTextgetJSON(POST_URI, params);
        try {
			contacts = Contacts.updateRegIds(contacts, json.getJSONArray("regs"));
		} catch (JSONException e) {
			Gen.appendError("Communication::getRegContacts> ", e);
		}
        
        return contacts;
	}
	
	private static String[] postText(String uri, List<NameValuePair> params)
	{
		Gen.appendLog("Communication::postText> Starting for uri = " + uri);
		
		int serverResponseCode = 0;
		String serverRespondeMsg = "";
		String[] result = new String[2];

		try
	    {
	        HttpClient client = new DefaultHttpClient();
	        HttpPost post = new HttpPost(uri);
	        post.setEntity(new UrlEncodedFormEntity(params));
	        HttpResponse response = client.execute(post);
	        serverResponseCode = response.getStatusLine().getStatusCode();
	        Gen.appendLog("Communication::postText> Response Code : " + serverResponseCode);
	        
        	HttpEntity httpEntity = response.getEntity();
        	serverRespondeMsg = EntityUtils.toString(httpEntity);
	    }
		catch(ClientProtocolException e)
		{
			Gen.appendError("Communication::postText> ", e);
            serverRespondeMsg = e.getMessage();
		}
		catch(IOException e)
		{
			Gen.appendError("Communication::postText> ", e);
            serverRespondeMsg = e.getMessage();
		}
		catch(NetworkOnMainThreadException e)
		{
			Gen.appendError("Communication::postText> ", e);
            serverRespondeMsg = e.getMessage();
		}
	    catch(Exception e)
	    {
	    	Gen.appendError("Communication::postText> ", e);
            serverRespondeMsg = e.getMessage();
	    }
		
		result[0] = String.valueOf(serverResponseCode);
		result[1] = serverRespondeMsg;
		
		Gen.appendLog("Communication::postText> Ending");
		return result;
	}
	
	private static JSONObject postEvent(String userid, String eventId, String comment, int rating, String mediaUri, int cat) 
	{
		Gen.appendLog("Communication::postEvent> Starting");
		int serverResponseCode = 0;
		InputStream is = null;
	    JSONObject jObj = null;
	    String json = "";
		
		try
	    {
	        HttpClient client = new DefaultHttpClient();
	        HttpPost post = new HttpPost(POST_URI);
	
	        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
	        entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
	        entityBuilder.addTextBody("action", ACTION_POST_EVENT_NEW_OR_EDIT);
	        entityBuilder.addTextBody("ui", userid);
	        entityBuilder.addTextBody("comment", Uri.encode(comment));
	        entityBuilder.addTextBody("rating", String.valueOf(rating));
	        entityBuilder.addTextBody("cat", String.valueOf(cat));
	
	        if(eventId != null) {
	        	entityBuilder.addTextBody("eid", eventId);
	        }
	        
	        if(mediaUri != null) {
	        	if(mediaUri.compareTo("nochange") == 0) {
		        	entityBuilder.addTextBody("file", mediaUri);
	        	} else {
	        		File mediaFile = new File(mediaUri);
	            	entityBuilder.addBinaryBody("uploaded_file", mediaFile);
	        	}
	        }
	
	        post.setEntity(entityBuilder.build());
	
	        HttpResponse response = client.execute(post);
	        serverResponseCode = response.getStatusLine().getStatusCode();
	        Gen.appendLog("Communication::postEvent> Response Code : " + serverResponseCode);
	
	        HttpEntity httpEntity = response.getEntity();
	        is = httpEntity.getContent();
	    }
		catch(NumberFormatException e) {
			Gen.appendError("Communication::postEvent> ", e);
		} catch(Exception e) {
			Gen.appendError("Communication::postEvent> ", e);
	    }
		
		try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Gen.appendError("Communication::postEvent> ", e);
        }
 
        try {
            jObj = new JSONObject(json);
        } catch (Exception e) {
            Gen.appendError("Communication::postEvent> ", e);
            Gen.appendError("Communication::postEvent> json : " + json);
        }
		
		Gen.appendLog("Communication::postEvent> Ending");
        return jObj;
	}
	
	private static JSONObject postTextgetJSON(String url, List<NameValuePair> params) {
		
		InputStream is = null;
	    JSONObject jObj = null;
	    String json = "";
	    
	    Gen.appendLog("Communication::postTextgetJSON> Starting");
		 
        // Making HTTP request
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(params));
 
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();
        } catch (Exception e) {
        	Gen.appendError("Communication::postTextgetJSON> ", e);
        }
 
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Gen.appendError("Communication::postTextgetJSON> ", e);
        }
 
        try {
            jObj = new JSONObject(json);
        } catch (Exception e) {
            Gen.appendError("Communication::postTextgetJSON> ", e);
            Gen.appendError("Communication::postTextgetJSON> json : " + json);
        }
        
        Gen.appendLog("Communication::postTextgetJSON> Ending");
 
        return jObj;
 
    }
	
	public static boolean checkNetState(Context c)
	{
		final ConnectivityManager conMgr =  (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
		return (activeNetwork != null && activeNetwork.isConnected());
	}
	
	public static String getPhoneNumber(Context c) {
		TelephonyManager tMgr = (TelephonyManager)c.getSystemService(Context.TELEPHONY_SERVICE);
		Gen.appendLog("Communication::getPhoneNumber> Phone number : " + tMgr.getLine1Number());
		return tMgr.getLine1Number();
	}
	
	private static String eventsToString(List<Event> events, boolean selectedOnly) {
		String sEvents = null;
		for (Event event : events) {
			Gen.appendLog("Communication::eventsToString> event : " + event.getEventId() + "(Selected:" + event.isSelected() + ")");
			if (event.isSelected() || !selectedOnly) {
				if (sEvents == null) {
					sEvents = event.getEventId();
				} else {
					sEvents += ";" + event.getEventId();
				}
			}
		}
		return sEvents;
	}
	
	public static String storiesToString(List<Story> stories, boolean selectedOnly) {
		String sStories = null;
		for (Story story : stories) {
			Gen.appendLog("Communication::storiesToString> Story : " + story.getTitle() + "(Selected:" + story.isSelected() + ")");
			if (story.isSelected() || !selectedOnly) {
				if (sStories == null) {
					sStories = story.getStoryId();
				} else {
					sStories += ";" + story.getStoryId();
				}
			}
		}
		return sStories;
	}
}
