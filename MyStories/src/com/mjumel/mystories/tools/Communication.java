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

public class Communication {
	
	public static void postEvent(String userid, String comment, int rating, String mediaUri, int cat) 
	{
		Gen.writeLog("Communication::postEvent> Starting");
		int serverResponseCode = 0;
		
		String upLoadServerUri = "http://anizoo.info/mystories/post.php"; 
		try
	    {
	        HttpClient client = new DefaultHttpClient();
	
	        HttpPost post = new HttpPost(upLoadServerUri);
	
	        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
	        entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
	        entityBuilder.addTextBody("user_id", userid);
	        entityBuilder.addTextBody("comment", comment);
	        entityBuilder.addTextBody("rating", String.valueOf(rating));
	        entityBuilder.addTextBody("cat", String.valueOf(cat));
	
	        if(mediaUri != null)
	        {
	        	File mediaFile = new File(mediaUri);
	            entityBuilder.addBinaryBody("uploaded_file", mediaFile);
	        }
	
	        post.setEntity(entityBuilder.build());
	
	        HttpResponse response = client.execute(post);
	        serverResponseCode = response.getStatusLine().getStatusCode();
	        Gen.writeLog("Communication::postEvent> Response Code : " + serverResponseCode);
	
	        HttpEntity httpEntity = response.getEntity();
	        String result = EntityUtils.toString(httpEntity);
	
	        Gen.writeLog("Communication::postEvent> Result" + result);
	    }
	    catch(Exception e)
	    {
	        //e.printStackTrace();
	        Gen.writeLog("Communication::postEvent> Exception error");
            Gen.writeLog("Communication::postEvent> " + e.getMessage());
	    }
		Gen.writeLog("Communication::postEvent> Ending");
	}
	
	public static int login(String login, String pwd) 
	{
		Gen.writeLog("Communication::login> Starting");
		
		String upLoadServerUri = "http://anizoo.info/mystories/include/auth.php";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "1"));
        params.add(new BasicNameValuePair("l", login));
        params.add(new BasicNameValuePair("p", pwd));
        
		String[] res = postText(upLoadServerUri, params);
		if (res[0].equals("200"))
		{
        	String[] message = res[1].split(":");
        	if (message[0].equals("OK"))
        		return Integer.valueOf(message[1]);
        }
       	return -1;
	}
	
	public static List<Event> getUserEvents(String userId)//, String userSession) 
	{
		Gen.writeLog("Communication::getUserEvents> Starting");
		
		String upLoadServerUri = "http://anizoo.info/mystories/post/userevents.php";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("action", "1"));
        params.add(new BasicNameValuePair("ui", userId));
        //params.add(new BasicNameValuePair("us", userSession));
        
		/*<feed>
		 * <uid id="5">
		 *  <story id="-1">
		 *   <event id="14">
		 * 	  <comment>COMMENT</comment>
		 *     <rating>4</rating>
		 *     <category id="">CAT 1</category>
		 *     <mediapath>
		 *   	<thumb>http://...</thumb>
		 *   	<resized>http://...</resized>
		 *     </mediapath>
		 *   </event>
		 *  </story>
		 * </uid>
		 *</feed>
		 */
        String[] res = postText(upLoadServerUri, params);
		if (res[0].equals("200"))
		{
       		try {
				return (new XmlParser()).parseEvents((Reader)new StringReader(res[1]));
			} catch (XmlPullParserException e) {
				e.printStackTrace();
				Gen.writeLog("Communication::getUserEvents> XmlPullParserException Error");
			} catch (IOException e) {
				e.printStackTrace();
				Gen.writeLog("Communication::getUserEvents> IOException Error");
			}
        }
        return null;
	}
	
	private static String[] postText(String uri, List<NameValuePair> params)
	{
		Gen.writeLog("Communication::post> Starting for uri = " + uri);
		
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
	        Gen.writeLog("Communication::post> Response Code : " + serverResponseCode);
	        
	        result[0] = String.valueOf(serverResponseCode);
	        if (serverResponseCode == 200)
	        {
	        	HttpEntity httpEntity = response.getEntity();
	        	serverRespondeMsg = EntityUtils.toString(httpEntity);
	        }
	    }
	    catch(Exception e)
	    {
	        Gen.writeLog("Communication::post> Exception error");
            Gen.writeLog("Communication::post> " + e.getMessage());
            serverRespondeMsg = e.getMessage();
	    }
		
		result[0] = String.valueOf(serverResponseCode);
		result[1] = serverRespondeMsg;
		
		Gen.writeLog("Communication::post> Ending");
		return result;
	}
	
	public static boolean checkNetState(Context c)
	{
		final ConnectivityManager conMgr =  (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
		return (activeNetwork != null && activeNetwork.isConnected());
	}
}
