package com.mjumel.mystories.tools;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
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
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;

import com.mjumel.mystories.Event;
import com.mjumel.mystories.Story;

public class Communication {
	
	private static final String AUTH_URI = "http://anizoo.info/mystories/include/auth.php";
	private static final String POST_URI = "http://anizoo.info/mystories/post/userevents.php";
	
	public static int login(String login, String pwd, Context c) 
	{
		Gen.appendLog("Communication::login> Starting");
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "1"));
        params.add(new BasicNameValuePair("l", login));
        params.add(new BasicNameValuePair("p", pwd));
        params.add(new BasicNameValuePair("phone", getPhoneNumber(c)));
        
		String[] res = postText(AUTH_URI, params);
		if (res[0].equals("200"))
		{
        	String[] message = res[1].split(":");
        	return Integer.valueOf(message[1]);
        }
       	return -1;
	}
	
	public static int register(String login, String pwd, String fname, String lname, String phone) 
	{
		Gen.appendLog("Communication::register> Starting");
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "3"));
        params.add(new BasicNameValuePair("l", login));
        params.add(new BasicNameValuePair("p", pwd));
        params.add(new BasicNameValuePair("fname", fname));
        params.add(new BasicNameValuePair("lname", lname));
        params.add(new BasicNameValuePair("phone", phone));
        
		String[] res = postText(AUTH_URI, params);
		if (res[0].equals("200"))
		{
        	String[] message = res[1].split(":");
        	return Integer.valueOf(message[1]);
        }
       	return -1;
	}
	
	public static int newEvent(String userid, String comment, int rating, String mediaUri, int cat) {
		return postEvent(userid, null, comment, rating, mediaUri, cat);
	}
	public static int editEvent(String userid, String eventId, String comment, int rating, String mediaUri, int cat) {
		return postEvent(userid, eventId, comment, rating, mediaUri, cat);
	}
	
	
	public static boolean deleteEvents(String sUserId, List<Event> events) 
	{
		Gen.appendLog("Communication::deleteEvents> Starting");
		
		
		String sEvents = eventsToString(events, true);
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "7"));
        params.add(new BasicNameValuePair("ui", sUserId));
        params.add(new BasicNameValuePair("ids", sEvents));
        
        String[] res = postText(POST_URI, params);
		if (res[0].equals("200"))
		{
			//Gen.appendLog("Communication::deleteEvents> Response = \n" + res[1] + "\n");
			try {
				return (Integer.parseInt(res[1]) == sEvents.split(";").length);
			} catch(NumberFormatException e) {
				Gen.appendError("Communication::deleteEvents> NumberFormatException Exception");
				Gen.appendError("Communication::deleteEvents> " + e.getLocalizedMessage());
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
        params.add(new BasicNameValuePair("action", "1"));
        params.add(new BasicNameValuePair("ui", userId));
        //params.add(new BasicNameValuePair("us", userSession));
        
        String[] res = postText(POST_URI, params);
		if (res[0].equals("200"))
		{
			//Gen.appendLog("Communication::getUserEvents> Response = \n" + res[1] + "\n");
       		try {
				return (new XmlParser()).parseEvents((Reader)new StringReader(res[1]));
			} catch (XmlPullParserException e) {
				e.printStackTrace();
				Gen.appendError("Communication::getUserEvents> XmlPullParserException Error");
			} catch (IOException e) {
				e.printStackTrace();
				Gen.appendError("Communication::getUserEvents> IOException Error");
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
	 * @param light Indicate if you want to get the complete details of stories or only the light version (title + id).
	 * @author maxime.jumel
	 */
	public static List<Story> getUserStories(String userId, boolean light)//, String userSession) 
	{
		Gen.appendLog("Communication::getUserStories> Starting");
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "5"));
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
				e.printStackTrace();
				Gen.appendError("Communication::getUserStories> XmlPullParserException Error");
			} catch (IOException e) {
				e.printStackTrace();
				Gen.appendError("Communication::getUserStories> IOException Error");
			}
        }
        return null;
	}
	
	
	public static int addStory(Story story) 
	{
		Gen.appendLog("Communication::addStory> Starting");
		
		String sEvents = eventsToString(story.getEvents(), false);
			
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "2"));
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
				Gen.appendError("Communication::addStory> NumberFormatException Exception");
				Gen.appendError("Communication::addStory> " + e.getLocalizedMessage());
       		}
        }
        return -1;
	}
	
	public static boolean deleteStories(String sUserId, List<Story> stories) 
	{
		Gen.appendLog("Communication::deleteStories> Starting");
		
		String sStories = storiesToString(stories, true);
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "4"));
        params.add(new BasicNameValuePair("ui", sUserId));
        params.add(new BasicNameValuePair("ids", sStories));
        
        String[] res = postText(POST_URI, params);
		if (res[0].equals("200"))
		{
			//Gen.appendLog("Communication::deleteStories> Response = \n" + res[1] + "\n");
			try {
				return (Integer.parseInt(res[1]) == sStories.split(";").length);
			} catch(NumberFormatException e) {
				Gen.appendError("Communication::deleteStories> NumberFormatException Exception");
				Gen.appendError("Communication::deleteStories> " + e.getLocalizedMessage());
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
        params.add(new BasicNameValuePair("action", "8"));
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
				Gen.appendError("Communication::linkEvents> NumberFormatException Exception");
				Gen.appendError("Communication::linkEvents> " + e.getLocalizedMessage());
       		}
        }
        return false;
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
	        
	        //if (serverResponseCode == 200)
	        //{
	        	HttpEntity httpEntity = response.getEntity();
	        	serverRespondeMsg = EntityUtils.toString(httpEntity);
	        //}
	    }
		catch(ClientProtocolException e)
		{
			Gen.appendError("Communication::postText> ClientProtocolException error");
            Gen.appendError("Communication::postText> " + e.getMessage());
            serverRespondeMsg = e.getMessage();
		}
		catch(IOException e)
		{
			Gen.appendError("Communication::postText> IOException error");
            Gen.appendError("Communication::postText> " + e.getMessage());
            serverRespondeMsg = e.getMessage();
		}
	    catch(Exception e)
	    {
	        Gen.appendError("Communication::postText> Exception error");
            Gen.appendError("Communication::postText> " + e.getMessage());
            serverRespondeMsg = e.getMessage();
	    }
		
		result[0] = String.valueOf(serverResponseCode);
		result[1] = serverRespondeMsg;
		
		Gen.appendLog("Communication::postText> Ending");
		return result;
	}
	
	private static int postEvent(String userid, String eventId, String comment, int rating, String mediaUri, int cat) 
	{
		Gen.appendLog("Communication::postEvent> Starting");
		int serverResponseCode = 0;
		
		try
	    {
	        HttpClient client = new DefaultHttpClient();
	
	        HttpPost post = new HttpPost(POST_URI);
	
	        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
	        entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
	        entityBuilder.addTextBody("action", "6");
	        entityBuilder.addTextBody("ui", userid);
	        entityBuilder.addTextBody("comment", Uri.encode(comment));
	        entityBuilder.addTextBody("rating", String.valueOf(rating));
	        entityBuilder.addTextBody("cat", String.valueOf(cat));
	
	        if(eventId != null) {
	        	entityBuilder.addTextBody("eid", eventId);
	        }
	        
	        if(mediaUri != null) {
	        	File mediaFile = new File(mediaUri);
	            entityBuilder.addBinaryBody("uploaded_file", mediaFile);
	        }
	
	        post.setEntity(entityBuilder.build());
	
	        HttpResponse response = client.execute(post);
	        serverResponseCode = response.getStatusLine().getStatusCode();
	        Gen.appendLog("Communication::postEvent> Response Code : " + serverResponseCode);
	
	        HttpEntity httpEntity = response.getEntity();
	        String result = EntityUtils.toString(httpEntity);
	
	        //Gen.appendLog("Communication::postEvent> Result : " + result);
	        return Integer.parseInt(result);
	    }
		catch(NumberFormatException e) {
			Gen.appendError("Communication::postEvent> NumberFormatException error");
            Gen.appendError("Communication::postEvent> " + e.getMessage());
            e.printStackTrace();
		} catch(Exception e) {
	        Gen.appendError("Communication::postEvent> Exception error");
            Gen.appendError("Communication::postEvent> " + e.getMessage());
            e.printStackTrace();
	    }
		Gen.appendLog("Communication::postEvent> Ending");
        return -1;
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
	
	private static String storiesToString(List<Story> stories, boolean selectedOnly) {
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
