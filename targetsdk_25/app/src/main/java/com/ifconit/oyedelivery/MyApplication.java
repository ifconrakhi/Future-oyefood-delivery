package com.ifconit.oyedelivery;


import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import java.io.File;
import java.util.HashMap;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

public class MyApplication extends Application {


    private static Context context;


    // The following line should be changed to include the correct property id.
    private static final String PROPERTY_ID = "UA-54114034-5";


    public static int GENERAL_TRACKER = 0;


    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        //ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }



    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
       // Fabric.with(this, new Crashlytics());
        MyApplication.context = getApplicationContext();

//        File cacheDir = StorageUtils.getCacheDirectory(context);
//        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
//                .memoryCacheExtraOptions(480, 800) // default = device screen dimensions
//                .diskCacheExtraOptions(480, 800, null)
//
//
//                .threadPoolSize(3) // default
//                .threadPriority(Thread.NORM_PRIORITY - 2) // default
//                .tasksProcessingOrder(QueueProcessingType.FIFO) // default
//                .denyCacheImageMultipleSizesInMemory()
//                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
//                .memoryCacheSize(2 * 1024 * 1024)
//                .memoryCacheSizePercentage(13) // default
//                .diskCache(new UnlimitedDiskCache(cacheDir)) // default
//                .diskCacheSize(100 * 1024 * 1024)
//                .diskCacheFileCount(100)
//                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
//                .imageDownloader(new BaseImageDownloader(context)) // default
//                        //.imageDecoder(new BaseImageDecoder()) // default
//                .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
//                .writeDebugLogs()
//                .build();
//
//        ImageLoader.getInstance().init(config);



       // UNIVERSAL IMAGE LOADER SETUP
//		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
//				.cacheOnDisc(true).cacheInMemory(true)
//				.imageScaleType(ImageScaleType.EXACTLY)
//				.displayer(new FadeInBitmapDisplayer(300)).build();
//
//		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
//				getApplicationContext())
//				.defaultDisplayImageOptions(defaultOptions)
//				.memoryCache(new WeakMemoryCache())
//				.discCacheSize(100 * 1024 * 1024).build();
//
//		ImageLoader.getInstance().init(config);
		// END - UNIVERSAL IMAGE LOADER SETUP


    }



    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    public static Context getAppContext() {
        return MyApplication.context;
    }
}