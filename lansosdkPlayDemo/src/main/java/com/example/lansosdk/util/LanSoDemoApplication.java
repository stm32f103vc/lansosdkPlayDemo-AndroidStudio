package com.example.lansosdk.util;


import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

public class LanSoDemoApplication extends Application {
    public final static String TAG = "LanSoDemoApplication";
    private static LanSoDemoApplication instance;



    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    /**
     * Called when the overall system is running low on memory
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();

    }

    /**
     * @return the main context of the Application
     */
    public static Context getAppContext()
    {
        return instance;
    }

    /**
     * @return the main resources from the Application
     */
    public static Resources getAppResources()
    {
        return instance.getResources();
    }
}
