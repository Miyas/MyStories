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

import com.mjumel.mystories.Event;
import com.mjumel.mystories.Story;

public class Communication {
	
	private static final String AUTH_URI = "http://anizoo.info/mystories/include/auth.php";
	private static final String POST_URI = "http://anizoo.info/mystories/post/userevents.php";
	
	public static int login(String login, String pwd) 
	{
		Gen.appendLog("Communication::login> Starting");
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "1"));
        params.add(new BasicNameValuePair("l", login));
        params.add(new BasicNameValuePair("p", pwd));
        
		String[] res = postText(AUTH_URI, params);
		if (res[0].equals("200"))
		{
        	String[] message = res[1].split(":");
        	if (message[0].equals("OK"))
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
	public static int postEvent(String userid, String eventId, String comment, int rating, String mediaUri, int cat) 
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
	        entityBuilder.addTextBody("comment", comment);
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
	
	        Gen.appendLog("Communication::postEvent> Result : " + result);
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
	
	public static boolean deleteEvent(Event event) 
	{
		Gen.appendLog("Communication::deleteEvent> Starting");
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "7"));
        params.add(new BasicNameValuePair("ui", event.getUserId()));
        params.add(new BasicNameValuePair("eid", event.getEventId()));
        
        String[] res = postText(POST_URI, params);
		if (res[0].equals("200"))
		{
			Gen.appendLog("Communication::deleteEvent> Response = \n" + res[1] + "\n");
			try {
				return (res[1].compareTo("1")==0?true:false);
			} catch(NumberFormatException e) {
				Gen.appendError("Communication::deleteEvent> NumberFormatException Exception");
				Gen.appendError("Communication::deleteEvent> " + e.getLocalizedMessage());
       		}
        }
        return false;
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
	
	public static List<Story> getUserStories(String userId)//, String userSession) 
	{
		Gen.appendLog("Communication::getUserStories> Starting");
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "5"));
        params.add(new BasicNameValuePair("ui", userId));
        //params.add(new BasicNameValuePair("us", userSession));
        
        String[] res = postText(POST_URI, params);
		if (res[0].equals("200"))
		{
			Gen.appendLog("Communication::getUserStories> Response = \n" + res[1] + "\n");
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
		
		String sEvents = null;
		for (Event event : story.getEvents()) {
			if (sEvents == null)
				sEvents = event.getEventId();
			else
				sEvents += ":" + event.getEventId();
		}
			
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
	
	public static boolean deleteStories(List<Story> stories) 
	{
		Gen.appendLog("Communication::deleteStories> Starting");
		
		String sStories = null;
		String sUserId = null;
		for (Story story : stories) {
			if (story.isSelected()) {
				if (sStories == null) {
					sStories = story.getStoryId();
				} else {
					sStories += ";" + story.getStoryId();
				}
			}
			sUserId = (sUserId == null ? story.getUserId() : sUserId);
		}
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "4"));
        params.add(new BasicNameValuePair("ui", sUserId));
        params.add(new BasicNameValuePair("sid", sStories));
        
        String[] res = postText(POST_URI, params);
		if (res[0].equals("200"))
		{
			Gen.appendLog("Communication::deleteStories> Response = \n" + res[1] + "\n");
			try {
				return (Integer.parseInt(res[1]) == stories.size()?true:false);
			} catch(NumberFormatException e) {
				Gen.appendError("Communication::deleteStories> NumberFormatException Exception");
				Gen.appendError("Communication::deleteStories> " + e.getLocalizedMessage());
       		}
        }
        return false;
	}
	
	public static boolean deleteStory(Story story) 
	{
		Gen.appendLog("Communication::deleteStory> Starting");
		
		List<Story> stories = new ArrayList<Story>();
		stories.add(story);
		return deleteStories(stories);
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
	
	public static boolean checkNetState(Context c)
	{
		final ConnectivityManager conMgr =  (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
		return (activeNetwork != null && activeNetwork.isConnected());
	}
}
