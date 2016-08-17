/****************************************************************
*Email: support@lansongtech.com
*  LibPlay.java
*  
*  
*这个程序仅仅是演示版本，仅是功能上的呈现，不保证性能和适用性。如正好满足您的项目，我们深感荣幸。
* 我们有更专业稳定强大的发行版本，期待和您进一步的合作。
*  
*Email: support@lansongtech.com 
****************************************************************/
package com.LanSoSdk.Play;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Surface;

import com.LanSoSdk.Play.Util.HWDecoderUtil;

public class LibPlay extends PlayObject<LibPlay.Event> {
    private static final String TAG = "LibPlay";

    public static class Event extends PlayEvent {
        protected Event(int type) {
            super(type);
        }
    }

    /** Native crash handler */
    private static OnNativeCrashListener sOnNativeCrashListener;

    public interface HardwareAccelerationError {
        void eventHardwareAccelerationError(); 
    }

    /**
     * Create a LibPlay withs options
     *
     * @param options
     */
    public LibPlay() 
    {
    	ArrayList<String> options=getLibOptions();
        boolean setAout = true, setChroma = true;
        if (options != null) 
        {
            for (String option : options) 
            {
                if (option.startsWith("--aout="))
                    setAout = false;
                if (option.startsWith("--androidwindow-chroma"))
                    setChroma = false;
                
                if (!setAout && !setChroma)
                    break;
            }
        }
        if (setAout || setChroma) 
        {
            if (options == null)
                options = new ArrayList<String>();
            
            if (setAout) 
            {
                final HWDecoderUtil.AudioOutput hwAout = HWDecoderUtil.getAudioOutputFromDevice();
                if (hwAout == HWDecoderUtil.AudioOutput.OPENSLES)
                    options.add("--aout=opensles");
                else
                    options.add("--aout=android_audiotrack");
            }
            if (setChroma) 
            {
                options.add("--androidwindow-chroma");
                options.add("RV32");
            }
        }
        nativeNew(options.toArray(new String[options.size()]));
    }

    /**
     * 设置当硬件加速错误是的回调.
     * 
     * @param error
     */
    public void setOnHardwareAccelerationError(HardwareAccelerationError error) {
        nativeSetOnHardwareAccelerationError(error);
    }
    private native void nativeSetOnHardwareAccelerationError(HardwareAccelerationError error);

    
    public native String version();

    @Override
    protected Event onEventNative(int eventType, long arg1, float arg2) {
        return null;
    }

    @Override
    protected void onReleaseNative() {
        nativeRelease();
    }

    public static interface OnNativeCrashListener {
        public void onNativeCrash();
    }

    public static void setOnNativeCrashListener(OnNativeCrashListener l) {
        sOnNativeCrashListener = l;
    }

    private static void onNativeCrash() {
        if (sOnNativeCrashListener != null)
            sOnNativeCrashListener.onNativeCrash();
    }
    private  ArrayList<String> getLibOptions() 
    {
        ArrayList<String> options = new ArrayList<String>(50);
        
        final String subtitlesEncoding ="";
        final boolean frameSkip =false; 
        final boolean verboseMode = true; 
        int deblocking = -1;  //auto delete block

        int networkCaching = 60000;  ///software codec used; 
        
        options.add("--no-audio-time-stretch");  
        options.add("--avcodec-skiploopfilter");
        options.add("" + deblocking);
        options.add("--avcodec-skip-frame");
        options.add(frameSkip ? "2" : "0");
        options.add("--avcodec-skip-idct");
        options.add(frameSkip ? "2" : "0");
        options.add("--subsdec-encoding");
        options.add(subtitlesEncoding);
        options.add("--stats");
        options.add("--network-caching=" + networkCaching);
        
        options.add("--androidwindow-chroma");
        options.add("RV32"); 
        options.add(verboseMode ? "-vvv" : "-vv");
        return options;
    }
    /**
     * Sets the application name. LibPlay passes this as the user agent string
     * when a protocol requires it.
     *
     * @param name human-readable application name, e.g. "FooBar player 1.2.3"
     * @param http HTTP User Agent, e.g. "FooBar/1.2.3 Python/2.6.0"
     */
    public void setUserAgent(String name, String http){
        nativeSetUserAgent(name, http);
    }

    /* JNI */
    private native void nativeNew(String[] options);
    private native void nativeRelease();
    private native void nativeSetUserAgent(String name, String http);


    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            try {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1)
                	System.loadLibrary("nwin1");
                else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR2)
                	System.loadLibrary("nwin2");
                else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1)  //android4.2
                	System.loadLibrary("nwin3");
                else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) //android4.4
                	System.loadLibrary("nwin4");
                else
                	System.loadLibrary("nwin5");  //android5.0
            } catch (Throwable t) {
                Log.w(TAG, "Unable to load the anw library: " + t);
            }

            try {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1)
                    System.loadLibrary("usehwr1");
                else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR2)
                    System.loadLibrary("usehwr2");
                else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    System.loadLibrary("usehwr3");
            } catch (Throwable t) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                    Log.w(TAG, "Unable to load the iomx library: " + t);
            }
        }

        try {
        	System.loadLibrary("lansosdkplay");
        } catch (UnsatisfiedLinkError ule) {
            Log.e(TAG, "Can't load lansosdkplay library: " + ule);
            System.exit(1);
        } catch (SecurityException se) {
            Log.e(TAG, "Encountered a security issue when loading lansosdkplay library: " + se);
            System.exit(1);
        }
    }
}
