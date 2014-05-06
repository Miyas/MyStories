package com.mjumel.mystories.tools;

import java.io.File;
import java.io.IOException;

import com.jakewharton.disklrucache.DiskLruCache;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Build.VERSION_CODES;
 
public class FileCache {
     
    //private File cacheDir;
    private DiskLruCache mDiskLruCache;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final int APP_VERSION = 1; // 10MB
    private static final int FILES_PER_CACHE = 1; // 10MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";

     
    public FileCache(Context context) {
    	new InitDiskCacheTask().execute(cacheDir);
    }
     
    public File getFile(String url){
        //Identify images by hashcode or encode by URLEncoder.encode.
        String filename=String.valueOf(url.hashCode());
         
        File f = new File(cacheDir, filename);
        return f;
         
    }
     
    public void clear(){
        // list all files inside cache directory
        File[] files=cacheDir.listFiles();
        if(files==null)
            return;
        //delete all cache directory files
        for(File f:files)
            f.delete();
    }
    
    class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
        @Override
        protected Void doInBackground(File... params) {
            synchronized (mDiskCacheLock) {
                File cacheDir = params[0];
                try {
					mDiskLruCache = DiskLruCache.open(cacheDir, APP_VERSION, FILES_PER_CACHE, DISK_CACHE_SIZE);
				} catch (IOException e) {
					Gen.appendLog("FileCache$InitDiskCacheTask::doInBackground> Error", "E");
					Gen.appendLog("FileCache$InitDiskCacheTask::doInBackground> " + e.getLocalizedMessage(), "E");
					e.printStackTrace();
				}
                mDiskCacheStarting = false; // Finished initialization
                mDiskCacheLock.notifyAll(); // Wake any waiting threads
            }
            return null;
        }
    }

    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        
        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {
            final String imageKey = String.valueOf(params[0]);

            // Check disk cache in background thread
            Bitmap bitmap = getBitmapFromDiskCache(imageKey);

            if (bitmap == null) { // Not found in disk cache
                // Process as normal
                final Bitmap bitmap = decodeSampledBitmapFromResource(
                        getResources(), params[0], 100, 100));
            }

            // Add final bitmap to caches
            addBitmapToMemoryCache(String.valueOf(params[0]), bitmap);
            addBitmapToCache(imageKey, bitmap);

            return bitmap;
        }
        
    }

    public void addBitmapToCache(String key, Bitmap bitmap) {
        // Add to memory cache as before
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }

        // Also add to disk cache
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null && mDiskLruCache.get(key) == null) {
                mDiskLruCache.put(key, bitmap);
            }
        }
    }

    public Bitmap getBitmapFromDiskCache(String key) {
        synchronized (mDiskCacheLock) {
            // Wait while disk cache is started from background thread
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {}
            }
            if (mDiskLruCache != null) {
                return mDiskLruCache.get(key);
            }
        }
        return null;
    }

    // Creates a unique subdirectory of the designated app cache directory. Tries to use external
    // but if not mounted, falls back on internal storage.
    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable() ? getExternalCacheDir(context).getPath() :
                                context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }
    
    /**
     * Check if external storage is built-in or removable.
     *
     * @return True if external storage is removable (like an SD card), false
     *         otherwise.
     */
    @TargetApi(VERSION_CODES.GINGERBREAD)
    public static boolean isExternalStorageRemovable() {
    	if (Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD)
    		return Environment.isExternalStorageRemovable();
    	else
    		return true;
    }
    
    /**
     * Get the external app cache directory.
     *
     * @param context The context to use
     * @return The external cache dir
     */
    @TargetApi(VERSION_CODES.FROYO)
    public static File getExternalCacheDir(Context context) {
    	if (Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD)
            return context.getExternalCacheDir();
    	else {
	        // Before Froyo we need to construct the external cache dir ourselves
	        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
	        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    	}
    }
 
}
