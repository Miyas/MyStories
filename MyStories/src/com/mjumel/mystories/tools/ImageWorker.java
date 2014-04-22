package com.mjumel.mystories.tools;

import android.app.Activity;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore.MediaColumns;

public class ImageWorker {

	// And to convert the image URI to the direct file system path of the image file
	public static String getRealPathFromURI(Activity acti, Uri uri) 
	{
		String retval = null;
		if (uri == null) return null;
		
		Gen.writeLog("ImageWorker::getRealPathFromURI> Started");
		String[] projection = {  MediaColumns.DATA};
	    Cursor cursor = acti.getContentResolver().query(uri, projection, null, null, null);
	    if(cursor != null) {
	        //HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
	        //THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
	        cursor.moveToFirst();
	        int columnIndex = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
	        String filePath = cursor.getString(columnIndex);
	        cursor.close();
	        retval = filePath;
	    }
	    else 
	    	retval = uri.getPath();

        Gen.writeLog("ImageWorker::getRealPathFromURI> Ended");
        return retval;
	}
	
	
	public static Bitmap decodeSampledBitmapFromFile(String path, 
			int reqWidth, int reqHeight) {

		Gen.writeLog("ImageWorker::decodeSampledBitmapFromFile> Started");
	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(path, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    
	    Gen.writeLog("ImageWorker::decodeSampledBitmapFromFile> Ended");
	    return BitmapFactory.decodeFile(path, options);
	}
	
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
	        int reqWidth, int reqHeight) {

		Gen.writeLog("ImageWorker::decodeSampledBitmapFromResource> Started");
	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    
	    Gen.writeLog("ImageWorker::decodeSampledBitmapFromResource> Ended");
	    return BitmapFactory.decodeResource(res, resId, options);
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) 
	{
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    Gen.writeLog("ImageWorker::calculateInSampleSize> Started");
	    if (height > reqHeight || width > reqWidth) {
	
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }
	
	    Gen.writeLog("ImageWorker::calculateInSampleSize> inSampleSize = " + inSampleSize);
	    Gen.writeLog("ImageWorker::calculateInSampleSize> Ended");
	    return inSampleSize;
	}
		
}
