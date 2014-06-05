package com.mjumel.mystories;

import java.io.File;

import android.app.Application;
import android.os.Environment;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MyStoriesApp extends Application {
	public static String LOG_FILENAME;
	public final static String CACHE_DIR = Environment.getExternalStorageDirectory() + "/.mystories/";
	public static final int CAMERA_REQUEST = 0;
	public static final int GALLERY_PICTURE = 1;
	public static final int SELECT_PICTURE = 2;
	
    @Override
    public void onCreate() {
        super.onCreate();
        
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
	        .cacheInMemory(true)
	        .cacheOnDisc(true)
	        .showImageOnLoading(R.drawable.ic_action_refresh) // resource or drawable
	        .showImageForEmptyUri(R.drawable.ic_action_cancel) // resource or drawable
        	.showImageOnFail(R.drawable.ic_action_cancel) // resource or drawable
	        .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
            .defaultDisplayImageOptions(defaultOptions)
            .writeDebugLogs()
            .discCacheSize(50 * 1024 * 1024)
            .build();
        ImageLoader.getInstance().init(config);
        
        LOG_FILENAME = getString(R.string.log_filename);
        new File(CACHE_DIR).mkdirs();
    }
}
