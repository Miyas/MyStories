package com.mjumel.mystories.tools;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

public class Gen {
	public static void writeLog(String text) { doWrite(text, "D", false); }
	public static void writeLog(String text, String crit) { doWrite(text, crit, false); }
	
	public static void appendLog(String text) { doWrite(text, "D", true); }
	public static void appendLog(String text, String crit) { doWrite(text, crit, true); }
	
	private static void doWrite(String text, String crit, boolean append)
	{
	   File logFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/log.txt");
	   if (!logFile.exists())
	   {
	      try
	      {
	         logFile.createNewFile();
	      } 
	      catch (IOException e)
	      {
	         // TODO Auto-generated catch block
	    	  Log.e("MAX", "Error while creating log file");
	    	  e.printStackTrace();
	    	  Log.e("MAX", e.getMessage());
	      }
	   }
	   try
	   {
	      //BufferedWriter for performance, true to set append to file flag
	      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, append));
	      SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE);
	      String sText = formater.format(new Date()) + "\t" + crit + "\t" + text;
	      
	      //if (append)
	    //	  buf.append(sText);
	      //else
	    	  buf.write(sText);
	      buf.newLine();
	      buf.close();
	   }
	   catch (IOException e)
	   {
	      // TODO Auto-generated catch block
		   Log.e("MAX", "Error while writing in log file");
	      e.printStackTrace();
	   }
	}
	
	public static String downloadFile(String string_url)
	{
		StringBuilder sBuilder = new StringBuilder();
		
		try {
        	Gen.appendLog( "Gen:downloadFile > Url: " + string_url);
            URL url = new URL(string_url);
            //if (p != null) p.SetHost("http://" + url.getHost());
            URLConnection conexion = url.openConnection(); 
            conexion.connect();
            InputStream input = new BufferedInputStream(url.openStream());
            BufferedReader r = new BufferedReader(new InputStreamReader(input));
            String line;
            while ((line = r.readLine()) != null) {
            	sBuilder.append(line + "\n");
            }
            r.close();
            input.close();
        }
		catch (Exception e) 
        {
        	Gen.appendLog( "Gen:downloadFile > Download url ERROR", "E");
        	Gen.appendLog( e.getMessage(), "E");
        }
		
		return sBuilder.toString();
	}
	
	public static Spannable textToSpan(String text, int color) 
	{
		Spannable wordtoSpan = new SpannableString(text);        
	    wordtoSpan.setSpan(new ForegroundColorSpan(color), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    return wordtoSpan;	
	}
	
	public static String md5Encrypt(String value)
    {
		String valEncrypted = null;
		
    	try {
    		MessageDigest md = MessageDigest.getInstance("MD5");
    		md.update(value.getBytes());
 
	        byte byteData[] = md.digest();
	 
	        //convert the byte to hex format
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < byteData.length; i++) {
	        	sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	        }
	 
	        valEncrypted = sb.toString();
	 
    	} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return valEncrypted;
    }
}
