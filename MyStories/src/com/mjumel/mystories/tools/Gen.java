package com.mjumel.mystories.tools;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.mjumel.mystories.MyStoriesApp;

public class Gen {
	
	public static final int IO_BUFFER_SIZE = 8 * 1024;
	
	public static void writeLog(String text) { doWrite(text, "D", false); }
	public static void writeLog(String text, String crit) { doWrite(text, crit, false); }
	
	public static void appendLog(String text) { doWrite(text, "D", true); }
	public static void appendLog(String text, String crit) { doWrite(text, crit, true); }
	
	public static void appendError(String text) { doWrite(text, "E", true); }
	
	private static void doWrite(String text, String crit, boolean append)
	{
	   File logFile = new File(MyStoriesApp.EXT_DIR + MyStoriesApp.LOG_FILENAME);
	   //Log.e("MAX", "Writing log to " + logFile.getAbsolutePath());
	   if (!logFile.exists()) {
	      try {
	         logFile.createNewFile();
	      } catch (IOException e) {
	    	  Log.e("MAX", "Error while creating log file");
	    	  e.printStackTrace();
	    	  Log.e("MAX", e.getMessage());
	      }
	   }
		   
	   try {
	      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, append));
	      SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ", Locale.FRANCE);
	      String sText = formater.format(new Date()) + "\t" + crit + "\t" + text;
	      buf.write(sText);
	      buf.newLine();
	      buf.close();
	   } catch (IOException e) {
		  Log.e("MAX", "Error while writing in log file");
	      e.printStackTrace();
	   }
	}
	
	public static String downloadFile(String string_url)
	{
		StringBuilder sBuilder = new StringBuilder();
		
		try {
        	Gen.appendLog( "Gen::downloadFile > Url: " + string_url);
            URL url = new URL(string_url);
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
        	Gen.appendError( "Gen::downloadFile > Download url ERROR");
        	Gen.appendError( e.getMessage());
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
    		Gen.appendError( "Gen::md5Encrypt > NoSuchAlgorithmException ERROR");
        	Gen.appendError( e.getMessage());
		}
    	
    	return valEncrypted;
    }
	
	public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
             
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              //Read byte from input stream
                 
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
               
              //Write byte from output stream
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
	
	public static String superTrim(String s) 
	{
		String sTmp = "";
		String charToRemove = " \\n\\r";
		
		Gen.appendLog( "Gen::superTrim > Input string = " + s);
		
		int pos = 0;
		Gen.appendLog( "Gen::superTrim > Current char(" + pos + ") = \"" + s.subSequence(pos, pos+1) + "\"");
		while(charToRemove.contains(s.subSequence(pos, pos+1))) {
			pos++;
			Gen.appendLog( "Gen::superTrim > Current char(" + pos + ") = \"" + s.subSequence(pos, pos+1) + "\"");
			Gen.appendLog( "Gen::superTrim > Char removed");
		}
		
		sTmp = s.substring(pos);
		Gen.appendLog( "Gen::superTrim > Output string = " + sTmp);
		
		return sTmp;
	}
}
