package com.mjumel.mystories;

import java.io.File;
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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Communication {
	
	public static void postEvent(String userid, String comment, int rating, final String mediaUri) 
	{
		Gen.writeLog("Communication::postEvent> Starting");
		//final ProgressDialog dialog = null;
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
		//final ProgressDialog dialog = null;
		int serverResponseCode = 0;
		
		String upLoadServerUri = "http://anizoo.info/mystories/include/auth.php"; 
		try
	    {
	        HttpClient client = new DefaultHttpClient();
	        HttpPost post = new HttpPost(upLoadServerUri);
	
	        List<NameValuePair> params = new ArrayList<NameValuePair>();
	        params.add(new BasicNameValuePair("action", "1"));
	        params.add(new BasicNameValuePair("l", login));
	        params.add(new BasicNameValuePair("p", pwd));
	        post.setEntity(new UrlEncodedFormEntity(params));
	
	        HttpResponse response = client.execute(post);
	        serverResponseCode = response.getStatusLine().getStatusCode();
	        Gen.writeLog("Communication::login> Response Code : " + serverResponseCode);
	        
	        if (serverResponseCode == 200)
	        {
	        	HttpEntity httpEntity = response.getEntity();
	        	String result = EntityUtils.toString(httpEntity);
	        	Gen.writeLog("Communication::login> " + result);
	        	String[] res = result.split(":");
	        	if (res[0] == "OK")
	        		return -1;
	        	else
	        		return Integer.valueOf(res[1]);
	        }
	        else
	        	return -1;
	    }
	    catch(Exception e)
	    {
	        //e.printStackTrace();
	        Gen.writeLog("Communication::login> Exception error");
            Gen.writeLog("Communication::login> " + e.getMessage());
            return -1;
	    }
	}
	
	public static boolean checkNetState(Context c)
	{
		final ConnectivityManager conMgr =  (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
		return (activeNetwork != null && activeNetwork.isConnected());
	}
}
