package com.mjumel.mystories.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.mjumel.mystories.MyStoriesApp;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class ImageWorker {

	final static int KITKAT_VERSION = 19;
	
	/**
	 * Get a file path from a Uri. This will get the the path for Storage Access
	 * Framework Documents, as well as the _data field for the MediaStore and
	 * other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @author paulburke
	 */
	@SuppressLint("NewApi")
	public static String getPath(final Context context, final Uri uri) {

	    final boolean isKitKat = Build.VERSION.SDK_INT >= KITKAT_VERSION;
	    String path = null;

       	Gen.appendLog("ImageWorker::getPath> Uri = " + uri + " / isKitKat = " + isKitKat);

	    // DocumentProvider
	    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
	        // ExternalStorageProvider
	        if (isExternalStorageDocument(uri)) {
	            final String docId = DocumentsContract.getDocumentId(uri);
	            final String[] split = docId.split(":");
	            final String type = split[0];

	            if ("primary".equalsIgnoreCase(type)) {
	            	Gen.appendLog("ImageWorker::getPath> Primary external storage found");
	            	path = Environment.getExternalStorageDirectory() + "/" + split[1];
	            }

	            // TODO handle non-primary volumes
	        }
	        // DownloadsProvider
	        else if (isDownloadsDocument(uri)) {

	            final String id = DocumentsContract.getDocumentId(uri);
	            final Uri contentUri = ContentUris.withAppendedId(
	                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

	            Gen.appendLog("ImageWorker::getPath> Download found");
	            path = getDataColumn(context, contentUri, null, null);
	        }
	        // MediaProvider
	        else if (isMediaDocument(uri)) {
	            final String docId = DocumentsContract.getDocumentId(uri);
	            final String[] split = docId.split(":");
	            final String type = split[0];

	            Uri contentUri = null;
	            if ("image".equals(type)) {
	                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	            } else if ("video".equals(type)) {
	                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	            } else if ("audio".equals(type)) {
	                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	            }

	            final String selection = "_id=?";
	            final String[] selectionArgs = new String[] {
	                    split[1]
	            };

	            Gen.appendLog("ImageWorker::getPath> MediaProvider found");
	            path = getDataColumn(context, contentUri, selection, selectionArgs);
	        }
	    }
	    // MediaStore (and general)
	    else if ("content".equalsIgnoreCase(uri.getScheme())) {
	    	Gen.appendLog("ImageWorker::getPath> Mediastore found");
	        path = getDataColumn(context, uri, null, null);
	    }
	    // File
	    else if ("file".equalsIgnoreCase(uri.getScheme())) {
	    	Gen.appendLog("ImageWorker::getPath> File found");
	        path = uri.getPath();
	    }
	    
	    if (uri != null && path == null) {
            try {
           	Gen.appendError("ImageWorker::getPath> Path not found, trying to get content otherwise");
				InputStream is = context.getContentResolver().openInputStream(uri);
				path = MyStoriesApp.CACHE_DIR + 
						 Gen.md5Encrypt(uri.toString()) + ".jpg";
				File tmpFile = new File(path);
				Gen.CopyStream(is, new FileOutputStream(tmpFile));
				is.close();
			} catch (FileNotFoundException e) {
				Gen.appendError("ImageWorker::getPath> FileNotFoundException");
				Gen.appendError("ImageWorker::getPath> imagePath = " + path);
				Gen.appendError("ImageWorker::getPath> " + e.getLocalizedMessage());
				e.printStackTrace();
				path = null;
			} catch (IOException e) {
				Gen.appendError("ImageWorker::getPath> IOException");
				Gen.appendError("ImageWorker::getPath> imagePath = " + path);
				Gen.appendError("ImageWorker::getPath> " + e.getLocalizedMessage());
				e.printStackTrace();
				path = null;
			}
   	 	}

	    Gen.appendLog("ImageWorker::getPath> New path : " + path);
	    return path;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @param selection (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
	        String[] selectionArgs) {

	    Cursor cursor = null;
	    final String column = "_data";
	    final String[] projection = {
	            column
	    };

	    try {
	        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
	                null);
	        if (cursor != null && cursor.moveToFirst()) {
	            final int column_index = cursor.getColumnIndexOrThrow(column);
	            return cursor.getString(column_index);
	        }
	    } finally {
	        if (cursor != null)
	            cursor.close();
	    }
	    return null;
	}


	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
	    return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
	    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
	    return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
	
	/**
	 * @param context The context.
	 * @param date The intent data from camera app.
	 * @return The correct Uri to the captured picture.
	 */
	public static Uri getCameraImagePath(Context context, Intent data) {
		Uri mImageCaptureUri_samsung = null;
        // Final Code As Below
        //try {
        	if (data != null) {
        		Gen.appendLog("ImageWorker::getCameraImagePath> inside Classical Phones");
            	return data.getData();
            }
        	
        	Gen.appendLog("ImageWorker::getCameraImagePath> inside Samsung Phones");
            String[] projection = {
                    MediaStore.Images.Thumbnails._ID, // The columns we want
                    MediaStore.Images.Thumbnails.IMAGE_ID,
                    MediaStore.Images.Thumbnails.KIND,
                    MediaStore.Images.Thumbnails.DATA};
            String selection = MediaStore.Images.Thumbnails.KIND + "=" + // Select
                                                                            // only
                                                                            // mini's
                    MediaStore.Images.Thumbnails.MINI_KIND;

            String sort = MediaStore.Images.Thumbnails._ID + " DESC";

            // At the moment, this is a bit of a hack, as I'm returning ALL
            // images, and just taking the latest one. There is a better way
            // to
            // narrow this down I think with a WHERE clause which is
            // currently
            // the selection variable
            Cursor myCursor = context.getContentResolver().query(
                    MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                    projection, selection, null, sort);

            long imageId = 0l;
            long thumbnailImageId = 0l;
            String thumbnailPath = "";
            int orientation = -1;

            try {
                myCursor.moveToFirst();
                imageId = myCursor
                        .getLong(myCursor
                                .getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID));
                thumbnailImageId = myCursor
                        .getLong(myCursor
                                .getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID));
                thumbnailPath = myCursor
                        .getString(myCursor
                                .getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
            } finally {
                myCursor.close();
            }

            // Create new Cursor to obtain the file Path for the large image

            String[] largeFileProjection = {
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.ORIENTATION};
            String largeFileSort = MediaStore.Images.ImageColumns._ID
                    + " DESC";
            myCursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    largeFileProjection, null, null, largeFileSort);
            String largeImagePath = "";

            try {
                myCursor.moveToFirst();

                // This will actually give you the file path location of the
                // image.
                largeImagePath = myCursor
                        .getString(myCursor
                                .getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
                orientation = myCursor
                        .getInt(myCursor
                                .getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION));
                Gen.appendLog("ImageWorker::getCameraImagePath> Orientation = " + orientation);
                mImageCaptureUri_samsung = Uri.fromFile(new File(largeImagePath));
                //mImageCaptureUri = null;
            } finally {
                myCursor.close();
            }

            // These are the two URI's you'll be interested in. They give
            // you a handle to the actual images
            Uri uriLargeImage = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    String.valueOf(imageId));
            Uri uriThumbnailImage = Uri.withAppendedPath(
                    MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                    String.valueOf(thumbnailImageId));

           	return mImageCaptureUri_samsung;
        /*} catch (Exception e) {
            mImageCaptureUri_samsung = null;
            Gen.appendLog("inside catch Samsung Phones exception " + e.toString(), "E");
        }*/
	}
}
