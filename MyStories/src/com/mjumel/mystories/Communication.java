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

public class Communication {
	
	public static void postEvent(String userid, String comment, int rating, final String mediaUri) 
	{
		Gen.writeLog("postEvent2::Starting");
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
	        Gen.writeLog("postEvent2::Response Code : " + serverResponseCode);
	
	        HttpEntity httpEntity = response.getEntity();
	        String result = EntityUtils.toString(httpEntity);
	
	        Gen.writeLog("postEvent2::Result" + result);
	    }
	    catch(Exception e)
	    {
	        //e.printStackTrace();
	        Gen.writeLog("postEvent2::Exception error");
            Gen.writeLog("postEvent2::" + e.getMessage());
	    }
		Gen.writeLog("postEvent2::Ending");
	}
	
	public static int login(String login, String pwd) 
	{
		Gen.writeLog("login::Starting");
		//final ProgressDialog dialog = null;
		int serverResponseCode = 0;
		
		String upLoadServerUri = "http://anizoo.info/mystories/login.php"; 
		try
	    {
	        HttpClient client = new DefaultHttpClient();
	        HttpPost post = new HttpPost(upLoadServerUri);
	
	        List<NameValuePair> params = new ArrayList<NameValuePair>();
	        params.add(new BasicNameValuePair("l", login));
	        params.add(new BasicNameValuePair("p", pwd));
	        post.setEntity(new UrlEncodedFormEntity(params));
	
	        HttpResponse response = client.execute(post);
	        serverResponseCode = response.getStatusLine().getStatusCode();
	        Gen.writeLog("login::Response Code : " + serverResponseCode);
	        
	        HttpEntity httpEntity = response.getEntity();
	        String result = EntityUtils.toString(httpEntity);
	
	        Gen.writeLog("login::Result" + result);
	    }
	    catch(Exception e)
	    {
	        //e.printStackTrace();
	        Gen.writeLog("login::Exception error");
            Gen.writeLog("login::" + e.getMessage());
            return -1;
	    }
		
		Gen.writeLog("login::Ending");
		return 1;
	}
}
