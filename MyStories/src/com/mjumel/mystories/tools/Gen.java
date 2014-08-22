package com.mjumel.mystories.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.mjumel.mystories.Event;
import com.mjumel.mystories.MyStoriesApp;
import com.mjumel.mystories.Story;

public class Gen {
	
	public static final int IO_BUFFER_SIZE = 8 * 1024;
	
	public static void writeLog(String text) { doWrite(text, "D", false); }
	public static void writeLog(String text, String crit) { doWrite(text, crit, false); }
	
	public static void appendLog(String text) { doWrite(text, "D", true); }
	public static void appendLog(String text, String crit) { doWrite(text, crit, true); }
	
	public static void appendError(String text) { doWrite(text, "E", true); }
	public static void appendError(String text, Exception e) {
		doWrite(text + "Class: " + e.getClass().getName(), "E", true);
		doWrite(text + "Message: " + e.getMessage(), "E", true);
		for(StackTraceElement s : e.getStackTrace())
			doWrite(text + s.toString(), "E", true);
	}
	
	private static void doWrite(String text, String crit, boolean append)
	{
		SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd", Locale.FRANCE);
		File logFile = new File(MyStoriesApp.EXT_DIR + formater.format(new Date()) + "_" + MyStoriesApp.LOG_FILENAME);

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
			formater.applyPattern("yyyy-MM-dd HH:mm:ss.SSSZ");
			String sText = formater.format(new Date()) + "\t" + crit + "\t" + text;
			buf.write(sText);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			Log.e("MAX", "Error while writing in log file");
			e.printStackTrace();
		}
	}
	
	public static void purgeLogFiles(int delay) 
	{
		SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd", Locale.FRANCE);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, delay * -1);
		String sDate = formater.format(cal.getTime());
		String fileName = sDate + "_" + MyStoriesApp.LOG_FILENAME;

		File logPath = new File(MyStoriesApp.EXT_DIR);
		if (logPath.exists()) {
			File[] files = logPath.listFiles(new Filter("_" + MyStoriesApp.LOG_FILENAME, fileName));
			for(File f : files) {
				Gen.appendLog("Gen::purgeLogFiles> Purging file : " + f.getAbsolutePath());
				f.delete();
			}
		}
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
    		Gen.appendError("Gen::md5Encrypt> ", e);
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
		
		int pos = 0;
		while(charToRemove.contains(s.subSequence(pos, pos+1))) {
			pos++;
		}
		
		sTmp = s.substring(pos);
		
		return sTmp;
	}
	
	/**
     * @return Application's version code from the {@code PackageManager}.
     */
	public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            //throw new RuntimeException("Could not get package name: " + e);
            Gen.appendError("Gen::getAppVersion> ", e);
            return -1;
        }
    }
	
	public static SpannableStringBuilder bicolorSpan(String text, int limit) {
		SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
		stringBuilder.append(text);
		stringBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#EBE3AA")), 0, limit, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		stringBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#CAD7B2")), limit, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		return stringBuilder;
	}
	
	public static class Filter implements FilenameFilter {
	
		String filterContains;
		String filterCompare;
		
		public Filter(String filterContains, String filterCompare) {
			this.filterContains = filterContains;
			this.filterCompare = filterCompare;
		}
		
		@Override
		public boolean accept(File dir, String filename) {
			return (filename.contains(filterContains) && filename.compareToIgnoreCase(filterCompare) <= 0);
		}
	}
	
	public static ArrayList<Event> eventListToArrayList(List<Event> list) {
		ArrayList<Event> arrayList = new ArrayList<Event>();
		arrayList.addAll(list);
		return arrayList;
	}
	
	public static ArrayList<Story> storyListToArrayList(List<Story> list) {
		ArrayList<Story> arrayList = new ArrayList<Story>();
		arrayList.addAll(list);
		return arrayList;
	}
}
