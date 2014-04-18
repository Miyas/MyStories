package com.mjumel.mystories;

import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.util.Log;

public class Communication {
	
	public static void postEvent(final Activity act, String userid, String comment, int rating, final String mediaUri) 
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
	
	        HttpEntity entity = entityBuilder.build();
	
	        post.setEntity(entity);
	
	        HttpResponse response = client.execute(post);
	
	        HttpEntity httpEntity = response.getEntity();
	
	        String result = EntityUtils.toString(httpEntity);
	
	        Log.v("result", result);
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
}
